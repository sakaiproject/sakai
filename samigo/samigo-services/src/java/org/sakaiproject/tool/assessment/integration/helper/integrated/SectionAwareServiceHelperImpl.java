/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.integration.helper.integrated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.assessment.integration.helper.ifc.SectionAwareServiceHelper;
import org.sakaiproject.tool.assessment.services.PersistenceService;


/**
 * An implementation of Samigo-specific authorization (based on Gradebook's) needs based
 * on the shared Section Awareness API.
 */
@Slf4j
public class SectionAwareServiceHelperImpl extends AbstractSectionsImpl implements SectionAwareServiceHelper {

	public boolean isUserAbleToGrade(String siteid, String userUid) {
		return (getSectionAwareness().isSiteMemberInRole(siteid, userUid, Role.INSTRUCTOR) || getSectionAwareness().isSiteMemberInRole(siteid, userUid, Role.TA));
	}

	public boolean isUserAbleToGradeAll(String siteid, String userUid) {
		return getSectionAwareness().isSiteMemberInRole(siteid, userUid, Role.INSTRUCTOR);
	}

	public boolean isUserAbleToGradeSection(String sectionUid, String userUid) {
		return getSectionAwareness().isSectionMemberInRole(sectionUid, userUid, Role.TA);
	}

	public boolean isUserAbleToEdit(String siteid, String userUid) {
		return getSectionAwareness().isSiteMemberInRole(siteid, userUid, Role.INSTRUCTOR);
	}

	public boolean isUserGradable(String siteid, String userUid) {
		return getSectionAwareness().isSiteMemberInRole(siteid, userUid, Role.STUDENT);
	}

	/**
	 */
	public List<EnrollmentRecord> getAvailableEnrollments(String siteid, String userUid) {
		List<EnrollmentRecord> enrollments;
		if ("-1".equals(userUid) || isUserAbleToGradeAll(siteid, userUid)) {
			enrollments = getSectionAwareness().getSiteMembersInRole(siteid, Role.STUDENT);
		} else {
			// We use a map because we may have duplicate students among the section
			// participation records.
			Map<String, EnrollmentRecord> enrollmentMap = new HashMap();
			List<CourseSection> sections = getAvailableSections(siteid, userUid);
			for (CourseSection section : sections) {
				List sectionEnrollments = getSectionEnrollmentsTrusted(section.getUuid(), userUid);
				for (Iterator eIter = sectionEnrollments.iterator(); eIter.hasNext(); ) {
					EnrollmentRecord enr = (EnrollmentRecord)eIter.next();
					enrollmentMap.put(enr.getUser().getUserUid(), enr);
				}
			}
			enrollments = new ArrayList<>(enrollmentMap.values());
		}
		return enrollments;
	}

