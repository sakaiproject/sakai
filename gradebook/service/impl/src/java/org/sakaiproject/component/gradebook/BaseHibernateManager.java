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
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
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

    // Oracle will throw a SQLException if we put more than this into a
    // "WHERE tbl.col IN (:paramList)" query.
    public static int MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST = 1000;

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

	/**
	 * Oracle has a low limit on the maximum length of a parameter list
	 * in SQL queries of the form "WHERE tbl.col IN (:paramList)".
	 * Since enrollment lists can sometimes be very long, we've replaced
	 * such queries with full selects followed by filtering. This helper
	 * method filters out unwanted grade records. (Typically they're not
	 * wanted because they're either no longer officially enrolled in the
	 * course or they're not members of the selected section.)
	 */
	protected List filterGradeRecordsByStudents(Collection gradeRecords, Collection studentUids) {
		List filteredRecords = new ArrayList();
		for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			AbstractGradeRecord agr = (AbstractGradeRecord)iter.next();
			if (studentUids.contains(agr.getStudentId())) {
				filteredRecords.add(agr);
			}
		}
		return filteredRecords;
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
