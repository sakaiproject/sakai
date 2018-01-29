/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.event.impl;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.*;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.SingleStorageUser;

/**
 * <p>
 * BaseNotificationService ...
 * </p>
 */
@Slf4j
public abstract class BaseNotificationService implements NotificationService, Observer, SingleStorageUser, CacheRefresher
{
	/** Storage manager for this service. */
	protected Storage m_storage = null;

	/** The initial portion of a relative access point URL. */
	protected String m_relativeAccessPoint = null;

	/** Transient notifications (NotificationEdit). */
	protected List m_transients = null;
	/** Configuration: make the email notifications To: reply-able. */
	protected boolean m_emailsToReplyable = false;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Abstractions, etc.
	 *********************************************************************************************************************************************************************************************************************************************************/
	/** Configuration: make the email notifications From: reply-able. */
	protected boolean m_emailsFromReplyable = false;
	private ComponentManager componentManager = org.sakaiproject.component.cover.ComponentManager.getInstance();

	/**
	 * Construct storage for this service.
	 */
	protected abstract Storage newStorage();

	/**
	 * Does the resource reference match the filter?
	 *
	 * @param filter
	 *        The resource reference filter.
	 * @param ref
	 *        The resource reference string.
	 * @return true if the filter matches the ref, false if not.
	 */
	protected boolean match(String filter, String ref)
	{
		if (filter == null) return true;
		if (filter.length() == 0) return true;

		if (ref.startsWith(filter)) return true;

		return false;
	}

	/**
	 * Access the partial URL that forms the root of resource URLs.
	 *
	 * @param relative
	 *        if true, form within the access path only (i.e. starting with /content)
	 * @return the partial URL that forms the root of resource URLs.
	 */
	protected String getAccessPoint(boolean relative)
	{
		return (relative ? "" : serverConfigurationService().getAccessUrl()) + m_relativeAccessPoint;
	}

