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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.GradingAuthz;
import org.sakaiproject.grading.api.GradingCategoryType;
import org.sakaiproject.grading.api.GradingPermissionService;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * An implementation of Gradebook-specific authorization needs based
 * on a combination of fine-grained site-scoped Sakai permissions and the
 * shared Section Awareness API. This is a transtional stage between
 * coarse-grained site-and-role-based authz and our hoped-for fine-grained
 * role-determined group-scoped authz.
 */
@Slf4j
public class GradingAuthzImpl implements GradingAuthz {

    @Autowired private SectionAwareness sectionAwareness;
    @Autowired private GradingPermissionService gradingPermissionService;
    @Autowired private FunctionManager functionManager;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;
    @Autowired private SiteService siteService;

    /**
     * Perform authorization-specific framework initializations for the Gradebook.
     */
    public void init() {

        Collection registered = functionManager.getRegisteredFunctions("gradebook");
        if (!registered.contains(PERMISSION_GRADE_ALL)) {
            functionManager.registerFunction(PERMISSION_GRADE_ALL);
        }

        if (!registered.contains(PERMISSION_GRADE_SECTION)) {
            functionManager.registerFunction(PERMISSION_GRADE_SECTION);
        }

        if (!registered.contains(PERMISSION_EDIT_ASSIGNMENTS)) {
            functionManager.registerFunction(PERMISSION_EDIT_ASSIGNMENTS);
        }

        if (!registered.contains(PERMISSION_VIEW_OWN_GRADES)) {
            functionManager.registerFunction(PERMISSION_VIEW_OWN_GRADES);
        }

        if (!registered.contains(PERMISSION_VIEW_STUDENT_NUMBERS)) {
            functionManager.registerFunction(PERMISSION_VIEW_STUDENT_NUMBERS);
        }
    }

    public boolean isUserAbleToGrade(String gradebookUid) {

        return (hasPermission(gradebookUid, PERMISSION_GRADE_ALL) || hasPermission(gradebookUid, PERMISSION_GRADE_SECTION));
    }

    public boolean isUserAbleToGrade(String gradebookUid, String userUid) {

        try {
            User user = userDirectoryService.getUser(userUid);
            return (hasPermission(user, gradebookUid, PERMISSION_GRADE_ALL) || hasPermission(user, gradebookUid, PERMISSION_GRADE_SECTION));
        } catch (UserNotDefinedException unde) {
            log.warn("User not found for userUid: " + userUid);
            return false;
        }

    }

    public boolean isUserAbleToGradeAll(String gradebookUid) {

        return hasPermission(gradebookUid, PERMISSION_GRADE_ALL);
    }

    public boolean isUserAbleToGradeAll(String gradebookUid, String userUid) {

        try {
            User user = userDirectoryService.getUser(userUid);
            return hasPermission(user, gradebookUid, PERMISSION_GRADE_ALL);
        } catch (UserNotDefinedException unde) {
            log.warn("User not found for userUid: " + userUid);
            return false;
        }
    }

    /**
     * When group-scoped permissions are available, this is where
     * they will go. My current assumption is that the call will look like:
     *
     *   return hasPermission(sectionUid, PERMISSION_GRADE_ALL);
     */
    public boolean isUserAbleToGradeSection(String sectionUid) {

        return sectionAwareness.isSectionMemberInRole(sectionUid, sessionManager.getCurrentSessionUserId(), Role.TA);
    }

    public boolean isUserAbleToEditAssessments(String gradebookUid) {

        return hasPermission(gradebookUid, PERMISSION_EDIT_ASSIGNMENTS);
    }

    public boolean isUserAbleToViewOwnGrades(String gradebookUid) {

        return hasPermission(gradebookUid, PERMISSION_VIEW_OWN_GRADES);
    }

    public boolean isUserAbleToViewStudentNumbers(String gradebookUid) {

        return hasPermission(gradebookUid, PERMISSION_VIEW_STUDENT_NUMBERS);
    }

    private boolean hasPermission(String gradebookUid, String permission) {

        return securityService.unlock(permission, siteService.siteReference(gradebookUid));
    }

    private boolean hasPermission(User user, String gradebookUid, String permission) {

        return securityService.unlock(user, permission, siteService.siteReference(gradebookUid));
    }

