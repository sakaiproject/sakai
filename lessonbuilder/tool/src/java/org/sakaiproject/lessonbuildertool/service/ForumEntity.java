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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.SessionFactory;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.api.app.messageforums.MembershipItem;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import lombok.extern.slf4j.Slf4j;
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

// this monstrosity has to do hibernate directly to avoid the dreaded 
// multiple objects with the same ID error. I do merge rather than save.
// unfortunately the normal API does saveOrUpdate.
//  This is complicated by the fact that we sessionFactory is set only
// when the main bean is set up. But setGroups is only called from 
// instances of this class that aren't the bean. Hence in the bean
// we save a copy of the session factory and then set it in the
// instance when we need it.

@Slf4j
public class ForumEntity extends HibernateDaoSupport implements LessonEntity, ForumInterface {

    protected static final int DEFAULT_EXPIRATION = 10 * 60;
    private static SessionFactory sessionFactory = null;

    static MessageForumsForumManager forumManager = (MessageForumsForumManager)
	ComponentManager.get("org.sakaiproject.api.app.messageforums.MessageForumsForumManager");
    static MessageForumsMessageManager messageManager = (MessageForumsMessageManager)
	ComponentManager.get("org.sakaiproject.api.app.messageforums.MessageForumsMessageManager");
    static UIPermissionsManager uiPermissionsManager = (UIPermissionsManager)
	ComponentManager.get("org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager");
    static DiscussionForumManager discussionForumManager = (DiscussionForumManager)
	ComponentManager.get("org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager");
    static AreaManager areaManager = (AreaManager)
	ComponentManager.get("org.sakaiproject.api.app.messageforums.AreaManager");
    static MessageForumsTypeManager typeManager = (MessageForumsTypeManager)
	ComponentManager.get("org.sakaiproject.api.app.messageforums.MessageForumsTypeManager");
	static ServerConfigurationService serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
	static SiteService siteService = ComponentManager.get(SiteService.class);
	static ToolManager toolManager = ComponentManager.get(ToolManager.class);
	static SqlService sqlService = ComponentManager.get(SqlService.class);

    private LessonEntity nextEntity = null;
    private SimplePageBean simplePageBean;

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

	static AuthzGroupService authzGroupService = null;
	public void setAuthzGroupService(AuthzGroupService service) {
		authzGroupService = service;
	}

    private static SimplePageToolDao simplePageToolDao;
    public void setSimplePageToolDao(Object dao) {
	//	log.info("set dao " + dao);
	simplePageToolDao = (SimplePageToolDao) dao;
    }

    private static HibernateTemplate hibernateTemplate = null;

    public void init () {	
	sessionFactory = getSessionFactory();
    }

    protected void initDao() throws Exception {
	super.initDao();
	log.info("initDao template " + getHibernateTemplate());
	hibernateTemplate = getHibernateTemplate();
    }

