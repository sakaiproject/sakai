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
package org.sakaiproject.assignment.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import java.time.Instant;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.hibernate.type.StringType;

import static org.sakaiproject.assignment.api.AssignmentConstants.EVENT_ADD_ASSIGNMENT;
import static org.sakaiproject.assignment.api.AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_ACCESS;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT;
import static org.sakaiproject.assignment.api.AssignmentConstants.EVENT_AVAILABLE_ASSIGNMENT;

import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.messaging.api.UserNotificationData;
import org.sakaiproject.messaging.api.AbstractUserNotificationHandler;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AddAssignmentUserNotificationHandler extends AbstractUserNotificationHandler {

    @Resource
    private AssignmentService assignmentService;

    @Resource
    private AuthzGroupService authzGroupService;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    private PlatformTransactionManager transactionManager;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    private SessionFactory sessionFactory;

    @Resource
    private SiteService siteService;

    @Resource
    private UserDirectoryService userDirectoryService;

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(EVENT_ADD_ASSIGNMENT, EVENT_UPDATE_ASSIGNMENT_ACCESS, EVENT_AVAILABLE_ASSIGNMENT);
    }

    @Override
    public Optional<List<UserNotificationData>> handleEvent(Event e) {

        String from = e.getUserId();

        String ref = e.getResource();
        String[] pathParts = ref.split("/");

        String siteId = pathParts[3];
        String assignmentId = pathParts[pathParts.length - 1];

        try {
            Assignment assignment = assignmentService.getAssignment(assignmentId);
            switch (e.getEvent()) {
                case EVENT_ADD_ASSIGNMENT:
                case EVENT_AVAILABLE_ASSIGNMENT:
                    return bhAlreadyExists(ref) ? Optional.empty() : Optional.of(handleAdd(from, siteId, assignmentId, assignment));
                case EVENT_UPDATE_ASSIGNMENT_ACCESS:
                    return Optional.of(handleUpdateAccess(from, ref, siteId, assignmentId, assignment));
                default:
                    return Optional.empty();
            }
        } catch (Exception ex) {
            log.error("Failed to find either the assignment or the site", ex);
        }

        return Optional.empty();
    }

    private List<UserNotificationData> handleAdd(String from, String siteId, String assignmentId, Assignment assignment) 
        throws Exception {

        List<UserNotificationData> bhEvents = new ArrayList<>();

        Instant openTime = assignment.getOpenDate();
        if (openTime == null || openTime.isBefore(Instant.now()) && !assignment.getDraft()) {
            Site site = siteService.getSite(siteId);
            String title = assignment.getTitle();
            Set<String> groupIds = assignment.getGroups();
            Collection<String> groupsUsers = authzGroupService.getAuthzUsersInGroups(groupIds);
            // Get all the members of the site with read ability
            Set<String> asnUsers = site.getUsersIsAllowed(SECURE_ACCESS_ASSIGNMENT);
            List<User> validUsers = userDirectoryService.getUsers(asnUsers);
            for (User u : validUsers) {
                String to = u.getId();
                //  If this is a grouped assignment, is 'to' in one of the groups?
                if (groupIds.isEmpty() || groupsUsers.contains(to)) {
                    if (!from.equals(to) && !securityService.isSuperUser(to)) {
                        String url = assignmentService.getDeepLink(siteId, assignmentId, to);
                        bhEvents.add(new UserNotificationData(from, to, siteId, title, url, AssignmentConstants.TOOL_ID, false, null));
                    }
                }
            }
        }

        return bhEvents;
    }

    private List<UserNotificationData> handleUpdateAccess(String from, String ref, String siteId, String assignmentId, Assignment assignment)
        throws Exception {

        Site site = siteService.getSite(siteId);
        Set<String> users = site.getUsersIsAllowed(SECURE_ACCESS_ASSIGNMENT);

        // Clean out all the alerts for the site assignments users. We'll be generating new ones shortly.
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {

            sessionFactory.getCurrentSession().createQuery("delete UserNotification where EVENT in :events and REF = :ref and TO_USER in :toUsers")
                .setParameterList("events", new String[] {EVENT_ADD_ASSIGNMENT, EVENT_UPDATE_ASSIGNMENT_ACCESS, EVENT_AVAILABLE_ASSIGNMENT})
                .setParameter("ref", ref, StringType.INSTANCE)
                .setParameterList("toUsers", users).executeUpdate();
            return null;
        });

        List<UserNotificationData> bhEvents = new ArrayList<>();

        if (assignment.getTypeOfAccess() != Assignment.Access.GROUP) {
            // Access has moved from group to site, notify all the site members. This may result in more than one
            // alert for the same assignment, but it keeps the logic and db queries simpler.
            String title = assignment.getTitle();
            List<User> validUsers = userDirectoryService.getUsers(users);
            for (User u : validUsers) {
                String to = u.getId();
                if (!from.equals(to) && !securityService.isSuperUser(to)) {
                    String url = assignmentService.getDeepLink(siteId, assignmentId, to);
                    bhEvents.add(new UserNotificationData(from, to, siteId, title, url, AssignmentConstants.TOOL_ID, false, null));
                }
            }
        } else {
            Set<String> groupIds = assignment.getGroups();
            Collection<String> groupsUsers = authzGroupService.getAuthzUsersInGroups(groupIds);

            // Now fire the alert for all the groups users, just in case a group has been added, with a new set of users
            String title = assignment.getTitle();
            List<User> validUsers = userDirectoryService.getUsers(groupsUsers);
            for (User u : validUsers) {
                String to = u.getId();
                if (!from.equals(to) && !securityService.isSuperUser(to)) {
                    String url = assignmentService.getDeepLink(siteId, assignmentId, to);
                    bhEvents.add(new UserNotificationData(from, to, siteId, title, url, AssignmentConstants.TOOL_ID, false, null));
                }
            }
        }

        return bhEvents;
    }

    private boolean bhAlreadyExists(String ref) {

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return transactionTemplate.execute(status -> {

                Long bhWithRef = (Long) sessionFactory.getCurrentSession()
                    .createQuery("select count(*) from UserNotification where ref = :ref and event = :event")
                    .setParameter("ref", ref, StringType.INSTANCE).setParameter("event", EVENT_UPDATE_ASSIGNMENT_ACCESS, StringType.INSTANCE).uniqueResult();
                return bhWithRef > 0;
            });
    }
}
