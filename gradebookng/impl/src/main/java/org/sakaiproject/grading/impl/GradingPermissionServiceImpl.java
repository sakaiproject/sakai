/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.grading.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.grading.api.GraderPermission;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.GradingPermissionService;
import org.sakaiproject.grading.api.GradingPersistenceManager;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.PermissionDefinition;
import org.sakaiproject.grading.api.model.Category;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.grading.api.model.Permission;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.tool.api.SessionManager;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GradingPermissionServiceImpl implements GradingPermissionService {

    @Autowired private GradingPersistenceManager gradingPersistenceManager;
    @Autowired protected SectionAwareness sectionAwareness;
    @Autowired private SessionManager sessionManager;

    public List<Long> getCategoriesForUser(Long gradebookId, String userId, List<Long> categoryIdList) throws IllegalArgumentException {

        if (gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getCategoriesForUser");
        }

        List<Permission> anyCategoryPermission = getPermissionsForUserAnyCategory(gradebookId, userId);
        if (anyCategoryPermission.size() > 0 ) {
            return categoryIdList;
        } else {
            List<Long> returnCatIds = new ArrayList<>();
            for (Permission perm : getPermissionsForUserForCategory(gradebookId, userId, categoryIdList)) {
                if (perm != null && !returnCatIds.contains(perm.getCategoryId())) {
                    returnCatIds.add(perm.getCategoryId());
                }
            }

            return returnCatIds;
        }
    }

    public List<Long> getCategoriesForUserForStudentView(Long gradebookId, String userId, String studentId, List<Long> categoriesIds, List<String> sectionIds) throws IllegalArgumentException {

        if (gradebookId == null || userId == null || studentId == null) {
            throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getCategoriesForUser");
        }

        List<Long> returnCategoryList = new ArrayList<>();
        if (categoriesIds == null || categoriesIds.isEmpty()) {
            return returnCategoryList;
        }

        List<Permission> graderPermissions = getPermissionsForUser(gradebookId, userId);
        if (graderPermissions == null || graderPermissions.isEmpty()) {
            return returnCategoryList;
        }

        List<String> studentSections = new ArrayList<>();

        if (sectionIds != null) {
            for (String sectionId : sectionIds) {
                if (sectionId != null && sectionAwareness.isSectionMemberInRole(sectionId, studentId, Role.STUDENT)) {
                    studentSections.add(sectionId);
                }
            }
        }

        for (Permission perm : graderPermissions) {
            String sectionId = perm.getGroupId();
            if (studentSections.contains(sectionId) || sectionId == null) {
                Long catId = perm.getCategoryId();
                if (catId == null) {
                    return returnCategoryList;
                } else {
                    returnCategoryList.add(catId);
                }
            }
        }

        return returnCategoryList;
    }

    public boolean getPermissionForUserForAllAssignment(Long gradebookId, String userId) throws IllegalArgumentException {

        if (gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getPermissionForUserForAllAssignment");
        }

        return getPermissionsForUserAnyCategory(gradebookId, userId).size() > 0;
    }

    public boolean getPermissionForUserForAllAssignmentForStudent(Long gradebookId, String userId, String studentId, List<String> sectionIds) throws IllegalArgumentException {

        if (gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getPermissionForUserForAllAssignment");
        }

        List<Permission> graderPermissions = this.getPermissionsForUser(gradebookId, userId);
        if (graderPermissions == null || graderPermissions.isEmpty()) {
            return false;
        }

        for (Permission perm : graderPermissions) {
            String sectionId = perm.getGroupId();
            if (sectionId == null || (sectionIds.contains(sectionId) && sectionAwareness.isSectionMemberInRole(sectionId, studentId, Role.STUDENT))) {
                if (perm.getCategoryId() == null) {
                    return true;
                }
            }
        }

        return false;
    }

    private void addToStudentMap(Map<String, String> studentMap, Map<String, String> studentMapForGroups) {

        for (Entry<String, String> entry : studentMapForGroups.entrySet()) {
            String key = entry.getKey();
            if ((studentMap.containsKey(key) && studentMap.get(key).equalsIgnoreCase(GradingConstants.viewPermission))
                    || !studentMap.containsKey(key)) {
                studentMap.put(key, entry.getValue());
            }
        }
    }

    public Map<String, String> getStudentsForItem(Long gradebookId, String userId, List<String> studentIds, Integer cateType, Long categoryId, List<CourseSection> courseSections)
        throws IllegalArgumentException {

        if (gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getStudentsForItem");
        }

        if (!Objects.equals(cateType, GradingConstants.CATEGORY_TYPE_ONLY_CATEGORY)
                && !Objects.equals(cateType, GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY)
                && !Objects.equals(cateType, GradingConstants.CATEGORY_TYPE_NO_CATEGORY)) {
            throw new IllegalArgumentException("Invalid category type in GradebookPermissionServiceImpl.getStudentsForItem");
        }

        if (studentIds != null) {
            Map<String, List<String>> sectionIdStudentIdsMap = getSectionIdStudentIdsMap(courseSections, studentIds);
            if (Objects.equals(cateType, GradingConstants.CATEGORY_TYPE_NO_CATEGORY)) {
                List<Permission> perms = getPermissionsForUserAnyGroup(gradebookId, userId);

                Map<String, String> studentMap = new HashMap<>();
                if (!perms.isEmpty()) {
                    boolean view = false;
                    boolean grade = false;
                    for (Permission perm : perms) {
                        if (perm != null && perm.getFunctionName().equalsIgnoreCase(GradingConstants.gradePermission)) {
                            grade = true;
                            break;
                        }
                        if (perm != null && perm.getFunctionName().equalsIgnoreCase(GradingConstants.viewPermission)) {
                            view = true;
                        }
                    }
                    for (String studentId : studentIds) {
                        if (grade) {
                            studentMap.put(studentId, GradingConstants.gradePermission);
                        } else if (view) {
                            studentMap.put(studentId, GradingConstants.viewPermission);
                        }
                    }
                }

                perms = getPermissionsForUser(gradebookId, userId);
                Map<String, String> studentMapForGroups = filterPermissionForGrader(perms, studentIds, sectionIdStudentIdsMap);
                addToStudentMap(studentMap, studentMapForGroups);

                return studentMap;
            } else {
                List<Long> cateList = new ArrayList<>();
                cateList.add(categoryId);
                List<Permission> perms = getPermissionsForUserAnyGroupForCategory(gradebookId, userId, cateList);

                Map<String, String> studentMap = new HashMap<>();
                if (!perms.isEmpty()) {
                    boolean view = false;
                    boolean grade = false;
                    for (Permission perm : perms) {
                        if (perm != null && perm.getFunctionName().equalsIgnoreCase(GradingConstants.gradePermission)) {
                            grade = true;
                            break;
                        }
                        if (perm != null && perm.getFunctionName().equalsIgnoreCase(GradingConstants.viewPermission)) {
                            view = true;
                        }
                    }
                    for (String studentId : studentIds) {
                        if (grade) {
                            studentMap.put(studentId, GradingConstants.gradePermission);
                        } else if (view) {
                            studentMap.put(studentId, GradingConstants.viewPermission);
                        }
                    }
                }
                perms = getPermissionsForUserAnyGroupAnyCategory(gradebookId, userId);
                Map<String, String> studentMapForGroups = filterPermissionForGraderForAllStudent(perms, studentIds);
                addToStudentMap(studentMap, studentMapForGroups);

                if (courseSections != null && !courseSections.isEmpty()) {
                    final List<String> groupIds = courseSections.stream().filter(Objects::nonNull)
                        .collect(Collectors.mapping(g -> g.getUuid(), Collectors.toList()));

                    perms = getPermissionsForUserForGoupsAnyCategory(gradebookId, userId, groupIds);
                    studentMapForGroups = filterPermissionForGrader(perms, studentIds, sectionIdStudentIdsMap);
                    addToStudentMap(studentMap, studentMapForGroups);
                }

                perms = getPermissionsForUserForCategory(gradebookId, userId, cateList);
                studentMapForGroups = filterPermissionForGrader(perms, studentIds, sectionIdStudentIdsMap);
                addToStudentMap(studentMap, studentMapForGroups);

                return studentMap;
            }
        }
        return null;
    }

    public Map<String, String> getStudentsForItem(String gradebookUid, String userId, List<String> studentIds, Integer cateType, Long categoryId, List<CourseSection> courseSections)
        throws IllegalArgumentException {

        if (gradebookUid == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getStudentsForItem");
        }

        Optional<Gradebook> optGradebook = gradingPersistenceManager.getGradebook(gradebookUid);

        if (optGradebook.isEmpty()) {
            log.warn("No gradebook for uid {}", gradebookUid);
            return Collections.<String, String>emptyMap();
        }

        Long gradebookId = optGradebook.get().getId();
        return getStudentsForItem(gradebookId, userId, studentIds, cateType, categoryId, courseSections);
    }

    public List<String> getViewableGroupsForUser(Long gradebookId, String userId, List<String> groupIds) {

        if (gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getViewableSectionsForUser");
        }

        if (groupIds == null || groupIds.size() == 0) {
            return null;
        }

        List<Permission> anyGroupPermission = getPermissionsForUserAnyGroup(gradebookId, userId);
        if (anyGroupPermission != null && anyGroupPermission.size() > 0 ) {
            return groupIds;
        } else {
            List<Permission> permList = getPermissionsForUserForGroup(gradebookId, userId, groupIds);

            List<String> filteredGroups = new ArrayList<>();
            for (String groupId : groupIds) {
                if (groupId != null) {
                    for (Permission perm : permList) {
                        if (perm != null && perm.getGroupId().equals(groupId)) {
                            filteredGroups.add(groupId);
                            break;
                        }
                    }
                }
            }
            return filteredGroups;
        }
    }

    public List<String> getViewableGroupsForUser(String gradebookUid, String userId, List<String> groupIds) {

        if (gradebookUid == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getViewableSectionsForUser");
        }

        Long gradebookId = getGradebook(gradebookUid).getId();

        return getViewableGroupsForUser(gradebookId, userId, groupIds);
    }

    public List<Permission> getGraderPermissionsForCurrentUser(Long gradebookId) {

        return getGraderPermissionsForUser(gradebookId, sessionManager.getCurrentSessionUserId());
    }

    public List<Permission> getGraderPermissionsForUser(Long gradebookId, String userId) {

        if (gradebookId == null || StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("The gradebookId and userId must be supplied");
        }

        return getPermissionsForUser(gradebookId, userId);
    }

    public List<Permission> getGraderPermissionsForCurrentUser(String gradebookUid) {

        return getGraderPermissionsForUser(gradebookUid, sessionManager.getCurrentSessionUserId());
    }

    public List<Permission> getGraderPermissionsForUser(String gradebookUid, String userId) {

        if (StringUtils.isBlank(gradebookUid) || StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("The gradebookUid and userId must be supplied");
        }

        if (StringUtils.isBlank(userId)) {
            userId = sessionManager.getCurrentSessionUserId();
        }

        Long gradebookId = getGradebook(gradebookUid).getId();

        return getPermissionsForUser(gradebookId, userId);
    }

    private Map<String, String> filterPermissionForGrader(final List<Permission> perms, List<String> studentIds, Map<String, List<String>> sectionIdStudentIdsMap) {

        Map<String, String> permMap = new HashMap<>();
        for (Permission perm : perms) {
            if (perm != null) {
                if (permMap.containsKey(perm.getGroupId()) && permMap.get(perm.getGroupId()).equalsIgnoreCase(GradingConstants.viewPermission)) {
                    if (perm.getFunctionName().equalsIgnoreCase(GradingConstants.gradePermission)) {
                        permMap.put(perm.getGroupId(), GradingConstants.gradePermission);
                    }
                } else if (!permMap.containsKey(perm.getGroupId())) {
                    permMap.put(perm.getGroupId(), perm.getFunctionName());
                }
            }
        }
        Map<String, String> studentMap = new HashMap<>();

        for (String studentId : studentIds) {
            if (sectionIdStudentIdsMap != null) {
                for (Map.Entry<String, List<String>> entry : sectionIdStudentIdsMap.entrySet()) {
                    String grpId = entry.getKey();
                    List<String> sectionMembers = entry.getValue();

                    if (sectionMembers != null && sectionMembers.contains(studentId) && permMap.containsKey(grpId)) {
                        if (studentMap.containsKey(studentId) && studentMap.get(studentId).equalsIgnoreCase(GradingConstants.viewPermission)) {
                            if (permMap.get(grpId).equalsIgnoreCase(GradingConstants.gradePermission)) {
                                studentMap.put(studentId, GradingConstants.gradePermission);
                            }
                        } else if (!studentMap.containsKey(studentId)) {
                            studentMap.put(studentId, permMap.get(grpId));
                        }
                    }
                }
            }
        }
        return studentMap;
    }

    private Map<String, String> filterPermissionForGraderForAllStudent(List<Permission> perms, List<String> studentIds) {

        Boolean grade = false;
        Boolean view = false;
        for (Permission perm : perms) {
            if (perm.getFunctionName().equalsIgnoreCase(GradingConstants.gradePermission)) {
                grade = true;
                break;
            } else if (perm.getFunctionName().equalsIgnoreCase(GradingConstants.viewPermission)) {
                view = true;
            }
        }

        Map<String, String> studentMap = new HashMap<>();

        if (grade || view) {
            for (String studentId : studentIds) {
                if (grade) {
                    studentMap.put(studentId, GradingConstants.gradePermission);
                } else if (view) {
                    studentMap.put(studentId, GradingConstants.viewPermission);
                }
            }
        }
        return studentMap;
    }

    private Map<Long, String> filterPermissionForGraderForAllAssignments(List<Permission> perms, List<GradebookAssignment> assignmentList) {

        Boolean grade = false;
        Boolean view = false;
        for (Permission perm : perms) {
            if (perm.getFunctionName().equalsIgnoreCase(GradingConstants.gradePermission)) {
                grade = true;
                break;
            } else if (perm.getFunctionName().equalsIgnoreCase(GradingConstants.viewPermission)) {
                view = true;
            }
        }

        Map<Long, String> assignMap = new HashMap<>();

        if (grade || view) {
            for (GradebookAssignment assign : assignmentList) {
                if (grade && assign != null) {
                    assignMap.put(assign.getId(), GradingConstants.gradePermission);
                } else if (view && assign != null) {
                    assignMap.put(assign.getId(), GradingConstants.viewPermission);
                }
            }
        }
        return assignMap;
    }

    private Map<Long, String> getAvailableItemsForStudent(Gradebook gradebook, String userId, String studentId, Map<String, CourseSection> sectionIdCourseSectionMap, Map<Long, Category> catIdCategoryMap, List<GradebookAssignment> assignments, List<Permission> permsForUserAnyGroup, List<Permission> allPermsForUser, List<Permission> permsForAnyGroupForCategories, List<Permission> permsForUserAnyGroupAnyCategory, List<Permission> permsForGroupsAnyCategory, List<Permission> permsForUserForCategories, Map<String, List<String>> sectionIdStudentIdsMap) throws IllegalArgumentException {

        if (gradebook == null || userId == null || studentId == null) {
            throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getAvailableItemsForStudent");
        }

        List<Category> cateList = new ArrayList<>(catIdCategoryMap.values());

        if (Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY)) {

            Map<Long, String> assignMap = new HashMap<>();
            if (permsForUserAnyGroup != null && permsForUserAnyGroup.size() > 0) {
                boolean view = false;
                boolean grade = false;
                for (Permission perm : permsForUserAnyGroup) {
                    if (perm != null && perm.getFunctionName().equalsIgnoreCase(GradingConstants.gradePermission)) {
                        grade = true;
                        break;
                    }
                    if (perm != null && perm.getFunctionName().equalsIgnoreCase(GradingConstants.viewPermission)) {
                        view = true;
                    }
                }
                for (GradebookAssignment as : assignments) {
                    if (grade && as != null) {
                        assignMap.put(as.getId(), GradingConstants.gradePermission);
                    } else if (view && as != null) {
                        assignMap.put(as.getId(), GradingConstants.viewPermission);
                    }
                }
            }

            if (allPermsForUser != null) {
                Map<Long, String> assignsMapForGroups = filterPermissionForGrader(allPermsForUser, studentId, assignments, sectionIdStudentIdsMap);
                for (Map.Entry<Long, String> entry : assignsMapForGroups.entrySet()) {
                    Long key = entry.getKey();
                    if (!assignMap.containsKey(key) || assignMap.get(key).equalsIgnoreCase(GradingConstants.viewPermission)) {
                        assignMap.put(key, entry.getValue());
                    }
                }
            }
            return assignMap;
        } else {
            Map<Long, String> assignMap = new HashMap<>();
            if (permsForAnyGroupForCategories != null && permsForAnyGroupForCategories.size() > 0) {
                for (Permission perm : permsForAnyGroupForCategories) {
                    if (perm != null) {
                        if (perm.getCategoryId() != null) {
                            for (Category cate : cateList) {
                                if (cate != null && cate.getId().equals(perm.getCategoryId())) {
                                    for (GradebookAssignment as : cate.getAssignmentList()) {
                                        if (as != null) {
                                            Long assignId = as.getId();
                                            if (as.getCategory() != null) {
                                                if (assignMap.containsKey(assignId) && assignMap.get(assignId).equalsIgnoreCase(GradingConstants.viewPermission)) {
                                                    if (perm.getFunctionName().equalsIgnoreCase(GradingConstants.gradePermission)) {
                                                        assignMap.put(assignId, GradingConstants.gradePermission);
                                                    }
                                                } else if (!assignMap.containsKey(assignId)) {
                                                    assignMap.put(assignId, perm.getFunctionName());
                                                }
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (permsForUserAnyGroupAnyCategory != null) {
                Map<Long, String> assignMapForGroups = filterPermissionForGraderForAllAssignments(permsForUserAnyGroupAnyCategory, assignments);
                for (Entry<Long, String> entry : assignMapForGroups.entrySet()) {
                    Long key = entry.getKey();
                    if ((assignMap.containsKey(key) && assignMap.get(key).equalsIgnoreCase(GradingConstants.viewPermission))
                            || !assignMap.containsKey(key)) {
                        assignMap.put(key, entry.getValue());
                    }
                }
            }

            if (permsForGroupsAnyCategory != null) {
                Map<Long, String> assignMapForGroups = filterPermissionForGrader(permsForGroupsAnyCategory, studentId, assignments, sectionIdStudentIdsMap);
                for (Entry<Long, String> entry : assignMapForGroups.entrySet()) {
                    Long key = entry.getKey();
                    if ((assignMap.containsKey(key) && assignMap.get(key).equalsIgnoreCase(GradingConstants.viewPermission))
                            || !assignMap.containsKey(key)) {
                        assignMap.put(key, entry.getValue());
                    }
                }
            }

            if (permsForUserForCategories != null) {
                Map<Long, String> assignMapForGroups = filterPermissionForGraderForCategory(permsForUserForCategories, studentId, cateList, sectionIdStudentIdsMap);
                if (assignMapForGroups != null) {
                    for (Entry<Long, String> entry : assignMapForGroups.entrySet()) {
                        Long key = entry.getKey();
                        if ((assignMap.containsKey(key) && assignMap.get(key).equalsIgnoreCase(GradingConstants.viewPermission))
                                || !assignMap.containsKey(key)) {
                            assignMap.put(key, entry.getValue());
                        }
                    }
                }
            }

            return assignMap;
        }
    }

    public Map<Long, String> getAvailableItemsForStudent(Long gradebookId, String userId, String studentId, List<CourseSection> courseSections) throws IllegalArgumentException {

        if (gradebookId == null || userId == null || studentId == null) {
            throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getAvailableItemsForStudent");
        }

        final Map<String, CourseSection> sectionIdCourseSectionMap
            = courseSections.stream().filter(Objects::nonNull).collect(Collectors.toMap(s -> s.getUuid(), s -> s));

        final Map<Long, Category> catIdCategoryMap
            = getCategoriesWithAssignments(gradebookId).stream()
                .filter(Objects::nonNull).collect(Collectors.toMap(c -> c.getId(), c -> c));

        List<String> studentIds = new ArrayList<>();
        studentIds.add(studentId);
        Map<String, List<String>> sectionIdStudentIdsMap = getSectionIdStudentIdsMap(courseSections, studentIds);

        Optional<Gradebook> optGradebook = gradingPersistenceManager.getGradebook(gradebookId);

        if (optGradebook.isEmpty()) {
            log.warn("No gradebook for id {}", gradebookId);
            return null;
        }

        List<GradebookAssignment> assignments = gradingPersistenceManager.getAssignmentsForGradebook(gradebookId);
        List<Long> categoryIds = new ArrayList<>(catIdCategoryMap.keySet());
        List<String> groupIds = new ArrayList<>(sectionIdCourseSectionMap.keySet());

        // Retrieve all the different permission info needed here so not called repeatedly for each student
        List<Permission> permsForUserAnyGroup = getPermissionsForUserAnyGroup(gradebookId, userId);
        List<Permission> allPermsForUser = getPermissionsForUser(gradebookId, userId);
        List<Permission> permsForAnyGroupForCategories
            = categoryIds.isEmpty() ? Collections.<Permission>emptyList()
                    : getPermissionsForUserAnyGroupForCategory(gradebookId, userId, categoryIds);
        List<Permission> permsForUserAnyGroupAnyCategory = getPermissionsForUserAnyGroupAnyCategory(gradebookId, userId);
        List<Permission> permsForGroupsAnyCategory = getPermissionsForUserForGoupsAnyCategory(gradebookId, userId, groupIds);
        List<Permission> permsForUserForCategories
            = categoryIds.isEmpty() ? Collections.<Permission>emptyList()
                    : getPermissionsForUserForCategory(gradebookId, userId, categoryIds);

        return getAvailableItemsForStudent(optGradebook.get(), userId, studentId, sectionIdCourseSectionMap, catIdCategoryMap, assignments, permsForUserAnyGroup, allPermsForUser, permsForAnyGroupForCategories, permsForUserAnyGroupAnyCategory, permsForGroupsAnyCategory, permsForUserForCategories, sectionIdStudentIdsMap);
    }

    public Map<Long, String> getAvailableItemsForStudent(String gradebookUid, String userId, String studentId, List<CourseSection> courseSections) throws IllegalArgumentException {

        if (gradebookUid == null || userId == null || studentId == null) {
            throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getAvailableItemsForStudent");
        }

        Long gradebookId = getGradebook(gradebookUid).getId();

        return getAvailableItemsForStudent(gradebookId, userId, studentId, courseSections);

    }

    private Map<Long, String> filterPermissionForGrader(List<Permission> perms, String studentId, List<GradebookAssignment> assignmentList, Map<String, List<String>> sectionIdStudentIdsMap) {

        Map<String, String> permMap = new HashMap<>();
        for (Permission perm : perms) {
            if (perm != null) {
                if (permMap.containsKey(perm.getGroupId()) && permMap.get(perm.getGroupId()).equalsIgnoreCase(GradingConstants.viewPermission)) {
                    if (perm.getFunctionName().equalsIgnoreCase(GradingConstants.gradePermission)) {
                        permMap.put(perm.getGroupId(), GradingConstants.gradePermission);
                    }
                } else if (!permMap.containsKey(perm.getGroupId())) {
                    permMap.put(perm.getGroupId(), perm.getFunctionName());
                }
            }
        }
        Map<Long, String> assignmentMap = new HashMap<>();

        if (sectionIdStudentIdsMap != null) {
            for (GradebookAssignment assign : assignmentList) {
                Long assignId = assign.getId();
                for (Map.Entry<String, List<String>> entry : sectionIdStudentIdsMap.entrySet()) {
                    String grpId = entry.getKey();
                    List<String> sectionMembers = sectionIdStudentIdsMap.get(grpId);

                    if (sectionMembers != null && sectionMembers.contains(studentId) && permMap.containsKey(grpId)) {
                        if (assignmentMap.containsKey(assignId) && assignmentMap.get(assignId).equalsIgnoreCase(GradingConstants.viewPermission)) {
                            if (permMap.get(grpId).equalsIgnoreCase(GradingConstants.gradePermission)) {
                                assignmentMap.put(assignId, GradingConstants.gradePermission);
                            }
                        } else if (!assignmentMap.containsKey(assignId)) {
                            assignmentMap.put(assignId, permMap.get(grpId));
                        }
                    }
                }
            }
        }
        return assignmentMap;
    }

    private  Map<Long, String> filterPermissionForGraderForCategory(List<Permission> perms, String studentId, List<Category> categoryList, Map<String, List<String>> sectionIdStudentIdsMap) {

        Map<Long, String> assignmentMap = new HashMap<>();

        for (Permission perm : perms) {
            if (perm != null && perm.getCategoryId() != null) {
                for (Category cate : categoryList) {
                    if (cate != null && cate.getId().equals(perm.getCategoryId())) {
                        for (GradebookAssignment as : cate.getAssignmentList()) {
                            if (as != null && sectionIdStudentIdsMap != null) {
                                Long assignId = as.getId();
                                for (Map.Entry<String, List<String>> entry : sectionIdStudentIdsMap.entrySet()) {
                                    String grpId = entry.getKey();
                                    List<String> sectionMembers = sectionIdStudentIdsMap.get(grpId);

                                    if (sectionMembers != null && sectionMembers.contains(studentId) && as.getCategory() != null) {
                                        if (assignmentMap.containsKey(assignId) && grpId.equals(perm.getGroupId()) && assignmentMap.get(assignId).equalsIgnoreCase(GradingConstants.viewPermission)) {
                                            if (perm.getFunctionName().equalsIgnoreCase(GradingConstants.gradePermission)) {
                                                assignmentMap.put(assignId, GradingConstants.gradePermission);
                                            }
                                        } else if (!assignmentMap.containsKey(assignId) && grpId.equals(perm.getGroupId())) {
                                            assignmentMap.put(assignId, perm.getFunctionName());
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        return assignmentMap;
    }

    public Map<String, Map<Long, String>> getAvailableItemsForStudents(Long gradebookId, String userId, List<String> studentIds, List<CourseSection> courseSections) throws IllegalArgumentException {

        if (gradebookId == null || userId == null || studentIds == null) {
            throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getAvailableItemsForStudents");
        }

        final Map<String, CourseSection> sectionIdCourseSectionMap
            = courseSections.stream().filter(Objects::nonNull).collect(Collectors.toMap(s -> s.getUuid(), s -> s));

        final Map<Long, Category> catIdCategoryMap
            = getCategoriesWithAssignments(gradebookId).stream()
                .filter(Objects::nonNull).collect(Collectors.toMap(c -> c.getId(), c -> c));

        Map<String, List<String>> sectionIdStudentIdsMap = getSectionIdStudentIdsMap(courseSections, studentIds);

        Optional<Gradebook> optGradebook = gradingPersistenceManager.getGradebook(gradebookId);

        if (optGradebook.isEmpty()) {
            log.warn("No gradebook for id {}", gradebookId);
            return null;
        }

        List<GradebookAssignment> assignments = gradingPersistenceManager.getAssignmentsForGradebook(gradebookId);
        List<Long> categoryIds = new ArrayList<>(catIdCategoryMap.keySet());
        List<String> groupIds = new ArrayList<>(sectionIdCourseSectionMap.keySet());

        // Retrieve all the different permission info needed here so not called repeatedly for each student
        List<Permission> permsForUserAnyGroup = getPermissionsForUserAnyGroup(gradebookId, userId);
        List<Permission> allPermsForUser = getPermissionsForUser(gradebookId, userId);
        List<Permission> permsForAnyGroupForCategories = getPermissionsForUserAnyGroupForCategory(gradebookId, userId, categoryIds);
        List<Permission> permsForUserAnyGroupAnyCategory = getPermissionsForUserAnyGroupAnyCategory(gradebookId, userId);
        List<Permission> permsForGroupsAnyCategory = getPermissionsForUserForGoupsAnyCategory(gradebookId, userId, groupIds);
        List<Permission> permsForUserForCategories = getPermissionsForUserForCategory(gradebookId, userId, categoryIds);

        Map<String, Map<Long, String>> studentsMap = new HashMap<>();
        for (String studentId : studentIds) {
            if (studentId != null) {
                Map<Long, String> assignMap = getAvailableItemsForStudent(optGradebook.get(),
                                                userId, studentId, sectionIdCourseSectionMap,
                                                catIdCategoryMap, assignments, permsForUserAnyGroup,
                                                allPermsForUser, permsForAnyGroupForCategories,
                                                permsForUserAnyGroupAnyCategory,
                                                permsForGroupsAnyCategory, permsForUserForCategories,
                                                sectionIdStudentIdsMap);
                studentsMap.put(studentId, assignMap);
            }
        }
        return studentsMap;
    }

    public Map<String, Map<Long, String>> getAvailableItemsForStudents(String gradebookUid, String userId, List<String> studentIds, List<CourseSection> courseSections) throws IllegalArgumentException {

        if (gradebookUid == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getAvailableItemsForStudents");
        }

        Long gradebookId = getGradebook(gradebookUid).getId();
        return getAvailableItemsForStudents(gradebookId, userId, studentIds, courseSections);
    }

    public Map<String, String> getCourseGradePermission(Long gradebookId, String userId, List<String> studentIds, List<CourseSection> courseSections) throws IllegalArgumentException {

        if (gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getCourseGradePermission");
        }

        if (studentIds == null) {
            return Collections.<String, String>emptyMap();
        }

        Map<String, String>  studentMap = new HashMap<>();
        Map<String, List<String>> sectionIdStudentIdsMap = getSectionIdStudentIdsMap(courseSections, studentIds);

        List<Permission> perms = getPermissionsForUserAnyGroupAnyCategory(gradebookId, userId);
        Map<String, String> studentMapForGroups = filterPermissionForGraderForAllStudent(perms, studentIds);
        addToStudentMap(studentMap, studentMapForGroups);

        if (courseSections != null) {
            List<String> groupIds = courseSections.stream()
                .filter(Objects::nonNull).map(grp -> grp.getUuid()).collect(Collectors.toList());

            perms = getPermissionsForUserForGoupsAnyCategory(gradebookId, userId, groupIds);
            studentMapForGroups = filterPermissionForGrader(perms, studentIds, sectionIdStudentIdsMap);
            addToStudentMap(studentMap, studentMapForGroups);

            Optional<Gradebook> optGradebook = gradingPersistenceManager.getGradebook(gradebookId);

            if (optGradebook.isEmpty()) {
                log.warn("No gradebook for id {}", gradebookId);
                return null;
            }

            Gradebook gradebook = optGradebook.get();

            if (gradebook != null && (Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_ONLY_CATEGORY)
                    || Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY))) {
                List<Category> cateList = gradingPersistenceManager.getCategoriesForGradebook(gradebookId);

                perms = getPermissionsForUserForGroup(gradebookId, userId, groupIds);
                studentMapForGroups = filterForAllCategoryStudents(perms, studentIds, cateList, sectionIdStudentIdsMap);
                addToStudentMap(studentMap, studentMapForGroups);

                final List<Long> cateIdList = cateList.stream()
                    .filter(Objects::nonNull).map(c -> c.getId()).collect(Collectors.toList());

                perms = getPermissionsForUserAnyGroupForCategory(gradebookId, userId, cateIdList);
                if (perms.size() > 0) {
                    studentMapForGroups = filterForAllCategoryStudentsAnyGroup(perms, courseSections, studentIds, cateList);
                    addToStudentMap(studentMap, studentMapForGroups);
                }
            }
        }

        return studentMap;
    }

    public Map<String, String> getCourseGradePermission(String gradebookUid, String userId, List<String> studentIds, List<CourseSection> courseSections) throws IllegalArgumentException {

        if (gradebookUid == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getCourseGradePermission");
        }

        Long gradebookId = getGradebook(gradebookUid).getId();
        return getCourseGradePermission(gradebookId, userId, studentIds, courseSections);
    }

    private Map<String, String> filterForAllCategoryStudents(List<Permission> perms, List<String> studentIds, List<Category> cateList, Map<String, List<String>> sectionIdStudentIdsMap) {

        if (sectionIdStudentIdsMap == null || studentIds == null || cateList == null) {
            return Collections.<String, String>emptyMap();
        }

        final List<Long> cateIdList = cateList.stream()
            .filter(Objects::nonNull).map(c -> c.getId()).collect(Collectors.toList());

        Map<String, Map<Long, String>> studentCateMap = new HashMap<>();
        for (String studentId : studentIds) {
            studentCateMap.put(studentId, new HashMap<Long, String>());
            if (studentId != null) {
                for (Map.Entry<String, List<String>> entry : sectionIdStudentIdsMap.entrySet()) {
                    String grpId = entry.getKey();

                    if (grpId != null) {
                        List<String> grpMembers = sectionIdStudentIdsMap.get(grpId);
                        if (grpMembers != null && !grpMembers.isEmpty() && grpMembers.contains(studentId)) {
                            for (Permission perm : perms) {
                                if (perm != null && perm.getGroupId().equals(grpId) && perm.getCategoryId() != null && cateIdList.contains(perm.getCategoryId())) {
                                    Map<Long, String> cateMap = studentCateMap.get(studentId);
                                    if (cateMap.get(perm.getCategoryId()) == null || cateMap.get(perm.getCategoryId()).equals(GradingConstants.viewPermission)) {
                                        cateMap.put(perm.getCategoryId(), perm.getFunctionName());
                                    }
                                    studentCateMap.put(studentId, cateMap);
                                }
                            }
                        }
                    }
                }
            }
        }

        Map<String, String> studentPermissionMap = new HashMap<>();
        for (Entry<String, Map<Long, String>> perEntry : studentCateMap.entrySet()) {
            String studentId = perEntry.getKey();
            Map<Long, String> cateMap = perEntry.getValue();
            if (cateMap != null) {
                for (Long existsCateId : cateIdList) {
                    if (existsCateId != null) {
                        boolean hasPermissionForCate = false;
                        String permission = null;
                        for (Entry<Long, String> entry : cateMap.entrySet()) {
                            Long cateId = entry.getKey();
                            if (cateId.equals(existsCateId)) {
                                hasPermissionForCate = true;
                                permission = entry.getValue();
                                break;
                            }
                        }
                        if (hasPermissionForCate && permission != null) {
                            if (studentPermissionMap.get(studentId) == null || studentPermissionMap.get(studentId).equals(GradingConstants.gradePermission)) {
                                studentPermissionMap.put(studentId, permission);
                            }
                        } else if (!hasPermissionForCate) {
                            if (studentPermissionMap.get(studentId) != null) {
                                studentPermissionMap.remove(studentId);
                            }
                        }
                    }
                }
            }
        }
        return studentPermissionMap;
    }

    private Map<String, String> filterForAllCategoryStudentsAnyGroup(List<Permission> perms, List<CourseSection> courseSections, List<String> studentIds, List<Category> cateList) {

        if (courseSections == null || studentIds == null || cateList == null) {
            return Collections.<String, String>emptyMap();
        }

        Map<Long, String> cateMap = new HashMap<>();
        for (Category cate : cateList) {
            if (cate != null) {
                boolean permissionExistForCate = false;
                for (Permission perm : perms) {
                    if (perm != null && perm.getCategoryId().equals(cate.getId())) {
                        if ((cateMap.get(cate.getId()) == null || cateMap.get(cate.getId()).equals(GradingConstants.viewPermission))) {
                            cateMap.put(cate.getId(), perm.getFunctionName());
                        }
                        permissionExistForCate = true;
                    }
                }
                if (!permissionExistForCate) {
                    return new HashMap<String, String>();
                }
            }
        }

        boolean view = false;
        for (Long catId : cateMap.keySet()) {
            String permission = cateMap.get(catId);
            if (permission != null && permission.equals(GradingConstants.viewPermission)) {
                view = true;
            }
        }
        Map<String, String> studentMap = new HashMap<>();
        for (String studentId : studentIds) {
            if (view) {
                studentMap.put(studentId, GradingConstants.viewPermission);
            } else {
                studentMap.put(studentId, GradingConstants.gradePermission);
            }
        }

        return studentMap;
    }

    public List<String> getViewableStudentsForUser(Long gradebookId, String userId, List<String> studentIds, List<CourseSection> sections) {

        if (gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getAvailableItemsForStudent");
        }

        if (studentIds == null || studentIds.isEmpty()) {
            return Collections.<String>emptyList();
        }

        List<Permission> permsForAnyGroup = getPermissionsForUserAnyGroup(gradebookId, userId);
        if (!permsForAnyGroup.isEmpty()) {
            return studentIds;
        }

        Map<String, List<String>> sectionIdStudentIdsMap = getSectionIdStudentIdsMap(sections, studentIds);

        if (sectionIdStudentIdsMap.isEmpty()) {
            return Collections.<String>emptyList();
        }

        // use a map to make sure the student ids are unique
        Map<String, Object> studentMap = new HashMap<>();

        // Next, check for permissions for specific sections
        List<String> groupIds = new ArrayList<>(sectionIdStudentIdsMap.keySet());
        List<Permission> permsForGroupsAnyCategory = getPermissionsForUserForGroup(gradebookId, userId, groupIds);

        if (permsForGroupsAnyCategory.isEmpty()) {
            return Collections.<String>emptyList();
        }

        for (Permission perm : permsForGroupsAnyCategory) {
            String groupId = perm.getGroupId();
            if (groupId != null) {
                List<String> sectionStudentIds = sectionIdStudentIdsMap.get(groupId);
                if (sectionStudentIds != null && !sectionStudentIds.isEmpty()) {
                    sectionStudentIds.forEach(id -> studentMap.put(id, null));
                }
            }
        }

        return new ArrayList<>(studentMap.keySet());
    }

    public List<String> getViewableStudentsForUser(String gradebookUid, String userId, List<String> studentIds, List<CourseSection> sections) {

        if (gradebookUid == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in GradebookPermissionServiceImpl.getViewableStudentsForUser");
        }

        Long gradebookId = getGradebook(gradebookUid).getId();

        return getViewableStudentsForUser(gradebookId, userId, studentIds, sections);

    }

    /**
     * Get a list of permissions defined for the given user based on section and role or all sections if allowed.
     * This method checks realms permissions for role/section and is independent of the
     * gb_permissions_t permissions.
     *
     * note: If user has the grade privilege, they are given the GraderPermission.VIEW_COURSE_GRADE permission to match
     * GB classic functionality. This needs to be reviewed.
     *
     * @param userUuid
     * @param siteId
     * @param role user Role
     * @return list of {@link org.sakaiproject.grading.api.PermissionDefinition PermissionDefinitions} or empty list if none
     */
    public List<PermissionDefinition> getRealmsPermissionsForUser(String userUuid,String siteId, Role role) {

        List<PermissionDefinition> permissions = new ArrayList<PermissionDefinition>();

        if (this.getGradingService().isUserAllowedToGrade(siteId,userUuid)) {
            //FIXME:giving them view course grade (this needs to be reviewed!!),
            //it appears in GB classic, User can view course grades if they have the ability to grade in realms
            PermissionDefinition permDef = new PermissionDefinition();
            permDef.setFunctionName(GraderPermission.VIEW_COURSE_GRADE.toString());
            permDef.setUserId(userUuid);
            permissions.add(permDef);

            if (this.getGradingService().isUserAllowedToGradeAll(siteId,userUuid)) {
                permDef = new PermissionDefinition();
                permDef.setFunctionName(GraderPermission.GRADE.toString());
                permDef.setUserId(userUuid);
                permissions.add(permDef);
            } else {
                //get list of sections belonging to user and set a PermissionDefinition for each one
                //Didn't find a method that returned gradeable sections for a TA, only for the logged in user.
                //grabbing list of sections for the site, if User is a member of the section and has privilege to
                //grade their sections, they are given the grade permission. Seems straight forward??
                List<CourseSection> sections = sectionAwareness.getSections(siteId);

                for (CourseSection section: sections) {
                    if (sectionAwareness.isSectionMemberInRole(section.getUuid(), userUuid,role)) {
                        //realms have no categories defined for grading, just perms and group id
                        permDef = new PermissionDefinition();
                        permDef.setFunctionName(GraderPermission.GRADE.toString());
                        permDef.setUserId(userUuid);
                        permDef.setGroupReference(section.getUuid());
                        permissions.add(permDef);
                    }
                }
            }
        }

        return permissions;
    }

    public GradingService getGradingService() {

        return (GradingService) ComponentManager.get("org.sakaiproject.grading.api.GradingService");
    }

    private Map<String, List<String>> getSectionIdStudentIdsMap(List<CourseSection> courseSections, List<String> studentIds) {

        if (courseSections == null) {
            return Collections.<String, List<String>>emptyMap();
        }

        Map<String, List<String>> sectionIdStudentIdsMap = new HashMap<>();
        for (CourseSection section: courseSections) {
            if (section != null) {
                String sectionId = section.getUuid();
                List<EnrollmentRecord> members = sectionAwareness.getSectionMembersInRole(sectionId, Role.STUDENT);
                List<String> sectionMembersFiltered = new ArrayList<>();
                if (!members.isEmpty()) {
                    for (EnrollmentRecord enr : members) {
                        String studentId = enr.getUser().getUserUid();
                        if (studentIds.contains(studentId)) {
                            sectionMembersFiltered.add(studentId);
                        }
                    }
                }
                sectionIdStudentIdsMap.put(sectionId, sectionMembersFiltered);
            }
        }
        return sectionIdStudentIdsMap;
    }

    @Override
    public List<PermissionDefinition> getPermissionsForUser(String gradebookUid, String userId) {

        Long gradebookId = getGradebook(gradebookUid).getId();

        return getPermissionsForUser(gradebookId, userId).stream()
            .map(this::toPermissionDefinition).collect(Collectors.toList());
    }

    @Override
    public void updatePermissionsForUser(String gradebookUid, String userId, List<PermissionDefinition> permissionDefinitions) {

        Long gradebookId = getGradebook(gradebookUid).getId();

        if (permissionDefinitions.isEmpty()) {
            PermissionDefinition noPermDef = new PermissionDefinition();
            noPermDef.setFunctionName(GraderPermission.NONE.toString());
            noPermDef.setUserId(userId);
            permissionDefinitions.add(noPermDef);
        }

        //get the current list of permissions
        final List<Permission> currentPermissions = getPermissionsForUser(gradebookId, userId);

        //convert PermissionDefinition to Permission
        final List<Permission> newPermissions = new ArrayList<>();
        for (PermissionDefinition def: permissionDefinitions) {
            if (!StringUtils.equalsIgnoreCase(def.getFunctionName(), GraderPermission.GRADE.toString())
                    && !StringUtils.equalsIgnoreCase(def.getFunctionName(), GraderPermission.VIEW.toString())
                    && !StringUtils.equalsIgnoreCase(def.getFunctionName(), GraderPermission.VIEW_COURSE_GRADE.toString())
                    && !StringUtils.equalsIgnoreCase(def.getFunctionName(), GraderPermission.NONE.toString())) {
                throw new IllegalArgumentException("Invalid function for permission definition: " + def.getFunctionName());
            }

            Permission permission = new Permission();
            permission.setCategoryId(def.getCategoryId());
            permission.setGradebookId(gradebookId);
            permission.setGroupId(def.getGroupReference());
            permission.setFunctionName(def.getFunctionName());
            permission.setUserId(userId);

            newPermissions.add(permission);
        }

        //Note: rather than iterate both lists and figure out the differences and add/update/delete as applicable,
        //it is far simpler to just remove the existing permissions and add new ones in one transaction
        currentPermissions.forEach(gradingPersistenceManager::deletePermission);
        newPermissions.forEach(gradingPersistenceManager::savePermission);
    }

    public void clearPermissionsForUser(String gradebookUid, String userId) {

        Long gradebookId = getGradebook(gradebookUid).getId();
        getPermissionsForUser(gradebookId, userId).forEach(gradingPersistenceManager::deletePermission);
    }

    public boolean currentUserHasGraderPermissions(String gradebookUid) {

        List<Permission> permissions = getGraderPermissionsForCurrentUser(gradebookUid);
        return permissions != null && !permissions.isEmpty();
    }

    public boolean userHasGraderPermissions(String gradebookUid, String userId) {

        List<Permission> permissions = getGraderPermissionsForUser(gradebookUid, userId);
        return permissions != null && !permissions.isEmpty();
    }

     /**
      * Maps a Permission to a PermissionDefinition
      * Note that the persistent groupId is actually the group reference
      * @param permission
      * @return a {@link PermissionDefinition}
      */
     private PermissionDefinition toPermissionDefinition(Permission permission) {

         PermissionDefinition rval = new PermissionDefinition();
         if (permission != null) {
             rval.setId(permission.getId());
             rval.setUserId(permission.getUserId());
             rval.setCategoryId(permission.getCategoryId());
             rval.setFunctionName(permission.getFunctionName());
             rval.setGroupReference(permission.getGroupId());
         }
         return rval;
     }

    private List<Permission> getPermissionsForUser(Long gradebookId, String userId) throws IllegalArgumentException {

        if (gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in getPermissionsForUser");
        }

        return gradingPersistenceManager.getPermissionsForGradebookAndUser(gradebookId, userId);
    }

    private List<Permission> getPermissionsForUserAnyCategory(Long gradebookId, String userId) throws IllegalArgumentException {

        if (gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) getPermissionsForUserAnyCategory");
        }

        return gradingPersistenceManager.getUncategorisedPermissionsForGradebookAndUserAndFunctions(gradebookId, userId, GraderPermission.getStandardPermissions());
    }

    private List<Permission> getPermissionsForUserForCategory(Long gradebookId, String userId, List<Long> categoryIds) throws IllegalArgumentException {

        if (gradebookId == null || userId == null || categoryIds == null || categoryIds.isEmpty()) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserForCategory");
        }

        return gradingPersistenceManager.getPermissionsForGradebookAndUserAndCategories(gradebookId, userId, categoryIds);
    }

    private List<Permission> getPermissionsForUserAnyGroup(Long gradebookId, String userId) throws IllegalArgumentException {

        if (gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in getPermissionsForUserAnyGroup");
        }

        return gradingPersistenceManager.getUngroupedPermissionsForGradebookAndUserAndFunctions(gradebookId, userId, GraderPermission.getStandardPermissions());
    }

    private List<Permission> getPermissionsForUserAnyGroupForCategory(Long gradebookId, String userId, List<Long> categoryIds) throws IllegalArgumentException {

        if (gradebookId == null || userId == null || categoryIds == null || categoryIds.isEmpty()) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserAnyGroupForCategory");
        }

        return gradingPersistenceManager.getUngroupedPermissionsForGradebookAndUserAndCategories(gradebookId, userId, categoryIds);
    }

    private List<Permission> getPermissionsForUserAnyGroupAnyCategory(Long gradebookId, String userId) throws IllegalArgumentException {

        if (gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserAnyGroupForCategory");
        }

        return gradingPersistenceManager.getPermissionsForGradebookAnyGroupAnyCategory(gradebookId, userId);
    }

    private List<Permission> getPermissionsForUserForGoupsAnyCategory(Long gradebookId, String userId, List<String> groupIds) throws IllegalArgumentException {

        if (gradebookId == null || userId == null || groupIds == null) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserForGoupsAnyCategory");
        }

        return gradingPersistenceManager.getUncategorisedPermissionsForGradebookAndGroups(gradebookId, userId, groupIds);
    }

    private List<Permission> getPermissionsForUserForGroup(Long gradebookId, String userId, List<String> groupIds) throws IllegalArgumentException {

        if (gradebookId == null || userId == null || groupIds == null || groupIds.isEmpty()) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserForGroup");
        }

        return gradingPersistenceManager.getPermissionsForGradebookAndGroups(gradebookId, userId, groupIds);
    }

    private List<Category> getCategoriesWithAssignments(Long gradebookId) {
        return gradingPersistenceManager.getCategoriesWithAssignmentsForGradebook(gradebookId);
    }

    private Gradebook getGradebook(String gradebookUid) {

        return gradingPersistenceManager.getGradebook(gradebookUid).orElse(null);
    }
}
