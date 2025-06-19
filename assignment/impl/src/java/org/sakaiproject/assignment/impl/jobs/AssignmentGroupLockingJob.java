/**
 * Copyright (c) 2003-2024 The Apereo Foundation
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
package org.sakaiproject.assignment.impl.jobs;

import java.time.Instant;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Job to lock assignment groups when they reach their open date.
 * This job runs periodically and checks for assignments that are about to open
 * and locks their associated groups to prevent membership changes.
 */
@Slf4j
public class AssignmentGroupLockingJob implements Job {

    @Setter private AssignmentService assignmentService;
    @Setter private AuthzGroupService authzGroupService;
    @Setter private SessionManager sessionManager;
    @Setter private SiteService siteService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("AssignmentGroupLockingJob started");
        
        // Set up the admin user
        Session session = sessionManager.getCurrentSession();
        String currentUserId = session.getUserId();
        session.setUserId("admin");
        session.setUserEid("admin");
        
        try {
            // Get all assignments that have not been locked yet but will open soon
            // Using a 5-minute buffer to ensure we don't miss assignments
            Instant now = Instant.now();
            Instant fiveMinutesFromNow = now.plusSeconds(5 * 60);
            
            List<Site> sites = siteService.getSites(SiteService.SelectionType.ANY, null, null, null, SiteService.SortType.NONE, null);
            
            for (Site site : sites) {
                try {
                    // Skip unpublished sites or softly deleted sites
                    if (!site.isPublished() || site.isSoftlyDeleted()) {
                        continue;
                    }
                    
                    // Get all assignments for this site
                    for (Assignment assignment : assignmentService.getAssignmentsForContext(site.getId())) {
                        // Check if assignment is:
                        // 1. Not a draft
                        // 2. Uses group access
                        // 3. Open date is between now and 5 minutes from now
                        if (!assignment.getDraft() &&
                            assignment.getTypeOfAccess() == Assignment.Access.GROUP &&
                            assignment.getOpenDate().isAfter(now) &&
                            assignment.getOpenDate().isBefore(fiveMinutesFromNow)) {
                            
                            lockAssignmentGroups(assignment, site);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error processing site {} in AssignmentGroupLockingJob: {}", site.getId(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error in AssignmentGroupLockingJob: {}", e.getMessage(), e);
        } finally {
            // Restore original user
            if (currentUserId != null) {
                session.setUserId(currentUserId);
            } else {
                session.setUserId(null);
            }
            
            log.info("AssignmentGroupLockingJob completed");
        }
    }
    
    /**
     * Locks the groups associated with an assignment
     * 
     * @param assignment The assignment to lock groups for
     * @param site The site that contains the groups
     */
    private void lockAssignmentGroups(Assignment assignment, Site site) {
        log.info("Locking groups for assignment {} in site {}", assignment.getId(), site.getId());
        
        String reference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
        
        for (String groupRef : assignment.getGroups()) {
            try {
                Group assignedGroup = site.getGroup(groupRef.replaceFirst(site.getReference() + "/group/", ""));
                boolean isAccessGroup = assignedGroup != null && 
                                         assignedGroup.getProperties() != null &&
                                         assignedGroup.getProperties().getProperty("lessonbuilder_ref") != null &&
                                         !assignedGroup.getProperties().getProperty("lessonbuilder_ref").isEmpty();
                
                if (!isAccessGroup) {
                    // Lock the group only if it's not an access group (those are handled differently)
                    AuthzGroup group = authzGroupService.getAuthzGroup(groupRef);
                    group.setLockForReference(reference, AuthzGroup.RealmLockMode.ALL);
                    authzGroupService.save(group);
                    log.debug("Locked group {} for assignment {}", groupRef, assignment.getId());
                }
            } catch (GroupNotDefinedException | AuthzPermissionException e) {
                log.warn("Could not lock group {} for assignment {}: {}", groupRef, assignment.getId(), e.getMessage());
            }
        }
    }
}