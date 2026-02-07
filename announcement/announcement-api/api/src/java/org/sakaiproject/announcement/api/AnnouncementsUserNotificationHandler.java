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

import static java.util.function.Predicate.not;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.sakaiproject.authz.api.Member;
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
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import org.hibernate.SessionFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AnnouncementsUserNotificationHandler extends AbstractUserNotificationHandler {

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

    private static final String SELECTED_ROLES_PROPERTY = "selectedRoles";

    private int motdTTLHours;

    @PostConstruct
    private void init() {
        motdTTLHours = serverConfigurationService.getInt("announcement.motd.notification.ttlhours", 24);
    }

    @Override
    public List<String> getHandledEvents() {

        return Arrays.asList(AnnouncementService.SECURE_ANNC_ADD,
                AnnouncementService.EVENT_ANNC_UPDATE_AVAILABILITY,
                AnnouncementService.SECURE_ANNC_REMOVE_OWN, AnnouncementService.SECURE_ANNC_REMOVE_ANY,
                AnnouncementService.EVENT_AVAILABLE_ANNC,
                AnnouncementService.EVENT_MOTD_NEW);
    }

    @Override
    public Optional<List<UserNotificationData>> handleEvent(Event event) {

        List<UserNotificationData> bhEvents = Collections.emptyList();

        final String eventName = event.getEvent();
        final String eventResource = event.getResource();

        // Ignore event that is "annc.new" and its resource contains "motd" as these are handled by a "motd.new" event
        if (AnnouncementService.SECURE_ANNC_ADD.equals(eventName) && eventResource.contains("motd")) {
            return Optional.of(bhEvents);
        }

        // Add a security advisor that allows "annc.read" and "annc.read.drafts" permissions, must be popped in finally
        SecurityAdvisor sa = unlock(new String[] {AnnouncementService.SECURE_ANNC_READ, AnnouncementService.SECURE_ANNC_READ_DRAFT});
        try {
            AnnouncementMessage message = (AnnouncementMessage) announcementService.getMessage(entityManager.newReference(eventResource));

            if (eventName.equals(AnnouncementService.EVENT_MOTD_NEW)) {

                String title = ((AnnouncementMessageHeader) message.getHeader()).getSubject();
                UserNotificationData userNotificationData = new UserNotificationData();
                userNotificationData.setFrom(event.getUserId());
                userNotificationData.setTo(UserNotificationData.BROADCAST);
                userNotificationData.setTitle(title);
                userNotificationData.setBroadcast(true);
                userNotificationData.setTtl(Duration.ofHours(motdTTLHours));
                return Optional.of(List.of(userNotificationData));
            }

            boolean isDraftMessage = false;
            boolean isFutureMessage = false;

            if (message != null) {
                isDraftMessage = message.getHeader().getDraft();
                isFutureMessage = Instant.now().isBefore(message.getHeader().getInstant());
            }

            // remove user notifications for the message that was deleted or those that have been updated and are not visible (draft or future)
            // annc.available.announcement
            if ((AnnouncementService.SECURE_ANNC_REMOVE_OWN.equals(eventName) || AnnouncementService.SECURE_ANNC_REMOVE_ANY.equals(eventName))
                    || (AnnouncementService.EVENT_ANNC_UPDATE_AVAILABILITY.equals(eventName) && (isDraftMessage || isFutureMessage))) {

                // remove user notifications
                try {
                    new TransactionTemplate(transactionManager).executeWithoutResult(transactionStatus -> {
                        Session session = sessionFactory.getCurrentSession();
                        CriteriaBuilder queryBuilder = session.getCriteriaBuilder();

                        // first we query for all those events that will be deleted which will flush any changes in the
                        // persistence context for this table then detach all the notifications from the persistence context
                        CriteriaQuery<UserNotification> eventQuery = queryBuilder.createQuery(UserNotification.class);
                        Root<UserNotification> eventQueryTable = eventQuery.from(UserNotification.class);
                        eventQuery.where(buildWhere(queryBuilder, eventQueryTable, eventResource));
                        session.createQuery(eventQuery).list().forEach(session::detach);

                        // perform the bulk delete operation. NOTE bulk delete operations don't update the persistence context
                        CriteriaDelete<UserNotification> eventDelete = queryBuilder.createCriteriaDelete(UserNotification.class);
                        Root<UserNotification> eventDeleteTable = eventDelete.from(UserNotification.class);
                        eventDelete.where(buildWhere(queryBuilder, eventDeleteTable, eventResource));
                        session.createQuery(eventDelete).executeUpdate();
                    });
                } catch (TransactionException te) {
                    log.warn("Could not remove bullhorn alerts for announcement [{}], {}", eventResource, te.toString());
                }
            } else if (message != null) { // process all other events as long as the message isn't null
                final String eventUserId = event.getUserId();
                final String eventContext = event.getContext();

                if (!isDraftMessage && announcementService.isMessageViewable(message)) {
                    Site site = siteService.getSite(eventContext);
                    ToolConfiguration toolConfig = site.getToolForCommonId("sakai.announcements");
                    // Check for null. We can get events with no tool there.
                    if (toolConfig != null) {
                        String url = serverConfigurationService.getPortalUrl()
                                + "/directtool/"
                                + toolConfig.getId()
                                + "?itemReference="
                                + eventResource
                                + "&sakai_action=doShowmetadata";

                        // In this case title = announcement subject
                        String title = ((AnnouncementMessageHeader) message.getHeader()).getSubject();
                        Set<String> usersToNotify = new HashSet<>();

                        // if the event context is !admin or the event resource contains "motd"
                        Collection<String> groups = message.getHeader().getGroups();
                        if (groups.isEmpty()) {
                            // if the message is not for a group then
                            // get all the members of the site with ability to read the announcement
                            usersToNotify = site.getUsersIsAllowed(AnnouncementService.SECURE_ANNC_READ);
                        } else {
                            // otherwise this is a message for a group(s)
                            for (String group : groups) {
                                // get all the members of the group(s) with ability to read the announcement
                                usersToNotify.addAll(site.getGroup(group).getUsersIsAllowed(AnnouncementService.SECURE_ANNC_READ));
                            }
                        }

                        Set<String> filteredUsersToNotify;
                        // filter users to notify based on their role
                        List<String> selectedRoles = message.getProperties().getPropertyList(SELECTED_ROLES_PROPERTY);
                        if (selectedRoles != null && !selectedRoles.isEmpty()) {
                            filteredUsersToNotify = usersToNotify.stream()
                                    .filter(u -> {
                                        Member member = site.getMember(u);
                                        return member != null && selectedRoles.contains(member.getRole().getId());
                                    })
                                    .collect(Collectors.toSet());
                        } else {
                            filteredUsersToNotify = usersToNotify;
                        }

                        // finally filter out the user who generated the event and superuser types
                        bhEvents = filteredUsersToNotify.stream()
                                .filter(not(eventUserId::equals))
                                .filter(not(securityService::isSuperUser))
                                .map(u -> new UserNotificationData(eventUserId, u, eventContext, title, url, AnnouncementService.SAKAI_ANNOUNCEMENT_TOOL_ID, false, null))
                                .collect(Collectors.toList());
                    }
                }
            } else { // all other events that had a null message come here
                log.debug("The event [{}] was not processed by this handler because message was null and should likely be investigated", event);
            }
        } catch (Exception e) {
            log.warn("Could not handle event [{}], {}", event, e.toString());
        } finally {
            // pop the security advisor
            lock(sa);
        }

        return Optional.of(bhEvents);
    }

    private Predicate buildWhere(CriteriaBuilder queryBuilder, Root<UserNotification> table, String eventResource) {

        return queryBuilder.and(
            queryBuilder.or(
                queryBuilder.equal(table.get("event"), AnnouncementService.SECURE_ANNC_ADD),
                queryBuilder.equal(table.get("event"), AnnouncementService.EVENT_AVAILABLE_ANNC),
                queryBuilder.equal(table.get("event"), AnnouncementService.EVENT_ANNC_UPDATE_AVAILABILITY)),
            queryBuilder.equal(table.get("ref"), eventResource)
        );
    }
}
