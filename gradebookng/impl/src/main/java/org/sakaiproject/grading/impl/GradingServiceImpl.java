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
import java.util.Arrays;
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
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import org.hibernate.StaleObjectStateException;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
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
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.GradingPermissionService;
import org.sakaiproject.grading.api.GradingPersistenceManager;
import org.sakaiproject.grading.api.GradingScaleDefinition;
import org.sakaiproject.grading.api.GradingSecurityException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.GradingEventStatus;
import org.sakaiproject.grading.api.InvalidCategoryException;
import org.sakaiproject.grading.api.InvalidGradeException;
import org.sakaiproject.grading.api.MessageHelper;
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
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.plus.api.PlusService;
import org.sakaiproject.grading.api.GradingAuthz;
import org.sakaiproject.util.NumberUtil;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class GradingServiceImpl implements GradingService {

    private String gradebookGroupEnabledCache = "org.sakaiproject.tool.gradebook.group.enabled";
    private String gradebookGroupInstancesCache = "org.sakaiproject.tool.gradebook.group.instances";

    public static final String UID_OF_DEFAULT_GRADING_SCALE_PROPERTY = "uidOfDefaultGradingScale";

    public static final String PROP_COURSE_POINTS_DISPLAYED = "gradebook.coursepoints.displayed";
    public static final String PROP_COURSE_GRADE_DISPLAYED = "gradebook.coursegrade.displayed";
    public static final String PROP_ASSIGNMENTS_DISPLAYED = "gradebook.assignments.displayed";
    public static final String PROP_ASSIGNMENT_STATS_DISPLAYED = "gradebook.stats.assignments.displayed";
    public static final String PROP_COURSE_GRADE_STATS_DISPLAYED = "gradebook.stats.coursegrade.displayed";

    @Autowired private AuthzGroupService authzGroupService;
    @Autowired private EventTrackingService eventTrackingService;
    @Autowired private EntityManager entityManager;
    @Autowired private GradingAuthz gradingAuthz;
    @Autowired private GradingPermissionService gradingPermissionService;
    @Autowired private GradingPersistenceManager gradingPersistenceManager;
    @Autowired private MemoryService memoryService;
    @Autowired private PlusService plusService;
    @Autowired private ResourceLoader resourceLoader;
    @Autowired private SiteService siteService;
    @Autowired private SectionAwareness sectionAwareness;
    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;
    @Autowired private ServerConfigurationService serverConfigurationService;

    // Local cache of static-between-deployment properties.
    private Map<String, String> propertiesMap = new HashMap<>();

    public void init() {

        log.debug(buildCacheLogDebug("creatingCache", gradebookGroupEnabledCache));
        memoryService.newCache(gradebookGroupEnabledCache);

        log.debug(buildCacheLogDebug("creatingCache", gradebookGroupInstancesCache));
        memoryService.newCache(gradebookGroupInstancesCache);
    }

    @Override
    public boolean isAssignmentDefined(String gradebookUid, String siteId, String assignmentName) {

        if (!isUserAbleToViewAssignments(siteId)) {
            log.warn("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to check for assignment {}", getUserUid(), gradebookUid,
                    assignmentName);
            throw new GradingSecurityException();
        }
        return getAssignmentWithoutStats(gradebookUid, assignmentName) != null;
    }

    @Override
    public boolean isUserAbleToViewAssignments(String siteId) {

        return (gradingAuthz.isUserAbleToEditAssessments(siteId) || gradingAuthz.isUserAbleToGrade(siteId));
    }

    private boolean isUserAbleToGradeItemForStudent(String gradebookUid, String siteId, Long itemId, String studentUid) {

        return gradingAuthz.isUserAbleToGradeItemForStudent(gradebookUid, siteId, itemId, studentUid);
    }

    private boolean isUserAbleToViewItemForStudent(String gradebookUid, String siteId, Long itemId, String studentUid) {

        return gradingAuthz.isUserAbleToViewItemForStudent(gradebookUid, siteId, itemId, studentUid);
    }

    @Override
    public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, String siteId, Long itemId, String studentUid) {

        return gradingAuthz.getGradeViewFunctionForUserForStudentForItem(gradebookUid, siteId, itemId, studentUid);
    }

    @Override
    @Transactional
    public List<Assignment> getAssignments(String gradebookUid, String siteId, SortType sortBy) {

        if (!isUserAbleToViewAssignments(siteId)) {
            log.warn("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to get assignments list", getUserUid(), gradebookUid);
            throw new GradingSecurityException();
        }

        Gradebook gradebook = getGradebook(gradebookUid);
        // Determine whether this gradebook uses Categories Only or Weighted Categories by checking category type.
        // We will avoid adding any legacy category information on the individual gb items if the instructor is no
        // longer using categories in the gradebook.
        final boolean gbUsesCategories = !Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY);

        List<GradebookAssignment> internalAssignments = getAssignments(gradebook.getId());

        return sortAssignments(internalAssignments, sortBy, true)
            .stream().map(ga -> getAssignmentDefinition(ga, gbUsesCategories)).collect(Collectors.toList());
    }

    @Override
    public Assignment getAssignment(String gradebookUid, String siteId, Long assignmentId) throws AssessmentNotFoundException {

        if (assignmentId == null || gradebookUid == null) {
            throw new IllegalArgumentException("null parameter passed to getAssignment. Values are assignmentId:"
                    + assignmentId + " gradebookUid:" + gradebookUid);
        }
        if (!isUserAbleToViewAssignments(siteId) && !currentUserHasViewOwnGradesPerm(siteId)) {
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
    @Deprecated // used on external tools, it will always be a site=id situation until SAK-49493 is completed
    public Assignment getAssignment(String gradebookUid, String siteId, String assignmentName) throws AssessmentNotFoundException {

        if (assignmentName == null || gradebookUid == null) {
            throw new IllegalArgumentException("null parameter passed to getAssignment. Values are assignmentName:"
                    + assignmentName + " gradebookUid:" + gradebookUid);
        }
        if (!isUserAbleToViewAssignments(siteId) && !currentUserHasViewOwnGradesPerm(siteId)) {
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
    }

    public List<String> getGradebookUidByExternalId(String externalId) {

        List<GradebookAssignment> optAsn = gradingPersistenceManager.getGradebookUidByExternalId(externalId);
        if (optAsn.isEmpty()) {
            return new ArrayList<>();
        }

        return optAsn.stream()
            .map(a -> a.getGradebook().getUid())
            .collect(Collectors.toList());
    }

    @Override
    public Assignment getAssignmentByNameOrId(String gradebookUid, String siteId, String assignmentName) throws AssessmentNotFoundException {

        Assignment assignment = null;
        try {
            assignment = getAssignment(gradebookUid, siteId, assignmentName);
        } catch (AssessmentNotFoundException e) {
            // Don't fail on this exception
            log.debug("Assessment not found by name", e);
        }

        if (assignment == null) {
            // Try to get the assignment by id
            if (NumberUtils.isCreatable(assignmentName)) {
                final Long assignmentId = NumberUtils.toLong(assignmentName, -1L);
                return getAssignment(gradebookUid, siteId, assignmentId);
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
        assignmentDefinition.setReference(internalAssignment.getReference());
        assignmentDefinition.setContext(internalAssignment.getGradebook().getUid());
        if (internalAssignment.getGradebook().getGradeType() != GradeType.LETTER) {
            assignmentDefinition.setPoints(internalAssignment.getPointsPossible());
        } else {
            getMaxLetterGrade(internalAssignment.getGradebook().getSelectedGradeMapping().getGradeMap()).ifPresent(assignmentDefinition::setMaxLetterGrade);
            getMaxPoints(internalAssignment.getGradebook().getSelectedGradeMapping().getGradeMap()).ifPresent(assignmentDefinition::setPoints);
        }
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
        assignmentDefinition.setLineItem(internalAssignment.getLineItem());

        return assignmentDefinition;
    }

    private Long createAssignment(Long gradebookId, String name, Double points, Date dueDate, Boolean isNotCounted,
        Boolean isReleased, Boolean isExtraCredit, Integer sortOrder,
        Assignment assignmentDefinition)
            throws ConflictingAssignmentNameException, StaleObjectModificationException {

        return createNewAssignment(gradebookId, null, name, points, dueDate, isNotCounted, isReleased, isExtraCredit, sortOrder, null, assignmentDefinition);
    }

    private Long createAssignmentForCategory(Long gradebookId, Long categoryId, String name, Double points, Date dueDate, Boolean isNotCounted,
        Boolean isReleased, Boolean isExtraCredit, Integer categorizedSortOrder, Assignment assignmentDefinition)
            throws ConflictingAssignmentNameException, StaleObjectModificationException, IllegalArgumentException {

        if (gradebookId == null || categoryId == null) {
            throw new IllegalArgumentException("gradebookId or categoryId is null in createAssignmentForCategory");
        }

        return createNewAssignment(gradebookId, categoryId, name, points, dueDate, isNotCounted, isReleased, isExtraCredit, null, categorizedSortOrder, assignmentDefinition);
    }

    private Long createNewAssignment(final Long gradebookId, final Long categoryId, final String name, final Double points, final Date dueDate, final Boolean isNotCounted,
            final Boolean isReleased, final Boolean isExtraCredit, final Integer sortOrder, final Integer categorizedSortOrder, Assignment assignmentDefinition)
                    throws ConflictingAssignmentNameException, StaleObjectModificationException {

        GradebookAssignment asn = prepareNewAssignment(name, points, dueDate, isNotCounted, isReleased, isExtraCredit, sortOrder, categorizedSortOrder, assignmentDefinition);
        return saveNewAssignment(gradebookId, categoryId, asn);
    }

    private GradebookAssignment prepareNewAssignment(final String name, final Double points, final Date dueDate, final Boolean isNotCounted, final Boolean isReleased,
            final Boolean isExtraCredit, final Integer sortOrder, final Integer categorizedSortOrder, Assignment assignmentDefinition) {

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

        // Add things not include in the calling sequence
        if ( assignmentDefinition != null ) {
            asn.setExternallyMaintained(assignmentDefinition.getExternallyMaintained());
            asn.setExternalId(assignmentDefinition.getExternalId());
            asn.setExternalAppName(assignmentDefinition.getExternalAppName());
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



    public void updateGradebook(final Gradebook gradebook, final String siteId) throws StaleObjectModificationException {

        // Get the gradebook and selected mapping from persistence
        final Gradebook gradebookFromPersistence = gradingPersistenceManager.getGradebook(gradebook.getId()).orElse(null);
        final GradeMapping mappingFromPersistence = gradebookFromPersistence.getSelectedGradeMapping();

        // If the mapping has changed, and there are explicitly entered
        // course grade records, disallow this update.
        if (!mappingFromPersistence.getId().equals(gradebook.getSelectedGradeMapping().getId())) {
            if (hasExplicitlyEnteredCourseGradeRecords(gradebook.getId(), siteId)) {
                throw new IllegalStateException("Selected grade mapping can not be changed, since explicit course grades exist.");
            }
        }

        // TODO Adrian - 'this is a bit janky. I don't like this at all.
        try {
            gradingPersistenceManager.saveGradebook(gradebook);
        } catch (final StaleObjectStateException e) {
            throw new StaleObjectModificationException(e);
        }
    }

    private boolean hasExplicitlyEnteredCourseGradeRecords(final Long gradebookId, final String siteId) {

        final Set<String> studentIds = getAllStudentUids(siteId);

        if (studentIds.isEmpty()) {
            return false;
        }

        return gradingPersistenceManager.hasCourseGradeRecordEntries(gradebookId, studentIds);
    }

    public Optional<String> getMaxLetterGrade(Map<String, Double> gradeMap) {

        return gradeMap.entrySet()
            .stream().max(Comparator.comparingDouble((Map.Entry<String, Double> e) -> e.getValue()))
            .map(Map.Entry::getKey);
    }

    public Optional<Double> getMaxPoints(Map<String, Double> gradeMap) {

        return gradeMap.entrySet()
            .stream().max(Comparator.comparingDouble((Map.Entry<String, Double> e) -> e.getValue()))
            .map(Map.Entry::getValue);
    }


    @Override
    public GradeDefinition getGradeDefinitionForStudentForItem(final String gradebookUid, final String siteId, final Long assignmentId, final String studentUid) {

        if (gradebookUid == null || assignmentId == null || studentUid == null) {
            throw new IllegalArgumentException("Null paraemter passed to getGradeDefinitionForStudentForItem");
        }

        // studentId can be a groupId (from Assignments)
        final boolean studentRequestingOwnScore = sessionManager.getCurrentSessionUserId().equals(studentUid)
                || isCurrentUserFromGroup(siteId, studentUid);

        final GradebookAssignment assignment = getAssignmentWithoutStatsByID(gradebookUid, assignmentId);

        if (assignment == null) {
            throw new AssessmentNotFoundException(
                    "There is no assignment with the assignmentId " + assignmentId + " in gradebook " + gradebookUid);
        }

        if (!studentRequestingOwnScore && !isUserAbleToViewItemForStudent(gradebookUid, siteId, assignment.getId(), studentUid)) {
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

                gradeDef.setExcused(BooleanUtils.toBoolean(gradeRecord.getExcludedFromGrade()));

                switch (gradebook.getGradeType()) {
                    case POINTS:
                        if (gradeRecord.getPointsEarned() != null) {
                            gradeDef.setGrade(gradeRecord.getPointsEarned().toString());
                        }
                        break;
                    case PERCENTAGE:
                        Double percent = calculateEquivalentPercent(assignment.getPointsPossible(),
                                gradeRecord.getPointsEarned());
                        if (percent != null) {
                            gradeDef.setGrade(percent.toString());
                        }
                        break;
                    case LETTER:
                        if (gradeRecord.getLetterEarned() != null) {
                            gradeDef.setGrade(gradeRecord.getLetterEarned());
                        }
                        break;
                    default:
                        gradeDef.setGrade(null);
                        log.warn("Unknown grade type: {}", gradebook.getGradeType());
                }
            }
        }

        log.debug("returning grade def for {}", studentUid);
        return gradeDef;
    }

    @Override
    public GradebookInformation getGradebookInformation(final String gradebookUid, final String siteId) {

        if (gradebookUid == null) {
            throw new IllegalArgumentException("null gradebookUid " + gradebookUid);
        }

        if (!currentUserHasEditPerm(siteId) && !currentUserHasGradingPerm(siteId)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to access gb information", getUserUid(), gradebookUid);
            throw new GradingSecurityException();
        }

        final Gradebook gradebook = getGradebook(gradebookUid);
        if (gradebook == null) {
            throw new IllegalArgumentException("Their is no gradebook associated with this Id: " + gradebookUid);
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
        rval.setCategories(getCategoryDefinitions(gradebookUid, siteId));

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
            final List<Assignment> assignments, final String toGradebookUid, final String fromContext, final List<String> options) {

        boolean copySettings = (options != null && options.contains(EntityTransferrer.COPY_SETTINGS_OPTION));
        boolean copyOnlySettings = (options != null && options.contains(EntityTransferrer.COPY_ONLY_SETTINGS_PSEUDO_OPTION)); // do not copy any gb items if true
        final Map<String, String> transversalMap = new HashMap<>();

        final Gradebook gradebook = getGradebook(toGradebookUid);
        if (copySettings) {
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

            updateGradebook(gradebook, toGradebookUid);
        }

        // all categories that we need to end up with
        final List<CategoryDefinition> categories = gradebookInformation.getCategories();

        // filter out externally managed assignments. These are never imported.
        assignments.removeIf(a -> a.getExternallyMaintained());

        // this map holds the names of categories that have been created in the site to the category ids
        // and is updated as we go along
        // likewise for list of assignments
        final Map<String, Long> categoriesCreated = new HashMap<>();
        final List<String> assignmentsCreated = new ArrayList<>();

        if (!categories.isEmpty() && !Objects.equals(gradebookInformation.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY)) {

            Integer toCategoryType = gradebook.getCategoryType();

            // conditionally migrate the categories with assignments
            categories.forEach(c -> {

                assignments.forEach(a -> {
                    int dash = a.getName().lastIndexOf('-');
                    String taskName = isGradebookGroupEnabled(fromContext) && dash >= 0
                            ? a.getName().substring(dash + 1)
                            : a.getName();

                    if (copySettings && StringUtils.equals(c.getName(), a.getCategoryName())
                        && !Objects.equals(toCategoryType, GradingConstants.CATEGORY_TYPE_NO_CATEGORY)) {

                        if (!categoriesCreated.containsKey(c.getName())) {
                            // create category
                            Long categoryId = null;
                            try {
				Double weight = Objects.equals(toCategoryType, GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY)
					 ? (c.getWeight() != null ? c.getWeight() : Double.valueOf(0.0))
					 : Double.valueOf(0.0);
                                categoryId = createCategory(gradebook.getId(), c.getName(), weight, c.getDropLowest(),
                                        c.getDropHighest(), c.getKeepHighest(), c.getExtraCredit(), c.getEqualWeight(), c.getCategoryOrder());
                            } catch (final ConflictingCategoryNameException e) {
                                // category already exists. Could be from a merge.
                                log.info("Category: {} already exists in target site. Skipping creation.", c.getName());
                            }

                            if (categoryId == null) {
                                // couldn't create so look up the id in the target site
                                final List<CategoryDefinition> existingCategories = getCategoryDefinitions(gradebook.getUid(), gradebook.getUid());
                                categoryId = existingCategories.stream().filter(e -> StringUtils.equals(e.getName(), c.getName()))
                                        .findFirst().get().getId();
                            }
                            // record that we have created this category
                            categoriesCreated.put(c.getName(), categoryId);
                        }

			if (!copyOnlySettings) {
				// create the assignment for the current category
				try {
					Long newId = createAssignmentForCategory(gradebook.getId(), categoriesCreated.get(c.getName()), taskName, a.getPoints(),
										 a.getDueDate(), !a.getCounted(), a.getReleased(), a.getExtraCredit(), a.getCategorizedSortOrder(), null);
					transversalMap.put("gb/"+a.getId(),"gb/"+newId);
				} catch (final ConflictingAssignmentNameException e) {
					// assignment already exists. Could be from a merge.
					log.info("GradebookAssignment: {} already exists in target site. Skipping creation.", taskName);
				} catch (final Exception ex) {
					log.warn("GradebookAssignment: exception {} trying to create {} in target site. Skipping creation.", ex.getMessage(), taskName);
				}

				// record that we have created this assignment
				assignmentsCreated.add(taskName);
			}
                    }
                });
            });

            // create any remaining categories that have no assignments
            if (copySettings && !Objects.equals(toCategoryType, GradingConstants.CATEGORY_TYPE_NO_CATEGORY)) {
                categories.removeIf(c -> categoriesCreated.containsKey(c.getName()));
                categories.forEach(c -> {
                    try {
			    Double weight = Objects.equals(toCategoryType, GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY)
					 ? c.getWeight()
					 : Double.valueOf(0.0);
                        createCategory(gradebook.getId(), c.getName(), weight, c.getDropLowest(), c.getDropHighest(), c.getKeepHighest(),
                                c.getExtraCredit(), c.getEqualWeight(), c.getCategoryOrder());
                    } catch (final ConflictingCategoryNameException e) {
                        // category already exists. Could be from a merge.
                        log.info("Category: {} already exists in target site. Skipping creation.", c.getName());
                    }
                });
            }
        }

	if (!copyOnlySettings) {
		// create any remaining assignments that have no categories
		assignments.removeIf(a -> {
				String taskName;
				if (isGradebookGroupEnabled(fromContext)) {
					final int dash = a.getName().lastIndexOf('-');
					taskName = (dash >= 0) ? a.getName().substring(dash + 1) : a.getName();
				} else {
					taskName = a.getName();
				}

				return assignmentsCreated.contains(taskName);
			});
		assignments.forEach(a -> {
				int dash = a.getName().lastIndexOf('-');
				String taskName = isGradebookGroupEnabled(fromContext) && dash >= 0
					? a.getName().substring(dash + 1)
					: a.getName();

				try {
					Long newId = createAssignment(gradebook.getId(), taskName, a.getPoints(), a.getDueDate(), !a.getCounted(), a.getReleased(), a.getExtraCredit(), a.getSortOrder(), null);
					transversalMap.put("gb/"+a.getId(),"gb/"+newId);
				} catch (final ConflictingAssignmentNameException e) {
					// assignment already exists. Could be from a merge.
					log.info("GradebookAssignment: {} already exists in target site. Skipping creation.", taskName);
				} catch (final Exception ex) {
					log.warn("GradebookAssignment: exception {} trying to create {} in target site. Skipping creation.", ex.getMessage(), taskName);
				}
			});
	}

        if (copySettings) {
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
                        updateGradebook(gradebook, toGradebookUid);
                        log.debug("Merge to gradebook {} updated grade mapping", toGradebookUid);

                        break MERGE_GRADE_MAPPING;
                    }
                }
                // Did not find a matching grading scale.
                log.info("Merge to gradebook {} skipped grade mapping change because grading scale {} is not defined", toGradebookUid,
                        fromGradingScaleUid);
            }
        }

        return transversalMap;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void removeAssignment(Long assignmentId) throws StaleObjectModificationException {

        gradingPersistenceManager.getAssignmentById(assignmentId).ifPresentOrElse(a -> {
            a.setRemoved(true);
            Gradebook gradebook = a.getGradebook();
            gradingPersistenceManager.saveAssignment(a);
            log.info("GradebookAssignment {} has been removed from {}", a.getName(), gradebook);
        }, () -> log.warn("No assignment for id {}", assignmentId));
    }

    @Override
    public Long addAssignment(final String gradebookUid, final String siteId, Assignment assignmentDefinition) {

        if (!gradingAuthz.isUserAbleToEditAssessments(siteId)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to add an assignment", getUserUid(), gradebookUid);
            throw new GradingSecurityException();
        }
        final Gradebook gradebook = getGradebook(gradebookUid);

        // When creating/editing LETTER gradebooks we don't allow a user to specify points, so we do it here.
        if (gradebook.getGradeType() == GradeType.LETTER) {
            getMaxLetterGrade(gradebook.getSelectedGradeMapping().getGradeMap()).ifPresent(assignmentDefinition::setMaxLetterGrade);
            getMaxPoints(gradebook.getSelectedGradeMapping().getGradeMap()).ifPresent(assignmentDefinition::setPoints);
        }

        final String validatedName = GradebookHelper.validateAssignmentNameAndPoints(assignmentDefinition, gradebook.getGradeType());

        Long assignmentId = null;
        // if attaching to category
        if (assignmentDefinition.getCategoryId() != null) {
            assignmentId = createAssignmentForCategory(gradebook.getId(), assignmentDefinition.getCategoryId(), validatedName,
                    assignmentDefinition.getPoints(), assignmentDefinition.getDueDate(), !assignmentDefinition.getCounted(), assignmentDefinition.getReleased(),
                    assignmentDefinition.getExtraCredit(), assignmentDefinition.getCategorizedSortOrder(),
                    assignmentDefinition);
        } else {
            assignmentId = createAssignment(gradebook.getId(), validatedName, assignmentDefinition.getPoints(), assignmentDefinition.getDueDate(),
                !assignmentDefinition.getCounted(), assignmentDefinition.getReleased(), assignmentDefinition.getExtraCredit(), assignmentDefinition.getSortOrder(),
                assignmentDefinition);
        }


        // Check if this ia a plus course
        if ( plusService.enabled() && isCurrentGbSite(gradebookUid)) {
            try {
                final Site site = this.siteService.getSite(gradebookUid);
                if ( plusService.enabled(site) ) {

                    String lineItem = plusService.createLineItem(site, assignmentId, assignmentDefinition);
                    log.debug("Lineitem={} created assignment={} gradebook={}", lineItem, assignmentId, gradebookUid);

                    // Update the assignment with the new lineItem
                    final GradebookAssignment assignment = getAssignmentWithoutStatsByID(gradebookUid, assignmentId);
                    if (assignment == null) {
                        throw new AssessmentNotFoundException(
                                "There is no assignment with id " + assignmentId + " in gradebook " + gradebookUid);
                    }
                    assignment.setLineItem(lineItem);
                    updateAssignment(assignment);
                }
            } catch (Exception e) {
                log.error("Could not load site associated with gradebook - lineitem not created", e);
            }
        }

        return assignmentId;

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void updateAssignment(final String gradebookUid, final String siteId, final Long assignmentId, final Assignment assignmentDefinition) {

        if (!gradingAuthz.isUserAbleToEditAssessments(siteId)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to change the definition of assignment {}", getUserUid(),
                    gradebookUid, assignmentId);
            throw new GradingSecurityException();
        }

        final Gradebook gradebook = this.getGradebook(gradebookUid);

        final String validatedName = GradebookHelper.validateAssignmentNameAndPoints(assignmentDefinition, gradebook.getGradeType());

        final GradebookAssignment assignment = getAssignmentWithoutStatsByID(gradebookUid, assignmentId);
        if (assignment == null) {
            throw new AssessmentNotFoundException(
                    "There is no assignment with id " + assignmentId + " in gradebook " + gradebookUid);
        }

        // check if we need to scale the grades
        boolean scaleGrades = false;
        final Double originalPointsPossible = assignment.getPointsPossible();
        if (GradeType.PERCENTAGE == gradebook.getGradeType()
                && !assignment.getPointsPossible().equals(assignmentDefinition.getPoints())) {
            scaleGrades = true;
        }

        if (GradeType.POINTS == gradebook.getGradeType() && assignmentDefinition.getScaleGrades()) {
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

        assignment.setLineItem(assignmentDefinition.getLineItem());

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

        // Check if this is a plus course
        if ( plusService.enabled() && isCurrentGbSite(gradebookUid)) {
            try {
                final Site site = this.siteService.getSite(gradebookUid);
                if ( plusService.enabled(site) ) {
                    log.debug("Lineitem updated={} created assignment={} gradebook={}", assignmentDefinition.getLineItem(), assignment.getId(), gradebookUid);
                    plusService.updateLineItem(site, assignmentDefinition);
                }
            } catch (Exception e) {
                log.error("Could not load site associated with gradebook - lineitem not updated", e);
            }
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
        final List<GradebookAssignment> countedAssigns = getCountedAssignments(gradebook)
            .stream().filter(a -> a.isIncludedInCalculations()).collect(Collectors.toList());

        for (CourseGradeRecord cgr : records) {
            final List<AssignmentGradeRecord> studentGradeRecs = gradeRecMap.get(cgr.getStudentId());

            applyDropScores(studentGradeRecs, gradebook.getGradeType(), gradebook.getCategoryType(), gradebook.getSelectedGradeMapping().getGradeMap());
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

        final GradeType gradeType = gradebook.getGradeType();
        if (gradeType != GradeType.POINTS && gradeType != GradeType.PERCENTAGE && gradeType != GradeType.LETTER) {
            log.error("Wrong grade type in GradebookCalculationImpl.getTotalPointsInternal");
            return -1;
        }

        if (studentGradeRecs == null || countedAssigns == null) {
            log.debug("Returning 0 from getTotalPointsInternal since studentGradeRecs or countedAssigns was null");
            return 0;
        }

        double totalPointsPossible = 0;

        final Set<GradebookAssignment> countedSet = new HashSet<>(countedAssigns);

        // we need to filter this list to identify only "counted" grade recs
        final List<AssignmentGradeRecord> countedGradeRecs = new ArrayList<>();
        for (AssignmentGradeRecord gradeRec : studentGradeRecs) {
            GradebookAssignment assign = gradeRec.getAssignment();
            boolean extraCredit = assign.getExtraCredit();
            if (!Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY) && assign.getCategory() != null
                    && assign.getCategory().getExtraCredit()) {
                extraCredit = true;
            }

            boolean excused = BooleanUtils.toBoolean(gradeRec.getExcludedFromGrade());
            if (assign.getCounted() && !assign.getUngraded() && !assign.getRemoved() && countedSet.contains(assign) &&
                    (assign.getPointsPossible() != null && assign.getPointsPossible() > 0) && !gradeRec.getDroppedFromGrade() && !extraCredit
                    && !excused) {
                countedGradeRecs.add(gradeRec);
            }
        }

        final Set<Long> assignmentsTaken = new HashSet<>();
        final Set<Long> categoryTaken = new HashSet<>();
        for (final AssignmentGradeRecord gradeRec : countedGradeRecs) {
            boolean gradeAssigned = gradeType == GradeType.LETTER ? gradeRec.getLetterEarned() != null : gradeRec.getPointsEarned() != null;
            if (gradeAssigned) {
                final GradebookAssignment gradingItem = gradeRec.getAssignment();
                if (Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY)) {
                    assignmentsTaken.add(gradingItem.getId());
                } else if ((Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_ONLY_CATEGORY)
                        || Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY))
                        && gradingItem != null && categories != null) {

                    for (Category category : categories) {
                        if (category != null && !category.getRemoved() && gradingItem.getCategory() != null
                                && category.getId().equals(gradingItem.getCategory().getId())
                                && ((category.getExtraCredit() != null && !category.getExtraCredit()) || category.getExtraCredit() == null)) {
                            assignmentsTaken.add(gradingItem.getId());
                            categoryTaken.add(category.getId());
                            break;
                        }
                    }
                }
            }
        }

        if (!assignmentsTaken.isEmpty()) {
            if (!literalTotal && Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY)) {
                for (Category category : categories) {
                    if (category != null && !category.getRemoved() && categoryTaken.contains(category.getId())) {
                        totalPointsPossible += category.getWeight();
                    }
                }
                return totalPointsPossible;
            }

            Double mappingMaxPointsPossible = getMaxPoints(gradebook.getSelectedGradeMapping().getGradeMap()).orElse(0D);

            for (GradebookAssignment gradingItem : countedAssigns) {
                if (gradingItem == null) continue;

                Double pointsPossible = gradingItem.getPointsPossible();

                if (Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY)
                        && assignmentsTaken.contains(gradingItem.getId())) {
                    totalPointsPossible += pointsPossible;
                } else if (Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_ONLY_CATEGORY)
                        && assignmentsTaken.contains(gradingItem.getId())) {
                    totalPointsPossible += pointsPossible;
                } else if (literalTotal && Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY)
                        && assignmentsTaken.contains(gradingItem.getId())) {
                    totalPointsPossible += pointsPossible;
                }
            }
        } else {
            totalPointsPossible = -1;
        }

        return totalPointsPossible;
    }

    private List<Double> getTotalPointsEarnedInternal(final String studentId, final Gradebook gradebook, final List<Category> categories,
        final List<AssignmentGradeRecord> gradeRecs, final List<GradebookAssignment> countedAssigns) {

        final GradeType gradeType = gradebook.getGradeType();
        if (gradeType != GradeType.POINTS && gradeType != GradeType.PERCENTAGE && gradeType != GradeType.LETTER) {
            log.error("Wrong grade type in GradebookCalculationImpl.getTotalPointsEarnedInternal");
            return Collections.emptyList();
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

        final Map<Long, BigDecimal> categoryScoreMap = new HashMap<>();
        final Map<Long, BigDecimal> categoryTotalScoreMap = new HashMap<>();
        final Set<Long> assignmentsTaken = new HashSet<>();

        for (final AssignmentGradeRecord gradeRec : gradeRecs) {
            final boolean excused = BooleanUtils.toBoolean(gradeRec.getExcludedFromGrade());

            boolean counted = !gradeRec.getDroppedFromGrade()
                && (gradeType == GradeType.LETTER ? StringUtils.isNotBlank(gradeRec.getLetterEarned()) : gradeRec.getPointsEarned() != null);
            if (counted) {
                final GradebookAssignment gradingItem = gradeRec.getAssignment();
                if (gradingItem.isIncludedInCalculations() && countedAssigns.contains(gradingItem)) {
                    BigDecimal pointsEarned = gradeType == GradeType.LETTER ? null : BigDecimal.valueOf(gradeRec.getPointsEarned());
                    BigDecimal pointsPossible = BigDecimal.valueOf(gradingItem.getPointsPossible());
                    if (gradeType == GradeType.LETTER) {
                        LetterGradePercentMapping gradeMapping = getLetterGradePercentMapping(gradebook);
                        pointsEarned = BigDecimal.valueOf(gradebook.getSelectedGradeMapping().getGradeMap().get(gradeRec.getLetterEarned()));
                    }

                    if (Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY)) {
                        if (!excused) {
                            totalPointsEarned = totalPointsEarned.add(pointsEarned, GradingService.MATH_CONTEXT);
                            literalTotalPointsEarned = pointsEarned.add(literalTotalPointsEarned, GradingService.MATH_CONTEXT);
                            assignmentsTaken.add(gradingItem.getId());
                        }
                    } else if (Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_ONLY_CATEGORY) && gradingItem != null) {
                        if (!excused) {
                            totalPointsEarned = totalPointsEarned.add(pointsEarned, GradingService.MATH_CONTEXT);
                            literalTotalPointsEarned = pointsEarned.add(literalTotalPointsEarned, GradingService.MATH_CONTEXT);
                            assignmentsTaken.add(gradingItem.getId());
                        }
                    } else if (Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY) && gradingItem != null
                            && categories != null) {
                        for (Category category : categories) {
                            if (category != null && !category.getRemoved() && gradingItem.getCategory() != null
                                    && category.getId().equals(gradingItem.getCategory().getId())) {
                                if (!excused) {
                                    assignmentsTaken.add(gradingItem.getId());
                                    literalTotalPointsEarned = pointsEarned.add(literalTotalPointsEarned, GradingService.MATH_CONTEXT);

                                    // If category is equal weight, manipulate points to be the average
                                    if (category.getEqualWeightAssignments()) {
                                        pointsEarned = pointsEarned.divide(pointsPossible, GradingService.MATH_CONTEXT);
                                    }

                                    if (categoryScoreMap.get(category.getId()) != null) {
                                        categoryScoreMap.put(category.getId(), ((BigDecimal)categoryScoreMap.get(category.getId())).add(pointsEarned, GradingService.MATH_CONTEXT));
                                    } else {
                                        categoryScoreMap.put(category.getId(), pointsEarned);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (!categories.isEmpty() && Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY)) {
            for (GradebookAssignment asgn : countedAssigns) {
                BigDecimal pointsPossible = new BigDecimal(asgn.getPointsPossible());

                if (assignmentsTaken.contains(asgn.getId())) {
                    for (Category category : categories) {
                        if (category != null && !category.getRemoved() && asgn.getCategory() != null
                                && category.getId().equals(asgn.getCategory().getId()) && !asgn.getExtraCredit()) {

                            // If it's equal-weight category, just want to divide averages by number of items
                            if (category.getEqualWeightAssignments()) {
                                pointsPossible = new BigDecimal("1");
                            }

                            if (categoryTotalScoreMap.get(category.getId()) == null) {
                                categoryTotalScoreMap.put(category.getId(), pointsPossible);
                            } else {
                                categoryTotalScoreMap.put(category.getId(),
                                        ((BigDecimal) categoryTotalScoreMap.get(category.getId())).add(pointsPossible));
                            }
                        }
                    }
                }
            }
        }

        if (assignmentsTaken.isEmpty()) {
            totalPointsEarned = new BigDecimal(-1);
        }

        if (Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY)) {
            for (Category category : categories) {
                if (category != null && !category.getRemoved() && categoryScoreMap.get(category.getId()) != null
                        && categoryTotalScoreMap.get(category.getId()) != null) {
                    if (category.getExtraCredit()) {
                        extraPointsEarned = extraPointsEarned.add(((BigDecimal) categoryScoreMap.get(category.getId())).multiply(new BigDecimal(category.getWeight()), GradingService.MATH_CONTEXT)
                                .divide((BigDecimal) categoryTotalScoreMap.get(category.getId()), GradingService.MATH_CONTEXT));
                    }
                    else {
                        totalPointsEarned = totalPointsEarned.add(((BigDecimal) categoryScoreMap.get(category.getId())).multiply(new BigDecimal(category.getWeight()), GradingService.MATH_CONTEXT)
                                .divide((BigDecimal) categoryTotalScoreMap.get(category.getId()), GradingService.MATH_CONTEXT));
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
     */
    private Gradebook getGradebook(Long id) {

        return gradingPersistenceManager.getGradebook(id).orElse(null);
    }

    private List<GradebookAssignment> getAssignmentsCounted(Long gradebookId) {

        return gradingPersistenceManager.getCountedAssignmentsForGradebook(gradebookId);
    }

    private List<AssignmentGradeRecord> getAllAssignmentGradeRecordsForGbItem(Long gradableObjectId, Collection studentUids) {

        if (studentUids.isEmpty()) {
            // If there are no enrollments, no need to execute the query.
            log.debug("getAllAssignmentGradeRecordsForGbItem: No enrollments were specified.  Returning an empty List of grade records");
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
            log.debug("getAllAssignmentGradeRecordsForGbItems: No enrollments were specified. Returning an empty List of grade records");
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
     */
    private List<GradebookAssignment> getSortedAssignments(Long gradebookId, SortType sortBy, boolean ascending) {

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

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<Assignment> getViewableAssignmentsForCurrentUser(final String gradebookUid, final String siteId, final SortType sortBy) {

        if (!gradingAuthz.isUserAbleToGradeAll(siteId)
                && !gradingAuthz.isUserAbleToGrade(siteId)
                && !gradingAuthz.isUserAbleToViewOwnGrades(siteId)) {
            return Collections.<Assignment>emptyList();
        }

        List<GradebookAssignment> viewableAssignments = new ArrayList<>();
        final LinkedHashSet<Assignment> assignmentsToReturn = new LinkedHashSet<>();

        final Gradebook gradebook = getGradebook(gradebookUid);

        // will send back all assignments if user can grade all
        if (gradingAuthz.isUserAbleToGradeAll(siteId)) {
            viewableAssignments = getSortedAssignments(gradebook.getId(), sortBy, true);
        } else if (gradingAuthz.isUserAbleToGrade(siteId)) {
            // if user can grade and doesn't have grader perm restrictions, they
            // may view all assigns
            if (!gradingPermissionService.currentUserHasGraderPermissions(gradebookUid)) {
                viewableAssignments = getSortedAssignments(gradebook.getId(), sortBy, true);
            } else {
                // this user has grader perms, so we need to filter the items returned
                // if this gradebook has categories enabled, we need to check for category-specific restrictions
                if (Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY)) {
                    assignmentsToReturn.addAll(getAssignments(gradebookUid, siteId, sortBy));
                } else {
                    final String userUid = getUserUid();
                    if (gradingPermissionService.getPermissionForUserForAllAssignment(gradebook.getId(), userUid)) {
                        assignmentsToReturn.addAll(getAssignments(gradebookUid, siteId, sortBy));
                    } else {
                        final List<Assignment> assignments = getAssignments(gradebookUid, siteId, sortBy);
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
        } else if (gradingAuthz.isUserAbleToViewOwnGrades(siteId)) {
            // if user is just a student, we need to filter out unreleased items
            final List<GradebookAssignment> allAssignments = getSortedAssignments(gradebook.getId(), sortBy, true);
            final String currentUserId = sessionManager.getCurrentSessionUserId();

            // Pre-filter external assignments for visibility in batch
            final Map<String, Boolean> externalVisibilityMap = getBatchExternalAssignmentVisibility(
                gradebook.getUid(), allAssignments, currentUserId);

            for (GradebookAssignment assign : allAssignments) {
                if (assign.isExternallyMaintained()) {
                    // Use pre-computed visibility result
                    Boolean isVisible = externalVisibilityMap.get(assign.getExternalId());
                    if (isVisible == null || !isVisible) {
                        continue;
                    }
                }

                if (assign.getReleased()) {
                    viewableAssignments.add(assign);
                }
            }
        }

        // Now we need to convert these to the assignment template objects
        if (!viewableAssignments.isEmpty()) {
            boolean usesCategories = !Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY);
            assignmentsToReturn.addAll(viewableAssignments.stream().collect(Collectors.mapping(a -> getAssignmentDefinition(a, usesCategories), Collectors.toList())));
        }

        return new ArrayList<>(assignmentsToReturn);
    }

    /**
     * Batch check external assignment visibility to avoid N+1 queries.
     * Uses the provider's getAllExternalAssignments method when available for better performance.
     */
    private Map<String, Boolean> getBatchExternalAssignmentVisibility(String gradebookUid,
            List<GradebookAssignment> assignments, String userId) {

        Map<String, Boolean> visibilityMap = new HashMap<>();

        // Group external assignments by app name
        Map<String, List<GradebookAssignment>> assignmentsByApp = assignments.stream()
            .filter(GradebookAssignment::isExternallyMaintained)
            .collect(Collectors.groupingBy(GradebookAssignment::getExternalAppName));

        // Process each app's assignments
        for (Map.Entry<String, List<GradebookAssignment>> entry : assignmentsByApp.entrySet()) {
            String appName = entry.getKey();
            List<GradebookAssignment> appAssignments = entry.getValue();

            // Find the provider for this app
            ExternalAssignmentProvider provider = getExternalAssignmentProviders().get(appName);
            if (provider == null) {
                // No provider found, default to visible (matches existing behavior)
                appAssignments.forEach(assignment ->
                    visibilityMap.put(assignment.getExternalId(), true));
                log.debug("No provider found for external app: {}, defaulting {} assignments to visible",
                    appName, appAssignments.size());
                continue;
            }

            // Try to use batch method if available
            try {
                Map<String, List<String>> allExternalAssignments =
                    provider.getAllExternalAssignments(gradebookUid, Collections.singleton(userId));

                List<String> visibleAssignmentIds = allExternalAssignments.get(userId);
                if (visibleAssignmentIds != null) {
                    // Use batch result - assignments in the list are visible
                    Set<String> visibleIds = new HashSet<>(visibleAssignmentIds);
                    for (GradebookAssignment assignment : appAssignments) {
                        boolean isVisible = visibleIds.contains(assignment.getExternalId());
                        visibilityMap.put(assignment.getExternalId(), isVisible);
                    }
                    log.debug("Used batch method for app {}: {} assignments processed",
                        appName, appAssignments.size());
                    continue;
                }
            } catch (Exception e) {
                log.debug("Batch method failed for provider {}, falling back to individual checks: {}",
                    appName, e.getMessage());
            }

            // Fallback to individual visibility checks
            for (GradebookAssignment assignment : appAssignments) {
                boolean isVisible = true; // Default to visible

                if (provider.isAssignmentDefined(appName, assignment.getExternalId())) {
                    try {
                        isVisible = provider.isAssignmentVisible(assignment.getExternalId(), userId);
                    } catch (Exception e) {
                        log.warn("Error checking visibility for assignment {}: {}",
                            assignment.getExternalId(), e.getMessage());
                        // Keep default visible on error
                    }
                }

                visibilityMap.put(assignment.getExternalId(), isVisible);
            }

            log.debug("Used individual checks for app {}: {} assignments processed",
                appName, appAssignments.size());
        }

        log.debug("Batch processed {} external assignments for visibility", visibilityMap.size());
        return visibilityMap;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map<String, String> getViewableStudentsForItemForUser(String userUid, String gradebookUid, String siteId, Long gradableObjectId) {

        if (gradebookUid == null || gradableObjectId == null || userUid == null) {
            throw new IllegalArgumentException("null gradebookUid or gradableObjectId or " +
                    "userId passed to getViewableStudentsForUserForItem." +
                    " gradebookUid: " + gradebookUid + " gradableObjectId:" +
                    gradableObjectId + " userId: " + userUid);
        }

        if (!this.gradingAuthz.isUserAbleToGrade(siteId, userUid)) {
            return new HashMap<>();
        }

        final GradebookAssignment gradebookItem = getAssignmentWithoutStatsByID(gradebookUid, gradableObjectId);

        if (gradebookItem == null) {
            log.debug("The gradebook item does not exist, so returning empty set");
            return new HashMap();
        }

        final Long categoryId = gradebookItem.getCategory() == null ? null : gradebookItem.getCategory().getId();

        final Map<EnrollmentRecord, String> enrRecFunctionMap = this.gradingAuthz.findMatchingEnrollmentsForItemForUser(userUid, gradebookUid, siteId,
                categoryId, getGradebook(gradebookUid).getCategoryType(), null, isCurrentGbSite(gradebookUid) ? null : gradebookUid);
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
    public boolean currentUserHasGradeAllPerm(final String siteId) {

        return this.gradingAuthz.isUserAbleToGradeAll(siteId);
    }

    @Override
    public boolean isUserAllowedToGradeAll(final String siteId, final String userUid) {

        return this.gradingAuthz.isUserAbleToGradeAll(siteId, userUid);
    }

    @Override
    public boolean currentUserHasGradingPerm(String siteId) {

        return gradingAuthz.isUserAbleToGrade(siteId);
    }

    @Override
    public boolean isUserAllowedToGrade(final String siteId, final String userUid) {

        return this.gradingAuthz.isUserAbleToGrade(siteId, userUid);
    }

    @Override
    public boolean currentUserHasEditPerm(final String siteId) {

        return this.gradingAuthz.isUserAbleToEditAssessments(siteId);
    }

    @Override
    public boolean currentUserHasViewOwnGradesPerm(final String siteId) {

        return this.gradingAuthz.isUserAbleToViewOwnGrades(siteId);
    }

    @Override
    public boolean currentUserHasViewStudentNumbersPerm(final String siteId) {

        return this.gradingAuthz.isUserAbleToViewStudentNumbers(siteId);
    }

    @Override
    public List<GradeDefinition> getGradesForStudentsForItem(final String gradebookUid, final String siteId, final Long gradableObjectId,
            final List<String> students) {

        // Ensure we have a mutable copy of the student list
        List<String> studentIds = new ArrayList<>(students);

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

            if (!this.gradingAuthz.isUserAbleToGrade(siteId)) {
                log.warn(
                        "User {} attempted to access grade information without permission in gb {} using gradebookService.getGradesForStudentsForItem",
                        sessionManager.getCurrentSessionUserId(), gradebook.getUid());
                throw new GradingSecurityException();
            }

            Long categoryId = gbItem.getCategory() != null ? gbItem.getCategory().getId() : null;
            Map<EnrollmentRecord, String> enrRecFunctionMap = this.gradingAuthz.findMatchingEnrollmentsForItem(gradebook.getUid(), siteId, categoryId,
                    gradebook.getCategoryType(), null, isCurrentGbSite(gradebook.getUid()) ? null : gradebook.getUid());
            Set<EnrollmentRecord> enrRecs = enrRecFunctionMap.keySet();
            Map<String, EnrollmentRecord> studentIdEnrRecMap
                = enrRecs.stream().filter(Objects::nonNull).collect(Collectors.toMap(r -> r.getUser().getUserUid(), r -> r));

            // filter the provided studentIds if user doesn't have permissions
            studentIds.removeIf(studentId -> {
                return !studentIdEnrRecMap.containsKey(studentId);
            });

            // retrieve the grading comments for all of the students
            List<Comment> commentRecs = getComments(gbItem, studentIds);
            Map<String, String> studentIdCommentTextMap
                = commentRecs.stream().filter(Objects::nonNull).collect(Collectors.toMap(c -> c.getStudentId(), c -> c.getCommentText()));

            // now, we can populate the grade information
            List<String> studentsWithGradeRec = new ArrayList<>();
            List<AssignmentGradeRecord> gradeRecs = getAllAssignmentGradeRecordsForGbItem(gradableObjectId, studentIds);
            if (GradeType.PERCENTAGE == gradebook.getGradeType()) {
                convertPointsToPercentage(gradebook, gradeRecs);
            }

            for (AssignmentGradeRecord agr : gradeRecs) {
                if (agr != null) {
                    String commentText = studentIdCommentTextMap.get(agr.getStudentId());
                    GradeDefinition gradeDef = convertGradeRecordToGradeDefinition(agr, gbItem, gradebook, commentText);

                    studentGrades.add(gradeDef);
                    studentsWithGradeRec.add(agr.getStudentId());
                }
            }

            // if student has a comment but no grade add an empty grade definition with the comment
            if (studentsWithGradeRec.size() < studentIds.size()) {
                for (String studentId : studentIdCommentTextMap.keySet()) {
                    if (!studentsWithGradeRec.contains(studentId)) {
                        String comment = studentIdCommentTextMap.get(studentId);
                        AssignmentGradeRecord emptyGradeRecord = new AssignmentGradeRecord(gbItem, studentId, null, null);
                        GradeDefinition gradeDef = convertGradeRecordToGradeDefinition(emptyGradeRecord, gbItem, gradebook,
                                comment);
                        studentGrades.add(gradeDef);
                    }
                }
            }
        }

        return studentGrades;
    }

    @Override
    public Map<Long, List<GradeDefinition>> getGradesWithoutCommentsForStudentsForItems(final String gradebookUid, final String siteId,
            final List<Long> gradableObjectIds, final List<String> studentIds) {

        if (gradableObjectIds == null || gradableObjectIds.isEmpty()) {
            throw new IllegalArgumentException("null or empty gradableObjectIds passed to getGradesWithoutCommentsForStudentsForItems");
        }

        // when user is not able to grade and user isn't requesting to view only their grades throw exception
        if (!this.gradingAuthz.isUserAbleToGrade(siteId) &&
                !(currentUserHasViewOwnGradesPerm(siteId)
                        && CollectionUtils.isEqualCollection(studentIds, List.of(sessionManager.getCurrentSessionUserId())))) {
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

    @Override
    public Map<Long, List<GradeDefinition>> getGradesWithCommentsForStudentsForItems(final String gradebookUid, final String siteId,
            final List<Long> gradableObjectIds, final List<String> studentIds) {

        if (gradableObjectIds == null || gradableObjectIds.isEmpty()) {
            throw new IllegalArgumentException("null or empty gradableObjectIds passed to getGradesWithCommentsForStudentsForItems");
        }

        // when user is not able to grade and user isn't requesting to view only their grades throw exception
        if (!this.gradingAuthz.isUserAbleToGrade(siteId) &&
                !(currentUserHasViewOwnGradesPerm(siteId)
                        && CollectionUtils.isEqualCollection(studentIds, List.of(sessionManager.getCurrentSessionUserId())))) {
            throw new GradingSecurityException();
        }

        final Map<Long, List<GradeDefinition>> gradesMap = new HashMap<>();
        if (studentIds == null || studentIds.isEmpty()) {
            return gradesMap;
        }

        // Get all the grades for the gradableObjectIds
        final List<AssignmentGradeRecord> gradeRecords = getAllAssignmentGradeRecordsForGbItems(gradableObjectIds, studentIds);

        // Group grade records by assignment ID to organize comment retrieval
        final Map<Long, List<AssignmentGradeRecord>> recordsByAssignment = gradeRecords.stream()
                .collect(Collectors.groupingBy(record -> record.getGradableObject().getId()));

        // Build comprehensive comment map: assignmentId -> studentId -> commentText
        final Map<Long, Map<String, String>> allCommentsMap = new HashMap<>();
        for (final Long assignmentId : gradableObjectIds) {
            final GradebookAssignment assignment = recordsByAssignment.containsKey(assignmentId) ?
                    (GradebookAssignment) recordsByAssignment.get(assignmentId).get(0).getGradableObject() :
                    getAssignmentWithoutStatsByID(gradebookUid, assignmentId);

            final Map<String, String> studentCommentMap = new HashMap<>();
            if (assignment != null) {
                final List<Comment> commentRecs = getComments(assignment, studentIds);
                if (commentRecs != null) {
                    for (final Comment comment : commentRecs) {
                        if (comment != null) {
                            studentCommentMap.put(comment.getStudentId(), comment.getCommentText());
                        }
                    }
                }
            }
            allCommentsMap.put(assignmentId, studentCommentMap);
        }

        // Process grade records and add to results map
        for (final AssignmentGradeRecord gradeRecord : gradeRecords) {
            final GradebookAssignment gbo = (GradebookAssignment) gradeRecord.getGradableObject();
            final Long gboId = gbo.getId();
            final Gradebook gradebook = gbo.getGradebook();
            if (!gradebookUid.equals(gradebook.getUid())) {
                throw new IllegalArgumentException("gradableObjectIds must belong to grades within this gradebook");
            }

            // Get comment for this specific student and assignment
            final String commentText = allCommentsMap.getOrDefault(gboId, Collections.emptyMap()).get(gradeRecord.getStudentId());
            final GradeDefinition gradeDef = convertGradeRecordToGradeDefinition(gradeRecord, gbo, gradebook, commentText);

            List<GradeDefinition> gradeList = gradesMap.computeIfAbsent(gboId, k -> new ArrayList<>());
            gradeList.add(gradeDef);
        }

        // Handle students who have comments but no grades (like the single-item version)
        for (final Long assignmentId : gradableObjectIds) {
            final Map<String, String> commentsForAssignment = allCommentsMap.get(assignmentId);
            if (commentsForAssignment != null) {
                for (final String studentId : studentIds) {
                    final String commentText = commentsForAssignment.get(studentId);
                    if (commentText != null) {
                        // Check if we already have a grade record for this student/assignment combination
                        final List<GradeDefinition> existingGrades = gradesMap.get(assignmentId);
                        boolean hasGradeRecord = false;
                        if (existingGrades != null) {
                            hasGradeRecord = existingGrades.stream()
                                    .anyMatch(gd -> studentId.equals(gd.getStudentUid()));
                        }

                        // If no grade record exists but comment exists, create a GradeDefinition with just the comment
                        if (!hasGradeRecord) {
                            // We need the assignment to create the GradeDefinition
                            final GradebookAssignment assignment = recordsByAssignment.containsKey(assignmentId) ?
                                    (GradebookAssignment) recordsByAssignment.get(assignmentId).get(0).getGradableObject() :
                                    getAssignmentWithoutStatsByID(gradebookUid, assignmentId);

                            if (assignment != null) {
                                final Gradebook gradebook = assignment.getGradebook();
                                final GradeDefinition gradeDef = new GradeDefinition();
                                gradeDef.setStudentUid(studentId);
                                gradeDef.setGradeComment(commentText);
                                gradeDef.setGradeEntryType(gradebook.getGradeType());
                                gradeDef.setGradeReleased(assignment.getReleased());

                                List<GradeDefinition> gradeList = gradesMap.computeIfAbsent(assignmentId, k -> new ArrayList<>());
                                gradeList.add(gradeDef);
                            }
                        }
                    }
                }
            }
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
        final GradeType gradeType = gradebook.getGradeType();
        gradeDef.setGradeEntryType(gradeType);

        String grade = null;
        switch (gradeType) {
            case POINTS:
                Double pointsEarned = gradeRecord.getPointsEarned();
                grade = pointsEarned != null ? pointsEarned.toString() : null;
                break;
            case PERCENTAGE:
                Double percent = calculateEquivalentPercent(gbo.getPointsPossible(), gradeRecord.getPointsEarned());
                grade = percent != null ? percent.toString() : null;
                break;
            case LETTER:
                grade = gradeRecord.getLetterEarned();
                break;
            default:
                log.warn("Invalid grade type passed to convertGradeRecordToGradeDefinition: {}", gradeType);
        }
        gradeDef.setGrade(grade);

        gradeDef.setGradeReleased(gradebook.getAssignmentsDisplayed() && gbo.getReleased());

        if (commentText != null) {
            gradeDef.setGradeComment(commentText);
        }

        Boolean excludedFromGrade = (gradeRecord.getExcludedFromGrade() != null) ? gradeRecord.getExcludedFromGrade() : Boolean.FALSE;
        gradeDef.setExcused(excludedFromGrade);

        return gradeDef;
    }

    @Override
    public boolean isGradeValid(String gradebookUuid, String grade) {

        if (gradebookUuid == null) {
            throw new IllegalArgumentException("Null gradebookUuid passed to isGradeValid");
        }

        Gradebook gradebook = getGradebook(gradebookUuid);
        GradeType gradeType = gradebook.getGradeType();
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

        if (StringUtils.isBlank(grade)) return true;

        boolean gradeIsValid = false;

        if (gradeEntryType == GradeType.POINTS || gradeEntryType == GradeType.PERCENTAGE) {
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

            String standardizedGrade = gradeMapping.standardizeInputGrade(grade);
            if (standardizedGrade != null) {
                gradeIsValid = true;
            }
        } else {
            throw new IllegalArgumentException("Invalid gradeEntryType passed to isGradeValid");
        }

        return gradeIsValid;
    }

    @Override
    public Set<String> identifyStudentsWithInvalidGrades(final String gradebookUid, final Map<String, String> studentIdToGradeMap) {

        if (gradebookUid == null) {
            throw new IllegalArgumentException("null gradebookUid passed to identifyStudentsWithInvalidGrades");
        }

        if (studentIdToGradeMap == null) return Collections.<String>emptySet();

        Gradebook gradebook = getGradebook(gradebookUid);
        GradeType gradeType = gradebook.getGradeType();

        LetterGradePercentMapping gradeMapping = gradeType == GradeType.LETTER ? getLetterGradePercentMapping(gradebook) : null;

        return studentIdToGradeMap.entrySet().stream()
            .filter(e -> !isGradeValid(e.getValue(), gradeType, gradeMapping))
            .collect(Collectors.mapping(Map.Entry::getKey, Collectors.toSet()));
    }

    @Override
    @Transactional
    public void saveGradeAndCommentForStudent(final String gradebookUid, final String siteId, final Long gradableObjectId, final String studentUid,
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

        final GradebookAssignment assignment = getAssignmentWithoutStatsByID(gradebookUid, gradableObjectId);

        final AssignmentGradeRecord record = getAssignmentGradeRecord(assignment, studentUid);
        if (record != null) {
            gradeDef.setExcused(BooleanUtils.toBoolean(record.getExcludedFromGrade()));
        } else {
            gradeDef.setExcused(false);
        }
        saveGradesAndComments(gradebookUid, siteId, gradableObjectId, gradeDefList);
    }

    @Override
    public void saveGradeAndExcuseForStudent(final String gradebookUid, final String siteId, final Long gradableObjectId, final String studentUid,
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

        saveGradesAndComments(gradebookUid, siteId, gradableObjectId, gradeDefList);
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
        GradebookAssignment assignment = getAssignmentWithoutStatsByID(gradebookUid, assignmentId);
        AssignmentGradeRecord agr = getAssignmentGradeRecord(assignment, studentUid);

        if (agr == null) {
            return false;
        }else{
            return BooleanUtils.toBoolean(agr.getExcludedFromGrade());
        }
    }

    @Override
    @Transactional
    public void saveGradesAndComments(final String gradebookUid, final String siteId, final Long gradableObjectId, final List<GradeDefinition> gradeDefList) {

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

        if (!currentUserHasGradingPerm(siteId)) {
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
        if (CollectionUtils.isNotEmpty(identifyStudentsWithInvalidGrades(gradebookUid, studentIdToGradeMap))) {
            throw new InvalidGradeException(
                    "At least one grade passed to be updated is invalid. No grades or comments were updated.");
        }

        // Retrieve all existing grade records for the given students and assignment
        final List<AssignmentGradeRecord> existingGradeRecords = getAllAssignmentGradeRecordsForGbItem(gradableObjectId,
                studentIdGradeDefMap.keySet());
        final Map<String, AssignmentGradeRecord> studentIdGradeRecordMap
            = existingGradeRecords.stream().collect(Collectors.toMap(agr -> agr.getStudentId(), agr -> agr));

        // Retrieve all existing comments for the given students and assignment
        final Map<String, Comment> studentIdCommentMap
            = getComments(assignment, studentIdGradeDefMap.keySet())
                .stream().collect(Collectors.toMap(c -> c.getStudentId(), c -> c));

        Gradebook gradebook = getGradebook(gradebookUid);

        final boolean userHasGradeAllPerm = currentUserHasGradeAllPerm(siteId);
        final String graderId = sessionManager.getCurrentSessionUserId();
        final Date now = new Date();
        LetterGradePercentMapping mapping = null;
        if (GradeType.LETTER == gradebook.getGradeType()) {
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
                if (!isUserAbleToGradeItemForStudent(gradebookUid, siteId, gradableObjectId, studentId)) {
                    log.warn("User {} attempted to save a grade for {} without authorization", graderId, studentId);
                    throw new GradingSecurityException();
                }
            }
            // Determine if the AssignmentGradeRecord needs to be updated
            final String newGrade = StringUtils.trimToEmpty(gradeDef.getGrade());
            AssignmentGradeRecord gradeRec = studentIdGradeRecordMap.get(studentId);
            boolean currentExcuse;
            if (gradeRec == null) {
                currentExcuse = false;
            } else {
                currentExcuse = BooleanUtils.toBoolean(gradeRec.getExcludedFromGrade());
            }

            GradeType gradeType = gradebook.getGradeType();
            final Double convertedGrade = convertInputGradeToPoints(gradeType, mapping, assignment.getPointsPossible(), newGrade);

            if (gradeRec != null) {
                boolean changed = false;

                if (gradeType == GradeType.POINTS || gradeType == GradeType.PERCENTAGE) {
                    final Double pointsEarned = gradeRec.getPointsEarned();
                    if ((convertedGrade == null && pointsEarned != null)
                            || (convertedGrade != null && pointsEarned == null)
                            || (convertedGrade != null && pointsEarned != null && !convertedGrade.equals(pointsEarned))
                            || (excuse != currentExcuse)) {

                        changed = true;
                        gradeRec.setPointsEarned(convertedGrade);
                    }
                } else if (gradeType == GradeType.LETTER) {
                    if (!StringUtils.equals(newGrade, gradeRec.getLetterEarned())) {
                        changed = true;
                        gradeRec.setLetterEarned(newGrade);
                    }
                }

                if (changed) {
                    gradeRec.setGraderId(graderUid);
                    gradeRec.setDateRecorded(gradedDate);
                    gradeRec.setExcludedFromGrade(excuse);
                    gradeRecordsToUpdate.add(gradeRec);

                    // Add a GradingEvent, which stores the actual input grade rather than the converted one
                    GradingEvent event = new GradingEvent(assignment, graderId, studentId, newGrade);
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
                    gradeRec = gradeType == GradeType.LETTER
                        ? new AssignmentGradeRecord(assignment, studentId, null, newGrade) : new AssignmentGradeRecord(assignment, studentId, convertedGrade, null);
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
            eventsToAdd.forEach(this::sendGradingEvent);
        } catch (final HibernateOptimisticLockingFailureException | StaleObjectStateException holfe) {
            // TODO: Adrian How janky is this?
            log.info("An optimistic locking failure occurred while attempting to save scores and comments for gb Item {}", gradableObjectId);
            throw new StaleObjectModificationException(holfe);
        }
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
        switch (gradeEntryType) {
            case PERCENTAGE:
            case POINTS:
                try {
                    final NumberFormat nbFormat = NumberFormat.getInstance(resourceLoader.getLocale());
                    final Double pointValue = nbFormat.parse(grade).doubleValue();
                    convertedValue = pointValue;
                } catch (NumberFormatException | ParseException nfe) {
                    throw new InvalidGradeException("Invalid grade passed to convertInputGradeToPoints");
                }
                break;
            case LETTER:
                if (mapping == null) {
                    throw new IllegalArgumentException("No mapping passed to convertInputGradeToPoints for a letter-based gb");
                }
                if (mapping.getGradeMap() != null) {
                    // standardize the grade mapping
                    final String standardizedGrade = mapping.standardizeInputGrade(grade);
                    Double percentage = mapping.getValue(standardizedGrade);
                    if (percentage == null) {
                        throw new IllegalArgumentException("Invalid grade passed to convertInputGradeToPoints");
                    }
                    convertedValue = calculateEquivalentPointValueForPercent(gbItemPointsPossible, percentage);
                }
                break;
            default:
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
    public String getAssignmentScoreString(String gradebookUid, String siteId, Long assignmentId, String studentUid)
            throws AssessmentNotFoundException {

        final boolean studentRequestingOwnScore = sessionManager.getCurrentSessionUserId().equals(studentUid);

        if (gradebookUid == null || assignmentId == null || studentUid == null) {
            throw new IllegalArgumentException("null parameter passed to getAssignment. Values are gradebookUid:"
                    + gradebookUid + " assignmentId:" + assignmentId + " studentUid:" + studentUid);
        }

        final GradebookAssignment assignment = getAssignmentWithoutStatsByID(gradebookUid, assignmentId);
        if (assignment == null) {
            throw new AssessmentNotFoundException(
                    "There is no assignment with id " + assignmentId + " in gradebook " + gradebookUid);
        }

        if (!studentRequestingOwnScore && !isUserAbleToViewItemForStudent(gradebookUid, siteId, assignmentId, studentUid)) {
            log.warn("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to retrieve grade for student {} for assignment {}",
                    getUserUid(), gradebookUid, studentUid, assignment.getName());
            throw new GradingSecurityException();
        }

        // If this is the student, then the assignment needs to have
        // been released.
        if (studentRequestingOwnScore && !assignment.getReleased()) {
            log.warn("AUTHORIZATION FAILURE: Student {} in gradebook {} attempted to retrieve score for unreleased assignment {}",
                    getUserUid(), gradebookUid, assignment.getName());
            throw new GradingSecurityException();
        }

        final AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid);
        log.debug("gradeRecord={}", gradeRecord);

        if (gradeRecord == null) return null;

        GradeType gradeType = assignment.getGradebook().getGradeType();

        if (gradeType == GradeType.LETTER) {
            return gradeRecord.getLetterEarned();
        }

        Double assignmentScore = gradeRecord.getPointsEarned();

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
    public void setAssignmentScoreString(String gradebookUid, String siteId, Long assignmentId, String studentUid, String score, String clientServiceDescription, String externalId)
            throws AssessmentNotFoundException {

        final GradebookAssignment assignment = getAssignmentWithoutStatsByID(gradebookUid, assignmentId);

        if (assignment == null) {
            throw new AssessmentNotFoundException(
                    "There is no assignment with id " + assignmentId + " in gradebook " + gradebookUid);
        }
        if (assignment.getExternallyMaintained() && StringUtils.isBlank(externalId)) {
            log.error(
                    "AUTHORIZATION FAILURE: User {} in gradebook {} attempted to grade externally maintained assignment {} from {}",
                    getUserUid(), gradebookUid, assignmentId, clientServiceDescription);
            throw new GradingSecurityException();
        }

        if (!isUserAbleToGradeItemForStudent(gradebookUid, siteId, assignment.getId(), studentUid)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to grade student {} from {} for item {}",
                    getUserUid(), gradebookUid, studentUid, clientServiceDescription, assignmentId);
            throw new GradingSecurityException();
        }

        final Date now = new Date();
        final String graderId = sessionManager.getCurrentSessionUserId();
        AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid);
        if (gradeRecord == null) {
            // Creating a new grade record.
            gradeRecord = new AssignmentGradeRecord(assignment, studentUid, convertStringToDouble(score), null);
            // TODO: test if it's ungraded item or not. if yes, set ungraded grade for this record. if not, need validation??
        } else {
            // TODO: test if it's ungraded item or not. if yes, set ungraded grade for this record. if not, need validation??
            switch (getGradebook(gradebookUid).getGradeType()) {
                case LETTER:
                    gradeRecord.setLetterEarned(score);
                    break;
                default:
                    gradeRecord.setPointsEarned(convertStringToDouble(score));
            }
        }
        gradeRecord.setGraderId(graderId);
        gradeRecord.setDateRecorded(now);
        gradingPersistenceManager.saveAssignmentGradeRecord(gradeRecord);

        gradingPersistenceManager.saveGradingEvent(new GradingEvent(assignment, graderId, studentUid, score));

        // Post an event in SAKAI_EVENT table
        postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, score);

        log.debug("Score updated in gradebookUid={}, assignmentId={} by userUid={} from client={}, new score={}", gradebookUid, assignmentId, getUserUid()
                , clientServiceDescription, score);
    }

    @Override
    public List<CategoryDefinition> getCategoryDefinitions(String gradebookUid, String siteId) {

        if (gradebookUid == null) {
            throw new IllegalArgumentException("Null gradebookUid passed to getCategoryDefinitions");
        }

        if (!isUserAbleToViewAssignments(siteId)) {
            log.warn("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to retrieve all categories without permission", getUserUid(),
                    gradebookUid);
            throw new GradingSecurityException();
        }

        Long gradebookId = getGradebook(gradebookUid).getId();

        // Return categories sorted to match Gradebook Settings order. This ensures
        // consistent ordering across Gradebook, Assignments, and Tests & Quizzes.
        return getCategories(gradebookId).stream()
            .map(category -> buildCategoryDefinition(category, siteId))
            .sorted(org.sakaiproject.grading.api.CategoryDefinition.orderComparator)
            .collect(Collectors.toList());
    }

    private CategoryDefinition buildCategoryDefinition(final Category category, final String siteId) {

        final CategoryDefinition categoryDef = new CategoryDefinition();
        if (category != null) {
            categoryDef.setId(category.getId());
            categoryDef.setName(category.getName());
            categoryDef.setWeight(category.getWeight());
            categoryDef.setDropLowest(category.getDropLowest());
            categoryDef.setDropHighest(category.getDropHighest());
            categoryDef.setKeepHighest(category.getKeepHighest());
            categoryDef.setAssignmentList(getAssignmentsWithCategory(category.getGradebook().getUid(), siteId, category.getId()));
            categoryDef.setDropKeepEnabled(category.isDropScores());
            categoryDef.setExtraCredit(category.getExtraCredit());
            categoryDef.setEqualWeight(category.getEqualWeightAssignments());
            categoryDef.setCategoryOrder(category.getCategoryOrder());
        }

        return categoryDef;
    }

    private Category updateCategoryFromDefinition(Category category, CategoryDefinition categoryDefinition) {

        category.setName(categoryDefinition.getName());
        category.setWeight(categoryDefinition.getWeight());
        category.setDropLowest(categoryDefinition.getDropLowest());
        category.setDropHighest(categoryDefinition.getDropHighest());
        category.setKeepHighest(categoryDefinition.getKeepHighest());
        category.setExtraCredit(categoryDefinition.getExtraCredit());
        category.setEqualWeightAssignments(categoryDefinition.getEqualWeight());
        category.setCategoryOrder(categoryDefinition.getCategoryOrder());

        return category;
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

        for (AssignmentGradeRecord gradeRec : allGradeRecs) {
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
    private List<GradebookAssignment> getCountedAssignments(Gradebook gradebook) {

        // making sure there's no invalid points possible for normal assignments
        return gradingPersistenceManager.getCountedAndGradedAssignmentsForGradebook(gradebook.getId())
            .stream()
            .filter(a -> a.getPointsPossible() != null && a.getPointsPossible() > 0)
            .collect(Collectors.toList());
    }

    /**
     * set the droppedFromGrade attribute of each of the n highest and the n lowest scores of a student based on the assignment's category
     *
     * @param gradeRecords
     *
     */
    private void applyDropScores(final Collection<AssignmentGradeRecord> gradeRecords, GradeType gradeType, Integer categoryType, Map<String, Double> gradeMap) {

        if (gradeRecords == null || gradeRecords.size() < 1) {
            return;
        }
        final long start = System.currentTimeMillis();

        final Set<String> studentIds = new HashSet<>();
        final List<Category> categories = new ArrayList<>();
        final Map<String, List<AssignmentGradeRecord>> gradeRecordMap = new HashMap<>();
        for (final AssignmentGradeRecord gradeRecord : gradeRecords) {

            if (gradeRecord == null || (gradeRecord.getPointsEarned() == null && gradeRecord.getLetterEarned() == null)) {
                // don't consider grades that have null pointsEarned (this occurs when a
                // previously entered score for an assignment is removed; record stays in
                // database)
                continue;
            }

            // reset
            gradeRecord.setDroppedFromGrade(false);

            if (Objects.equals(categoryType, GradingConstants.CATEGORY_TYPE_NO_CATEGORY)) {
                continue;
            }

            final GradebookAssignment assignment = gradeRecord.getAssignment();
            if (assignment.getUngraded()
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

        if (categories.isEmpty() || Objects.equals(categoryType, GradingConstants.CATEGORY_TYPE_NO_CATEGORY)) {
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
                                AssignmentGradeRecord highest = getHighestGrade(gradesByCategory, gradeType, gradeMap);
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
                                AssignmentGradeRecord lowest = getLowestGrade(gradesByCategory, gradeType, gradeMap);
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

    private AssignmentGradeRecord getHighestGrade(List<AssignmentGradeRecord> grades, GradeType gradeType, Map<String, Double> gradeMap) {
        return grades.stream().filter(Objects::nonNull).max(Comparator.comparing(agr -> getGradeAsPercentage(agr, gradeType, gradeMap))).orElse(null);
    }

    private AssignmentGradeRecord getLowestGrade(List<AssignmentGradeRecord> grades, GradeType gradeType, Map<String, Double> gradeMap) {
        return grades.stream().filter(Objects::nonNull).min(Comparator.comparing(agr -> getGradeAsPercentage(agr, gradeType, gradeMap))).orElse(null);
    }

    private Double getGradeAsPercentage(AssignmentGradeRecord gradeRecord, GradeType gradeType, Map<String, Double> gradeMap) {

        if (gradeRecord.getPointsEarned() == null && gradeRecord.getLetterEarned() == null) {
            return 0D;
        }

        BigDecimal bdPointsEarned = new BigDecimal(gradeType == GradeType.LETTER ? gradeMap.get(gradeRecord.getLetterEarned()).toString() : gradeRecord.getPointsEarned().toString());
        BigDecimal bdPossible = new BigDecimal(((GradebookAssignment)gradeRecord.getGradableObject()).getPointsPossible().toString());
        BigDecimal bdPercent = bdPointsEarned.divide(bdPossible, GradingConstants.MATH_CONTEXT).multiply(new BigDecimal("100"));
        return Double.valueOf(bdPercent.doubleValue());
    }

    /**
     *
     * @param doubleAsString
     * @return a locale-aware Double value representation of the given String
     */
    public Double convertStringToDouble(final String doubleAsString) {

        if (StringUtils.isBlank(doubleAsString)) {
            return null;
        }

        final Double scoreAsDouble = NumberUtil.parseLocaleDouble(doubleAsString, resourceLoader.getLocale());
        if (scoreAsDouble == null || !Double.isFinite(scoreAsDouble)) {
            log.warn("Failed to convert score for locale {}: '{}'", resourceLoader.getLocale(), doubleAsString);
            return null;
        }
        return scoreAsDouble;
    }

    /**
     * Get a list of assignments in the gradebook attached to the given category. Note that each assignment only knows the category by name.
     *
     * <p>
     * Note also that this is different to {@link BaseHibernateManager#getAssignmentsForCategory(Long)} because this method returns the
     * shared GradebookAssignment object.
     *
     * @param gradebookUid
     * @param siteId
     * @param categoryId
     * @return
     */
    private List<Assignment> getAssignmentsWithCategory(String gradebookUid, String siteId, Long categoryId) {
        if (!isUserAbleToViewAssignments(siteId)) {
            log.warn("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to get assignments list", getUserUid(), gradebookUid);
           throw new GradingSecurityException();
        }

        Gradebook gradebook = getGradebook(gradebookUid);
        // Determine whether this gradebook uses Categories Only or Weighted Categories by checking category type.
        // We will avoid adding any legacy category information on the individual gb items if the instructor is no
        // longer using categories in the gradebook.
        boolean gbUsesCategories = !Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY);

        List<GradebookAssignment> filtered = getAssignmentsByGradebookAndCategoryId(gradebook.getId(), categoryId);

        return sortAssignments(filtered, SortType.SORT_BY_NONE, true)
                   .stream()
                   .map(ga -> getAssignmentDefinition(ga, gbUsesCategories))
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
    private void postUpdateGradeEvent(String gradebookUid, String assignmentName, String studentUid, String grade) {

        log.debug("postUpdateGradeEvent {} {} {} {}", gradebookUid, assignmentName, studentUid, grade);
        postEvent("gradebook.updateItemScore",
                "/gradebook/" + gradebookUid + "/" + assignmentName + "/" + studentUid + "/" + grade + "/student");//aqui se pasa el uid y en otro el id?
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
    private void postUpdateCommentEvent(String gradebookUid, String assignmentId, String studentUid, Double pointsEarned) {

        log.debug("postUpdateCommentEvent {} {} {} {}", gradebookUid, assignmentId, studentUid, pointsEarned);
        postEvent("gradebook.updateItemComment",
                "/gradebook/" + gradebookUid + "/" + assignmentId + "/" + studentUid + "/" + pointsEarned + "/student");
    }

    /**
     * Get the student's course grade's GradableObject ID.
     *
     * @return coursegrade's GradableObject ID.
     */
    public Long getCourseGradeId(Long gradebookId) {
        return getCourseGrade(gradebookId).getId();
    }

    /**
     * Send a GradebookEvent to Sakai's event table
     *
     * @param gradebookEvent
     * @return
     */
    private void sendGradingEvent(GradingEvent gradingEvent) {
        String studentId = gradingEvent.getStudentId();
        String scoreStr = gradingEvent.getGrade();

        // Null is actually OK.
        Double score = null;
        if ( scoreStr != null ) {
            try {
                score = new Double(scoreStr);
            } catch (Exception e) {
                log.debug("Could not parse score as number studentId={} score={}", studentId, scoreStr);
                return;
            }
        }

        GradableObject go = gradingEvent.getGradableObject();

        log.debug("sendGradingEventchecking GradableObject studentId={} score={} go={}", studentId, score, go);

        if ( go == null ) return;
        String assignmentName = go.getName();
        Gradebook gb = go.getGradebook();
        if ( gb == null ) return;
        String gradebookUid = gb.getUid();

        postUpdateGradeEvent(gradebookUid, assignmentName, studentId, scoreStr);
    }

    /**
     * Retrieves the calculated average course grade.
     */
    @Override
    @Transactional
    public String getAverageCourseGrade(final String gradebookUid, final String siteId) {

        if (gradebookUid == null) {
            throw new IllegalArgumentException("Null gradebookUid passed to getAverageCourseGrade");
        }
        // Check user has permission to invoke method.
        if (!currentUserHasGradeAllPerm(siteId)) {
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
            Set<String> studentUids = getAllStudentUids(siteId);
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
    public void updateAssignmentOrder(final String gradebookUid, final String siteId, final Long assignmentId, Integer order) {

        if (!gradingAuthz.isUserAbleToEditAssessments(siteId)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to change the order of assignment {}", getUserUid(),
                    gradebookUid, assignmentId);
            throw new GradingSecurityException();
        }

        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        final Long gradebookId = getGradebook(gradebookUid).getId();

        // get all assignments for this gradebook
        final List<GradebookAssignment> assignments = getSortedAssignments(gradebookId, SortType.SORT_BY_SORTING, true);

        // find the assignment
        GradebookAssignment target = assignments.stream().filter(a -> a.getId().equals(assignmentId)).findAny().orElseThrow(() -> {
            return new IllegalArgumentException("No assignment for id " + assignmentId);
        });

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
    public Optional<CategoryScoreData> calculateCategoryScore(final Gradebook gradebook, final String studentUuid,
            final CategoryDefinition category, final List<Assignment> categoryAssignments,
            final Map<Long, String> gradeMap, final boolean includeNonReleasedItems) {

        // used for translating letter grades
        final Map<String, Double> gradingSchema = gradebook.getSelectedGradeMapping().getGradeMap();

        // collect the data and turn it into a list of AssignmentGradeRecords
        // this is the info that is compatible with both applyDropScores and the calculateCategoryScore method
        final List<AssignmentGradeRecord> gradeRecords = new ArrayList<>();
        for (final Assignment assignment : categoryAssignments) {

            final Long assignmentId = assignment.getId();

            final String rawGrade = gradeMap.get(assignmentId);
            final Double pointsPossible = assignment.getPoints();
            Double grade;

            // determine the grade we should be using depending on the grading type
            switch (gradebook.getGradeType()) {
                case PERCENTAGE:
                    grade = calculateEquivalentPointValueForPercent(pointsPossible, NumberUtils.createDouble(rawGrade));
                    break;
                case LETTER:
                    grade = gradingSchema.get(rawGrade);
                    break;
                default:
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
            a.setGradebook(gradebook);
            a.setCategory(c);
            a.setId(assignment.getId()); // store the id so we can find out later which grades were dropped, if any

            // create the AGR
            GradeType gt = gradebook.getGradeType();
            AssignmentGradeRecord gradeRecord
                = new AssignmentGradeRecord(a, studentUuid, gt == GradeType.LETTER ? null : grade, gt == GradeType.LETTER ? rawGrade : null);

            if (!a.getNotCounted()) {
                gradeRecords.add(gradeRecord);
            }
        }

        return calculateCategoryScore(studentUuid, category.getId(), gradeRecords, includeNonReleasedItems, gradebook.getGradeType(), gradebook.getCategoryType(), category.getEqualWeight(), gradingSchema);
    }

    @Override
    public Optional<CategoryScoreData> calculateCategoryScore(Long gradebookId, String studentUuid, Long categoryId,
          boolean includeNonReleasedItems, Boolean equalWeightAssignments) {

        Gradebook gradebook = getGradebook(gradebookId);

        // get all grade records for the student
        Map<String, List<AssignmentGradeRecord>> gradeRecMap = getGradeRecordMapForStudents(gradebookId, List.of(studentUuid));

        return calculateCategoryScore(studentUuid, categoryId, gradeRecMap.get(studentUuid), includeNonReleasedItems, gradebook.getGradeType(), gradebook.getCategoryType(), equalWeightAssignments, gradebook.getSelectedGradeMapping().getGradeMap());
    }

    /**
     * Calculate category scores for all categories for a student in one efficient operation.
     * This is much more efficient than calling calculateCategoryScore repeatedly for each category.
     *
     * @param gradebookId the gradebook id
     * @param studentUuid the student uuid
     * @param includeNonReleasedItems whether to include non-released items
     * @param categoryType the category type of the gradebook
     * @return map of categoryId to CategoryScoreData for all categories that have calculable scores
     */
    public Map<Long, CategoryScoreData> calculateAllCategoryScores(Long gradebookId, String studentUuid,
            boolean includeNonReleasedItems, Integer categoryType) {

        log.debug("Calculating all category scores for student: {} in gradebook: {}", studentUuid, gradebookId);

        // get all grade records for the student ONCE
        Map<String, List<AssignmentGradeRecord>> gradeRecMap = getGradeRecordMapForStudents(gradebookId, Collections.singletonList(studentUuid));
        List<AssignmentGradeRecord> allGradeRecords = gradeRecMap.get(studentUuid);

        if (allGradeRecords == null || allGradeRecords.isEmpty()) {
            log.debug("No grade records found for student: {}", studentUuid);
            return new HashMap<>();
        }

        // Get all categories for this gradebook
        List<Category> categories = getCategories(gradebookId);
        if (categories.isEmpty()) {
            log.debug("No categories found for gradebook: {}", gradebookId);
            return new HashMap<>();
        }

        Map<Long, CategoryScoreData> categoryScores = new HashMap<>();

        GradeType gradeType = getGradebook(gradebookId).getGradeType();

        Map<String, Double> gradeMap = getGradebook(gradebookId).getSelectedGradeMapping().getGradeMap();

        // Calculate score for each category using the same grade records
        for (Category category : categories) {
            if (category.getRemoved()) {
                continue; // Skip removed categories
            }

            Optional<CategoryScoreData> scoreData = calculateCategoryScore(
                    studentUuid,
                    category.getId(),
                    allGradeRecords,
                    includeNonReleasedItems,
                    gradeType,
                    categoryType,
                    category.getEqualWeightAssignments(),
                    gradeMap
            );

            if (scoreData.isPresent()) {
                categoryScores.put(category.getId(), scoreData.get());
            }
        }

        log.debug("Calculated {} category scores for student: {}", categoryScores.size(), studentUuid);
        return categoryScores;
    }

    /**
     * Calculate category scores for multiple students and all categories in one bulk operation.
     * This is the most efficient method when you need category scores for multiple students.
     *
     * @param gradebookId the gradebook id
     * @param studentUuids list of student uuids
     * @param includeNonReleasedItems whether to include non-released items
     * @param categoryType the category type of the gradebook
     * @return nested map: studentUuid -> categoryId -> CategoryScoreData
     */
    public Map<String, Map<Long, CategoryScoreData>> calculateAllCategoryScoresForStudents(Long gradebookId,
            List<String> studentUuids, boolean includeNonReleasedItems, Integer categoryType) {

        if (studentUuids == null || studentUuids.isEmpty()) {
            log.debug("No student UUIDs provided for bulk category score calculation");
            return new HashMap<>();
        }

        log.debug("Calculating all category scores for {} students in gradebook: {}", studentUuids.size(), gradebookId);

        // get all grade records for all students ONCE
        Map<String, List<AssignmentGradeRecord>> gradeRecMap = getGradeRecordMapForStudents(gradebookId, studentUuids);

        // Get all categories for this gradebook ONCE
        List<Category> categories = getCategories(gradebookId);
        if (categories.isEmpty()) {
            log.debug("No categories found for gradebook: {}", gradebookId);
            return new HashMap<>();
        }

        Map<String, Map<Long, CategoryScoreData>> allCategoryScores = new HashMap<>();

        GradeType gradeType = getGradebook(gradebookId).getGradeType();

        Map<String, Double> gradeMap = getGradebook(gradebookId).getSelectedGradeMapping().getGradeMap();

        // Calculate scores for each student
        for (String studentUuid : studentUuids) {
            List<AssignmentGradeRecord> studentGradeRecords = gradeRecMap.get(studentUuid);

            if (studentGradeRecords == null || studentGradeRecords.isEmpty()) {
                log.debug("No grade records found for student: {}", studentUuid);
                allCategoryScores.put(studentUuid, new HashMap<>());
                continue;
            }

            Map<Long, CategoryScoreData> studentCategoryScores = new HashMap<>();

            // Calculate score for each category using the student's grade records
            for (Category category : categories) {
                if (category.getRemoved()) {
                    continue; // Skip removed categories
                }

                Optional<CategoryScoreData> scoreData = calculateCategoryScore(
                        studentUuid,
                        category.getId(),
                        studentGradeRecords,
                        includeNonReleasedItems,
                        gradeType,
                        categoryType,
                        category.getEqualWeightAssignments(),
                        gradeMap
                );

                if (scoreData.isPresent()) {
                    studentCategoryScores.put(category.getId(), scoreData.get());
                }
            }

            allCategoryScores.put(studentUuid, studentCategoryScores);
        }

        int totalScores = allCategoryScores.values().stream().mapToInt(Map::size).sum();
        log.debug("Calculated {} total category scores for {} students", totalScores, studentUuids.size());

        return allCategoryScores;
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
            final List<AssignmentGradeRecord> gradeRecords, final boolean includeNonReleasedItems, GradeType gradeType, final Integer categoryType, Boolean equalWeightAssignments, Map<String, Double> gradeMap) {

        // validate
        if (gradeRecords == null) {
            log.debug("No grade records for student: {}. Nothing to do.", studentUuid);
            return Optional.empty();
        }

        if (categoryId == null) {
            log.debug("No category supplied, nothing to do.");
            return Optional.empty();
        }

        // CRITICAL FIX: Create a copy of the grade records to avoid modifying the shared list
        // This prevents the issue where calculating one category affects subsequent category calculations
        final List<AssignmentGradeRecord> gradeRecordsCopy = new ArrayList<>(gradeRecords);

        // setup
        int numScored = 0;
        int numOfAssignments = 0;
        BigDecimal totalEarned = new BigDecimal("0");
        BigDecimal totalEarnedMean = new BigDecimal("0");
        BigDecimal totalPossible = new BigDecimal("0");

        // apply any drop/keep settings for this category
        applyDropScores(gradeRecordsCopy, gradeType, categoryType, gradeMap);

        // find the records marked as dropped (highest/lowest) before continuing,
        // as gradeRecords will be modified in place after this and these records will be removed
        final List<Long> droppedItemIds = gradeRecordsCopy.stream()
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

        gradeRecordsCopy.removeIf(gradeRecord -> {

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
            if (excluded || assignment.getPointsPossible() == null || (gradeRecord.getPointsEarned() == null && gradeRecord.getLetterEarned() == null) || !assignment.getCounted()
                    || (!assignment.getReleased() && !includeNonReleasedItems) || gradeRecord.getDroppedFromGrade()) {
                return true;
            }

            return false;
        });

        log.debug("gradeRecordsCopy.size(): {}", gradeRecordsCopy.size());

        // pre-calculation
        // Rule 1. If category only has a single EC item, don't try to calculate category total.
        if (gradeRecordsCopy.size() == 1 && gradeRecordsCopy.get(0).getAssignment().getExtraCredit()) {
            return Optional.empty();
        }

        Gradebook gradebook = gradeRecords.size() > 0 ? gradeRecords.get(0).getAssignment().getGradebook() : null;

        // iterate the filtered list and set the variables for the calculation
        for (AssignmentGradeRecord gradeRecord : gradeRecordsCopy) {

            GradebookAssignment assignment = gradeRecord.getAssignment();
            BigDecimal possiblePoints = new BigDecimal(assignment.getPointsPossible().toString());

            // EC item, don't count points possible
            if (!assignment.getExtraCredit()) {
                totalPossible = totalPossible.add(possiblePoints);
                numOfAssignments++;
                numScored++;
            }

            BigDecimal grade = null;

            switch (gradebook.getGradeType()) {
                case LETTER:
                    grade = new BigDecimal(gradeRecord.getLetterEarned() != null ? gradeMap.get(gradeRecord.getLetterEarned()) : 0D);
                    break;
                default:
                    // sanitise grade, null values to "0";
                    String gradeString = (gradeRecord.getPointsEarned() != null) ? String.valueOf(gradeRecord.getPointsEarned()) : "0";
                    grade = new BigDecimal(gradeString);
            }

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
    public CourseGradeTransferBean getCourseGradeForStudent(String gradebookUid, String siteId, String userUuid) {
        return this.getCourseGradeForStudents(gradebookUid, siteId, List.of(userUuid)).get(userUuid);
    }

    @Override
    public Map<String, CourseGradeTransferBean> getCourseGradeForStudents(String gradebookUid, String siteId, List<String> userUuids) {

        try {
            GradeMapping gradeMap = getGradebook(gradebookUid).getSelectedGradeMapping();
            return getCourseGradeForStudents(gradebookUid, siteId, userUuids, gradeMap.getGradeMap());
        } catch (Exception e) {
            log.error("Error in getCourseGradeForStudents : {}", e.toString());
            return Collections.<String, CourseGradeTransferBean>emptyMap();
        }
    }

    @Override
    @Transactional
    public Map<String, CourseGradeTransferBean> getCourseGradeForStudents(final String gradebookUid, final String siteId,
            final List<String> userUuids, final Map<String, Double> gradeMap) {

        final Map<String, CourseGradeTransferBean> rval = new HashMap<>();

        try {
            final Gradebook gradebook = getGradebook(gradebookUid);

            // if not released, and not instructor or TA, don't do any work
            // note that this will return a course grade for Instructor and TA even if not released, see SAK-30119
            if (!gradebook.getCourseGradeDisplayed() && !(currentUserHasEditPerm(siteId) || currentUserHasGradingPerm(siteId))) {
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
                    cg.setPointsEarned(gr.getCalculatedPointsEarned());
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
    public List<CourseSection> getViewableSections(final String gradebookUid, final String siteId) {

        return gradingAuthz.getViewableSections(gradebookUid, siteId);
    }

    @Override
    public void updateGradebookSettings(final String gradebookUid, final String siteId, final GradebookInformation gbInfo) {

        if (gradebookUid == null) {
            throw new IllegalArgumentException("null gradebookUid " + gradebookUid);
        }

        // must be instructor type person
        if (!currentUserHasEditPerm(siteId)) {
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
        if (Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY)) {
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
        if (Objects.equals(gradebook.getCategoryType(), GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY)) {
            excludeUncategorisedItemsFromCourseGradeCalculations(gradebook);
        }

        // persist
        updateGradebook(gradebook, siteId);

    }

    @Override
    public Set<GradeMapping> getGradebookGradeMappings(Long gradebookId) {

        return gradingPersistenceManager.getGradebook(gradebookId).map(Gradebook::getGradeMappings).orElseGet(() -> {
            log.warn("No gradebook for id {}", gradebookId);
            return null;
        });
    }

    @Override
    public void updateCourseGradeForStudent(final String gradebookUid, final String siteId, final String studentUuid, final String grade, final String gradeScale) {

        // must be instructor type person
        if (!currentUserHasEditPerm(siteId)) {
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

        return gradeMappings.stream().collect(Collectors.mapping(m -> {
            return new GradeMappingDefinition(m.getId(), m.getName(),
                    GradeMappingDefinition.sortGradeMapping(m.getGradeMap()),
                    GradeMappingDefinition.sortGradeMapping(m.getDefaultBottomPercents()));
        }, Collectors.toList()));
    }

    /**
     * Updates the categorized order of an assignment
     *
     * @see GradingService.updateAssignmentCategorizedOrder(java.lang.String gradebookUid, java.lang.Long assignmentId, java.lang.Integer
     *      order)
     */
    @Override
    public void updateAssignmentCategorizedOrder(final String gradebookUid, final String siteId, final Long categoryId, final Long assignmentId, Integer order) {

        if (!gradingAuthz.isUserAbleToEditAssessments(siteId)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to change the order of assignment {}", getUserUid(),
                    gradebookUid, assignmentId);
            throw new GradingSecurityException();
        }

        if (order == null) {
            throw new IllegalArgumentException("Categorized Order cannot be null");
        }

        final Long gradebookId = getGradebook(gradebookUid).getId();

        // get all assignments for this gradebook
        final List<GradebookAssignment> assignments = getSortedAssignments(gradebookId, SortType.SORT_BY_CATEGORY, true);
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
        if (GradeType.PERCENTAGE == gradebook.getGradeType() && assignment.getPointsPossible() != null) {

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
        else if (GradeType.POINTS == gradebook.getGradeType() && assignment.getPointsPossible() != null) {

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
        eventsToAdd.forEach(this::sendGradingEvent);
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

    private boolean isCurrentUserFromGroup(final String siteId, final String studentId) {

        boolean isFromGroup = false;
        try {
            final Site s = this.siteService.getSite(siteId);
            final Group g = s.getGroup(studentId);
            isFromGroup = (g != null) && (g.getMember(sessionManager.getCurrentSessionUserId()) != null);
        } catch (final Exception e) {
            // Id not found
            log.error("Error in isCurrentUserFromGroup: ", e);
        }
        return isFromGroup;
    }

    private boolean isCurrentGbSite(String gradebookUid) {
        try {
            final Site s = this.siteService.getSite(gradebookUid);
        } catch (final Exception e) {
            return false;
        }
        return true;
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

    public void destroy() {
        log.debug("DESTROY");
        if (this.externalProviders != null) {
            this.externalProviders.clear();
            this.externalProviders = null;
        }
    }

    @Override
    @Transactional
    public void removeExternalAssignment(String gradebookUid, String externalId, String externalApp) throws AssessmentNotFoundException {

        List<GradebookAssignment> gas = new ArrayList<>();
        if (gradebookUid == null) {
            gas = gradingPersistenceManager.getGradebookUidByExternalId(externalId);
        } else {
            // Get the external assignment
            gas.add(getDbExternalAssignment(gradebookUid, externalId)
                        .orElseThrow(() -> new AssessmentNotFoundException("There is no external assessment id=" + externalId + " in gradebook uid=" + gradebookUid)));
        }

        for (GradebookAssignment asn : gas) {
            if (externalApp != null && !externalApp.equals(asn.getExternalAppName())) {
                log.debug("Skipping gradebook item with id {} from app {}", externalId, asn.getExternalAppName());
                continue;
            }

            int numDeleted = gradingPersistenceManager.deleteGradingEventsForAssignment(asn);
            log.debug("Deleted {} records from gb_grading_event_t", numDeleted);

            numDeleted = gradingPersistenceManager.deleteGradeRecordsForAssignment(asn);
            log.info("Deleted {} externally defined scores", numDeleted);

            numDeleted = gradingPersistenceManager.deleteCommentsForAssignment(asn);
            log.info("Deleted {} externally defined comments", numDeleted);

            // Delete the assessment.
            gradingPersistenceManager.deleteAssignment(asn);

            log.info("External assessment removed from gradebookUid={}, externalId={}, externalApp={} by userUid={}", gradebookUid, externalId, externalApp, getUserUid());
        }
    }

    private Optional<GradebookAssignment> getDbExternalAssignment(String gradebookUid, String externalId) {
        if (externalId == null) {
            log.debug("A null externalId supplied to getDbExternalAssignment. Returning empty ...");
            return Optional.empty();
        }
        return gradingPersistenceManager.getExternalAssignment(gradebookUid, externalId);
    }

    @Override
    public void updateExternalAssessmentComments(String gradebookUid, String siteId, String externalId,
            Map<String, String> studentUidsToComments) throws AssessmentNotFoundException {

        GradebookAssignment asn
            = getDbExternalAssignment(gradebookUid, externalId)
                .orElseThrow(() -> new AssessmentNotFoundException("There is no external assessment id=" + externalId + " in gradebook uid=" + gradebookUid));

        Set<String> studentIds = studentUidsToComments.keySet();
        if (studentIds.isEmpty()) {
            return;
        }

        List<AssignmentGradeRecord> existingScores
            = gradingPersistenceManager.getAssignmentGradeRecordsForAssignmentAndStudents(asn, studentIds);

        // Try to reduce data contention by only updating when a score
        // has changed or property has been set forcing a db update every time.
        boolean alwaysUpdate = isUpdateSameScore(siteId);

        Set<String> changedStudents = new HashSet<>();
        for (AssignmentGradeRecord agr : existingScores) {
            String studentUid = agr.getStudentId();

            CommentDefinition gradeComment = getAssignmentScoreComment(gradebookUid, asn.getId(), studentUid);
            String oldComment = gradeComment != null ? gradeComment.getCommentText() : null;
            String newComment = studentUidsToComments.get(studentUid);

            if (alwaysUpdate || (newComment != null && !newComment.equals(oldComment)) || (newComment == null && oldComment != null)) {
                changedStudents.add(studentUid);
                setAssignmentScoreComment(gradebookUid, asn.getId(), studentUid, newComment);
            }
        }

        log.debug("updateExternalAssessmentComments sent {} records, actually changed {}", studentIds.size(), changedStudents.size());
    }

    @Override
    public void updateExternalAssessmentScores(final String gradebookUid, final String siteId, final String externalId,
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

        // Try to reduce data contention by only updating when a score
        // has changed or property has been set forcing a db update every time.
        final boolean alwaysUpdate = isUpdateSameScore(siteId);

        for (final AssignmentGradeRecord agr : existingScores) {
            final String studentUid = agr.getStudentId();
            previouslyUnscoredStudents.remove(studentUid);

            final Double oldPointsEarned = agr.getPointsEarned();
            final Double newPointsEarned = studentUidsToScores.get(studentUid);
            if (alwaysUpdate || (newPointsEarned != null && !newPointsEarned.equals(oldPointsEarned))
                    || (newPointsEarned == null && oldPointsEarned != null)) {
                agr.setDateRecorded(now);
                agr.setGraderId(graderId);
                agr.setPointsEarned(newPointsEarned);
                gradingPersistenceManager.saveAssignmentGradeRecord(agr);
                changedStudents.add(studentUid);
                postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, newPointsEarned.toString());
            }
        }
        for (String studentUid : previouslyUnscoredStudents) {
            // Don't save unnecessary null scores.
            Double newPointsEarned = studentUidsToScores.get(studentUid);
            if (newPointsEarned != null) {
                AssignmentGradeRecord agr = new AssignmentGradeRecord(assignment, studentUid, newPointsEarned, null);
                agr.setDateRecorded(now);
                agr.setGraderId(graderId);
                gradingPersistenceManager.saveAssignmentGradeRecord(agr);
                changedStudents.add(studentUid);
                postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, newPointsEarned.toString());
            }
        }

        log.debug("updateExternalAssessmentScores sent {} records, actually changed {}", studentIds.size(), changedStudents.size());
    }

    @Override
    public void updateExternalAssessmentScoresString(final String gradebookUid, final String siteId, final String externalId,
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

        // Try to reduce data contention by only updating when a score
        // has changed or property has been set forcing a db update every time.
        final boolean alwaysUpdate = isUpdateSameScore(siteId);

        for (final AssignmentGradeRecord agr : existingScores) {
            final String studentUid = agr.getStudentId();
            previouslyUnscoredStudents.remove(studentUid);

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
                postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, newPointsEarned.toString());
            }
        }
        for (String studentUid : previouslyUnscoredStudents) {
            // Don't save unnecessary null scores.
            String newPointsEarned = studentUidsToScores.get(studentUid);
            if (newPointsEarned != null) {
                AssignmentGradeRecord agr = new AssignmentGradeRecord(assignment, studentUid,
                        convertStringToDouble(newPointsEarned), null);
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
    public void addExternalAssessment(final String gradebookUid, final String siteId, final String externalId, final String externalUrl, final String title, final Double points,
                                           final Date dueDate, final String externalServiceDescription, String externalData, final Boolean ungraded, final Long categoryId, String gradableReference)
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
        if (isAssignmentDefined(gradebookUid, siteId, title)) {
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
        asn.setReference(gradableReference);
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

        Long assignmentId = gradingPersistenceManager.saveGradebookAssignment(asn).getId();

        log.debug("External assessment added to gradebookUid={}, externalId={} from externalApp={}",
                gradebookUid, externalId, externalServiceDescription);

        // Check if this is a plus course and gb is site instance
        if ( plusService.enabled() && isCurrentGbSite(gradebookUid)) {
            try {
                final Site site = this.siteService.getSite(gradebookUid);
                if ( plusService.enabled(site) ) {

                    String lineItem = plusService.createLineItem(site, assignmentId, getAssignmentDefinition(asn));
                    log.debug("Lineitem created={} created assignment={} gradebook={}", lineItem, asn.getName(), gradebookUid);

                    // Update the assignment with the new lineItem
                    final GradebookAssignment assignment = getAssignmentWithoutStatsByID(gradebookUid, assignmentId);
                    if (assignment == null) {
                        throw new AssessmentNotFoundException(
                                "There is no assignment with id " + assignmentId + " in gradebook " + gradebookUid);
                    }
                    assignment.setLineItem(lineItem);
                    updateAssignment(assignment);
                }
            } catch (Exception e) {
                log.error("Could not load site associated with gradebook - lineitem not created", e);
            }
        }
    }

    @Override
    public void updateExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl, String externalData, final String title, Long categoryId,
                                         final Double points, final Date dueDate, final Boolean ungraded)
            throws AssessmentNotFoundException, ConflictingAssignmentNameException, AssignmentHasIllegalPointsException {

        GradebookAssignment asn
            = getDbExternalAssignment(gradebookUid, externalId)
                .orElseThrow(() -> new AssessmentNotFoundException("There is no external assessment id=" + externalId + " in gradebook uid=" + gradebookUid));

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
        if (categoryId != null) {
            if (categoryId != -1L) {
                asn.setCategory(getCategory(categoryId));
            } else {
                asn.setCategory(null);
            }
        }
        gradingPersistenceManager.saveGradebookAssignment(asn);

        log.info("External assessment updated in gradebookUid={}, externalId={} by userUid={}", gradebookUid, externalId, getUserUid());

        // Check if this is a plus course
        if ( plusService.enabled() && isCurrentGbSite(gradebookUid)) {
            try {
                final Site site = this.siteService.getSite(gradebookUid);
                if ( plusService.enabled(site) ) {
                    plusService.updateLineItem(site, getAssignmentDefinition(asn));
                    log.debug("Lineitem updated={} created assignment={} gradebook={}", asn.getLineItem(), asn.getName(), gradebookUid);
                }
            } catch (Exception e) {
                log.error("Could not load site associated with gradebook - lineitem not updated", e);
            }
        }
    }

    @Override
    public void updateExternalAssessmentComment(final String gradebookUid, final String siteId, final String externalId, final String studentUid,
            final String comment)
            throws AssessmentNotFoundException {

        GradebookAssignment asn
            = getDbExternalAssignment(gradebookUid, externalId)
                .orElseThrow(() -> new AssessmentNotFoundException("There is no external assessment id=" + externalId + " in gradebook uid=" + gradebookUid));

        log.debug("BEGIN: Update 1 score for gradebookUid={}, external assessment={} from {}", gradebookUid, externalId,
                asn.getExternalAppName());

        // Try to reduce data contention by only updating when the
        // score has actually changed or property has been set forcing a db update every time.
        final boolean alwaysUpdate = isUpdateSameScore(siteId);

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
    public void updateExternalAssessmentScore(final String gradebookUid, final String siteId, final String externalId, final String studentUid,
            final String points)
            throws AssessmentNotFoundException {

        GradebookAssignment asn
            = getDbExternalAssignment(gradebookUid, externalId)
                .orElseThrow(() -> new AssessmentNotFoundException("There is no external assessment id=" + externalId + " in gradebook uid=" + gradebookUid));

        log.debug("BEGIN: Update 1 score for gradebookUid={}, external assessment={} from {}", gradebookUid, externalId,
                asn.getExternalAppName());

        final Date now = new Date();

        AssignmentGradeRecord agr = getAssignmentGradeRecord(asn, studentUid);

        // Try to reduce data contention by only updating when the
        // score has actually changed or property has been set forcing a db update every time.
        final boolean alwaysUpdate = isUpdateSameScore(siteId);

        // TODO: for ungraded items, needs to set ungraded-grades later...
        final Double oldPointsEarned = (agr == null) ? null : agr.getPointsEarned();
        final Double newPointsEarned = (points == null) ? null : convertStringToDouble(points);
        if (alwaysUpdate || (newPointsEarned != null && !newPointsEarned.equals(oldPointsEarned)) ||
                (newPointsEarned == null && oldPointsEarned != null)) {
            if (agr == null) {
                if (newPointsEarned != null) {
                    agr = new AssignmentGradeRecord(asn, studentUid, Double.valueOf(newPointsEarned), null);
                } else {
                    agr = new AssignmentGradeRecord(asn, studentUid, null, null);
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
            postUpdateGradeEvent(gradebookUid, asn.getName(), studentUid, newPointsEarned.toString());
        } else {
            log.debug("Ignoring updateExternalAssessmentScore, since the new points value is the same as the old");
        }

        log.debug("END: Update 1 score for gradebookUid={}, external assessment={} from {}", gradebookUid, externalId,
                asn.getExternalAppName());
        log.debug("External assessment score updated in gradebookUid={}, externalId={} by userUid={}, new score={}", gradebookUid,
                externalId, getUserUid(), points);
    }

    private NumberFormat getNumberFormat() {
        return NumberFormat.getInstance(resourceLoader.getLocale());
    }

    @Override
    public Long getExternalAssessmentCategoryId(final String gradebookUId, final String externalId) {

        GradebookAssignment asn
            = getDbExternalAssignment(gradebookUId, externalId)
                .orElseThrow(() -> new AssessmentNotFoundException("There is no external assessment id=" + externalId + " in gradebook uid=" + gradebookUId));

        if (asn.getCategory() != null) {
            return asn.getCategory().getId();
        }

        return null;
    }

    /**
     * Determines whether to update a grade record when there have been no changes. This is useful when we need to update only
     * gb_grade_record_t's 'DATE_RECORDED' field for instance. Generally uses the sakai.property
     * 'gradebook.externalAssessments.updateSameScore', but a site property by the same name can override it. That is to say, the site
     * property is checked first, and if it is not present, the sakai.property is used.
     * @param siteId the id of the site when we can't get it from current context
     */
    private boolean isUpdateSameScore(final String siteId) {
        String siteProperty = null;
        try {
            final Site site = this.siteService.getSite(siteId);
            siteProperty = site.getProperties().getProperty(UPDATE_SAME_SCORE_PROP);
        } catch (final NullPointerException e) {
            // Fallback to gradebook UID, which is also the site ID
            try {
                siteProperty = siteService.getSite(siteId).getProperties().getProperty(UPDATE_SAME_SCORE_PROP);
            } catch (final Exception ex) {
                // Can't access site. Leave it set to null
            }
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

        return !Objects.equals(getGradebook(gradebookUid).getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY);
    }

    @Override
    @Transactional
    public Gradebook addGradebook(final String uid, final String name) {

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
        gradebook.setName(name);
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
            log.warn("No default GradeMapping found for new Gradebook={}; will set default to {}",
                gradebook.getUid(), defaultGradeMapping.getName());
        }
        gradebook.setSelectedGradeMapping(defaultGradeMapping);

        // The Hibernate mapping as of Sakai 2.2 makes this next
        // call meaningless when it comes to persisting changes at
        // the end of the transaction. It is, however, needed for
        // the mappings to be seen while the transaction remains
        // uncommitted.
        gradebook.setGradeMappings(gradeMappings);

        // SAK-33855 turn on stats for new gradebooks
        final Boolean propAssignmentStatsDisplayed = this.serverConfigurationService.getBoolean(PROP_ASSIGNMENT_STATS_DISPLAYED, true);
        gradebook.setAssignmentStatsDisplayed(propAssignmentStatsDisplayed);

        final Boolean propCourseGradeStatsDisplayed = this.serverConfigurationService.getBoolean(PROP_COURSE_GRADE_STATS_DISPLAYED, true);
        gradebook.setCourseGradeStatsDisplayed(propCourseGradeStatsDisplayed);

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

        gradingPersistenceManager.deleteGradebook(gradebookUid);
    }

    @Override
    public void updateGradeMapping(Long gradeMappingId, Map<String, Double> gradeMap) {

        gradingPersistenceManager.getGradeMapping(gradeMappingId).ifPresentOrElse(gm -> {

            gm.setGradeMap(gradeMap);
            gradingPersistenceManager.saveGradeMapping(gm);
        }, () ->  log.warn("No grade mapping for id {}", gradeMappingId));
    }

    public CommentDefinition getAssignmentScoreComment(String gradebookUid, Long assignmentId, String studentUid) throws AssessmentNotFoundException {

        if (StringUtils.isBlank(gradebookUid) || assignmentId == null || StringUtils.isBlank(studentUid)) {
            throw new IllegalArgumentException("gradebookUid, assignmentId and studentUid must be valid.");
        }
        String assignmentName = "";
        final GradebookAssignment assignment = getAssignmentWithoutStatsByID(gradebookUid, assignmentId);
        if (assignment == null) {
            CourseGrade courseGrade = getCourseGrade(getGradebook(gradebookUid).getId());
            if (courseGrade != null && courseGrade.getId().equals(assignmentId)) {   //check if this is a course grade before declaring it Not Found
                assignmentName = courseGrade.getName();
            } else {
                throw new AssessmentNotFoundException("There is no assignmentId " + assignmentId + " for gradebookUid " + gradebookUid);
            }
        } else {
            assignmentName = assignment.getName();
        }

        String asnName = assignmentName;
        return gradingPersistenceManager.getInternalComment(studentUid, gradebookUid, assignmentId).map(comment -> {

            CommentDefinition commentDefinition = new CommentDefinition();
            commentDefinition.setAssignmentName(asnName);
            commentDefinition.setCommentText(comment.getCommentText());
            commentDefinition.setDateRecorded(comment.getDateRecorded());
            commentDefinition.setGraderUid(comment.getGraderId());
            commentDefinition.setStudentUid(comment.getStudentId());
            return commentDefinition;
        }).orElse(null);
    }

    public void setAssignmentScoreComment(String gradebookUid, Long assignmentId, String studentUid, @Nullable String commentText) throws AssessmentNotFoundException {

        if (StringUtils.isBlank(gradebookUid) || assignmentId == null || StringUtils.isBlank(studentUid)) {
            throw new IllegalArgumentException("gradebookUid, assignmentId and studentUid must be valid.");
        }

        GradebookAssignment gradebookColumn = getAssignmentWithoutStatsByID(gradebookUid, assignmentId);

        final Optional<Comment> optComment = gradingPersistenceManager.getInternalComment(studentUid, gradebookUid, assignmentId);
        Comment comment = null;
        if (optComment.isEmpty()) {
            comment = new Comment(studentUid, commentText, gradebookColumn);
            if (gradebookColumn == null) {  //will happen if we are commenting on Course Grade
                CourseGrade courseGrade = getCourseGrade(getGradebook(gradebookUid).getId());
                if (courseGrade != null && courseGrade.getId().equals(assignmentId)) {   //make sure ID is actually making reference to the course grade
                    comment = new Comment(studentUid, commentText, courseGrade);
                }
            }
        } else {
            comment = optComment.get();
            comment.setCommentText(commentText);
        }
        comment.setGraderId(sessionManager.getCurrentSessionUserId());
        comment.setDateRecorded(new Date());
        gradingPersistenceManager.saveComment(comment);

        // Get score to send with comment event
        if (gradebookColumn != null ) {
            Double pointsEarned = 0.0;
            AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(gradebookColumn, studentUid);
            if (gradeRecord != null) {
                pointsEarned = gradeRecord.getPointsEarned();
            }
            postUpdateCommentEvent(gradebookUid, assignmentId.toString(), studentUid, pointsEarned);
        }
    }

    public void deleteAssignmentScoreComment(String gradebookUid, Long assignmentId, String studentUid) throws AssessmentNotFoundException {

        gradingPersistenceManager.deleteInternalComment(studentUid, gradebookUid, assignmentId);
    }

    @Transactional
    @Override
    public Gradebook getGradebook(String uid) {
        return getGradebook(uid, uid);
    }

    @Transactional
    @Override
    public Gradebook getGradebook(String uid, String siteId)  {

        if (!isValidGradebookUid(uid)) {
            log.warn("must have a valid uid [{}]", uid);
            return null;
        }

        return gradingPersistenceManager.getGradebook(uid).orElseGet(() -> {

            String name = uid;
            if (!StringUtils.equals(uid, siteId)) {
                // gradebook by group
                Group group = siteService.findGroup(uid);
                if (group != null && group.getContainingSite().getId().equals(siteId)) {
                    name = MessageHelper.getString("group.gradebook", resourceLoader.getLocale())
                            + " "
                            + group.getTitle();
                } else {
                    log.warn("must have a valid group [{}]", uid);
                    return null;
                }
            }
            return addGradebook(uid, name);
        });
    }

    /**
     * Validates that a gradebook UID corresponds to either a valid site or an existing group.
     *
     * <p>This method performs a two-step validation process:</p>
     * <ol>
     *   <li>First checks if the UID represents an existing site</li>
     *   <li>If not a site, verifies the UID represents an existing group</li>
     * </ol>
     *
     * <p>The validation ensures that gradebook operations are only performed on legitimate
     * site or group contexts, preventing unauthorized access to gradebook data.</p>
     *
     * @param gradebookUid the unique identifier to validate; must not be null, empty, or blank
     * @return {@code true} if the gradebookUid represents either:
     *         <ul>
     *           <li>An existing site, or</li>
     *           <li>An existing group</li>
     *         </ul>
     *         {@code false} otherwise, including when gradebookUid is blank
     *
     * @implNote This method logs debug information indicating the type (Site/Group) and
     *           validation result for troubleshooting purposes
     *
     * @see SiteService#siteExists(String)
     * @see SiteService#findGroup(String)
     */
    private boolean isValidGradebookUid(String gradebookUid) {
        boolean valid = false;
        char type = '-';

        if (StringUtils.isNotBlank(gradebookUid)) {
            if (siteService.siteExists(gradebookUid)) {
                // is a site
                type = 'S';
                valid = true;
            } else {
                // is a group
                Group group = siteService.findGroup(gradebookUid);
                if (group != null) {
                    type = 'G';
                    valid = true;
                }
            }
        }
        log.debug("gradebook uid [{}:{}] is valid: [{}]", type, gradebookUid, valid);
        return valid;
    }

    private List<GradebookAssignment> getAssignments(Long gradebookId) {
        return gradingPersistenceManager.getAssignmentsForGradebook(gradebookId);
    }

    private List<GradebookAssignment> getAssignmentsByGradebookAndCategoryId(Long gradebookId, Long categoryId) {
        return gradingPersistenceManager.getAssignmentsForGradebookAndCategoryId(gradebookId, categoryId);
    }

    public String getGradebookUid(final Long id) {

        return gradingPersistenceManager.getGradebook(id).map(Gradebook::getUid).orElseGet(() -> {
            log.warn("No gradebook for id {}", id);
            return null;
        });
    }

    @Deprecated
    private GradebookAssignment getAssignmentWithoutStats(String gradebookUid, String assignmentName) {
        // Check if assignmentName is really an assignmentId. If not get assignment by assignmentName (i.e., title).
        if (NumberUtils.isCreatable(assignmentName)) {
            final Long assignmentId = new Long(NumberUtils.toLong(assignmentName));
            return getAssignmentWithoutStatsByID(gradebookUid, new Long(assignmentId));
        }
        return gradingPersistenceManager.getAssignmentByNameAndGradebook(assignmentName, gradebookUid).orElse(null);
    }

    private GradebookAssignment getAssignmentWithoutStatsByID(String gradebookUid, Long assignmentId) {
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

    private Category getCategory(Long categoryId) {
        return gradingPersistenceManager.getCategory(categoryId).get();
    }

    public Optional<CategoryDefinition> getCategoryDefinition(Long categoryId, String siteId) {
        return gradingPersistenceManager.getCategory(categoryId).map(ca -> buildCategoryDefinition(ca, siteId));
    }

    public void updateCategory(CategoryDefinition def) {

        gradingPersistenceManager.getCategory(def.getId()).ifPresentOrElse(ca -> {
            gradingPersistenceManager.saveCategory(updateCategoryFromDefinition(ca, def));
        }, () -> log.error("No category for id {}. This is not right ...", def.getId()));
    }

    public void updateCategory(Category category) throws ConflictingCategoryNameException, StaleObjectModificationException {

        if (gradingPersistenceManager.existsDuplicateCategory(category.getName(), category.getGradebook(), category.getId())) {
            throw new ConflictingCategoryNameException("You can not save multiple category in a gradebook with the same name");
        }
        if (category.getWeight().doubleValue() > 1 || category.getWeight().doubleValue() < 0) {
            throw new IllegalArgumentException("weight for category is greater than 1 or less than 0 in updateCategory of BaseHibernateManager");
        }
        gradingPersistenceManager.saveCategory(category);
    }

    public void removeCategory(Long categoryId) throws StaleObjectModificationException{

        gradingPersistenceManager.getCategory(categoryId).ifPresentOrElse(ca -> {

            getAssignmentsForCategory(ca.getId()).forEach(a -> {

                a.setCategory(null);
                updateAssignment(a);
            });

            ca.setRemoved(true);
            gradingPersistenceManager.saveCategory(ca);
        }, () -> log.error("No category for id {}", categoryId));
    }

    private Optional<LetterGradePercentMapping> getDefaultLetterGradePercentMapping() {

        List<LetterGradePercentMapping> mappings = gradingPersistenceManager.getDefaultLetterGradePercentMappings();

        if (mappings.size() == 0) {
            log.info("Default letter grade mapping hasn't been created in DB in getDefaultLetterGradePercentMapping");
            return Optional.<LetterGradePercentMapping>empty();
        }

        if (mappings.size() > 1) {
            log.error("Duplicate default letter grade mapping was created in DB in getDefaultLetterGradePercentMapping");
            return Optional.<LetterGradePercentMapping>empty();
        }

        return Optional.of(mappings.get(0));
    }

    private void createOrUpdateDefaultLetterGradePercentMapping(Map gradeMap) {

        if (gradeMap == null) {
            throw new IllegalArgumentException("gradeMap is null in createOrUpdateDefaultLetterGradePercentMapping");
        }

        getDefaultLetterGradePercentMapping().ifPresentOrElse(lgpm -> {
            updateDefaultLetterGradePercentMapping(gradeMap, lgpm);
        }, () -> createDefaultLetterGradePercentMapping(gradeMap));
    }

    private void updateDefaultLetterGradePercentMapping(final Map<String, Double> gradeMap, final LetterGradePercentMapping lgpm) {

        if (!validateLetterGradeMapping(gradeMap)) {
            throw new IllegalArgumentException("gradeMap invalid in updateDefaultLetterGradePercentMapping");
        }

        lgpm.setGradeMap(gradeMap);
        gradingPersistenceManager.saveLetterGradePercentMapping(lgpm);
    }

    public void createDefaultLetterGradePercentMapping(final Map<String, Double> gradeMap) {

        if (getDefaultLetterGradePercentMapping().isPresent()) {
            throw new IllegalArgumentException("gradeMap has already been created in createDefaultLetterGradePercentMapping");
        }

        if (!validateLetterGradeMapping(gradeMap)) {
            throw new IllegalArgumentException("gradeMap invalid  in createDefaultLetterGradePercentMapping");
        }

        final LetterGradePercentMapping lgpm = new LetterGradePercentMapping();
        final Map<String, Double> saveMap = new HashMap<>(gradeMap);
        lgpm.setGradeMap(saveMap);
        lgpm.setMappingType(1);
        gradingPersistenceManager.saveLetterGradePercentMapping(lgpm);
    }

    public LetterGradePercentMapping getLetterGradePercentMapping(Gradebook gradebook) {

        return gradingPersistenceManager.getLetterGradePercentMappingForGradebook(gradebook.getId()).orElseGet(() -> {

            LetterGradePercentMapping lgpm = getDefaultLetterGradePercentMapping().get();
            LetterGradePercentMapping returnLgpm = new LetterGradePercentMapping();
            returnLgpm.setGradebookId(gradebook.getId());
            returnLgpm.setGradeMap(lgpm.getGradeMap());
            returnLgpm.setMappingType(2);
            return returnLgpm;
        });
    }

    /**
     * this method is different with getLetterGradePercentMapping -
     * it returns null if no mapping exists for gradebook instead of
     * returning default mapping.
     */
    private LetterGradePercentMapping getLetterGradePercentMappingForGradebook(Gradebook gradebook) {
        return gradingPersistenceManager.getLetterGradePercentMappingForGradebook(gradebook.getId()).orElse(null);
    }

    public void saveOrUpdateLetterGradePercentMapping(Map<String, Double> gradeMap, Gradebook gradebook) {

        if (!validateLetterGradeMapping(gradeMap)) {
            throw new IllegalArgumentException("gradeMap invalid in saveOrUpdateLetterGradePercentMapping");
        }

        LetterGradePercentMapping lgpm = getLetterGradePercentMappingForGradebook(gradebook);

        if (lgpm == null) {
            LetterGradePercentMapping lgpm1 = new LetterGradePercentMapping();
            Map<String, Double> saveMap = new HashMap<>(gradeMap);
            lgpm1.setGradeMap(saveMap);
            lgpm1.setGradebookId(gradebook.getId());
            lgpm1.setMappingType(2);
            gradingPersistenceManager.saveLetterGradePercentMapping(lgpm1);
        } else {
            updateLetterGradePercentMapping(gradeMap, gradebook);
        }
    }

    private void updateLetterGradePercentMapping(final Map<String, Double> gradeMap, final Gradebook gradebook) {

        LetterGradePercentMapping lgpm = getLetterGradePercentMapping(gradebook);

        if (lgpm == null) {
            throw new IllegalArgumentException("LetterGradePercentMapping is null in updateLetterGradePercentMapping");
        }
        if (!validateLetterGradeMapping(gradeMap)) {
            throw new IllegalArgumentException("gradeMap invalid  in updpateLetterGradePercentMapping");
        }
        lgpm.setGradeMap(new HashMap<>(gradeMap));
        gradingPersistenceManager.saveLetterGradePercentMapping(lgpm);
    }

    private boolean validateLetterGradeMapping(Map<String, Double> gradeMap) {

        if (gradeMap == null
                || gradeMap.keySet().size() != GradingConstants.validLetterGrade.length) {
            return false;
        }

        for (final String key : gradeMap.keySet()) {
            boolean validLetter = false;
            for (final String element : GradingConstants.validLetterGrade) {
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

    /**
     *
     * @param id
     * @return the GradebookAssignment object with the given id
     */
    @Override
    public GradebookAssignment getGradebookAssigment(String siteId, Long assignmentId) {

        if (assignmentId == null || siteId == null) {
            throw new IllegalArgumentException("null parameter passed to getAssignment. Values are assignmentId:" + assignmentId + " siteId:" + siteId);
        }
        if (!isUserAbleToViewAssignments(siteId) && !currentUserHasViewOwnGradesPerm(siteId)) {
            log.warn("AUTHORIZATION FAILURE: User {} in site {} attempted to get assignment with id {}", getUserUid(), siteId, assignmentId);
            throw new GradingSecurityException();
        }

        GradebookAssignment assignment = gradingPersistenceManager.getAssignmentById(assignmentId).orElse(null);

        if (assignment == null) {
            throw new AssessmentNotFoundException("No gradebook item exists with gradable object id = " + assignmentId);
        }

        return assignment;
    }

    @Override
    public String getGradebookUidByAssignmentById(String siteId, Long assignmentId) {
        return getAssignmentById(siteId, assignmentId).getContext();
    }

    private static final String GB_GROUP_SITE_PROPERTY = "gradebook_group";
    private static final String GB_GROUP_TOOL_PROPERTY = "gb-group";

    @Override
    public boolean isGradebookGroupEnabled(String siteId) {
        Cache<String, Boolean> gradebookGroupEnabled = memoryService.getCache(gradebookGroupEnabledCache);

        if (gradebookGroupEnabled != null && gradebookGroupEnabled.containsKey(siteId)) {
            log.debug(buildCacheLogDebug("cacheKeyFound", gradebookGroupEnabledCache));
            Boolean groupEnabledCacheValue = gradebookGroupEnabled.get(siteId);

            if (groupEnabledCacheValue != null) {
                log.debug(buildCacheLogDebug("cacheValueFound", gradebookGroupEnabledCache));
                return (boolean) groupEnabledCacheValue;
            }
        }

        try {
            final Site site = this.siteService.getSite(siteId);
            boolean enabled = Boolean.parseBoolean(site.getProperties().getProperty(GB_GROUP_SITE_PROPERTY));

            log.debug(buildCacheLogDebug("noCacheValueFound", gradebookGroupEnabledCache));
            log.debug(buildCacheLogDebug("saveNewCacheValue", gradebookGroupEnabledCache));
            gradebookGroupEnabled.put(siteId, enabled);
            return enabled;
        } catch (IdUnusedException idue) {
            log.warn("No site for id {}", siteId);
        }
        return false;
    }

    @Override
    public List<Gradebook> getGradebookGroupInstances(String siteId) {
        Cache<String, List<Gradebook>> gradebookGroupInstances = memoryService.getCache(gradebookGroupInstancesCache);

        if (gradebookGroupInstances != null && gradebookGroupInstances.containsKey(siteId)) {
            log.debug(buildCacheLogDebug("cacheKeyFound", gradebookGroupInstancesCache));
            List<Gradebook> gradebookGroupInstanceList = gradebookGroupInstances.get(siteId);

            if (gradebookGroupInstanceList != null) {
                log.debug(buildCacheLogDebug("cacheValueFound", gradebookGroupInstancesCache));

                return gradebookGroupInstanceList;
            }
        }

        List<Gradebook> gbList = new ArrayList<>();

        try {
            final Site site = this.siteService.getSite(siteId);
            Collection<ToolConfiguration> gbs = site.getTools("sakai.gradebookng");
            for (ToolConfiguration tc : gbs) {
                Properties props = tc.getPlacementConfig();
                String groupId = props.getProperty(GB_GROUP_TOOL_PROPERTY);
                if (groupId != null) {
                    log.debug("Detected gradebook for group {}", groupId);
                    gradingPersistenceManager.getGradebook(props.getProperty(GB_GROUP_TOOL_PROPERTY))
                      .ifPresentOrElse(gbList::add, () -> {
                        Gradebook createdGb = getGradebook(groupId, siteId);
                        if (createdGb != null) {
                            log.debug("Gradebook added for groupId={}", groupId);
                            gbList.add(createdGb);
                        } else {
                            log.warn("Gradebook not found in DB for groupId '{}'", groupId);
                        }
                    });
                }
            }
        } catch (IdUnusedException idue) {
            log.warn("No site for id {}", siteId);
        }

        log.debug(buildCacheLogDebug("noCacheValueFound", gradebookGroupInstancesCache));
        log.debug(buildCacheLogDebug("saveNewCacheValue", gradebookGroupInstancesCache));
        gradebookGroupInstances.put(siteId, gbList);
        return gbList;
    }

    @Override
    public List<String> getGradebookGroupInstancesIds(String siteId) {
        return getGradebookGroupInstances(siteId).stream()
                .map(Gradebook::getUid)
                .collect(Collectors.toList());
    }

    @Override
    public Assignment getAssignmentById(String siteId, Long assignmentId) {

        if (assignmentId == null || siteId == null) {
            throw new IllegalArgumentException("null parameter passed to getAssignment. Values are assignmentId:" + assignmentId + " siteId:" + siteId);
        }
        if (!isUserAbleToViewAssignments(siteId) && !currentUserHasViewOwnGradesPerm(siteId)) {
            log.warn("AUTHORIZATION FAILURE: User {} in site {} attempted to get assignment with id {}", getUserUid(), siteId, assignmentId);
            throw new GradingSecurityException();
        }

        GradebookAssignment assignment = gradingPersistenceManager.getAssignmentById(assignmentId).orElse(null);

        if (assignment == null) {
            throw new AssessmentNotFoundException("No gradebook item exists with gradable object id = " + assignmentId);
        }

        return getAssignmentDefinition(assignment, false);
    }

    // Possible new param to do log, warn or info instead of retriving message
    // I18n feature implementation replacing strings to messageProperties
    private String buildCacheLogDebug(String type, String cacheKey) {
        if (type != null && !StringUtils.isBlank(type) && cacheKey != null && !StringUtils.isBlank(cacheKey)) {
            switch (type) {
                case "creatingCache":
                    return "Creating cache with key '" + cacheKey + "'";
                case "cacheKeyFound":
                    return "Found cache key for '" + cacheKey + "'";
                case "cacheValueFound":
                    return "Found cache value for '" + cacheKey + "'";
                case "noCacheValueFound":
                    return "No cache value founded for '" + cacheKey + "'";
                case "saveNewCacheValue":
                    return "Saving new value for cache key '" + cacheKey + "'";
                default:
                    return "ERROR BUILDING CACHE LOG DEBUG ON GRADING SERVICE IMPL (INVALID TYPE)";
            }
        } else {
            return "ERROR BUILDING CACHE LOG DEBUG ON GRADING SERVICE IMPL (EMPTY PARAMETERS OR NULL)";
        }
    }

    @Override
    public List<String> getGradebookInstancesForUser(String siteId, String userId) {
        if (!isGradebookGroupEnabled(siteId)) {
            return List.of(siteId);
        }
        List<Gradebook> allGradebooks = getGradebookGroupInstances(siteId);
        List<String> userGradebooks = new ArrayList<>();
        try {
            final Site s = siteService.getSite(siteId);
            for (Gradebook group : allGradebooks) {
                final Group g = s.getGroup(group.getUid());
                if (g != null && g.getMember(userId) != null) {
                    userGradebooks.add(group.getUid());
                }
            }
        } catch (final Exception e) {
            log.error("Error in getGradebookInstancesForUser: ", e);
        }
        return userGradebooks;
    }

    @Override
    public void initializeGradebooksForSite(String siteId) {

        //List<String> gradebookUids = isGradebookGroupEnabled(siteId) ? getGradebookGroupInstancesIds(siteId) : List.of(siteId);
        (isGradebookGroupEnabled(siteId) ? getGradebookGroupInstancesIds(siteId) : List.of(siteId)).forEach(uid -> getGradebook(uid, siteId));
        //gradebookUids.forEach(uid -> getGradebook(uid, siteId));
        //}
    }

    private void createDefaultLetterGradeMapping(Map<String, Double> gradeMap) {

        if (!getDefaultLetterGradePercentMapping().isEmpty()) return;

        if (!validateLetterGradeMapping(gradeMap)) {
            throw new IllegalArgumentException("gradeMap invalid in createDefaultLetterGradePercentMapping");
        }

        LetterGradePercentMapping lgpm = new LetterGradePercentMapping();
        lgpm.setGradeMap(new HashMap<>(gradeMap));
        lgpm.setMappingType(1);
        gradingPersistenceManager.saveLetterGradePercentMapping(lgpm);
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

    private Set<String> getAllStudentUids(final String siteId) {

        final List<EnrollmentRecord> enrollments = sectionAwareness.getSiteMembersInRole(siteId, Role.STUDENT);
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

    // What this part does is search in one gradebook or several (if isGradebookGroupEnabled is active)
    // for the category that contains the assignment. When it finds it, it retrieves the points for the
    // category (category.getPointsForCategory()) and stores them in a HashMap that keeps track
    // of the gradebook + category score. In the end, instead of using the old Double,
    // we will iterate through the HashMap, using the Double from each entry, and perform
    // the same check as before.
    @Override
    public void buildGradebookPointsMap(String gbUid, String siteId, String assignmentRef,
    Map<String, Double> gradebookPointsMap, String newCategoryString) {
        Long catRef = -1L;

        List<CategoryDefinition> categoryDefinitions = getCategoryDefinitions(gbUid, siteId);
        if (!newCategoryString.equals("-1") || assignmentRef.isEmpty()) {
            // NO DEBERÍA EJECUTARSE
            // TODO JUANMA CATEGORIA VACIA
            // catRefList = newCategoryString;
        } else {
            for (CategoryDefinition categorie : categoryDefinitions) {
                if (categorie.isAssignmentInThisCategory(assignmentRef)) {
                    catRef = categorie.getId();
                }
            }
        }

        if (catRef != -1) {
            for (CategoryDefinition thisCategoryDefinition : categoryDefinitions) {
                if (Objects.equals(thisCategoryDefinition.getId(), catRef)) {
                    if (thisCategoryDefinition.getDropKeepEnabled() && !thisCategoryDefinition.getEqualWeight()) {
                        Double thisCategoryPoints = thisCategoryDefinition.getPointsForCategory();
                        if (thisCategoryPoints != null) {
                            gradebookPointsMap.put(gbUid, thisCategoryPoints);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean checkMultiSelectorList(String siteId, List<String> groupList, List<String> multiSelectorList, boolean isCategory) {
        if (isCategory) {
            for (String categoryId : multiSelectorList) {
                if (!categoryId.isEmpty() && !categoryId.isBlank()) {
                    boolean isCategoryInGradebook = false;

                    for (String groupId : groupList) {
                        List<CategoryDefinition> categoryDefinitionList = getCategoryDefinitions(groupId, siteId);

                        boolean foundCategory = categoryDefinitionList.stream()
                            .anyMatch(category -> category.getId().equals(Long.parseLong(categoryId)));

                        if (foundCategory) {
                            isCategoryInGradebook = true;
                            break;
                        }
                    }

                    if (!isCategoryInGradebook) {
                        return false;
                    }
                }
            }
        } else {
            List<String> gbUidList = new ArrayList<>();

            for (String gbItem : multiSelectorList) {
                if (StringUtils.isNotBlank(gbItem)) {
                    String gbUid = getGradebookUidByAssignmentById(siteId, Long.parseLong(gbItem));
                    gbUidList.add(gbUid);
                }
            }

            Collections.sort(gbUidList);
            Collections.sort(groupList);

            boolean areEqual = gbUidList.equals(groupList);

            if (!areEqual) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Map<String, String> buildCategoryGradebookMap(List<String> selectedGradebookUids, String categoriesString, String siteId) {
        Map<String, String> gradebookCategoryMap = new HashMap<>();

        if (categoriesString == null || categoriesString.isBlank()) {
            selectedGradebookUids.forEach(gbUid -> gradebookCategoryMap.put(gbUid, "-1"));
        } else {
            List<String> selectedCategories = Arrays.asList(categoriesString.split(","));

            for (String gbUid : selectedGradebookUids) {
                List<CategoryDefinition> categoryDefinitions = getCategoryDefinitions(gbUid, siteId);

                String categoryId = categoryDefinitions.stream()
                    .filter(category -> selectedCategories.contains(category.getId().toString()))
                    .map(category -> category.getId().toString())
                    .findFirst()
                    .orElse("-1");

                    gradebookCategoryMap.put(gbUid, categoryId);
            }
        }

        return gradebookCategoryMap;
    }

    public Long getMatchingUserGradebookItemId(String siteId, String userId, String gradebookItemIdString) {
        List<String> userGradebookList = getGradebookInstancesForUser(siteId, userId);
        List<String> gradebookItemList = Arrays.asList(gradebookItemIdString.split(","));

        for (String gradebookItem : gradebookItemList) {
            String foundGradebookUid = getGradebookUidByAssignmentById(siteId,
                Long.parseLong(gradebookItem));

            if (userGradebookList.contains(foundGradebookUid)) {
                Long gradebookItemId = Long.valueOf(gradebookItem);
                return gradebookItemId;
            }
        }

        return null;
    }

    @Override
    public void hardDeleteGradebook(String siteId) {

        try {
            deleteGradebook(siteId);
        } catch (Exception e) {
            log.warn("Could not hard delete gradebook for context {}", siteId, e);
        }
    }
}
