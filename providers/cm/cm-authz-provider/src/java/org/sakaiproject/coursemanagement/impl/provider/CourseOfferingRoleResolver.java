/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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
package org.sakaiproject.coursemanagement.impl.provider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;

/**
 * Resolves user roles in CourseOfferings.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseOfferingRoleResolver implements RoleResolver {
	private static final Log log = LogFactory.getLog(CourseOfferingRoleResolver.class);
	
	/** Map of CM course offering roles to Sakai roles */
	Map roleMap;

	/**
	 * {@inheritDoc}
	 */
	public Map getUserRoles(CourseManagementService cmService, Section section) {
		Map userRoleMap = new HashMap();
		// Get the members of this course offering
		Set coMembers = cmService.getCourseOfferingMemberships(section.getCourseOfferingEid());

		// Get the members of equivalent course offerings, and add them to the set of all memberships
		Set equivalentCourseOfferings = cmService.getEquivalentCourseOfferings(section.getCourseOfferingEid());
		for(Iterator iter = equivalentCourseOfferings.iterator(); iter.hasNext();) {
			CourseOffering equivCo = (CourseOffering)iter.next();
			Set equivMembers = cmService.getCourseOfferingMemberships(equivCo.getEid());
			if(equivMembers != null) {
				coMembers.addAll(equivMembers);
			}
		}

		if(coMembers != null) {
			if(log.isDebugEnabled()) log.debug(coMembers.size() +" members in course offering " + section.getCourseOfferingEid() + " and equivalents");
			for(Iterator iter = coMembers.iterator(); iter.hasNext();) {
				Membership membership = (Membership)iter.next();
				if(log.isDebugEnabled()) log.debug("Adding " + membership.getUserId() +
						" with role " + membership.getRole() + " for section " + section.getEid());
				if(userRoleMap.containsKey(membership.getUserId())) {
					log.warn("User " + membership.getUserId() + " is a member of multiple course offings (probably equivalents).  Their role in section " + section.getEid() + " is ambiguous");
				} else {
					String sakaiRole = convertRole(membership.getRole());
					if(sakaiRole != null) {
						userRoleMap.put(membership.getUserId(), sakaiRole);
					}
				}
			}
		}
		return userRoleMap;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map getGroupRoles(CourseManagementService cmService, String userEid) {
		Map sectionRoles = new HashMap();
		
		// Find all of the course offerings for which this user is a member
		Map courseOfferingRoles = cmService.findCourseOfferingRoles(userEid);
		if(log.isDebugEnabled()) log.debug("Found " + courseOfferingRoles.size() + " course offering roles for " + userEid);

		// Add all of the equivalent course offerings-> role mappings for this user
		Set coEids = new HashSet(courseOfferingRoles.keySet());
		for(Iterator coIter = coEids.iterator(); coIter.hasNext();) {
			String equivCoEid = (String)coIter.next();
			courseOfferingRoles.putAll(cmService.findCourseOfferingRoles(equivCoEid));
		}
		
		for(Iterator coIter = courseOfferingRoles.keySet().iterator(); coIter.hasNext();) {
			String coEid = (String)coIter.next();
			String coRole = (String)courseOfferingRoles.get(coEid);
			
			// If this role shouldn't be part of the site, ignore the membership in this course offering
			String sakaiRole = convertRole(coRole);
			if(sakaiRole == null) {
				if(log.isDebugEnabled()) log.debug("Course offering role " + coRole + " is not mapped to a sakai role.  Skipping this membership.");
				continue;
			}
			
			if(log.isDebugEnabled()) log.debug(userEid + " has role=" + coRole + " in course offering " + coEid);
			// Get the sections in each course offering
			Set sections = cmService.getSections(coEid);
			for(Iterator secIter = sections.iterator(); secIter.hasNext();) {
				// Add the section EIDs and the converted *CourseOffering* role to the sectionRoles map
				Section section = (Section)secIter.next();
				sectionRoles.put(section.getEid(), sakaiRole);
			}
		}
		return sectionRoles;
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
	
	public void setRoleMap(Map roleMap) {
		this.roleMap = roleMap;
	}

}
