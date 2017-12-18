/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.coursemanagement.impl.provider;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;

/**
 * Resolves users roles in sections.
 */
@Slf4j
public class SectionRoleResolver extends BaseRoleResolver {

	// Configuration keys.
	public static final String OFFICIAL_INSTRUCTOR_TO_SITE_ROLE = "officialInstructorToSiteRole";
	public static final String ENROLLMENT_STATUS_TO_SITE_ROLE = "enrollmentStatusToSiteRole";
	public static final String SECTION_ROLE_TO_SITE_ROLE = "sectionRoleToSiteRole";

	/** The Sakai role to use for official instructors of EnrollmentSets */
	protected String officialInstructorRole;

	/** The Sakai roles to use for official enrollments in EnrollmentSets,keyed on the enrollment status */
	protected Map<String, String> enrollmentStatusRoleMap;
	
	/**
	 * Internal configuration.
	 */
	public void init() {
		if (configuration != null) {
			if (log.isDebugEnabled()) log.debug("Using configuration object; section role map=" + configuration.get(SECTION_ROLE_TO_SITE_ROLE) + ", officialInstructorRole=" + configuration.get(OFFICIAL_INSTRUCTOR_TO_SITE_ROLE) + ", enrollment status role map=" + configuration.get(ENROLLMENT_STATUS_TO_SITE_ROLE));
			setRoleMap((Map<String, String>)configuration.get(SECTION_ROLE_TO_SITE_ROLE));
			setOfficialInstructorRole((String)configuration.get(OFFICIAL_INSTRUCTOR_TO_SITE_ROLE));
			setEnrollmentStatusRoleMap((Map<String, String>)configuration.get(ENROLLMENT_STATUS_TO_SITE_ROLE));
		} else {
			if (log.isDebugEnabled()) log.debug("Not using configuration object");
		}
	}

	/**
	 * Since a user may be both enrolled in a mapped EnrollmentSet and have a
	 * Membership role in a mapped Section, the following order of precedence is
	 * applied: Official Instructor, Enrollment, membership
	 */
	public Map<String, String> getUserRoles(CourseManagementService cmService, Section section) {
		Map<String, String> userRoleMap = new HashMap<String, String>();

		EnrollmentSet enrSet = section.getEnrollmentSet();
		if(log.isDebugEnabled()) log.debug( "EnrollmentSet  " + enrSet + " is attached to section " + section.getEid());
		if(enrSet != null) {
			// Check for official instructors
			if ((officialInstructorRole != null) && (officialInstructorRole.length() > 0)) {
				Set<String> officialInstructors = cmService.getInstructorsOfRecordIds(enrSet.getEid());
				for(Iterator<String> iter = officialInstructors.iterator(); iter.hasNext();) {
					userRoleMap.put(iter.next(), officialInstructorRole);
				}
			}

			// Check for enrollments
			if ((enrollmentStatusRoleMap != null) && (enrollmentStatusRoleMap.size() > 0)) {
				Set<Enrollment> enrollments = cmService.getEnrollments(section.getEnrollmentSet().getEid());
				for(Enrollment enr : enrollments) {
					if(enr.isDropped()) {
						continue;
					}
					String roleFromEnrollmentStatus = (String)enrollmentStatusRoleMap.get(enr.getEnrollmentStatus());

					// Only add the enrollment if it's not dropped and it has an enrollment role mapping
					// Defer to the official instructor status
					if( ! userRoleMap.containsKey(enr.getUserId()) && roleFromEnrollmentStatus != null &&  ! enr.isDropped()) {
						userRoleMap.put(enr.getUserId(), roleFromEnrollmentStatus);
					}
				}
			}
		}
		
		// Check for memberships
		if ((roleMap != null) && (roleMap.size() > 0)) {
			Set<Membership> memberships = cmService.getSectionMemberships(section.getEid());
			for(Membership membership : memberships) {
				// Only add the membership role if the user isn't enrolled or an official instructor(these take precedence)
				if( ! userRoleMap.containsKey(membership.getUserId())) {
					String convertedRole = convertRole(membership.getRole());
					if(convertedRole != null) {
						userRoleMap.put(membership.getUserId(), convertedRole);
					}
				}
			}
		}
		return userRoleMap;
	}

	public Map<String, String> getGroupRoles(CourseManagementService cmService, String userEid) {
		Map<String, String> groupRoleMap = new HashMap<String, String>();
		
		// Start with the sectionEid->role map
		if ((roleMap != null) && (roleMap.size() > 0)) {
			Map<String, String> sectionRoles = cmService.findSectionRoles(userEid);

			// Convert these roles to Sakai roles
			for(Entry<String, String> entry : sectionRoles.entrySet()) {
				groupRoleMap.put(entry.getKey(), convertRole(entry.getValue()));
			}
		}

		// Next add all enrollments to the sectionEid->role map, overriding memberships
		if ((enrollmentStatusRoleMap != null) && (enrollmentStatusRoleMap.size() > 0)) {
			Set<Section> enrolledSections = cmService.findEnrolledSections(userEid);
			if(log.isDebugEnabled()) log.debug("Found " + enrolledSections.size() + " currently enrolled sections for user " + userEid);
			for(Section section : enrolledSections) {
				if(log.isDebugEnabled()) log.debug(userEid + " is enrolled in an enrollment set attached to section " + section.getEid());
				// TODO Calling this for every section  is inefficient -- add new method to CM service?
				Enrollment enr = cmService.findEnrollment(userEid, section.getEnrollmentSet().getEid());
				String roleFromEnrollmentStatus = enrollmentStatusRoleMap.get(enr.getEnrollmentStatus());

				// Only add the enrollment if it's not dropped and it has an enrollment role mapping
				if(roleFromEnrollmentStatus != null && ! enr.isDropped()) {
					groupRoleMap.put(section.getEid(), roleFromEnrollmentStatus);
				}
			}
		}

		// Finally, add the official instructors, overriding any other roles if necessary
		if ((officialInstructorRole != null) && (officialInstructorRole.length() > 0)) {
			Set<Section> instructingSections = cmService.findInstructingSections(userEid);
			for(Section instructingSection : instructingSections) {
				groupRoleMap.put(instructingSection.getEid(), officialInstructorRole);
			}
		}
		
		if(log.isDebugEnabled()) {
			for(Entry<String, String> sectionEid : groupRoleMap.entrySet()) {
				log.debug("User " + userEid + " has role " + sectionEid.getValue() + " in " + sectionEid);
			}
		}
		return groupRoleMap;
	}
	
	public void setOfficialInstructorRole(String officialInstructorRole) {
		this.officialInstructorRole = officialInstructorRole;
	}

	public void setEnrollmentStatusRoleMap(Map<String, String> enrollmentStatusRoleMap) {
		this.enrollmentStatusRoleMap = enrollmentStatusRoleMap;
	}
	
}
