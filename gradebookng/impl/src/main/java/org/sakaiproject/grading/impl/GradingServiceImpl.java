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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import org.hibernate.StaleObjectStateException;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.AssignmentHasIllegalPointsException;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.CategoryScoreData;
import org.sakaiproject.grading.api.CommentDefinition;
import org.sakaiproject.grading.api.ConflictingAssignmentNameException;
import org.sakaiproject.grading.api.ConflictingCategoryNameException;
import org.sakaiproject.grading.api.ConflictingExternalIdException;
import org.sakaiproject.grading.api.CourseGradeTransferBean;
import org.sakaiproject.grading.api.ExternalAssignmentProvider;
import org.sakaiproject.grading.api.ExternalAssignmentProviderCompat;
import org.sakaiproject.grading.api.GradeDefinition;
import org.sakaiproject.grading.api.GradeMappingDefinition;
import org.sakaiproject.grading.api.GradebookHelper;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.grading.api.GradingAuthz;
import org.sakaiproject.grading.api.GradingCategoryType;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.GradingPermissionService;
import org.sakaiproject.grading.api.GradingPersistenceManager;
import org.sakaiproject.grading.api.GradingScaleDefinition;
import org.sakaiproject.grading.api.GradingSecurityException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.GradingEventStatus;
import org.sakaiproject.grading.api.InvalidCategoryException;
import org.sakaiproject.grading.api.InvalidGradeException;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.grading.api.StaleObjectModificationException;
import org.sakaiproject.grading.api.UnmappableCourseGradeOverrideException;
import org.sakaiproject.grading.api.model.AbstractGradeRecord;
import org.sakaiproject.grading.api.model.AssignmentGradeRecord;
import org.sakaiproject.grading.api.model.Category;
import org.sakaiproject.grading.api.model.Comment;
import org.sakaiproject.grading.api.model.CourseGrade;
import org.sakaiproject.grading.api.model.CourseGradeRecord;
import org.sakaiproject.grading.api.model.GradableObject;
import org.sakaiproject.grading.api.model.GradeMapping;
import org.sakaiproject.grading.api.model.GradePointsMapping;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.grading.api.model.GradebookProperty;
import org.sakaiproject.grading.api.model.GradingEvent;
import org.sakaiproject.grading.api.model.GradingScale;
import org.sakaiproject.grading.api.model.LetterGradeMapping;
import org.sakaiproject.grading.api.model.LetterGradePercentMapping;
import org.sakaiproject.grading.api.model.LetterGradePlusMinusMapping;
import org.sakaiproject.grading.api.model.PassNotPassMapping;
import org.sakaiproject.hibernate.HibernateCriterionUtils;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.grading.api.GradingAuthz;
import org.sakaiproject.util.ResourceLoader;

import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * A Hibernate implementation of GradingService.
 */
@Slf4j
@Getter
@Setter
public class GradingServiceImpl implements GradingService {

    public static final String UID_OF_DEFAULT_GRADING_SCALE_PROPERTY = "uidOfDefaultGradingScale";

    public static final String PROP_COURSE_POINTS_DISPLAYED = "gradebook.coursepoints.displayed";
    public static final String PROP_COURSE_GRADE_DISPLAYED = "gradebook.coursegrade.displayed";
    public static final String PROP_ASSIGNMENTS_DISPLAYED = "gradebook.assignments.displayed";
    public static final String PROP_ASSIGNMENT_STATS_DISPLAYED = "gradebook.stats.assignments.displayed";
    public static final String PROP_COURSE_GRADE_STATS_DISPLAYED = "gradebook.stats.coursegrade.displayed";

    @Autowired private EventTrackingService eventTrackingService;
    @Autowired private GradingAuthz gradingAuthz;
    @Autowired private GradingPermissionService gradingPermissionService;
    @Autowired private GradingPersistenceManager gradingPersistenceManager;
    @Autowired private ResourceLoader resourceLoader;
    @Autowired private SiteService siteService;
    @Autowired private SectionAwareness sectionAwareness;
    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private ToolManager toolManager;

    // Local cache of static-between-deployment properties.
    private Map<String, String> propertiesMap = new HashMap<>();

    @Override
    public boolean isAssignmentDefined(String gradebookUid, String assignmentName) {

        if (!isUserAbleToViewAssignments(gradebookUid)) {
            log.warn("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to check for assignment {}", getUserUid(), gradebookUid,
                    assignmentName);
            throw new GradingSecurityException();
        }
        return getAssignmentWithoutStats(gradebookUid, assignmentName) != null;
    }

    private boolean isUserAbleToViewAssignments(String gradebookUid) {

        return (gradingAuthz.isUserAbleToEditAssessments(gradebookUid) || gradingAuthz.isUserAbleToGrade(gradebookUid));
    }

    @Override
    public boolean isUserAbleToGradeItemForStudent(String gradebookUid, Long itemId, String studentUid) {

        return gradingAuthz.isUserAbleToGradeItemForStudent(gradebookUid, itemId, studentUid);
    }

    @Override
    public boolean isUserAbleToViewItemForStudent(String gradebookUid, Long itemId, String studentUid) {

        return gradingAuthz.isUserAbleToViewItemForStudent(gradebookUid, itemId, studentUid);
    }

