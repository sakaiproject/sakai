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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
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
@Slf4j
public class GroupPermissionsService {
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

        static private LessonEntity bltiEntity = null;
        public void setBltiEntity(Object e) {
	    bltiEntity = (LessonEntity)e;
        }


	public void init () {
	    convertGroupsTable();
	}

	public static String makeGroup(String siteId, String title, String oldTitle, String ref, SimplePageBean simplePageBean) throws IOException {
		Site site = null;
		AuthzGroup realm = null;

		try {
			site = SiteService.getSite(siteId);
			String realmId = SiteService.siteReference(siteId);
		} catch (Exception e) {
			log.warn("Unable to find site " + siteId + ": " + e);
			return null;
		}

		// see if group exists. must be visible
		Collection<Group> allGroups = site.getGroups();
		String retId = null;
		for (Group group: allGroups) {
		    // This is used by CC code to create groups having nothign to do with access control.
		    // In that case ref == null.
		    if (ref != null) {
			// unfortunately old groups won't have this. If it's there, use it, 
			// otherwise use title
			String groupRef = group.getProperties().getProperty("lessonbuilder_ref");
			if (groupRef != null) {
			    if (groupRef.equals(ref))
				return group.getId();
			    else 
				continue;
			}
		    }
		    // if the group was created by pre-10.0 code, it won't have the property.
		    // In that case match the title. But use a title generated as we used
		    // to generate them, i.e non-internationalized, since that's what would be there
		    if (oldTitle.equals(group.getTitle())) {
			if (group.getProperties().getProperty("group_prop_wsetup_created") != null) {
			    retId = group.getId();
			}
		    }
		}
		// no matching groupRef, did we find something with the right title?
		if (retId != null)
		    return retId;

		// need to copy all site roles into new gruop
		// in theory they should be the same, but if someone has setup
		// non-default roles, then the rule that we copy the user's
		// site role into the group will result in trying to access
		// a non-existent role.
		Set<Role> roles = site.getRoles();
		Group group = site.addGroup();
		for (Role role : roles) {
			try {
				group.addRole(role.getId(), role);
			} catch (RoleAlreadyDefinedException e) {
				// simpler just to try it and get error
				;
			}
		}

		// do we need to check whether title is unique? In theory we can
		// create 2 goups with the same title, e.g. if we have to truncate the title
		// since we match based on the property, this is weird but acceptable
		group.setTitle(title);

		// this is the key we actually use
		if (ref != null)
		    group.getProperties().addProperty("lessonbuilder_ref", ref);
		// needed to get it to show in the UI
		group.getProperties().addProperty("group_prop_wsetup_created", Boolean.TRUE.toString());
		try {
			SiteService.save(site);
			// clear cached current site
			simplePageBean.clearCurrentSite();
		} catch (IdUnusedException e) {
			log.warn("ID unused", e);
		} catch (PermissionException e) {
			log.warn("Permission Error", e);
		}

		return group.getId();
	}

