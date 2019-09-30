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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Resource;
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
import org.sakaiproject.portal.beans.BullhornAlert;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

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

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    private SessionFactory sessionFactory;

    @Inject
    private SiteService siteService;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    private PlatformTransactionManager transactionManager;

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(AnnouncementService.SECURE_ANNC_ADD, AnnouncementService.EVENT_ANNC_UPDATE_AVAILABILITY, AnnouncementService.SECURE_ANNC_REMOVE_OWN, AnnouncementService.SECURE_ANNC_REMOVE_ANY);
    }

    @Override
    public Optional<List<BullhornData>> handleEvent(Event e, Cache<String, Long> countCache) {

        String from = e.getUserId();

        String ref = e.getResource();
        String[] pathParts = ref.split("/");

        String siteId = pathParts[3];

        SecurityAdvisor sa = unlock(new String[] {AnnouncementService.SECURE_ANNC_READ, AnnouncementService.SECURE_ANNC_READ_DRAFT});
        AnnouncementMessage message = null;
        try {
            message = (AnnouncementMessage) announcementService.getMessage(entityManager.newReference(ref));
        } catch (Exception ex) {
            log.debug("No announcement with id {}", ref);
        }
        // TODO: the following code could be simplified. Lots of try catches.
        try {
            // If the announcement has just been hidden or removed, remove any existing alerts for it
            if ((AnnouncementService.SECURE_ANNC_REMOVE_OWN.equals(e.getEvent()) || AnnouncementService.SECURE_ANNC_REMOVE_ANY.equals(e.getEvent()))
                        || (AnnouncementService.EVENT_ANNC_UPDATE_AVAILABILITY.equals(e.getEvent()) && message.getHeader().getDraft())) {
                try {
                    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

                    transactionTemplate.execute(new TransactionCallbackWithoutResult() {

                        protected void doInTransactionWithoutResult(TransactionStatus status) {

                            // Grab the alerts we'll be deleting. We'll need to clear the count caches
                            // for the recipients
                            final List<BullhornAlert> alerts
                                = sessionFactory.getCurrentSession().createCriteria(BullhornAlert.class)
                                    .add(Restrictions.eq("event", AnnouncementService.SECURE_ANNC_ADD))
                                    .add(Restrictions.eq("ref", ref)).list();

                            alerts.forEach(a -> countCache.remove(a.getToUser()));

                            sessionFactory.getCurrentSession().createQuery("delete BullhornAlert where event = :event and ref = :ref")
                                .setString("event", AnnouncementService.SECURE_ANNC_ADD)
                                .setString("ref", ref).executeUpdate();
                        }
                    });
                } catch (Exception e1) {
                    log.error("Failed to delete bullhorn add announcement event", e1);
                }
                return Optional.empty();
            }

            if (!message.getHeader().getDraft() && announcementService.isMessageViewable(message)) {
                Site site = siteService.getSite(siteId);
                ToolConfiguration tc = site.getToolForCommonId("sakai.announcements");
                // Check for null. We can get events with no tool there.
                if (tc != null) {
                    String url = serverConfigurationService.getPortalUrl() + "/directtool/" + tc.getId()
                                        + "?itemReference=" + ref + "&sakai_action=doShowmetadata";

                    // In this case title = announcement subject
                    String title
                        = ((AnnouncementMessageHeader) message.getHeader()).getSubject();

                    List<BullhornData> bhEvents = new ArrayList<>();
                    Set<String> usersList = new HashSet<>();

                    if (message.getHeader().getGroups().isEmpty()) {
                        // Get all the members of the site with read ability if the announcement is not for groups
                        usersList = site.getUsersIsAllowed(AnnouncementService.SECURE_ANNC_READ);
                    }
                    else {
                        // Otherwise get the members of the groups
                        for (String group : message.getHeader().getGroups()) {
                            usersList.addAll(site.getGroup(group).getUsersIsAllowed(AnnouncementService.SECURE_ANNC_READ));
                        }
                    }

                    for (String  to : usersList) {
                        if (!from.equals(to) && !securityService.isSuperUser(to)) {
                            bhEvents.add(new BullhornData(from, to, siteId, title, url));
                            countCache.remove(to);
                        }
                    }
                    return Optional.of(bhEvents);
                }
            }
        } catch (Exception ex) {
            log.error("No site with id '" + siteId + "'", ex);
        } finally {
            lock(sa);
        }

        return Optional.empty();
    }
}
