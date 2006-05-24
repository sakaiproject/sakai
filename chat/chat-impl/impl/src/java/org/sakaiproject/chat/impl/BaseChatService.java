/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.chat.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.chat.api.ChatChannel;
import org.sakaiproject.chat.api.ChatChannelEdit;
import org.sakaiproject.chat.api.ChatMessage;
import org.sakaiproject.chat.api.ChatMessageEdit;
import org.sakaiproject.chat.api.ChatMessageHeader;
import org.sakaiproject.chat.api.ChatMessageHeaderEdit;
import org.sakaiproject.chat.api.ChatService;
import org.sakaiproject.entity.api.ContextObserver;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.message.api.MessageChannel;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.message.api.MessageHeaderEdit;
import org.sakaiproject.message.impl.BaseMessageService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * <p>
 * BaseChatService extends the BaseMessageService for the specifics of Chat.
 * </p>
 */
public abstract class BaseChatService extends BaseMessageService implements ChatService, ContextObserver
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(BaseChatService.class);

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();

		// register functions
		FunctionManager.registerFunction(eventId(SECURE_READ));
		FunctionManager.registerFunction(eventId(SECURE_ADD));
		FunctionManager.registerFunction(eventId(SECURE_REMOVE_ANY));
		FunctionManager.registerFunction(eventId(SECURE_REMOVE_OWN));

		// entity producer registration
		m_entityManager.registerEntityProducer(this, REFERENCE_ROOT);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * StorageUser implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct a new continer given just ids.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return The new containe Resource.
	 */
	public Entity newContainer(String ref)
	{
		return new BaseChatChannelEdit(ref);
	}

	/**
	 * Construct a new container resource, from an XML element.
	 * 
	 * @param element
	 *        The XML.
	 * @return The new container resource.
	 */
	public Entity newContainer(Element element)
	{
		return new BaseChatChannelEdit(element);
	}

	/**
	 * Construct a new container resource, as a copy of another
	 * 
	 * @param other
	 *        The other contianer to copy.
	 * @return The new container resource.
	 */
	public Entity newContainer(Entity other)
	{
		return new BaseChatChannelEdit((MessageChannel) other);
	}

	/**
	 * Construct a new rsource given just an id.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param id
	 *        The id for the new object.
	 * @param others
	 *        (options) array of objects to load into the Resource's fields.
	 * @return The new resource.
	 */
	public Entity newResource(Entity container, String id, Object[] others)
	{
		return new BaseChatMessageEdit((MessageChannel) container, id);
	}

	/**
	 * Construct a new resource, from an XML element.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param element
	 *        The XML.
	 * @return The new resource from the XML.
	 */
	public Entity newResource(Entity container, Element element)
	{
		return new BaseChatMessageEdit((MessageChannel) container, element);
	}

	/**
	 * Construct a new resource from another resource of the same type.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param other
	 *        The other resource.
	 * @return The new resource as a copy of the other.
	 */
	public Entity newResource(Entity container, Entity other)
	{
		return new BaseChatMessageEdit((MessageChannel) container, (Message) other);
	}

	/**
	 * Construct a new continer given just ids.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return The new containe Resource.
	 */
	public Edit newContainerEdit(String ref)
	{
		BaseChatChannelEdit rv = new BaseChatChannelEdit(ref);
		rv.activate();
		return rv;
	}

	/**
	 * Construct a new container resource, from an XML element.
	 * 
	 * @param element
	 *        The XML.
	 * @return The new container resource.
	 */
	public Edit newContainerEdit(Element element)
	{
		BaseChatChannelEdit rv = new BaseChatChannelEdit(element);
		rv.activate();
		return rv;
	}

	/**
	 * Construct a new container resource, as a copy of another
	 * 
	 * @param other
	 *        The other contianer to copy.
	 * @return The new container resource.
	 */
	public Edit newContainerEdit(Entity other)
	{
		BaseChatChannelEdit rv = new BaseChatChannelEdit((MessageChannel) other);
		rv.activate();
		return rv;
	}

	/**
	 * Construct a new rsource given just an id.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param id
	 *        The id for the new object.
	 * @param others
	 *        (options) array of objects to load into the Resource's fields.
	 * @return The new resource.
	 */
	public Edit newResourceEdit(Entity container, String id, Object[] others)
	{
		BaseChatMessageEdit rv = new BaseChatMessageEdit((MessageChannel) container, id);
		rv.activate();
		return rv;
	}

	/**
	 * Construct a new resource, from an XML element.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param element
	 *        The XML.
	 * @return The new resource from the XML.
	 */
	public Edit newResourceEdit(Entity container, Element element)
	{
		BaseChatMessageEdit rv = new BaseChatMessageEdit((MessageChannel) container, element);
		rv.activate();
		return rv;
	}

	/**
	 * Construct a new resource from another resource of the same type.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param other
	 *        The other resource.
	 * @return The new resource as a copy of the other.
	 */
	public Edit newResourceEdit(Entity container, Entity other)
	{
		BaseChatMessageEdit rv = new BaseChatMessageEdit((MessageChannel) container, (Message) other);
		rv.activate();
		return rv;
	}

	/**
	 * Collect the fields that need to be stored outside the XML (for the resource).
	 * 
	 * @return An array of field values to store in the record outside the XML (for the resource).
	 */
	public Object[] storageFields(Entity r)
	{
		Object[] rv = new Object[4];
		rv[0] = ((Message) r).getHeader().getDate();
		rv[1] = ((Message) r).getHeader().getFrom().getId();
		rv[2] = "0";
		rv[3] = r.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW) == null ? "0" : "1";

		return rv;
	}

	/**
	 * Check if this resource is in draft mode.
	 * 
	 * @param r
	 *        The resource.
	 * @return true if the resource is in draft mode, false if not.
	 */
	public boolean isDraft(Entity r)
	{
		return false;
	}

	/**
	 * Access the resource owner user id.
	 * 
	 * @param r
	 *        The resource.
	 * @return The resource owner user id.
	 */
	public String getOwnerId(Entity r)
	{
		return ((Message) r).getHeader().getFrom().getId();
	}

	/**
	 * Access the resource date.
	 * 
	 * @param r
	 *        The resource.
	 * @return The resource date.
	 */
	public Time getDate(Entity r)
	{
		return ((Message) r).getHeader().getDate();
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Abstractions, etc. satisfied
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Report the Service API name being implemented.
	 */
	protected String serviceName()
	{
		return ChatService.class.getName();
	}

	/**
	 * Construct a new message header from XML in a DOM element.
	 * 
	 * @param id
	 *        The message Id.
	 * @return The new message header.
	 */
	protected MessageHeaderEdit newMessageHeader(Message msg, String id)
	{
		return new BaseChatMessageHeaderEdit(msg, id);

	} // newMessageHeader

	/**
	 * Construct a new message header from XML in a DOM element.
	 * 
	 * @param el
	 *        The XML DOM element that has the header information.
	 * @return The new message header.
	 */
	protected MessageHeaderEdit newMessageHeader(Message msg, Element el)
	{
		return new BaseChatMessageHeaderEdit(msg, el);

	} // newMessageHeader

	/**
	 * Construct a new message header as a copy of another.
	 * 
	 * @param other
	 *        The other header to copy.
	 * @return The new message header.
	 */
	protected MessageHeaderEdit newMessageHeader(Message msg, MessageHeader other)
	{
		return new BaseChatMessageHeaderEdit(msg, other);

	} // newMessageHeader

	/**
	 * Form a tracking event string based on a security function string.
	 * 
	 * @param secure
	 *        The security function string.
	 * @return The event tracking string.
	 */
	protected String eventId(String secure)
	{
		return "chat." + secure;

	} // eventId

	/**
	 * Return the reference rooot for use in resource references and urls.
	 * 
	 * @return The reference rooot for use in resource references and urls.
	 */
	protected String getReferenceRoot()
	{
		return REFERENCE_ROOT;

	} // getReferenceRoot

	/**
	 * {@inheritDoc}
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		if (reference.startsWith(REFERENCE_ROOT))
		{
			String[] parts = StringUtil.split(reference, Entity.SEPARATOR);

			String id = null;
			String subType = null;
			String context = null;
			String container = null;

			// the first part will be null, then next the service, the third will be "msg" or "channel"
			if (parts.length > 2)
			{
				subType = parts[2];
				if (REF_TYPE_CHANNEL.equals(subType))
				{
					// next is the context id
					if (parts.length > 3)
					{
						context = parts[3];

						// next is the channel id
						if (parts.length > 4)
						{
							id = parts[4];
						}
					}
				}
				else if (REF_TYPE_MESSAGE.equals(subType))
				{
					// next three parts are context, channel (container) and mesage id
					if (parts.length > 5)
					{
						context = parts[3];
						container = parts[4];
						id = parts[5];
					}
				}
				else
					M_log.warn("parse(): unknown message subtype: " + subType + " in ref: " + reference);
			}

			ref.set(APPLICATION_ID, subType, id, container, context);

			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextCreated(String context, boolean toolPlacement)
	{
		if (toolPlacement) enableMessageChannel(context);
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextUpdated(String context, boolean toolPlacement)
	{
		if (toolPlacement) enableMessageChannel(context);
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextDeleted(String context, boolean toolPlacement)
	{
		disableMessageChannel(context);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] myToolIds()
	{
		String[] toolIds = { "sakai.chat" };
		return toolIds;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ChatService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Return a specific chat channel.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return the ChatChannel that has the specified name.
	 * @exception IdUnusedException
	 *            If this name is not defined for a chat channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to the channel.
	 */
	public ChatChannel getChatChannel(String ref) throws IdUnusedException, PermissionException
	{
		return (ChatChannel) getChannel(ref);

	} // getChatChannel

	/**
	 * Add a new chat channel.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return The newly created channel.
	 * @exception IdUsedException
	 *            if the id is not unique.
	 * @exception IdInvalidException
	 *            if the id is not made up of valid characters.
	 * @exception PermissionException
	 *            if the user does not have permission to add a channel.
	 */
	public ChatChannelEdit addChatChannel(String ref) throws IdUsedException, IdInvalidException, PermissionException
	{
		return (ChatChannelEdit) addChannel(ref);

	} // addChatChannel

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ResourceService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		return "chat";
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ChatChannel implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseChatChannelEdit extends BaseMessageChannelEdit implements ChatChannelEdit
	{
		/**
		 * Construct with a reference.
		 * 
		 * @param ref
		 *        The channel reference.
		 */
		public BaseChatChannelEdit(String ref)
		{
			super(ref);

		} // BaseChatChannelEdit

		/**
		 * Construct as a copy of another message.
		 * 
		 * @param other
		 *        The other message to copy.
		 */
		public BaseChatChannelEdit(MessageChannel other)
		{
			super(other);

		} // BaseChatChannelEdit

		/**
		 * Construct from a channel (and possibly messages) already defined in XML in a DOM tree. The Channel is added to storage.
		 * 
		 * @param el
		 *        The XML DOM element defining the channel.
		 */
		public BaseChatChannelEdit(Element el)
		{
			super(el);

		} // BaseChatChannelEdit

		/**
		 * Return a specific chat channel message, as specified by message name.
		 * 
		 * @param messageId
		 *        The id of the message to get.
		 * @return the ChatMessage that has the specified id.
		 * @exception IdUnusedException
		 *            If this name is not a defined message in this chat channel.
		 * @exception PermissionException
		 *            If the user does not have any permissions to read the message.
		 */
		public ChatMessage getChatMessage(String messageId) throws IdUnusedException, PermissionException
		{
			return (ChatMessage) getMessage(messageId);

		} // getChatMessage

		/**
		 * A (ChatMessageEdit) cover for editMessage. Return a specific channel message, as specified by message name, locked for update. Must commitEdit() to make official, or cancelEdit() when done!
		 * 
		 * @param messageId
		 *        The id of the message to get.
		 * @return the Message that has the specified id.
		 * @exception IdUnusedException
		 *            If this name is not a defined message in this channel.
		 * @exception PermissionException
		 *            If the user does not have any permissions to read the message.
		 * @exception InUseException
		 *            if the current user does not have permission to mess with this user.
		 */
		public ChatMessageEdit editChatMessage(String messageId) throws IdUnusedException, PermissionException, InUseException
		{
			return (ChatMessageEdit) editMessage(messageId);

		} // editChatMessage

		/**
		 * A (ChatMessageEdit) cover for addMessage. Add a new message to this channel. Must commitEdit() to make official, or cancelEdit() when done!
		 * 
		 * @return The newly added message, locked for update.
		 * @exception PermissionException
		 *            If the user does not have write permission to the channel.
		 */
		public ChatMessageEdit addChatMessage() throws PermissionException
		{
			return (ChatMessageEdit) addMessage();

		} // addChatMessage

		/**
		 * a (ChatMessage) cover for addMessage to add a new message to this channel.
		 * 
		 * @param attachments
		 *        The message header attachments, a vector of Reference objects.
		 * @param body
		 *        The body text.
		 * @return The newly added message.
		 * @exception PermissionException
		 *            If the user does not have write permission to the channel.
		 */
		public ChatMessage addChatMessage(List attachments, String body) throws PermissionException
		{
			ChatMessageEdit edit = (ChatMessageEdit) addMessage();
			ChatMessageHeaderEdit header = edit.getChatHeaderEdit();
			edit.setBody(body);
			header.replaceAttachments(attachments);

			commitMessage(edit);

			return edit;

		} // addChatMessage

	} // class BaseChatChannelEdit

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ChatMessage implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseChatMessageEdit extends BaseMessageEdit implements ChatMessageEdit
	{
		/**
		 * Construct.
		 * 
		 * @param channel
		 *        The channel in which this message lives.
		 * @param id
		 *        The message id.
		 */
		public BaseChatMessageEdit(MessageChannel channel, String id)
		{
			super(channel, id);

		} // BaseChatMessageEdit

		/**
		 * Construct as a copy of another message.
		 * 
		 * @param other
		 *        The other message to copy.
		 */
		public BaseChatMessageEdit(MessageChannel channel, Message other)
		{
			super(channel, other);

		} // BaseChatMessageEdit

		/**
		 * Construct from an existing definition, in xml.
		 * 
		 * @param channel
		 *        The channel in which this message lives.
		 * @param el
		 *        The message in XML in a DOM element.
		 */
		public BaseChatMessageEdit(MessageChannel channel, Element el)
		{
			super(channel, el);

		} // BaseChatMessageEdit

		/**
		 * Access the chat message header.
		 * 
		 * @return The chat message header.
		 */
		public ChatMessageHeader getChatHeader()
		{
			return (ChatMessageHeader) getHeader();

		} // getChatHeader

		/**
		 * Access the chat message header.
		 * 
		 * @return The chat message header.
		 */
		public ChatMessageHeaderEdit getChatHeaderEdit()
		{
			return (ChatMessageHeaderEdit) getHeader();

		} // getChatHeaderEdit

	} // class BasicChatMessageEdit

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ChatMessageHeaderEdit implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseChatMessageHeaderEdit extends BaseMessageHeaderEdit implements ChatMessageHeaderEdit
	{
		/**
		 * Construct.
		 * 
		 * @param id
		 *        The unique (within the channel) message id.
		 * @param from
		 *        The User who sent the message to the channel.
		 * @param attachments
		 *        The message header attachments, a vector of Reference objects.
		 */
		public BaseChatMessageHeaderEdit(Message msg, String id)
		{
			super(msg, id);

		} // BaseChatMessageHeaderEdit

		/**
		 * Construct, from an already existing XML DOM element.
		 * 
		 * @param el
		 *        The header in XML in a DOM element.
		 */
		public BaseChatMessageHeaderEdit(Message msg, Element el)
		{
			super(msg, el);

		} // BaseChatMessageHeaderEdit

		/**
		 * Construct as a copy of another header.
		 * 
		 * @param other
		 *        The other message header to copy.
		 */
		public BaseChatMessageHeaderEdit(Message msg, MessageHeader other)
		{
			super(msg, other);
		}
	}
}
