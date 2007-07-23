/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.facades.sections;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.site.api.Group;

import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Authz;

/**
 * An implementation of Gradebook-specific authorization needs based
 * on the shared Section Awareness API.
 */
public class AuthzSectionsImpl implements Authz {
    private static final Log log = LogFactory.getLog(AuthzSectionsImpl.class);

    private Authn authn;
    private SectionAwareness sectionAwareness;

	public boolean isUserAbleToGrade(String gradebookUid) {
		String userUid = authn.getUserUid();
		return (getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.INSTRUCTOR) || getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.TA));
	}

	public boolean isUserAbleToGradeAll(String gradebookUid) {
		String userUid = authn.getUserUid();
		return getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.INSTRUCTOR);
	}

	public boolean isUserAbleToGradeSection(String sectionUid) {
		String userUid = authn.getUserUid();
		return getSectionAwareness().isSectionMemberInRole(sectionUid, userUid, Role.TA);
	}

	public boolean isUserAbleToEditAssessments(String gradebookUid) {
		String userUid = authn.getUserUid();
		return getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.INSTRUCTOR);
	}

	public boolean isUserAbleToViewOwnGrades(String gradebookUid) {
		String userUid = authn.getUserUid();
		return getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.STUDENT);
	}

	/**
	 * Note that this is not a particularly efficient implementation.
	 * If the method becomes more heavily used, it should be optimized.
	 */
	public boolean isUserAbleToGradeStudent(String gradebookUid, String studentUid) {
		if (isUserAbleToGradeAll(gradebookUid)) {
			return true;
		}

		List sections = getAvailableSections(gradebookUid);
		for (Iterator iter = sections.iterator(); iter.hasNext(); ) {
			CourseSection section = (CourseSection)iter.next();
			if (getSectionAwareness().isSectionMemberInRole(section.getUuid(), studentUid, Role.STUDENT)) {
				return true;
			}
		}

		return false;
	}

	/**
	 */
	public List getAvailableEnrollments(String gradebookUid) {
		List enrollments;
		if (isUserAbleToGradeAll(gradebookUid)) {
			enrollments = getSectionAwareness().getSiteMembersInRole(gradebookUid, Role.STUDENT);
		} else {
			// We use a map because we may have duplicate students among the section
			// participation records.
			Map enrollmentMap = new HashMap();
			List sections = getAvailableSections(gradebookUid);
			for (Iterator iter = sections.iterator(); iter.hasNext(); ) {
				CourseSection section = (CourseSection)iter.next();
				List sectionEnrollments = getSectionEnrollmentsTrusted(section.getUuid());
				for (Iterator eIter = sectionEnrollments.iterator(); eIter.hasNext(); ) {
					EnrollmentRecord enr = (EnrollmentRecord)eIter.next();
					enrollmentMap.put(enr.getUser().getUserUid(), enr);
				}
			}
			enrollments = new ArrayList(enrollmentMap.values());
		}
		return enrollments;
	}

	public List getAvailableSections(String gradebookUid) {
		SectionAwareness sectionAwareness = getSectionAwareness();
		List availableSections = new ArrayList();
		boolean userAbleToGradeAll = isUserAbleToGradeAll(gradebookUid);

		// Get the list of sections. For now, just use whatever default
		// sorting we get from the Section Awareness component.
		List sections = sectionAwareness.getSections(gradebookUid);
		for (Iterator iter = sections.iterator(); iter.hasNext(); ) {
			CourseSection section = (CourseSection)iter.next();
			if (userAbleToGradeAll || isUserAbleToGradeSection(section.getUuid())) {
				availableSections.add(section);
			}
		}

		return availableSections;
	}
	
	public List getAllSections(String gradebookUid) {
		SectionAwareness sectionAwareness = getSectionAwareness();
		List sections = sectionAwareness.getSections(gradebookUid);

		return sections;
	}
	
	

	private List getSectionEnrollmentsTrusted(String sectionUid) {
		return getSectionAwareness().getSectionMembersInRole(sectionUid, Role.STUDENT);
	}

	public List getSectionEnrollments(String gradebookUid, String sectionUid) {
		String userUid = authn.getUserUid();
		List enrollments;
		if (isUserAbleToGradeAll(gradebookUid) || isUserAbleToGradeSection(sectionUid)) {
			enrollments = getSectionEnrollmentsTrusted(sectionUid);
		} else {
			enrollments = new ArrayList();
			log.warn("getSectionEnrollments for sectionUid=" + sectionUid + " called by unauthorized userUid=" + userUid);
		}
		return enrollments;
	}

	public List findMatchingEnrollments(String gradebookUid, String searchString, String optionalSectionUid) {
		List enrollments;
        List allEnrollmentsFilteredBySearch = getSectionAwareness().findSiteMembersInRole(gradebookUid, Role.STUDENT, searchString);

		if (allEnrollmentsFilteredBySearch.isEmpty() ||
			((optionalSectionUid == null) && isUserAbleToGradeAll(gradebookUid))) {
			enrollments = allEnrollmentsFilteredBySearch;
		} else {
			if (optionalSectionUid == null) {
				enrollments = getAvailableEnrollments(gradebookUid);
			} else {
				// The user has selected a particular section.
				enrollments = getSectionEnrollments(gradebookUid, optionalSectionUid);
			}
			Set availableStudentUids = new HashSet();
			for(Iterator iter = enrollments.iterator(); iter.hasNext();) {
				EnrollmentRecord enr = (EnrollmentRecord)iter.next();
				availableStudentUids.add(enr.getUser().getUserUid());
			}

			enrollments = new ArrayList();
			for (Iterator iter = allEnrollmentsFilteredBySearch.iterator(); iter.hasNext(); ) {
				EnrollmentRecord enr = (EnrollmentRecord)iter.next();
				if (availableStudentUids.contains(enr.getUser().getUserUid())) {
					enrollments.add(enr);
				}
			}
		}

		return enrollments;
	}
	
	public List findStudentSectionMemberships(String gradebookUid, String studentUid) {
		List sectionMemberships = new ArrayList();
		try {
			sectionMemberships = (List)org.sakaiproject.site.cover.SiteService.getSite(gradebookUid).getGroupsWithMember(studentUid);
    	} catch (IdUnusedException e) {
    		log.error("No site with id = " + gradebookUid);
    	}
    	
    	return sectionMemberships;
	}
	
	public List getStudentSectionMembershipNames(String gradebookUid, String studentUid) {
		List sectionNames = new ArrayList();
		List sections = findStudentSectionMemberships(gradebookUid, studentUid);
		if (sections != null && !sections.isEmpty()) {
			Iterator sectionIter = sections.iterator();
			while (sectionIter.hasNext()) {
				Group myGroup = (Group) sectionIter.next();
				sectionNames.add(myGroup.getTitle());
			}
		}
		
		return sectionNames;
	}

	public Authn getAuthn() {
		return authn;
	}
	public void setAuthn(Authn authn) {
		this.authn = authn;
	}
	public SectionAwareness getSectionAwareness() {
		return sectionAwareness;
	}
	public void setSectionAwareness(SectionAwareness sectionAwareness) {
		this.sectionAwareness = sectionAwareness;
	}

}
