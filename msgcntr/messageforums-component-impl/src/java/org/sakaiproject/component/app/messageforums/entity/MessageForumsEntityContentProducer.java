/**
 * Copyright (c) 2005-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.component.app.messageforums.entity;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.PortalUrlEnabledProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.util.FormattedText;

@Slf4j
public class MessageForumsEntityContentProducer implements
		EntityContentProducer, PortalUrlEnabledProducer {

	// runtime dependency
	private List addEvents = null;

	// runtime dependency
	private List removeEvents = null;
	
	/**
	 * @param addEvents
	 *        The addEvents to set.
	 */
	public void setAddEvents(List addEvents)
	{
		this.addEvents = addEvents;
	}

	
	public void setRemoveEvents(List removeEvents) {
		this.removeEvents = removeEvents;
	}
	
	// runtime dependency
	private String toolName = null;
	/**
	 * @param toolName
	 *        The toolName to set.
	 */
	public void setToolName(String toolName)
	{
		this.toolName = toolName;
	}
	
	
	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(
			DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}

	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}
	

	// injected dependency
	private SearchService searchService = null;
	/**
	 * @param searchService the searchService to set
	 */
	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}
	
	// injected dependency
	private SearchIndexBuilder searchIndexBuilder = null;

	/**
	 * @param searchIndexBuilder the searchIndexBuilder to set
	 */
	public void setSearchIndexBuilder(SearchIndexBuilder searchIndexBuilder)
	{
		this.searchIndexBuilder = searchIndexBuilder;
	}
	
	/**
	 * Forums Services
	 */
	private MessageForumsMessageManager messageForumsMessageManager;
	public void setMessageForumsMessageManager(
			MessageForumsMessageManager messageForumsMessageManager) {
		this.messageForumsMessageManager = messageForumsMessageManager;
	}

	private DiscussionForumManager discussionForumManager;
	
	public void setDiscussionForumManager(
			DiscussionForumManager discussionForumManager) {
		this.discussionForumManager = discussionForumManager;
	}


	public void setUIPermissionManager(UIPermissionsManager permissionManager) {
		uIPermissionManager = permissionManager;
	}

	private UIPermissionsManager uIPermissionManager; 
	
	private EntityBroker entityBroker;
	public void setEntityBroker(EntityBroker eb) {
		this.entityBroker = eb;
	}
	
	public void init()
	{

		if ( "true".equals(serverConfigurationService.getString(
				"search.enable", "false")))
		{
			for (Iterator i = addEvents.iterator(); i.hasNext();)
			{
				searchService.registerFunction((String) i.next());
			}
			
			for (Iterator i = removeEvents.iterator(); i.hasNext();)
			{
				searchService.registerFunction((String) i.next());
			}
			
			searchIndexBuilder.registerEntityContentProducer(this);
		}
	}
	
	

	
	public boolean canRead(String reference) {
		String msgId = EntityReference.getIdFromRefByKey(reference, "Message");
		Message m = messageForumsMessageManager.getMessageById(Long.valueOf(msgId));
		Topic topic = m.getTopic();
		boolean canRead = false;
		DiscussionTopic dt = discussionForumManager.getTopicById(topic.getId());
		if(dt != null){
			DiscussionForum df = discussionForumManager.getForumById(dt.getOpenForum().getId());
			canRead = uIPermissionManager.isRead(dt, df);
		}
		return canRead;
	}

	public Integer getAction(Event event) {
		String evt = event.getEvent();
		if (evt == null) return SearchBuilderItem.ACTION_UNKNOWN;
		for (Iterator i = addEvents.iterator(); i.hasNext();)
		{
			String match = (String) i.next();
			if (evt.equals(match))
			{
				return SearchBuilderItem.ACTION_ADD;
			}
		}
		for (Iterator i = removeEvents.iterator(); i.hasNext();)
		{
			String match = (String) i.next();
			if (evt.equals(match))
			{
				return SearchBuilderItem.ACTION_DELETE;
			}
		}
		return SearchBuilderItem.ACTION_UNKNOWN;
	}

	public String getContainer(String ref) {
		log.debug("getContainer(String "+  ref + ")");
		return "/site/" + getSiteId(ref);
	}

	public String getContent(String reference) {
		log.debug("getting content for " + reference);
		String msgId = EntityReference.getIdFromRefByKey(reference, "Message");
		Message m = messageForumsMessageManager.getMessageById(Long.valueOf(msgId));
		StringBuilder sb = new StringBuilder();
		if (m != null) {
			sb.append("author: " + m.getAuthor());
			sb.append(" title: " + m.getTitle());
			sb.append(" body: " + FormattedText.convertFormattedTextToPlaintext(m.getBody()));
			/* causes hibernate lazy init error
			List attachments = m.getAttachments();
			if (attachments != null && attachments.size() > 0) {
				for (int q = 0; q < attachments.size(); q++) {
					Attachment at = (Attachment) attachments.get(q);
					String id = at.getAttachmentId();
					EntityContentProducer ecp = searchIndexBuilder
					.newEntityContentProducer(id);
					String attachementDigest = ecp.getContent(id);
					sb.append("\n attachement: \n");
					sb.append(attachementDigest);
					sb.append("\n");
				}
				
			}
			*/
		}
		return sb.toString();
		
	}

	public Reader getContentReader(String reference) {
		return new StringReader(getContent(reference));
	}

	public Map getCustomProperties(String ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCustomRDF(String ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getId(String ref) {
		
		return EntityReference.getIdFromRefByKey(ref, "Message");
	}

	/**
	 * Deprecated method
	 */
	public List getSiteContent(String context) {
		log.warn("DEPRECATED method getSiteContent called");
		return null;
	}

	public Iterator getSiteContentIterator(String context) {
		log.debug("getSiteContentIterator(String "+ context + ")");
		List<Message> messages = messageForumsMessageManager.getAllMessagesInSite(context);
		log.debug("got a list of " + messages.size() + "messages");
		List<String> out = new ArrayList<String>();
		for (int i = 0; i < messages.size(); i ++) {
			Message m = (Message)messages.get(i);
			String ref = buildRefForContextAndId(context, m.getId());
			log.debug("adding " + ref);
			out.add(ref);

		}
		
		
		return out.iterator();
	}

	private String buildRefForContextAndId(String context, Long messageId) {
		///forums/site/705b3a28-e04d-4858-8b66-77fa9d9c9121/Message/2
		String ret = "/forums/site/" + context + "/Message/" + messageId.toString();
		return ret;
	}
	
	public String getSiteId(String reference) {
		return EntityReference.getIdFromRefByKey(reference, "site");
	}

	public String getSubType(String ref) {
		log.debug("getSubType(" +ref);
		return "message";
	}

	public String getTitle(String reference) {
		log.debug("getTitle: " + reference);
		String msgId = EntityReference.getIdFromRefByKey(reference, "Message");
		
		Message m = messageForumsMessageManager.getMessageById(Long.valueOf(msgId));
		if (m !=null)
			return m.getTitle();
		
		return null;
	}

	public String getTool() {
		
		return toolName;
	}

	public String getType(String ref) {
		log.debug("getType(" +ref);
		return "Message";
	}

	public String getUrl(String reference) {
		log.debug("getUrl(" + reference +")");
		
		String url = null;
		Map<String, String> params = new HashMap<String, String>();
		String msgId = EntityReference.getIdFromRefByKey(reference, "Message");
		Message m = messageForumsMessageManager.getMessageById(Long.valueOf(msgId));
		
		if (m != null) {
			params.put("messageId", msgId);
			params.put("topicId", m.getTopic().getId().toString());
			log.debug("got topic: " + m.getTopic().getId().toString());

			//Topic topic = developerHelperService.cloneBean(m.getTopic(), 1, null);
			DiscussionTopic topic = discussionForumManager.getTopicById(m.getTopic().getId());
			params.put("forumId", topic.getOpenForum().getId().toString());

			String context = "/site/" + this.getSiteId(reference);
			log.debug("context: " + context);

			//seems not to work "/discussionForum/message/dfViewMessage"
			String path = "/discussionForum/message/dfViewThreadDirect";

			try {
				url = developerHelperService.getToolViewURL("sakai.forums", path, params, context);
				log.debug("got url" + url);
			} catch (Exception e) {
				log.warn("Could not get the url for message: " + msgId);
			}
		}
		return url;
	}

	public boolean isContentFromReader(String reference) {
		return false;
	}

	public boolean isForIndex(String reference) {
		String msgId = EntityReference.getIdFromRefByKey(reference, "Message");
		Message m = messageForumsMessageManager.getMessageById(Long.valueOf(msgId));
		if (m != null && !m.getDeleted()) {
			log.debug("we will index " + reference);
			return true;
		}
			
		
		return false;
	}

	public boolean matches(String reference) {
		if (reference == null || "".equals(reference)) {
			return false;
		}
		try {
			String prefix = EntityReference.getPrefix(reference);
			log.debug("checking if " + prefix + " matches");
			if (toolName.equals(prefix))
				return true;
		} catch (Exception e) {
			log.warn("unable to parse reference: {}", reference, e);
		}
		return false;
	}

	public boolean matches(Event event) {
		// TODO Auto-generated method stub
		return matches(event.getResource());
	}

}
