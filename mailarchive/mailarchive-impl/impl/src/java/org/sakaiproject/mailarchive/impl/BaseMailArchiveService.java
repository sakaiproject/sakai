/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.mailarchive.impl;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.mail.internet.MimeUtility;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.entity.api.ContextObserver;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.mailarchive.api.MailArchiveChannel;
import org.sakaiproject.mailarchive.api.MailArchiveChannelEdit;
import org.sakaiproject.mailarchive.api.MailArchiveMessage;
import org.sakaiproject.mailarchive.api.MailArchiveMessageEdit;
import org.sakaiproject.mailarchive.api.MailArchiveMessageHeader;
import org.sakaiproject.mailarchive.api.MailArchiveMessageHeaderEdit;
import org.sakaiproject.mailarchive.api.MailArchiveService;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.message.api.MessageChannel;
import org.sakaiproject.message.api.MessageChannelEdit;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.message.api.MessageHeaderEdit;
import org.sakaiproject.message.util.BaseMessage;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * BaseMailArchiveService extends the BaseMessage for the specifics of MailArchive.
 * </p>
 */
@Slf4j
public abstract class BaseMailArchiveService extends BaseMessage implements MailArchiveService, ContextObserver
{
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Constructors, Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	@Setter protected AliasService aliasService;
	@Setter protected FunctionManager functionManager;
	@Setter protected NotificationService notificationService;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			super.init();

			// register a transient notification for mail
			NotificationEdit edit = notificationService.addTransientNotification();

			// set function
			edit.setFunction(eventId(SECURE_ADD));

			// set the filter to any email resource (see messageReference())
			edit.setResourceFilter(getAccessPoint(true) + Entity.SEPARATOR + REF_TYPE_MESSAGE);

			// set the action
			edit.setAction(new SiteEmailNotificationMail());

			// register functions
			functionManager.registerFunction(eventId(SECURE_READ), true);
			functionManager.registerFunction(eventId(SECURE_ADD), true);
			functionManager.registerFunction(eventId(SECURE_REMOVE_ANY), true);

			// entity producer registration
			entityManager.registerEntityProducer(this, REFERENCE_ROOT);

