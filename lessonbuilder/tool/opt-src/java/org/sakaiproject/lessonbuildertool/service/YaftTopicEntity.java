/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Adrian Fish, a.fish@lancaster.ac.uk
 * 
 * Copyright 2009 The Sakai Foundation
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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.lessonbuildertool.service.LessonSubmission;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.yaft.api.Discussion;
import org.sakaiproject.yaft.api.Forum;
import org.sakaiproject.yaft.api.YaftForumService;

/**
 * Interface to YAFT topics.
 * 
 * @author Adrian Fish <a.fish@lancaster.ac.uk>
 */
@Slf4j
public class YaftTopicEntity implements LessonEntity, ForumInterface {
	private static String TOOL_ID = "sakai.yaft";

	static YaftForumService forumService = (YaftForumService) ComponentManager.get("org.sakaiproject.yaft.api.YaftForumService");

	private LessonEntity nextEntity = null;
	public void setNextEntity(LessonEntity e) {
		nextEntity = e;
	}
	public LessonEntity getNextEntity() {
		return nextEntity;
	}

        // stick ourselves after the last required entity
	public void setPrevEntity(LessonEntity e) {
	    e.setNextEntity(this);
	}

	static MessageLocator messageLocator = null;
	public void setMessageLocator(MessageLocator m) {
		messageLocator = m;
	}

	public void init() {
	}

	public void destroy() {
		log.info("destroy()");
	}

	// to create bean. the bean is used only to call the pseudo-static
	// methods such as getEntitiesInSite. So type, id, etc are left
	// uninitialized

	protected YaftTopicEntity() {
	}

	protected YaftTopicEntity(int type, String id, int level) {
		this.type = type;
		this.id = id;
		this.level = level;
		if (forumService != null)
		    discussion = forumService.getDiscussion(id, false);
		if(discussion == null) {
		    log.error("This YaftTopicEntity's underlying discussion was null. It may have been deleted: " + id);
		}
	}

	public String getToolId() {
		return TOOL_ID;
	}

	// the underlying object, something Sakaiish
	protected String id;
	protected int type;
	protected int level;

