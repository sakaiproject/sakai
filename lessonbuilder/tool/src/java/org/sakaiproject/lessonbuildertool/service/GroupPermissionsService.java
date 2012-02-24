/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Charles Hedrick, hedrick@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
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

package org.sakaiproject.lessonbuildertool.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;

/**
 * Sets up and removes group permissions for assignments and tests. This is to be used so that
 * students are not able to access an assignment or test until the Lesson Builder gives them
 * permission.
 * 
 * @author Eric Jeney <jeney@rutgers.edu>
 * 
 */
public class GroupPermissionsService {
	private static Log log = LogFactory.getLog(GroupPermissionsService.class);

        static private LessonEntity forumEntity = null;
        public void setForumEntity(Object e) {
	    forumEntity = (LessonEntity)e;
	}

        static private LessonEntity quizEntity = null;
        public void setQuizEntity(Object e) {
	    quizEntity = (LessonEntity)e;
	}

        static private LessonEntity assignmentEntity = null;
        public void setAssignmentEntity(Object e) {
	    assignmentEntity = (LessonEntity)e;
	}

	public static String makeGroup(String siteId, String title) throws IOException {
		Site site = null;
		AuthzGroup realm = null;

		try {
			site = SiteService.getSite(siteId);
			String realmId = SiteService.siteReference(siteId);
			realm = AuthzGroupService.getAuthzGroup(realmId);
		} catch (Exception e) {
			log.warn("Unable to find site " + siteId + ": " + e);
			return null;
		}

		// see if group exists. must be visible
		Collection<Group> allGroups = site.getGroups();
		for (Group group: allGroups) {
		    if (title.equals(group.getTitle())) {
			if (group.getProperties().getProperty("group_prop_wsetup_created") != null) {
			    return group.getId();
			}
		    }
		}

		// need to copy all site roles into new gruop
		// in theory they should be the same, but if someone has setup
		// non-default roles, then the rule that we copy the user's
		// site role into the group will result in trying to access
		// a non-existent role.
		Set<Role> roles = realm.getRoles();
		Group group = site.addGroup();
		for (Role role : roles) {
			try {
				group.addRole(role.getId(), role);
			} catch (RoleAlreadyDefinedException e) {
				// simpler just to try it and get error
				;
			}
		}

		// do we need to check whether title is unique? I'm hopnig not, since we
		// will generate only one group per test or assignment
		group.setTitle(title);

		// needed to get it to show in the UI
		group.getProperties().addProperty("group_prop_wsetup_created", Boolean.TRUE.toString());

		try {
			SiteService.save(site);
		} catch (IdUnusedException e) {
			log.warn("ID unused", e);
		} catch (PermissionException e) {
			log.warn("Permission Error", e);
		} finally {
			// SecurityService.popAdvisor();
		}

		return group.getId();
	}

	public static boolean addCurrentUser(String siteId, String userid, String groupId) throws IOException {
		try {
			// we want to use the same role in the group that the user
			// has in the main site.
			String realmId = SiteService.siteReference(siteId);
			AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
			String rolename = realm.getMember(userid).getRole().getId();

			groupId = "/site/" + siteId + "/group/" + groupId;

			if (AuthzGroupService.getUserRole(userid, groupId) != null) {
				// Already in group
				return true;
			}

			SecurityService.pushAdvisor(new SecurityAdvisor() {
				public SecurityAdvice isAllowed(String userId, String function, String reference) {
					return SecurityAdvice.ALLOWED;
				}
			});

			AuthzGroupService.joinGroup(groupId, rolename, Integer.MAX_VALUE);

		} catch (Exception e) {
			// Typically means group couldn't be found.
			return false;
		} finally {
			SecurityService.popAdvisor();
		}

		return true;
	}

	public static boolean removeUser(String siteId, String userId, String groupId) throws IOException {
		groupId = "/site/" + siteId + "/group/" + groupId;

		try {
			SecurityService.pushAdvisor(new SecurityAdvisor() {
				public SecurityAdvice isAllowed(String userId, String function, String reference) {
					return SecurityAdvice.ALLOWED;
				}
			});

			AuthzGroupService.unjoinGroup(groupId);

		} catch (Exception e) {
		    // we've got a problem. unjoingroup can fail for maintain users
		    // we don't want to return false, or the caller will recreate the group. 

		    try {
			AuthzGroup group = AuthzGroupService.getAuthzGroup(groupId);
			if (group == null)
			    return false;
			if (group.getMember(userId) == null) {
			    // Already not in group
			    return true;
			}

			group.removeMember(userId);
			
			AuthzGroupService.save(group);
			return true;
		    } catch (Exception ee) {
			return false;
		    }

		} finally {
			SecurityService.popAdvisor();
		}

		return true;
	}

}