	public static boolean addCurrentUser(String siteId, String userid, String groupId) throws IOException {
		AuthzGroupService authzGroupService = ComponentManager.get(AuthzGroupService.class);
		SecurityService securityService = ComponentManager.get(SecurityService.class);

		SecurityAdvisor addUserAdvisor = new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference) {
				return SecurityAdvice.ALLOWED;
			}
		};

		try {
			// we want to use the same role in the group that the user
			// has in the main site.
			String realmId = SiteService.siteReference(siteId);
			AuthzGroup realm = authzGroupService.getAuthzGroup(realmId);
			String rolename = realm.getMember(userid).getRole().getId();

			groupId = "/site/" + siteId + "/group/" + groupId;

			if (authzGroupService.getUserRole(userid, groupId) != null) {
				// Already in group
				return true;
			}

			securityService.pushAdvisor(addUserAdvisor);
			authzGroupService.joinGroup(groupId, rolename, Integer.MAX_VALUE);
		} catch (Exception e) {
			// Typically means group couldn't be found.
			return false;
		} finally {
			securityService.popAdvisor(addUserAdvisor);
		}

		return true;
	}

	public static boolean removeUser(String siteId, String userId, String groupId) throws IOException {
		AuthzGroupService authzGroupService = ComponentManager.get(AuthzGroupService.class);
		SecurityService securityService = ComponentManager.get(SecurityService.class);
		groupId = "/site/" + siteId + "/group/" + groupId;

		SecurityAdvisor unjoinAdvisor = new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference) {
				return SecurityAdvice.ALLOWED;
			}
		};

		try {
			securityService.pushAdvisor(unjoinAdvisor);
			authzGroupService.unjoinGroup(groupId);
		} catch (Exception e) {
		    // we've got a problem. unjoingroup can fail for maintain users
		    // we don't want to return false, or the caller will recreate the group. 

		    try {
			AuthzGroup group = authzGroupService.getAuthzGroup(groupId);
			if (group == null)
			    return false;
			if (group.getMember(userId) == null) {
			    // Already not in group
			    return true;
			}

			group.removeMember(userId);
			
			authzGroupService.save(group);
			return true;
		    } catch (Exception ee) {
			return false;
		    }

		} finally {
			securityService.popAdvisor(unjoinAdvisor);
		}

		return true;
	}

        public LessonEntity getEntity(String sakaiId) {
	    LessonEntity lessonEntity = null;
	    String prefix = null;
	    int i = sakaiId.indexOf("/",1);
	    if (i > 0) {
		prefix = sakaiId.substring(1, i);
		log.info("prefix " + prefix);
		if (prefix.equals(LessonEntity.ASSIGNMENT) ||
		    prefix.equals(LessonEntity.ASSIGNMENT2))
		    lessonEntity = assignmentEntity.getEntity(sakaiId);
		else if (prefix.equals(LessonEntity.SAM_PUB) ||
			 prefix.equals(LessonEntity.MNEME))
		    lessonEntity = quizEntity.getEntity(sakaiId);
		else if (prefix.equals(LessonEntity.FORUM_TOPIC) ||
			 prefix.equals(LessonEntity.JFORUM_TOPIC) ||
			 prefix.equals(LessonEntity.YAFT_TOPIC))
		    lessonEntity = forumEntity.getEntity(sakaiId);
		else if (prefix.equals(LessonEntity.BLTI))
		    lessonEntity = bltiEntity.getEntity(sakaiId);			 
		else
		    return null;
	    } else {
		// old format
		if (sakaiId.indexOf("-") >= 0)
		    // id with - in it is assignment
		    lessonEntity = assignmentEntity.getEntity(sakaiId);		    
		else
		    // without - it is Samigo
		    lessonEntity = quizEntity.getEntity(sakaiId);
	    }		    

	    return lessonEntity;
	}

	// this is a one-time conversion. It seeems silly to add this query to the Dao
	// the code is here rather than components to avoid a possible dependency loop. It's not a good idea
	// for tool components to refer to each other
	void convertGroupsTable() {
	    // find entries created without siteId
	    // because this is only needed for old entries, don't need to update the code if we add providers
	    // for additional tools. This query should be immediate if conversion is done
	    List <String> sakaiIds = SqlService.dbRead("select itemId from lesson_builder_groups where siteId is null", null, null);                                              
	    if (sakaiIds == null)
		return;

	    SecurityService securityService = ComponentManager.get(SecurityService.class);
	    SecurityAdvisor convertGroupsAdvisor = new SecurityAdvisor() {
		    public SecurityAdvice isAllowed(String userId, String function, String reference) {
			return SecurityAdvice.ALLOWED;
		    }
	    };

	    try {
		securityService.pushAdvisor(convertGroupsAdvisor);
		for (String sakaiId: sakaiIds) {
		    String siteId = null;
		    log.info("sakaiid " + sakaiId);
		    LessonEntity lessonEntity = getEntity(sakaiId);
		    log.info("entity " + lessonEntity);
		    if (lessonEntity != null) {
			siteId = lessonEntity.getSiteId();
			log.info("siteid " + siteId);
		    }
		    if (siteId == null)
			siteId = "--";

		    // if we can't find a siteId set it to -- so we don't keep back trying to handle the entry

		    Object [] fields = new String[2];
		    fields[0] = siteId;
		    fields[1] = sakaiId;
		    
		    SqlService.dbWrite("update lesson_builder_groups set siteId = ? where itemId = ?", fields);
		}		
	    } finally {
		securityService.popAdvisor(convertGroupsAdvisor);
	    }
	}

}