    public void destroy()
    {
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

    public boolean showAdditionalLink() {
		return false;
    }

    public boolean isUsable() {
	return true;
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
    ToolConfiguration tool = siteService.getOptionalSite(toolManager.getCurrentPlacement().getContext())
			.map(site -> site.getToolForCommonId("sakai.forums"))
			.orElse(null);
	
    if(tool == null) {
    	
    	// Forums is not in this site. Move on to the next provider.
    	
    	if (nextEntity != null) 
    		ret.addAll(nextEntity.getEntitiesInSite());
    	
    	return ret;
    }

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
	    Topic topic = forumManager.getTopicById(flag, id);
	    if (topic == null)
		return null;
	    // the purpose of this is to trigger a null pointer error if the forum doesn't exist
	    // if you delete the forum, it doesn't delete the topics, but showing a topic without
	    // its forum causes confusion. The /direct code does the following, so it will give
	    // an error. 
	    if (topic.getOpenForum().getArea() == null)
		return null;
	    return topic;
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

    public String getDescription(){
        return "";
    }

    public String getUrl() {
	
	if (type == TYPE_FORUM_TOPIC) {
	    if (topic == null)
		topic = getTopicById(true, id);
	    if (topic == null)
		return "javascript:alert('" + messageLocator.getMessage("simplepage.forumdeleted") + "')";
	} else {
	    if (forum == null)
		forum = getForumById(true, id);
	    if (forum == null)
		return "javascript:alert('" + messageLocator.getMessage("simplepage.forumdeleted") + "')";
	}

		ToolConfiguration tool = siteService.getOptionalSite(toolManager.getCurrentPlacement().getContext())
				.map(site -> site.getToolForCommonId("sakai.forums"))
				.orElse(null);

	// LSNBLDR-21. If the tool is not in the current site we shouldn't return a url
	if(tool == null) {
	    return null;
	}
	
	String placement = tool.getId();

	if (type == TYPE_FORUM_TOPIC)
	    // if /direct doesn't work, but that was only in one beta release
	    //  return "/messageforums-tool/jsp/discussionForum/message/dfAllMessagesDirect.jsf?topicId=" + id + "&placementId=" + placement;
	    return "/direct/forum_topic/" + id;
	else
	    return "/direct/forum/" + id;
    }

    // I don't think they have this
    public Date getDueDate() {
	return null;
    }

    public LessonSubmission getSubmission(String user) {
	return null; // not used
    }
    public int getSubmissionCount(String user) {
	if (type == TYPE_FORUM_TOPIC)
	    return messageManager.findAuhtoredMessageCountByTopicIdByUserId(id, user);
	else
	    return messageManager.findAuthoredStatsForStudentByForumId(user, id).size();
    }

    // URL to create a new item. Normally called from the generic entity, not a specific one                                                 
    // can't be null                                                                                                                         
    public List<UrlItem> createNewUrls(SimplePageBean bean) {
	ArrayList<UrlItem> list = new ArrayList<UrlItem>();
	String tool = bean.getCurrentTool("sakai.forums");
	if (tool != null) {
	    tool = serverConfigurationService.getToolUrl() + "/" + tool + "/discussionForum/forumsOnly/dfForums";
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

    //   aaa/bb/../cc ==>
    //   aaa/cc
    public String removeDotDot(String s) {
	while (true) {
	    int i = s.indexOf("/../");
	    if (i < 1)
		return s;
	    int j = s.lastIndexOf("/", i-1);
	    if (j < 0)
		j = 0;
	    else
		j = j + 1;
	    s = s.substring(0, j) + s.substring(i+4);
	}
    }

    // returns SakaiId of thing just created
    public String importObject(String title, String topicTitle, String text, boolean texthtml, String base, String baseDir, String siteId, List<String>attachmentHrefs, boolean hide) {

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
		    log.info("oops, forum still not there the second time");
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
		log.info("oops, topic still not there the second time");
		return null;
	    }
	    topictry ++;

	    // create it

	    ourTopic = discussionForumManager.createTopic(ourForum);
	    ourTopic.setTitle(topicTitle);

	    if (attachmentHrefs != null && attachmentHrefs.size() > 0) {
		for (String href: attachmentHrefs) {
		    // we don't have any real label for attachments. About all we can do is use the filename, without path
		    String label = href;
		    int slash = label.lastIndexOf("/");
		    if (slash >= 0)
			label = label.substring(slash+1);
		    if (label.equals(""))
			label = "Attachment";

		    // basedir is a folder in contenthosting starting with /group/ where our content has been loaded
		    // discussionForum needs a Sakai content resource ID for the attachment
		    Attachment thisDFAttach = discussionForumManager.createDFAttachment(removeDotDot(baseDir + href), label);
		    ourTopic.addAttachment(thisDFAttach);
		}
	    }

	    String shortText = null;
	    FormattedText formattedText = ComponentManager.get(FormattedText.class);
	    if (texthtml) {
		ourTopic.setExtendedDescription(text.replaceAll("\\$IMS-CC-FILEBASE\\$", base));
		shortText = formattedText.convertFormattedTextToPlaintext(text);
	    } else {
		ourTopic.setExtendedDescription(formattedText.convertPlaintextToFormattedText(text));
		shortText = text;
	    }
	    shortText = org.apache.commons.lang3.StringUtils.abbreviate(shortText,254);

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
	if (type == TYPE_FORUM_TOPIC) {
	    if (topic == null)
		topic = getTopicById(true, id);
	    return topic != null;
	} else {
	    if (forum == null)
		forum = getForumById(true, id);
	    return forum != null;
	}

    }

    public boolean notPublished(String ref) {
	return false;
    }

    public boolean notPublished() {
	if (!objectExists())
	    return true;
	boolean isDraft = false;
	if (type == TYPE_FORUM_TOPIC) {
		isDraft = topic.getOpenForum().getDraft();
		if(isDraft){
			return true;
		}
		boolean topicVisibleByDate = !topic.getAvailabilityRestricted() || isResourceVisibleByDate(topic.getOpenDate(), topic.getCloseDate());
		boolean forumVisibleByDate = !topic.getOpenForum().getAvailabilityRestricted() || isResourceVisibleByDate(topic.getOpenForum().getOpenDate(), topic.getOpenForum().getCloseDate());
	    return !topicVisibleByDate || !forumVisibleByDate;
	} else {
		isDraft = ((DiscussionForum)forum).getDraft();
		if(isDraft){
			return true;
		}
		boolean forumVisibleByDate = !((DiscussionForum)forum).getAvailabilityRestricted() || isResourceVisibleByDate(((DiscussionForum)forum).getOpenDate(), ((DiscussionForum)forum).getCloseDate());
		
	    return !forumVisibleByDate;
	}

    }

	private static boolean isResourceVisibleByDate(Date openDate, Date closeDate){
		Date currentDate = new Date();

		if(openDate != null && closeDate != null){
			return currentDate.after(openDate) && currentDate.before(closeDate);
		} else if(openDate != null && closeDate == null){
			return currentDate.after(openDate);
		} else if(openDate == null && closeDate != null){
			return currentDate.before(closeDate);
		}

		return true;
	}

    // return the list of groups if the item is only accessible to specific groups
    // null if it's accessible to the whole site.
    public List<String> getGroups(boolean nocache) {

		Set<DBMembershipItem> oldMembershipItemSet = null;
		if (type == TYPE_FORUM_TOPIC) {
			topic = getTopicById(true, id);
			if (topic == null) {
				return null;
			}
			oldMembershipItemSet = uiPermissionsManager.getTopicItemsSet((DiscussionTopic)topic);
		} else if (type == TYPE_FORUM_FORUM) {
			forum = getForumById(true, id);
			if (forum == null) {
				return null;
			}
			oldMembershipItemSet = uiPermissionsManager.getForumItemsSet((DiscussionForum)forum);
		} else {
			return null;
		}

	List <String>ret = new ArrayList<String>();
	Collection<Group> groups = siteService.getOptionalSite(toolManager.getCurrentPlacement().getContext())
			.map(Site::getGroups)
			.orElse(Collections.emptyList());

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

	if (ret.size() == 0)
	    return null;
	else
	    return ret;
    }

	public void setGroups(Collection<String> groups) {
		Set<String> groupIds = (groups == null) ? Collections.emptySet() : new HashSet<>(groups);
		if (type == TYPE_FORUM_TOPIC) {
			discussionForumManager.setTopicGroupRestrictions(id, groupIds);
		} else if (type == TYPE_FORUM_FORUM) {
			discussionForumManager.setForumGroupRestrictions(id, groupIds);
		}
	}

	// only used for topics
    public String getObjectId(){
	String title = getTitle();
	// fetches topic as well
	if (title == null)
	    return null;

	if (type == TYPE_FORUM_TOPIC) {
	    BaseForum forum = topic.getBaseForum();
	    return "forum_topic/" + id + "/" + title + "\n" + forum.getTitle();
	} else {
	    return "forum_forum/" + id + "/" + title;
	}
    }

    public String findObject(String objectid, Map<String,String>objectMap, String siteid) {
        if (!objectid.startsWith("forum_topic/") && !objectid.startsWith("forum_forum/")) {
            if (nextEntity != null) {
                return nextEntity.findObject(objectid, objectMap, siteid);
            }
	    return null;
	}

	// isolate forum_topic/NNN from title
	int i = 0;

        if (objectid.startsWith("forum_topic/"))
	    i = objectid.indexOf("/", "forum_topic/".length());
	else
	    i = objectid.indexOf("/", "forum_forum/".length());
	if (i <= 0)
	    return null;
	String realobjectid = objectid.substring(0, i);
	// msgcenter uses forum/ not forum_forum/
        if (objectid.startsWith("forum_forum/"))
	    realobjectid = realobjectid.substring("forum_".length());

	// now see if it's in the map
	String newtopic = objectMap.get(realobjectid);
	if (newtopic != null) {
	    if (objectid.startsWith("forum_forum/"))
		return "/forum_" + newtopic;   // forum/ID >> /forum_forum/ID
	    else 
		return "/" + newtopic;  // sakaiid is /forum_topic/ID
	}

	// this must be 2.8. Can't find the topic in the map
	// i is start of title
	String title = null;
	String forumtitle = null;
        if (objectid.startsWith("forum_topic/")) {
	    int j = objectid.indexOf("\n");
	    title = objectid.substring(i+1,j);
	    forumtitle = objectid.substring(j+1);
	} else {
	    forumtitle = objectid.substring(i+1);
	}

	// unfortunately we have to search the topic tree to find it.
	SortedSet<DiscussionForum> forums = new TreeSet<DiscussionForum>(new ForumBySortIndexAscAndCreatedDateDesc());
	for (DiscussionForum forum: forumManager.getForumsForMainPage())
	    forums.add(forum);

	// security. assume this is only used in places where it's OK, so skip security checks
	// ignore draft status. We want to show drafts.
	for (DiscussionForum forum: forums) {
	    if (forum.getTitle().equals(forumtitle)) {
		if (title == null) // object is forum not topic
		    return "/forum_forum/" + forum.getId();
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

	List<String> siteIds = sqlService.dbRead(sql, fields, null);

	if (siteIds != null && siteIds.size() > 0)
	    return siteIds.get(0);
	
	return null;

    }
	@Override
	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

	@Override
    public void preShowItem(SimplePageItem simplePageItem)
    {
    }

}