	private Discussion discussion = null;

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
		if (type == TYPE_YAFT_TOPIC)
			return true;
		else
			return false;
	}

	public String getReference() {
		if (type == TYPE_YAFT_TOPIC)
			return "/" + YAFT_TOPIC + "/" + id;
		else
			return "/" + FORUM_FORUM + "/" + id;
	}

	public List<LessonEntity> getEntitiesInSite() {
	    return getEntitiesInSite(null);
	}

	public List<LessonEntity> getEntitiesInSite(SimplePageBean bean) {

		List<LessonEntity> ret = new ArrayList<LessonEntity>();

		String currentSiteId = ToolManager.getCurrentPlacement().getContext();

		// LSNBLDR-21. If the tool is not in the current site we shouldn't query
		// for topics owned by the tool.
		Site site = null;
		try {
			site = SiteService.getSite(currentSiteId);
		} catch (Exception impossible) {
			return ret;
		}

		ToolConfiguration tool = site.getToolForCommonId(TOOL_ID);

		if (tool == null) {

			// YAFT is not in this site. Move on to the next provider.

			if (nextEntity != null)
				ret.addAll(nextEntity.getEntitiesInSite());

			return ret;
		}

		if (forumService != null) {
		    for (Forum forum : forumService.getSiteForums(currentSiteId, false)) {
			for (Discussion discussion : forumService.getForumDiscussions(forum.getId(), false)) {
				YaftTopicEntity entity = new YaftTopicEntity(TYPE_YAFT_TOPIC, discussion.getId(), 2);
				entity.discussion = discussion;
				ret.add(entity);
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
		int i = ref.indexOf("/", 1);
		if (i < 0)
			return null;
		String typeString = ref.substring(1, i);
		String idString = ref.substring(i + 1);

		if (typeString.equals(YAFT_TOPIC)) {
			return new YaftTopicEntity(TYPE_YAFT_TOPIC, idString, 2);
		} else if (nextEntity != null) {
			return nextEntity.getEntity(ref);
		} else
			return null;
	}

	public String getTitle() {

		if (type == TYPE_YAFT_TOPIC) {
			if (discussion == null && forumService != null)
				discussion = forumService.getDiscussion(id, false);
			if (discussion == null)
				return null;
			return discussion.getSubject();
		}

		return null;
	}

	public String getUrl() {

		if (discussion == null) {
		    return null;
		}

		Site site = null;

		try {
			site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
		} catch (Exception impossible) {
			return null;
		}

		ToolConfiguration tool = site.getToolForCommonId(TOOL_ID);

		// LSNBLDR-21. If the tool is not in the current site we shouldn't
		// return a url
		if (tool == null) {
			return null;
		}

		if (type == TYPE_YAFT_TOPIC)
			return discussion.getUrl();
		else
			return null;
	}

	// I don't think they have this
	public Date getDueDate() {
		return null;
	}

	// access control
	public boolean addEntityControl(String siteId, String groupId) throws IOException {

		if (discussion == null && forumService != null)
			discussion = forumService.getDiscussion(id, false);

		return true;
	}

	public boolean removeEntityControl(String siteId, String groupId) throws IOException {

		return true;
	}

	/**
	 * From LessonEntity
	 */
	public boolean needSubmission() {
		return false;
	}

	/**
	 * From LessonEntity
	 */
	public LessonSubmission getSubmission(String user) {
		return null;
	}

	/**
	 * From LessonEntity
	 */
	public int getSubmissionCount(String user) {
		return 0;
	}

	/**
	 * From LessonEntity
	 */
	public List<UrlItem> createNewUrls(SimplePageBean bean) {
		ArrayList<UrlItem> list = new ArrayList<UrlItem>();
		String tool = bean.getCurrentTool(TOOL_ID);
		if (tool != null) {
			tool = ServerConfigurationService.getToolUrl()+ "/" + tool;
			list.add(new UrlItem(tool, messageLocator.getMessage("simplepage.create_yaft_topic")));
		}
		if (nextEntity != null)
			list.addAll(nextEntity.createNewUrls(bean));
		return list;
	}

	/**
	 * From LessonEntity
	 */
	public String editItemUrl(SimplePageBean bean) {
		return getUrl();
	}

	/**
	 * From LessonEntity
	 */
	public String editItemSettingsUrl(SimplePageBean bean) {
		return null;
	}

	/**
	 * From ForumInterface
	 */
        public String importObject(String title, String topicTitle, String text, boolean texthtml, String base, String baseDir, String siteId, List<String>arrachmenthrefs, boolean hide) {

		return null;
	}

        public boolean objectExists() {
	    if (discussion == null && forumService != null)
		discussion = forumService.getDiscussion(id, false);
	    if (discussion == null || "DELETED".equals(discussion.getStatus()))
		return false;
	    return true;
	}

	public boolean notPublished(String ref) {
	    return false;
	}

	public boolean notPublished() {
	    return false;
	}

	/**
	 * From LessonEntity
	 */
	public List<String> getGroups(boolean nocache) {
		return null;
	}

	/**
	 * From LessonEntity
	 */
	public void setGroups(Collection<String> groups) {
	}

    public String getObjectId(){
	return null;
    }

    public String findObject(String objectid, Map<String,String>objectMap, String siteid) {
	if (nextEntity != null)
	    return nextEntity.findObject(objectid, objectMap, siteid);
	return null;
    }

    public String getSiteId() {

	if (type == TYPE_YAFT_TOPIC) {
	    if (discussion == null && forumService != null)
		discussion = forumService.getDiscussion(id, false);
	    if (discussion == null)
		return null;
	    return discussion.getSiteId();
	}

	return null;
    }
}
