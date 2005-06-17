/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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

package org.sakaiproject.tool.gradebook.business.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.GradingEvents;
import org.sakaiproject.tool.gradebook.business.FacadeUtils;
import org.sakaiproject.tool.gradebook.business.GradableObjectManager;
import org.sakaiproject.tool.gradebook.business.GradeManager;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Enrollment;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * Manages GradeRecord persistence via hibernate.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class GradeManagerHibernateImpl extends HibernateDaoSupport implements GradeManager {
    private static final Log log = LogFactory.getLog(GradeManagerHibernateImpl.class);

    private GradableObjectManager gradableObjectManager;
    private Authn authn;

	/**
	 */
	public List getPointsEarnedSortedGradeRecords(GradableObject go) {
        // If this is a removed assignment, don't look for any grade records (this should never happen)
        if(go.isRemoved()) {
            log.warn(FacadeUtils.getUserUid(authn) + " attemped to get grade records for a removed assignment: " +
                    go + ".  Returning an empty list.");
            return new ArrayList();
        }

        List records = getHibernateTemplate().find(
            "from AbstractGradeRecord as agr where agr.gradableObject.id=? order by agr.pointsEarned",
            go.getId(), Hibernate.LONG);
        // If this is a course grade, calculate the point totals for the grade records
        if(go.isCourseGrade()) {
            Double totalPoints = ((CourseGrade)go).getTotalPoints();
            if(logger.isDebugEnabled()) logger.debug("Total points = " + totalPoints);
            for(Iterator iter = records.iterator(); iter.hasNext();) {
                CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
                if(logger.isDebugEnabled()) logger.debug("Points earned = " + cgr.getPointsEarned());
                Double autoCalc = new Double(cgr.getPointsEarned().doubleValue() / totalPoints.doubleValue() * 100);
                cgr.setAutoCalculatedGrade(autoCalc);
            }
        }

        return records;
	}

	/**
	 */
	public List getPointsEarnedSortedGradeRecords(final GradableObject go, final Collection studentUids) {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
				// If this is a removed assignment, don't look for any grade records (this should never happen)
				if(go.isRemoved()) {
					log.warn(FacadeUtils.getUserUid(authn) + " attemped to get grade records for a removed assignment: " +
							go + ".  Returning an empty list.");
					return new ArrayList();
				}

                if(studentUids == null || studentUids.size() == 0) {
                    if(logger.isInfoEnabled()) logger.info("Returning no grade records for an empty collection of student UIDs");
                    return new ArrayList();
                }

				Query q = session.createQuery("from AbstractGradeRecord as agr where agr.gradableObject.id=:gradableObjectId and agr.studentId in (:studentUids) order by agr.pointsEarned");
				q.setLong("gradableObjectId", go.getId().longValue());
				q.setParameterList("studentUids", studentUids);
				List records = q.list();

				// If this is a course grade, calculate the point totals for the grade records
				if(go.isCourseGrade()) {
					Double totalPoints = ((CourseGrade)go).getTotalPoints();
					if(logger.isDebugEnabled()) logger.debug("Total points = " + totalPoints);
					for(Iterator iter = records.iterator(); iter.hasNext();) {
						CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
						if(logger.isDebugEnabled()) logger.debug("Points earned = " + cgr.getPointsEarned());
                        if(cgr.getPointsEarned() != null) {
                            Double autoCalc = new Double(cgr.getPointsEarned().doubleValue() / totalPoints.doubleValue() * 100);
                            cgr.setAutoCalculatedGrade(autoCalc);
                        }
					}
				}

		        return records;
			}
		};
        return (List)getHibernateTemplate().execute(hc);
	}

    /**
     */
    public List getPointsEarnedSortedAllGradeRecords(final Long gradebookId) {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
				Query q = session.createQuery("from AbstractGradeRecord as agr where agr.gradableObject.removed=false and " +
						"agr.gradableObject.gradebook.id=:gradebookId order by agr.pointsEarned");
				q.setLong("gradebookId", gradebookId.longValue());
				return q.list();
            }
        };
        return (List)getHibernateTemplate().execute(hc);
    }

    /**
     */
    public List getPointsEarnedSortedAllGradeRecords(final Long gradebookId, final Collection studentUids) {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                if(studentUids.size() == 0) {
                    // If there are no enrollments, no need to execute the query.
                    if(logger.isInfoEnabled()) logger.info("No enrollments were specified.  Returning an empty List of grade records");
                    return new ArrayList();
                } else {
                    Query q = session.createQuery("from AbstractGradeRecord as agr where agr.gradableObject.removed=false and " +
                            "agr.gradableObject.gradebook.id=:gradebookId and agr.studentId in (:studentUids) order by agr.pointsEarned");
                    q.setLong("gradebookId", gradebookId.longValue());
                    q.setParameterList("studentUids", studentUids);
                    return q.list();
                }
            }
        };
        return (List)getHibernateTemplate().execute(hc);
    }

    /**
	 */
	public List getAllGradeRecords(final Long gradebookId) {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery("from AbstractGradeRecord as agr where agr.gradableObject.removed=false and agr.gradableObject.gradebook.id=:gradebookId");
                q.setLong("gradebookId", gradebookId.longValue());
                return q.list();
            }
        };
        return (List)getHibernateTemplate().execute(hc);
	}

   /**
     * @return Returns set of student UIDs who were given scores higher than the assignment's value.
     */
    public Set updateAssignmentGradeRecords(final Long assignmentId, final Map studentsToGrades)
        throws StaleObjectModificationException {
		return (Set)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Assignment assignment = (Assignment)session.load(Assignment.class, assignmentId);
				Date now = new Date();
				Gradebook gb = assignment.getGradebook();
				String graderId = FacadeUtils.getUserUid(authn);
				Set studentsWithUpdatedAssignmentGradeRecords = new HashSet();

				Set studentsWithExcessiveScores = new HashSet();

				// Find the records for this gradable object
				String hql = "from AssignmentGradeRecord as gr where gr.gradableObject=?";
				List allGradeRecords = session.find(hql, assignment, Hibernate.entity(Assignment.class));

                for(Iterator mapIter = studentsToGrades.keySet().iterator(); mapIter.hasNext();) {
                    // Get the student's userUid and grade from the studentsToGrades map
                    String studentId = (String)mapIter.next();
                    Number gradeNumber = (Number)studentsToGrades.get(studentId);
                    Double grade = (gradeNumber != null) ? new Double(gradeNumber.doubleValue()) : null;

                    AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)findStudentGradeRecord(studentId, allGradeRecords);

                    if (gradeRecord != null) {
						 // If the grade hasn't changed, just move on
						 if (gradeRecord.getPointsEarned() == null && grade == null) {
							 continue;
						 }
						 if (gradeRecord.getPointsEarned() != null && grade != null && gradeRecord.getPointsEarned().equals(grade)) {
							 continue;
						 }

						 // Since we are updating a student's grade in an assignment,
						 // keep track of the student so we can update his/her course grade record
						 studentsWithUpdatedAssignmentGradeRecords.add(studentId);

						 gradeRecord.setPointsEarned(grade);
                         gradeRecord.setGraderId(graderId);
                         gradeRecord.setDateRecorded(now);
                         if(logger.isDebugEnabled()) logger.debug(graderId + " is updating grade record " + gradeRecord);
                         session.update(gradeRecord);
                    } else if (grade == null) {
                         continue; // If there is no existing grade record, and there is no new grade, don't do anything
					} else {
						// We are adding an assignment grade record, so keep track of this student so we can update the course grade record
						studentsWithUpdatedAssignmentGradeRecords.add(studentId);

						gradeRecord = new AssignmentGradeRecord(assignment, studentId, graderId, grade);
						if(logger.isDebugEnabled()) logger.debug(graderId + " is saving grade record " + gradeRecord);
						session.save(gradeRecord);
                    }

                    // Check for excessive (AKA extra credit) scoring.
                    if ((grade != null) && (grade.compareTo(assignment.getPointsPossible()) > 0)) {
                    	studentsWithExcessiveScores.add(studentId);
                    }

                    // Log the grading event
                    session.save(new GradingEvent(assignment, graderId, studentId, grade));
                }

                // Update the course grade records for students with assignment grade record changes
            	recalculateCourseGradeRecords(gb, studentsWithUpdatedAssignmentGradeRecords);

                return studentsWithExcessiveScores;
			}
		});
    }

    /**
     */
    public void updateCourseGradeRecords(final Long gradebookId, final Map studentsToGrades)
        throws StaleObjectModificationException {
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				CourseGrade courseGrade = (CourseGrade)session.find("from CourseGrade as cg where cg.gradebook.id=?",
					gradebookId, Hibernate.LONG).get(0);
				Date now = new Date();
				Gradebook gb = courseGrade.getGradebook();
				String graderId = FacadeUtils.getUserUid(authn);

				// Find the number of points possible in this gradebook
				double totalPointsPossibleInGradebook = gradableObjectManager.getTotalPoints(gb.getId());

                // Find the records for this gradable object
                String hql = "from CourseGradeRecord as gr where gr.gradableObject=?";
                List allGradeRecords = session.find(hql, courseGrade, Hibernate.entity(CourseGrade.class));

                for(Iterator mapIter = studentsToGrades.keySet().iterator(); mapIter.hasNext();) {
                    // Get the student's userUid and grade from the studentsToGrades map
                    String studentId = (String)mapIter.next();
                    String grade = StringUtils.trimToNull((String)studentsToGrades.get(studentId));

                    // Find this student's grade record
                    CourseGradeRecord gradeRecord = (CourseGradeRecord)findStudentGradeRecord(studentId, allGradeRecords);

                    if(gradeRecord != null) {
                        // Update the existing record

						// If the entered grade hasn't changed, just move on
						if(gradeRecord.getEnteredGrade() == null && grade == null) {
							continue;
						}
						if(gradeRecord.getEnteredGrade() != null && grade != null && gradeRecord.getEnteredGrade().equals(grade)) {
							continue;
						}

						// Update the entered grade
						gradeRecord.setEnteredGrade(grade);

						// Update the sort grade
						if(grade == null) {
							gradeRecord.setSortGrade(gradeRecord.calculatePercent(totalPointsPossibleInGradebook));
						} else {
							gradeRecord.setSortGrade(gb.getSelectedGradeMapping().getValue(grade));
						}

						gradeRecord.setGraderId(graderId);
						gradeRecord.setDateRecorded(now);
                        if(logger.isDebugEnabled()) logger.debug(graderId + " is updating grade record " + gradeRecord);
                        session.update(gradeRecord);
                    } else {
                        if (grade == null) {
                            continue; // If there is no existing grade record, and there is no new grade, don't do anything
                        } else {
                            // Save the new course grade record
                            gradeRecord = new CourseGradeRecord(courseGrade, studentId, graderId, grade);

							// Set the sort grade
							gradeRecord.setSortGrade(gb.getSelectedGradeMapping().getValue(grade));

                            if(logger.isDebugEnabled()) logger.debug(graderId + " is saving grade record " + gradeRecord);
                            session.save(gradeRecord);
                        }
                    }
                    // Log the grading event
                    session.save(new GradingEvent(courseGrade, graderId, studentId, grade));
                }

                return null;
            }
        });
    }

    private AbstractGradeRecord findStudentGradeRecord(String studentId, List allGradeRecords) {
		AbstractGradeRecord gradeRecord = null;
		for(Iterator grIter = allGradeRecords.iterator(); grIter.hasNext();) {
			AbstractGradeRecord tmp = (AbstractGradeRecord)grIter.next();
			if(tmp.getStudentId().equals(studentId)) {
				gradeRecord = tmp;
				break;
			}
		}
		return gradeRecord;
    }

    /**
     */
    public void recalculateCourseGradeRecords(final Gradebook gradebook,
    		final Collection studentIds) {
        if(logger.isDebugEnabled()) logger.debug("Recalculating " + studentIds.size() + " course grade records");

        HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
                List assignments = gradableObjectManager.getAssignments(gradebook.getId());
                String graderId = FacadeUtils.getUserUid(authn);
                for(Iterator studentIter = studentIds.iterator(); studentIter.hasNext();) {
                    String studentId = (String)studentIter.next();

                    // TODO Run performance test: get all grade records and deal with them in memory vs. multiple queries

                    List gradeRecords = getStudentGradeRecords(gradebook.getId(), studentId, session);
                    CourseGrade cg = gradableObjectManager.getCourseGrade(gradebook.getId());

                    // Find the course grade record, if it exists
                    CourseGradeRecord cgr = getCourseGradeRecord(gradebook, studentId, session);
                    if(cgr == null) {
                        cgr = new CourseGradeRecord(cg, studentId, graderId, null);
                    }

                    // Calculate and update the total points and sort grade fields
                    cgr.calculateTotalPointsEarned(gradeRecords);
                    if(cgr.getEnteredGrade() == null) {
                        cgr.setSortGrade(cg.calculateCourseGrade(studentId, assignments, gradeRecords));
                    } else {
                        cgr.setSortGrade(gradebook.getSelectedGradeMapping().getValue(cgr.getEnteredGrade()));
                    }
                    session.saveOrUpdate(cgr);
                }
				return null;
			}
        };
        getHibernateTemplate().execute(hc);
    }


    /**
     */
    public boolean isExplicitlyEnteredCourseGradeRecords(final Long gradebookId) {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                String hql = "select count(cgr) from CourseGradeRecord as cgr where cgr.enteredGrade is not null and cgr.gradableObject.gradebook.id=?";
                Integer total = (Integer)session.iterate(hql, gradebookId, Hibernate.LONG).next();
                if (log.isInfoEnabled()) log.info("total number of explicitly entered course grade records = " + total);
                return total;
            }
        };
        return ((Integer)getHibernateTemplate().execute(hc)).intValue() > 0;
    }

    public boolean isEnteredAssignmentScores(final Long assignmentId) {
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				String hql = "select count(agr) from AssignmentGradeRecord as agr where agr.gradableObject.id=? and agr.pointsEarned is not null";
                Integer total = (Integer)session.iterate(hql, assignmentId, Hibernate.LONG).next();
                if (log.isInfoEnabled()) log.info("assignment " + assignmentId + " has " + total + " entered scores");
                return total;
            }
        };
        return ((Integer)getHibernateTemplate().execute(hc)).intValue() > 0;
    }

	/**
	 */
	public List getStudentGradeRecords(final Long gradebookId, final String studentId) {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                return getStudentGradeRecords(gradebookId, studentId, session);
            }
        };
        return (List)getHibernateTemplate().execute(hc);
	}

    /**
     * Gets all grade records for a student.
     *
     * @param studentId The student ID
     * @param session The hibernate session
     * @return A List of grade records
     *
     * @throws HibernateException
     */
    private List getStudentGradeRecords(Long gradebookId, String studentId, Session session) throws HibernateException {
        return session.find("from AbstractGradeRecord as agr where agr.studentId=? and " +
                "agr.gradableObject.removed=false and agr.gradableObject.gradebook.id=?",
                new Object[] {studentId, gradebookId}, new Type[] {Hibernate.STRING, Hibernate.LONG});
    }

	public CourseGradeRecord getStudentCourseGradeRecord(final Gradebook gradebook, final String studentId) {
		return (CourseGradeRecord)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getCourseGradeRecord(gradebook, studentId, session);
			}
		});
	}

    /**
     * Gets the course grade record for a student, or null if it does not yet exist.
     *
     * @param studentId The student ID
     * @param session The hibernate session
     * @return A List of grade records
     *
     * @throws HibernateException
     */
    private CourseGradeRecord getCourseGradeRecord(Gradebook gradebook,
            String studentId, Session session) throws HibernateException {
        List list = session.find("from CourseGradeRecord as cgr where cgr.studentId=? and cgr.gradableObject.gradebook=?",
                new Object[] {studentId, gradebook}, new Type[] {Hibernate.STRING, Hibernate.entity(gradebook.getClass())});
        if (list.size() == 0) {
        	return null;
        } else {
            return (CourseGradeRecord)list.get(0);
        }
    }

    /**
     * @see org.sakaiproject.tool.gradebook.business.GradeManager#getGradingEvents(org.sakaiproject.tool.gradebook.Gradebook, java.util.Collection)
     */
    public GradingEvents getGradingEvents(final GradableObject gradableObject, final Collection enrollments) {
        
        // Don't attempt to run the query if there are no enrollments
        if(enrollments == null || enrollments.size() == 0) {
            log.debug("No enrollments were specified.  Returning an empty GradingEvents object");
            return new GradingEvents();
        }
        
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                // Construct a set of student ids from the enrollments
                Set studentIds = new HashSet();
                for(Iterator iter = enrollments.iterator(); iter.hasNext();) {
                    studentIds.add(((Enrollment)iter.next()).getUser().getUserUid());
                }

                Query q = session.createQuery("from GradingEvent as ge where ge.gradableObject=:go and ge.studentId in (:students)");
                q.setParameter("go", gradableObject, Hibernate.entity(GradableObject.class));
                q.setParameterList("students", studentIds);
                return q.list();
            }
        };
        
        List list = (List)getHibernateTemplate().execute(hc);

        GradingEvents events = new GradingEvents();
        
        for(Iterator iter = list.iterator(); iter.hasNext();) {
            GradingEvent event = (GradingEvent)iter.next();
            events.addEvent(event);
        }
        return events;
    }

	/**
	 */
	public void setGradableObjectManager(GradableObjectManager gradableObjectManager) {
        this.gradableObjectManager = gradableObjectManager;
	}

	/**
	 */
	public void setAuthn(Authn authn) {
        this.authn = authn;
	}

}
/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
