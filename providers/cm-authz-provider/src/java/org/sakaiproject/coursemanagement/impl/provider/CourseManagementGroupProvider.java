/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.authz.api.GroupProvider;

/**
 * A Sakai GroupProvider that utilizes the CourseManagementService and the
 * CmMappingService to supply authz data to Sakai.  This implementation uses
 * a list of RoleResolvers, which can be used to resolve a user's role in a section
 * based on memberships in parent objects such as CourseSets.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class CourseManagementGroupProvider implements GroupProvider {
	private static final Log log = LogFactory.getLog(CourseManagementGroupProvider.class);
	
	/** The course management service */
	CourseManagementService cmService;
			
	/** The role resolvers to use when looking for CM roles in the hierarchy*/
	List<RoleResolver> roleResolvers;
	
	/** The ordered list of role preferences.  Roles earlier in the list are preferred to those later in the list. */
	List<String> rolePreferences;
	
	// GroupProvider methods
	
	/**
	 * This method is not longer in use in Sakai.  It should be removed from the
	 * GroupProvider interface.
	 */
	public String getRole(String id, String user) {
		log.error("\n------------------------------------------------------------------\n");
		log.error("THIS METHOD IS NEVER CALLED IN SAKAI.  WHAT HAPPENED???");
		log.error("\n------------------------------------------------------------------\n");
		return null;
	}
		
	/**
	 * Provides a Map of a user ids to (Sakai) roles for a given AuthzGroup.  Since a
	 * user may be both enrolled in a mapped EnrollmentSet and have a Membership
	 * role in a mapped Section, the following order of precedence is applied:
	 * Official Instructor, Enrollment, membership
	 */
	public Map getUserRolesForGroup(String id) {
		if(log.isDebugEnabled()) log.debug("------------------CMGP.getUserRolesForGroup(" + id + ")");
		Map<String, String> userRoleMap = new HashMap<String, String>();
		
		String[] sectionEids = unpackId(id);
		if(log.isDebugEnabled()) log.debug(id + " is mapped to " + sectionEids.length + " sections");

		for(Iterator<RoleResolver> rrIter = roleResolvers.iterator(); rrIter.hasNext();) {
			RoleResolver rr = rrIter.next();

			for(int i=0; i < sectionEids.length; i++) {
				String sectionEid = sectionEids[i];
				Section section = cmService.getSection(sectionEid);
				if(log.isDebugEnabled()) log.debug("Looking for roles in section " + sectionEid);
			
				Map<String, String> rrUserRoleMap = rr.getUserRoles(cmService, section);
				// Only add the roles if the user isn't already in the map.  Earlier resolvers take precedence.
				for(Iterator<String> rrRoleIter = rrUserRoleMap.keySet().iterator(); rrRoleIter.hasNext();) {
					String userEid = rrRoleIter.next();
					String existingRole = userRoleMap.get(userEid);
					String rrRole = rrUserRoleMap.get(userEid);

					// The Role Resolver has found no role for this user
					if(rrRole == null) {
						continue;
					}
					
					// Add or replace the role in the map if this is a more preferred role than the previous role
					if(existingRole == null) {
						if(log.isDebugEnabled()) log.debug("Adding "+ userEid + " to userRoleMap with role=" + rrRole);
						userRoleMap.put(userEid, rrRole);
					} else if(preferredRole(existingRole, rrRole).equals(rrRole)){
						if(log.isDebugEnabled()) log.debug("Changing "+ userEid + "'s role in userRoleMap from " + existingRole + " to " + rrRole + " for section " + sectionEid);
						userRoleMap.put(userEid, rrRole);
					}
				}
			}
		}
		if(log.isDebugEnabled()) log.debug("_____________getUserRolesForGroup=" + userRoleMap);
		return userRoleMap;
	}

	/**
	 * Provides a map of AuthzGroup ids to Sakai roles for a given user.  Enrollment
	 * is overridden by a membership role.
	 */
	public Map getGroupRolesForUser(String userEid) {
		if(log.isDebugEnabled()) log.debug("------------------CMGP.getGroupRolesForUser(" + userEid + ")");
		Map<String, String> groupRoleMap = new HashMap<String, String>();
		
		for(Iterator rrIter = roleResolvers.iterator(); rrIter.hasNext();) {
			RoleResolver rr = (RoleResolver)rrIter.next();
			Map<String, String> rrGroupRoleMap = rr.getGroupRoles(cmService, userEid);
			if(log.isDebugEnabled()) log.debug("Found " + rrGroupRoleMap.size() + " groups for " + userEid + " from resolver " + rr.getClass().getName());

			// Only add the section eids if they aren't already in the map (earlier resolvers take precedence) or if the new role has a higher preference.
			for(Iterator<String> rrRoleIter = rrGroupRoleMap.keySet().iterator(); rrRoleIter.hasNext();) {
				String sectionEid = rrRoleIter.next();
				String existingRole = groupRoleMap.get(sectionEid);
				String rrRole = rrGroupRoleMap.get(sectionEid);

				// The Role Resolver has found no role for this section
				if(rrRole == null) {
					continue;
				}
				
				if(existingRole ==  null) {
					if(log.isDebugEnabled()) log.debug("Adding " + sectionEid + " to groupRoleMap with sakai role" + rrRole + " for user " + userEid);
					groupRoleMap.put(sectionEid, rrRole);
				}  else if(preferredRole(existingRole, rrRole).equals(rrRole)){
					if(log.isDebugEnabled()) log.debug("Changing "+ userEid + "'s role in groupRoleMap from " + existingRole + " to " + rrRole + " for section " + sectionEid);
					groupRoleMap.put(sectionEid, rrRole);
				}
			}
		}
		if(log.isDebugEnabled()) log.debug("______________getGroupRolesForUser=" + groupRoleMap);
		return groupRoleMap;
	}

	/**
	 * {@inheritDoc}
	 */
	public String packId(String[] ids) {
		if(ids == null || ids.length == 0) {
			return null;
		}
		
		if(ids.length == 1) {
			return ids[0];
		}
		
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<ids.length; i++) {
			sb.append(ids[i]);
			if(i < ids.length - 1) {
				sb.append("+");
			}
		}
		return sb.toString();
	}

	public String[] unpackId(String id) {
		if(id == null) {
			return new String[0];
		}
		return id.split("\\+");
	}
	

	// Utility methods

	public void init() {
		if(log.isInfoEnabled()) log.info("initializing " + this.getClass().getName());
	}
	
	public void destroy() {
		if(log.isInfoEnabled()) log.info("destroying " + this.getClass().getName());
	}
	
	// Dependency injection
	
	public void setCmService(CourseManagementService cmService) {
		this.cmService = cmService;
	}

	public void setRoleResolvers(List<RoleResolver> roleResolvers) {
		this.roleResolvers = roleResolvers;
	}
	
	public String preferredRole(String one, String other) {
		int oneIndex = rolePreferences.indexOf(one);
		int otherIndex = rolePreferences.indexOf(other);
		if(otherIndex == -1) {
			return one;
		}
		if(oneIndex == -1) {
			return other;
		}
		return oneIndex < otherIndex ? one : other;
	}

	public void setRolePreferences(List<String> rolePreferences) {
		this.rolePreferences = rolePreferences;
	}
}