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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimpleStudentPage;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

// This class is used to check whether a page or item should be
// accessible for the user. It checks all conditions. SimplePageBean
// has individual checks for them, but this puts them all together,
// for use in services where individual checks aren't needed.
// In general this will be used by services such as /access or /direct
@Slf4j
public class LessonsAccess {
    // caching
    private static Cache<String, Set<?>> cache = null;
    // currently using 10 sec. The real goal is to prevent continual
    // reevaluation of items as we follow different paths. I.e. we mostly
    // care about it during a single transaction. But I'm using the normal
    // default of 10 min
    protected static final int DEFAULT_EXPIRATION = 60 * 10;
    // I have tested with useCache = true. I believe it works.
    // But there's been such a long history of problems caused by it that I
    // it's safest to leave it off for 2.10
    static final boolean useCache = true;

    // Sakai Service Beans
    private SimplePageToolDao dao;
    private AuthzGroupService authzGroupService;
    private SecurityService securityService;
    private MemoryService memoryService;
    private UserDirectoryService userDirectoryService;

    public void init() {
	if (useCache) {
	cache = memoryService
	    .getCache("org.sakaiproject.lessonbuildertool.service.LessonsAccess.cache");
	}
        log.info("init()");
    }

    public void destroy() {
        log.info("destroy()");
	if (useCache) {
	cache.close();
	cache = null;
	}
    }

    // my best estimate is about 100 bytes / call. The maximum likely chain is 
    // 100. So that could put 10K on the stack. With 1 G stacks I think that's OK

    /*
      main entry to this is getItemPaths(item, [])

      null means no constraints
      [], i.e. empty set, means impossible
      otherwise return a list of paths to get to it. Only one needs to work
      pageId == null if path has no prerequisites
    */
    
    /*

      A path represents all the group constraints on getting to an item.
      Generally there's more than one way you can get to a page. Each way represents
      a path through pages and subpages. To get to the item you have to pass the
      group restrictions for every page in the path. However if there's more than
      one path, any path will work. 

      There are two sets of methods to apply a path set. The one here has to check
      not just group constraints but prerequisites. The one in LessonsGradeInfoProvider 
      is purely group constraints.

      itemId is used only when prerequisites are being checked. In tracing the path,
      if we find a page has prerequesites, we put its item Id is this field and stop
      tracing the path. isPageAccessible will check whether the user has actually
      accessed that page. If so, we assume it is accessible. That's a lot cheaper than
      doing the actual prerequisiet checking, and good enough for the places we're 
      using this code. We prefer to let users explore the site, so if there aren't any
      prerequsites, we can afford a full check of accessibility. But if there are prerequisites, we simplify
      our job. That won't let them look ahead, unfortunately, but that's inherent in using
      prerequisites.

      Note that the path is independent of user, so we can usefully cache the values.

    */

    public class Path implements Cloneable{
	Long itemId;
	boolean topLevel;
	ArrayList<Set<String>>groups;
    }

    /* 
     * clone a set of paths.
     * The code in getPagePaths and getItemPaths modifies paths.
     * For that reason when we pull something from the cache we have to clone it,
     *   so changes aren't made to the cached copy
     * Similarly, we have to clone the copy we put in the cache, since other code
     *   is going to modify the copy we return
     */

    public Set<Path>clonePath(Set<Path> paths) {
	if (paths == null)
	    return null;

	Set<Path> ret = new HashSet<Path>();

	for (Path path:paths) {
	    Path newPath = new Path();
	    newPath.itemId = path.itemId;
	    newPath.topLevel = path.topLevel;
	    if (path.groups == null)
		newPath.groups = null;
	    else {
		newPath.groups = new ArrayList<Set<String>>();
		for (Set<String>groupIds: path.groups) {
		    Set<String>newGroups = new HashSet<String>();
		    for (String group: groupIds)
			newGroups.add(group);
		    newPath.groups.add(newGroups);
		}
	    }
	    ret.add(newPath);
	}
	
	return ret;
    }


    public String printPath(Set<Path> paths) {
	String ret = "";
	if (paths == null)
	    return "null";
	for (Path path: paths) {
	    if (ret.length() > 0) {
		ret = ret + ",";
	    }
	    if (path.groups == null)
		ret = ret + "null";
	    else
		ret = ret + path.groups.toString();
	}
	return ret;
    }

    // the code for access is broken into 3 parts:
    //
    // 1) getPagePaths returns accessibility data that is the same for all users.
    // that means that we can reasonably cache it, and get a good performance improvement.
    //
    // 2) isPageAccessible interprets the results of getPathPaths for a given user.
    //
    // 3) isItemAccessible checks that the containing page is accessible, and then
    // calls the normal available and visible tests from SimplePageBean. In a lot of
    // code those tests will be used directly, so this class will be used only
    // for isPageAccessible. But for the /access and /direct handlers, this is
    // the right way to check items.