    /**
     *
     * @param sectionUid
     * @return whether user is Role.TA in given section
     */
    private boolean isUserTAinSection(String sectionUid) {

        String userUid = sessionManager.getCurrentSessionUserId();
        return sectionAwareness.isSectionMemberInRole(sectionUid, userUid, Role.TA);
    }

    private boolean isUserTAinSection(String sectionUid, String userUid) {

        return sectionAwareness.isSectionMemberInRole(sectionUid, userUid, Role.TA);
    }

    public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, Long itemId, String studentUid) {

        if (itemId == null || studentUid == null || gradebookUid == null) {
            throw new IllegalArgumentException("Null parameter(s) in AuthzSectionsServiceImpl.isUserAbleToGradeItemForStudent");
        }

        if (isUserAbleToGradeAll(gradebookUid)) {
            return GradingService.gradePermission;
        }

        String userUid = sessionManager.getCurrentSessionUserId();

        List<CourseSection> viewableSections = getViewableSections(gradebookUid);

        if (gradingPermissionService.currentUserHasGraderPermissions(gradebookUid)) {

            // get the map of authorized item (assignment) ids to grade/view function
            Map<Long, String> itemIdFunctionMap
                = gradingPermissionService.getAvailableItemsForStudent(
                    gradebookUid, userUid, studentUid, viewableSections);

            if (itemIdFunctionMap == null || itemIdFunctionMap.isEmpty()) {
                return null;  // not authorized to grade/view any items for this student
            }

            String functionValueForItem = (String)itemIdFunctionMap.get(itemId);
            String view = GradingService.viewPermission;
            String grade = GradingService.gradePermission;

            if (functionValueForItem != null) {
                if (functionValueForItem.equalsIgnoreCase(grade)) {
                    return GradingService.gradePermission;
                }

                if (functionValueForItem.equalsIgnoreCase(view)) {
                    return GradingService.viewPermission;
                }
            }

            return null;

        } else {
            // use OOTB permissions based upon TA section membership
            List<String> sectionIds = viewableSections.stream().map(CourseSection::getUuid).collect(Collectors.toList());
            for (String sectionUuid : sectionIds) {
                if (isUserTAinSection(sectionUuid) && sectionAwareness.isSectionMemberInRole(sectionUuid, studentUid, Role.STUDENT)) {
                    return GradingService.gradePermission;
                }
            }

            return null;
        }
    }

    private boolean isUserAbleToGradeOrViewItemForStudent(String gradebookUid, Long itemId, String studentUid, String function) throws IllegalArgumentException {

        if (itemId == null || studentUid == null || function == null) {
            throw new IllegalArgumentException("Null parameter(s) in AuthzSectionsServiceImpl.isUserAbleToGradeItemForStudent");
        }

        if (isUserAbleToGradeAll(gradebookUid)) {
            return true;
        }

        String userUid = sessionManager.getCurrentSessionUserId();

        List<CourseSection> viewableSections = getViewableSections(gradebookUid);
        List<String> sectionIds = new ArrayList<>();
        if (!viewableSections.isEmpty()) {
            viewableSections.forEach(cs -> sectionIds.add(cs.getUuid()));
        }

        if (gradingPermissionService.currentUserHasGraderPermissions(gradebookUid)) {

            // get the map of authorized item (assignment) ids to grade/view function
            Map<Long, String> itemIdFunctionMap = gradingPermissionService.getAvailableItemsForStudent(gradebookUid, userUid, studentUid, viewableSections);

            if (itemIdFunctionMap == null || itemIdFunctionMap.isEmpty()) {
                return false;  // not authorized to grade/view any items for this student
            }

            String functionValueForItem = (String)itemIdFunctionMap.get(itemId);
            String view = GradingService.viewPermission;
            String grade = GradingService.gradePermission;

            if (functionValueForItem != null) {
                if (function.equalsIgnoreCase(grade) && functionValueForItem.equalsIgnoreCase(grade))
                    return true;

                if (function.equalsIgnoreCase(view) && (functionValueForItem.equalsIgnoreCase(grade) || functionValueForItem.equalsIgnoreCase(view)))
                    return true;
            }

            return false;

        } else {
            // use OOTB permissions based upon TA section membership
            for (String sectionUuid : sectionIds) {
                if (isUserTAinSection(sectionUuid) && sectionAwareness.isSectionMemberInRole(sectionUuid, studentUid, Role.STUDENT)) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean isUserAbleToGradeItemForStudent(String gradebookUid, Long itemId, String studentUid) throws IllegalArgumentException {

        return isUserAbleToGradeOrViewItemForStudent(gradebookUid, itemId, studentUid, GradingService.gradePermission);
    }

    public boolean isUserAbleToViewItemForStudent(String gradebookUid, Long itemId, String studentUid) throws IllegalArgumentException {

        return isUserAbleToGradeOrViewItemForStudent(gradebookUid, itemId, studentUid, GradingService.viewPermission);
    }

    public List<CourseSection> getViewableSections(String gradebookUid) {

        List<CourseSection> viewableSections = new ArrayList<>();

        List<CourseSection> allSections = getAllSections(gradebookUid);
        if (allSections.isEmpty()) {
            return viewableSections;
        }

        if (isUserAbleToGradeAll(gradebookUid)) {
            return allSections;
        }

        final Map<String, CourseSection> sectionIdCourseSectionMap
            = allSections.stream().collect(Collectors.toMap(s -> s.getUuid(), s -> s));

        String userUid = sessionManager.getCurrentSessionUserId();

        if (gradingPermissionService.userHasGraderPermissions(gradebookUid, userUid)) {

            List<String> viewableSectionIds =  gradingPermissionService.getViewableGroupsForUser(gradebookUid, userUid, new ArrayList<>(sectionIdCourseSectionMap.keySet()));
            if (viewableSectionIds != null && !viewableSectionIds.isEmpty()) {
                for (String sectionUuid : viewableSectionIds) {
                    CourseSection viewableSection = sectionIdCourseSectionMap.get(sectionUuid);
                    if (viewableSection != null) {
                        viewableSections.add(viewableSection);
                    }
                }
            }
        } else {
            // return all sections that the current user is a TA for
            for (Map.Entry<String, CourseSection> entry : sectionIdCourseSectionMap.entrySet()) {
                String sectionUuid = entry.getKey();
                if (isUserTAinSection(sectionUuid)) {
                    CourseSection viewableSection = sectionIdCourseSectionMap.get(sectionUuid);
                    if (viewableSection != null) {
                        viewableSections.add(viewableSection);
                    }
                }
            }
        }

        Collections.sort(viewableSections);

        return viewableSections;
    }

    public List<CourseSection> getAllSections(String gradebookUid) {

        return sectionAwareness.getSections(gradebookUid);
    }

    private List<EnrollmentRecord> getSectionEnrollmentsTrusted(String sectionUid) {

        return sectionAwareness.getSectionMembersInRole(sectionUid, Role.STUDENT);
    }

    public Map<EnrollmentRecord, String> findMatchingEnrollmentsForItem(String gradebookUid, Long categoryId, GradingCategoryType gbCategoryType, String optionalSearchString, String optionalSectionUid) {

        String userUid = sessionManager.getCurrentSessionUserId();
        return findMatchingEnrollmentsForItemOrCourseGrade(userUid, gradebookUid, categoryId, gbCategoryType, optionalSearchString, optionalSectionUid, false);
    }

    public Map<EnrollmentRecord, String> findMatchingEnrollmentsForItemForUser(String userUid, String gradebookUid, Long categoryId, GradingCategoryType gbCategoryType, String optionalSearchString, String optionalSectionUid) {

        return findMatchingEnrollmentsForItemOrCourseGrade(userUid, gradebookUid, categoryId, gbCategoryType, optionalSearchString, optionalSectionUid, false);
    }

    public Map<EnrollmentRecord, String> findMatchingEnrollmentsForViewableCourseGrade(String gradebookUid, GradingCategoryType gbCategoryType, String optionalSearchString, String optionalSectionUid) {

        String userUid = sessionManager.getCurrentSessionUserId();
        return findMatchingEnrollmentsForItemOrCourseGrade(userUid, gradebookUid, null, gbCategoryType, optionalSearchString, optionalSectionUid, true);
    }

    public Map<EnrollmentRecord, Map<Long, String>> findMatchingEnrollmentsForViewableItems(String gradebookUid, List allGbItems, String optionalSearchString, String optionalSectionUid) {

        Map<EnrollmentRecord, Map<Long, String>> enrollmentMap = new HashMap<>();
        List<EnrollmentRecord> filteredEnrollments = null;
        if (optionalSearchString != null) {
            filteredEnrollments = sectionAwareness.findSiteMembersInRole(gradebookUid, Role.STUDENT, optionalSearchString);
        } else {
            filteredEnrollments = sectionAwareness.getSiteMembersInRole(gradebookUid, Role.STUDENT);
        }

        if (filteredEnrollments == null || filteredEnrollments.isEmpty()) {
            return enrollmentMap;
        }

        // get all the students in the filtered section, if appropriate
        Map<String, EnrollmentRecord> studentsInSectionMap = new HashMap<>();
        if (optionalSectionUid !=  null) {
            List<EnrollmentRecord> sectionMembers = getSectionEnrollmentsTrusted(optionalSectionUid);
            if (!sectionMembers.isEmpty()) {
                sectionMembers.forEach(m -> studentsInSectionMap.put(m.getUser().getUserUid(), m));
            }
        }

        Map<String, EnrollmentRecord> studentIdEnrRecMap = new HashMap<>();
        for (EnrollmentRecord enr : filteredEnrollments) {
            String studentId = enr.getUser().getUserUid();
            if (optionalSectionUid != null) {
                if (studentsInSectionMap.containsKey(studentId)) {
                    studentIdEnrRecMap.put(studentId, enr);
                }
            } else {
                studentIdEnrRecMap.put(studentId, enr);
            }
        }

        if (isUserAbleToGradeAll(gradebookUid)) {
            List<EnrollmentRecord> enrollments = new ArrayList<>(studentIdEnrRecMap.values());

            HashMap<Long, String> assignFunctionMap = new HashMap<>();
            if (allGbItems != null && !allGbItems.isEmpty()) {
                for (Object assign : allGbItems) {
                    Long assignId = null;
                    if (assign instanceof Assignment) {
                        assignId = ((Assignment) assign).getId();
                    } else if (assign instanceof GradebookAssignment) {
                        assignId = ((GradebookAssignment)assign).getId();
                    }

                    if (assignId != null) {
                        assignFunctionMap.put(assignId, GradingService.gradePermission);
                    }
                }
            }

            enrollments.forEach(e -> enrollmentMap.put(e, assignFunctionMap));
        } else {
            String userId = sessionManager.getCurrentSessionUserId();

            Map<String, CourseSection> sectionIdCourseSectionMap = getViewableSections(gradebookUid)
                .stream().collect(Collectors.toMap(s -> s.getUuid(), s -> s));

            if (gradingPermissionService.currentUserHasGraderPermissions(gradebookUid)) {
                // user has special grader permissions that override default perms

                List<String> myStudentIds = new ArrayList<>(studentIdEnrRecMap.keySet());

                List<CourseSection> selSections = new ArrayList<>();
                if (optionalSectionUid == null) {
                    // pass all sections
                    selSections = new ArrayList<>(sectionIdCourseSectionMap.values());
                } else {
                    // only pass the selected section
                    CourseSection section = sectionIdCourseSectionMap.get(optionalSectionUid);
                    if (section != null)
                        selSections.add(section);
                }

                // we need to get the viewable students, so first create section id --> student ids map
                myStudentIds = gradingPermissionService.getViewableStudentsForUser(gradebookUid, userId, myStudentIds, selSections);
                Map<String, Map<Long, String>> viewableStudentIdItemsMap = new HashMap<>();
                if (allGbItems == null || allGbItems.isEmpty()) {
                    if (myStudentIds != null) {
                        for (String studentId : myStudentIds) {
                            if (studentId != null) {
                                viewableStudentIdItemsMap.put(studentId, null);
                            }
                        }
                    }
                } else {
                    viewableStudentIdItemsMap = gradingPermissionService.getAvailableItemsForStudents(gradebookUid, userId, myStudentIds, selSections);
                }

                if (!viewableStudentIdItemsMap.isEmpty()) {
                    for (Map.Entry<String, Map<Long, String>> entry : viewableStudentIdItemsMap.entrySet()) {
                        String studentId = entry.getKey();
                        EnrollmentRecord enrRec = studentIdEnrRecMap.get(studentId);
                        if (enrRec != null) {
                            enrollmentMap.put(enrRec, entry.getValue());
                        }
                    }
                }

            } else {
                // use default section-based permissions

                // Determine the current user's section memberships
                List<String> availableSections = new ArrayList<>();
                if (optionalSectionUid != null && isUserTAinSection(optionalSectionUid)) {
                    if (sectionIdCourseSectionMap.containsKey(optionalSectionUid))
                        availableSections.add(optionalSectionUid);
                } else {
                    for (String sectionUuid : sectionIdCourseSectionMap.keySet()) {
                        if (isUserTAinSection(sectionUuid)) {
                            availableSections.add(sectionUuid);
                        }
                    }
                }

                // Determine which enrollees are in these sections
                Map<String, EnrollmentRecord> uniqueEnrollees = new HashMap<>();
                for (String sectionUuid : availableSections) {
                    List<EnrollmentRecord> sectionEnrollments = getSectionEnrollmentsTrusted(sectionUuid);
                    sectionEnrollments.forEach(e -> uniqueEnrollees.put(e.getUser().getUserUid(), e));
                }

                // Filter out based upon the original filtered students
                for (String enrId : studentIdEnrRecMap.keySet()) {
                    if (uniqueEnrollees.containsKey(enrId)) {
                        // iterate through the assignments
                        Map<Long, String> itemFunctionMap = new HashMap<>();
                        if (allGbItems != null && !allGbItems.isEmpty()) {
                            for (Object assign : allGbItems) {
                                Long assignId = null;
                                if (assign instanceof org.sakaiproject.grading.api.Assignment) {
                                    assignId = ((org.sakaiproject.grading.api.Assignment)assign).getId();
                                } else if (assign instanceof GradebookAssignment) {
                                    assignId = ((GradebookAssignment)assign).getId();
                                }

                                if (assignId != null) {
                                    itemFunctionMap.put(assignId, GradingService.gradePermission);
                                }
                            }
                        }
                        enrollmentMap.put(studentIdEnrRecMap.get(enrId), itemFunctionMap);
                    }
                }
            }
        }

        return enrollmentMap;
    }

    /**
     * @param userUid
     * @param gradebookUid
     * @param categoryId
     * @param optionalSearchString
     * @param optionalSectionUid
     * @param itemIsCourseGrade
     * @return Map of EnrollmentRecord --> View or Grade
     */
    private Map<EnrollmentRecord, String> findMatchingEnrollmentsForItemOrCourseGrade(String userUid, String gradebookUid, Long categoryId, GradingCategoryType gbCategoryType, String optionalSearchString, String optionalSectionUid, boolean itemIsCourseGrade) {

        Map<EnrollmentRecord, String> enrollmentMap = new HashMap<>();
        List<EnrollmentRecord> filteredEnrollments = new ArrayList<>();

        if (optionalSearchString != null) {
            filteredEnrollments = sectionAwareness.findSiteMembersInRole(gradebookUid, Role.STUDENT, optionalSearchString);
        } else {
            filteredEnrollments = sectionAwareness.getSiteMembersInRole(gradebookUid, Role.STUDENT);
        }

        if (filteredEnrollments.isEmpty()) {
            return enrollmentMap;
        }

        // get all the students in the filtered section, if appropriate
        Map<String, EnrollmentRecord> studentsInSectionMap = new HashMap<>();
        if (optionalSectionUid !=  null) {
            List<EnrollmentRecord> sectionMembers = sectionAwareness.getSectionMembersInRole(optionalSectionUid, Role.STUDENT);
            if (!sectionMembers.isEmpty()) {
                sectionMembers.forEach(m -> studentsInSectionMap.put(m.getUser().getUserUid(), m));
            }
        }

        Map<String, EnrollmentRecord> studentIdEnrRecMap = new HashMap<>();
        for (EnrollmentRecord enr : filteredEnrollments) {
            String studentId = enr.getUser().getUserUid();
            if (optionalSectionUid != null) {
                if (studentsInSectionMap.containsKey(studentId)) {
                    studentIdEnrRecMap.put(studentId, enr);
                }
            } else {
                studentIdEnrRecMap.put(enr.getUser().getUserUid(), enr);
            }
        }

        if (isUserAbleToGradeAll(gradebookUid, userUid)) {
            studentIdEnrRecMap.values().forEach(e -> enrollmentMap.put(e, GradingService.gradePermission));
        } else {
            Map<String, CourseSection> sectionIdCourseSectionMap = new HashMap<>();
            getAllSections(gradebookUid).forEach(cs -> sectionIdCourseSectionMap.put(cs.getUuid(), cs));

            if (gradingPermissionService.userHasGraderPermissions(gradebookUid, userUid)) {
                // user has special grader permissions that override default perms

                List<String> myStudentIds = new ArrayList<>(studentIdEnrRecMap.keySet());

                List<CourseSection> selSections = new ArrayList<>();
                if (optionalSectionUid == null) {
                    // pass all sections
                    selSections = new ArrayList<>(sectionIdCourseSectionMap.values());
                } else {
                    // only pass the selected section
                    CourseSection section = sectionIdCourseSectionMap.get(optionalSectionUid);
                    if (section != null) {
                        selSections.add(section);
                    }
                }

                Map<String, String> viewableEnrollees = new HashMap<>();
                if (itemIsCourseGrade) {
                    viewableEnrollees = gradingPermissionService.getCourseGradePermission(gradebookUid, userUid, myStudentIds, selSections);
                } else {
                    viewableEnrollees = gradingPermissionService.getStudentsForItem(gradebookUid, userUid, myStudentIds, gbCategoryType, categoryId, selSections);
                }

                if (!viewableEnrollees.isEmpty()) {
                    for (Map.Entry<String, String> entry : viewableEnrollees.entrySet()) {
                        String studentId = entry.getKey();
                        EnrollmentRecord enrRec = studentIdEnrRecMap.get(studentId);
                        if (enrRec != null) {
                            enrollmentMap.put(enrRec, entry.getValue());
                        }
                    }
                }

            } else {
                // use default section-based permissions
                return getEnrollmentMapUsingDefaultPermissions(userUid, studentIdEnrRecMap, sectionIdCourseSectionMap, optionalSectionUid);
            }
        }

        return enrollmentMap;
    }

    /**
     *
     * @param userUid
     * @param studentIdEnrRecMap
     * @param sectionIdCourseSectionMap
     * @param optionalSectionUid
     * @return Map of EnrollmentRecord to function view/grade using the default permissions (based on TA section membership)
     */
    private Map<EnrollmentRecord, String> getEnrollmentMapUsingDefaultPermissions(String userUid, Map<String, EnrollmentRecord> studentIdEnrRecMap, Map<String, CourseSection> sectionIdCourseSectionMap, String optionalSectionUid) {

        // Determine the current user's section memberships
        Map<EnrollmentRecord, String> enrollmentMap = new HashMap<>();
        List<String> availableSections = new ArrayList<>();
        if (optionalSectionUid != null && isUserTAinSection(optionalSectionUid, userUid)) {
            if (sectionIdCourseSectionMap.containsKey(optionalSectionUid)) {
                availableSections.add(optionalSectionUid);
            }
        } else {
            for (String sectionUuid : sectionIdCourseSectionMap.keySet()) {
                if (isUserTAinSection(sectionUuid, userUid)) {
                    availableSections.add(sectionUuid);
                }
            }
        }

        // Determine which enrollees are in these sections
        Map<String, EnrollmentRecord> uniqueEnrollees = new HashMap<>();
        for (String sectionUuid : availableSections) {
            getSectionEnrollmentsTrusted(sectionUuid)
                .forEach(e -> uniqueEnrollees.put(e.getUser().getUserUid(), e));
        }

        // Filter out based upon the original filtered students
        for (String enrId : studentIdEnrRecMap.keySet()) {
            if (uniqueEnrollees.containsKey(enrId)) {
                enrollmentMap.put(studentIdEnrRecMap.get(enrId), GradingService.gradePermission);
            }
        }

        return enrollmentMap;
    }

    /*
    public List<Group> findStudentSectionMemberships(String gradebookUid, String studentUid) {

        List<Group> sectionMemberships = new ArrayList<>();
        try {
            sectionMemberships = siteService.getSite(gradebookUid).getGroupsWithMember(studentUid);
        } catch (IdUnusedException e) {
            log.error("No site with id = " + gradebookUid);
        }

        return sectionMemberships;
    }

    public List getStudentSectionMembershipNames(String gradebookUid, String studentUid) {

        List<String> sectionNames = new ArrayList<>();
        List<Group> sections = findStudentSectionMemberships(gradebookUid, studentUid);
        if (sections != null && !sections.isEmpty()) {
            sections.forEach(g -> sectionNames.add(myGroup.getTitle()));
        }

        return sectionNames;
    }
    */
}
