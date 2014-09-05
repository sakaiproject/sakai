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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.service.gradebook.shared.ExternalAssignmentProvider;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.lessonbuildertool.service.LessonsAccess;
import org.sakaiproject.lessonbuildertool.service.LessonsAccess.Path;

import java.util.*;


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
    private LessonsAccess lessonsAccess;

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
	String prefix = id.substring(0, i);
	String itemNum = id.substring(i+1);

	if ("lesson-builder".equals(prefix)) {
	    SimplePage page = null;
	    long pageId = 0;
	    try {
		pageId = Long.parseLong(itemNum);
		page = dao.getPage(pageId);
		if (page == null)
		    return false;
	    } catch (Exception e){
		return false;
	    }
	    return true;
	}

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

    boolean isPathSetGrouped(Set<Path> paths) {
	for (Path path: paths) {
	    if (path.groups == null)
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
	String prefix = id.substring(0, i);
	String itemNum = id.substring(i+1);

	if ("lesson-builder".equals(prefix)) {
	    SimplePage page = null;
	    long pageId = 0;
	    try {
		pageId = Long.parseLong(itemNum);
		page = dao.getPage(pageId);
		if (page == null)
		    return false;
	    } catch (Exception e){
		return false;
	    }
	    return isPathSetGrouped(lessonsAccess.getPagePaths(pageId, false));

	}

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

	return isPathSetGrouped(lessonsAccess.getItemPaths(itemId));

    }

    // my best estimate is about 100 bytes / call. The maximum likely chain is 
    // 100. So that could put 10K on the stack. With 1 G stacks I think that's OK

    /*
      main entry to this is getItemGroups(item, [])
      find all the groups allowed to use this item

      null means no constraints
      [], i.e. empty set, means impossible
    */

    boolean isUserInPath(String userId, Set<Path>paths, String siteId) {
	nextpath:
	for (Path path: paths) {
	    if (path.groups == null)  // no constraint
		return true;

	    for (Set<String>groupIds: path.groups) {
		ArrayList<String> groups = new ArrayList<String>();
		for (String groupId: groupIds)
		    groups.add("/site/" + siteId + "/group/" + groupId);
		
		List<AuthzGroup> matched = authzGroupService.getAuthzUserGroupIds(groups, userId);
		// have to at least one
		if (matched.size() < 1)
		    continue nextpath;
	    }
	    // matched all in path
	    return true;
	}
	return false;
    }

    Set<String> usersInPath(Collection<String> userIds, Set<Path>paths, String siteId) {

	Set<String>retUsers = new HashSet<String>();

	for (Path path: paths) {
	    if (path.groups == null)  // no constraint
		return new HashSet<String>(userIds);

	    // users for this path. It's users who are in all the groups
	    Set<String> pathUsers = new HashSet<String>(userIds);

	    for (Set<String>groupIds: path.groups) {
		Set<String> groups = new HashSet<String>();
		for (String groupId: groupIds)
		    groups.add("/site/" + siteId + "/group/" + groupId);
		Set<String> okUsers = new HashSet<String>(authzGroupService.getAuthzUsersInGroups(groups));
		pathUsers.retainAll(okUsers);
	    }

	    // these users are in all path elements, add them to be returned
	    retUsers.addAll(pathUsers);
	}

	return retUsers;
    }

    public boolean isAssignmentVisible(String id, String userId) {
	int i = id.lastIndexOf(":");
	if (i < 0)
	    return false;
	String prefix = id.substring(0, i);
	String itemNum = id.substring(i+1);
	long itemId = 0;
	try {
	    itemId = Long.parseLong(itemNum);
	} catch (Exception e) {
	    return false;
	}

	Set<Path> paths = null;

	// if it's a page rather than an item
	if ("lesson-builder".equals(prefix))
	    paths = lessonsAccess.getPagePaths(itemId, false);
	else {
	    SimplePageItem item = dao.findItem(itemId);
	    paths = lessonsAccess.getItemPaths(itemId);
	    itemId = item.getPageId();
	}

	// itemId is now the pageId, no matter what kind of thing this is

	// there are two things to check. One is whether the user is in the site.
	// the other is whether the item is grouped. If so, is the user in that group

	SimplePage page = dao.getPage(itemId);
	String siteId = page.getSiteId();
	String ref = "/site/" + siteId;
	boolean visible = securityService.unlock(userId, SimplePage.PERMISSION_LESSONBUILDER_READ, ref);
	if (!visible)
	    return false;

	// can access the site. If not grouped, we're done.

	if (!isPathSetGrouped(paths))
	    return true;

	return isUserInPath(userId, paths, siteId);
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
	    Set<Path> paths = lessonsAccess.getItemPaths(item.getId());
	    if (isUserInPath(userId, paths, gradebookUid)) {
		if (item.getGradebookId() != null)
		    ret.add(item.getGradebookId());
		if (item.getAltGradebook() != null)
		    ret.add(item.getAltGradebook());
	    }
	}
				       
	List<SimplePage> externalPages = dao.findGradebookPages(gradebookUid);
	for (SimplePage page : externalPages) {
	    Set<Path> paths = lessonsAccess.getPagePaths(page.getPageId(), false);
	    if (isUserInPath(userId, paths, gradebookUid)) {
		if (page.getGradebookPoints() != null)
		    ret.add("lesson-builder:" + page.getPageId());
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
	//System.out.println("isassignmentgrouped lesson-builder:1788 " + isAssignmentGrouped("lesson-builder:1788"));
	//System.out.println("isassignmentgrouped lesson-builder:comment:7289 " + isAssignmentGrouped("lesson-builder:comment:7289"));
	//System.out.println("isUserInPath " + isUserInPath("c08d3ac9-c717-472a-ad91-7ce0b434f42f", lessonsAccess.getPagePaths(1788L,false),"60c04ab8-40e5-4eb6-9f8d-7006ed023109"));
	//System.out.println("isUserInPath " + isUserInPath("c08d3ac9-c717-472a-ad91-7ce0b434f42f", lessonsAccess.getItemPaths(7289L),"60c04ab8-40e5-4eb6-9f8d-7006ed023109"));
	//HashSet<String> userset = new HashSet<String>();
	//userset.add("c08d3ac9-c717-472a-ad91-7ce0b434f42f");
	//userset.add("9d1a25ba-4735-48c4-bd5e-ff329f9f7749");

	//System.out.println("usersInPath " + usersInPath(userset, lessonsAccess.getPagePaths(1788L,false),"60c04ab8-40e5-4eb6-9f8d-7006ed023109"));
	//System.out.println("userInPath " + usersInPath(userset, lessonsAccess.getItemPaths(7289L),"60c04ab8-40e5-4eb6-9f8d-7006ed023109"));
	//System.out.println(isAssignmentVisible("lesson-builder:1788", "c08d3ac9-c717-472a-ad91-7ce0b434f42f"));
	//System.out.println(isAssignmentVisible("lesson-builder:comment:7289", "c08d3ac9-c717-472a-ad91-7ce0b434f42f"));
	//System.out.println(getExternalAssignmentsForCurrentUser("60c04ab8-40e5-4eb6-9f8d-7006ed023109"));

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

	    Set<Path> paths = lessonsAccess.getItemPaths(item.getId());
	    Set<String> users = usersInPath(studentIds, paths, gradebookUid);

	    // add this assignment to all users that are in the groups
	    for (String userId : users)
		if (allExternals.containsKey(userId)) {
		    if (item.getGradebookId() != null)
			allExternals.get(userId).add(item.getGradebookId());
		    if (item.getAltGradebook() != null)
			allExternals.get(userId).add(item.getAltGradebook());
		}

	}

	List<SimplePage> externalPages = dao.findGradebookPages(gradebookUid);
	for (SimplePage page: externalPages) {

	    Set<Path> paths = lessonsAccess.getPagePaths(page.getPageId(), false);
	    Set<String> users = usersInPath(studentIds, paths, gradebookUid);

	    // add this assignment to all users that are in the groups
	    for (String userId : users)
		if (allExternals.containsKey(userId)) {
		    if (page.getGradebookPoints() != null)
			allExternals.get(userId).add("lesson-builder:" + page.getPageId());
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
	//System.out.println("getAllExternalAssignments " + studentIds + " " + allExternals);

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
	List<SimplePage> externalPages = dao.findGradebookPages(gradebookUid);
	for (SimplePage page: externalPages) {
	    if (page.getGradebookPoints() != null)
		ret.add("lesson-builder:" + page.getPageId());
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
    
    public void setLessonsAccess(LessonsAccess l) {
	lessonsAccess = l;
    }

}

