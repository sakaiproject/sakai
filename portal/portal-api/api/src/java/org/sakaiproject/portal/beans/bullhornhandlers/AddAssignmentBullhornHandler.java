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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import java.time.Instant;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.hibernate.SessionFactory;

import static org.sakaiproject.assignment.api.AssignmentConstants.EVENT_ADD_ASSIGNMENT;
import static org.sakaiproject.assignment.api.AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_ACCESS;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT;

import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.portal.api.BullhornData;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AddAssignmentBullhornHandler extends AbstractBullhornHandler {

    @Inject
    private AssignmentService assignmentService;

    @Inject
    private AuthzGroupService authzGroupService;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    private PlatformTransactionManager transactionManager;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    private SessionFactory sessionFactory;

    @Inject
    private SiteService siteService;

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(EVENT_ADD_ASSIGNMENT, EVENT_UPDATE_ASSIGNMENT_ACCESS);
    }

    @Override
    public Optional<List<BullhornData>> handleEvent(Event e, Cache<String, Long> countCache) {

        String from = e.getUserId();

        String ref = e.getResource();
        String[] pathParts = ref.split("/");

        String siteId = pathParts[3];
        String assignmentId = pathParts[pathParts.length - 1];

        try {
            Assignment assignment = assignmentService.getAssignment(assignmentId);
            switch (e.getEvent()) {
                case EVENT_ADD_ASSIGNMENT:
                    return bhAlreadyExists(ref) ? Optional.empty() : Optional.of(handleAdd(from, siteId, assignmentId, assignment, countCache));
                case EVENT_UPDATE_ASSIGNMENT_ACCESS:
                    return Optional.of(handleUpdateAccess(from, ref, siteId, assignmentId, assignment, countCache));
                default:
                    return Optional.empty();
            }
        } catch (Exception ex) {
            log.error("Failed to find either the assignment or the site", ex);
        }

        return Optional.empty();
    }

    private List<BullhornData> handleAdd(String from, String siteId, String assignmentId, Assignment assignment, Cache<String, Long> countCache) 
        throws Exception {

        List<BullhornData> bhEvents = new ArrayList<>();

        Instant openTime = assignment.getOpenDate();
        if (openTime == null || openTime.isBefore(Instant.now()) && !assignment.getDraft()) {
            Site site = siteService.getSite(siteId);
            String title = assignment.getTitle();
            Set<String> groupIds = assignment.getGroups();
            Collection<String> groupsUsers = authzGroupService.getAuthzUsersInGroups(groupIds);

            // Get all the members of the site with read ability
            for (String to : site.getUsersIsAllowed(SECURE_ACCESS_ASSIGNMENT)) {
                //  If this is a grouped assignment, is 'to' in one of the groups?
                if (groupIds.size() == 0 || groupsUsers.contains(to)) {
                    if (!from.equals(to) && !securityService.isSuperUser(to)) {
                        String url = assignmentService.getDeepLink(siteId, assignmentId, to);
                        bhEvents.add(new BullhornData(from, to, siteId, title, url));
                        countCache.remove(to);
                    }
                }
            }
        }

        return bhEvents;
    }

    private List<BullhornData> handleUpdateAccess(String from, String ref, String siteId, String assignmentId, Assignment assignment, Cache<String, Long> countCache)
        throws Exception {

        Site site = siteService.getSite(siteId);
        Set<String> users = site.getUsersIsAllowed(SECURE_ACCESS_ASSIGNMENT);

        // Clean out all the alerts for the site assignments users. We'll be generating new ones shortly.
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {

            sessionFactory.getCurrentSession().createQuery("delete BullhornAlert where EVENT in :events and REF = :ref and TO_USER in :toUsers")
                .setParameterList("events", new String[] {EVENT_ADD_ASSIGNMENT, EVENT_UPDATE_ASSIGNMENT_ACCESS})
                .setString("ref", ref)
                .setParameterList("toUsers", users).executeUpdate();
            return null;
        });

        users.forEach(u -> countCache.remove(u));

        List<BullhornData> bhEvents = new ArrayList<>();

        if (assignment.getTypeOfAccess() != Assignment.Access.GROUP) {
            // Access has moved from group to site, notify all the site members. This may result in more than one
            // alert for the same assignment, but it keeps the logic and db queries simpler.
            String title = assignment.getTitle();
            for (String to : users) {
                if (!from.equals(to) && !securityService.isSuperUser(to)) {
                    String url = assignmentService.getDeepLink(siteId, assignmentId, to);
                    bhEvents.add(new BullhornData(from, to, siteId, title, url));
                    countCache.remove(to);
                }
            }
        } else {
            Set<String> groupIds = assignment.getGroups();
            Collection<String> groupsUsers = authzGroupService.getAuthzUsersInGroups(groupIds);

            // Now fire the alert for all the groups users, just in case a group has been added, with a new set of users
            String title = assignment.getTitle();
            for (String to : groupsUsers) {
                if (!from.equals(to) && !securityService.isSuperUser(to)) {
                    String url = assignmentService.getDeepLink(siteId, assignmentId, to);
                    bhEvents.add(new BullhornData(from, to, siteId, title, url));
                    countCache.remove(to);
                }
            }

            groupsUsers.forEach(u -> countCache.remove(u));
        }

        return bhEvents;
    }

    private boolean bhAlreadyExists(String ref) {

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return transactionTemplate.execute(status -> {

                Long bhWithRef = (Long) sessionFactory.getCurrentSession()
                    .createQuery("select count(*) from BullhornAlert where ref = :ref and event = :event")
                    .setString("ref", ref).setString("event", EVENT_UPDATE_ASSIGNMENT_ACCESS).uniqueResult();
                return bhWithRef > 0;
            });
    }
}
