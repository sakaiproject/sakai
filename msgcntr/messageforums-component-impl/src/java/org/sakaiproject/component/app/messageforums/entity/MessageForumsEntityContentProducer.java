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
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.EntityContentProducerEvents;
import org.sakaiproject.search.api.PortalUrlEnabledProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.util.api.FormattedText;


@Slf4j
public class MessageForumsEntityContentProducer implements
		EntityContentProducer, EntityContentProducerEvents, PortalUrlEnabledProducer {

	// runtime dependency
	private List<String> addEvents = new ArrayList<>();

	// runtime dependency
	private List<String> removeEvents = new ArrayList<>();

	// Map of events to their corresponding search index actions - built from Spring configuration
	private Map<String, Integer> eventActions = new HashMap<>();

	@Setter private FormattedText formattedText;
	@Setter private DeveloperHelperService developerHelperService;
	@Setter private EntityBroker entityBroker;
	@Setter private String toolName = null;
	@Setter private SearchIndexBuilder searchIndexBuilder = null;
	/**
	 * Forums Services
	 */
	@Setter private MessageForumsMessageManager messageForumsMessageManager;
	@Setter private DiscussionForumManager discussionForumManager;
	@Setter private UIPermissionsManager uIPermissionManager;


	/**
	 * @param addEvents
	 *        The addEvents to set.
	 */
	public void setAddEvents(List<String> addEvents)
	{
		this.addEvents = addEvents;
	}


	public void setRemoveEvents(List<String> removeEvents) {
		this.removeEvents = removeEvents;
	}


	public void init()
	{
		// Build eventActions map from Spring-configured lists
		for (String event : addEvents) {
			eventActions.put(event, SearchBuilderItem.ACTION_ADD);
		}
		for (String event : removeEvents) {
			eventActions.put(event, SearchBuilderItem.ACTION_DELETE);
		}

		searchIndexBuilder.registerEntityContentProducer(this);
	}
	
	public boolean canRead(String reference) {
		String msgId = EntityReference.getIdFromRefByKey(reference, "Message");
		Message m = messageForumsMessageManager.getMessageById(Long.valueOf(msgId));
		Topic topic = m.getTopic();
		boolean canRead = false;
		DiscussionTopic dt = discussionForumManager.getTopicById(topic.getId());
		if(dt != null){
			DiscussionForum df = discussionForumManager.getForumById(dt.getOpenForum().getId());
			String[] parts = reference.split(Entity.SEPARATOR);
			if (parts.length >= 4) {
				canRead = uIPermissionManager.isRead(dt, df, null, parts[3]);
			} else {
				canRead = uIPermissionManager.isRead(dt, df);
			}
		}
		return canRead;
	}

	public Integer getAction(Event event) {
		return eventActions.getOrDefault(event.getEvent(), SearchBuilderItem.ACTION_UNKNOWN);
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
			sb.append(" body: " + formattedText.convertFormattedTextToPlaintext(m.getBody()));
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
		return eventActions.containsKey(event.getEvent());
	}

	@Override
	public Set<String> getTriggerFunctions() {
		return eventActions.keySet();
	}

}
