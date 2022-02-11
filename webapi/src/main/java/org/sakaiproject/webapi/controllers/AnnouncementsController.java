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

import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.webapi.beans.AnnouncementRestBean;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.swagger.annotations.ApiOperation;


import lombok.extern.slf4j.Slf4j;

/**
 */
@Slf4j
@RestController
public class AnnouncementsController extends AbstractSakaiApiController {

	@Resource
	private AnnouncementService announcementService;

	@Resource
	private EntityManager entityManager;

	@Resource
	private SecurityService securityService;

	@Resource(name = "org.sakaiproject.component.api.ServerConfigurationService")
	private ServerConfigurationService serverConfigurationService;

	@Resource
	private SiteService siteService;

	@Resource
	private UserDirectoryService userDirectoryService;

    @ApiOperation(value = "Get a particular user's announcements data")
	@GetMapping(value = "/users/{userId}/announcements", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AnnouncementRestBean> getUserAnnouncements(@PathVariable String userId) throws UserNotDefinedException {

        Session session = checkSakaiSession();
        return announcementService.getViewableAnnouncementsForCurrentUser(10).entrySet()
            .stream()
            .map(e -> {

                try {
                    Site site = siteService.getSite(e.getKey());
                    return e.getValue().stream().map(am -> {
                        Optional<String> optionalUrl = entityManager.getUrl(am.getReference(), Entity.UrlType.PORTAL);
                        return new AnnouncementRestBean(site, am, optionalUrl.get());
                    }).collect(Collectors.toList());
                } catch (Exception ex) {
                    return null;
                }
            })
            .flatMap(Collection::stream)
            .sorted(Comparator.comparingLong(AnnouncementRestBean::getDate).reversed())
            .collect(Collectors.toList());
	}

    @ApiOperation(value = "Get a particular site's announcements data")
	@GetMapping(value = "/sites/{siteId}/announcements", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AnnouncementRestBean> getSiteAnnouncements(@PathVariable String siteId) throws UserNotDefinedException {

		Session session = checkSakaiSession();

        try {
            Site site = siteService.getSite(siteId);
            String channelRef = announcementService.channelReference(siteId, "main");
            return ((List<AnnouncementMessage>) announcementService.getMessages(channelRef, null, false, true))
                .stream()
                .map(am -> {
                    Optional<String> optionalUrl = entityManager.getUrl(am.getReference(), Entity.UrlType.PORTAL);
                    return new AnnouncementRestBean(site, am, optionalUrl.get());
                }).collect(Collectors.toList());
        } catch (IdUnusedException idue) {
            log.error("No announcements for id {}", siteId);
        } catch (PermissionException pe) {
            log.warn("The current user does not have permission to get announcements for this site {}", siteId);
        }

        return Collections.EMPTY_LIST;
	}
}
