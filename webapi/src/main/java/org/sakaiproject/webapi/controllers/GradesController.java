/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.GradeDefinition;
import org.sakaiproject.grading.api.GradingAuthz;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.samigo.api.SamigoReferenceReckoner;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.webapi.beans.GradableObjectRestBean;
import org.sakaiproject.webapi.beans.GradebookItemRestBean;
import org.sakaiproject.webapi.beans.GradebookRestBean;
import org.sakaiproject.webapi.beans.GradeRestBean;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Setter
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
    private SqlService sqlService;

    private final Function<String, List<GradeRestBean>> gradeDataSupplierForSite = siteId -> {

        if (!gradingService.isGradebookDefined(siteId)) {
            return Collections.emptyList();
        }

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
                            if (Objects.equals(GradingConstants.GRADE_TYPE_POINTS, d.getGradeEntryType())) {
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
    public Map<String, List> getSiteGrades(@PathVariable("siteId") String siteId) throws UserNotDefinedException {

        checkSakaiSession();

        return Map.of("grades", gradeDataSupplierForSite.apply(siteId));
    }

    @GetMapping(value = "/sites/{siteId}/grading/item-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List> getSiteCategories(@PathVariable("siteId") String siteId) {

        checkSakaiSession();

        return Map.<String, List>of("categories", gradingService.getCategoryDefinitions(siteId, siteId), "items", gradingService.getAssignments(siteId, siteId, SortType.SORT_BY_NONE));
    }

    @PostMapping(value = "/sites/{siteId}/grades/{gradingItemId}/{userId}")
    public void submitGrade(@PathVariable("siteId") String siteId, @PathVariable("gradingItemId") Long gradingItemId, @PathVariable("userId") String userId, @RequestBody Map<String, String> body) {

        String grade = body.get("grade");
        String comment = body.get("comment");
        String reference = body.get("reference");

        gradingService.setAssignmentScoreString(siteId, siteId, gradingItemId, userId, grade, "restapi", reference);

        if (StringUtils.isNotBlank(comment)) {
            gradingService.setAssignmentScoreComment(siteId, gradingItemId, userId, comment);
        }
    }

    @GetMapping(value = {"/sites/{siteId}/items/{appName}", "/sites/{siteId}/items/{appName}/{userId}", "/sites/{siteId}/items/{appName}/{userId}/{gbUid}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GradebookRestBean> getSiteItems(@PathVariable("siteId") String siteId, @PathVariable("appName") String appName, @PathVariable(value = "gbUid", required = false) Optional<String> gbUid, @PathVariable(value = "userId", required = false) Optional<String> userId) throws UserNotDefinedException {

        String currentUserId = checkSakaiSession().getUserId();

        List<GradebookRestBean> gbWithItems = new ArrayList<>();
        String user = currentUserId;
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

                boolean isInstructor = userId.get().equals(currentUserId) && (securityService.isSuperUser() || securityService.unlock("section.role.instructor", site.getReference()));
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
    public List<GradebookRestBean> getGroupCategoriesList(@PathVariable("siteId") String siteId) throws UserNotDefinedException {

        String userId = checkSakaiSession().getUserId();

        List<GradebookRestBean> gbWithItems = new ArrayList<>();

        Map<String, String> gradebookGroupMap = returnFoundGradebooks(siteId, userId);

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

        String currentUserId = checkSakaiSession().getUserId();

        try {
            List<Gradebook> gradebookList = gradingService.getGradebookGroupInstances(siteId);
            Map<String, String> gradebookGroupMap = new HashMap<>();

            Site site = siteService.getSite(siteId);
            Collection<Group> groupList = site.getGroups();

            // a specific user id is sent in some cases like forums
            boolean isInstructor = userId.equals(currentUserId) && (securityService.isSuperUser() || securityService.unlock("section.role.instructor", site.getReference()));
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

    // Returns a single gradebook item by its external id (or the last segment of it), 404 if not found
    @GetMapping(value = "/grades/gradable-object", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GradableObjectRestBean> getGradableObject(
        @RequestParam("siteId") String siteId,
        @RequestParam("externalId") String externalId
    ) {

        checkSakaiSession();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Assignment assignment = null;
        try {
            assignment = gradingService.getExternalAssignment(siteId, externalId);
        } catch (Exception e) {
            log.warn("getExternalAssignment failed for siteId=" + siteId + " externalId=" + externalId + ": " + e.getMessage());
        }

        if (assignment != null) {
            String entityId = StringUtils.isNotBlank(assignment.getExternalId()) ? assignment.getExternalId() : externalId;
            String dueDate = assignment.getDueDate() != null
                    ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(assignment.getDueDate())
                    : null;
            String openDate = findSamigoOpenDate(entityId, formatter);
            GradableObjectRestBean bean = new GradableObjectRestBean(
                    siteId, entityId, assignment.getExternalAppName(), assignment.getName(), assignment.getPoints(), dueDate, openDate);
            return new ResponseEntity<>(bean, HttpStatus.OK);
        }

        GradableObjectRestBean fromSql = findGradableObjectByReferenceTail(siteId, externalId, formatter);
        if (fromSql != null) {
            fromSql.setOpenDate(findSamigoOpenDate(fromSql.getEntityId(), formatter));
            return new ResponseEntity<>(fromSql, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Fallback lookup by EXTERNAL_ID or REFERENCE, matched exactly or by their last path segment
    private GradableObjectRestBean findGradableObjectByReferenceTail(String siteId, String externalId, DateTimeFormatter formatter) {

        String query = "SELECT gbo.NAME, gbo.EXTERNAL_ID, gbo.EXTERNAL_APP_NAME, gbo.POINTS_POSSIBLE, gbo.DUE_DATE, gbo.REFERENCE " +
                "FROM GB_GRADABLE_OBJECT_T gbo " +
                "JOIN GB_GRADEBOOK_T gb ON gbo.GRADEBOOK_ID = gb.ID " +
                "WHERE gb.GRADEBOOK_UID = ? " +
                "AND gbo.REMOVED = 0 " +
                // Assignments store a full path in EXTERNAL_ID, so its last segment is matched too
                "AND (gbo.EXTERNAL_ID = ? OR gbo.REFERENCE = ? OR gbo.REFERENCE LIKE CONCAT('%/', ?) OR gbo.EXTERNAL_ID LIKE CONCAT('%/', ?)) " +
                "ORDER BY CASE WHEN gbo.EXTERNAL_ID = ? THEN 0 WHEN gbo.REFERENCE = ? THEN 1 ELSE 2 END, gbo.ID " +
                "LIMIT 1";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = sqlService.borrowConnection();
            conn.setReadOnly(true);
            ps = conn.prepareStatement(query);
            ps.setString(1, siteId);
            ps.setString(2, externalId);
            ps.setString(3, externalId);
            ps.setString(4, externalId);
            ps.setString(5, externalId);
            ps.setString(6, externalId);
            ps.setString(7, externalId);
            rs = ps.executeQuery();

            if (rs.next()) {
                String storedExternalId = rs.getString("EXTERNAL_ID");
                String reference = rs.getString("REFERENCE");
                String entityId = StringUtils.isNotBlank(storedExternalId) ? storedExternalId : reference;
                if (StringUtils.isNotBlank(entityId)) {
                    int index = entityId.lastIndexOf("/");
                    entityId = (index != -1) ? entityId.substring(index + 1) : entityId;
                }
                Timestamp due = rs.getTimestamp("DUE_DATE");
                String dueDate = due != null ? due.toLocalDateTime().format(formatter) : null;
                return new GradableObjectRestBean(
                        siteId, entityId, rs.getString("EXTERNAL_APP_NAME"), rs.getString("NAME"),
                        rs.getDouble("POINTS_POSSIBLE"), dueDate, null);
            }
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                log.error("SQLException in finally block: " + e);
            }
            if (conn != null) {
                sqlService.returnConnection(conn);
            }
        }
    }

    // Samigo open date: the assessment's start date, or its creation date if no start date is set
    private String findSamigoOpenDate(String externalId, DateTimeFormatter formatter) {
        if (externalId == null) {
            return null;
        }
        long publishedAssessmentId;
        try {
            publishedAssessmentId = Long.parseLong(externalId.trim());
        } catch (NumberFormatException e) {
            return null; // not a Samigo published assessment id
        }

        String query = "SELECT ac.STARTDATE, pa.CREATEDDATE " +
                "FROM SAM_PUBLISHEDASSESSMENT_T pa " +
                "JOIN SAM_ASSESSMENTBASE_T ab ON ab.ID = pa.ASSESSMENTID " +
                "JOIN SAM_ASSESSACCESSCONTROL_T ac ON ab.ID = ac.ASSESSMENTID " +
                "WHERE pa.ID = ? AND pa.status = 1";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = sqlService.borrowConnection();
            conn.setReadOnly(true);
            ps = conn.prepareStatement(query);
            ps.setLong(1, publishedAssessmentId);
            rs = ps.executeQuery();
            if (rs.next()) {
                Timestamp open = rs.getTimestamp("STARTDATE");
                if (open == null) {
                    open = rs.getTimestamp("CREATEDDATE");
                }
                return open != null ? open.toLocalDateTime().format(formatter) : null;
            }
            return null;
        } catch (Exception e) {
            log.warn("findSamigoOpenDate failed for externalId=" + externalId + ": " + e.getMessage());
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                log.error("SQLException in finally block: " + e);
            }
            if (conn != null) {
                sqlService.returnConnection(conn);
            }
        }
    }
}
