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
import java.util.SortedSet;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Date;

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
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.authz.cover.AuthzGroupService;

import org.sakaiproject.util.FormattedText;
import java.net.URLEncoder;

import uk.org.ponder.messageutil.MessageLocator;

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

public class ForumEntity implements LessonEntity, ForumInterface {

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

    // find topics in site, but organized by forum
    public List<LessonEntity> getEntitiesInSite() {


	SortedSet<DiscussionForum> forums = new TreeSet<DiscussionForum>(new ForumBySortIndexAscAndCreatedDateDesc());
	for (DiscussionForum forum: forumManager.getForumsForMainPage())
	    forums.add(forum);

	List<LessonEntity> ret = new ArrayList<LessonEntity>();
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
	    ret.addAll(nextEntity.getEntitiesInSite());

	return ret;
    }

    public LessonEntity getEntity(String ref) {
	int i = ref.indexOf("/",1);
	if (i < 0)
	    return null;
	String typeString = ref.substring(1, i);
	String idString = ref.substring(i+1);
	Long id = 0L;
	try {
	    id = Long.parseLong(idString);
	} catch (Exception ignore) {
	    return null;
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
	

    // properties of entities
    public String getTitle() {
	if (type == TYPE_FORUM_TOPIC) {
	    if (topic == null)
		topic = forumManager.getTopicById(true, id);
	    if (topic == null)
		return null;
	    return topic.getTitle();
	} else {
	    if (forum == null)
		forum = forumManager.getForumById(true, id);
	    if (forum == null)
		return null;
	    return forum.getTitle();
	}
    }

    public String getUrl() {
	Site site = null;
	try {
	    site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
	} catch (Exception impossible) {
	    return null;
	}
	ToolConfiguration tool = site.getToolForCommonId("sakai.forums");
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
	    topic = forumManager.getTopicById(true, id);

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
	    topic = forumManager.getTopicById(true, id);

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
	    tool = "/portal/tool/" + tool + "/discussionForum/forumsOnly/dfForums";
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
    public String importObject(String title, String topicTitle, String text, boolean texthtml, String base, String siteId, List<String>attachmentHrefs) {

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
	    String attachHtml = "";
	    if (attachmentHrefs != null && attachmentHrefs.size() > 0) {
		for (String href: attachmentHrefs) {
		    String label = href;
		    int slash = label.lastIndexOf("/");
		    if (slash >= 0)
			label = label.substring(slash+1);
		    if (label.equals(""))
			label = "Attachment";
		    attachHtml = attachHtml + "<p><a target='_blank' href='" + base + href + "'>" + label + "</a>";
		}
	    }

	    if (texthtml) {
		ourTopic.setExtendedDescription(text.replaceAll("\\$IMS-CC-FILEBASE\\$", base) + attachHtml);
		ourTopic.setShortDescription(FormattedText.convertFormattedTextToPlaintext(text));
	    } else {
		ourTopic.setExtendedDescription(FormattedText.convertPlaintextToFormattedText(text) + attachHtml);
		ourTopic.setShortDescription(text);
	    }
	    // there's a better way to do attachments, but it's too complex for now

	    discussionForumManager.saveTopic(ourTopic);

	    // now go back and mmake sure everything is there

	}

	return "/" + FORUM_TOPIC + "/" + ourTopic.getId();
    }

}
