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

package org.sakaiproject.message.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.message.api.MessageChannel;
import org.sakaiproject.message.api.MessageChannelEdit;
import org.sakaiproject.message.api.MessageEdit;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.message.api.MessageHeaderEdit;
import org.sakaiproject.message.api.MessageService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.StorageUser;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * BaseMessageService is a simple implementation of the CHEF MessageService as a Turbine service.
 * </p>
 * <p>
 * BaseMessageService simply stored messages in memory and has no persistence past the lifetime of the service object.
 * </p>
 * <p>
 * Services Used:
 * <ul>
 * <li>SecurityService</li>
 * <li>EventTrackingService</li>
 * </ul>
 * </p>
 * Note: for simplicity, we implement only the Edit versions of message and header - otherwise we'd want to inherit from two places in the extensions. A non-edit version is implemented by the edit version. -ggolden
 */
public abstract class BaseMessageService implements MessageService, StorageUser, CacheRefresher
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(BaseMessageService.class);

	/** A Storage object for persistent storage. */
	protected Storage m_storage = null;

	/** A Cache object for caching: channels keyed by reference. (if m_caching) */
	protected Cache m_channelCache = null;

	/** A bunch of caches for messages: keyed by channel id, the cache is keyed by message reference. (if m_caching) */
	protected Hashtable m_messageCaches = null;

	/**
	 * Access this service from the inner classes.
	 */
	protected BaseMessageService service()
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

	/** Configuration: cache, or not. */
	protected boolean m_caching = false;

	/**
	 * Configuration: set the locks-in-db
	 * 
	 * @param path
	 *        The storage path.
	 */
	public void setCaching(String value)
	{
		m_caching = new Boolean(value).booleanValue();
	}

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

			// make the channel cache
			if (m_caching)
			{
				m_channelCache = m_memoryService.newCache(this, getAccessPoint(true) + Entity.SEPARATOR + REF_TYPE_CHANNEL
						+ Entity.SEPARATOR);

				// make the table to hold the message caches
				m_messageCaches = new Hashtable();
			}

			M_log.info("init(): caching: " + m_caching);
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}

		// entity producer registration in the extension services

		// Functions are registered in the extension services

	} // init

	/**
	 * Destroy
	 */
	public void destroy()
	{
		if (m_caching)
		{
			m_channelCache.destroy();
			m_channelCache = null;

			m_messageCaches.clear();
			m_messageCaches = null;
		}

		m_storage.close();
		m_storage = null;

		M_log.info("destroy()");
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
		StringBuffer buf = new StringBuffer();

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
		if (!SecurityService.unlock(eventId(lock), resource))
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
		if (SecurityService.unlock(eventId(lock1), resource)) return true;

		// if the second is different, check that
		if ((lock1 != lock2) && (SecurityService.unlock(eventId(lock2), resource))) return true;

		return false;

	} // unlockCheck2

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
	protected boolean unlockCheck3(String lock1, String lock2, String lock3, String resource)
	{
		// check the first lock
		if (SecurityService.unlock(eventId(lock1), resource)) return true;

		// if the second is different, check that
		if ((lock1 != lock2) && (SecurityService.unlock(eventId(lock2), resource))) return true;

		// if the third is different, check that
		if ((lock1 != lock3) && (lock2 != lock3) && (SecurityService.unlock(eventId(lock3), resource))) return true;

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
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), eventId(lock), resource);
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
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), eventId(lock1) + "|" + eventId(lock2), resource);
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
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), eventId(lock1) + "|" + eventId(lock2) + "|"
					+ eventId(lock3), resource);
		}

	} // unlock3

	/**
	 * Return a list of all the defined channels.
	 * 
	 * @return a list of MessageChannel (or extension) objects (may be empty).
	 */
	public List getChannels()
	{
		List channels = new Vector();
		if ((!m_caching) || (m_channelCache == null) || (m_channelCache.disabled()))
		{
			channels = m_storage.getChannels();
			return channels;
		}

		// use the cache
		// if the cache is complete, use it
		if (m_channelCache.isComplete())
		{
			// get just the channels in the cache
			channels = m_channelCache.getAll();
		}

		// otherwise get all the channels from storage
		else
		{
			// Note: while we are getting from storage, storage might change. These can be processed
			// after we get the storage entries, and put them in the cache, and mark the cache complete.
			// -ggolden
			synchronized (m_channelCache)
			{
				// if we were waiting and it's now complete...
				if (m_channelCache.isComplete())
				{
					// get just the channels in the cache
					channels = m_channelCache.getAll();
					return channels;
				}

				// save up any events to the cache until we get past this load
				m_channelCache.holdEvents();

				channels = m_storage.getChannels();
				// update the cache, and mark it complete
				for (int i = 0; i < channels.size(); i++)
				{
					MessageChannel channel = (MessageChannel) channels.get(i);
					m_channelCache.put(channel.getReference(), channel);
				}

				m_channelCache.setComplete();

				// now we are complete, process any cached events
				m_channelCache.processEvents();
			}
		}

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
	 * Find the channel, in cache or info store - cache it if newly found.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return The channel, if found.
	 */
	protected MessageChannel findChannel(String ref)
	{
		if (ref == null) return null;

		MessageChannel channel = null;

		if ((!m_caching) || (m_channelCache == null) || (m_channelCache.disabled()))
		{
			// if we have done this already in this thread, use that
			channel = (MessageChannel) ThreadLocalManager.get(ref);
			if (channel == null)
			{
				channel = m_storage.getChannel(ref);

				// "cache" the channel in the current service in case they are needed again in this thread...
				if (channel != null)
				{
					ThreadLocalManager.set(ref, channel);
				}
			}

			return channel;
		}

		// use the cache
		// if we have it cached, use it (even if it's cached as a null, a miss)
		if (m_channelCache.containsKey(ref))
		{
			channel = (MessageChannel) m_channelCache.get(ref);
		}

		// if not in the cache, see if we have it in our info store
		else
		{
			channel = m_storage.getChannel(ref);

			// if so, cache it, even misses
			m_channelCache.put(ref, channel);
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

		((BaseMessageChannelEdit) channel).setEvent(SECURE_ADD);

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
				M_log.warn("commitEdit(): closed ChannelEdit", e);
			}
			return;
		}

		m_storage.commitChannel(edit);

		// track event (no notification)
		Event event = EventTrackingService.newEvent(eventId(((BaseMessageChannelEdit) edit).getEvent()), edit.getReference(), true,
				NotificationService.NOTI_NONE);

		EventTrackingService.post(event);

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
				M_log.warn("cancelChannelEdit(): closed MessageChannelEdit", e);
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
				M_log.warn("removeChannel(): closed ChannelEdit", e);
			}
			return;
		}

		// check security
		unlock(SECURE_REMOVE_ANY, channel.getReference());

		m_storage.removeChannel(channel);

		// track event
		Event event = EventTrackingService.newEvent(eventId(SECURE_REMOVE_ANY), channel.getReference(), true);
		EventTrackingService.post(event);

		// mark the channel as removed
		((BaseMessageChannelEdit) channel).setRemoved(event);

		// close the edit object
		((BaseMessageChannelEdit) channel).closeEdit();

		// remove any realm defined for this resource
		try
		{
			AuthzGroupService.removeAuthzGroup(AuthzGroupService.getAuthzGroup(channel.getReference()));
		}
		catch (AuthzPermissionException e)
		{
			M_log.warn("removeChannel: removing realm for : " + channel.getReference() + " : " + e);
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

		// check security on the message
		if (!allowGetMessage(channelReference(ref.getContext(), ref.getContainer()), ref.getReference()))
		{
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), eventId(SECURE_READ), ref.getReference());
		}

		// get the channel, no security check
		MessageChannel c = findChannel(channelReference(ref.getContext(), ref.getContainer()));
		if (c == null)
		{
			throw new IdUnusedException(ref.getContainer());
		}

		// get the message from the channel
		Message m = ((BaseMessageChannelEdit) c).findMessage(ref.getId());

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
		// Assume this reference is for a message

		// if the use has all_message permission for the channel/site, allow it
		if (unlockCheck(SECURE_ALL_GROUPS, channelRef))
		{
			return true;
		}

		// check the message
		return unlockCheck(SECURE_READ, msgRef);
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
	public List getMessages(String channelRef, Time afterDate, int limitedToLatest, boolean ascending, boolean includeDrafts,
			boolean pubViewOnly) throws PermissionException
	{
		// channel read security
		if (!pubViewOnly)
		{
			unlock(SECURE_READ, channelRef);
		}

		// get the channel, no security check
		MessageChannel c = findChannel(channelRef);
		if (c == null) return new Vector();

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
				draftsForId = SessionManager.getCurrentSessionUserId();
			}
		}

		if ((!m_caching) || (m_channelCache == null) || (m_channelCache.disabled()))
		{
			// get messages filtered by date and count and drafts, in descending (latest first) order
			List rv = m_storage.getMessages(c, afterDate, limitedToLatest, draftsForId, pubViewOnly);

			// if ascending, reverse
			if (ascending)
			{
				Collections.reverse(rv);
			}

			return rv;
		}

		// use the cache

		// get the messages
		List msgs = ((BaseMessageChannelEdit) c).findFilterMessages(
				new MessageSelectionFilter(afterDate, draftsForId, pubViewOnly), ascending);

		// sub-select count
		if ((limitedToLatest != 0) && (limitedToLatest < msgs.size()))
		{
			if (ascending)
			{
				msgs = msgs.subList(msgs.size() - limitedToLatest, msgs.size());
			}
			else
			{
				msgs = msgs.subList(0, limitedToLatest);
			}
		}

		return msgs;
	}

	/**
	 * Access a list of channel ids that are defined related to the context.
	 * 
	 * @param context
	 *        The context in which to search
	 * @return A List (String) of channel id for channels withing the context.
	 */
	public List getChannelIds(String context)
	{
		if ((!m_caching) || (m_channelCache == null) || (m_channelCache.disabled()))
		{
			return m_storage.getChannelIdsMatching(channelReference(context, ""));
		}

		// use the cache
		List channels = getChannels();
		List rv = new Vector();
		for (Iterator i = channels.iterator(); i.hasNext();)
		{
			MessageChannel channel = (MessageChannel) i.next();
			if (context.equals(channel.getContext()))
			{
				rv.add(channel.getId());
			}
		}

		return rv;
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
		return null;
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
			if (REF_TYPE_CHANNEL.equals(ref.getSubType()) || REF_TYPE_CHANNEL_GROUPS.equals(ref.getSubType()))
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
			if (REF_TYPE_CHANNEL.equals(ref.getSubType()) || REF_TYPE_CHANNEL_GROUPS.equals(ref.getSubType()))
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
				M_log.warn("getProperties(): unknown message ref subtype: " + ref.getSubType() + " in ref: " + ref.getReference());
		}
		catch (PermissionException e)
		{
			M_log.warn("getProperties(): " + e);
		}
		catch (IdUnusedException e)
		{
			// This just means that the resource once pointed to as an attachment or something has been deleted.
			// M_log.warn("getProperties(): " + e);
		}
		catch (NullPointerException e)
		{
			M_log.warn("getProperties(): " + e);
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
			if (REF_TYPE_CHANNEL.equals(ref.getSubType()) || REF_TYPE_CHANNEL_GROUPS.equals(ref.getSubType()))
			{
				rv = getChannel(ref.getReference());
			}

			// otherwise a message
			else if (REF_TYPE_MESSAGE.equals(ref.getSubType()))
			{
				rv = getMessage(ref);
			}

			// else try {throw new Exception();} catch (Exception e) {M_log.warn("getResource(): unknown message ref subtype: " + m_subType + " in ref: " + m_reference, e);}
			else
				M_log.warn("getResource(): unknown message ref subtype: " + ref.getSubType() + " in ref: " + ref.getReference());
		}
		catch (PermissionException e)
		{
			M_log.warn("getResource(): " + e);
		}
		catch (IdUnusedException e)
		{
			M_log.warn("getResource(): " + e);
		}
		catch (NullPointerException e)
		{
			M_log.warn("getResource(): " + e);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getEntityAuthzGroups(Reference ref)
	{
		// we could check that the type is one of the message services, but lets just assume it is so we don't need to know them here -ggolden

		Collection rv = new Vector();

		// for MessageService messages:
		// if access set to CHANNEL (or PUBLIC), use the message, channel and site authzGroups.
		// if access set to GROUPED, use the message, and the groups, but not the channel or site authzGroups.
		// for Channels, use the channel and site authzGroups.
		// for Channels-groups, use the channel, site, and also any site group in the context
		try
		{
			// for message
			if (REF_TYPE_MESSAGE.equals(ref.getSubType()))
			{
				// message
				rv.add(ref.getReference());

				// get the channel to get the message to get group information
				// TODO: check for efficiency, cache and thread local caching usage -ggolden
				boolean grouped = false;
				Collection groups = null;
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

				if (grouped)
				{
					// groups
					rv.addAll(groups);
				}

				// not grouped
				else
				{
					// channel
					rv.add(channelRef);

					// site
					ref.addSiteContextAuthzGroup(rv);
				}
			}

			// for channel, or the channel+groups
			else
			{
				// channel
				rv.add(channelReference(ref.getContext(), ref.getId()));

				// site
				ref.addSiteContextAuthzGroup(rv);

				// for the specialy marked channel-group, also add any site group's azg
				if (REF_TYPE_CHANNEL_GROUPS.equals(ref.getSubType()))
				{
					Site site = SiteService.getSite(ref.getContext());
					Collection groups = site.getGroups();

					// get a list of the group refs, which are authzGroup ids
					Collection groupRefs = new Vector();
					for (Iterator i = groups.iterator(); i.hasNext();)
					{
						Group group = (Group) i.next();
						rv.add(group.getReference());
					}
				}
			}
		}
		catch (Throwable e)
		{
			M_log.warn("getEntityAuthzGroups(): " + e);
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
			if (REF_TYPE_CHANNEL.equals(ref.getSubType()) || REF_TYPE_CHANNEL_GROUPS.equals(ref.getSubType()))
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
				M_log.warn("getUrl(): unknown message ref subtype: " + ref.getSubType() + " in ref: " + ref.getReference());
		}
		catch (PermissionException e)
		{
			M_log.warn("getUrl(): " + e);
		}
		catch (IdUnusedException e)
		{
			M_log.warn("getUrl(): " + e);
		}
		catch (NullPointerException e)
		{
			M_log.warn("getUrl(): " + e);
		}

		return url;
	}

	/**
	 * {@inheritDoc}
	 */
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		// prepare the buffer for the results log
		StringBuffer results = new StringBuffer();

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

			stack.pop();
		}
		catch (Exception any)
		{
			M_log.warn("archve: exception archiving messages for service: " + serviceName() + " channel: " + channelRef);
		}

		stack.pop();

		return results.toString();
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
		StringBuffer results = new StringBuffer();

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

