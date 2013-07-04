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
import java.util.Arrays;
import java.util.Map;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.lessonbuildertool.service.LessonSubmission;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.PermissionLevel;
import org.sakaiproject.api.app.messageforums.PermissionsMask;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.component.app.messageforums.MembershipItem;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.component.cover.ServerConfigurationService;             
import org.sakaiproject.db.cover.SqlService;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.MemoryService;

import org.sakaiproject.util.FormattedText;
import java.net.URLEncoder;

import uk.org.ponder.messageutil.MessageLocator;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Interface to Message Forums, the forum that comes with Sakai
 *
 * @author Charles Hedrick <hedrick@rutgers.edu>
 * 
 */

// NOTE: almost no other class should import this. We want to be able
// to support both forums and jforum. So typically there will be a 
// forumEntity, but it's injected, and it can be either forum and jforum.
// Hence it has to be declared LessonEntity. That leads to a lot of
// declarations like LessonEntity forumEntity.  In this case forumEntity
// means either a ForumEntity or a JForumEntity. We can't just call the
// variables lessonEntity because the same module will probably have an
// injected class to handle tests and quizes as well. That will eventually
// be converted to be a LessonEntity.

// this monstrosity has to do hibernate directly to avoid the dreaded 
// multiple objects with the same ID error. I do merge rather than save.
// unfortunately the normal API does saveOrUpdate.
//  This is complicated by the fact that we sessionFactory is set only
// when the main bean is set up. But setGroups is only called from 
// instances of this class that aren't the bean. Hence in the bean
// we save a copy of the session factory and then set it in the
// instance when we need it.

public class ForumEntity extends HibernateDaoSupport implements LessonEntity, ForumInterface {

    private static Log log = LogFactory.getLog(ForumEntity.class);

    private static Cache topicCache = null;   // topicid => grouplist
    protected static final int DEFAULT_EXPIRATION = 10 * 60;
    private static SessionFactory sessionFactory = null;

    static MessageForumsForumManager forumManager = (MessageForumsForumManager)
	ComponentManager.get("org.sakaiproject.api.app.messageforums.MessageForumsForumManager");
    static MessageForumsMessageManager messageManager = (MessageForumsMessageManager)
	ComponentManager.get("org.sakaiproject.api.app.messageforums.MessageForumsMessageManager");
    static PermissionLevelManager permissionLevelManager = (PermissionLevelManager)
	ComponentManager.get("org.sakaiproject.api.app.messageforums.PermissionLevelManager");
    static UIPermissionsManager uiPermissionsManager = (UIPermissionsManager)
	ComponentManager.get("org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager");
    static DiscussionForumManager discussionForumManager = (DiscussionForumManager)
	ComponentManager.get("org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager");
    static AreaManager areaManager = (AreaManager)
	ComponentManager.get("org.sakaiproject.api.app.messageforums.AreaManager");
    static MessageForumsTypeManager typeManager = (MessageForumsTypeManager)
	ComponentManager.get("org.sakaiproject.api.app.messageforums.MessageForumsTypeManager");

    private LessonEntity nextEntity = null;
    public void setNextEntity(LessonEntity e) {
	nextEntity = e;
    }
    public LessonEntity getNextEntity() {
	return nextEntity;
    }
    
    static MessageLocator messageLocator = null;
    public void setMessageLocator(MessageLocator m) {
	messageLocator = m;
    }

    static MemoryService memoryService = null;
    public void setMemoryService(MemoryService m) {
	memoryService = m;
    }

    public void init () {	
	//	topicCache = memoryService
	//	    .newCache("org.sakaiproject.lessonbuildertool.service.ForumEntity.cache");
	sessionFactory = getSessionFactory();
    }

    protected void initDao() throws Exception {
	super.initDao();
	log.info("initDao template " + getHibernateTemplate());
    }

    public void destroy()
    {
	//	topicCache.destroy();
	//	topicCache = null;

	log.info("destroy()");
    }

    // to create bean. the bean is used only to call the pseudo-static
    // methods such as getEntitiesInSite. So type, id, etc are left uninitialized

    protected ForumEntity() {
    }

    protected ForumEntity(int type, Long id, int level) {
	this.type = type;
	this.id = id;
	this.level = level;
    }

    public String getToolId() {
	return "sakai.forums";
    }

    // the underlying object, something Sakaiish
    protected Long id;
    protected int type;
    protected int level;
    // not required fields. If we need to look up
    // the actual objects, lets us cache them
    protected Topic topic = null;
    protected BaseForum forum = null;

    // type of the underlying object
    public int getType() {
	return type;
    }

    public int getLevel() {
	return level;
    }

    public int getTypeOfGrade() {
	return 1;
    }

    public boolean isUsable() {
	if (type == TYPE_FORUM_TOPIC)
	    return true;
	else
	    return false;
    }

    public String getReference() {
	if (type == TYPE_FORUM_TOPIC)
	    return "/" + FORUM_TOPIC + "/" + id;
	else
	    return "/" + FORUM_FORUM + "/" + id;
    }

    public class ForumBySortIndexAscAndCreatedDateDesc implements Comparator<BaseForum> {

	public int compare(BaseForum forum, BaseForum otherForum) {
	    if (forum != null && otherForum != null) {
		Integer index1 = forum.getSortIndex();
		Integer index2 = otherForum.getSortIndex();
		if (index1.intValue() != index2.intValue()) return index1.intValue() - index2.intValue();
		Date date1 = forum.getCreated();
		Date date2 = otherForum.getCreated();
		int rval = date2.compareTo(date1);
		if (rval == 0) {
		    return otherForum.getId().compareTo(forum.getId());
		} else {
		    return rval;
		}
	    }
	    return -1;
	}

    }

