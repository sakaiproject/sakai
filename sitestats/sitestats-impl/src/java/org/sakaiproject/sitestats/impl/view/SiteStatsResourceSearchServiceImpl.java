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
package org.sakaiproject.sitestats.impl.view;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Setter;

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
import org.sakaiproject.sitestats.api.view.SiteStatsReportAccessService;
import org.sakaiproject.sitestats.api.view.SiteStatsResourceSearchService;
import org.sakaiproject.sitestats.impl.report.SiteStatsResourceLabels;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;

@Setter
public class SiteStatsResourceSearchServiceImpl implements SiteStatsResourceSearchService {

    private SiteStatsReportAccessService reportAccessService;
    private ContentHostingService contentHostingService;
    private LocaleService localeService;
    private ToolManager toolManager;

    @Override
    public List<ResourceOption> resources(String siteId) {
        reportAccessService.assertCanViewAll(siteId);
        Locale locale = locale(siteId);
        ResourceLoader messages = messages(locale);
        Map<String, String> roots = roots(siteId, messages);
        List<ResourceOption> resources = new ArrayList<>();
        Map<String, String> collectionLabels = new LinkedHashMap<>();
        // Only readable metadata is loaded; resource bodies are never read.
        for (String root : roots.keySet()) {
            for (ContentResource resource : contentHostingService.getAllResources(root)) {
                String id = resource.getId();
                resources.add(new ResourceOption(id, label(resource, messages),
                        location(id, roots, collectionLabels, messages), false));
            }
        }
        Collator collator = Collator.getInstance(locale);
        resources.sort(Comparator.comparing(ResourceOption::label, collator).thenComparing(ResourceOption::id));
        return List.copyOf(resources);
    }

    @Override
    public List<ResourceOption> selected(String siteId, List<String> ids) {
        reportAccessService.assertCanViewAll(siteId);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Locale locale = locale(siteId);
        ResourceLoader messages = messages(locale);
        Map<String, String> roots = roots(siteId, messages);
        Map<String, String> collectionLabels = new LinkedHashMap<>();
        List<ResourceOption> selections = new ArrayList<>();
        for (String id : ids) {
            String genericLabel = genericRootLabel(id, messages);
            if (genericLabel != null) {
                selections.add(new ResourceOption(id, genericLabel, "", true));
                continue;
            }
            if (id == null || roots.keySet().stream().noneMatch(id::startsWith)) {
                selections.add(unavailable(id, messages));
                continue;
            }
            try {
                boolean collection = id.endsWith("/");
                ContentEntity resource = collection ? contentHostingService.getCollection(id)
                        : contentHostingService.getResource(id);
                selections.add(new ResourceOption(id, label(resource, messages),
                        location(id, roots, collectionLabels, messages), collection));
            } catch (IdUnusedException | PermissionException | TypeException unavailable) {
                selections.add(unavailable(id, messages));
            }
        }
        return List.copyOf(selections);
    }

    private Locale locale(String siteId) {
        return localeService.getLocaleForSiteAndUser(siteId, reportAccessService.currentUserId());
    }

    private Map<String, String> roots(String siteId, ResourceLoader messages) {
        Map<String, String> roots = new LinkedHashMap<>();
        roots.put(contentHostingService.getSiteCollection(siteId),
                SiteStatsResourceLabels.rootLabel(StatsManager.RESOURCES_DIR, toolManager, messages));
        roots.put(contentHostingService.getDropboxCollection(siteId),
                SiteStatsResourceLabels.rootLabel(StatsManager.DROPBOX_DIR, toolManager, messages));
        roots.put(ContentHostingService.ATTACHMENTS_COLLECTION + siteId + "/",
                SiteStatsResourceLabels.rootLabel(StatsManager.ATTACHMENTS_DIR, toolManager, messages));
        return roots;
    }

    private String genericRootLabel(String id, ResourceLoader messages) {
        return SiteStatsResourceLabels.rootLabel(id, toolManager, messages);
    }

    private String label(ContentEntity resource, ResourceLoader messages) {
        String name = resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
        return name == null || name.isBlank() ? messages.getString("sitestats_resource_unnamed") : name;
    }

    private String location(String id, Map<String, String> roots, Map<String, String> collectionLabels, ResourceLoader messages) {
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
                    collectionLabels.put(collectionId, label(collection, messages));
                } catch (IdUnusedException | PermissionException | TypeException unavailable) {
                    collectionLabels.put(collectionId, null);
                }
            }
            String collectionLabel = collectionLabels.get(collectionId);
            if (collectionLabel == null) {
                return roots.get(root) + " / " + messages.getString("sitestats_resource_unavailable_location");
            }
            parts.add(collectionLabel);
        }
        return String.join(" / ", parts);
    }

    private ResourceOption unavailable(String id, ResourceLoader messages) {
        return new ResourceOption(id, messages.getString("sitestats_resource_unavailable"), "", id != null && id.endsWith("/"));
    }

    private ResourceLoader messages(Locale locale) {
        return new ResourceLoader("Messages", getClass().getClassLoader()) {
            @Override
            public Locale getLocale() {
                return locale;
            }
        };
    }
}