	/**
	 * @return the ComponentManager collaborator
	 */
	protected ComponentManager getComponentManager() {
		return componentManager;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Access the notification id extracted from a notification reference.
	 *
	 * @param ref
	 *        The notification reference string.
	 * @return The the notification id extracted from a notification reference.
	 */
	protected String notificationId(String ref)
	{
		String start = getAccessPoint(true) + Entity.SEPARATOR;
		int i = ref.indexOf(start);
		if (i == -1) return ref;
		String id = ref.substring(i + start.length());
		return id;
	}

	/**
	 * Access the external URL which can be used to access the resource from outside the system.
	 *
	 * @param id
	 *        The notification id.
	 * @return The the external URL which can be used to access the resource from outside the system.
	 */
	protected String notificationUrl(String id)
	{
		return getAccessPoint(false) + Entity.SEPARATOR + id;
	}

	/**
	 * @return the EventTrackingService collaborator.
	 */
	protected abstract EventTrackingService eventTrackingService();

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected abstract ServerConfigurationService serverConfigurationService();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Configuration
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the IdManager collaborator.
	 */
	protected abstract IdManager idManager();

	/**
	 * @return the MemoryService collaborator
	 */
	protected abstract MemoryService memoryService();

	/**
	 * Configuration: set reply-able status for email notifications in the To:.
	 *
	 * @param value
	 *        The setting
	 */
	public void setEmailToReplyable(boolean value)
	{
        log.warn("Use of this setter (emailToReplyable) is deprecated: use notify.email.to.replyable instead");
		m_emailsToReplyable = value;
	}

	/**
	 * Configuration: set reply-able status for email notifications in the From:.
	 * 
	 * @param value
	 *        The setting
	 */
	public void setEmailFromReplyable(boolean value)
	{
	    log.warn("Use of this setter (emailFromReplyable) is deprecated: use notify.email.from.replyable instead");
		m_emailsFromReplyable = value;
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
			// prepare for transients
			m_transients = new Vector();

			m_relativeAccessPoint = REFERENCE_ROOT;

			log.info(this + ".init() started");

			// construct storage and read
			m_storage = newStorage();
			m_storage.open();

			// start watching the events - only those generated on this server, not those from elsewhere
			eventTrackingService().addLocalObserver(this);

			// set these from real sakai config values
			m_emailsFromReplyable = serverConfigurationService().getBoolean("notify.email.from.replyable", false);
            m_emailsToReplyable = serverConfigurationService().getBoolean("notify.email.to.replyable", false);

			log.info(this + ".init() complete");
		}
		catch (Exception t)
		{
			log.warn(this + ".init(): ", t);
		}
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		// if we are not in a global shutdown, remove my event notification registration
		if (!getComponentManager().hasBeenClosed())
		{
			eventTrackingService().deleteObserver(this);
		}

		// clean up storage
		m_storage.close();
		m_storage = null;

		// clean up transients
		m_transients.clear();
		m_transients = null;

		log.info(this + ".destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * NotificationService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public NotificationEdit addNotification()
	{
		// check security (throws if not permitted)
		// unlock(SECURE_ADD_NOTIFICATION, notificationReference(id));

		// get a new unique id
		String id = idManager().createUuid();

		// reserve a notification with this id from the info store - if it's in use, this will return null
		NotificationEdit notification = m_storage.put(id);
		/*
		 * if (notification == null) { throw new IdUsedException(id); }
		 */

		((BaseNotificationEdit) notification).setEvent(SECURE_ADD_NOTIFICATION);

		return notification;
	}

	/**
	 * @inheritDoc
	 */
	public NotificationEdit addTransientNotification()
	{
		// the id is not unique, and not really used
		String id = "transient";

		// create an object, not through storage
		NotificationEdit notification = new BaseNotificationEdit(id);

		// remember it
		m_transients.add(notification);

		// no event, no other cluster server knows about it - it's transient and local
		return notification;
	}

	/**
	 * @inheritDoc
	 */
	public Notification getNotification(String id) throws NotificationNotDefinedException
	{
		Notification notification = m_storage.get(id);

		// if not found
		if (notification == null) throw new NotificationNotDefinedException(id);

		return notification;
	}

	/**
	 * @inheritDoc
	 */
	public String notificationReference(String id)
	{
		return getAccessPoint(true) + Entity.SEPARATOR + id;
	}

	/**
	 * @inheritDoc
	 */
	public NotificationEdit editNotification(String id) throws NotificationNotDefinedException, NotificationLockedException
	{
		// check security (throws if not permitted)
		// unlock(SECURE_UPDATE_NOTIFICATION, notificationReference(id));

		// check for existance
		if (!m_storage.check(id))
		{
			throw new NotificationNotDefinedException(id);
		}

		// ignore the cache - get the notification with a lock from the info store
		NotificationEdit notification = m_storage.edit(id);
		if (notification == null) throw new NotificationLockedException(id);

		((BaseNotificationEdit) notification).setEvent(SECURE_UPDATE_NOTIFICATION);

		return notification;
	}

	/**
	 * @inheritDoc
	 */
	public void commitEdit(NotificationEdit notification)
	{
		// check for closed edit
		if (!notification.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				log.warn(this + ".commitEdit(): closed NotificationEdit", e);
			}
			return;
		}

		// update the properties
		// addLiveUpdateProperties(notification.getPropertiesEdit());

		// complete the edit
		m_storage.commit(notification);

		// track it
		eventTrackingService().post(
				eventTrackingService()
						.newEvent(((BaseNotificationEdit) notification).getEvent(), notification.getReference(), true));

		// close the edit object
		((BaseNotificationEdit) notification).closeEdit();
	}

	/**
	 * @inheritDoc
	 */
	public void cancelEdit(NotificationEdit notification)
	{
		// check for closed edit
		if (!notification.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				log.warn(this + ".cancelEdit(): closed NotificationEdit", e);
			}
			return;
		}

		// release the edit lock
		m_storage.cancel(notification);

		// close the edit object
		((BaseNotificationEdit) notification).closeEdit();
	}

	/**
	 * @inheritDoc
	 */
	public void removeNotification(NotificationEdit notification)
	{
		// check for closed edit
		if (!notification.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				log.warn(this + ".removeNotification(): closed NotificationEdit", e);
			}
			return;
		}

		// check security (throws if not permitted)
		// unlock(SECURE_REMOVE_NOTIFICATION, notification.getReference());

		// complete the edit
		m_storage.remove(notification);

		// track it
		eventTrackingService().post(eventTrackingService().newEvent(SECURE_REMOVE_NOTIFICATION, notification.getReference(), true));

