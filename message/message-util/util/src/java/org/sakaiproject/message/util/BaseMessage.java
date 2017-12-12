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

package org.sakaiproject.message.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.*;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.*;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.message.api.*;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.*;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.*;

/**
 * BaseMessage is...
 */
@Slf4j
public abstract class BaseMessage implements MessageService, DoubleStorageUser
{
	/** A Storage object for persistent storage. */
	protected Storage m_storage = null;

	private static final String CHANNEL_PROP = "channel";
	private static final String PROPERTIES = "properties";
	private static final String PROPERTY = "property";
	private static final String SYNOPTIC_TOOL = "synoptic_tool";
	private static final String NAME = "name";
	private static final String VALUE = "value";
	private static final String STATE_UPDATE = "update";
	
	/** added to allow for scheduled notifications */
	private static final String SCHED_INV_UUID = "schInvUuid";
	//private static final String SCHINV_DELETE_EVENT = "schInv.delete";
	

	/**
	 * Access this service from the inner classes.
	 */
	protected BaseMessage service()
	{
		return this;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Constructors, Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Dependency: MemoryService. */
	protected MemoryService m_memoryService = null;

	/**
	 * Dependency: MemoryService.
	 *
	 * @param service
	 *        The MemoryService.
	 */
	public void setMemoryService(MemoryService service)
	{
		m_memoryService = service;
	}

	/** Dependency: ServerConfigurationService. */
	protected ServerConfigurationService m_serverConfigurationService = null;

	/**
	 * Dependency: ServerConfigurationService.
	 * 
	 * @param service
	 *        The ServerConfigurationService.
	 */
	public void setServerConfigurationService(ServerConfigurationService service)
	{
		m_serverConfigurationService = service;
	}

	/** Dependency: SessionManager. */
	protected SessionManager m_sessionManager = null;

	/**
	 * Dependency: SessionManager.
	 * 
	 * @param service
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager service)
	{
		m_sessionManager = service;
	}

	/** Dependency: AuthzGroupService. */
	protected AuthzGroupService m_authzGroupService = null;

	/**
	 * Dependency: AuthzGroupService.
	 * 
	 * @param service
	 *        The AuthzGroupService.
	 */
	public void setAuthzGroupService(AuthzGroupService service)
	{
		m_authzGroupService = service;
	}

	/** Dependency: SecurityService. */
	protected SecurityService m_securityService = null;

	/**
	 * Dependency: SecurityService.
	 * 
	 * @param service
	 *        The SecurityService.
	 */
	public void setSecurityService(SecurityService service)
	{
		m_securityService = service;
	}

	/** Dependency: TimeService. */
	protected TimeService m_timeService = null;

	/**
	 * Dependency: TimeService.
	 * 
	 * @param service
	 *        The TimeService.
	 */
	public void setTimeService(TimeService service)
	{
		m_timeService = service;
	}

	/** Dependency: EventTrackingService. */
	protected EventTrackingService m_eventTrackingService = null;

	/**
	 * Dependency: EventTrackingService.
	 * 
	 * @param service
	 *        The EventTrackingService.
	 */
	public void setEventTrackingService(EventTrackingService service)
	{
		m_eventTrackingService = service;
	}

	/** Dependency: IdManager. */
	protected IdManager m_idManager = null;

	/**
	 * Dependency: IdManager.
	 * 
	 * @param service
	 *        The IdManager.
	 */
	public void setIdManager(IdManager service)
	{
		m_idManager = service;
	}

	/** Dependency: SiteService. */
	protected SiteService m_siteService = null;

	/**
	 * Dependency: SiteService.
	 * 
	 * @param service
	 *        The SiteService.
	 */
	public void setSiteService(SiteService service)
	{
		m_siteService = service;
	}

	/** Dependency: UserDirectoryService. */
	protected UserDirectoryService m_userDirectoryService = null;

	/**
	 * Dependency: UserDirectoryService.
	 * 
	 * @param service
	 *        The UserDirectoryService.
	 */
	public void setUserDirectoryService(UserDirectoryService service)
	{
		m_userDirectoryService = service;
	}

	/** Dependency: ThreadLocalManager. */
	protected ThreadLocalManager m_threadLocalManager = null;

	/**
	 * Dependency: ThreadLocalManager.
	 * 
	 * @param service
	 *        The ThreadLocalManager.
	 */
	public void setThreadLocalManager(ThreadLocalManager service)
	{
		m_threadLocalManager = service;
	}

	/**
	 * Configuration: set the locks-in-db
	 * 
	 * @param path
	 *        The storage path.
     * @deprecated 7 April 2014 - this should be removed in sakai 11
	 */
	public void setCaching(String value) {} // intentionally blank - remove this later

	/** Dependency: EntityManager. */
	protected EntityManager m_entityManager = null;

	/**
	 * Dependency: EntityManager.
	 * 
	 * @param service
	 *        The EntityManager.
	 */
	public void setEntityManager(EntityManager service)
	{
		m_entityManager = service;
	}

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
			// construct a storage helper and read
			m_storage = newStorage();
			m_storage.open();

			log.info("init()");
		}
		catch (Throwable t)
		{
			log.warn("init(): "+t, t);
		}

		// entity producer registration in the extension services

