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
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.db.api.SqlReader;

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
    // currently using 10 sec. The real goal is to prevent continual
    // reevaluation of items as we follow different paths. I.e. we mostly
    // care about it during a single transaction. But I'm using the normal
    // default of 10 min
    protected static final int DEFAULT_EXPIRATION = 60 * 10;

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
	if (!id.startsWith("lesson-builder:"))
	    return false;
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
	if (!id.startsWith("lesson-builder:"))
	    return false;
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

	// null is a possible value. Because get returns null if something isn't
	// in the cache, use "null" for null. could also check whether it's in the cache
	// but documentation says this is expensive
	Object cached = cache.get(item.getId());
	//	Object cached = null; // for testing
	if (cached != null) {
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

	List<SimplePageItem> externalItems = dao.findGradebookItems(gradebookUid);
	for (SimplePageItem item : externalItems) {

	    Set<String> groupIds = getItemGroups(item);
	    /// System.out.println("item " + item.getId() + " " + groupIds);
	    if (groupIds ==  null) {
		if (item.getGradebookId() != null)
		    ret.add(item.getGradebookId());
		if (item.getAltGradebook() != null)
		    ret.add(item.getAltGradebook());
	    } else {
		ArrayList<String> groups = new ArrayList<String>();
		for (String groupId: groupIds) {
		    System.out.println("groups " + groups);
		    groups.add("/site/" + gradebookUid + "/group/" + groupId);
		}
		List<AuthzGroup> matched = authzGroupService.getAuthzUserGroupIds(groups, userId);
		if (matched.size() > 0) {
		    if (item.getGradebookId() != null)
			ret.add(item.getGradebookId());
		    if (item.getAltGradebook() != null)
			ret.add(item.getAltGradebook());
		}
	    }
	}
				       
	// list of items we have modified the group membership for. We have to override the
	// value returned by the tool itself
	Map<String, ArrayList<String>> otherTools = getExternalAssigns(gradebookUid);
	for (Map.Entry<String, ArrayList<String>> entry: otherTools.entrySet()) {
	    if (entry.getValue() == null)
		ret.add(entry.getKey());
	    else {
		List<AuthzGroup> matched = authzGroupService.getAuthzUserGroupIds(entry.getValue(), userId);
		if (matched.size() > 0)
		    ret.add(entry.getKey());
	    }
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

	List<SimplePageItem> externalItems = dao.findGradebookItems(gradebookUid);
	for (SimplePageItem item: externalItems) {

	    Set<String> groupIds = getItemGroups(item);
	    if (groupIds == null) {
		// no restriction add to all users
		for (String userId : studentIds)
		    if (allExternals.containsKey(userId)) {
			if (item.getGradebookId() != null)
			    allExternals.get(userId).add(item.getGradebookId());
			if (item.getAltGradebook() != null)
			    allExternals.get(userId).add(item.getAltGradebook());
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
			if (item.getGradebookId() != null)
			    allExternals.get(userId).add(item.getGradebookId());
			if (item.getAltGradebook() != null)
			    allExternals.get(userId).add(item.getAltGradebook());
		    }
	    }
	}

	// now handle other tools. If we modified their groups, we need to find the original groups
	// and return the users that match those groups
	// find list of items we've modified
	// map of external ID to list of original groups for that item
	Map<String, ArrayList<String>> otherTools = getExternalAssigns(gradebookUid);
	// for each externalId
	for (Map.Entry<String, ArrayList<String>> entry: otherTools.entrySet()) {
	    String externalId = entry.getKey();
	    // if no group restriction
	    if (entry.getValue() == null) {
		// add this item to all students
		for (String userId : studentIds) 
		    if (allExternals.containsKey(userId))
			allExternals.get(userId).add(externalId);
	    } else {
		// otherwise find users that are in the groups
		Set<String> okUsers = new HashSet<String>(authzGroupService.getAuthzUsersInGroups(new HashSet<String>(entry.getValue())));
		okUsers.retainAll(studentIds);
		// and add this item just to them
		for (String u : okUsers)
		    if (allExternals.containsKey(u)) {
			allExternals.get(u).add(externalId);
		    }
	    }
	}
	return allExternals;
    }

    // for the moment just our own assignments
    public List<String> getAllExternalAssignments(String gradebookUid) {
	List<String> ret = new ArrayList<String>();

	List<SimplePageItem> externalItems = dao.findGradebookItems(gradebookUid);
	for (SimplePageItem item: externalItems) {
	    if (item.getGradebookId() != null)
		ret.add(item.getGradebookId());
	    if (item.getAltGradebook() != null)
		ret.add(item.getAltGradebook());
	}	    

	return ret;
    }

    // return list of items where we've replaced the group with our own
    // returns map externalId -> group list
    public Map<String,ArrayList<String>> getExternalAssigns(String gradebookUid) {
	Map<String,ArrayList<String>> ret = new HashMap<String,ArrayList<String>>();
	
	// should this use the Dao? Not clear that it makes sense in the Lessons dao.
	// I don't see a good approach in gradebook to do this. We could fetch all assignments
	// and check externally maintained, but this is performace critical, so I hate to do that.

	// find the external items in the gradebook. Unfortunately we can have
	// items in the lesson_builder_groups table that aren't in the gradebook
	
	String sql = "select b.external_id from GB_GRADEBOOK_T a, GB_GRADABLE_OBJECT_T b where a.gradebook_uid=? and a.id=b.GRADEBOOK_ID and b.EXTERNALLY_MAINTAINED=1";
	Object[] fields = new Object[1];
	fields[0] = gradebookUid;
	List<String> externalIds = SqlService.dbRead(sql, fields, null);
	if (externalIds == null)
	    return null;
	Set<String> externalIdSet = new HashSet<String>(externalIds);

	// map sakaiId to group list as text
	Map<String,String> externals = dao.getExternalAssigns(gradebookUid);

	for (Map.Entry<String,String> entry: externals.entrySet()) {
	    String sakaiId = entry.getKey();
	    String externalId = null;
	    // only handle item types for which we actually hack on the group membership
	    // internal LB items are handled elsewhere, so this is just assignments and Samigo
	    // assignment 2 and Forums use gradebook items that aren't external, so group
	    // membership isn't relevant. We have to map from format of sakaiId to format
	    // of gradebook's external ID. GB uses just the item number for Samigo, and
	    // the full reference for Assignment
	    if (sakaiId.startsWith("/sam_pub/"))
		externalId = sakaiId.substring("/sam_pub/".length());
	    else if (sakaiId.startsWith("/assignment/"))
		externalId = "/assignment/a/" + gradebookUid + "/" + sakaiId.substring("/assignment/".length());
	    // sam and assignment is all we deal with, so anything else
	    // in new format we skip. Following is old format sakaiid's where
	    // the ID numbers were included with no type information
	    else if (sakaiId.startsWith("/"))
		continue;
	    else if (sakaiId.indexOf("-") >= 0)  // old format assignment
		externalId = "/assignment/a/" + gradebookUid + "/" + sakaiId;
	    else
		externalId = sakaiId; // old format Samigo
	    // in our groups table but not in gradebook, don't need it
	    if (!externalIdSet.contains(externalId))
		continue;
	    
	    // have external ID, get group list
	    ArrayList<String>groups = null;
	    String groupString = entry.getValue();
	    if (groupString != null && !groupString.equals("")) {
		groups = new ArrayList<String>();
		String [] groupArray = groupString.split(",");
		for (String groupId: groupArray) {
		    groups.add("/site/" + gradebookUid + "/group/" + groupId);
		}
	    }
	    ret.put(externalId, groups);
	}
	return ret;
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