    public Set<Path> getPagePaths (long pageId) {
	return getPagePaths(pageId, new HashSet<Long>(), true);
    }

    public Set<Path> getPagePaths (long pageId, Set<Long>seen) {
	return getPagePaths(pageId, seen, true);
    }

    public Set<Path> getPagePaths(long pageId, boolean usePrerequisites) {
	return getPagePaths(pageId, new HashSet<Long>(), usePrerequisites);
    }

    // usePrerequisites is normally used. However for GradebookInfo, we just
    // use group restrictions. We count an item for the student even if 
    // they can't get there yet because of prerequisites
    //   Cache only if usePrerequisites = false. We can't cache both or the cache
    // can have more than one value for a given page, and it's really the case
    // without we care about because of the gradebook
    Set<Path> getPagePaths(long pageId, Set<Long>seen, boolean usePrerequisites) {
	// if pageid is 0 this is a top level page. No further constraints
	Set<Path> ret = new HashSet<Path>();
	if (pageId == 0) {
	    Path path = new Path();
	    path.groups = null;
	    path.topLevel = false;
	    path.itemId = null;
	    ret.add(path);
	    return ret;
	}

	SimplePage page = dao.getPage(pageId);
	if (page == null) {// should be impossible
	    return ret;
	}

	// if we've seen it already, we're pursuing a path that goes back through the same page.
	// this can't affect the outcome, but will cause infinite recursion
	if (seen.contains(pageId)) {
	    return ret;
	}

	if (useCache && !usePrerequisites) {
	    Set<Path> cached = (Set<Path>)cache.get(Long.toString(pageId));
	    // need to copy the cached object because some of the code changes it
	    // unfortunately clone of a hashset is shallow, so have to do this ourselves
	    if (cached != null) {
		return clonePath(cached);
	    }
	}

	if (usePrerequisites && 
	    (page.isHidden() || (page.getReleaseDate() != null && page.getReleaseDate().after(new Date())))) {
	    // not released. Say inaccessible. The assumption is that this is being used only
	    // for students. Obviously the instructor can bypass release control.
	    return ret;
	}	    

	// List of items with this page on it
	List<SimplePageItem> items = null;

	// if it's a student page, have to handle specially. Find item that has the student content section
	// if usePrerequistes false, this can't happen, since no graded items will occur there
	if (page.getOwner() != null && !page.isOwned()) {
	    SimpleStudentPage student = dao.findStudentPage(page.getTopParent());
	    SimplePageItem item = dao.findItem(student.getItemId());
	    items = new ArrayList<SimplePageItem>();
	    items.add(item);
	} else
	    items = dao.findPageItemsBySakaiId(Long.toString(pageId));

	seen.add(pageId);

	for (SimplePageItem item: items) {
	    // union all their groups
	    String itemGroupString = item.getGroups();
	    Set<String>itemGroups = null;
	    if (itemGroupString != null && itemGroupString.length() > 0)
		itemGroups = new HashSet<String>(Arrays.asList(itemGroupString.split(",")));

	    if (usePrerequisites && item.isPrerequisite()) {
		// we don't do a recursive call for groups. because of the restriction on prerequisites,
		// we will only allow access to this page if there is a log entry. But in that case
		// we don't need further tests because the user had at some point gotten to the containing
		// page. Note that a log entry will be created when the containing page is shown, as long
		// as the page is marked has having prerequisites and they are satisfied. So log entry
		// is a good test for prerequisites, and a lot less work than the real test.
		Path path = new Path();
		path.itemId = item.getId();
		path.topLevel = (item.getPageId() == 0);
		if (itemGroups != null) {
		    path.groups = new ArrayList<Set<String>>();
		    path.groups.add(itemGroups);
		}
		// TODO: check for duplicates
		ret.add(path);
	    } else {
		Set<Path> paths = getPagePaths(item.getPageId(), seen);
		for (Path path: paths) {
		    // the values will end up cached. That means we can't
		    // modify the values in place, but have to copy them first
		    path = path;
		    if (itemGroups == null)
			; // use path.groups as is
		    else if (path.groups == null) {
			path.groups = new ArrayList<Set<String>>();
			path.groups.add(itemGroups);
		    } else {
			// note: we can't modify that set in path.groups, because it coudl be shared
			// with other copies of this Path
			path.groups.add(itemGroups);  // add to list or constraints, i.e. you have to be in all the groups
		    }
		    ret.add(path);
		}
	    }
	}

	seen.remove(pageId);

	// only cache final results, not intermediate computations
	// in some situations with loops the intermediate calculations can be wrong
	if (useCache && !usePrerequisites && seen.size() == 0)
	    cache.put(Long.toString(pageId), clonePath(ret));
	return ret;
    }

