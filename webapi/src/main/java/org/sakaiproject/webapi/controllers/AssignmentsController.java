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
import org.sakaiproject.assignment.api.model.Assignment.Access;
import org.sakaiproject.assignment.api.model.Assignment.GradeType;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.webapi.beans.AssignmentRestBean;
import org.sakaiproject.webapi.beans.AssignmentSubmissionRestBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class AssignmentsController extends AbstractSakaiApiController {
    
    private static ResourceLoader assignmentResourceLoader = new ResourceLoader("assignment");

    @Autowired
    SiteService siteService;

    @Autowired
    AssignmentService assignmentService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    UserDirectoryService userDirectoryService;

    @GetMapping(value = "/sites/{siteId}/assignments", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<AssignmentRestBean> getSiteAssignments(@PathVariable String siteId) {

        checkSakaiSession();

        return assignmentService.getAssignmentsForContext(siteId).stream().map(assignment -> {
            AssignmentRestBean bean = new AssignmentRestBean(assignment);
            bean.setStatus(assignmentService.getAssignmentStatus(assignment.getId()));
            bean.setScale(this.getScaleDisplay(assignment.getId()));
            bean.setAccess(this.getAccessDisplay(assignment.getId()));
            String assignmentReference = assignmentService.assignmentReference(siteId, assignment.getId());
            bean.setTotalSubmissions(assignmentService.countSubmissions(assignmentReference, null));
            bean.setNewSubmissions(assignmentService.countSubmissions(assignmentReference, false));
            return bean;
        }).collect(Collectors.toSet());
    }

    @GetMapping(value = "/sites/{siteId}/assignments/{assignmentId}/submissions", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<AssignmentSubmissionRestBean> getSiteAssignmentSubmissions(@PathVariable String siteId, @PathVariable String assignmentId) {

        String currentUserId = checkSakaiSession().getUserId();

        try {
            Assignment assignment = assignmentService.getAssignment(assignmentId);

            //Do not return Submissions for deleted Assignments
            if (assignment == null || assignment.getDeleted()) { 
                log.warn("User {} requested submissions for non existing or (soft)deleted Assingment {}", currentUserId, assignmentId);
                return Collections.emptySet(); 
            }

            List<String> userIds = assignmentService.getSubmitterIdList(null, "all", null, 
                assignmentService.assignmentReference(siteId, assignment.getId()), siteId);

            return userDirectoryService.getUsers(userIds).stream().map(user -> {
                try {
                    AssignmentSubmission submission = assignmentService.getSubmission(assignment.getId(), user);
                    AssignmentSubmissionRestBean bean = new AssignmentSubmissionRestBean(submission);
                    bean.setUser(user.getDisplayName());
                    bean.setStatusText(assignmentService.getSubmissionStatus(submission.getId(), false));
                    bean.setStatus(assignmentService.getSubmissionCanonicalStatus(submission, false).toString());
                    bean.setGrade(assignmentService.getGradeDisplay(assignmentService.getGradeForSubmitter(submission, user.getId()),
                        assignment.getTypeOfGrade(), assignment.getScaleFactor()));
                    return bean;
                } catch (PermissionException e) {
                    log.error("User {} requested Submission for Assignment {} without nessesary permissions: {}", currentUserId, assignmentId, e.toString());
                }
                return null;
            }).collect(Collectors.toSet());
        } catch (PermissionException e) {
            log.error("User {} requested Assignment {} without nessesary permissions: {}", currentUserId, assignmentId, e.toString());
        } catch (IdUnusedException e) {
            log.error("Assignment with Id {} not found. Requested by {}: {}", assignmentId, currentUserId, e.toString());
        }
        return Collections.emptySet();
    }

    private String getScaleDisplay(String assignmentId) {
        Assignment assignment;
        try {
            assignment = assignmentService.getAssignment(assignmentId);
        } catch (Exception e) {
            log.error("Assignment with id {} could not be retrieved", assignmentId);
            return null;
        }
        GradeType gradeType = assignment.getTypeOfGrade();
        switch(gradeType) {
            case GRADE_TYPE_NONE:
                return assignmentResourceLoader.getString("gen.notset");
            case UNGRADED_GRADE_TYPE:
                return assignmentResourceLoader.getString("gen.nograd");
            case LETTER_GRADE_TYPE:
                return assignmentResourceLoader.getString("letter");
            case SCORE_GRADE_TYPE:
                return String.format("%s %s", assignmentService.getMaxPointGradeDisplay(assignment.getScaleFactor(), assignment.getMaxGradePoint()),
                    assignmentResourceLoader.getString("points"));
            case PASS_FAIL_GRADE_TYPE:
                return assignmentResourceLoader.getString("gen.pf");
            case CHECK_GRADE_TYPE:
                return assignmentResourceLoader.getString("gen.checkmark");
            default:
                log.error("Grade type '{}' unknown to getScaleDisplay() for Assignment {}", gradeType.toString(), assignmentId);
                return null;
        }
    }

    private String getAccessDisplay(String assignmentId) {
        Assignment assignment;
        try {
            assignment = assignmentService.getAssignment(assignmentId);
        } catch (Exception e) {
            log.error("Assignment with id {} could not be retrieved", assignmentId);
            return null;
        }

        Site site;
        try {
            site = siteService.getSite(assignment.getContext());
        } catch (IdUnusedException e) {
            log.error("Site {} not found from assignment {} context: {}", assignment.getContext(), assignment.getId(), e.toString());
            return null;
        }

        Access accessType = assignment.getTypeOfAccess();
        switch(accessType) {
            case SITE:
                return assignmentResourceLoader.getString("gen.viewallgroupssections");
            case GROUP:
                String allGroupString = assignment.getGroups().stream().reduce("", (currentString, groupId) -> {
                    return currentString.concat(", " + site.getGroup(groupId).getTitle());
                });
                allGroupString = allGroupString.substring(2);
                return allGroupString;
            default:
                log.error("Access type '{}' unknown to getAccessDisplay() for Assignment {}", accessType.toString(), assignmentId);
                return null;
        }
    }
}