		// close the edit object
		((BaseNotificationEdit) notification).closeEdit();
	}

	/**
	 * @inheritDoc
	 */
	public List getNotifications(String function)
	{
		List notifications = m_storage.getAll(function);

		// if none found in storage
		if (notifications == null)
		{
			notifications = new Vector();
		}

		// add transients
		for (Iterator it = m_transients.iterator(); it.hasNext();)
		{
			Notification notification = (Notification) it.next();
			if (notification.containsFunction(function))
			{
				notifications.add(notification);
			}
		}

		return notifications;
	}

	/**
	 * @inheritDoc
	 */
	public Notification findNotification(String function, String filter)
	{
		// start with all those for this function (just 'cause we have a nice method to get them -ggolden)
		List notifications = getNotifications(function);
		for (Iterator iNotifications = notifications.iterator(); iNotifications.hasNext();)
		{
			Notification notification = (Notification) iNotifications.next();
			if (notification.getResourceFilter().equals(filter))
			{
				return notification;
			}
		}

		return null;
	}
	
	/**
	 * @inheritDoc
	 */
	public List<Notification> findNotifications(String function, String filter)
	{
		List<Notification> notificationsFound = new ArrayList<Notification>();
		// start with all those for this function (just 'cause we have a nice method to get them -ggolden)
		List notifications = getNotifications(function);
		for (Iterator iNotifications = notifications.iterator(); iNotifications.hasNext();)
		{
			Notification notification = (Notification) iNotifications.next();
			if (notification.getResourceFilter().startsWith(filter) && !notificationsFound.contains(notification))
			{
				notificationsFound.add(notification);
			}
		}

		return notificationsFound;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isNotificationToReplyable()
	{
		return this.m_emailsToReplyable;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isNotificationFromReplyable()
	{
		return this.m_emailsFromReplyable;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Observer implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * This method is called whenever the observed object is changed. An application calls an <tt>Observable</tt> object's <code>notifyObservers</code> method to have all the object's observers notified of the change. default implementation is to
	 * cause the courier service to deliver to the interface controlled by my controller. Extensions can override.
	 * 
	 * @param o
	 *        the observable object.
	 * @param arg
	 *        an argument passed to the <code>notifyObservers</code> method.
	 */
	public void update(Observable o, Object arg)
	{
		// arg is Event
		if (!(arg instanceof Event)) return;
		Event event = (Event) arg;

		// check the event function against the functions we have notifications watching for
		String function = event.getEvent();

		// for each notification watching for this event
		List notifications = getNotifications(function);
		for (Iterator it = notifications.iterator(); it.hasNext();)
		{
			Notification notification = (Notification) it.next();

			// if the resource matches the notification's resource filter
			if (match(notification.getResourceFilter(), event.getResource()))
			{
				// cause the notification to run
				notification.notify(event);
			}
		}

	} // update

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
		return new BaseNotification(id);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * StorageUser implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

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
		return new BaseNotification(element);
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
		return new BaseNotification((Notification) other);
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
		BaseNotificationEdit e = new BaseNotificationEdit(id);
		e.activate();
		return e;
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
		BaseNotificationEdit e = new BaseNotificationEdit(element);
		e.activate();
		return e;
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
		BaseNotificationEdit e = new BaseNotificationEdit((Notification) other);
		e.activate();
		return e;
	}

	/**
	 * Collect the fields that need to be stored outside the XML (for the resource).
	 *
	 * @return An array of field values to store in the record outside the XML (for the resource).
	 */
	public Object[] storageFields(Entity r)
	{
		return null;
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
		return null;
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
		return null;
	}

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
		// key is a reference, but our storage wants an id
		String id = notificationId((String) key);

		// get this from storage
		Notification notification = m_storage.get(id);

		if (log.isDebugEnabled()) log.debug(this + ".refresh(): " + key + " : " + id);

		return notification;

	} // refresh
	
	

	/**********************************************************************************************************************************************************************************************************************************************************
	 * CacheRefresher implementation (no container)
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected interface Storage
	{
		/**
		 * Open and be ready to read / write.
		 */
		public void open();

		/**
		 * Close.
		 */
		public void close();

		/**
		 * Check if a notification by this id exists.
		 *
		 * @param id
		 *        The notification id.
		 * @return true if a nitificaion by this id exists, false if not.
		 */
		public boolean check(String id);

		/**
		 * Add a new notification with this id.
		 *
		 * @param id
		 *        The notification id.
		 * @return The locked notification with this id, or null if in use.
		 */
		public NotificationEdit put(String id);

		/**
		 * Get the notification with this id, or null if not found.
		 *
		 * @param id
		 *        The notification id.
		 * @return The notification with this id, or null if not found.
		 */
		public Notification get(String id);

		/**
		 * Get a List of all the notifications that are interested in this Event function.
		 *
		 * @param function
		 *        The Event function
		 * @return The List (Notification) of all the notifications that are interested in this Event function.
		 */
		public List getAll(String function);

		/**
		 * Get a List of all notifications.
		 *
		 * @return The List (Notification) of all notifications.
		 */
		public List getAll();

		/**
		 * Get a lock on the notification with this id, or null if a lock cannot be gotten.
		 *
		 * @param id
		 *        The user id.
		 * @return The locked Notification with this id, or null if this records cannot be locked.
		 */
		public NotificationEdit edit(String id);

		/**
		 * Commit the changes and release the lock.
		 *
		 * @param user
		 *        The notification to commit.
		 */
		public void commit(NotificationEdit notification);

		/**
		 * Cancel the changes and release the lock.
		 *
		 * @param user
		 *        The notification to commit.
		 */
		public void cancel(NotificationEdit notification);

		/**
		 * Remove this notification.
		 *
		 * @param user
		 *        The notification to remove.
		 */
		public void remove(NotificationEdit notification);

	} // Storage

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Notification implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseNotification implements Notification
	{
		/** The Event(s) function we are watching for. */
		protected List m_functions = null;

		/** The resource reference filter. */
		protected String m_filter = null;

		/** The resource id. */
		protected String m_id = null;

		/** The resource properties. */
		protected ResourcePropertiesEdit m_properties = null;

		/** The action helper class. */
		protected NotificationAction m_action = null;

		/**
		 * Construct.
		 * 
		 * @param id
		 *        The id to use.
		 */
		public BaseNotification(String id)
		{
			// generate a new id
			m_id = id;

			// setup for properties
			m_properties = new BaseResourcePropertiesEdit();

			// setup for functions
			m_functions = new Vector();
		}

		/**
		 * @inheritDoc
		 */
		public BaseNotification(Notification other)
		{
			setAll(other);
		}

		/**
		 * @inheritDoc
		 */
		public BaseNotification(Element el)
		{
			// setup for properties
			m_properties = new BaseResourcePropertiesEdit();

			// setup for functions
			m_functions = new Vector();

			m_id = el.getAttribute("id");

			// the first function
			String func = StringUtils.trimToNull(el.getAttribute("function"));
			if (func != null)
			{
				m_functions.add(func);
			}

			m_filter = StringUtils.trimToNull(el.getAttribute("filter"));

			// the children (properties, action helper)
			NodeList children = el.getChildNodes();
			final int length = children.getLength();
			for (int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element element = (Element) child;

				// look for properties
				if (element.getTagName().equals("properties"))
				{
					// re-create properties
					m_properties = new BaseResourcePropertiesEdit(element);
				}

				// look for the helper element
				else if (element.getTagName().equals("action"))
				{
					// the class name
					String className = StringUtils.trimToNull(element.getAttribute("class"));
					if (className != null)
					{
						// create the class
						try
						{
							Class<?> actionClass;
							try {
								actionClass = Class.forName(className);
							} catch (ClassNotFoundException cnfe) {
								// we're trying to access a class not in the event pack's classloader
								// So ask the ComponentManager
								Object obj = getComponentManager().get(className);
								if (obj == null) throw new ClassNotFoundException("Cannot reconstitute the NotificationAction named as " + className);
								else actionClass = obj.getClass();
							}
								
							m_action = (NotificationAction) actionClass.newInstance();

							// let it pick up it's settings
							m_action.set(element);
						}
						catch (Exception e)
						{
							log.warn(this + " exception creating action helper: " + e.toString());
						}
					}
				}
				else if (element.getTagName().equals("function"))
				{
					func = StringUtils.trimToNull(element.getAttribute("id"));
					m_functions.add(func);
				}
			}
		}

		/**
		 * @inheritDoc
		 */
		protected void setAll(Notification other)
		{
			BaseNotification bOther = (BaseNotification) other;
			m_id = bOther.m_id;
			m_filter = bOther.m_filter;

			m_properties = new BaseResourcePropertiesEdit();
			m_properties.addAll(bOther.m_properties);

			m_functions = new Vector();
			m_functions.addAll(bOther.m_functions);

			if (bOther.m_action != null)
			{
				m_action = bOther.m_action.getClone();
			}
		}

		/**
		 * @inheritDoc
		 */
		public void notify(Event event)
		{
			if (m_action != null)
			{
				m_action.notify(this, event);
			}
		}

		/**
		 * @inheritDoc
		 */
		public String getFunction()
		{
			return (String) m_functions.get(0);
		}

		/**
		 * @inheritDoc
		 */
		public String getResourceFilter()
		{
			return m_filter;
		}

		/**
		 * @inheritDoc
		 */
		public List getFunctions()
		{
			List rv = new Vector();
			rv.addAll(m_functions);

			return rv;
		}

		/**
		 * @inheritDoc
		 */
		public boolean containsFunction(String function)
		{
			return m_functions.contains(function);
		}

		/**
		 * @inheritDoc
		 */
		public NotificationAction getAction()
		{
			return m_action;
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl()
		{
			return notificationUrl(m_id);
		}

		/**
		 * @inheritDoc
		 */
		public String getReference()
		{
			return notificationReference(m_id);
		}

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
		 * Access the id of the resource.
		 * 
		 * @return The id.
		 */
		public String getId()
		{
			return m_id;
		}

		/**
		 * Access the resource's properties.
		 * 
		 * @return The resource's properties.
		 */
		public ResourceProperties getProperties()
		{
			return m_properties;
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
			Element notification = doc.createElement("notification");
			if (stack.isEmpty())
			{
				doc.appendChild(notification);
			}
			else
			{
				((Element) stack.peek()).appendChild(notification);
			}

			stack.push(notification);

			notification.setAttribute("id", getId());

			// first function
			if (m_functions.size() >= 1)
			{
				notification.setAttribute("function", (String) m_functions.get(0));
			}

			if (m_filter != null) notification.setAttribute("filter", m_filter);

			// properties
			m_properties.toXml(doc, stack);

			// action
			if (m_action != null)
			{
				Element action = doc.createElement("action");
				notification.appendChild(action);
				action.setAttribute("class", m_action.getClass().getName());
				m_action.toXml(action);
			}

			// more functions
			if (m_functions.size() > 1)
			{
				for (int i = 1; i < m_functions.size(); i++)
				{
					String func = (String) m_functions.get(i);
					Element funcEl = doc.createElement("function");
					notification.appendChild(funcEl);
					funcEl.setAttribute("id", func);
				}
			}
			stack.pop();

			return notification;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
	            return true;
	        }
	        if (!(obj instanceof BaseNotification)) {
	            return false;
	        }
	        BaseNotification other = (BaseNotification) obj;
	        return this.getId().equals(other.getId());
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * NotificationEdit implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseNotificationEdit extends BaseNotification implements NotificationEdit, SessionBindingListener
	{
		/** The event code for this edit. */
		protected String m_event = null;

		/** Active flag. */
		protected boolean m_active = false;

		/**
		 * Construct.
		 * 
		 * @param id
		 *        The notification id.
		 */
		public BaseNotificationEdit(String id)
		{
			super(id);

		} // BaseNotificationEdit

		/**
		 * Construct from an existing definition, in xml.
		 * 
		 * @param el
		 *        The message in XML in a DOM element.
		 */
		public BaseNotificationEdit(Element el)
		{
			super(el);

		} // BaseNotificationEdit

		/**
		 * Construct from another Notification.
		 * 
		 * @param notification
		 *        The other notification to copy values from.
		 */
		public BaseNotificationEdit(Notification other)
		{
			super(other);

		} // BaseNotificationEdit

		/**
		 * Clean up.
		 */
		protected void finalize()
		{
			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancelEdit(this);
			}

		} // finalize

		/**
		 * Set the Event function, clearing any that have already been set.
		 * 
		 * @param event
		 *        The Event function to watch for.
		 */
		public void setFunction(String function)
		{
			m_functions.clear();
			m_functions.add(function);

		} // setFunction

		/**
		 * Add another Event function.
		 * 
		 * @param event
		 *        Another Event function to watch for.
		 */
		public void addFunction(String function)
		{
			m_functions.add(function);

		} // addFunction

		/**
		 * Set the resource reference filter.
		 * 
		 * @param filter
		 *        The resource reference filter.
		 */
		public void setResourceFilter(String filter)
		{
			m_filter = filter;

		} // setResourceFilter

		/**
		 * Set the action helper that handles the notify() action.
		 * 
		 * @param action
		 *        The action helper that handles the notify() action.
		 */
		public void setAction(NotificationAction action)
		{
			m_action = action;

		} // setAction

		/**
		 * Take all values from this object.
		 * 
		 * @param other
		 *        The notification object to take values from.
		 */
		protected void set(Notification other)
		{
			setAll(other);

		} // set

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
		protected void activate()
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
			if (log.isDebugEnabled()) log.debug(this + ".valueUnbound()");

			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancelEdit(this);
			}

		} // valueUnbound

	} // BaseNotificationEdit

} // BaseNotificationService