    /* for the moment, no one calling this needs prerequsite checking */

    public Set<Path> getItemPaths(long itemId) {

	SimplePageItem item = dao.findItem(itemId);

	String itemGroupString = item.getGroups();
	Set<String>itemGroups = null;
	if (itemGroupString != null && itemGroupString.length() > 0)
	    itemGroups = new HashSet<String>(Arrays.asList(itemGroupString.split(",")));
	
	long pageId = item.getPageId();
	
	Set<Path> paths = getPagePaths(pageId, false);

	for (Path path: paths) {
	    if (itemGroups == null)
		; // leave path.groups as is
	    else if (path.groups == null) {
		path.groups = new ArrayList<Set<String>>();
		path.groups.add(itemGroups);
	    } else
		path.groups.add(itemGroups);
	}

	return paths;
    }

    // in testing this code, not that ispageaccessible will sometimes return true
    // when testing isitemaccessible for an item pointing to the page will not.
    // there are two reasons:
    // * ispageaccessible will check all items that point to the page. Isitemaccessible
    // will just check one of them.
    // * for a page that is release controlled, ispageaccessible will check whether
    // there is a log entry for that page. Isitemaccessible will check the actual
    // condition. This is because doing the full check for every item in multiple paths
    // is too expensive. The test in ispageaccessible allows any access that will happen
    // in practice

    // note that this doesn't check site permissions. lb.write won't need this
    // check at all. lb.read needs to be true or this test is irrelevant.
    // I assume this method will mostly be used by code that wants to do item
    // checks on its own, so most likely it's already doign the permissions checks

    /* this always needs prerequisite checking */

    public boolean isPageAccessible(long pageId, String siteId, String currentUserId, SimplePageBean simplePageBean) {

	Set<Path> paths = getPagePaths(pageId);

	nextpath:
	for (Path path: paths) {
	    if (path.itemId != null) {
		// page needs to be marked available. When the containing page
		// finds that is accessible a dummy entry is created. So existence
		// of an entry, dummy or real means it is accessible.

		if (path.topLevel) {
		    // top level item. special code that checks tools
		    // if no prerequisites, top level items are OK
		    // groups will be checked below, though in fact top level items
		    // can't be restricted by group
		    SimplePageItem item = dao.findItem(path.itemId);
		    if (item.isPrerequisite()){
			SimplePage currentPage = null;
			if (simplePageBean == null) {
			    String pageString = item.getSakaiId();
			    long pageNum = 0;
			    try {
				pageNum = Long.parseLong(pageString, 10);
			    } catch (Exception e) {
				continue;
			    }
			    currentPage = dao.getPage(pageNum);
			    if (currentPage == null)
				continue;
			}

			simplePageBean = makeSimplePageBean(simplePageBean, siteId, currentPage);

			List<SimplePageItem> items = dao.findItemsInSite(siteId);
			// sorted by SQL

			boolean ok = true;
			for (SimplePageItem i : items) {
			    if (i.getId() == path.itemId)
				break; //ok
			    if (i.isRequired() && !simplePageBean.isItemComplete(i) && simplePageBean.isItemVisible(i)) {
				ok = false;
				break;
			    }
			}
			if (!ok) {
			    continue;
			}
		    }
		} else if (dao.getLogEntry(currentUserId, path.itemId, -1L) == null) {
		    continue;
		}

	    }

	    if (path.groups == null) {
		return true;
	    } else {
		for (Set<String>groupIds: path.groups) {
		    ArrayList<String> groups = new ArrayList<String>();
		    for (String groupId: groupIds)
			groups.add("/site/" + siteId + "/group/" + groupId);
		    List<AuthzGroup> matched = authzGroupService.getAuthzUserGroupIds(groups, currentUserId);
		    // must match at least one
		    if (matched.size() < 1) {
			continue nextpath;
		    }
		}
		// matched all of the items o the path
		return true;
	    }

	}

	return false;

    }

