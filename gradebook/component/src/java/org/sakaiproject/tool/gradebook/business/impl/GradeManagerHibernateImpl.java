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

package org.sakaiproject.tool.gradebook.business.impl;

import java.sql.SQLException;
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

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.StaleObjectStateException;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.GradeRecordSet;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.GradingEvents;
import org.sakaiproject.tool.gradebook.business.FacadeUtils;
import org.sakaiproject.tool.gradebook.business.GradeManager;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.CourseManagement;
import org.sakaiproject.tool.gradebook.facades.Enrollment;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.HibernateOptimisticLockingFailureException;

/**
 * Manages GradeRecord and GradableObject persistence via hibernate.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class GradeManagerHibernateImpl extends BaseHibernateManager implements GradeManager {
    private static final Log log = LogFactory.getLog(GradeManagerHibernateImpl.class);

	/**
	 */
	public List getPointsEarnedSortedGradeRecords(GradableObject go) {
        List records = getHibernateTemplate().find(
            "from AbstractGradeRecord as agr where agr.gradableObject.removed=false and agr.gradableObject.id=? order by agr.pointsEarned",
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
                if(studentUids == null || studentUids.size() == 0) {
                    if(logger.isInfoEnabled()) logger.info("Returning no grade records for an empty collection of student UIDs");
                    return new ArrayList();
                }

				Query q = session.createQuery("from AbstractGradeRecord as agr where agr.gradableObject.removed=false and agr.gradableObject.id=:gradableObjectId and agr.studentId in (:studentUids) order by agr.pointsEarned");
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

        HibernateCallback hc = new HibernateCallback() {
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
                try {
                    session.flush();
                    session.clear();
                } catch (StaleObjectStateException sose) {
                    if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update assignment grade records");
                    throw new StaleObjectModificationException(sose);
                }

                // Update the course grade records for students with assignment grade record changes
                recalculateCourseGradeRecords(gb, studentsWithUpdatedAssignmentGradeRecords, session);

                return studentsWithExcessiveScores;
			}
		};

        return (Set)getHibernateTemplate().execute(hc);
    }

    /**
     */
    public void updateCourseGradeRecords(final GradeRecordSet gradeRecordSet)
        throws StaleObjectModificationException {

        if(gradeRecordSet.getAllGradeRecords().size() == 0) {
            log.debug("updateCourseGradeRecords called with zero grade records to update");
            return;
        }

        HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
                for(Iterator iter = gradeRecordSet.getAllGradeRecords().iterator(); iter.hasNext();) {
                    session.evict(iter.next());
                }

				CourseGrade courseGrade = (CourseGrade)gradeRecordSet.getGradableObject();
				Date now = new Date();
				Gradebook gb = courseGrade.getGradebook();
				String graderId = FacadeUtils.getUserUid(authn);

				// Find the number of points possible in this gradebook
				double totalPointsPossibleInGradebook = getTotalPoints(gb.getId());

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
                    try {
                        session.saveOrUpdate(gradeRecordFromCall);
                        session.flush();
                    } catch (StaleObjectStateException sose) {
                        if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update course grade records");
                        throw new StaleObjectModificationException(sose);
                    }

                    // Log the grading event
                    session.save(new GradingEvent(courseGrade, graderId, gradeRecordFromCall.getStudentId(), gradeRecordFromCall.getEnteredGrade()));
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

	public CourseGradeRecord getStudentCourseGradeRecord(final Gradebook gradebook, final String studentId) {
		return (CourseGradeRecord)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getCourseGradeRecord(gradebook, studentId, session);
			}
		});
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

	public void setAuthn(Authn authn) {
        this.authn = authn;
	}


    ///////////////////////////////////////////////////////////////
    // Consolidated from GradeManagerHibernateImpl.java //
    ///////////////////////////////////////////////////////////////


    /**
     * @param courseManagement The courseManagement to set.
     */
    public void setCourseManagement(CourseManagement courseManagement) {
        this.courseManagement = courseManagement;
    }

    /**
     */
    public List getAssignments(final Long gradebookId, final String sortBy, final boolean ascending) {
        return (List)getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                List assignments = getAssignments(gradebookId, session);
                sortAssignments(assignments, sortBy, ascending);
                return assignments;
            }
        });
    }

    /**
     */
    public List getAssignmentsWithStats(final Long gradebookId, final Collection studentUids, final String sortBy, final boolean ascending) {
    	List assignments;
    	if (studentUids.isEmpty()) {
    		// Hibernate 2.1.8 generates invalid SQL if an empty collection is used
    		// as a parameter list.
    		assignments = getAssignments(gradebookId, sortBy, ascending);
    	} else {
    		assignments = (List)getHibernateTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					// TODO Combine queries for efficiency.
					Query q = session.createQuery(
						"from AbstractGradeRecord as gr where gr.gradableObject.gradebook.id=:gradebookId and gr.gradableObject.removed=false and gr.studentId in (:studentUids)");
					q.setLong("gradebookId", gradebookId.longValue());
					q.setParameterList("studentUids", studentUids);
					List gradeRecords = q.list();

					List assignments = getAssignments(gradebookId, sortBy, ascending);

					// Calculate and insert the statistics into the assignments
					for(Iterator asnIter = assignments.iterator(); asnIter.hasNext();) {
						Assignment asn = (Assignment)asnIter.next();
						asn.calculateStatistics(gradeRecords, studentUids.size());
					}
					sortAssignments(assignments, sortBy, ascending);
					return assignments;
				}
			});
		}
		return assignments;
    }

    /**
     * TODO Remove this method in favor of doing database sorting.
     *
     * @param assignments
     * @param sortBy
     * @param ascending
     */
    private void sortAssignments(List assignments, String sortBy, boolean ascending) {
        Comparator comp;
        if(Assignment.SORT_BY_NAME.equals(sortBy)) {
            comp = Assignment.nameComparator;
        } else if(Assignment.SORT_BY_MEAN.equals(sortBy)) {
            comp = Assignment.meanComparator;
        } else if(Assignment.SORT_BY_POINTS.equals(sortBy)) {
            comp = Assignment.pointsComparator;
        } else {
            comp = Assignment.dateComparator;
        }
        Collections.sort(assignments, comp);
        if(!ascending) {
            Collections.reverse(assignments);
        }
    }

    /**
     */
    public List getAssignments(Long gradebookId) {
        return getAssignments(gradebookId, Assignment.DEFAULT_SORT, true);
    }

    /**
     */
    public List getAssignmentsWithStats(Long gradebookId, Collection studentUids) {
        return getAssignmentsWithStats(gradebookId, studentUids, Assignment.DEFAULT_SORT, true);
    }

    /**
     */
    public GradableObject getGradableObject(Long gradableObjectId) {
        return (GradableObject)getHibernateTemplate().load(GradableObject.class, gradableObjectId);
    }

    /**
     */
    public GradableObject getGradableObjectWithStats(final Long gradableObjectId, final Collection studentUids) {
		GradableObject gradableObject = (GradableObject)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				List gradeRecords;
				if (studentUids.isEmpty()) {
					// Hibernate 2.1.8 generates invalid SQL if an empty collection is used
					// as a parameter list.
					gradeRecords = new ArrayList();
				} else {
					Query q = session.createQuery(
						"from AbstractGradeRecord as gr where gr.gradableObject.id=:gradableObjectId and gr.gradableObject.removed=false and gr.studentId in (:studentUids)");
					q.setLong("gradableObjectId", gradableObjectId.longValue());
					q.setParameterList("studentUids", studentUids);
					gradeRecords = q.list();
				}

				// Calculate the total points possible, along with the auto-calculated grade percentage for each grade record
				GradableObject go = (GradableObject)session.load(GradableObject.class, gradableObjectId);
				if(go.isCourseGrade()) {
					CourseGrade cg = (CourseGrade)go;
					cg.calculateTotalPointsPossible(getAssignments(go.getGradebook().getId()));
					for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
						CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
						if(cgr.getPointsEarned() != null) {
							Double autoCalc = new Double(cgr.getPointsEarned().doubleValue() /
									cg.getTotalPoints().doubleValue() * 100);
							cgr.setAutoCalculatedGrade(autoCalc);
						}
					}
				}

				go.calculateStatistics(gradeRecords, studentUids.size());
				return go;
			}
		});
		return gradableObject;
    }

    /**
     * We standardly use the HibernateCallback doInHibernate method to communicate with the DB.
     * Since we can't add declared exceptions to that method's interface, we need to use
     * a runtime exception and translate it outside of the call.
     */
    private static class RuntimeConflictingAssignmentNameException extends RuntimeException {
        public RuntimeConflictingAssignmentNameException(String message) {
            super(message);
        }
    }

    /**
     */
    public Long createAssignment(final Long gradebookId, final String name, final Double points, final Date dueDate)
        throws ConflictingAssignmentNameException, StaleObjectModificationException {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Gradebook gb = (Gradebook)(session.find(
                        "from Gradebook as gb where gb.id=?",
                        gradebookId, Hibernate.LONG).get(0));

                int numNameConflicts = ((Integer)session.iterate(
                        "select count(go) from GradableObject as go where go.name = ? and go.gradebook = ? and go.removed=false",
                        new Object[] {name, gb},
                        new Type[] {Hibernate.STRING, Hibernate.entity(Gradebook.class)}
                ).next()).intValue();

                if(numNameConflicts > 0) {
                    throw new RuntimeConflictingAssignmentNameException("You can not save multiple assignments in a gradebook with the same name");
                }

                Assignment asn = new Assignment();
                asn.setGradebook(gb);
                asn.setName(name);
                asn.setPointsPossible(points);
                asn.setDueDate(dueDate);

                // Save the new assignment
                Long id = (Long)session.save(asn);

                // Recalculate the course grades
                recalculateCourseGradeRecords(asn.getGradebook(), session);

                return id;
            }
        };

        Long newAssignmentId;
        try {
            newAssignmentId = (Long)getHibernateTemplate().execute(hc);
        } catch (RuntimeConflictingAssignmentNameException e) {
            throw new ConflictingAssignmentNameException(e.getMessage());
        }
        return newAssignmentId;
    }

    /**
     */
    public void updateAssignment(final Assignment assignment)
        throws ConflictingAssignmentNameException, StaleObjectModificationException {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                // Ensure that we don't have the assignment in the session, since
                // we need to compare the existing one in the db to our edited assignment
                session.evict(assignment);

                boolean pointsChanged = false;

                Assignment asnFromDb = (Assignment)session.load(Assignment.class, assignment.getId());

                int numNameConflicts = ((Integer)session.iterate(
                        "select count(go) from GradableObject as go where go.removed=false and go.name = ? and go.gradebook = ? and go.id != ?",
                        new Object[] {assignment.getName(), assignment.getGradebook(), assignment.getId()},
                        new Type[] {Hibernate.STRING, Hibernate.entity(Gradebook.class), Hibernate.LONG}
                ).next()).intValue();

                if(numNameConflicts > 0) {
                    throw new RuntimeConflictingAssignmentNameException("You can not save multiple assignments in a gradebook with the same name");
                }

                if(!asnFromDb.getPointsPossible().equals(assignment.getPointsPossible())) {
                    pointsChanged = true;
                }

                session.evict(asnFromDb);
                session.update(assignment);

                // Flush the session before calling on the course grade updates (we want data contention to happen here, not there)
                session.flush();
                session.clear();

                if(pointsChanged) {
                    updateCourseGradeRecordSortValues(assignment.getGradebook().getId(), false);
                }

                return null;
            }
        };
        try {
            getHibernateTemplate().execute(hc);
        } catch (RuntimeConflictingAssignmentNameException e) {
            throw new ConflictingAssignmentNameException(e.getMessage());
        } catch (HibernateOptimisticLockingFailureException holfe) {
            if(logger.isInfoEnabled()) logger.info("An optimistic locking failure occurred while attempting to update an assignment");
            throw new StaleObjectModificationException(holfe);
        }
    }

    /**
     */
    public CourseGrade getCourseGradeWithStats(Long gradebookId, Collection studentUids) {
        // TODO Combine queries for efficiency
        CourseGrade courseGrade = getCourseGrade(gradebookId);
//        courseGrade.calculateTotalPoints(getAssignments(gradebookId));
        return (CourseGrade)getGradableObjectWithStats(courseGrade.getId(), studentUids);
    }

    /**
     * TODO Should this be deleted, since it should be handled by recalculateCourseGradeRecords?
     */
    public void updateCourseGradeRecordSortValues(final Long gradebookId, final boolean manuallyEnteredRecords) {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                if(manuallyEnteredRecords) {
                    if(logger.isDebugEnabled()) logger.debug("Updating sort values on manually entered course grades");
                } else {
                    if(logger.isDebugEnabled()) logger.debug("Updating sort values on auto-calculated course grades");
                }
                Gradebook gb = (Gradebook)session.load(Gradebook.class, gradebookId);
                GradeMapping mapping = gb.getSelectedGradeMapping();

                // We'll need to get the total points in the gradebook if we're
                // resetting the sort values for non-explicitly set grade records
                // There's no need to take the performance hit of making the total
                // points query if we don't need it.
                double totalPointsPossible = 0;
                if(!manuallyEnteredRecords) {
                    totalPointsPossible = getTotalPoints(gradebookId);
                }

                StringBuffer hql = new StringBuffer("from CourseGradeRecord as cgr where cgr.enteredGrade is ");
                if(manuallyEnteredRecords) {
                    hql.append("not ");
                }
                hql.append("null and cgr.gradableObject.gradebook.id=?");

                List gradeRecords = session.find(hql.toString(), gradebookId, Hibernate.LONG);
                for(Iterator gradeRecordIterator = gradeRecords.iterator(); gradeRecordIterator.hasNext();) {
                    CourseGradeRecord cgr = (CourseGradeRecord)gradeRecordIterator.next();
                    if(manuallyEnteredRecords) {
                        cgr.setSortGrade(mapping.getValue(cgr.getEnteredGrade()));
                    } else {
                        cgr.setSortGrade(cgr.calculatePercent(totalPointsPossible));
                    }
                    session.update(cgr);
                }
                return null;
            }
        };
        getHibernateTemplate().execute(hc);
    }

    /**
     * Gets the total number of points possible in a gradebook.
     */
    public double getTotalPoints(Long gradebookId) {
        List assignmentPoints = getHibernateTemplate().find("select asn.pointsPossible from Assignment as asn where asn.removed=false and asn.gradebook.id=?", gradebookId, Hibernate.LONG);
        double totalPoints = 0;
        for(Iterator iter = assignmentPoints.iterator(); iter.hasNext();) {
            Double points = (Double)iter.next();
            totalPoints += points.doubleValue();
        }
        return totalPoints;
    }

}