    public List<LessonEntity> getEntitiesInSite() {    
	return getEntitiesInSite(null);
    }

    // find topics in site, but organized by forum
    public List<LessonEntity> getEntitiesInSite(SimplePageBean bean) {    
    	
    List<LessonEntity> ret = new ArrayList<LessonEntity>();
    
	// LSNBLDR-21. If the tool is not in the current site we shouldn't query
	// for topics owned by the tool.
	Site site = null;
	try {
	    site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
	} catch (Exception impossible) {
	    return ret;
	}
    	
    ToolConfiguration tool = site.getToolForCommonId("sakai.forums");
	
    if(tool == null) {
    	
    	// Forums is not in this site. Move on to the next provider.
    	
    	if (nextEntity != null) 
    		ret.addAll(nextEntity.getEntitiesInSite());
    	
    	return ret;
    }

	//ForumEntity e = new ForumEntity(TYPE_FORUM_TOPIC, 3L, 2);

	//e.setGroups(Arrays.asList("1c24287b-b880-43da-8cdd-c6cdc1249c5c", "75184424-853e-4dd4-9e92-980c851f0580"));
	//e.setGroups(Arrays.asList("75184424-853e-4dd4-9e92-980c851f0580"));

	SortedSet<DiscussionForum> forums = new TreeSet<DiscussionForum>(new ForumBySortIndexAscAndCreatedDateDesc());
	for (DiscussionForum forum: forumManager.getForumsForMainPage())
	    forums.add(forum);

	// security. assume this is only used in places where it's OK, so skip security checks
	for (DiscussionForum forum: forums) {
	    if (!forum.getDraft()) {
		ForumEntity entity = new ForumEntity(TYPE_FORUM_FORUM, forum.getId(), 1);
		entity.forum = forum;
		ret.add(entity);
		for (Object o: forum.getTopicsSet()) {
		    DiscussionTopic topic = (DiscussionTopic)o;
		    if (topic.getDraft().equals(Boolean.FALSE)) {
			entity = new ForumEntity(TYPE_FORUM_TOPIC, topic.getId(), 2);
			entity.topic = topic;
			ret.add(entity);
		    }
		}
	    }
	}

	if (nextEntity != null) 
	    ret.addAll(nextEntity.getEntitiesInSite(bean));

	return ret;
    }

    public LessonEntity getEntity(String ref, SimplePageBean o) {
	return getEntity(ref);
    }

    public LessonEntity getEntity(String ref) {
	int i = ref.indexOf("/",1);
	if (i < 0)
	    return null;
	String typeString = ref.substring(1, i);
	String idString = ref.substring(i+1);
	Long id = 0L;
	if (typeString.equals(FORUM_TOPIC) || typeString.equals(FORUM_FORUM)) {
		try {
			id = Long.parseLong(idString);
		} catch (Exception ignore) {
			return null;
		}
	}

	// note: I'm returning the minimal structures, not those with
	// topics and postings attached
	if (typeString.equals(FORUM_TOPIC)) {
	    return new ForumEntity(TYPE_FORUM_TOPIC, id, 2);
	} else if (typeString.equals(FORUM_FORUM)) {
	    return new ForumEntity(TYPE_FORUM_FORUM, id, 1);
	} else if (nextEntity != null) {
	    return nextEntity.getEntity(ref);
	} else
	    return null;
    }
	

    public Topic getTopicById(boolean flag, Long id) {
	try {
	    return forumManager.getTopicById(flag, id);
	} catch (Exception e) {
	    return null;
	}
    }

    public BaseForum getForumById(boolean flag, Long id) {
	try {
	    return forumManager.getForumById(flag, id);
	} catch (Exception e) {
	    return null;
	}
    }

    // properties of entities
    public String getTitle() {

	if (type == TYPE_FORUM_TOPIC) {
	    if (topic == null)
		topic = getTopicById(true, id);
	    if (topic == null)
		return null;
	    return topic.getTitle();
	} else {
	    if (forum == null)
		forum = getForumById(true, id);
	    if (forum == null)
		return null;
	    return forum.getTitle();
	}
    }

    public String getUrl() {
	
	if (topic == null)
	    topic = getTopicById(true, id);
	if (topic == null)
	    return "javascript:alert('" + messageLocator.getMessage("simplepage.forumdeleted") + "')";

	Site site = null;
	try {
	    site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
	} catch (Exception impossible) {
	    return null;
	}
	ToolConfiguration tool = site.getToolForCommonId("sakai.forums");
	
	// LSNBLDR-21. If the tool is not in the current site we shouldn't return a url
	if(tool == null) {
	    return null;
	}
	
	String placement = tool.getId();

	if (type == TYPE_FORUM_TOPIC)
	    return "/messageforums-tool/jsp/discussionForum/message/dfAllMessagesDirect.jsf?topicId=" + id + "&placementId=" + placement;
	else
	    return "/direct/forum/" + id;
    }

    // I don't think they have this
    public Date getDueDate() {
	return null;
    }

    // The msgcntr permissions model is completely undocumented. Here's a cheat sheet:

    // DBMembershipItem has 
    //    type, which is ALL, ROLE, GROUP or USER
    //    name which is the specific rolename, groupname or username
    //    permissionlevelname which is "Owner", "Contributor", "None", etc.
    //       In addition there is a bitmask that says which specific permissions
    //       are present, but we always use the default permissions for each level.