		// Functions are registered in the extension services

	} // init

	/**
	 * Destroy
	 */
	public void destroy()
	{
		m_storage.close();
		m_storage = null;

		log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Abstractions, etc.
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Report the Service API name being implemented.
	 */
	protected abstract String serviceName();

	/**
	 * Construct a Storage object.
	 * 
	 * @return The new storage object.
	 */
	protected abstract Storage newStorage();

	/**
	 * Construct a new message header from XML in a DOM element.
	 * 
	 * @param msg
	 *        The message to own this header.
	 * @param id
	 *        The message Id.
	 * @return The new message header.
	 */
	protected abstract MessageHeaderEdit newMessageHeader(Message msg, String id);

	/**
	 * Construct a new message header from XML in a DOM element.
	 * 
	 * @param msg
	 *        The message to own this header.
	 * @param el
	 *        The XML DOM element that has the header information.
	 * @return The new message header.
	 */
	protected abstract MessageHeaderEdit newMessageHeader(Message msg, Element el);

	/**
	 * Construct a new message header as a copy of another.
	 * 
	 * @param msg
	 *        The message to own this header.
	 * @param other
	 *        The other header to copy.
	 * @return The new message header.
	 */
	protected abstract MessageHeaderEdit newMessageHeader(Message msg, MessageHeader other);

	/**
	 * Form a tracking event string based on a security function string.
	 * 
	 * @param secure
	 *        The security function string.
	 * @return The event tracking string.
	 */
	protected abstract String eventId(String secure);

	/**
	 * Return the reference rooot for use in resource references and urls.
	 * 
	 * @return The reference rooot for use in resource references and urls.
	 */
	protected abstract String getReferenceRoot();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * MessageService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Access the partial URL that forms the root of resource URLs.
	 * 
	 * @param relative
	 *        if true, form within the access path only (i.e. starting with /msg)
	 * @return the partial URL that forms the root of resource URLs.
	 */
	protected String getAccessPoint(boolean relative)
	{
		return (relative ? "" : m_serverConfigurationService.getAccessUrl()) + getReferenceRoot();

	} // getAccessPoint

	/**
	 * Access the internal reference which can be used to access the channel from within the system.
	 * 
	 * @param context
	 *        The context.
	 * @param id
	 *        The channel id.
	 * @return The the internal reference which can be used to access the channel from within the system.
	 */
	public String channelReference(String context, String id)
	{
		return getAccessPoint(true) + Entity.SEPARATOR + REF_TYPE_CHANNEL + Entity.SEPARATOR + context + Entity.SEPARATOR + id;

	} // channelReference

	/**
	 * Access the internal reference which can be used to access the message from within the system.
	 * 
	 * @param context
	 *        The context.
	 * @param channelId
	 *        The channel id.
	 * @param id
	 *        The message id.
	 * @return The the internal reference which can be used to access the message from within the system.
	 */
	public String messageReference(String context, String channelId, String id)
	{
		return getAccessPoint(true) + Entity.SEPARATOR + REF_TYPE_MESSAGE + Entity.SEPARATOR + context + Entity.SEPARATOR
				+ channelId + Entity.SEPARATOR + id;

	} // messageReference

	/**
	 * Access the internal reference which can be used to access the message from within the system.
	 * 
	 * @param channelRef
	 *        The channel reference.
	 * @param id
	 *        The message id.
	 * @return The the internal reference which can be used to access the message from within the system.
	 */
	public String messageReference(String channelRef, String id)
	{
		StringBuilder buf = new StringBuilder();

		// start with the channel ref
		buf.append(channelRef);

		// swap channel for msg
		int pos = buf.indexOf(REF_TYPE_CHANNEL);
		buf.replace(pos, pos + REF_TYPE_CHANNEL.length(), REF_TYPE_MESSAGE);

		// add the id
		buf.append(Entity.SEPARATOR);
		buf.append(id);

		return buf.toString();
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @return true if allowd, false if not
	 */
	protected boolean unlockCheck(String lock, String resource)
	{
		if (!m_securityService.unlock(eventId(lock), resource))
		{
			return false;
		}

		return true;

	} // unlockCheck

	/**
	 * Check security permission, for either of two locks/
	 * 
	 * @param lock1
	 *        The lock id string.
	 * @param lock2
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @return true if either allowed, false if not
	 */
	protected boolean unlockCheck2(String lock1, String lock2, String resource)
	{
		// check the first lock
		if (m_securityService.unlock(eventId(lock1), resource)) return true;

		// if the second is different, check that
		if ((!lock1.equals(lock2)) && (m_securityService.unlock(eventId(lock2), resource))) return true;

		return false;

	} // unlockCheck2

	/**
	 * Check security permission, for any of three locks/
	 * 
	 * @param lock1
	 *        The lock id string.
	 * @param lock2
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @return true if either allowed, false if not
	 */
	protected boolean unlockCheck3(String lock1, String lock2, String lock3, String resource)
	{
		// check the first lock
		if (m_securityService.unlock(eventId(lock1), resource)) return true;

		// if the second is different, check that
		if ((!lock1.equals(lock2)) && (m_securityService.unlock(eventId(lock2), resource))) return true;

		// if the third is different, check that
		if ((!lock1.equals(lock3)) && (!lock2.equals(lock3)) && (m_securityService.unlock(eventId(lock3), resource))) return true;

		return false;

	} // unlockCheck3

	/**
	 * Check security permission.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @exception PermissionException
	 *            Thrown if the user does not have access
	 */
	protected void unlock(String lock, String resource) throws PermissionException
	{
		if (!unlockCheck(lock, resource))
		{
			throw new PermissionException(m_sessionManager.getCurrentSessionUserId(), eventId(lock), resource);
		}

	} // unlock

	/**
	 * Check security permission.
	 * 
	 * @param lock1
	 *        The lock id string.
	 * @param lock2
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @exception PermissionException
	 *            Thrown if the user does not have access to either.
	 */
	protected void unlock2(String lock1, String lock2, String resource) throws PermissionException
	{
		if (!unlockCheck2(lock1, lock2, resource))
		{
			throw new PermissionException(m_sessionManager.getCurrentSessionUserId(), eventId(lock1) + "|" + eventId(lock2), resource);
		}

	} // unlock2

	/**
	 * Check security permission.
	 * 
	 * @param lock1
	 *        The lock id string.
	 * @param lock2
	 *        The lock id string.
	 * @param lock3
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @exception PermissionException
	 *            Thrown if the user does not have access to either.
	 */
	protected void unlock3(String lock1, String lock2, String lock3, String resource) throws PermissionException
	{
		if (!unlockCheck3(lock1, lock2, lock3, resource))
		{
			throw new PermissionException(m_sessionManager.getCurrentSessionUserId(), eventId(lock1) + "|" + eventId(lock2) + "|"
					+ eventId(lock3), resource);
		}

	} // unlock3

	/**
	 * Return a list of all the defined channels.
	 *
	 * @return a list of MessageChannel (or extension) objects (may be empty).
	 * @deprecated since 8 April 2014 (Sakai 10), this is not useful and would perform very badly
	 */
	public List<MessageChannel> getChannels()
	{
		List<MessageChannel> channels = m_storage.getChannels();
		return channels;
	} // getChannels

	/**
	 * check permissions for getChannel().
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return true if the user is allowed to getChannel(channelId), false if not.
	 */
	public boolean allowGetChannel(String ref)
	{
		return unlockCheck(SECURE_READ, ref);

	} // allowGetChannel

	/**
	 * Return a specific channel.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return the MessageChannel that has the specified name.
	 * @exception IdUnusedException
	 *            If this name is not defined for any channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to the channel.
	 */
	public MessageChannel getChannel(String ref) throws IdUnusedException, PermissionException
	{
		MessageChannel c = findChannel(ref);
		if (c == null) throw new IdUnusedException(ref);

		// check security (throws if not permitted)
		unlock(SECURE_READ, ref);

		return c;

	} // getChannel

	/**
	 * Return a specific channel
	 * 
	 * Warning: No check is made on channel permissions -- caller should filter for public messages
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return The channel, if found.
	 */
	public MessageChannel getChannelPublic(String ref)
	{
		return findChannel(ref);
	}
	
	/**
	 * Find the channel, in cache or info store - cache it if newly found.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return The channel, if found.
	 */
	protected MessageChannel findChannel(String ref)
	{
		if (ref == null) return null;

		MessageChannel channel = (MessageChannel) m_threadLocalManager.get(ref);
		if (channel == null)
		{
			channel = m_storage.getChannel(ref);

			// "cache" the channel in the current service in case they are needed again in this thread...
			if (channel != null)
			{
				m_threadLocalManager.set(ref, channel);
			}
		}

		return channel;
	} // findChannel

	/**
	 * check permissions for addChannel().
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return true if the user is allowed to addChannel(channelId), false if not.
	 */
	public boolean allowAddChannel(String ref)
	{
		// check security (throws if not permitted)
		return unlockCheck(SECURE_ADD, ref);

	} // allowAddChannel

	/**
	 * Add a new channel. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return The newly created channel, locked for update.
	 * @exception IdUsedException
	 *            if the id is not unique.
	 * @exception IdInvalidException
	 *            if the id is not made up of valid characters.
	 * @exception PermissionException
	 *            if the user does not have permission to add a channel.
	 */
	public MessageChannelEdit addChannel(String ref) throws IdUsedException, IdInvalidException, PermissionException
	{
		// check the name's validity
		if (!m_entityManager.checkReference(ref)) throw new IdInvalidException(ref);

		// check for existance
		if (m_storage.checkChannel(ref))
		{
			throw new IdUsedException(ref);
		}

		// check security
		unlock(SECURE_ADD, ref);

		// keep it
		MessageChannelEdit channel = m_storage.putChannel(ref);

		// We distinctly log the creation of a channel - even though we check the
		// NEW for security - some might suggest that this should wait for the commit
		// But it *has* been added - so we should know this happenned one
		// way or another.
		String channel_reference = "";
		if (channel != null) 
			channel_reference = channel.getReference();
		else
			log.info("addChannel: null channel returned from putChannel("+ref+")");
		
		Event event = m_eventTrackingService.newEvent(eventId(SECURE_CREATE), channel_reference, true);
		m_eventTrackingService.post(event);

		return channel;

	} // addChannel

	/**
	 * check permissions for editChannel()
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return true if the user is allowed to update the channel, false if not.
	 */
	public boolean allowEditChannel(String ref)
	{
		// check security (throws if not permitted)
		return unlockCheck3(SECURE_ADD, SECURE_UPDATE_ANY, SECURE_UPDATE_OWN, ref);

	} // allowEditChannel

	/**
	 * Return a specific channel, as specified by channel id, locked for update. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return the Channel that has the specified id.
	 * @exception IdUnusedException
	 *            If this name is not a defined channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to edit the channel.
	 * @exception InUseException
	 *            if the channel is locked for edit by someone else.
	 */
	public MessageChannelEdit editChannel(String ref) throws IdUnusedException, PermissionException, InUseException
	{
		// check for existance
		if (!m_storage.checkChannel(ref))
		{
			throw new IdUnusedException(ref);
		}

		// check security (throws if not permitted)
		unlock3(SECURE_ADD, SECURE_UPDATE_ANY, SECURE_UPDATE_OWN, ref);

		// ignore the cache - get the channel with a lock from the info store
		MessageChannelEdit edit = m_storage.editChannel(ref);
		if (edit == null) throw new InUseException(ref);

		((BaseMessageChannelEdit) edit).setEvent(SECURE_UPDATE_ANY);

		return edit;

	} // editChannel

	/**
	 * Commit the changes made to a MessageChannelEdit object, and release the lock. The MessageChannelEdit is disabled, and not to be used after this call.
	 * 
	 * @param user
	 *        The MessageChannelEdit object to commit.
	 */
	public void commitChannel(MessageChannelEdit edit)
	{
		if (edit == null) return;

		// check for closed edit
		if (!edit.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				log.warn("commitEdit(): closed ChannelEdit", e);
			}
			return;
		}

		m_storage.commitChannel(edit);

		// close the edit object
		((BaseMessageChannelEdit) edit).closeEdit();

	} // commitChannel

	/**
	 * Cancel the changes made to a MessageChannelEdit object, and release the lock. The MessageChannelEdit is disabled, and not to be used after this call.
	 * 
	 * @param user
	 *        The MessageChannelEdit object to commit.
	 */
	public void cancelChannel(MessageChannelEdit edit)
	{
		if (edit == null) return;

		// check for closed edit
		if (!edit.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				log.warn("cancelChannelEdit(): closed MessageChannelEdit", e);
			}
			return;
		}

		// release the edit lock
		m_storage.cancelChannel(edit);

		// close the edit object
		((BaseMessageChannelEdit) edit).closeEdit();

	} // cancelChannel

	/**
	 * Check permissions for removeChannel().
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return true if the user is allowed to removeChannel(), false if not.
	 */
	public boolean allowRemoveChannel(String ref)
	{
		// check security (throws if not permitted)
		return unlockCheck(SECURE_REMOVE_ANY, ref);

	} // allowRemoveChannel

	/**
	 * Remove a channel. Remove a channel - it must be locked from editChannel().
	 * 
	 * @param channel
	 *        The channel to remove.
	 * @exception PermissionException
	 *            if the user does not have permission to remove a channel.
	 */
	public void removeChannel(MessageChannelEdit channel) throws PermissionException
	{
		if (channel == null) return;

		// check for closed edit
		if (!channel.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				log.warn("removeChannel(): closed ChannelEdit", e);
			}
			return;
		}

		// check security
		unlock(SECURE_REMOVE_ANY, channel.getReference());

		m_storage.removeChannel(channel);

		// track event
		Event event = m_eventTrackingService.newEvent(eventId(SECURE_REMOVE_ANY), channel.getReference(), true);
		m_eventTrackingService.post(event);

		// mark the channel as removed
		((BaseMessageChannelEdit) channel).setRemoved(event);

		// close the edit object
		((BaseMessageChannelEdit) channel).closeEdit();

		// remove any realm defined for this resource
		try
		{
			m_authzGroupService.removeAuthzGroup(m_authzGroupService.getAuthzGroup(channel.getReference()));
		}
		catch (AuthzPermissionException e)
		{
			log.warn("removeChannel: removing realm for : " + channel.getReference() + " : " + e);
		}
		catch (GroupNotDefinedException ignore)
		{
		}

	} // removeChannel

	/**
	 * Get a message, given a reference. This call avoids the need to have channel security, as long as the user has permissions to the message.
	 * 
	 * @param ref
	 *        The message reference
	 * @return The message.
	 * @exception IdUnusedException
	 *            If this reference does not identify a message.
	 * @exception PermissionException
	 *            If the user does not have any permissions to the message.
	 */
	public Message getMessage(Reference ref) throws IdUnusedException, PermissionException
	{
		// could also check type, but need to know which message service we are working for! -ggolden
		if (!ref.getSubType().equals(REF_TYPE_MESSAGE))
		{
			throw new IdUnusedException(ref.getReference());
		}

		// get the channel, no security check
		MessageChannel c = findChannel(channelReference(ref.getContext(), ref.getContainer()));
		if (c == null)
		{
			throw new IdUnusedException(ref.getContainer());
		}

		// get the message from the channel
		Message m = ((BaseMessageChannelEdit) c).findMessage(ref.getId());

		// check security on the message, if not public
		if (m.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW) == null ||
			 !m.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW).equals(Boolean.TRUE.toString()))
		{
			boolean isDraft = m.getHeader().getDraft();
			if (!allowGetMessage(channelReference(ref.getContext(), ref.getContainer()), ref.getReference(), isDraft))
			{
			    if (isDraft)
			        throw new PermissionException(m_sessionManager.getCurrentSessionUserId(), eventId(SECURE_READ_DRAFT), ref.getReference());
			    else
			        throw new PermissionException(m_sessionManager.getCurrentSessionUserId(), eventId(SECURE_READ), ref.getReference());
			}
		}

		return m;

	} // getMessage


	/**
	 * Check the message read permission for the message
	 * 
	 * @param ref
	 *        The Reference (assumed to be to a message).
	 * @return True if the end user has permission to read the message, or permission to all messages in the channel, false if not.
	 */
	protected boolean allowGetMessage(String channelRef, String msgRef)
	{
		// check the message for default SECURE_READ
		return allowGetMessage(channelRef, msgRef, false);
	}

	/**
	 * Check the message read permission for the message
	 * 
	 * @param ref
	 *        The Reference (assumed to be to a message).
	 *        isDraft 
	 *        Whether or not the message is in draft
	 * @return True if the end user has permission to read the message, or permission to all messages in the channel, false if not.
	 */

	protected boolean allowGetMessage(String channelRef, String msgRef, boolean isDraft)
	{
		// Assume this reference is for a message
		// check the message
	    if (isDraft)
	        return unlockCheck(SECURE_READ_DRAFT, msgRef);
	    else
	        return unlockCheck(SECURE_READ,msgRef);
	}


	/**
	 * Cancel the changes made to a MessageEdit object, and release the lock. The MessageChannelEdit is disabled, and not to be used after this call.
	 * 
	 * @param user
	 *        The MessageEdit object to cancel.
	 */
	public void cancelMessage(MessageEdit edit)
	{
		if ((edit == null) || (((BaseMessageEdit) edit).m_channel == null)) return;

		((BaseMessageEdit) edit).m_channel.cancelMessage(edit);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Message> getMessages(String channelRef, Time afterDate, int limitedToLatest, boolean ascending, boolean includeDrafts,
			boolean pubViewOnly) throws PermissionException
	{
		// channel read security
		if (!pubViewOnly)
		{
			unlock(SECURE_READ, channelRef);
		}

		// get the channel, no security check
		MessageChannel c = findChannel(channelRef);
		if (c == null) return new Vector<Message>();

		// null - no drafts, "*", all drafts, <userId> drafts created by user id only
		String draftsForId = null;
		if (includeDrafts)
		{
			if (unlockCheck(SECURE_READ_DRAFT, channelRef))
			{
				draftsForId = "*";
			}
			else
			{
				draftsForId = m_sessionManager.getCurrentSessionUserId();
			}
		}

		// get messages filtered by date and count and drafts, in descending (latest first) order
		List<Message> msgs = m_storage.getMessages(c, afterDate, limitedToLatest, draftsForId, pubViewOnly);

		// if ascending, reverse
		if (ascending)
		{
			Collections.reverse(msgs);
		}

		return filterGroupAccess(msgs, c.getContext(), c.getReference());
	}

	/**
	 * Filter messages based on group access
	 * @param msgs Messages to filter
	 * @param context The context for this channel
	 * @param reference The internal reference for this channel
	 * @return Filtered list of Messages
	 */
	public List <Message> filterGroupAccess(List <Message> msgs, String context, String reference) {
		// check for the allowed groups of the current end use if we need it, and only once
		List filtered = new Vector();
		Collection allowedGroups = null;
		
		for (int i = 0; i < msgs.size(); i++)
		{
			Message msg = (Message) msgs.get(i);

			// if grouped, check that the end user has get access to any of this message's groups; reject if not
			if (msg.getHeader().getAccess() == MessageHeader.MessageAccess.GROUPED)
			{
				// check the message's groups to the allowed (get) groups for the current user
				Collection msgGroups = msg.getHeader().getGroups();

				// we need the allowed groups, so get it if we have not done so yet
				if (allowedGroups == null)
				{
					allowedGroups = getGroupsAllowFunction(SECURE_READ, context, reference);
				}

				// reject if there is no intersection in groups, but go through and validate special case
				if (!isIntersectionGroupRefsToGroups(msgGroups, allowedGroups)) {
					User currentUsr=null;
					try {
						currentUsr = m_userDirectoryService.getUser(m_sessionManager.getCurrentSessionUserId());

					} catch (UserNotDefinedException e1) {
						// TODO Auto-generated catch block
						log.info("User Not Defined: " + e1.getMessage());
					}
					
//					boolean isViewingAs = (m_securityService.getUserEffectiveRole(context) != null);
					
					//its possible the user wasn't found above
					String userId = "";
					if (currentUsr != null) {
						userId = currentUsr.getId();
					}
					
					//If user is not instructor 
					
					Site site = null;
					try {
						site = m_siteService.getSite(context);
					} catch (IdUnusedException e) {
						// TODO Auto-generated catch block
						log.debug("Site not found for " + context + " " + e.getMessage());
					}

					if (!canSeeAllGroups(userId, site)){
						continue;	
					}
				}
			}
			//Add it to the group filtered list
			filtered.add(msg);
		}
		return filtered;
	}
	
	/**
	 * See if the collection of group reference strings has at least one group that is in the collection of Group objects.
	 * 
	 * @param groupRefs
	 *        The collection (String) of group references.
	 * @param groups
	 *        The collection (Group) of group objects.
	 * @return true if there is interesection, false if not.
	 */
	protected boolean isIntersectionGroupRefsToGroups(Collection groupRefs, Collection groups)
	{
		for (Iterator iRefs = groupRefs.iterator(); iRefs.hasNext();)
		{
			String findThisGroupRef = (String) iRefs.next();
			for (Iterator iGroups = groups.iterator(); iGroups.hasNext();)
			{
				String thisGroupRef = ((Group) iGroups.next()).getReference();
				if (thisGroupRef.equals(findThisGroupRef))
				{
					return true;
				}
			}
		}

		return false;
	}
	
	/**
	 * Get the groups of this channel's contex-site that the end user has permission to "function" in.
	 * 
	 * @param function
	 *        The function to check
	 * @return The Collection (Group) of groups defined for the context of this channel that the end user has specified permissions in, empty if none.
	 */
	protected Collection<Group> getGroupsAllowFunction(String function, String m_context, String reference)
	{
		Collection<Group> rv = new Vector<Group>();

		try
		{
			// get the channel's site's groups
			Site site = m_siteService.getSite(m_context);
			Collection<Group> groups = site.getGroups();

			// if the user has SECURE_ALL_GROUPS in the context (site), and the function for the channel (channel,site), or is super, select all site groups
			if (m_securityService.isSuperUser() || (m_securityService.unlock(m_sessionManager.getCurrentSessionUserId(), eventId(SECURE_ALL_GROUPS), m_siteService.siteReference(m_context))
					&& unlockCheck(function, reference)))
			{
				return groups;
			}

			// otherwise, check the groups for function

			// get a list of the group refs, which are authzGroup ids
			Collection<String> groupRefs = new Vector<String>();
			for (Iterator<Group> i = groups.iterator(); i.hasNext();)
			{
				Group group = (Group) i.next();
				groupRefs.add(group.getReference());
			}

			// ask the authzGroup service to filter them down based on function
			groupRefs = m_authzGroupService.getAuthzGroupsIsAllowed(m_sessionManager.getCurrentSessionUserId(),
					eventId(function), groupRefs);

			// pick the Group objects from the site's groups to return, those that are in the groupRefs list
			for (Iterator<Group> i = groups.iterator(); i.hasNext();)
			{
				Group group = (Group) i.next();
				if (groupRefs.contains(group.getReference()))
				{
					rv.add(group);
				}
			}
		}
		catch (IdUnusedException e)
		{
		}

		return rv;
	}
	
	protected boolean canSeeAllGroups(String userId, Site site){
		if(site != null && site.getMember(userId) != null){
			if(m_securityService.unlock(userId, eventId(SECURE_ALL_GROUPS), m_siteService.siteReference(site.getId()))){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Access a list of channel ids that are defined related to the context.
	 * 
	 * @param context
	 *        The context in which to search
	 * @return A List (String) of channel id for channels withing the context.
	 */
	public List<String> getChannelIds(String context)
	{
		return m_storage.getChannelIdsMatching(channelReference(context, ""));
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ResourceService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public boolean willArchiveMerge()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public HttpAccess getHttpAccess()
	{
		return new HttpAccess()
		{

			public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref,
					Collection copyrightAcceptedRefs) throws EntityPermissionException, EntityNotDefinedException
			{
				try
				{
					// We need to write to a temporary stream for better speed, plus
					// so we can get a byte count. Internet Explorer has problems
					// if we don't make the setContentLength() call.
					ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
					OutputStreamWriter sw = new OutputStreamWriter(outByteStream);

					String skin = m_serverConfigurationService.getString("skin.default");
					String skinRepo = m_serverConfigurationService.getString("skin.repo");

					Message message = getMessage(ref);
					String title = ref.getDescription();
					MessageHeader messageHead = message.getHeader();
					String date = messageHead.getDate().toStringLocalFullZ();
					//Integer message_order=messageHead.getMessage_order();
					String from = messageHead.getFrom().getDisplayName();
					StringBuilder groups = new StringBuilder();
					Collection gr = messageHead.getGroups();
					for (Iterator i = gr.iterator(); i.hasNext();)
					{
						groups.append("<li>" + i.next() + "</li>");
					}
					String body = message.getBody();

					sw
							.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
									+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n"
									+ "<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
									+ "<link href=\"");
					sw.write(skinRepo);
					sw.write("/tool_base.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n" + "<link href=\"");
					sw.write(skinRepo);
					sw.write("/");
					sw.write(skin);
					sw.write("/tool.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n"
							+ "<meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />\n" + "<title>");
					sw.write(title);
					sw.write("</title></head><body><div class=\"portletBody\">\n" + "<h2>");
					sw.write(title);
					sw.write("</h2><ul><li>Date ");
					sw.write(date);
					sw.write("</li>");
					sw.write("<li>From ");
					sw.write(from);
					sw.write("</li>");
					sw.write(groups.toString());
					sw.write("<ul><p>");
					sw.write(body);
					sw.write("</p></div></body></html> ");

					sw.flush();
					res.setContentType("text/html");
					res.setContentLength(outByteStream.size());

					if (outByteStream.size() > 0)
					{
						// Increase the buffer size for more speed.
						res.setBufferSize(outByteStream.size());
					}

					OutputStream out = null;
					try
					{
						out = res.getOutputStream();
						if (outByteStream.size() > 0)
						{
							outByteStream.writeTo(out);
						}
						out.flush();
						out.close();
					}
					catch (Throwable ignore)
					{
					}
					finally
					{
						if (out != null)
						{
							try
							{
								out.close();
							}
							catch (Throwable ignore)
							{
							}
						}
					}
				}
				catch (PermissionException e)
				{
					throw new EntityPermissionException(e.getUser(), e.getLocalizedMessage(), e.getResource());
				}
				catch (IdUnusedException e)
				{
					throw new EntityNotDefinedException(e.getId());
				}
				catch (Throwable t)
				{
					throw new RuntimeException("Faied to find message ", t);
				}
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityDescription(Reference ref)
	{
		// we could check that the type is one of the message services, but lets just assume it is so we don't need to know them here -ggolden

		String rv = "Message: " + ref.getReference();

		try
		{
			// if this is a channel
			if (REF_TYPE_CHANNEL.equals(ref.getSubType()))
			{
				MessageChannel channel = getChannel(ref.getReference());
				rv = "Channel: " + channel.getId() + " (" + channel.getContext() + ")";
			}
		}
		catch (PermissionException e)
		{
		}
		catch (IdUnusedException e)
		{
		}
		catch (NullPointerException e)
		{
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		// we could check that the type is one of the message services, but lets just assume it is so we don't need to know them here -ggolden

		ResourceProperties rv = null;

		try
		{
			// if this is a channel
			if (REF_TYPE_CHANNEL.equals(ref.getSubType()))
			{
				MessageChannel channel = getChannel(ref.getReference());
				rv = channel.getProperties();
			}

			// otherwise a message
			else if (REF_TYPE_MESSAGE.equals(ref.getSubType()))
			{
				Message message = getMessage(ref);
				rv = message.getProperties();
			}

			else
				log.warn("getProperties(): unknown message ref subtype: " + ref.getSubType() + " in ref: " + ref.getReference());
		}
		catch (PermissionException e)
		{
			log.warn("getProperties(): " + e);
		}
		catch (IdUnusedException e)
		{
			// This just means that the resource once pointed to as an attachment or something has been deleted.
			// log.warn("getProperties(): " + e);
		}
		catch (NullPointerException e)
		{
			log.warn("getProperties(): " + e);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Entity getEntity(Reference ref)
	{
		// we could check that the type is one of the message services, but lets just assume it is so we don't need to know them here -ggolden

		Entity rv = null;

		try
		{
			// if this is a channel
			if (REF_TYPE_CHANNEL.equals(ref.getSubType()))
			{
				rv = getChannel(ref.getReference());
			}

			// otherwise a message
			else if (REF_TYPE_MESSAGE.equals(ref.getSubType()))
			{
				rv = getMessage(ref);
			}

			// else try {throw new Exception();} catch (Exception e) {log.warn("getResource(): unknown message ref subtype: " + m_subType + " in ref: " + m_reference, e);}
			else
				log.warn("getResource(): unknown message ref subtype: " + ref.getSubType() + " in ref: " + ref.getReference());
		}
		catch (PermissionException e)
		{
			log.warn("getResource(): " + e);
		}
		catch (IdUnusedException e)
		{
			log.warn("getResource(): " + e);
		}
		catch (NullPointerException e)
		{
			log.warn("getResource(): " + e);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<String> getEntityAuthzGroups(Reference ref, String userId)
	{
		// we could check that the type is one of the message services, but lets just assume it is so we don't need to know them here -ggolden

		Collection<String> rv = new Vector<String>();

		// for MessageService messages:
		// if access set to CHANNEL (or PUBLIC), use the message, channel and site authzGroups.
		// if access set to GROUPED, use the message, and the groups, but not the channel or site authzGroups.
		// if the user has SECURE_ALL_GROUPS in the context, ignore GROUPED access and treat as if CHANNEL

		// for Channels, use the channel and site authzGroups.
		try
		{
			// for message
			if (REF_TYPE_MESSAGE.equals(ref.getSubType()))
			{
				// message
				rv.add(ref.getReference());
				
				boolean grouped = false;
				Collection<String> groups = null;

				// check SECURE_ALL_GROUPS - if not, check if the message has groups or not
				// TODO: the last param needs to be a ContextService.getRef(ref.getContext())... or a ref.getContextAuthzGroup() -ggolden
				if ((userId == null) || ((!m_securityService.isSuperUser(userId)) && (!m_securityService.unlock(userId, eventId(SECURE_ALL_GROUPS), m_siteService.siteReference(ref.getContext())))))
				{
					// get the channel to get the message to get group information
					// TODO: check for efficiency, cache and thread local caching usage -ggolden
					String channelRef = channelReference(ref.getContext(), ref.getContainer());
					MessageChannel c = findChannel(channelRef);
					if (c != null)
					{
						Message m = ((BaseMessageChannelEdit) c).findMessage(ref.getId());
						if (m != null)
						{
							grouped = MessageHeader.MessageAccess.GROUPED == m.getHeader().getAccess();
							groups = m.getHeader().getGroups();
						}
					}
				}

				if (grouped)
				{
					// groups
					rv.addAll(groups);
				}

				// not grouped
				else
				{
					// channel
					rv.add(channelReference(ref.getContext(), ref.getContainer()));

					// site
					ref.addSiteContextAuthzGroup(rv);
				}
			}

			// for channel
			else
			{
				// channel
				rv.add(channelReference(ref.getContext(), ref.getId()));

				// site
				ref.addSiteContextAuthzGroup(rv);
			}
		}
		catch (Throwable e)
		{
			log.warn("getEntityAuthzGroups(): " + e);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityUrl(Reference ref)
	{
		// we could check that the type is one of the message services, but lets just assume it is so we don't need to know them here -ggolden

		String url = null;

		try
		{
			// if this is a channel
			if (REF_TYPE_CHANNEL.equals(ref.getSubType()))
			{
				MessageChannel channel = getChannel(ref.getReference());
				url = channel.getUrl();
			}

			// otherwise a message
			else if (REF_TYPE_MESSAGE.equals(ref.getSubType()))
			{
				Message message = getMessage(ref);
				url = message.getUrl();
			}

			else
				log.warn("getUrl(): unknown message ref subtype: " + ref.getSubType() + " in ref: " + ref.getReference());
		}
		catch (PermissionException e)
		{
			log.warn("getUrl(): " + e);
		}
		catch (IdUnusedException e)
		{
			log.warn("getUrl(): " + e);
		}
		catch (NullPointerException e)
		{
			log.warn("getUrl(): " + e);
		}

		return url;
	}

	/**
	 * {@inheritDoc}
	 */
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		// prepare the buffer for the results log
		StringBuilder results = new StringBuilder();

		// start with an element with our very own (service) name
		Element element = doc.createElement(serviceName());
		((Element) stack.peek()).appendChild(element);
		stack.push(element);

		// get the channel associated with this site
		String channelRef = channelReference(siteId, SiteService.MAIN_CONTAINER);

		results.append("archiving " + getLabel() + " channel " + channelRef + ".\n");

		try
		{
			// do the channel
			MessageChannel channel = getChannel(channelRef);
			Element containerElement = channel.toXml(doc, stack);
			stack.push(containerElement);

			// do the messages in the channel
			Iterator messages = channel.getMessages(null, true).iterator();
			while (messages.hasNext())
			{
				Message msg = (Message) messages.next();
				msg.toXml(doc, stack);

				// collect message attachments
				MessageHeader header = msg.getHeader();
				List atts = header.getAttachments();
				for (int i = 0; i < atts.size(); i++)
				{
					Reference ref = (Reference) atts.get(i);
					// if it's in the attachment area, and not already in the list
					if ((ref.getReference().startsWith("/content/attachment/")) && (!attachments.contains(ref)))
					{
						attachments.add(ref);
					}
				}
			}
			
			// archive the synoptic tool options
			archiveSynopticOptions(siteId, doc, element);

			stack.pop();
		}
		catch (Exception any)
		{
			log.warn("archve: exception archiving messages for service: " + serviceName() + " channel: " + channelRef);
		}

		stack.pop();

		return results.toString();
	}
	
	/**
	 * try to add synoptic options for this tool to the archive, if they exist
	 * @param siteId
	 * @param doc
	 * @param element
	 */
	public void archiveSynopticOptions(String siteId, Document doc, Element element)
	{
		try
		{
			// archive the synoptic tool options
			Site site = m_siteService.getSite(siteId);
			ToolConfiguration synTool = site.getToolForCommonId("sakai.synoptic." + getLabel());
			Properties synProp = synTool.getPlacementConfig();
			if (synProp != null && synProp.size() > 0) {
				Element synElement = doc.createElement(SYNOPTIC_TOOL);
				Element synProps = doc.createElement(PROPERTIES);

				Set synPropSet = synProp.keySet();
				Iterator propIter = synPropSet.iterator();
				while (propIter.hasNext())
				{
					String propName = (String)propIter.next();
					Element synPropEl = doc.createElement(PROPERTY);
					synPropEl.setAttribute(NAME, propName);
					synPropEl.setAttribute(VALUE, synProp.getProperty(propName));
					synProps.appendChild(synPropEl);
				}

				synElement.appendChild(synProps);
				element.appendChild(synElement);
			}
		}
		catch (Exception e)
		{
			log.warn("archive: exception archiving synoptic options for service: " + serviceName());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport)
	{
		// get the system name: FROM_WT, FROM_CT, FROM_SAKAI
		String source = "";
		// root: <service> node
		Node parent = root.getParentNode(); // parent: <archive> node containing "system"
		if (parent.getNodeType() == Node.ELEMENT_NODE)
		{
			Element parentEl = (Element) parent;
			source = parentEl.getAttribute("system");
		}

		HashSet userSet = (HashSet) userListAllowImport;

		Map ids = new HashMap();

		// prepare the buffer for the results log
		StringBuilder results = new StringBuilder();

		// get the channel associated with this site
		String channelRef = channelReference(siteId, SiteService.MAIN_CONTAINER);

		int count = 0;

		try
		{
			MessageChannel channel = null;
			try
			{
				channel = getChannel(channelRef);
			}
			catch (IdUnusedException e)
			{
				MessageChannelEdit edit = addChannel(channelRef);
				commitChannel(edit);
				channel = edit;
			}

			// pass the DOM to get new message ids, record the mapping from old to new, and adjust attachments
			NodeList children2 = root.getChildNodes();
			int length2 = children2.getLength();
			for (int i2 = 0; i2 < length2; i2++)
			{
				Node child2 = children2.item(i2);
				if (child2.getNodeType() == Node.ELEMENT_NODE)
				{
					Element element2 = (Element) child2;

					// get the "channel" child
					if (element2.getTagName().equals("channel"))
					{
						NodeList children3 = element2.getChildNodes();
						final int length3 = children3.getLength();
						for (int i3 = 0; i3 < length3; i3++)
						{
							Node child3 = children3.item(i3);
							if (child3.getNodeType() == Node.ELEMENT_NODE)
							{
								Element element3 = (Element) child3;

								parseMergeChannelExtra(element3, channelRef);

								// for "message" children
								if (element3.getTagName().equals("message"))
								{
									// get the header child
									NodeList children4 = element3.getChildNodes();
									final int length4 = children4.getLength();
									for (int i4 = 0; i4 < length4; i4++)
									{
										Node child4 = children4.item(i4);
										if (child4.getNodeType() == Node.ELEMENT_NODE)
										{
											Element element4 = (Element) child4;

											// for "header" children
											if (element4.getTagName().equals("header"))
											{
												String oldUserId = element4.getAttribute("from");

												// userIdTrans is not empty only when from WT
												if (!userIdTrans.isEmpty())
												{
													if (userIdTrans.containsKey(oldUserId))
													{
														element4.setAttribute("from", (String) userIdTrans.get(oldUserId));
													}
												}

												// adjust the id
												String oldId = element4.getAttribute("id");
												String newId = m_idManager.createUuid();
												ids.put(oldId, newId);
												element4.setAttribute("id", newId);

												// get the attachment kids
												NodeList children5 = element4.getChildNodes();
												final int length5 = children5.getLength();
												for (int i5 = 0; i5 < length5; i5++)
												{
													Node child5 = children5.item(i5);
													if (child5.getNodeType() == Node.ELEMENT_NODE)
													{
														Element element5 = (Element) child5;

														// for "attachment" children
														if (element5.getTagName().equals("attachment"))
														{
															// map the attachment area folder name
															String oldUrl = element5.getAttribute("relative-url");
															if (oldUrl.startsWith("/content/attachment/"))
															{
																String newUrl = (String) attachmentNames.get(oldUrl);
																if (newUrl != null)
																{
																	if (newUrl.startsWith("/attachment/"))
																		newUrl = "/content".concat(newUrl);

																	element5.setAttribute("relative-url", Validator
																			.escapeQuestionMark(newUrl));
																}
															}

															// map any references to this site to the new site id
															else if (oldUrl.startsWith("/content/group/" + fromSiteId + "/"))
															{
																String newUrl = "/content/group/" + siteId
																		+ oldUrl.substring(15 + fromSiteId.length());
																element5.setAttribute("relative-url", Validator
																		.escapeQuestionMark(newUrl));
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
					// merge synoptic tool
					else if (element2.getTagName().equals(SYNOPTIC_TOOL)) 
					{
						Site site = m_siteService.getSite(siteId);
						ToolConfiguration synTool = site.getToolForCommonId("sakai.synoptic." + getLabel());
						Properties synProps = synTool.getPlacementConfig();

						NodeList synPropNodes = element2.getChildNodes();
						for (int props = 0; props < synPropNodes.getLength(); props++)
						{
							Node propsNode = synPropNodes.item(props);
							if (propsNode.getNodeType() == Node.ELEMENT_NODE)
							{
								Element synPropEl = (Element) propsNode;
								if (synPropEl.getTagName().equals(PROPERTIES))
								{
									NodeList synProperties = synPropEl.getChildNodes();
									for (int p = 0; p < synProperties.getLength(); p++)
									{
										Node propertyNode = synProperties.item(p);
										if (propertyNode.getNodeType() == Node.ELEMENT_NODE)
										{
											Element propEl = (Element) propertyNode;
											if (propEl.getTagName().equals(PROPERTY))
											{
												String propName = propEl.getAttribute(NAME);
												String propValue = propEl.getAttribute(VALUE);
												
												if (propName != null && propName.length() > 0 && propValue != null && propValue.length() > 0)
												{
													if (propName.equals(CHANNEL_PROP))
													{
														int index = propValue.lastIndexOf("/");
														propValue = propValue.substring(index + 1);
														if (propValue != null && propValue.length() > 0) 
														{
															String synChannelRef = channelReference(siteId, propValue);					
															try	
															{
																MessageChannelEdit c = editChannel(synChannelRef);
																synProps.setProperty(propName, channelRef.toString());
															}
															catch (IdUnusedException e)	
															{
																// do not add channel b/c it does not exist in tool
																log.warn("Synoptic Tool Channel option not added- " + synChannelRef + ":" + e);
															}
														}			
													}
													else
													{
														synProps.setProperty(propName, propValue);
													}
												}
											}
										}
									}
								}
							}
						}
						Session session = m_sessionManager.getCurrentSession();
						ToolSession toolSession = session.getToolSession(synTool.getId());
						if (toolSession.getAttribute(STATE_UPDATE) == null)
						{
							toolSession.setAttribute(STATE_UPDATE, STATE_UPDATE);
						}
						m_siteService.save(site);
					}
				}
			}

			// one more pass to update reply-to (now we have a complte id mapping),
			// and we are ready then to create the message
			children2 = root.getChildNodes();
			length2 = children2.getLength();
			for (int i2 = 0; i2 < length2; i2++)
			{
				Node child2 = children2.item(i2);
				if (child2.getNodeType() == Node.ELEMENT_NODE)
				{
					Element element2 = (Element) child2;

					// get the "channel" child
					if (element2.getTagName().equals("channel"))
					{
						NodeList children3 = element2.getChildNodes();
						final int length3 = children3.getLength();
						for (int i3 = 0; i3 < length3; i3++)
						{
							Node child3 = children3.item(i3);
							if (child3.getNodeType() == Node.ELEMENT_NODE)
							{
								Element element3 = (Element) child3;

								// for "message" children
								if (element3.getTagName().equals("message"))
								{
									// a flag showing if continuing merging the message
									boolean goAhead = true;

									// get the header child
									NodeList children4 = element3.getChildNodes();
									final int length4 = children4.getLength();
									for (int i4 = 0; i4 < length4; i4++)
									{
										Node child4 = children4.item(i4);
										if (child4.getNodeType() == Node.ELEMENT_NODE)
										{
											Element element4 = (Element) child4;

											// for "header" children
											if (element4.getTagName().equals("header"))
											{
												// adjust the replyTo
												String oldReplyTo = element4.getAttribute("replyTo");
												if ((oldReplyTo != null) && (oldReplyTo.length() > 0))
												{
													String newReplyTo = (String) ids.get(oldReplyTo);
													if (newReplyTo != null)
													{
														element4.setAttribute("replyTo", newReplyTo);
													}
												}

												// only merge this message when the userId has the right role
												String fUserId = element4.getAttribute("from");
												if (!fUserId.equalsIgnoreCase("postmaster")
														&& !userSet.contains(element4.getAttribute("from")))
												{
													goAhead = false;
												}
												// TODO: reall want a draft? -ggolden
												// set draft status based upon property setting
												if (!m_serverConfigurationService.getBoolean("import.importAsDraft", true))
												{
													String draftAttribute = element4.getAttribute("draft");
													if (draftAttribute.equalsIgnoreCase("true") || draftAttribute.equalsIgnoreCase("false"))
														element4.setAttribute("draft", draftAttribute);
													else
														element4.setAttribute("draft", "true");
												}
												else
												{
													element4.setAttribute("draft", "true");
												}
											}
										}
									}

									// merge if ok
									if (goAhead)
									{
										// create a new message in the channel
										MessageEdit edit = channel.mergeMessage(element3);
										// commit the new message without notification
										channel.commitMessage(edit, NotificationService.NOTI_NONE);
										count++;
									}
								}
							}
						}
					}
				}
			}
		}
		catch (Exception any)
		{
			log.warn("mergeMessages(): exception in handling " + serviceName() + " : ", any);
		}

		results.append("merging " + getLabel() + " channel " + channelRef + " (" + count + ") messages.\n");
		return results.toString();

	} // merge

	/**
	 * Import the synoptic tool options from another site
	 * 
	 * @param fromContext
	 * @param toContext
	 */
	public void transferSynopticOptions(String fromContext, String toContext)
	{
		try 
		{
			// transfer the synoptic tool options
			Site fromSite = m_siteService.getSite(fromContext);
			ToolConfiguration fromSynTool = fromSite.getToolForCommonId("sakai.synoptic." + getLabel());
			Properties fromSynProp = null;
			if (fromSynTool != null) 
				fromSynProp = fromSynTool.getPlacementConfig();

			Site toSite = m_siteService.getSite(toContext);
			ToolConfiguration toSynTool = toSite.getToolForCommonId("sakai.synoptic." + getLabel());
			Properties toSynProp = null;
			if (toSynTool != null)
				toSynProp = toSynTool.getPlacementConfig();

			if (fromSynProp != null && !fromSynProp.isEmpty()) 
			{
				Set synPropSet = fromSynProp.keySet();
				Iterator propIter = synPropSet.iterator();
				while (propIter.hasNext())
				{
					String propName = ((String)propIter.next());
					String propValue = fromSynProp.getProperty(propName);
					if (propValue != null && propValue.length() > 0)
					{
						if (propName.equals(CHANNEL_PROP))
						{
							int index = propValue.lastIndexOf("/");
							propValue = propValue.substring(index + 1);
							if (propValue != null && propValue.length() > 0) 
							{
								String channelRef = channelReference(toContext, propValue);
								toSynProp.setProperty(propName, channelRef.toString());
							}
						}
						else
						{
							toSynProp.setProperty(propName, propValue);
						}
					}
				}

				Session session = m_sessionManager.getCurrentSession();
				ToolSession toolSession = session.getToolSession(toSynTool.getId());
				if (toolSession.getAttribute(STATE_UPDATE) == null)
				{
					toolSession.setAttribute(STATE_UPDATE, STATE_UPDATE);
				}

				m_siteService.save(toSite);
			}
		}
		catch (PermissionException pe)
		{
			log.warn("PermissionException transferring synoptic options for " + serviceName() + ':', pe);
		}
		catch (IdUnusedException e)
		{
			log.warn("Channel " + fromContext + " cannot be found. ");
		}
		catch (Exception e)
		{
			log.warn("transferSynopticOptions(): exception in handling " + serviceName() + " : ", e);
		}
	}

	/**
	 * Handle the extra "categtories" stuff in the channel part of the merge xml.
	 * 
	 * @param element
	 *        The "channel" node child.
	 * @param channelRef
	 *        The message channel ref.
	 */
	protected void parseMergeChannelExtra(Element element3, String channelRef)
	{
	}

	/**
	 * Setup a main message channel for a site.
	 * 
	 * @param siteId
	 *        The site id.
	 */
	protected void enableMessageChannel(String siteId)
	{
		// form the channel name
		String channelRef = channelReference(siteId, SiteService.MAIN_CONTAINER);

		// see if there's a channel
		try
		{
			getChannel(channelRef);
		}
		catch (IdUnusedException un)
		{
			try
			{
				// create a channel
				MessageChannelEdit edit = addChannel(channelRef);
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
		catch (PermissionException e)
		{
		}
	}

	/**
	 * Remove the main message channel for a site.
	 * 
	 * @param site
	 *        The site.
	 */
	protected void disableMessageChannel(String siteId)
	{
		// TODO: we do nothing now - channels hang around after the tool is removed from the site or the site is deleted -ggolden
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * MessageChannel implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseMessageChannelEdit<T extends MessageEdit> extends Observable implements MessageChannelEdit<T>, SessionBindingListener
	{
		/** The context in which this channel exists. */
		protected String m_context = null;

		/** The channel id, unique within the context. */
		protected String m_id = null;

		/** The properties. */
		protected ResourcePropertiesEdit m_properties = null;

		/** When true, the channel has been removed. */
		protected boolean m_isRemoved = false;

		/** The event code for this edit. */
		protected String m_event = null;

		/** Active flag. */
		protected boolean m_active = false;

		/**
		 * Construct with a reference.
		 * 
		 * @param ref
		 *        The channel reference.
		 */
		public BaseMessageChannelEdit(String ref)
		{
			// set the ids
			Reference r = m_entityManager.newReference(ref);
			m_context = r.getContext();
			m_id = r.getId();

			// setup for properties
			m_properties = new BaseResourcePropertiesEdit();

		} // BaseMessageChannelEdit

		/**
		 * Construct as a copy of another.
		 * 
		 * @param id
		 *        The other to copy.
		 */
		public BaseMessageChannelEdit(MessageChannel other)
		{
			// set the ids
			m_context = other.getContext();
			m_id = other.getId();

			// setup for properties
			m_properties = new BaseResourcePropertiesEdit();
			m_properties.addAll(other.getProperties());

		} // BaseMessageChannelEdit

		/**
		 * Construct from a channel (and possibly messages) already defined in XML in a DOM tree. The Channel is added to storage.
		 * 
		 * @param el
		 *        The XML DOM element defining the channel.
		 */
		public BaseMessageChannelEdit(Element el)
		{
			// setup for properties
			m_properties = new BaseResourcePropertiesEdit();

			// read the ids
			m_id = el.getAttribute("id");
			m_context = el.getAttribute("context");

			// the children (properties, ignore messages)
			NodeList children = el.getChildNodes();
			final int length = children.getLength();
			for (int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element element = (Element) child;

				// look for properties (ignore possible "message" entries)
				if (element.getTagName().equals("properties"))
				{
					// re-create properties
					m_properties = new BaseResourcePropertiesEdit(element);
				}
			}

		} // BaseMessageChannelEdit

		/**
		 * Clean up.
		 */
		protected void finalize()
		{
			deleteObservers();

			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancelChannel(this);
			}

		} // finalize

		/**
		 * Set the channel as removed.
		 * 
		 * @param event
		 *        The tracking event associated with this action.
		 */
		public void setRemoved(Event event)
		{
			m_isRemoved = true;

			// channel notification
			startNotifyThread(event);

			// now clear observers
			deleteObservers();

		} // setRemoved

		public void startNotifyThread(Event event) {
			try
			{
				NotifyThread n = new NotifyThread(event);
				Thread t = new Thread(n);
				t.start();
			}
			catch(Exception e)
			{
			}
		}

		/**
		 * Access the context of the resource.
		 * 
		 * @return The context.
		 */
		public String getContext()
		{
			return m_context;

		} // getContext

		/**
		 * Access the id of the resource.
		 * 
		 * @return The id.
		 */
		public String getId()
		{
			return m_id;

		} // getId

		/**
		 * Access the URL which can be used to access the resource.
		 * 
		 * @return The URL which can be used to access the resource.
		 */
		public String getUrl()
		{
			return m_serverConfigurationService.getAccessUrl() + getReference();

		} // getUrl

		/**
		 * Access the internal reference which can be used to access the resource from within the system.
		 * 
		 * @return The the internal reference which can be used to access the resource from within the system.
		 */
		public String getReference()
		{
			return channelReference(m_context, m_id);

		} // getReference

		/**
		 * @inheritDoc
		 */
		public String getReference(String rootProperty)
		{
			return getReference();
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl(String rootProperty)
		{
			return getUrl();
		}

		/**
		 * Access the channel's properties.
		 * 
		 * @return The channel's properties.
		 */
		public ResourceProperties getProperties()
		{
			return m_properties;

		} // getProperties

		/**
		 * check permissions for getMessages() or getMessage().
		 * 
		 * @return true if the user is allowed to get messages from this channel, false if not.
		 */
		public boolean allowGetMessages()
		{
			return unlockCheck(SECURE_READ, getReference());

		} // allowGetMessages

		/**
		 * @inheritDoc
		 */
		public int getCount() throws PermissionException
		{
			// Allow the Storage to indicate "not implemented"
			int retval = m_storage.getCount(this);
			if ( retval >= 0 ) return retval;
			List msgs = getMessages(null, true);
			return msgs.size();
		}

		/**
		 * @inheritDoc
		 */
		public int getCount(Filter filter) throws PermissionException
		{
			// Allow the Storage to indicate "not implemented"
			int retval = m_storage.getCount(this, filter);
			if ( retval >= 0 ) return retval;
			List msgs = getMessages(filter, true, null);
			return msgs.size();
		}

		/**
		 * Return a list of all or filtered messages in the channel. The order in which the messages will be found in the iteration is by date, oldest first if ascending is true, newest first if ascending is false.
		 * 
		 * @param filter
		 *        A filtering object to accept messages, or null if no filtering is desired.
		 * @param ascending
		 *        Order of messages, ascending if true, descending if false
		 * @return a list on channel Message objects or specializations of Message objects (may be empty).
		 * @exception PermissionException
		 *            if the user does not have read permission to the channel.
		 */
		public List getMessages(Filter filter, boolean ascending) throws PermissionException
		{
			// check security on the channel (throws if not permitted)
			unlock(SECURE_READ, getReference());
			// track event
			// m_eventTrackingService.post(m_eventTrackingService.newEvent(eventId(SECURE_READ), getReference(), false));

			return findFilterMessages(filter, ascending);

		} // getMessages

		/**
		 * {@inheritDoc}
		 */
		public List getMessages(Filter filter, boolean ascending, PagingPosition pages) throws PermissionException
		{
			// check security on the channel (throws if not permitted)
			unlock(SECURE_READ, getReference());

			// Allow the Storage to indicate "not implemented"
			List rv = m_storage.getMessages(this, filter, ascending, pages);
			if ( rv != null ) return rv;

			// This *will* get us messages regardless
			rv = findFilterMessages(filter, ascending);
			if ( pages != null ) 
			{
                		pages.validate(rv.size());
                		rv = rv.subList(pages.getFirst() - 1, pages.getLast());
			}
			return rv;
		} // getMessages

		/**
		 * Return a list of all public messages in the channel. 
		 * The order in which the messages will be found in the iteration is by date, 
		 * oldest first if ascending is true, newest first if ascending is false.
		 * 
		 * @param filter
		 *        Optional additional filtering object to accept messages, or null
		 * @param ascending
		 *        Order of messages, ascending if true, descending if false
		 * @return a list of channel Message objects or specializations of Message objects (may be empty).
		 */
		public List getMessagesPublic(Filter filter, boolean ascending)
		{
			String anncRead = eventId(MessageService.SECURE_READ);
			boolean isChannelPublic = m_securityService.unlock(m_userDirectoryService.getAnonymousUser(), anncRead, this.getReference());

			return findFilterMessages(new PublicFilter(filter, isChannelPublic), ascending);
		} // getMessagesPublic


		/**
		 * Return a specific channel message, as specified by message name.
		 * 
		 * @param messageId
		 *        The id of the message to get.
		 * @return the Message that has the specified id.
		 * @exception IdUnusedException
		 *            If this name is not a defined message in this channel.
		 * @exception PermissionException
		 *            If the user does not have any permissions to read the message.
		 */
		public Message getMessage(String messageId) throws IdUnusedException, PermissionException
		{
			// check security on the message
			if (!allowGetMessage(getReference(), messageReference(getReference(), messageId)))
			{
				throw new PermissionException(m_sessionManager.getCurrentSessionUserId(), eventId(SECURE_READ), messageReference(
						getReference(), messageId));
			}

			Message m = findMessage(messageId);

			if (m == null) throw new IdUnusedException(messageId);

			// track event
			// m_eventTrackingService.post(m_eventTrackingService.newEvent(eventId(SECURE_READ), m.getReference(), false));

			return m;

		} // getMessage

		/**
		 * check permissions for editMessage()
		 * 
		 * @param id
		 *        The message id.
		 * @return true if the user is allowed to update the message, false if not.
		 */
		public boolean allowEditMessage(String messageId)
		{
			Message m = findMessage(messageId);
			if (m == null) return false;

			return allowEditMessage(m, SECURE_UPDATE_OWN, SECURE_UPDATE_ANY);

		} // allowEditMessage

		/**
		 * check permissions for the message, to be able to edit or save it.
		 * 
		 * @param m
		 *        The message.
		 * @return true if the user is allowed to update the message, false if not.
		 */
		protected boolean allowEditMessage(Message m, String fOwn, String fAny)
		{
			// is this the user's own?
			if (m.getHeader().getFrom().getId().equals(m_sessionManager.getCurrentSessionUserId()))
			{
				// own or any
				return unlockCheck2(fOwn, fAny, m.getReference());
			}

			else
			{
				// just any
				return unlockCheck(fAny, m.getReference());
			}
		}

		/**
		 * Return a specific channel message, as specified by message name, locked for update. Must commitEdit() to make official, or cancelEdit() when done!
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
		public MessageEdit editMessage(String messageId) throws IdUnusedException, PermissionException, InUseException
		{
			Message m = findMessage(messageId);
			if (m == null) throw new IdUnusedException(messageId);

			// pick the security function
			String function = null;
			if (m.getHeader().getFrom().getId().equals(m_sessionManager.getCurrentSessionUserId()))
			{
				// own or any
				function = SECURE_UPDATE_OWN;
			}
			else
			{
				// just any
				function = SECURE_UPDATE_ANY;
			}

			// security check
			if (!allowEditMessage(messageId))
			{
				throw new PermissionException(m_sessionManager.getCurrentSessionUserId(), eventId(function), m.getReference());
			}

			// ignore the cache - get the message with a lock from the info store
			MessageEdit msg = m_storage.editMessage(this, messageId);
			if (msg == null) throw new InUseException(messageId);

			((BaseMessageEdit) msg).setEvent(function);

			return msg;

		} // editMessage
		
		
		/**
		 * {@inheritDoc}
		 */
		public void commitMessage_order(MessageEdit edit)
		{
			
			/* TO DO- Set permission to reorder announcement
			if (edit.getHeaderEdit().getMessage_order()!=this.)
			{
				try
				{
					throw new Exception();
				}
				catch (Exception e)
				{
					log.warn("commitEdit(): closed MessageEdit", e);
				}
				return;
			}
			*/
			
			int priority=NotificationService.NOTI_NONE;
			
			// complete the edit
		m_storage.commitMessage(this, edit);

		// clear out any thread local caching of this message, since it has just changed
		m_threadLocalManager.set(edit.getReference(), null);
		
		// pick the security function
		String function = getUpdateFunction(edit, SECURE_UPDATE_OWN, SECURE_UPDATE_ANY);
		
		// track event
		Event event = m_eventTrackingService.newEvent(eventId(function), edit.getReference(), true,
					priority);
		m_eventTrackingService.post(event);

		
		// close the edit object
		((BaseMessageEdit) edit).closeEdit();
	}

		/**
		 * Based on the message creator, find the proper security update function
		 * @param edit
		 * @return
		 */
		private String getUpdateFunction(MessageEdit edit, String ownFunction, String anyFunction) {
			// pick the security function
			String function = null;
			if (edit.getHeader().getFrom().getId().equals(m_sessionManager.getCurrentSessionUserId()))
			{
				// own or any
				function = ownFunction;
			}
			else
			{
				// just any
				function = anyFunction;
			}
			return function;
		}

		/**
		 * {@inheritDoc}
		 */
		public void commitMessage(MessageEdit edit)
		{
			commitMessage(edit, NotificationService.NOTI_OPTIONAL);

		} // commitMessage

		/**
		 * Commit the changes made to a MessageEdit object, and release the lock. The MessageEdit is disabled, and not to be used after this call.
		 * 
		 * @param user
		 *        The UserEdit object to commit.
		 * @param priority
		 *        The notification priority for this commit.
		 */
		public void commitMessage(MessageEdit edit, int priority)
		{
			commitMessage(edit, priority, "");
		}
		
		/**
		 * Commit the changes made to a MessageEdit object, and release the lock. The MessageEdit is disabled, and not to be used after this call.
		 * 
		 * @param user
		 *        The UserEdit object to commit.
		 * @param priority
		 *        The notification priority for this commit.
		 * @param invoker
		 * 		  The object to be called if a scheduled notification is used.
		 */
		public void commitMessage(MessageEdit edit, int priority, String invokee)
		{
			// check for closed edit
			if (!edit.isActiveEdit())
			{
				try
				{
					throw new Exception();
				}
				catch (Exception e)
				{
					log.warn("commitEdit(): closed MessageEdit", e);
				}
				return;
			}

			// update the properties
			// addLiveUpdateProperties(edit.getPropertiesEdit());//%%%

			boolean transientNotification = true;
			if(priority != NotificationService.NOTI_IGNORE){
				// if this message had a future invocation before, delete it because
				// this modification changed the date of release so either it will notify it now
				// or set a new future notification
				ScheduledInvocationManager scheduledInvocationManager = ComponentManager.get(ScheduledInvocationManager.class);

				if (edit.getProperties().getProperty(SCHED_INV_UUID) != null)
				{
					scheduledInvocationManager.deleteDelayedInvocation(edit.getProperties().getProperty(SCHED_INV_UUID));
					edit.getPropertiesEdit().removeProperty(SCHED_INV_UUID);
				}

				// For Scheduled Notification, compare header date with now to deterine
				// if an immediate notification is needed or a scheduled one
				// Put here since need to store uuid for notification just in case need to
				// delete/modify
				Time now = m_timeService.newTime();
				

				if (now.before(edit.getHeader().getDate()) && priority != NotificationService.NOTI_NONE)
				{
					final String uuid = scheduledInvocationManager.createDelayedInvocation(edit.getHeader().getDate(), 
							invokee, edit.getReference());

					final ResourcePropertiesEdit editProps = edit.getPropertiesEdit();

					editProps.addProperty(SCHED_INV_UUID, uuid);

					transientNotification = false;
				}
			}

			// complete the edit
			m_storage.commitMessage(this, edit);

			// clear out any thread local caching of this message, since it has just changed
			m_threadLocalManager.set(edit.getReference(), null);
			// clear out this messasge in the threadLocalManager findMessages cache
			removeFromFindMessagesCache(edit);

			// track event
			Event event = m_eventTrackingService.newEvent(eventId(((BaseMessageEdit) edit).getEvent()), edit.getReference(), true,
						priority);
			m_eventTrackingService.post(event);

			// channel notification
			if (priority != NotificationService.NOTI_IGNORE && transientNotification) 
			{
				startNotifyThread(event);
			}
			
			// close the edit object
			((BaseMessageEdit) edit).closeEdit();

		} // commitMessage
		

		public void removeFromFindMessagesCache (MessageEdit messageReference) {
			List msgs = (List) m_threadLocalManager.get(getReference() + ".msgs");
			if (msgs != null)
			{
				//Attempt to remove this message
				msgs.remove(messageReference);
				m_threadLocalManager.set(getReference() + ".msgs", msgs);
			}
		}

		/**
		 * Commit the draft changes made to a MessageEdit object. The MessageEdit is disabled, and not to be used after this call.
		 * This is mainly used to commit draft changes for messages, which are associated with non-existing groups.	
		 * 
		 * @param user
		 *        The UserEdit object to commit.
		 */
		public void commitDraftChanges(MessageEdit edit)
		{
			
			// complete the edit
			m_storage.commitMessage(this, edit);

			// clear out any thread local caching of this message, since it has just changed
			m_threadLocalManager.set(edit.getReference(), null);

			// close the edit object
			((BaseMessageEdit) edit).closeEdit();

		} // commitMessage

		/**
		 * Cancel the changes made to a MessageEdit object, and release the lock. The MessageEdit is disabled, and not to be used after this call.
		 * 
		 * @param user
		 *        The UserEdit object to commit.
		 */
		public void cancelMessage(MessageEdit edit)
		{
			// check for closed edit
			if (!edit.isActiveEdit())
			{
				try
				{
					throw new Exception();
				}
				catch (Exception e)
				{
					log.warn("commitEdit(): closed MessageEdit", e);
				}
				return;
			}

			// release the edit lock
			m_storage.cancelMessage(this, edit);

			// if an add, remove the message
			if (SECURE_ADD.equals(((BaseMessageEdit) edit).getEvent()))
			{
				m_storage.removeMessage(this, edit);
			}

			// close the edit object
			((BaseMessageEdit) edit).closeEdit();

		} // cancelMessage

		/**
		 * @inheritDoc
		 */
		public Collection getGroupsAllowGetMessage()
		{
			return getGroupsAllowFunction(SECURE_READ);
		}

		/**
		 * check permissions for addMessage().
		 * 
		 * @return true if the user is allowed to addMessage(...), false if not.
		 */
		public boolean allowAddMessage()
		{
			// checking allow at the channel (site) level
			if (allowAddChannelMessage()) return true;

			// if not, see if the user has any groups to which adds are allowed
			return (!getGroupsAllowAddMessage().isEmpty());

		} // allowAddMessage
		
		/**
		 * @inheritDoc
		 */
		public boolean allowAddDraftMessage()
		{
			// checking for permission for allow adding any message
			if (!allowAddMessage()) return false;

			// if allow to add message, one can save it as draft if only he can modify the draft afterwards.
			return (unlockCheck2(SECURE_UPDATE_ANY, SECURE_UPDATE_OWN, getReference()));

		} // allowAddDraftMessage

		/**
		 * @inheritDoc
		 */
		public boolean allowAddChannelMessage()
		{
			// check for messages that will be channel-wide:
			// base the check for SECURE_ADD on the site and the channel only (not the groups).

			// check security on the channel (throws if not permitted)
			return unlockCheck(SECURE_ADD, getReference());
		}

		/**
		 * @inheritDoc
		 */
		public Collection getGroupsAllowAddMessage()
		{
			return getGroupsAllowFunction(SECURE_ADD);
		}

		/**
		 * @inheritDoc
		 */
		public Collection getGroupsAllowRemoveMessage(boolean own)
		{
			return getGroupsAllowFunction(own ? SECURE_REMOVE_OWN : SECURE_REMOVE_ANY);
		}

		/**
		 * Add a new message to this channel. Must commitEdit() to make official, or cancelEdit() when done!
		 * 
		 * @return The newly added message.
		 * @exception PermissionException
		 *            If the user does not have write permission to the channel.
		 */
		public MessageEdit addMessage() throws PermissionException
		{
			// security check
			if (!allowAddMessage())
			{
				throw new PermissionException(m_sessionManager.getCurrentSessionUserId(), eventId(SECURE_ADD), getReference());
			}

			String id = null;
			// allocate a new unique message id
			id = m_idManager.createUuid();

			// get a new message in the info store
			MessageEdit msg = m_storage.putMessage(this, id);

			((BaseMessageEdit) msg).setEvent(SECURE_ADD);

			return msg;

		} // addMessage

		/**
		 * Merge in a new message as defined in the xml. Must commitEdit() to make official, or cancelEdit() when done!
		 * 
		 * @param el
		 *        The message information in XML in a DOM element.
		 * @return The newly added message, locked for update.
		 * @exception PermissionException
		 *            If the user does not have write permission to the channel.
		 * @exception IdUsedException
		 *            if the user id is already used.
		 */
		public MessageEdit mergeMessage(Element el) throws PermissionException, IdUsedException
		{
			// check security on the channel (throws if not permitted)
			unlock(SECURE_ADD, getReference());

			Message msgFromXml = (Message) newResource(this, el);

			// reserve a message with this id from the info store - if it's in use, this will return null
			MessageEdit msg = m_storage.putMessage(this, msgFromXml.getId());
			if (msg == null)
			{
				throw new IdUsedException(msgFromXml.getId());
			}

			// transfer from the XML read object to the Edit
			((BaseMessageEdit) msg).set(msgFromXml);

			// clear the groups and mark the message as channel
			// TODO: might be better done in merge(), but easier here -ggolden
			if (MessageHeader.MessageAccess.GROUPED == msg.getHeader().getAccess())
			{
				((BaseMessageHeaderEdit) msg.getHeaderEdit()).m_access = MessageHeader.MessageAccess.CHANNEL;
				((BaseMessageHeaderEdit) msg.getHeaderEdit()).m_groups = new Vector();
			}

			((BaseMessageEdit) msg).setEvent(SECURE_ADD);

			return msg;

		} // mergeMessage

		/**
		 * check permissions for removeMessage().
		 * 
		 * @param m
		 *        The message from this channel to remove.
		 * @return true if the user is allowed to removeMessage(...), false if not.
		 */
		public boolean allowRemoveMessage(Message m)
		{
			// this is true if we can remove it due to any of our group membership
			boolean allowed = allowEditMessage(m, SECURE_REMOVE_OWN, SECURE_REMOVE_ANY);

			// but we must also assure, that for grouped messages, we can remove it from ALL of the groups
			if (allowed && (m.getHeader().getAccess() == MessageHeader.MessageAccess.GROUPED))
			{
				boolean own = (m.getHeader().getFrom() == null) ? true : m.getHeader().getFrom().getId().equals(m_sessionManager.getCurrentSessionUserId());
				allowed = EntityCollections.isContainedEntityRefsToEntities(m.getHeader().getGroups(), getGroupsAllowRemoveMessage(own));
			}
			
			return allowed;

		} // allowRemoveMessage

		/**
		 * @inheritDoc
		 */
		public void removeMessage(String messageId) throws PermissionException
		{
			// ignore the cache - get the message with a lock from the info store
			MessageEdit message = m_storage.editMessage(this, messageId);
			if (message == null)
			{
				try
				{
					throw new Exception();
				}
				catch (Exception e)
				{
					log.warn("removeMessage(String): null edit ", e);
				}
				return;
			}

			removeMessage(message);

		} // removeMessage

		/**
		 * Remove a message from the channel - it must be locked from editMessage().
		 * 
		 * @param message
		 *        The message from this channel to remove.
		 * @exception PermissionException
		 *            if the user does not have permission to remove the message.
		 */
		public void removeMessage(MessageEdit message) throws PermissionException
		{
			// check for closed edit
			if (!message.isActiveEdit())
			{
				log.warn("removeMessage(): message is not in active edit, unable to remove");
				return;
			}

			// pick the security function
			String function = getUpdateFunction(message, SECURE_REMOVE_OWN, SECURE_REMOVE_ANY);

			// securityCheck
			if (!allowRemoveMessage(message))
			{
				cancelMessage(message);
				throw new PermissionException(m_sessionManager.getCurrentSessionUserId(), eventId(function), message.getReference());
			}

			m_storage.removeMessage(this, message);

			// track event
			Event event = m_eventTrackingService.newEvent(eventId(function), message.getReference(), true);
			m_eventTrackingService.post(event);

			startNotifyThread(event);

			// close the edit object
			((BaseMessageEdit) message).closeEdit();

			// remove any realm defined for this resource
			try
			{
				m_authzGroupService.removeAuthzGroup(message.getReference());
			}
			catch (AuthzPermissionException e)
			{
				log.warn("removeMessage: removing realm for : " + message.getReference() + " : " + e);
			}

		} // removeMessage

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
			Element channel = doc.createElement("channel");

			if (stack.isEmpty())
			{
				doc.appendChild(channel);
			}
			else
			{
				((Element) stack.peek()).appendChild(channel);
			}

			stack.push(channel);

			channel.setAttribute("context", getContext());
			channel.setAttribute("id", getId());

			// properties
			m_properties.toXml(doc, stack);

			stack.pop();

			return channel;

		} // toXml

		/**
		 * Find the message, in cache or info store - cache it if newly found.
		 * 
		 * @param messageId
		 *        The id of the message.
		 * @return The message, if found.
		 */
		protected Message findMessage(String messageId)
		{
			// if we have "cached" the entire set of messages in the thread, get that and find our message there
			List msgs = (List) m_threadLocalManager.get(getReference() + ".msgs");
			if (msgs != null)
			{
				for (Iterator i = msgs.iterator(); i.hasNext();)
				{
					Message m = (Message) i.next();
					if (m.getId().equals(messageId))
					{
						return m;
					}
				}
			}

			// if we have this one message cached, get that
			Message m = (Message) m_threadLocalManager.get(messageReference(getReference(), messageId));

			// if not, get from storage and cache
			if (m == null)
			{
				m = m_storage.getMessage(this, messageId);

				// if we got one, cache it in the thread
				if (m != null)
				{
					m_threadLocalManager.set(m.getReference(), m);
				}
			}

			return m;
		} // findMessage

		/**
		 * Find all messages.
		 * 
		 * @return a List of all messages in the channel.
		 */
		protected List findMessages()
		{
			// if we have done this already in this thread, use that
			List msgs = (List) m_threadLocalManager.get(getReference() + ".msgs");
			if (msgs == null)
			{
				msgs = m_storage.getMessages(this);

				// "cache" the mesasge in the current service in case they are needed again in this thread...
				m_threadLocalManager.set(getReference() + ".msgs", msgs);
			}

			return msgs;
		} // findMessages

		/**
		 * Find messages, sort, filter by group and the provided filter.
		 * 
		 * @param filter
		 *        A filtering object to accept messages, or null if no filtering is desired.
		 * @param ascending
		 *        Order of messages, ascending if true, descending if false
		 * @return All messages, sorted and filtered.
		 */
		public List findFilterMessages(Filter filter, boolean ascending)
		{
			List msgs = findMessages();
			if (msgs.size() == 0) return msgs;
			
			// sort - natural order is date ascending
			Collections.sort(msgs);

			// reverse, if not ascending
			if (!ascending)
			{
				Collections.reverse(msgs);
			}

			// filter out
			List filtered = new Vector();

			for (int i = 0; i < msgs.size(); i++) {
				Message msg = (Message) msgs.get(i);
				// reject if the filter rejects
				if ((filter != null) && (!filter.accept(msg))) continue;
				// if not rejected, keep
				filtered.add(msg);
			}

			return filterGroupAccess(filtered);
		} // findFilterMessages

		/**
		 * Filter messages based on group access
		 * @param msgs Messages to filter
		 * @return Filtered list of Messages
		 */
		public List <Message> filterGroupAccess(List <Message> msgs) {
			return BaseMessage.this.filterGroupAccess(msgs, m_context, getReference());
		}

		/**
		 * Test a collection of Group object for the specified group reference
		 * @param groups The collection (Group) of groups
		 * @param groupRef The string group reference to find.
		 * @return true if found, false if not.
		 */
		protected boolean groupCollectionContainsRefString(Collection groups, String groupRef)
		{
			for (Iterator i = groups.iterator(); i.hasNext();)
			{
				Group group = (Group) i.next();
				if (group.getReference().equals(groupRef)) return true;
			}

			return false;
		}

		/**
		 * Access the event code for this edit.
		 * 
		 * @return The event code for this edit.
		 */
		protected String getEvent()
		{
			return m_event;
		}

		/**
		 * Set the event code for this edit.
		 * 
		 * @param event
		 *        The event code for this edit.
		 */
		protected void setEvent(String event)
		{
			m_event = event;
		}

		/**
		 * Access the resource's properties for modification
		 * 
		 * @return The resource's properties.
		 */
		public ResourcePropertiesEdit getPropertiesEdit()
		{
			return m_properties;

		} // getPropertiesEdit

		/**
		 * Enable editing.
		 */
		public void activate()
		{
			m_active = true;

		} // activate

		/**
		 * Check to see if the edit is still active, or has already been closed.
		 * 
		 * @return true if the edit is active, false if it's been closed.
		 */
		public boolean isActiveEdit()
		{
			return m_active;

		} // isActiveEdit

		/**
		 * Close the edit object - it cannot be used after this.
		 */
		protected void closeEdit()
		{
			m_active = false;

		} // closeEdit

		/**
		 * Get the groups of this channel's contex-site that the end user has permission to "function" in.
		 * 
		 * @param function
		 *        The function to check
		 * @return The Collection (Group) of groups defined for the context of this channel that the end user has specified permissions in, empty if none.
		 */
		protected Collection<Group> getGroupsAllowFunction(String function)
		{
			return BaseMessage.this.getGroupsAllowFunction(function,m_context,getReference());
		}
		

		/******************************************************************************************************************************************************************************************************************************************************
		 * SessionBindingListener implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		public void valueBound(SessionBindingEvent event)
		{
		}

		public void valueUnbound(SessionBindingEvent event)
		{
			if (log.isDebugEnabled()) log.debug("valueUnbound()");

			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancelChannel(this);
			}

		} // valueUnbound

	} // class BaseMessageChannel

	/**********************************************************************************************************************************************************************************************************************************************************
	 * MessageEdit implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseMessageEdit implements MessageEdit, SessionBindingListener
	{
		/** The event code for this edit. */
		protected String m_event = null;

		/** Active flag. */
		protected boolean m_active = false;

		/** The message header. */
		protected MessageHeaderEdit m_header = null;

		/** The message body. */
		protected String m_body = null;

		/** The properties. */
		protected ResourcePropertiesEdit m_properties = null;

		/** A transient backpointer to the channel */
		protected MessageChannel m_channel = null;

		/**
		 * Construct.
		 * 
		 * @param id
		 *        The message id.
		 */
		public BaseMessageEdit(MessageChannel channel, String id)
		{
			// store the channel
			m_channel = channel;

			// store the id in a new (appropriate typed) header
			m_header = newMessageHeader(this, id);

			// setup for properties
			m_properties = new BaseResourcePropertiesEdit();

		} // BaseMessageEdit

		/**
		 * Construct as a copy of another message.
		 * 
		 * @param other
		 *        The other message to copy.
		 */
		public BaseMessageEdit(MessageChannel channel, Message other)
		{
			// store the channel
			m_channel = channel;

			setAll(other);

		} // BaseMessageEdit

		/**
		 * Construct from an existing definition, in xml.
		 * 
		 * @param channel
		 *        The channel in which this message lives.
		 * @param el
		 *        The message in XML in a DOM element.
		 */
		public BaseMessageEdit(MessageChannel channel, Element el)
		{
			this(channel, "");

			m_body = FormattedText.decodeFormattedTextAttribute(el, "body");

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
							if (m_body != null) m_body = FormattedText.convertPlaintextToFormattedText(m_body);
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

		} // BaseMessageEdit

		/**
		 * Take all values from this object.
		 * 
		 * @param user
		 *        The other object to take values from.
		 */
		protected void setAll(Message other)
		{
			// copy the header
			m_header = newMessageHeader(this, other.getHeader());

			// body
			m_body = other.getBody();

			// setup for properties
			m_properties = new BaseResourcePropertiesEdit();
			m_properties.addAll(other.getProperties());

		} // setAll

		/**
		 * Clean up.
		 */
		protected void finalize()
		{
			// catch the case where an edit was made but never resolved
			if ((m_active) && (m_channel != null))
			{
				m_channel.cancelMessage(this);
			}


		} // finalize

		/**
		 * Access the message header.
		 * 
		 * @return The message header.
		 */
		public MessageHeader getHeader()
		{
			return m_header;

		} // getHeader

		/**
		 * Access the id of the resource.
		 * 
		 * @return The id.
		 */
		public String getId()
		{
			return m_header.getId();

		} // getId

		/**
		 * Access the URL which can be used to access the resource.
		 * 
		 * @return The URL which can be used to access the resource.
		 */
		public String getUrl()
		{
			if (m_channel == null) return "";
			return m_serverConfigurationService.getAccessUrl() + getReference();

		} // getUrl

		/**
		 * Access the internal reference which can be used to access the resource from within the system.
		 * 
		 * @return The the internal reference which can be used to access the resource from within the system.
		 */
		public String getReference()
		{
			if (m_channel == null) return "";
			return messageReference(m_channel.getContext(), m_channel.getId(), getId());

		} // getReference

		/**
		 * @inheritDoc
		 */
		public String getReference(String rootProperty)
		{
			return getReference();
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl(String rootProperty)
		{
			return getUrl();
		}

		/**
		 * Access the channel's properties.
		 * 
		 * @return The channel's properties.
		 */
		public ResourceProperties getProperties()
		{
			return m_properties;

		} // getProperties

		/**
		 * Access the body text, as a string.
		 * 
		 * @return The body text, as a string.
		 */
		public String getBody()
		{
			return ((m_body == null) ? "" : m_body);

		} // getBodyText

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
			Element message = doc.createElement("message");

			if (stack.isEmpty())
			{
				doc.appendChild(message);
			}
			else
			{
				((Element) stack.peek()).appendChild(message);
			}

			stack.push(message);

			m_header.toXml(doc, stack);

			FormattedText.encodeFormattedTextAttribute(message, "body", getBody());

			/*
			 * // Note: the old way to set the body - CDATA is too sensitive to the characters within -ggolden Element body = doc.createElement("body"); message.appendChild(body); body.appendChild(doc.createCDATASection(getBody()));
			 */

			// properties
			m_properties.toXml(doc, stack);

			stack.pop();

			return message;

		} // toXml

		/**
		 * Compare this object with the specified object for order.
		 * 
		 * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
		 */
		public int compareTo(Object obj)
		{
			if (!(obj instanceof Message)) throw new ClassCastException();

			// if the object are the same, say so
			if (obj == this) return 0;

			// compare the header's date
			int compare = getHeader().getDate().compareTo(((Message) obj).getHeader().getDate());

			return compare;

		} // compareTo

		/**
		 * Are these objects equal? If they are both Message objects, and they have matching id's, they are.
		 * 
		 * @return true if they are equal, false if not.
		 */
		public boolean equals(Object obj)
		{
			if (!(obj instanceof Message)) return false;
			return ((Message) obj).getId().equals(getId());

		} // equals

		/**
		 * Make a hash code that reflects the equals() logic as well. We want two objects, even if different instances, if they have the same id to hash the same.
		 */
		public int hashCode()
		{
			return getId().hashCode();

		} // hashCode

		/**
		 * Replace the body, as a string.
		 * 
		 * @param body
		 *        The body, as a string.
		 */
		public void setBody(String body)
		{
			m_body = body;

		} // setBody

		/**
		 * Take all values from this object.
		 * 
		 * @param user
		 *        The other object to take values from.
		 */
		protected void set(Message other)
		{
			setAll(other);

		} // set

		/**
		 * Access the message header.
		 * 
		 * @return The message header.
		 */
		public MessageHeaderEdit getHeaderEdit()
		{
			return m_header;

		} // getHeaderEdit

		/**
		 * Access the event code for this edit.
		 * 
		 * @return The event code for this edit.
		 */
		protected String getEvent()
		{
			return m_event;
		}

		/**
		 * Set the event code for this edit.
		 * 
		 * @param event
		 *        The event code for this edit.
		 */
		protected void setEvent(String event)
		{
			m_event = event;
		}

		/**
		 * Access the resource's properties for modification
		 * 
		 * @return The resource's properties.
		 */
		public ResourcePropertiesEdit getPropertiesEdit()
		{
			return m_properties;

		} // getPropertiesEdit

		/**
		 * Enable editing.
		 */
		public void activate()
		{
			m_active = true;

		} // activate

		/**
		 * Check to see if the edit is still active, or has already been closed.
		 * 
		 * @return true if the edit is active, false if it's been closed.
		 */
		public boolean isActiveEdit()
		{
			return m_active;

		} // isActiveEdit

		/**
		 * Close the edit object - it cannot be used after this.
		 */
		protected void closeEdit()
		{
			m_active = false;

		} // closeEdit

		/******************************************************************************************************************************************************************************************************************************************************
		 * SessionBindingListener implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		public void valueBound(SessionBindingEvent event)
		{
		}

		public void valueUnbound(SessionBindingEvent event)
		{
			if (log.isDebugEnabled()) log.debug("valueUnbound()");

			// catch the case where an edit was made but never resolved
			if ((m_active) && (m_channel != null))
			{
				m_channel.cancelMessage(this);
			}

		} // valueUnbound

	} // BaseMessageEdit

	/**********************************************************************************************************************************************************************************************************************************************************
	 * MessageHeaderEdit implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseMessageHeaderEdit implements MessageHeaderEdit
	{
		/** The unique (within the channel) message id. */
		protected String m_id = null;

		/** The date/time the message was sent to the channel. */
		protected Time m_date = null;
		

		/** The message order the message was sent to the channel. */
		protected Integer m_message_order = 0;

		/** The User who sent the message to the channel. */
		protected User m_from = null;

		/** The attachments - dereferencer objects. */
		protected List m_attachments = null;

		/** The draft status for the message. */
		protected boolean m_draft = false;

		/** The Collection of groups (authorization group id strings, i.e. group refs). */
		protected Collection m_groups = new Vector();

		/** The message access. */
		protected MessageAccess m_access = MessageAccess.CHANNEL;

		/** A transient backpointer to the message. */
		protected Message m_message = null;

		/**
		 * Construct. Time and From set automatically.
		 * 
		 * @param id
		 *        The message id.
		 */
		public BaseMessageHeaderEdit(Message msg, String id)
		{
			m_message = msg;
			m_id = id;
			m_message_order=0;
			m_date = m_timeService.newTime();
			try
			{
				m_from = m_userDirectoryService.getUser(m_sessionManager.getCurrentSessionUserId());
			}
			catch (UserNotDefinedException e)
			{
				m_from = m_userDirectoryService.getAnonymousUser();
			}

			// init the AttachmentContainer
			m_attachments = m_entityManager.newReferenceList();

		} // BaseMessageHeaderEdit

		/**
		 * Construct as a copy of another header.
		 * 
		 * @param other
		 *        The other message header to copy.
		 */
		public BaseMessageHeaderEdit(Message msg, MessageHeader other)
		{
			m_message = msg;
			m_id = other.getId();
			m_date = m_timeService.newTime(other.getDate().getTime());
			m_from = other.getFrom();
			m_draft = other.getDraft();
			m_access = other.getAccess();
			m_message_order=other.getMessage_order();

			m_attachments = m_entityManager.newReferenceList();
			replaceAttachments(other.getAttachments());

			m_groups = new Vector(other.getGroups());

		} // BaseMessageHeaderEdit

		/**
		 * Construct, from an already existing XML DOM element.
		 * 
		 * @param el
		 *        The header in XML in a DOM element.
		 */
		public BaseMessageHeaderEdit(Message msg, Element el)
		{
			m_message = msg;
			m_id = el.getAttribute("id");
			try
			{
				m_from = m_userDirectoryService.getUser(el.getAttribute("from"));
			}
			catch (UserNotDefinedException e)
			{
				m_from = m_userDirectoryService.getAnonymousUser();
			}
			m_date = m_timeService.newTimeGmt(el.getAttribute("date"));
			String order = el.getAttribute("message_order");
			if (order !=null && !"".equals(order)) 
			{
				m_message_order=Integer.parseInt(order);
			} else {
				m_message_order = 0;
			}
			try
			{
				m_draft = Boolean.valueOf(el.getAttribute("draft")).booleanValue();
			}
			catch (Throwable any)
			{
			}

			// attachments and groups
			m_attachments = m_entityManager.newReferenceList();

			NodeList children = el.getChildNodes();
			final int length = children.getLength();
			for (int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE)
				{
					Element element = (Element) child;

					// look for an attachment
					if (element.getTagName().equals("attachment"))
					{
						m_attachments.add(m_entityManager.newReference(element.getAttribute("relative-url")));
					}

					// look for an group
					else if (element.getTagName().equals("group"))
					{
						m_groups.add(element.getAttribute("authzGroup"));
					}
				}
			}

			// extract access
			MessageAccess access = MessageAccess.fromString(el.getAttribute("access"));
			if (access != null)
			{
				m_access = access;
			}

		} // BaseMessageHeaderEdit

		/**
		 * Access the unique (within the channel) message id.
		 * 
		 * @return The unique (within the channel) message id.
		 */
		public String getId()
		{
			return m_id;

		} // getId

		/**
		 * Access the date/time the message was sent to the channel.
		 * 
		 * @return The date/time the message was sent to the channel.
		 */
		public Time getDate()
		{
			return m_date;

		} // getDate
		
		/**
		 * Access the message order the message was sent to the channel.
		 * 
		 * @return The message order the message was sent to the channel.
		 */
		public Integer getMessage_order()
		{
			return m_message_order;

		} // getMessage_order

		/**
		 * Access the User who sent the message to the channel.
		 * 
		 * @return The User who sent the message to the channel.
		 */
		public User getFrom()
		{
			return m_from;

		} // getFrom

		/**
		 * Access the draft status of the message.
		 * 
		 * @return True if the message is a draft, false if not.
		 */
		public boolean getDraft()
		{
			return m_draft;

		} // getDraft

		/**
		 * Set the draft status of the message.
		 * 
		 * @param draft
		 *        True if the message is a draft, false if not.
		 */
		public void setDraft(boolean draft)
		{
			m_draft = draft;

		} // setDraft

		/**
		 * @inheritDoc
		 */
		public Collection getGroups()
		{
			return new Vector(m_groups);
		}

		/**
		 * {@inheritDoc}
		 */
		public Collection getGroupObjects()
		{
			Vector rv = new Vector();
			if (m_groups != null)
			{
				for (Iterator i = m_groups.iterator(); i.hasNext();)
				{
					String groupId = (String) i.next();
					Group group = m_siteService.findGroup(groupId);
					if (group != null)
					{
						rv.add(group);
					}
				}
			}

			return rv;
		}

		/**
		 * @inheritDoc
		 */
		public MessageAccess getAccess()
		{
			return m_access;
		}

		/**
		 * @inheritDoc
		 */
		public void setGroupAccess(Collection groups) throws PermissionException
		{
			// convenience (and what else are we going to do?)
			if ((groups == null) || (groups.size() == 0))
			{
				clearGroupAccess();
				return;
			}
			
			// is there any change?  If we are already grouped, and the group list is the same, ignore the call
			if ((m_access == MessageAccess.GROUPED) && (EntityCollections.isEqualEntityRefsToEntities(m_groups, groups))) return;
			
			// there should not be a case where there's no message or a message with no channel... -ggolden
			if ((m_message == null) || ((BaseMessageEdit) m_message).m_channel == null)
			{
				log.warn("setGroupAccess() called with null message: " + ((m_message == null) ? "null" : ((BaseMessageEdit) m_message).toString()) + " or channel: " + ((m_message == null) ? "" : ((BaseMessageEdit) m_message).m_channel.toString()));
				throw new PermissionException(m_sessionManager.getCurrentSessionUserId(), "access:channel", ((m_message == null) ? "" : ((BaseMessageEdit) m_message).getReference()));
			}

			// isolate any groups that would be removed or added
			Collection addedGroups = new Vector();
			Collection removedGroups = new Vector();
			EntityCollections.computeAddedRemovedEntityRefsFromNewEntitiesOldRefs(addedGroups, removedGroups, groups, m_groups);

			// verify that the user has permission to remove
			if (removedGroups.size() > 0)
			{
				// is this the user's own?
				boolean own = (getFrom() == null) ? true : getFrom().getId().equals(m_sessionManager.getCurrentSessionUserId());

				// the Group objects the user has remove permission
				Collection allowedGroups = ((BaseMessageEdit) m_message).m_channel.getGroupsAllowRemoveMessage(own);

				for (Iterator i = removedGroups.iterator(); i.hasNext();)
				{
					String ref = (String) i.next();

					// is ref a group the user can remove from?
					if (!EntityCollections.entityCollectionContainsRefString(allowedGroups, ref))
					{
						throw new PermissionException(m_sessionManager.getCurrentSessionUserId(), "access:group:remove", ref);
					}
				}
			}
			
			// verify that the user has permission to add in those contexts
			if (addedGroups.size() > 0)
			{
				// the Group objects the user has add permission
				Collection allowedGroups = ((BaseMessageEdit) m_message).m_channel.getGroupsAllowAddMessage();

				for (Iterator i = addedGroups.iterator(); i.hasNext();)
				{
					String ref = (String) i.next();

					// is ref a group the user can remove from?
					if (!EntityCollections.entityCollectionContainsRefString(allowedGroups, ref))
					{
						throw new PermissionException(m_sessionManager.getCurrentSessionUserId(), "access:group:add", ref);
					}
				}
			}
			
			// we are clear to perform this
			m_access = MessageAccess.GROUPED;
			EntityCollections.setEntityRefsFromEntities(m_groups, groups);
		}

		/**
		 * @inheritDoc
		 */
		public void clearGroupAccess() throws PermissionException
		{
			// is there any change?  If we are already channel, ignore the call
			if (m_access == MessageAccess.CHANNEL) return;

			// there should not be a case where there's no message or a message with no channel... -ggolden
			if ((m_message == null) || ((BaseMessageEdit) m_message).m_channel == null)
			{
				log.warn("clearGroupAccess() called with null message: " + ((m_message == null) ? "null" : ((BaseMessageEdit) m_message).toString()) + " or channel: " + ((m_message == null) ? "" : ((BaseMessageEdit) m_message).m_channel.toString()));
				throw new PermissionException(m_sessionManager.getCurrentSessionUserId(), "access:channel", ((m_message == null) ? "" : ((BaseMessageEdit) m_message).getReference()));
			}

			// verify that the user has permission to add in the channel context
			boolean allowed = (m_message != null) && (((BaseMessageEdit) m_message).m_channel).allowAddChannelMessage();
			if (!allowed)
			{
				throw new PermissionException(m_sessionManager.getCurrentSessionUserId(), "access:channel", ((BaseMessageEdit) m_message).getReference());				
			}

			// we are clear to perform this
			m_access = MessageAccess.CHANNEL;
			m_groups.clear();
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
			Element header = doc.createElement("header");
			((Element) stack.peek()).appendChild(header);
			header.setAttribute("id", getId());
			header.setAttribute("from", getFrom().getId());
			header.setAttribute("date", getDate().toString());
			if(getMessage_order()==null){
				header.setAttribute("message_order", "0");
			}
			else{
			header.setAttribute("message_order", getMessage_order().toString());
			}
			if ((m_attachments != null) && (m_attachments.size() > 0))
			{
				for (int i = 0; i < m_attachments.size(); i++)
				{
					Reference attch = (Reference) m_attachments.get(i);
					Element attachment = doc.createElement("attachment");
					header.appendChild(attachment);
					attachment.setAttribute("relative-url", attch.getReference());
				}
			}

			// add groups
			if ((m_groups != null) && (m_groups.size() > 0))
			{
				for (Iterator i = m_groups.iterator(); i.hasNext();)
				{
					String group = (String) i.next();
					Element sect = doc.createElement("group");
					header.appendChild(sect);
					sect.setAttribute("authzGroup", group);
				}
			}

			// add access
			header.setAttribute("access", m_access.toString());

			return header;

		} // toXml

		/**
		 * Set the date/time the message was sent to the channel.
		 * 
		 * @param date
		 *        The date/time the message was sent to the channel.
		 */
		public void setDate(Time date)
		{
			if (!date.equals(m_date))
			{
				m_date.setTime(date.getTime());
			}

		} // setDate
		
		/**
		 * Set the message_order the message was sent to the channel.
		 * 
		 * @param date
		 *        The message_order the message was sent to the channel.
		 */
		public void setMessage_order(Integer message_order)
		{
			if (!message_order.equals(m_message_order))
			{
				m_message_order=message_order;
			}

		} // setMessage_order

		/**
		 * Set the User who sent the message to the channel.
		 * 
		 * @param user
		 *        The User who sent the message to the channel.
		 */
		public void setFrom(User user)
		{
			if (!user.equals(m_from))
			{
				m_from = user;
			}

		} // setFrom

		/******************************************************************************************************************************************************************************************************************************************************
		 * AttachmentContainer implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		/**
		 * Access the attachments of the event.
		 * 
		 * @return An copy of the set of attachments (a ReferenceVector containing Reference objects) (may be empty).
		 */
		public List getAttachments()
		{
			return m_entityManager.newReferenceList(m_attachments);

		} // getAttachments

		/**
		 * Add an attachment.
		 * 
		 * @param ref
		 *        The attachment Reference.
		 */
		public void addAttachment(Reference ref)
		{
			m_attachments.add(ref);

		} // addAttachment

		/**
		 * Remove an attachment.
		 * 
		 * @param ref
		 *        The attachment Reference to remove (the one removed will equal this, they need not be ==).
		 */
		public void removeAttachment(Reference ref)
		{
			m_attachments.remove(ref);

		} // removeAttachment

		/**
		 * Replace the attachment set.
		 * 
		 * @param attachments
		 *        A vector of Reference objects that will become the new set of attachments.
		 */
		public void replaceAttachments(List attachments)
		{
			m_attachments.clear();

			if (attachments != null)
			{
				Iterator it = attachments.iterator();
				while (it.hasNext())
				{
					m_attachments.add(it.next());
				}
			}

		} // replaceAttachments

		/**
		 * Clear all attachments.
		 */
		public void clearAttachments()
		{
			m_attachments.clear();

		} // clearAttachments

	} // BasicMessageHeaderEdit

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected interface Storage
	{
		/**
		 * Open and read.
		 */
		public void open();

		/**
		 * Write and Close.
		 */
		public void close();

		/**
		 * Return the identified channel, or null if not found.
		 */
		public MessageChannel getChannel(String ref);

		/**
		 * Return true if the identified channel exists.
		 */
		public boolean checkChannel(String ref);

		/**
		 * Get a list of all channels
		 */
		public List getChannels();

		/**
		 * Keep a new channel.
		 */
		public MessageChannelEdit putChannel(String ref);

		/**
		 * Get a channel locked for update
		 */
		public MessageChannelEdit editChannel(String ref);

		/**
		 * Commit a channel edit.
		 */
		public void commitChannel(MessageChannelEdit edit);

		/**
		 * Cancel a channel edit.
		 */
		public void cancelChannel(MessageChannelEdit edit);

		/**
		 * Forget about a channel.
		 */
		public void removeChannel(MessageChannelEdit channel);

		/**
		 * Get a message from a channel.
		 */
		public Message getMessage(MessageChannel channel, String messageId);

		/**
		 * Get a message from a channel locked for update
		 */
		public MessageEdit editMessage(MessageChannel channel, String messageId);

		/**
		 * Commit an edit.
		 */
		public void commitMessage(MessageChannel channel, MessageEdit edit);

		/**
		 * Cancel an edit.
		 */
		public void cancelMessage(MessageChannel channel, MessageEdit edit);

		/**
		 * Does this messages exist in a channel?
		 */
		public boolean checkMessage(MessageChannel channel, String messageId);

		/**
		 * Get the messages from a channel
		 */
		public List getMessages(MessageChannel channel);

		/**
		 * Make and lock a new message.
		 */
		public MessageEdit putMessage(MessageChannel channel, String id);

		/**
		 * Forget about a message.
		 */
		public void removeMessage(MessageChannel channel, MessageEdit edit);

		/**
		 * Count messages 
		 * @return message count (-1 indicates this is not implemented in Storage)
		 */ 
		public int getCount(MessageChannel channel);

		/**
		 * Count messages with a Filter
		 * @return message count (-1 indicates this is not implemented in Storage)
		 */
		public int getCount(MessageChannel channel, Filter filter);

		/**
		 * Get messages filtered, sorted, and paged
		 * 
		 * @param context
		 *        The root channel ref to match.
		 * @param filter
		 *        filter the records according to this filter - may be null
		 * @param pager
		 *        limit on the records returned - may be null
		 * @return A list of Message objects that meet the criteria; may be empty
	         *         returning null indicates - not implemened in Storage.
		 */
		public List getMessages(MessageChannel channel, Filter filter, boolean asc, PagingPosition pager);

		/**
		 * Get messages filtered by date and count and drafts, in descending (latest first) order
		 * 
		 * @param afterDate
		 *        if null, no date limit, else limited to only messages after this date.
		 * @param limitedToLatest
		 *        if 0, no count limit, else limited to only the latest this number of messages.
		 * @param draftsForId
		 *        how to handle drafts: null means no drafts, "*" means all, otherwise drafts only if created by this userId.
		 * @param pubViewOnly
		 *        if true, include only messages marked pubview, else include any.
		 * @return A list of Message objects that meet the criteria; may be empty
		 */
		public List getMessages(MessageChannel channel, Time afterDate, int limitedToLatest, String draftsForId, boolean pubViewOnly);

		/**
		 * Access a list of channel ids from channels with refs that start with (match) context.
		 * 
		 * @param context
		 *        The root channel ref to match.
		 * @return A List (String) of channel id for channels within the context.
		 */
		public List getChannelIdsMatching(String root);

	} // Storage

	/**********************************************************************************************************************************************************************************************************************************************************
	 * filter
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected class MessagePermissionFilter implements Filter
	{
		/**
		 * Does this object satisfy the criteria of the filter?
		 * 
		 * @param o
		 *        The object to test.
		 * @return true if the object is accepted by the filter, false if not.
		 */
		public boolean accept(Object o)
		{
			// we want to test only messages
			if (!(o instanceof Message))
			{
				return false;
			}

			// if the item cannot be read, reject it
			if (!unlockCheck(SECURE_READ, ((Message) o).getReference()))
			{
				return false;
			}

			// accept this one
			return true;

		} // accept

	} // MessagePermissionFilter

	protected class MessageSelectionFilter implements Filter
	{
		protected Time m_afterDate = null;

		protected String m_draftsForId = null;

		protected boolean m_pubViewOnly = false;

		public MessageSelectionFilter(Time afterDate, String draftsForId, boolean pubViewOnly)
		{
			m_afterDate = afterDate;
			m_draftsForId = draftsForId;
			m_pubViewOnly = pubViewOnly;
		}

		/**
		 * Does this object satisfy the criteria of the filter?
		 * 
		 * @param o
		 *        The object to test.
		 * @return true if the object is accepted by the filter, false if not.
		 */
		public boolean accept(Object o)
		{
			// we want to test only messages
			if (!(o instanceof Message))
			{
				return false;
			}

			if (m_afterDate != null)
			{
				if (!((Message) o).getHeader().getDate().after(m_afterDate))
				{
					return false;
				}
			}

			// if we want pub view only
			if (m_pubViewOnly)
			{
				if (((Entity) o).getProperties().getProperty(ResourceProperties.PROP_PUBVIEW) == null)
				{
					return false;
				}
			}

			// if we don't want all drafts
			if (!"*".equals(m_draftsForId))
			{
				if (isDraft((Entity) o))
				{
					// reject all drafts?
					if ((m_draftsForId == null) || (!getOwnerId((Entity) o).equals(m_draftsForId)))
					{
						return false;
					}
				}
			}

			// accept this one
			return true;
		}
	}

	protected String getSummaryFromHeader(Message item, MessageHeader header)
	{
            String newtext;
            String body = item.getBody();
            if ( body.length() > 50 ) body = body.substring(1,49);
            String newText = body + ", " + header.getFrom().getDisplayName() + ", " + header.getDate().toStringLocalFull();
	    return newText;
	}


        /**********************************************************************************************************************************************************************************************************************************************************
         * getSummary implementation
         *********************************************************************************************************************************************************************************************************************************************************/
        public Map getSummary(String channel, int items, int days)
                        throws org.sakaiproject.exception.IdUsedException, org.sakaiproject.exception.IdInvalidException,
                        org.sakaiproject.exception.PermissionException
        {
            long startTime = System.currentTimeMillis() - (days * 24l * 60l * 60l * 1000l);

            List messages = getMessages(channel, m_timeService.newTime(startTime), items, false, false, false);
            Iterator iMsg = messages.iterator();
            Time pubDate = null;
            String summaryText = null;
            Map m = new HashMap();
            while (iMsg.hasNext()) {
                Message item  = (Message) iMsg.next();
                MessageHeader header = item.getHeader();
                Time newTime = header.getDate();
                if ( pubDate == null || newTime.before(pubDate) ) pubDate = newTime;
                String newText = getSummaryFromHeader(item, header);
                if ( summaryText == null ) {
                    summaryText = newText;
                } else {
                    summaryText = summaryText + "<br>\r\n" + newText;
                }
            }
            if ( pubDate != null ) {
                m.put(Summary.PROP_PUBDATE, pubDate.toStringRFC822Local());
            }
            if ( summaryText != null ) {
                m.put(Summary.PROP_DESCRIPTION, summaryText);
                return m;
            }
            return null;
        }
        
    	/* (non-Javadoc)
    	 * @see org.sakaiproject.entity.api.EntitySummary#getSummarizableReference(java.lang.String)
    	 */
    	public String getSummarizableReference(String siteId, String toolIdentifier)
    	{
			return channelReference(
					siteId, SiteService.MAIN_CONTAINER);
    	}


	/**
	 * A filter that will reject messages which are draft or not public
	 */
	protected class PublicFilter implements Filter
	{
		/** The other filter to check with. May be null. */
		protected Filter m_filter = null;
		
		/** The site. May be public. */
		protected boolean m_isChannelPublic;

 		/**
 		 * Constructor
 		 *  @param filter
 		 *        The other filter we check with.
 		 * @param isChannelPublic
 		 *        Whether the channel is public or not
 		 */
		public PublicFilter(Filter filter, boolean isChannelPublic)
		{
			m_filter = filter;
			m_isChannelPublic = isChannelPublic;

		} // PublicFilter

		/**
		 * Does this object satisfy the criteria of the filter?
		 * 
		 * @return true if the object is accepted by the filter, false if not.
		 */
		public boolean accept(Object o)
		{
			// reject if not a public message
			if (o instanceof Message)
			{
				Message msg = (Message) o;
				if ((msg.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW) == null ||
						!msg.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW).equals(Boolean.TRUE.toString()))
						&& !m_isChannelPublic)
					return false;
				else if (msg.getHeader().getDraft())
					return false;
			}
				
			// next, use the specified filter, if present
			if (m_filter != null) 
				return m_filter.accept(o);

			return true;

		} // accept

	} // PublicFilter

	/** Class that starts a session to import XML data. */
	protected class NotifyThread extends Observable implements Runnable 
	{
		public void init(){}
		public void start(){}
		
		private Event m_event = null;
		
		//constructor
		NotifyThread(Event event)
		{
			m_event = event;
		}

		public void run()
		{
		    try
			{
		    	notify(m_event);
			}
		    catch(Exception e) {
		
		    }
		    finally
			{
				//clear any current bindings
				m_threadLocalManager.clear();
			}
		}
		
		/**
		 * Notify the channel that it has changed (i.e. when a message has changed)
		 * 
		 * @param event
		 *        The event that caused the update.
		 */
		protected void notify(Event event)
		{
			// notify observers, sending the tracking event to identify the change
			setChanged();
			notifyObservers(event);

		} // notify
		
	}//notifyThread
}
