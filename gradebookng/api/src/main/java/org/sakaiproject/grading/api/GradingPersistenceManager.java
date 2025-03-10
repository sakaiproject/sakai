/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.grading.api;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.sakaiproject.grading.api.model.AssignmentGradeRecord;
import org.sakaiproject.grading.api.model.Category;
import org.sakaiproject.grading.api.model.Comment;
import org.sakaiproject.grading.api.model.CourseGrade;
import org.sakaiproject.grading.api.model.CourseGradeRecord;
import org.sakaiproject.grading.api.model.GradableObject;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.grading.api.model.GradebookManager;
import org.sakaiproject.grading.api.model.GradebookProperty;
import org.sakaiproject.grading.api.model.GradeMapping;
import org.sakaiproject.grading.api.model.GradingEvent;
import org.sakaiproject.grading.api.model.GradingScale;
import org.sakaiproject.grading.api.model.LetterGradePercentMapping;
import org.sakaiproject.grading.api.model.Permission;

public interface GradingPersistenceManager {

    void createGradebookManager(GradebookManager gradebookManager);

    GradebookManager saveGradebookManager(GradebookManager gradebookManager);

    Optional<GradebookManager> getGradebookManager(String id);

    boolean existsGradebookManager(String id);

    Gradebook saveGradebook(Gradebook gradebook);
    Optional<Gradebook> getGradebook(String gradebookId);
    void deleteGradebook(String gradebookId);

    CourseGrade saveCourseGrade(CourseGrade courseGrade);
    List<CourseGrade> getCourseGradesByGradebookId(String gradebookId);

    GradingScale saveGradingScale(GradingScale gradingScale);
    List<GradingScale> getAvailableGradingScales();
    List<GradingScale> getOtherAvailableGradingScales(Set<String> notTheseUids);
    List<GradingScale> getGradingScalesByUids(Set<String> theseUids);

    LetterGradePercentMapping saveLetterGradePercentMapping(LetterGradePercentMapping lgpm);
    List<LetterGradePercentMapping> getDefaultLetterGradePercentMappings();
    Optional<LetterGradePercentMapping> getLetterGradePercentMappingForGradebook(String gradebookId);

    Optional<GradeMapping> getGradeMapping(Long id);
    GradeMapping saveGradeMapping(GradeMapping gradeMapping);

    GradebookAssignment saveAssignment(GradebookAssignment assignment);
    void deleteAssignment(GradebookAssignment assignment);
    Optional<GradebookAssignment> getAssignmentByNameAndGradebook(String name, String gradebookId);
    List<GradebookAssignment> getAssignmentsForGradebook(String gradebookId);
    List<GradebookAssignment> getAssignmentsForCategory(Long categoryId);
    Optional<GradebookAssignment> getAssignmentByIdAndGradebook(Long id, String gradebookId);
    Optional<GradebookAssignment> getAssignmentById(Long id);
    List<GradebookAssignment> getCountedAssignmentsForGradebook(String gradebookId);
    List<GradebookAssignment> getCountedAndGradedAssignmentsForGradebook(String gradebookId);
    Long countAssignmentsByGradbookAndExternalId(String gradebookId, String externalId);
    Long countAssignmentsByNameAndGradebookUid(String name, String gradebookUid);
    Long countDuplicateAssignments(GradebookAssignment assignment);
    Optional<GradebookAssignment> getExternalAssignment(String gradebookId, String externalId);
    GradebookAssignment saveGradebookAssignment(GradebookAssignment assignment);

    Optional<Comment> getInternalComment(String studentUid, String gradebookId, Long gradableId);
    Comment saveComment(Comment comment);
    List<Comment> getCommentsForStudents(GradebookAssignment assignment, Collection<String> studentIds);
    void deleteInternalComment(String studentUid, String gradebookId, Long assignmentId);
    int deleteCommentsForAssignment(GradebookAssignment assignment);

    Optional<Category> getCategory(Long categoryId);
    List<Category> getCategoriesForGradebook(String gradebookId);
    boolean isCategoryDefined(String name, Gradebook gradebook);
    boolean existsDuplicateCategory(String name, Gradebook gradebook, Long id);
    Category saveCategory(Category category);

    boolean isAssignmentDefined(Long assignmentId);

    List<Permission> getPermissionsForGradebook(Long gradebookId);
    List<Permission> getPermissionsForGradebookAndUser(String gradebookId, String userId);
    List<Permission> getPermissionsForGradebookAndUserAndCategories(String gradebookId, String userId, List<Long> categoryIds);
    List<Permission> getUncategorisedPermissionsForGradebookAndUserAndFunctions(String gradebookId, String userId, List<String> functionNames);
    List<Permission> getUngroupedPermissionsForGradebookAndUserAndFunctions(String gradebookId, String userId, List<String> functionNames);
    List<Permission> getUngroupedPermissionsForGradebookAndUserAndCategories(String gradebookId, String userId, List<Long> categoryIds);
    List<Permission> getPermissionsForGradebookAndCategories(Long gradebookId, List<Long> categoryIds);
    List<Permission> getPermissionsForGradebookAnyGroupAnyCategory(String gradebookId, String userId);
    List<Permission> getUncategorisedPermissionsForGradebookAndGroups(String gradebookId, String userId, List<String> groupIds);
    List<Permission> getPermissionsForGradebookAndGroups(String gradebookId, String userId, List<String> groupIds);
    Permission savePermission(Permission permission);
    void deletePermission(Permission permission);

    List<CourseGradeRecord> getCourseGradeRecordsForCourseGrade(Long courseGradeId);
    CourseGradeRecord saveCourseGradeRecord(CourseGradeRecord courseGradeRecord);
    List<CourseGradeRecord> getCourseGradeOverrides(Gradebook gradebook);
    Optional<CourseGradeRecord> getCourseGradeRecord(Gradebook gradebook, String studentId);
    boolean hasCourseGradeRecordEntries(String gradebookId, Set<String> studentIds);

    List<AssignmentGradeRecord> getAllAssignmentGradeRecordsForGradebook(String gradebookId);
    List<AssignmentGradeRecord> getAllAssignmentGradeRecordsForAssignment(Long assignmentId);
    AssignmentGradeRecord getAssignmentGradeRecordForAssignmentAndStudent(Long assignmentId, String studentUid);
    AssignmentGradeRecord saveAssignmentGradeRecord(AssignmentGradeRecord record);
    int deleteGradeRecordsForAssignment(GradebookAssignment assignment);
    List<AssignmentGradeRecord> getAssignmentGradeRecordsForAssignmentIdsAndStudentIds(
                List<Long> gradableObjectIds, List<String> studentUids);
    List<AssignmentGradeRecord> getAssignmentGradeRecordsForGradebookAndStudents(String gradebookId, Collection<String> studentIds);
    List<AssignmentGradeRecord> getAssignmentGradeRecordsForAssignmentAndStudents(GradebookAssignment assignment, Collection<String> studentIds);

    GradingEvent saveGradingEvent(GradingEvent ge);
    List<GradingEvent> getGradingEventsForAssignment(Long assignmentId, String studentId);
    List<GradingEvent> getGradingEventsForAssignmentsSince(List<Long> assignmentIds, Date since);
    int deleteGradingEventsForAssignment(GradebookAssignment assignment);

    Optional<GradebookProperty> getGradebookProperty(String name);
    GradebookProperty saveGradebookProperty(GradebookProperty property);

    Optional<GradableObject> getGradableByIdAndGradebook(String gradebookId, Long gradableObjectId);
}