    // Here's typical code. First we create a permissionlevel with a given bitmask.
    // This permissionlevel controls the detailed permissions. As noted, we always 
    // default levels. Unfortunately we have to create a new copy of the level
    // for every entry. Some fo their internal code uses common permissionlevels, but
    // if you don't have a separate level object and database entry for each membershipitem,
    // things get very confused.
    //	  PermissionLevel contributorLevel = permissionLevelManager.
    //	      createPermissionLevel("Contributor",  typeManager.getContributorLevelType(), contributorMask);
    //    permissionLevelManager.savePermissionLevel(contributorLevel);

    // Now we create the actual entry. Note that this one says members of the specified
    // group are contributors. Then it sets the default contributor bitmask. You can call
    // a permission contributor but set any bits you want. However we're going to assume
    // that people pick a name that represents what they want, and make minimal changes.
    //	  DBMembershipItem membershipItem = permissionLevelManager.
    //	    createDBMembershipItem(groupName, "Contributor", MembershipItem.TYPE_GROUP);
    //	  membershipItem.setPermissionLevel(contributorLevel);
    //	  permissionLevelManager.saveDBMembershipItem(membershipItem);	


    // How we use it:

    // ACCESS CONTROL:

    // Our model is fairly simple. When we control access, Owner is the maintain role, and 
    //    contributor is the group we control. Everything else is set to none.
    // When you decontrol something, we make Owner the maintain role
    //    and Contributor all the other roles. Once we support groups, we'll put back
    //    saved group access, but we won't try to put back anything else.
    // The tool code makes sure that items are added for all roles, so we don't have to add entries
    //    in most cases, just change their permission levels.

    // GROUP ACCESS WHEN WE AREN'T CONTROLLING:

    // Now, our group management code, which should only be used when we're not controlling:
    // Getgroups returns which groups are contributor.
    //    If you want something more complex, you'll have to do it in the tool, but if there
    //       are any groups with contributor, we'll only let students in those groups access
    //       through our tool.
    // Setgroups with a non-null list: we set all contributor entries to none, and then set the
    //    specified groups to contribtor. By only handling groups, we avoid interfering with
    //    anything you might do in the tool. But the moment you use access control, we take
    //    over. Sorry. Once we've done that you could go back into the tool and hack, but I
    //    don't recommend that.
    // Setgroups with a null list: we set all contributor entries to none, and then set all roles
    //    other than maintain to contributor.
    // It may be safer to do changes in the tool.


    // the following methods all take references. So they're in effect static.
    // They ignore the entity from which they're called.
    // The reason for not making them a normal method is that many of the
    // implementations seem to let you set access control and find submissions
    // from a reference, without needing the actual object. So doing it this
    // way could save some database activity

    PermissionsMask noneMask = null;
    PermissionsMask contributorMask = null;
    PermissionsMask ownerMask = null;

