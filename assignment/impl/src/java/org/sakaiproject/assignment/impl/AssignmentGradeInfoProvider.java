/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.service.gradebook.shared.ExternalAssignmentProvider;
import org.sakaiproject.service.gradebook.shared.ExternalAssignmentProviderCompat;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssignmentGradeInfoProvider implements ExternalAssignmentProvider, ExternalAssignmentProviderCompat {

    // Sakai Service Beans
    private AssignmentService assignmentService;
    private SiteService siteService;
    private GradebookExternalAssessmentService geaService;
    private AuthzGroupService authzGroupService;
    private EntityManager entityManager;
    private SecurityService securityService;
    private SessionManager sessionManager;

    public void init() {
        log.info("INIT and register AssignmentGradeInfoProvider");
        geaService.registerExternalAssignmentProvider(this);
    }

    public void destroy() {
        log.info("DESTROY and unregister AssignmentGradeInfoProvider");
        geaService.unregisterExternalAssignmentProvider(getAppKey());
    }

    public String getAppKey() {
        return "assignment";
    }

    //NOTE: This is pretty hackish because the AssignmentService
    //      does strenuous checking of current user and group access,
    //      while we want to be able to check for any user.
    private Assignment getAssignment(String assignmentReference) {
        Assignment assignment = null;
        if (assignmentReference != null) {
            Reference aref = entityManager.newReference(assignmentReference);
            SecurityAdvisor accessAssignmentAdvisor = new MySecurityAdvisor(sessionManager.getCurrentSessionUserId(),
                AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, assignmentReference);
            SecurityAdvisor accessGroupsAdvisor = new MySecurityAdvisor(sessionManager.getCurrentSessionUserId(),
                AssignmentServiceConstants.SECURE_ALL_GROUPS, siteService.siteReference(aref.getContext()));
            try {
                securityService.pushAdvisor(accessAssignmentAdvisor);
                securityService.pushAdvisor(accessGroupsAdvisor);
                assignment = assignmentService.getAssignment(aref);
            } catch (IdUnusedException e) {
                log.info("Unexpected IdUnusedException after finding assignment with ID: " + assignmentReference);
            } catch (PermissionException e) {
                log.info("Unexpected Permission Exception while using security advisor "
                        + "for assignment with ID: " + assignmentReference);
            } finally {
                securityService.popAdvisor(accessGroupsAdvisor);
                securityService.popAdvisor(accessAssignmentAdvisor);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Assignment not found with ID: " + assignmentReference);
            }
        }
        return assignment;
    }

    public boolean isAssignmentDefined(String externalAppName, String id) {
        if (!externalAppName.equals(getAppKey()) && !externalAppName.equals(assignmentService.getToolTitle())) {
          return false;
        }
        return getAssignment(id) != null;
    }

    public boolean isAssignmentGrouped(String id) {
        return Assignment.Access.GROUP.equals(getAssignment(id).getTypeOfAccess());
    }

    public boolean isAssignmentVisible(String id, String userId) {
        // This method is more involved than just a call to getAssignment,
        // which checks visibility, because AssignmentService assumes the
        // current user. Here, we do the checks for the specified user.
        boolean visible = false;
        Assignment a = getAssignment(id);
        if (a == null) {
            visible = false;
        } else if (Assignment.Access.GROUP.equals(a.getTypeOfAccess())) {
            ArrayList<String> azgList = new ArrayList<String>((Collection<String>) a.getGroups());
            List<AuthzGroup> matched = authzGroupService.getAuthzUserGroupIds(azgList, userId);
            visible = (matched.size() > 0);
        } else {
            visible = securityService.unlock(userId, AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, assignmentService.createAssignmentEntity(a).getReference());
        }
        return visible;
    }

    public List<String> getExternalAssignmentsForCurrentUser(String gradebookUid) {
        return getAllExternalAssignments(gradebookUid);
    }

    public List<String> getAllExternalAssignments(String gradebookUid) {
        // We check and cast here on the very slim chance that something other than
        // a AssignmentService is registered as the service. If that is the case,
        // we won't have access to the protected method to get unfiltered assignments
        // and the best we can do is return the filtered list, which is exposed on
        // the AssignmentService interface.

        return assignmentService.getAssignmentsForContext(gradebookUid).stream()
                .map(assignmentService::createAssignmentEntity) // get the assignment entity
                .map(Entity::getReference) // return the reference
                .filter(Objects::nonNull) // filter nulls
                .collect(Collectors.toList()); // return the list
    }

    public Map<String, List<String>> getAllExternalAssignments(String gradebookUid, Collection<String> studentIds) {
        Map<String, List<String>> allExternals = new HashMap<String, List<String>>();
        for (String studentId : studentIds) {
            allExternals.put(studentId, new ArrayList<String>());
        }

        Map<Assignment, List<String>> submitters = assignmentService.getSubmittableAssignmentsForContext(gradebookUid);
        for (Assignment assignment : submitters.keySet()) {
            String externalId = assignmentService.createAssignmentEntity(assignment).getReference();
            for (String userId : submitters.get(assignment)) {
                if (allExternals.containsKey(userId)) {
                    allExternals.get(userId).add(externalId);
                }
            }
        }
        return allExternals;
    }

    public void setGradebookExternalAssessmentService(GradebookExternalAssessmentService geaService) {
        this.geaService = geaService;
    }

    public GradebookExternalAssessmentService getGradebookExternalAssessmentService() {
        return geaService;
    }

    public void setAssignmentService(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    public AssignmentService getAssignmentService() {
        return assignmentService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    public AuthzGroupService getAuthzGroupService() {
        return authzGroupService;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

}