	public List getGroupReleaseEnrollments(String siteid, String userUid, String publishedAssessmentId) {
		List availEnrollments = getAvailableEnrollments(siteid, userUid);
		List enrollments = new ArrayList();

		HashSet<String> membersInReleaseGroups = new HashSet<>(0);
		try {
		    List releaseGroupIds = PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().getReleaseToGroupIdsForPublishedAssessment(publishedAssessmentId);
		    Set<String> releaseGroupIdsSet = new HashSet<>(releaseGroupIds);
		    Site site = SiteService.getInstance().getSite(siteid); // this follows the way the service is already written but it is a bad practice
			membersInReleaseGroups = new HashSet<>( site.getMembersInGroups(releaseGroupIdsSet) );
		} catch (IdUnusedException ex) {
			// no site found, just log a warning
		    log.warn("Unable to find a site with id ("+siteid+") in order to get the enrollments, will return 0 enrollments");
		}

		for (Iterator eIter = availEnrollments.iterator(); eIter.hasNext(); ) {
			EnrollmentRecord enr = (EnrollmentRecord)eIter.next();
			if (membersInReleaseGroups.contains( enr.getUser().getUserUid())) {
				enrollments.add(enr);
			}
		}

		return enrollments;
	}
	
	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	private boolean isUserInReleaseGroup(String userId, String siteId, String publishedAssessmentId) {
		//String functionName="assessment.takeAssessment";
		Collection siteGroups = null;
		try {
			siteGroups = SiteService.getSite(siteId).getGroupsWithMember(userId);
		}
		catch (IdUnusedException ex) {
			// no site found
		}
		List releaseGroupIds = PersistenceService.getInstance()
		  .getPublishedAssessmentFacadeQueries()
		  .getReleaseToGroupIdsForPublishedAssessment(publishedAssessmentId);

		if (siteGroups == null) {
			return false;
		}
		Iterator groupsIter = siteGroups.iterator();
		while (groupsIter.hasNext()) {
			Group group = (Group) groupsIter.next();
			for (Iterator releaseGroupIdsIter = releaseGroupIds.iterator(); 
				releaseGroupIdsIter.hasNext();) {
				//if this group is a release group
				if (group.getId().equals((String)releaseGroupIdsIter.next())) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	public List<CourseSection> getAvailableSections(String siteid, String userUid) {

		List<CourseSection> availableSections = new ArrayList<>();

		SectionAwareness sectionAwareness = getSectionAwareness();
		if (sectionAwareness ==null) {
		}
                else {

		// Get the list of sections. For now, just use whatever default
		// sorting we get from the Section Awareness component.
/*
		List sectionCategories = sectionAwareness.getSectionCategories(siteid);
		for (Iterator catIter = sectionCategories.iterator(); catIter.hasNext(); ) {
			String category = (String)catIter.next();
			List sections = sectionAwareness.getSectionsInCategory(siteid, category);
			for (Iterator iter = sections.iterator(); iter.hasNext(); ) {
				CourseSection section = (CourseSection)iter.next();
				if (isUserAbleToGradeAll(siteid, userUid) || isUserAbleToGradeSection(section.getUuid(), userUid)) {
					availableSections.add(section);
				}
			}
		}
*/

                List sections = sectionAwareness.getSections(siteid);
                for (Iterator iter = sections.iterator(); iter.hasNext(); ) {
                        CourseSection section = (CourseSection)iter.next();
                     //    if (isUserAbleToGradeAll(gradebookUid) || isUserAbleToGradeSection(section.getUuid())) {
				if (isUserAbleToGradeAll(siteid, userUid) || isUserAbleToGradeSection(section.getUuid(), userUid)) {
                                availableSections.add(section);
                        }
                }
                }



		return availableSections;
	}


	public List getSectionEnrollmentsTrusted(String sectionUid) {
		return getSectionEnrollmentsTrusted(sectionUid, null);
	}
	
	private List getSectionEnrollmentsTrusted(String sectionUid, String userUid) {
		return getSectionAwareness().getSectionMembersInRole(sectionUid, Role.STUDENT);
	}

	public List getSectionEnrollments(String siteid, String sectionUid, String userUid) {
		List enrollments;
		if (isUserAbleToGradeAll(siteid, userUid) || isUserAbleToGradeSection(sectionUid, userUid)) {
			enrollments = getSectionEnrollmentsTrusted(sectionUid, userUid);
		} else {
			enrollments = new ArrayList();
			log.warn("getSectionEnrollments for sectionUid=" + sectionUid + " called by unauthorized userUid=" + userUid);
		}
		return enrollments;
	}

	public List findMatchingEnrollments(String siteid, String searchString, String optionalSectionUid, String userUid) {
		List enrollments;
        List allEnrollmentsFilteredBySearch = getSectionAwareness().findSiteMembersInRole(siteid, Role.STUDENT, searchString);

		if (allEnrollmentsFilteredBySearch.isEmpty() ||
			((optionalSectionUid == null) && isUserAbleToGradeAll(siteid, userUid))) {
			enrollments = allEnrollmentsFilteredBySearch;
		} else {
			if (optionalSectionUid == null) {
				enrollments = getAvailableEnrollments(siteid, userUid);
			} else {
				// The user has selected a particular section.
				enrollments = getSectionEnrollments(siteid, optionalSectionUid, userUid);
			}
			Set availableStudentUids = FacadeUtils.getStudentUids(enrollments);
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


        public boolean isSectionMemberInRoleStudent(String sectionId, String studentId) {
                return getSectionAwareness().isSectionMemberInRole(sectionId, studentId, Role.STUDENT);
        }

}
