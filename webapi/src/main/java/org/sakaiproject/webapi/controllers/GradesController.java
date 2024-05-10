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

import org.apache.commons.lang3.StringUtils;

import org.apache.commons.lang3.math.NumberUtils;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.grading.api.GradeDefinition;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.webapi.beans.GradeRestBean;

import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 */
@Slf4j
@RestController
public class GradesController extends AbstractSakaiApiController {

    @Resource
    private AssignmentService assignmentService;

    @Resource
    private EntityManager entityManager;

    @Resource(name = "org.sakaiproject.grading.api.GradingService")
    private org.sakaiproject.grading.api.GradingService gradingService;

    @Resource
    private PortalService portalService;

    @Resource
    private SiteService siteService;

    private final Function<String, List<GradeRestBean>> gradeDataSupplierForSite = (siteId) -> {
        List<org.sakaiproject.grading.api.Assignment> assignments = gradingService.getViewableAssignmentsForCurrentUser(siteId);
        List<Long> assignmentIds = assignments.stream().map(org.sakaiproject.grading.api.Assignment::getId).collect(Collectors.toList());

        // no need to continue if the site doesn't have gradebook items
        if (assignmentIds.isEmpty()) return Collections.emptyList();

        // collect site information
        return siteService.getOptionalSite(siteId).map(site -> {
            String userId = checkSakaiSession().getUserId();
            Role role = site.getUserRole(userId);
            boolean isMaintainer = StringUtils.equalsIgnoreCase(site.getMaintainRole(), role.getId());

            List<String> userIds = isMaintainer
                    ? site.getRoles().stream()
                            .map(Role::getId)
                            .filter(r -> !site.getMaintainRole().equals(r))
                            .flatMap(r -> site.getUsersHasRole(r).stream())
                            .collect(Collectors.toList())
                    : List.of(userId);

            Map<Long, List<GradeDefinition>> gradeDefinitions = gradingService.getGradesWithoutCommentsForStudentsForItems(siteId, assignmentIds, userIds);

            List<GradeRestBean> beans = new ArrayList<>();
            // collect information for each gradebook item
            for (org.sakaiproject.grading.api.Assignment a : assignments) {
                GradeRestBean bean = new GradeRestBean(a);
                bean.setSiteTitle(site.getTitle());
                bean.setSiteRole(role.getId());

                if (!a.getExternallyMaintained()) {
                    // collect information for internal gb item
                    List<GradeDefinition> gd = gradeDefinitions.get(a.getId());

                    if (gd == null) {
                        // no grades for this gb assignment yet
                        bean.setScore("-");
                        if (isMaintainer) {
                            bean.setUngraded(userIds.size());
                            bean.setNotGradedYet(true);
                        }
                    } else {
                        if (isMaintainer) {
                            double total = 0;
                            for (GradeDefinition d : gd) {
                                if (Objects.equals(GradingConstants.GRADE_TYPE_POINTS, d.getGradeEntryType())) {
                                    String grade = d.getGrade();
                                    if (!StringUtils.isBlank(grade)) {
                                        total += Double.parseDouble(grade);
                                    }
                                }
                            }
                            bean.setScore(total > 0 ? String.format("%.2f", total / gd.size()) : "-");
                            bean.setUngraded(userIds.size() - gd.size());
                            bean.setNotGradedYet(gd.isEmpty());
                        } else {
                            bean.setScore(!gd.isEmpty() ? StringUtils.trimToEmpty(gd.get(0).getGrade()) : "");
                        }
                    }

                    ToolConfiguration tc = site.getToolForCommonId("sakai.gradebookng");
                    String gbUrl = tc != null ? "/portal/directtool/" + tc.getId() : "";
                    bean.setUrl(gbUrl);
                } else {
                    // collect information for external gb item
                    int submitted = 0, graded = 0;
                    double total = 0;
                    switch (a.getExternalAppName()) {
                        case AssignmentConstants.TOOL_ID:
                            bean.setUrl(entityManager.getUrl(a.getExternalId(), Entity.UrlType.PORTAL).orElse(""));
                            if (isMaintainer) {
                                try {
                                    Assignment assignment = assignmentService.getAssignment(entityManager.newReference(a.getExternalId()));
                                    for (AssignmentSubmission as : assignment.getSubmissions()) {
                                        if (as.getUserSubmission()) submitted += 1;
                                        if (as.getGraded()) graded += 1;
                                        if (Assignment.GradeType.SCORE_GRADE_TYPE.equals(assignment.getTypeOfGrade())) {
                                            String score = assignmentService.getGradeDisplay(as.getGrade(), assignment.getTypeOfGrade(), assignment.getScaleFactor());
                                            if (NumberUtils.isParsable(score)) total += Double.parseDouble(score);
                                        }
                                    }
                                } catch (Exception e) {
                                    log.warn("Could not access assignment [{}], {}", a.getExternalId(), e.toString());
                                }
                            } else {
                                try {
                                    AssignmentSubmission submission = assignmentService.getSubmission(a.getExternalId(), userId);
                                    if (submission != null && submission.getGraded() && submission.getGradeReleased()) {
                                        bean.setNotGradedYet(false);
                                        Assignment assignment = submission.getAssignment();
                                        bean.setScore(assignmentService.getGradeDisplay(submission.getGrade(), assignment.getTypeOfGrade(), assignment.getScaleFactor()));
                                    } else {
                                        bean.setNotGradedYet(true);
                                    }
                                } catch (Exception e) {
                                    log.warn("Could not access submission for assignment [{}] user [{}], {}", a.getExternalId(), userId, e.toString());
                                }
                            }
                            break;
                        case SamigoConstants.TOOL_ID:
                            GradingService samigoGradingService = new GradingService();
                            if (isMaintainer) {
                                List<AssessmentGradingData> gradings = samigoGradingService.getAllSubmissions(a.getExternalId());
                                for (AssessmentGradingData grading : gradings) {
                                    if (Objects.equals(AssessmentGradingData.SUBMITTED, grading.getStatus()))
                                        submitted += 1;
                                    // TODO: this is bum way of working out if an instructor has scored a quiz.
                                    if (grading.getFinalScore() != null && grading.getFinalScore() != 0D) {
                                        graded++;
                                        total += grading.getFinalScore();
                                    }
                                }
                            } else {
                                AssessmentGradingData agr = samigoGradingService.getLastSubmittedAssessmentGradingByAgentId(a.getExternalId(), userId, null);
                                if (agr != null && Objects.equals(AssessmentGradingData.SUBMITTED, agr.getStatus()) && agr.getGradedDate() != null) {
                                    bean.setNotGradedYet(false);
                                    bean.setScore(agr.getFinalScore().toString());
                                } else {
                                    bean.setNotGradedYet(true);
                                }
                            }
                            break;
                        default:
                    }
                    if (isMaintainer) {
                        bean.setScore(total > 0 ? String.format("%.2f", total / graded) : "-");
                        bean.setUngraded(submitted - graded);
                    }
                    if (StringUtils.isBlank(bean.getUrl())) bean.setUrl(gradingService.getUrlForAssignment(a));
                }

                // add data to list
                beans.add(bean);
            }
            return beans;
        }).orElse(Collections.emptyList());
    };

    @GetMapping(value = "/users/me/grades", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GradeRestBean> getUserGrades() {

        Session session = checkSakaiSession();
        return portalService.getPinnedSites(session.getUserId()).stream()
                .flatMap(s -> gradeDataSupplierForSite.apply(s).stream())
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/sites/{siteId}/grades", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GradeRestBean> getSiteGrades(@PathVariable String siteId) throws UserNotDefinedException {

        checkSakaiSession();

        return gradeDataSupplierForSite.apply(siteId);
    }

}
