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

package org.sakaiproject.tool.gradebook.business.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.TransientObjectException;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.component.gradebook.GradebookServiceHibernateImpl;
import org.sakaiproject.hibernate.HibernateCriterionUtils;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingSpreadsheetNameException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.MultipleAssignmentSavingException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.GradingEvents;
import org.sakaiproject.tool.gradebook.LetterGradePercentMapping;
import org.sakaiproject.tool.gradebook.Spreadsheet;
import org.sakaiproject.tool.gradebook.business.GbSynchronizer;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException;

import lombok.extern.slf4j.Slf4j;

/**
 * synchronize from external application
 */
@Slf4j
public abstract class GradebookManagerHibernateImpl extends GradebookServiceHibernateImpl
        implements GradebookManager {

    /** synchronize from external application*/
    GbSynchronizer synchronizer = null;

    @Override
    public void removeAssignment(final Long assignmentId) throws StaleObjectModificationException {
        final HibernateCallback hc = new HibernateCallback() {
            @Override
            public Object doInHibernate(final Session session) throws HibernateException {
                final GradebookAssignment asn = (GradebookAssignment)session.load(GradebookAssignment.class, assignmentId);
                final Gradebook gradebook = asn.getGradebook();
                asn.setRemoved(true);
                session.update(asn);
                /** synchronize from external application*/
                if ( (GradebookManagerHibernateImpl.this.synchronizer != null) && (!GradebookManagerHibernateImpl.this.synchronizer.isProjectSite()))
                {
                	GradebookManagerHibernateImpl.this.synchronizer.deleteLegacyAssignment(asn.getName());
                }
                if(log.isInfoEnabled()) {
					log.info("GradebookAssignment " + asn.getName() + " has been removed from " + gradebook);
                }
                return null;
            }
        };
        getHibernateTemplate().execute(hc);
    }

    @Override
    public List getAssignmentGradeRecords(final GradebookAssignment assignment, final Collection studentUids) {
        final HibernateCallback hc = new HibernateCallback() {
            @Override
            public Object doInHibernate(final Session session) throws HibernateException {
                if(studentUids == null || studentUids.size() == 0) {
                    if(log.isInfoEnabled()) {
						log.info("Returning no grade records for an empty collection of student UIDs");
					}
                    return new ArrayList();
                } else if (assignment.isRemoved()) {
                    return new ArrayList();
                }

                final Query q = session.createQuery("from AssignmentGradeRecord as agr where agr.gradableObject.id=:gradableObjectId order by agr.pointsEarned");
                q.setLong("gradableObjectId", assignment.getId().longValue());
                final List records = filterGradeRecordsByStudents(q.list(), studentUids);
                return records;
            }
        };
        return (List)getHibernateTemplate().execute(hc);
    }
    
    @Override
    public CourseGradeRecord getPointsEarnedCourseGradeRecords(final CourseGrade courseGrade, final String studentUid) {
    	final Set<String> oneStudent = new HashSet<String>(1);
    	oneStudent.add(studentUid);

    	final List<CourseGradeRecord> list = getPointsEarnedCourseGradeRecords(courseGrade, oneStudent);
    	for (final CourseGradeRecord cgr : list) {
    		return cgr;
    	}
		return null;
    }

    @Override
    public List getPointsEarnedCourseGradeRecordsWithStats(final CourseGrade courseGrade, final Collection studentUids) {
    	// Get good class-wide statistics by including all students, whether
    	// the caller is specifically interested in their grade records or not.
    	final Long gradebookId = courseGrade.getGradebook().getId();
    	final Set allStudentUids = getAllStudentUids(getGradebookUid(gradebookId));
    	List courseGradeRecords = getPointsEarnedCourseGradeRecords(courseGrade, allStudentUids);
    	courseGrade.calculateStatistics(courseGradeRecords, allStudentUids.size());

    	// Filter out the grade records which weren't specified.
    	courseGradeRecords = filterGradeRecordsByStudents(courseGradeRecords, studentUids);

    	return courseGradeRecords;
    }

    @Override
    public void addToGradeRecordMap(final Map gradeRecordMap, final List gradeRecords) {
		for (final Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			final AbstractGradeRecord gradeRecord = (AbstractGradeRecord)iter.next();
			if (gradeRecord instanceof AssignmentGradeRecord) {
				((AssignmentGradeRecord)gradeRecord).setUserAbleToView(true);
			}
			final String studentUid = gradeRecord.getStudentId();
			Map studentMap = (Map)gradeRecordMap.get(studentUid);
			if (studentMap == null) {
				studentMap = new HashMap();
				gradeRecordMap.put(studentUid, studentMap);
			}
			studentMap.put(gradeRecord.getGradableObject().getId(), gradeRecord);
		}
    }
    
    @Override
    public void addToGradeRecordMap(final Map gradeRecordMap, final List gradeRecords, final Map studentIdItemIdFunctionMap) {
		for (final Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			final AbstractGradeRecord gradeRecord = (AbstractGradeRecord)iter.next();
			final String studentUid = gradeRecord.getStudentId();
			Map studentMap = (Map)gradeRecordMap.get(studentUid);
			if (studentMap == null) {
				studentMap = new HashMap();
				gradeRecordMap.put(studentUid, studentMap);
			}
			final Long itemId = gradeRecord.getGradableObject().getId();
			// check to see if this item is included in the items that the current user is able to view/grade
			final Map itemIdFunctionMap = (Map)studentIdItemIdFunctionMap.get(studentUid);
			if (gradeRecord instanceof AssignmentGradeRecord) {
				
				if (itemIdFunctionMap != null && itemIdFunctionMap.get(itemId) != null) {
					((AssignmentGradeRecord)gradeRecord).setUserAbleToView(true);
				} else {
					((AssignmentGradeRecord)gradeRecord).setUserAbleToView(false);
					((AssignmentGradeRecord)gradeRecord).setLetterEarned(null);
					((AssignmentGradeRecord)gradeRecord).setPointsEarned(null);
					((AssignmentGradeRecord)gradeRecord).setPercentEarned(null);
				}
				studentMap.put(itemId, gradeRecord);
			} else {
				studentMap.put(itemId, gradeRecord);
			}
		}
    }
    
    @Override
    public void addToCategoryResultMap(final Map categoryResultMap, final List categories, final Map gradeRecordMap, final Map enrollmentMap) {
    	if (gradeRecordMap == null || gradeRecordMap.isEmpty()) {
    		return;
		}
    	
    	for (final Iterator stuIter = enrollmentMap.keySet().iterator(); stuIter.hasNext(); ){
    		final String studentUid = (String) stuIter.next();
    		final Map studentMap = (Map) gradeRecordMap.get(studentUid);
    		
    		if (studentMap != null) {
	    		for (final Iterator iter = categories.iterator(); iter.hasNext(); ){
	    			final Object obj = iter.next();
	    			if(!(obj instanceof Category)){
	    				continue;
	    			}
	    			final Category category = (Category) obj;
		    		
		    		final List categoryAssignments = category.getAssignmentList();
		    		if (categoryAssignments == null){
		    			continue;
		    		}
		    		
		    		final List gradeRecords = new ArrayList();
									
		    		for (final Iterator assignmentsIter = categoryAssignments.iterator(); assignmentsIter.hasNext(); ){
		    			final GradebookAssignment assignment = (GradebookAssignment) assignmentsIter.next();
		    			final AbstractGradeRecord gradeRecord = (AbstractGradeRecord) studentMap.get(assignment.getId());
		    			gradeRecords.add(gradeRecord);
				
		    		}
		    		applyDropScores(gradeRecords);
		    		category.calculateStatisticsPerStudent(gradeRecords, studentUid);
	
		    		Map studentCategoryMap = (Map) categoryResultMap.get(studentUid);
			    	if (studentCategoryMap == null) {
			    		studentCategoryMap = new HashMap();
			    		categoryResultMap.put(studentUid, studentCategoryMap);
			    	}
			    	final Map stats = new HashMap();
			    	stats.put("studentAverageScore", category.getAverageScore());
			    	stats.put("studentAverageTotalPoints", category.getAverageTotalPoints());
			    	stats.put("studentMean", category.getMean());
			    	stats.put("studentTotalPointsEarned", category.getTotalPointsEarned());
			    	stats.put("studentTotalPointsPossible", category.getTotalPointsPossible());
			    	
			    	stats.put("category", category);
	
			    	studentCategoryMap.put(category.getId(), stats);
		    	}
	    	}
    	}
    	
    }

    @Override
    public AssignmentGradeRecord getAssignmentGradeRecordById(final Long id) {
    	final AssignmentGradeRecord agr = getHibernateTemplate().load(AssignmentGradeRecord.class, id);
    	AssignmentGradeRecord agrCalculated = new AssignmentGradeRecord();
    	if (agr != null){
    		final List assignRecordsFromDB = new ArrayList();
    		assignRecordsFromDB.add(agr);
    		List agrs = this.convertPointsToLetterGrade(agr.getAssignment(), agr.getAssignment().getGradebook(), assignRecordsFromDB);
    		agrs = this.convertPointsToPercentage(agr.getAssignment(), agr.getAssignment().getGradebook(), agrs);
    		if (agrs.get(0) != null){
    			agrCalculated = (AssignmentGradeRecord)agrs.get(0);
    		}
    	}
    	return agrCalculated;
    }
    
    @Override
    public Comment getCommentById(final Long id) {
    	return getHibernateTemplate().load(Comment.class, id);
    }
    
    @Override
    public AssignmentGradeRecord getAssignmentGradeRecordForAssignmentForStudent(final GradebookAssignment assignment, final String studentUid) {
	    final HibernateCallback hc = new HibernateCallback() {
	        @Override
            public Object doInHibernate(final Session session) throws HibernateException {
	            if(studentUid == null) {
	                if(log.isInfoEnabled()) {
						log.info("Returning no grade records for a null student UID");
					}
	                return new ArrayList();
	            } else if (assignment.isRemoved()) {
	                return new ArrayList();
	            }

	            final Query q = session.createQuery("from AssignmentGradeRecord as agr where agr.gradableObject.id=:gradableObjectId " +
	            		"and agr.studentId=:student");
	            q.setLong("gradableObjectId", assignment.getId().longValue());
	            q.setString("student", studentUid);
	            return q.list();
	        }
	    };
	    final List results = (List) getHibernateTemplate().execute(hc);
	    if (results.size() > 0){
	    	return (AssignmentGradeRecord)results.get(0);
	    } else {
	    	return new AssignmentGradeRecord();
	    }
	}
    
    @Override
    public List getAllAssignmentGradeRecordsConverted(final Long gradebookId, final Collection studentUids)
    {
    	final List allAssignRecordsFromDB = getAllAssignmentGradeRecords(gradebookId, studentUids);
    	final Gradebook gradebook = getGradebook(gradebookId);
    	if(gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS) {
    		return allAssignRecordsFromDB;
		} else if(gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE)
    	{
    		return convertPointsToPercentage(gradebook, allAssignRecordsFromDB);
    	}
    	else if(gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER)
    	{
    		return convertPointsToLetterGrade(gradebook, allAssignRecordsFromDB);
    	}
    	return null;
    }

    /**
     * @return Returns set of student UIDs who were given scores higher than the assignment's value.
     */
    @Override
    public Set updateAssignmentGradeRecords(final GradebookAssignment assignment, final Collection gradeRecordsFromCall)
            throws StaleObjectModificationException {
        // If no grade records are sent, don't bother doing anything with the db
        if(gradeRecordsFromCall.size() == 0) {
            log.debug("updateAssignmentGradeRecords called for zero grade records");
            return new HashSet();
        }

        if (log.isDebugEnabled()) {
			log.debug("BEGIN: Update " + gradeRecordsFromCall.size() + " scores for gradebook=" + assignment.getGradebook().getUid() + ", assignment=" + assignment.getName());
		}

        final HibernateCallback hc = new HibernateCallback() {
            @Override
            public Object doInHibernate(final Session session) throws HibernateException {
                final Date now = new Date();
                String graderId = GradebookManagerHibernateImpl.this.authn.getUserUid();

                final Set studentsWithUpdatedAssignmentGradeRecords = new HashSet();
                final Set studentsWithExcessiveScores = new HashSet();

                /** synchronize from external application*/
                if(GradebookManagerHibernateImpl.this.synchronizer != null)
                {
                	final boolean isUpdateAll = Boolean.TRUE.equals(ThreadLocalManager.get("iquiz_update_all"));
                	final boolean isIquizCall = Boolean.TRUE.equals(ThreadLocalManager.get("iquiz_call"));
                	final boolean isStudentView = Boolean.TRUE.equals(ThreadLocalManager.get("iquiz_student_view"));

                	Map iquizAssignmentMap = null;
                	final List legacyUpdates = new ArrayList();
                	Map convertedEidUidRecordMap = null;

                	convertedEidUidRecordMap = GradebookManagerHibernateImpl.this.synchronizer.convertEidUid(gradeRecordsFromCall);
                	if (!isUpdateAll && GradebookManagerHibernateImpl.this.synchronizer !=null && !GradebookManagerHibernateImpl.this.synchronizer.isProjectSite()){
                		iquizAssignmentMap = GradebookManagerHibernateImpl.this.synchronizer.getLegacyAssignmentWithStats(assignment.getName());
                	}
                	Map recordsFromCLDb = null;
                	if(GradebookManagerHibernateImpl.this.synchronizer != null && isIquizCall && isUpdateAll)
                	{
                		recordsFromCLDb = GradebookManagerHibernateImpl.this.synchronizer.getPersistentRecords(assignment.getId());
                	}

                	for(final Iterator iter = gradeRecordsFromCall.iterator(); iter.hasNext();) {
                		AssignmentGradeRecord gradeRecordFromCall = (AssignmentGradeRecord)iter.next();

                		boolean updated = false;
                		if(isIquizCall && GradebookManagerHibernateImpl.this.synchronizer != null)
                		{
                			gradeRecordFromCall = GradebookManagerHibernateImpl.this.synchronizer.convertIquizRecordToUid(gradeRecordFromCall, convertedEidUidRecordMap, isUpdateAll, graderId);
                		}
                		else
                		{
                			gradeRecordFromCall.setGraderId(graderId);
                			gradeRecordFromCall.setDateRecorded(now);
                		}
                		try {
                			/** sychronize - add condition for null value */
                			if(gradeRecordFromCall != null)
                			{
                				if(gradeRecordFromCall.getId() == null && isIquizCall && isUpdateAll && recordsFromCLDb != null)
                				{
                					final AssignmentGradeRecord returnedPersistentItem = (AssignmentGradeRecord) recordsFromCLDb.get(gradeRecordFromCall.getStudentId());
                					if(returnedPersistentItem != null && returnedPersistentItem.getPointsEarned() != null && gradeRecordFromCall.getPointsEarned() != null
                							&& !returnedPersistentItem.getPointsEarned().equals(gradeRecordFromCall.getPointsEarned()))
                					{
                						graderId = gradeRecordFromCall.getGraderId();
                						updated = true;
                						returnedPersistentItem.setGraderId(gradeRecordFromCall.getGraderId());
                						returnedPersistentItem.setPointsEarned(gradeRecordFromCall.getPointsEarned());
                						returnedPersistentItem.setDateRecorded(gradeRecordFromCall.getDateRecorded());
                						session.saveOrUpdate(returnedPersistentItem);
                					}
                					else if(returnedPersistentItem == null)
                					{
                						graderId = gradeRecordFromCall.getGraderId();
                						updated = true;
                						session.saveOrUpdate(gradeRecordFromCall);
                					}
                				}
                				else
                				{
                					updated = true;
                					session.saveOrUpdate(gradeRecordFromCall);
                				}
                			}
                			if (!isUpdateAll && !isStudentView && GradebookManagerHibernateImpl.this.synchronizer != null && !GradebookManagerHibernateImpl.this.synchronizer.isProjectSite())
                			{
                				final Object updateIquizRecord = GradebookManagerHibernateImpl.this.synchronizer.getNeededUpdateIquizRecord(assignment, gradeRecordFromCall);
                				if(updateIquizRecord != null) {
                					legacyUpdates.add(updateIquizRecord);
                			}
                			}
                		} catch (final TransientObjectException e) {
                			// It's possible that a previously unscored student
                			// was scored behind the current user's back before
                			// the user saved the new score. This translates
                			// that case into an optimistic locking failure.
                			if(log.isInfoEnabled()) {
								log.info("An optimistic locking failure occurred while attempting to add a new assignment grade record");
							}
                			throw new StaleObjectModificationException(e);
                		}

                		// Check for excessive (AKA extra credit) scoring.
                		/** synchronize - add condition for null value*/
                		if(gradeRecordFromCall != null && updated == true)
                		{
                			if (gradeRecordFromCall.getPointsEarned() != null &&
                					!assignment.getUngraded() &&
                					gradeRecordFromCall.getPointsEarned().compareTo(assignment.getPointsPossible()) > 0) {
                				studentsWithExcessiveScores.add(gradeRecordFromCall.getStudentId());
                			}

                			logAssignmentGradingEvent(gradeRecordFromCall, graderId, assignment, session);
                			studentsWithUpdatedAssignmentGradeRecords.add(gradeRecordFromCall.getStudentId());
                		}

                		/** synchronize external records */
                		if (legacyUpdates.size() > 0 && GradebookManagerHibernateImpl.this.synchronizer != null)
                		{
                			GradebookManagerHibernateImpl.this.synchronizer.updateLegacyGradeRecords(assignment.getName(), legacyUpdates);
                		}
                	}

                }
                else
                {
                	for(final Iterator iter = gradeRecordsFromCall.iterator(); iter.hasNext();) {
                		final AssignmentGradeRecord gradeRecordFromCall = (AssignmentGradeRecord)iter.next();
                		gradeRecordFromCall.setGraderId(graderId);
                		gradeRecordFromCall.setDateRecorded(now);
                		try {
                			session.saveOrUpdate(gradeRecordFromCall);
                		} catch (final TransientObjectException e) {
                			// It's possible that a previously unscored student
                			// was scored behind the current user's back before
                			// the user saved the new score. This translates
                			// that case into an optimistic locking failure.
                			if(log.isInfoEnabled()) {
								log.info("An optimistic locking failure occurred while attempting to add a new assignment grade record");
							}
                			throw new StaleObjectModificationException(e);
                		}

                		// Check for excessive (AKA extra credit) scoring.
                		if (gradeRecordFromCall.getPointsEarned() != null &&
                				!assignment.getUngraded() &&
                				gradeRecordFromCall.getPointsEarned().compareTo(assignment.getPointsPossible()) > 0) {
                			studentsWithExcessiveScores.add(gradeRecordFromCall.getStudentId());
                		}

                		// Logger the grading event, and keep track of the students with saved/updated grades
                		logAssignmentGradingEvent(gradeRecordFromCall, graderId, assignment, session);

                		studentsWithUpdatedAssignmentGradeRecords.add(gradeRecordFromCall.getStudentId());
                	}
                }
                if (log.isDebugEnabled()) {
					log.debug("Updated " + studentsWithUpdatedAssignmentGradeRecords.size() + " assignment score records");
				}

                return studentsWithExcessiveScores;
            }
        };

        final Set studentsWithExcessiveScores = (Set)getHibernateTemplate().execute(hc);
        if (log.isDebugEnabled()) {
			log.debug("END: Update " + gradeRecordsFromCall.size() + " scores for gradebook=" + assignment.getGradebook().getUid() + ", assignment=" + assignment.getName());
		}
        return studentsWithExcessiveScores;
    }
    
    /**
     * 
     * @return Returns set of Assignments given scores higher than the assignment's value.
     */
    private Set updateStudentGradeRecords(final Collection gradeRecordsFromCall, final String studentId)
            throws StaleObjectModificationException, IllegalArgumentException {
    	if (studentId == null) {
    		throw new IllegalArgumentException("no studentId passed to GradebookManagerHibernateImpl.updateStudentGradeRecords");
    	}
        // If no grade records are sent, don't bother doing anything with the db
        if(gradeRecordsFromCall.size() == 0) {
            log.debug("updateStudentGradeRecords called for zero grade records");
            return new HashSet();
        }

        if (log.isDebugEnabled()) {
			log.debug("BEGIN: Update " + gradeRecordsFromCall.size());
		}

        final HibernateCallback hc = new HibernateCallback() {
            @Override
            public Object doInHibernate(final Session session) throws HibernateException {
                final Date now = new Date();
                String graderId = GradebookManagerHibernateImpl.this.authn.getUserUid();

                final Set studentsWithUpdatedAssignmentGradeRecords = new HashSet();
                final Set assignmentsWithExcessiveScores = new HashSet();
                
                /** synchronize from external application*/
                if(GradebookManagerHibernateImpl.this.synchronizer != null)
                {
                	final boolean isUpdateAll = Boolean.TRUE.equals(ThreadLocalManager.get("iquiz_update_all"));
                	final boolean isIquizCall = Boolean.TRUE.equals(ThreadLocalManager.get("iquiz_call"));
                	final boolean isStudentView = Boolean.TRUE.equals(ThreadLocalManager.get("iquiz_student_view"));
           
                	final List legacyUpdates = new ArrayList();
                	final Map convertedEidUidRecordMap = GradebookManagerHibernateImpl.this.synchronizer.convertEidUid(gradeRecordsFromCall);
     
                	Map recordsFromCLDb = null;
                	if(GradebookManagerHibernateImpl.this.synchronizer != null && isIquizCall && isUpdateAll)
                	{
                		recordsFromCLDb = GradebookManagerHibernateImpl.this.synchronizer.getPersistentRecordsForStudent(studentId);
                	}

                	for(final Iterator iter = gradeRecordsFromCall.iterator(); iter.hasNext();) {
                		AssignmentGradeRecord gradeRecordFromCall = (AssignmentGradeRecord)iter.next();
                		GradebookAssignment assignment = null;
                		if (gradeRecordFromCall != null) {
                			assignment = gradeRecordFromCall.getAssignment();
                		}

                		boolean updated = false;
                		if(isIquizCall && GradebookManagerHibernateImpl.this.synchronizer != null)
                		{
                			gradeRecordFromCall = GradebookManagerHibernateImpl.this.synchronizer.convertIquizRecordToUid(gradeRecordFromCall, convertedEidUidRecordMap, isUpdateAll, graderId);
                		}
                		else
                		{
                			gradeRecordFromCall.setGraderId(graderId);
                			gradeRecordFromCall.setDateRecorded(now);
                		}
                		try {
                			/** sychronize - add condition for null value */
                			if(gradeRecordFromCall != null)
                			{
                				if(gradeRecordFromCall.getId() == null && isIquizCall && isUpdateAll && recordsFromCLDb != null)
                				{
                					final AssignmentGradeRecord returnedPersistentItem = (AssignmentGradeRecord) recordsFromCLDb.get(gradeRecordFromCall.getGradableObject().getId());
                					if(returnedPersistentItem != null && returnedPersistentItem.getPointsEarned() != null && gradeRecordFromCall.getPointsEarned() != null
                							&& !returnedPersistentItem.getPointsEarned().equals(gradeRecordFromCall.getPointsEarned()))
                					{
                						graderId = gradeRecordFromCall.getGraderId();
                						updated = true;
                						returnedPersistentItem.setGraderId(gradeRecordFromCall.getGraderId());
                						returnedPersistentItem.setPointsEarned(gradeRecordFromCall.getPointsEarned());
                						returnedPersistentItem.setDateRecorded(gradeRecordFromCall.getDateRecorded());
                						session.saveOrUpdate(returnedPersistentItem);
                					}
                					else if(returnedPersistentItem == null)
                					{
                						graderId = gradeRecordFromCall.getGraderId();
                						updated = true;
                						session.saveOrUpdate(gradeRecordFromCall);
                					}
                				}
                				else
                				{
                					updated = true;
                					session.saveOrUpdate(gradeRecordFromCall);
                				}
                			}
                			if (assignment != null && !isUpdateAll && !isStudentView && GradebookManagerHibernateImpl.this.synchronizer != null && !GradebookManagerHibernateImpl.this.synchronizer.isProjectSite())
                			{
                				final Object updateIquizRecord = GradebookManagerHibernateImpl.this.synchronizer.getNeededUpdateIquizRecord(assignment, gradeRecordFromCall);
                				if(updateIquizRecord != null) {
                					legacyUpdates.add(updateIquizRecord);
                			}
                			}
                		} catch (final TransientObjectException e) {
                			// It's possible that a previously unscored student
                			// was scored behind the current user's back before
                			// the user saved the new score. This translates
                			// that case into an optimistic locking failure.
                			if(log.isInfoEnabled()) {
								log.info("An optimistic locking failure occurred while attempting to add a new assignment grade record");
							}
                			throw new StaleObjectModificationException(e);
                		}

                		// Check for excessive (AKA extra credit) scoring.
                		/** synchronize - add condition for null value*/
                		if(gradeRecordFromCall != null && updated == true && assignment != null)
                		{
                			if (gradeRecordFromCall.getPointsEarned() != null &&
                					!assignment.getUngraded() && 
                					gradeRecordFromCall.getPointsEarned().compareTo(assignment.getPointsPossible()) > 0) {
                				assignmentsWithExcessiveScores.add(assignment);
                			}

                			// Logger the grading event, and keep track of the students with saved/updated grades
                			logAssignmentGradingEvent(gradeRecordFromCall, graderId, assignment, session);
                			
                			studentsWithUpdatedAssignmentGradeRecords.add(gradeRecordFromCall.getStudentId());
                		}

                		/** synchronize external records */
                		if (legacyUpdates.size() > 0 && GradebookManagerHibernateImpl.this.synchronizer != null && assignment != null)
                		{
                			GradebookManagerHibernateImpl.this.synchronizer.updateLegacyGradeRecords(assignment.getName(), legacyUpdates);
                		}
                	}

                }
                else
                {

	                for(final Iterator iter = gradeRecordsFromCall.iterator(); iter.hasNext();) {
	                	final AssignmentGradeRecord gradeRecordFromCall = (AssignmentGradeRecord)iter.next();
	                	final GradebookAssignment assignment = gradeRecordFromCall.getAssignment();
	                	final Double pointsPossible = assignment.getPointsPossible();
	                	
	                	gradeRecordFromCall.setGraderId(graderId);
	                	gradeRecordFromCall.setDateRecorded(now);
	                	try {
	                		session.saveOrUpdate(gradeRecordFromCall);
	                	} catch (final TransientObjectException e) {
	                		// It's possible that a previously unscored student
	                		// was scored behind the current user's back before
	                		// the user saved the new score. This translates
	                		// that case into an optimistic locking failure.
	                		if(log.isInfoEnabled()) {
								log.info("An optimistic locking failure occurred while attempting to add a new assignment grade record");
							}
	                		throw new StaleObjectModificationException(e);
	                	}
	
	                	// Check for excessive (AKA extra credit) scoring.
	                	if (gradeRecordFromCall.getPointsEarned() != null &&
	                			!assignment.getUngraded() && 
	                			gradeRecordFromCall.getPointsEarned().compareTo(pointsPossible) > 0) {
	                		assignmentsWithExcessiveScores.add(assignment);
	                	}
	
	                	// Logger the grading event, and keep track of the students with saved/updated grades
	                	logAssignmentGradingEvent(gradeRecordFromCall, graderId, assignment, session);
	                	
	                	studentsWithUpdatedAssignmentGradeRecords.add(gradeRecordFromCall.getStudentId());
	                }
                }
				if (log.isDebugEnabled()) {
					log.debug("Updated " + studentsWithUpdatedAssignmentGradeRecords.size() + " assignment score records");
				}

                return assignmentsWithExcessiveScores;
            }
        };

        final Set assignmentsWithExcessiveScores = (Set)getHibernateTemplate().execute(hc);
        if (log.isDebugEnabled()) {
			log.debug("END: Update " + gradeRecordsFromCall.size());
		}
        return assignmentsWithExcessiveScores;
    }

    @Override
	public Set updateAssignmentGradesAndComments(final GradebookAssignment assignment, final Collection gradeRecords, final Collection comments) throws StaleObjectModificationException {
		//Set studentsWithExcessiveScores = updateAssignmentGradeRecords(assignment, gradeRecords);
		final Gradebook gradebook = getGradebook(assignment.getGradebook().getId());
		final Set studentsWithExcessiveScores = updateAssignmentGradeRecords(assignment, gradeRecords, gradebook.getGrade_type());
		
		updateComments(comments);
		
		return studentsWithExcessiveScores;
	}
	
    @Override
	public void updateComments(final Collection comments) throws StaleObjectModificationException {
        final Date now = new Date();
        final String graderId = this.authn.getUserUid();

        // Unlike the complex grade update logic, this method assumes that
		// the client has done the work of filtering out any unchanged records
		// and isn't interested in throwing an optimistic locking exception for untouched records
		// which were changed by other sessions.
		final HibernateCallback hc = new HibernateCallback() {
			@Override
            public Object doInHibernate(final Session session) throws HibernateException {
				for (final Iterator iter = comments.iterator(); iter.hasNext();) {
					final Comment comment = (Comment)iter.next();
					comment.setGraderId(graderId);
					comment.setDateRecorded(now);
					session.saveOrUpdate(comment);
				}
				return null;
			}
		};
		try {
			getHibernateTemplate().execute(hc);
		} catch (final DataIntegrityViolationException e) {
			// If a student hasn't yet received a comment for this
			// assignment, and two graders try to save a new comment record at the
			// same time, the database should report a unique constraint violation.
			// Since that's similar to the conflict between two graders who
			// are trying to update an existing comment record at the same
			// same time, this method translates the exception into an
			// optimistic locking failure.
			if(log.isInfoEnabled()) {
				log.info("An optimistic locking failure occurred while attempting to update comments");
			}
			throw new StaleObjectModificationException(e);
		}
	}

	/**
     */
    @Override
    public void updateCourseGradeRecords(final CourseGrade courseGrade, final Collection gradeRecordsFromCall)
            throws StaleObjectModificationException {

        if(gradeRecordsFromCall.size() == 0) {
            log.debug("updateCourseGradeRecords called with zero grade records to update");
            return;
        }
        
        if (log.isDebugEnabled()) {
			log.debug("BEGIN: Update " + gradeRecordsFromCall.size() + " course grades for gradebook=" + courseGrade.getGradebook().getUid());
		}

        final HibernateCallback hc = new HibernateCallback() {
            @Override
            public Object doInHibernate(final Session session) throws HibernateException {
                for(final Iterator iter = gradeRecordsFromCall.iterator(); iter.hasNext();) {
                    session.evict(iter.next());
                }

                final Date now = new Date();
                final String graderId = GradebookManagerHibernateImpl.this.authn.getUserUid();
                int numberOfUpdatedGrades = 0;

                for(final Iterator iter = gradeRecordsFromCall.iterator(); iter.hasNext();) {
                    // The modified course grade record
                    final CourseGradeRecord gradeRecordFromCall = (CourseGradeRecord)iter.next();
                    gradeRecordFromCall.setGraderId(graderId);
                    gradeRecordFromCall.setDateRecorded(now);
                    try {
                        session.saveOrUpdate(gradeRecordFromCall);
                        session.flush();
                    } catch (final StaleObjectStateException sose) {
                        if(log.isInfoEnabled()) {
							log.info("An optimistic locking failure occurred while attempting to update course grade records");
						}
                        throw new StaleObjectModificationException(sose);
                    }

                    // Logger the grading event
                    session.save(new GradingEvent(courseGrade, graderId, gradeRecordFromCall.getStudentId(), gradeRecordFromCall.getEnteredGrade()));
                    
                    numberOfUpdatedGrades++;
                }
                if (log.isDebugEnabled()) {
					log.debug("Changed " + numberOfUpdatedGrades + " course grades for gradebook=" + courseGrade.getGradebook().getUid());
				}
                return null;
            }
        };

        try {
	        getHibernateTemplate().execute(hc);
	        if (log.isDebugEnabled()) {
				log.debug("END: Update " + gradeRecordsFromCall.size() + " course grades for gradebook=" + courseGrade.getGradebook().getUid());
			}
		} catch (final DataIntegrityViolationException e) {
			// It's possible that a previously ungraded student
			// was graded behind the current user's back before
			// the user saved the new grade. This translates
			// that case into an optimistic locking failure.
			if(log.isInfoEnabled()) {
				log.info("An optimistic locking failure occurred while attempting to update course grade records");
			}
			throw new StaleObjectModificationException(e);
		}
    }

    @Override
    public boolean isEnteredAssignmentScores(final Long assignmentId) {
        final HibernateCallback<List<AssignmentGradeRecord>> hcb = session -> session.createQuery(
                    "select agr from AssignmentGradeRecord as agr where agr.gradableObject.id = :id and agr.pointsEarned is not null").
                    setLong("id", assignmentId)
                    .list();
		final List<AssignmentGradeRecord> list = getHibernateTemplate().execute(hcb);
		final Integer total = list.size();
		log.debug("assignment {} has {} entered scores", assignmentId, total);
        return total > 0;
    }

    /**
     */
    @Override
    public List<AssignmentGradeRecord> getStudentGradeRecords(final Long gradebookId, final String studentId) {
        final HibernateCallback<List<AssignmentGradeRecord>> hc = session -> session.createQuery(
                "from AssignmentGradeRecord as agr where agr.studentId = :studentid and agr.gradableObject.removed=false and agr.gradableObject.gradebook.id = :gradebookid").
                setString("studentid", studentId).
                setLong("gradebookid", gradebookId).
                list();
        return getHibernateTemplate().execute(hc);
    }
    
    @Override
    public List getStudentGradeRecordsConverted(final Long gradebookId, final String studentId) {
    	final List studentGradeRecsFromDB = getStudentGradeRecords(gradebookId, studentId);
    	final Gradebook gradebook = getGradebook(gradebookId);
    	if(gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS) {
    		return studentGradeRecsFromDB;
		} else if(gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE)
    	{
    		return convertPointsToPercentage(gradebook, studentGradeRecsFromDB);
    	}
    	else if(gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER)
    	{
    		return convertPointsToLetterGrade(gradebook, studentGradeRecsFromDB);
    	}
    	
    	return null;
    }
    
    /**
     * 
     * @param studentId
     * @param gradebook
     * @param categories
     * @param gradeRecsthe AssignmentGradeRecords for the given student
     * @param countedAssigns - the Assignments in this gradebook that are counted toward the course grade. 
     * use {@link #getCountedAssignments(Session, Long)} to retrieve this list
     * @return the total points earned that count toward the course grade.
     * a List is returned with two elements:
     * [1] is (Double) totalPointsEarned
     * [2] is (Double) literalTotalPointsEarned
     */
    abstract List getTotalPointsEarnedInternal(final String studentId, final Gradebook gradebook, final List categories, final List<AssignmentGradeRecord> gradeRecs, List<GradebookAssignment> countedAssigns);

    //for testing
    public double getTotalPointsEarnedInternal(final Long gradebookId, final String studentId, final Gradebook gradebook, final List categories) 
    {
    	final HibernateCallback hc = new HibernateCallback() {
    		@Override
            public Object doInHibernate(final Session session) throws HibernateException {
    			double totalPointsEarned = 0;
    			final Iterator scoresIter = session.createQuery(
    			"select agr.pointsEarned, asn from AssignmentGradeRecord agr, Assignment asn where agr.gradableObject=asn and agr.studentId=:student and asn.gradebook.id=:gbid and asn.removed=false and asn.notCounted=false and asn.ungraded=false and asn.pointsPossible > 0").
    			setParameter("student", studentId).
    			setParameter("gbid", gradebookId).
    			list().iterator();

    			final List assgnsList = session.createQuery(
    			"from GradebookAssignment as asn where asn.gradebook.id=:gbid and asn.removed=false and asn.notCounted=false and asn.ungraded=false and asn.pointsPossible > 0").
    			setParameter("gbid", gradebookId).
    			list();

    			final Map cateScoreMap = new HashMap();
    			final Map cateTotalScoreMap = new HashMap();

    			final Set assignmentsTaken = new HashSet();
    			while (scoresIter.hasNext()) {
    				final Object[] returned = (Object[])scoresIter.next();
    				final Double pointsEarned = (Double)returned[0];
    				final GradebookAssignment go = (GradebookAssignment) returned[1];
    				if (pointsEarned != null) {
    					if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
    					{
    						totalPointsEarned += pointsEarned.doubleValue();
        				assignmentsTaken.add(go.getId());
    					}
    					else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && go != null)
    					{
    						totalPointsEarned += pointsEarned.doubleValue();
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
    			
    			if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && categories != null)
    			{
    				final Iterator assgnsIter = assgnsList.iterator();
    				while (assgnsIter.hasNext()) 
    				{
    					final GradebookAssignment asgn = (GradebookAssignment)assgnsIter.next();
    					if(assignmentsTaken.contains(asgn.getId()))
    					{
    						for(int i=0; i<categories.size(); i++)
    						{
    							final Category cate = (Category) categories.get(i);
    							if(cate != null && !cate.isRemoved() && asgn.getCategory() != null && cate.getId().equals(asgn.getCategory().getId()))
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
    						totalPointsEarned += ((Double)cateScoreMap.get(cate.getId())).doubleValue() * cate.getWeight().doubleValue() / ((Double)cateTotalScoreMap.get(cate.getId())).doubleValue();
    					}
    				}
    			}

    			if (log.isDebugEnabled()) {
					log.debug("getTotalPointsEarnedInternal for studentId=" + studentId + " returning " + totalPointsEarned);
				}
    			return totalPointsEarned;
    		}
    	};
    	return (Double)getHibernateTemplate().execute(hc);
    }

    @Override
    public GradingEvents getGradingEvents(final GradableObject gradableObject, final Collection studentIds) {

        // Don't attempt to run the query if there are no enrollments
        if(studentIds == null || studentIds.size() == 0) {
            log.debug("No enrollments were specified.  Returning an empty GradingEvents object");
            return new GradingEvents();
        }

        final HibernateCallback<List<GradingEvent>> hc = session -> session.createCriteria(GradingEvent.class)
				.add(Restrictions.eq("gradableObject", gradableObject))
				.add(HibernateCriterionUtils.CriterionInRestrictionSplitter("studentId", studentIds))
				.list();

        final List<GradingEvent> list = getHibernateTemplate().execute(hc);

        final GradingEvents events = new GradingEvents();

		for (final GradingEvent event : list) {
			events.addEvent(event);
		}
        return events;
    }
    
    @Override
    public Map getGradingEventsForStudent(final String studentId, final Collection gradableObjects) {
    	if (log.isDebugEnabled()) {
			log.debug("getGradingEventsForStudent called for studentId:" + studentId);
		}
    	final Map goEventListMap = new HashMap();
    	
        // Don't attempt to run the query if there are no gradableObjects or student id
        if(gradableObjects == null || gradableObjects.size() == 0) {
            log.debug("No gb items were specified.  Returning an empty GradingEvents object");
            return goEventListMap;
        }
        if (studentId == null) {
        	log.debug("No student id was specified.  Returning an empty GradingEvents object");
        	return goEventListMap;
        }
        
        
        for (final Iterator goIter = gradableObjects.iterator(); goIter.hasNext();) {
        	final GradableObject go = (GradableObject) goIter.next();
        	goEventListMap.put(go, new ArrayList());
        }

		final HibernateCallback<List<GradingEvent>> hc = session -> session.createCriteria(GradingEvent.class)
				.add(Restrictions.eq("studentId", studentId))
				.add(HibernateCriterionUtils.CriterionInRestrictionSplitter("gradableObject", gradableObjects))
				.list();

        final List<GradingEvent> list = getHibernateTemplate().execute(hc);

		for (final GradingEvent event : list) {
			final GradableObject go = event.getGradableObject();
			final List goEventList = (List) goEventListMap.get(go);
			if (goEventList != null) {
				goEventList.add(event);
			} else {
				log.debug("event retrieved by getGradingEventsForStudent not associated with passed go list");
			}
		}
        
        return goEventListMap;
    }


    /**
     */
    @Override
    public List getAssignments(final Long gradebookId, final String sortBy, final boolean ascending) {
		List assignments = super.getAssignments(gradebookId);
		/** synchronize from external application*/
		if (this.synchronizer != null) {
			this.synchronizer.synchrornizeAssignments(assignments);

			assignments = super.getAssignments(gradebookId);
		}
		/** end synchronize from external application*/

		sortAssignments(assignments, sortBy, ascending);
		return assignments;
	}


    /**
     */
    @Override
    public List getAssignmentsWithStats(final Long gradebookId, final String sortBy, final boolean ascending) {
       return getAssignmentsWithStats(gradebookId, sortBy, ascending, false);
    }
    
    /**
     */
    public List getAssignmentsWithStats(final Long gradebookId, final String sortBy, final boolean ascending, final boolean includeDroppedScores) {
        final Set studentUids = getAllStudentUids(getGradebookUid(gradebookId));
        final List<AssignmentGradeRecord> gradeRecords = getAllAssignmentGradeRecords(gradebookId, studentUids);
        if(!includeDroppedScores) {
            applyDropScores(gradeRecords);
        }
        final List assignments = getAssignmentsWithStats(gradebookId, sortBy, ascending, gradeRecords);
        return assignments;
    }
    
    /**
     * 
     * @param gradebookId
     * @param sortBy
     * @param ascending
     * @param gradeRecords - use {@link #getAllAssignmentGradeRecords(Long, Collection)}
     * @return a list of all assignments with stats populated. this method is
     * helpful to eliminate repeated calls to {@link #getAllAssignmentGradeRecords(Long, Collection)}
     * if you have already retrieved them
     */
    
    private List getAssignmentsWithStats(final Long gradebookId, final String sortBy, 
            final boolean ascending, final List<AssignmentGradeRecord> gradeRecords) {

        final List assignments = getAssignments(gradebookId);
        for (final Iterator iter = assignments.iterator(); iter.hasNext(); ) {
            final GradebookAssignment assignment = (GradebookAssignment)iter.next();
            assignment.calculateStatistics(gradeRecords);
        }
        sortAssignments(assignments, sortBy, ascending);
        return assignments;
    }

    @Override
    public List getAssignmentsAndCourseGradeWithStats(final Long gradebookId, final String sortBy, final boolean ascending) {
        final Set studentUids = getAllStudentUids(getGradebookUid(gradebookId));
        final List assignments = getAssignments(gradebookId);
        final CourseGrade courseGrade = getCourseGrade(gradebookId);
        final Map gradeRecordMap = new HashMap();
        final List<AssignmentGradeRecord> gradeRecords = getAllAssignmentGradeRecords(gradebookId, studentUids);
        applyDropScores(gradeRecords);
        addToGradeRecordMap(gradeRecordMap, gradeRecords);
        
        for (final Iterator iter = assignments.iterator(); iter.hasNext(); ) {
        	final GradebookAssignment assignment = (GradebookAssignment)iter.next();
        	assignment.calculateStatistics(gradeRecords);
        }
        
        final List<CourseGradeRecord> courseGradeRecords = getPointsEarnedCourseGradeRecords(courseGrade, studentUids, assignments, gradeRecordMap);
        courseGrade.calculateStatistics(courseGradeRecords, studentUids.size());
        
        sortAssignments(assignments, sortBy, ascending);
        
        // Always put the Course Grade at the end.
        assignments.add(courseGrade);

        return assignments;
    }

    protected List filterAndPopulateCourseGradeRecordsByStudents(final CourseGrade courseGrade, final Collection gradeRecords, final Collection studentUids) {
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

    /**
     * TODO Remove this method in favor of doing database sorting.
     *
     * @param assignments
     * @param sortBy
     * @param ascending
     */
    private void sortAssignments(final List assignments, final String sortBy, final boolean ascending) {
        // WARNING: AZ - this method is duplicated in GradebookManagerHibernateImpl
        Comparator comp;
        if (GradebookAssignment.SORT_BY_NAME.equals(sortBy)) {
            comp = GradableObject.nameComparator;
        } else if(GradebookAssignment.SORT_BY_DATE.equals(sortBy)){
            comp = GradableObject.dateComparator;
        } else if(GradebookAssignment.SORT_BY_MEAN.equals(sortBy)) {
            comp = GradableObject.meanComparator;
        } else if(GradebookAssignment.SORT_BY_POINTS.equals(sortBy)) {
            comp = GradebookAssignment.pointsComparator;
        } else if(GradebookAssignment.SORT_BY_RELEASED.equals(sortBy)){
            comp = GradebookAssignment.releasedComparator;
        } else if(GradebookAssignment.SORT_BY_COUNTED.equals(sortBy)){
            comp = GradebookAssignment.countedComparator;
        } else if(GradebookAssignment.SORT_BY_EDITOR.equals(sortBy)){
            comp = GradebookAssignment.gradeEditorComparator;
        } else if (GradebookAssignment.SORT_BY_SORTING.equals(sortBy)) {
            comp = GradableObject.sortingComparator;
        } else {
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

    /**
     */
    @Override
    public List getAssignments(final Long gradebookId) {
        return getAssignments(gradebookId, GradebookAssignment.DEFAULT_SORT, true);
    }

    /**
     */
    @Override
    public GradebookAssignment getAssignmentWithStats(final Long assignmentId) {
        return getAssignmentWithStats(assignmentId, false);
    }
    
    /**
     */
    public GradebookAssignment getAssignmentWithStats(final Long assignmentId, final boolean includeDroppedScores) {

    	final GradebookAssignment assignment = getAssignment(assignmentId);
    	final Long gradebookId = assignment.getGradebook().getId();
        final Set studentUids = getAllStudentUids(getGradebookUid(gradebookId));
        final List<AssignmentGradeRecord> gradeRecords = getAssignmentGradeRecords(assignment, studentUids);
        if(!includeDroppedScores) {
            applyDropScores(gradeRecords);
        }
        assignment.calculateStatistics(gradeRecords);
        return assignment;
    }

    /**
     */
    @Override
    public void updateAssignment(final GradebookAssignment assignment)
        throws ConflictingAssignmentNameException, StaleObjectModificationException {
       	super.updateAssignment(assignment);
        try {
        	/** synchronize from external application*/
        	String oldTitle = null;
        	if(this.synchronizer != null)
        	{
        		final GradebookAssignment assign = getAssignment(assignment.getId());
        		oldTitle = assign.getName();
        	}
        	/** synchronize from external application*/
        	if(this.synchronizer != null && oldTitle != null  && !this.synchronizer.isProjectSite())
        	{
        		this.synchronizer.updateAssignment(oldTitle, assignment.getName(), assignment.getGradebook().getGrade_type());
        	}
        } catch (final HibernateOptimisticLockingFailureException holfe) {
            if(log.isInfoEnabled()) {
				log.info("An optimistic locking failure occurred while attempting to update an assignment");
        	}
            throw new StaleObjectModificationException(holfe);
        }
    }
    

    /**
     * update category and assignments in same session
     * for drop scores functionality
     */
    @Override
    public void updateCategoryAndAssignmentsPointsPossible(final Long gradebookId, final Category category)
        throws ConflictingAssignmentNameException, StaleObjectModificationException {
		updateCategory(category);
            // get assignments for this category
        final HibernateCallback<List<GradebookAssignment>> hc = session -> session
				.createQuery("select asn from GradebookAssignment asn where asn.gradebook.id=:gbid and asn.category=:category and asn.removed = false")
				.setParameter("gbid", gradebookId)
				.setParameter("category", category)
				.list();
		final List<GradebookAssignment> list = getHibernateTemplate().execute(hc);
        for (final GradebookAssignment assignment : list) {
			if (assignment.getGradebook().getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
				assignment.setUngraded(true);
			}
			if (assignment.getUngraded()) {
				assignment.setNotCounted(true);
			}
			// for drop score categories pointsPossible comes from the category
			assignment.setPointsPossible(category.getItemValue());
			super.updateAssignment(assignment);
		}
		try {
            /** synchronize from external application*/
            
            final Map oldTitles = new HashMap();
            final List assignments = category.getAssignmentList();
            if(this.synchronizer != null) {
                for(final Iterator iter = assignments.iterator(); iter.hasNext();) {
                    final GradebookAssignment assignment = (GradebookAssignment) iter.next();
                    final GradebookAssignment assign = getAssignment(assignment.getId());
                    oldTitles.put(assignment.getId(), assign.getName());
                }
            }
            getHibernateTemplate().execute(hc);
            
            /** synchronize from external application*/
            for(final Iterator iter = assignments.iterator(); iter.hasNext();) {
                final GradebookAssignment assignment = (GradebookAssignment) iter.next();
                final String oldTitle = (String)oldTitles.get(assignment.getId());
                assignment.setPointsPossible(category.getItemValue());
                if(this.synchronizer != null && oldTitle != null  && !this.synchronizer.isProjectSite() && !assignment.getUngraded()) {
                    this.synchronizer.updateAssignment(oldTitle, assignment.getName(), assignment.getGradebook().getGrade_type());
                }
                }
        } catch (final HibernateOptimisticLockingFailureException holfe) {
            if(log.isInfoEnabled()) {
				log.info("An optimistic locking failure occurred while attempting to update an assignment");
            }
            throw new StaleObjectModificationException(holfe);
        }
    }

    /**
     * Gets the total number of points possible in a gradebook.
     */
    @Override
    public double getTotalPoints(final Long gradebookId) {
    	final Double totalPoints = (Double)getHibernateTemplate().execute(new HibernateCallback() {
    		@Override
            public Object doInHibernate(final Session session) throws HibernateException {
    			final Gradebook gradebook = getGradebook(gradebookId);
    			final List cates = getCategoriesWithAssignments(gradebookId);
    			return new Double(getLiteralTotalPointsInternal(gradebookId, session, gradebook, cates));
    			//return new Double(getTotalPointsInternal(gradebookId, session));
    		}
    	});
    	return totalPoints.doubleValue();
    }
 
    //for testing
    public double getTotalPointsInternal(final Long gradebookId, final Gradebook gradebook, final List categories, final String studentId) 
    {
    	final HibernateCallback hc = new HibernateCallback() {
    		@Override
            public Object doInHibernate(final Session session) throws HibernateException {
    			double totalPointsPossible = 0;
    			final List assgnsList = session.createQuery(
    			"select asn from GradebookAssignment asn where asn.gradebook.id=:gbid and asn.removed=false and asn.notCounted=false and asn.ungraded=false and asn.pointsPossible > 0").
    			setParameter("gbid", gradebookId).
    			list();
    			
    			final Iterator scoresIter = session.createQuery(
    			"select agr.pointsEarned, asn from AssignmentGradeRecord agr, Assignment asn where agr.gradableObject=asn and agr.studentId=:student and asn.gradebook.id=:gbid and asn.removed=false and asn.notCounted=false and asn.ungraded=false and asn.pointsPossible > 0").
    			setParameter("student", studentId).
    			setParameter("gbid", gradebookId).
    			list().iterator();

    			final Set categoryTaken = new HashSet();
    			final Set assignmentsTaken = new HashSet();
    			while (scoresIter.hasNext()) {
    				final Object[] returned = (Object[])scoresIter.next();
    				final Double pointsEarned = (Double)returned[0];
    				final GradebookAssignment go = (GradebookAssignment) returned[1];
    				if (pointsEarned != null) {
    					if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
    					{
        				assignmentsTaken.add(go.getId());
    					}
    					else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && go != null)
    					{
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
            				categoryTaken.add(cate.getId());
    								break;
    							}
    						}
    					}
    				}
    			}

    			if(!assignmentsTaken.isEmpty())
    			{
    				if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
    				{
  		    		for(int i=0; i<categories.size(); i++)
  		    		{
  		    			final Category cate = (Category) categories.get(i);
  		    			if(cate != null && !cate.isRemoved() && categoryTaken.contains(cate.getId()) )
  		    			{
  		    				totalPointsPossible += cate.getWeight().doubleValue();
  		    			}
  		    		}
  		    		return totalPointsPossible;
    				}
    				final Iterator assignmentIter = assgnsList.iterator();
    				while (assignmentIter.hasNext()) {
    					final GradebookAssignment asn = (GradebookAssignment) assignmentIter.next();
    					if(asn != null)
    					{
    						final Double pointsPossible = asn.getPointsPossible();

    						if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY && assignmentsTaken.contains(asn.getId()))
    						{
    							totalPointsPossible += pointsPossible.doubleValue();
    						}
    						else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && assignmentsTaken.contains(asn.getId()))
    						{
    							totalPointsPossible += pointsPossible.doubleValue();
    						}
    					}
    				}
    			} else {
    				totalPointsPossible = -1;
				}
    			
    			return totalPointsPossible;
    		}
    	};
    	return (Double)getHibernateTemplate().execute(hc);    	
    }
    
    /**
     * 
     * @param gradebook
     * @param categories
     * @param studentId
     * @param studentGradeRecs - the AssignmentGradeRecords for the given student
     * @param countedAssigns - the Assignments in this gradebook that are counted toward the course grade. use {@link #getCountedAssignments(Session, Long)}
     * @return the total points possible for the given student. if the grade rec is
     * null or doesn't exist for a counted assignment, then that assignment does not count toward the course grade
     * for this particular student. 
     */
    @Override
    public abstract double getTotalPointsInternal(final Gradebook gradebook, final List categories, final String studentId, List<AssignmentGradeRecord> studentGradeRecs, List<GradebookAssignment> countedAssigns, boolean literalTotal);

    //for test
    public double getLiteralTotalPointsInternal(final Long gradebookId, final Gradebook gradebook, final List categories)
    {
    	final HibernateCallback hc = new HibernateCallback() {
    		@Override
            public Object doInHibernate(final Session session) throws HibernateException {
    			double totalPointsPossible = 0;
    			final Iterator assignmentIter = session.createQuery(
    			"select asn from GradebookAssignment asn where asn.gradebook.id=:gbid and asn.removed=false and asn.notCounted=false and asn.ungraded=false").
    			setParameter("gbid", gradebookId).
    			list().iterator();
    			while (assignmentIter.hasNext()) {
    				final GradebookAssignment asn = (GradebookAssignment) assignmentIter.next();
    				if(asn != null)
    				{
    					final Double pointsPossible = asn.getPointsPossible();

    					if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
    					{
    						totalPointsPossible += pointsPossible.doubleValue();
    					}
    					else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY )
    					{
    						totalPointsPossible += pointsPossible.doubleValue();    						
    					}
    					else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && categories != null)
    					{
    						for(int i=0; i<categories.size(); i++)
    						{
    							final Category cate = (Category) categories.get(i);
    							if(cate != null && !cate.isRemoved() && asn.getCategory() != null && cate.getId().equals(asn.getCategory().getId()))
    							{
    								totalPointsPossible += pointsPossible.doubleValue();
    								break;
    							}
    						}
    					}
    				}
    			}
    			return totalPointsPossible;
    		}
    	};
    	return (Double)getHibernateTemplate().execute(hc);    	
    }

    private double getLiteralTotalPointsInternal(final Long gradebookId, final Session session, final Gradebook gradebook, final List categories)
    {
    	double totalPointsPossible = 0;
    	final Map<Long,Integer> numAssignments = new HashMap<Long,Integer>();
        
    	final Iterator assignmentIter = session.createQuery(
    			"select asn from GradebookAssignment asn where asn.gradebook.id=:gbid and asn.removed=false and asn.notCounted=false and asn.ungraded=false and (asn.extraCredit=false or asn.extraCredit is null)").
    			setParameter("gbid", gradebookId).
    			list().iterator();
        
    	while (assignmentIter.hasNext()) {
    		final GradebookAssignment asn = (GradebookAssignment) assignmentIter.next();
    		if(asn != null)
    		{
    			final Double pointsPossible = asn.getPointsPossible();

    			if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
    			{
    				if (pointsPossible!=null) {
    					totalPointsPossible += pointsPossible.doubleValue();
    			}
    			}
    			else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY)
				{
				if (pointsPossible!=null && asn.getCategory() != null && !asn.getCategory().getIsExtraCredit()) {
    					totalPointsPossible += pointsPossible.doubleValue();
				}
                    for(int i=0; i<categories.size(); i++)
                    {
                        final Category cate = (Category) categories.get(i);
                        if(cate != null && !cate.isRemoved() && asn.getCategory() != null && cate.getId().equals(asn.getCategory().getId()))
                        {
                            
                            Integer num = numAssignments.get(cate.getId()); // to calculate totalPointsToDrop, must know the number of assignments for each category
                            if(num == null) {
                                num = new Integer(0);
                            }
                            num++;
                            numAssignments.put(cate.getId(), num);
                            break;
                        }
                    }
                }
    			else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && categories != null)
    			{
    				for(int i=0; i<categories.size(); i++)
    				{
    					final Category cate = (Category) categories.get(i);
    					if(cate != null && !cate.isRemoved() && asn.getCategory() != null && cate.getId().equals(asn.getCategory().getId()))
    					{
							if (pointsPossible!=null && !asn.getCategory().getIsExtraCredit()) {
    							totalPointsPossible += pointsPossible.doubleValue();
							}
    						
    						Integer num = numAssignments.get(cate.getId()); // to calculate totalPointsToDrop, must know the number of assignments for each category
                            if(num == null) {
                                num = new Integer(0);
                            }
                            num++;
    						numAssignments.put(cate.getId(), num);
    						break;
    					}
    				}
    			}
    		}
    	}
        double totalPointsToDrop = 0;
        
        for(int i=0; i<categories.size(); i++) {
            final Category category = (Category) categories.get(i);
            if(category != null && !category.isRemoved() && category.isDropScores()) {
                final Double itemValue = category.getItemValue();
                final Integer dropHighest = category.getDropHighest();
				Integer dropLowest = category.getDropLowest();
                final Integer keepHighest = category.getKeepHighest();
                
                final Integer assignmentCount = numAssignments.get(category.getId());
                if(keepHighest != null && keepHighest > 0) {
                    if(assignmentCount != null && assignmentCount > 0) {
                        dropLowest = assignmentCount - keepHighest; // dropLowest and keepHighest will not occur at the same time
                        if(dropLowest < 0) {
                            dropLowest = 0;
                        }
                    }
                }
                if(assignmentCount != null && assignmentCount > (dropLowest + dropHighest)) {
                    totalPointsToDrop += (itemValue * dropHighest);
                    totalPointsToDrop += (itemValue * dropLowest);
                }
            }                       
        }
    	totalPointsPossible -= totalPointsToDrop;
    	return totalPointsPossible;
    }
