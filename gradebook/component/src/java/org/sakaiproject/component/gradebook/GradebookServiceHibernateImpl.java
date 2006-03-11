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

import java.util.*;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.StaleObjectStateException;
import net.sf.hibernate.type.Type;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookExistsException;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.GradeRecordSet;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradeManager;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.business.impl.BaseHibernateManager;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.springframework.orm.hibernate.HibernateCallback;

/**
 * A Hibernate implementation of GradebookService, which can be used by other
 * applications to insert, modify, and remove "read-only" assignments and scores
 * in the gradebook.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class GradebookServiceHibernateImpl extends BaseHibernateManager implements GradebookService {
    private static final Log log = LogFactory.getLog(GradebookServiceHibernateImpl.class);

    private GradebookManager gradebookManager;
    private GradeManager gradeManager;
    private Authz authz;

	public void addGradebook(final String uid, final String name) {
        if(isGradebookDefined(uid)) {
            log.warn("You can not add a gradebook with uid=" + uid + ".  That gradebook already exists.");
            throw new GradebookExistsException("You can not add a gradebook with uid=" + uid + ".  That gradebook already exists.");
        }
        if (log.isInfoEnabled()) log.info("Adding gradebook uid=" + uid + " by userUid=" + getUserUid());

        getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				// Create and save the gradebook
				Gradebook gradebook = new Gradebook(name);
				gradebook.setUid(uid);
				gradebook.setId((Long)session.save(gradebook)); // Grab the new id

				// Create the course grade for the gradebook
				CourseGrade cg = new CourseGrade();
				cg.setGradebook(gradebook);
				session.save(cg);

				// According to the specification, Display Assignment Grades is
				// on by default, and Display course grade is off.
				gradebook.setAssignmentsDisplayed(true);
				gradebook.setCourseGradeDisplayed(false);

				// Add and save the grade mappings
				Set gms = gradebook.getAvailableGradeMappings();
				for(Iterator iter = gms.iterator(); iter.hasNext();) {
					GradeMapping gm = (GradeMapping)iter.next();
					gm.setGradebook(gradebook);
					gm.setDefaultValues(); // Populate the grade map
					gm.setId((Long)session.save(gm)); // grab the new id
					if(gm.isDefault()) {
						gradebook.setSelectedGradeMapping(gm);
					}
				}

				// Update the gradebook with the new selected grade mapping
				session.update(gradebook);
				return null;
			}
		});
	}

	public void deleteGradebook(final String uid)
		throws GradebookNotFoundException {
        if (log.isInfoEnabled()) log.info("Deleting gradebook uid=" + uid + " by userUid=" + getUserUid());
        final Long gradebookId = gradebookManager.getGradebook(uid).getId();
        getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				// This should be much more efficient in Hibernate 3, which
				// supports bulk deletion in HQL. In Hibernate 2, deletions happen
				// one record at a time.
				int numberDeleted = session.delete("from GradingEvent as ge where ge.gradableObject.gradebook.id=?", gradebookId, Hibernate.LONG);
				if (log.isInfoEnabled()) log.info("Deleted " + numberDeleted + " grading events");
				numberDeleted = session.delete("from AbstractGradeRecord as gr where gr.gradableObject.gradebook.id=?", gradebookId, Hibernate.LONG);
				if (log.isInfoEnabled()) log.info("Deleted " + numberDeleted + " grade records");
				numberDeleted = session.delete("from GradableObject as go where go.gradebook.id=?", gradebookId, Hibernate.LONG);
				if (log.isInfoEnabled()) log.info("Deleted " + numberDeleted + " gradable objects");

				Gradebook gradebook = (Gradebook)session.load(Gradebook.class, gradebookId);
				gradebook.setSelectedGradeMapping(null);
				numberDeleted = session.delete("from GradeMapping as gm where gm.gradebook.id=?", gradebookId, Hibernate.LONG);
				if (log.isInfoEnabled()) log.info("Deleted " + numberDeleted + " grade mappings");
				session.flush();

				session.delete(gradebook);
				session.flush();
				session.clear();
				return null;
			}
		});
	}

    /**
     * @see org.sakaiproject.service.gradebook.shared.GradebookService#isGradebookDefined(java.lang.String)
     */
    public boolean isGradebookDefined(String gradebookUid) {
        String hql = "from Gradebook as gb where gb.uid=?";
        return getHibernateTemplate().find(hql, gradebookUid, Hibernate.STRING).size() == 1;
    }

    /**
     * @see org.sakaiproject.service.gradebook.shared.GradebookService#gradebookExists(java.lang.String)
     */
    public boolean gradebookExists(String gradebookUid) {
        return isGradebookDefined(gradebookUid);
    }

    /**
	 * @see org.sakaiproject.service.gradebook.shared.GradebookService#addExternalAssessment(java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, java.util.Date, java.lang.String)
	 */
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



		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				// Ensure that the externalId is unique within this gradebook
				Integer externalIdConflicts = (Integer)session.iterate(
					"select count(asn) from Assignment as asn where asn.externalId=? and asn.gradebook.uid=?",
					new Object[] {externalId, gradebookUid},
					new Type[] {Hibernate.STRING, Hibernate.STRING}
				).next();
				if (externalIdConflicts.intValue() > 0) {
					throw new ConflictingExternalIdException("An external assessment with that ID already exists in gradebook uid=" + gradebookUid);
				}

				// Get the gradebook
				Gradebook gradebook = gradebookManager.getGradebook(gradebookUid);

				// Create the external assignment
				Assignment asn = new Assignment(gradebook, title, new Double(points), dueDate);
				asn.setExternallyMaintained(true);
				asn.setExternalId(externalId);
				asn.setExternalInstructorLink(externalUrl);
				asn.setExternalStudentLink(externalUrl);
				asn.setExternalAppName(externalServiceDescription);

				session.save(asn);
				recalculateCourseGradeRecords(gradebook, session);
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



        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                boolean updateCourseGradeSortScore = false;
                asn.setExternalInstructorLink(externalUrl);
                asn.setExternalStudentLink(externalUrl);
                asn.setName(title);
                asn.setDueDate(dueDate);
                // If the points possible changes, we need to update the course grade sort values
                if(!asn.getPointsPossible().equals(new Double(points))) {
                    updateCourseGradeSortScore = true;
                }
                asn.setPointsPossible(new Double(points));
                session.update(asn);
                if (log.isInfoEnabled()) log.info("External assessment updated in gradebookUid=" + gradebookUid + ", externalId=" + externalId + " by userUid=" + getUserUid());
                if (updateCourseGradeSortScore) {
                    recalculateCourseGradeRecords(asn.getGradebook(), session);
                }
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

        // Make sure the gradebook uid is valid.  This throws a gradebook not found exception.
        Gradebook gb = gradebookManager.getGradebook(gradebookUid);

        // Get the external assignment
        final Assignment asn = getExternalAssignment(gradebookUid, externalId);
        if(asn == null) {
            throw new AssessmentNotFoundException("There is no external assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
        }

        // Delete the assignment and all of its grade records
        HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
                String studentIdsHql = "select agr.studentId from AssignmentGradeRecord as agr where agr.gradableObject=?";
                List studentsWithExternalScores = (List)session.find(studentIdsHql, asn, Hibernate.entity(GradableObject.class));

                String deleteExternalScoresHql = "from AssignmentGradeRecord as agr where agr.gradableObject=?";
                int numScoresDeleted = session.delete(deleteExternalScoresHql, asn, Hibernate.entity(GradableObject.class));
                if (log.isInfoEnabled()) log.info(numScoresDeleted + " externally defined scores deleted from the gradebook");

                // Delete the assessment
                session.flush();
                session.clear();
                session.delete(asn);

                // Delete the scores
                try {
                    recalculateCourseGradeRecords(asn.getGradebook(), studentsWithExternalScores, session);
                } catch (StaleObjectStateException e) {
                    if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to remove an external assessment");
                    throw new StaleObjectModificationException(e);
                }
                return null;
			}
        };
        getHibernateTemplate().execute(hc);
        if (log.isInfoEnabled()) log.info("External assessment removed from gradebookUid=" + gradebookUid + ", externalId=" + externalId + " by userUid=" + getUserUid());
	}

    private Assignment getExternalAssignment(final String gradebookUid, final String externalId) throws GradebookNotFoundException {
        final Gradebook gradebook = gradebookManager.getGradebook(gradebookUid);

        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                String asnHql = "from Assignment as asn where asn.gradebook=? and asn.externalId=?";
                return session.find(asnHql, new Object[] {gradebook, externalId},
                        new Type[] {Hibernate.entity(Gradebook.class), Hibernate.STRING});
            }
        };
        List assignments = (List)getHibernateTemplate().execute(hc);
        if(assignments.size() == 1) {
            return (Assignment)assignments.get(0);
        } else {
            return null;
        }
    }

	/**
	 * @see org.sakaiproject.service.gradebook.shared.GradebookService#updateExternalAssessmentScore(java.lang.String, java.lang.String, java.lang.String, Double)
	 */
	public void updateExternalAssessmentScore(final String gradebookUid, final String externalId,
			final String studentUid, final Double points) throws GradebookNotFoundException, AssessmentNotFoundException {

        final Assignment asn = getExternalAssignment(gradebookUid, externalId);

        if(asn == null) {
            throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
        }

        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Date now = new Date();
                AssignmentGradeRecord agr = getAssignmentGradeRecord(asn, studentUid, session);
                if (agr == null) {
                	agr = new AssignmentGradeRecord(asn, studentUid, points);
                } else {
                	agr.setPointsEarned(points);
                }
				agr.setDateRecorded(now);
				agr.setGraderId(getUserUid());
                session.saveOrUpdate(agr);

                Gradebook gradebook = asn.getGradebook();
                Set set = new HashSet();
                set.add(studentUid);

				// Need to sync database before recalculating.
				session.flush();
				session.clear();
                try {
                    recalculateCourseGradeRecords(gradebook, set, session);
                } catch (StaleObjectStateException e) {
                    if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update an external score");
                    throw new StaleObjectModificationException(e);
                }
                return null;
            }
        };
        getHibernateTemplate().execute(hc);
		if (log.isDebugEnabled()) log.debug("External assessment score updated in gradebookUid=" + gradebookUid + ", externalId=" + externalId + " by userUid=" + getUserUid() + ", new score=" + points);
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

	public boolean isUserAbleToGradeStudent(String gradebookUid, String studentUid) {
		return getAuthz().isUserAbleToGradeStudent(gradebookUid, studentUid);
	}

	public List getAssignments(String gradebookUid)
		throws GradebookNotFoundException {
		Gradebook gradebook = gradebookManager.getGradebook(gradebookUid);
		List assignments = new ArrayList();
		List internalAssignments = gradeManager.getAssignments(gradebook.getId());
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

		Double assignmentScore = assignmentScore = (Double)getHibernateTemplate().execute(new HibernateCallback() {
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
				// Need to sync database before recalculating.
				session.flush();
				session.clear();

				Set set = new HashSet();
				set.add(studentUid);
				try {
					recalculateCourseGradeRecords(assignment.getGradebook(), set, session);
				} catch (StaleObjectStateException e) {
					if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while user " + graderId + " was attempting to update score for assignment " + assignment.getName() + " and student " + studentUid + " from client " + clientServiceDescription);
					throw new StaleObjectModificationException(e);
				}
				return null;
			}
		});

		if (log.isInfoEnabled()) log.info("Score updated in gradebookUid=" + gradebookUid + ", assignmentName=" + assignmentName + " by userUid=" + getUserUid() + " from client=" + clientServiceDescription + ", new score=" + score);
	}

	public GradebookManager getGradebookManager() {
		return gradebookManager;
	}
	public void setGradebookManager(GradebookManager gradebookManager) {
		this.gradebookManager = gradebookManager;
	}
	public GradeManager getGradeManager() {
		return gradeManager;
	}
	public void setGradeManager(GradeManager gradeManager) {
		this.gradeManager = gradeManager;
	}
    public Authz getAuthz() {
        return authz;
    }
    public void setAuthz(Authz authz) {
        this.authz = authz;
    }

	private Assignment getAssignmentWithoutStats(String gradebookUid, String assignmentName, Session session) throws HibernateException {
		List asns = session.find(
			"from Assignment as asn where asn.name=? and asn.gradebook.uid=? and asn.removed=false",
			new Object[] {assignmentName, gradebookUid},
			new Type[] {Hibernate.STRING, Hibernate.STRING}
		);
		if (asns.size() < 1) {
			return null;
		} else {
			return (Assignment)asns.get(0);
		}
	}

	private AssignmentGradeRecord getAssignmentGradeRecord(Assignment assignment, String studentUid, Session session) throws HibernateException {
		List scores = session.find("from AssignmentGradeRecord as agr where agr.studentId=? and agr.gradableObject.id=?",
			new Object[] {studentUid, assignment.getId()},
			new Type[] {Hibernate.STRING, Hibernate.LONG});
		if (scores.size() < 1) {
			return null;
		} else {
			return (AssignmentGradeRecord)scores.get(0);
		}
	}


}

