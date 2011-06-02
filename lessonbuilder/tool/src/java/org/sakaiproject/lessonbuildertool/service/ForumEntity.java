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
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;

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

    // access control
    public boolean addEntityControl(String siteId, String groupId) throws IOException {
	return false;
	// not yet
    };
	
    public boolean removeEntityControl(String siteId, String groupId) throws IOException {
	return false;
	// not yet
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

	DiscussionForumManager manager = (DiscussionForumManager)
	    ComponentManager.get("org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager");
	AreaManager areaManager = (AreaManager)
	    ComponentManager.get("org.sakaiproject.api.app.messageforums.AreaManager");
	DiscussionForum ourForum = null;
	DiscussionTopic ourTopic = null;

	int forumtry = 0;
	int topictry = 0;

	for (;;) {

	    ourForum = null;

	    SortedSet<DiscussionForum> forums = new TreeSet<DiscussionForum>(new ForumBySortIndexAscAndCreatedDateDesc());
	    for (DiscussionForum forum: manager.getForumsForMainPage())
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

		ourForum = manager.createForum();
		ourForum.setTitle(title);
		manager.saveForum(siteId, ourForum);
		
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

	    ourTopic = manager.createTopic(ourForum);
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

	    manager.saveTopic(ourTopic);

	    // now go back and mmake sure everything is there

	}

	return "/" + FORUM_TOPIC + "/" + ourTopic.getId();
    }

}