    @Override
    public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, Long itemId, String studentUid) {

        return gradingAuthz.getGradeViewFunctionForUserForStudentForItem(gradebookUid, itemId, studentUid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> getAssignments(String gradebookUid) {

        return getAssignments(gradebookUid, SortType.SORT_BY_NONE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> getAssignments(String gradebookUid, SortType sortBy) {

        if (!isUserAbleToViewAssignments(gradebookUid)) {
            log.warn("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to get assignments list", getUserUid(), gradebookUid);
            throw new GradingSecurityException();
        }

        Gradebook gradebook = getGradebook(gradebookUid);
        // Determine whether this gradebook uses Categories Only or Weighted Categories by checking category type.
        // We will avoid adding any legacy category information on the individual gb items if the instructor is no
        // longer using categories in the gradebook.
        final boolean gbUsesCategories = gradebook.getCategoryType() != GradingCategoryType.NO_CATEGORY;

        List<GradebookAssignment> internalAssignments = getAssignments(gradebook.getId());

        return sortAssignments(internalAssignments, sortBy, true)
            .stream().map(ga -> getAssignmentDefinition(ga, gbUsesCategories)).collect(Collectors.toList());
    }

    @Override
    public Assignment getAssignment(String gradebookUid, Long assignmentId) throws AssessmentNotFoundException {

        if (assignmentId == null || gradebookUid == null) {
            throw new IllegalArgumentException("null parameter passed to getAssignment. Values are assignmentId:"
                    + assignmentId + " gradebookUid:" + gradebookUid);
        }
        if (!isUserAbleToViewAssignments(gradebookUid) && !currentUserHasViewOwnGradesPerm(gradebookUid)) {
            log.warn("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to get assignment with id {}", getUserUid(), gradebookUid,
                    assignmentId);
            throw new GradingSecurityException();
        }

        GradebookAssignment assignment = getAssignmentWithoutStatsByID(gradebookUid, assignmentId);

        if (assignment == null) {
            throw new AssessmentNotFoundException("No gradebook item exists with gradable object id = " + assignmentId);
        }

        return getAssignmentDefinition(assignment);
    }

    @Override
    @Deprecated
    public Assignment getAssignment(String gradebookUid, String assignmentName) throws AssessmentNotFoundException {

        if (assignmentName == null || gradebookUid == null) {
            throw new IllegalArgumentException("null parameter passed to getAssignment. Values are assignmentName:"
                    + assignmentName + " gradebookUid:" + gradebookUid);
        }
        if (!isUserAbleToViewAssignments(gradebookUid) && !currentUserHasViewOwnGradesPerm(gradebookUid)) {
            log.warn("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to get assignment {}", getUserUid(), gradebookUid,
                    assignmentName);
            throw new GradingSecurityException();
        }

        GradebookAssignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentName);

        return assignment != null ? getAssignmentDefinition(assignment) : null;
    }

    public Assignment getExternalAssignment(String gradebookUid, String externalId) {

        return getDbExternalAssignment(gradebookUid, externalId).map(this::getAssignmentDefinition)
            .orElseThrow(() -> new IllegalArgumentException("Invalid gradebookUid or externalId"));
        //return getAssignmentDefinition(getDbExternalAssignment(gradebookUid, externalId).get());
    }

    @Override
    public Assignment getAssignmentByNameOrId(String gradebookUid, String assignmentName) throws AssessmentNotFoundException {

        Assignment assignment = null;
        try {
            assignment = getAssignment(gradebookUid, assignmentName);
        } catch (AssessmentNotFoundException e) {
            // Don't fail on this exception
            log.debug("Assessment not found by name", e);
        }

        if (assignment == null) {
            // Try to get the assignment by id
            if (NumberUtils.isCreatable(assignmentName)) {
                final Long assignmentId = NumberUtils.toLong(assignmentName, -1L);
                return getAssignment(gradebookUid, assignmentId);
            }
        }
        return assignment;
    }

    private Assignment getAssignmentDefinition(GradebookAssignment internalAssignment) {

        return getAssignmentDefinition(internalAssignment, true);
    }

    private Assignment getAssignmentDefinition(GradebookAssignment internalAssignment, boolean gbUsesCategories) {

        Assignment assignmentDefinition = new Assignment();
        assignmentDefinition.setName(internalAssignment.getName());
        assignmentDefinition.setPoints(internalAssignment.getPointsPossible());
        assignmentDefinition.setDueDate(internalAssignment.getDueDate());
        assignmentDefinition.setCounted(internalAssignment.getCounted());
        assignmentDefinition.setExternallyMaintained(internalAssignment.getExternallyMaintained());
        assignmentDefinition.setExternalAppName(internalAssignment.getExternalAppName());
        assignmentDefinition.setExternalId(internalAssignment.getExternalId());
        assignmentDefinition.setExternalData(internalAssignment.getExternalData());
        assignmentDefinition.setReleased(internalAssignment.getReleased());
        assignmentDefinition.setId(internalAssignment.getId());
        assignmentDefinition.setExtraCredit(internalAssignment.getExtraCredit());
        if (gbUsesCategories && internalAssignment.getCategory() != null) {
            assignmentDefinition.setCategoryName(internalAssignment.getCategory().getName());
            assignmentDefinition.setWeight(internalAssignment.getCategory().getWeight());
            assignmentDefinition.setCategoryExtraCredit(internalAssignment.getCategory().getExtraCredit());
            assignmentDefinition.setCategoryEqualWeight(internalAssignment.getCategory().getEqualWeightAssignments());
            assignmentDefinition.setCategoryId(internalAssignment.getCategory().getId());
            assignmentDefinition.setCategoryOrder(internalAssignment.getCategory().getCategoryOrder());
        }
        assignmentDefinition.setUngraded(internalAssignment.getUngraded());
        assignmentDefinition.setSortOrder(internalAssignment.getSortOrder());
        assignmentDefinition.setCategorizedSortOrder(internalAssignment.getCategorizedSortOrder());

        return assignmentDefinition;
    }

    public Long createAssignment(Long gradebookId, String name, Double points, Date dueDate, Boolean isNotCounted,
        Boolean isReleased, Boolean isExtraCredit, Integer sortOrder)
            throws ConflictingAssignmentNameException, StaleObjectModificationException {

        return createNewAssignment(gradebookId, null, name, points, dueDate, isNotCounted, isReleased, isExtraCredit, sortOrder, null);
    }

    public Long createAssignmentForCategory(Long gradebookId, Long categoryId, String name, Double points, Date dueDate, Boolean isNotCounted,
        Boolean isReleased, Boolean isExtraCredit, Integer categorizedSortOrder)
            throws ConflictingAssignmentNameException, StaleObjectModificationException, IllegalArgumentException {

        if (gradebookId == null || categoryId == null) {
            throw new IllegalArgumentException("gradebookId or categoryId is null in BaseHibernateManager.createAssignmentForCategory");
        }

        return createNewAssignment(gradebookId, categoryId, name, points, dueDate, isNotCounted, isReleased, isExtraCredit, null, categorizedSortOrder);
    }

    private Long createNewAssignment(final Long gradebookId, final Long categoryId, final String name, final Double points, final Date dueDate, final Boolean isNotCounted,
            final Boolean isReleased, final Boolean isExtraCredit, final Integer sortOrder, final Integer categorizedSortOrder)
                    throws ConflictingAssignmentNameException, StaleObjectModificationException {

        GradebookAssignment asn = prepareNewAssignment(name, points, dueDate, isNotCounted, isReleased, isExtraCredit, sortOrder, categorizedSortOrder);
        return saveNewAssignment(gradebookId, categoryId, asn);
    }

    private GradebookAssignment prepareNewAssignment(final String name, final Double points, final Date dueDate, final Boolean isNotCounted, final Boolean isReleased,
            final Boolean isExtraCredit, final Integer sortOrder, final Integer categorizedSortOrder) {

        // name cannot contain these special chars as they are reserved for special columns in import/export
        String validatedName = GradebookHelper.validateGradeItemName(name);

        GradebookAssignment asn = new GradebookAssignment();
        asn.setName(validatedName);
        asn.setPointsPossible(points);
        asn.setDueDate(dueDate);
        asn.setUngraded(false);
        if (isNotCounted != null) {
            asn.setNotCounted(isNotCounted.booleanValue());
        }
        if (isExtraCredit != null) {
            asn.setExtraCredit(isExtraCredit.booleanValue());
        }
        if (isReleased != null) {
            asn.setReleased(isReleased.booleanValue());
        }
        if (sortOrder != null) {
            asn.setSortOrder(sortOrder);
        }
        if (categorizedSortOrder != null) {
            asn.setCategorizedSortOrder(categorizedSortOrder);
        }

        return asn;
    }

    private Long saveNewAssignment(Long gradebookId, Long categoryId, GradebookAssignment asn) throws ConflictingAssignmentNameException {

        loadAssignmentGradebookAndCategory(asn, gradebookId, categoryId);

        if (assignmentNameExists(asn.getName(), asn.getGradebook())) {
            throw new ConflictingAssignmentNameException("You cannot save multiple assignments in a gradebook with the same name");
        }

        return gradingPersistenceManager.saveAssignment(asn).getId();
    }



    public void updateGradebook(final Gradebook gradebook) throws StaleObjectModificationException {

        // Get the gradebook and selected mapping from persistence
        //final Gradebook gradebookFromPersistence = (Gradebook)session.load(gradebook.getClass(), gradebook.getId());
        final Gradebook gradebookFromPersistence = gradingPersistenceManager.getGradebook(gradebook.getId()).orElse(null);
        final GradeMapping mappingFromPersistence = gradebookFromPersistence.getSelectedGradeMapping();

        // If the mapping has changed, and there are explicitly entered
        // course grade records, disallow this update.
        if (!mappingFromPersistence.getId().equals(gradebook.getSelectedGradeMapping().getId())) {
            if (hasExplicitlyEnteredCourseGradeRecords(gradebook.getId())) {
                throw new IllegalStateException("Selected grade mapping can not be changed, since explicit course grades exist.");
            }
        }

        // Evict the persisted objects from the session and update the gradebook
        // so the new grade mapping is used in the sort column update
        //session.evict(mappingFromPersistence);
        //for (final Object element : gradebookFromPersistence.getGradeMappings()) {
        //    session.evict(element);
        //}
        //session.evict(gradebookFromPersistence);
        // TODO Adrian - 'this is a bit janky. I don't like this at all.
        try {
            gradingPersistenceManager.saveGradebook(gradebook);
        } catch (final StaleObjectStateException e) {
            throw new StaleObjectModificationException(e);
        }
    }

    public boolean hasExplicitlyEnteredCourseGradeRecords(final Long gradebookId) {

        final Set<String> studentIds = getAllStudentUids(getGradebookUid(gradebookId));

        if (studentIds.isEmpty()) {
            return false;
        }

        return gradingPersistenceManager.hasCourseGradeRecordEntries(gradebookId, studentIds);
    }


    @Override
    public GradeDefinition getGradeDefinitionForStudentForItem(final String gradebookUid, final Long assignmentId, final String studentUid) {

        if (gradebookUid == null || assignmentId == null || studentUid == null) {
            throw new IllegalArgumentException("Null paraemter passed to getGradeDefinitionForStudentForItem");
        }

        // studentId can be a groupId (from Assignments)
        final boolean studentRequestingOwnScore = sessionManager.getCurrentSessionUserId().equals(studentUid)
                || isCurrentUserFromGroup(gradebookUid, studentUid);

        final GradebookAssignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentId);

        if (assignment == null) {
            throw new AssessmentNotFoundException(
                    "There is no assignment with the assignmentId " + assignmentId + " in gradebook " + gradebookUid);
        }

        if (!studentRequestingOwnScore && !isUserAbleToViewItemForStudent(gradebookUid, assignment.getId(), studentUid)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to retrieve grade for student {} for assignment {}",
                    getUserUid(), gradebookUid, studentUid, assignmentId);
            throw new GradingSecurityException();
        }

        final Gradebook gradebook = assignment.getGradebook();

        final GradeDefinition gradeDef = new GradeDefinition();
        gradeDef.setStudentUid(studentUid);
        gradeDef.setGradeEntryType(gradebook.getGradeType());
        gradeDef.setGradeReleased(assignment.getReleased());

        // If this is the student, then the global setting needs to be enabled and the assignment needs to have
        // been released. Return null score information if not released
        if (studentRequestingOwnScore && (!gradebook.getAssignmentsDisplayed() || !assignment.getReleased())) {
            gradeDef.setDateRecorded(null);
            gradeDef.setGrade(null);
            gradeDef.setGraderUid(null);
            gradeDef.setGradeComment(null);
            log.debug("Student {} in gradebook {} retrieving score for unreleased assignment {}", getUserUid(), gradebookUid,
                    assignment.getName());
        } else {

            final AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid);
            final CommentDefinition gradeComment = getAssignmentScoreComment(gradebookUid, assignmentId, studentUid);
            final String commentText = gradeComment != null ? gradeComment.getCommentText() : null;
            log.debug("gradeRecord={}", gradeRecord);

            if (gradeRecord == null) {
                gradeDef.setDateRecorded(null);
                gradeDef.setGrade(null);
                gradeDef.setGraderUid(null);
                gradeDef.setGradeComment(commentText);
                gradeDef.setExcused(false);
            } else {
                gradeDef.setDateRecorded(gradeRecord.getDateRecorded());
                gradeDef.setGraderUid(gradeRecord.getGraderId());
                gradeDef.setGradeComment(commentText);

                gradeDef.setExcused(gradeRecord.getExcludedFromGrade());

                if (gradebook.getGradeType() == GradeType.LETTER) {
                    final List<AssignmentGradeRecord> gradeList = new ArrayList<>();
                    gradeList.add(gradeRecord);
                    convertPointsToLetterGrade(gradebook, gradeList);
                    final AssignmentGradeRecord gradeRec = gradeList.get(0);
                    if (gradeRec != null) {
                        gradeDef.setGrade(gradeRec.getLetterEarned());
                    }
                } else if (gradebook.getGradeType() == GradeType.PERCENTAGE) {
                    final Double percent = calculateEquivalentPercent(assignment.getPointsPossible(),
                            gradeRecord.getPointsEarned());
                    if (percent != null) {
                        gradeDef.setGrade(percent.toString());
                    }
                } else {
                    if (gradeRecord.getPointsEarned() != null) {
                        gradeDef.setGrade(gradeRecord.getPointsEarned().toString());
                    }
                }
            }
        }

        log.debug("returning grade def for {}", studentUid);
        return gradeDef;
    }

    @Override
    public GradebookInformation getGradebookInformation(final String gradebookUid) {

        if (gradebookUid == null) {
            throw new IllegalArgumentException("null gradebookUid " + gradebookUid);
        }

        if (!currentUserHasEditPerm(gradebookUid) && !currentUserHasGradingPerm(gradebookUid)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to access gb information", getUserUid(), gradebookUid);
            throw new GradingSecurityException();
        }

        final Gradebook gradebook = getGradebook(gradebookUid);
        if (gradebook == null) {
            throw new IllegalArgumentException("Their is no gradbook associated with this Id: " + gradebookUid);
        }

        final GradebookInformation rval = new GradebookInformation();

        // add in all available grademappings for this gradebook
        rval.setGradeMappings(getGradebookGradeMappings(gradebook.getGradeMappings()));

        // add in details about the selected one
        final GradeMapping selectedGradeMapping = gradebook.getSelectedGradeMapping();
        if (selectedGradeMapping != null) {

            rval.setSelectedGradingScaleUid(selectedGradeMapping.getGradingScale().getUid());
            rval.setSelectedGradeMappingId(Long.toString(selectedGradeMapping.getId()));

            // note that these are not the DEFAULT bottom percents but the configured ones per gradebook
            Map<String, Double> gradeMap = selectedGradeMapping.getGradeMap();
            gradeMap = GradeMappingDefinition.sortGradeMapping(gradeMap);
            rval.setSelectedGradingScaleBottomPercents(gradeMap);
            rval.setGradeScale(selectedGradeMapping.getGradingScale().getName());
        }

        rval.setGradeType(gradebook.getGradeType());
        rval.setCategoryType(gradebook.getCategoryType());
        rval.setDisplayReleasedGradeItemsToStudents(gradebook.getAssignmentsDisplayed());

        // add in the category definitions
        rval.setCategories(getCategoryDefinitions(gradebookUid));

        // add in the course grade display settings
        rval.setCourseGradeDisplayed(gradebook.getCourseGradeDisplayed());
        rval.setCourseLetterGradeDisplayed(gradebook.getCourseLetterGradeDisplayed());
        rval.setCoursePointsDisplayed(gradebook.getCoursePointsDisplayed());
        rval.setCourseAverageDisplayed(gradebook.getCourseAverageDisplayed());

        // add in stats display settings
        rval.setAssignmentStatsDisplayed(gradebook.getAssignmentStatsDisplayed());
        rval.setCourseGradeStatsDisplayed(gradebook.getCourseGradeStatsDisplayed());

        // add in compare grades with classmates settings
        rval.setAllowStudentsToCompareGrades(gradebook.getAllowStudentsToCompareGrades());
        rval.setComparingDisplayStudentNames(gradebook.getComparingDisplayStudentNames());
        rval.setComparingDisplayStudentSurnames(gradebook.getComparingDisplayStudentSurnames());
        rval.setComparingDisplayTeacherComments(gradebook.getComparingDisplayTeacherComments());
        rval.setComparingIncludeAllGrades(gradebook.getComparingIncludeAllGrades());
        rval.setComparingRandomizeDisplayedData(gradebook.getComparingRandomizeDisplayedData());

        return rval;
    }

    @Override
    public Map<String,String> transferGradebook(final GradebookInformation gradebookInformation,
            final List<org.sakaiproject.grading.api.Assignment> assignments, final String toGradebookUid, final String fromContext) {

        final Map<String, String> transversalMap = new HashMap<>();

        final Gradebook gradebook = getGradebook(toGradebookUid);

        gradebook.setCategoryType(gradebookInformation.getCategoryType());
        gradebook.setGradeType(gradebookInformation.getGradeType());
        gradebook.setAssignmentStatsDisplayed(gradebookInformation.getAssignmentStatsDisplayed());
        gradebook.setCourseGradeStatsDisplayed(gradebookInformation.getCourseGradeStatsDisplayed());
        gradebook.setAssignmentsDisplayed(gradebookInformation.getDisplayReleasedGradeItemsToStudents());
        gradebook.setCourseGradeDisplayed(gradebookInformation.getCourseGradeDisplayed());
        gradebook.setAllowStudentsToCompareGrades(gradebookInformation.getAllowStudentsToCompareGrades());
        gradebook.setComparingDisplayStudentNames(gradebookInformation.getComparingDisplayStudentNames());
        gradebook.setComparingDisplayStudentSurnames(gradebookInformation.getComparingDisplayStudentSurnames());
        gradebook.setComparingDisplayTeacherComments(gradebookInformation.getComparingDisplayTeacherComments());
        gradebook.setComparingIncludeAllGrades(gradebookInformation.getComparingIncludeAllGrades());
        gradebook.setComparingRandomizeDisplayedData(gradebookInformation.getComparingRandomizeDisplayedData());
        gradebook.setCourseLetterGradeDisplayed(gradebookInformation.getCourseLetterGradeDisplayed());
        gradebook.setCoursePointsDisplayed(gradebookInformation.getCoursePointsDisplayed());
        gradebook.setCourseAverageDisplayed(gradebookInformation.getCourseAverageDisplayed());

        updateGradebook(gradebook);

        // all categories that we need to end up with
        final List<CategoryDefinition> categories = gradebookInformation.getCategories();

        // filter out externally managed assignments. These are never imported.
        assignments.removeIf(a -> a.getExternallyMaintained());

        // this map holds the names of categories that have been created in the site to the category ids
        // and is updated as we go along
        // likewise for list of assignments
        final Map<String, Long> categoriesCreated = new HashMap<>();
        final List<String> assignmentsCreated = new ArrayList<>();

        if (!categories.isEmpty() && gradebookInformation.getCategoryType() != GradingCategoryType.NO_CATEGORY) {

            // migrate the categories with assignments
            categories.forEach(c -> {

                assignments.forEach(a -> {

                    if (StringUtils.equals(c.getName(), a.getCategoryName())) {

                        if (!categoriesCreated.containsKey(c.getName())) {

                            // create category
                            Long categoryId = null;
                            try {
                                categoryId = createCategory(gradebook.getId(), c.getName(), c.getWeight(), c.getDropLowest(),
                                        c.getDropHighest(), c.getKeepHighest(), c.getExtraCredit(), c.getEqualWeight(), c.getCategoryOrder());
                            } catch (final ConflictingCategoryNameException e) {
                                // category already exists. Could be from a merge.
                                log.info("Category: {} already exists in target site. Skipping creation.", c.getName());
                            }

                            if (categoryId == null) {
                                // couldn't create so look up the id in the target site
                                final List<CategoryDefinition> existingCategories = getCategoryDefinitions(gradebook.getUid());
                                categoryId = existingCategories.stream().filter(e -> StringUtils.equals(e.getName(), c.getName()))
                                        .findFirst().get().getId();
                            }
                            // record that we have created this category
                            categoriesCreated.put(c.getName(), categoryId);

                        }

                        // create the assignment for the current category
                        try {
                            Long newId = createAssignmentForCategory(gradebook.getId(), categoriesCreated.get(c.getName()), a.getName(), a.getPoints(),
                                    a.getDueDate(), !a.getCounted(), a.getReleased(), a.getExtraCredit(), a.getCategorizedSortOrder());
                            transversalMap.put("gb/"+a.getId(),"gb/"+newId);
                        } catch (final ConflictingAssignmentNameException e) {
                            // assignment already exists. Could be from a merge.
                            log.info("GradebookAssignment: {} already exists in target site. Skipping creation.", a.getName());
                        } catch (final Exception ex) {
                            log.warn("GradebookAssignment: exception {} trying to create {} in target site. Skipping creation.", ex.getMessage(), a.getName());
                        }

                        // record that we have created this assignment
                        assignmentsCreated.add(a.getName());
                    }
                });
            });

            // create any remaining categories that have no assignments
            categories.removeIf(c -> categoriesCreated.containsKey(c.getName()));
            categories.forEach(c -> {
                try {
                    createCategory(gradebook.getId(), c.getName(), c.getWeight(), c.getDropLowest(), c.getDropHighest(), c.getKeepHighest(),
                            c.getExtraCredit(), c.getEqualWeight(), c.getCategoryOrder());
                } catch (final ConflictingCategoryNameException e) {
                    // category already exists. Could be from a merge.
                    log.info("Category: {} already exists in target site. Skipping creation.", c.getName());
                }
            });
        }

        // create any remaining assignments that have no categories
        assignments.removeIf(a -> assignmentsCreated.contains(a.getName()));
        assignments.forEach(a -> {

            try {
                Long newId = createAssignment(gradebook.getId(), a.getName(), a.getPoints(), a.getDueDate(), !a.getCounted(), a.getReleased(), a.getExtraCredit(), a.getSortOrder());
                transversalMap.put("gb/"+a.getId(),"gb/"+newId);
            } catch (final ConflictingAssignmentNameException e) {
                // assignment already exists. Could be from a merge.
                log.info("GradebookAssignment: {} already exists in target site. Skipping creation.", a.getName());
            } catch (final Exception ex) {
                log.warn("GradebookAssignment: exception {} trying to create {} in target site. Skipping creation.", ex.getMessage(), a.getName());
            }
        });

        // Carry over the old gradebook's selected grading scheme if possible.
        final String fromGradingScaleUid = gradebookInformation.getSelectedGradingScaleUid();

        MERGE_GRADE_MAPPING: if (!StringUtils.isEmpty(fromGradingScaleUid)) {
            for (final GradeMapping gradeMapping : gradebook.getGradeMappings()) {
                if (gradeMapping.getGradingScale().getUid().equals(fromGradingScaleUid)) {
                    // We have a match. Now make sure that the grades are as expected.
                    final Map<String, Double> inputGradePercents = gradebookInformation.getSelectedGradingScaleBottomPercents();
                    final Set<String> gradeCodes = inputGradePercents.keySet();

                    // If the grades dont map one-to-one, clear out the destination site's existing map
                    if (!gradeCodes.containsAll(gradeMapping.getGradeMap().keySet())) {
                        gradeMapping.getGradeMap().clear();
                    }

                    // Modify the existing grade-to-percentage map.
                    for (String gradeCode : gradeCodes) {
                        gradeMapping.getGradeMap().put(gradeCode, inputGradePercents.get(gradeCode));
                    }
                    gradebook.setSelectedGradeMapping(gradeMapping);
                    updateGradebook(gradebook);
                    log.info("Merge to gradebook {} updated grade mapping", toGradebookUid);

                    break MERGE_GRADE_MAPPING;
                }
            }
            // Did not find a matching grading scale.
            log.info("Merge to gradebook {} skipped grade mapping change because grading scale {} is not defined", toGradebookUid,
                    fromGradingScaleUid);
        }
        return transversalMap;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void removeAssignment(Long assignmentId) throws StaleObjectModificationException {

        Optional<GradebookAssignment> optAsn = gradingPersistenceManager.getAssignmentById(assignmentId);
        if (optAsn.isPresent()) {
            optAsn.get().setRemoved(true);
            Gradebook gradebook = optAsn.get().getGradebook();
            gradingPersistenceManager.saveAssignment(optAsn.get());
            log.info("GradebookAssignment {} has been removed from {}", optAsn.get().getName(), gradebook);
        } else {
            log.warn("No assignment for id {}", assignmentId);
        }
    }

    @Override
    public Long addAssignment(final String gradebookUid, Assignment assignmentDefinition) {

        if (!gradingAuthz.isUserAbleToEditAssessments(gradebookUid)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to add an assignment", getUserUid(), gradebookUid);
            throw new GradingSecurityException();
        }

        final String validatedName = GradebookHelper.validateAssignmentNameAndPoints(assignmentDefinition);

        final Gradebook gradebook = getGradebook(gradebookUid);

        // if attaching to category
        if (assignmentDefinition.getCategoryId() != null) {
            return createAssignmentForCategory(gradebook.getId(), assignmentDefinition.getCategoryId(), validatedName,
                    assignmentDefinition.getPoints(), assignmentDefinition.getDueDate(), !assignmentDefinition.getCounted(), assignmentDefinition.getReleased(),
                    assignmentDefinition.getExtraCredit(), assignmentDefinition.getCategorizedSortOrder());
        }

        return createAssignment(gradebook.getId(), validatedName, assignmentDefinition.getPoints(), assignmentDefinition.getDueDate(),
                !assignmentDefinition.getCounted(), assignmentDefinition.getReleased(), assignmentDefinition.getExtraCredit(), assignmentDefinition.getSortOrder());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void updateAssignment(final String gradebookUid, final Long assignmentId, final Assignment assignmentDefinition) {

        if (!gradingAuthz.isUserAbleToEditAssessments(gradebookUid)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to change the definition of assignment {}", getUserUid(),
                    gradebookUid, assignmentId);
            throw new GradingSecurityException();
        }

        final String validatedName = GradebookHelper.validateAssignmentNameAndPoints(assignmentDefinition);

        final Gradebook gradebook = this.getGradebook(gradebookUid);

        final GradebookAssignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentId);
        if (assignment == null) {
            throw new AssessmentNotFoundException(
                    "There is no assignment with id " + assignmentId + " in gradebook " + gradebookUid);
        }

        // check if we need to scale the grades
        boolean scaleGrades = false;
        final Double originalPointsPossible = assignment.getPointsPossible();
        if (gradebook.getGradeType() == GradeType.PERCENTAGE
                && !assignment.getPointsPossible().equals(assignmentDefinition.getPoints())) {
            scaleGrades = true;
        }

        if (gradebook.getGradeType() == GradeType.POINTS && assignmentDefinition.getScaleGrades()) {
            scaleGrades = true;
        }

        // external assessments are supported, but not these fields
        if (!assignmentDefinition.getExternallyMaintained()) {
            assignment.setName(validatedName);
            assignment.setPointsPossible(assignmentDefinition.getPoints());
            assignment.setDueDate(assignmentDefinition.getDueDate());
        }
        assignment.setExtraCredit(assignmentDefinition.getExtraCredit());
        assignment.setCounted(assignmentDefinition.getCounted());
        assignment.setReleased(assignmentDefinition.getReleased());

        assignment.setExternalAppName(assignmentDefinition.getExternalAppName());
        assignment.setExternallyMaintained(assignmentDefinition.getExternallyMaintained());
        assignment.setExternalId(assignmentDefinition.getExternalId());
        assignment.setExternalData(assignmentDefinition.getExternalData());

        // if we have a category, get it and set it
        // otherwise clear it fully
        if (assignmentDefinition.getCategoryId() != null) {
            final Category cat = gradingPersistenceManager.getCategory(assignmentDefinition.getCategoryId()).orElse(null);
            assignment.setCategory(cat);
        } else {
            assignment.setCategory(null);
        }

        updateAssignment(assignment);

        if (scaleGrades) {
            scaleGrades(gradebook, assignment, originalPointsPossible);
        }
    }

    private CourseGrade getCourseGrade(Long gradebookId) {
        return gradingPersistenceManager.getCourseGradesByGradebookId(gradebookId).get(0);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<CourseGradeRecord> getPointsEarnedCourseGradeRecords(final CourseGrade courseGrade, final Collection studentUids) {

        if (studentUids == null || studentUids.isEmpty()) {
            log.info("Returning no grade records for an empty collection of student UIDs");
            return Collections.<CourseGradeRecord>emptyList();
        }

        List<CourseGradeRecord> unfilteredRecords = gradingPersistenceManager.getCourseGradeRecordsForCourseGrade(courseGrade.getId());
        final List<CourseGradeRecord> records = filterAndPopulateCourseGradeRecordsByStudents(courseGrade, unfilteredRecords, studentUids);

        final Long gradebookId = courseGrade.getGradebook().getId();
        final Gradebook gradebook = getGradebook(gradebookId);
        final List<Category> cates = getCategories(gradebookId);

        // get all of the AssignmentGradeRecords here to avoid repeated db calls
        final Map<String, List<AssignmentGradeRecord>> gradeRecMap = getGradeRecordMapForStudents(gradebookId, studentUids);

        // get all of the counted assignments
        final List<GradebookAssignment> countedAssigns = getCountedAssignments(gradebookId)
            .stream().filter(a -> a.isIncludedInCalculations()).collect(Collectors.toList());
        // double totalPointsPossible = getTotalPointsInternal(gradebookId, session);
        // if (log.isDebugEnabled()) log.debug("Total points = " + totalPointsPossible);

        for (CourseGradeRecord cgr : records) {
            // double totalPointsEarned = getTotalPointsEarnedInternal(gradebookId, cgr.getStudentId(), session);
            final List<AssignmentGradeRecord> studentGradeRecs = gradeRecMap.get(cgr.getStudentId());

            applyDropScores(studentGradeRecs, gradebook.getCategoryType());
            final List<Double> totalEarned = getTotalPointsEarnedInternal(cgr.getStudentId(), gradebook, cates, studentGradeRecs,
                    countedAssigns);
            final double totalPointsEarned = totalEarned.get(0);
            final double literalTotalPointsEarned = totalEarned.get(1);
            final double extraPointsEarned = totalEarned.get(2);
            final double totalPointsPossible = getTotalPointsInternal(gradebook, cates, cgr.getStudentId(), studentGradeRecs,
                    countedAssigns, false);
            cgr.initNonpersistentFields(totalPointsPossible, totalPointsEarned, literalTotalPointsEarned, extraPointsEarned);
            log.debug("Points earned = {}", cgr.getPointsEarned());
            log.debug("Points possible = {}", cgr.getTotalPointsPossible());
        }

        return records;
    }

    private List<CourseGradeRecord> filterAndPopulateCourseGradeRecordsByStudents(CourseGrade courseGrade, Collection<CourseGradeRecord> gradeRecords, Collection studentUids) {

        final List<CourseGradeRecord> filteredRecords = new ArrayList<>();
        final Set<String> missingStudents = new HashSet<>(studentUids);
        for (CourseGradeRecord cgr : gradeRecords) {
            if (studentUids.contains(cgr.getStudentId())) {
                filteredRecords.add(cgr);
                missingStudents.remove(cgr.getStudentId());
            }
        }
        missingStudents.forEach(id -> filteredRecords.add(new CourseGradeRecord(courseGrade, id)));
        return filteredRecords;
    }

    private double getTotalPointsInternal(final Gradebook gradebook, final List<Category> categories, final String studentId,
            final List<AssignmentGradeRecord> studentGradeRecs, final List<GradebookAssignment> countedAssigns,
            final boolean literalTotal) {

        final GradeType gbGradeType = gradebook.getGradeType();
        if (gbGradeType != GradeType.POINTS && gbGradeType != GradeType.PERCENTAGE) {
            log.error("Wrong grade type in GradebookCalculationImpl.getTotalPointsInternal");
            return -1;
        }

        if (studentGradeRecs == null || countedAssigns == null) {
            log.debug("Returning 0 from getTotalPointsInternal since studentGradeRecs or countedAssigns was null");
            return 0;
        }

        double totalPointsPossible = 0;

        final HashSet<GradebookAssignment> countedSet = new HashSet<>(countedAssigns);

        // we need to filter this list to identify only "counted" grade recs
        final List<AssignmentGradeRecord> countedGradeRecs = new ArrayList<>();
        for (AssignmentGradeRecord gradeRec : studentGradeRecs) {
            GradebookAssignment assign = gradeRec.getAssignment();
            boolean extraCredit = assign.getExtraCredit();
            if (gradebook.getCategoryType() != GradingCategoryType.NO_CATEGORY && assign.getCategory() != null
                    && assign.getCategory().getExtraCredit()) {
                extraCredit = true;
            }

            boolean excused = BooleanUtils.toBoolean(gradeRec.getExcludedFromGrade());
            if (assign.getCounted() && !assign.getUngraded() && !assign.getRemoved() && countedSet.contains(assign) &&
                    assign.getPointsPossible() != null && assign.getPointsPossible() > 0 && !gradeRec.getDroppedFromGrade() && !extraCredit
                    && !excused) {
                countedGradeRecs.add(gradeRec);
            }
        }

        final Set<Long> assignmentsTaken = new HashSet<>();
        final Set<Long> categoryTaken = new HashSet<>();
        for (final AssignmentGradeRecord gradeRec : countedGradeRecs) {
            if (gradeRec.getPointsEarned() != null && !gradeRec.getPointsEarned().equals("")) {
                final Double pointsEarned = gradeRec.getPointsEarned();
                final GradebookAssignment go = gradeRec.getAssignment();
                if (pointsEarned != null) {
                    if (gradebook.getCategoryType() == GradingCategoryType.NO_CATEGORY) {
                        assignmentsTaken.add(go.getId());
                    } else if ((gradebook.getCategoryType() == GradingCategoryType.ONLY_CATEGORY || gradebook
                            .getCategoryType() == GradingCategoryType.WEIGHTED_CATEGORY)
                            && go != null && categories != null) {
                        // assignmentsTaken.add(go.getId());
                        // }
                        // else if (gradebook.getCategoryType() == GradingCategoryType.WEIGHTED_CATEGORY && go != null &&
                        // categories != null)
                        // {
                        for (Category cate : categories) {
                            if (cate != null && !cate.getRemoved() && go.getCategory() != null
                                    && cate.getId().equals(go.getCategory().getId())
                                    && ((cate.getExtraCredit() != null && !cate.getExtraCredit()) || cate.getExtraCredit() == null)) {
                                assignmentsTaken.add(go.getId());
                                categoryTaken.add(cate.getId());
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (!assignmentsTaken.isEmpty()) {
            if (!literalTotal && gradebook.getCategoryType() == GradingCategoryType.WEIGHTED_CATEGORY) {
                for (Category cate : categories) {
                    if (cate != null && !cate.getRemoved() && categoryTaken.contains(cate.getId())) {
                        totalPointsPossible += cate.getWeight();
                    }
                }
                return totalPointsPossible;
            }
            final Iterator assignmentIter = countedAssigns.iterator();
            while (assignmentIter.hasNext()) {
                final GradebookAssignment asn = (GradebookAssignment) assignmentIter.next();
                if (asn != null) {
                    final Double pointsPossible = asn.getPointsPossible();

                    if (gradebook.getCategoryType() == GradingCategoryType.NO_CATEGORY
                            && assignmentsTaken.contains(asn.getId())) {
                        totalPointsPossible += pointsPossible;
                    } else if (gradebook.getCategoryType() == GradingCategoryType.ONLY_CATEGORY
                            && assignmentsTaken.contains(asn.getId())) {
                        totalPointsPossible += pointsPossible;
                    } else if (literalTotal && gradebook.getCategoryType() == GradingCategoryType.WEIGHTED_CATEGORY
                            && assignmentsTaken.contains(asn.getId())) {
                        totalPointsPossible += pointsPossible;
                    }
                }
            }
        } else {
            totalPointsPossible = -1;
        }

        return totalPointsPossible;
    }

    private List<Double> getTotalPointsEarnedInternal(final String studentId, final Gradebook gradebook, final List<Category> categories,
            final List<AssignmentGradeRecord> gradeRecs, final List<GradebookAssignment> countedAssigns) {

        final GradeType gbGradeType = gradebook.getGradeType();
        if (gbGradeType != GradeType.POINTS && gbGradeType != GradeType.PERCENTAGE) {
            log.error("Wrong grade type in GradebookCalculationImpl.getTotalPointsEarnedInternal");
            return Collections.<Double>emptyList();
        }

        if (gradeRecs == null || countedAssigns == null) {
            log.debug("getTotalPointsEarnedInternal for studentId={} returning 0 because null gradeRecs or countedAssigns", studentId);
            List<Double> returnList = new ArrayList<>();
            returnList.add(new Double(0));
            returnList.add(new Double(0));
            returnList.add(new Double(0)); // 3rd one is for the pre-adjusted course grade
            return returnList;
        }

        BigDecimal totalPointsEarned = new BigDecimal(0);
        BigDecimal extraPointsEarned = new BigDecimal(0);
        BigDecimal literalTotalPointsEarned = new BigDecimal(0d);

        final Map<Long, BigDecimal> cateScoreMap = new HashMap<>();
        final Map<Long, BigDecimal> cateTotalScoreMap = new HashMap<>();
        final Set<Long> assignmentsTaken = new HashSet<>();

        for (final AssignmentGradeRecord gradeRec : gradeRecs) {
            final boolean excused = BooleanUtils.toBoolean(gradeRec.getExcludedFromGrade());

            if (gradeRec.getPointsEarned() != null && !gradeRec.getPointsEarned().equals("") && !gradeRec.getDroppedFromGrade()) {
                final GradebookAssignment go = gradeRec.getAssignment();
                if (go.isIncludedInCalculations() && countedAssigns.contains(go)) {
                    BigDecimal pointsEarned = BigDecimal.valueOf(gradeRec.getPointsEarned());
                    final BigDecimal pointsPossible = BigDecimal.valueOf(go.getPointsPossible());

                    // if (gbGradeType == GradeType.POINTS)
                    // {
                    if (gradebook.getCategoryType() == GradingCategoryType.NO_CATEGORY) {
                        if (!excused) {
                            totalPointsEarned = totalPointsEarned.add(pointsEarned, GradingService.MATH_CONTEXT);
                            literalTotalPointsEarned = pointsEarned.add(literalTotalPointsEarned, GradingService.MATH_CONTEXT);
                            assignmentsTaken.add(go.getId());
                        }
                    } else if (gradebook.getCategoryType() == GradingCategoryType.ONLY_CATEGORY && go != null) {
                        if (!excused) {
                            totalPointsEarned = totalPointsEarned.add(pointsEarned, GradingService.MATH_CONTEXT);
                            literalTotalPointsEarned = pointsEarned.add(literalTotalPointsEarned, GradingService.MATH_CONTEXT);
                            assignmentsTaken.add(go.getId());
                        }
                    } else if (gradebook.getCategoryType() == GradingCategoryType.WEIGHTED_CATEGORY && go != null
                            && categories != null) {
                        for (Category cate : categories) {
                            if (cate != null && !cate.getRemoved() && go.getCategory() != null
                                    && cate.getId().equals(go.getCategory().getId())) {
                                if (!excused) {
                                    assignmentsTaken.add(go.getId());
                                    literalTotalPointsEarned = pointsEarned.add(literalTotalPointsEarned, GradingService.MATH_CONTEXT);

                                    // If category is equal weight, manipulate points to be the average
                                    if (cate.getEqualWeightAssignments()) {
                                        pointsEarned = pointsEarned.divide(pointsPossible, GradingService.MATH_CONTEXT);
                                    }

                                    if (cateScoreMap.get(cate.getId()) != null) {
                                        cateScoreMap.put(cate.getId(), ((BigDecimal)cateScoreMap.get(cate.getId())).add(pointsEarned, GradingService.MATH_CONTEXT));
                                    } else {
                                        cateScoreMap.put(cate.getId(), pointsEarned);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (categories.size() > 0 && gradebook.getCategoryType() == GradingCategoryType.WEIGHTED_CATEGORY) {
            for (GradebookAssignment asgn : countedAssigns) {
                BigDecimal pointsPossible = new BigDecimal(asgn.getPointsPossible());

                if (assignmentsTaken.contains(asgn.getId())) {
                    for (Category cate : categories) {
                        if (cate != null && !cate.getRemoved() && asgn.getCategory() != null
                                && cate.getId().equals(asgn.getCategory().getId()) && !asgn.getExtraCredit()) {

                            // If it's equal-weight category, just want to divide averages by number of items
                            if (cate.getEqualWeightAssignments()) {
                                pointsPossible = new BigDecimal("1");
                            }

                            if (cateTotalScoreMap.get(cate.getId()) == null) {
                                cateTotalScoreMap.put(cate.getId(), pointsPossible);
                            } else {
                                cateTotalScoreMap.put(cate.getId(),
                                        ((BigDecimal) cateTotalScoreMap.get(cate.getId())).add(pointsPossible));
                            }
                        }
                    }
                }
            }
        }

        if (assignmentsTaken.isEmpty()) {
            totalPointsEarned = new BigDecimal(-1);
        }

        if (gradebook.getCategoryType() == GradingCategoryType.WEIGHTED_CATEGORY) {
            for (Category cate : categories) {
                if (cate != null && !cate.getRemoved() && cateScoreMap.get(cate.getId()) != null
                        && cateTotalScoreMap.get(cate.getId()) != null) {
                    if (cate.getExtraCredit()) {
                        extraPointsEarned = extraPointsEarned.add(((BigDecimal) cateScoreMap.get(cate.getId())).multiply(new BigDecimal(cate.getWeight()), GradingService.MATH_CONTEXT)
                                .divide((BigDecimal) cateTotalScoreMap.get(cate.getId()), GradingService.MATH_CONTEXT));
                    }
                    else {
                        totalPointsEarned = totalPointsEarned.add(((BigDecimal) cateScoreMap.get(cate.getId())).multiply(new BigDecimal(cate.getWeight()), GradingService.MATH_CONTEXT)
                                .divide((BigDecimal) cateTotalScoreMap.get(cate.getId()), GradingService.MATH_CONTEXT));
                    }
                }
            }
        }

        log.debug("getTotalPointsEarnedInternal for studentId={} returning {}", studentId, totalPointsEarned);
        List<Double> returnList = new ArrayList<>();
        returnList.add(totalPointsEarned.doubleValue());
        returnList.add(literalTotalPointsEarned.doubleValue());
        returnList.add(extraPointsEarned.doubleValue());

        return returnList;
    }

    /**
     * Internal method to get a gradebook based on its id.
     *
     * @param id
     * @return
     *
     *      NOTE: When the UI changes, this is to be turned private again
     */
    public Gradebook getGradebook(Long id) {

        return gradingPersistenceManager.getGradebook(id).orElse(null);
    }

    private List<GradebookAssignment> getAssignmentsCounted(Long gradebookId) {

        return gradingPersistenceManager.getCountedAssignmentsForGradebook(gradebookId);
    }

    @Override
    public boolean checkStudentsNotSubmitted(String gradebookUid) {

        final Gradebook gradebook = getGradebook(gradebookUid);
        final Set<String> studentUids = getAllStudentUids(getGradebookUid(gradebook.getId()));
        if (gradebook.getCategoryType() == GradingCategoryType.NO_CATEGORY
                || gradebook.getCategoryType() == GradingCategoryType.ONLY_CATEGORY) {

            List<GradebookAssignment> filteredAssigns = getAssignments(gradebook.getId(), SortType.SORT_BY_SORTING, true)
                .stream().filter(a -> a.getCounted() && !a.getUngraded()).collect(Collectors.toList());

            final List<AssignmentGradeRecord> records = getAllAssignmentGradeRecords(gradebook.getId(), studentUids);
            final List<AssignmentGradeRecord> filteredRecords = new ArrayList<>();
            for (AssignmentGradeRecord agr : records) {
                if (!agr.isCourseGradeRecord() && agr.getAssignment().getCounted() && !agr.getAssignment().getUngraded()) {
                    if (agr.getPointsEarned() == null) {
                        return true;
                    }
                    filteredRecords.add(agr);
                }
            }

            return filteredRecords.size() < (filteredAssigns.size() * studentUids.size());
        } else {
            final List<GradebookAssignment> assigns = getAssignments(gradebook.getId(), SortType.SORT_BY_SORTING, true);
            final List<AssignmentGradeRecord> records = getAllAssignmentGradeRecords(gradebook.getId(), studentUids);

            final Set<Long> filteredAssigns = new HashSet<>();
            for (GradebookAssignment assign : assigns) {
                if (assign != null && assign.getCounted() && !assign.getUngraded()) {
                    if (assign.getCategory() != null && !assign.getCategory().getRemoved()) {
                        filteredAssigns.add(assign.getId());
                    }
                }
            }

            final List<AssignmentGradeRecord> filteredRecords = new ArrayList<>();
            for (AssignmentGradeRecord agr : records) {
                if (filteredAssigns.contains(agr.getAssignment().getId()) && !agr.isCourseGradeRecord()) {
                    if (agr.getPointsEarned() == null) {
                        return true;
                    }
                    filteredRecords.add(agr);
                }
            }

            return filteredRecords.size() < filteredAssigns.size() * studentUids.size();
        }
    }

    /**
     * Get all assignment grade records for the given students
     *
     * @param gradebookId
     * @param studentUids
     * @return
     *
     *      NOTE When the UI changes, this needs to be made private again
     */
    public List<AssignmentGradeRecord> getAllAssignmentGradeRecords(Long gradebookId, Collection<String> studentUids) {

        if (studentUids.isEmpty()) {
            // If there are no enrollments, no need to execute the query.
            log.info("No enrollments were specified.  Returning an empty List of grade records");
            return Collections.<AssignmentGradeRecord>emptyList();
        } else {
            List<AssignmentGradeRecord> unfilteredRecords = gradingPersistenceManager.getAllAssignmentGradeRecordsForGradebook(gradebookId);
            return filterGradeRecordsByStudents(unfilteredRecords, studentUids);
        }
    }

    private List<AssignmentGradeRecord> getAllAssignmentGradeRecordsForGbItem(Long gradableObjectId, Collection studentUids) {

        if (studentUids.isEmpty()) {
            // If there are no enrollments, no need to execute the query.
            log.info("No enrollments were specified.  Returning an empty List of grade records");
            return Collections.<AssignmentGradeRecord>emptyList();
        } else {
            List<AssignmentGradeRecord> unfilteredRecords = gradingPersistenceManager.getAllAssignmentGradeRecordsForAssignment(gradableObjectId);
            return filterGradeRecordsByStudents(unfilteredRecords, studentUids);
        }
    }

    /**
     * Gets all AssignmentGradeRecords on the gradableObjectIds limited to students specified by studentUids
     */
    private List<AssignmentGradeRecord> getAllAssignmentGradeRecordsForGbItems(final List<Long> gradableObjectIds, final List<String> studentUids) {

        final List<AssignmentGradeRecord> gradeRecords = new ArrayList<>();
        if (studentUids.isEmpty()) {
            // If there are no enrollments, no need to execute the query.
            log.debug("No enrollments were specified. Returning an empty List of grade records");
            return gradeRecords;
        }
        /*
         * Watch out for Oracle's "in" limit. Ignoring oracle, the query would be:
         * "from AssignmentGradeRecord as agr where agr.gradableObject.removed = false and agr.gradableObject.id in (:gradableObjectIds) and agr.studentId in (:studentUids)"
         * Note: the order is not important. The calling methods will iterate over all entries and add them to a map. We could have
         * made this method return a map, but we'd have to iterate over the items in order to add them to the map anyway. That would
         * be a waste of a loop that the calling method could use to perform additional tasks.
         */
        // For Oracle, iterate over gbItems 1000 at a time (sympathies to whoever needs to query grades for a thousand gbItems)
        int minGbo = 0;
        int maxGbo = Math.min(gradableObjectIds.size(), 1000);
        while (minGbo < gradableObjectIds.size()) {
            // For Oracle, iterate over students 1000 at a time
            int minStudent = 0;
            int maxStudent = Math.min(studentUids.size(), 1000);
            while (minStudent < studentUids.size()) {
                List<AssignmentGradeRecord> subRecords
                    = gradingPersistenceManager.getAssignmentGradeRecordsForAssignmentIdsAndStudentIds(
                    gradableObjectIds.subList(minGbo, maxGbo), studentUids.subList(minStudent, maxStudent));
                // Add the query results to our overall results (in case there's over a thousand things)
                gradeRecords.addAll(subRecords);
                minStudent += 1000;
                maxStudent = Math.min(studentUids.size(), minStudent + 1000);
            }
            minGbo += 1000;
            maxGbo = Math.min(gradableObjectIds.size(), minGbo + 1000);
        }
        return gradeRecords;
    }

    /**
     * Get a list of assignments, sorted
     *
     * @param gradebookId
     * @param sortBy
     * @param ascending
     * @return
     *
     *      NOTE: When the UI changes, this needs to go back to private
     */
    private List<GradebookAssignment> getAssignments(Long gradebookId, SortType sortBy, boolean ascending) {

        return sortAssignments(getAssignments(gradebookId), sortBy, ascending);
    }

    /**
     * Sort the list of (internal) assignments by the given criteria
     *
     * @param assignments
     * @param sortBy
     * @param ascending
     */
    private List<GradebookAssignment> sortAssignments(final List<GradebookAssignment> assignments, SortType sortBy, final boolean ascending) {

        // note, this is duplicated in the tool GradebookManagerHibernateImpl class
        Comparator comp;

        if (sortBy == null) {
            sortBy = SortType.SORT_BY_SORTING; // default
        }

        switch (sortBy) {

            case SORT_BY_NONE:
                return assignments; // no sorting
            case SORT_BY_NAME:
                comp = GradableObject.nameComparator;
                break;
            case SORT_BY_DATE:
                comp = GradableObject.dateComparator;
                break;
            case SORT_BY_MEAN:
                comp = GradableObject.meanComparator;
                break;
            case SORT_BY_POINTS:
                comp = GradebookAssignment.pointsComparator;
                break;
            case SORT_BY_RELEASED:
                comp = GradebookAssignment.releasedComparator;
                break;
            case SORT_BY_COUNTED:
                comp = GradebookAssignment.countedComparator;
                break;
            case SORT_BY_EDITOR:
                comp = GradebookAssignment.gradeEditorComparator;
                break;
            case SORT_BY_SORTING:
                comp = GradableObject.sortingComparator;
                break;
            case SORT_BY_CATEGORY:
                comp = GradebookAssignment.categoryComparator;
                break;
            default:
                comp = GradableObject.defaultComparator;
        }

        Collections.sort(assignments, comp);
        if (!ascending) {
            Collections.reverse(assignments);
        }
        log.debug("sortAssignments: ordering by {} ({}), ascending={}", sortBy, comp, ascending);
        return assignments;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sakaiproject.grading.api.GradingService#getViewableAssignmentsForCurrentUser(java.lang.String)
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<Assignment> getViewableAssignmentsForCurrentUser(String gradebookUid) {

        return getViewableAssignmentsForCurrentUser(gradebookUid, SortType.SORT_BY_SORTING);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sakaiproject.grading.api.GradingService#getViewableAssignmentsForCurrentUser(java.lang.String, java.)
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<Assignment> getViewableAssignmentsForCurrentUser(final String gradebookUid, final SortType sortBy) {

        if (!gradingAuthz.isUserAbleToGradeAll(gradebookUid)
                && !gradingAuthz.isUserAbleToGrade(gradebookUid)
                && !gradingAuthz.isUserAbleToViewOwnGrades(gradebookUid)) {
            return Collections.<Assignment>emptyList();
        }

        List<GradebookAssignment> viewableAssignments = new ArrayList<>();
        final LinkedHashSet<Assignment> assignmentsToReturn = new LinkedHashSet<>();

        final Gradebook gradebook = getGradebook(gradebookUid);

        // will send back all assignments if user can grade all
        if (gradingAuthz.isUserAbleToGradeAll(gradebookUid)) {
            viewableAssignments = getAssignments(gradebook.getId(), sortBy, true);
        } else if (gradingAuthz.isUserAbleToGrade(gradebookUid)) {
            // if user can grade and doesn't have grader perm restrictions, they
            // may view all assigns
            if (!gradingPermissionService.currentUserHasGraderPermissions(gradebookUid)) {
                viewableAssignments = getAssignments(gradebook.getId(), sortBy, true);
            } else {
                // this user has grader perms, so we need to filter the items returned
                // if this gradebook has categories enabled, we need to check for category-specific restrictions
                if (gradebook.getCategoryType() == GradingCategoryType.NO_CATEGORY) {
                    assignmentsToReturn.addAll(getAssignments(gradebookUid, sortBy));
                } else {
                    final String userUid = getUserUid();
                    if (gradingPermissionService.getPermissionForUserForAllAssignment(gradebook.getId(), userUid)) {
                        assignmentsToReturn.addAll(getAssignments(gradebookUid, sortBy));
                    } else {
                        final List<org.sakaiproject.grading.api.Assignment> assignments = getAssignments(gradebookUid, sortBy);
                        final List<Long> categoryIds = ((List<Category>) getCategories(gradebook.getId())).stream().map(Category::getId)
                                .collect(Collectors.toList());
                        // categories are enabled, so we need to check the category restrictions
                        if (!categoryIds.isEmpty()) {
                            List<Long> viewableCategoryIds = gradingPermissionService.getCategoriesForUser(gradebook.getId(),
                                    userUid, categoryIds);
                            for (Assignment assignment : assignments) {
                                if (assignment.getCategoryId() != null && viewableCategoryIds.contains(assignment.getCategoryId())) {
                                    assignmentsToReturn.add(assignment);
                                }
                            }
                        }
                    }
                }
            }
        } else if (gradingAuthz.isUserAbleToViewOwnGrades(gradebookUid)) {
            // if user is just a student, we need to filter out unreleased items
            final List<GradebookAssignment> allAssigns = getAssignments(gradebook.getId(), sortBy, true);
            if (allAssigns != null) {
                for (final Iterator aIter = allAssigns.iterator(); aIter.hasNext();) {
                    final GradebookAssignment assign = (GradebookAssignment) aIter.next();
                    if (assign != null && assign.getReleased()) {
                        viewableAssignments.add(assign);
                    }
                }
            }
        }

        // Now we need to convert these to the assignment template objects
        if (viewableAssignments != null && !viewableAssignments.isEmpty()) {
            final boolean gbUsesCategories = gradebook.getCategoryType() != GradingCategoryType.NO_CATEGORY;
            for (final Object element : viewableAssignments) {
                final GradebookAssignment assignment = (GradebookAssignment) element;
                assignmentsToReturn.add(getAssignmentDefinition(assignment, gbUsesCategories));
            }
        }

        return new ArrayList<>(assignmentsToReturn);
    }

    @Override
    public Map<String, String> getViewableStudentsForItemForCurrentUser(String gradebookUid, Long gradableObjectId) {

        String userUid = sessionManager.getCurrentSessionUserId();
        return getViewableStudentsForItemForUser(userUid, gradebookUid, gradableObjectId);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map<String, String> getViewableStudentsForItemForUser(String userUid, String gradebookUid, Long gradableObjectId) {

        if (gradebookUid == null || gradableObjectId == null || userUid == null) {
            throw new IllegalArgumentException("null gradebookUid or gradableObjectId or " +
                    "userId passed to getViewableStudentsForUserForItem." +
                    " gradebookUid: " + gradebookUid + " gradableObjectId:" +
                    gradableObjectId + " userId: " + userUid);
        }

        if (!this.gradingAuthz.isUserAbleToGrade(gradebookUid, userUid)) {
            return new HashMap<>();
        }

        final GradebookAssignment gradebookItem = getAssignmentWithoutStatsByID(gradebookUid, gradableObjectId);

        if (gradebookItem == null) {
            log.debug("The gradebook item does not exist, so returning empty set");
            return new HashMap();
        }

        final Long categoryId = gradebookItem.getCategory() == null ? null : gradebookItem.getCategory().getId();

        final Map<EnrollmentRecord, String> enrRecFunctionMap = this.gradingAuthz.findMatchingEnrollmentsForItemForUser(userUid, gradebookUid,
                categoryId, getGradebook(gradebookUid).getCategoryType(), null, null);
        if (enrRecFunctionMap == null) {
            return new HashMap();
        }

        final Map<String, String> studentIdFunctionMap = new HashMap();
        for (final Entry<EnrollmentRecord, String> entry : enrRecFunctionMap.entrySet()) {
            final EnrollmentRecord enr = entry.getKey();
            if (enr != null && enrRecFunctionMap.get(enr) != null) {
                studentIdFunctionMap.put(enr.getUser().getUserUid(), entry.getValue());
            }
        }
        return studentIdFunctionMap;
    }

    @Override
    public boolean isGradableObjectDefined(final Long gradableObjectId) {

        if (gradableObjectId == null) {
            throw new IllegalArgumentException("null gradableObjectId passed to isGradableObjectDefined");
        }

        return gradingPersistenceManager.isAssignmentDefined(gradableObjectId);
    }

    @Override
    public Map<String, String> getViewableSectionUuidToNameMap(String gradebookUid) {

        if (gradebookUid == null) {
            throw new IllegalArgumentException("Null gradebookUid passed to getViewableSectionIdToNameMap");
        }

        return gradingAuthz.getViewableSections(gradebookUid).stream().filter(Objects::nonNull)
            .collect(Collectors.toMap(s -> s.getUuid(), s -> s.getTitle()));
    }

    @Override
    public boolean currentUserHasGradeAllPerm(final String gradebookUid) {

        return this.gradingAuthz.isUserAbleToGradeAll(gradebookUid);
    }

    @Override
    public boolean isUserAllowedToGradeAll(final String gradebookUid, final String userUid) {

        return this.gradingAuthz.isUserAbleToGradeAll(gradebookUid, userUid);
    }

    @Override
    public boolean currentUserHasGradingPerm(String gradebookUid) {

        return gradingAuthz.isUserAbleToGrade(gradebookUid);
    }

    @Override
    public boolean isUserAllowedToGrade(final String gradebookUid, final String userUid) {

        return this.gradingAuthz.isUserAbleToGrade(gradebookUid, userUid);
    }

    @Override
    public boolean currentUserHasEditPerm(final String gradebookUid) {

        return this.gradingAuthz.isUserAbleToEditAssessments(gradebookUid);
    }

    @Override
    public boolean currentUserHasViewOwnGradesPerm(final String gradebookUid) {

        return this.gradingAuthz.isUserAbleToViewOwnGrades(gradebookUid);
    }

    @Override
    public boolean currentUserHasViewStudentNumbersPerm(final String gradebookUid) {

        return this.gradingAuthz.isUserAbleToViewStudentNumbers(gradebookUid);
    }

    @Override
    public List<GradeDefinition> getGradesForStudentsForItem(final String gradebookUid, final Long gradableObjectId,
            final List<String> studentIds) {

        if (gradableObjectId == null) {
            throw new IllegalArgumentException("null gradableObjectId passed to getGradesForStudentsForItem");
        }

        if (studentIds == null || studentIds.isEmpty()) {
            return Collections.<GradeDefinition>emptyList();
        }

        final List<GradeDefinition> studentGrades = new ArrayList<>();

        // first, we need to make sure the current user is authorized to view the
        // grades for all of the requested students
        final GradebookAssignment gbItem = getAssignmentWithoutStatsByID(gradebookUid, gradableObjectId);

        if (gbItem != null) {
            final Gradebook gradebook = gbItem.getGradebook();

            if (!this.gradingAuthz.isUserAbleToGrade(gradebook.getUid())) {
                log.error(
                        "User {} attempted to access grade information without permission in gb {} using gradebookService.getGradesForStudentsForItem",
                        sessionManager.getCurrentSessionUserId(), gradebook.getUid());
                throw new GradingSecurityException();
            }

            final Long categoryId = gbItem.getCategory() != null ? gbItem.getCategory().getId() : null;
            final Map enrRecFunctionMap = this.gradingAuthz.findMatchingEnrollmentsForItem(gradebook.getUid(), categoryId,
                    gradebook.getCategoryType(), null, null);
            final Set enrRecs = enrRecFunctionMap.keySet();
            final Map studentIdEnrRecMap = new HashMap();
            if (enrRecs != null) {
                for (final Iterator enrIter = enrRecs.iterator(); enrIter.hasNext();) {
                    final EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
                    if (enr != null) {
                        studentIdEnrRecMap.put(enr.getUser().getUserUid(), enr);
                    }
                }
            }

            // filter the provided studentIds if user doesn't have permissions
            studentIds.removeIf(studentId -> {
                return !studentIdEnrRecMap.containsKey(studentId);
            });

            // retrieve the grading comments for all of the students
            final List<Comment> commentRecs = getComments(gbItem, studentIds);
            final Map<String, String> studentIdCommentTextMap = new HashMap();
            if (commentRecs != null) {
                for (final Comment comment : commentRecs) {
                    if (comment != null) {
                        studentIdCommentTextMap.put(comment.getStudentId(), comment.getCommentText());
                    }
                }
            }

            // now, we can populate the grade information
            final List<String> studentsWithGradeRec = new ArrayList<>();
            final List<AssignmentGradeRecord> gradeRecs = getAllAssignmentGradeRecordsForGbItem(gradableObjectId, studentIds);
            if (gradeRecs != null) {
                if (gradebook.getGradeType() == GradeType.LETTER) {
                    convertPointsToLetterGrade(gradebook, gradeRecs);
                } else if (gradebook.getGradeType() == GradeType.PERCENTAGE) {
                    convertPointsToPercentage(gradebook, gradeRecs);
                }

                for (final Object element : gradeRecs) {
                    final AssignmentGradeRecord agr = (AssignmentGradeRecord) element;
                    if (agr != null) {
                        final String commentText = studentIdCommentTextMap.get(agr.getStudentId());
                        final GradeDefinition gradeDef = convertGradeRecordToGradeDefinition(agr, gbItem, gradebook, commentText);

                        studentGrades.add(gradeDef);
                        studentsWithGradeRec.add(agr.getStudentId());
                    }
                }

                // if student has a comment but no grade add an empty grade definition with the comment
                if (studentsWithGradeRec.size() < studentIds.size()) {
                    for (final String studentId : studentIdCommentTextMap.keySet()) {
                        if (!studentsWithGradeRec.contains(studentId)) {
                            final String comment = studentIdCommentTextMap.get(studentId);
                            final AssignmentGradeRecord emptyGradeRecord = new AssignmentGradeRecord(gbItem, studentId, null);
                            final GradeDefinition gradeDef = convertGradeRecordToGradeDefinition(emptyGradeRecord, gbItem, gradebook,
                                    comment);
                            studentGrades.add(gradeDef);
                        }
                    }
                }
            }
        }

        return studentGrades;
    }

    @Override
    public Map<Long, List<GradeDefinition>> getGradesWithoutCommentsForStudentsForItems(final String gradebookUid,
            final List<Long> gradableObjectIds, final List<String> studentIds) {

        if (gradableObjectIds == null || gradableObjectIds.isEmpty()) {
            throw new IllegalArgumentException("null or empty gradableObjectIds passed to getGradesWithoutCommentsForStudentsForItems");
        }

        if (!this.gradingAuthz.isUserAbleToGrade(gradebookUid)) {
            throw new GradingSecurityException();
        }

        final Map<Long, List<GradeDefinition>> gradesMap = new HashMap<>();
        if (studentIds == null || studentIds.isEmpty()) {
            // We could populate the map with (gboId : new ArrayList()), but it's cheaper to allow get(gboId) to return null.
            return gradesMap;
        }

        // Get all the grades for the gradableObjectIds
        final List<AssignmentGradeRecord> gradeRecords = getAllAssignmentGradeRecordsForGbItems(gradableObjectIds, studentIds);
        // AssignmentGradeRecord is not in the API. So we need to convert grade records into GradeDefinition objects.
        // GradeDefinitions are not tied to their gbos, so we need to return a map associating them back to their gbos
        final List<GradeDefinition> gradeDefinitions = new ArrayList<>();
        for (final AssignmentGradeRecord gradeRecord : gradeRecords) {
            final GradebookAssignment gbo = (GradebookAssignment) gradeRecord.getGradableObject();
            final Long gboId = gbo.getId();
            final Gradebook gradebook = gbo.getGradebook();
            if (!gradebookUid.equals(gradebook.getUid())) {
                // The user is authorized against gradebookUid, but we have grades for another gradebook.
                // This is an authorization issue caused by gradableObjectIds violating the method contract.
                throw new IllegalArgumentException("gradableObjectIds must belong to grades within this gradebook");
            }

            final GradeDefinition gradeDef = convertGradeRecordToGradeDefinition(gradeRecord, gbo, gradebook, null);

            List<GradeDefinition> gradeList = gradesMap.get(gboId);
            if (gradeList == null) {
                gradeList = new ArrayList<>();
                gradesMap.put(gboId, gradeList);
            }
            gradeList.add(gradeDef);
        }

        return gradesMap;
    }

    /**
     * Converts an AssignmentGradeRecord into a GradeDefinition object.
     *
     * @param gradeRecord
     * @param gbo
     * @param gradebook
     * @param commentText - goes into the GradeComment attribute. Will be omitted if null
     * @return a GradeDefinition object whose attributes match the passed in gradeRecord
     */
    private GradeDefinition convertGradeRecordToGradeDefinition(final AssignmentGradeRecord gradeRecord, final GradebookAssignment gbo,
            final Gradebook gradebook, final String commentText) {
        final GradeDefinition gradeDef = new GradeDefinition();
        gradeDef.setStudentUid(gradeRecord.getStudentId());
        gradeDef.setGraderUid(gradeRecord.getGraderId());
        gradeDef.setDateRecorded(gradeRecord.getDateRecorded());
        final GradeType gradeEntryType = gradebook.getGradeType();
        gradeDef.setGradeEntryType(gradeEntryType);
        String grade = null;
        if (gradeEntryType == GradeType.LETTER) {
            grade = gradeRecord.getLetterEarned();
        } else if (gradeEntryType == GradeType.PERCENTAGE) {
            final Double percentEarned = gradeRecord.getPercentEarned();
            grade = percentEarned != null ? percentEarned.toString() : null;
        } else {
            final Double pointsEarned = gradeRecord.getPointsEarned();
            grade = pointsEarned != null ? pointsEarned.toString() : null;
        }
        gradeDef.setGrade(grade);
        gradeDef.setGradeReleased(gradebook.getAssignmentsDisplayed() && gbo.getReleased());

        if (commentText != null) {
            gradeDef.setGradeComment(commentText);
        }

        gradeDef.setExcused(gradeRecord.getExcludedFromGrade());

        return gradeDef;
    }

    @Override
    public boolean isGradeValid(String gradebookUuid, String grade) {

        if (gradebookUuid == null) {
            throw new IllegalArgumentException("Null gradebookUuid passed to isGradeValid");
        }

        Gradebook gradebook = getGradebook(gradebookUuid);
        GradeType gradeType = getGradebook(gradebookUuid).getGradeType();
        LetterGradePercentMapping mapping = null;
        if (gradeType == GradeType.LETTER) {
            mapping = getLetterGradePercentMapping(gradebook);
        }

        return isGradeValid(grade, gradeType, mapping);
    }

    @Override
    public boolean isValidNumericGrade(final String grade) {
        boolean gradeIsValid = false;

        try {
            final NumberFormat nbFormat = NumberFormat.getInstance(resourceLoader.getLocale());
            final Double gradeAsDouble = nbFormat.parse(grade).doubleValue();
            final String decSeparator = ((DecimalFormat) nbFormat).getDecimalFormatSymbols().getDecimalSeparator() + "";

            // grade must be greater than or equal to 0
            if (gradeAsDouble >= 0) {
                final String[] splitOnDecimal = grade.split("\\" + decSeparator);
                // check that there are no more than 2 decimal places
                if (splitOnDecimal == null) {
                    gradeIsValid = true;

                    // check for a valid score matching ##########.##
                    // where integer is maximum of 10 integers in length
                    // and maximum of 2 decimal places
                } else if (grade.matches("[0-9]{0,10}(\\" + decSeparator + "[0-9]{0,2})?")) {
                    gradeIsValid = true;
                }
            }
        } catch (NumberFormatException | ParseException nfe) {
            log.debug("Passed grade is not a numeric value");
        }

        return gradeIsValid;
    }

    private boolean isGradeValid(final String grade, final GradeType gradeEntryType, final LetterGradePercentMapping gradeMapping) {

        boolean gradeIsValid = false;

        if (grade == null || "".equals(grade)) {

            gradeIsValid = true;

        } else {

            if (gradeEntryType == GradeType.POINTS ||
                    gradeEntryType == GradeType.PERCENTAGE) {
                try {
                    final NumberFormat nbFormat = NumberFormat.getInstance(resourceLoader.getLocale());
                    final Double gradeAsDouble = nbFormat.parse(grade).doubleValue();
                    final String decSeparator = ((DecimalFormat) nbFormat).getDecimalFormatSymbols().getDecimalSeparator() + "";
                    // grade must be greater than or equal to 0
                    if (gradeAsDouble >= 0) {
                        final String[] splitOnDecimal = grade.split("\\" + decSeparator);
                        // check that there are no more than 2 decimal places
                        if (splitOnDecimal == null) {
                            gradeIsValid = true;

                            // check for a valid score matching ##########.##
                            // where integer is maximum of 10 integers in length
                            // and maximum of 2 decimal places
                        } else if (grade.matches("[0-9]{0,10}(\\" + decSeparator + "[0-9]{0,2})?")) {
                            gradeIsValid = true;
                        }
                    }
                } catch (NumberFormatException | ParseException nfe) {
                    log.debug("Passed grade is not a numeric value");
                }

            } else if (gradeEntryType == GradeType.LETTER) {
                if (gradeMapping == null) {
                    throw new IllegalArgumentException("Null mapping passed to isGradeValid for a letter grade-based gradeook");
                }

                final String standardizedGrade = gradeMapping.standardizeInputGrade(grade);
                if (standardizedGrade != null) {
                    gradeIsValid = true;
                }
            } else {
                throw new IllegalArgumentException("Invalid gradeEntryType passed to isGradeValid");
            }
        }

        return gradeIsValid;
    }

    @Override
    public List<String> identifyStudentsWithInvalidGrades(final String gradebookUid, final Map<String, String> studentIdToGradeMap) {

        if (gradebookUid == null) {
            throw new IllegalArgumentException("null gradebookUid passed to identifyStudentsWithInvalidGrades");
        }

        final List<String> studentsWithInvalidGrade = new ArrayList<>();

        if (studentIdToGradeMap != null) {
            Gradebook gradebook = getGradebook(gradebookUid);
            GradeType gradeType = gradebook.getGradeType();

            LetterGradePercentMapping gradeMapping = null;
            if (gradeType == GradeType.LETTER) {
                gradeMapping = getLetterGradePercentMapping(gradebook);
            }

            for (final String studentId : studentIdToGradeMap.keySet()) {
                final String grade = studentIdToGradeMap.get(studentId);
                if (!isGradeValid(grade, gradeType, gradeMapping)) {
                    studentsWithInvalidGrade.add(studentId);
                }
            }
        }
        return studentsWithInvalidGrade;
    }

    @Override
    @Transactional
    public void saveGradeAndCommentForStudent(final String gradebookUid, final Long gradableObjectId, final String studentUid,
            final String grade, final String comment) {

        if (gradebookUid == null || gradableObjectId == null || studentUid == null) {
            throw new IllegalArgumentException(
                    "Null gradebookUid or gradableObjectId or studentUid passed to saveGradeAndCommentForStudent");
        }

        final GradeDefinition gradeDef = new GradeDefinition();
        gradeDef.setStudentUid(studentUid);
        gradeDef.setGrade(grade);
        gradeDef.setGradeComment(comment);

        final List<GradeDefinition> gradeDefList = new ArrayList<>();
        gradeDefList.add(gradeDef);

        final GradebookAssignment assignment = getAssignmentWithoutStats(gradebookUid, gradableObjectId);

        final AssignmentGradeRecord record = getAssignmentGradeRecord(assignment, studentUid);
        if (record != null) {
            gradeDef.setExcused(BooleanUtils.toBoolean(record.getExcludedFromGrade()));
        } else {
            gradeDef.setExcused(false);
        }
        saveGradesAndComments(gradebookUid, gradableObjectId, gradeDefList);
    }

    @Override
    public void saveGradeAndExcuseForStudent(final String gradebookUid, final Long gradableObjectId, final String studentUid,
            final String grade, final boolean excuse) {
        if (gradebookUid == null || gradableObjectId == null || studentUid == null) {
            throw new IllegalArgumentException(
                    "Null gradebookUid, gradeableObjectId, or studentUid passed to saveGradeAndExcuseForStudent");
        }

        final GradeDefinition gradeDef = new GradeDefinition();
        gradeDef.setStudentUid(studentUid);
        gradeDef.setGrade(grade);
        gradeDef.setExcused(excuse);

        // Lookup any existing comments and set the text so that they don't get wiped out on a save
        CommentDefinition gradeComment = getAssignmentScoreComment(gradebookUid, gradableObjectId, studentUid);
        if (gradeComment != null) {
            gradeDef.setGradeComment(gradeComment.getCommentText());
        }

        final List<GradeDefinition> gradeDefList = new ArrayList<>();
        gradeDefList.add(gradeDef);

        saveGradesAndComments(gradebookUid, gradableObjectId, gradeDefList);
    }

    /**
     * Gets the course grade record for a student, or null if it does not yet exist.
     *
     * @param studentId The student ID
     * @return The course grade record for the student
     */
    private CourseGradeRecord getCourseGradeRecord(final Gradebook gradebook, final String studentId) {
        return gradingPersistenceManager.getCourseGradeRecord(gradebook, studentId).orElse(null);
    }


    @Override
    public boolean getIsAssignmentExcused(final String gradebookUid, final Long assignmentId, final String studentUid) throws AssessmentNotFoundException {

        if (gradebookUid == null || assignmentId == null || studentUid == null) {
            throw new IllegalArgumentException("null parameter passed to getAssignmentScoreComment. Values are gradebookUid:" + gradebookUid + " assignmentId:" + assignmentId + " studentUid:"+ studentUid);
        }
        GradebookAssignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentId);
        AssignmentGradeRecord agr = getAssignmentGradeRecord(assignment, studentUid);

        if (agr == null) {
            return false;
        }else{
            return agr.getExcludedFromGrade();
        }
    }

    @Override
    @Transactional
    public void saveGradesAndComments(final String gradebookUid, final Long gradableObjectId, final List<GradeDefinition> gradeDefList) {

        if (gradebookUid == null || gradableObjectId == null) {
            throw new IllegalArgumentException("Null gradebookUid or gradableObjectId passed to saveGradesAndComments");
        }

        if (CollectionUtils.isEmpty(gradeDefList)) {
            return;
        }

        final GradebookAssignment assignment = getAssignmentWithoutStatsByID(gradebookUid, gradableObjectId);
        if (assignment == null) {
            throw new AssessmentNotFoundException("No gradebook item exists with gradable object id = " + gradableObjectId);
        }

        if (!currentUserHasGradingPerm(gradebookUid)) {
            log.warn("User attempted to save grades and comments without authorization");
            throw new GradingSecurityException();
        }

        // identify all of the students being updated first
        final Map<String, GradeDefinition> studentIdGradeDefMap = new HashMap<>();
        final Map<String, String> studentIdToGradeMap = new HashMap<>();

        for (final GradeDefinition gradeDef : gradeDefList) {
            studentIdGradeDefMap.put(gradeDef.getStudentUid(), gradeDef);
            studentIdToGradeMap.put(gradeDef.getStudentUid(), gradeDef.getGrade());
        }

        /*
         * TODO: this check may be unnecessary if we're validating grades in the first step of the grade import wizard BUT, this can
         * only be removed if the only place it's used is in the grade import (other places may not perform the grade validation prior
         * to calling this
         */
        // Check for invalid grades
        final List<String> invalidStudentUUIDs = identifyStudentsWithInvalidGrades(gradebookUid, studentIdToGradeMap);
        if (CollectionUtils.isNotEmpty(invalidStudentUUIDs)) {
            throw new InvalidGradeException(
                    "At least one grade passed to be updated is " + "invalid. No grades or comments were updated.");
        }

        // Retrieve all existing grade records for the given students and assignment
        final List<AssignmentGradeRecord> existingGradeRecords = getAllAssignmentGradeRecordsForGbItem(gradableObjectId,
                studentIdGradeDefMap.keySet());
        final Map<String, AssignmentGradeRecord> studentIdGradeRecordMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(existingGradeRecords)) {
            for (final AssignmentGradeRecord agr : existingGradeRecords) {
                studentIdGradeRecordMap.put(agr.getStudentId(), agr);
            }
        }

        // Retrieve all existing comments for the given students and assignment
        final List<Comment> existingComments = getComments(assignment, studentIdGradeDefMap.keySet());
        final Map<String, Comment> studentIdCommentMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(existingComments)) {
            for (final Comment comment : existingComments) {
                studentIdCommentMap.put(comment.getStudentId(), comment);
            }
        }

        Gradebook gradebook = getGradebook(gradebookUid);

        final boolean userHasGradeAllPerm = currentUserHasGradeAllPerm(gradebookUid);
        final String graderId = sessionManager.getCurrentSessionUserId();
        final Date now = new Date();
        LetterGradePercentMapping mapping = null;
        if (gradebook.getGradeType() == GradeType.LETTER) {
            mapping = getLetterGradePercentMapping(gradebook);
        }

        // Don't use a HashSet because you may have multiple Comments with null ID and the same comment at this point.
        // The Comment object defines objects as equal if they have the same ID, comment text, and gradebook item. The
        // only difference may be the student IDs
        final List<Comment> commentsToUpdate = new ArrayList<>();
        final Set<GradingEvent> eventsToAdd = new HashSet<>();
        final Set<AssignmentGradeRecord> gradeRecordsToUpdate = new HashSet<>();
        for (final GradeDefinition gradeDef : gradeDefList) {
            final String studentId = gradeDef.getStudentUid();

            // use the grader ID from the definition if it is not null, otherwise use the current user ID
            final String graderUid = gradeDef.getGraderUid() != null ? gradeDef.getGraderUid() : graderId;
            // use the grade date from the definition if it is not null, otherwise use the current date
            final Date gradedDate = gradeDef.getDateRecorded() != null ? gradeDef.getDateRecorded() : now;

            final boolean excuse = gradeDef.isExcused();

            // check specific grading privileges if user does not have
            // grade all perm
            if (!userHasGradeAllPerm) {
                if (!isUserAbleToGradeItemForStudent(gradebookUid, gradableObjectId, studentId)) {
                    log.warn("User {} attempted to save a grade for {} without authorization", graderId, studentId);
                    throw new GradingSecurityException();
                }
            }
            // Determine if the AssignmentGradeRecord needs to be updated
            final String newGrade = StringUtils.trimToEmpty(gradeDef.getGrade());
            final Double convertedGrade = convertInputGradeToPoints(gradebook.getGradeType(), mapping, assignment.getPointsPossible(),
                    newGrade);
            AssignmentGradeRecord gradeRec = studentIdGradeRecordMap.get(studentId);
            boolean currentExcuse;
            if (gradeRec == null) {
                currentExcuse = false;
            } else {
                currentExcuse = BooleanUtils.toBoolean(gradeRec.getExcludedFromGrade());
            }

            if (gradeRec != null) {
                final Double pointsEarned = gradeRec.getPointsEarned();
                if ((convertedGrade == null && pointsEarned != null)
                        || (convertedGrade != null && pointsEarned == null)
                        || (convertedGrade != null && pointsEarned != null && !convertedGrade.equals(pointsEarned))
                        || (excuse != currentExcuse)) {

                    gradeRec.setPointsEarned(convertedGrade);
                    gradeRec.setGraderId(graderUid);
                    gradeRec.setDateRecorded(gradedDate);
                    gradeRec.setExcludedFromGrade(excuse);
                    gradeRecordsToUpdate.add(gradeRec);

                    // Add a GradingEvent, which stores the actual input grade rather than the converted one
                    final GradingEvent event = new GradingEvent(assignment, graderId, studentId, newGrade);
                    if (excuse != currentExcuse) {
                        event.setStatus(excuse ?
                                GradingEventStatus.GRADE_EXCLUDED :
                                GradingEventStatus.GRADE_INCLUDED);
                    }
                    eventsToAdd.add(event);
                }
            } else {
                // if the grade is something other than null, add a new AGR
                if (StringUtils.isNotBlank(newGrade) && (StringUtils.isNotBlank(gradeDef.getGrade()) || excuse != currentExcuse)) {
                    gradeRec = new AssignmentGradeRecord(assignment, studentId, convertedGrade);
                    gradeRec.setGraderId(graderUid);
                    gradeRec.setDateRecorded(gradedDate);
                    gradeRecordsToUpdate.add(gradeRec);
                    gradeRec.setExcludedFromGrade(excuse);

                    // Add a GradingEvent, which stores the actual input grade rather than the converted one
                    final GradingEvent event = new GradingEvent(assignment, graderId, studentId, newGrade);
                    if (excuse != currentExcuse) {
                        event.setStatus(excuse ?
                                GradingEventStatus.GRADE_EXCLUDED:
                                GradingEventStatus.GRADE_INCLUDED);
                    }
                    eventsToAdd.add(event);
                }
            }
            // Determine if the Comment needs to be updated
            Comment comment = studentIdCommentMap.get(studentId);
            final String newCommentText = StringUtils.trimToEmpty(gradeDef.getGradeComment());
            if (comment != null) {
                final String existingCommentText = StringUtils.trimToEmpty(comment.getCommentText());
                final boolean existingCommentTextIsEmpty = existingCommentText.isEmpty();
                final boolean newCommentTextIsEmpty = newCommentText.isEmpty();
                if ((existingCommentTextIsEmpty && !newCommentTextIsEmpty)
                        || (!existingCommentTextIsEmpty && newCommentTextIsEmpty)
                        || (!existingCommentTextIsEmpty && !newCommentTextIsEmpty && !newCommentText.equals(existingCommentText))) {
                    comment.setCommentText(newCommentText);
                    comment.setGraderId(graderId);
                    comment.setDateRecorded(gradedDate);
                    commentsToUpdate.add(comment);
                }
            } else {
                // If the comment is something other than null, add a new Comment
                if (!newCommentText.isEmpty()) {
                    comment = new Comment(studentId, newCommentText, assignment);
                    comment.setGraderId(graderId);
                    comment.setDateRecorded(gradedDate);
                    commentsToUpdate.add(comment);
                }
            }
        }

        // Save or update the necessary items
        try {
            gradeRecordsToUpdate.forEach(gradingPersistenceManager::saveAssignmentGradeRecord);
            commentsToUpdate.forEach(gradingPersistenceManager::saveComment);
            eventsToAdd.forEach(gradingPersistenceManager::saveGradingEvent);
        } catch (final HibernateOptimisticLockingFailureException | StaleObjectStateException holfe) {
            // TODO: Adrian How janky is this?
            log.info("An optimistic locking failure occurred while attempting to save scores and comments for gb Item {}", gradableObjectId);
            throw new StaleObjectModificationException(holfe);
        }
    }

    /**
     * Helper method to retrieve Assignment by ID without stats for the given gradebook. Reduces code duplication in several areas.
     *
     * @param gradebookUID
     * @param gradeableObjectID
     * @return
     */
    private GradebookAssignment getAssignmentWithoutStatsByID(String gradebookUID, Long gradeableObjectID) {

        return getAssignmentWithoutStats(gradebookUID, gradeableObjectID);
    }

    /**
     *
     * @param gradeEntryType
     * @param mapping
     * @param gbItemPointsPossible
     * @param grade
     * @return given a generic String grade, converts it to the equivalent Double point value that will be stored in the db based upon the
     *         gradebook's grade entry type
     */
    private Double convertInputGradeToPoints(final GradeType gradeEntryType, final LetterGradePercentMapping mapping,
            final Double gbItemPointsPossible, final String grade) throws InvalidGradeException {

        if (StringUtils.isBlank(grade)) {
            return null;
        }

        Double convertedValue = null;
        if (gradeEntryType == GradeType.POINTS) {
            try {
                final NumberFormat nbFormat = NumberFormat.getInstance(resourceLoader.getLocale());
                final Double pointValue = nbFormat.parse(grade).doubleValue();
                convertedValue = pointValue;
            } catch (NumberFormatException | ParseException nfe) {
                throw new InvalidGradeException("Invalid grade passed to convertInputGradeToPoints");
            }
        } else if (gradeEntryType == GradeType.PERCENTAGE ||
                gradeEntryType == GradeType.LETTER) {

            // for letter or %-based grading, we need to calculate the equivalent point value
            if (gbItemPointsPossible == null) {
                throw new IllegalArgumentException("Null points possible passed" +
                        " to convertInputGradeToPoints for letter or % based grading");
            }

            Double percentage = null;
            if (gradeEntryType == GradeType.LETTER) {
                if (mapping == null) {
                    throw new IllegalArgumentException("No mapping passed to convertInputGradeToPoints for a letter-based gb");
                }

                if (mapping.getGradeMap() != null) {
                    // standardize the grade mapping
                    final String standardizedGrade = mapping.standardizeInputGrade(grade);
                    percentage = mapping.getValue(standardizedGrade);
                    if (percentage == null) {
                        throw new IllegalArgumentException("Invalid grade passed to convertInputGradeToPoints");
                    }
                }
            } else {
                try {
                    final NumberFormat nbFormat = NumberFormat.getInstance(resourceLoader.getLocale());
                    percentage = nbFormat.parse(grade).doubleValue();
                } catch (NumberFormatException | ParseException nfe) {
                    throw new IllegalArgumentException("Invalid % grade passed to convertInputGradeToPoints");
                }
            }

            convertedValue = calculateEquivalentPointValueForPercent(gbItemPointsPossible, percentage);

        } else {
            throw new InvalidGradeException("invalid grade entry type passed to convertInputGradeToPoints");
        }

        return convertedValue;
    }

    @Override
    public GradeType getGradeEntryType(String gradebookUid) {

        if (gradebookUid == null) {
            throw new IllegalArgumentException("null gradebookUid passed to getGradeEntryType");
        }

        return getGradebook(gradebookUid).getGradeType();
    }

    @Override
    public Map getEnteredCourseGrade(final String gradebookUid) {

        final Gradebook thisGradebook = getGradebook(gradebookUid);

        final Long gradebookId = thisGradebook.getId();
        final CourseGrade courseGrade = getCourseGrade(gradebookId);

        Map enrollmentMap;

        final Map viewableEnrollmentsMap
            = gradingAuthz.findMatchingEnrollmentsForViewableCourseGrade(gradebookUid, thisGradebook.getCategoryType(), null, null);

        enrollmentMap = new HashMap();

        final Map enrollmentMapUid = new HashMap();
        for (final Iterator iter = viewableEnrollmentsMap.keySet().iterator(); iter.hasNext();) {
            final EnrollmentRecord enr = (EnrollmentRecord) iter.next();
            enrollmentMap.put(enr.getUser().getUserUid(), enr);
            enrollmentMapUid.put(enr.getUser().getUserUid(), enr);
        }

        final List<CourseGradeRecord> unfilteredRecords
            = gradingPersistenceManager.getCourseGradeRecordsForCourseGrade(courseGrade.getId());

        final List<CourseGradeRecord> records = filterAndPopulateCourseGradeRecordsByStudents(courseGrade, unfilteredRecords, enrollmentMap.keySet());

        final Map returnMap = new HashMap();

        for (CourseGradeRecord cgr : records) {
            if (cgr.getEnteredGrade() != null && !cgr.getEnteredGrade().equalsIgnoreCase("")) {
                final EnrollmentRecord enr = (EnrollmentRecord) enrollmentMapUid.get(cgr.getStudentId());
                if (enr != null) {
                    returnMap.put(enr.getUser().getDisplayId(), cgr.getEnteredGrade());
                }
            }
        }

        return returnMap;
    }

    @Override
    public String getAssignmentScoreString(String gradebookUid, Long assignmentId, String studentUid)
            throws AssessmentNotFoundException {

        final boolean studentRequestingOwnScore = sessionManager.getCurrentSessionUserId().equals(studentUid);

        if (gradebookUid == null || assignmentId == null || studentUid == null) {
            throw new IllegalArgumentException("null parameter passed to getAssignment. Values are gradebookUid:"
                    + gradebookUid + " assignmentId:" + assignmentId + " studentUid:" + studentUid);
        }

        Double assignmentScore = null;

        final GradebookAssignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentId);
        if (assignment == null) {
            throw new AssessmentNotFoundException(
                    "There is no assignment with id " + assignmentId + " in gradebook " + gradebookUid);
        }

        if (!studentRequestingOwnScore && !isUserAbleToViewItemForStudent(gradebookUid, assignmentId, studentUid)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to retrieve grade for student {} for assignment {}",
                    getUserUid(), gradebookUid, studentUid, assignment.getName());
            throw new GradingSecurityException();
        }

        // If this is the student, then the assignment needs to have
        // been released.
        if (studentRequestingOwnScore && !assignment.getReleased()) {
            log.error("AUTHORIZATION FAILURE: Student {} in gradebook {} attempted to retrieve score for unreleased assignment {}",
                    getUserUid(), gradebookUid, assignment.getName());
            throw new GradingSecurityException();
        }

        final AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid);
        log.debug("gradeRecord={}", gradeRecord);
        if (gradeRecord != null) {
            assignmentScore = gradeRecord.getPointsEarned();
        }

        // TODO: when ungraded items is considered, change column to ungraded-grade
        // its possible that the assignment score is null
        if (assignmentScore == null) {
            return null;
        }

        // avoid scientific notation on large scores by using a formatter
        final NumberFormat numberFormat = NumberFormat.getInstance(resourceLoader.getLocale());
        final DecimalFormat df = (DecimalFormat) numberFormat;
        df.setGroupingUsed(false);

        log.debug("assignment score before formatting: {}", assignmentScore);

        String formatted = df.format(assignmentScore);
        log.debug("assignment score after formatting: {}", formatted);
        return formatted;
    }

    @Override
    public String getAssignmentScoreString(final String gradebookUid, final String assignmentName, final String studentUid)
            throws AssessmentNotFoundException {

        if (gradebookUid == null || assignmentName == null || studentUid == null) {
            throw new IllegalArgumentException("null parameter passed to getAssignment. Values are gradebookUid:"
                    + gradebookUid + " assignmentName:" + assignmentName + " studentUid:" + studentUid);
        }

        final GradebookAssignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentName);

        if (assignment == null) {
            throw new AssessmentNotFoundException("There is no assignment with name " + assignmentName + " in gradebook " + gradebookUid);
        }

        return getAssignmentScoreString(gradebookUid, assignment.getId(), studentUid);
    }

    @Override
    public String getAssignmentScoreStringByNameOrId(final String gradebookUid, final String assignmentName, final String studentUid)
            throws AssessmentNotFoundException {
        String score = null;
        try {
            score = getAssignmentScoreString(gradebookUid, assignmentName, studentUid);
        } catch (final AssessmentNotFoundException e) {
            // Don't fail on this exception
            log.debug("Assessment not found by name", e);
        } catch (final GradingSecurityException gse) {
            log.warn("User {} does not have permission to retrieve score for assignment {}", studentUid, assignmentName, gse);
            return null;
        }

        if (score == null) {
            // Try to get the assignment by id
            if (NumberUtils.isCreatable(assignmentName)) {
                final Long assignmentId = NumberUtils.toLong(assignmentName, -1L);
                try {
                    score = getAssignmentScoreString(gradebookUid, assignmentId, studentUid);
                } catch (AssessmentNotFoundException anfe) {
                    log.debug("Assessment could not be found for gradebook id {} and assignment id {} and student id {}", gradebookUid, assignmentName, studentUid);
                }
            }
        }
        return score;
    }

    @Override
    public void setAssignmentScoreString(String gradebookUid, Long assignmentId, String studentUid, String score, String clientServiceDescription)
            throws AssessmentNotFoundException {

        final GradebookAssignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentId);
        if (assignment == null) {
            throw new AssessmentNotFoundException(
                    "There is no assignment with id " + assignmentId + " in gradebook " + gradebookUid);
        }
        if (assignment.getExternallyMaintained()) {
            log.error(
                    "AUTHORIZATION FAILURE: User {} in gradebook {} attempted to grade externally maintained assignment {} from {}",
                    getUserUid(), gradebookUid, assignmentId, clientServiceDescription);
            throw new GradingSecurityException();
        }

        if (!isUserAbleToGradeItemForStudent(gradebookUid, assignment.getId(), studentUid)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to grade student {} from {} for item {}",
                    getUserUid(), gradebookUid, studentUid, clientServiceDescription, assignmentId);
            throw new GradingSecurityException();
        }

        final Date now = new Date();
        final String graderId = sessionManager.getCurrentSessionUserId();
        AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid);
        if (gradeRecord == null) {
            // Creating a new grade record.
            gradeRecord = new AssignmentGradeRecord(assignment, studentUid, convertStringToDouble(score));
            // TODO: test if it's ungraded item or not. if yes, set ungraded grade for this record. if not, need validation??
        } else {
            // TODO: test if it's ungraded item or not. if yes, set ungraded grade for this record. if not, need validation??
            gradeRecord.setPointsEarned(convertStringToDouble(score));
        }
        gradeRecord.setGraderId(graderId);
        gradeRecord.setDateRecorded(now);
        gradingPersistenceManager.saveAssignmentGradeRecord(gradeRecord);

        gradingPersistenceManager.saveGradingEvent(new GradingEvent(assignment, graderId, studentUid, score));

        // Post an event in SAKAI_EVENT table
        postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, convertStringToDouble(score));

        log.debug("Score updated in gradebookUid={}, assignmentId={} by userUid={} from client={}, new score={}", gradebookUid, assignmentId, getUserUid()
                , clientServiceDescription, score);
    }

    @Override
    public void setAssignmentScoreString(String gradebookUid, String assignmentName, String studentUid, String score, String clientServiceDescription)
            throws AssessmentNotFoundException {

        GradebookAssignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentName);

        if (assignment == null) {
            throw new AssessmentNotFoundException("There is no assignment with name " + assignmentName + " in gradebook " + gradebookUid);
        }

        setAssignmentScoreString(gradebookUid, assignment.getId(), studentUid, score, clientServiceDescription);
    }

    @Override
    public void finalizeGrades(final String gradebookUid) {

        if (!gradingAuthz.isUserAbleToGradeAll(gradebookUid)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to finalize grades", getUserUid(), gradebookUid);
            throw new GradingSecurityException();
        }
        finalizeNullGradeRecords(getGradebook(gradebookUid));
    }

    @Override
    public String getLowestPossibleGradeForGbItem(final String gradebookUid, final Long gradebookItemId) {

        if (gradebookUid == null || gradebookItemId == null) {
            throw new IllegalArgumentException("Null gradebookUid and/or gradebookItemId " +
                    "passed to getLowestPossibleGradeForGbItem. gradebookUid:" +
                    gradebookUid + " gradebookItemId:" + gradebookItemId);
        }

        final GradebookAssignment gbItem = getAssignmentWithoutStatsByID(gradebookUid, gradebookItemId);

        if (gbItem == null) {
            throw new AssessmentNotFoundException("No gradebook item found with id " + gradebookItemId);
        }

        final Gradebook gradebook = gbItem.getGradebook();

        // double check that user has some permission to access gb items in this site
        if (!isUserAbleToViewAssignments(gradebookUid) && !currentUserHasViewOwnGradesPerm(gradebookUid)) {
            throw new GradingSecurityException();
        }

        String lowestPossibleGrade = null;

        if (gbItem.getUngraded()) {
            lowestPossibleGrade = null;
        } else if (gradebook.getGradeType() == GradeType.PERCENTAGE ||
                gradebook.getGradeType() == GradeType.POINTS) {
            lowestPossibleGrade = "0";
        } else if (gbItem.getGradebook().getGradeType() == GradeType.LETTER) {
            final LetterGradePercentMapping mapping = getLetterGradePercentMapping(gradebook);
            lowestPossibleGrade = mapping.getGrade(0d);
        }

        return lowestPossibleGrade;
    }

    @Override
    public List<CategoryDefinition> getCategoryDefinitions(String gradebookUid) {

        if (gradebookUid == null) {
            throw new IllegalArgumentException("Null gradebookUid passed to getCategoryDefinitions");
        }

        if (!isUserAbleToViewAssignments(gradebookUid)) {
            log.warn("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to retrieve all categories without permission", getUserUid(),
                    gradebookUid);
            throw new GradingSecurityException();
        }

        return getCategories(getGradebook(gradebookUid).getId())
            .stream().map(this::getCategoryDefinition).collect(Collectors.toList());
    }

    private CategoryDefinition getCategoryDefinition(final Category category) {

        final CategoryDefinition categoryDef = new CategoryDefinition();
        if (category != null) {
            categoryDef.setId(category.getId());
            categoryDef.setName(category.getName());
            categoryDef.setWeight(category.getWeight());
            categoryDef.setDropLowest(category.getDropLowest());
            categoryDef.setDropHighest(category.getDropHighest());
            categoryDef.setKeepHighest(category.getKeepHighest());
            categoryDef.setAssignmentList(getAssignments(category.getGradebook().getUid(), category.getName()));
            categoryDef.setDropKeepEnabled(category.isDropScores());
            categoryDef.setExtraCredit(category.getExtraCredit());
            categoryDef.setEqualWeight(category.getEqualWeightAssignments());
            categoryDef.setCategoryOrder(category.getCategoryOrder());
        }

        return categoryDef;
    }

    /**
     *
     * @param gradebookId
     * @param studentUids
     * @return a map of studentUid to a list of that student's AssignmentGradeRecords for the given studentUids list in the given gradebook.
     *         the grade records are all recs for assignments that are not removed and have a points possible > 0
     */
    private Map<String, List<AssignmentGradeRecord>> getGradeRecordMapForStudents(Long gradebookId, Collection<String> studentUids) {

        final Map<String, List<AssignmentGradeRecord>> filteredGradeRecs = new HashMap<>();
        final List<AssignmentGradeRecord> allGradeRecs = gradingPersistenceManager.getAssignmentGradeRecordsForGradebookAndStudents(gradebookId, studentUids);

        for (final AssignmentGradeRecord gradeRec : allGradeRecs) {
            if (studentUids.contains(gradeRec.getStudentId())) {
                final String studentId = gradeRec.getStudentId();
                List<AssignmentGradeRecord> gradeRecList = filteredGradeRecs.get(studentId);
                if (gradeRecList == null) {
                    gradeRecList = new ArrayList<>();
                    gradeRecList.add(gradeRec);
                    filteredGradeRecs.put(studentId, gradeRecList);
                } else {
                    gradeRecList.add(gradeRec);
                    filteredGradeRecs.put(studentId, gradeRecList);
                }
            }
        }

        return filteredGradeRecs;
    }

    /**
     *
     * @param session
     * @param gradebookId
     * @return a list of Assignments that have not been removed, are "counted", graded, and have a points possible > 0
     */
    private List<GradebookAssignment> getCountedAssignments(Long gradebookId) {

        List<GradebookAssignment> assignList = new ArrayList<>();

        List<GradebookAssignment> results = gradingPersistenceManager.getCountedAndGradedAssignmentsForGradebook(gradebookId);

        if (results != null) {
            // making sure there's no invalid points possible for normal assignments
            for (final GradebookAssignment a : results) {

                if (a.getPointsPossible() != null && a.getPointsPossible() > 0) {
                    assignList.add(a);
                }
            }
        }

        return assignList;
    }

    /**
     * set the droppedFromGrade attribute of each of the n highest and the n lowest scores of a student based on the assignment's category
     *
     * @param gradeRecords
     *
     *            NOTE: When the UI changes, this needs to be made private again
     */
    public void applyDropScores(final Collection<AssignmentGradeRecord> gradeRecords, GradingCategoryType categoryType) {

        if (gradeRecords == null || gradeRecords.size() < 1) {
            return;
        }
        final long start = System.currentTimeMillis();

        final Set<String> studentIds = new HashSet<>();
        final List<Category> categories = new ArrayList<>();
        final Map<String, List<AssignmentGradeRecord>> gradeRecordMap = new HashMap<>();
        for (final AssignmentGradeRecord gradeRecord : gradeRecords) {

            if (gradeRecord == null || gradeRecord.getPointsEarned() == null) {
                // don't consider grades that have null pointsEarned (this occurs when a
                // previously entered score for an assignment is removed; record stays in
                // database)
                continue;
            }

            // reset
            gradeRecord.setDroppedFromGrade(false);

            if (categoryType == GradingCategoryType.NO_CATEGORY) {
                continue;
            }

            final GradebookAssignment assignment = gradeRecord.getAssignment();
            if (assignment.getUngraded() // GradeType.LETTER
                    || assignment.getNotCounted() // don't consider grades that are not counted toward course grade
                    || assignment.getItemType().equals(GradebookAssignment.item_type_adjustment)
                    || assignment.getRemoved()) {
                continue;
            }
            // get all the students represented
            final String studentId = gradeRecord.getStudentId();
            studentIds.add(studentId);
            // get all the categories represented
            final Category cat = gradeRecord.getAssignment().getCategory();
            if (cat != null) {
                if (!categories.contains(cat)) {
                    categories.add(cat);
                }
                List<AssignmentGradeRecord> gradeRecordsByCatAndStudent = gradeRecordMap.get(studentId + cat.getId());
                if (gradeRecordsByCatAndStudent == null) {
                    gradeRecordsByCatAndStudent = new ArrayList<>();
                    gradeRecordsByCatAndStudent.add(gradeRecord);
                    gradeRecordMap.put(studentId + cat.getId(), gradeRecordsByCatAndStudent);
                } else {
                    gradeRecordsByCatAndStudent.add(gradeRecord);
                }
            }
        }

        if (categories.size() < 1 || categoryType == GradingCategoryType.NO_CATEGORY) {
            return;
        }
        for (final Category cat : categories) {
            final Integer dropHighest = cat.getDropHighest();
            Integer dropLowest = cat.getDropLowest();
            final Integer keepHighest = cat.getKeepHighest();
            final Long catId = cat.getId();

            if ((dropHighest != null && dropHighest > 0) || (dropLowest != null && dropLowest > 0)
                    || (keepHighest != null && keepHighest > 0)) {

                for (final String studentId : studentIds) {
                    // get the student's gradeRecords for this category
                    final List<AssignmentGradeRecord> gradesByCategory = new ArrayList<>();
                    final List<AssignmentGradeRecord> gradeRecordsByCatAndStudent = gradeRecordMap.get(studentId + cat.getId());
                    if (gradeRecordsByCatAndStudent != null) {
                        for (AssignmentGradeRecord agr : gradeRecordsByCatAndStudent) {
                            if (!BooleanUtils.toBoolean(agr.getExcludedFromGrade())) {
                                gradesByCategory.add(agr);
                            }
                        }

                        final int numGrades = gradesByCategory.size();

                        if (dropHighest > 0 && numGrades > dropHighest + dropLowest) {
                            for (int i = 0; i < dropHighest; i++) {
                                final AssignmentGradeRecord highest = Collections.max(gradesByCategory,
                                        AssignmentGradeRecord.numericComparator);
                                highest.setDroppedFromGrade(true);
                                gradesByCategory.remove(highest);
                                log.debug("dropHighest applied to {}", highest);
                            }
                        }

                        if (keepHighest > 0 && numGrades > (gradesByCategory.size() - keepHighest)) {
                            dropLowest = gradesByCategory.size() - keepHighest;
                        }

                        if (dropLowest > 0 && numGrades > dropLowest + dropHighest) {
                            for (int i = 0; i < dropLowest; i++) {
                                AssignmentGradeRecord lowest = Collections.min(gradesByCategory,
                                        AssignmentGradeRecord.numericComparator);
                                lowest.setDroppedFromGrade(true);
                                gradesByCategory.remove(lowest);
                                log.debug("dropLowest applied to {}", lowest);
                            }
                        }
                    }
                }
                log.debug("processed {} student in category {}", studentIds.size(), cat.getId());
            }
        }

        log.debug("GradebookManager.applyDropScores took {} millis to execute", (System.currentTimeMillis() - start));
    }

    @Override
    public PointsPossibleValidation isPointsPossibleValid(String gradebookUid, Assignment gradebookItem, Double pointsPossible) {

        if (gradebookUid == null) {
            throw new IllegalArgumentException("Null gradebookUid passed to isPointsPossibleValid");
        }
        if (gradebookItem == null) {
            throw new IllegalArgumentException("Null gradebookItem passed to isPointsPossibleValid");
        }

        // At this time, all gradebook items follow the same business rules for
        // points possible (aka relative weight in % gradebooks) so special logic
        // using the properties of the gradebook item is unnecessary.
        // In the future, we will have the flexibility to change
        // that behavior without changing the method signature

        // the points possible must be a non-null value greater than 0 with
        // no more than 2 decimal places

        if (pointsPossible == null) {
            return PointsPossibleValidation.INVALID_NULL_VALUE;
        }

        if (pointsPossible <= 0) {
            return PointsPossibleValidation.INVALID_NUMERIC_VALUE;
        }
        // ensure there are no more than 2 decimal places
        BigDecimal bd = new BigDecimal(pointsPossible);
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP); // Two decimal places
        double roundedVal = bd.doubleValue();
        double diff = pointsPossible - roundedVal;
        if (diff != 0) {
            return PointsPossibleValidation.INVALID_DECIMAL;
        }

        return PointsPossibleValidation.VALID;
    }

    /**
     *
     * @param doubleAsString
     * @return a locale-aware Double value representation of the given String
     * @throws ParseException
     */
    private Double convertStringToDouble(final String doubleAsString) {

        if (StringUtils.isBlank(doubleAsString)) {
            return null;
        }

        Double scoreAsDouble = null;
        try {
            NumberFormat numberFormat = NumberFormat.getInstance(resourceLoader.getLocale());
            Number numericScore = numberFormat.parse(doubleAsString.trim());
            return numericScore.doubleValue();
        } catch (final ParseException e) {
            log.error("Failed to convert {}: {}", doubleAsString, e.toString());
            return null;
        }
    }

    /**
     * Get a list of assignments in the gradebook attached to the given category. Note that each assignment only knows the category by name.
     *
     * <p>
     * Note also that this is different to {@link BaseHibernateManager#getAssignmentsForCategory(Long)} because this method returns the
     * shared GradebookAssignment object.
     *
     * @param gradebookUid
     * @param categoryName
     * @return
     */
    private List<Assignment> getAssignments(String gradebookUid, String categoryName) {

        return getAssignments(gradebookUid).stream()
            .filter(a -> StringUtils.equals(a.getCategoryName(), categoryName))
            .collect(Collectors.toList());
    }

    /**
     * Post an event to Sakai's event table
     *
     * @param gradebookUid
     * @param assignmentName
     * @param studentUid
     * @param pointsEarned
     * @return
     */
    private void postUpdateGradeEvent(String gradebookUid, String assignmentName, String studentUid, Double pointsEarned) {

        postEvent("gradebook.updateItemScore",
                "/gradebook/" + gradebookUid + "/" + assignmentName + "/" + studentUid + "/" + pointsEarned + "/student");
    }

    /**
     * Retrieves the calculated average course grade.
     */
    @Override
    @Transactional
    public String getAverageCourseGrade(final String gradebookUid) {

        if (gradebookUid == null) {
            throw new IllegalArgumentException("Null gradebookUid passed to getAverageCourseGrade");
        }
        // Check user has permission to invoke method.
        if (!currentUserHasGradeAllPerm(gradebookUid)) {
            StringBuilder sb = new StringBuilder()
                .append("User ")
                .append(sessionManager.getCurrentSessionUserId())
                .append(" attempted to access the average course grade without permission in gb ")
                .append(gradebookUid)
                .append(" using gradebookService.getAverageCourseGrade");
            throw new GradingSecurityException(sb.toString());
        }

        String courseGradeLetter = null;
        Gradebook gradebook = getGradebook(gradebookUid);
        if (gradebook != null) {
            CourseGrade courseGrade = getCourseGrade(gradebook.getId());
            Set<String> studentUids = getAllStudentUids(gradebookUid);
            // This call handles the complex rules of which assignments and grades to include in the calculation
            List<CourseGradeRecord> courseGradeRecs = getPointsEarnedCourseGradeRecords(courseGrade, studentUids);
            if (courseGrade != null) {
                // Calculate the course mean grade whether the student grade was manually entered or auto-calculated.
                courseGrade.calculateStatistics(courseGradeRecs, studentUids.size());
                if (courseGrade.getMean() != null) {
                    courseGradeLetter = gradebook.getSelectedGradeMapping().getMappedGrade(courseGrade.getMean());
                }
            }

        }
        return courseGradeLetter;
    }

    /**
     * Updates the order of an assignment
     *
     * @see GradingService.updateAssignmentOrder(java.lang.String gradebookUid, java.lang.Long assignmentId, java.lang.Integer order)
     */
    @Override
    @Transactional
    public void updateAssignmentOrder(final String gradebookUid, final Long assignmentId, Integer order) {

        if (!gradingAuthz.isUserAbleToEditAssessments(gradebookUid)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to change the order of assignment {}", getUserUid(),
                    gradebookUid, assignmentId);
            throw new GradingSecurityException();
        }

        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        final Long gradebookId = getGradebook(gradebookUid).getId();

        // get all assignments for this gradebook
        final List<GradebookAssignment> assignments = getAssignments(gradebookId, SortType.SORT_BY_SORTING, true);

        // find the assignment
        Optional<GradebookAssignment> optTarget = assignments.stream().filter(a -> a.getId().equals(assignmentId)).findAny();

        if (!optTarget.isPresent()) {
            throw new IllegalArgumentException("No assignment for id " + assignmentId);
        }

        GradebookAssignment target = optTarget.get();

        // adjust order to be within bounds
        if (order < 0) {
            order = 0;
        } else if (order > assignments.size()) {
            order = assignments.size();
        }

        // add the assignment to the list via a 'pad, remove, add' approach
        assignments.add(null); // ensure size remains the same for the remove
        assignments.remove(target); // remove item
        assignments.add(order, target); // add at ordered position, will shuffle others along

        // the assignments are now in the correct order within the list, we just need to update the sort order for each one
        // create a new list for the assignments we need to update in the database
        final List<GradebookAssignment> assignmentsToUpdate = new ArrayList<>();

        int i = 0;
        for (final GradebookAssignment a : assignments) {

            // skip if null
            if (a == null) {
                continue;
            }

            // if the sort order is not the same as the counter, update the order and add to the other list
            // this allows us to skip items that have not had their position changed and saves some db work later on
            // sort order may be null if never previously sorted, so give it the current index
            if (a.getSortOrder() == null || !a.getSortOrder().equals(i)) {
                a.setSortOrder(i);
                assignmentsToUpdate.add(a);
            }

            i++;
        }

        // do the updates
        assignmentsToUpdate.forEach(this::updateAssignment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GradingEvent> getGradingEvents(final String studentId, final long assignmentId) {

        log.debug("getGradingEvents called for studentId: {}", studentId);

        if (studentId == null) {
            log.debug("No student id was specified.  Returning an empty GradingEvents list");
            return Collections.<GradingEvent>emptyList();
        }

        return gradingPersistenceManager.getGradingEventsForAssignment(assignmentId, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryScoreData> calculateCategoryScore(final Object gradebook, final String studentUuid,
            final CategoryDefinition category, final List<Assignment> categoryAssignments,
            final Map<Long, String> gradeMap, final boolean includeNonReleasedItems) {

        final Gradebook gb = (Gradebook) gradebook;

        // used for translating letter grades
        final Map<String, Double> gradingSchema = gb.getSelectedGradeMapping().getGradeMap();

        // collect the data and turn it into a list of AssignmentGradeRecords
        // this is the info that is compatible with both applyDropScores and the calculateCategoryScore method
        final List<AssignmentGradeRecord> gradeRecords = new ArrayList<>();
        for (final Assignment assignment : categoryAssignments) {

            final Long assignmentId = assignment.getId();

            final String rawGrade = gradeMap.get(assignmentId);
            final Double pointsPossible = assignment.getPoints();
            Double grade;

            // determine the grade we should be using depending on the grading type
            if (gb.getGradeType() == GradeType.PERCENTAGE) {
                grade = calculateEquivalentPointValueForPercent(pointsPossible, NumberUtils.createDouble(rawGrade));
            } else if (gb.getGradeType() == GradeType.LETTER) {
                grade = gradingSchema.get(rawGrade);
            } else {
                grade = NumberUtils.createDouble(rawGrade);
            }

            // recreate the category (required fields only)
            final Category c = new Category();
            c.setId(category.getId());
            c.setDropHighest(category.getDropHighest());
            c.setDropLowest(category.getDropLowest());
            c.setKeepHighest(category.getKeepHighest());
            c.setEqualWeightAssignments(category.getEqualWeight());

            // recreate the assignment (required fields only)
            final GradebookAssignment a = new GradebookAssignment();
            a.setPointsPossible(assignment.getPoints());
            a.setUngraded(assignment.getUngraded());
            a.setCounted(assignment.getCounted());
            a.setExtraCredit(assignment.getExtraCredit());
            a.setReleased(assignment.getReleased());
            a.setRemoved(false); // shared.GradebookAssignment doesn't include removed so this will always be false
            a.setGradebook(gb);
            a.setCategory(c);
            a.setId(assignment.getId()); // store the id so we can find out later which grades were dropped, if any

            // create the AGR
            final AssignmentGradeRecord gradeRecord = new AssignmentGradeRecord(a, studentUuid, grade);

            if (!a.getNotCounted()) {
                gradeRecords.add(gradeRecord);
            }
        }

        return calculateCategoryScore(studentUuid, category.getId(), gradeRecords, includeNonReleasedItems, gb.getCategoryType(), category.getEqualWeight());
    }


    @Override
    public Optional<CategoryScoreData> calculateCategoryScore(Long gradebookId, String studentUuid, Long categoryId,
          boolean includeNonReleasedItems, GradingCategoryType categoryType, Boolean equalWeightAssignments) {

        // get all grade records for the student
        Map<String, List<AssignmentGradeRecord>> gradeRecMap = getGradeRecordMapForStudents(gradebookId, Collections.singletonList(studentUuid));

        // apply the settings
        List<AssignmentGradeRecord> gradeRecords = gradeRecMap.get(studentUuid);

        return calculateCategoryScore(studentUuid, categoryId, gradeRecords, includeNonReleasedItems, categoryType, equalWeightAssignments);
    }

    /**
     * Does the heavy lifting for the category calculations. Requires the List of AssignmentGradeRecord so that we can applyDropScores.
     *
     * @param studentUuid the student uuid
     * @param categoryId the category id we are interested in
     * @param gradeRecords all grade records for the student
     * @return
     */
    private Optional<CategoryScoreData> calculateCategoryScore(final String studentUuid, final Long categoryId,
            final List<AssignmentGradeRecord> gradeRecords, final boolean includeNonReleasedItems, final GradingCategoryType categoryType, Boolean equalWeightAssignments) {

        // validate
        if (gradeRecords == null) {
            log.debug("No grade records for student: {}. Nothing to do.", studentUuid);
            return Optional.empty();
        }

        if (categoryId == null) {
            log.debug("No category supplied, nothing to do.");
            return Optional.empty();
        }

        // setup
        int numScored = 0;
        int numOfAssignments = 0;
        BigDecimal totalEarned = new BigDecimal("0");
        BigDecimal totalEarnedMean = new BigDecimal("0");
        BigDecimal totalPossible = new BigDecimal("0");

        // apply any drop/keep settings for this category
        applyDropScores(gradeRecords, categoryType);

        // find the records marked as dropped (highest/lowest) before continuing,
        // as gradeRecords will be modified in place after this and these records will be removed
        final List<Long> droppedItemIds = gradeRecords.stream()
                .filter(AssignmentGradeRecord::getDroppedFromGrade)
                .map(agr -> agr.getAssignment().getId())
                .collect(Collectors.toList());

        // Since all gradeRecords for the student are passed in, not just for this category,
        // plus they may not meet the criteria for including in the calculation,
        // this list is filtered down according to the following rules:
        // Rule 1. remove gradeRecords that don't match the given category
        // Rule 2. the assignment must have points to be assigned
        // Rule 3. there is a non blank grade for the student
        // Rule 4. the assignment is included in course grade calculations
        // Rule 5. the assignment is released to the student (instructor gets to see category grade regardless of release status; student does not)
        // Rule 6. the grade is not dropped from the calc
        // Rule 7. extra credit items have their grade value counted only. Their total points possible does not apply to the calculations
        log.debug("categoryId: {}", categoryId);

        gradeRecords.removeIf(gradeRecord -> {

            final GradebookAssignment assignment = gradeRecord.getAssignment();

            // remove if not for this category (rule 1)
            if (assignment.getCategory() == null) {
                return true;
            }
            if (categoryId.longValue() != assignment.getCategory().getId().longValue()) {
                return true;
            }

            final boolean excluded = BooleanUtils.toBoolean(gradeRecord.getExcludedFromGrade());
            // remove if the assignment/graderecord doesn't meet the criteria for the calculation (rule 2-6)
            if (excluded || assignment.getPointsPossible() == null || gradeRecord.getPointsEarned() == null || !assignment.getCounted()
                    || (!assignment.getReleased() && !includeNonReleasedItems) || gradeRecord.getDroppedFromGrade()) {
                return true;
            }

            return false;
        });

        log.debug("gradeRecords.size(): {}", gradeRecords.size());

        // pre-calculation
        // Rule 1. If category only has a single EC item, don't try to calculate category total.
        if (gradeRecords.size() == 1 && gradeRecords.get(0).getAssignment().getExtraCredit()) {
            return Optional.empty();
        }

        // iterate the filtered list and set the variables for the calculation
        for (AssignmentGradeRecord gradeRecord : gradeRecords) {

            GradebookAssignment assignment = gradeRecord.getAssignment();
            BigDecimal possiblePoints = new BigDecimal(assignment.getPointsPossible().toString());

            // EC item, don't count points possible
            if (!assignment.getExtraCredit()) {
                totalPossible = totalPossible.add(possiblePoints);
                numOfAssignments++;
                numScored++;
            }

            // sanitise grade, null values to "0";
            String gradeString = (gradeRecord.getPointsEarned() != null) ? String.valueOf(gradeRecord.getPointsEarned()) : "0";
            BigDecimal grade = new BigDecimal(gradeString);

            // update total points earned
            totalEarned = totalEarned.add(grade);

            // keep running total of averages in case the category is equal weighted
            try {
                totalEarnedMean
                    = totalEarnedMean.add(grade.divide(possiblePoints, GradingService.MATH_CONTEXT));
            } catch (ArithmeticException ae) {
                   totalEarnedMean = totalEarnedMean.add(new BigDecimal("0"));
            }
        }

        if (numScored == 0 || numOfAssignments == 0 || totalPossible.doubleValue() == 0) {
            return Optional.empty();
        }

        BigDecimal mean = totalEarned.divide(new BigDecimal(numScored), GradingService.MATH_CONTEXT)
                .divide((totalPossible.divide(new BigDecimal(numOfAssignments), GradingService.MATH_CONTEXT)),
                        GradingService.MATH_CONTEXT)
                .multiply(new BigDecimal("100"));

        if (equalWeightAssignments == null) {
            Category category = getCategory(categoryId);
            equalWeightAssignments = category.getEqualWeightAssignments();
        }
        if (equalWeightAssignments) {
            mean = totalEarnedMean.divide(new BigDecimal(numScored), GradingService.MATH_CONTEXT).multiply(new BigDecimal("100"));
        }

        return Optional.of(new CategoryScoreData(mean.doubleValue(), droppedItemIds));
    }

    @Override
    public CourseGradeTransferBean getCourseGradeForStudent(String gradebookUid, String userUuid) {
        return this.getCourseGradeForStudents(gradebookUid, Collections.singletonList(userUuid)).get(userUuid);
    }

    @Override
    public Map<String, CourseGradeTransferBean> getCourseGradeForStudents(String gradebookUid, List<String> userUuids) {

        try {
            GradeMapping gradeMap = getGradebook(gradebookUid).getSelectedGradeMapping();
            return getCourseGradeForStudents(gradebookUid, userUuids, gradeMap.getGradeMap());
        } catch (Exception e) {
            log.error("Error in getCourseGradeForStudents : {}", e.toString());
            return Collections.<String, CourseGradeTransferBean>emptyMap();
        }
    }

    @Override
    @Transactional
    public Map<String, CourseGradeTransferBean> getCourseGradeForStudents(final String gradebookUid,
            final List<String> userUuids, final Map<String, Double> gradeMap) {

        final Map<String, CourseGradeTransferBean> rval = new HashMap<>();

        try {
            final Gradebook gradebook = getGradebook(gradebookUid);

            // if not released, and not instructor or TA, don't do any work
            // note that this will return a course grade for Instructor and TA even if not released, see SAK-30119
            if (!gradebook.getCourseGradeDisplayed() && !(currentUserHasEditPerm(gradebookUid) || currentUserHasGradingPerm(gradebookUid))) {
                return rval;
            }

            final List<GradebookAssignment> assignments = getAssignmentsCounted(gradebook.getId());

            // this takes care of drop/keep scores
            final List<CourseGradeRecord> gradeRecords = getPointsEarnedCourseGradeRecords(getCourseGrade(gradebook.getId()), userUuids);

            // gradeMap MUST be sorted for the grade mapping to apply correctly
            final Map<String, Double> sortedGradeMap = GradeMappingDefinition.sortGradeMapping(gradeMap);

            gradeRecords.forEach(gr -> {

                final CourseGradeTransferBean cg = new CourseGradeTransferBean();

                // ID of the course grade item
                cg.setId(gr.getCourseGrade().getId());

                // set entered grade
                cg.setEnteredGrade(gr.getEnteredGrade());

                // set date recorded
                cg.setDateRecorded(gr.getDateRecorded());

                // set entered points
                cg.setEnteredPoints(gr.getEnteredPoints());

                if (!assignments.isEmpty()) {

                    boolean showCalculatedGrade = serverConfigurationService.getBoolean("gradebook.coursegrade.showCalculatedGrade", true);

                    // calculated grade
                    // may be null if no grade entries to calculate
                    Double calculatedGrade = showCalculatedGrade == true ? gr.getAutoCalculatedGrade() : gr.getEnteredPoints();

                    if (calculatedGrade == null) {
                        calculatedGrade = gr.getAutoCalculatedGrade();
                    }

                    if (calculatedGrade != null) {
                        cg.setCalculatedGrade(calculatedGrade.toString());

                        // SAK-33997 Adjust the rounding of the calculated grade so we get the appropriate
                        // grade mapping
                        BigDecimal bd = new BigDecimal(calculatedGrade)
                                .setScale(10, RoundingMode.HALF_UP)
                                .setScale(2, RoundingMode.HALF_UP);
                        calculatedGrade = bd.doubleValue();
                    }

                    // mapped grade
                    final String mappedGrade = GradeMapping.getMappedGrade(sortedGradeMap, calculatedGrade);
                    log.debug("calculatedGrade: {} -> mappedGrade: {}", calculatedGrade, mappedGrade);
                    cg.setMappedGrade(mappedGrade);

                    // points
                    cg.setPointsEarned(gr.getPointsEarned()); // synonymous with gradeRecord.getCalculatedPointsEarned()
                    cg.setTotalPointsPossible(gr.getTotalPointsPossible());
                }
                rval.put(gr.getStudentId(), cg);
            });
        } catch (final Exception e) {
            log.error("Error in getCourseGradeForStudents: {}", e.toString());
        }
        return rval;
    }

    @Override
    public List<CourseSection> getViewableSections(final String gradebookUid) {

        return gradingAuthz.getViewableSections(gradebookUid);
    }

    @Override
    public void updateGradebookSettings(final String gradebookUid, final GradebookInformation gbInfo) {

        if (gradebookUid == null) {
            throw new IllegalArgumentException("null gradebookUid " + gradebookUid);
        }

        // must be instructor type person
        if (!currentUserHasEditPerm(gradebookUid)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to edit gb information", getUserUid(), gradebookUid);
            throw new GradingSecurityException("You do not have permission to edit gradebook information in site " + gradebookUid);
        }

        final Gradebook gradebook = getGradebook(gradebookUid);
        if (gradebook == null) {
            throw new IllegalArgumentException("There is no gradebook associated with this id: " + gradebookUid);
        }

        final Map<String, Double> bottomPercents = gbInfo.getSelectedGradingScaleBottomPercents();

        // Before we do any work, check if any existing course grade overrides might be left in an unmappable state
        final List<CourseGradeRecord> courseGradeOverrides = gradingPersistenceManager.getCourseGradeOverrides(gradebook);
        courseGradeOverrides.forEach(cgr -> {

            if (!bottomPercents.containsKey(cgr.getEnteredGrade())) {
                throw new UnmappableCourseGradeOverrideException(
                        "The grading schema could not be updated as it would leave some course grade overrides in an unmappable state.");
            }
        });

        // iterate all available grademappings for this gradebook and set the one that we have the ID and bottomPercents for
        final Set<GradeMapping> gradeMappings = gradebook.getGradeMappings();
        gradeMappings.forEach(gradeMapping -> {

            if (StringUtils.equals(Long.toString(gradeMapping.getId()), gbInfo.getSelectedGradeMappingId())) {
                gradebook.setSelectedGradeMapping(gradeMapping);

                // update the map values
                updateGradeMapping(gradeMapping.getId(), bottomPercents);
            }
        });

        // set grade type, but only if sakai.property is true OR user is admin
        final boolean gradeTypeAvailForNonAdmins = serverConfigurationService.getBoolean("gradebook.settings.gradeEntry.showToNonAdmins", true);
        if (gradeTypeAvailForNonAdmins || securityService.isSuperUser()) {
            gradebook.setGradeType(gbInfo.getGradeType());
        }

        // set category type
        gradebook.setCategoryType(gbInfo.getCategoryType());

        // set display release items to students
        gradebook.setAssignmentsDisplayed(gbInfo.getDisplayReleasedGradeItemsToStudents());

        // set course grade display settings
        gradebook.setCourseGradeDisplayed(gbInfo.getCourseGradeDisplayed());
        gradebook.setCourseLetterGradeDisplayed(gbInfo.getCourseLetterGradeDisplayed());
        gradebook.setCoursePointsDisplayed(gbInfo.getCoursePointsDisplayed());
        gradebook.setCourseAverageDisplayed(gbInfo.getCourseAverageDisplayed());

        // set stats display settings
        gradebook.setAssignmentStatsDisplayed(gbInfo.getAssignmentStatsDisplayed());
        gradebook.setCourseGradeStatsDisplayed(gbInfo.getCourseGradeStatsDisplayed());

        // set allow students to compare grades
        gradebook.setAllowStudentsToCompareGrades(gbInfo.getAllowStudentsToCompareGrades());
        gradebook.setComparingDisplayStudentNames(gbInfo.getComparingDisplayStudentNames());
        gradebook.setComparingDisplayStudentSurnames(gbInfo.getComparingDisplayStudentSurnames());
        gradebook.setComparingDisplayTeacherComments(gbInfo.getComparingDisplayTeacherComments());
        gradebook.setComparingIncludeAllGrades(gbInfo.getComparingIncludeAllGrades());
        gradebook.setComparingRandomizeDisplayedData(gbInfo.getComparingRandomizeDisplayedData());

        final List<CategoryDefinition> newCategoryDefinitions = gbInfo.getCategories();

        // if we have categories and they are weighted, check the weightings sum up to 100% (or 1 since it's a fraction)
        if (gradebook.getCategoryType() == GradingCategoryType.WEIGHTED_CATEGORY) {
            double totalWeight = 0;
            for (CategoryDefinition newDef : newCategoryDefinitions) {

                if (newDef.getWeight() == null) {
                    throw new IllegalArgumentException("No weight specified for a category, but weightings enabled");
                }

                totalWeight += newDef.getWeight();
            }
            if (Math.rint(totalWeight) != 1) {
                throw new IllegalArgumentException("Weightings for the categories do not equal 100%");
            }
        }

        // get current categories and build a mapping list of Category.id to Category
        final List<Category> currentCategories = getCategories(gradebook.getId());
        final Map<Long, Category> currentCategoryMap
            = currentCategories.stream().collect(Collectors.toMap(c -> c.getId(), c -> c));

        // compare current list with given list, add/update/remove as required
        // Rules:
        // If category does not have an ID it is new; add these later after all removals have been processed
        // If category has an ID it is to be updated. Update and remove from currentCategoryMap.
        // Any categories remaining in currentCategoryMap are to be removed.
        // Sort by category order as we resequence the order values to avoid gaps
        Collections.sort(newCategoryDefinitions, CategoryDefinition.orderComparator);
        final Map<CategoryDefinition, Integer> newCategories = new HashMap<>();
        int categoryIndex = 0;
        for (final CategoryDefinition newDef : newCategoryDefinitions) {

            // preprocessing and validation
            // Rule 1: If category has no name, it is to be removed/skipped
            // Note that we no longer set weights to 0 even if unweighted category type selected. The weights are not considered if its not
            // a weighted category type
            // so this allows us to switch back and forth between types without losing information

            if (StringUtils.isBlank(newDef.getName())) {
                continue;
            }

            if (newDef.getId() == null) {
                // new
                newCategories.put(newDef, categoryIndex);
                categoryIndex++;
            } else {
                // existing
                final Category existing = currentCategoryMap.get(newDef.getId());
                existing.setName(newDef.getName());
                existing.setWeight(newDef.getWeight());
                existing.setDropLowest(newDef.getDropLowest());
                existing.setDropHighest(newDef.getDropHighest());
                existing.setKeepHighest(newDef.getKeepHighest());
                existing.setExtraCredit(newDef.getExtraCredit());
                existing.setEqualWeightAssignments(newDef.getEqualWeight());
                existing.setCategoryOrder(categoryIndex);
                updateCategory(existing);

                // remove from currentCategoryMap so we know not to delete it
                currentCategoryMap.remove(newDef.getId());

                categoryIndex++;
            }

        }

        // handle deletes
        // anything left in currentCategoryMap was not included in the new list, delete them
        currentCategoryMap.keySet().forEach(this::removeCategory);

        // Handle the additions
        for (final Entry<CategoryDefinition, Integer> entry : newCategories.entrySet()) {
            final CategoryDefinition newCat = entry.getKey();
            this.createCategory(gradebook.getId(), newCat.getName(), newCat.getWeight(), newCat.getDropLowest(),
                    newCat.getDropHighest(), newCat.getKeepHighest(), newCat.getExtraCredit(), newCat.getEqualWeight(), entry.getValue());
        }

        // if weighted categories, all uncategorised assignments are to be removed from course grade calcs
        if (gradebook.getCategoryType() == GradingCategoryType.WEIGHTED_CATEGORY) {
            excludeUncategorisedItemsFromCourseGradeCalculations(gradebook);
        }

        // persist
        updateGradebook(gradebook);

    }

    @Override
    public Set<GradeMapping> getGradebookGradeMappings(Long gradebookId) {

        Optional<Gradebook> optGradebook = gradingPersistenceManager.getGradebook(gradebookId);

        if (optGradebook.isPresent()) {
            return optGradebook.get().getGradeMappings();
        } else {
            log.warn("No gradebook for id {}", gradebookId);
            return null;
        }
    }

    @Override
    public Set<GradeMapping> getGradebookGradeMappings(String gradebookUid) {

        final Long gradebookId = getGradebook(gradebookUid).getId();
        return this.getGradebookGradeMappings(gradebookId);
    }

    @Override
    public void updateCourseGradeForStudent(final String gradebookUid, final String studentUuid, final String grade, final String gradeScale) {

        // must be instructor type person
        if (!currentUserHasEditPerm(gradebookUid)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to update course grade for student: {}", getUserUid(),
                    gradebookUid, studentUuid);
            throw new GradingSecurityException("You do not have permission to update course grades in " + gradebookUid);
        }

        final Gradebook gradebook = getGradebook(gradebookUid);
        if (gradebook == null) {
            throw new IllegalArgumentException("There is no gradebook associated with this id: " + gradebookUid);
        }

        // get course grade for the student
        CourseGradeRecord courseGradeRecord = getCourseGradeRecord(gradebook, studentUuid);

        // if user doesn't have an entered course grade, we need to find the course grade and create a record
        if (courseGradeRecord == null) {

            final CourseGrade courseGrade = getCourseGrade(gradebook.getId());

            courseGradeRecord = new CourseGradeRecord(courseGrade, studentUuid);
            courseGradeRecord.setGraderId(getUserUid());

        } else {
            // if passed in grade override is same as existing grade override, nothing to do
            if (StringUtils.equals(courseGradeRecord.getEnteredGrade(), gradeScale) && Double.compare(courseGradeRecord.getEnteredPoints(), Double.parseDouble(grade)) == 0) {
                return;
            }
        }

        // set the grade override
        courseGradeRecord.setEnteredGrade(gradeScale);
        if (grade == null) {
            courseGradeRecord.setEnteredPoints(null);
        } else {
            courseGradeRecord.setEnteredPoints(Double.parseDouble(grade));
        }
        // record the last grade override date
        courseGradeRecord.setDateRecorded(new Date());

        // create a grading event
        final GradingEvent gradingEvent = new GradingEvent(courseGradeRecord.getCourseGrade(), getUserUid(), studentUuid, courseGradeRecord.getEnteredGrade());

        gradingPersistenceManager.saveCourseGradeRecord(courseGradeRecord);
        gradingPersistenceManager.saveGradingEvent(gradingEvent);
    }

    /**
     * Map a set of GradeMapping to a list of GradeMappingDefinition
     *
     * @param gradeMappings set of GradeMapping
     * @return list of GradeMappingDefinition
     */
    private List<GradeMappingDefinition> getGradebookGradeMappings(final Set<GradeMapping> gradeMappings) {
        final List<GradeMappingDefinition> rval = new ArrayList<>();

        for (final GradeMapping mapping : gradeMappings) {
            rval.add(new GradeMappingDefinition(mapping.getId(), mapping.getName(),
                    GradeMappingDefinition.sortGradeMapping(mapping.getGradeMap()),
                    GradeMappingDefinition.sortGradeMapping(mapping.getDefaultBottomPercents())));
        }
        return rval;

    }

    /**
     * Updates the categorized order of an assignment
     *
     * @see GradingService.updateAssignmentCategorizedOrder(java.lang.String gradebookUid, java.lang.Long assignmentId, java.lang.Integer
     *      order)
     */
    @Override
    public void updateAssignmentCategorizedOrder(final String gradebookUid, final Long categoryId, final Long assignmentId, Integer order) {

        if (!gradingAuthz.isUserAbleToEditAssessments(gradebookUid)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to change the order of assignment {}", getUserUid(),
                    gradebookUid, assignmentId);
            throw new GradingSecurityException();
        }

        if (order == null) {
            throw new IllegalArgumentException("Categorized Order cannot be null");
        }

        final Long gradebookId = getGradebook(gradebookUid).getId();

        // get all assignments for this gradebook
        final List<GradebookAssignment> assignments = getAssignments(gradebookId, SortType.SORT_BY_CATEGORY, true);
        final List<GradebookAssignment> assignmentsInNewCategory = new ArrayList<>();
        for (final GradebookAssignment assignment : assignments) {
            if (assignment.getCategory() == null) {
                if (categoryId == null) {
                    assignmentsInNewCategory.add(assignment);
                }
            } else if (assignment.getCategory().getId().equals(categoryId)) {
                assignmentsInNewCategory.add(assignment);
            }
        }

        // adjust order to be within bounds
        if (order < 0) {
            order = 0;
        } else if (order > assignmentsInNewCategory.size()) {
            order = assignmentsInNewCategory.size();
        }

        // find the assignment
        GradebookAssignment target = null;
        for (final GradebookAssignment a : assignmentsInNewCategory) {
            if (a.getId().equals(assignmentId)) {
                target = a;
                break;
            }
        }

        // add the assignment to the list via a 'pad, remove, add' approach
        assignmentsInNewCategory.add(null); // ensure size remains the same for the remove
        assignmentsInNewCategory.remove(target); // remove item
        assignmentsInNewCategory.add(order, target); // add at ordered position, will shuffle others along

        // the assignments are now in the correct order within the list, we just need to update the sort order for each one
        // create a new list for the assignments we need to update in the database
        final List<GradebookAssignment> assignmentsToUpdate = new ArrayList<>();

        int i = 0;
        for (final GradebookAssignment a : assignmentsInNewCategory) {

            // skip if null
            if (a == null) {
                continue;
            }

            // if the sort order is not the same as the counter, update the order and add to the other list
            // this allows us to skip items that have not had their position changed and saves some db work later on
            // sort order may be null if never previously sorted, so give it the current index
            if (a.getCategorizedSortOrder() == null || !a.getCategorizedSortOrder().equals(i)) {
                a.setCategorizedSortOrder(i);
                assignmentsToUpdate.add(a);
            }

            i++;
        }

        assignmentsToUpdate.forEach(this::updateAssignment);

    }

    /**
     * Return the grade changes made since a given time
     *
     * @param assignmentIds ids of assignments to check
     * @param since timestamp from which to check for changes
     * @return set of changes made
     */
    @Override
    public List<GradingEvent> getGradingEvents(List<Long> assignmentIds, Date since) {

        if (assignmentIds == null || assignmentIds.isEmpty() || since == null) {
            return Collections.<GradingEvent>emptyList();
        }

        return gradingPersistenceManager.getGradingEventsForAssignmentsSince(assignmentIds, since);
    }

    /**
     * Update the persistent grade points for an assignment when the total points is changed.
     *
     * @param gradebook the gradebook
     * @param assignment assignment with original total point value
     */
    private void scaleGrades(final Gradebook gradebook, final GradebookAssignment assignment,
            final Double originalPointsPossible) {

        if (gradebook == null || assignment == null || assignment.getPointsPossible() == null) {
            throw new IllegalArgumentException("null values found in convertGradePointsForUpdatedTotalPoints.");
        }

        final List<String> studentUids = getStudentsForGradebook(gradebook);
        final List<AssignmentGradeRecord> gradeRecords = getAllAssignmentGradeRecordsForGbItem(assignment.getId(), studentUids);
        final Set<GradingEvent> eventsToAdd = new HashSet<>();
        final String currentUserUid = sessionManager.getCurrentSessionUserId();

        // scale for total points changed when on percentage grading
        if (gradebook.getGradeType() == GradeType.PERCENTAGE && assignment.getPointsPossible() != null) {

            log.debug("Scaling percentage grades");

            for (final AssignmentGradeRecord gr : gradeRecords) {
                if (gr.getPointsEarned() != null) {
                    final BigDecimal scoreAsPercentage = (new BigDecimal(gr.getPointsEarned())
                            .divide(new BigDecimal(originalPointsPossible), GradingService.MATH_CONTEXT))
                                    .multiply(new BigDecimal(100));

                    final BigDecimal scaledScore = new BigDecimal(calculateEquivalentPointValueForPercent(assignment.getPointsPossible(),
                            scoreAsPercentage.doubleValue()), GradingService.MATH_CONTEXT).setScale(2, RoundingMode.HALF_UP);

                    log.debug("scoreAsPercentage: {}, scaledScore: {}", scoreAsPercentage, scaledScore);

                    gr.setPointsEarned(scaledScore.doubleValue());
                    eventsToAdd.add(new GradingEvent(assignment, currentUserUid, gr.getStudentId(), scaledScore));
                }
            }
        }
        else if (gradebook.getGradeType() == GradeType.POINTS && assignment.getPointsPossible() != null) {

            log.debug("Scaling point grades");

            final BigDecimal previous = new BigDecimal(originalPointsPossible);
            final BigDecimal current = new BigDecimal(assignment.getPointsPossible());
            final BigDecimal factor = current.divide(previous, GradingService.MATH_CONTEXT);

            log.debug("previous points possible: {}, current points possible: {}, factor: {}", previous, current, factor);

            for (final AssignmentGradeRecord gr : gradeRecords) {
                if (gr.getPointsEarned() != null) {

                    final BigDecimal currentGrade = new BigDecimal(gr.getPointsEarned(), GradingService.MATH_CONTEXT);
                    final BigDecimal scaledGrade = currentGrade.multiply(factor, GradingService.MATH_CONTEXT).setScale(2, RoundingMode.HALF_UP);

                    log.debug("currentGrade: {}, scaledGrade: {}", currentGrade, scaledGrade);

                    gr.setPointsEarned(scaledGrade.doubleValue());
                    DecimalFormat df = (DecimalFormat)NumberFormat.getNumberInstance((resourceLoader).getLocale());
                    df.setGroupingUsed(false);
                    String pointsLocale = df.format(scaledGrade);
                    eventsToAdd.add(new GradingEvent(assignment, currentUserUid, gr.getStudentId(), pointsLocale));
                }
            }
        }

        // save all
        gradeRecords.forEach(gradingPersistenceManager::saveAssignmentGradeRecord);

        // Insert the new grading events (GradeRecord)
        eventsToAdd.forEach(gradingPersistenceManager::saveGradingEvent);
    }

    /**
     * Get the list of students for the given gradebook
     *
     * @param gradebook the gradebook for the site
     * @return a list of uuids for the students
     */
    private List<String> getStudentsForGradebook(Gradebook gradebook) {

        final List<EnrollmentRecord> enrolments = sectionAwareness.getSiteMembersInRole(gradebook.getUid(), Role.STUDENT);

        final List<String> rval = enrolments.stream()
                .map(EnrollmentRecord::getUser)
                .map(User::getUserUid)
                .collect(Collectors.toList());

        return rval;
    }

    private boolean isCurrentUserFromGroup(final String gradebookUid, final String studentId) {

        boolean isFromGroup = false;
        try {
            final Site s = this.siteService.getSite(gradebookUid);
            final Group g = s.getGroup(studentId);
            isFromGroup = (g != null) && (g.getMember(sessionManager.getCurrentSessionUserId()) != null);
        } catch (final Exception e) {
            // Id not found
            log.error("Error in isCurrentUserFromGroup: ", e);
        }
        return isFromGroup;
    }

    /**
     * Updates all uncategorised items to exclude them from the course grade calcs
     *
     * @param gradebook
     */
    private void excludeUncategorisedItemsFromCourseGradeCalculations(final Gradebook gradebook) {
        final List<GradebookAssignment> allAssignments = getAssignments(gradebook.getId());

        final List<GradebookAssignment> assignments = allAssignments.stream().filter(a -> a.getCategory() == null)
                .collect(Collectors.toList());
        assignments.forEach(a -> {
            a.setCounted(false);
            gradingPersistenceManager.saveAssignment(a);
        });
    }


    private ConcurrentHashMap<String, ExternalAssignmentProvider> externalProviders = new ConcurrentHashMap<String, ExternalAssignmentProvider>();

    // Mapping of providers to their getAllExternalAssignments(String gradebookUid) methods,
    // used to allow the method to be called on providers not declaring the Compat interface.
    // This is to allow the same code to be used on 2.9 and beyond, where the secondary interface
    // may be removed, without build profiles.
    private final ConcurrentHashMap<ExternalAssignmentProvider, Method> providerMethods = new ConcurrentHashMap<ExternalAssignmentProvider, Method>();

    /**
     * Property in sakai.properties used to allow this service to update scores in the db every time the update method is called. By
     * default, scores are only updated if the score is different than what is currently in the db.
     */
    public static final String UPDATE_SAME_SCORE_PROP = "gradebook.externalAssessments.updateSameScore";
    public static final boolean UPDATE_SAME_SCORE_PROP_DEFAULT = false;

    public ConcurrentMap<String, ExternalAssignmentProvider> getExternalAssignmentProviders() {
        if (this.externalProviders == null) {
            this.externalProviders = new ConcurrentHashMap<>(0);
        }
        return this.externalProviders;
    }

    @Override
    public void registerExternalAssignmentProvider(final ExternalAssignmentProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("provider cannot be null");
        } else {
            getExternalAssignmentProviders().put(provider.getAppKey(), provider);

            // Try to duck-type the provider so it doesn't have to declare the Compat interface.
            // TODO: Remove this handling once the Compat interface has been merged or the issue is otherwise resolved.
            if (!(provider instanceof ExternalAssignmentProviderCompat)) {
                try {
                    final Method m = provider.getClass().getDeclaredMethod("getAllExternalAssignments", String.class);
                    if (m.getReturnType().equals(List.class)) {
                        this.providerMethods.put(provider, m);
                    }
                } catch (final Exception e) {
                    log.warn("ExternalAssignmentProvider [" + provider.getAppKey() + " / " + provider.getClass().toString()
                            + "] does not implement getAllExternalAssignments. It will not be able to exclude items from student views/grades. "
                            + "See the ExternalAssignmentProviderCompat interface and SAK-23733 for details.");
                }
            }
        }
    }

    @Override
    public void unregisterExternalAssignmentProvider(final String providerAppKey) {
        if (providerAppKey == null || "".equals(providerAppKey)) {
            throw new IllegalArgumentException("providerAppKey must be set");
        } else if (getExternalAssignmentProviders().containsKey(providerAppKey)) {
            final ExternalAssignmentProvider provider = getExternalAssignmentProviders().get(providerAppKey);
            this.providerMethods.remove(provider);
            getExternalAssignmentProviders().remove(providerAppKey);
        }
    }

    public void init() {
        log.debug("INIT");
    }

    public void destroy() {
        log.debug("DESTROY");
        if (this.externalProviders != null) {
            this.externalProviders.clear();
            this.externalProviders = null;
        }
    }

    @Override
    public void addExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl,
            final String title, final double points, final Date dueDate, final String externalServiceDescription, String externalData)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException {

        // Ensure that the required strings are not empty
        if (StringUtils.trimToNull(externalServiceDescription) == null ||
                StringUtils.trimToNull(externalId) == null ||
                StringUtils.trimToNull(title) == null) {
            throw new RuntimeException("External service description, externalId, and title must not be empty");
        }

        // Ensure that points is > zero
        if (points <= 0) {
            throw new AssignmentHasIllegalPointsException("Points must be > 0");
        }

        // Ensure that the assessment name is unique within this gradebook
        if (isAssignmentDefined(gradebookUid, title)) {
            throw new ConflictingAssignmentNameException("An assignment with that name already exists in gradebook uid=" + gradebookUid);
        }


        // name cannot contain these chars as they are reserved for special columns in import/export
        GradebookHelper.validateGradeItemName(title);

        // Ensure that the externalId is unique within this gradebook
        final Long conflicts = gradingPersistenceManager.countAssignmentsByGradbookAndExternalId(gradebookUid, externalId);

        if (conflicts.intValue() > 0) {
            throw new ConflictingExternalIdException(
                    "An external assessment with ID=" + externalId + " already exists in gradebook uid=" + gradebookUid);
        }

        // Get the gradebook
        final Gradebook gradebook = getGradebook(gradebookUid);

        // Create the external assignment
        final GradebookAssignment asn = new GradebookAssignment(gradebook, title, Double.valueOf(points), dueDate);
        asn.setExternallyMaintained(true);
        asn.setExternalId(externalId);
        asn.setExternalInstructorLink(externalUrl);
        asn.setExternalStudentLink(externalUrl);
        asn.setExternalAppName(externalServiceDescription);
        asn.setExternalData(externalData);
        // set released to be true to support selective release
        asn.setReleased(true);
        asn.setUngraded(false);

        gradingPersistenceManager.saveAssignment(asn);

        log.info("External assessment added to gradebookUid={}, externalId={} by userUid={} from externalApp={}", gradebookUid, externalId,
                getUserUid(), externalServiceDescription);
    }

    @Override
    public void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl,
                                         String externalData, String title, double points, Date dueDate)
            throws AssessmentNotFoundException, AssignmentHasIllegalPointsException {

        final Optional<GradebookAssignment> optAsn = getDbExternalAssignment(gradebookUid, externalId);

        if (optAsn.isEmpty()) {
            throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
        }

        GradebookAssignment asn = optAsn.get();

        // Ensure that points is > zero
        if (points <= 0) {
            throw new AssignmentHasIllegalPointsException("Points must be > 0");
        }

        // Ensure that the required strings are not empty
        if (StringUtils.trimToNull(externalId) == null ||
                StringUtils.trimToNull(title) == null) {
            throw new RuntimeException("ExternalId, and title must not be empty");
        }

        // name cannot contain these chars as they are reserved for special columns in import/export
        GradebookHelper.validateGradeItemName(title);

        asn.setExternalInstructorLink(externalUrl);
        asn.setExternalStudentLink(externalUrl);
        asn.setExternalData(externalData);
        asn.setName(title);
        asn.setDueDate(dueDate);
        // support selective release
        asn.setReleased(BooleanUtils.isTrue(asn.getReleased()));
        asn.setPointsPossible(Double.valueOf(points));
        gradingPersistenceManager.saveAssignment(asn);
        log.info("External assessment updated in gradebookUid={}, externalId={} by userUid={}", gradebookUid, externalId,
                getUserUid());
    }

    @Override
    @Transactional
    public void removeExternalAssignment(String gradebookUid, String externalId) throws AssessmentNotFoundException {

        // Get the external assignment
        final Optional<GradebookAssignment> optAsn = getDbExternalAssignment(gradebookUid, externalId);
        if (optAsn.isEmpty()) {
            throw new AssessmentNotFoundException("There is no external assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
        }

        GradebookAssignment asn = optAsn.get();

        int numDeleted = gradingPersistenceManager.deleteGradingEventsForAssignment(asn);
        log.debug("Deleted {} records from gb_grading_event_t", numDeleted);

        numDeleted = gradingPersistenceManager.deleteGradeRecordsForAssignment(asn);
        log.info("Deleted {} externally defined scores", numDeleted);

        numDeleted = gradingPersistenceManager.deleteCommentsForAssignment(asn);
        log.info("Deleted {} externally defined comments", numDeleted);

        // Delete the assessment.
        gradingPersistenceManager.deleteAssignment(asn);

        log.info("External assessment removed from gradebookUid={}, externalId={} by userUid={}", gradebookUid, externalId, getUserUid());
    }

    private Optional<GradebookAssignment> getDbExternalAssignment(String gradebookUid, String externalId) {

        if (externalId == null) {
            log.warn("null externalId supplied to getDbExternalAssignment. Returning empty ...");
            return Optional.<GradebookAssignment>empty();
        }

        return gradingPersistenceManager.getExternalAssignment(gradebookUid, externalId);
    }

    @Override
    public void updateExternalAssessmentComments(String gradebookUid, String externalId,
            Map<String, String> studentUidsToComments) throws AssessmentNotFoundException {

        Optional<GradebookAssignment> optAsn = getDbExternalAssignment(gradebookUid, externalId);
        if (optAsn.isEmpty()) {
            throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
        }
        Set<String> studentIds = studentUidsToComments.keySet();
        if (studentIds.isEmpty()) {
            return;
        }

        GradebookAssignment asn = optAsn.get();

        List<AssignmentGradeRecord> existingScores
            = gradingPersistenceManager.getAssignmentGradeRecordsForAssignmentAndStudents(asn, studentIds);

        Set<String> changedStudents = new HashSet<>();
        for (AssignmentGradeRecord agr : existingScores) {
            String studentUid = agr.getStudentId();

            // Try to reduce data contention by only updating when a score
            // has changed or property has been set forcing a db update every time.
            boolean alwaysUpdate = isUpdateSameScore();

            CommentDefinition gradeComment = getAssignmentScoreComment(gradebookUid, asn.getId(), studentUid);
            String oldComment = gradeComment != null ? gradeComment.getCommentText() : null;
            String newComment = studentUidsToComments.get(studentUid);

            if (alwaysUpdate || (newComment != null && !newComment.equals(oldComment)) || (newComment == null && oldComment != null)) {
                changedStudents.add(studentUid);
                setAssignmentScoreComment(gradebookUid, asn.getId(), studentUid, newComment);
            }
        }

        log.debug("updateExternalAssessmentScores sent {} records, actually changed {}", studentIds.size(), changedStudents.size());
    }

    @Override
    public void updateExternalAssessmentScores(final String gradebookUid, final String externalId,
            final Map<String, Double> studentUidsToScores) throws AssessmentNotFoundException {

        final Optional<GradebookAssignment> optAssignment = getDbExternalAssignment(gradebookUid, externalId);
        if (optAssignment.isEmpty()) {
            throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
        }
        final Set<String> studentIds = studentUidsToScores.keySet();
        if (studentIds.isEmpty()) {
            return;
        }

        GradebookAssignment assignment = optAssignment.get();

        final Date now = new Date();
        final String graderId = getUserUid();

        final List<AssignmentGradeRecord> existingScores
            = gradingPersistenceManager.getAssignmentGradeRecordsForAssignmentAndStudents(assignment, studentIds);

        final Set<String> previouslyUnscoredStudents = new HashSet<>(studentIds);
        final Set<String> changedStudents = new HashSet<>();
        for (final AssignmentGradeRecord agr : existingScores) {
            final String studentUid = agr.getStudentId();
            previouslyUnscoredStudents.remove(studentUid);

            // Try to reduce data contention by only updating when a score
            // has changed or property has been set forcing a db update every time.
            final boolean alwaysUpdate = isUpdateSameScore();

            final Double oldPointsEarned = agr.getPointsEarned();
            final Double newPointsEarned = studentUidsToScores.get(studentUid);
            if (alwaysUpdate || (newPointsEarned != null && !newPointsEarned.equals(oldPointsEarned))
                    || (newPointsEarned == null && oldPointsEarned != null)) {
                agr.setDateRecorded(now);
                agr.setGraderId(graderId);
                agr.setPointsEarned(newPointsEarned);
                gradingPersistenceManager.saveAssignmentGradeRecord(agr);
                changedStudents.add(studentUid);
                postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, newPointsEarned);
            }
        }
        for (String studentUid : previouslyUnscoredStudents) {
            // Don't save unnecessary null scores.
            Double newPointsEarned = studentUidsToScores.get(studentUid);
            if (newPointsEarned != null) {
                AssignmentGradeRecord agr = new AssignmentGradeRecord(assignment, studentUid, newPointsEarned);
                agr.setDateRecorded(now);
                agr.setGraderId(graderId);
                gradingPersistenceManager.saveAssignmentGradeRecord(agr);
                changedStudents.add(studentUid);
                postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, newPointsEarned);
            }
        }

        log.debug("updateExternalAssessmentScores sent {} records, actually changed {}", studentIds.size(), changedStudents.size());
    }

    @Override
    public void updateExternalAssessmentScoresString(final String gradebookUid, final String externalId,
            final Map<String, String> studentUidsToScores) throws AssessmentNotFoundException {

        final Optional<GradebookAssignment> optAssignment = getDbExternalAssignment(gradebookUid, externalId);
        if (optAssignment.isEmpty()) {
            throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
        }
        GradebookAssignment assignment = optAssignment.get();
        final Set<String> studentIds = studentUidsToScores.keySet();
        if (studentIds.isEmpty()) {
            return;
        }
        final Date now = new Date();
        final String graderId = getUserUid();

        List<AssignmentGradeRecord> existingScores
            = gradingPersistenceManager.getAssignmentGradeRecordsForAssignmentAndStudents(assignment, studentIds);

        final Set<String> previouslyUnscoredStudents = new HashSet<>(studentIds);
        final Set<String> changedStudents = new HashSet<>();
        for (final AssignmentGradeRecord agr : existingScores) {
            final String studentUid = agr.getStudentId();
            previouslyUnscoredStudents.remove(studentUid);

            // Try to reduce data contention by only updating when a score
            // has changed or property has been set forcing a db update every time.
            final boolean alwaysUpdate = isUpdateSameScore();

            // TODO: for ungraded items, needs to set ungraded-grades later...
            final Double oldPointsEarned = agr.getPointsEarned();
            final String newPointsEarnedString = studentUidsToScores.get(studentUid);
            final Double newPointsEarned = (newPointsEarnedString == null) ? null : convertStringToDouble(newPointsEarnedString);
            if (alwaysUpdate || (newPointsEarned != null && !newPointsEarned.equals(oldPointsEarned))
                    || (newPointsEarned == null && oldPointsEarned != null)) {
                agr.setDateRecorded(now);
                agr.setGraderId(graderId);
                if (newPointsEarned != null) {
                    agr.setPointsEarned(newPointsEarned);
                } else {
                    agr.setPointsEarned(null);
                }
                gradingPersistenceManager.saveAssignmentGradeRecord(agr);
                changedStudents.add(studentUid);
                postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, newPointsEarned);
            }
        }
        for (String studentUid : previouslyUnscoredStudents) {
            // Don't save unnecessary null scores.
            String newPointsEarned = studentUidsToScores.get(studentUid);
            if (newPointsEarned != null) {
                AssignmentGradeRecord agr = new AssignmentGradeRecord(assignment, studentUid,
                        convertStringToDouble(newPointsEarned));
                agr.setDateRecorded(now);
                agr.setGraderId(graderId);
                gradingPersistenceManager.saveAssignmentGradeRecord(agr);
                changedStudents.add(studentUid);
                postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, convertStringToDouble(newPointsEarned));
            }
        }

        log.debug("updateExternalAssessmentScores sent {} records, actually changed {}", studentIds.size(), changedStudents.size());
    }

    @Override
    public boolean isExternalAssignmentDefined(String gradebookUid, String externalId) {
        // SAK-19668
        return getDbExternalAssignment(gradebookUid, externalId).isPresent();
    }

    @Override
    public boolean isExternalAssignmentGrouped(final String gradebookUid, final String externalId) {

        // SAK-19668
        final Optional<GradebookAssignment> optAssignment = getDbExternalAssignment(gradebookUid, externalId);
        // If we check all available providers for an existing, externally maintained assignment
        // and none manage it, return false since grouping is the outlier case and all items
        // showed for all users until the 2.9 release.
        boolean result = false;
        boolean providerResponded = false;
        if (optAssignment.isEmpty()) {
            log.info("No assignment found for external assignment check: gradebookUid=" + gradebookUid + ", externalId=" + externalId);
        } else {
            GradebookAssignment assignment = optAssignment.get();
            for (final ExternalAssignmentProvider provider : getExternalAssignmentProviders().values()) {
                if (provider.isAssignmentDefined(assignment.getExternalAppName(), externalId)) {
                    providerResponded = true;
                    result = result || provider.isAssignmentGrouped(externalId);
                }
            }
        }
        return result || !providerResponded;
    }

    @Override
    public boolean isExternalAssignmentVisible(final String gradebookUid, final String externalId, final String userId) {

        // SAK-19668
        final Optional<GradebookAssignment> optAssignment = getDbExternalAssignment(gradebookUid, externalId);
        // If we check all available providers for an existing, externally maintained assignment
        // and none manage it, assume that it should be visible. This matches the pre-2.9 behavior
        // when a provider is not implemented to handle the assignment. Also, any provider that
        // returns true will allow access (logical OR of responses).
        boolean result = false;
        boolean providerResponded = false;
        if (optAssignment.isEmpty()) {
            log.info("No assignment found for external assignment check: gradebookUid=" + gradebookUid + ", externalId=" + externalId);
        } else {
            GradebookAssignment assignment = optAssignment.get();
            for (final ExternalAssignmentProvider provider : getExternalAssignmentProviders().values()) {
                if (provider.isAssignmentDefined(assignment.getExternalAppName(), externalId)) {
                    providerResponded = true;
                    result = result || provider.isAssignmentVisible(externalId, userId);
                }
            }
        }
        return result || !providerResponded;
    }

    @Override
    public Map<String, String> getExternalAssignmentsForCurrentUser(final String gradebookUid) {

        final Map<String, String> visibleAssignments = new HashMap<>();
        final Set<String> providedAssignments = getProvidedExternalAssignments(gradebookUid);

        for (final ExternalAssignmentProvider provider : getExternalAssignmentProviders().values()) {
            final String appKey = provider.getAppKey();
            final List<String> assignments = provider.getExternalAssignmentsForCurrentUser(gradebookUid);
            for (final String externalId : assignments) {
                visibleAssignments.put(externalId, appKey);
            }
        }

        // We include those items that the gradebook has marked as externally maintained, but no provider has
        // identified as items under its authority. This maintains the behavior prior to the grouping support
        // introduced for the 2.9 release (SAK-11485 and SAK-19688), where a tool that does not have a provider
        // implemented does not have its items filtered for student views and grading.
        final List<org.sakaiproject.grading.api.Assignment> gbAssignments = getViewableAssignmentsForCurrentUser(gradebookUid);
        for (final org.sakaiproject.grading.api.Assignment assignment : gbAssignments) {
            final String id = assignment.getExternalId();
            if (assignment.getExternallyMaintained() && !providedAssignments.contains(id) && !visibleAssignments.containsKey(id)) {
                log.debug("External assignment in gradebook [{}] is not handled by a provider; ID: {}", gradebookUid, id);
                visibleAssignments.put(id, null);
            }
        }

        return visibleAssignments;
    }

    private Set<String> getProvidedExternalAssignments(final String gradebookUid) {
        final Set<String> allAssignments = new HashSet<>();
        for (final ExternalAssignmentProvider provider : getExternalAssignmentProviders().values()) {
            // TODO: This is a temporary cast; if this method proves to be the right fit
            // and perform well enough, it will be moved to the regular interface.
            if (provider instanceof ExternalAssignmentProviderCompat) {
                allAssignments.addAll(
                        ((ExternalAssignmentProviderCompat) provider).getAllExternalAssignments(gradebookUid));
            } else if (this.providerMethods.containsKey(provider)) {
                final Method m = this.providerMethods.get(provider);
                try {
                    @SuppressWarnings("unchecked")
                    final List<String> reflectedAssignments = (List<String>) m.invoke(provider, gradebookUid);
                    allAssignments.addAll(reflectedAssignments);
                } catch (final Exception e) {
                    log.debug("Exception calling getAllExternalAssignments", e);
                }
            }
        }
        return allAssignments;
    }

    @Override
    public Map<String, List<String>> getVisibleExternalAssignments(final String gradebookUid, final Collection<String> studentIds) {

        final Set<String> providedAssignments = getProvidedExternalAssignments(gradebookUid);

        final Map<String, Set<String>> visible = new HashMap<>();
        for (final String studentId : studentIds) {
            visible.put(studentId, new HashSet<String>());
        }

        for (final ExternalAssignmentProvider provider : getExternalAssignmentProviders().values()) {
            // SAK-24407 - Some tools modify this set so we can't pass it. I considered making it an unmodifableCollection but that would
            // require changing a number of tools
            final Set<String> studentIdsCopy = new HashSet<>(studentIds);
            final Map<String, List<String>> externals = provider.getAllExternalAssignments(gradebookUid, (studentIdsCopy));
            for (final String studentId : externals.keySet()) {
                if (visible.containsKey(studentId)) {
                    visible.get(studentId).addAll(externals.get(studentId));
                }
            }
        }

        // SAK-23733 - This covers a tricky case where items that the gradebook thinks are external
        // but are not reported by any provider should be included for everyone. This is
        // to accommodate tools that use the external assessment mechanisms but have not
        // implemented an ExternalAssignmentProvider.
        List<Assignment> allAssignments = getViewableAssignmentsForCurrentUser(gradebookUid);
        for (Assignment assignment : allAssignments) {
            String id = assignment.getExternalId();
            if (assignment.getExternallyMaintained() && !providedAssignments.contains(id)) {
                for (String studentId : visible.keySet()) {
                    visible.get(studentId).add(id);
                }
            }
        }

        return visible.keySet().stream()
            .collect(Collectors.toMap(k -> k, k -> new ArrayList<String>(visible.get(k))));
    }

    @Override
    public void setExternalAssessmentToGradebookAssignment(final String gradebookUid, final String externalId) {

        final Optional<GradebookAssignment> optAssignment = getDbExternalAssignment(gradebookUid, externalId);
        if (optAssignment.isEmpty()) {
            throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
        }
        GradebookAssignment assignment = optAssignment.get();
        assignment.setExternalAppName(null);
        assignment.setExternalId(null);
        assignment.setExternalInstructorLink(null);
        assignment.setExternalStudentLink(null);
        assignment.setExternalData(null);
        assignment.setExternallyMaintained(false);
        gradingPersistenceManager.saveAssignment(assignment);
        log.info("Externally-managed assignment {} moved to Gradebook management in gradebookUid={} by userUid={}", externalId,
                gradebookUid, getUserUid());
    }

    /**
     * Wrapper created when category was added for assignments tool
     */
    @Override
    public void addExternalAssessment(String gradebookUid, String externalId, String externalUrl, String title, Double points,
                                      Date dueDate, String externalServiceDescription, String externalData, Boolean ungraded)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException {

        addExternalAssessment(gradebookUid, externalId, externalUrl, title, points, dueDate, externalServiceDescription, externalData, ungraded, null);
    }

    @Override
    public void addExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl, final String title, final Double points,
                                                   final Date dueDate, final String externalServiceDescription, String externalData, final Boolean ungraded, final Long categoryId)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException {
        // Ensure that the required strings are not empty
        if (StringUtils.trimToNull(externalServiceDescription) == null ||
                StringUtils.trimToNull(externalId) == null ||
                StringUtils.trimToNull(title) == null) {
            throw new RuntimeException("External service description, externalId, and title must not be empty");
        }

        // Ensure that points is > zero
        if ((ungraded != null && !ungraded.booleanValue() && (points == null || points.doubleValue() <= 0))
                || (ungraded == null && (points == null || points.doubleValue() <= 0))) {
            throw new AssignmentHasIllegalPointsException("Points can't be null or Points must be > 0");
        }

        // Ensure that the assessment name is unique within this gradebook
        if (isAssignmentDefined(gradebookUid, title)) {
            throw new ConflictingAssignmentNameException("An assignment with that name already exists in gradebook uid=" + gradebookUid);
        }

        // name cannot contain these chars as they are reserved for special columns in import/export
        GradebookHelper.validateGradeItemName(title);

        // Ensure that the externalId is unique within this gradebook
        Long conflicts = gradingPersistenceManager.countAssignmentsByGradbookAndExternalId(gradebookUid, externalId);
        if (conflicts > 0L) {
            throw new ConflictingExternalIdException(
                    "An external assessment with that ID already exists in gradebook uid=" + gradebookUid);
        }

        // Get the gradebook
        final Gradebook gradebook = getGradebook(gradebookUid);

        // if a category was indicated, double check that it is valid
        Category persistedCategory = null;
        if (categoryId != null) {
            persistedCategory = getCategory(categoryId);
            if (persistedCategory.isDropScores() && !persistedCategory.getEqualWeightAssignments()) {
                List<GradebookAssignment> thisCategoryAssignments = getAssignmentsForCategory(categoryId);
                for (GradebookAssignment thisAssignment : thisCategoryAssignments) {
                    if (!Objects.equals(thisAssignment.getPointsPossible(), points)) {
                        String errorMessage = "Assignment points mismatch the selected Gradebook Category ("
                            + thisAssignment.getPointsPossible().toString() + ") and cannot be added to Gradebook )";
                        throw new InvalidCategoryException(errorMessage);
                    }
                }
            }
            if (persistedCategory == null || persistedCategory.getRemoved() ||
                    !persistedCategory.getGradebook().getId().equals(gradebook.getId())) {
                throw new InvalidCategoryException("The category with id " + categoryId +
                        " is not valid for gradebook " + gradebook.getUid());
            }
        }

        // Create the external assignment
        final GradebookAssignment asn = new GradebookAssignment(gradebook, title, points, dueDate);
        asn.setExternallyMaintained(true);
        asn.setExternalId(externalId);
        asn.setExternalInstructorLink(externalUrl);
        asn.setExternalStudentLink(externalUrl);
        asn.setExternalAppName(externalServiceDescription);
        asn.setExternalData(externalData);
        if (persistedCategory != null) {
            asn.setCategory(persistedCategory);
        }
        // set released to be true to support selective release
        asn.setReleased(true);
        if (ungraded != null) {
            asn.setUngraded(ungraded);
        } else {
            asn.setUngraded(false);
        }

        gradingPersistenceManager.saveGradebookAssignment(asn);

        log.info("External assessment added to gradebookUid={}, externalId={} by userUid={} from externalApp={}", gradebookUid, externalId,
                getUserUid(), externalServiceDescription);
    }

    @Override
    public void updateExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl, String externalData, final String title,
                                         final Double points, final Date dueDate, final Boolean ungraded)
            throws AssessmentNotFoundException, ConflictingAssignmentNameException, AssignmentHasIllegalPointsException {
        final Optional<GradebookAssignment> optAsn = getDbExternalAssignment(gradebookUid, externalId);

        if (optAsn.isEmpty()) {
            throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
        }

        GradebookAssignment asn = optAsn.get();

        // Ensure that points is > zero
        if ((ungraded != null && !ungraded.booleanValue() && (points == null || points.doubleValue() <= 0))
                || (ungraded == null && (points == null || points.doubleValue() <= 0))) {
            throw new AssignmentHasIllegalPointsException("Points can't be null or Points must be > 0");
        }

        // Ensure that the required strings are not empty
        if (StringUtils.trimToNull(externalId) == null ||
                StringUtils.trimToNull(title) == null) {
            throw new RuntimeException("ExternalId, and title must not be empty");
        }

        // name cannot contain these chars as they are reserved for special columns in import/export
        GradebookHelper.validateGradeItemName(title);

        asn.setExternalInstructorLink(externalUrl);
        asn.setExternalStudentLink(externalUrl);
        asn.setExternalData(externalData);
        asn.setName(title);
        asn.setDueDate(dueDate);
        // support selective release
        asn.setReleased(BooleanUtils.isTrue(asn.getReleased()));
        asn.setPointsPossible(points);
        if (ungraded != null) {
            asn.setUngraded(ungraded.booleanValue());
        } else {
            asn.setUngraded(false);
        }
        gradingPersistenceManager.saveGradebookAssignment(asn);

        log.info("External assessment updated in gradebookUid={}, externalId={} by userUid={}", gradebookUid, externalId, getUserUid());
    }

    @Override
    public void updateExternalAssessmentComment(final String gradebookUid, final String externalId, final String studentUid,
            final String comment)
            throws AssessmentNotFoundException {

        final Optional<GradebookAssignment> optAsn = getDbExternalAssignment(gradebookUid, externalId);

        if (optAsn.isEmpty()) {
            throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
        }

        GradebookAssignment asn = optAsn.get();

        log.debug("BEGIN: Update 1 score for gradebookUid={}, external assessment={} from {}", gradebookUid, externalId,
                asn.getExternalAppName());

        // Try to reduce data contention by only updating when the
        // score has actually changed or property has been set forcing a db update every time.
        final boolean alwaysUpdate = isUpdateSameScore();

        final CommentDefinition gradeComment = getAssignmentScoreComment(gradebookUid, asn.getId(), studentUid);
        final String oldComment = gradeComment != null ? gradeComment.getCommentText() : null;

        if (alwaysUpdate || (comment != null && !comment.equals(oldComment)) ||
                (comment == null && oldComment != null)) {
            if (comment != null) {
                setAssignmentScoreComment(gradebookUid, asn.getId(), studentUid, comment);
            } else {
                setAssignmentScoreComment(gradebookUid, asn.getId(), studentUid, null);
            }
            log.debug("updateExternalAssessmentComment: grade record saved");
        } else {
            log.debug("Ignoring updateExternalAssessmentComment, since the new comment is the same as the old");
        }
        log.debug("END: Update 1 score for gradebookUid={}, external assessment={} from {}", gradebookUid, externalId,
                asn.getExternalAppName());
        log.debug("External assessment comment updated in gradebookUid={}, externalId={} by userUid={}, new score={}", gradebookUid,
                externalId, getUserUid(), comment);
    }

    @Override
    public void updateExternalAssessmentScore(final String gradebookUid, final String externalId, final String studentUid,
            final String points)
            throws AssessmentNotFoundException {
        final Optional<GradebookAssignment> optAsn = getDbExternalAssignment(gradebookUid, externalId);

        if (optAsn.isEmpty()) {
            throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
        }

        GradebookAssignment asn = optAsn.get();

        log.debug("BEGIN: Update 1 score for gradebookUid={}, external assessment={} from {}", gradebookUid, externalId,
                asn.getExternalAppName());

        final Date now = new Date();

        AssignmentGradeRecord agr = getAssignmentGradeRecord(asn, studentUid);

        // Try to reduce data contention by only updating when the
        // score has actually changed or property has been set forcing a db update every time.
        final boolean alwaysUpdate = isUpdateSameScore();

        // TODO: for ungraded items, needs to set ungraded-grades later...
        final Double oldPointsEarned = (agr == null) ? null : agr.getPointsEarned();
        final Double newPointsEarned = (points == null) ? null : convertStringToDouble(points);
        if (alwaysUpdate || (newPointsEarned != null && !newPointsEarned.equals(oldPointsEarned)) ||
                (newPointsEarned == null && oldPointsEarned != null)) {
            if (agr == null) {
                if (newPointsEarned != null) {
                    agr = new AssignmentGradeRecord(asn, studentUid, Double.valueOf(newPointsEarned));
                } else {
                    agr = new AssignmentGradeRecord(asn, studentUid, null);
                }
            } else {
                if (newPointsEarned != null) {
                    agr.setPointsEarned(Double.valueOf(newPointsEarned));
                } else {
                    agr.setPointsEarned(null);
                }
            }

            agr.setDateRecorded(now);
            agr.setGraderId(getUserUid());
            log.debug("About to save AssignmentGradeRecord id={}, version={}, studenttId={}, pointsEarned={}", agr.getId(),
                    agr.getVersion(), agr.getStudentId(), agr.getPointsEarned());
            gradingPersistenceManager.saveAssignmentGradeRecord(agr);

            // Sync database.
            postUpdateGradeEvent(gradebookUid, asn.getName(), studentUid, newPointsEarned);
        } else {
            log.debug("Ignoring updateExternalAssessmentScore, since the new points value is the same as the old");
        }

        log.debug("END: Update 1 score for gradebookUid={}, external assessment={} from {}", gradebookUid, externalId,
                asn.getExternalAppName());
        log.debug("External assessment score updated in gradebookUid={}, externalId={} by userUid={}, new score={}", gradebookUid,
                externalId, getUserUid(), points);
    }

    /**
     *
     * @param s the string we want to convert to a double
     * @return a locale-aware Double value representation of the given String
     * @throws ParseException
     */
    /*
    private Double convertStringToDouble(final String s) {
        Double scoreAsDouble = null;
        String doubleAsString = s;
        if (doubleAsString != null && !"".equals(doubleAsString)) {
            try {
                // check if grade uses a comma as separator because of number format and change to a comma y the external app sends a point
                // as separator
                final DecimalFormat dcformat = (DecimalFormat) getNumberFormat();
                final String decSeparator = dcformat.getDecimalFormatSymbols().getDecimalSeparator() + "";
                if (",".equals(decSeparator)) {
                    doubleAsString = doubleAsString.replace(".", ",");
                }
                final Number numericScore = getNumberFormat().parse(doubleAsString.trim());
                scoreAsDouble = numericScore.doubleValue();
            } catch (final ParseException e) {
                log.error(e.getMessage());
            }
        }

        return scoreAsDouble;
    }
    */

    private NumberFormat getNumberFormat() {
        return NumberFormat.getInstance(resourceLoader.getLocale());
    }

    @Override
    public Long getExternalAssessmentCategoryId(final String gradebookUId, final String externalId) {
        Long categoryId = null;
        final Optional<GradebookAssignment> optAssignment = getDbExternalAssignment(gradebookUId, externalId);
        if (optAssignment.isEmpty()) {
            throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUId);
        }
        GradebookAssignment assignment = optAssignment.get();
        if (assignment.getCategory() != null) {
            categoryId = assignment.getCategory().getId();
        }
        return categoryId;
    }

    /**
     * Determines whether to update a grade record when there have been no changes. This is useful when we need to update only
     * gb_grade_record_t's 'DATE_RECORDED' field for instance. Generally uses the sakai.property
     * 'gradebook.externalAssessments.updateSameScore', but a site property by the same name can override it. That is to say, the site
     * property is checked first, and if it is not present, the sakai.property is used.
     */
    private boolean isUpdateSameScore() {
        String siteProperty = null;
        try {
            final String siteId = this.toolManager.getCurrentPlacement().getContext();
            final Site site = this.siteService.getSite(siteId);
            siteProperty = site.getProperties().getProperty(UPDATE_SAME_SCORE_PROP);
        } catch (final Exception e) {
            // Can't access site property. Leave it set to null
        }

        // Site property override not set. Use setting in sakai.properties
        if (siteProperty == null) {
            return this.serverConfigurationService.getBoolean(UPDATE_SAME_SCORE_PROP, UPDATE_SAME_SCORE_PROP_DEFAULT);
        }

        return Boolean.TRUE.toString().equals(siteProperty);
    }

    @Override
    public boolean isCategoriesEnabled(String gradebookUid) {

        return getGradebook(gradebookUid).getCategoryType() != GradingCategoryType.NO_CATEGORY;
    }

    @Override
    @Transactional
    public Gradebook addGradebook(final String uid) {

        log.debug("Adding gradebook uid={} by userUid={}", uid, getUserUid());

        createDefaultLetterGradeMapping(getHardDefaultLetterMapping());

        // Get available grade mapping templates.
        List<GradingScale> gradingScales = gradingPersistenceManager.getAvailableGradingScales();

        // The application won't be able to run without grade mapping
        // templates, so if for some reason none have been defined yet,
        // do that now.
        if (gradingScales.isEmpty()) {
            log.info("No Grading Scale defined yet. This is probably because you have upgraded or you are working with a new database. Default grading scales will be created. Any customized system-wide grade mappings you may have defined in previous versions will have to be reconfigured.");
            gradingScales = addDefaultGradingScales();
        }

        // Create and save the gradebook
        final Gradebook gradebook = new Gradebook();
        gradebook.setName(uid);
        gradebook.setUid(uid);
        gradingPersistenceManager.saveGradebook(gradebook);

        // Create the course grade for the gradebook
        final CourseGrade cg = new CourseGrade();
        cg.setGradebook(gradebook);
        gradingPersistenceManager.saveCourseGrade(cg);

        // According to the specification, Display GradebookAssignment Grades is
        // on by default, and Display course grade is off. But can be overridden via properties

        Boolean propAssignmentsDisplayed = this.serverConfigurationService.getBoolean(PROP_ASSIGNMENTS_DISPLAYED,true);
        gradebook.setAssignmentsDisplayed(propAssignmentsDisplayed);

        Boolean propCourseGradeDisplayed = this.serverConfigurationService.getBoolean(PROP_COURSE_GRADE_DISPLAYED,false);
        gradebook.setCourseGradeDisplayed(propCourseGradeDisplayed);

        Boolean propCoursePointsDisplayed = this.serverConfigurationService.getBoolean(PROP_COURSE_POINTS_DISPLAYED,false);
        gradebook.setCoursePointsDisplayed(propCoursePointsDisplayed);

        final String defaultScaleUid = getPropertyValue(UID_OF_DEFAULT_GRADING_SCALE_PROPERTY);

        // Add and save grade mappings based on the templates.
        GradeMapping defaultGradeMapping = null;
        Set<GradeMapping> gradeMappings = new HashSet<>();
        for (GradingScale gradingScale : gradingScales) {
            GradeMapping gradeMapping = new GradeMapping(gradingScale);
            gradeMapping.setGradebook(gradebook);
            gradingPersistenceManager.saveGradeMapping(gradeMapping);
            gradeMappings.add(gradeMapping);
            if (gradingScale.getUid().equals(defaultScaleUid)) {
                defaultGradeMapping = gradeMapping;
            }
        }

        // Check for null default.
        if (defaultGradeMapping == null) {
            defaultGradeMapping = gradeMappings.iterator().next();
            if (log.isWarnEnabled()) {
                log.warn("No default GradeMapping found for new Gradebook={}; will set default to {}",
                        gradebook.getUid(), defaultGradeMapping.getName());
            }
        }
        gradebook.setSelectedGradeMapping(defaultGradeMapping);

        // The Hibernate mapping as of Sakai 2.2 makes this next
        // call meaningless when it comes to persisting changes at
        // the end of the transaction. It is, however, needed for
        // the mappings to be seen while the transaction remains
        // uncommitted.
        gradebook.setGradeMappings(gradeMappings);

        gradebook.setGradeType(GradeType.POINTS);
        gradebook.setCategoryType(GradingCategoryType.NO_CATEGORY);

        //SAK-29740 make backwards compatible
        gradebook.setCourseLetterGradeDisplayed(true);
        gradebook.setCourseAverageDisplayed(true);

        // SAK-33855 turn on stats for new gradebooks
        final Boolean propAssignmentStatsDisplayed = this.serverConfigurationService.getBoolean(PROP_ASSIGNMENT_STATS_DISPLAYED, true);
        gradebook.setAssignmentStatsDisplayed(propAssignmentStatsDisplayed);

        final Boolean propCourseGradeStatsDisplayed = this.serverConfigurationService.getBoolean(PROP_COURSE_GRADE_STATS_DISPLAYED, true);
        gradebook.setCourseGradeStatsDisplayed(propCourseGradeStatsDisplayed);

        // Update the gradebook with the new selected grade mapping
        return gradingPersistenceManager.saveGradebook(gradebook);
    }

    private List<GradingScale> addDefaultGradingScales() {

        final List<GradingScale> gradingScales = new ArrayList<>();

        // Base the default set of templates on the old
        // statically defined GradeMapping classes.
        final GradeMapping[] oldGradeMappings = {
            new LetterGradeMapping(),
            new LetterGradePlusMinusMapping(),
            new PassNotPassMapping(),
            new GradePointsMapping()
        };

        for (final GradeMapping sampleMapping : oldGradeMappings) {
            sampleMapping.setDefaultValues();
            GradingScale gradingScale = new GradingScale();
            String uid = sampleMapping.getClass().getName();
            uid = uid.substring(uid.lastIndexOf('.') + 1);
            gradingScale.setUid(uid);
            gradingScale.setUnavailable(false);
            gradingScale.setName(sampleMapping.getName());
            gradingScale.setGrades(new ArrayList<>(sampleMapping.getGrades()));
            gradingScale.setDefaultBottomPercents(new HashMap<>(sampleMapping.getGradeMap()));
            gradingPersistenceManager.saveGradingScale(gradingScale);
            log.info("Added Grade Mapping " + gradingScale.getUid());
            gradingScales.add(gradingScale);
        }
        setDefaultGradingScale("LetterGradePlusMinusMapping");
        return gradingScales;
    }

    @Override
    public void setAvailableGradingScales(Collection gradingScaleDefinitions) {
        mergeGradeMappings(gradingScaleDefinitions);
    }

    @Override
    public void saveGradeMappingToGradebook(String scaleUuid, String gradebookUid) {

        List<GradingScale> gradingScales = gradingPersistenceManager.getAvailableGradingScales();

        for (GradingScale gradingScale : gradingScales) {
            if (gradingScale.getUid().equals(scaleUuid)) {
                GradeMapping gradeMapping = new GradeMapping(gradingScale);
                Gradebook gradebookToSet = getGradebook(gradebookUid);
                gradeMapping.setGradebook(gradebookToSet);
                gradingPersistenceManager.saveGradeMapping(gradeMapping);
            }
        }
    }

    @Override
    public List<GradingScale> getAvailableGradingScales() {

        List<GradingScale> gradingScales = gradingPersistenceManager.getAvailableGradingScales();

        // The application won't be able to run without grade mapping
        // templates, so if for some reason none have been defined yet,
        // do that now.
        if (gradingScales.isEmpty()) {
            log.info("No Grading Scale defined yet. This is probably because you have upgraded or you are working with a new database. Default grading scales will be created. Any customized system-wide grade mappings you may have defined in previous versions will have to be reconfigured.");
            gradingScales = addDefaultGradingScales();
        }
        return gradingScales;
    }

    @Override
    public List<GradingScaleDefinition> getAvailableGradingScaleDefinitions() {
        final List<GradingScale> gradingScales = getAvailableGradingScales();

        final List<GradingScaleDefinition> rval = new ArrayList<>();
        for (final GradingScale gradingScale: gradingScales) {
            rval.add(gradingScale.toGradingScaleDefinition());
        }
        return rval;
    }

    @Override
    public void setDefaultGradingScale(final String uid) {
        setPropertyValue(UID_OF_DEFAULT_GRADING_SCALE_PROPERTY, uid);
    }

    private void copyDefinitionToScale(final GradingScaleDefinition bean, final GradingScale gradingScale) {

        gradingScale.setUnavailable(false);
        gradingScale.setName(bean.getName());
        gradingScale.setGrades(bean.getGrades());
        Map<String, Double> defaultBottomPercents = new HashMap<>();
        Iterator gradesIter = bean.getGrades().iterator();
        Iterator defaultBottomPercentsIter = bean.getDefaultBottomPercentsAsList().iterator();
        while (gradesIter.hasNext() && defaultBottomPercentsIter.hasNext()) {
            String grade = (String)gradesIter.next();
            Double value = (Double)defaultBottomPercentsIter.next();
            defaultBottomPercents.put(grade, value);
        }
        gradingScale.setDefaultBottomPercents(defaultBottomPercents);
    }

    private void mergeGradeMappings(final Collection<GradingScaleDefinition> gradingScaleDefinitions) {

        final Map<String, GradingScaleDefinition> newMappingDefinitionsMap = new HashMap<>();
        final Set<String> uidsToSet = new HashSet<>();
        for (GradingScaleDefinition bean : gradingScaleDefinitions) {
            newMappingDefinitionsMap.put(bean.getUid(), bean);
            uidsToSet.add(bean.getUid());
        }

        // Until we move to Hibernate 3 syntax, we need to update one record at a time.
        // Toggle any scales that are no longer specified.
        List<GradingScale> gmtList = gradingPersistenceManager.getOtherAvailableGradingScales(uidsToSet);
        for (GradingScale gradingScale : gmtList) {
            gradingScale.setUnavailable(true);
            gradingPersistenceManager.saveGradingScale(gradingScale);
            log.info("Set Grading Scale {} unavailable", gradingScale.getUid());
        }

        // Modify any specified scales that already exist.
        //q = session.createQuery("from GradingScale as gradingScale where gradingScale.uid in (:uidList)");
        gmtList = gradingPersistenceManager.getGradingScalesByUids(uidsToSet);
        for (GradingScale gradingScale : gmtList) {
            copyDefinitionToScale(newMappingDefinitionsMap.get(gradingScale.getUid()), gradingScale);
            uidsToSet.remove(gradingScale.getUid());
            gradingPersistenceManager.saveGradingScale(gradingScale);
            log.info("Updated Grading Scale {}", gradingScale.getUid());
        }

        // Add any new scales.
        for (final String uid : uidsToSet) {
            final GradingScale gradingScale = new GradingScale();
            gradingScale.setUid(uid);
            final GradingScaleDefinition bean = newMappingDefinitionsMap.get(uid);
            copyDefinitionToScale(bean, gradingScale);
            gradingPersistenceManager.saveGradingScale(gradingScale);
            log.info("Added Grading Scale {}", gradingScale.getUid());
        }
    }

    @Override
    public void deleteGradebook(String gradebookUid) {

        log.debug("Deleting gradebook uid={} by userUid={}", gradebookUid, getUserUid());

        //Gradebook gradebook = gradingPersistencegetGradebook(uid);
        gradingPersistenceManager.deleteGradebook(gradebookUid);
    }

    @Override
    public void updateGradeMapping(Long gradeMappingId, Map<String, Double> gradeMap) {

        Optional<GradeMapping> optGradeMapping = gradingPersistenceManager.getGradeMapping(gradeMappingId);

        if (optGradeMapping.isPresent()) {
            optGradeMapping.get().setGradeMap(gradeMap);
            gradingPersistenceManager.saveGradeMapping(optGradeMapping.get());
        } else {
            log.warn("No grade mapping for id {}", gradeMappingId);
        }
    }

    public CommentDefinition getAssignmentScoreComment(String gradebookUid, Long assignmentId, String studentUid) throws AssessmentNotFoundException {

        if (StringUtils.isBlank(gradebookUid) || assignmentId == null || StringUtils.isBlank(studentUid)) {
            throw new IllegalArgumentException("gradebookUid, assignmentId and studentUid must be valid.");
        }

        final GradebookAssignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentId);
        if (assignment == null) {
            throw new AssessmentNotFoundException("There is no assignmentId " + assignmentId + " for gradebookUid " + gradebookUid);
        }

        CommentDefinition commentDefinition = null;
        final Optional<Comment> optComment = gradingPersistenceManager.getInternalComment(studentUid, gradebookUid, assignmentId);
        if (optComment.isPresent()) {
            Comment comment = optComment.get();
            commentDefinition = new CommentDefinition();
            commentDefinition.setAssignmentName(assignment.getName());
            commentDefinition.setCommentText(comment.getCommentText());
            commentDefinition.setDateRecorded(comment.getDateRecorded());
            commentDefinition.setGraderUid(comment.getGraderId());
            commentDefinition.setStudentUid(comment.getStudentId());
        }
        return commentDefinition;
    }

    public void setAssignmentScoreComment(String gradebookUid, Long assignmentId, String studentUid, String commentText) throws AssessmentNotFoundException {

        if (StringUtils.isBlank(gradebookUid) || assignmentId == null || StringUtils.isBlank(studentUid) || StringUtils.isBlank(commentText)) {
            throw new IllegalArgumentException("gradebookUid, assignmentId, studentUid and commentText must be valid.");
        }

        final Optional<Comment> optComment = gradingPersistenceManager.getInternalComment(studentUid, gradebookUid, assignmentId);
        Comment comment = null;
        if (optComment.isEmpty()) {
            comment = new Comment(studentUid, commentText, getAssignmentWithoutStats(gradebookUid, assignmentId));
        } else {
            comment = optComment.get();
            comment.setCommentText(commentText);
        }
        comment.setGraderId(sessionManager.getCurrentSessionUserId());
        comment.setDateRecorded(new Date());
        gradingPersistenceManager.saveComment(comment);
    }

    public Gradebook getGradebook(String uid) {

        return gradingPersistenceManager.getGradebook(uid).orElseGet(() -> addGradebook(uid));
    }

    private List<GradebookAssignment> getAssignments(Long gradebookId) {
        return gradingPersistenceManager.getAssignmentsForGradebook(gradebookId);
    }

    public String getGradebookUid(final Long id) {

        Optional<Gradebook> optGradebook = gradingPersistenceManager.getGradebook(id);
        if (optGradebook.isPresent()) {
            return optGradebook.get().getUid();
        } else {
            log.warn("No gradebook for id {}", id);
            return null;
        }
    }

    @Deprecated
    private GradebookAssignment getAssignmentWithoutStats(String gradebookUid, String assignmentName) {
        return gradingPersistenceManager.getAssignmentByNameAndGradebook(assignmentName, gradebookUid).orElse(null);
    }

    private GradebookAssignment getAssignmentWithoutStats(String gradebookUid, Long assignmentId) {
        return gradingPersistenceManager.getAssignmentByIdAndGradebook(assignmentId, gradebookUid).orElse(null);
    }

    private void updateAssignment(final GradebookAssignment assignment) throws ConflictingAssignmentNameException {
        // Ensure that we don't have the assignment in the session, since
        // we need to compare the existing one in the db to our edited assignment
        //final Session session = getSessionFactory().getCurrentSession();
        //session.evict(assignment);

        //final GradebookAssignment asnFromDb = (GradebookAssignment) session.load(GradebookAssignment.class, assignment.getId());

        final Long count = gradingPersistenceManager.countDuplicateAssignments(assignment);

        if (count > 0) {
            throw new ConflictingAssignmentNameException("You can not save multiple assignments in a gradebook with the same name");
        }

        //session.evict(asnFromDb);
        gradingPersistenceManager.saveAssignment(assignment);
        //session.update(assignment);
    }

    private AssignmentGradeRecord getAssignmentGradeRecord(GradebookAssignment assignment, String studentUid) {
        return gradingPersistenceManager.getAssignmentGradeRecordForAssignmentAndStudent(assignment.getId(), studentUid);
    }

    public void postEvent(final String event, final String objectReference) {
        this.eventTrackingService.post(this.eventTrackingService.newEvent(event, objectReference, true));
    }

    public List<Category> getCategories(Long gradebookId) {
        return gradingPersistenceManager.getCategoriesForGradebook(gradebookId);
    }

    public List<GradebookAssignment> getAssignmentsForCategory(Long categoryId) {
        return gradingPersistenceManager.getAssignmentsForCategory(categoryId);
    }

    public Category getCategory(Long categoryId) {
        return gradingPersistenceManager.getCategory(categoryId).orElse(null);
    }

    public void updateCategory(final Category category) throws ConflictingCategoryNameException, StaleObjectModificationException {
        //session.evict(category);
        final Category persistentCat = gradingPersistenceManager.getCategory(category.getId()).orElse(null);

        if (gradingPersistenceManager.existsDuplicateCategory(category.getName(), category.getGradebook(), category.getId())) {
            throw new ConflictingCategoryNameException("You can not save multiple category in a gradebook with the same name");
        }
        if (category.getWeight().doubleValue() > 1 || category.getWeight().doubleValue() < 0) {
            throw new IllegalArgumentException("weight for category is greater than 1 or less than 0 in updateCategory of BaseHibernateManager");
        }
        //session.evict(persistentCat);
        gradingPersistenceManager.saveCategory(category);
    }

    public void removeCategory(Long categoryId) throws StaleObjectModificationException{

        Optional<Category> optCategory = gradingPersistenceManager.getCategory(categoryId);

        if (optCategory.isPresent()) {

            getAssignmentsForCategory(optCategory.get().getId()).forEach(assignment -> {

                assignment.setCategory(null);
                updateAssignment(assignment);
            });

            optCategory.get().setRemoved(true);
            gradingPersistenceManager.saveCategory(optCategory.get());
        } else {
            log.warn("No category for id {}", categoryId);
        }
    }

    private Optional<LetterGradePercentMapping> getDefaultLetterGradePercentMapping() {

        List<LetterGradePercentMapping> mappings = gradingPersistenceManager.getDefaultLetterGradePercentMappings();

        if (mappings.size() == 0) {
            log.info("Default letter grade mapping hasn't been created in DB in BaseHibernateManager.getDefaultLetterGradePercentMapping");
            return Optional.<LetterGradePercentMapping>empty();
        }

        if (mappings.size() > 1) {
            log.error("Duplicate default letter grade mapping was created in DB in BaseHibernateManager.getDefaultLetterGradePercentMapping");
            return Optional.<LetterGradePercentMapping>empty();
        }

        return Optional.of(mappings.get(0));
    }

    private void createOrUpdateDefaultLetterGradePercentMapping(final Map gradeMap) {

        if (gradeMap == null) {
            throw new IllegalArgumentException("gradeMap is null in BaseHibernateManager.createOrUpdateDefaultLetterGradePercentMapping");
        }

        Optional<LetterGradePercentMapping> lgpm = getDefaultLetterGradePercentMapping();

        if (lgpm.isPresent()) {
            updateDefaultLetterGradePercentMapping(gradeMap, lgpm.get());
        } else {
            createDefaultLetterGradePercentMapping(gradeMap);
        }
    }

    private void updateDefaultLetterGradePercentMapping(final Map<String, Double> gradeMap, final LetterGradePercentMapping lgpm) {

        if (gradeMap.keySet().size() != GradingService.validLetterGrade.length) {
            throw new IllegalArgumentException("gradeMap doesn't have right size in BaseHibernateManager.updateDefaultLetterGradePercentMapping");
        }

        if (!validateLetterGradeMapping(gradeMap)) {
            throw new IllegalArgumentException("gradeMap contains invalid letter in BaseHibernateManager.updateDefaultLetterGradePercentMapping");
        }

        lgpm.setGradeMap(gradeMap);
        gradingPersistenceManager.saveLetterGradePercentMapping(lgpm);
    }

    public void createDefaultLetterGradePercentMapping(final Map<String, Double> gradeMap) {

        if (getDefaultLetterGradePercentMapping().isPresent()) {
            throw new IllegalArgumentException("gradeMap has already been created in BaseHibernateManager.createDefaultLetterGradePercentMapping");
        }

        if (gradeMap == null) {
            throw new IllegalArgumentException("gradeMap is null in BaseHibernateManager.createDefaultLetterGradePercentMapping");
        }

        final Set<String> keySet = gradeMap.keySet();

        if (keySet.size() != GradingService.validLetterGrade.length) {
            throw new IllegalArgumentException("gradeMap doesn't have right size in BaseHibernateManager.createDefaultLetterGradePercentMapping");
        }

        if (!validateLetterGradeMapping(gradeMap)) {
            throw new IllegalArgumentException("gradeMap contains invalid letter in BaseHibernateManager.createDefaultLetterGradePercentMapping");
        }

        final LetterGradePercentMapping lgpm = new LetterGradePercentMapping();
        final Map<String, Double> saveMap = new HashMap<>(gradeMap);
        lgpm.setGradeMap(saveMap);
        lgpm.setMappingType(1);
        gradingPersistenceManager.saveLetterGradePercentMapping(lgpm);
    }

    public LetterGradePercentMapping getLetterGradePercentMapping(Gradebook gradebook) {

        Optional<LetterGradePercentMapping> optMapping
            = gradingPersistenceManager.getLetterGradePercentMappingForGradebook(gradebook.getId());

        if (optMapping.isEmpty()) {
            LetterGradePercentMapping lgpm = getDefaultLetterGradePercentMapping().get();
            LetterGradePercentMapping returnLgpm = new LetterGradePercentMapping();
            returnLgpm.setGradebookId(gradebook.getId());
            returnLgpm.setGradeMap(lgpm.getGradeMap());
            returnLgpm.setMappingType(2);
            return returnLgpm;
        } else {
            return optMapping.get();
        }
    }

    /**
     * this method is different with getLetterGradePercentMapping -
     * it returns null if no mapping exists for gradebook instead of
     * returning default mapping.
     */
    private LetterGradePercentMapping getLetterGradePercentMappingForGradebook(Gradebook gradebook) {
        return gradingPersistenceManager.getLetterGradePercentMappingForGradebook(gradebook.getId()).orElse(null);
    }

    public void saveOrUpdateLetterGradePercentMapping(final Map<String, Double> gradeMap, final Gradebook gradebook) {

        if (gradeMap == null) {
            throw new IllegalArgumentException("gradeMap is null in BaseHibernateManager.saveOrUpdateLetterGradePercentMapping");
        }

        final LetterGradePercentMapping lgpm = getLetterGradePercentMappingForGradebook(gradebook);

        if (lgpm == null) {
            final Set<String> keySet = gradeMap.keySet();

            if (keySet.size() != GradingService.validLetterGrade.length) { //we only consider letter grade with -/+ now.
                throw new IllegalArgumentException("gradeMap doesn't have right size in BaseHibernateManager.saveOrUpdateLetterGradePercentMapping");
            }
            if (!validateLetterGradeMapping(gradeMap)) {
                throw new IllegalArgumentException("gradeMap contains invalid letter in BaseHibernateManager.saveOrUpdateLetterGradePercentMapping");
            }

            final LetterGradePercentMapping lgpm1 = new LetterGradePercentMapping();
            final Map<String, Double> saveMap = new HashMap<>(gradeMap);
            lgpm1.setGradeMap(saveMap);
            lgpm1.setGradebookId(gradebook.getId());
            lgpm1.setMappingType(2);
            gradingPersistenceManager.saveLetterGradePercentMapping(lgpm1);
        }
        else
        {
            udpateLetterGradePercentMapping(gradeMap, gradebook);
        }
    }

    private void udpateLetterGradePercentMapping(final Map<String, Double> gradeMap, final Gradebook gradebook) {

        final LetterGradePercentMapping lgpm = getLetterGradePercentMapping(gradebook);

        if (lgpm == null) {
            throw new IllegalArgumentException("LetterGradePercentMapping is null in BaseHibernateManager.updateLetterGradePercentMapping");
        }
        if (gradeMap == null) {
            throw new IllegalArgumentException("gradeMap is null in BaseHibernateManager.updateLetterGradePercentMapping");
        }
        final Set<String> keySet = gradeMap.keySet();

        if (keySet.size() != GradingService.validLetterGrade.length) { //we only consider letter grade with -/+ now.
            throw new IllegalArgumentException("gradeMap doesn't have right size in BaseHibernateManager.udpateLetterGradePercentMapping");
        }
        if (validateLetterGradeMapping(gradeMap) == false) {
            throw new IllegalArgumentException("gradeMap contains invalid letter in BaseHibernateManager.udpateLetterGradePercentMapping");
        }
        final Map<String, Double> saveMap = new HashMap<>(gradeMap);
        lgpm.setGradeMap(saveMap);
        gradingPersistenceManager.saveLetterGradePercentMapping(lgpm);
    }

    private boolean validateLetterGradeMapping(final Map<String, Double> gradeMap) {

        for (final String key : gradeMap.keySet()) {
            boolean validLetter = false;
            for (final String element : GradingService.validLetterGrade) {
                if (key.equalsIgnoreCase(element)) {
                    validLetter = true;
                    break;
                }
            }
            if (!validLetter) {
                return false;
            }
        }
        return true;
    }

    public Long createUngradedAssignment(final Long gradebookId, final String name, final Date dueDate, final Boolean isNotCounted,
                                         final Boolean isReleased) throws ConflictingAssignmentNameException, StaleObjectModificationException {
        final Gradebook gb = gradingPersistenceManager.getGradebook(gradebookId).orElse(null);;

        // trim the name before validation
        final String trimmedName = StringUtils.trimToEmpty(name);

        if (assignmentNameExists(trimmedName, gb)) {
            throw new ConflictingAssignmentNameException("You can not save multiple assignments in a gradebook with the same name");
        }

        final GradebookAssignment asn = new GradebookAssignment();
        asn.setGradebook(gb);
        asn.setName(trimmedName);
        asn.setDueDate(dueDate);
        asn.setUngraded(true);
        if (isNotCounted != null) {
            asn.setNotCounted(isNotCounted);
        }
        if (isReleased != null) {
            asn.setReleased(isReleased);
        }

        return gradingPersistenceManager.saveAssignment(asn).getId();
    }

    /**
     *
     * @param id
     * @return the GradebookAssignment object with the given id
     */
    public GradebookAssignment getAssignment(Long id) {
        return gradingPersistenceManager.getAssignmentById(id).orElse(null);
    }

    private void createDefaultLetterGradeMapping(final Map gradeMap) {

        if (getDefaultLetterGradePercentMapping().isEmpty()) {
            final Set keySet = gradeMap.keySet();

            if (keySet.size() != GradingService.validLetterGrade.length) {
                throw new IllegalArgumentException("gradeMap doesn't have right size in BaseHibernateManager.createDefaultLetterGradePercentMapping");
            }

            if (!validateLetterGradeMapping(gradeMap)) {
                throw new IllegalArgumentException("gradeMap contains invalid letter in BaseHibernateManager.createDefaultLetterGradePercentMapping");
            }

            final LetterGradePercentMapping lgpm = new LetterGradePercentMapping();
            gradingPersistenceManager.saveLetterGradePercentMapping(lgpm);
            final Map saveMap = new HashMap();
            for (final Iterator iter = gradeMap.keySet().iterator(); iter.hasNext();)
            {
                final String key = (String) iter.next();
                saveMap.put(key, gradeMap.get(key));
            }
            if (lgpm != null)
            {
                lgpm.setGradeMap(saveMap);
                lgpm.setMappingType(1);
                gradingPersistenceManager.saveLetterGradePercentMapping(lgpm);
            }
        }
    }

    private Map<String, Double> getHardDefaultLetterMapping() {

        final Map<String, Double> gradeMap = new HashMap<>();
        gradeMap.put("A+", Double.valueOf(100));
        gradeMap.put("A", Double.valueOf(95));
        gradeMap.put("A-", Double.valueOf(90));
        gradeMap.put("B+", Double.valueOf(87));
        gradeMap.put("B", Double.valueOf(83));
        gradeMap.put("B-", Double.valueOf(80));
        gradeMap.put("C+", Double.valueOf(77));
        gradeMap.put("C", Double.valueOf(73));
        gradeMap.put("C-", Double.valueOf(70));
        gradeMap.put("D+", Double.valueOf(67));
        gradeMap.put("D", Double.valueOf(63));
        gradeMap.put("D-", Double.valueOf(60));
        gradeMap.put("F", Double.valueOf(0.0));

        return gradeMap;
    }

    private void finalizeNullGradeRecords(final Gradebook gradebook) {

        final Set<String> studentUids = getAllStudentUids(gradebook.getUid());
        final Date now = new Date();
        final String graderId = sessionManager.getCurrentSessionUserId();

        final List<GradebookAssignment> countedAssignments
            = gradingPersistenceManager.getCountedAndGradedAssignmentsForGradebook(gradebook.getId());

        final Map<String, Set<GradebookAssignment>> visible = getVisibleExternalAssignments(gradebook, studentUids, countedAssignments);

        for (final GradebookAssignment assignment : countedAssignments) {
            final List<AssignmentGradeRecord> scoredGradeRecords
                = gradingPersistenceManager.getAllAssignmentGradeRecordsForAssignment(assignment.getId());

            final Map<String, AssignmentGradeRecord> studentToGradeRecordMap = new HashMap<>();
            for (final AssignmentGradeRecord scoredGradeRecord : scoredGradeRecords) {
                studentToGradeRecordMap.put(scoredGradeRecord.getStudentId(), scoredGradeRecord);
            }

            for (String studentUid : studentUids) {
                // SAK-11485 - We don't want to add scores for those grouped activities
                //             that this student should not see or be scored on.
                if (assignment.getExternallyMaintained() && (!visible.containsKey(studentUid) || !visible.get(studentUid).contains(assignment))) {
                    continue;
                }
                AssignmentGradeRecord gradeRecord = studentToGradeRecordMap.get(studentUid);
                if (gradeRecord != null) {
                    if (gradeRecord.getPointsEarned() == null) {
                        gradeRecord.setPointsEarned(0d);
                    } else {
                        continue;
                    }
                } else {
                    gradeRecord = new AssignmentGradeRecord(assignment, studentUid, 0d);
                }
                gradeRecord.setGraderId(graderId);
                gradeRecord.setDateRecorded(now);
                gradingPersistenceManager.saveAssignmentGradeRecord(gradeRecord);
                gradingPersistenceManager.saveGradingEvent(new GradingEvent(assignment, graderId, studentUid, gradeRecord.getPointsEarned()));
            }
        }
    }

    private Map<String, Set<GradebookAssignment>> getVisibleExternalAssignments(final Gradebook gradebook, final Collection<String> studentIds, final List<GradebookAssignment> assignments) {

        final String gradebookUid = gradebook.getUid();
        final Map<String, List<String>> allExternals = getVisibleExternalAssignments(gradebookUid, studentIds);
        final Map<String, GradebookAssignment> allRequested = new HashMap<String, GradebookAssignment>();

        for (final GradebookAssignment a : assignments) {
            if (a.getExternallyMaintained()) {
                allRequested.put(a.getExternalId(), a);
            }
        }

        final Map<String, Set<GradebookAssignment>> visible = new HashMap<String, Set<GradebookAssignment>>();
        for (final String studentId : allExternals.keySet()) {
            if (studentIds.contains(studentId)) {
                final Set<GradebookAssignment> studentAssignments = new HashSet<GradebookAssignment>();
                for (final String assignmentId : allExternals.get(studentId)) {
                    if (allRequested.containsKey(assignmentId)) {
                        studentAssignments.add(allRequested.get(assignmentId));
                    }
                }
                visible.put(studentId, studentAssignments);
            }
        }
        return visible;
    }

    private String getPropertyValue(final String name) {

        // TODO: ADRIAN should be caching these like this?
        String value = this.propertiesMap.get(name);
        if (value == null) {
            final Optional<GradebookProperty> property = gradingPersistenceManager.getGradebookProperty(name);
            if (property.isPresent()) {
                value = property.get().getValue();
                this.propertiesMap.put(name, value);
            }
        }
        return value;
    }

    private void setPropertyValue(final String name, final String value) {

        GradebookProperty property
            = gradingPersistenceManager.getGradebookProperty(name).orElse(new GradebookProperty(name));
        property.setValue(value);
        gradingPersistenceManager.saveGradebookProperty(property);
        this.propertiesMap.put(name, value);
    }

    /**
     * Oracle has a low limit on the maximum length of a parameter list
     * in SQL queries of the form "WHERE tbl.col IN (:paramList)".
     * Since enrollment lists can sometimes be very long, we've replaced
     * such queries with full selects followed by filtering. This helper
     * method filters out unwanted grade records. (Typically they're not
     * wanted because they're either no longer officially enrolled in the
     * course or they're not members of the selected section.)
     */
    private List filterGradeRecordsByStudents(final Collection gradeRecords, final Collection studentUids) {

        final List filteredRecords = new ArrayList();
        for (final Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
            final AbstractGradeRecord agr = (AbstractGradeRecord)iter.next();
            if (studentUids.contains(agr.getStudentId())) {
                filteredRecords.add(agr);
            }
        }
        return filteredRecords;
    }

    private Set<String> getAllStudentUids(final String gradebookUid) {

        final List<EnrollmentRecord> enrollments = sectionAwareness.getSiteMembersInRole(gradebookUid, Role.STUDENT);
        return enrollments.stream().map(e -> e.getUser().getUserUid()).collect(Collectors.toSet());
    }


    private String getUserUid() {
        return sessionManager.getCurrentSessionUserId();
    }

    private void loadAssignmentGradebookAndCategory(GradebookAssignment asn, Long gradebookId, Long categoryId) {

        Gradebook gb = gradingPersistenceManager.getGradebook(gradebookId).get();
        asn.setGradebook(gb);
        if (categoryId != null) {
            asn.setCategory(gradingPersistenceManager.getCategory(categoryId).orElse(null));
        }
    }

    private Long createCategory(Long gradebookId, String name, Double weight, Integer drop_lowest,
                               Integer dropHighest, Integer keepHighest, Boolean is_extra_credit, Boolean is_equal_weight) {
        return createCategory(gradebookId, name, weight, drop_lowest, dropHighest, keepHighest, is_extra_credit, is_equal_weight, null);
    }

    private Long createCategory(Long gradebookId,  String name,  Double weight,  Integer drop_lowest,
                                Integer dropHighest,  Integer keepHighest,  Boolean is_extra_credit,  Boolean is_equal_weight,
                                Integer categoryOrder) throws ConflictingCategoryNameException, StaleObjectModificationException {

        //final Gradebook gb = (Gradebook)session.load(Gradebook.class, gradebookId);
        final Optional<Gradebook> optGb = gradingPersistenceManager.getGradebook(gradebookId);

        final Gradebook gb = optGb.get();

        if (gradingPersistenceManager.isCategoryDefined(name, gb)) {
            throw new ConflictingCategoryNameException("You can not save multiple categories in a gradebook with the same name");
        }
        if (weight > 1 || weight < 0) {
            throw new IllegalArgumentException("weight for category is greater than 1 or less than 0 in createCategory of BaseHibernateManager");
        }
        if (((drop_lowest != null && drop_lowest > 0) || (dropHighest!=null && dropHighest > 0)) && (keepHighest!=null && keepHighest > 0)) {
            throw new IllegalArgumentException("a combination of positive values for keepHighest and either drop_lowest or dropHighest occurred in createCategory of BaseHibernateManager");
        }

        final Category ca = new Category();
        ca.setGradebook(gb);
        ca.setName(name);
        ca.setWeight(weight);
        ca.setDropLowest(drop_lowest);
        ca.setDropHighest(dropHighest);
        ca.setKeepHighest(keepHighest);
        //ca.setItemValue(itemValue);
        ca.setRemoved(false);
        ca.setExtraCredit(is_extra_credit);
        ca.setEqualWeightAssignments(is_equal_weight);
        ca.setCategoryOrder(categoryOrder);

        return gradingPersistenceManager.saveCategory(ca).getId();
    }

    private Double calculateEquivalentPointValueForPercent(final Double doublePointsPossible, final Double doublePercentEarned) {

        if (doublePointsPossible == null || doublePercentEarned == null) {
            return null;
        }

        final BigDecimal pointsPossible = new BigDecimal(doublePointsPossible.toString());
        final BigDecimal percentEarned = new BigDecimal(doublePercentEarned.toString());
        final BigDecimal equivPoints = pointsPossible.multiply(percentEarned.divide(new BigDecimal("100"), GradingConstants.MATH_CONTEXT));
        return equivPoints.doubleValue();
    }

    private List<Comment> getComments(GradebookAssignment assignment, Collection<String> studentIds) {

        if (studentIds.isEmpty()) {
            return Collections.<Comment>emptyList();
        }

        return gradingPersistenceManager.getCommentsForStudents(assignment, studentIds);
    }

    /**
     * Converts points to letter grade for the given AssignmentGradeRecords
     * @param gradebook
     * @param studentRecordsFromDB
     * @return
     */
    private void convertPointsToLetterGrade(Gradebook gradebook, List<AssignmentGradeRecord> studentRecordsFromDB) {

        LetterGradePercentMapping lgpm = getLetterGradePercentMapping(gradebook);
        for (AssignmentGradeRecord agr : studentRecordsFromDB) {
            if (agr != null) {
                Double pointsPossible = agr.getAssignment().getPointsPossible();
                // TODO Adrian: What is this about? Weirddddddd
                agr.setDateRecorded(agr.getDateRecorded());
                agr.setGraderId(agr.getGraderId());
                if (pointsPossible == null || agr.getPointsEarned() == null) {
                    agr.setLetterEarned(null);
                } else {
                    String letterGrade = lgpm.getGrade(calculateEquivalentPercent(pointsPossible, agr.getPointsEarned()));
                    agr.setLetterEarned(letterGrade);
                }
            }
        }
    }

    /**
     *
     * @param name the assignment name (will not be trimmed)
     * @param gradebook the gradebook to check
     * @return true if an assignment with the given name already exists in this gradebook.
     */
    private boolean assignmentNameExists(String name, Gradebook gradebook) {

        return gradingPersistenceManager.countAssignmentsByNameAndGradebookUid(name, gradebook.getUid()) > 0L;
    }

    /**
     *
     * @param doublePointsPossible
     * @param doublePointsEarned
     * @return the % equivalent for the given points possible and points earned
     */
    private Double calculateEquivalentPercent(final Double doublePointsPossible, final Double doublePointsEarned) {

        if (doublePointsEarned == null || doublePointsPossible == null) {
            return null;
        }

        // scale to handle points stored as repeating decimals
        final BigDecimal pointsEarned = new BigDecimal(doublePointsEarned.toString());
        final BigDecimal pointsPossible = new BigDecimal(doublePointsPossible.toString());

        // Avoid dividing by zero
        if (pointsEarned.compareTo(BigDecimal.ZERO) == 0 || pointsPossible.compareTo(BigDecimal.ZERO) == 0) {
            return new Double(0);
        }

        final BigDecimal equivPercent = pointsEarned.divide(pointsPossible, GradingConstants.MATH_CONTEXT).multiply(new BigDecimal("100"));
        return Double.valueOf(equivPercent.doubleValue());

    }

    /**
     * Converts points to percentage for the given AssignmentGradeRecords
     * @param gradebook
     * @param studentRecordsFromDB
     * @return
     */
    private void convertPointsToPercentage(Gradebook gradebook, List<AssignmentGradeRecord> studentRecordsFromDB) {

        for (AssignmentGradeRecord agr : studentRecordsFromDB) {
            if (agr != null) {
                final Double pointsPossible = agr.getAssignment().getPointsPossible();
                if (pointsPossible == null || agr.getPointsEarned() == null) {
                    agr.setPercentEarned(null);
                } else {
                    agr.setDateRecorded(agr.getDateRecorded());
                    agr.setGraderId(agr.getGraderId());
                    agr.setPercentEarned(calculateEquivalentPercent(pointsPossible, agr.getPointsEarned()));
                }
            }
        }
    }
}
