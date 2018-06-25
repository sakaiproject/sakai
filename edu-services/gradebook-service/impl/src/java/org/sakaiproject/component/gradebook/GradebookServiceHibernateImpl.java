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

package org.sakaiproject.component.gradebook;

import java.math.BigDecimal;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.criterion.Restrictions;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.hibernate.HibernateCriterionUtils;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.CategoryScoreData;
import org.sakaiproject.service.gradebook.shared.CommentDefinition;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingCategoryNameException;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradeMappingDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookHelper;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookPermissionService;
import org.sakaiproject.service.gradebook.shared.GradebookSecurityException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.InvalidGradeException;
import org.sakaiproject.service.gradebook.shared.SortType;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.service.gradebook.shared.exception.UnmappableCourseGradeOverrideException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.LetterGradePercentMapping;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.util.ResourceLoader;

import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException;

/**
 * A Hibernate implementation of GradebookService.
 */
@Slf4j
public class GradebookServiceHibernateImpl extends BaseHibernateManager implements GradebookService {

    private Authz authz;
    private GradebookPermissionService gradebookPermissionService;
    protected SiteService siteService;

    @Override
	public boolean isAssignmentDefined(final String gradebookUid, final String assignmentName) {
		if (!isUserAbleToViewAssignments(gradebookUid)) {
			log.warn("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to check for assignment {}", getUserUid(), gradebookUid, assignmentName);
			throw new GradebookSecurityException();
		}
        @SuppressWarnings("unchecked")
		final
        GradebookAssignment assignment = (GradebookAssignment)getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(final Session session) {
				return getAssignmentWithoutStats(gradebookUid, assignmentName);
			}
		});
        return (assignment != null);
    }

	private boolean isUserAbleToViewAssignments(final String gradebookUid) {
		final Authz authz = getAuthz();
		return (authz.isUserAbleToEditAssessments(gradebookUid) || authz.isUserAbleToGrade(gradebookUid));
	}

	@Override
	public boolean isUserAbleToGradeItemForStudent(final String gradebookUid, final Long itemId, final String studentUid) {
		return getAuthz().isUserAbleToGradeItemForStudent(gradebookUid, itemId, studentUid);
	}

	@Override
	public boolean isUserAbleToViewItemForStudent(final String gradebookUid, final Long itemId, final String studentUid) {
		return getAuthz().isUserAbleToViewItemForStudent(gradebookUid, itemId, studentUid);
	}

	@Override
	public String getGradeViewFunctionForUserForStudentForItem(final String gradebookUid, final Long itemId, final String studentUid) {
		return getAuthz().getGradeViewFunctionForUserForStudentForItem(gradebookUid, itemId, studentUid);
	}

	@Override
	public List<org.sakaiproject.service.gradebook.shared.Assignment> getAssignments(final String gradebookUid) throws GradebookNotFoundException {
		return getAssignments(gradebookUid, SortType.SORT_BY_NONE);
	}

	@Override
	public List<org.sakaiproject.service.gradebook.shared.Assignment> getAssignments(final String gradebookUid, final SortType sortBy) throws GradebookNotFoundException {
			if (!isUserAbleToViewAssignments(gradebookUid)) {
				log.warn("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to get assignments list", getUserUid(), gradebookUid);
				throw new GradebookSecurityException();
			}

			final Long gradebookId = getGradebook(gradebookUid).getId();

			final List<GradebookAssignment> internalAssignments = getAssignments(gradebookId);

	        sortAssignments(internalAssignments, sortBy, true);

			final List<org.sakaiproject.service.gradebook.shared.Assignment> assignments = new ArrayList<>();
			for (final GradebookAssignment gradebookAssignment : internalAssignments) {
				final GradebookAssignment assignment = gradebookAssignment;
				assignments.add(getAssignmentDefinition(assignment));
			}
			return assignments;
		}

	@Override
	public org.sakaiproject.service.gradebook.shared.Assignment getAssignment(final String gradebookUid, final Long assignmentId) throws AssessmentNotFoundException {
		if (assignmentId == null || gradebookUid == null) {
			throw new IllegalArgumentException("null parameter passed to getAssignment. Values are assignmentId:"
				+ assignmentId + " gradebookUid:" + gradebookUid);
		}
		if (!isUserAbleToViewAssignments(gradebookUid) && !currentUserHasViewOwnGradesPerm(gradebookUid)) {
			log.warn("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to get assignment with id {}", getUserUid(), gradebookUid, assignmentId);
			throw new GradebookSecurityException();
			}

		GradebookAssignment assignment = getAssignmentWithoutStatsByID(gradebookUid, assignmentId);

		if (assignment == null) {
			throw new AssessmentNotFoundException("No gradebook item exists with gradable object id = " + assignmentId);
		}

		return getAssignmentDefinition(assignment);
	}

	@Override
	@Deprecated
	public org.sakaiproject.service.gradebook.shared.Assignment getAssignment(final String gradebookUid, final String assignmentName) throws AssessmentNotFoundException {
		if (assignmentName == null || gradebookUid == null) {
			throw new IllegalArgumentException("null parameter passed to getAssignment. Values are assignmentName:"
					+ assignmentName + " gradebookUid:" + gradebookUid);
		}
		if (!isUserAbleToViewAssignments(gradebookUid) && !currentUserHasViewOwnGradesPerm(gradebookUid)) {
			log.warn("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to get assignment {}", getUserUid(), gradebookUid, assignmentName);
			throw new GradebookSecurityException();		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		final
        GradebookAssignment assignment = (GradebookAssignment)getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(final Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, assignmentName);
			}
		});

		if (assignment != null) {
			return getAssignmentDefinition(assignment);
		} else {
			return null;
		}
	}

	@Override
	public org.sakaiproject.service.gradebook.shared.Assignment getAssignmentByNameOrId(final String gradebookUid,
			final String assignmentName) throws AssessmentNotFoundException {

		org.sakaiproject.service.gradebook.shared.Assignment assignment = null;
		try {
			assignment = getAssignment(gradebookUid, assignmentName);
		}
		catch (final AssessmentNotFoundException e) {
			//Don't fail on this exception
			log.debug("Assessment not found by name", e);
		}

		if (assignment == null) {
			//Try to get the assignment by id
			if (NumberUtils.isCreatable(assignmentName)) {
				final Long assignmentId = NumberUtils.toLong(assignmentName, -1L);
				return getAssignment(gradebookUid, assignmentId);
			}
		}
		return assignment;
	}

	private org.sakaiproject.service.gradebook.shared.Assignment getAssignmentDefinition(final GradebookAssignment internalAssignment) {
		final org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition = new org.sakaiproject.service.gradebook.shared.Assignment();
    	assignmentDefinition.setName(internalAssignment.getName());
    	assignmentDefinition.setPoints(internalAssignment.getPointsPossible());
    	assignmentDefinition.setDueDate(internalAssignment.getDueDate());
    	assignmentDefinition.setCounted(internalAssignment.isCounted());
    	assignmentDefinition.setExternallyMaintained(internalAssignment.isExternallyMaintained());
    	assignmentDefinition.setExternalAppName(internalAssignment.getExternalAppName());
    	assignmentDefinition.setExternalId(internalAssignment.getExternalId());
    	assignmentDefinition.setReleased(internalAssignment.isReleased());
    	assignmentDefinition.setId(internalAssignment.getId());
    	assignmentDefinition.setExtraCredit(internalAssignment.isExtraCredit());
    	if(internalAssignment.getCategory() != null) {
    		assignmentDefinition.setCategoryName(internalAssignment.getCategory().getName());
    		assignmentDefinition.setWeight(internalAssignment.getCategory().getWeight());
    		assignmentDefinition.setCategoryExtraCredit(internalAssignment.getCategory().isExtraCredit());
    		assignmentDefinition.setCategoryId(internalAssignment.getCategory().getId());
    		assignmentDefinition.setCategoryOrder(internalAssignment.getCategory().getCategoryOrder());
    	}
    	assignmentDefinition.setUngraded(internalAssignment.getUngraded());
    	assignmentDefinition.setSortOrder(internalAssignment.getSortOrder());
    	assignmentDefinition.setCategorizedSortOrder(internalAssignment.getCategorizedSortOrder());

    	return assignmentDefinition;
    }



	@Override
	public GradeDefinition getGradeDefinitionForStudentForItem(final String gradebookUid, final Long assignmentId, final String studentUid) {

		if (gradebookUid == null || assignmentId == null || studentUid == null) {
			throw new IllegalArgumentException("Null paraemter passed to getGradeDefinitionForStudentForItem");
		}

		//studentId can be a groupId (from Assignments)
		final boolean studentRequestingOwnScore = this.authn.getUserUid().equals(studentUid) || isCurrentUserFromGroup(gradebookUid, studentUid);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		final
		GradeDefinition gradeDef = (GradeDefinition)getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(final Session session) throws HibernateException {

				final GradebookAssignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentId);

				if (assignment == null) {
					throw new AssessmentNotFoundException("There is no assignment with the assignmentId " + assignmentId + " in gradebook " + gradebookUid);
				}

				if (!studentRequestingOwnScore && !isUserAbleToViewItemForStudent(gradebookUid, assignment.getId(), studentUid)) {
					log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to retrieve grade for student {} for assignment {}", getUserUid(), gradebookUid, studentUid, assignmentId);
					throw new GradebookSecurityException();
				}

				final Gradebook gradebook = assignment.getGradebook();

				final GradeDefinition gradeDef = new GradeDefinition();
				gradeDef.setStudentUid(studentUid);
				gradeDef.setGradeEntryType(gradebook.getGrade_type());
				gradeDef.setGradeReleased(assignment.isReleased());

				// If this is the student, then the global setting needs to be enabled and the assignment needs to have
				// been released. Return null score information if not released
				if (studentRequestingOwnScore && (!gradebook.isAssignmentsDisplayed() || !assignment.isReleased())) {
					gradeDef.setDateRecorded(null);
					gradeDef.setGrade(null);
					gradeDef.setGraderUid(null);
					gradeDef.setGradeComment(null);
					log.debug("Student {} in gradebook {} retrieving score for unreleased assignment {}", getUserUid(), gradebookUid, assignment.getName());

				} else {

					final AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid);
					final CommentDefinition gradeComment = getAssignmentScoreComment(gradebookUid, assignmentId, studentUid);
					final String commentText = gradeComment != null ? gradeComment.getCommentText() : null;
					if (log.isDebugEnabled()) {
						log.debug("gradeRecord=" + gradeRecord);
					}

					if (gradeRecord == null) {
						gradeDef.setDateRecorded(null);
						gradeDef.setGrade(null);
						gradeDef.setGraderUid(null);
						gradeDef.setGradeComment(commentText);
					} else {
						gradeDef.setDateRecorded(gradeRecord.getDateRecorded());
						gradeDef.setGraderUid(gradeRecord.getGraderId());
						gradeDef.setGradeComment(commentText);

						if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
							final List<AssignmentGradeRecord> gradeList = new ArrayList<>();
							gradeList.add(gradeRecord);
							convertPointsToLetterGrade(gradebook, gradeList);
							final AssignmentGradeRecord gradeRec = gradeList.get(0);
							if (gradeRec != null) {
								gradeDef.setGrade(gradeRec.getLetterEarned());
							}
						} else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
							final Double percent = calculateEquivalentPercent(assignment.getPointsPossible(), gradeRecord.getPointsEarned());
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

				return gradeDef;
			}
		});
		if (log.isDebugEnabled()) {
			log.debug("returning grade def for " + studentUid);
		}
		return gradeDef;
	}

	@Override
	public String getGradebookDefinitionXml(final String gradebookUid) {
		final Long gradebookId = getGradebook(gradebookUid).getId();
		final Gradebook gradebook = getGradebook(gradebookUid);

		final GradebookDefinition gradebookDefinition = new GradebookDefinition();
		final GradeMapping selectedGradeMapping = gradebook.getSelectedGradeMapping();
		gradebookDefinition.setSelectedGradingScaleUid(selectedGradeMapping.getGradingScale().getUid());
		gradebookDefinition.setSelectedGradingScaleBottomPercents(new HashMap<>(selectedGradeMapping.getGradeMap()));
		gradebookDefinition.setAssignments(getAssignments(gradebookUid));

		gradebookDefinition.setGradeType(gradebook.getGrade_type());
		gradebookDefinition.setCategoryType(gradebook.getCategory_type());
		gradebookDefinition.setCategory(getCategories(gradebookId));

		return VersionedExternalizable.toXml(gradebookDefinition);
	}

	@Override
	public GradebookInformation getGradebookInformation(final String gradebookUid) {

		if (gradebookUid == null ) {
	          throw new IllegalArgumentException("null gradebookUid " + gradebookUid) ;
		}

        if (!currentUserHasEditPerm(gradebookUid) && !currentUserHasGradingPerm(gradebookUid)) {
            log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to access gb information", getUserUid(), gradebookUid);
            throw new GradebookSecurityException();
        }

		final Gradebook gradebook = getGradebook(gradebookUid);
		if(gradebook==null) {
			throw new IllegalArgumentException("Their is no gradbook associated with this Id: "+gradebookUid);
		}

		final GradebookInformation rval = new GradebookInformation();

		//add in all available grademappings for this gradebook
		rval.setGradeMappings(getGradebookGradeMappings(gradebook.getGradeMappings()));

		//add in details about the selected one
		final GradeMapping selectedGradeMapping = gradebook.getSelectedGradeMapping();
		if(selectedGradeMapping!=null) {

			rval.setSelectedGradingScaleUid(selectedGradeMapping.getGradingScale().getUid());
			rval.setSelectedGradeMappingId(Long.toString(selectedGradeMapping.getId()));

			//note that these are not the DEFAULT bottom percents but the configured ones per gradebook
			Map<String, Double> gradeMap = selectedGradeMapping.getGradeMap();
			gradeMap = GradeMappingDefinition.sortGradeMapping(gradeMap);
			rval.setSelectedGradingScaleBottomPercents(gradeMap);
			rval.setGradeScale(selectedGradeMapping.getGradingScale().getName());
		}

		rval.setGradeType(gradebook.getGrade_type());
		rval.setCategoryType(gradebook.getCategory_type());
		rval.setDisplayReleasedGradeItemsToStudents(gradebook.isAssignmentsDisplayed());

		//add in the category definitions
		rval.setCategories(getCategoryDefinitions(gradebookUid));

		//add in the course grade display settings
		rval.setCourseGradeDisplayed(gradebook.isCourseGradeDisplayed());
		rval.setCourseLetterGradeDisplayed(gradebook.isCourseLetterGradeDisplayed());
		rval.setCoursePointsDisplayed(gradebook.isCoursePointsDisplayed());
		rval.setCourseAverageDisplayed(gradebook.isCourseAverageDisplayed());

		return rval;
	}

	@SuppressWarnings("rawtypes")
	@Override
	@Deprecated
	public void transferGradebookDefinitionXml(final String fromGradebookUid, final String toGradebookUid, final String fromGradebookXml) {
		final Gradebook gradebook = getGradebook(toGradebookUid);
		final Gradebook fromGradebook = getGradebook(fromGradebookUid);

		final GradebookDefinition gradebookDefinition = (GradebookDefinition)VersionedExternalizable.fromXml(fromGradebookXml);

		gradebook.setCategory_type(gradebookDefinition.getCategoryType());
		gradebook.setGrade_type(gradebookDefinition.getGradeType());

		updateGradebook(gradebook);

		final List category = getCategories(fromGradebook.getId());

		int assignmentsAddedCount = 0;
		Long catId = null;
		int undefined_nb = 0;

		final List catList = gradebookDefinition.getCategory();
		final List<Category> catList_tempt = new ArrayList<>();

		if(!category.isEmpty()) {
			//deal with category with assignments
			for(final Iterator iter = category.iterator(); iter.hasNext();) {

				int categoryCount = 0;
				final String catName = ((Category)iter.next()).getName();

				for (final org.sakaiproject.service.gradebook.shared.Assignment obj : gradebookDefinition.getAssignments()) {
					final org.sakaiproject.service.gradebook.shared.Assignment assignmentDef = obj;

					boolean newCategory = false;
					// Externally managed assessments should not be included.
					if (assignmentDef.isExternallyMaintained()) {
						continue;
					}

					if(catName.equals(assignmentDef.getCategoryName())) {
						newCategory = true;
						categoryCount++;
					}

					if (assignmentDef.getCategoryName() != null) {
						if(!newCategory) {}
						else if(newCategory && categoryCount == 1) {
							catId = createCategory(gradebook.getId(), assignmentDef.getCategoryName(), assignmentDef.getWeight(), 0, 0, 0, assignmentDef.isCategoryExtraCredit());
							final Category catTempt = getCategory(catId);

							catList_tempt.add(catTempt);
							createAssignmentForCategory(gradebook.getId(), catId, assignmentDef.getName(), assignmentDef.getPoints(), assignmentDef.getDueDate(), true, false, assignmentDef.isExtraCredit());
							assignmentsAddedCount++;
						}
						else{
							createAssignmentForCategory(gradebook.getId(), catId, assignmentDef.getName(), assignmentDef.getPoints(), assignmentDef.getDueDate(), true, false, assignmentDef.isExtraCredit());
							assignmentsAddedCount++;
						}

					}
					//deal with assignments in undefined.
					else {
						if (undefined_nb == 0) {
							createAssignment(gradebook.getId(), assignmentDef.getName(), assignmentDef.getPoints(), assignmentDef.getDueDate(), true, false, assignmentDef.isExtraCredit());
							assignmentsAddedCount++;

						}
					}
				}
				undefined_nb++;
			}

			//deal with Category without assignments inside
			final Iterator it_tempt = catList_tempt.iterator();
			Iterator it = catList.iterator();

			Category cat_cat ;
			Category cat_tempt;

			while(it_tempt.hasNext()) {
				cat_tempt = (Category) it_tempt.next();
				while(it.hasNext()) {
					cat_cat = (Category) it.next();
					if(cat_tempt.getName().equals(cat_cat.getName())) {
						it.remove();
						it = catList.iterator();
					}
				}
				it = catList.iterator();
			}

			final Iterator itUpdate = catList.iterator();
			while(itUpdate.hasNext()){
				final Category catObj = (Category)itUpdate.next();
				createCategory(gradebook.getId(), catObj.getName(), catObj.getWeight(), catObj.getDropLowest(), catObj.getDropHighest(), catObj.getKeepHighest(), catObj.isExtraCredit());
			}
		}
		//deal with no categories
		else {
			for (final org.sakaiproject.service.gradebook.shared.Assignment obj : gradebookDefinition.getAssignments()) {
				final org.sakaiproject.service.gradebook.shared.Assignment assignmentDef = obj;

				// Externally managed assessments should not be included.
				if (assignmentDef.isExternallyMaintained()) {
					continue;
				}

				// All assignments should be unreleased even if they were released in the original.

				createAssignment(gradebook.getId(), assignmentDef.getName(), assignmentDef.getPoints(), assignmentDef.getDueDate(), true, false, assignmentDef.isExtraCredit());
				assignmentsAddedCount++;
			}

		}

		if (log.isInfoEnabled()) {
			log.info("Merge to gradebook " + toGradebookUid + " added " + assignmentsAddedCount + " assignments");
		}

			// Carry over the old gradebook's selected grading scheme if possible.
		final String fromGradingScaleUid = gradebookDefinition.getSelectedGradingScaleUid();
		MERGE_GRADE_MAPPING: if (!StringUtils.isEmpty(fromGradingScaleUid)) {
		for (final GradeMapping gradeMapping : gradebook.getGradeMappings()) {
				if (gradeMapping.getGradingScale().getUid().equals(fromGradingScaleUid)) {
					// We have a match. Now make sure that the grades are as expected.
					final Map<String, Double> inputGradePercents = gradebookDefinition.getSelectedGradingScaleBottomPercents();
					final Set<String> gradeCodes = inputGradePercents.keySet();
					if (gradeCodes.containsAll(gradeMapping.getGradeMap().keySet())) {
						// Modify the existing grade-to-percentage map.
						for (final String gradeCode : gradeCodes) {
							gradeMapping.getGradeMap().put(gradeCode, inputGradePercents.get(gradeCode));
						}
						gradebook.setSelectedGradeMapping(gradeMapping);
						updateGradebook(gradebook);
						if (log.isInfoEnabled()) {
							log.info("Merge to gradebook " + toGradebookUid + " updated grade mapping");
						}
					} else {
						if (log.isInfoEnabled()) {
							log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because the " + fromGradingScaleUid + " grade codes did not match");
						}
					}
					break MERGE_GRADE_MAPPING;
				}
			}
			// Did not find a matching grading scale.
			if (log.isInfoEnabled()) {
				log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because grading scale " + fromGradingScaleUid + " is not defined");
			}
		}
	}

	@Override
	public void transferGradebook(final GradebookInformation gradebookInformation, final List<org.sakaiproject.service.gradebook.shared.Assignment> assignments, final String toGradebookUid) {

		final Gradebook gradebook = getGradebook(toGradebookUid);

		gradebook.setCategory_type(gradebookInformation.getCategoryType());
		gradebook.setGrade_type(gradebookInformation.getGradeType());

		updateGradebook(gradebook);

		//all categories that we need to end up with
		final List<CategoryDefinition> categories = gradebookInformation.getCategories();

		//filter out externally managed assignments. These are never imported.
		assignments.removeIf(a -> a.isExternallyMaintained());

		//this map holds the names of categories that have been created in the site to the category ids
		//and is updated as we go along
		//likewise for list of assignments
		final Map<String, Long> categoriesCreated = new HashMap<>();
		final List<String> assignmentsCreated = new ArrayList<>();

		if(!categories.isEmpty()) {

			//migrate the categories with assignments
			categories.forEach(c -> {

				assignments.forEach(a -> {

					if(StringUtils.equals(c.getName(), a.getCategoryName())) {

						if(!categoriesCreated.containsKey(c.getName())) {

							//create category
							Long categoryId = null;
							try {
								categoryId = createCategory(gradebook.getId(), c.getName(), a.getWeight(), 0, 0, 0, a.isCategoryExtraCredit());
							} catch (final ConflictingCategoryNameException e) {
								//category already exists. Could be from a merge.
								log.info("Category: {} already exists in target site. Skipping creation.", c.getName());
							}

							if(categoryId == null) {
								//couldn't create so look up the id in the target site
								final List<CategoryDefinition> existingCategories = getCategoryDefinitions(gradebook.getUid());
								categoryId = existingCategories.stream().filter(e -> StringUtils.equals(e.getName(), c.getName())).findFirst().get().getId();
							}
							//record that we have created this category
							categoriesCreated.put(c.getName(), categoryId);

						}

						//create the assignment for the current category
						try {
							createAssignmentForCategory(gradebook.getId(), categoriesCreated.get(c.getName()), a.getName(), a.getPoints(), a.getDueDate(), true, false, a.isExtraCredit());
						} catch (final ConflictingAssignmentNameException e) {
							//assignment already exists. Could be from a merge.
							log.info("GradebookAssignment: {} already exists in target site. Skipping creation.", a.getName());
						}

						//record that we have created this assignment
						assignmentsCreated.add(a.getName());
					}
				});
			});

			//create any remaining categories that have no assignments
			categories.removeIf(c -> categoriesCreated.containsKey(c.getName()));
			categories.forEach(c -> {
				try {
					createCategory(gradebook.getId(), c.getName(), c.getWeight(), c.getDropLowest(), c.getDropHighest(), c.getKeepHighest(),
							c.isExtraCredit());
				} catch (final ConflictingCategoryNameException e) {
					//category already exists. Could be from a merge.
					log.info("Category: {} already exists in target site. Skipping creation.", c.getName());
				}
			});
		}

		//create any remaining assignments that have no categories
		assignments.removeIf(a -> assignmentsCreated.contains(a.getName()));
		assignments.forEach(a -> {
			try {
				createAssignment(gradebook.getId(), a.getName(), a.getPoints(), a.getDueDate(), true, false, a.isExtraCredit());
			} catch (final ConflictingAssignmentNameException e) {
				//assignment already exists. Could be from a merge.
				log.info("GradebookAssignment: {} already exists in target site. Skipping creation.", a.getName());
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
					if (gradeCodes.containsAll(gradeMapping.getGradeMap().keySet())) {
						// Modify the existing grade-to-percentage map.
						for (final String gradeCode : gradeCodes) {
							gradeMapping.getGradeMap().put(gradeCode, inputGradePercents.get(gradeCode));
						}
						gradebook.setSelectedGradeMapping(gradeMapping);
						updateGradebook(gradebook);
						log.info("Merge to gradebook {} updated grade mapping", toGradebookUid);
					} else {
						log.info("Merge to gradebook {} skipped grade mapping change because the {} grade codes did not match", toGradebookUid, fromGradingScaleUid);
					}
					break MERGE_GRADE_MAPPING;
				}
			}
			// Did not find a matching grading scale.
			log.info("Merge to gradebook {} skipped grade mapping change because grading scale {} is not defined", toGradebookUid, fromGradingScaleUid);
		}


	}

	@Override
	public void mergeGradebookDefinitionXml(final String toGradebookUid, final String fromGradebookXml) {
		final Gradebook gradebook = getGradebook(toGradebookUid);
		final GradebookDefinition gradebookDefinition = (GradebookDefinition)VersionedExternalizable.fromXml(fromGradebookXml);

		final List<String> assignmentNames = getHibernateTemplate().execute(session -> session
				.createQuery("select asn.name from GradebookAssignment as asn where asn.gradebook.id = :gradebookid and asn.removed is false")
				.setLong("gradebookid", gradebook.getId())
				.list());

		// Add any non-externally-managed assignments with non-duplicate names.
		int assignmentsAddedCount = 0;
		for (final org.sakaiproject.service.gradebook.shared.Assignment obj : gradebookDefinition.getAssignments()) {
			final org.sakaiproject.service.gradebook.shared.Assignment assignmentDef = obj;

			// Externally managed assessments should not be included.
			if (assignmentDef.isExternallyMaintained()) {
				continue;
			}

			// Skip any input assignments with duplicate names.
			if (assignmentNames.contains(assignmentDef.getName())) {
				if (log.isInfoEnabled()) {
					log.info("Merge to gradebook " + toGradebookUid + " skipped duplicate assignment named " + assignmentDef.getName());
				}
				continue;
			}

			// All assignments should be unreleased even if they were released in the original.
			createAssignment(gradebook.getId(), assignmentDef.getName(), assignmentDef.getPoints(), assignmentDef.getDueDate(), true, false, assignmentDef.isExtraCredit());
			assignmentsAddedCount++;
		}
		if (log.isInfoEnabled()) {
			log.info("Merge to gradebook " + toGradebookUid + " added " + assignmentsAddedCount + " assignments");
		}

		// Carry over the old gradebook's selected grading scheme if possible.
		final String fromGradingScaleUid = gradebookDefinition.getSelectedGradingScaleUid();
		MERGE_GRADE_MAPPING: if (!StringUtils.isEmpty(fromGradingScaleUid)) {
			for (final GradeMapping gradeMapping : gradebook.getGradeMappings()) {
				if (gradeMapping.getGradingScale().getUid().equals(fromGradingScaleUid)) {
					// We have a match. Now make sure that the grades are as expected.
					final Map<String, Double> inputGradePercents = gradebookDefinition.getSelectedGradingScaleBottomPercents();
					final Set<String> gradeCodes = inputGradePercents.keySet();
					if (gradeCodes.containsAll(gradeMapping.getGradeMap().keySet())) {
						// Modify the existing grade-to-percentage map.
						for (final String gradeCode : gradeCodes) {
							gradeMapping.getGradeMap().put(gradeCode, inputGradePercents.get(gradeCode));
						}
						gradebook.setSelectedGradeMapping(gradeMapping);
						updateGradebook(gradebook);
						if (log.isInfoEnabled()) {
							log.info("Merge to gradebook " + toGradebookUid + " updated grade mapping");
						}
					} else {
						if (log.isInfoEnabled()) {
							log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because the " + fromGradingScaleUid + " grade codes did not match");
						}
					}
					break MERGE_GRADE_MAPPING;
				}
			}
			// Did not find a matching grading scale.
			if (log.isInfoEnabled()) {
				log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because grading scale " + fromGradingScaleUid + " is not defined");
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void removeAssignment(final Long assignmentId) throws StaleObjectModificationException {

		final HibernateCallback hc = new HibernateCallback() {
            @Override
			public Object doInHibernate(final Session session) throws HibernateException {
                final GradebookAssignment asn = (GradebookAssignment)session.load(GradebookAssignment.class, assignmentId);
                final Gradebook gradebook = asn.getGradebook();
                asn.setRemoved(true);
                session.update(asn);

                if(log.isInfoEnabled()) {
					log.info("GradebookAssignment " + asn.getName() + " has been removed from " + gradebook);
				}
                return null;
            }
        };
        getHibernateTemplate().execute(hc);

    }

	@Override
	public Long addAssignment(final String gradebookUid, final org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition) {
		if (!getAuthz().isUserAbleToEditAssessments(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to add an assignment", getUserUid(), gradebookUid);
			throw new GradebookSecurityException();
		}

        // Ensure that points is > zero.
		final Double points = assignmentDefinition.getPoints();
        if ((points == null) || (points <= 0)) {
            throw new AssignmentHasIllegalPointsException("Points must be > 0");
        }

		final Gradebook gradebook = getGradebook(gradebookUid);

		//if attaching to category
		if(assignmentDefinition.getCategoryId() != null) {
			return createAssignmentForCategory(gradebook.getId(), assignmentDefinition.getCategoryId(), assignmentDefinition.getName(), points, assignmentDefinition.getDueDate(), !assignmentDefinition.isCounted(), assignmentDefinition.isReleased(), assignmentDefinition.isExtraCredit());
		}

		return createAssignment(gradebook.getId(), assignmentDefinition.getName(), points, assignmentDefinition.getDueDate(), !assignmentDefinition.isCounted(), assignmentDefinition.isReleased(), assignmentDefinition.isExtraCredit());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void updateAssignment(final String gradebookUid, final Long assignmentId,
			final org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition) {
		if (!getAuthz().isUserAbleToEditAssessments(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to change the definition of assignment {}", getUserUid(), gradebookUid, assignmentId);
			throw new GradebookSecurityException();
		}

		//validate the name
		final String validatedName = StringUtils.trimToNull(assignmentDefinition.getName());
        if (validatedName == null){
            throw new ConflictingAssignmentNameException("You cannot save an assignment without a name");
        }

        // name cannot contain these chars as they are reserved for special columns in import/export
        GradebookHelper.validateGradeItemName(validatedName);

		final Gradebook gradebook = this.getGradebook(gradebookUid);

		getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(final Session session) throws HibernateException {
				final GradebookAssignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentId);
				if (assignment == null) {
					throw new AssessmentNotFoundException(
							"There is no assignment with id " + assignmentId + " in gradebook " + gradebookUid);
				}

				// check if we need to scale the grades
				// this will be if we have percentage grading and we change the points possible
				boolean scaleGrades = false;
				final Double originalPointsPossible = assignment.getPointsPossible();
				if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE
					&& !assignment.getPointsPossible().equals(assignmentDefinition.getPoints())) {
					scaleGrades = true;
				}

				// external assessments are supported, but not these fields
				if (!assignmentDefinition.isExternallyMaintained()) {
					assignment.setName(validatedName);
					assignment.setPointsPossible(assignmentDefinition.getPoints());
					assignment.setDueDate(assignmentDefinition.getDueDate());
				}
				assignment.setExtraCredit(assignmentDefinition.isExtraCredit());
				assignment.setCounted(assignmentDefinition.isCounted());
				assignment.setReleased(assignmentDefinition.isReleased());

				// if we have a category, get it and set it
				// otherwise clear it fully
				if (assignmentDefinition.getCategoryId() != null) {
					final Category cat = (Category) session.load(Category.class, assignmentDefinition.getCategoryId());
					assignment.setCategory(cat);
				} else {
					assignment.setCategory(null);
				}

				updateAssignment(assignment);

				if (scaleGrades) {
					convertGradePointsForUpdatedTotalPoints(gradebook, assignment, originalPointsPossible);
				}

				return null;
			}
		});
	}

	@Override
	public Map<String, String> getImportCourseGrade(final String gradebookUid)
	{
		return getImportCourseGrade(gradebookUid, true, true);
	}

	@Override
	public Map<String, String> getImportCourseGrade(final String gradebookUid, final boolean useDefault)
	{
		return getImportCourseGrade(gradebookUid, useDefault, true);
	}

	@Override
	public Map<String, String> getImportCourseGrade(final String gradebookUid, final boolean useDefault, final boolean mapTheGrades)
	{
		final HashMap<String, String> returnMap = new HashMap<> ();

		try
		{
			//There is a new permission for course grade visibility for TA's as part of GradebookNG.
			//However the permission cannot be added here as it is not backwards compatible with Gradebook classique
			//  and would mean that all existing permissions need to be updated to add it.
			//See GradebookNgBusinessService.isCourseGradeVisible.
			//At some point it should be migrated and a DB conversion performed.

			final Gradebook thisGradebook = getGradebook(gradebookUid);

			final List assignList = getAssignmentsCounted(thisGradebook.getId());
			boolean nonAssignment = false;
			if(assignList == null || assignList.size() < 1)
			{
				nonAssignment = true;
			}

			final Long gradebookId = thisGradebook.getId();
			final CourseGrade courseGrade = getCourseGrade(gradebookId);

			final Map viewableEnrollmentsMap = this.authz.findMatchingEnrollmentsForViewableCourseGrade(gradebookUid, thisGradebook.getCategory_type(), null, null);
			final Map<String, EnrollmentRecord> enrollmentMap = new HashMap<>();

			final Map<String, EnrollmentRecord> enrollmentMapUid = new HashMap<>();
			for (final Iterator iter = viewableEnrollmentsMap.keySet().iterator(); iter.hasNext(); )
			{
				final EnrollmentRecord enr = (EnrollmentRecord)iter.next();
				enrollmentMap.put(enr.getUser().getUserUid(), enr);
				enrollmentMapUid.put(enr.getUser().getUserUid(), enr);
			}
			final List gradeRecords = getPointsEarnedCourseGradeRecords(courseGrade, enrollmentMap.keySet());
			for (final Iterator iter = gradeRecords.iterator(); iter.hasNext(); )
			{
				final CourseGradeRecord gradeRecord = (CourseGradeRecord)iter.next();

				final GradeMapping gradeMap= thisGradebook.getSelectedGradeMapping();

				final EnrollmentRecord enr = enrollmentMapUid.get(gradeRecord.getStudentId());
				if(enr != null)
				{
					// SAK-29243: if we are not mapping grades, we don't want letter grade here
					if(mapTheGrades && StringUtils.isNotBlank(gradeRecord.getEnteredGrade()))
					{
						returnMap.put(enr.getUser().getDisplayId(), gradeRecord.getEnteredGrade());
					}
					else
					{
						if(!nonAssignment) {
							Double grade;

							if(useDefault)
							{
								grade = gradeRecord.getNonNullAutoCalculatedGrade();
							}
							else
							{
								grade = gradeRecord.getAutoCalculatedGrade();
							}

							if(mapTheGrades)
							{
								returnMap.put(enr.getUser().getDisplayId(), gradeMap.getMappedGrade(grade));
							}
							else
							{
								returnMap.put(enr.getUser().getDisplayId(), grade.toString());
							}

						}
					}
				}
			}
		}
		catch(final Exception e)
		{
			log.error("Error in getImportCourseGrade", e);
		}
		return returnMap;
	}

	@Override
	public CourseGrade getCourseGrade(final Long gradebookId) {
		return (CourseGrade) getHibernateTemplate().findByNamedParam("from CourseGrade as cg where cg.gradebook.id = :gradebookid", "gradebookid", gradebookId).get(0);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List getPointsEarnedCourseGradeRecords(final CourseGrade courseGrade, final Collection studentUids) {
		final HibernateCallback hc = new HibernateCallback() {
			@Override
			public Object doInHibernate(final Session session) throws HibernateException {
				if(studentUids == null || studentUids.isEmpty()) {
					if(log.isInfoEnabled()) {
						log.info("Returning no grade records for an empty collection of student UIDs");
					}
					return new ArrayList();
				}

				final Query q = session.createQuery("from CourseGradeRecord as cgr where cgr.gradableObject.id=:gradableObjectId");
				q.setLong("gradableObjectId", courseGrade.getId());
				final List records = filterAndPopulateCourseGradeRecordsByStudents(courseGrade, q.list(), studentUids);

				final Long gradebookId = courseGrade.getGradebook().getId();
				final Gradebook gradebook = getGradebook(gradebookId);
				final List cates = getCategories(gradebookId);

				// get all of the AssignmentGradeRecords here to avoid repeated db calls
    			final Map<String, List<AssignmentGradeRecord>> gradeRecMap = getGradeRecordMapForStudents(gradebookId, studentUids);

    			// get all of the counted assignments
    			final List<GradebookAssignment> assignments = getCountedAssignments(session, gradebookId);
    			final List<GradebookAssignment> countedAssigns = new ArrayList<>();
    			if (assignments != null) {
    	                    for (final GradebookAssignment assign : assignments) {
    	                        // extra check to account for new features like extra credit
    	                        if (assign.isIncludedInCalculations()) {
    	                            countedAssigns.add(assign);
    	                        }
    	                    }
    	                }
				//double totalPointsPossible = getTotalPointsInternal(gradebookId, session);
				//if(log.isDebugEnabled()) log.debug("Total points = " + totalPointsPossible);

				for(final Iterator iter = records.iterator(); iter.hasNext();) {
					final CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
					//double totalPointsEarned = getTotalPointsEarnedInternal(gradebookId, cgr.getStudentId(), session);
					final List<AssignmentGradeRecord> studentGradeRecs = gradeRecMap.get(cgr.getStudentId());

    				applyDropScores(studentGradeRecs);
					final List totalEarned = getTotalPointsEarnedInternal(cgr.getStudentId(), gradebook, cates, studentGradeRecs, countedAssigns);
					final double totalPointsEarned = ((Double)totalEarned.get(0));
					final double literalTotalPointsEarned = ((Double)totalEarned.get(1));
					final double totalPointsPossible = getTotalPointsInternal(gradebook, cates, cgr.getStudentId(), studentGradeRecs, countedAssigns, false);
					cgr.initNonpersistentFields(totalPointsPossible, totalPointsEarned, literalTotalPointsEarned);
					if(log.isDebugEnabled()) {
						log.debug("Points earned = " + cgr.getPointsEarned());
					}
					if(log.isDebugEnabled()) {
						log.debug("Points possible = " + cgr.getTotalPointsPossible());
					}
				}

				return records;
			}
		};
		return (List)getHibernateTemplate().execute(hc);
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List filterAndPopulateCourseGradeRecordsByStudents(final CourseGrade courseGrade, final Collection gradeRecords, final Collection studentUids) {
		final List filteredRecords = new ArrayList();
		final Set missingStudents = new HashSet(studentUids);
		for (final Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			final CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
			if (studentUids.contains(cgr.getStudentId())) {
				filteredRecords.add(cgr);
				missingStudents.remove(cgr.getStudentId());
			}
		}
		for (final Iterator iter = missingStudents.iterator(); iter.hasNext(); ) {
			final String studentUid = (String)iter.next();
			final CourseGradeRecord cgr = new CourseGradeRecord(courseGrade, studentUid);
			filteredRecords.add(cgr);
		}
		return filteredRecords;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private double getTotalPointsInternal(final Gradebook gradebook, final List categories, final String studentId, final List<AssignmentGradeRecord> studentGradeRecs, final List<GradebookAssignment> countedAssigns, final boolean literalTotal)
        {
            final int gbGradeType = gradebook.getGrade_type();
            if( gbGradeType != GradebookService.GRADE_TYPE_POINTS && gbGradeType != GradebookService.GRADE_TYPE_PERCENTAGE)
            {
                if(log.isErrorEnabled()) {
					log.error("Wrong grade type in GradebookCalculationImpl.getTotalPointsInternal");
				}
                return -1;
            }

            if (studentGradeRecs == null || countedAssigns == null) {
                if (log.isDebugEnabled()) {
					log.debug("Returning 0 from getTotalPointsInternal " +
					        "since studentGradeRecs or countedAssigns was null");
				}
                return 0;
            }

            double totalPointsPossible = 0;

            final HashSet<GradebookAssignment> countedSet = new HashSet<>(countedAssigns);

            // we need to filter this list to identify only "counted" grade recs
            final List<AssignmentGradeRecord> countedGradeRecs = new ArrayList<>();
            for (final AssignmentGradeRecord gradeRec : studentGradeRecs) {
                final GradebookAssignment assign = gradeRec.getAssignment();
                boolean extraCredit = assign.isExtraCredit();
                if(gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY && assign.getCategory() != null && assign.getCategory().isExtraCredit()) {
					extraCredit = true;
				}

                if (assign.isCounted() && !assign.getUngraded() && !assign.isRemoved() && countedSet.contains(assign) &&
                        assign.getPointsPossible() != null && assign.getPointsPossible() > 0 && !gradeRec.getDroppedFromGrade() && !extraCredit) {
                    countedGradeRecs.add(gradeRec);
                }
            }

            final Set assignmentsTaken = new HashSet();
            final Set categoryTaken = new HashSet();
            for (final AssignmentGradeRecord gradeRec : countedGradeRecs)
            {
                if (gradeRec.getPointsEarned() != null && !gradeRec.getPointsEarned().equals(""))
                {
                    final Double pointsEarned = gradeRec.getPointsEarned();
                    final GradebookAssignment go = gradeRec.getAssignment();
                    if (pointsEarned != null)
                    {
                        if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
                        {
                            assignmentsTaken.add(go.getId());
                        }
                        else if ((gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY || gradebook
                                .getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
                                && go != null && categories != null)
                        {
                            //                              assignmentsTaken.add(go.getId());
                            //                          }
                            //                          else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && go != null && categories != null)
                            //                          {
                            for(int i=0; i<categories.size(); i++)
                            {
                                final Category cate = (Category) categories.get(i);
                                if(cate != null && !cate.isRemoved() && go.getCategory() != null && cate.getId().equals(go.getCategory().getId()) && ((cate.isExtraCredit()!=null && !cate.isExtraCredit()) || cate.isExtraCredit()==null))
                                {
                                    assignmentsTaken.add(go.getId());
                                    categoryTaken.add(cate.getId());
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if(!assignmentsTaken.isEmpty())
            {
                if(!literalTotal && gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
                {
                    for(int i=0; i<categories.size(); i++)
                    {
                        final Category cate = (Category) categories.get(i);
                        if(cate != null && !cate.isRemoved() && categoryTaken.contains(cate.getId()) )
                        {
                            totalPointsPossible += cate.getWeight();
                        }
                    }
                    return totalPointsPossible;
                }
                final Iterator assignmentIter = countedAssigns.iterator();
                while (assignmentIter.hasNext())
                {
                    final GradebookAssignment asn = (GradebookAssignment) assignmentIter.next();
                    if(asn != null)
                    {
                        final Double pointsPossible = asn.getPointsPossible();

                        if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY && assignmentsTaken.contains(asn.getId()))
                        {
                            totalPointsPossible += pointsPossible;
                        }
                        else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && assignmentsTaken.contains(asn.getId()))
                        {
                            totalPointsPossible += pointsPossible;
                        }else if(literalTotal && gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && assignmentsTaken.contains(asn.getId()))
                        {
                            totalPointsPossible += pointsPossible;
                        }
                    }
                }
            } else {
				totalPointsPossible = -1;
			}

            return totalPointsPossible;
        }


	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List getTotalPointsEarnedInternal(final String studentId, final Gradebook gradebook, final List categories,
	        final List<AssignmentGradeRecord> gradeRecs, final List<GradebookAssignment> countedAssigns)
	{
	    final int gbGradeType = gradebook.getGrade_type();
	    if( gbGradeType != GradebookService.GRADE_TYPE_POINTS && gbGradeType != GradebookService.GRADE_TYPE_PERCENTAGE)
	    {
	        if(log.isErrorEnabled()) {
				log.error("Wrong grade type in GradebookCalculationImpl.getTotalPointsEarnedInternal");
			}
	        return new ArrayList();
	    }

	    if (gradeRecs == null || countedAssigns == null) {
	        if (log.isDebugEnabled()) {
				log.debug("getTotalPointsEarnedInternal for " +
				        "studentId=" + studentId + " returning 0 because null gradeRecs or countedAssigns");
			}
	        final List returnList = new ArrayList();
	        returnList.add(new Double(0));
	        returnList.add(new Double(0));
	        returnList.add(new Double(0)); // 3rd one is for the pre-adjusted course grade
	        return returnList;
	    }


	    double totalPointsEarned = 0;
	    BigDecimal literalTotalPointsEarned = new BigDecimal(0d);

	    final Map cateScoreMap = new HashMap();
	    final Map cateTotalScoreMap = new HashMap();

	    final Set assignmentsTaken = new HashSet();
	    for (final AssignmentGradeRecord gradeRec : gradeRecs)
	    {
	        if(gradeRec.getPointsEarned() != null && !gradeRec.getPointsEarned().equals("") && !gradeRec.getDroppedFromGrade())
	        {
	            final GradebookAssignment go = gradeRec.getAssignment();
	            if (go.isIncludedInCalculations() && countedAssigns.contains(go))
	            {
	                final Double pointsEarned = gradeRec.getPointsEarned();
	                //if(gbGradeType == GradebookService.GRADE_TYPE_POINTS)
	                //{
	                if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
	                {
	                    totalPointsEarned += pointsEarned;
	                    literalTotalPointsEarned = (new BigDecimal(pointsEarned)).add(literalTotalPointsEarned);
	                    assignmentsTaken.add(go.getId());
	                }
	                else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && go != null)
	                {
	                    totalPointsEarned += pointsEarned;
	                    literalTotalPointsEarned = (new BigDecimal(pointsEarned)).add(literalTotalPointsEarned);
	                    assignmentsTaken.add(go.getId());
	                }
	                else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && go != null && categories != null)
	                {
	                    for(int i=0; i<categories.size(); i++)
	                    {
	                        final Category cate = (Category) categories.get(i);
	                        if(cate != null && !cate.isRemoved() && go.getCategory() != null && cate.getId().equals(go.getCategory().getId()))
	                        {
	                            assignmentsTaken.add(go.getId());
	                            literalTotalPointsEarned = (new BigDecimal(pointsEarned)).add(literalTotalPointsEarned);
	                            if(cateScoreMap.get(cate.getId()) != null)
	                            {
	                                cateScoreMap.put(cate.getId(), ((Double)cateScoreMap.get(cate.getId())) + pointsEarned);
	                            }
	                            else
	                            {
	                                cateScoreMap.put(cate.getId(), pointsEarned);
	                            }
	                            break;
	                        }
	                    }
	                }
	            }
	        }
	    }

	    if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && categories != null)
	    {
	        final Iterator assgnsIter = countedAssigns.iterator();
	        while (assgnsIter.hasNext())
	        {
	            final GradebookAssignment asgn = (GradebookAssignment)assgnsIter.next();
	            if(assignmentsTaken.contains(asgn.getId()))
	            {
	                for(int i=0; i<categories.size(); i++)
	                {
	                    final Category cate = (Category) categories.get(i);
	                    if(cate != null && !cate.isRemoved() && asgn.getCategory() != null && cate.getId().equals(asgn.getCategory().getId()) && !asgn.isExtraCredit())
	                    {

	                        if(cateTotalScoreMap.get(cate.getId()) == null)
	                        {
	                            cateTotalScoreMap.put(cate.getId(), asgn.getPointsPossible());
	                        }
	                        else
	                        {
	                            cateTotalScoreMap.put(cate.getId(), ((Double)cateTotalScoreMap.get(cate.getId())) + asgn.getPointsPossible());
	                        }

	                    }
	                }
	            }
	        }
	    }

	    if(assignmentsTaken.isEmpty()) {
			totalPointsEarned = -1;
		}

	    if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
	    {
	        for(int i=0; i<categories.size(); i++)
	        {
	            final Category cate = (Category) categories.get(i);
	            if(cate != null && !cate.isRemoved() && cateScoreMap.get(cate.getId()) != null && cateTotalScoreMap.get(cate.getId()) != null)
	            {
	                totalPointsEarned += ((Double)cateScoreMap.get(cate.getId())) * cate.getWeight() / ((Double)cateTotalScoreMap.get(cate.getId()));
	            }
	        }
	    }

	    if (log.isDebugEnabled()) {
			log.debug("getTotalPointsEarnedInternal for studentId=" + studentId + " returning " + totalPointsEarned);
		}
	    final List returnList = new ArrayList();
	    returnList.add(totalPointsEarned);
	    returnList.add((new BigDecimal(literalTotalPointsEarned.doubleValue(), GradebookService.MATH_CONTEXT)).doubleValue());

	    return returnList;
	}

	/**
	 * Internal method to get a gradebook based on its id.
	 * @param id
	 * @return
	 *
	 * NOTE: When the UI changes, this is to be turned private again
	 */
	public Gradebook getGradebook(final Long id) {
		return getHibernateTemplate().load(Gradebook.class, id);
	}

	private List<GradebookAssignment> getAssignmentsCounted(final Long gradebookId) throws HibernateException {
		final HibernateCallback<List<GradebookAssignment>> hc = session -> session
                .createQuery("from GradebookAssignment as asn where asn.gradebook.id = :gradebookid and asn.removed is false and asn.notCounted is false")
                .setLong("gradebookid", gradebookId)
                .list();
		return getHibernateTemplate().execute(hc);
	}

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public boolean checkStudentsNotSubmitted(final String gradebookUid)
  {
  	final Gradebook gradebook = getGradebook(gradebookUid);
  	final Set studentUids = getAllStudentUids(getGradebookUid(gradebook.getId()));
  	if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY || gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY)
  	{
  		final List records = getAllAssignmentGradeRecords(gradebook.getId(), studentUids);
  		final List assigns = getAssignments(gradebook.getId(), SortType.SORT_BY_SORTING, true);
  		final List filteredAssigns = new ArrayList();
  		for(final Iterator iter = assigns.iterator(); iter.hasNext();)
  		{
  			final GradebookAssignment assignment = (GradebookAssignment)iter.next();
  			if(assignment.isCounted() && !assignment.getUngraded()) {
				filteredAssigns.add(assignment);
			}
  		}
  		final List filteredRecords = new ArrayList();
  		for(final Iterator iter = records.iterator(); iter.hasNext();)
  		{
  			final AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
  			if(!agr.isCourseGradeRecord() && agr.getAssignment().isCounted() && !agr.getAssignment().getUngraded())
  			{
  				if(agr.getPointsEarned() == null) {
					return true;
				}
  				filteredRecords.add(agr);
  			}
  		}

  		return filteredRecords.size() < (filteredAssigns.size() * studentUids.size());
  	}
  	else
  	{
    	final List assigns = getAssignments(gradebook.getId(), SortType.SORT_BY_SORTING, true);
    	final List records = getAllAssignmentGradeRecords(gradebook.getId(), studentUids);
    	final Set filteredAssigns = new HashSet();
    	for (final Iterator iter = assigns.iterator(); iter.hasNext(); )
    	{
    		final GradebookAssignment assign = (GradebookAssignment) iter.next();
    		if(assign != null && assign.isCounted() && !assign.getUngraded())
    		{
    			if(assign.getCategory() != null && !assign.getCategory().isRemoved())
    			{
    				filteredAssigns.add(assign.getId());
    			}
    		}
    	}

  		final List filteredRecords = new ArrayList();
  		for(final Iterator iter = records.iterator(); iter.hasNext();)
  		{
  			final AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
  			if(filteredAssigns.contains(agr.getAssignment().getId()) && !agr.isCourseGradeRecord())
  			{
  				if(agr.getPointsEarned() == null) {
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
   * NOTE When the UI changes, this needs to be made private again
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List getAllAssignmentGradeRecords(final Long gradebookId, final Collection studentUids) {
  	final HibernateCallback hc = new HibernateCallback() {
  		@Override
		public Object doInHibernate(final Session session) throws HibernateException {
  			if(studentUids.isEmpty()) {
  				// If there are no enrollments, no need to execute the query.
  				if(log.isInfoEnabled()) {
					log.info("No enrollments were specified.  Returning an empty List of grade records");
				}
  				return new ArrayList();
  			} else {
  				final Query q = session.createQuery("from AssignmentGradeRecord as agr where agr.gradableObject.removed=false and " +
  				"agr.gradableObject.gradebook.id=:gradebookId order by agr.pointsEarned");
  				q.setLong("gradebookId", gradebookId);
  				return filterGradeRecordsByStudents(q.list(), studentUids);
  			}
  		}
  	};
  	return (List)getHibernateTemplate().execute(hc);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private List getAllAssignmentGradeRecordsForGbItem(final Long gradableObjectId,
		  final Collection studentUids) {
	  	final HibernateCallback hc = new HibernateCallback() {
	  		@Override
			public Object doInHibernate(final Session session) throws HibernateException {
	  			if(studentUids.isEmpty()) {
	  				// If there are no enrollments, no need to execute the query.
	  				if(log.isInfoEnabled()) {
						log.info("No enrollments were specified.  Returning an empty List of grade records");
					}
	  				return new ArrayList();
	  			} else {
	  				final Query q = session.createQuery("from AssignmentGradeRecord as agr where agr.gradableObject.removed=false and " +
	  				"agr.gradableObject.id=:gradableObjectId order by agr.pointsEarned");
	  				q.setLong("gradableObjectId", gradableObjectId);
	  				return filterGradeRecordsByStudents(q.list(), studentUids);
	  			}
	  		}
	  	};
	  	return (List)getHibernateTemplate().execute(hc);
	  }

	/**
	 * Gets all AssignmentGradeRecords on the gradableObjectIds limited to students specified by studentUids
	 */
	private List<AssignmentGradeRecord> getAllAssignmentGradeRecordsForGbItems(final List<Long> gradableObjectIds, final List studentUids)
	{
		final HibernateCallback hc = new HibernateCallback()
		{
			@Override
			public Object doInHibernate(final Session session) throws HibernateException
			{
				final List<AssignmentGradeRecord> gradeRecords = new ArrayList<>();
				if (studentUids.isEmpty())
				{
					// If there are no enrollments, no need to execute the query.
					if (log.isDebugEnabled()) {
						log.debug("No enrollments were specified. Returning an empty List of grade records");
					}
					return gradeRecords;
				}
				/*
				 * Watch out for Oracle's "in" limit. Ignoring oracle, the query would be:
				 * "from AssignmentGradeRecord as agr where agr.gradableObject.removed = false and agr.gradableObject.id in (:gradableObjectIds) and agr.studentId in (:studentUids)"
				 * Note: the order is not important. The calling methods will iterate over all entries and add them to a map.
				 * We could have made this method return a map, but we'd have to iterate over the items in order to add them to the map anyway.
				 * That would be a waste of a loop that the calling method could use to perform additional tasks.
				 */
				// For Oracle, iterate over gbItems 1000 at a time (sympathies to whoever needs to query grades for a thousand gbItems)
				int minGbo = 0;
				int maxGbo = Math.min(gradableObjectIds.size(), 1000);
				while (minGbo < gradableObjectIds.size())
				{
					// For Oracle, iterate over students 1000 at a time
					int minStudent = 0;
					int maxStudent = Math.min(studentUids.size(), 1000);
					while (minStudent < studentUids.size())
					{
						final Query q = session.createQuery("from AssignmentGradeRecord as agr where agr.gradableObject.removed = false and " +
							"agr.gradableObject.id in (:gradableObjectIds) and agr.studentId in (:studentUids)");
						q.setParameterList("gradableObjectIds", gradableObjectIds.subList(minGbo, maxGbo));
						q.setParameterList("studentUids", studentUids.subList(minStudent, maxStudent));
						// Add the query results to our overall results (in case there's over a thousand things)
						gradeRecords.addAll(q.list());
						minStudent += 1000;
						maxStudent = Math.min(studentUids.size(), minStudent + 1000);
					}
					minGbo += 1000;
					maxGbo = Math.min(gradableObjectIds.size(), minGbo + 1000);
				}
				return gradeRecords;
			}
		};
		return (List<AssignmentGradeRecord>) getHibernateTemplate().execute(hc);
	}

  /**
   * Get a list of assignments, sorted
   * @param gradebookId
   * @param sortBy
   * @param ascending
   * @return
   *
   * NOTE: When the UI changes, this needs to go back to private
   */
  public List getAssignments(final Long gradebookId, final SortType sortBy, final boolean ascending) {
	  final List assignments = getAssignments(gradebookId);
	  sortAssignments(assignments, sortBy, ascending);
	  return assignments;
  }

  /**
   * Sort the list of (internal) assignments by the given criteria
   * @param assignments
   * @param sortBy
   * @param ascending
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void sortAssignments(final List assignments, SortType sortBy, final boolean ascending) {

	//note, this is duplicated in the tool GradebookManagerHibernateImpl class
	Comparator comp;

	if(sortBy == null) {
		sortBy = SortType.SORT_BY_SORTING; //default
	}

  	switch (sortBy) {

  		case SORT_BY_NONE:
  			return; //no sorting
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
  	if(!ascending) {
  		Collections.reverse(assignments);
  	}
  	if (log.isDebugEnabled()) {
  	    log.debug("sortAssignments: ordering by "+sortBy+" ("+comp+"), ascending="+ascending);
  	}
  }

/*
 * (non-Javadoc)
 * @see org.sakaiproject.service.gradebook.shared.GradebookService#getViewableAssignmentsForCurrentUser(java.lang.String)
 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<org.sakaiproject.service.gradebook.shared.Assignment> getViewableAssignmentsForCurrentUser(final String gradebookUid)
			throws GradebookNotFoundException {
		return getViewableAssignmentsForCurrentUser(gradebookUid, SortType.SORT_BY_SORTING);
	}

  /*
   * (non-Javadoc)
   * @see org.sakaiproject.service.gradebook.shared.GradebookService#getViewableAssignmentsForCurrentUser(java.lang.String, java.)
   */
  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<org.sakaiproject.service.gradebook.shared.Assignment> getViewableAssignmentsForCurrentUser(final String gradebookUid, final SortType sortBy)
  throws GradebookNotFoundException {

	  List<GradebookAssignment> viewableAssignments = new ArrayList<>();
		final LinkedHashSet<org.sakaiproject.service.gradebook.shared.Assignment> assignmentsToReturn = new LinkedHashSet<>();

	  final Gradebook gradebook = getGradebook(gradebookUid);

	  // will send back all assignments if user can grade all
	  if (getAuthz().isUserAbleToGradeAll(gradebookUid)) {
		  viewableAssignments = getAssignments(gradebook.getId(), sortBy, true);
	  } else if (getAuthz().isUserAbleToGrade(gradebookUid)) {
		  // if user can grade and doesn't have grader perm restrictions, they
		  // may view all assigns
		  if (!getAuthz().isUserHasGraderPermissions(gradebookUid)) {
			  viewableAssignments = getAssignments(gradebook.getId(), sortBy, true);
		  } else {
			  // this user has grader perms, so we need to filter the items returned
			  // if this gradebook has categories enabled, we need to check for category-specific restrictions
			  if (gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY) {
				  assignmentsToReturn.addAll(getAssignments(gradebookUid, sortBy));
			  } else {
				  final String userUid = getUserUid();
				  if (getGradebookPermissionService().getPermissionForUserForAllAssignment(gradebook.getId(), userUid)) {
					  assignmentsToReturn.addAll(getAssignments(gradebookUid, sortBy));
				  } else {
					  final List<org.sakaiproject.service.gradebook.shared.Assignment> assignments = getAssignments(gradebookUid, sortBy);
					  final List<Long> categoryIds = ((List<Category>)getCategories(gradebook.getId())).stream().map(Category::getId).collect(Collectors.toList());
					  // categories are enabled, so we need to check the category restrictions
					  if (!categoryIds.isEmpty()) {
						  final List<Long> viewableCategoryIds = getGradebookPermissionService().getCategoriesForUser(gradebook.getId(), userUid, categoryIds);
						  for (final org.sakaiproject.service.gradebook.shared.Assignment assignment : assignments) {
							  if (assignment.getCategoryId() != null && viewableCategoryIds.contains(assignment.getCategoryId())) {
								  assignmentsToReturn.add(assignment);
							  }
						  }
					  }
				  }
			  }
		  }
	  } else if (getAuthz().isUserAbleToViewOwnGrades(gradebookUid)) {
		  // if user is just a student, we need to filter out unreleased items
		  final List allAssigns = getAssignments(gradebook.getId(), null, true);
		  if (allAssigns != null) {
			  for (final Iterator aIter = allAssigns.iterator(); aIter.hasNext();) {
				  final GradebookAssignment assign = (GradebookAssignment) aIter.next();
				  if (assign != null && assign.isReleased()) {
					  viewableAssignments.add(assign);
				  }
			  }
		  }
	  }

	  // Now we need to convert these to the assignment template objects
	  if (viewableAssignments != null && !viewableAssignments.isEmpty()) {
		  for (final Object element : viewableAssignments) {
			  final GradebookAssignment assignment = (GradebookAssignment) element;
			  assignmentsToReturn.add(getAssignmentDefinition(assignment));
		  }
	  }

	  return new ArrayList<>(assignmentsToReturn);

  }

  @Override
  public Map<String, String> getViewableStudentsForItemForCurrentUser(final String gradebookUid, final Long gradableObjectId) {
	  final String userUid = this.authn.getUserUid();

	  return getViewableStudentsForItemForUser(userUid, gradebookUid, gradableObjectId);
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map<String, String> getViewableStudentsForItemForUser(final String userUid, final String gradebookUid, final Long gradableObjectId) {

      if (gradebookUid == null || gradableObjectId == null || userUid == null) {
          throw new IllegalArgumentException("null gradebookUid or gradableObjectId or " +
                  "userId passed to getViewableStudentsForUserForItem." +
                  " gradebookUid: " + gradebookUid + " gradableObjectId:" +
                  gradableObjectId + " userId: " + userUid);
      }

      if (!this.authz.isUserAbleToGrade(gradebookUid, userUid)) {
          return new HashMap<>();
      }

      final GradebookAssignment gradebookItem = getAssignmentWithoutStatsByID(gradebookUid, gradableObjectId);

      if (gradebookItem == null) {
          log.debug("The gradebook item does not exist, so returning empty set");
          return new HashMap();
      }

      final Long categoryId = gradebookItem.getCategory() == null ? null : gradebookItem.getCategory().getId();

      final Map<EnrollmentRecord, String> enrRecFunctionMap  = this.authz.findMatchingEnrollmentsForItemForUser(userUid, gradebookUid, categoryId, getGradebook(gradebookUid).getCategory_type(), null, null);
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

	  return isAssignmentDefined(gradableObjectId);
  }

  @Override
  public Map getViewableSectionUuidToNameMap(final String gradebookUid) {
	  if (gradebookUid == null) {
		  throw new IllegalArgumentException("Null gradebookUid passed to getViewableSectionIdToNameMap");
	  }

	  final Map<String, String> sectionIdNameMap = new HashMap<>();

	  final List viewableCourseSections = getAuthz().getViewableSections(gradebookUid);
	  if (viewableCourseSections == null || viewableCourseSections.isEmpty()) {
		  return sectionIdNameMap;
	  }

	  for (final Iterator sectionIter = viewableCourseSections.iterator(); sectionIter.hasNext();) {
		  final CourseSection section = (CourseSection) sectionIter.next();
		  if (section != null) {
			  sectionIdNameMap.put(section.getUuid(), section.getTitle());
		  }
	  }

	  return sectionIdNameMap;
  }

  @Override
  public boolean currentUserHasGradeAllPerm(final String gradebookUid) {
	  return this.authz.isUserAbleToGradeAll(gradebookUid);
  }

  @Override
  public boolean isUserAllowedToGradeAll(final String gradebookUid, final String userUid) {
      return this.authz.isUserAbleToGradeAll(gradebookUid, userUid);
  }

  @Override
  public boolean currentUserHasGradingPerm(final String gradebookUid) {
	  return this.authz.isUserAbleToGrade(gradebookUid);
  }

  @Override
  public boolean isUserAllowedToGrade(final String gradebookUid, final String userUid) {
      return this.authz.isUserAbleToGrade(gradebookUid, userUid);
  }

  @Override
  public boolean currentUserHasEditPerm(final String gradebookUid) {
	  return this.authz.isUserAbleToEditAssessments(gradebookUid);
  }

  @Override
  public boolean currentUserHasViewOwnGradesPerm(final String gradebookUid) {
	  return this.authz.isUserAbleToViewOwnGrades(gradebookUid);
  }

  @Override
  public boolean currentUserHasViewStudentNumbersPerm(final String gradebookUid)
  {
	  return this.authz.isUserAbleToViewStudentNumbers(gradebookUid);
  }

  @Override
  public List<GradeDefinition> getGradesForStudentsForItem(final String gradebookUid, final Long gradableObjectId, final List<String> studentIds) {
	  if (gradableObjectId == null) {
		  throw new IllegalArgumentException("null gradableObjectId passed to getGradesForStudentsForItem");
	  }

	  final List<org.sakaiproject.service.gradebook.shared.GradeDefinition> studentGrades = new ArrayList();

	  if (studentIds != null && !studentIds.isEmpty()) {
		  // first, we need to make sure the current user is authorized to view the
		  // grades for all of the requested students
		  final GradebookAssignment gbItem = getAssignmentWithoutStatsByID(gradebookUid, gradableObjectId);

		  if (gbItem != null) {
			  final Gradebook gradebook = gbItem.getGradebook();

			  if (!this.authz.isUserAbleToGrade(gradebook.getUid())) {
				  log.error("User {} attempted to access grade information without permission in gb {} using gradebookService.getGradesForStudentsForItem", this.authn.getUserUid(), gradebook.getUid());
				  throw new GradebookSecurityException();
			  }

			  final Long categoryId = gbItem.getCategory() != null ? gbItem.getCategory().getId() : null;
			  final Map enrRecFunctionMap = this.authz.findMatchingEnrollmentsForItem(gradebook.getUid(), categoryId, gradebook.getCategory_type(), null, null);
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

			  //filter the provided studentIds if user doesn't have permissions
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
				  if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
					  convertPointsToLetterGrade(gradebook, gradeRecs);
				  } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
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
							  final GradeDefinition gradeDef = convertGradeRecordToGradeDefinition(emptyGradeRecord, gbItem, gradebook, comment);
							  studentGrades.add(gradeDef);
						  }
					  }
				  }
			  }
		  }
	  }

	  return studentGrades;
  }

	@Override
	public Map<Long, List<GradeDefinition>> getGradesWithoutCommentsForStudentsForItems(final String gradebookUid, final List<Long> gradableObjectIds, final List<String> studentIds)
	{
		if (!this.authz.isUserAbleToGrade(gradebookUid))
		{
			throw new GradebookSecurityException();
		}

		if (gradableObjectIds == null || gradableObjectIds.isEmpty())
		{
			throw new IllegalArgumentException("null or empty gradableObjectIds passed to getGradesWithoutCommentsForStudentsForItems");
		}

		final Map<Long, List<GradeDefinition>> gradesMap = new HashMap<>();
		if (studentIds == null || studentIds.isEmpty())
		{
			// We could populate the map with (gboId : new ArrayList()), but it's cheaper to allow get(gboId) to return null.
			return gradesMap;
		}

		// Get all the grades for the gradableObjectIds
		final List<AssignmentGradeRecord> gradeRecords = getAllAssignmentGradeRecordsForGbItems(gradableObjectIds, studentIds);
		// AssignmentGradeRecord is not in the API. So we need to convert grade records into GradeDefinition objects.
		// GradeDefinitions are not tied to their gbos, so we need to return a map associating them back to their gbos
		final List<GradeDefinition> gradeDefinitions = new ArrayList<>();
		for (final AssignmentGradeRecord gradeRecord : gradeRecords)
		{
			final GradebookAssignment gbo = (GradebookAssignment)gradeRecord.getGradableObject();
			final Long gboId = gbo.getId();
			final Gradebook gradebook = gbo.getGradebook();
			if (!gradebookUid.equals(gradebook.getUid()))
			{
				// The user is authorized against gradebookUid, but we have grades for another gradebook.
				// This is an authorization issue caused by gradableObjectIds violating the method contract.
				throw new IllegalArgumentException("gradableObjectIds must belong to grades within this gradebook");
			}

			final GradeDefinition gradeDef = convertGradeRecordToGradeDefinition(gradeRecord, gbo, gradebook, null);

			List<GradeDefinition> gradeList = gradesMap.get(gboId);
			if (gradeList == null)
			{
				gradeList = new ArrayList<>();
				gradesMap.put(gboId, gradeList);
			}
			gradeList.add(gradeDef);
		}

		return gradesMap;
	}

	/**
	 * Converts an AssignmentGradeRecord into a GradeDefinition object.
	 * @param gradeRecord
	 * @param gbo
	 * @param gradebook
	 * @param commentText - goes into the GradeComment attribute. Will be omitted if null
	 * @return a GradeDefinition object whose attributes match the passed in gradeRecord
	 */
	private GradeDefinition convertGradeRecordToGradeDefinition(final AssignmentGradeRecord gradeRecord, final GradebookAssignment gbo, final Gradebook gradebook, final String commentText)
	{
		final GradeDefinition gradeDef = new GradeDefinition();
		gradeDef.setStudentUid(gradeRecord.getStudentId());
		gradeDef.setGraderUid(gradeRecord.getGraderId());
		gradeDef.setDateRecorded(gradeRecord.getDateRecorded());
		final int gradeEntryType = gradebook.getGrade_type();
		gradeDef.setGradeEntryType(gradeEntryType);
		String grade = null;
		if (gradeEntryType == GradebookService.GRADE_TYPE_LETTER)
		{
			grade = gradeRecord.getLetterEarned();
		}
		else if (gradeEntryType == GradebookService.GRADE_TYPE_PERCENTAGE)
		{
			final Double percentEarned = gradeRecord.getPercentEarned();
			grade = percentEarned != null ? percentEarned.toString() : null;
		}
		else
		{
			final Double pointsEarned = gradeRecord.getPointsEarned();
			grade = pointsEarned != null ? pointsEarned.toString() : null;
		}
		gradeDef.setGrade(grade);
		gradeDef.setGradeReleased(gradebook.isAssignmentsDisplayed() && gbo.isReleased());

		if (commentText != null)
		{
			gradeDef.setGradeComment(commentText);
		}

		return gradeDef;
	}

  @Override
  public boolean isGradeValid(final String gradebookUuid, final String grade) {
	  if (gradebookUuid == null) {
		  throw new IllegalArgumentException("Null gradebookUuid passed to isGradeValid");
	  }
	  Gradebook gradebook;
	  try {
		  gradebook = getGradebook(gradebookUuid);
	  } catch (final GradebookNotFoundException gnfe) {
		  throw new GradebookNotFoundException("No gradebook exists with the given gradebookUid: " +
				  gradebookUuid + "Error: " + gnfe.getMessage());
	  }

	  final int gradeEntryType = gradebook.getGrade_type();
	  LetterGradePercentMapping mapping = null;
	  if (gradeEntryType == GradebookService.GRADE_TYPE_LETTER) {
		  mapping = getLetterGradePercentMapping(gradebook);
	  }

	  return isGradeValid(grade, gradeEntryType, mapping);
  }

	@Override
	public boolean isValidNumericGrade(String grade)
	{
		boolean gradeIsValid = false;

		try
		{
			NumberFormat nbFormat = NumberFormat.getInstance(new ResourceLoader().getLocale());
			Double gradeAsDouble = nbFormat.parse(grade).doubleValue();
			String decSeparator =((DecimalFormat)nbFormat).getDecimalFormatSymbols().getDecimalSeparator() + "";

			// grade must be greater than or equal to 0
			if (gradeAsDouble >= 0) {
				String[] splitOnDecimal = grade.split("\\" + decSeparator);
				// check that there are no more than 2 decimal places
				if (splitOnDecimal == null) {
					gradeIsValid = true;

				// check for a valid score matching ##########.##
				// where integer is maximum of 10 integers in length
				// and maximum of 2 decimal places
				} else if (grade.matches("[0-9]{0,10}(\\"+decSeparator+"[0-9]{0,2})?")) {
					gradeIsValid = true;
				}
			}
		}
		catch (NumberFormatException | ParseException nfe)
		{
			log.debug("Passed grade is not a numeric value");
		}

		return gradeIsValid;
	}

  private boolean isGradeValid(final String grade, final int gradeEntryType, final LetterGradePercentMapping gradeMapping) {

	  boolean gradeIsValid = false;

	  if (grade == null || "".equals(grade)) {

		  gradeIsValid = true;

	  } else {

		  if (gradeEntryType == GradebookService.GRADE_TYPE_POINTS ||
				  gradeEntryType == GradebookService.GRADE_TYPE_PERCENTAGE) {
			  try {
				  final NumberFormat nbFormat = NumberFormat.getInstance(new ResourceLoader().getLocale());
				  final Double gradeAsDouble = nbFormat.parse(grade).doubleValue();
				  final String decSeparator =((DecimalFormat)nbFormat).getDecimalFormatSymbols().getDecimalSeparator()+"";
				  // grade must be greater than or equal to 0
				  if (gradeAsDouble >= 0) {
						final String[] splitOnDecimal = grade.split("\\"+decSeparator);
					  // check that there are no more than 2 decimal places
					  if (splitOnDecimal == null) {
						  gradeIsValid = true;

					  // check for a valid score matching ##########.##
					  // where integer is maximum of 10 integers in length
					  // and maximum of 2 decimal places
					  } else if (grade.matches("[0-9]{0,10}(\\"+decSeparator+"[0-9]{0,2})?")) {
						  gradeIsValid = true;
					  }
				  }
			  } catch (NumberFormatException | ParseException nfe) {
				  log.debug("Passed grade is not a numeric value");
			  }

		  } else if (gradeEntryType == GradebookService.GRADE_TYPE_LETTER) {
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
		  Gradebook gradebook;

		  try {
			  gradebook = getGradebook(gradebookUid);
		  } catch (final GradebookNotFoundException gnfe) {
			  throw new GradebookNotFoundException("No gradebook exists with the given gradebookUid: " +
					  gradebookUid + "Error: " + gnfe.getMessage());
		  }

		  LetterGradePercentMapping gradeMapping = null;
		  if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
			  gradeMapping = getLetterGradePercentMapping(gradebook);
		  }

		  for (final String studentId : studentIdToGradeMap.keySet()) {
			  final String grade = studentIdToGradeMap.get(studentId);
			  if (!isGradeValid(grade, gradebook.getGrade_type(), gradeMapping)) {
				  studentsWithInvalidGrade.add(studentId);
			  }
		  }
	  }
	  return studentsWithInvalidGrade;
  }

  @Override
  public void saveGradeAndCommentForStudent(final String gradebookUid, final Long gradableObjectId, final String studentUid, final String grade, final String comment) {
	  if (gradebookUid == null || gradableObjectId == null || studentUid == null) {
		  throw new IllegalArgumentException("Null gradebookUid or gradableObjectId or studentUid passed to saveGradeAndCommentForStudent");
	  }

	  final GradeDefinition gradeDef = new GradeDefinition();
	  gradeDef.setStudentUid(studentUid);
	  gradeDef.setGrade(grade);
	  gradeDef.setGradeComment(comment);

	  final List<GradeDefinition> gradeDefList = new ArrayList<>();
	  gradeDefList.add(gradeDef);

	  saveGradesAndComments(gradebookUid, gradableObjectId, gradeDefList);
  }

  @Override
  public void saveGradesAndComments(final String gradebookUid, final Long gradableObjectId, final List<GradeDefinition> gradeDefList) {
	  if (gradebookUid == null || gradableObjectId == null) {
		  throw new IllegalArgumentException("Null gradebookUid or gradableObjectId passed to saveGradesAndComments");
	  }

	  if (CollectionUtils.isNotEmpty(gradeDefList)) {
		  Gradebook gradebook;

		  try {
			  gradebook = getGradebook(gradebookUid);
		  } catch (final GradebookNotFoundException gnfe) {
			  throw new GradebookNotFoundException("No gradebook exists with the given gradebookUid: " +
					  gradebookUid + "Error: " + gnfe.getMessage());
		  }

		  final GradebookAssignment assignment = getAssignmentWithoutStatsByID(gradebookUid, gradableObjectId);
		  if (assignment == null) {
			  throw new AssessmentNotFoundException("No gradebook item exists with gradable object id = " + gradableObjectId);
		  }

		  if (!currentUserHasGradingPerm(gradebookUid)) {
			  log.warn("User attempted to save grades and comments without authorization");
			  throw new GradebookSecurityException();
		  }

		  // identify all of the students being updated first
		  final Map<String, GradeDefinition> studentIdGradeDefMap = new HashMap<>();
		  final Map<String, String> studentIdToGradeMap = new HashMap<>();

		  for (final GradeDefinition gradeDef: gradeDefList) {
			  studentIdGradeDefMap.put(gradeDef.getStudentUid(), gradeDef);
			  studentIdToGradeMap.put(gradeDef.getStudentUid(), gradeDef.getGrade());
		  }

		  /* TODO: this check may be unnecessary if we're validating grades in the first step of the grade import wizard
				BUT, this can only be removed if the only place it's used is in the grade import (other places may not
				perform the grade validation prior to calling this
		  */
		  // Check for invalid grades
		  List<String> invalidStudentUUIDs = identifyStudentsWithInvalidGrades(gradebookUid, studentIdToGradeMap);
		  if (CollectionUtils.isNotEmpty(invalidStudentUUIDs)) {
			  throw new InvalidGradeException("At least one grade passed to be updated is " + "invalid. No grades or comments were updated.");
		  }

		  // Retrieve all existing grade records for the given students and assignment
		  List<AssignmentGradeRecord> existingGradeRecords = getAllAssignmentGradeRecordsForGbItem(gradableObjectId, studentIdGradeDefMap.keySet());
		  Map<String, AssignmentGradeRecord> studentIdGradeRecordMap = new HashMap<>();
		  if (CollectionUtils.isNotEmpty(existingGradeRecords)) {
			  for (AssignmentGradeRecord agr : existingGradeRecords) {
				  studentIdGradeRecordMap.put(agr.getStudentId(), agr);
			  }
		  }

		  // Retrieve all existing comments for the given students and assignment
		  List<Comment> existingComments = getComments(assignment, studentIdGradeDefMap.keySet());
		  final Map<String, Comment> studentIdCommentMap = new HashMap<>();
		  if (CollectionUtils.isNotEmpty(existingComments)) {
			  for (Comment comment : existingComments) {
				  studentIdCommentMap.put(comment.getStudentId(), comment);
			  }
		  }

		  boolean userHasGradeAllPerm = currentUserHasGradeAllPerm(gradebookUid);
		  String graderId = getAuthn().getUserUid();
		  Date now = new Date();
		  LetterGradePercentMapping mapping = null;
		  if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
			  mapping = getLetterGradePercentMapping(gradebook);
		  }

		  // Don't use a HashSet because you may have multiple Comments with null ID and the same comment at this point.
		  // The Comment object defines objects as equal if they have the same ID, comment text, and gradebook item. The
		  // only difference may be the student IDs
		  final List<Comment> commentsToUpdate = new ArrayList<>();
		  final Set<GradingEvent> eventsToAdd = new HashSet<>();
		  Set<AssignmentGradeRecord> gradeRecordsToUpdate = new HashSet<>();
		  for (final GradeDefinition gradeDef : gradeDefList) {
			  final String studentId = gradeDef.getStudentUid();

			  // use the grader ID from the definition if it is not null, otherwise use the current user ID
			  final String graderUid = gradeDef.getGraderUid() != null ? gradeDef.getGraderUid() : graderId;
			  // use the grade date from the definition if it is not null, otherwise use the current date
			  final Date gradedDate = gradeDef.getDateRecorded() != null ? gradeDef.getDateRecorded() : now;

			  // check specific grading privileges if user does not have grade all perm
			  if (!userHasGradeAllPerm) {
				  if (!isUserAbleToGradeItemForStudent(gradebookUid, gradableObjectId, studentId)) {
					  log.warn("User {} attempted to save a grade for {} without authorization", graderId, studentId);
					  throw new GradebookSecurityException();
				  }
			  }

			  // Determine if the AssignmentGradeRecord needs to be updated
			  String newGrade = StringUtils.trimToEmpty(gradeDef.getGrade());
			  Double convertedGrade = convertInputGradeToPoints(gradebook.getGrade_type(), mapping, assignment.getPointsPossible(), newGrade);
			  AssignmentGradeRecord gradeRec = studentIdGradeRecordMap.get(studentId);
			  if (gradeRec != null) {
				  Double pointsEarned = gradeRec.getPointsEarned();
				  if ((convertedGrade == null && pointsEarned != null)
						  || (convertedGrade != null && pointsEarned == null)
						  || (convertedGrade != null && pointsEarned != null && !convertedGrade.equals(pointsEarned))) {

					  gradeRec.setPointsEarned(convertedGrade);
					  gradeRec.setGraderId(graderUid);
					  gradeRec.setDateRecorded(gradedDate);
					  gradeRecordsToUpdate.add(gradeRec);

					  // Add a GradingEvent, which stores the actual input grade rather than the converted one
					  GradingEvent event = new GradingEvent(assignment, graderId, studentId, newGrade);
					  eventsToAdd.add(event);
				  }
			  } else {
				  // if the grade is something other than null, add a new AGR
				  if (StringUtils.isNotBlank(newGrade)) {
					  gradeRec =  new AssignmentGradeRecord(assignment, studentId, convertedGrade);
					  gradeRec.setGraderId(graderUid);
					  gradeRec.setDateRecorded(gradedDate);
					  gradeRecordsToUpdate.add(gradeRec);

					  // Add a GradingEvent, which stores the actual input grade rather than the converted one
					  final GradingEvent event = new GradingEvent(assignment, graderId, studentId, newGrade);
					  eventsToAdd.add(event);
				  }
			  }

			  // Determine if the Comment needs to be updated
			  Comment comment = studentIdCommentMap.get(studentId);
			  String newCommentText = StringUtils.trimToEmpty(gradeDef.getGradeComment());
			  if (comment != null) {
				  String existingCommentText = StringUtils.trimToEmpty(comment.getCommentText());
				  boolean existingCommentTextIsEmpty = existingCommentText.isEmpty();
				  boolean newCommentTextIsEmpty = newCommentText.isEmpty();
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
			for (final AssignmentGradeRecord assignmentGradeRecord : gradeRecordsToUpdate) {
				getHibernateTemplate().saveOrUpdate(assignmentGradeRecord);
			}
			for (final Comment comment : commentsToUpdate) {
				getHibernateTemplate().saveOrUpdate(comment);
			}
			for (final GradingEvent gradingEvent : eventsToAdd) {
				getHibernateTemplate().saveOrUpdate(gradingEvent);
			}
		  } catch (final HibernateOptimisticLockingFailureException | StaleObjectStateException holfe) {
			  if(log.isInfoEnabled()) {
				log.info("An optimistic locking failure occurred while attempting to save scores and comments for gb Item " + gradableObjectId);
			  }
			  throw new StaleObjectModificationException(holfe);
		  }
	  }
  }

	/**
	 * Helper method to retrieve Assignment by ID without stats for the given gradebook.
	 * Reduces code duplication in several areas.
	 *
	 * @param gradebookUID
	 * @param gradeableObjectID
	 * @return
	 */
	private GradebookAssignment getAssignmentWithoutStatsByID(final String gradebookUID, final Long gradeableObjectID) {
		return (GradebookAssignment) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUID, gradeableObjectID);
			}
		});
	}

  /**
   *
   * @param gradeEntryType
   * @param mapping
   * @param gbItemPointsPossible
   * @param grade
   * @return given a generic String grade, converts it to the equivalent Double
   * point value that will be stored in the db based upon the gradebook's grade entry type
   */
  private Double convertInputGradeToPoints(final int gradeEntryType, final LetterGradePercentMapping mapping,
		  final Double gbItemPointsPossible, final String grade) throws InvalidGradeException {
	  Double convertedValue = null;

	  if (grade != null && !"".equals(grade)) {
		  if (gradeEntryType == GradebookService.GRADE_TYPE_POINTS) {
			  try {
				  final NumberFormat nbFormat = NumberFormat.getInstance(new ResourceLoader().getLocale());
				  final Double pointValue = nbFormat.parse(grade).doubleValue();
				  convertedValue = pointValue;
			  } catch (NumberFormatException | ParseException nfe) {
				  throw new InvalidGradeException("Invalid grade passed to convertInputGradeToPoints");
			  }
		  } else if (gradeEntryType == GradebookService.GRADE_TYPE_PERCENTAGE ||
				  gradeEntryType == GradebookService.GRADE_TYPE_LETTER) {

			  // for letter or %-based grading, we need to calculate the equivalent point value
			  if (gbItemPointsPossible == null) {
				  throw new IllegalArgumentException("Null points possible passed" +
				  " to convertInputGradeToPoints for letter or % based grading");
			  }

			  Double percentage = null;
			  if (gradeEntryType == GradebookService.GRADE_TYPE_LETTER) {
				  if (mapping == null) {
					  throw new IllegalArgumentException("No mapping passed to convertInputGradeToPoints for a letter-based gb");
				  }

				  if(mapping.getGradeMap() != null)
				  {
				      // standardize the grade mapping
				      final String standardizedGrade = mapping.standardizeInputGrade(grade);
					  percentage = mapping.getValue(standardizedGrade);
					  if(percentage == null)
					  {
						  throw new IllegalArgumentException("Invalid grade passed to convertInputGradeToPoints");
					  }
				  }
			  } else {
				  try {
					  final NumberFormat nbFormat = NumberFormat.getInstance(new ResourceLoader().getLocale());
					  percentage = nbFormat.parse(grade).doubleValue();
				  } catch (NumberFormatException | ParseException nfe) {
					  throw new IllegalArgumentException("Invalid % grade passed to convertInputGradeToPoints");
				  }
			  }

			  convertedValue = calculateEquivalentPointValueForPercent(gbItemPointsPossible, percentage);

		  } else {
			  throw new InvalidGradeException("invalid grade entry type passed to convertInputGradeToPoints");
		  }
	  }

	  return convertedValue;
  }

    @Override
	public int getGradeEntryType(final String gradebookUid) {
		if (gradebookUid == null) {
			throw new IllegalArgumentException("null gradebookUid passed to getGradeEntryType");
		}

		try {
			final Gradebook gradebook = getGradebook(gradebookUid);
			return gradebook.getGrade_type();
		} catch (final GradebookNotFoundException gnfe) {
			throw new GradebookNotFoundException("No gradebook exists with the given gradebookUid: " + gradebookUid);
		}
	}

    @Override
	public Map getEnteredCourseGrade(final String gradebookUid)
	{
		final HibernateCallback hc = new HibernateCallback() {
			@Override
			public Object doInHibernate(final Session session) throws HibernateException {
				final Gradebook thisGradebook = getGradebook(gradebookUid);

				final Long gradebookId = thisGradebook.getId();
				final CourseGrade courseGrade = getCourseGrade(gradebookId);

				Map enrollmentMap;

				final Map viewableEnrollmentsMap = GradebookServiceHibernateImpl.this.authz.findMatchingEnrollmentsForViewableCourseGrade(gradebookUid, thisGradebook.getCategory_type(), null, null);
				enrollmentMap = new HashMap();

				final Map enrollmentMapUid = new HashMap();
				for (final Iterator iter = viewableEnrollmentsMap.keySet().iterator(); iter.hasNext(); )
				{
					final EnrollmentRecord enr = (EnrollmentRecord)iter.next();
					enrollmentMap.put(enr.getUser().getUserUid(), enr);
					enrollmentMapUid.put(enr.getUser().getUserUid(), enr);
				}

				final Query q = session.createQuery("from CourseGradeRecord as cgr where cgr.gradableObject.id=:gradableObjectId");
				q.setLong("gradableObjectId", courseGrade.getId());
				final List records = filterAndPopulateCourseGradeRecordsByStudents(courseGrade, q.list(), enrollmentMap.keySet());

				final Map returnMap = new HashMap();

				for(int i=0; i<records.size(); i++)
				{
					final CourseGradeRecord cgr = (CourseGradeRecord) records.get(i);
					if(cgr.getEnteredGrade() != null && !cgr.getEnteredGrade().equalsIgnoreCase(""))
					{
						final EnrollmentRecord enr = (EnrollmentRecord)enrollmentMapUid.get(cgr.getStudentId());
						if(enr != null)
						{
							returnMap.put(enr.getUser().getDisplayId(), cgr.getEnteredGrade());
						}
					}
				}

				return returnMap;
			}
		};
		return (Map)getHibernateTemplate().execute(hc);
	}


  @Override
  public String getAssignmentScoreString(final String gradebookUid, final Long assignmentId, final String studentUid)
  throws GradebookNotFoundException, AssessmentNotFoundException
  {
		final boolean studentRequestingOwnScore = this.authn.getUserUid().equals(studentUid);

		if (gradebookUid == null || assignmentId == null || studentUid == null) {
			throw new IllegalArgumentException("null parameter passed to getAssignment. Values are gradebookUid:"
					+ gradebookUid + " assignmentId:" + assignmentId + " studentUid:" + studentUid);
		}

	  	final Double assignmentScore = (Double)getHibernateTemplate().execute(new HibernateCallback() {
	  		@Override
			public Object doInHibernate(final Session session) throws HibernateException {
	  			final GradebookAssignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentId);
	  			if (assignment == null) {
	  				throw new AssessmentNotFoundException("There is no assignment with id " + assignmentId + " in gradebook " + gradebookUid);
	  			}

	  			if (!studentRequestingOwnScore && !isUserAbleToViewItemForStudent(gradebookUid, assignmentId, studentUid)) {
	  				log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to retrieve grade for student {} for assignment {}", getUserUid(), gradebookUid, studentUid, assignment.getName());
	  				throw new GradebookSecurityException();
	  			}

	  			// If this is the student, then the assignment needs to have
	  			// been released.
	  			if (studentRequestingOwnScore && !assignment.isReleased()) {
	  				log.error("AUTHORIZATION FAILURE: Student {} in gradebook {} attempted to retrieve score for unreleased assignment {}", getUserUid(), gradebookUid, assignment.getName());
	  				throw new GradebookSecurityException();
	  			}

	  			final AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid);
	  			if (log.isDebugEnabled()) {
					log.debug("gradeRecord=" + gradeRecord);
				}
	  			if (gradeRecord == null) {
	  				return null;
	  			} else {
	  				return gradeRecord.getPointsEarned();
	  			}
	  		}
	  	});
	  	if (log.isDebugEnabled()) {
			log.debug("returning " + assignmentScore);
		}

	  	//TODO: when ungraded items is considered, change column to ungraded-grade
	  	//its possible that the assignment score is null
	  	if (assignmentScore == null) {
			return null;
		}

	  	// avoid scientific notation on large scores by using a formatter
	  	final DecimalFormat df = new DecimalFormat();
	  	df.setGroupingUsed(false);

	  	return df.format(assignmentScore);
  	}

  	@Override
  	public String getAssignmentScoreString(final String gradebookUid, final String assignmentName, final String studentUid)
  			throws GradebookNotFoundException, AssessmentNotFoundException {

		if (gradebookUid == null || assignmentName == null || studentUid == null) {
			throw new IllegalArgumentException("null parameter passed to getAssignment. Values are gradebookUid:"
					+ gradebookUid + " assignmentName:" + assignmentName + " studentUid:" + studentUid);
		}

		final GradebookAssignment assignment = (GradebookAssignment)getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(final Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, assignmentName);
			}
		});

		if (assignment == null) {
			throw new AssessmentNotFoundException("There is no assignment with name " + assignmentName + " in gradebook " + gradebookUid);
		}

		return getAssignmentScoreString(gradebookUid, assignment.getId(), studentUid);
  	}

	@Override
	public String getAssignmentScoreStringByNameOrId(final String gradebookUid, final String assignmentName, final String studentUid)
			throws GradebookNotFoundException, AssessmentNotFoundException {
		// TODO Auto-generated method stub
		String assignment = null;
		try {
			assignment = getAssignmentScoreString(gradebookUid, assignmentName, studentUid);
		}
		catch (final AssessmentNotFoundException e) {
			//Don't fail on this exception
			log.debug("Assessment not found by name", e);
		}

		if (assignment == null) {
			//Try to get the assignment by id
			if (NumberUtils.isCreatable(assignmentName)) {
				final Long assignmentId = NumberUtils.toLong(assignmentName, -1L);
				return getAssignmentScoreString(gradebookUid, assignmentId, studentUid);
			}
		}
		return null;
	}

  	@Override
	public void setAssignmentScoreString(final String gradebookUid, final Long assignmentId, final String studentUid, final String score, final String clientServiceDescription)
	throws GradebookNotFoundException, AssessmentNotFoundException
	{
		getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(final Session session) throws HibernateException {
				final GradebookAssignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentId);
				if (assignment == null) {
					throw new AssessmentNotFoundException("There is no assignment with id " + assignmentId + " in gradebook " + gradebookUid);
				}
				if (assignment.isExternallyMaintained()) {
					log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to grade externally maintained assignment {} from {}", getUserUid(), gradebookUid, assignmentId, clientServiceDescription);
					throw new GradebookSecurityException();
				}

				if (!isUserAbleToGradeItemForStudent(gradebookUid, assignment.getId(), studentUid)) {
					log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to grade student {} from {} for item {}", getUserUid(), gradebookUid, studentUid, clientServiceDescription, assignmentId);
					throw new GradebookSecurityException();
				}

				final Date now = new Date();
				final String graderId = getAuthn().getUserUid();
				AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid);
				if (gradeRecord == null) {
					// Creating a new grade record.
					gradeRecord = new AssignmentGradeRecord(assignment, studentUid, convertStringToDouble(score));
					//TODO: test if it's ungraded item or not. if yes, set ungraded grade for this record. if not, need validation??
				} else {
					//TODO: test if it's ungraded item or not. if yes, set ungraded grade for this record. if not, need validation??
					gradeRecord.setPointsEarned(convertStringToDouble(score));
				}
				gradeRecord.setGraderId(graderId);
				gradeRecord.setDateRecorded(now);
				session.saveOrUpdate(gradeRecord);

				session.save(new GradingEvent(assignment, graderId, studentUid, score));

				// Sync database.
				session.flush();
				session.clear();

           		// Post an event in SAKAI_EVENT table
           		postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, convertStringToDouble(score));
				return null;
			}
		});

		if (log.isDebugEnabled()) {
			log.debug("Score updated in gradebookUid=" + gradebookUid + ", assignmentId=" + assignmentId + " by userUid=" + getUserUid() + " from client=" + clientServiceDescription + ", new score=" + score);
		}
	}

  	@Override
	public void setAssignmentScoreString(final String gradebookUid, final String assignmentName, final String studentUid, final String score, final String clientServiceDescription)
			throws GradebookNotFoundException, AssessmentNotFoundException {

  		final GradebookAssignment assignment = (GradebookAssignment)getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(final Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, assignmentName);
			}
		});

		if (assignment == null) {
			throw new AssessmentNotFoundException("There is no assignment with name " + assignmentName + " in gradebook " + gradebookUid);
		}

		setAssignmentScoreString(gradebookUid, assignment.getId(), studentUid, score, clientServiceDescription);
  	}

    @Override
	public void finalizeGrades(final String gradebookUid)
			throws GradebookNotFoundException {
		if (!getAuthz().isUserAbleToGradeAll(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to finalize grades", getUserUid(), gradebookUid);
			throw new GradebookSecurityException();
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
	    		throw new GradebookSecurityException();
	    }

	    String lowestPossibleGrade = null;

	    if (gbItem.getUngraded()) {
	        lowestPossibleGrade = null;
	    } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE ||
	            gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS) {
	        lowestPossibleGrade = "0";
	    } else if (gbItem.getGradebook().getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
	        final LetterGradePercentMapping mapping = getLetterGradePercentMapping(gradebook);
	        lowestPossibleGrade = mapping.getGrade(0d);
	    }

	    return lowestPossibleGrade;
	}

    @Override
	public List<CategoryDefinition> getCategoryDefinitions(final String gradebookUid) {
	    if (gradebookUid == null) {
	        throw new IllegalArgumentException("Null gradebookUid passed to getCategoryDefinitions");
	    }

	    if (!isUserAbleToViewAssignments(gradebookUid)) {
	        log.warn("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to retrieve all categories without permission", getUserUid(), gradebookUid);
	        throw new GradebookSecurityException();
	    }

	    final List<CategoryDefinition> categoryDefList = new ArrayList<>();

	    final List<Category> gbCategories = getCategories(getGradebook(gradebookUid).getId());

	    if (gbCategories != null) {
	        for (final Category category : gbCategories) {
	            categoryDefList.add(getCategoryDefinition(category));
	        }
	    }

	    return categoryDefList;
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
	        categoryDef.setExtraCredit(category.isExtraCredit());
	        categoryDef.setCategoryOrder(category.getCategoryOrder());
	    }

	    return categoryDef;
	}


	/**
	 *
	 * @param gradebookId
	 * @param studentUids
	 * @return a map of studentUid to a list of that student's AssignmentGradeRecords for the given studentUids list
	 * in the given gradebook.  the grade records are all recs for assignments that are not removed and
	 * have a points possible > 0
	 */
	protected Map<String,List<AssignmentGradeRecord>> getGradeRecordMapForStudents(final Long gradebookId, final Collection<String> studentUids) {
	    final Map<String,List<AssignmentGradeRecord>> filteredGradeRecs = new HashMap<>();
	    if (studentUids != null) {
	        final List<AssignmentGradeRecord> allGradeRecs = getHibernateTemplate().execute(session -> session.createCriteria(AssignmentGradeRecord.class)
					.createAlias("gradableObject", "go")
					.createAlias("gradableObject.gradebook", "gb")
                    .add(Restrictions.eq("gb.id", gradebookId))
                    .add(Restrictions.eq("go.removed", false))
                    .add(HibernateCriterionUtils.CriterionInRestrictionSplitter("studentId", studentUids))
                    .list());

	        if (allGradeRecs != null) {
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
	        }
	    }

	    return filteredGradeRecs;
	}

	/**
	 *
	 * @param session
	 * @param gradebookId
	 * @return a list of Assignments that have not been removed, are "counted", graded,
	 * and have a points possible > 0
	 */
	protected List<GradebookAssignment> getCountedAssignments(final Session session, final Long gradebookId) {
	    final List<GradebookAssignment> assignList = new ArrayList<>();

	    final List <GradebookAssignment>results = session.createQuery(
        "from GradebookAssignment as asn where asn.gradebook.id=:gbid and asn.removed=false and " +
        "asn.notCounted=false and asn.ungraded=false").
        setParameter("gbid", gradebookId).
        list();

	    if (results != null) {
	    	// making sure there's no invalid points possible for normal assignments
	    	for (final GradebookAssignment a : results)
	    	{

	    		if (a.getPointsPossible()!=null && a.getPointsPossible()>0)
	    		{
	    			assignList.add(a);
	    		}
	    	}
	    }

	    return assignList;
	}

	/**
     * set the droppedFromGrade attribute of each
     * of the n highest and the n lowest scores of a
     * student based on the assignment's category
     * @param gradeRecords
     *
     * NOTE: When the UI changes, this needs to be made private again
     */
    public void applyDropScores(final Collection<AssignmentGradeRecord> gradeRecords) {
        if(gradeRecords == null || gradeRecords.size() < 1) {
            return;
        }
        final long start = System.currentTimeMillis();

        final List<String> studentIds = new ArrayList<>();
        final List<Category> categories = new ArrayList<>();
        final Map<String, List<AssignmentGradeRecord>> gradeRecordMap = new HashMap<>();
        for(final AssignmentGradeRecord gradeRecord : gradeRecords) {

            if(gradeRecord == null
                    || gradeRecord.getPointsEarned() == null) { // don't consider grades that have null pointsEarned (this occurs when a previously entered score for an assignment is removed; record stays in database)
                continue;
            }

            // reset
            gradeRecord.setDroppedFromGrade(false);

            final GradebookAssignment assignment = gradeRecord.getAssignment();
            if(assignment.getUngraded()  // GradebookService.GRADE_TYPE_LETTER
                    || assignment.isNotCounted() // don't consider grades that are not counted toward course grade
                    || assignment.getItemType().equals(GradebookAssignment.item_type_adjustment)
                    || assignment.isRemoved()) {
                continue;
            }
            // get all the students represented
            final String studentId = gradeRecord.getStudentId();
            if(!studentIds.contains(studentId)) {
                studentIds.add(studentId);
            }
            // get all the categories represented
            final Category cat = gradeRecord.getAssignment().getCategory();
            if(cat != null) {
                if(!categories.contains(cat)) {
                    categories.add(cat);
                }
                List<AssignmentGradeRecord> gradeRecordsByCatAndStudent = gradeRecordMap.get(studentId + cat.getId());
                if(gradeRecordsByCatAndStudent == null) {
                    gradeRecordsByCatAndStudent = new ArrayList<>();
                    gradeRecordsByCatAndStudent.add(gradeRecord);
                    gradeRecordMap.put(studentId + cat.getId(), gradeRecordsByCatAndStudent);
                } else {
                    gradeRecordsByCatAndStudent.add(gradeRecord);
                }
            }
        }

        if(categories.size() < 1) {
            return;
        }
        for(final Category cat : categories) {
            final Integer dropHighest = cat.getDropHighest();
            Integer dropLowest = cat.getDropLowest();
            final Integer keepHighest = cat.getKeepHighest();
            final Long catId = cat.getId();

            if((dropHighest != null && dropHighest > 0) || (dropLowest != null && dropLowest > 0) || (keepHighest != null && keepHighest > 0)) {

                for(final String studentId : studentIds) {
                    // get the student's gradeRecords for this category
                    final List<AssignmentGradeRecord> gradesByCategory = new ArrayList<>();
                    final List<AssignmentGradeRecord> gradeRecordsByCatAndStudent = gradeRecordMap.get(studentId + cat.getId());
                    if(gradeRecordsByCatAndStudent != null) {
                        gradesByCategory.addAll(gradeRecordsByCatAndStudent);

                        final int numGrades = gradesByCategory.size();

                        if(dropHighest > 0 && numGrades > dropHighest + dropLowest) {
                            for(int i=0; i<dropHighest; i++) {
                                final AssignmentGradeRecord highest = Collections.max(gradesByCategory, AssignmentGradeRecord.numericComparator);
                                highest.setDroppedFromGrade(true);
                                gradesByCategory.remove(highest);
                                if(log.isDebugEnabled()) {
									log.debug("dropHighest applied to " + highest);
								}
                            }
                        }

                        if(keepHighest > 0 && numGrades > (gradesByCategory.size() - keepHighest)) {
                            dropLowest = gradesByCategory.size() - keepHighest;
                        }

                        if(dropLowest > 0 &&  numGrades > dropLowest + dropHighest) {
                            for(int i=0; i<dropLowest; i++) {
                                final AssignmentGradeRecord lowest = Collections.min(gradesByCategory, AssignmentGradeRecord.numericComparator);
                                lowest.setDroppedFromGrade(true);
                                gradesByCategory.remove(lowest);
                                if(log.isDebugEnabled()) {
									log.debug("dropLowest applied to " + lowest);
								}
                            }
                        }
                    }
                }
                if(log.isDebugEnabled()) {
					log.debug("processed " + studentIds.size() + "students in category " + cat.getId());
				}
            }
        }

        if(log.isDebugEnabled()) {
			log.debug("GradebookManager.applyDropScores took " + (System.currentTimeMillis() - start) + " millis to execute");
		}
    }

	@Override
    public PointsPossibleValidation isPointsPossibleValid(final String gradebookUid, final org.sakaiproject.service.gradebook.shared.Assignment gradebookItem,
            final Double pointsPossible) {
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
        final double roundedVal = bd.doubleValue();
        final double diff = pointsPossible - roundedVal;
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
	    Double scoreAsDouble = null;
	    if (doubleAsString != null && !"".equals(doubleAsString)) {
	        try {
	        	final NumberFormat numberFormat = NumberFormat.getInstance(new ResourceLoader().getLocale());
				final Number numericScore = numberFormat.parse(doubleAsString.trim());
				scoreAsDouble = numericScore.doubleValue();
			} catch (final ParseException e) {
				log.error(e.getMessage());
			}
	    }

	    return scoreAsDouble;
	}

	/**
	 * Get a list of assignments in the gradebook attached to the given category.
	 * Note that each assignment only knows the category by name.
	 *
	 * <p>Note also that this is different to {@link BaseHibernateManager#getAssignmentsForCategory(Long)} because this method returns the shared GradebookAssignment object.
	 *
	 * @param gradebookUid
	 * @param categoryName
	 * @return
	 */
	private List<org.sakaiproject.service.gradebook.shared.Assignment> getAssignments(final String gradebookUid, final String categoryName) {

		final List<org.sakaiproject.service.gradebook.shared.Assignment> allAssignments = getAssignments(gradebookUid);
		final List<org.sakaiproject.service.gradebook.shared.Assignment> matchingAssignments = new ArrayList<>();

		for(final org.sakaiproject.service.gradebook.shared.Assignment assignment: allAssignments) {
			if(StringUtils.equals(assignment.getCategoryName(), categoryName)) {
				matchingAssignments.add(assignment);
			}
		}
		return matchingAssignments;
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
	private void postUpdateGradeEvent(final String gradebookUid, final String assignmentName, final String studentUid, final Double pointsEarned) {
	    if (this.eventTrackingService != null) {
            this.eventTrackingService.postEvent("gradebook.updateItemScore","/gradebook/"+gradebookUid+"/"+assignmentName+"/"+studentUid+"/"+pointsEarned+"/student");
        }
    }

	/**
	 * Retrieves the calculated average course grade.
	 */
	@Override
	public String getAverageCourseGrade(final String gradebookUid) {
	    if (gradebookUid == null) {
	        throw new IllegalArgumentException("Null gradebookUid passed to getAverageCourseGrade");
	    }
	    // Check user has permission to invoke method.
	    if (!currentUserHasGradeAllPerm(gradebookUid)) {
	    	final StringBuilder sb = new StringBuilder()
	    	.append("User ")
	    	.append(this.authn.getUserUid())
	    	.append(" attempted to access the average course grade without permission in gb ")
	    	.append(gradebookUid)
	    	.append(" using gradebookService.getAverageCourseGrade");
	        throw new GradebookSecurityException(sb.toString());
	    }

	    String courseGradeLetter = null;
	    final Gradebook gradebook = getGradebook(gradebookUid);
	    if (gradebook != null) {
		    final CourseGrade courseGrade = getCourseGrade(gradebook.getId());
		    final Set<String> studentUids = getAllStudentUids(gradebookUid);
		    // This call handles the complex rules of which assignments and grades to include in the calculation
		    final List<CourseGradeRecord> courseGradeRecs = getPointsEarnedCourseGradeRecords(courseGrade, studentUids);
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
	 * @see GradebookService.updateAssignmentOrder(java.lang.String gradebookUid, java.lang.Long assignmentId, java.lang.Integer order)
	 */
	@Override
	public void updateAssignmentOrder(final String gradebookUid, final Long assignmentId, Integer order) {

		if (!getAuthz().isUserAbleToEditAssessments(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to change the order of assignment {}", getUserUid(), gradebookUid, assignmentId);
			throw new GradebookSecurityException();
		}

		if(order == null) {
			throw new IllegalArgumentException("Order cannot be null");
		}

		final Long gradebookId = getGradebook(gradebookUid).getId();

		//get all assignments for this gradebook
		final List<GradebookAssignment> assignments = getAssignments(gradebookId, SortType.SORT_BY_SORTING, true);

		//adjust order to be within bounds
		if(order < 0) {
			order = 0;
		} else if (order > assignments.size()) {
			order = assignments.size();
		}

		//find the assignment
		GradebookAssignment target = null;
		for(final GradebookAssignment a: assignments){
			if(a.getId().equals(assignmentId)) {
				target = a;
				break;
			}
		}

		//add the assignment to the list via a 'pad, remove, add' approach
		assignments.add(null); //ensure size remains the same for the remove
		assignments.remove(target); //remove item
		assignments.add(order, target); //add at ordered position, will shuffle others along

		//the assignments are now in the correct order within the list, we just need to update the sort order for each one
		//create a new list for the assignments we need to update in the database
		final List<GradebookAssignment> assignmentsToUpdate = new ArrayList<>();

		int i = 0;
		for(final GradebookAssignment a: assignments){

			//skip if null
			if(a == null) {
				continue;
			}

			//if the sort order is not the same as the counter, update the order and add to the other list
			//this allows us to skip items that have not had their position changed and saves some db work later on
			//sort order may be null if never previously sorted, so give it the current index
			if(a.getSortOrder() == null || !a.getSortOrder().equals(i)) {
				a.setSortOrder(i);
				assignmentsToUpdate.add(a);
			}

			i++;
		}

		//do the updates
		for(final GradebookAssignment assignmentToUpdate: assignmentsToUpdate){
			getHibernateTemplate().execute(new HibernateCallback() {
				@Override
				public Object doInHibernate(final Session session) throws HibernateException {
					updateAssignment(assignmentToUpdate);
					return null;
				}
			});
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public List<GradingEvent> getGradingEvents(final String studentId, final long assignmentId) {

		if (log.isDebugEnabled()) {
    		log.debug("getGradingEvents called for studentId:" + studentId);
    	}

    	List<GradingEvent> rval = new ArrayList<>();

        if (studentId == null) {
        	log.debug("No student id was specified.  Returning an empty GradingEvents object");
        	return rval;
        }

        final HibernateCallback<List<GradingEvent>> hc = session -> {
            final Query q = session.createQuery("from GradingEvent as ge where ge.studentId=:studentId and ge.gradableObject.id=:assignmentId");
            q.setParameter("studentId", studentId);
            q.setParameter("assignmentId", assignmentId);
            return q.list();
        };

        rval = getHibernateTemplate().execute(hc);
        return rval;
    }

	@Override
	public Optional<CategoryScoreData> calculateCategoryScore(final Object gradebook, final String studentUuid, final CategoryDefinition category, final List<org.sakaiproject.service.gradebook.shared.Assignment> categoryAssignments, final Map<Long,String> gradeMap) {

		final Gradebook gb = (Gradebook) gradebook;

		// used for translating letter grades
		final Map<String, Double> gradingSchema = gb.getSelectedGradeMapping().getGradeMap();

		//collect the data and turn it into a list of AssignmentGradeRecords
		//this is the info that is compatible with both applyDropScores and the calculateCategoryScore method
		final List<AssignmentGradeRecord> gradeRecords = new ArrayList<>();
		for(final org.sakaiproject.service.gradebook.shared.Assignment assignment: categoryAssignments) {

			final Long assignmentId = assignment.getId();

			final String rawGrade = gradeMap.get(assignmentId);
			final Double pointsPossible = assignment.getPoints();
			Double grade;

			//determine the grade we should be using depending on the grading type
			if (gb.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
				grade = calculateEquivalentPointValueForPercent(pointsPossible, NumberUtils.createDouble(rawGrade));
			} else if (gb.getGrade_type() == GradebookService.GRADE_TYPE_LETTER){
				grade = gradingSchema.get(rawGrade);
			} else {
				grade = NumberUtils.createDouble(rawGrade);
			}

			//recreate the category (required fields only)
			final Category c = new Category();
			c.setId(category.getId());
			c.setDropHighest(category.getDropHighest());
			c.setDropLowest(category.getDropLowest());
			c.setKeepHighest(category.getKeepHighest());

			//recreate the assignment (required fields only)
			final GradebookAssignment a = new GradebookAssignment();
			a.setPointsPossible(assignment.getPoints());
			a.setUngraded(assignment.getUngraded());
			a.setCounted(assignment.isCounted());
			a.setExtraCredit(assignment.isExtraCredit());
			a.setReleased(assignment.isReleased());
			a.setRemoved(false); //shared.GradebookAssignment doesn't include removed so this will always be false
			a.setGradebook(gb);
			a.setCategory(c);
			a.setId(assignment.getId());  // store the id so we can find out later which grades were dropped, if any

			//create the AGR
			final AssignmentGradeRecord gradeRecord = new AssignmentGradeRecord(a, studentUuid, grade);

			gradeRecords.add(gradeRecord);
		}

		return calculateCategoryScore(studentUuid, category.getId(), gradeRecords);
	}

	@Override
	public Optional<CategoryScoreData> calculateCategoryScore(final Long gradebookId, final String studentUuid, final Long categoryId) {

		//get all grade records for the student
		@SuppressWarnings({ "unchecked", "rawtypes"})
		final
		Map<String, List<AssignmentGradeRecord>> gradeRecMap = (Map<String, List<AssignmentGradeRecord>>)getHibernateTemplate().execute(new HibernateCallback() {
            @Override
			public Object doInHibernate(final Session session) throws HibernateException {
                return getGradeRecordMapForStudents(gradebookId, Collections.singletonList(studentUuid));
            }
		});

		//apply the settings
		final List<AssignmentGradeRecord> gradeRecords = gradeRecMap.get(studentUuid);

		return calculateCategoryScore(studentUuid, categoryId, gradeRecords);
	}

	/**
	 * Does the heavy lifting for the category calculations.
	 * Requires the List of AssignmentGradeRecord so that we can applyDropScores.
	 * @param studentUuid the student uuid
	 * @param categoryId the category id we are interested in
	 * @param gradeRecords all grade records for the student
	 * @return
	 */
	private Optional<CategoryScoreData> calculateCategoryScore(final String studentUuid, final Long categoryId, final List<AssignmentGradeRecord> gradeRecords) {

		//validate
		if(gradeRecords == null) {
			log.debug("No grade records for student: {}. Nothing to do.", studentUuid);
			return Optional.empty();
		}

		if (categoryId == null) {
			log.debug("No category supplied, nothing to do.");
			return Optional.empty();
		}

		//setup
		int numScored = 0;
		int numOfAssignments = 0;
		BigDecimal totalEarned = new BigDecimal("0");
		BigDecimal totalPossible = new BigDecimal("0");

		// apply any drop/keep settings for this category
		applyDropScores(gradeRecords);

		// find the records marked as dropped (highest/lowest) before continuing,
		// as gradeRecords will be modified in place after this and these records will be removed
		List<Long> droppedItemIds = gradeRecords.stream()
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
		// Rule 5. the assignment is  released to the student (safety check against condition 3)
		// Rule 6. the grade is not dropped from the calc
		// Rule 7. extra credit items have their grade value counted only. Their total points possible does not apply to the calculations
		log.debug("categoryId: {}", categoryId);

		gradeRecords.removeIf(gradeRecord -> {
			final GradebookAssignment assignment = gradeRecord.getAssignment();

			// remove if not for this category (rule 1)
			if(assignment.getCategory() == null){
				return true;
			}
			if(categoryId.longValue() != assignment.getCategory().getId().longValue()){
				return true;
			}

			//remove if the assignment/graderecord doesn't meet the criteria for the calculation (rule 2-6)
			if(assignment.getPointsPossible() == null || gradeRecord.getPointsEarned() == null || !assignment.isCounted() || !assignment.isReleased() || gradeRecord.getDroppedFromGrade()) {
				return true;
			}

			return false;
		});

		log.debug("gradeRecords.size(): {}", gradeRecords.size());

		// pre-calculation
		// Rule 1. If category only has a single EC item, don't try to calculate category total.
		if(gradeRecords.size() == 1 && gradeRecords.get(0).getAssignment().isExtraCredit()) {
			return Optional.empty();
		}

		//iterate the filtered list and set the variables for the calculation
		for(final AssignmentGradeRecord gradeRecord: gradeRecords) {

			final GradebookAssignment assignment = gradeRecord.getAssignment();

			// EC item, don't count points possible
			if(!assignment.isExtraCredit()) {
				totalPossible = totalPossible.add(new BigDecimal(assignment.getPointsPossible().toString()));
				numOfAssignments++;
				numScored++;
			}

			//sanitise grade, null values to "0";
			final String grade = (gradeRecord.getPointsEarned() != null) ? String.valueOf(gradeRecord.getPointsEarned()) : "0";

			//update total points earned
			totalEarned = totalEarned.add(new BigDecimal(grade));

		}

		if (numScored == 0 || numOfAssignments == 0 || totalPossible.doubleValue() == 0) {
    		return Optional.empty();
    	}

    	final BigDecimal mean = totalEarned.divide(new BigDecimal(numScored), GradebookService.MATH_CONTEXT).divide((totalPossible.divide(new BigDecimal(numOfAssignments), GradebookService.MATH_CONTEXT)), GradebookService.MATH_CONTEXT).multiply(new BigDecimal("100"));
    	return Optional.of(new CategoryScoreData(mean.doubleValue(), droppedItemIds));
	}

	@Override
	public org.sakaiproject.service.gradebook.shared.CourseGrade getCourseGradeForStudent(final String gradebookUid, final String userUuid) {
		return this.getCourseGradeForStudents(gradebookUid, Collections.singletonList(userUuid)).get(userUuid);
	}

	@Override
	public Map<String,org.sakaiproject.service.gradebook.shared.CourseGrade> getCourseGradeForStudents(final String gradebookUid, final List<String> userUuids) {

		final Map<String,org.sakaiproject.service.gradebook.shared.CourseGrade> rval = new HashMap<>();

		try {
			final Gradebook gradebook = getGradebook(gradebookUid);
			final GradeMapping gradeMap = gradebook.getSelectedGradeMapping();

			rval.putAll(this.getCourseGradeForStudents(gradebookUid, userUuids, gradeMap.getGradeMap()));
		}
		catch(final Exception e) {
			log.error("Error in getCourseGradeForStudents", e);
		}
		return rval;
	}

	@Override
	public Map<String, org.sakaiproject.service.gradebook.shared.CourseGrade> getCourseGradeForStudents(final String gradebookUid,
			final List<String> userUuids, final Map<String, Double> gradeMap) {
		final Map<String,org.sakaiproject.service.gradebook.shared.CourseGrade> rval = new HashMap<>();

		try {
			final Gradebook gradebook = getGradebook(gradebookUid);

			//if not released, and not instructor or TA, don't do any work
			//note that this will return a course grade for Instructor and TA even if not released, see SAK-30119
			if(!gradebook.isCourseGradeDisplayed() && !(currentUserHasEditPerm(gradebookUid) || currentUserHasGradingPerm(gradebookUid))) {
				return rval;
			}

			final List<GradebookAssignment> assignments = getAssignmentsCounted(gradebook.getId());

			//this takes care of drop/keep scores
			final List<CourseGradeRecord> gradeRecords = getPointsEarnedCourseGradeRecords(getCourseGrade(gradebook.getId()), userUuids);

			// gradeMap MUST be sorted for the grade mapping to apply correctly
			final Map<String, Double> sortedGradeMap = GradeMappingDefinition.sortGradeMapping(gradeMap);

			gradeRecords.forEach(gr -> {

				final org.sakaiproject.service.gradebook.shared.CourseGrade cg = new org.sakaiproject.service.gradebook.shared.CourseGrade();

				//ID of the course grade item
				cg.setId(gr.getCourseGrade().getId());

				//set entered grade
				cg.setEnteredGrade(gr.getEnteredGrade());

				//set date recorded
				cg.setDateRecorded(gr.getDateRecorded());

				if(!assignments.isEmpty()) {

					//calculated grade
					//may be null if no grade entries to calculate
					final Double calculatedGrade = gr.getAutoCalculatedGrade();
					if(calculatedGrade != null) {
						cg.setCalculatedGrade(calculatedGrade.toString());
					}

					//mapped grade
					final String mappedGrade = GradeMapping.getMappedGrade(sortedGradeMap, calculatedGrade);
					log.debug("calculatedGrade: {} -> mappedGrade: {}", calculatedGrade, mappedGrade);
					cg.setMappedGrade(mappedGrade);

					//points
					cg.setPointsEarned(gr.getPointsEarned()); //synonymous with gradeRecord.getCalculatedPointsEarned()
					cg.setTotalPointsPossible(gr.getTotalPointsPossible());

				}
				rval.put(gr.getStudentId(), cg);
			});
		}
		catch(final Exception e) {
			log.error("Error in getCourseGradeForStudents", e);
		}
		return rval;
	}

	@Override
	public List<CourseSection> getViewableSections(final String gradebookUid) {
		return getAuthz().getViewableSections(gradebookUid);
	}

	@Override
	public void updateGradebookSettings(final String gradebookUid, final GradebookInformation gbInfo) {
		if (gradebookUid == null ) {
			throw new IllegalArgumentException("null gradebookUid " + gradebookUid) ;
		}

		//must be instructor type person
		if (!currentUserHasEditPerm(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to edit gb information", getUserUid(), gradebookUid);
			throw new GradebookSecurityException("You do not have permission to edit gradebook information in site " + gradebookUid);
		}

		final Gradebook gradebook = getGradebook(gradebookUid);
		if(gradebook==null) {
			throw new IllegalArgumentException("There is no gradebook associated with this id: " + gradebookUid);
		}

		final Map<String,Double> bottomPercents = gbInfo.getSelectedGradingScaleBottomPercents();

		//Before we do any work, check if any existing course grade overrides might be left in an unmappable state
		final List<CourseGradeRecord> courseGradeOverrides = getHibernateTemplate().execute(session -> getCourseGradeOverrides(gradebook));
		courseGradeOverrides.forEach(cgr -> {
			if(!bottomPercents.containsKey(cgr.getEnteredGrade())) {
				throw new UnmappableCourseGradeOverrideException("The grading schema could not be updated as it would leave some course grade overrides in an unmappable state.");
			}
		});

		//iterate all available grademappings for this gradebook and set the one that we have the ID and bottomPercents for
		final Set<GradeMapping> gradeMappings = gradebook.getGradeMappings();
		gradeMappings.forEach(gradeMapping -> {
			if(StringUtils.equals(Long.toString(gradeMapping.getId()), gbInfo.getSelectedGradeMappingId())) {
				gradebook.setSelectedGradeMapping(gradeMapping);

				//update the map values
				updateGradeMapping(gradeMapping.getId(), bottomPercents);
			}
		});

		//set grade type, but only if sakai.property is true OR user is admin
		boolean gradeTypeAvailForNonAdmins = ServerConfigurationService.getBoolean("gradebook.settings.gradeEntry.showToNonAdmins", true);
		if(gradeTypeAvailForNonAdmins || SecurityService.isSuperUser()) {
			gradebook.setGrade_type(gbInfo.getGradeType());
		}

		//set category type
		gradebook.setCategory_type(gbInfo.getCategoryType());

		//set display release items to students
		gradebook.setAssignmentsDisplayed(gbInfo.isDisplayReleasedGradeItemsToStudents());

		//set course grade display settings
		gradebook.setCourseGradeDisplayed(gbInfo.isCourseGradeDisplayed());
		gradebook.setCourseLetterGradeDisplayed(gbInfo.isCourseLetterGradeDisplayed());
		gradebook.setCoursePointsDisplayed(gbInfo.isCoursePointsDisplayed());
		gradebook.setCourseAverageDisplayed(gbInfo.isCourseAverageDisplayed());

		final List<CategoryDefinition> newCategoryDefinitions = gbInfo.getCategories();

		//if we have categories and they are weighted, check the weightings sum up to 100% (or 1 since it's a fraction)
		if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY) {
			double totalWeight = 0;
			for(final CategoryDefinition newDef: newCategoryDefinitions) {

				if(newDef.getWeight() == null) {
					throw new IllegalArgumentException("No weight specified for a category, but weightings enabled");
				}

				totalWeight += newDef.getWeight();
			}
			if(Math.rint(totalWeight) != 1) {
				throw new IllegalArgumentException("Weightings for the categories do not equal 100%");
			}
		}

		//get current categories and build a mapping list of Category.id to Category
		final List<Category> currentCategories = getCategories(gradebook.getId());
		final Map<Long,Category> currentCategoryMap = new HashMap<>();
		for(final Category c: currentCategories) {
			currentCategoryMap.put(c.getId(), c);
		}

		//compare current list with given list, add/update/remove as required
		//Rules:
		//If category does not have an ID it is new; add these later after all removals have been processed
		//If category has an ID it is to be updated. Update and remove from currentCategoryMap.
		//Any categories remaining in currentCategoryMap are to be removed.
		//Sort by category order as we resequence the order values to avoid gaps
		Collections.sort(newCategoryDefinitions, CategoryDefinition.orderComparator);
		final Map<CategoryDefinition, Integer> newCategories = new HashMap<>();
		int categoryIndex = 0;
		for(final CategoryDefinition newDef: newCategoryDefinitions) {

			//preprocessing and validation
			//Rule 1: If category has no name, it is to be removed/skipped
			//Note that we no longer set weights to 0 even if unweighted category type selected. The weights are not considered if its not a weighted category type
			//so this allows us to switch back and forth between types without losing information

			if(StringUtils.isBlank(newDef.getName())) {
				continue;
			}

			//new
			if(newDef.getId() == null) {
				newCategories.put(newDef, categoryIndex);
				categoryIndex++;
			}

			//update
			else {
				final Category existing = currentCategoryMap.get(newDef.getId());
				existing.setName(newDef.getName());
				existing.setWeight(newDef.getWeight());
				existing.setDropLowest(newDef.getDropLowest());
				existing.setDropHighest(newDef.getDropHighest());
				existing.setKeepHighest(newDef.getKeepHighest());
				existing.setExtraCredit(newDef.isExtraCredit());
				existing.setCategoryOrder(categoryIndex);
				updateCategory(existing);

				//remove from currentCategoryMap so we know not to delete it
				currentCategoryMap.remove(newDef.getId());

				categoryIndex++;
			}

		}

		//handle deletes
		//anything left in currentCategoryMap was not included in the new list, delete them
		for(final Entry<Long, Category> cat: currentCategoryMap.entrySet()) {
			removeCategory(cat.getKey());
		}

		// Handle the additions
		for(final Entry<CategoryDefinition, Integer> entry : newCategories.entrySet()) {
			final CategoryDefinition newCat = entry.getKey();
			this.createCategory(gradebook.getId(), newCat.getName(), newCat.getWeight(), newCat.getDropLowest(),
						newCat.getDropHighest(), newCat.getKeepHighest(), newCat.isExtraCredit(), entry.getValue());
		}

		//if weighted categories, all uncategorised assignments are to be removed from course grade calcs
		if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY) {
			excludeUncategorisedItemsFromCourseGradeCalculations(gradebook);
		}

		//persist
		updateGradebook(gradebook);

	}


	public Authz getAuthz() {
		return this.authz;
	}

	public void setAuthz(final Authz authz) {
		this.authz = authz;
	}

	public GradebookPermissionService getGradebookPermissionService() {
		return this.gradebookPermissionService;
	}

	public void setGradebookPermissionService(final GradebookPermissionService gradebookPermissionService) {
		this.gradebookPermissionService = gradebookPermissionService;
	}

	public void setSiteService(final SiteService siteService) {
		this.siteService = siteService;
	}

	public SiteService getSiteService() {
		return this.siteService;
	}

	@Override
	public Set getGradebookGradeMappings(final Long gradebookId) {
		return (Set)getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Set doInHibernate(final Session session) throws HibernateException {
				final Gradebook gradebook = (Gradebook)session.load(Gradebook.class, gradebookId);
				Hibernate.initialize(gradebook.getGradeMappings());
				return gradebook.getGradeMappings();
			}
		});
	}

	@Override
	public Set getGradebookGradeMappings(final String gradebookUid) {
		final Long gradebookId = getGradebook(gradebookUid).getId();
		return this.getGradebookGradeMappings(gradebookId);
	}

	@Override
	public void updateCourseGradeForStudent(final String gradebookUid, final String studentUuid, final String grade) {

		//must be instructor type person
		if (!currentUserHasEditPerm(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to update course grade for student: {}", getUserUid(),
					gradebookUid, studentUuid);
			throw new GradebookSecurityException("You do not have permission to update course grades in " + gradebookUid);
		}

		final Gradebook gradebook = getGradebook(gradebookUid);
		if(gradebook==null) {
			throw new IllegalArgumentException("There is no gradebook associated with this id: " + gradebookUid);
		}

		//get course grade for the student
		CourseGradeRecord courseGradeRecord = (CourseGradeRecord)getHibernateTemplate().execute(new HibernateCallback() {
            @Override
			public Object doInHibernate(final Session session) throws HibernateException {
                return getCourseGradeRecord(gradebook, studentUuid);
            }
		});

		//if user doesn't have an entered course grade, we need to find the course grade and create a record
		if(courseGradeRecord == null) {

			final CourseGrade courseGrade = getCourseGrade(gradebook.getId());

			courseGradeRecord = new CourseGradeRecord(courseGrade, studentUuid);
			courseGradeRecord.setGraderId(getUserUid());
			courseGradeRecord.setDateRecorded(new Date());

		} else {
			//if passed in grade override is same as existing grade override, nothing to do
			if(StringUtils.equals(courseGradeRecord.getEnteredGrade(), grade)) {
				return;
			}
		}

		//set the grade override
		courseGradeRecord.setEnteredGrade(grade);

		//create a grading event
		final GradingEvent gradingEvent = new GradingEvent();
		gradingEvent.setGradableObject(courseGradeRecord.getCourseGrade());
		gradingEvent.setGraderId(getUserUid());
		gradingEvent.setStudentId(studentUuid);
		gradingEvent.setGrade(courseGradeRecord.getEnteredGrade());

		//save
		getHibernateTemplate().saveOrUpdate(courseGradeRecord);
		getHibernateTemplate().saveOrUpdate(gradingEvent);
	}


	/**
	 * Map a set of GradeMapping to a list of GradeMappingDefinition
	 * @param gradeMappings set of GradeMapping
	 * @return list of GradeMappingDefinition
	 */
	private List<GradeMappingDefinition> getGradebookGradeMappings(final Set<GradeMapping> gradeMappings) {
		final List<GradeMappingDefinition> rval = new ArrayList<>();

		for(final GradeMapping mapping: gradeMappings) {
			rval.add(new GradeMappingDefinition(mapping.getId(), mapping.getName(),
					GradeMappingDefinition.sortGradeMapping(mapping.getGradeMap()),
					GradeMappingDefinition.sortGradeMapping(mapping.getDefaultBottomPercents())));
		}
		return rval;

	}


	/**
	 * Updates the categorized order of an assignment
	 *
	 * @see GradebookService.updateAssignmentCategorizedOrder(java.lang.String gradebookUid, java.lang.Long assignmentId, java.lang.Integer order)
	 */
	@Override
	public void updateAssignmentCategorizedOrder(final String gradebookUid, final Long categoryId, final Long assignmentId, Integer order) {

		if (!getAuthz().isUserAbleToEditAssessments(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User {} in gradebook {} attempted to change the order of assignment {}", getUserUid(), gradebookUid, assignmentId);
			throw new GradebookSecurityException();
		}

		if(order == null) {
			throw new IllegalArgumentException("Categorized Order cannot be null");
		}

		final Long gradebookId = getGradebook(gradebookUid).getId();

		//get all assignments for this gradebook
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

		//adjust order to be within bounds
		if(order < 0) {
			order = 0;
		} else if (order > assignmentsInNewCategory.size()) {
			order = assignmentsInNewCategory.size();
		}

		//find the assignment
		GradebookAssignment target = null;
		for(final GradebookAssignment a: assignmentsInNewCategory){
			if(a.getId().equals(assignmentId)) {
				target = a;
				break;
			}
		}

		//add the assignment to the list via a 'pad, remove, add' approach
		assignmentsInNewCategory.add(null); //ensure size remains the same for the remove
		assignmentsInNewCategory.remove(target); //remove item
		assignmentsInNewCategory.add(order, target); //add at ordered position, will shuffle others along

		//the assignments are now in the correct order within the list, we just need to update the sort order for each one
		//create a new list for the assignments we need to update in the database
		final List<GradebookAssignment> assignmentsToUpdate = new ArrayList<>();

		int i = 0;
		for(final GradebookAssignment a: assignmentsInNewCategory){

			//skip if null
			if(a == null) {
				continue;
			}

			//if the sort order is not the same as the counter, update the order and add to the other list
			//this allows us to skip items that have not had their position changed and saves some db work later on
			//sort order may be null if never previously sorted, so give it the current index
			if(a.getCategorizedSortOrder() == null || !a.getCategorizedSortOrder().equals(i)) {
				a.setCategorizedSortOrder(i);
				assignmentsToUpdate.add(a);
			}

			i++;
		}

		//do the updates
		for(final GradebookAssignment assignmentToUpdate: assignmentsToUpdate){
			getHibernateTemplate().execute(new HibernateCallback() {
				@Override
				public Object doInHibernate(final Session session) throws HibernateException {
					updateAssignment(assignmentToUpdate);
					return null;
				}
			});
		}

	}


	/**
	 * Return the grade changes made since a given time
	 * @param assignmentIds	ids of assignments to check
	 * @param since	timestamp from which to check for changes
	 * @return set of changes made
	 */
	@Override
	public List<GradingEvent> getGradingEvents(final List<Long> assignmentIds, final Date since) {
		List<GradingEvent> rval;

		if (since == null) {
			log.debug("No `since` timestamp was specified.  Returning null");
			return null;
		}

		final HibernateCallback<List<GradingEvent>> hc = session -> {
            final Query q = session.createQuery("from GradingEvent as ge where ge.dateGraded >= :since" +
                " and ge.gradableObject.id in (:assignmentIds)");
            q.setParameter("since", since);
            q.setParameterList("assignmentIds", assignmentIds);
            return q.list();
        };

		rval = getHibernateTemplate().execute(hc);
		return rval;
	}

	/**
	 * Update the persistent grade points for an assignment when the total points is changed.
	 *
	 * @param gradebook the gradebook
	 * @param assignment assignment with original total point value
	 */
	private void convertGradePointsForUpdatedTotalPoints(final Gradebook gradebook, final GradebookAssignment assignment, final Double originalPointsPossible) {
		if (gradebook == null || assignment == null || assignment.getPointsPossible() == null) {
			throw new IllegalArgumentException("null values found in convertGradePointsForUpdatedTotalPoints.");
		}

		final List<String> studentUids = getStudentsForGradebook(gradebook);
		final List<AssignmentGradeRecord> gradeRecords = getAllAssignmentGradeRecordsForGbItem(assignment.getId(), studentUids);

		// scale for total points changed when on percentage grading
		// TODO could scale for total points changed when on a points grading as well, though needs different logic
		if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {

			// Calculate the new points value that we should be persisting based on the new total points
			if (assignment.getPointsPossible() != null) {
				for (final AssignmentGradeRecord gr : gradeRecords) {
					if (gr.getPointsEarned() != null) {
						final BigDecimal scoreAsPercentage = (new BigDecimal(gr.getPointsEarned())
								.divide(new BigDecimal(originalPointsPossible), GradebookService.MATH_CONTEXT))
								.multiply(new BigDecimal(100));

						final Double scaledScore = calculateEquivalentPointValueForPercent(assignment.getPointsPossible(),
																						   scoreAsPercentage.doubleValue());

						gr.setPointsEarned(scaledScore);
					}
				}
			}
		}

		// save all
		batchPersistEntities(gradeRecords);
	}

	/**
	 * Get the list of students for the given gradebook
	 *
	 * @param gradebook the gradebook for the site
	 * @return a list of uuids for the students
	 */
	private List<String> getStudentsForGradebook(final Gradebook gradebook) {
		final List<EnrollmentRecord> enrolments = getSectionAwareness().getSiteMembersInRole(gradebook.getUid(), Role.STUDENT);

		final List<String> rval = enrolments.stream()
				.map(EnrollmentRecord::getUser)
				.map(User::getUserUid)
				.collect(Collectors.toList());

		return rval;
	}

	/**
	 * Helper to batch persist entities
	 *
	 * @param entities a list of entities.
	 */
	private void batchPersistEntities(final List<?> entities) {
		final Session session = getSessionFactory().getCurrentSession();
		entities.forEach(session::update);
	}

	private boolean isCurrentUserFromGroup(final String gradebookUid, final String studentId) {
		boolean isFromGroup=false;
		try {
			final Site s = this.siteService.getSite(gradebookUid);
			final Group g = s.getGroup(studentId);
			isFromGroup = (g != null) && (g.getMember(this.authn.getUserUid()) != null);
		} catch (final Exception e) {
			// Id not found
			log.error("Error in isCurrentUserFromGroup: ", e);
		}
		return isFromGroup;
	}

	/**
	 * Updates all uncategorised items to exclude them from the course grade calcs
	 * @param gradebook
	 */
	private void excludeUncategorisedItemsFromCourseGradeCalculations(final Gradebook gradebook) {
		final List<GradebookAssignment> allAssignments = getAssignments(gradebook.getId());

        final List<GradebookAssignment> assignments = allAssignments.stream().filter(a -> a.getCategory() == null).collect(Collectors.toList());
        assignments.forEach(a -> a.setCounted(false));
        batchPersistEntities(assignments);
	}
}
