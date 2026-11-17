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
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.grading.api.ExternalAssignmentProvider;
import org.sakaiproject.grading.api.ExternalAssignmentProviderCompat;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class AssignmentGradeInfoProvider implements ExternalAssignmentProvider, ExternalAssignmentProviderCompat {

    private AssignmentService assignmentService;
    private SiteService siteService;
    private GradingService gradingService;
    private AuthzGroupService authzGroupService;
    private EntityManager entityManager;
    private SecurityService securityService;
    private SessionManager sessionManager;

    public void init() {
        log.info("INIT and register AssignmentGradeInfoProvider");
        gradingService.registerExternalAssignmentProvider(this);
    }

    public void destroy() {
        log.info("DESTROY and unregister AssignmentGradeInfoProvider");
        gradingService.unregisterExternalAssignmentProvider(getAppKey());
    }

    @Override
    public boolean isGradebookItemInUse(String gradebookUid, org.sakaiproject.grading.api.Assignment item) {
        org.sakaiproject.site.api.Group group = siteService.findGroup(gradebookUid);
        String siteId = group == null ? gradebookUid : group.getContainingSite().getId();
        try {
            for (String link : new String[] {item.getId().toString(), item.getExternalId(), item.getName()}) {
                if (StringUtils.isNotBlank(link) && !assignmentService.getAssignmentsForGradebookLink(siteId, link).isEmpty()) {
                    return true;
                }
            }
            return false;
        } catch (IdUnusedException | PermissionException e) {
            throw new IllegalStateException("Cannot check Assignment links for Gradebook item " + item.getId(), e);
        }
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
        if (!externalAppName.equals(getAppKey()) && !externalAppName.equals(assignmentService.getToolId())) {
          return false;
        }
        return !getLinkedAssignments(id).isEmpty() || getAssignment(id) != null;
    }

    private List<Assignment> getLinkedAssignments(String id) {
        String context = AssignmentReferenceReckoner.reckoner().reference(id).reckon().getContext();
        try {
            return assignmentService.getAssignmentsForGradebookLink(context, id);
        } catch (IdUnusedException | PermissionException e) {
            throw new IllegalStateException("Cannot find Assignments linked to " + id, e);
        }
    }

    private List<String> getExternalIds(Assignment assignment) {
        String ownReference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
        String links = assignment.getProperties().get(AssignmentConstants.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
        return Stream.concat(Stream.of(ownReference), Arrays.stream(StringUtils.defaultString(links).split(",")))
                .filter(link -> link.startsWith(AssignmentServiceConstants.REFERENCE_ROOT + "/"))
                .distinct().collect(Collectors.toList());
    }

    public boolean isAssignmentGrouped(String id) {
        List<Assignment> linked = getLinkedAssignments(id);
        return linked.isEmpty() ? Assignment.Access.GROUP.equals(getAssignment(id).getTypeOfAccess())
                : linked.stream().allMatch(assignment -> Assignment.Access.GROUP.equals(assignment.getTypeOfAccess()));
    }

    public boolean isAssignmentVisible(String id, String userId) {
        // This method is more involved than just a call to getAssignment,
        // which checks visibility, because AssignmentService assumes the
        // current user. Here, we do the checks for the specified user.
        List<Assignment> linked = getLinkedAssignments(id);
        if (linked.isEmpty()) {
            Assignment assignment = getAssignment(id);
            linked = assignment == null ? Collections.emptyList() : Collections.singletonList(assignment);
        }
        for (Assignment assignment : linked) {
            String reference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
            if (getAssignment(reference) == null) {
                continue;
            }
            if (Assignment.Access.GROUP.equals(assignment.getTypeOfAccess())) {
                if (!authzGroupService.getAuthzUserGroupIds(new ArrayList<>(assignment.getGroups()), userId).isEmpty()) {
                    return true;
                }
            } else if (securityService.unlock(userId, AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, reference)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getExternalAssignmentsForCurrentUser(String gradebookUid) {
        return getAllExternalAssignments(gradebookUid);
    }

    public List<String> getAllExternalAssignments(String gradebookUid) {
        return assignmentService.getAssignmentsForContext(gradebookUid).stream()
                .filter(Objects::nonNull)
                .flatMap(assignment -> getExternalIds(assignment).stream())
                .distinct().collect(Collectors.toList());
    }

    public Map<String, List<String>> getAllExternalAssignments(String gradebookUid, Collection<String> studentIds) {
        Map<String, List<String>> allExternals = new HashMap<String, List<String>>();
        for (String studentId : studentIds) {
            allExternals.put(studentId, new ArrayList<String>());
        }

        Map<Assignment, List<String>> submitters = assignmentService.getSubmittableAssignmentsForContext(gradebookUid);
        for (Assignment assignment : submitters.keySet()) {
            List<String> externalIds = getExternalIds(assignment);
            for (String userId : submitters.get(assignment)) {
                if (allExternals.containsKey(userId)) {
                    allExternals.get(userId).addAll(externalIds);
                }
            }
        }
        return allExternals;
    }
}

