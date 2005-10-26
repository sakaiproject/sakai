/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.component.gradebook;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.GradeMapping;
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

        if(gradebookExists(uid)) {
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

    /**
     * @see org.sakaiproject.service.gradebook.shared.GradebookService#gradebookExists(java.lang.String)
     */
    public boolean gradebookExists(String gradebookUid) {
        String hql = "from Gradebook as gb where gb.uid=?";
        return getHibernateTemplate().find(hql, gradebookUid, Hibernate.STRING).size() == 1;
    }

    /**
	 * @see org.sakaiproject.service.gradebook.shared.GradebookService#addExternalAssessment(java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, java.util.Date, java.lang.String)
	 */
	public void addExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl,
			final String title, final long points, final Date dueDate, final String externalServiceDescription)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException, GradebookNotFoundException {

        // Ensure that the required strings are not empty
        if(StringUtils.trimToNull(externalServiceDescription) == null ||
                StringUtils.trimToNull(externalId) == null ||
                StringUtils.trimToNull(title) == null) {
            throw new RuntimeException("External service description, externalId, and title must not be empty");
        }

        // Ensure that points is >= zero
        if(points < 0) {
            throw new RuntimeException("Points must be >= 0");
        }

        // This issue of not being able to throw checked exceptions inside doInHibernate
        // is getting really annoying.  Perhaps we should change to runtime exceptions?

        // Ensure that the externalId is unique within this gradebook
        HibernateCallback idConflictsCallback = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                String hql = "select count(asn) from Assignment as asn where asn.externalId=? and asn.gradebook.uid=?";
                return (Integer)session.iterate(hql, new Object[] {externalId, gradebookUid}, new Type[] {Hibernate.STRING, Hibernate.STRING}).next();
            }
        };
        Integer externalIdConflicts = (Integer)getHibernateTemplate().execute(idConflictsCallback);
        if(externalIdConflicts.intValue() > 0) {
            throw new ConflictingExternalIdException("An external assessment with that ID already exists in gradebook uid=" + gradebookUid);
        }

        // Ensure that the assessment name is unique within this gradebook
        HibernateCallback nameConflictsCallback = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                String hql = "select count(asn) from Assignment as asn where asn.name=? and asn.gradebook.uid=?";
                return (Integer)session.iterate(hql, new Object[] {title, gradebookUid}, new Type[] {Hibernate.STRING, Hibernate.STRING}).next();
            }
        };
        Integer nameConflicts = (Integer)getHibernateTemplate().execute(nameConflictsCallback);
        if(nameConflicts.intValue() > 0) {
            throw new ConflictingAssignmentNameException("An assignment with that name already exists in gradebook uid=" + gradebookUid);
        }

        // Get the gradebook
        final Gradebook gradebook = gradebookManager.getGradebook(gradebookUid);

        // Create the external assignment
        final Assignment asn = new Assignment(gradebook, title, new Double(points), dueDate);
        asn.setExternallyMaintained(true);
        asn.setExternalId(externalId);
        asn.setExternalInstructorLink(externalUrl);
        asn.setExternalStudentLink(externalUrl);
        asn.setExternalAppName(externalServiceDescription);

        getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                session.save(asn);
                recalculateCourseGradeRecords(gradebook, getSession());
                return null;
            }});
        if (log.isInfoEnabled()) log.info("External assessment added to gradebookUid=" + gradebookUid + ", externalId=" + externalId + " by userUid=" + getUserUid() + " from externalApp=" + externalServiceDescription);
	}

	/**
	 * @see org.sakaiproject.service.gradebook.shared.GradebookService#updateExternalAssessment(java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, java.util.Date)
	 */
	public void updateExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl,
			final String title, final long points, final Date dueDate) throws GradebookNotFoundException, AssessmentNotFoundException {
        final Assignment asn = getExternalAssignment(gradebookUid, externalId);

        if(asn == null) {
            throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
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
                return new Boolean(updateCourseGradeSortScore);
            }
        };
        Boolean performSortUpdate = (Boolean)getHibernateTemplate().execute(hc);
        if(performSortUpdate.booleanValue()) {
            gradeManager.updateCourseGradeRecordSortValues(gradebookManager.getGradebook(gradebookUid).getId(), false);
        }
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
                log.warn(numScoresDeleted + " externally defined scores deleted from the gradebook");

                // Delete the assessment
                session.delete(asn);

                // Delete the scores
                try {
                    session.flush();
                    session.clear();
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
			final String studentId, final Double points) throws GradebookNotFoundException, AssessmentNotFoundException {

        final Assignment asn = getExternalAssignment(gradebookUid, externalId);

        if(asn == null) {
            throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
        }

        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Date now = new Date();
                String grHql = "from AssignmentGradeRecord as agr where agr.gradableObject=? and agr.studentId=?";
                List list = session.find(grHql, new Object[] {asn, studentId},
                        new Type[] {Hibernate.entity(GradableObject.class), Hibernate.STRING});
                if(list.size() == 0) {
                    AssignmentGradeRecord agr = new AssignmentGradeRecord(asn, studentId, points);
                    agr.setDateRecorded(now);
                    agr.setGraderId(getUserUid());
                    session.save(agr);
                } else {
                    AssignmentGradeRecord agr = (AssignmentGradeRecord)list.get(0);
                    agr.setDateRecorded(now);
                    agr.setGraderId(getUserUid());
                    agr.setPointsEarned(points);
                    session.update(agr);
                }
                Gradebook gradebook = asn.getGradebook();
                Set set = new HashSet();
                set.add(studentId);
                try {
                    session.flush();
                    session.clear();
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
}

