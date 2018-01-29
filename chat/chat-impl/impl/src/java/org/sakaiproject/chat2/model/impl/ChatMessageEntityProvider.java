/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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

package org.sakaiproject.chat2.model.impl;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.chat2.model.ChatChannel;
import org.sakaiproject.chat2.model.ChatManager;
import org.sakaiproject.chat2.model.ChatMessage;
import org.sakaiproject.chat2.model.MessageDateString;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Deleteable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.api.FormattedText;

@Slf4j
public class ChatMessageEntityProvider implements CoreEntityProvider,
		AutoRegisterEntityProvider, Outputable, Inputable, Resolvable,
		Describeable, Createable, Deleteable, CollectionResolvable, ActionsExecutable {

	private ChatManager chatManager;
	
	@Setter private UserDirectoryService userDirectoryService;
	@Setter private SessionManager sessionManager;
	@Setter private FormattedText formattedText;

	public final static String ENTITY_PREFIX = "chat-message";

	// We use a custom object here to avoid side-effects of EB setting the body value for new messages,
	// and avoid returning unwanted fields for getting messages.
	@Data
	public class SimpleChatMessage {

		private String id;
		private String chatChannelId;
		private String context;
		private String owner;
		private String ownerDisplayId;
		private String ownerDisplayName;
		private Date messageDate;
		private String body;
		private boolean removeable;
		private MessageDateString messageDateString;

		public SimpleChatMessage() {
		}

		public SimpleChatMessage(ChatMessage msg)
		{
			this.id = msg.getId();
			this.owner = msg.getOwner();
			this.body = msg.getBody();
			this.messageDate = msg.getMessageDate();
			this.chatChannelId = msg.getChatChannel().getId();
			this.context = msg.getChatChannel().getContext();
			
			try {
				User msgowner = userDirectoryService.getUser(this.owner);
				this.ownerDisplayId = msgowner.getDisplayId();
				this.ownerDisplayName = msgowner.getDisplayName();
			} catch (UserNotDefinedException e) {
				// user not found - ignore
			}
			
			removeable = chatManager.getCanDelete(msg);
		}
	}

	public boolean entityExists(String id) {

		if (id == null) {
			return false;
		}

		if ("".equals(id))
			return false;

		return (chatManager.getMessage(id) != null);
	}

	public Object getEntity(EntityReference ref) {

		String id = ref.getId();

		if (id == null || "".equals(id)) {
			return new ChatMessage();
		}

		ChatMessage msg = chatManager.getMessage(id);

		if (msg == null) {
			throw new IllegalArgumentException("Invalid message id");
		}

		if (!chatManager.getCanReadMessage(msg.getChatChannel())) {
			throw new SecurityException(
					"You do not have permission to access this message");
		}

		return new SimpleChatMessage(msg);
	}

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	public ChatManager getChatManager() {
		return chatManager;
	}

	public void setChatManager(ChatManager chatManager) {
		this.chatManager = chatManager;
	}

	public String[] getHandledOutputFormats() {
		return new String[] { Formats.HTML, Formats.XML, Formats.JSON, Formats.FORM };
	}

	public String[] getHandledInputFormats() {
		return new String[] { Formats.HTML, Formats.XML, Formats.JSON, Formats.FORM };
	}

	public Object getSampleEntity() {
		return new SimpleChatMessage();
	}

	public String createEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		
		SimpleChatMessage inmsg = (SimpleChatMessage) entity;

		String channelId = inmsg.getChatChannelId();
		String context = inmsg.getContext();
		
		ChatChannel channel = null;
		
		if (channelId != null) {
			channel = chatManager.getChatChannel(channelId);
		} else if (context != null) {
			channel = chatManager.getDefaultChannel(context, null);
		}
		
		if (channel == null) {
			throw new IllegalArgumentException("Invalid channel id");
		}

		if (inmsg.getBody() == null || "".equals(inmsg.getBody().trim())) {
			throw new IllegalArgumentException("Empty message");			
		}
		
		ChatMessage message;
		
		try {
			message = getChatManager().createNewMessage(channel,
					sessionManager.getCurrentSessionUserId());
		} catch (PermissionException e) {
			throw new SecurityException("No permission to post in this channel");
		}

		message.setBody(formattedText.convertPlaintextToFormattedText(inmsg.getBody()));

		chatManager.updateMessage(message);
		chatManager.sendMessage(message);

		return message.getId();
	}

	public List<SimpleChatMessage> getEntities(EntityReference ref, Search search) {

		List<SimpleChatMessage> msglist = new ArrayList<SimpleChatMessage>();

		String channelId = null;
		String context = null;
		
		// by channel id
		
		Restriction channelRes = search.getRestrictionByProperty("channelId");

		if (channelRes != null) {
			channelId = channelRes.getStringValue();
		}

		// by context (site)
		
		Restriction locRes = search.getRestrictionByProperty(CollectionResolvable.SEARCH_LOCATION_REFERENCE);

        if (locRes != null) {
        	String location = locRes.getStringValue();
        	context = new EntityReference(location).getId();
        }

        // number of messages
        
		Restriction itemRes = search.getRestrictionByProperty("items");

		int items = 10;
		
		if (itemRes != null) {
			// set item count
			try {
				items = Integer.valueOf(itemRes.getStringValue()).intValue();
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Invalid items format - please specify an integer");
			}
		}

		// messages since this date (timestamp)

		Date fromdate = null;

		Restriction dateRes = search.getRestrictionByProperty("messageDate");
		if (dateRes != null) {
			try {
				long timestamp = Long.valueOf(dateRes.getStringValue()).longValue();
				fromdate = new Date(timestamp);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Invalid timestamp format - please specify in milliseconds since epoch format");
			}
		}
		
		ChatChannel channel = null;
		
		if (channelId != null) {
			channel = chatManager.getChatChannel(channelId);
		}
		
		boolean sortAsc = true;
		
		try {
			List<ChatMessage> chatmsgs = chatManager.getChannelMessages(channel, context, fromdate, 0, items, sortAsc);
			
			for (ChatMessage c : chatmsgs) {
				msglist.add(new SimpleChatMessage(c));
			}

		} catch (PermissionException e) {
			throw new SecurityException("No permission to read messages from this channel or context");
		}	
		
		return msglist;
	}
	
	public void deleteEntity(EntityReference ref, Map<String, Object> params){
		
		String id = ref.getId();

		ChatMessage msg = chatManager.getMessage(id);

		if (msg == null) {
			throw new IllegalArgumentException("Invalid message id");
		}

		try {
			chatManager.deleteMessage(msg);
		}catch(Exception e){
			throw new SecurityException("No permission to remove this message");
		}

	}
	
	/**
     * The JS client calls this to grab the latest data in one call. Latest messages, online users and removed messages
     * (in a channel) are all returned in one lump of JSON. Also is used to indicate that current user is alive (updating his heartbeat).
     */
	@EntityCustomAction(action = "chatData", viewKey = EntityView.VIEW_SHOW)
	public Map<String,Object> handleChatData(EntityReference ref, Map<String,Object> params) {
		User currentUser = userDirectoryService.getCurrentUser();
		User anon = userDirectoryService.getAnonymousUser();
		
		if (anon.equals(currentUser)) {
			log.debug("No current user");
			throw new SecurityException("You must be logged in to use this service");
		}
		
		String siteId = (String) params.get("siteId");
		if (StringUtils.isBlank(siteId)) {
			log.debug("No siteId specified");
			throw new SecurityException("You must be specify the site ID");
		}
		log.debug("siteId: {}", siteId);
		
		String channelId = (String) params.get("channelId");
		if (StringUtils.isBlank(channelId)) {
			log.debug("No channelId specified");
			throw new SecurityException("You must be specify the channel ID");
		}
		log.debug("channelId: {}", channelId);
		
		ChatChannel channel = chatManager.getChatChannel(channelId);
		if (!chatManager.getCanReadMessage(channel)) {
			throw new SecurityException("You do not have permission to access this channel");
		}

		Map<String,Object> data = chatManager.handleChatData(siteId, channelId, chatManager.getSessionKey());
		if(data.get("messages") != null){
			List<ChatMessage> messages = (List<ChatMessage>)data.get("messages");
			List<SimpleChatMessage> msglist = new ArrayList<SimpleChatMessage>();
			for(ChatMessage msg : messages){
				SimpleChatMessage s_msg = new SimpleChatMessage(msg);
				s_msg.setMessageDateString(chatManager.getMessageDateString(msg));
				msglist.add(s_msg);
			}
			data.put("messages", msglist);
		}
		return data;
	}

}
