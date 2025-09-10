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

import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.webapi.beans.AnnouncementRestBean;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.http.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class AnnouncementsController extends AbstractSakaiApiController {

	@Autowired
	private AnnouncementService announcementService;

	@Autowired
	private EntityManager entityManager;

    @GetMapping(value = "/users/me/announcements", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List> getUserAnnouncements() throws UserNotDefinedException {

        checkSakaiSession();

        Filter filter = announcementService.getMaxAgeInDaysAndAmountFilter(10, 100);

        try {

            List<AnnouncementRestBean> announcements = portalService.getPinnedSites().stream().flatMap(siteId -> {

                    try {
                        Site site = siteService.getSite(siteId);

                        return announcementService.getMessages(announcementService.channelReference(siteId, SiteService.MAIN_CONTAINER), filter, false, false)
                            .stream()
                            .map(am -> {
                                Optional<String> optionalUrl = entityManager.getUrl(am.getReference(), Entity.UrlType.PORTAL);
                                return new AnnouncementRestBean(site, am, optionalUrl.get());
                            });
                    } catch (IdUnusedException idue) {
                        log.warn("Failed to get messages for site {}: {}", siteId, idue.toString());
                        return Stream.<AnnouncementRestBean>empty();
                    } catch (PermissionException pe) {
                        log.warn("No permission to get messages for site id {}", siteId, pe.toString());
                        return Stream.<AnnouncementRestBean>empty();
                    }
                })
                .sorted((a1, a2) -> Long.compare(a2.getDate(), a1.getDate()))
                .collect(Collectors.toList());

            return Map.of("announcements", announcements, "sites", getPinnedSiteList());
        } catch (Exception ex) {
            log.error("Error getting announcements: {}", ex.toString());
        }

        return Collections.EMPTY_MAP;
    }

    @GetMapping(value = "/sites/{siteId}/announcements", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List> getSiteAnnouncements(@PathVariable String siteId) throws UserNotDefinedException {

        checkSakaiSession();

        try {
            Site site = siteService.getSite(siteId);
            String channelRef = announcementService.channelReference(siteId, "main");

            ToolConfiguration placement = site.getToolForCommonId(AnnouncementService.SAKAI_ANNOUNCEMENT_TOOL_ID);
            String mergedChannels = placement.getPlacementConfig().getProperty(AnnouncementService.PORTLET_CONFIG_PARM_MERGED_CHANNELS);

            return Map.of("announcements", announcementService.getChannelMessages(channelRef, null, false, mergedChannels, false, false, siteId, 10)
                .stream()
                .filter(announcementService::isMessageViewable)
                .map(am -> {
                    Optional<String> optionalUrl = entityManager.getUrl(am.getReference(), Entity.UrlType.PORTAL);
                    return new AnnouncementRestBean(site, am, optionalUrl.get());
                }).collect(Collectors.toList()));
        } catch (IdUnusedException idue) {
            log.error("No announcements for id {}", siteId);
        } catch (Exception ex) {
            log.warn("Error getting announcements for this site {}", siteId, ex);
        }

        return Collections.EMPTY_MAP;
    }
}
