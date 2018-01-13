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
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.coursemanagement.api.CanonicalCourse;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;

/**
 * Resolves user roles in CourseOfferings.
 *
 */
@Slf4j
public class CourseSetRoleResolver extends BaseRoleResolver {

	// Configuration keys.
	public static final String COURSE_SET_ROLE_TO_SITE_ROLE = "courseSetRoleToSiteRole";

	/**
	 * Internal configuration.
	 */
	public void init() {
		if (configuration != null) {
			setRoleMap((Map<String, String>)configuration.get(COURSE_SET_ROLE_TO_SITE_ROLE));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> getUserRoles(CourseManagementService cmService, Section section) {
		Map<String, String> userRoleMap = new HashMap<String, String>();

		// Don't bother doing anything if the integration is configured to ignore
		// CourseSet memberships.
		if ((roleMap == null) || (roleMap.size() == 0)) {
			return userRoleMap;
		}

		Set<String> csEids = getCourseSetEids(cmService,section);
		if(csEids.isEmpty()) {
			if(log.isDebugEnabled()) log.debug("There are no course sets associated with section " + section.getEid());
			return new HashMap<String, String>();
		}
		
		// Iterate over the course set EIDs.  If the user is a member of any of the
		// course sets, add that role to the map.
		
		// TODO:  We need to account for cases where a user has different roles in multiple course sets.
		
		for(String eid : csEids) {
			Set<Membership> csMembers = cmService.getCourseSetMemberships(eid);
			if(csMembers == null) {
				if(log.isDebugEnabled()) log.debug("CourseSet " + eid + " has a null set of members");
				continue;
			}
			if(log.isDebugEnabled()) log.debug("CourseSet " + eid + " has " + csMembers.size() + " members");
			for(Membership membership : csMembers) {
				String sakaiRole = convertRole(membership.getRole());
				if(sakaiRole != null) {
					if(log.isDebugEnabled()) log.debug("Adding user " + membership.getUserId() + " to userRoleMap with role " + sakaiRole);
					userRoleMap.put(membership.getUserId(), sakaiRole);
				}
			}
		}
		return userRoleMap;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> getGroupRoles(CourseManagementService cmService, String userEid) {
		log.debug("getGroupRoles: " + userEid);
		Map<String, String> sectionRoles = new HashMap<String, String>();

		// Don't bother doing anything if the integration is configured to ignore
		// CourseSet memberships.
		if ((roleMap == null) || (roleMap.size() == 0)) {
			return sectionRoles;
		}

		Map<String, String> courseSetRoles = cmService.findCourseSetRoles(userEid);
		
		// Look at each of the course sets for which this user has a role
		for(Entry<String, String> csEntry : courseSetRoles.entrySet()) {
			//we want the users role in this Set
			String csRole = csEntry.getValue();
			
			// If this course set role shouldn't be added to the site, ignore this course set
			String sakaiRole = convertRole(csRole);
			log.debug("got role " + sakaiRole + " for section " + csEntry.getKey());
			if(sakaiRole == null) {
				continue;
			}
			
			// Look at each of the course offerings in the course set
			Set<CourseOffering> courseOfferings = cmService.getCourseOfferingsInCourseSet(csEntry.getKey());
			for(CourseOffering co : courseOfferings) {
				// Get the sections in each course offering
				Set<Section> sections = cmService.getSections(co.getEid());
				for(Section section : sections) {
					// Add the section EIDs and *CourseSet* role to the sectionRoles map
					sectionRoles.put(section.getEid(), sakaiRole);
				}
			}
		}
		return sectionRoles;
	}

	private Set<String> getCourseSetEids(CourseManagementService cmService, Section section) {
		// Look up the hierarchy for any course sets
		CourseOffering co;
		CanonicalCourse cc;
		try {
			co = cmService.getCourseOffering(section.getCourseOfferingEid());
			cc = cmService.getCanonicalCourse(co.getCanonicalCourseEid());
		} catch (IdNotFoundException ide) {
			if(log.isDebugEnabled()) log.debug("Unable to find CM objects: " + ide);
			return new HashSet<String>();
		}
		
		if(log.isDebugEnabled()) log.debug("Found course offering " + co);
		if(log.isDebugEnabled()) log.debug("Found canonical course " + cc);
		
		// Now that we have the CourseOffering, check for cross-listed courses
		Set<CourseOffering> xListedCourseOfferings = cmService.getEquivalentCourseOfferings(co.getEid());
		Set<CanonicalCourse> xListedCanonCourses = cmService.getEquivalentCanonicalCourses(cc.getEid());

		// Collect all of the CourseSet EIDs connected to this course or an equivalent
		Set<String> csEids = co.getCourseSetEids();
		if(log.isDebugEnabled()) log.debug("Course offering " + co.getEid() + " is a member of " + csEids.size() + " course sets");

		// Collect all of the CourseSet EIDs for which these cross listed course offerings are a member
		for(CourseOffering xListCo : xListedCourseOfferings) {
			String xListCcEid = xListCo.getCanonicalCourseEid();
			CanonicalCourse xListCc = cmService.getCanonicalCourse(xListCcEid);
			csEids.addAll(xListCc.getCourseSetEids());
		}
		for(CanonicalCourse xListCc : xListedCanonCourses) {
			csEids.addAll(xListCc.getCourseSetEids());
		}
		if(log.isDebugEnabled()) log.debug("Found " + csEids.size() + " course sets for section " + section.getEid() );
		return csEids;
	}
}
