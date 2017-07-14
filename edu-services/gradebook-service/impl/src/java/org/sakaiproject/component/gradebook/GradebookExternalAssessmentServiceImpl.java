/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.CommentDefinition;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.ExternalAssignmentProvider;
import org.sakaiproject.service.gradebook.shared.ExternalAssignmentProviderCompat;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.InvalidCategoryException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.facades.EventTrackingService;
import org.sakaiproject.util.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class GradebookExternalAssessmentServiceImpl extends BaseHibernateManager implements GradebookExternalAssessmentService {
	private static final Logger log = LoggerFactory.getLogger(GradebookExternalAssessmentServiceImpl.class);
    // Special logger for data contention analysis.
    private static final Logger logData = LoggerFactory.getLogger(GradebookExternalAssessmentServiceImpl.class.getName() + ".GB_DATA");

    private GradebookService gradebookService;
    private EventTrackingService eventTrackingService;
	
    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public void setGradebookService(GradebookService gradebookService) {
        this.gradebookService = gradebookService;
    }

    public GradebookService getGradebookService() {
        return (GradebookService) ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
    }

	private ConcurrentHashMap<String, ExternalAssignmentProvider> externalProviders =
			new ConcurrentHashMap<String, ExternalAssignmentProvider>();

    public ConcurrentHashMap<String, ExternalAssignmentProvider> getExternalAssignmentProviders() {
        if (externalProviders == null) {
            externalProviders = new ConcurrentHashMap<String, ExternalAssignmentProvider>(0);
        }
        return externalProviders;
    }

    // Mapping of providers to their getAllExternalAssignments(String gradebookUid) methods,
    // used to allow the method to be called on providers not declaring the Compat interface.
    // This is to allow the same code to be used on 2.9 and beyond, where the secondary interface
    // may be removed, without build profiles.
    private ConcurrentHashMap<ExternalAssignmentProvider, Method> providerMethods =
        new ConcurrentHashMap<ExternalAssignmentProvider, Method>();

    /* (non-Javadoc)
     * @see org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService#registerExternalAssignmentProvider(org.sakaiproject.service.gradebook.shared.ExternalAssignmentProvider)
     */
    public void registerExternalAssignmentProvider(ExternalAssignmentProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("provider cannot be null");
        } else {
            getExternalAssignmentProviders().put(provider.getAppKey(), provider);

            // Try to duck-type the provider so it doesn't have to declare the Compat interface.
            // TODO: Remove this handling once the Compat interface has been merged or the issue is otherwise resolved.
            if (!(provider instanceof ExternalAssignmentProviderCompat)) {
                try {
                    Method m = provider.getClass().getDeclaredMethod("getAllExternalAssignments", String.class);
                    if (m.getReturnType().equals(List.class)) {
                        providerMethods.put(provider, m);
                    }
                } catch (Exception e) {
                    log.warn("ExternalAssignmentProvider [" + provider.getAppKey() + " / " + provider.getClass().toString()
                            + "] does not implement getAllExternalAssignments. It will not be able to exclude items from student views/grades. "
                            + "See the ExternalAssignmentProviderCompat interface and SAK-23733 for details.");
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService#unregisterExternalAssignmentProvider(java.lang.String)
     */
    public void unregisterExternalAssignmentProvider(String providerAppKey) {
        if (providerAppKey == null || "".equals(providerAppKey)) {
            throw new IllegalArgumentException("providerAppKey must be set");
        } else if (getExternalAssignmentProviders().containsKey(providerAppKey)) {
            ExternalAssignmentProvider provider = getExternalAssignmentProviders().get(providerAppKey);
            providerMethods.remove(provider);
            getExternalAssignmentProviders().remove(providerAppKey);
        }
    }


    public void init() {
        log.info("INIT");
    }

    public void destroy() {
        log.info("DESTROY");
        if (externalProviders != null) {
            externalProviders.clear();
            externalProviders = null;
        }
    }


    /**
     * Property in sakai.properties used to allow this service to update scores in the db every
     * time the update method is called. By default, scores are only updated if the
     * score is different than what is currently in the db.
     */
    public static final String UPDATE_SAME_SCORE_PROP = "gradebook.externalAssessments.updateSameScore";

	public void addExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl,
			final String title, final double points, final Date dueDate, final String externalServiceDescription)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException, GradebookNotFoundException {

        // Ensure that the required strings are not empty
        if(StringUtils.trimToNull(externalServiceDescription) == null ||
                StringUtils.trimToNull(externalId) == null ||
                StringUtils.trimToNull(title) == null) {
            throw new RuntimeException("External service description, externalId, and title must not be empty");
        }

        // Ensure that points is > zero
        if(points <= 0) {
            throw new AssignmentHasIllegalPointsException("Points must be > 0");
        }

        // Ensure that the assessment name is unique within this gradebook
		if (isAssignmentDefined(gradebookUid, title)) {
            throw new ConflictingAssignmentNameException("An assignment with that name already exists in gradebook uid=" + gradebookUid);
        }
		
		// name cannot start with * or # as they are reserved for special columns in import/export
        if(StringUtils.startsWithAny(title, new String[]{"*", "#"})) {
            // TODO InvalidAssignmentNameException plus move all exceptions to their own package
        	throw new ConflictingAssignmentNameException("Assignment names cannot start with * or # as they are reserved");
        }

		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				// Ensure that the externalId is unique within this gradebook
				List conflictList = (List)session.createQuery(
					"select asn from Assignment as asn where asn.externalId=? and asn.gradebook.uid=?").
					setString(0, externalId).
					setString(1, gradebookUid).list();
				Integer externalIdConflicts = conflictList.size();
				if (externalIdConflicts.intValue() > 0) {
					throw new ConflictingExternalIdException("An external assessment with ID=" + externalId + " already exists in gradebook uid=" + gradebookUid);
				}

				// Get the gradebook
				Gradebook gradebook = getGradebook(gradebookUid);

				// Create the external assignment
				Assignment asn = new Assignment(gradebook, title, Double.valueOf(points), dueDate);
				asn.setExternallyMaintained(true);
				asn.setExternalId(externalId);
				asn.setExternalInstructorLink(externalUrl);
				asn.setExternalStudentLink(externalUrl);
				asn.setExternalAppName(externalServiceDescription);
                //set released to be true to support selective release
                asn.setReleased(true);
        asn.setUngraded(false);

                session.save(asn);
				return null;
			}
		});
        if (log.isInfoEnabled()) log.info("External assessment added to gradebookUid=" + gradebookUid + ", externalId=" + externalId + " by userUid=" + getUserUid() + " from externalApp=" + externalServiceDescription);
	}

	/**
	 * @see org.sakaiproject.service.gradebook.shared.GradebookService#updateExternalAssessment(java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, java.util.Date)
     */
    public void updateExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl,
                                         final String title, final double points, final Date dueDate) throws GradebookNotFoundException, AssessmentNotFoundException,AssignmentHasIllegalPointsException {
        final Assignment asn = getExternalAssignment(gradebookUid, externalId);

        if(asn == null) {
            throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
        }

        // Ensure that points is > zero
        if(points <= 0) {
            throw new AssignmentHasIllegalPointsException("Points must be > 0");
        }

        // Ensure that the required strings are not empty
        if( StringUtils.trimToNull(externalId) == null ||
                StringUtils.trimToNull(title) == null) {
            throw new RuntimeException("ExternalId, and title must not be empty");
        }
        
        // name cannot start with * or # as they are reserved for special columns in import/export
        if(StringUtils.startsWithAny(title, new String[]{"*", "#"})) {
            // TODO InvalidAssignmentNameException plus move all exceptions to their own package
        	throw new ConflictingAssignmentNameException("Assignment names cannot start with * or # as they are reserved");
        }

        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                asn.setExternalInstructorLink(externalUrl);
                asn.setExternalStudentLink(externalUrl);
                asn.setName(title);
                asn.setDueDate(dueDate);
                //support selective release
                asn.setReleased(true);
                asn.setPointsPossible(Double.valueOf(points));
                session.update(asn);
                if (log.isInfoEnabled()) log.info("External assessment updated in gradebookUid=" + gradebookUid + ", externalId=" + externalId + " by userUid=" + getUserUid());
                return null;

            }
        };
        getHibernateTemplate().execute(hc);
	}

	/**
	 * @see org.sakaiproject.service.gradebook.shared.GradebookService#removeExternalAssessment(java.lang.String, java.lang.String)
	 */
	public void removeExternalAssessment(final String gradebookUid,
            final String externalId) throws GradebookNotFoundException, AssessmentNotFoundException {
        // Get the external assignment
        final Assignment asn = getExternalAssignment(gradebookUid, externalId);
        if(asn == null) {
            throw new AssessmentNotFoundException("There is no external assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
        }

        // We need to go through Spring's HibernateTemplate to do
        // any deletions at present. See the comments to deleteGradebook
        // for the details.
        HibernateTemplate hibTempl = getHibernateTemplate();
        
        List toBeDeletedEvents = hibTempl.find("from GradingEvent as ge where ge.gradableObject=?", asn);
        int numberDeletedEvents = toBeDeletedEvents.size();
        hibTempl.deleteAll(toBeDeletedEvents);
        if (logData.isDebugEnabled()) logData.debug("Deleted " + numberDeletedEvents + "records from gb_grading_event_t");

        List toBeDeleted = hibTempl.find("from AssignmentGradeRecord as agr where agr.gradableObject=?", asn);
        int numberDeleted = toBeDeleted.size();
        hibTempl.deleteAll(toBeDeleted);
        if (log.isInfoEnabled()) log.info("Deleted " + numberDeleted + " externally defined scores");

        toBeDeleted = hibTempl.find( "from Comment as c where c.gradableObject = ?", asn );
        hibTempl.deleteAll( toBeDeleted );
        if( log.isInfoEnabled() )
        {
            log.info( "Deleted " + toBeDeleted.size() + " externally defined score comments" );
        }

        // Delete the assessment.
		hibTempl.flush();
		hibTempl.clear();
		hibTempl.delete(asn);

        if (log.isInfoEnabled()) log.info("External assessment removed from gradebookUid=" + gradebookUid + ", externalId=" + externalId + " by userUid=" + getUserUid());
	}

    private Assignment getExternalAssignment(final String gradebookUid, final String externalId) throws GradebookNotFoundException {
        final Gradebook gradebook = getGradebook(gradebookUid);

        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
				return session.createQuery(
					"from Assignment as asn where asn.gradebook=? and asn.externalId=?").
					setEntity(0, gradebook).
					setString(1, externalId).
					uniqueResult();
            }
        };
        return (Assignment)getHibernateTemplate().execute(hc);
    }

	
    public void updateExternalAssessmentComments(final String gradebookUid, final String externalId, final Map<String, String> studentUidsToComments)
    		throws GradebookNotFoundException, AssessmentNotFoundException {
    	//TODO DO	
    	final Assignment asn = getExternalAssignment(gradebookUid, externalId);
    	if (asn == null) {
    		throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
    	}
    	final Set studentIds = studentUidsToComments.keySet();
    	if (studentIds.isEmpty()) {
    		return;
    	}
    	final Date now = new Date();
    	final String graderId = getUserUid();

    	getHibernateTemplate().execute(new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException {
    			List existingScores;
    			if (studentIds.size() <= MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST) {
    				Query q = session.createQuery("from AssignmentGradeRecord as gr where gr.gradableObject=:go and gr.studentId in (:studentIds)");
    				q.setParameter("go", asn);
    				q.setParameterList("studentIds", studentIds);
    				existingScores = q.list();
    			} else {
    				Query q = session.createQuery("from AssignmentGradeRecord as gr where gr.gradableObject=:go");
    				q.setParameter("go", asn);
    				existingScores = filterGradeRecordsByStudents(q.list(), studentIds);
    			}

    			Set changedStudents = new HashSet();
    			for (Iterator iter = existingScores.iterator(); iter.hasNext(); ) {
    				AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
    				String studentUid = agr.getStudentId();

    				// Try to reduce data contention by only updating when a score
    				// has changed or property has been set forcing a db update every time.
    				boolean alwaysUpdate = ServerConfigurationService.getBoolean(UPDATE_SAME_SCORE_PROP, false);

    				CommentDefinition gradeComment = getAssignmentScoreComment(gradebookUid, asn.getId(), studentUid);
    				String oldComment = gradeComment != null ? gradeComment.getCommentText() : null;
    				String newComment = (String) studentUidsToComments.get(studentUid);

    				if ( alwaysUpdate || (newComment != null && !newComment.equals(oldComment)) || (newComment == null && oldComment != null) ) {
    					changedStudents.add(studentUid);
    					setAssignmentScoreComment(gradebookUid, asn.getId(), studentUid, newComment);
    				}
    			}

    			if (log.isDebugEnabled()) log.debug("updateExternalAssessmentScores sent " + studentIds.size() + " records, actually changed " + changedStudents.size());

    			// Sync database.
    			session.flush();
    			session.clear();
    			return null;
    		}
    	});
    }

	public void updateExternalAssessmentScores(final String gradebookUid, final String externalId, final Map<String, Double> studentUidsToScores)
	throws GradebookNotFoundException, AssessmentNotFoundException {

      final Assignment assignment = getExternalAssignment(gradebookUid, externalId);
      if (assignment == null) {
          throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
      }
	final Set studentIds = studentUidsToScores.keySet();
	if (studentIds.isEmpty()) {
		return;
	}
	final Date now = new Date();
	final String graderId = getUserUid();

	getHibernateTemplate().execute(new HibernateCallback() {
		public Object doInHibernate(Session session) throws HibernateException {
			List existingScores;
			if (studentIds.size() <= MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST) {
				Query q = session.createQuery("from AssignmentGradeRecord as gr where gr.gradableObject=:go and gr.studentId in (:studentIds)");
				q.setParameter("go", assignment);
				q.setParameterList("studentIds", studentIds);
				existingScores = q.list();
			} else {
				Query q = session.createQuery("from AssignmentGradeRecord as gr where gr.gradableObject=:go");
				q.setParameter("go", assignment);
				existingScores = filterGradeRecordsByStudents(q.list(), studentIds);
			}

			Set previouslyUnscoredStudents = new HashSet(studentIds);
			Set changedStudents = new HashSet();
			for (Iterator iter = existingScores.iterator(); iter.hasNext(); ) {
				AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
				String studentUid = agr.getStudentId();
				previouslyUnscoredStudents.remove(studentUid);

				// Try to reduce data contention by only updating when a score
				// has changed or property has been set forcing a db update every time.
				boolean alwaysUpdate = ServerConfigurationService.getBoolean(UPDATE_SAME_SCORE_PROP, false);
				
				Double oldPointsEarned = agr.getPointsEarned();
				Double newPointsEarned = (Double)studentUidsToScores.get(studentUid);
				if ( alwaysUpdate || (newPointsEarned != null && !newPointsEarned.equals(oldPointsEarned)) || (newPointsEarned == null && oldPointsEarned != null) ) {
					agr.setDateRecorded(now);
					agr.setGraderId(graderId);
					agr.setPointsEarned(newPointsEarned);
					session.update(agr);
					changedStudents.add(studentUid);
					postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, newPointsEarned);
				}
			}
			for (Iterator iter = previouslyUnscoredStudents.iterator(); iter.hasNext(); ) {
				String studentUid = (String)iter.next();

				// Don't save unnecessary null scores.
				Double newPointsEarned = (Double)studentUidsToScores.get(studentUid);
				if (newPointsEarned != null) {
					AssignmentGradeRecord agr = new AssignmentGradeRecord(assignment, studentUid, newPointsEarned);
					agr.setDateRecorded(now);
					agr.setGraderId(graderId);
					session.save(agr);
					changedStudents.add(studentUid);
					postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, newPointsEarned);
				}
			}

			if (log.isDebugEnabled()) log.debug("updateExternalAssessmentScores sent " + studentIds.size() + " records, actually changed " + changedStudents.size());

			// Sync database.
			session.flush();
			session.clear();
              return null;
          }
      });
	}

	public void updateExternalAssessmentScoresString(final String gradebookUid, final String externalId, final Map<String, String> studentUidsToScores)
		throws GradebookNotFoundException, AssessmentNotFoundException {

		final Assignment assignment = getExternalAssignment(gradebookUid, externalId);
		if (assignment == null) {
			throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
		}
		final Set studentIds = studentUidsToScores.keySet();
		if (studentIds.isEmpty()) {
			return;
		}
		final Date now = new Date();
		final String graderId = getUserUid();

		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				List existingScores;
				if (studentIds.size() <= MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST) {
					Query q = session.createQuery("from AssignmentGradeRecord as gr where gr.gradableObject=:go and gr.studentId in (:studentIds)");
					q.setParameter("go", assignment);
					q.setParameterList("studentIds", studentIds);
					existingScores = q.list();
				} else {
					Query q = session.createQuery("from AssignmentGradeRecord as gr where gr.gradableObject=:go");
					q.setParameter("go", assignment);
					existingScores = filterGradeRecordsByStudents(q.list(), studentIds);
				}

				Set previouslyUnscoredStudents = new HashSet(studentIds);
				Set changedStudents = new HashSet();
				for (Iterator iter = existingScores.iterator(); iter.hasNext(); ) {
					AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
					String studentUid = agr.getStudentId();
					previouslyUnscoredStudents.remove(studentUid);

					// Try to reduce data contention by only updating when a score
					// has changed or property has been set forcing a db update every time.
	                boolean alwaysUpdate = ServerConfigurationService.getBoolean(UPDATE_SAME_SCORE_PROP, false);
					
	                //TODO: for ungraded items, needs to set ungraded-grades later...
					Double oldPointsEarned = agr.getPointsEarned();
					//Double newPointsEarned = (Double)studentUidsToScores.get(studentUid);
					String newPointsEarnedString = (String)studentUidsToScores.get(studentUid);
					Double newPointsEarned = (newPointsEarnedString == null) ? null : convertStringToDouble(newPointsEarnedString); 
					if ( alwaysUpdate || (newPointsEarned != null && !newPointsEarned.equals(oldPointsEarned)) || (newPointsEarned == null && oldPointsEarned != null) ) {
						agr.setDateRecorded(now);
						agr.setGraderId(graderId);
						if(newPointsEarned != null)
							agr.setPointsEarned(Double.valueOf(newPointsEarned));
						else
							agr.setPointsEarned(null);
						session.update(agr);
						changedStudents.add(studentUid);
                		postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, newPointsEarned);
					}
				}
				for (Iterator iter = previouslyUnscoredStudents.iterator(); iter.hasNext(); ) {
					String studentUid = (String)iter.next();

					// Don't save unnecessary null scores.
					String newPointsEarned = (String)studentUidsToScores.get(studentUid);
					if (newPointsEarned != null) {
						AssignmentGradeRecord agr = new AssignmentGradeRecord(assignment, studentUid, convertStringToDouble(newPointsEarned));
						agr.setDateRecorded(now);
						agr.setGraderId(graderId);
						session.save(agr);
						changedStudents.add(studentUid);
						postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, convertStringToDouble(newPointsEarned));
					}
				}

				if (log.isDebugEnabled()) log.debug("updateExternalAssessmentScores sent " + studentIds.size() + " records, actually changed " + changedStudents.size());

				// Sync database.
				session.flush();
				session.clear();
				return null;
			}
		});
	}

	public boolean isAssignmentDefined(final String gradebookUid, final String assignmentName)
        throws GradebookNotFoundException {
        Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, assignmentName, session);
			}
		});
        return (assignment != null);
    }

	/* (non-Javadoc)
	 * @see org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService#isExternalAssignmentDefined(java.lang.String, java.lang.String)
	 */
	public boolean isExternalAssignmentDefined(String gradebookUid, String externalId) throws GradebookNotFoundException {
        // SAK-19668
        Assignment assignment = getExternalAssignment(gradebookUid, externalId);
        return (assignment != null);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService#isExternalAssignmentGrouped(java.lang.String, java.lang.String)
	 */
	public boolean isExternalAssignmentGrouped(String gradebookUid, String externalId)
		throws GradebookNotFoundException
	{
        // SAK-19668
		final Assignment assignment = getExternalAssignment(gradebookUid, externalId);
		// If we check all available providers for an existing, externally maintained assignment
		// and none manage it, return false since grouping is the outlier case and all items
		// showed for all users until the 2.9 release.
		boolean result = false;
		if (assignment == null) {
            result = false;
            log.info("No assignment found for external assignment check: gradebookUid="+gradebookUid+", externalId="+externalId);
		} else {
	        for (ExternalAssignmentProvider provider : getExternalAssignmentProviders().values()) {
	            if (provider.isAssignmentDefined(externalId)) {
	                result = provider.isAssignmentGrouped(externalId);
	            }
	        }
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService#isExternalAssignmentVisible(java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean isExternalAssignmentVisible(String gradebookUid, String externalId, String userId)
		throws GradebookNotFoundException
	{
	    // SAK-19668
		final Assignment assignment = getExternalAssignment(gradebookUid, externalId);
		// If we check all available providers for an existing, externally maintained assignment
		// and none manage it, assume that it should be visible. This matches the pre-2.9 behavior
		// when a provider is not implemented to handle the assignment. Also, any provider that
		// returns true will allow access (logical OR of responses).
		boolean result = false;
		boolean providerResponded = false;
		if (assignment == null) {
		    result = false;
			log.info("No assignment found for external assignment check: gradebookUid="+gradebookUid+", externalId="+externalId);
		} else {
			for (ExternalAssignmentProvider provider : getExternalAssignmentProviders().values()) {
				if (provider.isAssignmentDefined(externalId)) {
					providerResponded = true;
					result = result || provider.isAssignmentVisible(externalId, userId);
				}
			}
		}
		return result || !providerResponded;
	}

	public Map<String, String> getExternalAssignmentsForCurrentUser(String gradebookUid)
		throws GradebookNotFoundException
	{
		final Gradebook gradebook = getGradebook(gradebookUid);

		Map<String, String> visibleAssignments = new HashMap<String, String>();
		Set<String> providedAssignments = getProvidedExternalAssignments(gradebookUid);

		for (ExternalAssignmentProvider provider : getExternalAssignmentProviders().values()) {
			String appKey = provider.getAppKey();
			List<String> assignments = provider.getExternalAssignmentsForCurrentUser(gradebookUid);
			for (String externalId : assignments) {
				visibleAssignments.put(externalId, appKey);
			}
		}

		// We include those items that the gradebook has marked as externally maintained, but no provider has
		// identified as items under its authority. This maintains the behavior prior to the grouping support
		// introduced for the 2.9 release (SAK-11485 and SAK-19688), where a tool that does not have a provider
		// implemented does not have its items filtered for student views and grading.
		List<org.sakaiproject.service.gradebook.shared.Assignment> gbAssignments = getGradebookService().getViewableAssignmentsForCurrentUser(gradebookUid);
		for (org.sakaiproject.service.gradebook.shared.Assignment assignment : gbAssignments) {
			String id = assignment.getExternalId();
			if (assignment.isExternallyMaintained() && !providedAssignments.contains(id) && !visibleAssignments.containsKey(id)) {
				if (log.isDebugEnabled()) {
					log.debug(String.format("External assignment in gradebook [%s] is not handled by a provider; ID: %s", gradebookUid, id));
				}
				visibleAssignments.put(id, null);
			}
		}

		return visibleAssignments;
	}

	protected Set<String> getProvidedExternalAssignments(String gradebookUid) {
		Set<String> allAssignments = new HashSet<String>();
		for (ExternalAssignmentProvider provider : getExternalAssignmentProviders().values()) {
			// TODO: This is a temporary cast; if this method proves to be the right fit
			//       and perform well enough, it will be moved to the regular interface.
			if (provider instanceof ExternalAssignmentProviderCompat) {
				allAssignments.addAll(
						((ExternalAssignmentProviderCompat) provider).getAllExternalAssignments(gradebookUid));
			} else if (providerMethods.containsKey(provider)) {
				Method m = providerMethods.get(provider);
				try {
					List<String> reflectedAssignments = (List<String>) m.invoke(provider, gradebookUid);
					allAssignments.addAll(reflectedAssignments);
				} catch (Exception e) {
					log.debug("Exception calling getAllExternalAssignments", e);
				}
			}
		}
		return allAssignments;
	}

	public Map<String, List<String>> getVisibleExternalAssignments(String gradebookUid, Collection<String> studentIds)
		throws GradebookNotFoundException
	{
		final Gradebook gradebook = getGradebook(gradebookUid);

		Set<String> providedAssignments = getProvidedExternalAssignments(gradebookUid);

		Map<String, Set<String>> visible = new HashMap<String, Set<String>>();
		for (String studentId : studentIds) {
			visible.put(studentId, new HashSet<String>());
		}

		for (ExternalAssignmentProvider provider : getExternalAssignmentProviders().values()) {
			//SAK-24407 - Some tools modify this set so we can't pass it. I considered making it an unmodifableCollection but that would require changing a number of tools
			Set studentIdsCopy = new HashSet(studentIds);
			Map<String, List<String>> externals = provider.getAllExternalAssignments(gradebookUid, (studentIdsCopy));
			for (String studentId : externals.keySet()) {
				if (visible.containsKey(studentId)) {
					visible.get(studentId).addAll(externals.get(studentId));
				}
			}
		}

		// SAK-23733 - This covers a tricky case where items that the gradebook thinks are external
		//             but are not reported by any provider should be included for everyone. This is
		//             to accommodate tools that use the external assessment mechanisms but have not
		//             implemented an ExternalAssignmentProvider.
		List<org.sakaiproject.service.gradebook.shared.Assignment> allAssignments = getGradebookService().getViewableAssignmentsForCurrentUser(gradebookUid);
		for (org.sakaiproject.service.gradebook.shared.Assignment assignment : allAssignments) {
			String id = assignment.getExternalId();
			if (assignment.isExternallyMaintained() && !providedAssignments.contains(id)) {
				for (String studentId : visible.keySet()) {
					visible.get(studentId).add(id);
				}
			}
		}

		Map<String, List<String>> visibleList = new HashMap<String, List<String>>();
		for (String studentId : visible.keySet()) {
			visibleList.put(studentId, new ArrayList<String>(visible.get(studentId)));
		}
		return visibleList;
	}

	public void setExternalAssessmentToGradebookAssignment(final String gradebookUid, final String externalId) {
        final Assignment assignment = getExternalAssignment(gradebookUid, externalId);
        if (assignment == null) {
            throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
        }
        assignment.setExternalAppName(null);
        assignment.setExternalId(null);
        assignment.setExternalInstructorLink(null);
        assignment.setExternalStudentLink(null);
        assignment.setExternallyMaintained(false);
        getHibernateTemplate().execute(new HibernateCallback() {
        	public Object doInHibernate(Session session) throws HibernateException {
                session.update(assignment);
                if (log.isInfoEnabled()) log.info("Externally-managed assignment " + externalId + " moved to Gradebook management in gradebookUid=" + gradebookUid + " by userUid=" + getUserUid());
                return null;
        	}
        });
	}
	
	/**
	 * Wrapper created when category was added for assignments tool
	 */
	public void addExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl, final String title, final Double points, 
			final Date dueDate, final String externalServiceDescription, final Boolean ungraded) 
			throws GradebookNotFoundException, ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException
	{
		addExternalAssessment(gradebookUid, externalId, externalUrl, title, points, dueDate, externalServiceDescription, ungraded, null);
	}
	
	public void addExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl, final String title, final Double points, 
		final Date dueDate, final String externalServiceDescription, final Boolean ungraded, final Long categoryId) 
		throws GradebookNotFoundException, ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException
	{
		// Ensure that the required strings are not empty
		if(StringUtils.trimToNull(externalServiceDescription) == null ||
				StringUtils.trimToNull(externalId) == null ||
				StringUtils.trimToNull(title) == null) {
			throw new RuntimeException("External service description, externalId, and title must not be empty");
		}

		// Ensure that points is > zero
		if((ungraded != null && !ungraded.booleanValue() && (points == null ||  points.doubleValue() <= 0))
				|| (ungraded == null && (points == null ||  points.doubleValue() <= 0))) {
			throw new AssignmentHasIllegalPointsException("Points can't be null or Points must be > 0");
		}

		// Ensure that the assessment name is unique within this gradebook
		if (isAssignmentDefined(gradebookUid, title)) {
			throw new ConflictingAssignmentNameException("An assignment with that name already exists in gradebook uid=" + gradebookUid);
		}
		
		// name cannot start with * or # as they are reserved for special columns in import/export
        if(StringUtils.startsWithAny(title, new String[]{"*", "#"})) {
            // TODO InvalidAssignmentNameException plus move all exceptions to their own package
        	throw new ConflictingAssignmentNameException("Assignment names cannot start with * or # as they are reserved");
        }

		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				// Ensure that the externalId is unique within this gradebook
				List conflictList = (List)session.createQuery(
				"select asn from Assignment as asn where asn.externalId=? and asn.gradebook.uid=?").
				setString(0, externalId).
				setString(1, gradebookUid).list();
				Integer externalIdConflicts = conflictList.size();
				if (externalIdConflicts.intValue() > 0) {
					throw new ConflictingExternalIdException("An external assessment with that ID already exists in gradebook uid=" + gradebookUid);
				}

				// Get the gradebook
				Gradebook gradebook = getGradebook(gradebookUid);
				
				// if a category was indicated, double check that it is valid
				Category persistedCategory = null;
				if (categoryId != null) {
				    persistedCategory = getCategory(categoryId);
				    if (persistedCategory == null || persistedCategory.isRemoved() ||
				            !persistedCategory.getGradebook().getId().equals(gradebook.getId())) {
				        throw new InvalidCategoryException("The category with id " + categoryId + 
				                " is not valid for gradebook " + gradebook.getUid());
				    }
				}

				// Create the external assignment
				Assignment asn = new Assignment(gradebook, title, points, dueDate);
				asn.setExternallyMaintained(true);
				asn.setExternalId(externalId);
				asn.setExternalInstructorLink(externalUrl);
				asn.setExternalStudentLink(externalUrl);
				asn.setExternalAppName(externalServiceDescription);
				if (persistedCategory != null) { 
					asn.setCategory(persistedCategory);
				}
				//set released to be true to support selective release
				asn.setReleased(true);
				if(ungraded != null)
					asn.setUngraded(ungraded.booleanValue());
				else
					asn.setUngraded(false);

				session.save(asn);
				return null;
			}
		});
		if (log.isInfoEnabled()) log.info("External assessment added to gradebookUid=" + gradebookUid + ", externalId=" + externalId + " by userUid=" + getUserUid() + " from externalApp=" + externalServiceDescription);
	}

	public void updateExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl, final String title, final Double points, final Date dueDate, final Boolean ungraded) 
	throws GradebookNotFoundException, AssessmentNotFoundException, ConflictingAssignmentNameException, AssignmentHasIllegalPointsException
	{
    final Assignment asn = getExternalAssignment(gradebookUid, externalId);

    if(asn == null) {
        throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
    }

    // Ensure that points is > zero
		if((ungraded != null && !ungraded.booleanValue() && (points == null ||  points.doubleValue() <= 0))
				|| (ungraded == null && (points == null ||  points.doubleValue() <= 0))) {
			throw new AssignmentHasIllegalPointsException("Points can't be null or Points must be > 0");
		}

    // Ensure that the required strings are not empty
    if( StringUtils.trimToNull(externalId) == null ||
            StringUtils.trimToNull(title) == null) {
        throw new RuntimeException("ExternalId, and title must not be empty");
    }
    
    // name cannot start with * or # as they are reserved for special columns in import/export
    if(StringUtils.startsWithAny(title, new String[]{"*", "#"})) {
        // TODO InvalidAssignmentNameException plus move all exceptions to their own package
    	throw new ConflictingAssignmentNameException("Assignment names cannot start with * or # as they are reserved");
    }

    HibernateCallback hc = new HibernateCallback() {
        public Object doInHibernate(Session session) throws HibernateException {
            asn.setExternalInstructorLink(externalUrl);
            asn.setExternalStudentLink(externalUrl);
            asn.setName(title);
            asn.setDueDate(dueDate);
            //support selective release
            asn.setReleased(true);
            asn.setPointsPossible(points);
    				if(ungraded != null)
    					asn.setUngraded(ungraded.booleanValue());
    				else
    					asn.setUngraded(false);
            session.update(asn);
            if (log.isInfoEnabled()) log.info("External assessment updated in gradebookUid=" + gradebookUid + ", externalId=" + externalId + " by userUid=" + getUserUid());
            return null;

        }
    };
    getHibernateTemplate().execute(hc);
	}

	
	public void updateExternalAssessmentComment(final String gradebookUid, final String externalId, final String studentUid, final String comment) 
	throws GradebookNotFoundException, AssessmentNotFoundException {
		final Assignment asn = getExternalAssignment(gradebookUid, externalId);

		if(asn == null) {
			throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
		}

		if (logData.isDebugEnabled()) logData.debug("BEGIN: Update 1 score for gradebookUid=" + gradebookUid + ", external assessment=" + externalId + " from " + asn.getExternalAppName());

		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Date now = new Date();

				// Try to reduce data contention by only updating when the
				// score has actually changed or property has been set forcing a db update every time.
				boolean alwaysUpdate = ServerConfigurationService.getBoolean(UPDATE_SAME_SCORE_PROP, false);

				CommentDefinition gradeComment = getAssignmentScoreComment(gradebookUid, asn.getId(), studentUid);
				String oldComment = gradeComment != null ? gradeComment.getCommentText() : null;

				if ( alwaysUpdate || (comment != null && !comment.equals(oldComment)) ||
						(comment == null && oldComment != null) ) {
					if(comment != null)
						setAssignmentScoreComment(gradebookUid, asn.getId(), studentUid, comment);
					else
						setAssignmentScoreComment(gradebookUid, asn.getId(), studentUid, null);
				}
				return null;
			}
		};
		getHibernateTemplate().execute(hc);
		if (logData.isDebugEnabled()) logData.debug("END: Update 1 score for gradebookUid=" + gradebookUid + ", external assessment=" + externalId + " from " + asn.getExternalAppName());
		if (log.isDebugEnabled()) log.debug("External assessment comment updated in gradebookUid=" + gradebookUid + ", externalId=" + externalId + " by userUid=" + getUserUid() + ", new score=" + comment);
	}
	
	public void updateExternalAssessmentScore(final String gradebookUid, final String externalId, final String studentUid, final String points) 
	throws GradebookNotFoundException, AssessmentNotFoundException
	{
		final Assignment asn = getExternalAssignment(gradebookUid, externalId);

		if(asn == null) {
			throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
		}

		if (logData.isDebugEnabled()) logData.debug("BEGIN: Update 1 score for gradebookUid=" + gradebookUid + ", external assessment=" + externalId + " from " + asn.getExternalAppName());

		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Date now = new Date();

				AssignmentGradeRecord agr = getAssignmentGradeRecord(asn, studentUid, session);

				// Try to reduce data contention by only updating when the
				// score has actually changed or property has been set forcing a db update every time.
                boolean alwaysUpdate = ServerConfigurationService.getBoolean(UPDATE_SAME_SCORE_PROP, false);
                
				//TODO: for ungraded items, needs to set ungraded-grades later...
				Double oldPointsEarned = (agr == null) ? null : agr.getPointsEarned();
				Double newPointsEarned = (points == null) ? null : convertStringToDouble(points); 
				if ( alwaysUpdate || (newPointsEarned != null && !newPointsEarned.equals(oldPointsEarned)) ||
						(newPointsEarned == null && oldPointsEarned != null) ) {
					if (agr == null) {
						if(newPointsEarned != null)
							agr = new AssignmentGradeRecord(asn, studentUid, Double.valueOf(newPointsEarned));
						else
							agr = new AssignmentGradeRecord(asn, studentUid, null);
					} else {
						if(newPointsEarned != null)
							agr.setPointsEarned(Double.valueOf(newPointsEarned));
						else
							agr.setPointsEarned(null);
					}

					agr.setDateRecorded(now);
					agr.setGraderId(getUserUid());
					if (log.isDebugEnabled()) log.debug("About to save AssignmentGradeRecord id=" + agr.getId() + ", version=" + agr.getVersion() + ", studenttId=" + agr.getStudentId() + ", pointsEarned=" + agr.getPointsEarned());
					session.saveOrUpdate(agr);

					// Sync database.
					session.flush();
					session.clear();
            		postUpdateGradeEvent(gradebookUid, asn.getName(), studentUid, newPointsEarned);
				} else {
					if(log.isDebugEnabled()) log.debug("Ignoring updateExternalAssessmentScore, since the new points value is the same as the old");
				}
				return null;
			}
		};
		getHibernateTemplate().execute(hc);
		if (logData.isDebugEnabled()) logData.debug("END: Update 1 score for gradebookUid=" + gradebookUid + ", external assessment=" + externalId + " from " + asn.getExternalAppName());
		if (log.isDebugEnabled()) log.debug("External assessment score updated in gradebookUid=" + gradebookUid + ", externalId=" + externalId + " by userUid=" + getUserUid() + ", new score=" + points);
	}
	
	private void postUpdateGradeEvent(String gradebookUid, String assignmentName, String studentUid, Double pointsEarned) {
	    if (eventTrackingService != null) {
            eventTrackingService.postEvent("gradebook.updateItemScore","/gradebook/"+gradebookUid+"/"+assignmentName+"/"+studentUid+"/"+pointsEarned+"/student");
        }
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
				// check if grade uses a comma as separator because of number format and change to a comma y the external app sends a point as separator
				DecimalFormat dcformat = (DecimalFormat) getNumberFormat();
				String decSeparator = dcformat.getDecimalFormatSymbols().getDecimalSeparator() + "";
				if (",".equals(decSeparator)) {
					doubleAsString = doubleAsString.replace(".", ",");
				}
				Number numericScore = getNumberFormat().parse(doubleAsString.trim());
				scoreAsDouble = numericScore.doubleValue();
			} catch (ParseException e) {
				log.error(e.getMessage());
			}
	    }

	    return scoreAsDouble;
	}

	private NumberFormat getNumberFormat() {
	    return NumberFormat.getInstance(new ResourceLoader().getLocale());
	}
	
	public Long getExternalAssessmentCategoryId(String gradebookUId, String externalId) {
		Long categoryId = null;
		final Assignment assignment = getExternalAssignment(gradebookUId, externalId);
		if (assignment == null) {
			throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUId);
		}
		if (assignment.getCategory() != null) {
			categoryId = assignment.getCategory().getId();
		}
		return categoryId;
	}

}