    public SimplePageBean makeSimplePageBean(SimplePageBean simplePageBean, String siteId, SimplePage currentPage) {
	if (simplePageBean == null) {
	    simplePageBean = new SimplePageBean();
	    simplePageBean.setMessageLocator(messageLocator);
	    simplePageBean.setToolManager(toolManager);
	    simplePageBean.setSecurityService(securityService);
	    simplePageBean.setSessionManager(sessionManager);
	    simplePageBean.setSiteService(siteService);
	    simplePageBean.setContentHostingService(contentHostingService);
	    simplePageBean.setSimplePageToolDao(dao);
	    simplePageBean.setForumEntity(forumEntity);
	    simplePageBean.setQuizEntity(quizEntity);
	    simplePageBean.setAssignmentEntity(assignmentEntity);
	    simplePageBean.setBltiEntity(bltiEntity);
	    simplePageBean.setGradebookIfc(gradebookIfc);
	    simplePageBean.setCurrentSiteId(siteId);
	    simplePageBean.setCurrentPage(currentPage);
	    simplePageBean.setCurrentPageId(currentPage.getPageId());
	    simplePageBean.init();
	}

	return simplePageBean;
    }


    // currently does not check site permissions
    // if you need to evaluate multiple items on a page, please use of create a
    // simplePageBean and pass it. THat will let us cache a bunch of data between
    // the calls, and save a lot of database queries
    public boolean isItemAccessible(long itemId, String siteId, String currentUserId, SimplePageBean simplePageBean) {
	
	SimplePageItem item = dao.findItem(itemId);
	if (item == null) {
	    return false;
	}
	
	SimplePage currentPage = dao.getPage(item.getPageId());
	if (currentPage == null) {
	    return false;
	}

	//Look up the siteId if not provided
	if (siteId == null) {
        siteId = currentPage.getSiteId();   
	}
	
	// top-level pseudo-item is special, as there is no containing page
	// just test the page it points to
	if (item.getPageId() == 0L && item.getType() == SimplePageItem.PAGE) {
	    String pageString = item.getSakaiId();
	    long pageNum = 0;
	    try {
		pageNum = Long.parseLong(pageString, 10);
	    } catch (Exception e) {
		return false;
	    }
	    return isPageAccessible(pageNum, siteId, currentUserId, simplePageBean);
	}

	simplePageBean = makeSimplePageBean(simplePageBean, siteId, currentPage);

	// containing page must be accessible.
	if (!isPageAccessible(currentPage.getPageId(), siteId, currentUserId, simplePageBean)) {
	    return false;
	}

	if (!simplePageBean.isItemAvailable(item, item.getPageId())) {
	    return false;
	}

	try {
	if (!simplePageBean.isItemVisible(item, null, false)) {
	    return false;
	}
	} catch (Exception e) {
	    return false;
	}

	return true;
    }

    // shoud be the same as canEditPage in SimplePageBean, but without the caching from the bean.
    public boolean canEditPage (String siteId, SimplePage page) {
	String ref = "/site/" + siteId;
	if (securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_UPDATE, ref))
	    return true;
	if (page != null && isPageOwner(page))
	    return true;
	return false;
    }

    // copied from SimplePageBean, for use at service level. 
    public boolean isPageOwner(SimplePage page) {
	String owner = page.getOwner();
	String group = page.getGroup();
	if (group != null)
	    group = "/site/" + page.getSiteId() + "/group/" + group;
	if (owner == null)
	    return false;
	String currentUserId = userDirectoryService.getCurrentUser().getId();
	if (group == null)
	    return owner.equals(currentUserId);
	else
	    return authzGroupService.getUserRole(currentUserId, group) != null;
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

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
    
    public MessageLocator messageLocator;

    public void setMessageLocator(MessageLocator s) {
	messageLocator = s;
    }

    private ToolManager toolManager;
    
    public void setToolManager(ToolManager s) {
	toolManager = s;
    }

    SessionManager sessionManager = null;

    public void setSessionManager(SessionManager s) {
	sessionManager = s;
    }

    private SiteService siteService;

    public void setSiteService(SiteService s) {
	siteService = s;
    }

    ContentHostingService contentHostingService = null;

    public void setContentHostingService(ContentHostingService s) {
	contentHostingService = s;
    }

    LessonEntity forumEntity = null;

    public void setForumEntity(Object e) {
	forumEntity = (LessonEntity) e;
    }

    LessonEntity quizEntity = null;
    
    public void setQuizEntity(Object e) {
	quizEntity = (LessonEntity) e;
    }

    LessonEntity assignmentEntity = null;

    public void setAssignmentEntity(Object e) {
	assignmentEntity = (LessonEntity) e;
    }
    
    LessonEntity bltiEntity = null;
    public void setBltiEntity(Object e) {
	bltiEntity = (LessonEntity)e;
    }

    private GradebookIfc gradebookIfc = null;

    public void setGradebookIfc(GradebookIfc g) {
	gradebookIfc = g;
    }

}
