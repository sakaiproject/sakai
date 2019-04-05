/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.portal.beans.bullhornhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.portal.api.BullhornData;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AnnouncementsBullhornHandler extends AbstractBullhornHandler {

    @Inject
    private AnnouncementService announcementService;

    @Inject
    private EntityManager entityManager;

    @Inject
    private ServerConfigurationService serverConfigurationService;

    @Inject
    private SiteService siteService;

    @Override
    public String getHandledEvent() {
        return AnnouncementService.SECURE_ANNC_ADD;
    }

    @Override
    public Optional<List<BullhornData>> handleEvent(Event e, Cache<String, Map> countCache) {

        String from = e.getUserId();

        String ref = e.getResource();
        String[] pathParts = ref.split("/");

        String siteId = pathParts[3];

        SecurityAdvisor sa = unlock(new String[] {AnnouncementService.SECURE_ANNC_READ});
        try {
            AnnouncementMessage message
                = (AnnouncementMessage) announcementService.getMessage(
                                                entityManager.newReference(ref));

            if (announcementService.isMessageViewable(message)) {
                Site site = siteService.getSite(siteId);
                String toolId = site.getToolForCommonId("sakai.announcements").getId();
                String url = serverConfigurationService.getPortalUrl() + "/directtool/" + toolId
                                    + "?itemReference=" + ref + "&sakai_action=doShowmetadata";

                // In this case title = announcement subject
                String title
                    = ((AnnouncementMessageHeader) message.getHeader()).getSubject();

                List<BullhornData> bhEvents = new ArrayList<>();

                // Get all the members of the site with read ability
                for (String  to : site.getUsersIsAllowed(AnnouncementService.SECURE_ANNC_READ)) {
                    if (!from.equals(to) && !securityService.isSuperUser(to)) {
                        bhEvents.add(new BullhornData(from, to, siteId, title, url, false));
                        countCache.remove(to);
                    }
                }
                return Optional.of(bhEvents);
            }
        } catch (Exception ex) {
            log.error("No site with id '" + siteId + "'", ex);
        } finally {
            lock(sa);
        }

        return Optional.empty();
    }
}
