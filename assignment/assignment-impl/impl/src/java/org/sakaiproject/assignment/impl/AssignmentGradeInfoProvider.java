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
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.service.gradebook.shared.ExternalAssignmentProvider;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;

public class AssignmentGradeInfoProvider implements ExternalAssignmentProvider {

    private Log log = LogFactory.getLog(AssignmentGradeInfoProvider.class);

    // Sakai Service Beans
    private AssignmentService assignmentService;
    private GradebookExternalAssessmentService geaService;
    private AuthzGroupService authzGroupService;
    private SecurityService securityService;

    // This allows us to check assignment existence by ID, regardless of role.
    // It is not used to allow actual access to entities.
    private SecurityAdvisor allowAllAdvisor = new SecurityAdvisor() {
        public SecurityAdvice isAllowed(String userId, String function, String reference) {
            return SecurityAdvice.ALLOWED;
        }
    };

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

    private Assignment getAssignment(String id) {
        Assignment assignment = null;
        try {
            securityService.pushAdvisor(allowAllAdvisor);
            assignment = assignmentService.getAssignment(id);
        } catch (IdUnusedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Assignment not found with ID: " + id);
            }
        } catch (PermissionException e) {
            log.info("Unexpected Permission Exception while using security advisor "
                    + "for assignment with ID: " + id);
        } finally {
            securityService.popAdvisor();
            //securityService.popAdvisor(allowAllAdvisor);
        }
        return assignment;
    }

    public boolean isAssignmentDefined(String id) {
        return getAssignment(id) != null;
    }

    public boolean isAssignmentGrouped(String id) {
        return Assignment.AssignmentAccess.GROUPED.equals(getAssignment(id).getAccess());
    }

    public boolean isAssignmentVisible(String id, String userId) {
        // This method is more involved than just a call to getAssignment,
        // which checks visibility, because AssignmentService assumes the
        // current user. Here, we do the checks for the specified user.
        boolean visible = false;
        Assignment a = getAssignment(id);
        if (a == null) {
            visible = false;
        }
        else if (Assignment.AssignmentAccess.GROUPED.equals(a.getAccess())) {
            ArrayList<String> azgList = new ArrayList<String>( (Collection<String>) a.getGroups());
            List<AuthzGroup> matched = authzGroupService.getAuthzUserGroupIds(azgList, userId);
            visible = (matched.size() > 0);
        }
        else {
            visible = securityService.unlock(userId, AssignmentService.SECURE_ACCESS_ASSIGNMENT, a.getReference());
        }
        return visible;
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

    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    public AuthzGroupService getAuthzGroupService() {
        return authzGroupService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }
}

