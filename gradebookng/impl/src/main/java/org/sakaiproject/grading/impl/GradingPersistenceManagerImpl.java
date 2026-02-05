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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.grading.api.GradingPersistenceManager;
import org.sakaiproject.grading.api.model.AssignmentGradeRecord;
import org.sakaiproject.grading.api.model.Category;
import org.sakaiproject.grading.api.model.Comment;
import org.sakaiproject.grading.api.model.CourseGrade;
import org.sakaiproject.grading.api.model.CourseGradeRecord;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.grading.api.model.GradebookProperty;
import org.sakaiproject.grading.api.model.GradeMapping;
import org.sakaiproject.grading.api.model.GradingEvent;
import org.sakaiproject.grading.api.model.GradingScale;
import org.sakaiproject.grading.api.model.LetterGradePercentMapping;
import org.sakaiproject.grading.api.model.Permission;

import org.sakaiproject.grading.api.repository.AssignmentGradeRecordRepository;
import org.sakaiproject.grading.api.repository.CategoryRepository;
import org.sakaiproject.grading.api.repository.CommentRepository;
import org.sakaiproject.grading.api.repository.CourseGradeRepository;
import org.sakaiproject.grading.api.repository.CourseGradeRecordRepository;
import org.sakaiproject.grading.api.repository.GradebookAssignmentRepository;
import org.sakaiproject.grading.api.repository.GradebookRepository;
import org.sakaiproject.grading.api.repository.GradebookPropertyRepository;
import org.sakaiproject.grading.api.repository.GradeMappingRepository;
import org.sakaiproject.grading.api.repository.GradingEventRepository;
import org.sakaiproject.grading.api.repository.GradingScaleRepository;
import org.sakaiproject.grading.api.repository.LetterGradePercentMappingRepository;
import org.sakaiproject.grading.api.repository.PermissionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class GradingPersistenceManagerImpl implements GradingPersistenceManager {

    @Autowired protected AssignmentGradeRecordRepository assignmentGradeRecordRepository;
    @Autowired protected CategoryRepository categoryRepository;
    @Autowired protected CommentRepository commentRepository;
    @Autowired protected CourseGradeRepository courseGradeRepository;
    @Autowired protected CourseGradeRecordRepository courseGradeRecordRepository;
    @Autowired protected GradebookAssignmentRepository gradebookAssignmentRepository;
    @Autowired protected GradebookRepository gradebookRepository;
    @Autowired protected GradebookPropertyRepository gradebookPropertyRepository;
    @Autowired protected GradeMappingRepository gradeMappingRepository;
    @Autowired protected GradingEventRepository gradingEventRepository;
    @Autowired protected GradingScaleRepository gradingScaleRepository;
    @Autowired protected LetterGradePercentMappingRepository letterGradePercentMappingRepository;
    @Autowired protected PermissionRepository permissionRepository;

    public Gradebook saveGradebook(Gradebook gradebook) {
        return gradebookRepository.save(gradebook);
    }

    @Transactional
    public void deleteGradebook(String gradebookUid) {

        Gradebook gradebook = gradebookRepository.findByUid(gradebookUid)
            .orElseThrow(() -> new IllegalArgumentException("No gradebook with uid " + gradebookUid));
        Long gradebookId = gradebook.getId();

        gradingEventRepository.deleteAll(gradingEventRepository.findByGradableObject_Gradebook_Uid(gradebookUid));

        commentRepository.deleteAll(commentRepository.findByGradableObject_Gradebook_Uid(gradebookUid));

        assignmentGradeRecordRepository.deleteAll(assignmentGradeRecordRepository.findByGradableObject_Gradebook_Uid(gradebookUid));

        courseGradeRecordRepository.deleteAll(courseGradeRecordRepository.findByGradableObject_Gradebook_Uid(gradebookUid));

        gradebookAssignmentRepository.deleteAll(gradebookAssignmentRepository.findByGradebook_Uid(gradebookUid));

        courseGradeRepository.deleteAll(courseGradeRepository.findByGradebook_Uid(gradebookUid));

        categoryRepository.deleteAll(categoryRepository.findByGradebook_Uid(gradebookUid));

        gradebookRepository.deleteSpreadsheetsForGradebook(gradebook.getId());

        permissionRepository.deleteAll(permissionRepository.findByGradebookId(gradebook.getId()));

        gradeMappingRepository.deleteAll(gradeMappingRepository.findByGradebook_Uid(gradebookUid));

        gradebookRepository.delete(gradebook);
    }

    public Optional<Gradebook> getGradebook(String gradebookUid) {
        return gradebookRepository.findByUid(gradebookUid);
    }

    public Optional<Gradebook> getGradebook(Long gradebookId) {
        return gradebookRepository.findById(gradebookId);
    }

    public CourseGrade saveCourseGrade(CourseGrade courseGrade) {
        return courseGradeRepository.save(courseGrade);
    }

    public List<CourseGrade> getCourseGradesByGradebookId(Long gradebookId) {
        return courseGradeRepository.findByGradebook_Id(gradebookId);
    }

    public List<GradingScale> getAvailableGradingScales() {
        return gradingScaleRepository.findByUnavailable(false);
    }

    public GradingScale saveGradingScale(GradingScale gradingScale) {
        return gradingScaleRepository.save(gradingScale);
    }

    public LetterGradePercentMapping saveLetterGradePercentMapping(LetterGradePercentMapping lgpm) {
        return letterGradePercentMappingRepository.save(lgpm);
    }

    public List<LetterGradePercentMapping> getDefaultLetterGradePercentMappings() {
        return letterGradePercentMappingRepository.findByMappingType(1);
    }

    public Optional<LetterGradePercentMapping> getLetterGradePercentMappingForGradebook(Long gradebookId) {
        return letterGradePercentMappingRepository.findByGradebookIdAndMappingType(gradebookId, 2);
    }

    public Optional<GradeMapping> getGradeMapping(Long id) {
        return gradeMappingRepository.findById(id);
    }

    public GradeMapping saveGradeMapping(GradeMapping gradeMapping) {
        return gradeMappingRepository.save(gradeMapping);
    }

    public List<GradingScale> getOtherAvailableGradingScales(Set<String> notTheseUids) {
        return gradingScaleRepository.findByUnavailableAndUidNotIn(false, notTheseUids);
    }

    public List<GradingScale> getGradingScalesByUids(Set<String> theseUids) {
        return gradingScaleRepository.findByUidIn(theseUids);
    }

    public GradebookAssignment saveAssignment(GradebookAssignment assignment) {
        return gradebookAssignmentRepository.save(assignment);
    }

    public Optional<GradebookAssignment> getAssignmentByNameAndGradebook(String name, String gradebookUid) {
        return gradebookAssignmentRepository.findByNameAndGradebook_UidAndRemoved(name, gradebookUid, false);
    }

    public List<GradebookAssignment> getAssignmentsForGradebook(Long gradebookId) {
        return gradebookAssignmentRepository.findByGradebook_IdAndRemoved(gradebookId, false);
    }

    public List<GradebookAssignment> getAssignmentsForGradebookAndCategoryId(Long gradebookId, Long categoryId) {
        return gradebookAssignmentRepository.findByGradebook_IdAndCategory_IdAndRemoved(gradebookId, categoryId, false);
    }

    public List<GradebookAssignment> getAssignmentsForCategory(Long categoryId) {
        return gradebookAssignmentRepository.findByCategory_IdAndRemoved(categoryId, false);
    }

    public List<GradebookAssignment> getCountedAssignmentsForGradebook(Long gradebookId) {
        return gradebookAssignmentRepository.findByGradebook_IdAndRemovedAndNotCounted(gradebookId, false, false);
    }

    public List<GradebookAssignment> getCountedAndGradedAssignmentsForGradebook(Long gradebookId) {
        return gradebookAssignmentRepository.findByGradebook_IdAndRemovedAndNotCountedAndUngraded(gradebookId, false, false, false);
    }

    public Optional<GradebookAssignment> getAssignmentByIdAndGradebook(Long id, String gradebookUid) {
        return gradebookAssignmentRepository.findByIdAndGradebook_UidAndRemoved(id, gradebookUid, false);
    }

    public Optional<GradebookAssignment> getAssignmentById(Long id) {
        return gradebookAssignmentRepository.findById(id);
    }

    public Long countAssignmentsByGradbookAndExternalId(String gradebookUid, String externalId) {
        return gradebookAssignmentRepository.countByGradebook_UidAndExternalId(gradebookUid, externalId);
    }

    public Long countAssignmentsByNameAndGradebookUid(String name, String gradebookUid) {
        return gradebookAssignmentRepository.countByNameAndGradebook_UidAndRemoved(name, gradebookUid, false);
    }

    public Long countDuplicateAssignments(GradebookAssignment assignment) {

        return gradebookAssignmentRepository.countByNameAndGradebookAndNotIdAndRemoved(
            assignment.getName(), assignment.getGradebook(), assignment.getId(), false);
    }

    public void deleteAssignment(GradebookAssignment assignment) {
        gradebookAssignmentRepository.delete(assignment);
    }

    public Optional<GradebookAssignment> getExternalAssignment(String gradebookUid, String externalId) {
        return gradebookAssignmentRepository.findByGradebook_UidAndExternalId(gradebookUid, externalId);
    }

    public List<GradebookAssignment> getGradebookUidByExternalId(String externalId) {
        return gradebookAssignmentRepository.findByExternalId(externalId);
    }

    public GradebookAssignment saveGradebookAssignment(GradebookAssignment assignment) {
        return gradebookAssignmentRepository.save(assignment);
    }

    public Optional<Comment> getInternalComment(String studentUid, String gradebookUid, Long assignmentId) {

        return commentRepository.findByStudentIdAndGradableObject_Gradebook_UidAndGradableObject_IdAndGradableObject_Removed(
            studentUid, gradebookUid, assignmentId, false);
    }

    public void deleteInternalComment(String studentUid, String gradebookUid, Long assignmentId) {

        getInternalComment(studentUid, gradebookUid, assignmentId).ifPresent(commentRepository::delete);
    }

    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsForStudents(GradebookAssignment assignment, Collection<String> studentIds) {
        return commentRepository.findByGradableObjectAndStudentIdIn(assignment, studentIds);
    }

    public int deleteCommentsForAssignment(GradebookAssignment assignment) {
       return commentRepository.deleteByGradableObject(assignment);
    }

    public Optional<Category> getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public List<Category> getCategoriesForGradebook(Long gradebookId) {
        return categoryRepository.findByGradebook_IdAndRemoved(gradebookId, false);
    }

    public List<Category> getCategoriesWithAssignmentsForGradebook(Long gradebookId) {
        List<Category> categories = categoryRepository.findByGradebook_IdAndRemoved(gradebookId, false);

        if (!categories.isEmpty()) {
            List<GradebookAssignment> allAssignments = gradebookAssignmentRepository
                .findByGradebook_IdAndRemoved(gradebookId, false);

            java.util.Map<Long, List<GradebookAssignment>> assignmentsByCategory = allAssignments.stream()
                .filter(assignment -> assignment.getCategory() != null)
                .collect(java.util.stream.Collectors.groupingBy(assignment -> assignment.getCategory().getId()));

            categories.forEach(category -> {
                List<GradebookAssignment> categoryAssignments = assignmentsByCategory.getOrDefault(
                    category.getId(), java.util.Collections.emptyList());
                category.setAssignmentList(categoryAssignments);
            });
        }

        return categories;
    }

    public boolean isCategoryDefined(String name, Gradebook gradebook) {
        return categoryRepository.existsByNameAndGradebookAndRemoved(name, gradebook, false);
    }

    public boolean existsDuplicateCategory(String name, Gradebook gradebook, Long id) {
        return categoryRepository.existsByNameAndGradebookAndNotIdAndRemoved(name, gradebook, id, false);
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public boolean isAssignmentDefined(Long id) {
        return gradebookAssignmentRepository.existsByIdAndRemoved(id, false);
    }

    public List<Permission> getPermissionsForGradebookAndUser(Long gradebookId, String userId) {
        return permissionRepository.findByGradebookIdAndUserId(gradebookId, userId);
    }

    public List<Permission> getPermissionsForGradebook(Long gradebookId) {
        return permissionRepository.findByGradebookId(gradebookId);
    }

    public List<Permission> getPermissionsForGradebookAndUserAndCategories(Long gradebookId, String userId, List<Long> categoryIds) {
        return permissionRepository.findByGradebookIdAndUserIdAndCategoryIdIn(gradebookId, userId, categoryIds);
    }

    public List<Permission> getUncategorisedPermissionsForGradebookAndUserAndFunctions(Long gradebookId, String userId, List<String> functions) {
        return permissionRepository.findByGradebookIdAndUserIdAndCategoryIdIsNullAndFunctionNameIn(gradebookId, userId, functions);
    }

    public List<Permission> getUngroupedPermissionsForGradebookAndUserAndFunctions(Long gradebookId, String userId, List<String> functions) {
        return permissionRepository.findByGradebookIdAndUserIdAndGroupIdIsNullAndFunctionNameIn(gradebookId, userId, functions);
    }

    public List<Permission> getUngroupedPermissionsForGradebookAndUserAndCategories(Long gradebookId, String userId, List<Long> categoryIds) {
        return permissionRepository.findByGradebookIdAndUserIdAndGroupIdIsNullAndCategoryIdIn(gradebookId, userId, categoryIds);
    }

    public List<Permission> getPermissionsForGradebookAndCategories(Long gradebookId, List<Long> categoryIds) {
        return permissionRepository.findByGradebookIdAndCategoryIdIn(gradebookId, categoryIds);
    }

    public List<Permission> getPermissionsForGradebookAnyGroupAnyCategory(Long gradebookId, String userId) {
        return permissionRepository.findByGradebookIdAndUserIdAndCategoryIdIsNullAndGroupIdIsNull(gradebookId, userId);
    }

    public List<Permission> getUncategorisedPermissionsForGradebookAndGroups(Long gradebookId, String userId, List<String> groupIds) {
        return permissionRepository.findByGradebookIdAndUserIdAndCategoryIdIsNullAndGroupIdIn(gradebookId, userId, groupIds);
    }

    public List<Permission> getPermissionsForGradebookAndGroups(Long gradebookId, String userId, List<String> groupIds) {
        return permissionRepository.findByGradebookIdAndUserIdAndGroupIdIn(gradebookId, userId, groupIds);
    }

    public Permission savePermission(Permission permission) {
        return permissionRepository.save(permission);
    }

    public void deletePermission(Permission permission) {
        permissionRepository.delete(permission);
    }

    public CourseGradeRecord saveCourseGradeRecord(CourseGradeRecord record) {
        return courseGradeRecordRepository.save(record);
    }

    public List<CourseGradeRecord> getCourseGradeRecordsForCourseGrade(Long courseGradeId) {
        return courseGradeRecordRepository.findByGradableObject_Id(courseGradeId);
    }

    public List<CourseGradeRecord> getCourseGradeOverrides(Gradebook gradebook) {
        return courseGradeRecordRepository.findByGradableObject_GradebookAndEnteredGradeNotNull(gradebook);
    }

    public Optional<CourseGradeRecord> getCourseGradeRecord(Gradebook gradebook, String studentId) {
        return courseGradeRecordRepository.findByGradableObject_GradebookAndStudentId(gradebook, studentId);
    }

    public boolean hasCourseGradeRecordEntries(Long gradebookId, Set<String> studentIds) {
        return courseGradeRecordRepository.countByGradableObject_Gradebook_IdAndEnteredGradeNotNullAndStudentIdIn(gradebookId, studentIds) > 0L;
    }

    public List<AssignmentGradeRecord> getAllAssignmentGradeRecordsForGradebook(Long gradebookId) {

        return assignmentGradeRecordRepository
            .findByGradableObject_Gradebook_IdAndGradableObject_RemovedOrderByPointsEarned(gradebookId, false);
    }

    public List<AssignmentGradeRecord> getAllAssignmentGradeRecordsForAssignment(Long assignmentId) {

        GradeType gradeType = gradebookAssignmentRepository.findById(assignmentId).map(a -> a.getGradebook().getGradeType()).orElse(GradeType.POINTS);
        switch (gradeType) {
            case PERCENTAGE:
            case POINTS:
                return assignmentGradeRecordRepository
                    .findByGradableObject_IdAndGradableObject_RemovedOrderByPointsEarned(assignmentId, false);
            case LETTER:
                return assignmentGradeRecordRepository
                    .findByGradableObject_IdAndGradableObject_Removed(assignmentId, false);
            default:
                return Collections.<AssignmentGradeRecord>emptyList();
        }
    }

    public AssignmentGradeRecord getAssignmentGradeRecordForAssignmentAndStudent(Long assignmentId, String studentUid) {
        return assignmentGradeRecordRepository.findByGradableObject_IdAndStudentId(assignmentId, studentUid).orElse(null);
    }

    public List<AssignmentGradeRecord> getAssignmentGradeRecordsForGradebookAndStudents(Long gradebookId, Collection<String> studentIds) {
        return assignmentGradeRecordRepository.findByGradableObject_Gradebook_IdAndGradableObject_RemovedAndStudentIdIn(gradebookId, false, studentIds);
    }

    public List<AssignmentGradeRecord> getAssignmentGradeRecordsForAssignmentAndStudents(GradebookAssignment assignment, Collection<String> studentIds) {
        return assignmentGradeRecordRepository.findByGradableObjectAndStudentIdIn(assignment, studentIds);
    }

    public AssignmentGradeRecord saveAssignmentGradeRecord(AssignmentGradeRecord record) {
        return assignmentGradeRecordRepository.save(record);
    }

    public List<AssignmentGradeRecord> getAssignmentGradeRecordsForAssignmentIdsAndStudentIds(
                List<Long> gradableObjectIds, List<String> studentIds) {

        return assignmentGradeRecordRepository.findByGradableObject_RemovedAndGradableObject_IdInAndStudentIdIn(false, gradableObjectIds, studentIds);
    } 

    public int deleteGradeRecordsForAssignment(GradebookAssignment assignment) {
        return assignmentGradeRecordRepository.deleteByGradableObject(assignment);
    }

    public GradingEvent saveGradingEvent(GradingEvent ge) {
        return gradingEventRepository.save(ge);
    }

    public List<GradingEvent> getGradingEventsForAssignment(Long assignmentId, String studentId) {
        return gradingEventRepository.findByGradableObject_IdAndStudentIdOrderByDateGraded(assignmentId, studentId);
    }

    public List<GradingEvent> getGradingEventsForAssignmentsSince(List<Long> assignmentIds, Date since) {
        return gradingEventRepository.findByDateGreaterThanEqualAndGradableObject_IdIn(since, assignmentIds);
    }

    public int deleteGradingEventsForAssignment(GradebookAssignment assignment) {
        return gradingEventRepository.deleteByGradableObject(assignment);
    }

    public Optional<GradebookProperty> getGradebookProperty(String name) {
        return gradebookPropertyRepository.findByName(name);
    }

    public GradebookProperty saveGradebookProperty(GradebookProperty property) {
        return gradebookPropertyRepository.save(property);
    }

}
