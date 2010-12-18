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
 *       http://www.osedu.org/licenses/ECL-2.0
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
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentEdit;
import org.sakaiproject.assignment.cover.AssignmentService;
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
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;

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

			AuthzGroupService.joinGroup(groupId, rolename);

		} catch (Exception e) {
			// Typically means group couldn't be found.
			return false;
		} finally {
			SecurityService.popAdvisor();
		}

		return true;
	}

	public static boolean removeUser(String siteId, String groupId) throws IOException {
		groupId = "/site/" + siteId + "/group/" + groupId;

		try {
			SecurityService.pushAdvisor(new SecurityAdvisor() {
				public SecurityAdvice isAllowed(String userId, String function, String reference) {
					return SecurityAdvice.ALLOWED;
				}
			});

			AuthzGroupService.unjoinGroup(groupId);

		} catch (Exception e) {
			// Typically means group couldn't be found.
			return false;
		} finally {
			SecurityService.popAdvisor();
		}

		return true;
	}

	private static boolean addAssessmentControl(String assessmentId, String siteId, String groupId) throws IOException {
		PublishedAssessmentService assessmentService = new PublishedAssessmentService();
		PublishedAssessmentFacade assessment = null;
		AssessmentAccessControlIfc control = null;

		try {
			assessment = assessmentService.getPublishedAssessment(assessmentId);
			control = assessment.getAssessmentAccessControl();
		} catch (Exception e) {
			log.warn("can't find published " + assessmentId, e);
			return false;
		}

		AuthzQueriesFacadeAPI authz = PersistenceService.getInstance().getAuthzQueriesFacade();

		if (authz == null) {
			log.warn("Null Authorization");
			return false;
		}
		if (!control.getReleaseTo().equals(AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS)) {
			control.setReleaseTo(AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS);
			assessmentService.saveAssessment(assessment);
			String qualifierIdString = assessment.getPublishedAssessmentId().toString();

			// the original one lists the site. once we set release to groups, it will try to look
			// up the site id as a group id. very bad, so remove all existing ones.
			authz.removeAuthorizationByQualifierAndFunction(qualifierIdString, "TAKE_PUBLISHED_ASSESSMENT");

			// and add our group
			authz.createAuthorization(groupId, "TAKE_PUBLISHED_ASSESSMENT", assessmentId);
		} else {
			// already release to groups. see if we need to add our group
			List<AuthorizationData> authorizations = authz.getAuthorizationByFunctionAndQualifier("TAKE_PUBLISHED_ASSESSMENT", assessmentId);
			boolean found = false;

			for (AuthorizationData ad : authorizations) {
				if (ad.getAgentIdString().equals(groupId)) {
					found = true;
					break;
				}
			}

			// if not, add it; can't add it otherwise or we get duplicates
			if (!found) {
				authz.createAuthorization(groupId, "TAKE_PUBLISHED_ASSESSMENT", assessmentId);
			}
		}

		return true;
	}

	private static boolean removeAssessmentControl(String assessmentId, String siteId, String groupId) throws IOException {
		PublishedAssessmentService assessmentService = new PublishedAssessmentService();
		PublishedAssessmentFacade assessment = null;
		AssessmentAccessControlIfc control = null;

		try {
			assessment = assessmentService.getPublishedAssessment(assessmentId);
			control = assessment.getAssessmentAccessControl();
		} catch (Exception e) {
			return false;
		}

		AuthzQueriesFacadeAPI authz = PersistenceService.getInstance().getAuthzQueriesFacade();

		if (!control.getReleaseTo().equals(AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS)) {
			// not release to groups, nothing to do
			return true;
		} else {
			// what do we do if it was originally released to groups, and then we added ours? I
			// guess jsut remove ours?
			List<AuthorizationData> authorizations = authz.getAuthorizationByFunctionAndQualifier("TAKE_PUBLISHED_ASSESSMENT", assessmentId);
			boolean foundother = false;
			for (AuthorizationData ad : authorizations) {
				if (ad.getAgentIdString().equals(groupId)) {} else {
					foundother = true;
				}
			}

			if (foundother) {
				// just remove our group
				authz.removeAuthorizationByAgentQualifierAndFunction(groupId, assessmentId, "TAKE_PUBLISHED_ASSESSMENT");
			} else {
				Site site = null;

				try {
					site = SiteService.getSite(siteId);
				} catch (Exception e) {
					return false;
				}

				// otherwise remove all groups
				authz.removeAuthorizationByQualifierAndFunction(assessmentId, "TAKE_PUBLISHED_ASSESSMENT");

				// put back the site
				authz.createAuthorization(siteId, "TAKE_PUBLISHED_ASSESSMENT", assessmentId);

				// and put back the access control
				control.setReleaseTo(site.getTitle()); // what if it's too long?

				// and save the updated info
				assessmentService.saveAssessment(assessment);
			}
		}

		return true;
	}

	private static boolean addAssignmentControl(String ref, String siteId, String groupId) throws IOException {
		Site site = null;
		ref = "/assignment/a/" + siteId + "/" + ref;

		try {
			site = SiteService.getSite(siteId);
		} catch (Exception e) {
			log.warn("Unable to find site " + siteId, e);
			return false;
		}

		AssignmentEdit edit = null;

		try {
			edit = AssignmentService.editAssignment(ref);
		} catch (IdUnusedException e) {
			log.warn("ID unused ", e);
			return false;
		} catch (PermissionException e) {
			log.warn(e);
			return false;
		} catch (InUseException e) {
			log.warn(e);
			return false;
		}

		boolean doCancel = true;

		try {
			// need this to make sure we always unlock

			if (edit.getAccess() == Assignment.AssignmentAccess.GROUPED) {
				Collection<String> groups = edit.getGroups();
				groupId = "/site/" + siteId + "/group/" + groupId;

				if (groups.contains(groupId)) {
					return true;
				}

				Group group = site.getGroup(groupId);
				if (group == null) {
					return false;
				}

				// odd; getgruops returns a list of string
				// but setgroupacces wants a collection of actual groups
				// so we have to copy the list
				Collection<Group> newGroups = new ArrayList<Group>();
				for (String gid : groups) {
					newGroups.add(site.getGroup(gid));
				}

				// now add in this one
				newGroups.add(group);

				try {
					edit.setGroupAccess(newGroups);
				} catch (PermissionException e) {
					log.warn(e);
					return false;
				}

				AssignmentService.commitEdit(edit);
				doCancel = false;
				return true;

			} else {
				// currently not grouped
				Collection groups = new ArrayList<String>();
				Group group = site.getGroup(groupId);

				if (group == null) {
					log.warn("Could not find Group");
					return false;
				}

				groups.add(group);

				try {
					// this change mode to grouped
					edit.setGroupAccess(groups);
				} catch (PermissionException e) {
					log.warn(e);
					return false;
				}

				AssignmentService.commitEdit(edit);
				doCancel = false;
				return true;
			}
		} catch (Exception e) {
			log.warn(e);
			return false;
		} finally {
			if (doCancel) {
				AssignmentService.commitEdit(edit);
			}
		}

	}

	private static boolean removeAssignmentControl(String ref, String siteId, String groupId) throws IOException {
		Site site = null;
		ref = "/assignment/a/" + siteId + "/" + ref;
		try {
			site = SiteService.getSite(siteId);
		} catch (Exception e) {
			log.warn("Unable to find site " + siteId, e);
			return false;
		}

		AssignmentEdit edit = null;

		try {
			edit = AssignmentService.editAssignment(ref);
		} catch (IdUnusedException e) {
			log.warn(e);
			return false;
		} catch (PermissionException e) {
			log.warn(e);
			return false;
		} catch (InUseException e) {
			log.warn(e);
			return false;
		}

		boolean doCancel = true;

		try {
			// need this to make sure we always unlock

			if (edit.getAccess() == Assignment.AssignmentAccess.GROUPED) {
				Collection<String> groups = edit.getGroups();
				groupId = "/site/" + siteId + "/group/" + groupId;

				if (!groups.contains(groupId)) {
					// nothing to do
					return true;
				}

				// odd; getgruops returns a list of string
				// but setgroupacces wants a collection of actual groups
				// so we have to copy the list
				Collection<Group> newGroups = new ArrayList<Group>();
				for (String gid : groups) {
					// remove our group
					if (!gid.equals(groupId)) {
						newGroups.add(site.getGroup(gid));
					}
				}

				if (newGroups.size() > 0) {
					// there's groups left, just remove ours
					try {
						edit.setGroupAccess(newGroups);
					} catch (PermissionException e) {
						log.warn(e);
						return false;
					}
				} else {
					// no groups left, put site access back
					edit.setAccess(Assignment.AssignmentAccess.SITE);
					edit.clearGroupAccess();
				}

				AssignmentService.commitEdit(edit);
				doCancel = false;
				return true;

			} else {
				// currently not grouped
				// nothing to do

				return true;
			}

		} catch (Exception e) {
			log.warn(e);
			return false;
		} finally {
			if (doCancel) {
				AssignmentService.commitEdit(edit);
			}
		}

	}

	public static boolean addControl(String ref, String siteId, String groupId, int type) throws IOException {
		if (type == SimplePageItem.ASSESSMENT) {
			return addAssessmentControl(ref, siteId, groupId);
		} else if (type == SimplePageItem.ASSIGNMENT) {
			return addAssignmentControl(ref, siteId, groupId);
		} else if (type == SimplePageItem.FORUM) {
			LessonEntity entity = forumEntity.getEntity(ref);
			if (entity == null)
			    return false;
			else
			    return entity.addEntityControl(siteId, groupId);
		} else {
			return false;
		}
	}

	public static boolean removeControl(String ref, String siteId, String groupId, int type) throws IOException {
		if (type == SimplePageItem.ASSESSMENT) {
			return removeAssessmentControl(ref, siteId, groupId);
		} else if (type == SimplePageItem.ASSIGNMENT) {
			return removeAssignmentControl(ref, siteId, groupId);
		} else if (type == SimplePageItem.FORUM) {
			LessonEntity entity = forumEntity.getEntity(ref);
			if (entity == null)
			    return false;
			else
			    return entity.removeEntityControl(siteId, groupId);
		} else {
			return false;
		}
	}
}