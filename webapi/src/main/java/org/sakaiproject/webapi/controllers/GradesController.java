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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.GradeDefinition;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.webapi.beans.GradeRestBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 */
@Slf4j
@RestController
public class GradesController extends AbstractSakaiApiController {

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

                // collect information for internal gb item
                List<GradeDefinition> gd = gradeDefinitions.get(a.getId());

                if (gd == null) {
                    // no grades for this gb assignment yet
                    bean.setScore("");
                    if (isMaintainer) {
                        bean.setUngraded(userIds.size());
                    }
                    bean.setNotGradedYet(true);
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
                        bean.setScore(total > 0 ? String.format("%.2f", total / gd.size()) : "");
                        bean.setUngraded(userIds.size() - gd.size());
                        bean.setNotGradedYet(gd.isEmpty());
                    } else {
                        if (a.getReleased() && !gd.isEmpty()) {
                            bean.setNotGradedYet(false);
                            bean.setScore(StringUtils.trimToEmpty(gd.get(0).getGrade()));
                        } else {
                            bean.setScore("");
                            bean.setNotGradedYet(true);
                        }
                    }
                }

                String url = "";
                if (a.getExternallyMaintained()) {
                    url = entityManager.getUrl(a.getReference(), Entity.UrlType.PORTAL).orElse("");
                }
                if (StringUtils.isBlank(url)) {
                    ToolConfiguration tc = site.getToolForCommonId("sakai.gradebookng");
                    url = tc != null ? "/portal/directtool/" + tc.getId() : "";
                }
                bean.setUrl(url);

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

    @GetMapping(value = "/sites/{siteId}/grading/item-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List> getSiteCategories(@PathVariable String siteId) {

        checkSakaiSession();

        return Map.<String, List>of("categories", gradingService.getCategoryDefinitions(siteId), "items", gradingService.getAssignments(siteId));
    }

    @PostMapping(value = "/sites/{siteId}/grades/{gradingItemId}/{userId}")
    public void submitGrade(@PathVariable String siteId, @PathVariable Long gradingItemId, @PathVariable String userId, @RequestBody Map<String, String> body) {

        String grade = body.get("grade");
        String comment = body.get("comment");
        String reference = body.get("reference");

        gradingService.setAssignmentScoreString(siteId, gradingItemId, userId, grade, "restapi", reference);

        if (StringUtils.isNotBlank(comment)) {
            gradingService.setAssignmentScoreComment(siteId, gradingItemId, userId, comment);
        }
    }
}
