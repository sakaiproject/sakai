/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.mvc;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiteStatsResourceSelectionService {

    private static final int PAGE_SIZE = 20;
    private static final int MAX_QUERY_LENGTH = 256;

    private final SiteStatsToolAuthorizationService authorizationService;
    private final ContentHostingService contentHostingService;
    private final LocaleService localeService;
    private final MessageSource messageSource;

    public ResourceSearchPage search(String requestedSiteId, String query, int page) {
        String siteId = authorizationService.reportSite(requestedSiteId);
        String nameQuery = query == null ? "" : query.strip();
        if (page < 0 || nameQuery.length() > MAX_QUERY_LENGTH) {
            throw new InvalidReportConfigurationException("sitestats_report_configuration_invalid");
        }
        if (nameQuery.codePointCount(0, nameQuery.length()) < 2) {
            return new ResourceSearchPage(List.of(), page, false);
        }
        Locale locale = locale(siteId);
        Map<String, String> roots = roots(siteId, locale);
        String foldedQuery = nameQuery.toLowerCase(Locale.ROOT);
        Map<String, ContentResource> matches = new LinkedHashMap<>();
        try {
            // The Kernel applies current read/availability permissions and returns metadata only.
            // Context/type lookup excludes attachments; the full-text index excludes Drop Box.
            for (String root : roots.keySet()) {
                for (ContentResource resource : contentHostingService.getAllResources(root)) {
                    String id = resource.getId();
                    if (!resource.isCollection() && id.startsWith(root)
                            && label(resource, locale).toLowerCase(Locale.ROOT).contains(foldedQuery)) {
                        matches.putIfAbsent(id, resource);
                    }
                }
            }
            Collator collator = Collator.getInstance(locale);
            List<ContentResource> ordered = new ArrayList<>(matches.values());
            ordered.sort(Comparator.comparing((ContentResource resource) -> label(resource, locale), collator)
                    .thenComparing(ContentResource::getId));
            long offset = (long) page * PAGE_SIZE;
            if (offset >= ordered.size()) {
                return new ResourceSearchPage(List.of(), page, false);
            }
            int from = (int) offset;
            int to = Math.min(ordered.size(), from + PAGE_SIZE);
            List<ResourceOption> items = new ArrayList<>();
            Map<String, String> collectionLabels = new LinkedHashMap<>();
            for (ContentResource resource : ordered.subList(from, to)) {
                items.add(new ResourceOption(resource.getId(), label(resource, locale),
                        location(resource.getId(), roots, collectionLabels, locale)));
            }
            return new ResourceSearchPage(List.copyOf(items), page, to < ordered.size());
        } catch (RuntimeException failure) {
            log.warn("Unable to search report resources in site {}", siteId, failure);
            throw failure;
        }
    }

    public List<SelectedResourceOption> selected(String requestedSiteId, List<String> ids) {
        String siteId = authorizationService.reportSite(requestedSiteId);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Locale locale = locale(siteId);
        Map<String, String> roots = roots(siteId, locale);
        Map<String, String> collectionLabels = new LinkedHashMap<>();
        List<SelectedResourceOption> selections = new ArrayList<>();
        for (String id : ids) {
            String genericLabel = genericRootLabel(id, locale);
            if (genericLabel != null) {
                selections.add(new SelectedResourceOption(id, genericLabel, "", false, true));
                continue;
            }
            if (id == null || roots.keySet().stream().noneMatch(id::startsWith)) {
                selections.add(unavailable(id, locale));
                continue;
            }
            try {
                boolean collection = id.endsWith("/");
                ContentEntity resource = collection ? contentHostingService.getCollection(id)
                        : contentHostingService.getResource(id);
                selections.add(new SelectedResourceOption(id, label(resource, locale),
                        location(id, roots, collectionLabels, locale), false, collection));
            } catch (IdUnusedException | PermissionException | TypeException unavailable) {
                selections.add(unavailable(id, locale));
            }
        }
        return List.copyOf(selections);
    }

    public List<SelectedResourceOption> selectedFromForm(String requestedSiteId, String ids) {
        List<String> selections = ids == null ? List.of()
                : ids.lines().map(String::trim).filter(id -> !id.isEmpty()).toList();
        return selected(requestedSiteId, selections);
    }

    private Locale locale(String siteId) {
        return localeService.getLocaleForSiteAndUser(siteId, authorizationService.currentUserId());
    }

    private Map<String, String> roots(String siteId, Locale locale) {
        Map<String, String> roots = new LinkedHashMap<>();
        addRoot(roots, contentHostingService.getSiteCollection(siteId), "sitestats_resource_location_resources", locale);
        addRoot(roots, contentHostingService.getDropboxCollection(siteId), "sitestats_resource_location_dropbox", locale);
        // Existing SiteStats report semantics use the site-specific attachments collection.
        addRoot(roots, ContentHostingService.ATTACHMENTS_COLLECTION + siteId + "/",
                "sitestats_resource_location_attachments", locale);
        return roots;
    }

    private void addRoot(Map<String, String> roots, String root, String labelKey, Locale locale) {
        if (root != null && root.length() > 1 && !List.of(StatsManager.RESOURCES_DIR,
                StatsManager.DROPBOX_DIR, StatsManager.ATTACHMENTS_DIR).contains(root)) {
            roots.put(root, message(labelKey, locale));
        }
    }

    private String genericRootLabel(String id, Locale locale) {
        if (StatsManager.RESOURCES_DIR.equals(id)) {
            return message("sitestats_resource_location_resources", locale);
        }
        if (StatsManager.DROPBOX_DIR.equals(id)) {
            return message("sitestats_resource_location_dropbox", locale);
        }
        if (StatsManager.ATTACHMENTS_DIR.equals(id)) {
            return message("sitestats_resource_location_attachments", locale);
        }
        if ("all".equals(id)) {
            return message("report_what_all", locale);
        }
        return null;
    }

    private String label(ContentEntity resource, Locale locale) {
        String name = resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
        return name == null || name.isBlank() ? message("sitestats_resource_unnamed", locale) : name;
    }

    private String location(String id, Map<String, String> roots, Map<String, String> collectionLabels, Locale locale) {
        String root = roots.keySet().stream().filter(id::startsWith).findFirst().orElseThrow();
        List<String> parts = new ArrayList<>();
        parts.add(roots.get(root));
        int parentEnd = id.lastIndexOf('/', id.endsWith("/") ? id.length() - 2 : id.length() - 1);
        for (int end = id.indexOf('/', root.length()); end >= 0 && end <= parentEnd;
                end = id.indexOf('/', end + 1)) {
            String collectionId = id.substring(0, end + 1);
            if (!collectionLabels.containsKey(collectionId)) {
                try {
                    ContentCollection collection = contentHostingService.getCollection(collectionId);
                    collectionLabels.put(collectionId, label(collection, locale));
                } catch (IdUnusedException | PermissionException | TypeException unavailable) {
                    return roots.get(root) + " / " + message("sitestats_resource_unavailable_location", locale);
                }
            }
            parts.add(collectionLabels.get(collectionId));
        }
        return String.join(" / ", parts);
    }

    private SelectedResourceOption unavailable(String id, Locale locale) {
        return new SelectedResourceOption(id, message("sitestats_resource_unavailable", locale), "", true, id != null && id.endsWith("/"));
    }

    private String message(String key, Locale locale) {
        return messageSource.getMessage(key, null, key, locale);
    }

    @Getter
    @RequiredArgsConstructor
    public static class ResourceOption {
        private final String id;
        private final String label;
        private final String location;
    }

    @Getter
    @RequiredArgsConstructor
    public static class SelectedResourceOption {
        private final String id;
        private final String label;
        private final String location;
        private final boolean unavailable;
        private final boolean legacyCollection;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ResourceSearchPage {
        private final List<ResourceOption> items;
        private final int page;
        private final boolean hasNext;
    }
}
