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
import java.util.HashMap;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.GradeRecordSet;
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
    public Set updateAssignmentGradeRecords(final GradeRecordSet gradeRecordSet)
        throws StaleObjectModificationException {

        final Collection gradeRecordsFromCall = gradeRecordSet.getAllGradeRecords();
        final Set studentIds = gradeRecordSet.getAllStudentIds();
        
        // If no grade records are sent, don't bother doing anything with the db
        if(gradeRecordsFromCall.size() == 0) {
            log.debug("updateAssignmentGradeRecords called for zero grade records");
            return new HashSet();
        }
        
        return (Set)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
                // Evict the grade records from the session so we can check for point changes
                for(Iterator iter = gradeRecordsFromCall.iterator(); iter.hasNext();) {
                    session.evict(iter.next());
                }
                
                Assignment assignment = (Assignment)session.load(Assignment.class, gradeRecordSet.getGradableObject().getId());
				Date now = new Date();
				Gradebook gb = assignment.getGradebook();
				String graderId = FacadeUtils.getUserUid(authn);

                Set studentsWithUpdatedAssignmentGradeRecords = new HashSet();
				Set studentsWithExcessiveScores = new HashSet();

				String hql = "select gr.studentId, gr.pointsEarned from AssignmentGradeRecord as gr where gr.gradableObject=:go and gr.studentId in (:studentIds)";
                Query q = session.createQuery(hql);
                q.setParameter("go", assignment);
                q.setParameterList("studentIds", studentIds);
				List persistentGradeRecords = q.list();
                
				// Construct a map of student id to persistent grade record scores
                Map scoreMap = new HashMap();
                for(Iterator iter = persistentGradeRecords.iterator(); iter.hasNext();) {
                    Object[] oa = (Object[])iter.next();
                    scoreMap.put(oa[0], oa[1]);
                }

                for(Iterator iter = gradeRecordsFromCall.iterator(); iter.hasNext();) {
                    // Keep track of whether this grade record needs to be updated
                    boolean performUpdate = false;

                    AssignmentGradeRecord gradeRecordFromCall = (AssignmentGradeRecord)iter.next();
                    if(scoreMap.containsKey(gradeRecordFromCall.getStudentId())) {
                        // The student already has a grade record, only perform an update if the grade has changed
                        Double pointsInDb = (Double)scoreMap.get(gradeRecordFromCall.getStudentId());
                        
                        if( (pointsInDb != null && !pointsInDb.equals(gradeRecordFromCall.getPointsEarned())) ||
                                (pointsInDb == null && gradeRecordFromCall.getPointsEarned() != null)) {
                            // The grade record's value has changed
                            gradeRecordFromCall.setGraderId(graderId);
                            gradeRecordFromCall.setDateRecorded(now);
                            session.update(gradeRecordFromCall);
                            performUpdate = true;
                        }
                    } else {
                        // This is a new grade record
                        if(gradeRecordFromCall.getPointsEarned() != null) {
                            gradeRecordFromCall.setGraderId(graderId);
                            gradeRecordFromCall.setDateRecorded(now);
                            session.save(gradeRecordFromCall);
                            performUpdate = true;
                        }
                    }

                    // Check for excessive (AKA extra credit) scoring.
                    if (performUpdate &&
                            gradeRecordFromCall.getPointsEarned() != null &&
                            gradeRecordFromCall.getPointsEarned().compareTo(assignment.getPointsPossible()) > 0) {
                    	studentsWithExcessiveScores.add(gradeRecordFromCall.getStudentId());
                    }

                    // Log the grading event, and keep track of the students with saved/updated grades
                    if(performUpdate) {
                        session.save(new GradingEvent(assignment, graderId, gradeRecordFromCall.getStudentId(), gradeRecordFromCall.getPointsEarned()));
                        studentsWithUpdatedAssignmentGradeRecords.add(gradeRecordFromCall.getStudentId());
                    }
                }

                // Update the course grade records for students with assignment grade record changes
            	recalculateCourseGradeRecords(gb, studentsWithUpdatedAssignmentGradeRecords);

                return studentsWithExcessiveScores;
			}
		});
    }

    /**
     */
    public void updateCourseGradeRecords(final GradeRecordSet gradeRecordSet)
        throws StaleObjectModificationException {
        
        if(gradeRecordSet.getAllGradeRecords().size() == 0) {
            log.debug("updateCourseGradeRecords called with zero grade records to update");
            return;
        }

        getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
                for(Iterator iter = gradeRecordSet.getAllGradeRecords().iterator(); iter.hasNext();) {
                    session.evict(iter.next());
                }
                
				CourseGrade courseGrade = (CourseGrade)gradeRecordSet.getGradableObject();
				Date now = new Date();
				Gradebook gb = courseGrade.getGradebook();
				String graderId = FacadeUtils.getUserUid(authn);

				// Find the number of points possible in this gradebook
				double totalPointsPossibleInGradebook = gradableObjectManager.getTotalPoints(gb.getId());

                // Find the grade records for these students on this gradable object
                String hql = "select gr.studentId, gr.enteredGrade from CourseGradeRecord as gr where gr.gradableObject=:go and gr.studentId in (:studentIds)";
                Query q = session.createQuery(hql);
                q.setParameter("go", courseGrade);
                q.setParameterList("studentIds", gradeRecordSet.getAllStudentIds());
                List persistentGradeRecords = q.list();

                // Construct a map of student id to persistent grade record scores
                Map scoreMap = new HashMap();
                for(Iterator iter = persistentGradeRecords.iterator(); iter.hasNext();) {
                    Object[] oa = (Object[])iter.next();
                    scoreMap.put(oa[0], oa[1]);
                }
                
                for(Iterator iter = gradeRecordSet.getAllGradeRecords().iterator(); iter.hasNext();) {
                    // The possibly modified course grade record
                    CourseGradeRecord gradeRecordFromCall = (CourseGradeRecord)iter.next();
                    
                    // The entered grade in the db for this grade record
                    String grade = (String)scoreMap.get(gradeRecordFromCall.getStudentId());

                    // Update the existing record

					// If the entered grade hasn't changed, just move on
					if(gradeRecordFromCall.getEnteredGrade() == null && grade == null) {
						continue;
					}
					if(gradeRecordFromCall.getEnteredGrade() != null && grade != null && gradeRecordFromCall.getEnteredGrade().equals(grade)) {
						continue;
					}

					// Update the sort grade
					if(gradeRecordFromCall.getEnteredGrade() == null) {
						gradeRecordFromCall.setSortGrade(gradeRecordFromCall.calculatePercent(totalPointsPossibleInGradebook));
					} else {
						gradeRecordFromCall.setSortGrade(gb.getSelectedGradeMapping().getValue(gradeRecordFromCall.getEnteredGrade()));
					}

					gradeRecordFromCall.setGraderId(graderId);
					gradeRecordFromCall.setDateRecorded(now);
                    session.saveOrUpdate(gradeRecordFromCall);

                    // Log the grading event
                    session.save(new GradingEvent(courseGrade, graderId, gradeRecordFromCall.getStudentId(), gradeRecordFromCall.getEnteredGrade()));
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