    private void setMasks() {
	if (noneMask == null) {
	    noneMask = new PermissionsMask();
	    noneMask.put(PermissionLevel.NEW_FORUM, Boolean.valueOf(false));
	    noneMask.put(PermissionLevel.NEW_TOPIC, Boolean.valueOf(false));
	    noneMask.put(PermissionLevel.NEW_RESPONSE, Boolean.valueOf(false));
	    noneMask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.valueOf(false));
	    noneMask.put(PermissionLevel.MOVE_POSTING, Boolean.valueOf(false));
	    noneMask.put(PermissionLevel.CHANGE_SETTINGS,Boolean.valueOf(false));
	    noneMask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.valueOf(false));
	    noneMask.put(PermissionLevel.READ, Boolean.valueOf(false));
	    noneMask.put(PermissionLevel.MARK_AS_READ,Boolean.valueOf(false));
	    noneMask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.valueOf(false));
	    noneMask.put(PermissionLevel.DELETE_OWN, Boolean.valueOf(false));
	    noneMask.put(PermissionLevel.DELETE_ANY, Boolean.valueOf(false));
	    noneMask.put(PermissionLevel.REVISE_OWN, Boolean.valueOf(false));
	    noneMask.put(PermissionLevel.REVISE_ANY, Boolean.valueOf(false));
	}
	if (contributorMask == null) {
	    contributorMask = new PermissionsMask();
	    contributorMask.put(PermissionLevel.NEW_FORUM, Boolean.valueOf(false));
	    contributorMask.put(PermissionLevel.NEW_TOPIC, Boolean.valueOf(false));
	    contributorMask.put(PermissionLevel.NEW_RESPONSE, Boolean.valueOf(true));
	    contributorMask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.valueOf(true));
	    contributorMask.put(PermissionLevel.MOVE_POSTING, Boolean.valueOf(false));
	    contributorMask.put(PermissionLevel.CHANGE_SETTINGS,Boolean.valueOf(false));
	    contributorMask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.valueOf(false));
	    contributorMask.put(PermissionLevel.READ, Boolean.valueOf(true));
	    contributorMask.put(PermissionLevel.MARK_AS_READ,Boolean.valueOf(true));
	    contributorMask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.valueOf(false));
	    contributorMask.put(PermissionLevel.DELETE_OWN, Boolean.valueOf(false));
	    contributorMask.put(PermissionLevel.DELETE_ANY, Boolean.valueOf(false));
	    contributorMask.put(PermissionLevel.REVISE_OWN, Boolean.valueOf(false));
	    contributorMask.put(PermissionLevel.REVISE_ANY, Boolean.valueOf(false));
	}
	if (ownerMask == null) {
	    ownerMask = new PermissionsMask();
	    ownerMask.put(PermissionLevel.NEW_FORUM, Boolean.valueOf(true));
	    ownerMask.put(PermissionLevel.NEW_TOPIC, Boolean.valueOf(true));
	    ownerMask.put(PermissionLevel.NEW_RESPONSE, Boolean.valueOf(true));
	    ownerMask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.valueOf(true));
	    ownerMask.put(PermissionLevel.MOVE_POSTING, Boolean.valueOf(true));
	    ownerMask.put(PermissionLevel.CHANGE_SETTINGS,Boolean.valueOf(true));
	    ownerMask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.valueOf(true));
	    ownerMask.put(PermissionLevel.READ, Boolean.valueOf(true));
	    ownerMask.put(PermissionLevel.MARK_AS_READ,Boolean.valueOf(true));
	    ownerMask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.valueOf(true));
	    ownerMask.put(PermissionLevel.DELETE_OWN, Boolean.valueOf(false));
	    ownerMask.put(PermissionLevel.DELETE_ANY, Boolean.valueOf(true));
	    ownerMask.put(PermissionLevel.REVISE_OWN, Boolean.valueOf(false));
	    ownerMask.put(PermissionLevel.REVISE_ANY, Boolean.valueOf(true));
	}
    }

    // access control
    public boolean addEntityControl(String siteId, String groupId) throws IOException {

	setMasks();

	if (topic == null)
	    topic = getTopicById(true, id);
	if (topic == null)
	    return false;

	Set<DBMembershipItem> oldMembershipItemSet = uiPermissionsManager.getTopicItemsSet((DiscussionTopic)topic);

	Set membershipItemSet = new HashSet();

	String groupName = null;
	String maintainRole = null;
	try {
	    Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
	    groupName = site.getGroup(groupId).getTitle();
	    maintainRole = AuthzGroupService.getAuthzGroup("/site/" + site.getId()).getMaintainRole();
	} catch (Exception e) {
	    System.out.println("Unable to get site info for AddEntityControl " + e);
	}

	PermissionLevel ownerLevel = permissionLevelManager.
	    createPermissionLevel("Owner",  typeManager.getOwnerLevelType(), ownerMask);
	permissionLevelManager.savePermissionLevel(ownerLevel);

	PermissionLevel contributorLevel = permissionLevelManager.
	    createPermissionLevel("Contributor",  typeManager.getContributorLevelType(), contributorMask);
	permissionLevelManager.savePermissionLevel(contributorLevel);

	DBMembershipItem membershipItem = permissionLevelManager.
	    createDBMembershipItem(groupName, "Contributor", MembershipItem.TYPE_GROUP);
	membershipItem.setPermissionLevel(contributorLevel);
	permissionLevelManager.saveDBMembershipItem(membershipItem);	

	membershipItemSet.add(membershipItem);

	membershipItem = permissionLevelManager.
	    createDBMembershipItem(maintainRole, "Owner", MembershipItem.TYPE_ROLE);
	membershipItem.setPermissionLevel(ownerLevel);
	permissionLevelManager.saveDBMembershipItem(membershipItem);	
	
	membershipItemSet.add(membershipItem);

	// now change any existing ones into null
	for (DBMembershipItem item: oldMembershipItemSet) {
	    if (!(maintainRole.equals(item.getName()) && item.getType().equals(MembershipItem.TYPE_ROLE) ||
		  groupName.equals(item.getName()) && item.getType().equals(MembershipItem.TYPE_GROUP))) {
		PermissionLevel noneLevel = permissionLevelManager.
		    createPermissionLevel("None",  typeManager.getNoneLevelType(), noneMask);
		permissionLevelManager.savePermissionLevel(noneLevel);

		membershipItem = permissionLevelManager.
		    createDBMembershipItem(item.getName(), "None", item.getType());
		membershipItem.setPermissionLevel(noneLevel);
		permissionLevelManager.saveDBMembershipItem(membershipItem);	
		membershipItemSet.add(membershipItem);
	    }
	}

        permissionLevelManager.deleteMembershipItems(oldMembershipItemSet);

	topic.setMembershipItemSet(membershipItemSet);
	discussionForumManager.saveTopic((DiscussionTopic) topic);

	return true;
    };
	
    public boolean removeEntityControl(String siteId, String groupId) throws IOException {

	setMasks();

	if (topic == null)
	    topic = getTopicById(true, id);
	if (topic == null)
	    return false;

	Set<DBMembershipItem> oldMembershipItemSet = uiPermissionsManager.getTopicItemsSet((DiscussionTopic)topic);

	Set membershipItemSet = new HashSet();

	String groupName = null;
	String maintainRole = null;
	try {
	    Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
	    groupName = site.getGroup(groupId).getTitle();
	    maintainRole = AuthzGroupService.getAuthzGroup("/site/" + site.getId()).getMaintainRole();
	} catch (Exception e) {
	    System.out.println("Unable to get site info for AddEntityControl " + e);
	}

	PermissionLevel ownerLevel = permissionLevelManager.
	    createPermissionLevel("Owner",  typeManager.getOwnerLevelType(), ownerMask);
	permissionLevelManager.savePermissionLevel(ownerLevel);

	DBMembershipItem membershipItem = permissionLevelManager.
	    createDBMembershipItem(maintainRole, "Owner", MembershipItem.TYPE_ROLE);
	membershipItem.setPermissionLevel(ownerLevel);
	permissionLevelManager.saveDBMembershipItem(membershipItem);	
	
	membershipItemSet.add(membershipItem);

	// now change any existing ones into null
	for (DBMembershipItem item: oldMembershipItemSet) {
 	    if (item.getType().equals(MembershipItem.TYPE_ROLE)) {
		if (!maintainRole.equals(item.getName())) { // that was done above, other roles contributor
		    PermissionLevel contributorLevel = permissionLevelManager.
			createPermissionLevel("Contributor",  typeManager.getContributorLevelType(), contributorMask);
		    permissionLevelManager.savePermissionLevel(contributorLevel);

		    membershipItem = permissionLevelManager.
			createDBMembershipItem(item.getName(), "Contributor", item.getType());
		    membershipItem.setPermissionLevel(contributorLevel);
		    permissionLevelManager.saveDBMembershipItem(membershipItem);	
		    membershipItemSet.add(membershipItem);
		}
	    } else {  // everything else off
		PermissionLevel noneLevel = permissionLevelManager.
		    createPermissionLevel("None",  typeManager.getNoneLevelType(), noneMask);
		permissionLevelManager.savePermissionLevel(noneLevel);
		
		membershipItem = permissionLevelManager.
		    createDBMembershipItem(item.getName(), "None", item.getType());
		membershipItem.setPermissionLevel(noneLevel);
		permissionLevelManager.saveDBMembershipItem(membershipItem);	
		membershipItemSet.add(membershipItem);
	    }
	}

        permissionLevelManager.deleteMembershipItems(oldMembershipItemSet);

	topic.setMembershipItemSet(membershipItemSet);
	discussionForumManager.saveTopic((DiscussionTopic) topic);

	return true;
    };

    // submission
    // do we need the data from submission?
    //  not for the moment. If a posting is required, we just check whether one
    //  has been done. While you can grade submissions, grading is done manually
    //  later. It's unlikely that faculty will want to test on those grades
    public boolean needSubmission(){
	return false;
    }
    public LessonSubmission getSubmission(String user) {
	return null; // not used
    }
    public int getSubmissionCount(String user) {
	return messageManager.findAuhtoredMessageCountByTopicIdByUserId(id, user);
    }

    // URL to create a new item. Normally called from the generic entity, not a specific one                                                 
    // can't be null                                                                                                                         
    public List<UrlItem> createNewUrls(SimplePageBean bean) {
	ArrayList<UrlItem> list = new ArrayList<UrlItem>();
	String tool = bean.getCurrentTool("sakai.forums");
	if (tool != null) {
	    tool = ServerConfigurationService.getToolUrl() + "/" + tool + "/discussionForum/forumsOnly/dfForums";
	    list.add(new UrlItem(tool, messageLocator.getMessage("simplepage.create_forums")));
	}
	if (nextEntity != null)
	    list.addAll(nextEntity.createNewUrls(bean));
	return list;
    }

    // URL to edit an existing entity.                                                                                                       
    // Can be null if we can't get one or it isn't needed                                                                                    
    public String editItemUrl(SimplePageBean bean) {
	return getUrl();
    }


    // for most entities editItem is enough, however tests allow separate editing of                                                         
    // contents and settings. This will be null except in that situation                                                                     
    public String editItemSettingsUrl(SimplePageBean bean) {
	return null;
    }

    // returns SakaiId of thing just created
    public String importObject(String title, String topicTitle, String text, boolean texthtml, String base, String siteId, List<String>attachmentHrefs, boolean hide) {

	DiscussionForum ourForum = null;
	DiscussionTopic ourTopic = null;

	int forumtry = 0;
	int topictry = 0;

	for (;;) {

	    ourForum = null;

	    SortedSet<DiscussionForum> forums = new TreeSet<DiscussionForum>(new ForumBySortIndexAscAndCreatedDateDesc());
	    for (DiscussionForum forum: discussionForumManager.getForumsForMainPage())
		forums.add(forum);

	    for (DiscussionForum forum: forums) {
		if (forum.getTitle().equals(title)) {
		    ourForum = forum;
		    break;
		}
	    }
	
	    if (ourForum == null) {
		if (forumtry > 0) {
		    System.out.println("oops, forum still not there the second time");
		    return null;
		}
		forumtry ++;

		// if a new site, may need to create the area or we'll get a backtrace when creating forum
		areaManager.getDiscussionArea(siteId);

		ourForum = discussionForumManager.createForum();
		ourForum.setTitle(title);
		discussionForumManager.saveForum(siteId, ourForum);
		
		continue;  // reread, better be there this time

	    }

	    // forum now exists, and was just reread

	    ourTopic = null;

	    for (Object o: ourForum.getTopicsSet()) {
		DiscussionTopic topic = (DiscussionTopic)o;
		if (topic.getTitle().equals(topicTitle)) {
		    ourTopic = topic;
		    break;
		}
	    }

	    if (ourTopic != null) // ok, forum and topic exist
		break;

	    if (topictry > 0) {
		System.out.println("oops, topic still not there the second time");
		return null;
	    }
	    topictry ++;

	    // create it

	    ourTopic = discussionForumManager.createTopic(ourForum);
	    ourTopic.setTitle(topicTitle);
	    StringBuilder attachHtml = new StringBuilder("");
	    if (attachmentHrefs != null && attachmentHrefs.size() > 0) {
		for (String href: attachmentHrefs) {
		    String label = href;
		    int slash = label.lastIndexOf("/");
		    if (slash >= 0)
			label = label.substring(slash+1);
		    if (label.equals(""))
			label = "Attachment";
		    attachHtml.append("<p><a target='_blank' href='");
		    attachHtml.append(base);
		    attachHtml.append(href);
		    attachHtml.append("'>");
		    attachHtml.append(label);
		    attachHtml.append("</a>");
		}
	    }

	    String shortText = null;
	    if (texthtml) {
		ourTopic.setExtendedDescription(text.replaceAll("\\$IMS-CC-FILEBASE\\$", base) + attachHtml.toString());
		shortText = FormattedText.convertFormattedTextToPlaintext(text);
	    } else {
		ourTopic.setExtendedDescription(FormattedText.convertPlaintextToFormattedText(text) + attachHtml.toString());
		shortText = text;
	    }
	    shortText = org.apache.commons.lang.StringUtils.abbreviate(shortText,254);

	    ourTopic.setShortDescription(shortText);

	    // there's a better way to do attachments, but it's too complex for now

	    if (hide) 
		discussionForumManager.saveTopicAsDraft(ourTopic);
	    else
		discussionForumManager.saveTopic(ourTopic);

	    // now go back and mmake sure everything is there

	}

	return "/" + FORUM_TOPIC + "/" + ourTopic.getId();
    }

    public boolean objectExists() {
	if (topic == null)
	    topic = getTopicById(true, id);
	return topic != null;
    }

    // return the list of groups if the item is only accessible to specific groups
    // null if it's accessible to the whole site.
    public List<String> getGroups(boolean nocache) {

	// don't need cache, since simplepagebean is now caching groups
	//	List<String>ret = (List<String>)topicCache.get(id);
	//	if (!nocache && ret != null) {
	//	    if (ret.size() == 0)
	//		return null;
	//	    else 
	//		return ret;
	//	} else {
	//	}

	List <String>ret = new ArrayList<String>();

	if (topic == null)
	    topic = getTopicById(true, id);
	if (topic == null)
	    return null;

	Set<DBMembershipItem> oldMembershipItemSet = uiPermissionsManager.getTopicItemsSet((DiscussionTopic)topic);

	Collection<Group> groups = null;

	try {
	    Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
	    groups = site.getGroups();
	} catch (Exception e) {
	    System.out.println("Unable to get site info for getGroups " + e);
	}

	// now change any existing ones into null
	for (DBMembershipItem item: oldMembershipItemSet) {
	    if (item.getPermissionLevelName().equals("Contributor") &&
		item.getType().equals(MembershipItem.TYPE_GROUP)) {
		String name = item.getName();   // oddly, this is the actual name, not the ID
		for (Group group: groups) {
		    if (name.equals(group.getTitle()))
			ret.add(group.getId());
		}
	    }
	}

	//	topicCache.put(id, ret, DEFAULT_EXPIRATION);
	if (ret.size() == 0)
	    return null;
	else
	    return ret;
    }

    // set the item to be accessible only to the specific groups.
    // null to make it accessible to the whole site
    public void setGroups(Collection<String> groups) {

    // Setgroups with a non-null list: we set all contributor entries to none, and then set the
    //    specified groups to contribtor. By only handling groups, we avoid interfering with
    //    anything you might do in the tool. But the moment you use access control, we take
    //    over. Sorry. Once we've done that you could go back into the tool and hack, but I
    //    don't recommend that.
    // Setgroups with a null list: we set all contributor entries to none, and then set all roles
    //    other than maintain to contributor.

	setMasks();

	//System.out.println("topic 1 " + topic + " " + groups);
	if (topic == null)
	    topic = getTopicById(true, id);
	//System.out.println("topic 2 " + topic);
	if (topic == null)
	    return;

	// topicCache.remove(id);

	// old entries
	Set<DBMembershipItem> oldMembershipItemSet = uiPermissionsManager.getTopicItemsSet((DiscussionTopic)topic);

	// which old entires to delete
	Set<DBMembershipItem> deleteItemSet = new HashSet<DBMembershipItem>();

	// all entries we will keep
	Set membershipItemSet = new HashSet();

	Site site = null;
	String maintainRole = null;

	// used so we can give an access level to each role. Remove roles from this as we see
	// them, so at the we just do the ones remaining
	List<String> roles = new ArrayList<String>();


	try {
	    site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
	    maintainRole = AuthzGroupService.getAuthzGroup("/site/" + site.getId()).getMaintainRole();
	    Set<Role>roleObjs =  AuthzGroupService.getAuthzGroup("/site/" + site.getId()).getRoles();
	    for (Role roleObj: roleObjs)
		roles.add(roleObj.getId());
	} catch (Exception e) {
	    System.out.println("Unable to get site info for AddEntityControl " + e);
	    return;
	}

	DBMembershipItem membershipItem = null;

	boolean haveOwner = false;

	if (groups != null && groups.size() > 0) {

	    // this is the groups we've been asked to use
	    // remove groups form this as we see them if they already have access
	    // so at the end we just add the ones remaining
	    List<String>groupNames = new ArrayList<String>();
	    for (String groupId: groups)
		groupNames.add(site.getGroup(groupId).getTitle());
	    // delete groups from here as they are done.

	    // if we've seen an owner. Otherwise set the maintain role as owner

	    // Setgroups with a non-null list: we set all contributor entries to none, and then set the
	    //    specified groups to contribtor. However we don't touch owner.
	    // By only handling groups, we avoid interfering with
	    //    anything you might do in the tool. But the moment you use access control, we take
	    //    over. Sorry. Once we've done that you could go back into the tool and hack, but I
	    //    don't recommend that.

	    for (DBMembershipItem item: oldMembershipItemSet) {
		//System.out.println("old item " + item.getPermissionLevelName() + " " + item.getType() + " " + item.getName());
		if (item.getPermissionLevelName().equals("Owner"))
		    haveOwner = true;
		if (item.getType().equals(MembershipItem.TYPE_ROLE) && roles.contains(item.getName()))
		    roles.remove(item.getName());   // we've seen it, don't need to add
		if (item.getType().equals(MembershipItem.TYPE_GROUP) && groupNames.contains(item.getName())) {
		    // if it's one of our groups make it a contributor if it's not already an owner
		    if (!item.getPermissionLevelName().equals("Contributor") && 
			!item.getPermissionLevelName().equals("Owner")) {

			//System.out.println("make contributor");
			PermissionLevel contributorLevel = permissionLevelManager.
			    createPermissionLevel("Contributor",  IdManager.createUuid(), contributorMask);
			permissionLevelManager.savePermissionLevel(contributorLevel);

			membershipItem = permissionLevelManager.
			    createDBMembershipItem(item.getName(), "Contributor", MembershipItem.TYPE_GROUP);
			membershipItem.setPermissionLevel(contributorLevel);
			permissionLevelManager.saveDBMembershipItem(membershipItem);	
			membershipItemSet.add(membershipItem);
			deleteItemSet.add(item);
		    } else {  // if it was contributor or owner, keep it
			//System.out.println("keep");
			membershipItemSet.add(item);
		    }
		    groupNames.remove(item.getName());   // it's done
		} else if (item.getPermissionLevelName().equals("Contributor")) {  // only group members are contributors
		    // remove contributor from anything else, both groups and roles
		    //System.out.println("set none");
		    PermissionLevel noneLevel = permissionLevelManager.
			createPermissionLevel("None",  IdManager.createUuid(), noneMask);
		    permissionLevelManager.savePermissionLevel(noneLevel);

		    membershipItem = permissionLevelManager.
			createDBMembershipItem(item.getName(), "None", item.getType());
		    membershipItem.setPermissionLevel(noneLevel);
		    permissionLevelManager.saveDBMembershipItem(membershipItem);	
		    membershipItemSet.add(membershipItem);
		    deleteItemSet.add(item);
		} else {   // for other permission types, leave as is
		    //System.out.println("leave alone");
		    membershipItemSet.add(item);
		}			
	    }
	    // do any left
	    for (String name: groupNames) {
		//System.out.println("make contributor: " + name);
		PermissionLevel contributorLevel = permissionLevelManager.
		    createPermissionLevel("Contributor",  IdManager.createUuid(), contributorMask);
		permissionLevelManager.savePermissionLevel(contributorLevel);

		membershipItem = permissionLevelManager.
		    createDBMembershipItem(name, "Contributor", MembershipItem.TYPE_GROUP);
		membershipItem.setPermissionLevel(contributorLevel);
		permissionLevelManager.saveDBMembershipItem(membershipItem);	
		membershipItemSet.add(membershipItem);
	    }
	    if (!haveOwner) {
		//System.out.println("add owner");
		PermissionLevel ownerLevel = permissionLevelManager.
		    createPermissionLevel("Owner",  IdManager.createUuid(), ownerMask);
		permissionLevelManager.savePermissionLevel(ownerLevel);

		membershipItem = permissionLevelManager.
		    createDBMembershipItem(maintainRole, "Owner", MembershipItem.TYPE_ROLE);
		membershipItem.setPermissionLevel(ownerLevel);
		permissionLevelManager.saveDBMembershipItem(membershipItem);	
		membershipItemSet.add(membershipItem);
		roles.remove(maintainRole);  // we've processed this, so don't make it None
	    }
	    for (String name: roles) {
		//System.out.println("make none " + name);
		PermissionLevel noneLevel = permissionLevelManager.
		    createPermissionLevel("None",  IdManager.createUuid(), noneMask);
		permissionLevelManager.savePermissionLevel(noneLevel);

		membershipItem = permissionLevelManager.
		    createDBMembershipItem(name, "None", MembershipItem.TYPE_ROLE);
		membershipItem.setPermissionLevel(noneLevel);
		permissionLevelManager.saveDBMembershipItem(membershipItem);	
		membershipItemSet.add(membershipItem);
	    }
	} else {
	    // Setgroups with a null list: we set all contributor entries to none, and then set all roles
	    //    to contributor.  However we don't touch Owners.

	    for (DBMembershipItem item: oldMembershipItemSet) {
		if (item.getPermissionLevelName().equals("Owner"))
		    haveOwner = true;
		if (item.getType().equals(MembershipItem.TYPE_ROLE) && roles.contains(item.getName()))
		    roles.remove(item.getName());   // we've seen it, don't need to add
		if (item.getType().equals(MembershipItem.TYPE_ROLE) && !item.getPermissionLevelName().equals("Owner")) {
		    // turn all roles into contributor, unless already owner
		    PermissionLevel contributorLevel = permissionLevelManager.
			createPermissionLevel("Contributor",  IdManager.createUuid(), contributorMask);
		    permissionLevelManager.savePermissionLevel(contributorLevel);

		    membershipItem = permissionLevelManager.
			createDBMembershipItem(item.getName(), "Contributor", item.getType());
		    membershipItem.setPermissionLevel(contributorLevel);
		    permissionLevelManager.saveDBMembershipItem(membershipItem);	
		    membershipItemSet.add(membershipItem);
		    deleteItemSet.add(item);
		} else if (item.getPermissionLevelName().equals("Contributor")) {
		    // kill other contributors
		    PermissionLevel noneLevel = permissionLevelManager.
			createPermissionLevel("None",  IdManager.createUuid(), noneMask);
		    permissionLevelManager.savePermissionLevel(noneLevel);

		    membershipItem = permissionLevelManager.
			createDBMembershipItem(item.getName(), "None", item.getType());
		    membershipItem.setPermissionLevel(noneLevel);
		    permissionLevelManager.saveDBMembershipItem(membershipItem);	
		    membershipItemSet.add(membershipItem);
		    deleteItemSet.add(item);
		} else {   // for other permission types, leave as is
		    membershipItemSet.add(item);
		}			
	    }
	    if (!haveOwner) {
		//System.out.println("make owner " +maintainRole);
		PermissionLevel ownerLevel = permissionLevelManager.
		    createPermissionLevel("Owner",  IdManager.createUuid(), ownerMask);
		permissionLevelManager.savePermissionLevel(ownerLevel);

		membershipItem = permissionLevelManager.
		    createDBMembershipItem(maintainRole, "Owner", MembershipItem.TYPE_ROLE);
		membershipItem.setPermissionLevel(ownerLevel);
		permissionLevelManager.saveDBMembershipItem(membershipItem);	
		membershipItemSet.add(membershipItem);
		roles.remove(maintainRole);  // we've processed this, so don't make it None
	    }
	    for (String name: roles) {
		//System.out.println("make contributor: " + name);
		PermissionLevel contributorLevel = permissionLevelManager.
		    createPermissionLevel("Contributor",  IdManager.createUuid(), contributorMask);
		permissionLevelManager.savePermissionLevel(contributorLevel);

		membershipItem = permissionLevelManager.
		    createDBMembershipItem(name, "Contributor", MembershipItem.TYPE_ROLE);
		membershipItem.setPermissionLevel(contributorLevel);
		permissionLevelManager.saveDBMembershipItem(membershipItem);	
		membershipItemSet.add(membershipItem);
	    }
	}

	//System.out.println("delete " + deleteItemSet);
        permissionLevelManager.deleteMembershipItems(deleteItemSet);

	//System.out.println("new membership list ");
	//for (DBMembershipItem item: (Set<DBMembershipItem>)membershipItemSet) {
	//    System.out.println(item.getPermissionLevelName() + " " + item.getType() + " " + item.getName());
	//}
	
	topic.setMembershipItemSet(membershipItemSet);

	// should do
	//discussionForumManager.saveTopic((DiscussionTopic) topic);
	// but that uses saveOrUpdate, which gives an error because
	// we typically have more than one copy of the topic in the session.
	// The only fix that works without modifying code I can't touch
	// is to do merge rather than saveOrUpdate. But that means I
	// have to do my own hibernate save rather than using the
	// API's savetopic.  I checked the code for saveTopic, and
	// when you're dealing with an existing topic, all you need
	// is the save. The real saveTopic code just sets up fields
	// that would be null if it's a new topic. Of course the changed
	// object won't be visible in other sesssions. So if you try
	// getGroups after doing the save, and you're in the same session,
	// which is typically the same request, you'll get the old value.
	// Sorry about that.

	Session session = sessionFactory.openSession();
	Transaction tx = null;

	try {
	    tx = session.beginTransaction();

	    session.merge(topic);
	    tx.commit();	    

	} catch (Exception e) {
	    if (tx != null)
		tx.rollback();
	} finally {
	    if (session != null)
		session.close();
	}
	
    }

    // only used for topics
    public String getObjectId(){
	String title = getTitle();
	// fetches topic as well
	if (title == null)
	    return null;

	BaseForum forum = topic.getBaseForum();
	
	return "forum_topic/" + id + "/" + title + "\n" + forum.getTitle();

    }

    public String findObject(String objectid, Map<String,String>objectMap, String siteid) {
        if (!objectid.startsWith("forum_topic/")) {
            if (nextEntity != null) {
                return nextEntity.findObject(objectid, objectMap, siteid);
            }
	    return null;
	}

	// isolate forum_topic/NNN from title
	int i = objectid.indexOf("/", "forum_topic/".length());
	if (i <= 0)
	    return null;
	String realobjectid = objectid.substring(0, i);

	// now see if it's in the map
	String newtopic = objectMap.get(realobjectid);
	if (newtopic != null)
	    return "/" + newtopic;  // sakaiid is /forum_topic/ID

	// this must be 2.8. Can't find the topic in the map
	// i is start of title
	int j = objectid.indexOf("\n");
	String title = objectid.substring(i+1,j);
	String forumtitle = objectid.substring(j+1);

	// unfortunately we have to search the topic tree to find it.
	SortedSet<DiscussionForum> forums = new TreeSet<DiscussionForum>(new ForumBySortIndexAscAndCreatedDateDesc());
	for (DiscussionForum forum: forumManager.getForumsForMainPage())
	    forums.add(forum);

	// security. assume this is only used in places where it's OK, so skip security checks
	// ignore draft status. We want to show drafts.
	for (DiscussionForum forum: forums) {
	    if (forum.getTitle().equals(forumtitle)) {
		for (Object o: forum.getTopicsSet()) {
		    DiscussionTopic topic = (DiscussionTopic)o;
		    if (topic.getTitle().equals(title)) {
			return "/forum_topic/" + topic.getId();
		    }
		}
	    }
	}

	return null;

    }

    public String getSiteId() {
	// should be this:
	// return topic.getBaseForum().getArea().getContextId();
	// but requires a hibernate session, which doesn't exit.
	String sql = "select c.context_id from MFR_TOPIC_T a,MFR_OPEN_FORUM_T b,MFR_AREA_T c where a.id=? and a.of_surrogateKey=b.id and b.surrogatekey=c.id";

	Object fields[] = new Object[1];
	fields[0] = id;

	List<String> siteIds = SqlService.dbRead(sql, fields, null);

	if (siteIds != null && siteIds.size() > 0)
	    return siteIds.get(0);
	
	return null;

    }

}
