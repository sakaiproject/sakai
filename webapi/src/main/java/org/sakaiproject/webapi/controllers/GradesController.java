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
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.GradeDefinition;
import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.grading.api.GradingAuthz;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.samigo.api.SamigoReferenceReckoner;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.webapi.beans.GradebookItemRestBean;
import org.sakaiproject.webapi.beans.GradebookRestBean;
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

@Slf4j
@RestController
public class GradesController extends AbstractSakaiApiController {

    @Resource
    private AuthzGroupService authzGroupService;

    @Resource
    private EntityManager entityManager;

    @Resource(name = "org.sakaiproject.grading.api.GradingService")
    private org.sakaiproject.grading.api.GradingService gradingService;

    @Resource
    private SecurityService securityService;

    @Resource
    private UserDirectoryService userDirectoryService;

    private final Function<String, List<GradeRestBean>> gradeDataSupplierForSite = siteId -> {

        List<Assignment> assignments = gradingService.getViewableAssignmentsForCurrentUser(siteId, siteId, SortType.SORT_BY_NONE);

        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).collect(Collectors.toList());

        // no need to continue if the site doesn't have gradebook items
        if (assignmentIds.isEmpty()) return Collections.emptyList();

        // collect site information
        return siteService.getOptionalSite(siteId).map(site -> {

            String userId = checkSakaiSession().getUserId();
            boolean canGrade = securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, site.getReference())
              || securityService.unlock(AssignmentServiceConstants.SECURE_GRADE_ASSIGNMENT_SUBMISSION, site.getReference())
              || securityService.unlock(SamigoConstants.AUTHZ_GRADE_ASSESSMENT_ANY, site.getReference());

            List<String> siteUserIds = canGrade ? getGradableUsers(site) : List.of(userId);

            Map<Long, List<GradeDefinition>> gradeDefinitions = gradingService.getGradesWithoutCommentsForStudentsForItems(siteId, siteId, assignmentIds, siteUserIds);

            List<GradeRestBean> beans = new ArrayList<>();
            // collect information for each gradebook item
            for (Assignment a : assignments) {
                GradeRestBean bean = new GradeRestBean(a);
                bean.setSiteTitle(site.getTitle());
                bean.setCanGrade(canGrade);

                // collect information for internal gb item
                List<GradeDefinition> gd = gradeDefinitions.get(a.getId());

                String reference = a.getExternallyMaintained() ? a.getExternalId() : "";

                // Samigo only sets the assessment id as the external id, not an Entity reference
                if (StringUtils.equals(a.getExternalAppName(), SamigoConstants.TOOL_ID)) {
                    reference = SamigoReferenceReckoner.reckoner().site(siteId).subtype("p").id(a.getExternalId()).reckon().getReference();
                }

                int totalGradableUserCount = siteUserIds.size();

                if (a.getExternallyMaintained()) {
                    Optional<Entity> optionalEntity = entityManager.getEntity(reference);
                    if (optionalEntity.isEmpty() || optionalEntity.get().getGroupReferences().isEmpty()) continue; // entity is not accessible, so skip

                    Entity entity = optionalEntity.get();
                    totalGradableUserCount = entity.getGroupReferences()
                            .filter(refs -> !refs.isEmpty())
                            .map(refs -> refs.stream()
                                    .mapToInt(g -> {
                                        try {
                                            return getGradableUsers(authzGroupService.getAuthzGroup(g)).size();
                                        } catch (Exception e) {
                                            log.warn("No authz group found for reference {}", g);
                                            return 0;
                                        }
                                    })
                                    .sum())
                            .orElse(siteUserIds.size());
                }

                if (gd == null) {
                    // no grades for this gb assignment yet
                    bean.setScore("");
                    if (canGrade) {
                        bean.setUngraded(totalGradableUserCount);
                    }
                    bean.setNotGradedYet(true);
                } else {
                    if (canGrade) {
                        double total = 0;
                        for (GradeDefinition d : gd) {
                            if (GradeType.POINTS == d.getGradeEntryType()) {
                                String grade = d.getGrade();
                                if (!StringUtils.isBlank(grade)) {
                                    total += Double.parseDouble(grade);
                                }
                            }
                        }
                        bean.setScore(total > 0 ? String.format("%.2f", total / gd.size()) : "");
                        bean.setUngraded(totalGradableUserCount - gd.size());
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
                    Collection<ToolConfiguration> gbs = site.getTools("sakai.gradebookng");
                    for (ToolConfiguration tc : gbs) {
                        url = tc != null ? "/portal/directtool/" + tc.getId() : "";
                        break;
                    }
                }
                bean.setUrl(url);

                // add data to list
                beans.add(bean);
            }
            return beans;
        }).orElse(Collections.emptyList());
    };

    private List<String> getGradableUsers(AuthzGroup gr) {

      return gr.getRoles().stream().filter(r -> {

        return (r.isAllowed(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION)
                  || r.isAllowed(SamigoConstants.CAN_TAKE))
              && !r.isAllowed(GradingAuthz.PERMISSION_GRADE_ALL)
              && !r.isAllowed(GradingAuthz.PERMISSION_GRADE_SECTION);
      })
      .flatMap(r -> gr.getUsersHasRole(r.getId()).stream()).collect(Collectors.toList());
    }

    @GetMapping(value = "/users/me/grades", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List> getUserGrades() {

        Session session = checkSakaiSession();

        List<GradeRestBean> grades = portalService.getPinnedSites(session.getUserId()).stream()
                .flatMap(s -> gradeDataSupplierForSite.apply(s).stream())
                .collect(Collectors.toList());

        return Map.of("grades", grades, "sites", getPinnedSiteList());
    }

    @GetMapping(value = "/sites/{siteId}/grades", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List> getSiteGrades(@PathVariable String siteId) throws UserNotDefinedException {

        checkSakaiSession();

        return Map.of("grades", gradeDataSupplierForSite.apply(siteId));
    }

    @GetMapping(value = "/sites/{siteId}/grading/item-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List> getSiteCategories(@PathVariable String siteId) {

        checkSakaiSession();

        return Map.<String, List>of("categories", gradingService.getCategoryDefinitions(siteId, siteId), "items", gradingService.getAssignments(siteId, siteId, SortType.SORT_BY_NONE));
    }

    @PostMapping(value = "/sites/{siteId}/grades/{gradingItemId}/{userId}")
    public void submitGrade(@PathVariable String siteId, @PathVariable Long gradingItemId, @PathVariable String userId, @RequestBody Map<String, String> body) {

        String grade = body.get("grade");
        String comment = body.get("comment");
        String reference = body.get("reference");

        gradingService.setAssignmentScoreString(siteId, siteId, gradingItemId, userId, grade, "restapi", reference);

        if (StringUtils.isNotBlank(comment)) {
            gradingService.setAssignmentScoreComment(siteId, gradingItemId, userId, comment);
        }
    }

    @GetMapping(value = {"/sites/{siteId}/items/{appName}", "/sites/{siteId}/items/{appName}/{userId}", "/sites/{siteId}/items/{appName}/{userId}/{gbUid}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GradebookRestBean> getSiteItems(@PathVariable String siteId, @PathVariable String appName, @PathVariable Optional<String> gbUid,
        @PathVariable Optional<String> userId) throws UserNotDefinedException {

        checkSakaiSession();

        List<GradebookRestBean> gbWithItems = new ArrayList<>();
        String user = userDirectoryService.getCurrentUser().getId();
        if (userId.isPresent()) {
            user = userId.get();
        }

        boolean isGradebookGroupEnabled = gradingService.isGradebookGroupEnabled(siteId);
        Map<String, String> gradebookGroupMap = new HashMap<>();

        if (isGradebookGroupEnabled && gbUid.isPresent() && userId.isPresent()) {
            try {
                Site site = siteService.getSite(siteId);
                Collection<Group> groupList = site.getGroups();

                Optional<Group> foundedGroup = groupList.stream().filter(group -> group.getId().equals(gbUid.get())).findFirst();

                boolean isInstructor = userId.get().equals(userDirectoryService.getCurrentUser().getId()) && (securityService.isSuperUser() || securityService.unlock("section.role.instructor", site.getReference()));
                List<String> groupIds = site.getGroupsWithMember(userId.get()).stream().map(Group::getId).collect(Collectors.toList());

                if (foundedGroup.isPresent() && (isInstructor || groupIds.contains(foundedGroup.get().getId()))) {
                    gradebookGroupMap.put(gbUid.get(), foundedGroup.get().getTitle());
                }
            } catch (IdUnusedException e) {
                log.error("Error while trying to get gradebooks for site {} : {}", siteId, e.getMessage());
            }
        } else {
            gradebookGroupMap = returnFoundGradebooks(siteId, user);
        }

        for (Map.Entry<String, String> entry : gradebookGroupMap.entrySet()) {
            List<GradebookItemRestBean> gbItems = new ArrayList<>();

            String gradebookUid = entry.getKey();
            String groupTitle = entry.getValue();

            List<Assignment> gradebookAssignments = gradingService.getAssignments(gradebookUid, siteId, SortType.SORT_BY_NONE);

            for (Assignment gAssignment : gradebookAssignments) {
                if (!gAssignment.getExternallyMaintained() || gAssignment.getExternallyMaintained() && gAssignment.getExternalAppName().equals(appName)) {
                    // gradebook item has been associated or not
                    String gaId = gAssignment.getId().toString();
                    // gradebook assignment label
                    String label = gAssignment.getName();

                    GradebookItemRestBean itemDto = new GradebookItemRestBean(gaId, label, false);
                    gbItems.add(itemDto);
                }
            }

            GradebookRestBean gbDto = new GradebookRestBean(gradebookUid, groupTitle, gbItems);
            gbWithItems.add(gbDto);

        }

        return gbWithItems;
    }

    @GetMapping(value = "/sites/{siteId}/categories", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GradebookRestBean> getGroupCategoriesList(@PathVariable String siteId) throws UserNotDefinedException {

        checkSakaiSession();

        List<GradebookRestBean> gbWithItems = new ArrayList<>();

        Map<String, String> gradebookGroupMap = returnFoundGradebooks(siteId, userDirectoryService.getCurrentUser().getId());

        for (Map.Entry<String, String> entry : gradebookGroupMap.entrySet()) {
            List<GradebookItemRestBean> gbItems = new ArrayList<>();

            String gbUid = entry.getKey();
            String groupTitle = entry.getValue();

            if (gradingService.isCategoriesEnabled(gbUid)) {
                List<CategoryDefinition> categoryDefinitionList = gradingService.getCategoryDefinitions(gbUid, siteId);

                for (CategoryDefinition category : categoryDefinitionList) {
                    Long categoryId = category.getId();
                    String categoryName = category.getName();

                    gbItems.add(new GradebookItemRestBean(categoryId.toString(), categoryName, false));
                }
            }

            gbWithItems.add(new GradebookRestBean(gbUid, groupTitle, gbItems));
        }

        return gbWithItems;
    }

    private Map<String, String> returnFoundGradebooks(String siteId, String userId) {

        try {
            List<Gradebook> gradebookList = gradingService.getGradebookGroupInstances(siteId);
            Map<String, String> gradebookGroupMap = new HashMap<>();

            Site site = siteService.getSite(siteId);
            Collection<Group> groupList = site.getGroups();

            // a specific user id is sent in some cases like forums
            boolean isInstructor = userId.equals(userDirectoryService.getCurrentUser().getId()) && (securityService.isSuperUser() || securityService.unlock("section.role.instructor", site.getReference()));
            List<String> groupIds = site.getGroupsWithMember(userId).stream().map(Group::getId).collect(Collectors.toList());

            gradebookList.forEach(gradebook -> {
                String gradebookUid = gradebook.getUid();

                Optional<Group> opGroup = groupList.stream()
                                                .filter(group -> group.getId().equals(gradebookUid))
                                                .findFirst();

                if(opGroup.isPresent() && (isInstructor || groupIds.contains(opGroup.get().getId()))) {
                    gradebookGroupMap.put(gradebookUid, opGroup.get().getTitle());
                }
            });

            return gradebookGroupMap;
        } catch (IdUnusedException e) {
            log.error("Error while trying to get gradebooks for site {} : {}", siteId, e.getMessage());
            return new HashMap<>();
        }
    }

}
