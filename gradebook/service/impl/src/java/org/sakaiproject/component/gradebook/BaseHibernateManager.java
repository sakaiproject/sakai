/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/
package org.sakaiproject.component.gradebook;

import java.util.*;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradebookProperty;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Provides methods which are shared between service business logic and application business
 * logic, but not exposed to external callers.
 */
public abstract class BaseHibernateManager extends HibernateDaoSupport {
    private static final Log log = LogFactory.getLog(BaseHibernateManager.class);

    protected SectionAwareness sectionAwareness;
    protected Authn authn;

    // Local cache of static-between-deployment properties.
    protected Map propertiesMap = new HashMap();

    public Gradebook getGradebook(String uid) throws GradebookNotFoundException {
    	List list = getHibernateTemplate().find("from Gradebook as gb where gb.uid=?",
    		uid);
		if (list.size() == 1) {
			return (Gradebook)list.get(0);
		} else {
            throw new GradebookNotFoundException("Could not find gradebook uid=" + uid);
        }
    }

    protected List getAssignments(Long gradebookId, Session session) throws HibernateException {
        List assignments = session.createQuery(
        	"from Assignment as asn where asn.gradebook.id=? and asn.removed=false").
        	setLong(0, gradebookId.longValue()).
        	list();
        return assignments;
    }

    protected List getCountedStudentGradeRecords(Long gradebookId, String studentId, Session session) throws HibernateException {
        return session.createQuery(
        	"select agr from AssignmentGradeRecord as agr, Assignment as asn where agr.studentId=? and agr.gradableObject=asn and asn.removed=false and asn.notCounted=false and asn.gradebook.id=?").
        	setString(0, studentId).
        	setLong(1, gradebookId.longValue()).
        	list();
    }

    /**
     */
    public CourseGrade getCourseGrade(Long gradebookId) {
        return (CourseGrade)getHibernateTemplate().find(
                "from CourseGrade as cg where cg.gradebook.id=?",
                gradebookId).get(0);
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
        return (CourseGradeRecord)session.createQuery(
        	"from CourseGradeRecord as cgr where cgr.studentId=? and cgr.gradableObject.gradebook=?").
        	setString(0, studentId).
        	setEntity(1, gradebook).
        	uniqueResult();
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
     * TODO Clean up optimistic locking difficulties in recalculate grades.
     * We currently have a "update calculation on write" model. Inside the Gradebook
     * application, reading the calculated grade happens much more often than updating
     * it, and so that design made sense at first. But nowadays, many sites will be
     * updating the Gradebook through services more often than the instructor looks
     * at the calculated course grade. And so we should probably move to a "mark
     * calculation as dirty and update calculation on read" design instead.
     *
     * @param gradebook The gradebook containing the course grade records to update
     * @param studentIds The collection of student IDs
     * @param session The hibernate session
     */
    protected void recalculateCourseGradeRecords(final Gradebook gradebook,
            final Collection studentIds, Session session) throws HibernateException {
        if(logger.isDebugEnabled()) logger.debug("Recalculating " + studentIds.size() + " course grade records");
        int changedRecordCount = 0;	// For debugging

        List assignments = getAssignments(gradebook.getId(), session);
        String graderId = getUserUid();
        Date now = new Date();
        for(Iterator studentIter = studentIds.iterator(); studentIter.hasNext();) {
            String studentId = (String)studentIter.next();

            List gradeRecords = getCountedStudentGradeRecords(gradebook.getId(), studentId, session);
            CourseGrade cg = getCourseGrade(gradebook.getId());
            cg.calculateTotalPointsPossible(assignments);

            // Find the course grade record, if it exists
            CourseGradeRecord cgr = getCourseGradeRecord(gradebook, studentId, session);
            if(cgr == null) {
                cgr = new CourseGradeRecord(cg, studentId, null);
                cgr.setGraderId(graderId);
                cgr.setDateRecorded(now);
            }

            // Store old value to see whether an update is really needed.
            Double oldPointsEarned = cgr.getPointsEarned();

            // Calculate and update the total points and sort grade fields
            cgr.calculateTotalPointsEarned(gradeRecords);

            // Only update if there's been a change.
            Double newPointsEarned = cgr.getPointsEarned();
            if ( ((newPointsEarned != null) && (!newPointsEarned.equals(oldPointsEarned))) ||
            	((newPointsEarned == null) && (oldPointsEarned != null)) ) {
				if (cgr.getEnteredGrade() == null) {
					cgr.setSortGrade(cgr.calculatePercent(cg.getTotalPoints().doubleValue()));
				}
				session.saveOrUpdate(cgr);
				changedRecordCount++;
			}
        }
        if(logger.isDebugEnabled()) logger.debug("Stored " + changedRecordCount + " changed course grade records");
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
        recalculateCourseGradeRecords(gradebook, getAllStudentUids(gradebook.getUid()), session);
    }

    public String getGradebookUid(Long id) {
        return ((Gradebook)getHibernateTemplate().load(Gradebook.class, id)).getUid();
    }

	protected Set getAllStudentUids(String gradebookUid) {
		List enrollments = getSectionAwareness().getSiteMembersInRole(gradebookUid, Role.STUDENT);
        Set studentUids = new HashSet();
        for(Iterator iter = enrollments.iterator(); iter.hasNext();) {
            studentUids.add(((EnrollmentRecord)iter.next()).getUser().getUserUid());
        }
        return studentUids;
	}

	protected Map getPropertiesMap() {

		return propertiesMap;
	}

	public String getPropertyValue(final String name) {
		String value = (String)propertiesMap.get(name);
		if (value == null) {
			List list = getHibernateTemplate().find("from GradebookProperty as prop where prop.name=?",
				name);
			if (!list.isEmpty()) {
				GradebookProperty property = (GradebookProperty)list.get(0);
				value = property.getValue();
				propertiesMap.put(name, value);
			}
		}
		return value;
	}
	public void setPropertyValue(final String name, final String value) {
		GradebookProperty property;
		List list = getHibernateTemplate().find("from GradebookProperty as prop where prop.name=?",
			name);
		if (!list.isEmpty()) {
			property = (GradebookProperty)list.get(0);
		} else {
			property = new GradebookProperty(name);
		}
		property.setValue(value);
		getHibernateTemplate().saveOrUpdate(property);
		propertiesMap.put(name, value);
	}

    public Authn getAuthn() {
        return authn;
    }
    public void setAuthn(Authn authn) {
        this.authn = authn;
    }

    protected String getUserUid() {
    	return authn.getUserUid();
    }

	protected SectionAwareness getSectionAwareness() {
		return sectionAwareness;
	}
	public void setSectionAwareness(SectionAwareness sectionAwareness) {
		this.sectionAwareness = sectionAwareness;
	}
}
