/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.webapi.beans.AssignmentRestBean;
import org.sakaiproject.webapi.beans.AssignmentSubmissionRestBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class AssignmentsController extends AbstractSakaiApiController {

    @Autowired
    SiteService siteService;

    @Autowired
    AssignmentService assignmentService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    UserDirectoryService userDirectoryService;

    @GetMapping(value = "/sites/{siteId}/assignments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AssignmentRestBean> getSiteAssignments(@PathVariable String siteId) {

        checkSakaiSession();

        return assignmentService.getAssignmentsForContext(siteId).stream().map(assignment -> {
            AssignmentRestBean bean = new AssignmentRestBean(assignment);
            bean.setStatus(assignmentService.getAssignmentStatus(assignment.getId()));
            bean.setScale(assignmentService.getScaleDisplay(assignment.getId()));
            bean.setAccess(assignmentService.getAccessDisplay(assignment.getId()));
            String assignmentReference = assignmentService.assignmentReference(siteId, assignment.getId());
            bean.setTotalSubmissions(assignmentService.countSubmissions(assignmentReference, null));
            bean.setNewSubmissions(assignmentService.countSubmissions(assignmentReference, false));
            return bean;
        }).collect(Collectors.toList());
    }

    @GetMapping(value = "/sites/{siteId}/assignments/{assignmentId}/submissions", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AssignmentSubmissionRestBean> getSiteAssignmentSubmissions(@PathVariable String siteId, @PathVariable String assignmentId) {

        String currentUserId = checkSakaiSession().getUserId();

        try {
            Assignment assignment = assignmentService.getAssignment(assignmentId);

            //Do not return Submissions for deleted Assignments
            if (assignment == null || assignment.getDeleted()) { 
                log.warn("User {} requested submissions for non existing or (soft)deleted Assingment {}", currentUserId, assignmentId);
                return Collections.emptyList(); 
            }

            List<String> userIds = assignmentService.getSubmitterIdList(null, "all", null, 
                assignmentService.assignmentReference(siteId, assignment.getId()), siteId);

            return userDirectoryService.getUsers(userIds).stream().map(user -> {
                try {
                    AssignmentSubmission submission = assignmentService.getSubmission(assignment.getId(), user);
                    AssignmentSubmissionRestBean bean = new AssignmentSubmissionRestBean(submission);
                    bean.setUser(user.getDisplayName());
                    bean.setStatusText(assignmentService.getSubmissionStatus(submission.getId()));
                    bean.setStatus(assignmentService.getSubmissionCanonicalStatus(submission, false).toString());
                    bean.setGrade(assignmentService.getGradeDisplay(assignmentService.getGradeForSubmitter(submission, user.getId()),
                        assignment.getTypeOfGrade(), assignment.getScaleFactor()));
                    return bean;
                } catch (PermissionException e) {
                    log.error("User {} requested Submission for Assignment {} without nessesary permissions: {}", currentUserId, assignmentId, e.toString());
                }
                return null;
            }).collect(Collectors.toList());
        } catch (PermissionException e) {
            log.error("User {} requested Assignment {} without nessesary permissions: {}", currentUserId, assignmentId, e.toString());
        } catch (IdUnusedException e) {
            log.error("Assignment with Id {} not found. Requested by {}: {}", assignmentId, currentUserId, e.toString());
        }
        return Collections.emptyList();
    }
}
