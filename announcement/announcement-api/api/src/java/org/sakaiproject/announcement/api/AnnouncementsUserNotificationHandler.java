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
package org.sakaiproject.announcement.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.messaging.api.UserNotificationData;
import org.sakaiproject.messaging.api.AbstractUserNotificationHandler;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.user.api.UserDirectoryService;

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
public class AnnouncementsUserNotificationHandler extends AbstractUserNotificationHandler {

    private static final String ADD_EVENT = AnnouncementService.SECURE_ANNC_ADD;
    private static final String UPDATE_EVENT = AnnouncementService.EVENT_ANNC_UPDATE_AVAILABILITY;

    @Resource
    private AnnouncementService announcementService;

    @Resource
    private EntityManager entityManager;

    @Resource
    private ServerConfigurationService serverConfigurationService;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    private SessionFactory sessionFactory;

    @Resource
    private SiteService siteService;

    @Resource
    private UserDirectoryService userDirectoryService;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    private PlatformTransactionManager transactionManager;

    @Override
    public List<String> getHandledEvents() {

        return Arrays.asList(AnnouncementService.SECURE_ANNC_ADD,
                AnnouncementService.EVENT_ANNC_UPDATE_AVAILABILITY,
                AnnouncementService.SECURE_ANNC_REMOVE_OWN, AnnouncementService.SECURE_ANNC_REMOVE_ANY,
                AnnouncementService.EVENT_AVAILABLE_ANNC,
                AnnouncementService.EVENT_MOTD_NEW);
    }

    @Override
    public Optional<List<UserNotificationData>> handleEvent(Event e) {

        String from = e.getUserId();

        String ref = e.getResource();
        String[] pathParts = ref.split("/");

        String siteId = pathParts[3];

        // We ignore this event as it will be repeated with an "motd.new" event
        if (e.getEvent().equals(AnnouncementService.SECURE_ANNC_ADD) && ref.contains("motd")) {
            return Optional.of(List.of());
        }

        SecurityAdvisor sa = unlock(new String[] {AnnouncementService.SECURE_ANNC_READ, AnnouncementService.SECURE_ANNC_READ_DRAFT});
        AnnouncementMessage message = null;
        try {
            message = (AnnouncementMessage) announcementService.getMessage(entityManager.newReference(ref));
        } catch (Exception ex) {
            log.debug("No announcement with id {}", ref);
        }

        boolean isDraft = message.getHeader().getDraft();
        boolean releasedInFuture = Instant.now().isBefore(message.getHeader().getInstant());
        // TODO: the following code could be simplified. Lots of try catches.
        try {
            // If the announcement has just been hidden or removed, remove any existing alerts for it
            // Also if it has been saved as draft or with release date in the future
            if ((AnnouncementService.SECURE_ANNC_REMOVE_OWN.equals(e.getEvent()) || AnnouncementService.SECURE_ANNC_REMOVE_ANY.equals(e.getEvent()))
                        || (UPDATE_EVENT.equals(e.getEvent()) && (isDraft || releasedInFuture))) {
                try {
                    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

                    transactionTemplate.execute(new TransactionCallbackWithoutResult() {

                        protected void doInTransactionWithoutResult(TransactionStatus status) {

                            // Grab the alerts we'll be deleting. We'll need to clear the count caches
                            // for the recipients
                            final List<UserNotification> alerts
                                = sessionFactory.getCurrentSession().createCriteria(UserNotification.class)
                                    .add(Restrictions.or(Restrictions.eq("event", ADD_EVENT), Restrictions.eq("event", UPDATE_EVENT)))
                                    .add(Restrictions.eq("ref", ref)).list();


                            // Remove notifications related to the same announcement, whether they are 'annc.new' or 'annc.revise.availability'
                            sessionFactory.getCurrentSession().createQuery("delete UserNotification where (event = :newEvent or event = :reviseEvent) and ref = :ref")
                                .setString("newEvent", ADD_EVENT)
                                .setString("reviseEvent", UPDATE_EVENT)
                                .setString("ref", ref).executeUpdate();
                        }
                    });
                } catch (Exception e1) {
                    log.error("Failed to delete user notification add announcement event", e1);
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

                    List<UserNotificationData> bhEvents = new ArrayList<>();
                    Set<String> usersList = new HashSet<>();

                    if (siteId.equals(SiteService.ADMIN_SITE_ID) && ref.contains("motd")) {
                        usersList = userDirectoryService.getUsers().stream().map(u -> u.getId()).collect(Collectors.toSet());
                    } else if (message.getHeader().getGroups().isEmpty()) {
                        // Get all the members of the site with read ability if the announcement is not for groups
                        usersList = site.getUsersIsAllowed(AnnouncementService.SECURE_ANNC_READ);
                    } else {
                        // Otherwise get the members of the groups
                        for (String group : message.getHeader().getGroups()) {
                            usersList.addAll(site.getGroup(group).getUsersIsAllowed(AnnouncementService.SECURE_ANNC_READ));
                        }
                    }

                    for (String  to : usersList) {
                        if (!from.equals(to) && !securityService.isSuperUser(to)) {
                            bhEvents.add(new UserNotificationData(from, to, siteId, title, url));
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
