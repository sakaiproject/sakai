/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.chat2.model.impl;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.chat2.model.ChatChannel;
import org.sakaiproject.chat2.model.ChatManager;
import org.sakaiproject.chat2.model.ChatMessage;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;

public class ChatMessageEntityProvider implements CoreEntityProvider,
		AutoRegisterEntityProvider, Outputable, Inputable, Resolvable,
		Describeable, Createable, CollectionResolvable {

	private ChatManager chatManager;

	public final static String ENTITY_PREFIX = "chat-message";

	protected final Log LOG = LogFactory.getLog(getClass());

	// We use a custom object here to avoid side-effects of EB setting the body value for new messages,
	// and avoid returning unwanted fields for getting messages.
	
	public class SimpleChatMessage {

		private String id;
		private String chatChannelId;
		private String context;
		private String owner;
		private String ownerDisplayId;
		private String ownerDisplayName;
		private Date messageDate;
		private String body;

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
				User msgowner = UserDirectoryService.getUser(this.owner);
				this.ownerDisplayId = msgowner.getDisplayId();
				this.ownerDisplayName = msgowner.getDisplayName();
			} catch (UserNotDefinedException e) {
				// user not found - ignore
			}
		}
		
		public String getBody() {
			return body;
		}

		public void setBody(String body) {
			this.body = body;
		}

		public String getChatChannelId() {
			return chatChannelId;
		}

		public void setChatChannelId(String chatChannelId) {
			this.chatChannelId = chatChannelId;
		}

		public String getId() {
			return id;
		}

		public Date getMessageDate() {
			return messageDate;
		}

		public String getContext() {
			return context;
		}

		public void setContext(String context) {
			this.context = context;
		}
	
		public String getOwner() {
			return owner;
		}

		public void setOwnerDisplayId(String ownerDisplayId) {
			this.ownerDisplayId = ownerDisplayId;
		}

		public String getOwnerDisplayId() {
			return ownerDisplayId;
		}

		public void setOwnerDisplayName(String ownerDisplayName) {
			this.ownerDisplayName = ownerDisplayName;
		}

		public String getOwnerDisplayName() {
			return ownerDisplayName;
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
					SessionManager.getCurrentSessionUserId());
		} catch (PermissionException e) {
			throw new SecurityException("No permission to post in this channel");
		}

		message.setBody(FormattedText.convertPlaintextToFormattedText(inmsg.getBody()));

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

}
