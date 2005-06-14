/**********************************************************************************
*
* $Header: /cvs/sakai2/gradebook/component/src/java/org/sakaiproject/tool/gradebook/business/impl/GradableObjectManagerHibernateImpl.java,v 1.4 2005/06/11 17:40:00 ray.media.berkeley.edu Exp $
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

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradableObjectManager;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.CourseManagement;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.HibernateOptimisticLockingFailureException;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * A Hibernate implementation of GradableObjectManager
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class GradableObjectManagerHibernateImpl extends HibernateDaoSupport
    implements GradableObjectManager {
	private static final Log logger = LogFactory.getLog(GradableObjectManagerHibernateImpl.class);

	private CourseManagement courseManagement;
    private GradebookManager gradebookManager;

	/**
	 * @param courseManagement The courseManagement to set.
	 */
	public void setCourseManagement(CourseManagement courseManagement) {
		this.courseManagement = courseManagement;
	}

	public void setGradebookManager(GradebookManager gradebookManager) {
		this.gradebookManager = gradebookManager;
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

	private List getAssignments(Long gradebookId, Session session) throws HibernateException {
		String hql = "from Assignment as asn where asn.gradebook.id=? and asn.removed=false";
        List assignments = session.find(hql,
			new Object[] {gradebookId},
			new Type[] {Hibernate.LONG});
        return assignments;
    }

    /**
     */
    public List getAssignmentsWithStats(final Long gradebookId, final String sortBy, final boolean ascending) {
		return (List)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				// TODO Combine queries for efficiency.
				List gradeRecords = getHibernateTemplate().find(
					"from AssignmentGradeRecord as gr where gr.gradableObject.gradebook.id=?" +
					" and gr.gradableObject.removed=false", gradebookId, Hibernate.LONG);
				List assignments = getAssignments(gradebookId, sortBy, ascending);

				// Calculate and insert the statistics into the assignments
				int enrollmentsSize = getEnrollmentsSize(gradebookId, session);
				for(Iterator asnIter = assignments.iterator(); asnIter.hasNext();) {
					Assignment asn = (Assignment)asnIter.next();
					asn.calculateStatistics(gradeRecords, enrollmentsSize);
				}
                sortAssignments(assignments, sortBy, ascending);
				return assignments;
			}
		});
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
    public List getAssignmentsWithStats(Long gradebookId) {
        return getAssignmentsWithStats(gradebookId, Assignment.DEFAULT_SORT, true);
    }

    private int getEnrollmentsSize(Long gradebookId, Session session) throws HibernateException {
    	String gradebookUid = ((Gradebook)session.load(Gradebook.class, gradebookId)).getUid();
    	return courseManagement.getEnrollmentsSize(gradebookUid);
    }

    /**
     */
    public GradableObject getGradableObject(Long gradableObjectId) {
        return (GradableObject)getHibernateTemplate().load(GradableObject.class, gradableObjectId);
    }

    /**
     */
    public GradableObject getGradableObjectWithStats(final Long gradableObjectId) {
		return (GradableObject)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				List gradeRecords = session.find(
					"from AbstractGradeRecord as gr where gr.gradableObject.id=?" +
					" and gr.gradableObject.removed=false", gradableObjectId, Hibernate.LONG);
				GradableObject go = (GradableObject)session.load(GradableObject.class, gradableObjectId);

                // Calculate the total points possible, along with the auto-calculated grade percentage for each grade record
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

                go.calculateStatistics(gradeRecords, getEnrollmentsSize(go.getGradebook().getId(), session));
				return go;
			}
		});
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
                return (Long)session.save(asn);
            }
        };
        Long newAssignmentId;
        try {
	        newAssignmentId = (Long)getHibernateTemplate().execute(hc);
	    } catch (RuntimeConflictingAssignmentNameException e) {
	    	throw new ConflictingAssignmentNameException(e.getMessage());
	    }
        updateCourseGradeRecordSortValues(gradebookId, false);
        return newAssignmentId;
    }

    /**
     */
    public void updateAssignment(final Long assignmentId, final String name, final Double points, final Date dueDate)
        throws ConflictingAssignmentNameException, StaleObjectModificationException {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                boolean pointsChanged = false;
                if (logger.isInfoEnabled()) logger.info("updateAssignment(" + assignmentId + ", " + name + ", " + points + ", " + dueDate);
                Assignment asn = (Assignment)session.load(Assignment.class, assignmentId);

                int numNameConflicts = ((Integer)session.iterate(
                        "select count(go) from GradableObject as go where go.removed=false and go.name = ? and go.gradebook = ? and go.id != ?",
                        new Object[] {name, asn.getGradebook(), assignmentId},
                        new Type[] {Hibernate.STRING, Hibernate.entity(Gradebook.class), Hibernate.LONG}
                ).next()).intValue();

                if(numNameConflicts > 0) {
                    throw new RuntimeConflictingAssignmentNameException("You can not save multiple assignments in a gradebook with the same name");
                }

                asn.setName(name);
                if(!asn.getPointsPossible().equals(points)) {
                    pointsChanged = true;
                }
                asn.setPointsPossible(points);
                asn.setDueDate(dueDate);
                session.update(asn);
                return new Boolean(pointsChanged);
            }
        };
        try {
			if(((Boolean)getHibernateTemplate().execute(hc)).booleanValue()) {
				Long gradebookId = ((Assignment)getHibernateTemplate().load(Assignment.class, assignmentId)).getGradebook().getId();
				updateCourseGradeRecordSortValues(gradebookId, false);
			}
	    } catch (RuntimeConflictingAssignmentNameException e) {
	    	throw new ConflictingAssignmentNameException(e.getMessage());
	    } catch (HibernateOptimisticLockingFailureException holfe) {
            throw new StaleObjectModificationException(holfe);
        }
    }

	/**
	 */
	public CourseGrade getCourseGrade(Long gradebookId) {
        return (CourseGrade)getHibernateTemplate().find(
                "from CourseGrade as cg where cg.gradebook.id=?",
                gradebookId, Hibernate.LONG).get(0);
	}

	/**
	 */
	public CourseGrade getCourseGradeWithStats(Long gradebookId) {
        // TODO Combine queries for efficiency
        CourseGrade courseGrade = getCourseGrade(gradebookId);
//        courseGrade.calculateTotalPoints(getAssignments(gradebookId));
        return (CourseGrade)getGradableObjectWithStats(courseGrade.getId());
	}

    /**
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
/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/gradebook/component/src/java/org/sakaiproject/tool/gradebook/business/impl/GradableObjectManagerHibernateImpl.java,v 1.4 2005/06/11 17:40:00 ray.media.berkeley.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
