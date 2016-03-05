/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation, The MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.component.gradebook;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.CommentDefinition;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradeMappingDefinition;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingCategoryNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookFrameworkService;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.GradebookPermissionService;
import org.sakaiproject.service.gradebook.shared.InvalidGradeException;
import org.sakaiproject.service.gradebook.shared.SortType;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.LetterGradePercentMapping;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.facades.EventTrackingService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;

/**
 * A Hibernate implementation of GradebookService.
 */
public class GradebookServiceHibernateImpl extends BaseHibernateManager implements GradebookService {
    private static final Log log = LogFactory.getLog(GradebookServiceHibernateImpl.class);

    private Authz authz;
    private GradebookPermissionService gradebookPermissionService;
    private EventTrackingService eventTrackingService;
	
    @Override
	public boolean isAssignmentDefined(final String gradebookUid, final String assignmentName)
        throws GradebookNotFoundException {
		if (!isUserAbleToViewAssignments(gradebookUid)) {
			log.warn("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to check for assignment " + assignmentName);
			throw new SecurityException("You do not have permission to perform this operation");
		}
        @SuppressWarnings("unchecked")
		Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, assignmentName, session);
			}
		});
        return (assignment != null);
    }

	private boolean isUserAbleToViewAssignments(String gradebookUid) {
		Authz authz = getAuthz();
		return (authz.isUserAbleToEditAssessments(gradebookUid) || authz.isUserAbleToGrade(gradebookUid));
	}

	@Override
	public boolean isUserAbleToGradeItemForStudent(String gradebookUid, Long itemId, String studentUid) {
		return getAuthz().isUserAbleToGradeItemForStudent(gradebookUid, itemId, studentUid);
	}
	
	@Override
	public boolean isUserAbleToViewItemForStudent(String gradebookUid, Long itemId, String studentUid) {
		return getAuthz().isUserAbleToViewItemForStudent(gradebookUid, itemId, studentUid);
	}
	
	@Override
	public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, Long itemId, String studentUid) {
		return getAuthz().getGradeViewFunctionForUserForStudentForItem(gradebookUid, itemId, studentUid);
	}

	@Override
	public List<org.sakaiproject.service.gradebook.shared.Assignment> getAssignments(String gradebookUid) throws GradebookNotFoundException {
		return getAssignments(gradebookUid, SortType.SORT_BY_NONE);
	}
	
	@Override
	public List<org.sakaiproject.service.gradebook.shared.Assignment> getAssignments(String gradebookUid, SortType sortBy) throws GradebookNotFoundException {
			if (!isUserAbleToViewAssignments(gradebookUid)) {
				log.warn("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to get assignments list");
				throw new SecurityException("You do not have permission to perform this operation");
			}

			final Long gradebookId = getGradebook(gradebookUid).getId();

	        @SuppressWarnings({ "unchecked", "rawtypes"})
			List<Assignment> internalAssignments = (List<Assignment>)getHibernateTemplate().execute(new HibernateCallback() {
	            public Object doInHibernate(Session session) throws HibernateException {
	                return getAssignments(gradebookId, session);
	            }
	        });
	        
	        sortAssignments(internalAssignments, sortBy, true);

			List<org.sakaiproject.service.gradebook.shared.Assignment> assignments = new ArrayList<org.sakaiproject.service.gradebook.shared.Assignment>();
			for (Iterator<Assignment> iter = internalAssignments.iterator(); iter.hasNext(); ) {
				Assignment assignment = (Assignment)iter.next();
				assignments.add(getAssignmentDefinition(assignment));
			}
			return assignments;
		}
	
	@Override
	public org.sakaiproject.service.gradebook.shared.Assignment getAssignment(final String gradebookUid, final Long assignmentId) throws AssessmentNotFoundException {
		if (assignmentId == null || gradebookUid == null) {
			throw new IllegalArgumentException("null parameter passed to getAssignment");
		}
		if (!isUserAbleToViewAssignments(gradebookUid) && !currentUserHasViewOwnGradesPerm(gradebookUid)) {
			log.warn("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to get assignment with id " + assignmentId);
			throw new SecurityException("You do not have permission to perform this operation");
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, assignmentId, session);
			}
		});
		
		if (assignment == null) {
			throw new AssessmentNotFoundException("No gradebook item exists with gradable object id = " + assignmentId);
		}
		
		return getAssignmentDefinition(assignment);
	}
	
	@Override
	@Deprecated
	public org.sakaiproject.service.gradebook.shared.Assignment getAssignment(final String gradebookUid, final String assignmentName) throws AssessmentNotFoundException {
		if (assignmentName == null || gradebookUid == null) {
			throw new IllegalArgumentException("null parameter passed to getAssignment");
		}
		if (!isUserAbleToViewAssignments(gradebookUid) && !currentUserHasViewOwnGradesPerm(gradebookUid)) {
			log.warn("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to get assignment " + assignmentName);
			throw new SecurityException("You do not have permission to perform this operation");
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, assignmentName, session);
			}
		});
		
		if (assignment != null) {
			return getAssignmentDefinition(assignment);
		} else {
			return null;
		}
	}
	
	
	private org.sakaiproject.service.gradebook.shared.Assignment getAssignmentDefinition(Assignment internalAssignment) {
		org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition = new org.sakaiproject.service.gradebook.shared.Assignment();
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
		
		final boolean studentRequestingOwnScore = authn.getUserUid().equals(studentUid);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		GradeDefinition gradeDef = (GradeDefinition)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				
				Assignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentId, session);
	
				if (assignment == null) {
					throw new AssessmentNotFoundException("There is no assignment with the assignmentId " + assignmentId + " in gradebook " + gradebookUid);
				}
				
				if (!studentRequestingOwnScore && !isUserAbleToViewItemForStudent(gradebookUid, assignment.getId(), studentUid)) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to retrieve grade for student " + studentUid + " for assignment " + assignmentId);
					throw new SecurityException("You do not have permission to perform this operation");
				}
				
				Gradebook gradebook = assignment.getGradebook();
				
				GradeDefinition gradeDef = new GradeDefinition();
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
					log.debug("Student " + getUserUid() + " in gradebook " + gradebookUid + " retrieving score for unreleased assignment " + assignment.getName());		
				
				} else {
				
					AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid, session);
					CommentDefinition gradeComment = getAssignmentScoreComment(gradebookUid, assignmentId, studentUid);
					String commentText = gradeComment != null ? gradeComment.getCommentText() : null;
					if (log.isDebugEnabled()) log.debug("gradeRecord=" + gradeRecord);
					
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
							List<AssignmentGradeRecord> gradeList = new ArrayList<AssignmentGradeRecord>();
							gradeList.add(gradeRecord);
							convertPointsToLetterGrade(gradebook, gradeList);
							AssignmentGradeRecord gradeRec = (AssignmentGradeRecord)gradeList.get(0);
							if (gradeRec != null) {
								gradeDef.setGrade(gradeRec.getLetterEarned());
							}
						} else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
							Double percent = calculateEquivalentPercent(assignment.getPointsPossible(), gradeRecord.getPointsEarned());
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
		if (log.isDebugEnabled()) log.debug("returning grade def for " + studentUid);
		return gradeDef;
	}
	
	@Override
	public String getGradebookDefinitionXml(String gradebookUid) {		
		Long gradebookId = getGradebook(gradebookUid).getId();
		Gradebook gradebook = getGradebook(gradebookUid);
		
		GradebookDefinition gradebookDefinition = new GradebookDefinition();
		GradeMapping selectedGradeMapping = gradebook.getSelectedGradeMapping();
		gradebookDefinition.setSelectedGradingScaleUid(selectedGradeMapping.getGradingScale().getUid());
		gradebookDefinition.setSelectedGradingScaleBottomPercents(new HashMap<String,Double>(selectedGradeMapping.getGradeMap()));
		gradebookDefinition.setAssignments(getAssignments(gradebookUid));
		
		gradebookDefinition.setGradeType(gradebook.getGrade_type());
		gradebookDefinition.setCategoryType(gradebook.getCategory_type());	
		gradebookDefinition.setCategory(getCategories(gradebookId));
		
		return VersionedExternalizable.toXml(gradebookDefinition);
	}
	
	@Override
	public GradebookInformation getGradebookInformation(String gradebookUid) {
		
		if (gradebookUid == null ) {
	          throw new IllegalArgumentException("null gradebookUid " + gradebookUid) ;
		}
	    
        if (!currentUserHasEditPerm(gradebookUid) && !currentUserHasGradingPerm(gradebookUid)) {
            log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to access gb information");
                throw new SecurityException("You do not have permission to access gradebook information in site " + gradebookUid);
        }
	        
		Gradebook gradebook = getGradebook(gradebookUid);
		if(gradebook==null) {
			throw new IllegalArgumentException("Their is no gradbook associated with this Id: "+gradebookUid);
		}
		
		GradebookInformation rval = new GradebookInformation();
		
		//add in all available grademappings for this gradebook
		rval.setGradeMappings(getGradebookGradeMappings(gradebook.getGradeMappings()));
		
		//add in details about the selected one
		GradeMapping selectedGradeMapping = gradebook.getSelectedGradeMapping();
		if(selectedGradeMapping!=null) {
		
			rval.setSelectedGradingScaleUid(selectedGradeMapping.getGradingScale().getUid());
			rval.setSelectedGradeMappingId(Long.toString(selectedGradeMapping.getId()));
			
			//note that these are not the DEFAULT bottom percents but the configured ones per gradebook
			rval.setSelectedGradingScaleBottomPercents(new HashMap<String,Double>(selectedGradeMapping.getGradeMap()));
			rval.setGradeScale(selectedGradeMapping.getGradingScale().getName());
		}
		
		rval.setGradeType(gradebook.getGrade_type());
		rval.setCategoryType(gradebook.getCategory_type());
		rval.setDisplayReleasedGradeItemsToStudents(gradebook.isAssignmentsDisplayed());

		//add in the category definitions
		rval.setCategories(this.getCategoryDefinitions(gradebookUid));
		
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
	public void transferGradebookDefinitionXml(String fromGradebookUid, String toGradebookUid, String fromGradebookXml) {
		final Gradebook gradebook = getGradebook(toGradebookUid);
		final Gradebook fromGradebook = getGradebook(fromGradebookUid);
		
		GradebookDefinition gradebookDefinition = (GradebookDefinition)VersionedExternalizable.fromXml(fromGradebookXml);
		
		gradebook.setCategory_type(gradebookDefinition.getCategoryType());
		gradebook.setGrade_type(gradebookDefinition.getGradeType());
		
		updateGradebook(gradebook);
		
		List category = getCategories(fromGradebook.getId());
	
		int assignmentsAddedCount = 0;
		Long catId = null;
		int undefined_nb = 0;
		
		List catList = gradebookDefinition.getCategory();
		List<Category> catList_tempt = new ArrayList<Category>();
				
		if(category.size() !=0) {
			//deal with category with assignments
			for(Iterator iter = category.iterator(); iter.hasNext();) {
			
				int categoryCount = 0;
				String catName = ((Category)iter.next()).getName();
		
				for (org.sakaiproject.service.gradebook.shared.Assignment obj : gradebookDefinition.getAssignments()) {
					org.sakaiproject.service.gradebook.shared.Assignment assignmentDef = (org.sakaiproject.service.gradebook.shared.Assignment)obj;
				
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
							Category catTempt = getCategory(catId);
							
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
			Iterator it_tempt = catList_tempt.iterator();
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
						
			Iterator itUpdate = catList.iterator();
			while(itUpdate.hasNext()){
				Category catObj = (Category)itUpdate.next();
				createCategory(gradebook.getId(), catObj.getName(), catObj.getWeight(), catObj.getDrop_lowest(), catObj.getDropHighest(), catObj.getKeepHighest(), catObj.isExtraCredit());				
			}
		}
		//deal with no categories
		else {
			for (org.sakaiproject.service.gradebook.shared.Assignment obj : gradebookDefinition.getAssignments()) {
				org.sakaiproject.service.gradebook.shared.Assignment assignmentDef = (org.sakaiproject.service.gradebook.shared.Assignment)obj;
				
				// Externally managed assessments should not be included.
				if (assignmentDef.isExternallyMaintained()) {
					continue;
				}
				
				// All assignments should be unreleased even if they were released in the original.
			
				createAssignment(gradebook.getId(), assignmentDef.getName(), assignmentDef.getPoints(), assignmentDef.getDueDate(), true, false, assignmentDef.isExtraCredit());
				assignmentsAddedCount++;
			}	
			
		}
				
		if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " added " + assignmentsAddedCount + " assignments");
		
			// Carry over the old gradebook's selected grading scheme if possible.
		String fromGradingScaleUid = gradebookDefinition.getSelectedGradingScaleUid();
		MERGE_GRADE_MAPPING: if (!StringUtils.isEmpty(fromGradingScaleUid)) {
		for (GradeMapping gradeMapping : gradebook.getGradeMappings()) {
				if (gradeMapping.getGradingScale().getUid().equals(fromGradingScaleUid)) {
					// We have a match. Now make sure that the grades are as expected.
					Map<String, Double> inputGradePercents = gradebookDefinition.getSelectedGradingScaleBottomPercents();
					Set<String> gradeCodes = (Set<String>)inputGradePercents.keySet();
					if (gradeCodes.containsAll(gradeMapping.getGradeMap().keySet())) {
						// Modify the existing grade-to-percentage map.
						for (String gradeCode : gradeCodes) {
							gradeMapping.getGradeMap().put(gradeCode, inputGradePercents.get(gradeCode));							
						}
						gradebook.setSelectedGradeMapping(gradeMapping);
						updateGradebook(gradebook);
						if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " updated grade mapping");
					} else {
						if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because the " + fromGradingScaleUid + " grade codes did not match");
					}
					break MERGE_GRADE_MAPPING;
				}
			}
			// Did not find a matching grading scale.
			if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because grading scale " + fromGradingScaleUid + " is not defined");
		}
	}
	
	@Override
	public void transferGradebook(final GradebookInformation gradebookInformation, final List<org.sakaiproject.service.gradebook.shared.Assignment> assignments, final String toGradebookUid) {

		final Gradebook gradebook = getGradebook(toGradebookUid);
				
		gradebook.setCategory_type(gradebookInformation.getCategoryType());
		gradebook.setGrade_type(gradebookInformation.getGradeType());
		
		updateGradebook(gradebook);
		
		//all categories that we need to end up with
		List<CategoryDefinition> categories = gradebookInformation.getCategories();
		
		//filter out externally managed assignments. These are never imported.
		assignments.removeIf(a -> a.isExternallyMaintained());	
	
		//this map holds the names of categories that have been created in the site to the category ids
		//and is updated as we go along
		//likewise for list of assignments
		Map<String, Long> categoriesCreated = new HashMap<>();
		List<String> assignmentsCreated = new ArrayList<>();
	
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
							} catch (ConflictingCategoryNameException e) {
								//category already exists. Could be from a merge.
								log.info("Category: " + c.getName() + " already exists in target site. Skipping creation.");
							} 
							
							if(categoryId == null) {
								//couldn't create so look up the id in the target site
								List<CategoryDefinition> existingCategories = this.getCategoryDefinitions(gradebook.getUid());
								categoryId = existingCategories.stream().filter(e -> StringUtils.equals(e.getName(), c.getName())).findFirst().get().getId();
							}
							//record that we have created this category
							categoriesCreated.put(c.getName(), categoryId);
							
						}	
						
						//create the assignment for the current category
						try {
							createAssignmentForCategory(gradebook.getId(), categoriesCreated.get(c.getName()), a.getName(), a.getPoints(), a.getDueDate(), true, false, a.isExtraCredit());
						} catch (ConflictingAssignmentNameException e) {
							//assignment already exists. Could be from a merge.
							log.info("Assignment: " + a.getName() + " already exists in target site. Skipping creation.");
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
					createCategory(gradebook.getId(), c.getName(), c.getWeight(), c.getDrop_lowest(), c.getDropHighest(), c.getKeepHighest(), c.isExtraCredit());
				} catch (ConflictingCategoryNameException e) {
					//category already exists. Could be from a merge.
					log.info("Category: " + c.getName() + " already exists in target site. Skipping creation.");
				}
			});						
		}
	
		//create any remaining assignments that have no categories
		assignments.removeIf(a -> assignmentsCreated.contains(a.getName()));	
		assignments.forEach(a -> {
			try {
				createAssignment(gradebook.getId(), a.getName(), a.getPoints(), a.getDueDate(), true, false, a.isExtraCredit());
			} catch (ConflictingAssignmentNameException e) {
				//assignment already exists. Could be from a merge.
				log.info("Assignment: " + a.getName() + " already exists in target site. Skipping creation.");
			} 
		});							
		
		// Carry over the old gradebook's selected grading scheme if possible.
		String fromGradingScaleUid = gradebookInformation.getSelectedGradingScaleUid();
		
		MERGE_GRADE_MAPPING: if (!StringUtils.isEmpty(fromGradingScaleUid)) {
		for (GradeMapping gradeMapping : gradebook.getGradeMappings()) {
				if (gradeMapping.getGradingScale().getUid().equals(fromGradingScaleUid)) {
					// We have a match. Now make sure that the grades are as expected.
					Map<String, Double> inputGradePercents = gradebookInformation.getSelectedGradingScaleBottomPercents();
					Set<String> gradeCodes = inputGradePercents.keySet();
					if (gradeCodes.containsAll(gradeMapping.getGradeMap().keySet())) {
						// Modify the existing grade-to-percentage map.
						for (String gradeCode : gradeCodes) {
							gradeMapping.getGradeMap().put(gradeCode, inputGradePercents.get(gradeCode));							
						}
						gradebook.setSelectedGradeMapping(gradeMapping);
						updateGradebook(gradebook);
						log.info("Merge to gradebook " + toGradebookUid + " updated grade mapping");
					} else {
						log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because the " + fromGradingScaleUid + " grade codes did not match");
					}
					break MERGE_GRADE_MAPPING;
				}
			}
			// Did not find a matching grading scale.
			log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because grading scale " + fromGradingScaleUid + " is not defined");
		}
		
		
	}
	
	@Override
	public void mergeGradebookDefinitionXml(String toGradebookUid, String fromGradebookXml) {
		final Gradebook gradebook = getGradebook(toGradebookUid);
		GradebookDefinition gradebookDefinition = (GradebookDefinition)VersionedExternalizable.fromXml(fromGradebookXml);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<String> assignmentNames = (List<String>)getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(final Session session) throws HibernateException {
            	return session.createQuery(
            		"select asn.name from Assignment as asn where asn.gradebook.id=? and asn.removed=false").
            		setLong(0, gradebook.getId().longValue()).
            		list();
            }
        });
		
		// Add any non-externally-managed assignments with non-duplicate names.
		int assignmentsAddedCount = 0;
		for (org.sakaiproject.service.gradebook.shared.Assignment obj : gradebookDefinition.getAssignments()) {
			org.sakaiproject.service.gradebook.shared.Assignment assignmentDef = (org.sakaiproject.service.gradebook.shared.Assignment)obj;
			
			// Externally managed assessments should not be included.
			if (assignmentDef.isExternallyMaintained()) {
				continue;
			}

			// Skip any input assignments with duplicate names.
			if (assignmentNames.contains(assignmentDef.getName())) {
				if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " skipped duplicate assignment named " + assignmentDef.getName());
				continue;				
			}
			
			// All assignments should be unreleased even if they were released in the original.
			createAssignment(gradebook.getId(), assignmentDef.getName(), assignmentDef.getPoints(), assignmentDef.getDueDate(), true, false, assignmentDef.isExtraCredit());
			assignmentsAddedCount++;
		}
		if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " added " + assignmentsAddedCount + " assignments");
		
		// Carry over the old gradebook's selected grading scheme if possible.
		String fromGradingScaleUid = gradebookDefinition.getSelectedGradingScaleUid();
		MERGE_GRADE_MAPPING: if (!StringUtils.isEmpty(fromGradingScaleUid)) {
			for (GradeMapping gradeMapping : gradebook.getGradeMappings()) {
				if (gradeMapping.getGradingScale().getUid().equals(fromGradingScaleUid)) {
					// We have a match. Now make sure that the grades are as expected.
					Map<String, Double> inputGradePercents = gradebookDefinition.getSelectedGradingScaleBottomPercents();
					Set<String> gradeCodes = (Set<String>)inputGradePercents.keySet();
					if (gradeCodes.containsAll(gradeMapping.getGradeMap().keySet())) {
						// Modify the existing grade-to-percentage map.
						for (String gradeCode : gradeCodes) {
							gradeMapping.getGradeMap().put(gradeCode, inputGradePercents.get(gradeCode));							
						}
						gradebook.setSelectedGradeMapping(gradeMapping);
						updateGradebook(gradebook);
						if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " updated grade mapping");
					} else {
						if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because the " + fromGradingScaleUid + " grade codes did not match");
					}
					break MERGE_GRADE_MAPPING;
				}			
			}
			// Did not find a matching grading scale.
			if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because grading scale " + fromGradingScaleUid + " is not defined");
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void removeAssignment(final Long assignmentId) throws StaleObjectModificationException {
    	
		HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Assignment asn = (Assignment)session.load(Assignment.class, assignmentId);
                Gradebook gradebook = asn.getGradebook();
                asn.setRemoved(true);
                session.update(asn);
                
                if(log.isInfoEnabled()) log.info("Assignment " + asn.getName() + " has been removed from " + gradebook);
                return null;
            }
        };
        getHibernateTemplate().execute(hc);
        
    }
	
	@Override
	public Long addAssignment(String gradebookUid, org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition) {
		if (!getAuthz().isUserAbleToEditAssessments(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to add an assignment");
			throw new SecurityException("You do not have permission to perform this operation");
		}

        // Ensure that points is > zero.
		Double points = assignmentDefinition.getPoints();
        if ((points == null) || (points.doubleValue() <= 0)) {
            throw new AssignmentHasIllegalPointsException("Points must be > 0");
        }

		Gradebook gradebook = getGradebook(gradebookUid);
		
		//if attaching to category
		if(assignmentDefinition.getCategoryId() != null) {
			return createAssignmentForCategory(gradebook.getId(), assignmentDefinition.getCategoryId(), assignmentDefinition.getName(), points, assignmentDefinition.getDueDate(), !assignmentDefinition.isCounted(), assignmentDefinition.isReleased(), assignmentDefinition.isExtraCredit());
		}
		
		return createAssignment(gradebook.getId(), assignmentDefinition.getName(), points, assignmentDefinition.getDueDate(), !assignmentDefinition.isCounted(), assignmentDefinition.isReleased(), assignmentDefinition.isExtraCredit());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void updateAssignment(final String gradebookUid, final Long assignmentId, final org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition) {		
		if (!getAuthz().isUserAbleToEditAssessments(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to change the definition of assignment " + assignmentId);
			throw new SecurityException("You do not have permission to perform this operation");
		}
		
		getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException {
				final Assignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentId, session);
				if (assignment == null) {
					throw new AssessmentNotFoundException("There is no assignment with id " + assignmentId + " in gradebook " + gradebookUid);
				}
				
				//external assessments are supported, but not these fields
				if (!assignmentDefinition.isExternallyMaintained()) {
					assignment.setName(assignmentDefinition.getName().trim());
					assignment.setPointsPossible(assignmentDefinition.getPoints());
					assignment.setDueDate(assignmentDefinition.getDueDate());
				}
				assignment.setExtraCredit(assignmentDefinition.isExtraCredit());
				assignment.setCounted(assignmentDefinition.isCounted());
				assignment.setReleased(assignmentDefinition.isReleased());
				
				//if we have a category, get it and set it
				//otherwise clear it fully
				if (assignmentDefinition.getCategoryId() != null) {
					Category cat = (Category) session.load(Category.class, assignmentDefinition.getCategoryId());
					assignment.setCategory(cat);
				} else {
					assignment.setCategory(null);
				}
				
				updateAssignment(assignment, session);
				return null;
			}
		});
	}

	@Override
	public Map<String, String> getImportCourseGrade(String gradebookUid)
	{
		return getImportCourseGrade(gradebookUid, true, true);
	}

	@Override
	public Map<String, String> getImportCourseGrade(String gradebookUid, boolean useDefault)
	{
		return getImportCourseGrade(gradebookUid, useDefault, true);
	}

	@Override
	public Map<String, String> getImportCourseGrade(String gradebookUid, boolean useDefault, boolean mapTheGrades)
	{
		HashMap<String, String> returnMap = new HashMap<String, String> ();

		try
		{
			//There is a new permission for course grade visibility for TA's as part of GradebookNG.
			//However the permission cannot be added here as it is not backwards compatible with Gradebook classique
			//  and would mean that all existing permissions need to be updated to add it.
			//See GradebookNgBusinessService.isCourseGradeVisible.
			//At some point it should be migrated and a DB conversion performed.
			
			Gradebook thisGradebook = getGradebook(gradebookUid);
			
			List assignList = getAssignmentsCounted(thisGradebook.getId());
			boolean nonAssignment = false;
			if(assignList == null || assignList.size() < 1)
			{
				nonAssignment = true;
			}
			
			Long gradebookId = thisGradebook.getId();
			CourseGrade courseGrade = getCourseGrade(gradebookId);

			Map viewableEnrollmentsMap = authz.findMatchingEnrollmentsForViewableCourseGrade(gradebookUid, thisGradebook.getCategory_type(), null, null);
			Map<String, EnrollmentRecord> enrollmentMap = new HashMap<String, EnrollmentRecord>();

			Map<String, EnrollmentRecord> enrollmentMapUid = new HashMap<String, EnrollmentRecord>();
			for (Iterator iter = viewableEnrollmentsMap.keySet().iterator(); iter.hasNext(); ) 
			{
				EnrollmentRecord enr = (EnrollmentRecord)iter.next();
				enrollmentMap.put(enr.getUser().getUserUid(), enr);
				enrollmentMapUid.put(enr.getUser().getUserUid(), enr);
			}
			List gradeRecords = getPointsEarnedCourseGradeRecords(courseGrade, enrollmentMap.keySet());
			for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) 
			{
				CourseGradeRecord gradeRecord = (CourseGradeRecord)iter.next();

				GradeMapping gradeMap= thisGradebook.getSelectedGradeMapping();

				EnrollmentRecord enr = enrollmentMapUid.get(gradeRecord.getStudentId());
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
							Double grade = null;

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
								returnMap.put(enr.getUser().getDisplayId(), (String)gradeMap.getGrade(grade));
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
		catch(Exception e)
		{
			log.error("Error in getImportCourseGrade", e);
		}
		return returnMap;
	}

	@Override
	public CourseGrade getCourseGrade(Long gradebookId) {
		return (CourseGrade)getHibernateTemplate().find(
				"from CourseGrade as cg where cg.gradebook.id=?",
				gradebookId).get(0);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List getPointsEarnedCourseGradeRecords(final CourseGrade courseGrade, final Collection studentUids) {
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				if(studentUids == null || studentUids.size() == 0) {
					if(log.isInfoEnabled()) log.info("Returning no grade records for an empty collection of student UIDs");
					return new ArrayList();
				}

				Query q = session.createQuery("from CourseGradeRecord as cgr where cgr.gradableObject.id=:gradableObjectId");
				q.setLong("gradableObjectId", courseGrade.getId().longValue());
				List records = filterAndPopulateCourseGradeRecordsByStudents(courseGrade, q.list(), studentUids);

				Long gradebookId = courseGrade.getGradebook().getId();
				Gradebook gradebook = getGradebook(gradebookId);
				List cates = getCategories(gradebookId);
				
				// get all of the AssignmentGradeRecords here to avoid repeated db calls
    			Map<String, List<AssignmentGradeRecord>> gradeRecMap = getGradeRecordMapForStudents(session, gradebookId, studentUids);
    			
    			// get all of the counted assignments
    			List<Assignment> assignments = getCountedAssignments(session, gradebookId);
    			List<Assignment> countedAssigns = new ArrayList<Assignment>();
    			if (assignments != null) {
    	                    for (Assignment assign : assignments) {
    	                        // extra check to account for new features like extra credit
    	                        if (assign.isIncludedInCalculations()) {
    	                            countedAssigns.add(assign);
    	                        }
    	                    }
    	                }
				//double totalPointsPossible = getTotalPointsInternal(gradebookId, session);
				//if(log.isDebugEnabled()) log.debug("Total points = " + totalPointsPossible);

				for(Iterator iter = records.iterator(); iter.hasNext();) {
					CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
					//double totalPointsEarned = getTotalPointsEarnedInternal(gradebookId, cgr.getStudentId(), session);
					List<AssignmentGradeRecord> studentGradeRecs = gradeRecMap.get(cgr.getStudentId());
    				
    				applyDropScores(studentGradeRecs);
					List totalEarned = getTotalPointsEarnedInternal(cgr.getStudentId(), gradebook, cates, studentGradeRecs, countedAssigns);
					double totalPointsEarned = ((Double)totalEarned.get(0)).doubleValue();
					double literalTotalPointsEarned = ((Double)totalEarned.get(1)).doubleValue();
					double totalPointsPossible = getTotalPointsInternal(gradebook, cates, cgr.getStudentId(), studentGradeRecs, countedAssigns, false);
					cgr.initNonpersistentFields(totalPointsPossible, totalPointsEarned, literalTotalPointsEarned);
					if(log.isDebugEnabled()) log.debug("Points earned = " + cgr.getPointsEarned());
					if(log.isDebugEnabled()) log.debug("Points possible = " + cgr.getTotalPointsPossible());
				}

				return records;
			}
		};
		return (List)getHibernateTemplate().execute(hc);
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List filterAndPopulateCourseGradeRecordsByStudents(CourseGrade courseGrade, Collection gradeRecords, Collection studentUids) {
		List filteredRecords = new ArrayList();
		Set missingStudents = new HashSet(studentUids);
		for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
			if (studentUids.contains(cgr.getStudentId())) {
				filteredRecords.add(cgr);
				missingStudents.remove(cgr.getStudentId());
			}
		}
		for (Iterator iter = missingStudents.iterator(); iter.hasNext(); ) {
			String studentUid = (String)iter.next();
			CourseGradeRecord cgr = new CourseGradeRecord(courseGrade, studentUid);
			filteredRecords.add(cgr);
		}
		return filteredRecords;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private double getTotalPointsInternal(final Gradebook gradebook, final List categories, final String studentId, List<AssignmentGradeRecord> studentGradeRecs, List<Assignment> countedAssigns, boolean literalTotal)
        {
            int gbGradeType = gradebook.getGrade_type();
            if( gbGradeType != GradebookService.GRADE_TYPE_POINTS && gbGradeType != GradebookService.GRADE_TYPE_PERCENTAGE)
            {
                if(log.isInfoEnabled()) log.error("Wrong grade type in GradebookCalculationImpl.getTotalPointsInternal");
                return -1;
            }

            if (studentGradeRecs == null || countedAssigns == null) {
                if (log.isDebugEnabled()) log.debug("Returning 0 from getTotalPointsInternal " +
                        "since studentGradeRecs or countedAssigns was null");
                return 0;
            }

            double totalPointsPossible = 0;

            HashSet<Assignment> countedSet = new HashSet<Assignment>(countedAssigns);

            // we need to filter this list to identify only "counted" grade recs
            List<AssignmentGradeRecord> countedGradeRecs = new ArrayList<AssignmentGradeRecord>();
            for (AssignmentGradeRecord gradeRec : studentGradeRecs) {
                Assignment assign = gradeRec.getAssignment();
                boolean extraCredit = assign.isExtraCredit();
                if(gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY && assign.getCategory() != null && assign.getCategory().isExtraCredit())
                    extraCredit = true;

                if (assign.isCounted() && !assign.getUngraded() && !assign.isRemoved() && countedSet.contains(assign) &&
                        assign.getPointsPossible() != null && assign.getPointsPossible() > 0 && !gradeRec.getDroppedFromGrade() && !extraCredit) {
                    countedGradeRecs.add(gradeRec);
                }
            }

            Set assignmentsTaken = new HashSet();
            Set categoryTaken = new HashSet();
            for (AssignmentGradeRecord gradeRec : countedGradeRecs)
            {
                if (gradeRec.getPointsEarned() != null && !gradeRec.getPointsEarned().equals("")) 
                {
                    Double pointsEarned = new Double(gradeRec.getPointsEarned());
                    Assignment go = gradeRec.getAssignment();
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
                                Category cate = (Category) categories.get(i);
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
                        Category cate = (Category) categories.get(i);
                        if(cate != null && !cate.isRemoved() && categoryTaken.contains(cate.getId()) )
                        {
                            totalPointsPossible += cate.getWeight().doubleValue();
                        }
                    }
                    return totalPointsPossible;
                }
                Iterator assignmentIter = countedAssigns.iterator();
                while (assignmentIter.hasNext()) 
                {
                    Assignment asn = (Assignment) assignmentIter.next();
                    if(asn != null)
                    {
                        Double pointsPossible = asn.getPointsPossible();

                        if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY && assignmentsTaken.contains(asn.getId()))
                        {
                            totalPointsPossible += pointsPossible.doubleValue();
                        }
                        else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && assignmentsTaken.contains(asn.getId()))
                        {
                            totalPointsPossible += pointsPossible.doubleValue();
                        }else if(literalTotal && gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && assignmentsTaken.contains(asn.getId()))
                        {
                            totalPointsPossible += pointsPossible.doubleValue();
                        }
                    }
                }
            }
            else
                totalPointsPossible = -1;

            return totalPointsPossible;
        }

	       
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List getTotalPointsEarnedInternal(final String studentId, final Gradebook gradebook, final List categories,
	        final List<AssignmentGradeRecord> gradeRecs, List<Assignment> countedAssigns) 
	{
	    int gbGradeType = gradebook.getGrade_type();
	    if( gbGradeType != GradebookService.GRADE_TYPE_POINTS && gbGradeType != GradebookService.GRADE_TYPE_PERCENTAGE)
	    {
	        if(log.isInfoEnabled()) log.error("Wrong grade type in GradebookCalculationImpl.getTotalPointsEarnedInternal");
	        return new ArrayList();
	    }

	    if (gradeRecs == null || countedAssigns == null) {
	        if (log.isDebugEnabled()) log.debug("getTotalPointsEarnedInternal for " +
	                "studentId=" + studentId + " returning 0 because null gradeRecs or countedAssigns");
	        List returnList = new ArrayList();
	        returnList.add(new Double(0));
	        returnList.add(new Double(0));
	        returnList.add(new Double(0)); // 3rd one is for the pre-adjusted course grade
	        return returnList;
	    }


	    double totalPointsEarned = 0;
	    BigDecimal literalTotalPointsEarned = new BigDecimal(0d);

	    Map cateScoreMap = new HashMap();
	    Map cateTotalScoreMap = new HashMap();

	    Set assignmentsTaken = new HashSet();
	    for (AssignmentGradeRecord gradeRec : gradeRecs)
	    {
	        if(gradeRec.getPointsEarned() != null && !gradeRec.getPointsEarned().equals("") && !gradeRec.getDroppedFromGrade())
	        {
	            Assignment go = gradeRec.getAssignment();
	            if (go.isIncludedInCalculations() && countedAssigns.contains(go))
	            {
	                Double pointsEarned = new Double(gradeRec.getPointsEarned());
	                //if(gbGradeType == GradebookService.GRADE_TYPE_POINTS)
	                //{
	                if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
	                {
	                    totalPointsEarned += pointsEarned.doubleValue();
	                    literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
	                    assignmentsTaken.add(go.getId());
	                }
	                else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && go != null)
	                {
	                    totalPointsEarned += pointsEarned.doubleValue();
	                    literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
	                    assignmentsTaken.add(go.getId());
	                }
	                else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && go != null && categories != null)
	                {
	                    for(int i=0; i<categories.size(); i++)
	                    {
	                        Category cate = (Category) categories.get(i);
	                        if(cate != null && !cate.isRemoved() && go.getCategory() != null && cate.getId().equals(go.getCategory().getId()))
	                        {
	                            assignmentsTaken.add(go.getId());
	                            literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
	                            if(cateScoreMap.get(cate.getId()) != null)
	                            {
	                                cateScoreMap.put(cate.getId(), new Double(((Double)cateScoreMap.get(cate.getId())).doubleValue() + pointsEarned.doubleValue()));
	                            }
	                            else
	                            {
	                                cateScoreMap.put(cate.getId(), new Double(pointsEarned));
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
	        Iterator assgnsIter = countedAssigns.iterator();
	        while (assgnsIter.hasNext()) 
	        {
	            Assignment asgn = (Assignment)assgnsIter.next();
	            if(assignmentsTaken.contains(asgn.getId()))
	            {
	                for(int i=0; i<categories.size(); i++)
	                {
	                    Category cate = (Category) categories.get(i);
	                    if(cate != null && !cate.isRemoved() && asgn.getCategory() != null && cate.getId().equals(asgn.getCategory().getId()) && !asgn.isExtraCredit())
	                    {

	                        if(cateTotalScoreMap.get(cate.getId()) == null)
	                        {                                                               
	                            cateTotalScoreMap.put(cate.getId(), asgn.getPointsPossible());
	                        }
	                        else
	                        {                                                               
	                            cateTotalScoreMap.put(cate.getId(), new Double(((Double)cateTotalScoreMap.get(cate.getId())).doubleValue() + asgn.getPointsPossible().doubleValue()));                                                  
	                        }

	                    }
	                }
	            }
	        }
	    }

	    if(assignmentsTaken.isEmpty())
	        totalPointsEarned = -1;

	    if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
	    {
	        for(int i=0; i<categories.size(); i++)
	        {
	            Category cate = (Category) categories.get(i);
	            if(cate != null && !cate.isRemoved() && cateScoreMap.get(cate.getId()) != null && cateTotalScoreMap.get(cate.getId()) != null)
	            {
	                totalPointsEarned += ((Double)cateScoreMap.get(cate.getId())).doubleValue() * cate.getWeight().doubleValue() / ((Double)cateTotalScoreMap.get(cate.getId())).doubleValue();
	            }
	        }
	    }

	    if (log.isDebugEnabled()) log.debug("getTotalPointsEarnedInternal for studentId=" + studentId + " returning " + totalPointsEarned);
	    List returnList = new ArrayList();
	    returnList.add(new Double(totalPointsEarned));
	    returnList.add(new Double((new BigDecimal(literalTotalPointsEarned.doubleValue(), GradebookService.MATH_CONTEXT)).doubleValue()));

	    return returnList;
	}

	/**
	 * Internal method to get a gradebook based on its id.
	 * @param id
	 * @return
	 * 
	 * NOTE: When the UI changes, this is to be turned private again
	 */
	public Gradebook getGradebook(Long id) {
		return (Gradebook)getHibernateTemplate().load(Gradebook.class, id);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List getAssignmentsCounted(final Long gradebookId) throws HibernateException 
	{
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				List assignments = session.createQuery(
						"from Assignment as asn where asn.gradebook.id=? and asn.removed=false and asn.notCounted=false").
						setLong(0, gradebookId.longValue()).
						list();
				return assignments;
			}
		};
		return (List)getHibernateTemplate().execute(hc);
	}
	
  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public boolean checkStudentsNotSubmitted(String gradebookUid)
  {
  	Gradebook gradebook = getGradebook(gradebookUid);
  	Set studentUids = getAllStudentUids(getGradebookUid(gradebook.getId()));
  	if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY || gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY)
  	{
  		List records = getAllAssignmentGradeRecords(gradebook.getId(), studentUids);
  		List assigns = getAssignments(gradebook.getId(), SortType.SORT_BY_SORTING, true);
  		List filteredAssigns = new ArrayList();
  		for(Iterator iter = assigns.iterator(); iter.hasNext();)
  		{
  			Assignment assignment = (Assignment)iter.next();
  			if(assignment.isCounted() && !assignment.getUngraded())
  				filteredAssigns.add(assignment);
  		}
  		List filteredRecords = new ArrayList();
  		for(Iterator iter = records.iterator(); iter.hasNext();)
  		{
  			AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
  			if(!agr.isCourseGradeRecord() && agr.getAssignment().isCounted() && !agr.getAssignment().getUngraded())
  			{
  				if(agr.getPointsEarned() == null)
  					return true;
  				filteredRecords.add(agr);
  			}
  		}

  		if(filteredRecords.size() < (filteredAssigns.size() * studentUids.size()))
  			return true;
  		
  		return false;
  	}
  	else
  	{
    	List assigns = getAssignments(gradebook.getId(), SortType.SORT_BY_SORTING, true);
    	List records = getAllAssignmentGradeRecords(gradebook.getId(), studentUids);
    	Set filteredAssigns = new HashSet();
    	for (Iterator iter = assigns.iterator(); iter.hasNext(); )
    	{
    		Assignment assign = (Assignment) iter.next();
    		if(assign != null && assign.isCounted() && !assign.getUngraded())
    		{
    			if(assign.getCategory() != null && !assign.getCategory().isRemoved())
    			{
    				filteredAssigns.add(assign.getId());
    			}
    		}
    	}
    	
  		List filteredRecords = new ArrayList();
  		for(Iterator iter = records.iterator(); iter.hasNext();)
  		{
  			AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
  			if(filteredAssigns.contains(agr.getAssignment().getId()) && !agr.isCourseGradeRecord())
  			{
  				if(agr.getPointsEarned() == null)
  					return true;
  				filteredRecords.add(agr);
  			}
  		}

  		if(filteredRecords.size() < filteredAssigns.size() * studentUids.size())
  			return true;

  		return false;
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
  	HibernateCallback hc = new HibernateCallback() {
  		public Object doInHibernate(Session session) throws HibernateException {
  			if(studentUids.size() == 0) {
  				// If there are no enrollments, no need to execute the query.
  				if(log.isInfoEnabled()) log.info("No enrollments were specified.  Returning an empty List of grade records");
  				return new ArrayList();
  			} else {
  				Query q = session.createQuery("from AssignmentGradeRecord as agr where agr.gradableObject.removed=false and " +
  				"agr.gradableObject.gradebook.id=:gradebookId order by agr.pointsEarned");
  				q.setLong("gradebookId", gradebookId.longValue());
  				return filterGradeRecordsByStudents(q.list(), studentUids);
  			}
  		}
  	};
  	return (List)getHibernateTemplate().execute(hc);
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private List getAllAssignmentGradeRecordsForGbItem(final Long gradableObjectId, 
		  final Collection studentUids) {
	  	HibernateCallback hc = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException {
	  			if(studentUids.size() == 0) {
	  				// If there are no enrollments, no need to execute the query.
	  				if(log.isInfoEnabled()) log.info("No enrollments were specified.  Returning an empty List of grade records");
	  				return new ArrayList();
	  			} else {
	  				Query q = session.createQuery("from AssignmentGradeRecord as agr where agr.gradableObject.removed=false and " +
	  				"agr.gradableObject.id=:gradableObjectId order by agr.pointsEarned");
	  				q.setLong("gradableObjectId", gradableObjectId.longValue());
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
		HibernateCallback hc = new HibernateCallback()
		{
			public Object doInHibernate(Session session) throws HibernateException
			{
				List<AssignmentGradeRecord> gradeRecords = new ArrayList<AssignmentGradeRecord>();
				if (studentUids.size() == 0)
				{
					// If there are no enrollments, no need to execute the query.
					if (log.isDebugEnabled()) log.debug("No enrollments were specified. Returning an empty List of grade records");
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
						Query q = session.createQuery("from AssignmentGradeRecord as agr where agr.gradableObject.removed = false and " +
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
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List getAssignments(final Long gradebookId, final SortType sortBy, final boolean ascending) {
  	return (List)getHibernateTemplate().execute(new HibernateCallback() {
  		public Object doInHibernate(Session session) throws HibernateException {
  			List assignments = getAssignments(gradebookId, session);

  			sortAssignments(assignments, sortBy, ascending);
  			return assignments;
  		}
  	});
  }
  
  /**
   * Sort the list of (internal) assignments by the given criteria
   * @param assignments
   * @param sortBy
   * @param ascending
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void sortAssignments(List assignments, SortType sortBy, boolean ascending) {
  	
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
			 comp = Assignment.pointsComparator;
			 break;
	  	case SORT_BY_RELEASED:
			 comp = Assignment.releasedComparator;
			 break;
	  	case SORT_BY_COUNTED:
			 comp = Assignment.countedComparator;
			 break;
	  	case SORT_BY_EDITOR:
			 comp = Assignment.gradeEditorComparator;
			 break;
	  	case SORT_BY_SORTING:
			 comp = Assignment.sortingComparator;
			 break;
			case SORT_BY_CATEGORY:
				comp = Assignment.categoryComparator;
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
	public List<org.sakaiproject.service.gradebook.shared.Assignment> getViewableAssignmentsForCurrentUser(String gradebookUid)
			throws GradebookNotFoundException {
		return getViewableAssignmentsForCurrentUser(gradebookUid, SortType.SORT_BY_SORTING);
	}

  /*
   * (non-Javadoc)
   * @see org.sakaiproject.service.gradebook.shared.GradebookService#getViewableAssignmentsForCurrentUser(java.lang.String, java.)
   */
  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<org.sakaiproject.service.gradebook.shared.Assignment> getViewableAssignmentsForCurrentUser(String gradebookUid, SortType sortBy)
  throws GradebookNotFoundException {

	  List<Assignment> viewableAssignments = new ArrayList<>();
		LinkedHashSet<org.sakaiproject.service.gradebook.shared.Assignment> assignmentsToReturn = new LinkedHashSet<>();

	  Gradebook gradebook = getGradebook(gradebookUid);

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
				  assignmentsToReturn.addAll(getAssignments(gradebookUid));
			  } else {

				  String userUid = getUserUid();
				  if (getGradebookPermissionService().getPermissionForUserForAllAssignment(gradebook.getId(), userUid)) {
					  assignmentsToReturn.addAll(getAssignments(gradebookUid));
				  }

				  // categories are enabled, so we need to check the category restrictions
				  List allCategories = getCategoriesWithAssignments(gradebook.getId());
				  if (allCategories != null && !allCategories.isEmpty()) {
					  List<Long> catIds = new ArrayList<Long>();
					  for (Category category : (List<Category>) allCategories) {
						  catIds.add(category.getId());
					  }
					  List<Long> viewableCategorieIds = getGradebookPermissionService().getCategoriesForUser(gradebook.getId(), userUid, catIds);
					  List<Category> viewableCategories = new ArrayList<Category>();
					  for (Category category : (List<Category>) allCategories) {
						  if(viewableCategorieIds.contains(category.getId())){
							  viewableCategories.add(category);
						  }
					  }
					  
					  for (Iterator catIter = viewableCategories.iterator(); catIter.hasNext();) {
						  Category cat = (Category) catIter.next();
						  if (cat != null) {
							  List assignments = cat.getAssignmentList();
							  if (assignments != null && !assignments.isEmpty()) {
								  viewableAssignments.addAll(assignments);
							  }
						  }
					  }
				  }
			  }
		  }
	  } else if (getAuthz().isUserAbleToViewOwnGrades(gradebookUid)) {
		  // if user is just a student, we need to filter out unreleased items
		  List allAssigns = getAssignments(gradebook.getId(), null, true);
		  if (allAssigns != null) {
			  for (Iterator aIter = allAssigns.iterator(); aIter.hasNext();) {
				  Assignment assign = (Assignment) aIter.next();
				  if (assign != null && assign.isReleased()) {
					  viewableAssignments.add(assign);
				  }
			  }
		  }
	  }

	  // Now we need to convert these to the assignment template objects
	  if (viewableAssignments != null && !viewableAssignments.isEmpty()) {
		  for (Iterator assignIter = viewableAssignments.iterator(); assignIter.hasNext();) {
			  Assignment assignment = (Assignment) assignIter.next();
			  assignmentsToReturn.add(getAssignmentDefinition(assignment));
		  }
	  }

	  return new ArrayList<>(assignmentsToReturn);

  }
  
  @Override
  public Map<String, String> getViewableStudentsForItemForCurrentUser(final String gradebookUid, final Long gradableObjectId) {
	  String userUid = authn.getUserUid();

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
      
      if (!authz.isUserAbleToGrade(gradebookUid, userUid)) {
          return new HashMap<>();
      }

      Assignment gradebookItem = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException {
              return getAssignmentWithoutStats(gradebookUid, gradableObjectId, session);
          }
      });

      if (gradebookItem == null) {
          log.debug("The gradebook item does not exist, so returning empty set");
          return new HashMap();
      }

      Long categoryId = gradebookItem.getCategory() == null ? null : gradebookItem.getCategory().getId();

      Map<EnrollmentRecord, String> enrRecFunctionMap  = authz.findMatchingEnrollmentsForItemForUser(userUid, gradebookUid, categoryId, getGradebook(gradebookUid).getCategory_type(), null, null);
      if (enrRecFunctionMap == null) {
          return new HashMap();
      }

      Map<String, String> studentIdFunctionMap = new HashMap();
      for (Iterator<Entry<EnrollmentRecord, String>> enrIter = enrRecFunctionMap.entrySet().iterator(); enrIter.hasNext();) {
    	  Entry<EnrollmentRecord, String> entry = enrIter.next();
          EnrollmentRecord enr = entry.getKey();
          if (enr != null && enrRecFunctionMap.get(enr) != null) {
              studentIdFunctionMap.put(enr.getUser().getUserUid(), entry.getValue());
          }
      }
      return studentIdFunctionMap;
  }

  @Override
  public boolean isGradableObjectDefined(Long gradableObjectId) {
	  if (gradableObjectId == null) {
		  throw new IllegalArgumentException("null gradableObjectId passed to isGradableObjectDefined");
	  }
	  
	  return isAssignmentDefined(gradableObjectId);
  }
  
  @Override
  public Map getViewableSectionUuidToNameMap(String gradebookUid) {
	  if (gradebookUid == null) {
		  throw new IllegalArgumentException("Null gradebookUid passed to getViewableSectionIdToNameMap");
	  }
	  
	  Map<String, String> sectionIdNameMap = new HashMap<>();
	  
	  List viewableCourseSections = getAuthz().getViewableSections(gradebookUid); 
	  if (viewableCourseSections == null || viewableCourseSections.isEmpty()) {
		  return sectionIdNameMap;
	  }
	  
	  for (Iterator sectionIter = viewableCourseSections.iterator(); sectionIter.hasNext();) {
		  CourseSection section = (CourseSection) sectionIter.next();
		  if (section != null) {
			  sectionIdNameMap.put(section.getUuid(), section.getTitle());
		  }
	  }
	  
	  return sectionIdNameMap;
  }
  
  @Override
  public boolean currentUserHasGradeAllPerm(String gradebookUid) {
	  return authz.isUserAbleToGradeAll(gradebookUid);
  }
  
  @Override
  public boolean isUserAllowedToGradeAll(String gradebookUid, String userUid) {
      return authz.isUserAbleToGradeAll(gradebookUid, userUid);
  }

  @Override
  public boolean currentUserHasGradingPerm(String gradebookUid) {
	  return authz.isUserAbleToGrade(gradebookUid);
  }
  
  @Override
  public boolean isUserAllowedToGrade(String gradebookUid, String userUid) {
      return authz.isUserAbleToGrade(gradebookUid, userUid);
  }

  @Override
  public boolean currentUserHasEditPerm(String gradebookUid) {
	  return authz.isUserAbleToEditAssessments(gradebookUid);
  }

  @Override
  public boolean currentUserHasViewOwnGradesPerm(String gradebookUid) {
	  return authz.isUserAbleToViewOwnGrades(gradebookUid);
  }
  
  @Override
  public List<GradeDefinition> getGradesForStudentsForItem(final String gradebookUid, final Long gradableObjectId, List<String> studentIds) {
	  if (gradableObjectId == null) {
		  throw new IllegalArgumentException("null gradableObjectId passed to getGradesForStudentsForItem");
	  }
	  
	  List<org.sakaiproject.service.gradebook.shared.GradeDefinition> studentGrades = new ArrayList();
	  
	  if (studentIds != null && !studentIds.isEmpty()) {
		  // first, we need to make sure the current user is authorized to view the
		  // grades for all of the requested students
		  Assignment gbItem = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			  public Object doInHibernate(Session session) throws HibernateException {
				  return getAssignmentWithoutStats(gradebookUid, gradableObjectId, session);
			  }
		  });
		  
		  if (gbItem != null) {
			  Gradebook gradebook = gbItem.getGradebook();
			  
			  if (!authz.isUserAbleToGrade(gradebook.getUid())) {
				  throw new SecurityException("User " + authn.getUserUid() + 
						  " attempted to access grade information without permission in gb " + 
						  gradebook.getUid() + " using gradebookService.getGradesForStudentsForItem");
			  }
			  
			  Long categoryId = gbItem.getCategory() != null ? gbItem.getCategory().getId() : null;
			  Map enrRecFunctionMap = authz.findMatchingEnrollmentsForItem(gradebook.getUid(), categoryId, gradebook.getCategory_type(), null, null);
			  Set enrRecs = enrRecFunctionMap.keySet();
			  Map studentIdEnrRecMap = new HashMap();
			  if (enrRecs != null) {
				  for (Iterator enrIter = enrRecs.iterator(); enrIter.hasNext();) {
					  EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
					  if (enr != null) {
						  studentIdEnrRecMap.put(enr.getUser().getUserUid(), enr);
					  }
				  }
			  }
			  
			  for (Iterator stIter = studentIds.iterator(); stIter.hasNext();) {
				  String studentId = (String) stIter.next();
				  if (studentId != null) {
					  if (!studentIdEnrRecMap.containsKey(studentId)) {
						  throw new SecurityException("User " + authn.getUserUid() + 
						  " attempted to access grade information for student " + studentId + 
						  " without permission in gb " + gradebook.getUid() + 
						  " using gradebookService.getGradesForStudentsForItem");
					  }
				  }
			  }
			  
			  // retrieve the grading comments for all of the students
			  List<Comment> commentRecs = getComments(gbItem, studentIds);
			  Map<String, String> studentIdCommentTextMap = new HashMap();
			  if (commentRecs != null) {
				  for (Iterator<Comment> cIter = commentRecs.iterator(); cIter.hasNext();) {
					  Comment comment = cIter.next();
					  if (comment != null) {
						  studentIdCommentTextMap.put(comment.getStudentId(), comment.getCommentText());
					  }
				  }
			  }
			  
			  // now, we can populate the grade information
			  List<AssignmentGradeRecord> gradeRecs = getAllAssignmentGradeRecordsForGbItem(gradableObjectId, studentIds);
			  if (gradeRecs != null) {
				  if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
					  convertPointsToLetterGrade(gradebook, gradeRecs);
				  } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
					  convertPointsToPercentage(gradebook, gradeRecs);
				  }
				  				  
				  for (Iterator gradeIter = gradeRecs.iterator(); gradeIter.hasNext();) {
					  AssignmentGradeRecord agr = (AssignmentGradeRecord) gradeIter.next();
					  if (agr != null) {
						  String commentText = studentIdCommentTextMap.get(agr.getStudentId());
						  GradeDefinition gradeDef = convertGradeRecordToGradeDefinition(agr, gbItem, gradebook, commentText);
					  	  	
						  studentGrades.add(gradeDef);
					  }
				  }
			  }
		  }
	  }
	  
	  return studentGrades;
  }

	@Override
	public Map<Long, List<GradeDefinition>> getGradesWithoutCommentsForStudentsForItems(final String gradebookUid, final List<Long> gradableObjectIds, List<String> studentIds)
	{
		if (!authz.isUserAbleToGrade(gradebookUid))
		{
			throw new SecurityException("You do not have permission to perform this operation");
		}

		if (gradableObjectIds == null || gradableObjectIds.isEmpty())
		{
			throw new IllegalArgumentException("null or empty gradableObjectIds passed to getGradesWithoutCommentsForStudentsForItems");
		}

		Map<Long, List<GradeDefinition>> gradesMap = new HashMap<Long, List<GradeDefinition>>();
		if (studentIds == null || studentIds.isEmpty())
		{
			// We could populate the map with (gboId : new ArrayList()), but it's cheaper to allow get(gboId) to return null.
			return gradesMap;
		}

		// Get all the grades for the gradableObjectIds
		List<AssignmentGradeRecord> gradeRecords = getAllAssignmentGradeRecordsForGbItems(gradableObjectIds, studentIds);
		// AssignmentGradeRecord is not in the API. So we need to convert grade records into GradeDefinition objects.
		// GradeDefinitions are not tied to their gbos, so we need to return a map associating them back to their gbos
		List<GradeDefinition> gradeDefinitions = new ArrayList<GradeDefinition>();
		for (AssignmentGradeRecord gradeRecord : gradeRecords)
		{
			Assignment gbo = (Assignment)gradeRecord.getGradableObject();
			Long gboId = gbo.getId();
			Gradebook gradebook = gbo.getGradebook();
			if (!gradebookUid.equals(gradebook.getUid()))
			{
				// The user is authorized against gradebookUid, but we have grades for another gradebook.
				// This is an authorization issue caused by gradableObjectIds violating the method contract.
				throw new IllegalArgumentException("gradableObjectIds must belong to grades within this gradebook");
			}

			GradeDefinition gradeDef = convertGradeRecordToGradeDefinition(gradeRecord, gbo, gradebook, null);

			List<GradeDefinition> gradeList = gradesMap.get(gboId);
			if (gradeList == null)
			{
				gradeList = new ArrayList<GradeDefinition>();
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
	private GradeDefinition convertGradeRecordToGradeDefinition(AssignmentGradeRecord gradeRecord, Assignment gbo, Gradebook gradebook, String commentText)
	{
		GradeDefinition gradeDef = new GradeDefinition();
		gradeDef.setStudentUid(gradeRecord.getStudentId());
		gradeDef.setGraderUid(gradeRecord.getGraderId());
		gradeDef.setDateRecorded(gradeRecord.getDateRecorded());
		int gradeEntryType = gradebook.getGrade_type();
		gradeDef.setGradeEntryType(gradeEntryType);
		String grade = null;
		if (gradeEntryType == GradebookService.GRADE_TYPE_LETTER)
		{
			grade = gradeRecord.getLetterEarned();
		}
		else if (gradeEntryType == GradebookService.GRADE_TYPE_PERCENTAGE)
		{
			Double percentEarned = gradeRecord.getPercentEarned();
			grade = percentEarned != null ? percentEarned.toString() : null;
		}
		else
		{
			Double pointsEarned = gradeRecord.getPointsEarned();
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
  public boolean isGradeValid(String gradebookUuid, String grade) {
	  if (gradebookUuid == null) {
		  throw new IllegalArgumentException("Null gradebookUuid passed to isGradeValid");
	  }
	  Gradebook gradebook;
	  try {
		  gradebook = getGradebook(gradebookUuid);
	  } catch (GradebookNotFoundException gnfe) {
		  throw new GradebookNotFoundException("No gradebook exists with the given gradebookUid: " + 
				  gradebookUuid + "Error: " + gnfe.getMessage());
	  }
	  
	  int gradeEntryType = gradebook.getGrade_type();
	  LetterGradePercentMapping mapping = null;
	  if (gradeEntryType == GradebookService.GRADE_TYPE_LETTER) {
		  mapping = getLetterGradePercentMapping(gradebook);
	  }
	  
	  return isGradeValid(grade, gradeEntryType, mapping);
  }
  
  private boolean isGradeValid(String grade, int gradeEntryType, LetterGradePercentMapping gradeMapping) {

	  boolean gradeIsValid = false;

	  if (grade == null || "".equals(grade)) {

		  gradeIsValid = true;

	  } else {

		  if (gradeEntryType == GradebookService.GRADE_TYPE_POINTS ||
				  gradeEntryType == GradebookService.GRADE_TYPE_PERCENTAGE) {
			  try {
				  Double gradeAsDouble = Double.parseDouble(grade);
				  // grade must be greater than or equal to 0
				  if (gradeAsDouble.doubleValue() >= 0) {
					  // check that there are no more than 2 decimal places
					  String[] splitOnDecimal = grade.split("\\.");
					  if (splitOnDecimal == null || splitOnDecimal.length < 2) {
						  gradeIsValid = true;
					  } else if (splitOnDecimal.length == 2) {
						  String decimal = splitOnDecimal[1];
						  if (decimal.length() <= 2) {
							  gradeIsValid = true;
						  }
					  }
				  }
			  } catch (NumberFormatException nfe) {
				  if (log.isDebugEnabled()) log.debug("Passed grade is not a numeric value");
			  }

		  } else if (gradeEntryType == GradebookService.GRADE_TYPE_LETTER) {
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
	  }

	  return gradeIsValid;
  }

  @Override
  public List<String> identifyStudentsWithInvalidGrades(String gradebookUid, Map<String, String> studentIdToGradeMap) {
	  if (gradebookUid == null) {
		  throw new IllegalArgumentException("null gradebookUid passed to identifyStudentsWithInvalidGrades");
	  }

	  List<String> studentsWithInvalidGrade = new ArrayList<String>();

	  if (studentIdToGradeMap != null) {
		  Gradebook gradebook;

		  try {
			  gradebook = getGradebook(gradebookUid);
		  } catch (GradebookNotFoundException gnfe) {
			  throw new GradebookNotFoundException("No gradebook exists with the given gradebookUid: " + 
					  gradebookUid + "Error: " + gnfe.getMessage());
		  }

		  LetterGradePercentMapping gradeMapping = null;
		  if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
			  gradeMapping = getLetterGradePercentMapping(gradebook);
		  }

		  for (String studentId : studentIdToGradeMap.keySet()) {
			  String grade = studentIdToGradeMap.get(studentId);
			  if (!isGradeValid(grade, gradebook.getGrade_type(), gradeMapping)) {
				  studentsWithInvalidGrade.add(studentId);
			  }
		  }
	  }  
	  return studentsWithInvalidGrade;
  }

  @Override
  public void saveGradeAndCommentForStudent(String gradebookUid, Long gradableObjectId, String studentUid, String grade, String comment) {
	  if (gradebookUid == null || gradableObjectId == null || studentUid == null) {
		  throw new IllegalArgumentException("Null gradebookUid or gradableObjectId or studentUid passed to saveGradeAndCommentForStudent");
	  }

	  GradeDefinition gradeDef = new GradeDefinition();
	  gradeDef.setStudentUid(studentUid);
	  gradeDef.setGrade(grade);
	  gradeDef.setGradeComment(comment);

	  List<GradeDefinition> gradeDefList = new ArrayList<GradeDefinition>();
	  gradeDefList.add(gradeDef);

	  saveGradesAndComments(gradebookUid, gradableObjectId, gradeDefList);
  }

  @Override
  public void saveGradesAndComments(final String gradebookUid, final Long gradableObjectId, List<GradeDefinition> gradeDefList) {
	  if (gradebookUid == null || gradableObjectId == null) {
		  throw new IllegalArgumentException("Null gradebookUid or gradableObjectId passed to saveGradesAndComments");
	  }

	  if (gradeDefList != null) {
		  Gradebook gradebook;

		  try {
			  gradebook = getGradebook(gradebookUid); 
		  } catch (GradebookNotFoundException gnfe) {
			  throw new GradebookNotFoundException("No gradebook exists with the given gradebookUid: " + 
					  gradebookUid + "Error: " + gnfe.getMessage());
		  }

		  Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			  public Object doInHibernate(Session session) throws HibernateException {
				  return getAssignmentWithoutStats(gradebookUid, gradableObjectId, session);
			  }
		  });

		  if (assignment == null) {
			  throw new AssessmentNotFoundException("No gradebook item exists with gradable object id = " + gradableObjectId);
		  }

		  if (!currentUserHasGradingPerm(gradebookUid)) {
			  log.warn("User attempted to save grades and comments without authorization");
			  throw new SecurityException("Current user is not authorized to save grades or comments in gradebook " + gradebookUid);
		  }

		  // let's identify all of the students being updated first
		  Map<String, GradeDefinition> studentIdGradeDefMap = new HashMap<String, GradeDefinition>();
		  Map<String, String> studentIdToGradeMap = new HashMap<String, String>();

		  for (GradeDefinition gradeDef: gradeDefList) {
			  studentIdGradeDefMap.put(gradeDef.getStudentUid(), gradeDef);
			  studentIdToGradeMap.put(gradeDef.getStudentUid(), gradeDef.getGrade());
		  }

		  // check for invalid grades
		  List invalidStudents = identifyStudentsWithInvalidGrades(gradebookUid, studentIdToGradeMap);
		  if (invalidStudents != null && !invalidStudents.isEmpty()) {
			  throw new InvalidGradeException ("At least one grade passed to be updated is " +
			  "invalid. No grades or comments were updated.");
		  }

		  boolean userHasGradeAllPerm = currentUserHasGradeAllPerm(gradebookUid);

		  // let's retrieve all of the existing grade recs for the given students
		  // and assignments
		  List<AssignmentGradeRecord> allGradeRecs = 
			  getAllAssignmentGradeRecordsForGbItem(gradableObjectId, studentIdGradeDefMap.keySet());


		  // put in map for easier accessibility
		  Map<String, AssignmentGradeRecord> studentIdToAgrMap = new HashMap<String, AssignmentGradeRecord>();
		  if (allGradeRecs != null) {
			  for (AssignmentGradeRecord rec : allGradeRecs) {
				  studentIdToAgrMap.put(rec.getStudentId(), rec);
			  }
		  }

		  // set up the grader and grade time
		  String graderId = getAuthn().getUserUid();
		  Date now = new Date();

		  // get grade mapping, if nec, to convert grades to points
		  LetterGradePercentMapping mapping = null;
		  if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
			  mapping = getLetterGradePercentMapping(gradebook);
		  }

		  // get all of the comments, as well
		  List<Comment> allComments = getComments(assignment, studentIdGradeDefMap.keySet());
		  // put in a map for easier accessibility
		  Map<String, Comment> studentIdCommentMap = new HashMap<String, Comment>();
		  if (allComments != null) {
			  for (Comment comment : allComments) {
				  studentIdCommentMap.put(comment.getStudentId(), comment);
			  }
		  }

		  // these are the records that will need to be updated. iterate through
		  // everything and then we'll save it all at once
		  Set<AssignmentGradeRecord> agrToUpdate = new HashSet<AssignmentGradeRecord>();
		  // do not use a HashSet b/c you may have multiple Comments with null id and the same comment at this point.
		  // the Comment object defines objects as equal if they have the same id, comment text, and gb item. the
		  // only difference may be the student ids
		  List<Comment> commentsToUpdate = new ArrayList<Comment>();
		  Set<GradingEvent> eventsToAdd = new HashSet<GradingEvent>();

		  for (GradeDefinition gradeDef : gradeDefList) {

			  String studentId = gradeDef.getStudentUid();

			  // check specific grading privileges if user does not have
			  // grade all perm
			  if (!userHasGradeAllPerm) {
				  if (!isUserAbleToGradeItemForStudent(gradebookUid, gradableObjectId, studentId)) {
					  log.warn("User " + graderId + " attempted to save a grade for " + studentId + 
					  " without authorization");

					  throw new SecurityException("User " + graderId + " attempted to save a grade for " + 
							  studentId + " without authorization");
				  }
			  }

			  Double convertedGrade = convertInputGradeToPoints(gradebook.getGrade_type(), mapping, assignment.getPointsPossible(), gradeDef.getGrade());

			  // let's see if this agr needs to be updated
			  AssignmentGradeRecord gradeRec = studentIdToAgrMap.get(studentId);
			  if (gradeRec != null) {
				  if ((convertedGrade == null && gradeRec.getPointsEarned() != null) || 
						  (convertedGrade != null && gradeRec.getPointsEarned() == null) ||
						  (convertedGrade != null && gradeRec.getPointsEarned() != null && 
								  !convertedGrade.equals(gradeRec.getPointsEarned()))) {
					  
					  gradeRec.setPointsEarned(convertedGrade);
					  gradeRec.setGraderId(graderId);
					  gradeRec.setDateRecorded(now);

					  agrToUpdate.add(gradeRec);

					  // we also need to add a GradingEvent
					  // the event stores the actual input grade, not the converted one
					  GradingEvent event = new GradingEvent(assignment, graderId, studentId, gradeDef.getGrade());
					  eventsToAdd.add(event);
				  }
			  } else {
				  // if the grade is something other than null, add a new AGR
				  if (gradeDef.getGrade() != null && !gradeDef.getGrade().trim().equals("")) {
					  gradeRec =  new AssignmentGradeRecord(assignment, studentId, convertedGrade);
					  gradeRec.setPointsEarned(convertedGrade);
					  gradeRec.setGraderId(graderId);
					  gradeRec.setDateRecorded(now);

					  agrToUpdate.add(gradeRec);

					  // we also need to add a GradingEvent
					  // the event stores the actual input grade, not the converted one
					  GradingEvent event = new GradingEvent(assignment, graderId, studentId, gradeDef.getGrade());
					  eventsToAdd.add(event);
				  }
			  }

			  // let's see if the comment needs to be updated
			  Comment comment = studentIdCommentMap.get(studentId);
			  if (comment != null) {
				  boolean oldCommentIsNull = comment.getCommentText() == null || comment.getCommentText().equals("");
				  boolean newCommentIsNull = gradeDef.getGradeComment() == null || gradeDef.getGradeComment().equals("");
				  
				  if ((oldCommentIsNull && !newCommentIsNull) || 
						  (!oldCommentIsNull && newCommentIsNull) ||
						  (!oldCommentIsNull && !newCommentIsNull && 
								  !gradeDef.getGradeComment().equals(comment.getCommentText()))) {
					  // update this comment
					  comment.setCommentText(gradeDef.getGradeComment());
					  comment.setGraderId(graderId);
					  comment.setDateRecorded(now);

					  commentsToUpdate.add(comment);
				  }
			  } else {
				  // if there is a comment, add it
				  if (gradeDef.getGradeComment() != null && !gradeDef.getGradeComment().trim().equals("")) {
					  comment = new Comment(studentId, gradeDef.getGradeComment(), assignment);
					  comment.setGraderId(graderId);
					  comment.setDateRecorded(now);

					  commentsToUpdate.add(comment);
				  }
			  }
		  }

		  // now let's save them
		  try {
			for (AssignmentGradeRecord assignmentGradeRecord : agrToUpdate) {
				getHibernateTemplate().saveOrUpdate(assignmentGradeRecord);
			}
			for (Comment comment : commentsToUpdate) {
				getHibernateTemplate().saveOrUpdate(comment);
			}
			for (GradingEvent gradingEvent : eventsToAdd) {
				getHibernateTemplate().saveOrUpdate(gradingEvent);
			}
		  }	catch (HibernateOptimisticLockingFailureException holfe) {
			  if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to save scores and comments for gb Item " + gradableObjectId);
			  throw new StaleObjectModificationException(holfe);
		  } catch (StaleObjectStateException sose) {
			  if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to save scores and comments for gb Item " + gradableObjectId);
			  throw new StaleObjectModificationException(sose);
		  }
	  }
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
  private Double convertInputGradeToPoints(int gradeEntryType, LetterGradePercentMapping mapping, 
		  Double gbItemPointsPossible, String grade) throws InvalidGradeException {
	  Double convertedValue = null;

	  if (grade != null && !"".equals(grade)) {
		  if (gradeEntryType == GradebookService.GRADE_TYPE_POINTS) {
			  try {
				  Double pointValue = Double.parseDouble(grade);
				  convertedValue = pointValue;
			  } catch (NumberFormatException nfe) {
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
				      String standardizedGrade = mapping.standardizeInputGrade(grade);
					  percentage = mapping.getValue(standardizedGrade);
					  if(percentage == null)
					  {
						  throw new IllegalArgumentException("Invalid grade passed to convertInputGradeToPoints");
					  }
				  }
			  } else {
				  try {
					  percentage = Double.parseDouble(grade);
				  } catch (NumberFormatException nfe) {
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
	public int getGradeEntryType(String gradebookUid) {
		if (gradebookUid == null) {
			throw new IllegalArgumentException("null gradebookUid passed to getGradeEntryType");
		}
		
		try {
			Gradebook gradebook = getGradebook(gradebookUid);
			return gradebook.getGrade_type();
		} catch (GradebookNotFoundException gnfe) {
			throw new GradebookNotFoundException("No gradebook exists with the given gradebookUid: " + gradebookUid);
		}
	}

    @Override
	public Map getEnteredCourseGrade(final String gradebookUid)
	{
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Gradebook thisGradebook = getGradebook(gradebookUid);

				Long gradebookId = thisGradebook.getId();
				CourseGrade courseGrade = getCourseGrade(gradebookId);

				Map enrollmentMap;

				Map viewableEnrollmentsMap = authz.findMatchingEnrollmentsForViewableCourseGrade(gradebookUid, thisGradebook.getCategory_type(), null, null);
				enrollmentMap = new HashMap();

				Map enrollmentMapUid = new HashMap();
				for (Iterator iter = viewableEnrollmentsMap.keySet().iterator(); iter.hasNext(); ) 
				{
					EnrollmentRecord enr = (EnrollmentRecord)iter.next();
					enrollmentMap.put(enr.getUser().getUserUid(), enr);
					enrollmentMapUid.put(enr.getUser().getUserUid(), enr);
				}

				Query q = session.createQuery("from CourseGradeRecord as cgr where cgr.gradableObject.id=:gradableObjectId");
				q.setLong("gradableObjectId", courseGrade.getId().longValue());
				List records = filterAndPopulateCourseGradeRecordsByStudents(courseGrade, q.list(), enrollmentMap.keySet());

				Map returnMap = new HashMap();

				for(int i=0; i<records.size(); i++)
				{
					CourseGradeRecord cgr = (CourseGradeRecord) records.get(i);
					if(cgr.getEnteredGrade() != null && !cgr.getEnteredGrade().equalsIgnoreCase(""))
					{		
						EnrollmentRecord enr = (EnrollmentRecord)enrollmentMapUid.get(cgr.getStudentId());
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
		final boolean studentRequestingOwnScore = authn.getUserUid().equals(studentUid);
	  
		if (gradebookUid == null || assignmentId == null || studentUid == null) {
			throw new IllegalArgumentException("null parameter passed to getAssignmentScoreString");
		}	

	  	Double assignmentScore = (Double)getHibernateTemplate().execute(new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException {
	  			Assignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentId, session);
	  			if (assignment == null) {
	  				throw new AssessmentNotFoundException("There is no assignment with id " + assignmentId + " in gradebook " + gradebookUid);
	  			}

	  			if (!studentRequestingOwnScore && !isUserAbleToViewItemForStudent(gradebookUid, assignmentId, studentUid)) {
	  				log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to retrieve grade for student " + studentUid + " for assignment " + assignment.getName());
	  				throw new SecurityException("You do not have permission to perform this operation");
	  			}

	  			// If this is the student, then the assignment needs to have
	  			// been released.
	  			if (studentRequestingOwnScore && !assignment.isReleased()) {
	  				log.error("AUTHORIZATION FAILURE: Student " + getUserUid() + " in gradebook " + gradebookUid + " attempted to retrieve score for unreleased assignment " + assignment.getName());
	  				throw new SecurityException("You do not have permission to perform this operation");					
	  			}

	  			AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid, session);
	  			if (log.isDebugEnabled()) log.debug("gradeRecord=" + gradeRecord);
	  			if (gradeRecord == null) {
	  				return null;
	  			} else {
	  				return gradeRecord.getPointsEarned();
	  			}
	  		}
	  	});
	  	if (log.isDebugEnabled()) log.debug("returning " + assignmentScore);
	  	
	  	//TODO: when ungraded items is considered, change column to ungraded-grade 
	  	//its possible that the assignment score is null
	  	if (assignmentScore == null)
	  		return null;
	  	
	  	return Double.valueOf(assignmentScore).toString();
  	}
  
  	@Override
  	public String getAssignmentScoreString(final String gradebookUid, final String assignmentName, final String studentUid) 
  			throws GradebookNotFoundException, AssessmentNotFoundException {
	  
		if (gradebookUid == null || assignmentName == null || studentUid == null) {
			throw new IllegalArgumentException("null parameter passed to getAssignmentScoreString");
		}	

		Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, assignmentName, session);
			}
		});
		
		if (assignment == null) {
			throw new AssessmentNotFoundException("There is no assignment with name " + assignmentName + " in gradebook " + gradebookUid);
		}
		
		return getAssignmentScoreString(gradebookUid, assignment.getId(), studentUid);
  	}
  
  	@Override
	public void setAssignmentScoreString(final String gradebookUid, final Long assignmentId, final String studentUid, final String score, final String clientServiceDescription) 
	throws GradebookNotFoundException, AssessmentNotFoundException 
	{
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Assignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentId, session);
				if (assignment == null) {
					throw new AssessmentNotFoundException("There is no assignment with id " + assignmentId + " in gradebook " + gradebookUid);
				}
				if (assignment.isExternallyMaintained()) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to grade externally maintained assignment " + assignmentId + " from " + clientServiceDescription);
					throw new SecurityException("You do not have permission to perform this operation");
				}

				if (!isUserAbleToGradeItemForStudent(gradebookUid, assignment.getId(), studentUid)) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to grade student " + studentUid + " from " + clientServiceDescription + " for item " + assignmentId);
					throw new SecurityException("You do not have permission to perform this operation");
				}

				Date now = new Date();
				String graderId = getAuthn().getUserUid();
				AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid, session);
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

		if (log.isInfoEnabled()) log.info("Score updated in gradebookUid=" + gradebookUid + ", assignmentId=" + assignmentId + " by userUid=" + getUserUid() + " from client=" + clientServiceDescription + ", new score=" + score);
	}
  	
  	@Override
	public void setAssignmentScoreString(final String gradebookUid, final String assignmentName, final String studentUid, final String score, final String clientServiceDescription) 
			throws GradebookNotFoundException, AssessmentNotFoundException {
  		
  		Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, assignmentName, session);
			}
		});
		
		if (assignment == null) {
			throw new AssessmentNotFoundException("There is no assignment with name " + assignmentName + " in gradebook " + gradebookUid);
		}
		
		setAssignmentScoreString(gradebookUid, assignment.getId(), studentUid, score, clientServiceDescription);
  	}

    @Override
	public void finalizeGrades(String gradebookUid)
			throws GradebookNotFoundException {
		if (!getAuthz().isUserAbleToGradeAll(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to finalize grades");
			throw new SecurityException("You do not have permission to perform this operation");
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
	    
	    Assignment gbItem = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                return getAssignmentWithoutStats(gradebookUid, gradebookItemId, session);
            }
        });
	    
	    if (gbItem == null) {
	        throw new AssessmentNotFoundException("No gradebook item found with id " + gradebookItemId);
	    }
	    
	    Gradebook gradebook = gbItem.getGradebook();
	    
	    // double check that user has some permission to access gb items in this site
	    if (!isUserAbleToViewAssignments(gradebookUid) && !currentUserHasViewOwnGradesPerm(gradebookUid)) {
	        throw new SecurityException("User attempted to access gradebookItem: " + 
	                gradebookItemId + " in gradebook:" + gradebookUid + " without permission!");
	    }
	    
	    String lowestPossibleGrade = null;
	    
	    if (gbItem.getUngraded()) {
	        lowestPossibleGrade = null;
	    } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE || 
	            gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS) {
	        lowestPossibleGrade = "0";
	    } else if (gbItem.getGradebook().getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
	        LetterGradePercentMapping mapping = getLetterGradePercentMapping(gradebook);
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
	        log.warn("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to retrieve all categories without permission");
	        throw new SecurityException("You do not have permission to perform this operation");
	    }

	    List<CategoryDefinition> categoryDefList = new ArrayList<CategoryDefinition>();

	    List<Category> gbCategories = getCategories(getGradebook(gradebookUid).getId());

	    if (gbCategories != null) {
	        for (Category category : gbCategories) {
	            categoryDefList.add(getCategoryDefinition(category));
	        }
	    }

	    return categoryDefList;
	}

	private CategoryDefinition getCategoryDefinition(Category category) {
	    CategoryDefinition categoryDef = new CategoryDefinition();
	    if (category != null) {
	        categoryDef.setId(category.getId());
	        categoryDef.setName(category.getName());
	        categoryDef.setWeight(category.getWeight());
	        categoryDef.setDrop_lowest(category.getDrop_lowest());
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
	 * @param session
	 * @param gradebookId
	 * @param studentUids
	 * @return a map of studentUid to a list of that student's AssignmentGradeRecords for the given studentUids list
	 * in the given gradebook.  the grade records are all recs for assignments that are not removed and
	 * have a points possible > 0
	 */
	protected Map<String,List<AssignmentGradeRecord>> getGradeRecordMapForStudents(Session session, Long gradebookId, Collection<String> studentUids) {
	    Map<String,List<AssignmentGradeRecord>> filteredGradeRecs = new HashMap<String,List<AssignmentGradeRecord>>();
	    if (studentUids != null) {
	        List<AssignmentGradeRecord> allGradeRecs = new ArrayList<AssignmentGradeRecord>();

	        if (studentUids.size() >= MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST) {
	            allGradeRecs = session.createQuery(
	                    "from AssignmentGradeRecord agr where agr.gradableObject.gradebook.id=:gbid " +
	                    "and agr.gradableObject.removed=false").
	                    setParameter("gbid", gradebookId).
	                    list();
	        } else {
	            String query = "from AssignmentGradeRecord agr where agr.gradableObject.gradebook.id=:gbid and " +
	            "agr.gradableObject.removed=false and " +
	            "agr.studentId in (:studentUids)";
	            
	            allGradeRecs = session.createQuery(
	                    query).
	                    setParameter("gbid", gradebookId).
	                    setParameterList("studentUids", studentUids).
	                    list();
	        }

	        if (allGradeRecs != null) {
	            for (AssignmentGradeRecord gradeRec : allGradeRecs) {
	                if (studentUids.contains(gradeRec.getStudentId())) {
	                    String studentId = gradeRec.getStudentId();
	                    List<AssignmentGradeRecord> gradeRecList = filteredGradeRecs.get(studentId);
	                    if (gradeRecList == null) {
	                        gradeRecList = new ArrayList<AssignmentGradeRecord>();
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
	protected List<Assignment> getCountedAssignments(Session session, Long gradebookId) {
	    List<Assignment> assignList = new ArrayList<Assignment>();
	    
	    List <Assignment>results = session.createQuery(
        "from Assignment as asn where asn.gradebook.id=:gbid and asn.removed=false and " +
        "asn.notCounted=false and asn.ungraded=false").
        setParameter("gbid", gradebookId).
        list();
	    
	    if (results != null) {
	    	// making sure there's no invalid points possible for normal assignments
	    	for (Assignment a : results)
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
     * @return void
     * 
     * NOTE: When the UI changes, this needs to be made private again
     */
    public void applyDropScores(Collection<AssignmentGradeRecord> gradeRecords) {
        if(gradeRecords == null || gradeRecords.size() < 1) {
            return;
        }
        long start = System.currentTimeMillis();
        
        List<String> studentIds = new ArrayList<String>();
        List<Category> categories = new ArrayList<Category>();
        Map<String, List<AssignmentGradeRecord>> gradeRecordMap = new HashMap<String, List<AssignmentGradeRecord>>();
        for(AssignmentGradeRecord gradeRecord : gradeRecords) {
            
            if(gradeRecord == null 
                    || gradeRecord.getPointsEarned() == null) { // don't consider grades that have null pointsEarned (this occurs when a previously entered score for an assignment is removed; record stays in database) 
                continue;
            }
            
            // reset
            gradeRecord.setDroppedFromGrade(false);
            
            Assignment assignment = gradeRecord.getAssignment();
            if(assignment.getUngraded()  // GradebookService.GRADE_TYPE_LETTER
                    || assignment.isNotCounted() // don't consider grades that are not counted toward course grade
                    || assignment.getItemType().equals(Assignment.item_type_adjustment)
                    || assignment.isRemoved()) {
                continue;
            }
            // get all the students represented
            String studentId = gradeRecord.getStudentId();
            if(!studentIds.contains(studentId)) {
                studentIds.add(studentId);
            }
            // get all the categories represented
            Category cat = gradeRecord.getAssignment().getCategory();
            if(cat != null) {
                if(!categories.contains(cat)) {
                    categories.add(cat);
                }
                List<AssignmentGradeRecord> gradeRecordsByCatAndStudent = gradeRecordMap.get(studentId + cat.getId());
                if(gradeRecordsByCatAndStudent == null) {
                    gradeRecordsByCatAndStudent = new ArrayList<AssignmentGradeRecord>();
                    gradeRecordsByCatAndStudent.add(gradeRecord);
                    gradeRecordMap.put(studentId + cat.getId(), gradeRecordsByCatAndStudent);
                } else {
                    gradeRecordsByCatAndStudent.add(gradeRecord);
                }
            }            
        }
        
        if(categories == null || categories.size() < 1) {
            return;
        }
        for(Category cat : categories) {
            Integer dropHighest = cat.getDropHighest();
            Integer dropLowest = cat.getDrop_lowest();
            Integer keepHighest = cat.getKeepHighest();
            Long catId = cat.getId();
            
            if((dropHighest != null && dropHighest > 0) || (dropLowest != null && dropLowest > 0) || (keepHighest != null && keepHighest > 0)) {
                
                for(String studentId : studentIds) {
                    // get the student's gradeRecords for this category
                    List<AssignmentGradeRecord> gradesByCategory = new ArrayList<AssignmentGradeRecord>();
                    List<AssignmentGradeRecord> gradeRecordsByCatAndStudent = gradeRecordMap.get(studentId + cat.getId());
                    if(gradeRecordsByCatAndStudent != null) {
                        gradesByCategory.addAll(gradeRecordsByCatAndStudent);
                    
                        int numGrades = gradesByCategory.size();
                        
                        if(dropHighest > 0 && numGrades > dropHighest + dropLowest) {
                            for(int i=0; i<dropHighest; i++) {
                                AssignmentGradeRecord highest = Collections.max(gradesByCategory, AssignmentGradeRecord.numericComparator);
                                highest.setDroppedFromGrade(true);
                                gradesByCategory.remove(highest);
                                if(log.isDebugEnabled()) log.debug("dropHighest applied to " + highest);
                            }
                        }
                        
                        if(keepHighest > 0 && numGrades > (gradesByCategory.size() - keepHighest)) {
                            dropLowest = gradesByCategory.size() - keepHighest;
                        }
                        
                        if(dropLowest > 0 &&  numGrades > dropLowest + dropHighest) {
                            for(int i=0; i<dropLowest; i++) {
                                AssignmentGradeRecord lowest = Collections.min(gradesByCategory, AssignmentGradeRecord.numericComparator);
                                lowest.setDroppedFromGrade(true);
                                gradesByCategory.remove(lowest);
                                if(log.isDebugEnabled()) log.debug("dropLowest applied to " + lowest);
                            }
                        }
                    }
                }
                if(log.isDebugEnabled()) log.debug("processed " + studentIds.size() + "students in category " + cat.getId());
            }
        }
        
        if(log.isDebugEnabled()) log.debug("GradebookManager.applyDropScores took " + (System.currentTimeMillis() - start) + " millis to execute");
    }
    
	@Override
    public PointsPossibleValidation isPointsPossibleValid(String gradebookUid, org.sakaiproject.service.gradebook.shared.Assignment gradebookItem, 
            Double pointsPossible) {
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
        
        if (pointsPossible.doubleValue() <= 0) {
            return PointsPossibleValidation.INVALID_NUMERIC_VALUE;
        }
        // ensure there are no more than 2 decimal places
        BigDecimal bd = new BigDecimal(pointsPossible.doubleValue());
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
	private Double convertStringToDouble(String doubleAsString) {
	    Double scoreAsDouble = null;
	    if (doubleAsString != null && !"".equals(doubleAsString)) {
	        try {
	        	NumberFormat numberFormat = NumberFormat.getInstance(new ResourceLoader().getLocale());
				Number numericScore = numberFormat.parse(doubleAsString.trim());
				scoreAsDouble = numericScore.doubleValue();
			} catch (ParseException e) {
				log.error(e);
			}
	    }

	    return scoreAsDouble;
	}
	
	/**
	 * Get a list of assignments in the gradebook attached to the given category.
	 * Note that each assignment only knows the category by name.
	 * 
	 * <p>Note also that this is different to {@link BaseHibernateManager#getAssignmentsForCategory(Long)} because this method returns the shared Assignment object.
	 * 
	 * @param gradebookUid
	 * @param categoryName
	 * @return
	 */
	private List<org.sakaiproject.service.gradebook.shared.Assignment> getAssignments(String gradebookUid, String categoryName) {
		
		List<org.sakaiproject.service.gradebook.shared.Assignment> allAssignments = getAssignments(gradebookUid);
		List<org.sakaiproject.service.gradebook.shared.Assignment> matchingAssignments = new ArrayList<org.sakaiproject.service.gradebook.shared.Assignment>();
		
		for(org.sakaiproject.service.gradebook.shared.Assignment assignment: allAssignments) {
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
	private void postUpdateGradeEvent(String gradebookUid, String assignmentName, String studentUid, Double pointsEarned) {
	    if (eventTrackingService != null) {
            eventTrackingService.postEvent("gradebook.updateItemScore","/gradebook/"+gradebookUid+"/"+assignmentName+"/"+studentUid+"/"+pointsEarned+"/student");
        }
    }
	
	/**
	 * Retrieves the calculated average course grade.
	 */
	@Override
	public String getAverageCourseGrade(String gradebookUid) {
	    if (gradebookUid == null) {
	        throw new IllegalArgumentException("Null gradebookUid passed to getAverageCourseGrade");
	    }
	    // Check user has permission to invoke method.
	    if (!currentUserHasGradeAllPerm(gradebookUid)) {
	    	StringBuilder sb = new StringBuilder()
	    	.append("User ")
	    	.append(authn.getUserUid())
	    	.append(" attempted to access the average course grade without permission in gb ")
	    	.append(gradebookUid)
	    	.append(" using gradebookService.getAverageCourseGrade");
	        throw new SecurityException(sb.toString());
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
			        courseGradeLetter = gradebook.getSelectedGradeMapping().getGrade(courseGrade.getMean());
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
			log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to change the order of assignment " + assignmentId);
			throw new SecurityException("You do not have permission to perform this operation");
		}
		
		if(order == null) {
			throw new IllegalArgumentException("Order cannot be null");
		}
	
		final Long gradebookId = getGradebook(gradebookUid).getId();
		
		//get all assignments for this gradebook
		List<Assignment> assignments = getAssignments(gradebookId, SortType.SORT_BY_SORTING, true);
		
		//adjust order to be within bounds
		if(order < 0) {
			order = 0;
		} else if (order > assignments.size()) {
			order = assignments.size();
		}
		
		//find the assignment
		Assignment target = null;
		for(Assignment a: assignments){
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
		List<Assignment> assignmentsToUpdate = new ArrayList<>();
		
		int i = 0;
		for(Assignment a: assignments){
			
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
		for(final Assignment assignmentToUpdate: assignmentsToUpdate){
			getHibernateTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					updateAssignment(assignmentToUpdate, session);
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
        
        HibernateCallback hc = new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.createQuery("from GradingEvent as ge where ge.studentId=:studentId and ge.gradableObject.id=:assignmentId");
                q.setParameter("studentId", studentId);
                q.setParameter("assignmentId", assignmentId);
                return q.list();
            }
        };

        rval = (List)getHibernateTemplate().execute(hc);
        return rval;
    }
	
	@Override
	public Double calculateCategoryScore(Object gradebook, String studentUuid, CategoryDefinition category, final List<org.sakaiproject.service.gradebook.shared.Assignment> viewableAssignments, Map<Long,String> gradeMap) {
		
		Gradebook gb = (Gradebook) gradebook;
				
		//collect the data and turn it into a list of AssignmentGradeRecords
		//this is the info that is compatible with both applyDropScores and the calculateCategoryScore method
		List<AssignmentGradeRecord> gradeRecords = new ArrayList<>();
		for(org.sakaiproject.service.gradebook.shared.Assignment assignment: viewableAssignments) {
			
			Long assignmentId = assignment.getId();
			Double grade = NumberUtils.createDouble(gradeMap.get(assignmentId));
			
			//recreate the category (required fields only)
			Category c = new Category();
			c.setId(category.getId());
			c.setDropHighest(category.getDropHighest());
			c.setDrop_lowest(category.getDrop_lowest());
			c.setKeepHighest(category.getKeepHighest());
			
			//recreate the assignment (required fields only)
			Assignment a = new Assignment();
			a.setPointsPossible(assignment.getPoints());
			a.setUngraded(assignment.getUngraded());
			a.setCounted(assignment.isCounted());
			a.setExtraCredit(assignment.isExtraCredit());
			a.setReleased(assignment.isReleased());
			a.setRemoved(false); //shared.Assignment doesn't include removed so this will always be false
			a.setGradebook(gb);
			a.setCategory(c);
			
			//create the AGR
			AssignmentGradeRecord gradeRecord = new AssignmentGradeRecord(a, studentUuid, grade);
			
			gradeRecords.add(gradeRecord);
		}
		
		return calculateCategoryScore(studentUuid, category.getId(), gradeRecords);
	}
	
	@Override
	public Double calculateCategoryScore(Long gradebookId, String studentUuid, Long categoryId) {
			
		//get all grade records for the student
		@SuppressWarnings({ "unchecked", "rawtypes"})
		Map<String, List<AssignmentGradeRecord>> gradeRecMap = (Map<String, List<AssignmentGradeRecord>>)getHibernateTemplate().execute(new HibernateCallback() {
            @Override
			public Object doInHibernate(Session session) throws HibernateException {
                return getGradeRecordMapForStudents(session, gradebookId, Collections.singletonList(studentUuid));
            }
		});
			
		//apply the settings
		List<AssignmentGradeRecord> gradeRecords = gradeRecMap.get(studentUuid);
		
		return calculateCategoryScore(studentUuid, categoryId, gradeRecords);
	}
	
	/**
	 * Does the heavy lifting for the category calculations.
	 * Requires the List of AssignmentGradeRecord so that we can applyDropScores.
	 * @param studentUuid
	 * @param categoryId
	 * @param gradeRecords
	 * @return
	 */
	private Double calculateCategoryScore(String studentUuid, Long categoryId, List<AssignmentGradeRecord> gradeRecords) {
				
		//validate
		if(gradeRecords == null) {
			log.debug("No grade records for student: " + studentUuid + ". Nothing to do.");
			return null;
		}
		
		//setup
		int numScored = 0;
		int numOfAssignments = 0;
		BigDecimal totalEarned = new BigDecimal("0");
		BigDecimal totalPossible = new BigDecimal("0");
				
		//apply any drop/keep settings for this category
		this.applyDropScores(gradeRecords);
				
		//iterate every grade record, check it's for the category we want, otherwise discard
		for(AssignmentGradeRecord gradeRecord: gradeRecords) {
			
			Assignment assignment = gradeRecord.getAssignment();
						
			//check category ids match, otherwise skip
			if(assignment.getCategory() != null && categoryId != null && categoryId.longValue() != assignment.getCategory().getId().longValue()){
				continue;
			}
						
			//only update the variables for the calculation if:
			// 1. the assignment has points to be assigned
			// 2. there is a grade for the student
			// 3. the assignment is included in course grade calculations
			// 4. the assignment is  released to the student (safety check against condition 3)
			// 5. the grade is not dropped from the calc
			if(assignment.getPointsPossible() != null && gradeRecord.getPointsEarned() != null && assignment.isCounted() && assignment.isReleased() && !gradeRecord.getDroppedFromGrade()) {
				totalPossible = totalPossible.add(new BigDecimal(assignment.getPointsPossible().toString()));
				numOfAssignments++;
				numScored++;
				
				//sanitise grade, null values to "0";
				String grade = (gradeRecord.getPointsEarned() != null) ? String.valueOf(gradeRecord.getPointsEarned()) : "0";
				
				//update total points earned
				totalEarned = totalEarned.add(new BigDecimal(grade));
			}
			
		}
		
		if (numScored == 0 || numOfAssignments == 0 || totalPossible.doubleValue() == 0) {
    		return null;
    	}
	
    	BigDecimal mean = totalEarned.divide(new BigDecimal(numScored), GradebookService.MATH_CONTEXT).divide((totalPossible.divide(new BigDecimal(numOfAssignments), GradebookService.MATH_CONTEXT)), GradebookService.MATH_CONTEXT).multiply(new BigDecimal("100"));    	
    	return Double.valueOf(mean.doubleValue());
	}
	
	@Override
	public org.sakaiproject.service.gradebook.shared.CourseGrade getCourseGradeForStudent(String gradebookUid, String userUuid) {
		return this.getCourseGradeForStudents(gradebookUid, Collections.singletonList(userUuid)).get(0);
	}
	
	@Override
	public List<org.sakaiproject.service.gradebook.shared.CourseGrade> getCourseGradeForStudents(String gradebookUid, List<String> userUuids) {
		
		List<org.sakaiproject.service.gradebook.shared.CourseGrade> rval = new ArrayList<>();

		try {
			Gradebook gradebook = getGradebook(gradebookUid);
	
			//if not released, and not instructor or TA, don't do any work
			//note that this will return a course grade for Instructor and TA even if not released, see SAK-30119
			if(!gradebook.isCourseGradeDisplayed() && (!currentUserHasEditPerm(gradebookUid) || !currentUserHasGradingPerm(gradebookUid))){
				return rval;
			}
			
			List<Assignment> assignments = getAssignmentsCounted(gradebook.getId());
			GradeMapping gradeMap = gradebook.getSelectedGradeMapping();
			
			//this takes care of drop/keep scores
			List<CourseGradeRecord> gradeRecords = getPointsEarnedCourseGradeRecords(getCourseGrade(gradebook.getId()), userUuids);
			
			gradeRecords.forEach(gr -> {
				
				org.sakaiproject.service.gradebook.shared.CourseGrade cg = new org.sakaiproject.service.gradebook.shared.CourseGrade();

				//ID of the course grade item
				cg.setId(gr.getCourseGrade().getId());
				
				//set entered grade
				cg.setEnteredGrade(gr.getEnteredGrade());
				
				if(!assignments.isEmpty()) {
					
					//calculated grade
					//may be null if no grade entries to calculate
					Double calculatedGrade = gr.getAutoCalculatedGrade();
					if(calculatedGrade != null) {
						cg.setCalculatedGrade(calculatedGrade.toString());
					}

					//mapped grade
					String mappedGrade = gradeMap.getGrade(calculatedGrade);
					cg.setMappedGrade(mappedGrade);
					
					//points
					cg.setPointsEarned(gr.getPointsEarned()); //synonymous with gradeRecord.getCalculatedPointsEarned()
					cg.setTotalPointsPossible(gr.getTotalPointsPossible());
					
				}
				rval.add(cg);
			});
		}
		catch(Exception e) {
			log.error("Error in getCourseGradeForStudents", e);
		}
		return rval;
	}
	
	@Override
	public List<CourseSection> getViewableSections(String gradebookUid) {
		return this.getAuthz().getViewableSections(gradebookUid);
	}
	
	@Override
	public void updateGradebookSettings(String gradebookUid, GradebookInformation gbInfo) {
		if (gradebookUid == null ) {
			throw new IllegalArgumentException("null gradebookUid " + gradebookUid) ;
		}
	    
		//must be instructor type person
		if (!currentUserHasEditPerm(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to edit gb information");
			throw new SecurityException("You do not have permission to edit gradebook information in site " + gradebookUid);
		}
	        
		final Gradebook gradebook = getGradebook(gradebookUid);
		if(gradebook==null) {
			throw new IllegalArgumentException("There is no gradebook associated with this id: " + gradebookUid);
		}
		
		//iterate all available grademappings for this gradebook and set the one that we have the ID for
		Set<GradeMapping> gradeMappings = gradebook.getGradeMappings();
		for(GradeMapping gradeMapping: gradeMappings) {
			if(StringUtils.equals(Long.toString(gradeMapping.getId()), gbInfo.getSelectedGradeMappingId())) {
				gradebook.setSelectedGradeMapping(gradeMapping);
					
				//update the map values
				updateGradeMapping(gradeMapping.getId(), gbInfo.getSelectedGradingScaleBottomPercents());
			}
		}
				
		//set grade type
		gradebook.setGrade_type(gbInfo.getGradeType());
		
		//set category type
		gradebook.setCategory_type(gbInfo.getCategoryType());
		
		//set display release items to students
		gradebook.setAssignmentsDisplayed(gbInfo.isDisplayReleasedGradeItemsToStudents());
		
		//set course grade display settings
		gradebook.setCourseGradeDisplayed(gbInfo.isCourseGradeDisplayed());
		gradebook.setCourseLetterGradeDisplayed(gbInfo.isCourseLetterGradeDisplayed());
		gradebook.setCoursePointsDisplayed(gbInfo.isCoursePointsDisplayed());
		gradebook.setCourseAverageDisplayed(gbInfo.isCourseAverageDisplayed());
		
		List<CategoryDefinition> newCategoryDefinitions = gbInfo.getCategories();
		
		//if we have categories and they are weighted, check the weightings sum up to 100% (or 1 since it's a fraction)
		if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY) {
			double totalWeight = 0;
			for(CategoryDefinition newDef: newCategoryDefinitions) {
				
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
		List<Category> currentCategories = this.getCategories(gradebook.getId());
		Map<Long,Category> currentCategoryMap = new HashMap<>();
		for(Category c: currentCategories) {
			currentCategoryMap.put(c.getId(), c);
		}
		
		//compare current list with given list, add/update/remove as required
		//Rules:
		//If category does not have an ID it is new
		//If category has an ID it is to be updated. Update and remove from currentCategoryMap.
		//Any categories remaining in currentCategoryMap are to be removed.
		//Sort by category order as we resequence the order values to avoid gaps
		Collections.sort(newCategoryDefinitions, CategoryDefinition.orderComparator);
		int categoryIndex = 0;
		for(CategoryDefinition newDef: newCategoryDefinitions) {
			
			//preprocessing and validation
			//Rule 1: If category has no name, it is to be removed/skipped
			//Note that we no longer set weights to 0 even if unweighted category type selected. The weights are not considered if its not a weighted category type
			//so this allows us to switch back and forth between types without losing information
			
			if(StringUtils.isBlank(newDef.getName())) {
				continue;
			}
			
			//new
			if(newDef.getId() == null) {
				this.createCategory(gradebook.getId(), newDef.getName(), newDef.getWeight(), newDef.getDrop_lowest(), newDef.getDropHighest(), newDef.getKeepHighest(), newDef.isExtraCredit(), Integer.valueOf(categoryIndex));
				categoryIndex++;
				continue;
			} 
			
			//update
			else {
				Category existing = currentCategoryMap.get(newDef.getId());
				existing.setName(newDef.getName());
				existing.setWeight(newDef.getWeight());
				existing.setDrop_lowest(newDef.getDrop_lowest());
				existing.setDropHighest(newDef.getDropHighest());
				existing.setKeepHighest(newDef.getKeepHighest());
				existing.setExtraCredit(newDef.isExtraCredit());
				existing.setCategoryOrder(categoryIndex);
				this.updateCategory(existing);
				
				//remove from currentCategoryMap so we know not to delete it
				currentCategoryMap.remove(newDef.getId());

				categoryIndex++;
				continue;
			}
			
		}
		
		//handle deletes
		//anything left in currentCategoryMap was not included in the new list, delete them
		for(Entry<Long, Category> cat: currentCategoryMap.entrySet()) {
			this.removeCategory(cat.getKey());
		}
		
		//no need to set assignments, gbInfo doesn't update them

		//persist
		this.updateGradebook(gradebook);
		
	}

	
	
	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}
    
	public Authz getAuthz() {
		return authz;
	}
	
	public void setAuthz(Authz authz) {
		this.authz = authz;
	}
    
	public GradebookPermissionService getGradebookPermissionService() {
		return gradebookPermissionService;
	}
    
	public void setGradebookPermissionService(GradebookPermissionService gradebookPermissionService) {
		this.gradebookPermissionService = gradebookPermissionService;
	}

	@Override
	public Set getGradebookGradeMappings(final Long gradebookId) {
		return (Set)getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Set doInHibernate(Session session) throws HibernateException {
				Gradebook gradebook = (Gradebook)session.load(Gradebook.class, gradebookId);
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
			log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to update course grade for student: " + studentUuid);
			throw new SecurityException("You do not have permission to update course grades in " + gradebookUid);
		}
	        
		final Gradebook gradebook = getGradebook(gradebookUid);
		if(gradebook==null) {
			throw new IllegalArgumentException("There is no gradebook associated with this id: " + gradebookUid);
		}
		
		//get course grade for the student
		CourseGradeRecord courseGradeRecord = (CourseGradeRecord)getHibernateTemplate().execute(new HibernateCallback() {
            @Override
			public Object doInHibernate(Session session) throws HibernateException {
                return getCourseGradeRecord(gradebook, studentUuid, session);
            }
		});
				
		//if user doesn't have an entered course grade, we need to find the course grade and create a record
		if(courseGradeRecord == null) {
			
			CourseGrade courseGrade = this.getCourseGrade(gradebook.getId());
			
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
		GradingEvent gradingEvent = new GradingEvent();
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
	private List<GradeMappingDefinition> getGradebookGradeMappings(Set<GradeMapping> gradeMappings) {
		List<GradeMappingDefinition> rval = new ArrayList<>();
		
		for(GradeMapping mapping: gradeMappings) {
			rval.add(new GradeMappingDefinition(mapping.getId(), mapping.getName(), mapping.getGradeMap(), mapping.getDefaultBottomPercents()));
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
			log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to change the order of assignment " + assignmentId);
			throw new SecurityException("You do not have permission to perform this operation");
		}

		if(order == null) {
			throw new IllegalArgumentException("Categorized Order cannot be null");
		}

		final Long gradebookId = getGradebook(gradebookUid).getId();

		//get all assignments for this gradebook
		List<Assignment> assignments = getAssignments(gradebookId, SortType.SORT_BY_SORTING, true);
		List<Assignment> assignmentsInNewCategory = new ArrayList<Assignment>();
		for (Assignment assignment : assignments) {
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
		Assignment target = null;
		for(Assignment a: assignmentsInNewCategory){
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
		List<Assignment> assignmentsToUpdate = new ArrayList<>();

		int i = 0;
		for(Assignment a: assignmentsInNewCategory){

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
		for(final Assignment assignmentToUpdate: assignmentsToUpdate){
			getHibernateTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					updateAssignment(assignmentToUpdate, session);
					return null;
				}
			});
		}

	}
}