//    
//    private double getLiteralTotalPointsInternal(final Long gradebookId, Session session, final Gradebook gradebook, final List categories)
//    {
//    	double totalPointsPossible = 0;
//    	Iterator assignmentIter = session.createQuery(
//    			"select asn from GradebookAssignment asn where asn.gradebook.id=:gbid and asn.removed=false and asn.notCounted=false and asn.ungraded=false").
//    			setParameter("gbid", gradebookId).
//    			list().iterator();
//    	while (assignmentIter.hasNext()) {
//    		GradebookAssignment asn = (GradebookAssignment) assignmentIter.next();
//    		if(asn != null)
//    		{
//    			Double pointsPossible = asn.getPointsPossible();
//
//    			if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
//    			{
//    				totalPointsPossible += pointsPossible.doubleValue();
//    			}
//    			else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY)
// 					{
//    				totalPointsPossible += pointsPossible.doubleValue();
// 					}
//    			else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && categories != null)
//    			{
//    				for(int i=0; i<categories.size(); i++)
//    				{
//    					Category cate = (Category) categories.get(i);
//    					if(cate != null && !cate.isRemoved() && asn.getCategory() != null && cate.getId().equals(asn.getCategory().getId()))
//    					{
//    						totalPointsPossible += pointsPossible.doubleValue();
//    						break;
//    					}
//    				}
//    			}
//    		}
//    	}
//    	return totalPointsPossible;
//    }

    @Override
    public Gradebook getGradebookWithGradeMappings(final Long id) {
		return (Gradebook)getHibernateTemplate().execute(new HibernateCallback() {
			@Override
            public Object doInHibernate(final Session session) throws HibernateException {
				final Gradebook gradebook = (Gradebook)session.load(Gradebook.class, id);
				Hibernate.initialize(gradebook.getGradeMappings());
				return gradebook;
			}
		});
	}


    /**
     *
     * @param spreadsheetId
     * @return
     */
    @Override
    public Spreadsheet getSpreadsheet(final Long spreadsheetId) {
        return getHibernateTemplate().load(Spreadsheet.class, spreadsheetId);
    }

    /**
     *
     * @param gradebookId
     * @return
     */
    @Override
    public List getSpreadsheets(final Long gradebookId) {
        return (List)getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(final Session session) throws HibernateException {
                final List spreadsheets = getSpreadsheets(gradebookId, session);
                return spreadsheets;
            }
        });
    }

    /**
     *
     * @param spreadsheetId
     */
    @Override
    public void removeSpreadsheet(final Long spreadsheetId)throws StaleObjectModificationException {

        final HibernateCallback hc = new HibernateCallback() {
            @Override
            public Object doInHibernate(final Session session) throws HibernateException {
                final Spreadsheet spt = (Spreadsheet)session.load(Spreadsheet.class, spreadsheetId);
                session.delete(spt);
                if(log.isInfoEnabled()) {
					log.info("Spreadsheet " + spt.getName() + " has been removed from gradebook" );
				}

                return null;
            }
        };
        getHibernateTemplate().execute(hc);

    }

    /**
     *
     * @param spreadsheet
     */
    public void updateSpreadsheet(final Spreadsheet spreadsheet)throws ConflictingAssignmentNameException, StaleObjectModificationException  {
            final HibernateCallback hc = new HibernateCallback() {
                @Override
                public Object doInHibernate(final Session session) throws HibernateException {
                    // Ensure that we don't have the assignment in the session, since
                    // we need to compare the existing one in the db to our edited assignment
                    session.evict(spreadsheet);

                    final Spreadsheet sptFromDb = (Spreadsheet)session.load(Spreadsheet.class, spreadsheet.getId());
                    final List conflictList = session.createQuery(
                            "select spt from Spreadsheet as spt where spt.name = :name and spt.gradebook = :gradebook and spt.id != :id").
                            setString("name", spreadsheet.getName()).
                            setEntity("gradebook", spreadsheet.getGradebook()).
                            setLong("id", spreadsheet.getId())
							.list();
					final int numNameConflicts = conflictList.size();
                    if(numNameConflicts > 0) {
                        throw new ConflictingAssignmentNameException("You can not save multiple spreadsheets in a gradebook with the same name");
                    }

                    session.evict(sptFromDb);
                    session.update(spreadsheet);

                    return null;
                }
            };
            try {
                getHibernateTemplate().execute(hc);
            } catch (final HibernateOptimisticLockingFailureException holfe) {
                if(log.isInfoEnabled()) {
					log.info("An optimistic locking failure occurred while attempting to update a spreadsheet");
				}
                throw new StaleObjectModificationException(holfe);
            }
    }

    @Override
    public Long createSpreadsheet(final Long gradebookId, final String name, final String creator, final Date dateCreated, final String content) throws ConflictingSpreadsheetNameException,StaleObjectModificationException {

        final HibernateCallback hc = new HibernateCallback() {
            @Override
            public Object doInHibernate(final Session session) throws HibernateException {
                final Gradebook gb = (Gradebook)session.load(Gradebook.class, gradebookId);
                final List conflictList = (session.createQuery(
                        "select spt from Spreadsheet as spt where spt.name = :name and spt.gradebook = :gradebook").
                        setString("name", name).
                        setEntity("gradebook", gb).list());
            		final int numNameConflicts = conflictList.size();
                if(numNameConflicts > 0) {
                    throw new ConflictingSpreadsheetNameException("You can not save multiple spreadsheets in a gradebook with the same name");
                }

                final Spreadsheet spt = new Spreadsheet();
                spt.setGradebook(gb);
                spt.setName(name);
                spt.setCreator(creator);
                spt.setDateCreated(new Date());
                spt.setContent(content);

                // Save the new assignment
                final Long id = (Long)session.save(spt);
                return id;
            }
        };

        return (Long)getHibernateTemplate().execute(hc);

    }

    protected List getSpreadsheets(final Long gradebookId, final Session session) throws HibernateException {
        final List spreadsheets = session.createQuery(
                "from Spreadsheet as spt where spt.gradebook.id = :id").
                setLong("id", gradebookId.longValue()).
                list();
        return spreadsheets;
    }

    @Override
    public List<Comment> getStudentAssignmentComments(final String studentId, final Long gradebookId) {
        final HibernateCallback<List<Comment>> hcb =  session -> session
				.createQuery("from Comment as c where c.studentId=:studentId and c.gradableObject.gradebook.id=:gradebookId")
				.setParameter("studentId", studentId)
				.setParameter("gradebookId",gradebookId)
				.list();
		return getHibernateTemplate().execute(hcb);
    }
    
    @Override
    public boolean validateCategoryWeighting(final Long gradebookId)
    {
    	final Gradebook gradebook = getGradebook(gradebookId);
    	if(gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY) {
    		return true;
		}
    	final List cats = getCategories(gradebookId);
    	double weight = 0.0;
    	for(int i=0; i<cats.size(); i++)
    	{
    		final Category cat = (Category) cats.get(i);
    		if(cat != null)
    		{
    			weight += cat.getWeight().doubleValue();
    		}
    	}
    	if(Math.rint(weight) == 1) {
    		return true;
		} else {
    		return false;
    }
    }
    
    @Override
    public Set updateAssignmentGradeRecords(final GradebookAssignment assignment, final Collection gradeRecords, final int grade_type)
    {
    	if(grade_type == GradebookService.GRADE_TYPE_POINTS) {
    		return updateAssignmentGradeRecords(assignment, gradeRecords);
		} else if(grade_type == GradebookService.GRADE_TYPE_PERCENTAGE)
    	{
    		final Collection convertList = new ArrayList();
    		for(final Iterator iter = gradeRecords.iterator(); iter.hasNext();)
    		{
    			final AssignmentGradeRecord agr = (AssignmentGradeRecord) iter.next();
    			final Double doubleValue = calculateDoublePointForRecord(agr);
    			if(agr != null && doubleValue != null)
    			{
    				agr.setPointsEarned(doubleValue);
    				convertList.add(agr);
    			}
    			else if(agr != null)
    			{
    				agr.setPointsEarned(null);
    				convertList.add(agr);
    			}
    		}
    		return updateAssignmentGradeRecords(assignment, convertList);
    	}
    	else if(grade_type == GradebookService.GRADE_TYPE_LETTER)
    	{
    		final Collection convertList = new ArrayList();
    		for(final Iterator iter = gradeRecords.iterator(); iter.hasNext();)
    		{
    			final AssignmentGradeRecord agr = (AssignmentGradeRecord) iter.next();
    			final Double doubleValue = calculateDoublePointForLetterGradeRecord(agr);
    			if(agr != null && doubleValue != null)
    			{
        		agr.setPointsEarned(doubleValue);
        		convertList.add(agr);
        	}
        	else if(agr != null)
        	{
        		agr.setPointsEarned(null);
        		convertList.add(agr);
        	}
        }
        return updateAssignmentGradeRecords(assignment, convertList);
    	} else {
    		return null;
    }
    }
    
    /**
     * Updates student grade records based upon the grade entry type -
     * grade will be converted appropriately before update
     * 
     * @param studentUid
     * @param gradeRecords
     * @param grade_type
     * @return
     */
    @Override
    public Set updateStudentGradeRecords(final Collection gradeRecords, final int grade_type, final String studentId)
    {
    	if(grade_type == GradebookService.GRADE_TYPE_POINTS) {
    		return updateStudentGradeRecords(gradeRecords, studentId);
		} else if(grade_type == GradebookService.GRADE_TYPE_PERCENTAGE)
    	{
    		final Collection convertList = new ArrayList();
    		for(final Iterator iter = gradeRecords.iterator(); iter.hasNext();)
    		{
    			final AssignmentGradeRecord agr = (AssignmentGradeRecord) iter.next();
    			final Double doubleValue = calculateDoublePointForRecord(agr);
    			if(agr != null && doubleValue != null)
    			{
    				agr.setPointsEarned(doubleValue);
    				convertList.add(agr);
    			}
    			else if(agr != null)
    			{
    				agr.setPointsEarned(null);
    				convertList.add(agr);
    			}
    		}
    		return updateStudentGradeRecords(convertList, studentId);
    	}
    	else if(grade_type == GradebookService.GRADE_TYPE_LETTER)
    	{
    		final Collection convertList = new ArrayList();
    		for(final Iterator iter = gradeRecords.iterator(); iter.hasNext();)
    		{
    			final AssignmentGradeRecord agr = (AssignmentGradeRecord) iter.next();
    			final Double doubleValue = calculateDoublePointForLetterGrade(agr);
    			if(agr != null && doubleValue != null)
    			{
    				agr.setPointsEarned(doubleValue);
    				convertList.add(agr);
    			}
    			else if(agr != null)
    			{
    				agr.setPointsEarned(null);
    				convertList.add(agr);
    			}
    		}
    		return updateStudentGradeRecords(convertList, studentId);
    	} else {
    		return null;
    }
    }

    private Double calculateDoublePointForRecord(final AssignmentGradeRecord gradeRecordFromCall)
    {
    	final GradebookAssignment assign = getAssignment(gradeRecordFromCall.getAssignment().getId());
    	if(gradeRecordFromCall.getPercentEarned() != null)
    	{
    		if(gradeRecordFromCall.getPercentEarned().doubleValue() / 100.0 < 0)
    		{
    			throw new IllegalArgumentException("percent for record is less than 0 for percentage points in GradebookManagerHibernateImpl.calculateDoublePointForRecord");
    		}
    		return new Double(assign.getPointsPossible().doubleValue() * (gradeRecordFromCall.getPercentEarned().doubleValue() / 100.0));
    	} else {
    		return null;
    }
    }
    
    private Double calculateDoublePointForLetterGradeRecord(final AssignmentGradeRecord gradeRecordFromCall)
    {
    	final GradebookAssignment assign = getAssignment(gradeRecordFromCall.getAssignment().getId());
    	final Gradebook gradebook = getGradebook(assign.getGradebook().getId());
    	if(gradeRecordFromCall.getLetterEarned() != null)
    	{
    		final LetterGradePercentMapping lgpm = getLetterGradePercentMapping(gradebook);
    		if(lgpm != null && lgpm.getGradeMap() != null)
    		{
    			final Double doublePercentage = lgpm.getValue(gradeRecordFromCall.getLetterEarned());
    			if(doublePercentage == null)
    			{
    				log.error("percentage for " + gradeRecordFromCall.getLetterEarned() + " is not found in letter grade mapping in GradebookManagerHibernateImpl.calculateDoublePointForLetterGradeRecord");
    				return null;
    			}
    			
    			return calculateEquivalentPointValueForPercent(assign.getPointsPossible(), doublePercentage);
    		}
    		return null;
    	} else {
    		return null;
    }
    }

    private Double calculateDoublePointForLetterGrade(final AssignmentGradeRecord gradeRecordFromCall)
    {
    	final GradebookAssignment assign = getAssignment(gradeRecordFromCall.getAssignment().getId());
    	if(gradeRecordFromCall.getLetterEarned() != null)
    	{
    		final LetterGradePercentMapping lgpm = getLetterGradePercentMapping(assign.getGradebook());
    		if(lgpm != null && lgpm.getGradeMap() != null)
    		{
    			final Double doublePercentage = lgpm.getValue(gradeRecordFromCall.getLetterEarned());
    			if(doublePercentage == null)
    			{
    				log.error("percentage for " + gradeRecordFromCall.getLetterEarned() + " is not found in letter grade mapping in GradebookManagerHibernateImpl.calculateDoublePointForLetterGrade");
    				return null;
    			}
    			
    			return calculateEquivalentPointValueForPercent(assign.getPointsPossible(), doublePercentage);
    		}
    		return null;
    	} else {
    		return null;
    }
    }
    
    @Override
    public List getAssignmentGradeRecordsConverted(final GradebookAssignment assignment, final Collection studentUids)
    {
    	final List assignRecordsFromDB = getAssignmentGradeRecords(assignment, studentUids);
    	final Gradebook gradebook = getGradebook(assignment.getGradebook().getId());
    	if(gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS) {
    		return assignRecordsFromDB;
		} else if(gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE)
    	{
    		return convertPointsToPercentage(assignment, gradebook, assignRecordsFromDB);
    	}
    	else if(gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER)
    	{
    		return convertPointsToLetterGrade(assignment, gradebook, assignRecordsFromDB);
    	}    	
    	return null;
    }

    private List convertPointsToPercentage(final GradebookAssignment assignment, final Gradebook gradebook, final List assignRecordsFromDB)
    {
    	final Double pointPossible = assignment.getPointsPossible();
    	final List percentageList = new ArrayList();
    	if(pointPossible.doubleValue() > 0)
    	{

    		for(int i=0; i<assignRecordsFromDB.size(); i++)
    		{
    			final AssignmentGradeRecord agr = (AssignmentGradeRecord) assignRecordsFromDB.get(i);
    			if(agr != null) {
        			agr.setDateRecorded(agr.getDateRecorded());
        			agr.setGraderId(agr.getGraderId());
    			}
    			if(agr != null && agr.getPointsEarned() != null)
    			{
    				agr.setPercentEarned(calculateEquivalentPercent(pointPossible, agr.getPointsEarned()));
    				percentageList.add(agr);
    			}
    			else if(agr != null)
    			{
    				agr.setPercentEarned(null);
    				percentageList.add(agr);
    			}
    		}
    	}
    	return percentageList;
    }
    
    private List convertPointsToLetterGrade(final GradebookAssignment assignment, final Gradebook gradebook, final List assignRecordsFromDB)
    {
    	final Double pointPossible = assignment.getPointsPossible();
    	if(pointPossible.doubleValue() > 0)
    	{
    		final List letterGradeList = new ArrayList();
    		final LetterGradePercentMapping lgpm = getLetterGradePercentMapping(assignment.getGradebook());
    		for(int i=0; i<assignRecordsFromDB.size(); i++)
    		{
    			final AssignmentGradeRecord agr = (AssignmentGradeRecord) assignRecordsFromDB.get(i);
      		if(agr != null) {
      		    agr.setDateRecorded(agr.getDateRecorded());
                agr.setGraderId(agr.getGraderId());
      		}
      		if(agr != null && agr.getPointsEarned() != null )
      		{
        		final String letterGrade = lgpm.getGrade(calculateEquivalentPercent(pointPossible, agr.getPointsEarned()));
      			agr.setLetterEarned(letterGrade);
      			letterGradeList.add(agr);
      		}
      		else if(agr != null)
      		{
      			agr.setLetterEarned(null);
      			letterGradeList.add(agr);
      		}
    		}
    		return letterGradeList;
    	}
    	return null;
    }
    
    @Override
    public List getAssignmentsCategoriesAndCourseGradeWithStats(final Long gradebookId,
            String assignmentSort, final boolean assignAscending, final String categorySort, final boolean categoryAscending) {
        final List catAssignCGList = new ArrayList();
        
        final Set<String> allStudentUids = getAllStudentUids(getGradebookUid(gradebookId));
        final List<AssignmentGradeRecord> gradeRecords = getAllAssignmentGradeRecords(gradebookId, allStudentUids);
        
        if (assignmentSort == null) {
            assignmentSort = GradebookAssignment.DEFAULT_SORT;
        }
        
        final List<GradebookAssignment> allAssignments = getAssignmentsWithStats(gradebookId, assignmentSort, assignAscending, gradeRecords);

        // this method also returns the course grade
        final List categoriesPlusCG = getCategoriesWithStats(gradebookId, assignmentSort,
                assignAscending, categorySort, categoryAscending, gradeRecords, allAssignments);
        
        // we will add assignments, then categories, then course grade (which is included in cate list)
        if (allAssignments != null) {
            catAssignCGList.addAll(allAssignments);
        }
        
        if (categoriesPlusCG != null) {
            catAssignCGList.addAll(categoriesPlusCG);
        }
        
        return catAssignCGList;
    }
    
    /**
     * 
     * @param gradebookId
     * @param assignmentSort
     * @param assignAscending
     * @param categorySort
     * @param categoryAscending
     * @param gradeRecs
     * @param assignmentsWithStats
     * @return a list of the Categories with stats populated plus the Course Grade.
     * this method is useful if you have already retrieved all grade recs and
     * all assignments with stats to avoid repeated calls
     */
    private List getCategoriesWithStats(final Long gradebookId, final String assignmentSort, final boolean assignAscending,
            final String categorySort, final boolean categoryAscending, final List<AssignmentGradeRecord> gradeRecs,
            final List<GradebookAssignment> assignmentsWithStats){
    	final Set studentUids = getAllStudentUids(getGradebookUid(gradebookId));
    	return getCategoriesWithStats(gradebookId, assignmentSort, assignAscending, categorySort, categoryAscending, gradeRecs, assignmentsWithStats, studentUids);
	}
    
    private List getCategoriesWithStats(final Long gradebookId, final String assignmentSort, final boolean assignAscending,
                                        final String categorySort, final boolean categoryAscending, final List<AssignmentGradeRecord> gradeRecs,
                                        final List<GradebookAssignment> assignmentsWithStats, final Set studentUids) {
        final List categories = getCategories(gradebookId);

        final Map cateMap = new HashMap();
        for (final Object element : assignmentsWithStats) {
            final GradebookAssignment assign = (GradebookAssignment) element;
            if(assign != null)
            {
            	// the assigns already have stats calculated
                //assign.calculateStatistics(gradeRecs);

                if(assign.getCategory() != null && cateMap.get(assign.getCategory().getId()) == null)
                {
                    final List assignList = new ArrayList();
                    assignList.add(assign);
                    cateMap.put(assign.getCategory().getId(), assignList);
                }
                else
                {
                    if(assign.getCategory() != null)
                    {
                        final List assignList = (List) cateMap.get(assign.getCategory().getId());
                        assignList.add(assign);
                        cateMap.put(assign.getCategory().getId(),assignList);
                    }
                }
            }
        }
        
        for (final Iterator iter = categories.iterator(); iter.hasNext(); )
        {
            final Category cate = (Category) iter.next();
            if(cate != null && cateMap.get(cate.getId()) != null)
            {
                cate.calculateStatistics((List) cateMap.get(cate.getId()));
                cate.setAssignmentList((List)cateMap.get(cate.getId()));
            }
        }
        
        if(categorySort != null) {
            sortCategories(categories, categorySort, categoryAscending);
		} else {
            sortCategories(categories, Category.SORT_BY_NAME, categoryAscending);
		}

        final CourseGrade courseGrade = getCourseGrade(gradebookId);
        final Map gradeRecordMap = new HashMap();
        addToGradeRecordMap(gradeRecordMap, gradeRecs);
        //      List<CourseGradeRecord> courseGradeRecords = getPointsEarnedCourseGradeRecords(courseGrade, studentUids, releasedAssignments, gradeRecordMap);
        final List<CourseGradeRecord> courseGradeRecords = getPointsEarnedCourseGradeRecords(courseGrade, studentUids, assignmentsWithStats, gradeRecordMap);
        courseGrade.calculateStatistics(courseGradeRecords, studentUids.size());

        categories.add(courseGrade);

        return categories;
    }
    
    @Override
    public List getCategoriesWithStats(final Long gradebookId, final String assignmentSort, final boolean assignAscending, final String categorySort, final boolean categoryAscending) {
    	return getCategoriesWithStats(gradebookId, assignmentSort, assignAscending, categorySort, categoryAscending, false);
    }
    
    @Override
    public List getCategoriesWithStats(final Long gradebookId, final String assignmentSort,
			final boolean assignAscending, final String categorySort,
			final boolean categoryAscending, final boolean includeDroppedScores){
        final Set studentUids = getAllStudentUids(getGradebookUid(gradebookId));
        return getCategoriesWithStats(gradebookId, assignmentSort, assignAscending, categorySort, categoryAscending, includeDroppedScores, studentUids);
    }
    
    @Override
    public List getCategoriesWithStats(final Long gradebookId, String assignmentSort,
			final boolean assignAscending, final String categorySort,
			final boolean categoryAscending, final boolean includeDroppedScores, final Set studentUids){
    	List allAssignments;
    	
    	if (assignmentSort == null) {
    	    assignmentSort = GradebookAssignment.DEFAULT_SORT;
    	}
    	
        final List gradeRecords = getAllAssignmentGradeRecords(gradebookId, studentUids);
        if(!includeDroppedScores) {
            applyDropScores(gradeRecords);
        }
    	allAssignments = getAssignmentsWithStats(gradebookId, assignmentSort, assignAscending, gradeRecords);
    	
    	return getCategoriesWithStats(gradebookId, assignmentSort, assignAscending, 
    	        categorySort, categoryAscending, gradeRecords, allAssignments, studentUids);
    }

    private void sortCategories(final List categories, final String sortBy, final boolean ascending)
    {
    	Comparator comp;
    	if(Category.SORT_BY_NAME.equals(sortBy)) 
    	{
    		comp = Category.nameComparator;
    	}
    	else if(Category.SORT_BY_AVERAGE_SCORE.equals(sortBy))
    	{
    		comp = Category.averageScoreComparator;
    	}
    	else if(Category.SORT_BY_WEIGHT.equals(sortBy))
    	{
    		comp = Category.weightComparator;
    	}
    	else
    	{
    		comp = Category.nameComparator;
    	}
    	Collections.sort(categories, comp);
    	if(!ascending) 
    	{
    		Collections.reverse(categories);
    	}
    }

    @Override
    public List<GradebookAssignment> getAssignmentsWithNoCategory(final Long gradebookId, final String assignmentSort, final boolean assignAscending)
    {
	HibernateCallback<List<GradebookAssignment>> hc = session -> session
			.createQuery("from GradebookAssignment as asn where asn.gradebook.id = :id and asn.removed=false and asn.category is null")
			.setLong("id", gradebookId)
			.list();

    	final List<GradebookAssignment> assignList = getHibernateTemplate().execute(hc);
    	if(assignmentSort != null) {
    		sortAssignments(assignList, assignmentSort, assignAscending);
		} else {
    		sortAssignments(assignList, GradebookAssignment.DEFAULT_SORT, assignAscending);
		}
    	
    	return assignList;
    }

    @Override
    public List getAssignmentsWithNoCategoryWithStats(final Long gradebookId, final String assignmentSort, final boolean assignAscending)
    {
    	final Set studentUids = getAllStudentUids(getGradebookUid(gradebookId));
    	final List assignments = getAssignmentsWithNoCategory(gradebookId, assignmentSort, assignAscending);
    	final List<AssignmentGradeRecord> gradeRecords = getAllAssignmentGradeRecords(gradebookId, studentUids);
        applyDropScores(gradeRecords);
    	for (final Iterator iter = assignments.iterator(); iter.hasNext(); ) {
    		final GradebookAssignment assignment = (GradebookAssignment)iter.next();
    		assignment.calculateStatistics(gradeRecords);
    	}
    	// AZ - fixing bug, sorts based on stats need to be resorted
        if (assignmentSort != null) {
            sortAssignments(assignments, assignmentSort, assignAscending);
        } else {
            sortAssignments(assignments, GradebookAssignment.DEFAULT_SORT, assignAscending);
        }
    	return assignments;
    }

    @Override
    public void convertGradingEventsConverted(final GradebookAssignment assign, final GradingEvents events, final List studentUids, final int grade_type)
    {
    	LetterGradePercentMapping lgpm = new LetterGradePercentMapping();
    	if (grade_type == GradebookService.GRADE_TYPE_LETTER) {
    		lgpm = getLetterGradePercentMapping(assign.getGradebook());
    	}
    	
    	for(final Iterator iter = studentUids.iterator(); iter.hasNext();)
    	{
    		final List gradingEvents = events.getEvents((String)iter.next());
    		for(final Iterator eventIter = gradingEvents.iterator(); eventIter.hasNext();)
    		{
    			final GradingEvent ge = (GradingEvent) eventIter.next();
    			if (ge.getGrade() != null) {
	    			if(grade_type == GradebookService.GRADE_TYPE_PERCENTAGE)
	    			{
	    				ge.setGrade(calculateEquivalentPercent(assign.getPointsPossible(), new Double(ge.getGrade())).toString());
	    			} else if(grade_type == GradebookService.GRADE_TYPE_LETTER) {
	    				String letterGrade = null;
	    				if (lgpm != null) {
	    					letterGrade = lgpm.getGrade(calculateEquivalentPercent(assign.getPointsPossible(), new Double(ge.getGrade())));
	    				}
	    				ge.setGrade(letterGrade);	
	    			}
    			}
    		}
    	}
    }
    
    @Override
    public void convertGradingEventsConvertedForStudent(final Gradebook gradebook, final Map gradableObjectEventListMap, final int grade_type) {
    	if (gradableObjectEventListMap == null || gradableObjectEventListMap.isEmpty()) {
    		return;
    	}
    	
    	LetterGradePercentMapping lgpm = new LetterGradePercentMapping();
    	if (grade_type == GradebookService.GRADE_TYPE_LETTER) {
    		lgpm = getLetterGradePercentMapping(gradebook);
    	}
    	
    	for (final Iterator<Map.Entry<GradableObject, List>> goIter = gradableObjectEventListMap.entrySet().iterator(); goIter.hasNext();) {
            final Map.Entry<GradableObject, List> entry = goIter.next();
            final GradableObject go = entry.getKey();

    		if (go instanceof GradebookAssignment) {
    			final GradebookAssignment assign = (GradebookAssignment) go;
    			final Double pointsPossible = assign.getPointsPossible();
    			
	    		final List eventList = (List) gradableObjectEventListMap.get(go);
	    		if (eventList != null && eventList.size() > 0) {
	    			for(final Iterator eventIter = eventList.iterator(); eventIter.hasNext();)
	        		{
	        			final GradingEvent ge = (GradingEvent) eventIter.next();
	        			if (ge.getGrade() != null) {
	    	    			if(grade_type == GradebookService.GRADE_TYPE_PERCENTAGE)
	    	    			{
	    	    				ge.setGrade(calculateEquivalentPercent(pointsPossible, new Double(ge.getGrade())).toString());
	    	    			} else if(grade_type == GradebookService.GRADE_TYPE_LETTER) {
	    	    				String letterGrade = null;
	    	    				if (lgpm != null) {
	    	    					letterGrade = lgpm.getGrade(calculateEquivalentPercent(pointsPossible, new Double(ge.getGrade())));
	    	    				}
	    	    				ge.setGrade(letterGrade);	
	    	    			}
	        			}
	        		}
	    		}
    		}
    	}
    }
    
    @Override
    public boolean checkStuendsNotSubmitted(final Gradebook gradebook)
    {
    	final Set studentUids = getAllStudentUids(getGradebookUid(gradebook.getId()));
    	if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY || gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY)
    	{
    		final List records = getAllAssignmentGradeRecords(gradebook.getId(), studentUids);
    		final List assigns = getAssignments(gradebook.getId(), GradebookAssignment.DEFAULT_SORT, true);
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

    		if(filteredRecords.size() < (filteredAssigns.size() * studentUids.size())) {
    			return true;
			}
    		
    		return false;
    	}
    	else
    	{
      	final List assigns = getAssignments(gradebook.getId(), GradebookAssignment.DEFAULT_SORT, true);
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
    		
    		if(filteredRecords.size() < filteredAssigns.size() * studentUids.size()) {
    			return true;
			}
    		
    		return false;
    	}
    }
    
    @Override
    public void fillInZeroForNullGradeRecords(final Gradebook gradebook)
    {
    	finalizeNullGradeRecords(gradebook);
    }

    @Override
    public void convertGradePointsForUpdatedTotalPoints(final Gradebook gradebook, final GradebookAssignment assignment, final Double newTotal, final List studentUids)
    {
  		if(newTotal == null || assignment == null || gradebook == null)
  		{
  			throw new IllegalArgumentException("null values found in convertGradePointsForUpdatedTotalPoints.");
  		}
    	if(gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE && assignment.getPointsPossible() != null)
    	{
    		final List records = getAssignmentGradeRecordsConverted(assignment, studentUids);
    		for(final Iterator iter = records.iterator(); iter.hasNext(); )
    		{
    			final AssignmentGradeRecord agr = (AssignmentGradeRecord) iter.next();
    			if(agr != null && agr.getPercentEarned() != null)
    			{
    				agr.setPointsEarned(calculateEquivalentPointValueForPercent(newTotal, agr.getPercentEarned()));
    			}
    		}
    		updateAssignmentGradeRecords(assignment, records);
    	}
    	else if(gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER && assignment.getPointsPossible() != null)
    	{
    		final List records = getAssignmentGradeRecordsConverted(assignment, studentUids);
    		final LetterGradePercentMapping lgpm = getLetterGradePercentMapping(gradebook);
    		for(final Iterator iter = records.iterator(); iter.hasNext(); )
    		{
    			final AssignmentGradeRecord agr = (AssignmentGradeRecord) iter.next();
    			if(agr != null && agr.getLetterEarned() != null)
	    		{	
    				final Double doublePercentage = lgpm.getValue(agr.getLetterEarned());
    				if (doublePercentage != null) {
	    				agr.setPointsEarned(calculateEquivalentPointValueForPercent(newTotal, doublePercentage));
    				} else {
    					log.error("No equivalent % mapping for letter grade: " + agr.getLetterEarned() + " in method convertGradePointsForTotalUpdatedPoints");
    				}
    			}
    		}
    		updateAssignmentGradeRecords(assignment, records);
    	}
    }

    @Override
    protected Long saveNewAssignment(final Long gradebookId, final Long categoryId, final GradebookAssignment asn) throws ConflictingAssignmentNameException
    {
        final Long result = super.saveNewAssignment(gradebookId, categoryId, asn);
        
        syncAssignment(asn.getName());
        
        return result;
    }    
    
    private void syncAssignment(final String asnName)
    {
        /** synchronize from external application */
        if (this.synchronizer != null && !this.synchronizer.isProjectSite())
        {
                this.synchronizer.addLegacyAssignment(asnName);
        }
     }

    /** synchronize from external application */
    public void setSynchronizer(final GbSynchronizer synchronizer)
    {
    	this.synchronizer = synchronizer;
    }
    
    @Override
    public void createAssignments(final Long gradebookId, final List assignList) throws MultipleAssignmentSavingException
    {
    	final List assignIds = new ArrayList();
    	try
    	{
    		for(final Iterator iter = assignList.iterator(); iter.hasNext();)
    		{
    			final GradebookAssignment assign = (GradebookAssignment) iter.next();
    			if(assign.getCategory() == null)
    			{
    				assignIds.add(createAssignment(gradebookId, assign.getName(), assign.getPointsPossible(), assign.getDueDate(), assign.isNotCounted(), assign.isReleased(), assign.isExtraCredit()));
    			} else {
    				assignIds.add(createAssignmentForCategory(gradebookId, assign.getCategory().getId(), assign.getName(), assign.getPointsPossible(), assign.getDueDate(), assign.isNotCounted(), assign.isReleased(), assign.isExtraCredit()));
    		}
    	}
    	}
    	catch(final Exception e)
    	{
    		for(final Iterator iter = assignIds.iterator(); iter.hasNext();)
    		{
    			removeAssignment((Long)iter.next());
    		}

    		throw new MultipleAssignmentSavingException("Errors occur while trying to saving multiple assignment items in createAssignments -- " + e.getMessage());
    	}
    }
    
    @Override
    public boolean checkValidName(final Long gradebookId, final GradebookAssignment assignment)
    {
    	final HibernateCallback<List<GradableObject>> hc = session -> {
            final Gradebook gb = (Gradebook) session.load(Gradebook.class, gradebookId);
            return session.createQuery(
                    "select go from GradableObject as go where go.name = :name and go.gradebook = :gradebook and go.removed = false").
                    setString("name", assignment.getName()).
                    setEntity("gradebook", gb)
					.list();
        };

    	final Integer conflicts = getHibernateTemplate().execute(hc).size();
    	
    	if (conflicts > 0) {
			return false;
		}
    	return true;
    }
	
	private void logAssignmentGradingEvent(final AssignmentGradeRecord gradeRecord, final String graderId, final GradebookAssignment assignment, final Session session) {
		if (gradeRecord == null || assignment == null) {
			throw new IllegalArgumentException("null gradeRecord or assignment passed to logAssignmentGradingEvent");
		}
		
		// Logger the grading event, and keep track of the students with saved/updated grades
		// we need to log what the user entered depending on the grade entry type
		final Gradebook gradebook = assignment.getGradebook();
		String gradeEntry = null;
		if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
			gradeEntry = gradeRecord.getLetterEarned();
		} else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
			if (gradeRecord.getPercentEarned() != null) {
				gradeEntry = gradeRecord.getPercentEarned().toString();
			}
		} else {
			if (gradeRecord.getPointsEarned() != null) {
				gradeEntry = gradeRecord.getPointsEarned().toString();
		}
		}
		
		session.save(new GradingEvent(assignment, graderId, gradeRecord.getStudentId(), gradeEntry));
	}

}
