/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;

/**
 * Resolves users roles in sections.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class SectionRoleResolver implements RoleResolver {
	private static final Log log = LogFactory.getLog(SectionRoleResolver.class);

	/** Map of CM section roles to Sakai roles */
	protected Map<String, String> roleMap;
	
	/** The Sakai role to use for official instructors of EnrollmentSets */
	protected String officialInstructorRole;

	/** The Sakai roles to use for official enrollments in EnrollmentSets,keyed on the enrollment status */
	protected Map<String, String> enrollmentStatusRoleMap;

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> getUserRoles(CourseManagementService cmService, Section section) {
		Map<String, String> userRoleMap = new HashMap<String, String>();

		EnrollmentSet enrSet = section.getEnrollmentSet();
		if(log.isDebugEnabled()) log.debug( "EnrollmentSet  " + enrSet + " is attached to section " + section.getEid());
		if(enrSet != null) {
			// Check for official instructors
			Set<String> officialInstructors = cmService.getInstructorsOfRecordIds(enrSet.getEid());
			for(Iterator<String> iter = officialInstructors.iterator(); iter.hasNext();) {
				userRoleMap.put(iter.next(), officialInstructorRole);
			}

			// Check for enrollments
			Set enrollments = cmService.getEnrollments(section.getEnrollmentSet().getEid());
			for(Iterator iter = enrollments.iterator(); iter.hasNext();) {
				Enrollment enr = (Enrollment)iter.next();
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
		
		// Check for memberships
		Set memberships = cmService.getSectionMemberships(section.getEid());
		for(Iterator iter = memberships.iterator(); iter.hasNext();) {
			Membership membership = (Membership)iter.next();
			// Only add the membership role if the user isn't enrolled or an official instructor(these take precedence)
			if( ! userRoleMap.containsKey(membership.getUserId())) {
				String convertedRole = convertRole(membership.getRole());
				if(convertedRole != null) {
					userRoleMap.put(membership.getUserId(), convertedRole);
				}
			}
		}
		return userRoleMap;
	}

	public Map<String, String> getGroupRoles(CourseManagementService cmService, String userEid) {
		// Start with the sectionEid->role map
		Map<String, String> groupRoleMap = cmService.findSectionRoles(userEid);
		
		// Convert these roles to Sakai roles
		Set<String> iterSet = new HashSet<String>(groupRoleMap.keySet());
		for(Iterator iter = iterSet.iterator(); iter.hasNext();) {
			String key = (String)iter.next();
			groupRoleMap.put(key, convertRole((String)groupRoleMap.get(key)));
		}

		// Next add all enrollments to the sectionEid->role map, overriding memberships
		Set enrolledSections = cmService.findEnrolledSections(userEid);
		if(log.isDebugEnabled()) log.debug("Found " + enrolledSections.size() + " currently enrolled sections for user " + userEid);
		for(Iterator secIter = enrolledSections.iterator(); secIter.hasNext();) {
			Section section = (Section)secIter.next();
			if(log.isDebugEnabled()) log.debug(userEid + " is enrolled in an enrollment set attached to section " + section.getEid());
			// TODO Calling this for every section  is inefficient -- add new method to CM service?
			Enrollment enr = cmService.findEnrollment(userEid, section.getEnrollmentSet().getEid());
			String roleFromEnrollmentStatus = enrollmentStatusRoleMap.get(enr.getEnrollmentStatus());
			
			// Only add the enrollment if it's not dropped and it has an enrollment role mapping
			if(roleFromEnrollmentStatus != null && ! enr.isDropped()) {
				groupRoleMap.put(section.getEid(), roleFromEnrollmentStatus);
			}
		}

		// Finally, add the official instructors, overriding any other roles if necessary
		Set instructingSections = cmService.findInstructingSections(userEid);
		for(Iterator iter = instructingSections.iterator(); iter.hasNext();) {
			Section instructingSection = (Section)iter.next();
			groupRoleMap.put(instructingSection.getEid(), officialInstructorRole);
		}
		
		if(log.isDebugEnabled()) {
			for(Iterator iter = groupRoleMap.keySet().iterator(); iter.hasNext();) {
				String sectionEid = (String)iter.next();
				log.debug("User " + userEid + " has role " + groupRoleMap.get(sectionEid) + " in " + sectionEid);
			}
		}
		return groupRoleMap;
	}
	
	public String convertRole(String cmRole) {
		if (cmRole == null) {
			log.warn("Can not convert CM role 'null' to a sakai role.");
			return null;
		}
		String sakaiRole = (String)roleMap.get(cmRole);
		if(sakaiRole== null) {
			log.warn("Unable to find sakai role for CM role " + cmRole);
			return null;
		} else {
			return sakaiRole;
		}
	}

	// Dependency injection
	
	public void setRoleMap(Map<String, String> roleMap) {
		this.roleMap = roleMap;
	}
		
	public void setOfficialInstructorRole(String officialInstructorRole) {
		this.officialInstructorRole = officialInstructorRole;
	}

	public void setEnrollmentStatusRoleMap(Map<String, String> enrollmentStatusRoleMap) {
		this.enrollmentStatusRoleMap = enrollmentStatusRoleMap;
	}
}
