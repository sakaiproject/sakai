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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;

/**
 * Resolves user roles in CourseOfferings.
 *
 */
@Slf4j
public class CourseOfferingRoleResolver extends BaseRoleResolver {

	// Configuration keys.
	public static final String COURSE_OFFERING_ROLE_TO_SITE_ROLE = "courseOfferingRoleToSiteRole";
	
	/**
	 * Internal configuration.
	 */
	public void init() {
		if (configuration != null) {
			setRoleMap((Map<String, String>)configuration.get(COURSE_OFFERING_ROLE_TO_SITE_ROLE));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> getUserRoles(CourseManagementService cmService, Section section) {
		Map<String, String> userRoleMap = new HashMap<String, String>();

		// Don't bother doing anything if the integration is configured to ignore
		// CourseOffering memberships.
		if ((roleMap == null) || (roleMap.size() == 0)) {
			return userRoleMap;
		}
		
		String coEid = section.getCourseOfferingEid();
		
		// Get the members of this course offering
		Set<Membership> coMembers = cmService.getCourseOfferingMemberships(coEid);
		Set<Membership> equivMembers = new HashSet<Membership>();
		
		// Get the members of equivalent course offerings, and add them to the set of equivalent memberships
		Set<CourseOffering> equivalentCourseOfferings = cmService.getEquivalentCourseOfferings(coEid);
		for(Iterator<CourseOffering> iter = equivalentCourseOfferings.iterator(); iter.hasNext();) {
			CourseOffering equivCo = iter.next();
			equivMembers.addAll(cmService.getCourseOfferingMemberships(equivCo.getEid()));
		}

		// Add the course offering members
		addMemberRoles(userRoleMap, coMembers);
		
		// Add the equivalent course offering members (but don't override any roles in the original course offering)
		addMemberRoles(userRoleMap, equivMembers);
		
		return userRoleMap;
	}

	private void addMemberRoles(Map<String, String> userRoleMap, Set<Membership>coMembers) {
		for(Iterator<Membership> iter = coMembers.iterator(); iter.hasNext();) {
			Membership membership = iter.next();
			if(userRoleMap.containsKey(membership.getUserId())) {
				// Don't override existing roles in the map.
			} else {
				String sakaiRole = convertRole(membership.getRole());
				if(sakaiRole != null) {
					userRoleMap.put(membership.getUserId(), sakaiRole);
				}
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> getGroupRoles(CourseManagementService cmService, String userEid) {
		Map<String, String> sectionRoles = new HashMap<String, String>();

		// Don't bother doing anything if the integration is configured to ignore
		// CourseOffering memberships.
		if ((roleMap == null) || (roleMap.size() == 0)) {
			return sectionRoles;
		}

		// Find all of the course offerings for which this user is a member
		Map<String, String> courseOfferingRoles = cmService.findCourseOfferingRoles(userEid);
		if(log.isDebugEnabled()) log.debug("Found " + courseOfferingRoles.size() + " course offering roles for " + userEid);

		// Add all of the equivalent course offerings-> role mappings for this user
		Set<String> coEids = new HashSet<String>(courseOfferingRoles.keySet());
		for(String coEid : coEids) {
			Set<CourseOffering> equivOfferings = cmService.getEquivalentCourseOfferings(coEid);
			for(Iterator<CourseOffering> equivIter = equivOfferings.iterator(); equivIter.hasNext();) {
				CourseOffering equiv = equivIter.next();
				// Use the role in the original course offering for this equivalent course offering
				courseOfferingRoles.put(equiv.getEid(), courseOfferingRoles.get(coEid));
			}
		}
		
		for(Iterator<Entry<String, String>> coIter = courseOfferingRoles.entrySet().iterator(); coIter.hasNext();) {
			Entry<String, String> entry = coIter.next();
			String coEid = entry.getKey();
			String coRole = entry.getValue();
			
			// If this role shouldn't be part of the site, ignore the membership in this course offering
			String sakaiRole = convertRole(coRole);
			if(sakaiRole == null) {
				if(log.isDebugEnabled()) log.debug("Course offering role " + coRole + " is not mapped to a sakai role.  Skipping this membership.");
				continue;
			}
			
			if(log.isDebugEnabled()) log.debug(userEid + " has role=" + coRole + " in course offering " + coEid);
			// Get the sections in each course offering
			Set<Section> sections = cmService.getSections(coEid);
			for(Iterator<Section> secIter = sections.iterator(); secIter.hasNext();) {
				// Add the section EIDs and the converted *CourseOffering* role to the sectionRoles map
				Section section = secIter.next();
				sectionRoles.put(section.getEid(), sakaiRole);
			}
		}
		return sectionRoles;
	}

}
