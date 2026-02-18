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

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.messaging.api.UserNotificationTransferBean;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.http.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class SitesController extends AbstractSakaiApiController {

	@Autowired
	@Qualifier("org.sakaiproject.coursemanagement.api.CourseManagementService")
	private CourseManagementService cmService;

	@Autowired
	private ServerConfigurationService serverConfigurationService;

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

                    List<UserNotificationTransferBean> siteNotifications = notifications.stream().filter(n -> StringUtils.equals(n.siteId, s.getId())).collect(Collectors.toList());

                    Map<String, Object> site = new HashMap<>();
                    site.put("siteId", s.getId());
                    site.put("title", s.getTitle());
                    // Include short description for sorting/display (trimmed; omit if blank)
                    String sd = StringUtils.trimToNull(s.getShortDescription());
                    if (sd != null) {
                        site.put("shortDescription", sd);
                    }
                    site.put("url", s.getUrl());
                    site.put("pinned", pinnedSites.contains(s.getId()));

                    site.put("image", s.getProperties().getProperty(Site.PROP_COURSE_IMAGE_URL));

                    site.put("tools", s.getPages().stream().map(sp -> {

                            List<ToolConfiguration> tools = sp.getTools();
                            if (tools.size() != 1) return null;
                            if (tools.get(0).getTool() == null) return null;
                            String url = serverConfigurationService.getPortalUrl() + "/site/" + s.getId() + "/tool/" + tools.get(0).getId();
                            String commonToolId = tools.get(0).getTool().getId();
                            boolean hasAlerts = siteNotifications.stream().anyMatch(sn -> !sn.viewed && StringUtils.equals(sn.tool, commonToolId));
                            return Map.of("id", commonToolId, "title", sp.getTitle(), "url", url, "iconClass", "si-" + commonToolId.replace(".", "-"), "hasAlerts", hasAlerts);
                        }).filter(Objects::nonNull).collect(Collectors.toList()));

                    if (StringUtils.equals(s.getType(), "course")) {
                        site.put("course", true);
                        site.put("term", s.getProperties().getProperty(Site.PROP_SITE_TERM));
                    } else if (StringUtils.equals(s.getType(), "project")) {
                        site.put("project", true);
                    }
                    return site;
                }).filter(Objects::nonNull).collect(Collectors.toList()));
	}
}
