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

import java.util.*;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;

import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.FacadeUtils;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.CourseManagement;

/**
 * Base class for all gradebook managers.  Provides methods common to two or more
 * managers, thus eliminating (for now) a tangle of circular dependencies.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public abstract class BaseHibernateManager extends HibernateDaoSupport {
    private static final Log log = LogFactory.getLog(BaseHibernateManager.class);

    protected CourseManagement courseManagement;
    protected Authn authn;

    protected List getAssignments(Long gradebookId, Session session) throws HibernateException {
        String hql = "from Assignment as asn where asn.gradebook.id=? and asn.removed=false";
        List assignments = session.find(hql,
            new Object[] {gradebookId},
            new Type[] {Hibernate.LONG});
        return assignments;
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
    protected List getStudentGradeRecords(Long gradebookId, String studentId, Session session) throws HibernateException {
        return session.find("from AssignmentGradeRecord as agr where agr.studentId=? and agr.gradableObject.removed=false and agr.gradableObject.gradebook.id=?",
                new Object[] {studentId, gradebookId}, new Type[] {Hibernate.STRING, Hibernate.LONG});
    }

    /**
     */
    public CourseGrade getCourseGrade(Long gradebookId) {
        return (CourseGrade)getHibernateTemplate().find(
                "from CourseGrade as cg where cg.gradebook.id=?",
                gradebookId, Hibernate.LONG).get(0);
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
    protected CourseGradeRecord getCourseGradeRecord(Gradebook gradebook,
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
     * Recalculates the course grade records for the specified set of students.
     * This should be called any time the total number of points possible in a
     * gradebook is modified, either by editing, adding, or removing assignments
     * or external assessments.
     *
     * You must flush and clear the hibernate session prior to calling this method,
     * or you risk causing data contention here.  If data contention does occur
     * here, you will be unable to catch the exception (due to the spring proxy
     * mechanism).
     *
     * TODO Clean up optimistic locking difficulties in recalculate grades
     *
     * @param gradebook The gradebook containing the course grade records to update
     * @param studentIds The collection of student IDs
     * @param session The hibernate session
     */
    protected void recalculateCourseGradeRecords(final Gradebook gradebook,
            final Collection studentIds, Session session) throws HibernateException {
        if(logger.isDebugEnabled()) logger.debug("Recalculating " + studentIds.size() + " course grade records");

        List assignments = getAssignments(gradebook.getId(), session);
        String graderId = FacadeUtils.getUserUid(authn);
        Date now = new Date();
        for(Iterator studentIter = studentIds.iterator(); studentIter.hasNext();) {
            String studentId = (String)studentIter.next();

            // TODO Run performance test: get all grade records and deal with them in memory vs. multiple queries

            List gradeRecords = getStudentGradeRecords(gradebook.getId(), studentId, session);
            CourseGrade cg = getCourseGrade(gradebook.getId());

            // Find the course grade record, if it exists
            CourseGradeRecord cgr = getCourseGradeRecord(gradebook, studentId, session);
            if(cgr == null) {
                cgr = new CourseGradeRecord(cg, studentId, null);
                cgr.setGraderId(graderId);
                cgr.setDateRecorded(now);
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
    }

    /**
     * Recalculates the course grade records for all students in a gradebook.
     * This should be called any time the total number of points possible in a
     * gradebook is modified, either by editing, adding, or removing assignments
     * or external assessments.
     *
     * @param gradebook
     * @param session
     * @throws HibernateException
     */
    protected void recalculateCourseGradeRecords(Gradebook gradebook, Session session) throws HibernateException {
		// Need to fix any data contention before calling the recalculation.
		session.flush();
		session.clear();

        Set enrollments = courseManagement.getEnrollments(gradebook.getUid());
        Set studentIds = new HashSet();
        for(Iterator iter = enrollments.iterator(); iter.hasNext();) {
            studentIds.add(((EnrollmentRecord)iter.next()).getUser().getUserUid());
        }
        recalculateCourseGradeRecords(gradebook, studentIds, session);
    }

    public void setAuthn(Authn authn) {
        this.authn = authn;
    }
    public void setCourseManagement(CourseManagement courseManagement) {
        this.courseManagement = courseManagement;
    }
}
