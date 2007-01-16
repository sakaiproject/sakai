/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://www.opensource.org/licenses/ecl1.php
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.component.gradebook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookFrameworkService;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * A Hibernate implementation of GradebookService.
 */
public class GradebookServiceHibernateImpl extends BaseHibernateManager implements GradebookService {
    private static final Log log = LogFactory.getLog(GradebookServiceHibernateImpl.class);
    
    private GradebookFrameworkService frameworkService;
    private GradebookExternalAssessmentService externalAssessmentService;

    private Authz authz;

	public boolean isAssignmentDefined(final String gradebookUid, final String assignmentName)
        throws GradebookNotFoundException {
		if (!isUserAbleToViewAssignments(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to check for assignment " + assignmentName);
			throw new SecurityException("You do not have permission to perform this operation");
		}
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

	public boolean isUserAbleToGradeStudent(String gradebookUid, String studentUid) {
		return getAuthz().isUserAbleToGradeStudent(gradebookUid, studentUid);
	}

	public List getAssignments(String gradebookUid)
		throws GradebookNotFoundException {
		if (!isUserAbleToViewAssignments(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to get assignments list");
			throw new SecurityException("You do not have permission to perform this operation");
		}

		final Long gradebookId = getGradebook(gradebookUid).getId();

        List internalAssignments = (List)getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                return getAssignments(gradebookId, session);
            }
        });

		List assignments = new ArrayList();
		for (Iterator iter = internalAssignments.iterator(); iter.hasNext(); ) {
			Assignment assignment = (Assignment)iter.next();
			assignments.add(new AssignmentImpl(assignment));
		}
		return assignments;
	}

	public Double getAssignmentScore(final String gradebookUid, final String assignmentName, final String studentUid)
		throws GradebookNotFoundException, AssessmentNotFoundException {
		if (!isUserAbleToGradeStudent(gradebookUid, studentUid)) {
			log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to retrieve grade for student " + studentUid);
			throw new SecurityException("You do not have permission to perform this operation");
		}

		Double assignmentScore = (Double)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Assignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentName, session);
				if (assignment == null) {
					throw new AssessmentNotFoundException("There is no assignment named " + assignmentName + " in gradebook " + gradebookUid);
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
		return assignmentScore;
	}

	public void setAssignmentScore(final String gradebookUid, final String assignmentName, final String studentUid, final Double score, final String clientServiceDescription)
		throws GradebookNotFoundException, AssessmentNotFoundException {
		if (!isUserAbleToGradeStudent(gradebookUid, studentUid)) {
			log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to grade student " + studentUid + " from " + clientServiceDescription);
			throw new SecurityException("You do not have permission to perform this operation");
		}

		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Assignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentName, session);
				if (assignment == null) {
					throw new AssessmentNotFoundException("There is no assignment named " + assignmentName + " in gradebook " + gradebookUid);
				}
				if (assignment.isExternallyMaintained()) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to grade externally maintained assignment " + assignmentName + " from " + clientServiceDescription);
					throw new SecurityException("You do not have permission to perform this operation");
				}

				Date now = new Date();
				String graderId = getAuthn().getUserUid();
				AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid, session);
				if (gradeRecord == null) {
					// Creating a new grade record.
					gradeRecord = new AssignmentGradeRecord(assignment, studentUid, score);
				} else {
					gradeRecord.setPointsEarned(score);
				}
				gradeRecord.setGraderId(graderId);
				gradeRecord.setDateRecorded(now);
				session.saveOrUpdate(gradeRecord);
				// Sync database.
				session.flush();
				session.clear();
				return null;
			}
		});

		if (log.isInfoEnabled()) log.info("Score updated in gradebookUid=" + gradebookUid + ", assignmentName=" + assignmentName + " by userUid=" + getUserUid() + " from client=" + clientServiceDescription + ", new score=" + score);
	}

    public Authz getAuthz() {
        return authz;
    }
    public void setAuthz(Authz authz) {
        this.authz = authz;
    }

    // Deprecated calls to new framework-specific interface.
    
	public void addGradebook(String uid, String name) {
		frameworkService.addGradebook(uid, name);
	}
	public void setAvailableGradingScales(Collection gradingScaleDefinitions) {
		frameworkService.setAvailableGradingScales(gradingScaleDefinitions);
	}
	public void setDefaultGradingScale(String uid) {
		frameworkService.setDefaultGradingScale(uid);
	}
	public void deleteGradebook( String uid)
		throws GradebookNotFoundException {
		frameworkService.deleteGradebook(uid);
	}
    public boolean isGradebookDefined(String gradebookUid) {
        return frameworkService.isGradebookDefined(gradebookUid);
    }

	public GradebookFrameworkService getFrameworkService() {
		return frameworkService;
	}
	public void setFrameworkService(GradebookFrameworkService frameworkService) {
		this.frameworkService = frameworkService;
	}
	
	// Deprecated calls to new interface for external assessment engines.

	public void addExternalAssessment(String gradebookUid, String externalId, String externalUrl,
			String title, double points, Date dueDate, String externalServiceDescription)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException, GradebookNotFoundException {
		externalAssessmentService.addExternalAssessment(gradebookUid, externalId, externalUrl, title, points, dueDate, externalServiceDescription);
	}
    public void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl,
                                         String title, double points, Date dueDate) throws GradebookNotFoundException, AssessmentNotFoundException,AssignmentHasIllegalPointsException {
    	externalAssessmentService.updateExternalAssessment(gradebookUid, externalId, externalUrl, title, points, dueDate);
	}
	public void removeExternalAssessment(String gradebookUid,
            String externalId) throws GradebookNotFoundException, AssessmentNotFoundException {
		externalAssessmentService.removeExternalAssessment(gradebookUid, externalId);
	}
	public void updateExternalAssessmentScore(String gradebookUid, String externalId,
			String studentUid, Double points) throws GradebookNotFoundException, AssessmentNotFoundException {
		externalAssessmentService.updateExternalAssessmentScore(gradebookUid, externalId, studentUid, points);
	}
	public void updateExternalAssessmentScores(String gradebookUid, String externalId, Map studentUidsToScores)
		throws GradebookNotFoundException, AssessmentNotFoundException {
		externalAssessmentService.updateExternalAssessmentScores(gradebookUid, externalId, studentUidsToScores);
	}
	public boolean isExternalAssignmentDefined(String gradebookUid, String externalId) throws GradebookNotFoundException {
		return externalAssessmentService.isExternalAssignmentDefined(gradebookUid, externalId);
	}
	
	public GradebookExternalAssessmentService getExternalAssessmentService() {
		return externalAssessmentService;
	}
	public void setExternalAssessmentService(
			GradebookExternalAssessmentService externalAssessmentService) {
		this.externalAssessmentService = externalAssessmentService;
	}

}

