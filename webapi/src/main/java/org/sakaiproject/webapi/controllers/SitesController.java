/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.messaging.api.UserNotificationTransferBean;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.http.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class SitesController extends AbstractSakaiApiController {

    private static final String COURSE_IMAGE = "course_image";
    private static final String COURSE_IMAGE_FILE = COURSE_IMAGE + ".png";

    @Autowired
    private ContentHostingService contentHostingService;

    @Autowired
    @Qualifier("org.sakaiproject.coursemanagement.api.CourseManagementService")
    private CourseManagementService cmService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    private SiteService siteService;

    @Autowired
    private UserMessagingService userMessagingService;

    @GetMapping(value = "/users/{userId}/sites", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Map<String, Object>>> getSites(@PathVariable String userId, @RequestParam Optional<Boolean> pinned)
        throws UserNotDefinedException {

        checkSakaiSession();

        List<String> pinnedSites = portalService.getPinnedSites();
        List<UserNotificationTransferBean> notifications = userMessagingService.getNotifications();

        return Map.of(
            "terms", cmService.getAcademicSessions().stream().map(as -> {
                    return Map.<String, Object>of("id", as.getEid(), "name", as.getTitle());
                }).collect(Collectors.toList()),
            "sites", siteService.getUserSites().stream().map(s -> {

                    if (pinned.isPresent() && pinned.get().equals(Boolean.TRUE) && !pinnedSites.contains(s.getId())) {
                        return null;
                    }

                    return this.getSiteMap(s, notifications, pinnedSites);
                }).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    @GetMapping(value = "/sites/{siteId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getSite(@PathVariable String siteId) {

        List<UserNotificationTransferBean> notifications = userMessagingService.getNotifications();
        List<String> pinnedSites = portalService.getPinnedSites();

        return siteService.getOptionalSite(siteId).map(s -> ResponseEntity.ok(this.getSiteMap(s, notifications, pinnedSites)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/sites/{siteId}/image", produces = "text/plain")
    public String postImage(HttpServletRequest req, @PathVariable String siteId) throws Exception {

        if (!securityService.unlock(SiteService.SECURE_UPDATE_SITE, siteService.siteReference(siteId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        try {
            Site site = siteService.getSite(siteId);
            String fileUrl = saveSiteImage(req, site, null);
            siteService.save(site);
            return fileUrl;
        } catch (Exception e) {
            log.error("Failed to update image for site {}: {}", siteId, e.toString());
            throw e;
        }
    }

    @PostMapping(value = "/sites/{siteId}/card-config")
    public void postCardConfig(HttpServletRequest req, @PathVariable String siteId, @RequestParam String mode, @RequestParam(required = false) String background, @RequestParam(required = false) String foreground) throws Exception {

        if (!securityService.unlock(SiteService.SECURE_UPDATE_SITE, siteService.siteReference(siteId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        try {
            Site site = siteService.getSite(siteId);
            if ("image".equals(mode)) {
                saveSiteImage(req, site, foreground);
            } else if ("color".equals(mode)) {
                site.getProperties().addProperty(Site.PROP_COURSE_CARD_BACKGROUND_COLOR, background);
                site.getProperties().addProperty(Site.PROP_COURSE_CARD_FOREGROUND_COLOR, foreground);
                site.getProperties().removeProperty(Site.PROP_COURSE_IMAGE_URL);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unrecognized mode: " + mode);
            }
            siteService.save(site);
        } catch (Exception e) {
            log.error("Failed to update card config for site {}: {}", siteId, e.toString());
            throw e;
        }
    }

    private String saveSiteImage(HttpServletRequest req, Site site, String foreground) throws Exception {

        FileItem fi = (FileItem) req.getAttribute("siteImage");

        if (foreground != null) {
            site.getProperties().addProperty(Site.PROP_COURSE_CARD_FOREGROUND_COLOR, foreground);
        }

        if (fi != null) {
            site.getProperties().removeProperty(Site.PROP_COURSE_CARD_BACKGROUND_COLOR);
            String collectionId = contentHostingService.getSiteCollection(site.getId());

            // Ensure we have a collection for this site. If we've not added resources to the
            // site yet, there may be no collection.
            try {
                contentHostingService.checkCollection(collectionId);
            } catch (IdUnusedException idue) {
                contentHostingService.commitCollection(contentHostingService.addCollection(collectionId));
            }

            ContentResourceEdit edit;
            try {
                edit = contentHostingService.editResource(collectionId + COURSE_IMAGE_FILE);
            } catch (IdUnusedException | PermissionException e) {
                edit = contentHostingService.addResource(collectionId, COURSE_IMAGE, ".png", 1);
            }

            boolean committed = false;
            try {
                edit.setContent(fi.get());
                edit.setContentLength(fi.getSize());
                edit.setContentType(fi.getContentType());
                contentHostingService.commitResource(edit, NotificationService.NOTI_NONE);
                committed = true;
                site.getProperties().addProperty(Site.PROP_COURSE_IMAGE_URL, edit.getUrl());
                return edit.getUrl();
            } catch (Exception e) {
                log.warn("Failed to save site image", e);
                if (edit != null && !committed) {
                    contentHostingService.cancelResource(edit);
                }
            }
        }

        return "";
    }

    private Map<String, Object> getSiteMap(Site site, List<UserNotificationTransferBean> notifications, List<String> pinnedSites) {

        List<UserNotificationTransferBean> siteNotifications
                            = notifications.stream().filter(n -> StringUtils.equals(n.siteId, site.getId()))
                                .collect(Collectors.toList());

        Map<String, Object> siteMap = new HashMap<>();
        siteMap.put("siteId", site.getId());
        siteMap.put("title", site.getTitle());
        siteMap.put("canEdit", securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference()));
        // Include short description for sorting/display (trimmed; omit if blank)
        String sd = StringUtils.trimToNull(site.getShortDescription());
        if (sd != null) {
            siteMap.put("shortDescription", sd);
        }
        siteMap.put("url", site.getUrl());
        siteMap.put("pinned", pinnedSites.contains(site.getId()));

        siteMap.put("image", site.getProperties().getProperty(Site.PROP_COURSE_IMAGE_URL));
        siteMap.put("courseCardBackgroundColor", site.getProperties().getProperty(Site.PROP_COURSE_CARD_BACKGROUND_COLOR));
        siteMap.put("courseCardForegroundColor", site.getProperties().getProperty(Site.PROP_COURSE_CARD_FOREGROUND_COLOR));

        siteMap.put("tools", site.getPages().stream().map(sp -> {

                List<ToolConfiguration> tools = sp.getTools();
                if (tools.size() != 1) return null;
                if (tools.get(0).getTool() == null) return null;
                String url = serverConfigurationService.getPortalUrl() + "/site/" + site.getId() + "/tool/" + tools.get(0).getId();
                String commonToolId = tools.get(0).getTool().getId();
                boolean hasAlerts = siteNotifications.stream().anyMatch(sn -> !sn.viewed && StringUtils.equals(sn.tool, commonToolId));
                return Map.of("id", commonToolId, "title", sp.getTitle(), "url", url, "iconClass", "si-" + commonToolId.replace(".", "-"), "hasAlerts", hasAlerts);
            }).filter(Objects::nonNull).collect(Collectors.toList()));

        if (StringUtils.equals(site.getType(), "course")) {
            siteMap.put("course", true);
            siteMap.put("term", site.getProperties().getProperty(Site.PROP_SITE_TERM));
        } else if (StringUtils.equals(site.getType(), "project")) {
            siteMap.put("project", true);
        }

        return siteMap;
    }
}
