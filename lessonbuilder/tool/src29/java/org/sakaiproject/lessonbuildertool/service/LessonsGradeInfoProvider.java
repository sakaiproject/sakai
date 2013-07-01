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

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.MemoryService;

import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;


public class LessonsGradeInfoProvider implements ExternalAssignmentProvider {

    private Log log = LogFactory.getLog(LessonsGradeInfoProvider.class);

    // caching
    private static Cache cache = null;
    protected static final int DEFAULT_EXPIRATION = 10 * 60;

    // Sakai Service Beans
    private GradebookExternalAssessmentService geaService;
    private SimplePageToolDao dao;
    private AuthzGroupService authzGroupService;
    private SecurityService securityService;
    private MemoryService memoryService;

    public void init() {
	cache = memoryService
	    .newCache("org.sakaiproject.lessonbuildertool.service.LessonsGradeInfoProvider.cache");
        log.info("INIT and register LessonsGradeInfoProvider");
        geaService.registerExternalAssignmentProvider(this);
    }

    public void destroy() {
        log.info("DESTROY and unregister LessonsGradeInfoProvider");
        geaService.unregisterExternalAssignmentProvider(getAppKey());
	cache.destroy();
	cache = null;
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

    // is access to this item restricted by group access
    public boolean isAssignmentGrouped(String id) {
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

	Set<String> groupIds = getItemGroups(item);
	if (groupIds == null)
	    return false;

	return true;
    }

    // my best estimate is about 100 bytes / call. The maximum likely chain is 
    // 100. So that could put 10K on the stack. With 1 G stacks I think that's OK

    /*
      main entry to this is getItemGroups(item, [])
      find all the groups allowed to use this item

      null means no constraints
      [], i.e. empty set, means impossible
    */

    Set<String> getItemGroups (SimplePageItem item) {
	return getItemGroups(item, new HashSet<Long>());
    }

    // find all groups allowed to use this item
    // intersect group directly associated with item
    // and groups allowed to use the page it's on

    Set<String> getItemGroups (SimplePageItem item, Set<Long>seen) {
	/// System.out.println("item " + item.getId() + " groups");

	// null is a possible value. Because get returns null if something isn't
	// in the cache, use "null" for null. could also check whether it's in the cache
	// but documentation says this is expensive
	//	Object cached = cache.get(item.getId());
	Object cached = null; // for testing
	if (cached != null) {
	    System.out.println("returning cached");
	    if (cached instanceof String) // "null"
		return null;
	    return (Set<String>)cached;
	}
	Long itemId = (Long)item.getId();

	if (seen.contains(itemId)) // loop, can't really get there
	    return new HashSet<String>();
	seen.add(itemId); // note that we're in progress, to stop infinte loop

	// if either has no restriction use the other
	// otherwise intersect them

	String itemGroupString = item.getGroups();
	Set<String>itemGroups = null;
	if (itemGroupString != null && itemGroupString.length() > 0)
	    itemGroups = new HashSet<String>(Arrays.asList(itemGroupString.split(",")));
	/// System.out.println("item " + item.getId() + "local groups " + itemGroups);

	Set<String>  pageGroups = getPageGroups(item.getPageId(), seen);
	/// System.out.println("item " + item.getId() + "page groups " + pageGroups);
	if (itemGroups == null)
	    itemGroups = pageGroups;
	else if (pageGroups == null)
            ; //nothing
	else
	    itemGroups.retainAll(pageGroups);
	if (itemGroups == null)
	    cache.put(item.getId(), "null", DEFAULT_EXPIRATION);
	else
	    cache.put(item.getId(), itemGroups, DEFAULT_EXPIRATION);
	/// System.out.println("item " + item.getId() + " returned " + itemGroups);

        // no longer is progress
	seen.remove(itemId);
	return itemGroups;
    }
	    
    // return all group allowed to access this page
    // find all items that point to the page and
    // take the union of groups allowed t use each

    Set<String> getPageGroups(long pageId, Set<Long>seen) {
	/// System.out.println("page " + pageId + " getgroups");
	// if pageid is 0 this is a top level page. No further constraints
	if (pageId == 0)
	    return null;

	Set<String> ret = new HashSet<String>();

	// List of items with this page on it
	List<SimplePageItem> items = dao.findPageItemsBySakaiId(Long.toString(pageId));
	/// System.out.print("page " + pageId + " called from items ");
	/// for (SimplePageItem item: items) {
	///	    System.out.print(item.getId() + " ");
	/// }
	/// System.out.println();

	for (SimplePageItem item: items) {
	    // union all their groups
	    Set<String> pageGroups = getItemGroups(item, seen);

	    /// System.out.println("page " + pageId + "item " + item.getId() + " " + pageGroups);
	    // except if we find one that's unconstrained, the final result is thta
	    if (pageGroups == null)
		return null;
	    ret.addAll(pageGroups);
	 }
	/// System.out.println("page " + pageId + " returns " + ret);
	return ret;
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

	Set<String> groupIds = getItemGroups(item);
	if (groupIds == null)
	    return true;

	ArrayList<String> groups = new ArrayList<String>();
	for (String groupId: groupIds)
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

	    Set<String> groupIds = getItemGroups(item);
	    /// System.out.println("item " + item.getId() + " " + groupIds);
	    if (groupIds ==  null) {
		// no group restriction. add this item
		ret.add(externalId);
		continue;
	    }

	    ArrayList<String> groups = new ArrayList<String>();
	    for (String groupId: groupIds)
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
	    Set<String> groupIds = getItemGroups(item);
	    if (groupIds == null) {
		// no restriction add to all users
		for (String userId : studentIds)
		    if (allExternals.containsKey(userId)) {
			allExternals.get(userId).add(externalId);
		    }
	    } else {
		// restricted to group
		Set<String> groups = new HashSet<String>();
		for (String groupId: groupIds)
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

    public void setMemoryService(MemoryService m) {
	memoryService = m;
    }


}