// TODO: how to do discussion's channel category... also how did this ever work? -ggolden
//								if (element3.getTagName().equals("categories"))
//								{
//									NodeList children4 = element3.getChildNodes();
//									final int length4 = children4.getLength();
//									for (int i4 = 0; i4 < length4; i4++)
//									{
//										Node child4 = children4.item(i4);
//										if (child4.getNodeType() == Node.ELEMENT_NODE)
//										{
//											Element element4 = (Element) child4;
//											if (element4.getTagName().equals("category"))
//											{
//												MessageChannelEdit c = editChannel(channelRef);
//												String category = element4.getAttribute("name");
//												commitChannel(c);
//												((DiscussionChannel) c).addCategory(category);
//											}
//										}
//									}
//								}

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
												String newId = IdManager.createUuid();
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
												element4.setAttribute("draft", "true");
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
			M_log.warn("mergeMessages(): exception in handling " + serviceName() + " : ", any);
		}

		results.append("merging " + getLabel() + " channel " + channelRef + " (" + count + ") messages.\n");
		return results.toString();

	} // merge

	/**
	 * {@inheritDoc}
	 */
	public void syncWithSiteChange(Site site, EntityProducer.ChangeType change)
	{
		// nothing at this level, may be overriden in an extension class
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

	public class BaseMessageChannelEdit extends Observable implements MessageChannelEdit, SessionBindingListener
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
			notify(event);

			// now clear observers
			deleteObservers();

		} // setRemoved

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
			return getAccessPoint(false) + SEPARATOR + getId() + SEPARATOR; // %%% needs fixing re: context

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
		public Collection getGroupsAllowGetMessage()
		{
			return getGroupsAllowFunction(SECURE_READ);
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
			// EventTrackingService.post(EventTrackingService.newEvent(eventId(SECURE_READ), getReference(), false));

			return findFilterMessages(filter, ascending);

		} // getMessages

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
				throw new PermissionException(SessionManager.getCurrentSessionUserId(), eventId(SECURE_READ), messageReference(
						getReference(), messageId));
			}

			Message m = findMessage(messageId);

			if (m == null) throw new IdUnusedException(messageId);

			// track event
			// EventTrackingService.post(EventTrackingService.newEvent(eventId(SECURE_READ), m.getReference(), false));

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
			boolean channelCheck = MessageHeader.MessageAccess.CHANNEL == m.getHeader().getAccess();

			// if the use has all_message permission for the channel/site, allow it
			if (unlockCheck(SECURE_ALL_GROUPS, getReference()))
			{
				return true;
			}

			// if the message is not grouped, do the regular check (message, channel, site)
			if (channelCheck)
			{
				// is this the user's own?
				if (m.getHeader().getFrom().getId().equals(SessionManager.getCurrentSessionUserId()))
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

			// if grouped, check that the user has permissions in every group that the message uses
			else
			{
				for (Iterator i = m.getHeader().getGroups().iterator(); i.hasNext();)
				{
					String groupRef = (String) i.next();

					// is this the user's own?
					if (m.getHeader().getFrom().getId().equals(SessionManager.getCurrentSessionUserId()))
					{
						// own or any
						if (!unlockCheck2(fOwn, fAny, groupRef)) return false;
					}

					else
					{
						// just any
						if (!unlockCheck(fAny, groupRef)) return false;
					}
				}
			}

			return true;
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
			if (m.getHeader().getFrom().getId().equals(SessionManager.getCurrentSessionUserId()))
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
				throw new PermissionException(SessionManager.getCurrentSessionUserId(), eventId(function), m.getReference());
			}

			// ignore the cache - get the message with a lock from the info store
			MessageEdit msg = m_storage.editMessage(this, messageId);
			if (msg == null) throw new InUseException(messageId);

			((BaseMessageEdit) msg).setEvent(function);

			return msg;

		} // editMessage

		/**
		 * Commit the changes made to a MessageEdit object, and release the lock. The MessageEdit is disabled, and not to be used after this call.
		 * 
		 * @param user
		 *        The UserEdit object to commit.
		 */
		public void commitMessage(MessageEdit edit) throws PermissionException
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
		public void commitMessage(MessageEdit edit, int priority) throws PermissionException
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
					M_log.warn("commitEdit(): closed MessageEdit", e);
				}
				return;
			}

			// check permission - this checks if the message in its current form (grouped, channel, etc) is still valid for the end user to save
			String ownSecurityFunction = (((BaseMessageEdit) edit).getEvent().equals(SECURE_ADD) ? SECURE_ADD : SECURE_UPDATE_OWN);
			String anySecurityFunction = (((BaseMessageEdit) edit).getEvent().equals(SECURE_ADD) ? SECURE_ADD : SECURE_UPDATE_ANY);
			if (!allowEditMessage(edit, ownSecurityFunction, anySecurityFunction))
			{
				cancelMessage(edit);
				throw new PermissionException(SessionManager.getCurrentSessionUserId(),
						eventId(((BaseMessageEdit) edit).getEvent()), edit.getReference());
			}

			// update the properties
			// addLiveUpdateProperties(edit.getPropertiesEdit());//%%%

			// complete the edit
			m_storage.commitMessage(this, edit);

			// clear out any thread local caching of this message, since it has just changed
			ThreadLocalManager.set(edit.getReference(), null);

			// track event
			Event event = EventTrackingService.newEvent(eventId(((BaseMessageEdit) edit).getEvent()), edit.getReference(), true,
					priority);
			EventTrackingService.post(event);

			// channel notification
			notify(event);

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
					M_log.warn("commitEdit(): closed MessageEdit", e);
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
		 * check permissions for addMessage().
		 * 
		 * @return true if the user is allowed to addMessage(...), false if not.
		 */
		public boolean allowAddMessage()
		{
			// base the check for SECURE_ADD on the site, any of the site's groups, and the channel
			// if the user can SECURE_ADD anywhere in that mix, they can add a message
			// this stack is not the normal azg set for channels, so use a special refernce to get this behavior

			String specialReference = getAccessPoint(true) + Entity.SEPARATOR + REF_TYPE_CHANNEL_GROUPS + Entity.SEPARATOR
					+ m_context + Entity.SEPARATOR + m_id;

			// check security on the channel (throws if not permitted)
			return unlockCheck(SECURE_ADD, specialReference);

		} // allowAddMessage

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
				throw new PermissionException(SessionManager.getCurrentSessionUserId(), eventId(SECURE_ADD), getReference());
			}

			String id = null;
			// allocate a new unique message id, using the CHEF Service API cover
			id = IdManager.createUuid();

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
				msg.getHeaderEdit().setAccess(MessageHeader.MessageAccess.CHANNEL);
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
			return allowEditMessage(m, SECURE_REMOVE_OWN, SECURE_REMOVE_ANY);

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
					M_log.warn("removeMessage(String): null edit ", e);
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
				try
				{
					throw new Exception();
				}
				catch (Exception e)
				{
					M_log.warn("removeMessage(): closed MessageEdit", e);
				}
				return;
			}

			// pick the security function
			String function = null;
			if (message.getHeader().getFrom().getId().equals(SessionManager.getCurrentSessionUserId()))
			{
				// own or any
				function = SECURE_REMOVE_OWN;
			}
			else
			{
				// just any
				function = SECURE_REMOVE_ANY;
			}

			// securityCheck
			if (!allowRemoveMessage(message))
			{
				cancelMessage(message);
				throw new PermissionException(SessionManager.getCurrentSessionUserId(), eventId(function), message.getReference());
			}

			m_storage.removeMessage(this, message);

			// track event
			Event event = EventTrackingService.newEvent(eventId(function), message.getReference(), true);
			EventTrackingService.post(event);

			// channel notification
			notify(event);

			// close the edit object
			((BaseMessageEdit) message).closeEdit();

			// remove any realm defined for this resource
			try
			{
				AuthzGroupService.removeAuthzGroup(message.getReference());
			}
			catch (AuthzPermissionException e)
			{
				M_log.warn("removeMessage: removing realm for : " + message.getReference() + " : " + e);
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
		 * Notify the channel that it has changed (i.e. when a message has changed)
		 * 
		 * @param event
		 *        The event that caused the update.
		 */
		public void notify(Event event)
		{
			// notify observers, sending the tracking event to identify the change
			setChanged();
			notifyObservers(event);

		} // notify

		/**
		 * Find the message, in cache or info store - cache it if newly found.
		 * 
		 * @param messageId
		 *        The id of the message.
		 * @return The message, if found.
		 */
		protected Message findMessage(String messageId)
		{
			if ((!m_caching) || (m_channelCache == null) || (m_channelCache.disabled()))
			{
				// if we have "cached" the entire set of messages in the thread, get that and find our message there
				List msgs = (List) ThreadLocalManager.get(getReference() + ".msgs");
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
				Message m = (Message) ThreadLocalManager.get(messageReference(getReference(), messageId));

				// if not, get from storage and cache
				if (m == null)
				{
					m = m_storage.getMessage(this, messageId);

					// if we got one, cache it in the thread
					if (m != null)
					{
						ThreadLocalManager.set(m.getReference(), m);
					}
				}

				return m;
			}

			// use the cache
			Message m = null;

			// messages are cached with the full reference as key
			String key = messageReference(m_context, m_id, messageId);

			// find the message cache
			Cache msgCache = (Cache) m_messageCaches.get(getReference());
			if (msgCache == null)
			{
				synchronized (m_messageCaches)
				{
					// check again
					msgCache = (Cache) m_messageCaches.get(getReference());

					// if still not there, make one
					if (msgCache == null)
					{
						msgCache = m_memoryService.newCache(service(), messageReference(m_context, m_id, ""));
						m_messageCaches.put(getReference(), msgCache);
					}
				}
			}

			// if we have it cached, use it (even if it's cached as a null, a miss)
			if (msgCache.containsKey(key))
			{
				m = (Message) msgCache.get(key);
			}

			// if not in the cache, see if we have it in our info store
			else
			{
				m = m_storage.getMessage(this, messageId);

				// if so, cache it, even misses
				msgCache.put(key, m);
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
			if ((!m_caching) || (m_channelCache == null) || (m_channelCache.disabled()))
			{
				// if we have done this already in this thread, use that
				List msgs = (List) ThreadLocalManager.get(getReference() + ".msgs");
				if (msgs == null)
				{
					msgs = m_storage.getMessages(this);

					// "cache" the mesasge in the current service in case they are needed again in this thread...
					ThreadLocalManager.set(getReference() + ".msgs", msgs);
				}

				return msgs;
			}

			// use the cache
			List msgs = new Vector();

			// find the message cache
			Cache msgCache = (Cache) m_messageCaches.get(getReference());
			if (msgCache == null)
			{
				synchronized (m_messageCaches)
				{
					// check again
					msgCache = (Cache) m_messageCaches.get(getReference());

					// if still not there, make one
					if (msgCache == null)
					{
						msgCache = m_memoryService.newCache(service(), messageReference(m_context, m_id, ""));
						m_messageCaches.put(getReference(), msgCache);
					}
				}
			}

			// if the cache is complete, use it
			if (msgCache.isComplete())
			{
				// get just this channel's messages
				msgs = msgCache.getAll();
			}

			// otherwise get all the msgs from storage
			else
			{
				// Note: while we are getting from storage, storage might change. These can be processed
				// after we get the storage entries, and put them in the cache, and mark the cache complete.
				// -ggolden
				synchronized (msgCache)
				{
					// if we were waiting and it's now complete...
					if (msgCache.isComplete())
					{
						// get just this channel's messages
						msgs = msgCache.getAll();
					}
					else
					{
						// cache up any events to the cache until we get past this load
						msgCache.holdEvents();

						msgs = m_storage.getMessages(this);
						// update the cache, and mark it complete
						for (int i = 0; i < msgs.size(); i++)
						{
							Message msg = (Message) msgs.get(i);
							msgCache.put(msg.getReference(), msg);
						}

						msgCache.setComplete();

						// now we are complete, process any cached events
						msgCache.processEvents();
					}
				}
			}

			return msgs;

		} // findMessages

		/**
		 * Find messages, sort, and filter.
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

			// check for the allowed groups of the current end use if we need it, and only once
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
						allowedGroups = getGroupsAllowGetMessage();
					}

					// reject if there is no intersection
					if (!isIntersectionGroupRefsToGroups(msgGroups, allowedGroups)) continue;
				}

				// reject if the filter rejects
				if ((filter != null) && (!filter.accept(msg))) continue;

				// if not rejected, keep
				filtered.add(msg);
			}

			return filtered;

		} // findFilterMessages

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
		 */
		protected Collection getGroupsAllowFunction(String function)
		{
			Collection rv = new Vector();

			try
			{
				// get the channel's site's groups
				Site site = SiteService.getSite(m_context);
				Collection groups = site.getGroups();

				// if the user has annc.allgrp for the channel (channel, site), and the function for the channel (channel,site), select all site groups
				if (unlockCheck(SECURE_ALL_GROUPS, getReference()))
				{
					return groups;
				}

				// otherwise, check the groups for function

				// get a list of the group refs, which are authzGroup ids
				Collection groupRefs = new Vector();
				for (Iterator i = groups.iterator(); i.hasNext();)
				{
					Group group = (Group) i.next();
					groupRefs.add(group.getReference());
				}

				// ask the authzGroup service to filter them down based on function
				groupRefs = AuthzGroupService.getAuthzGroupsIsAllowed(UserDirectoryService.getCurrentUser().getId(),
						eventId(function), groupRefs);

				// pick the Group objects from the site's groups to return, those that are in the groupRefs list
				for (Iterator i = groups.iterator(); i.hasNext();)
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

		/******************************************************************************************************************************************************************************************************************************************************
		 * SessionBindingListener implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		public void valueBound(SessionBindingEvent event)
		{
		}

		public void valueUnbound(SessionBindingEvent event)
		{
			if (M_log.isDebugEnabled()) M_log.debug("valueUnbound()");

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
							// %%% JANDERSE - Handle conversion from plaintext messages to formatted text messages
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

			m_channel = null;

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
			return m_channel.getUrl() + getId();

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
			if (M_log.isDebugEnabled()) M_log.debug("valueUnbound()");

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

		/** The User who sent the message to the channel. */
		protected User m_from = null;

		/** The attachments - dereferencer objects. */
		protected List m_attachments = null;

		/** The draft status for the message. */
		protected boolean m_draft = false;

		/** The Collection of groups (authorization group id strings). */
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
			m_date = TimeService.newTime();
			try
			{
				m_from = UserDirectoryService.getUser(SessionManager.getCurrentSessionUserId());
			}
			catch (UserNotDefinedException e)
			{
				m_from = UserDirectoryService.getAnonymousUser();
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
			m_date = TimeService.newTime(other.getDate().getTime());
			m_from = other.getFrom();
			m_draft = other.getDraft();
			m_access = other.getAccess();

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
				m_from = UserDirectoryService.getUser(el.getAttribute("from"));
			}
			catch (UserNotDefinedException e)
			{
				m_from = UserDirectoryService.getAnonymousUser();
			}
			m_date = TimeService.newTimeGmt(el.getAttribute("date"));
			try
			{
				m_draft = new Boolean(el.getAttribute("draft")).booleanValue();
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
		 * @inheritDoc
		 */
		public void addGroup(Group group) throws PermissionException
		{
			if (group == null)
				throw new PermissionException(SessionManager.getCurrentSessionUserId(), eventId(SECURE_ADD), "null");

			// does the current user have SECURE_ADD permission in this group's authorization group, or SECURE_ALL_GROUPS in the channel?
			if (!unlockCheck(SECURE_ADD, group.getReference()))
			{
				if (!unlockCheck(SECURE_ALL_GROUPS, ((BaseMessageEdit) m_message).m_channel.getReference()))
				{
					throw new PermissionException(SessionManager.getCurrentSessionUserId(), eventId(SECURE_ADD), group
							.getReference());
				}
			}

			if (!m_groups.contains(group.getReference())) m_groups.add(group.getReference());
		}

		/**
		 * @inheritDoc
		 */
		public void removeGroup(Group group) throws PermissionException
		{
			if (group == null)
				throw new PermissionException(SessionManager.getCurrentSessionUserId(), eventId(SECURE_ADD), "null");

			// does the current user have SECURE_ADD permission in this group's authorization group, or SECURE_ALL_GROUPS in the channel?
			if (!unlockCheck(SECURE_ADD, group.getReference()))
			{
				if (!unlockCheck(SECURE_ALL_GROUPS, ((BaseMessageEdit) m_message).m_channel.getReference()))
				{
					throw new PermissionException(SessionManager.getCurrentSessionUserId(), eventId(SECURE_ADD), group
							.getReference());
				}
			}

			if (m_groups.contains(group.getReference())) m_groups.remove(group.getReference());
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
		public void setAccess(MessageAccess access)
		{
			m_access = access;
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
	 * CacheRefresher implementation (no container)
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Get a new value for this key whose value has already expired in the cache.
	 * 
	 * @param key
	 *        The key whose value has expired and needs to be refreshed.
	 * @param oldValue
	 *        The old exipred value of the key.
	 * @param event
	 *        The event which triggered this refresh.
	 * @return a new value for use in the cache for this key; if null, the entry will be removed.
	 */
	public Object refresh(Object key, Object oldValue, Event event)
	{
		Object rv = null;

		// key is a reference
		Reference ref = m_entityManager.newReference((String) key);

		// get from storage only (not cache!)

		// for a message
		if (REF_TYPE_MESSAGE.equals(ref.getSubType()))
		{
			if (M_log.isDebugEnabled())
				M_log.debug("refresh(): key " + key + " channel id : " + ref.getContext() + "/" + ref.getContainer()
						+ " message id : " + ref.getId());

			// get channel (Note: from the cache is ok)
			MessageChannel channel = findChannel(channelReference(ref.getContext(), ref.getContainer()));

			// get the message (Note: not from cache! but only from storage)
			if (channel != null)
			{
				rv = m_storage.getMessage(channel, ref.getId());
			}
		}

		// for a channel
		else
		{
			if (M_log.isDebugEnabled()) M_log.debug("refresh(): key " + key + " channel id : " + ref.getReference());

			// return the channel from channel getId() (Note: not from cache! but only from storage)
			rv = m_storage.getChannel(ref.getReference());
		}

		return rv;

	} // refresh

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
}
