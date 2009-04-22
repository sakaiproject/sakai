package org.sakaiproject.chat2.model.impl;

import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.chat2.model.ChatChannel;
import org.sakaiproject.chat2.model.ChatManager;
import org.sakaiproject.chat2.model.ChatMessage;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.FormattedText;

public class ChatMessageEntityProvider implements CoreEntityProvider,
		AutoRegisterEntityProvider, Outputable, Inputable, Resolvable,
		Describeable, Createable {

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

		public String getOwner() {
			return owner;
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

		ChatChannel channel = chatManager.getChatChannel(inmsg.getChatChannelId());

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

}
