/**********************************************************************************
 * $URL: https://newtools.oirt.rutgers.edu:8443/repos/sakai2.x/sakai/trunk/assignment/assignment-impl/impl/src/java/org/sakaiproject/assignment/impl/AssignmentGradeInfoProvider.java $
 * $Id: AssignmentGradeInfoProvider.java 4492 2013-03-22 15:02:00Z willkara $
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.service.gradebook.shared.ExternalAssignmentProvider;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.user.api.User;

import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;


public class LessonsGradeInfoProvider implements ExternalAssignmentProvider {

    private Log log = LogFactory.getLog(LessonsGradeInfoProvider.class);

    // Sakai Service Beans
    private GradebookExternalAssessmentService geaService;
    private SimplePageToolDao dao;
    private AuthzGroupService authzGroupService;
    private SecurityService securityService;

    public void init() {
        log.info("INIT and register LessonsGradeInfoProvider");
        geaService.registerExternalAssignmentProvider(this);
    }

    public void destroy() {
        log.info("DESTROY and unregister LessonsGradeInfoProvider");
        geaService.unregisterExternalAssignmentProvider(getAppKey());
    }

    public String getAppKey() {
        return "Lesson Builder";
    }

    // general note:
    // this code needs to be efficient for large sites
    // I do my best to use bulk operations

    // ids look like lesson-builder:comment:NNN
    // throughout this class you'll see code to find
    // the item from the id by finding the item number
    // at the end and that looking up by ID

    public boolean isAssignmentDefined(String id) {
	int i = id.lastIndexOf(":");
	if (i < 0)
	    return false;
	String itemNum = id.substring(i+1);
	long itemId = 0;
	try {
	    itemId = Long.parseLong(itemNum);
	    if (dao.findItem(itemId) == null)
		return false;
	} catch (Exception e){
	    return false;
	}
	return true;
    }

    // for the moment we're setting grades individually, so say no
    public boolean isAssignmentGrouped(String id) {
	return false;
    }

    public boolean isAssignmentVisible(String id, String userId) {
	int i = id.lastIndexOf(":");
	if (i < 0)
	    return false;
	String itemNum = id.substring(i+1);
	SimplePageItem item = null;
	long itemId = 0;
	try {
	    itemId = Long.parseLong(itemNum);
	    item = dao.findItem(itemId);
	    if (item == null)
		return false;
	} catch (Exception e){
	    return false;
	}

	// there are two things to check. One is whether the user is in the site.
	// the other is whether the item is grouped. If so, is the user in that group

	SimplePage page = dao.getPage(item.getPageId());
	String siteId = page.getSiteId();
	String ref = "/site/" + siteId;
	boolean visible = securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_READ, ref);
	if (!visible)
	    return false;

	// can access the site. If not grouped, we're done.
	String groupString = item.getGroups();
	if (groupString == null || groupString.equals("")) {
	    return true;
	}

	// grouped. See if user is in the group
	String[] groupsArray = groupString.split(",");
	ArrayList<String> groups = new ArrayList<String>();
	for (String groupId: groupsArray)
	    groups.add("/site/" + siteId + "/group/" + groupId);
	List<AuthzGroup> matched = authzGroupService.getAuthzUserGroupIds(groups, userId);
	return (matched.size() > 0);
    }

    public List<String> getExternalAssignmentsForCurrentUser(String gradebookUid) {

	List<String> ret = new ArrayList<String>();

	String ref = "/site/" + gradebookUid;
	boolean visible = securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_READ, ref);
	if (!visible)
	    return ret;

	String userId = UserDirectoryService.getCurrentUser().getId();

	List<String> externalIds = dao.findGradebookIds(gradebookUid);
	for (String externalId : externalIds) {
	    int i = externalId.lastIndexOf(":");
	    if (i < 0)
		continue;

	    String itemNum = externalId.substring(i+1);
	    SimplePageItem item = null;
	    long itemId = 0;
	    try {
		itemId = Long.parseLong(itemNum);
		item = dao.findItem(itemId);
		if (item == null)
		    continue;
	    } catch (Exception e){
		continue;
	    }

	    String groupString = item.getGroups();
	    if (groupString == null || groupString.equals("")) {
		// no group restriction. add this item
		ret.add(externalId);
		continue;
	    }

	    String[] groupsArray = groupString.split(",");
	    ArrayList<String> groups = new ArrayList<String>();
	    for (String groupId: groupsArray)
		groups.add("/site/" + gradebookUid + "/group/" + groupId);
	    List<AuthzGroup> matched = authzGroupService.getAuthzUserGroupIds(groups, userId);
	    if (matched.size() > 0)
		ret.add(externalId);
	}
				       
	return ret;
    }

    public Map<String, List<String>> getAllExternalAssignments(String gradebookUid, Collection<String> studentIds) {
	Map<String,List<String>> allExternals = new HashMap<String, List<String>>();

	String ref = "/site/" + gradebookUid;
	List<User> allowedUsers = securityService.unlockUsers(SimplePage.PERMISSION_LESSONBUILDER_READ, ref);
	if (allowedUsers.size() < 1)
	    return allExternals;  // no users allowed, nothing to do
	List<String> allowedIds = new ArrayList<String>();
	for (User user: allowedUsers)
	    allowedIds.add(user.getId());

	// remove any user without lesson builder read in the site
	studentIds.retainAll(allowedIds);
        for (String studentId : studentIds) {
            allExternals.put(studentId, new ArrayList<String>());
	}

	List<String> externalIds = dao.findGradebookIds(gradebookUid);
	for (String externalId : externalIds) {
	    int i = externalId.lastIndexOf(":");
	    if (i < 0)
		continue;
	    String itemNum = externalId.substring(i+1);
	    SimplePageItem item = null;
	    long itemId = 0;
	    try {
		itemId = Long.parseLong(itemNum);
		item = dao.findItem(itemId);
		if (item == null)
		    continue;
	    } catch (Exception e){
		continue;
	    }
	    String groupString = item.getGroups();
	    if (groupString == null || groupString.equals("")) {
		// no restriction add to all users
		for (String userId : studentIds)
		    if (allExternals.containsKey(userId)) {
			allExternals.get(userId).add(externalId);
		    }
	    } else {
		// restricted to group
		String[] groupsArray = groupString.split(",");
		Set<String> groups = new HashSet<String>();
		for (String groupId: groupsArray)
		    groups.add("/site/" + gradebookUid + "/group/" + groupId);

		// see if anyone on our list is in one of the groups
		// this call is new, but this code is only needed for 2.9.1 and later
		Set<String> okUsers = new HashSet<String>(authzGroupService.getAuthzUsersInGroups(groups));
		okUsers.retainAll(studentIds);
		for (String userId : okUsers)
		    if (allExternals.containsKey(userId)) {
			allExternals.get(userId).add(externalId);
		    }
	    }
	}

	return allExternals;
    }

    public void setGradebookExternalAssessmentService(GradebookExternalAssessmentService geaService) {
        this.geaService = geaService;
    }

    public GradebookExternalAssessmentService getGradebookExternalAssessmentService() {
        return geaService;
    }

    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    public AuthzGroupService getAuthzGroupService() {
        return authzGroupService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSimplePageToolDao(SimplePageToolDao s) {
        dao = s;
    }

}