			log.info("init()");
		}
		catch (Throwable t)
		{
			log.warn("init(): ", t);
		}

	} // init

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
		return new BaseMailArchiveChannelEdit(ref);
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
		return new BaseMailArchiveChannelEdit(element);
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
		return new BaseMailArchiveChannelEdit((MessageChannel) other);
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
		return new BaseMailArchiveMessageEdit((MessageChannel) container, id);
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
		return new BaseMailArchiveMessageEdit((MessageChannel) container, element);
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
		return new BaseMailArchiveMessageEdit((MessageChannel) container, (Message) other);
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
		BaseMailArchiveChannelEdit rv = new BaseMailArchiveChannelEdit(ref);
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
		BaseMailArchiveChannelEdit rv = new BaseMailArchiveChannelEdit(element);
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
		BaseMailArchiveChannelEdit rv = new BaseMailArchiveChannelEdit((MessageChannel) other);
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
		BaseMailArchiveMessageEdit rv = new BaseMailArchiveMessageEdit((MessageChannel) container, id);
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
		BaseMailArchiveMessageEdit rv = new BaseMailArchiveMessageEdit((MessageChannel) container, element);
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
		BaseMailArchiveMessageEdit rv = new BaseMailArchiveMessageEdit((MessageChannel) container, (Message) other);
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
		Object[] rv = new Object[6];
		rv[0] = ((Message) r).getHeader().getDate();
		rv[1] = ((Message) r).getHeader().getFrom().getId();
		rv[2] = "0";
		rv[3] = r.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW) == null ? "0" : "1";
		rv[4] = ((MailArchiveMessage) r).getMailArchiveHeader().getSubject();
		rv[5] = ((Message) r).getBody();
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
		return MailArchiveService.class.getName();
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
		return new BaseMailArchiveMessageHeaderEdit(msg, id);

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
		return new BaseMailArchiveMessageHeaderEdit(msg, el);

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
		return new BaseMailArchiveMessageHeaderEdit(msg, other);

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
		return SECURE_MAIL_ROOT + secure;

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
			String[] parts = StringUtils.split(reference, Entity.SEPARATOR);

			String id = null;
			String subType = null;
			String context = null;
			String container = null;

			// the first part will be "mailarchive", then next the service, the third will be "msg" or "channel"
			if (parts.length > 1)
			{
				subType = parts[1];
				if (REF_TYPE_CHANNEL.equals(subType))
				{
					// next is the context id
					if (parts.length > 2)
					{
						context = parts[2];

						// next is the channel id
						if (parts.length > 3)
						{
							id = parts[3];
						}
					}
				}
				else if (REF_TYPE_MESSAGE.equals(subType))
				{
					// next three parts are context, channel (container) and mesage id
					if (parts.length > 4)
					{
						context = parts[2];
						container = parts[3];
						id = parts[4];
					}
				}
				else
					log.warn("parse(): unknown message subtype: " + subType + " in ref: " + reference);
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
		if (toolPlacement) enableMailbox(context);
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextUpdated(String context, boolean toolPlacement)
	{
		if (toolPlacement) enableMailbox(context);
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextDeleted(String context, boolean toolPlacement)
	{
		disableMailbox(context);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] myToolIds()
	{
		String[] toolIds = { "sakai.mailbox" };
		return toolIds;
	}

	/**
	 * Setup the mailbox for an active site.
	 * 
	 * @param siteId
	 *        The site id.
	 */
	protected void enableMailbox(String siteId)
	{
		// form the email channel name
		String channelRef = channelReference(siteId, siteService.MAIN_CONTAINER);

		// see if there's a channel
		MessageChannel channel = null;
		try
		{
			channel = getChannel(channelRef);
		}
		catch (IdUnusedException e)
		{
		}
		catch (PermissionException e)
		{
		}

		// if it exists, make sure it's enabled
		if (channel != null)
		{
			if (channel.getProperties().getProperty(ResourceProperties.PROP_CHANNEL_ENABLED) == null)
			{
				try
				{
					MessageChannelEdit edit = (MessageChannelEdit) editChannel(channelRef);
					edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_CHANNEL_ENABLED, "true");
					commitChannel(edit);
					
				}
				catch (IdUnusedException ignore)
				{
				}
				catch (PermissionException ignore)
				{
				}
				catch (InUseException ignore)
				{
				}
			}
		}
		// otherwise create it
		else
		{
			try
			{
				// create a channel and mark it as enabled
				MessageChannelEdit edit = addMailArchiveChannel(channelRef);
				edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_CHANNEL_ENABLED, "true");
				commitChannel(edit);
				
			}
			catch (IdUsedException e)
			{
			}
			catch (IdInvalidException e)
			{
			}
			catch (PermissionException e)
			{
			}
		}
	}

	/**
	 * Set a site's mailbox to inactive - it remains in existance, just disabled
	 * 
	 * @param siteId
	 *        The site id.
	 */
	protected void disableMailbox(String siteId)
	{
		// form the email channel name
		String channelRef = channelReference(siteId, siteService.MAIN_CONTAINER);

		// see if there's a channel
		MessageChannel channel = null;
		try
		{
			channel = getChannel(channelRef);
		}
		catch (IdUnusedException e)
		{
		}
		catch (PermissionException e)
		{
		}

		// if it exists, make sure it's disabled
		if (channel != null)
		{
			if (channel.getProperties().getProperty(ResourceProperties.PROP_CHANNEL_ENABLED) != null)
			{
				try
				{
					MessageChannelEdit edit = (MessageChannelEdit) editChannel(channelRef);
					edit.getPropertiesEdit().removeProperty(ResourceProperties.PROP_CHANNEL_ENABLED);
					commitChannel(edit);
					channel = edit;
				}
				catch (IdUnusedException ignore)
				{
				}
				catch (PermissionException ignore)
				{
				}
				catch (InUseException ignore)
				{
				}
			}
		}

		// remove any alias
		try
		{
			aliasService.removeTargetAliases(channelRef);
		}
		catch (PermissionException e)
		{
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * MailArchiveService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Return a specific mail message channel.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return the MailArchiveChannel that has the specified name.
	 * @exception IdUnusedException
	 *            If this name is not defined for a mail message channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to the channel.
	 */
	public MailArchiveChannel getMailArchiveChannel(String ref) throws IdUnusedException, PermissionException
	{
		return (MailArchiveChannel) getChannel(ref);

	} // getMailArchiveChannel

	/**
	 * Add a new mail message channel.
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
	public MailArchiveChannelEdit addMailArchiveChannel(String ref) throws IdUsedException, IdInvalidException, PermissionException
	{
		return (MailArchiveChannelEdit) addChannel(ref);

	} // addMailArchiveChannel

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ResourceService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		return "email";
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * MailArchiveChannel implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseMailArchiveChannelEdit extends BaseMessageChannelEdit implements MailArchiveChannelEdit
	{
		/**
		 * Construct with an id.
		 * 
		 * @param ref
		 *        The channel reference.
		 */
		public BaseMailArchiveChannelEdit(String ref)
		{
			super(ref);

		} // BaseMailArchiveChannelEdit

		/**
		 * Construct as a copy of another message.
		 * 
		 * @param other
		 *        The other message to copy.
		 */
		public BaseMailArchiveChannelEdit(MessageChannel other)
		{
			super(other);

		} // BaseMailArchiveChannelEdit

		/**
		 * Construct from a channel (and possibly messages) already defined in XML in a DOM tree. The Channel is added to storage.
		 * 
		 * @param el
		 *        The XML DOM element defining the channel.
		 */
		public BaseMailArchiveChannelEdit(Element el)
		{
			super(el);

		} // BaseMailArchiveChannelEdit

		/**
		 * Return a specific mail message channel message, as specified by message name.
		 * 
		 * @param messageId
		 *        The id of the message to get.
		 * @return the MailArchiveMessage that has the specified id.
		 * @exception IdUnusedException
		 *            If this name is not a defined message in this mail message channel.
		 * @exception PermissionException
		 *            If the user does not have any permissions to read the message.
		 */
		public MailArchiveMessage getMailArchiveMessage(String messageId) throws IdUnusedException, PermissionException
		{
			MailArchiveMessage msg = (MailArchiveMessage) getMessage(messageId);

			return msg;

		} // getMailArchiveMessage

		/**
		 * A (MailArchiveMessageEdit) cover for editMessage. Return a specific channel message, as specified by message name, locked for update. Must commitEdit() to make official, or cancelEdit() when done!
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
		public MailArchiveMessageEdit editMailArchiveMessage(String messageId) throws IdUnusedException, PermissionException,
				InUseException
		{
			return (MailArchiveMessageEdit) editMessage(messageId);

		} // editMailArchiveMessage

		/**
		 * A (MailArchiveMessageEdit) cover for addMessage. Add a new message to this channel. Must commitEdit() to make official, or cancelEdit() when done!
		 * 
		 * @return The newly added message, locked for update.
		 * @exception PermissionException
		 *            If the user does not have write permission to the channel.
		 */
		public MailArchiveMessageEdit addMailArchiveMessage() throws PermissionException
		{
			return (MailArchiveMessageEdit) addMessage();

		} // addMailArchiveMessage

		/**
		 * a (MailArchiveMessage) cover for addMessage to add a new message to this channel.
		 * 
		 * @param subject
		 *        The message header subject.
		 * @param fromAddress
		 *        The mail from: address from the message.
		 * @param dateSent
		 *        The date: sent from the message.
		 * @param mailHeaders
		 *        The full set of mail headers from the message.
		 * @param attachments
		 *        The message header attachments, a vector of Reference objects.
		 * @param body
		 *        The message body.- body[0] is plain/text; body[1] is html/text
		 * @return The newly added message.
		 * @exception PermissionException
		 *            If the user does not have write permission to the channel.
		 */
		public MailArchiveMessage addMailArchiveMessage(String subject, String fromAddress, Instant dateSent, List mailHeaders,
				List attachments, String[] body) throws PermissionException
		{
			final String cleanedHtml = formattedText.processFormattedText(body[1], null, null);
			final String cleanedText = formattedText.encodeUnicode(body[0]);
			final String cleanedSubject = formattedText.processFormattedText(subject, null, null);

			MailArchiveMessageEdit edit = (MailArchiveMessageEdit) addMessage();
			MailArchiveMessageHeaderEdit archiveHeaders = edit.getMailArchiveHeaderEdit();
			edit.setBody(cleanedText);
			edit.setHtmlBody(cleanedHtml);
			archiveHeaders.replaceAttachments(attachments);
			archiveHeaders.setSubject(cleanedSubject);
			archiveHeaders.setFromAddress(fromAddress);
			archiveHeaders.setInstantSent(dateSent);
			archiveHeaders.setMailHeaders(mailHeaders);

			// lets make sure that folks who have signed up for email get it
			//SAK 12800
			if (getSendToList()) {
				commitMessage(edit, NotificationService.NOTI_OPTIONAL);
			} else {
				commitMessage(edit, NotificationService.NOTI_NONE);
			}

			return edit;

		} // addMailArchiveMessage

		/** @return true if the channel enabled, false if not. */
		public boolean getEnabled()
		{
			boolean enabled = false;
			try
			{
				enabled = getProperties().getBooleanProperty(ResourceProperties.PROP_CHANNEL_ENABLED);
			}
			catch (Exception ignore)
			{
			}

			return enabled;

		} // getEnabled

		/** @return true if the channel is open to messages from outside the membership, false if not. */
		public boolean getOpen()
		{
			boolean open = false;
			try
			{
				open = getProperties().getBooleanProperty(PROP_MAIL_CHANNEL_OPEN);
			}
			catch (Exception ignore)
			{
			}

			return open;

		} // getOpen

		/**
		 * Set the enabled status of the channe. Disabled channels will not recieve email.
		 * 
		 * @param setting
		 *        The new setting.
		 */
		public void setEnabled(boolean setting)
		{
			if (setting)
			{
				getPropertiesEdit().addProperty(ResourceProperties.PROP_CHANNEL_ENABLED, "true");
			}
			else
			{
				getPropertiesEdit().removeProperty(ResourceProperties.PROP_CHANNEL_ENABLED);
			}

		} // setEnabled

		/**
		 * Set the open status of the channe. Open channels will recieve email from anyone - otherwise messages will be accepted only from users (based on the main from email address) with add permission.
		 * 
		 * @param setting
		 *        The new setting.
		 */
		public void setOpen(boolean setting)
		{
			if (setting)
			{
				getPropertiesEdit().addProperty(PROP_MAIL_CHANNEL_OPEN, "true");
			}
			else
			{
				getPropertiesEdit().removeProperty(PROP_MAIL_CHANNEL_OPEN);
			}
		}

		/**
		 * check permissions for addMessage() for the given user.
		 * 
		 * @param user
		 *        The user.
		 * @return true if the specified user is allowed to addMessage(...), false if not.
		 */
		public boolean allowAddMessage(User user)
		{
			return securityService.unlock(user, eventId(SECURE_ADD), getReference());
		}

		/*
		 * (non-Javadoc)
		 * @see org.sakaiproject.mailarchive.api.MailArchiveChannelEdit#setReplyToList(boolean)
		 */
		public void setReplyToList(boolean replyToList)
		{
			if (replyToList)
			{
				getPropertiesEdit().addProperty(PROP_MAIL_CHANNEL_REPLY_LIST, "true");
			}
			else
			{
				getPropertiesEdit().removeProperty(PROP_MAIL_CHANNEL_REPLY_LIST);
			}			
		}

		/*
		 * (non-Javadoc)
		 * @see org.sakaiproject.mailarchive.api.MailArchiveChannel#getReplyToList()
		 */
		public boolean getReplyToList()
		{
			boolean open = false;
			try
			{
				open = getProperties().getBooleanProperty(PROP_MAIL_CHANNEL_REPLY_LIST);
			}
			catch (Exception ignore)
			{
			}

			return open;
		}

		/*
		 * (non-Javadoc)
		 * @see org.sakaiproject.mailarchive.api.MailArchiveChannelEdit#setSendToList(boolean)
		 */
		public void setSendToList(boolean sendToList)
		{
			//If not set it defaults to true
			if (sendToList==false)
			{
				getPropertiesEdit().addProperty(PROP_MAIL_CHANNEL_SENDTO_LIST, "false");
			}
			else
			{
				getPropertiesEdit().removeProperty(PROP_MAIL_CHANNEL_SENDTO_LIST);
			}			
		}

		/*
		 * (non-Javadoc)
		 * @see org.sakaiproject.mailarchive.api.MailArchiveChannel#getSendToList()
		 */
		public boolean getSendToList()
		{
			boolean open = true;
			//TODO:This shouldn't try/catch for nothing
			try
			{
				open = getProperties().getBooleanProperty(PROP_MAIL_CHANNEL_SENDTO_LIST);
			}
			catch (Exception ignore)
			{
			}

			return open;
		}

	} // class BaseMailArchiveChannelEdit

	/**********************************************************************************************************************************************************************************************************************************************************
	 * MailArchiveMessage implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseMailArchiveMessageEdit extends BaseMessageEdit implements MailArchiveMessageEdit
	{
		protected String m_html_body = null;
		
		/**
		 * Construct.
		 * 
		 * @param channel
		 *        The channel in which this message lives.
		 * @param id
		 *        The message id.
		 */
		public BaseMailArchiveMessageEdit(MessageChannel channel, String id)
		{
			super(channel, id);
			m_html_body = "";

		} // BaseMailArchiveMessageEdit

		/**
		 * Construct as a copy of another message.
		 * 
		 * @param other
		 *        The other message to copy.
		 */
		public BaseMailArchiveMessageEdit(MessageChannel channel, Message other)
		{
			super(channel, other);

		} // BaseMailArchiveMessageEdit

		/**
		 * Construct from an existing definition, in xml.
		 * 
		 * @param channel
		 *        The channel in which this message lives.
		 * @param el
		 *        The message in XML in a DOM element.
		 */
		public BaseMailArchiveMessageEdit(MessageChannel channel, Element el)
		{
			this(channel, "");

			m_html_body = Xml.decodeAttribute(el, "body-html");
			m_body      = Xml.decodeAttribute(el, "body");

			// the children (header, body)
			NodeList children = el.getChildNodes();
			final int length = children.getLength();
			for (int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE)
				{
					Element element = (Element) child;

					// look for a header
					if (element.getTagName().equals("header"))
					{
						// re-create a header
						m_header = newMessageHeader(this, element);
					}

					// or look for a body (old style of encoding)
					else if (element.getTagName().equals("body"))
					{
						if ((element.getChildNodes() != null) && (element.getChildNodes().item(0) != null))
						{
							// convert from plaintext messages to formatted text messages
							m_body = element.getChildNodes().item(0).getNodeValue();
							if (m_body != null) m_body = formattedText.convertPlaintextToFormattedText(m_body);
						}
						if (m_body == null)
						{
							m_body = "";
						}
					}

					// or look for properties
					else if (element.getTagName().equals("properties"))
					{
						// re-create properties
						m_properties = new BaseResourcePropertiesEdit(element);
					}
				}
			}

		} // BaseMailArchiveMessageEdit

		/**
		 * Access the mail message message header.
		 * 
		 * @return The mail message message header.
		 */
		public MailArchiveMessageHeader getMailArchiveHeader()
		{
			return (MailArchiveMessageHeader) getHeader();

		} // getMailArchiveHeader

		/**
		 * Access the mail message message header.
		 * 
		 * @return The mail message message header.
		 */
		public MailArchiveMessageHeaderEdit getMailArchiveHeaderEdit()
		{
			return (MailArchiveMessageHeaderEdit) getHeader();

		} // getMailArchiveHeaderEdit

		/**
		 * Replace the html body, as a string.
		 * 
		 * @param body
		 *        The html-encoded body, as a string.
		 */
		public void setHtmlBody(String body)
		{
			m_html_body = body;
		} // setHtmlBody

		/**
		 * Get the html body, as a string.
		 * 
		 * @return The html-encoded body, as a string.
		 */
		public String getHtmlBody()
		{
			return formattedText.getHtmlBody(m_html_body);
		} // getHtmlBody

		/**
		 * Get the formatted body (either html or plain-text converted to html), as a string.
		 * 
		 * @return The formatted body as a string.
		 */
		public String getFormattedBody()
		{
			if ( getHtmlBody() != null && getHtmlBody().length() > 0 )
				return getHtmlBody();
			else 
				return formattedText.encodeUrlsAsHtml( formattedText.convertPlaintextToFormattedText( formattedText.decodeNumericCharacterReferences(m_body)) );
				
		} // getHtmlBody

		/**
		 * Take all values from this object.
		 * 
		 * @param other
		 *        The other object to take values from.
		 */
		protected void setAll(Message other)
		{
			super.setAll( other );
			if ( other instanceof BaseMailArchiveMessageEdit )
				m_html_body = ((BaseMailArchiveMessageEdit)other).getHtmlBody();
		}

		/**
		 * Serialize the resource into XML, adding an element to the doc under the top of the stack element.
		 * 
		 * @param doc
		 *        The DOM doc to contain the XML (or null for a string return).
		 * @param stack
		 *        The DOM elements, the top of which is the containing element of the new "resource" element.
		 * @return The newly added element.
		 */
		public Element toXml(Document doc, Stack stack)
		{
			Element message = super.toXml(doc, stack);

			// Over-ride super.toXml() definition of body and body-html attributes
			
			// store the html body in attribute
			Xml.encodeAttribute(message, "body-html", getHtmlBody() );

			// store the plain-text body in attribute
			Xml.encodeAttribute(message, "body", getBody() );

			return message;
		}
	} // class BasicMailArchiveMessageEdit

	/**********************************************************************************************************************************************************************************************************************************************************
	 * MailArchiveMessageHeaderEdit implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseMailArchiveMessageHeaderEdit extends BaseMessageHeaderEdit implements MailArchiveMessageHeaderEdit
	{
		/** The subject for the mail message. */
		protected String m_subject = null;

		/** The from: address for the message. */
		protected String m_fromAddress = null;

		/** The date: sent for the message. */
		protected Time m_dateSent = null;

		/** The entire set of mail headers. */
		protected List m_mailHeaders = new Vector();

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
		public BaseMailArchiveMessageHeaderEdit(Message msg, String id)
		{
			super(msg, id);

		} // BaseMailArchiveMessageHeaderEdit

		/**
		 * Construct, from an already existing XML DOM element.
		 * 
		 * @param el
		 *        The header in XML in a DOM element.
		 */
		public BaseMailArchiveMessageHeaderEdit(Message msg, Element el)
		{
			super(msg, el);

			// now extract the subject, from address, date sent
			m_subject = el.getAttribute("subject");
			m_fromAddress = el.getAttribute("mail-from");
			m_dateSent = timeService.newTimeGmt(el.getAttribute("mail-date"));

			// mail headers
			NodeList children = el.getChildNodes();
			final int length = children.getLength();
			for (int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element element = (Element) child;

				// look for a header
				if (!element.getTagName().equals("mail-header")) continue;

				m_mailHeaders.add(element.getAttribute("value"));
			}

		} // BaseMailArchiveMessageHeaderEdit

		/**
		 * Construct as a copy of another header.
		 * 
		 * @param other
		 *        The other message header to copy.
		 */
		public BaseMailArchiveMessageHeaderEdit(Message msg, MessageHeader other)
		{
			super(msg, other);

			m_subject = ((MailArchiveMessageHeader) other).getSubject();
			m_fromAddress = ((MailArchiveMessageHeader) other).getFromAddress();
			m_dateSent = ((MailArchiveMessageHeader) other).getDateSent();
			m_mailHeaders.addAll(((MailArchiveMessageHeader) other).getMailHeaders());

		} // BaseMailArchiveMessageHeaderEdit

		/**
		 * Access the subject of the mail message.
		 * 
		 * @return The subject of the mail message.
		 */
		@Override
		public String getSubject()
		{
			return ((m_subject == null) ? "" : m_subject);

		} // getSubject

		/**
		 * Set the subject of the mail message.
		 * 
		 * @param subject
		 *        The subject of the mail message.
		 */
		public void setSubject(String subject)
		{
			m_subject = subject;

		} // setSubject

		/**
		 * Access the from: address of the message.
		 * 
		 * @return The from: address of the message.
		 */
		public String getFromAddress()
		{
			String fromAddress = ((m_fromAddress == null) ? "" : m_fromAddress);
			
			try
			{
				fromAddress = MimeUtility.decodeText(fromAddress);
			}
			catch (UnsupportedEncodingException e)
			{
				// if unable to decode RFC address, just return address as is
				log.debug(this+".getFromAddress "+e.toString());
			}
			
			return fromAddress;

		} // getFromAddress

		/**
		 * Set the the from: address of the message.
		 * 
		 * @param from
		 *        The from: address of the message.
		 */
		public void setFromAddress(String from)
		{
			m_fromAddress = from;

		} // setFromAddress

		/**
		 * Access the date: sent of the message.
		 * 
		 * @return The date: sent of the message.
		 */
		@Override
		public Time getDateSent()
		{
			return ((m_dateSent == null) ? this.getDate() : m_dateSent);

		} // getDateSent

		@Override
		public void setInstantSent(Instant sent) {
			m_dateSent = timeService.newTime(sent.toEpochMilli());
		}


		@Override
		public Instant getInstantSent() {
			return Instant.ofEpochMilli(m_dateSent.getTime());
		}
		/**
		 * Access the entire set of mail headers the message.
		 * 
		 * @return The entire set of mail headers of the message (List of String).
		 */
		public List getMailHeaders()
		{
			return m_mailHeaders;

		} // getMailHeaders

		/**
		 * Set the entire set of mail headers of the message.
		 * 
		 * @param headers
		 *        The the entire set of mail headers of the message.
		 */
		public void setMailHeaders(List headers)
		{
			m_mailHeaders.clear();
			m_mailHeaders.addAll(headers);

		} // setMailHeaders

		/**
		 * Serialize the resource into XML, adding an element to the doc under the top of the stack element.
		 * 
		 * @param doc
		 *        The DOM doc to contain the XML (or null for a string return).
		 * @param stack
		 *        The DOM elements, the top of which is the containing element of the new "resource" element.
		 * @return The newly added element.
		 */
		public Element toXml(Document doc, Stack stack)
		{
			// get the basic work done
			Element header = super.toXml(doc, stack);

			// add draft, subject
			header.setAttribute("subject", getSubject());
			header.setAttribute("mail-from", getFromAddress());
			header.setAttribute("mail-date", getDateSent().toString());
			for (int i = 0; i < m_mailHeaders.size(); i++)
			{
				Element mailHeader = doc.createElement("mail-header");
				header.appendChild(mailHeader);
				mailHeader.setAttribute("value", (String) m_mailHeaders.get(i));
			}

			return header;

		} // toXml


	} // BaseMailArchiveMessageHeader
	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntitySummary#summarizableToolIds()
	 */
	public String[] summarizableToolIds()
	{
		return new String[] {
				"sakai.mailarchive"
		};
	}

} // BaseMailArchiveService
