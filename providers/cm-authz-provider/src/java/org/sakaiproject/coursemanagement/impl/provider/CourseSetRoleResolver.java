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
import org.sakaiproject.coursemanagement.api.CanonicalCourse;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;

/**
 * Resolves user roles in CourseOfferings.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseSetRoleResolver implements RoleResolver {
	private static final Log log = LogFactory.getLog(CourseSetRoleResolver.class);
		
	/** Map of CM course set roles to Sakai roles */
	Map roleMap;

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> getUserRoles(CourseManagementService cmService, Section section) {
		Map<String, String> userRoleMap = new HashMap<String, String>();

		Set csEids = getCourseSetEids(cmService,section);
		if(csEids.isEmpty()) {
			if(log.isDebugEnabled()) log.debug("There are no course sets associated with section " + section.getEid());
			return new HashMap<String, String>();
		}
		
		// Iterate over the course set EIDs.  If the user is a member of any of the
		// course sets, add that role to the map.
		
		// TODO:  We need to account for cases where a user has different roles in multiple course sets.
		
		for(Iterator iter = csEids.iterator(); iter.hasNext();) {
			String eid = (String)iter.next();
			Set csMembers = cmService.getCourseSetMemberships(eid);
			if(csMembers == null) {
				if(log.isDebugEnabled()) log.debug("CourseSet " + eid + " has a null set of members");
				continue;
			}
			if(log.isDebugEnabled()) log.debug("CourseSet " + eid + " has " + csMembers.size() + " members");
			for(Iterator memberIter = csMembers.iterator(); memberIter.hasNext();) {
				Membership membership = (Membership)memberIter.next();
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
		Map<String, String> sectionRoles = new HashMap<String, String>();
		Map courseSetRoles = cmService.findCourseSetRoles(userEid);
		
		// Look at each of the course sets for which this user has a role
		for(Iterator csIter = courseSetRoles.keySet().iterator(); csIter.hasNext();) {
			String csEid = (String)csIter.next();
			String csRole = (String)courseSetRoles.get(csEid);
			
			// If this course set role shouldn't be added to the site, ignore this course set
			String sakaiRole = convertRole(csRole);
			if(sakaiRole == null) {
				continue;
			}
			
			// Look at each of the course offerings in the course set
			Set courseOfferings = cmService.getCourseOfferingsInCourseSet(csEid);
			for(Iterator coIter = courseOfferings.iterator(); coIter.hasNext();) {
				CourseOffering co = (CourseOffering)coIter.next();
				// Get the sections in each course offering
				Set sections = cmService.getSections(co.getEid());
				for(Iterator secIter = sections.iterator(); secIter.hasNext();) {
					// Add the section EIDs and *CourseSet* role to the sectionRoles map
					Section section = (Section)secIter.next();
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
		Set xListedCourseOfferings = cmService.getEquivalentCourseOfferings(co.getEid());
		Set xListedCanonCourses = cmService.getEquivalentCanonicalCourses(cc.getEid());

		// Collect all of the CourseSet EIDs connected to this course or an equivalent
		Set<String> csEids = co.getCourseSetEids();
		if(log.isDebugEnabled()) log.debug("Course offering " + co.getEid() + " is a member of " + csEids.size() + " course sets");

		// Collect all of the CourseSet EIDs for which these cross listed course offerings are a member
		for(Iterator coIter = xListedCourseOfferings.iterator(); coIter.hasNext();) {
			CourseOffering xListCo = (CourseOffering)coIter.next();
			String xListCcEid = xListCo.getCanonicalCourseEid();
			CanonicalCourse xListCc = cmService.getCanonicalCourse(xListCcEid);
			csEids.addAll(xListCc.getCourseSetEids());
		}
		for(Iterator ccIter = xListedCanonCourses.iterator(); ccIter.hasNext();) {
			CanonicalCourse xListCc = (CanonicalCourse)ccIter.next();
			csEids.addAll(xListCc.getCourseSetEids());
		}
		if(log.isDebugEnabled()) log.debug("Found " + csEids.size() + " course sets for section " + section.getEid() );
		return csEids;
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
