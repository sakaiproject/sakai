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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.sakaiproject.grading.api.GradingPersistenceManager;
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

import org.sakaiproject.grading.api.repository.AssignmentGradeRecordRepository;
import org.sakaiproject.grading.api.repository.CategoryRepository;
import org.sakaiproject.grading.api.repository.CommentRepository;
import org.sakaiproject.grading.api.repository.CourseGradeRepository;
import org.sakaiproject.grading.api.repository.CourseGradeRecordRepository;
import org.sakaiproject.grading.api.repository.GradebookAssignmentRepository;
import org.sakaiproject.grading.api.repository.GradebookRepository;
import org.sakaiproject.grading.api.repository.GradebookManagerRepository;
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
    @Autowired protected GradebookManagerRepository gradebookManagerRepository;
    @Autowired protected GradebookPropertyRepository gradebookPropertyRepository;
    @Autowired protected GradeMappingRepository gradeMappingRepository;
    @Autowired protected GradingEventRepository gradingEventRepository;
    @Autowired protected GradingScaleRepository gradingScaleRepository;
    @Autowired protected LetterGradePercentMappingRepository letterGradePercentMappingRepository;
    @Autowired protected PermissionRepository permissionRepository;

    @Override
    public void createGradebookManager(GradebookManager gradebookManager) {
        gradebookManagerRepository.createGradebookManager(gradebookManager);
    }

    @Override
    public GradebookManager saveGradebookManager(GradebookManager gradebookManager) {
        return gradebookManagerRepository.save(gradebookManager);
    }

    @Override
    public Optional<GradebookManager> getGradebookManager(String id) {
        return gradebookManagerRepository.findById(id);
    }

    @Override
    public boolean existsGradebookManager(String id) {
        return gradebookManagerRepository.existsById(id);
    }


    public Gradebook saveGradebook(Gradebook gradebook) {
        return gradebookRepository.save(gradebook);
    }

    @Transactional
    public void deleteGradebook(String gradebookId) {

        Gradebook gradebook = gradebookRepository.findById(gradebookId)
            .orElseThrow(() -> new IllegalArgumentException("No gradebook with id " + gradebookId));

        gradingEventRepository.deleteAll(gradingEventRepository.findByGradableObject_GradebookId(gradebookId));

        commentRepository.deleteAll(commentRepository.findByGradableObjectGradebookId(gradebookId));

        assignmentGradeRecordRepository.deleteAll(assignmentGradeRecordRepository.findByGradableObject_GradebookId(gradebookId));

        courseGradeRecordRepository.deleteAll(courseGradeRecordRepository.findByGradableObject_GradebookId(gradebookId));

        gradebookAssignmentRepository.deleteAll(gradebookAssignmentRepository.findByGradebookId(gradebookId));

        courseGradeRepository.deleteAll(courseGradeRepository.findByGradebookId(gradebookId));

        categoryRepository.deleteAll(categoryRepository.findByGradebookId(gradebookId));

        gradeMappingRepository.deleteAll(gradeMappingRepository.findByGradebookId(gradebookId));

        GradebookManager gradebookManager = gradebook.getGradebookManager();
        Set<String> contextIds = gradebookManager.getContextMapping().entrySet().stream()
                .filter(e -> e.getValue().equals(gradebookId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        gradebookManager.getGroups().removeAll(contextIds);
        gradebookManager.getContextMapping().entrySet().removeIf(e -> e.getValue().equals(gradebookId));
        gradebookManager.getGradebooks().removeIf(g -> g.getId().equals(gradebookId));
        gradebook.setGradebookManager(null);
        gradebookManagerRepository.save(gradebookManager);
        gradebookRepository.deleteById(gradebookId);
    }

    public Optional<Gradebook> getGradebook(String gradebookId) {
        return gradebookRepository.findById(gradebookId);
    }

    public CourseGrade saveCourseGrade(CourseGrade courseGrade) {
        return courseGradeRepository.save(courseGrade);
    }

    public List<CourseGrade> getCourseGradesByGradebookId(String gradebookId) {
        return courseGradeRepository.findByGradebookId(gradebookId);
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

    public Optional<LetterGradePercentMapping> getLetterGradePercentMappingForGradebook(String gradebookId) {
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

    public Optional<GradebookAssignment> getAssignmentByNameAndGradebook(String name, String gradebookId) {
        return gradebookAssignmentRepository.findByNameAndGradebookIdAndRemoved(name, gradebookId, false);
    }

    public List<GradebookAssignment> getAssignmentsForGradebook(String gradebookId) {
        return gradebookAssignmentRepository.findByGradebookIdAndRemoved(gradebookId, false);
    }

    public List<GradebookAssignment> getAssignmentsForCategory(Long categoryId) {
        return gradebookAssignmentRepository.findByCategoryIdAndRemoved(categoryId, false);
    }

    public List<GradebookAssignment> getCountedAssignmentsForGradebook(String gradebookId) {
        return gradebookAssignmentRepository.findByGradebookIdAndRemovedAndNotCounted(gradebookId, false, false);
    }

    public List<GradebookAssignment> getCountedAndGradedAssignmentsForGradebook(String gradebookId) {
        return gradebookAssignmentRepository.findByGradebookIdAndRemovedAndNotCountedAndUngraded(gradebookId, false, false, false);
    }

    public Optional<GradebookAssignment> getAssignmentByIdAndGradebook(Long id, String gradebookId) {
        return gradebookAssignmentRepository.findByIdAndGradebookIdAndRemoved(id, gradebookId, false);
    }

    public Optional<GradebookAssignment> getAssignmentById(Long id) {
        return gradebookAssignmentRepository.findById(id);
    }

    public Long countAssignmentsByGradbookAndExternalId(String gradebookId, String externalId) {
        return gradebookAssignmentRepository.countByGradebookIdAndExternalId(gradebookId, externalId);
    }

    public Long countAssignmentsByNameAndGradebookUid(String name, String gradebookUid) {
        return gradebookAssignmentRepository.countByNameAndGradebookIdAndRemoved(name, gradebookUid, false);
    }

    public Long countDuplicateAssignments(GradebookAssignment assignment) {

        return gradebookAssignmentRepository.countByNameAndGradebookAndNotIdAndRemoved(
            assignment.getName(), assignment.getGradebook(), assignment.getId(), false);
    }

    public void deleteAssignment(GradebookAssignment assignment) {
        gradebookAssignmentRepository.delete(assignment);
    }

    public Optional<GradebookAssignment> getExternalAssignment(String gradebookId, String externalId) {

        return gradebookAssignmentRepository.findByGradebookIdAndExternalId(gradebookId, externalId);
    }

    public GradebookAssignment saveGradebookAssignment(GradebookAssignment assignment) {
        return gradebookAssignmentRepository.save(assignment);
    }

    public Optional<Comment> getInternalComment(String studentUid, String gradebookId, Long gradableId) {
        return commentRepository.findByStudentIdAndGradableObject_GradebookIdAndGradableObjectIdAndGradableObject_Removed(
            studentUid, gradebookId, gradableId, false);
    }

    public void deleteInternalComment(String studentUid, String gradebookId, Long assignmentId) {

        getInternalComment(studentUid, gradebookId, assignmentId).ifPresent(commentRepository::delete);
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

    public List<Category> getCategoriesForGradebook(String gradebookId) {
        return categoryRepository.findByGradebook_IdAndRemoved(gradebookId, false);
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

    public List<Permission> getPermissionsForGradebookAndUser(String gradebookId, String userId) {
        return permissionRepository.findByGradebookIdAndUserId(gradebookId, userId);
    }

    public List<Permission> getPermissionsForGradebook(Long gradebookId) {
        return permissionRepository.findByGradebookId(gradebookId);
    }

    public List<Permission> getPermissionsForGradebookAndUserAndCategories(String gradebookId, String userId, List<Long> categoryIds) {
        return permissionRepository.findByGradebookIdAndUserIdAndCategoryIdIn(gradebookId, userId, categoryIds);
    }

    public List<Permission> getUncategorisedPermissionsForGradebookAndUserAndFunctions(String gradebookId, String userId, List<String> functions) {
        return permissionRepository.findByGradebookIdAndUserIdAndCategoryIdIsNullAndFunctionNameIn(gradebookId, userId, functions);
    }

    public List<Permission> getUngroupedPermissionsForGradebookAndUserAndFunctions(String gradebookId, String userId, List<String> functions) {
        return permissionRepository.findByGradebookIdAndUserIdAndGroupIdIsNullAndFunctionNameIn(gradebookId, userId, functions);
    }

    public List<Permission> getUngroupedPermissionsForGradebookAndUserAndCategories(String gradebookId, String userId, List<Long> categoryIds) {
        return permissionRepository.findByGradebookIdAndUserIdAndGroupIdIsNullAndCategoryIdIn(gradebookId, userId, categoryIds);
    }

    public List<Permission> getPermissionsForGradebookAndCategories(Long gradebookId, List<Long> categoryIds) {
        return permissionRepository.findByGradebookIdAndCategoryIdIn(gradebookId, categoryIds);
    }

    public List<Permission> getPermissionsForGradebookAnyGroupAnyCategory(String gradebookId, String userId) {
        return permissionRepository.findByGradebookIdAndUserIdAndCategoryIdIsNullAndGroupIdIsNull(gradebookId, userId);
    }

    public List<Permission> getUncategorisedPermissionsForGradebookAndGroups(String gradebookId, String userId, List<String> groupIds) {
        return permissionRepository.findByGradebookIdAndUserIdAndCategoryIdIsNullAndGroupIdIn(gradebookId, userId, groupIds);
    }

    public List<Permission> getPermissionsForGradebookAndGroups(String gradebookId, String userId, List<String> groupIds) {
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

    public boolean hasCourseGradeRecordEntries(String gradebookId, Set<String> studentIds) {
        return courseGradeRecordRepository.countByGradableObject_Gradebook_IdAndEnteredGradeNotNullAndStudentIdIn(gradebookId, studentIds) > 0L;
    }

    public List<AssignmentGradeRecord> getAllAssignmentGradeRecordsForGradebook(String gradebookId) {

        return assignmentGradeRecordRepository
            .findByGradableObject_Gradebook_IdAndGradableObject_RemovedOrderByPointsEarned(gradebookId, false);
    }

    public List<AssignmentGradeRecord> getAllAssignmentGradeRecordsForAssignment(Long assignmentId) {

        return assignmentGradeRecordRepository
            .findByGradableObject_IdAndGradableObject_RemovedOrderByPointsEarned(assignmentId, false);
    }

    public AssignmentGradeRecord getAssignmentGradeRecordForAssignmentAndStudent(Long assignmentId, String studentUid) {
        return assignmentGradeRecordRepository.findByGradableObject_IdAndStudentId(assignmentId, studentUid).orElse(null);
    }

    public List<AssignmentGradeRecord> getAssignmentGradeRecordsForGradebookAndStudents(String gradebookId, Collection<String> studentIds) {
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

    @Override
    public Optional<GradableObject> getGradableByIdAndGradebook(String gradebookId, Long gradableObjectId) {
        return gradebookAssignmentRepository.findByGradableIdAndGradebookIdAndRemoved(gradableObjectId, gradebookId, false);
    }
}
