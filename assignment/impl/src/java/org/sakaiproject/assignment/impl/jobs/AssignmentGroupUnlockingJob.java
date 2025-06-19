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
import java.util.Collection;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
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
 * Job to handle unlocking groups for assignments that have had their open date pushed to the future.
 * This will only unlock groups if no submissions have been made to the assignment yet.
 */
@Slf4j
public class AssignmentGroupUnlockingJob implements Job {

    @Setter private AssignmentService assignmentService;
    @Setter private AuthzGroupService authzGroupService;
    @Setter private SessionManager sessionManager;
    @Setter private SiteService siteService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("AssignmentGroupUnlockingJob started");
        
        // Set up the admin user
        Session session = sessionManager.getCurrentSession();
        String currentUserId = session.getUserId();
        session.setUserId("admin");
        session.setUserEid("admin");
        
        try {
            // Get all sites
            List<Site> sites = siteService.getSites(SiteService.SelectionType.ANY, null, null, null, SiteService.SortType.NONE, null);
            
            Instant now = Instant.now();
            
            for (Site site : sites) {
                try {
                    // Skip unpublished sites or softly deleted sites
                    if (!site.isPublished() || site.isSoftlyDeleted()) {
                        continue;
                    }
                    
                    // Process assignments that are not drafts, use group access, and haven't opened yet
                    for (Assignment assignment : assignmentService.getAssignmentsForContext(site.getId())) {
                        if (!assignment.getDraft() && 
                            assignment.getTypeOfAccess() == Assignment.Access.GROUP && 
                            assignment.getOpenDate().isAfter(now)) {
                            
                            // Check if any submissions exist
                            Collection<AssignmentSubmission> submissions = assignmentService.getSubmissions(assignment);
                            if (submissions == null || submissions.isEmpty()) {
                                // No submissions, so it's safe to unlock groups
                                manageGroupLocks(assignment, site, false);
                            } else {
                                log.debug("Not unlocking groups for assignment {} because it already has submissions", assignment.getId());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error processing site {} in AssignmentGroupUnlockingJob: {}", site.getId(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error in AssignmentGroupUnlockingJob: {}", e.getMessage(), e);
        } finally {
            // Restore original user
            if (currentUserId != null) {
                session.setUserId(currentUserId);
            } else {
                session.setUserId(null);
            }
            
            log.info("AssignmentGroupUnlockingJob completed");
        }
    }
    
    /**
     * Manages locks on groups associated with an assignment
     * 
     * @param assignment The assignment to manage group locks for
     * @param site The site that contains the groups
     * @param lock If true, lock the groups; if false, unlock them
     */
    private void manageGroupLocks(Assignment assignment, Site site, boolean lock) {
        log.info("{} groups for assignment {} in site {}", 
                (lock ? "Locking" : "Unlocking"), assignment.getId(), site.getId());
        
        String reference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
        
        for (String groupRef : assignment.getGroups()) {
            try {
                Group assignedGroup = site.getGroup(groupRef.replaceFirst(site.getReference() + "/group/", ""));
                boolean isAccessGroup = assignedGroup != null && 
                                         assignedGroup.getProperties() != null &&
                                         assignedGroup.getProperties().getProperty("lessonbuilder_ref") != null &&
                                         !assignedGroup.getProperties().getProperty("lessonbuilder_ref").isEmpty();
                
                if (!isAccessGroup) {
                    // Only manage locks for non-access groups
                    AuthzGroup group = authzGroupService.getAuthzGroup(groupRef);
                    
                    if (lock) {
                        group.setLockForReference(reference, AuthzGroup.RealmLockMode.ALL);
                    } else {
                        group.setLockForReference(reference, AuthzGroup.RealmLockMode.NONE);
                    }
                    
                    authzGroupService.save(group);
                    log.debug("{} group {} for assignment {}", 
                            (lock ? "Locked" : "Unlocked"), groupRef, assignment.getId());
                }
            } catch (GroupNotDefinedException | AuthzPermissionException e) {
                log.warn("Could not {} group {} for assignment {}: {}", 
                        (lock ? "lock" : "unlock"), groupRef, assignment.getId(), e.getMessage());
            }
        }
    }
}