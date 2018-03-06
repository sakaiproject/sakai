/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.presence.impl;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Element;

import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.courier.api.PresenceUpdater;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.presence.api.PresenceService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * <p>
 * Implements the PresenceService, all but a Storage model.
 * </p>
 */
@Slf4j
public abstract class BasePresenceService implements PresenceService, PresenceUpdater
{

	/** SessionState key. */
	protected final static String SESSION_KEY = "sakai.presence.service";

	/** Storage. */
	protected Storage m_storage = null;

	/**
	 * Allocate a new storage object.
	 * 
	 * @return A new storage object.
	 */
	protected abstract Storage newStorage();

	/** The maintenance. */
	protected Maintenance m_maintenance = null;


	/**********************************************************************************************************************************************************************************************************************************************************
	 * Constructors, Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Dependency: SessionManager */
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

	/** Dependency: UsageSessionService */
	protected UsageSessionService m_usageSessionService = null;

	/**
	 * Dependency: UsageSessionService.
	 * 
	 * @param service
	 *        The UsageSessionService.
	 */
	public void setUsageSessionService(UsageSessionService service)
	{
		m_usageSessionService = service;
	}

	/** Dependency: UserDirectoryService */
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

	/** Dependency: EventTrackingService */
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

	/** Dependency: PrivacyManager */
	protected PrivacyManager m_privacyManager = null;

	/**
	 * Dependency: PrivacyManager.
	 * 
	 * @param service
	 * 			The PrivacyManager.
	 */
	public void setPrivacyManager(PrivacyManager service) {
		m_privacyManager = service;
	}

	/** Configuration: default value in seconds till a non-refreshed presence entry times out. */
	protected int m_timeout = 60;

	private NotificationEdit notification;

	private NotificationService notificationService;

	/**
	 * Configuration: SECONDS till a non-refreshed presence entry times out.
	 * 
	 * @param value
	 *        timeout seconds.
	 */
	public void setTimeoutSeconds(String value)
	{
		try
		{
			m_timeout = Integer.parseInt(value);
		}
		catch (Exception ignore)
		{
		}
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
			// storage
			m_storage = newStorage();

			// start the maintenance thread
			m_maintenance = new Maintenance();
			m_maintenance.start();

			log.info("init()");
			
			// register a transient notification for resources
			notification = notificationService.addTransientNotification();

			// add all the functions that are registered to trigger search index
			// modification

			notification.setFunction(UsageSessionService.EVENT_LOGOUT);

			// set the action
			notification.setAction(new NotificationAction(){
				
				public NotificationAction getClone()
				{
					return null;
				}

				public void notify(Notification notification, Event event)
				{
					removeSessionPresence(event.getSessionId());					
				}

				public void set(Element el)
				{					
				}

				public void set(NotificationAction other)
				{
				}

				public void toXml(Element el)
				{
				}
				
			});


		}
		catch (Exception t)
		{
			log.warn("init(): ", t);
		}
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		m_storage = null;
		log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * PresenceService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public String presenceReference(String id)
	{
		return REFERENCE_ROOT + Entity.SEPARATOR + id;

	} // presenceReference

	/**
	 * {@inheritDoc}
	 */
	protected String presenceId(String ref)
	{
		String start = presenceReference("");
		int i = ref.indexOf(start);
		if (i == -1) return ref;
		String id = ref.substring(i + start.length());
		return id;

	} // presenceId

	/**
	 * {@inheritDoc}
	 */
	public void setPresence(String locationId)
	{
		setPresence(locationId, m_timeout);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setPresence(String locationId, int timeout)
	{
		if (locationId == null) return;

		if (!checkPresence(locationId, true))
		{
			// presence relates a usage session (the current one) with a location
			UsageSession curSession = m_usageSessionService.getSession();
			if (curSession == null) return;

			// update the storage
			m_storage.setPresence(curSession.getId(), locationId);

			// generate the event
			Event event = m_eventTrackingService.newEvent(EVENT_PRESENCE, presenceReference(locationId), true);
			m_eventTrackingService.post(event, curSession);

			// create a presence for tracking

			// bind a presence tracking object to the sakai session for auto-cleanup when logout or inactivity invalidates the sakai session
			Session session = m_sessionManager.getCurrentSession();
			ToolSession ts = session.getToolSession(SESSION_KEY);
			Presence p = new Presence(curSession, locationId, timeout);
			ts.setAttribute(locationId, p);
		}

		// retire any expired presence
		checkPresenceForExpiration();

	} // setPresence

	/**
	 * {@inheritDoc}
	 */
	public void removePresence(String locationId)
	{
		if (locationId == null) return;

		if (checkPresence(locationId, false))
		{
			UsageSession curSession = m_usageSessionService.getSession();

			// tell maintenance
			m_storage.removePresence(curSession.getId(), locationId);

			// generate the event
			Event event = m_eventTrackingService.newEvent(EVENT_ABSENCE, presenceReference(locationId), true);
			m_eventTrackingService.post(event, curSession);

			// remove from state
			Session session = m_sessionManager.getCurrentSession();
			ToolSession ts = session.getToolSession(SESSION_KEY);
			Presence p = (Presence) ts.getAttribute(locationId);
			if (p != null)
			{
				p.deactivate();
				ts.removeAttribute(locationId);
			}
		}

	} // removePresence

	/**
	 * {@inheritDoc}
	 */
	public void removeSessionPresence(String sessionId)
	{
		List<String> presence = m_storage.removeSessionPresence(sessionId); 
		
		// get the session
        UsageSession session = m_usageSessionService.getSession(sessionId);
		
		// send presence end events for these
		for (String locationId  : presence)
		{
			Event event = m_eventTrackingService.newEvent(PresenceService.EVENT_ABSENCE, 
					presenceReference(locationId), true);
			m_eventTrackingService.post(event, session);
		}

	}

	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<UsageSession> getPresence(String locationId)
	{
		// get the sessions at this location
		List<UsageSession> sessions = m_storage.getSessions(locationId);

		// sort
		Collections.sort(sessions);

		return sessions;

	} // getPresence

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<User> getPresentUsers(String locationId)
	{
		// get the sessions
		List<UsageSession> sessions = m_storage.getSessions(locationId);

		// form a list of user ids
		List<String> userIds = new Vector<String>();
		
		for (UsageSession s : sessions)
		{	
			if (!userIds.contains(s.getUserId()))
			{
				userIds.add(s.getUserId());
			}
		}

		// get the users for these ids
		List<User> users = m_userDirectoryService.getUsers(userIds);

		// sort
		Collections.sort(users);

		return users;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<User> getPresentUsers(String locationId, String siteId)
	{
		// get the sessions
		List<UsageSession> sessions = m_storage.getSessions(locationId);

		// form a list of user ids
		List<String> userIds = new Vector<String>();
		
		for (UsageSession s : sessions)
		{
			if (!userIds.contains(s.getUserId()))
			{
				userIds.add(s.getUserId());
			}
		}
		
		Set<String> userIdsSet = m_privacyManager.findViewable("/site/" + siteId, new HashSet(userIds));

		// get the users for these ids
		List<User> users = m_userDirectoryService.getUsers(userIdsSet);
		
		// sort
		Collections.sort(users);

		return users;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getLocations()
	{
		List<String> locations = m_storage.getLocations();

		Collections.sort(locations);

		return locations;

	} // getLocations

	/**
	 * {@inheritDoc}
	 */
	public String locationId(String site, String page, String tool)
	{
		// TODO: remove
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLocationDescription(String location)
	{
		// TODO: get a description for a placement!
		return "location: " + location;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getTimeout()
	{
		return m_timeout;
	}

	/**
	 * Check if the current session is present at the location - optionally refreshing it
	 * 
	 * @param locationId
	 *        The location to check.
	 * @param refresh
	 *        If true, refresh the timeout on the presence if found
	 * @return True if the current session is present at that location, false if not.
	 */
	protected boolean checkPresence(String locationId, boolean refresh)
	{
		Session session = m_sessionManager.getCurrentSession();
		ToolSession ts = session.getToolSession(SESSION_KEY);
		Presence p = (Presence) ts.getAttribute(locationId);

		if ((p != null) && refresh)
		{
			p.setActive();
		}

		return (p != null);
	}

	/**
	 * Check current session presences and remove any expired ones
	 */
	@SuppressWarnings("unchecked")
	protected void checkPresenceForExpiration()
	{
		Session session = m_sessionManager.getCurrentSession();
		ToolSession ts = session.getToolSession(SESSION_KEY);
		Enumeration locations = ts.getAttributeNames();
		while (locations.hasMoreElements())
		{
			String location = (String) locations.nextElement();

			Presence p = (Presence) ts.getAttribute(location);
			if (p != null && p.isExpired())
			{
				ts.removeAttribute(location);
			}
		}
	}

	/**
	 * Check all session presences and remove any expired ones
	 */
	@SuppressWarnings("unchecked")
	protected void checkAllPresenceForExpiration()
	{
		List<Session> sessions = m_sessionManager.getSessions();
		
		for (Iterator<Session> i = sessions.iterator(); i.hasNext();)
		{
			Session session = (Session) i.next();
			ToolSession ts = session.getToolSession(SESSION_KEY);
			Enumeration locations = ts.getAttributeNames();
			while (locations.hasMoreElements())
			{
				String location = (String) locations.nextElement();

				Presence p = (Presence) ts.getAttribute(location);

				if (log.isDebugEnabled()) log.debug("checking expiry of session " + session.getId() + " in location " + location);
				
				if (p != null && p.isExpired())
				{
					ts.removeAttribute(location);
				}
			}
		
		}

	}

		
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected interface Storage
	{
		/**
		 * Add this session id's presence at this location, if not already there.
		 * 
		 * @param sessionId
		 *        The session id.
		 * @param locationId
		 *        The location id.
		 */
		void setPresence(String sessionId, String locationId);

		/**
		 * Remove this sessions id's presence at this location.
		 * 
		 * @param sessionId
		 *        The session id.
		 * @param locationId
		 *        The location id.
		 */
		void removePresence(String sessionId, String locationId);

		/**
		 * Remove presence for all locations for this session id.
		 * 
		 * @param sessionId
		 *        The session id.
		 * @param locationId
		 *        The location id.
		 */
		List<String> removeSessionPresence(String sessionId);
		
		/**
		 * Access the List of UsageSessions present at this location.
		 * 
		 * @param locationId
		 *        The location id.
		 * @return The List of sessions (UsageSession) present at this location.
		 */
		List<UsageSession> getSessions(String locationId);

		/**
		 * Access the List of all known location ids.
		 * 
		 * @return The List (String) of all known locations.
		 */
		List<String> getLocations();
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Presence
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected class Presence implements SessionBindingListener
	{
		/** The session. */
		protected UsageSession m_session = null;

		/** The location id. */
		protected String m_locationId = null;

		/** If true, process the unbound. */
		protected boolean m_active = true;

		/** Time in seconds before expiry. */
		protected long m_presence_timeout = 0;

		/** Timestamp in milliseconds to expire. */
		protected long m_expireTime = 0;

		public Presence(UsageSession session, String locationId, int timeout)
		{
			m_session = session;
			m_locationId = locationId;
			m_presence_timeout = timeout;
			m_expireTime = System.currentTimeMillis() + m_presence_timeout * 1000;
		}

		public void deactivate()
		{
			m_active = false;
		}

		/**
		 * Reset the timeout based on current activity
		 */
		public void setActive()
		{
			m_expireTime = System.currentTimeMillis() + m_presence_timeout * 1000;
		}

		/**
		 * Has this presence timed out?
		 * 
		 * @return true if expired, false if not.
		 */
		public boolean isExpired()
		{
			return System.currentTimeMillis() > m_expireTime;
		}

		/**
		 * {@inheritDoc}
		 */
		public void valueBound(SessionBindingEvent event)
		{
		}

		/**
		 * {@inheritDoc}
		 */
		public void valueUnbound(SessionBindingEvent evt)
		{
			if (m_active)
			{
				m_storage.removePresence(m_session.getId(), m_locationId);

				// generate the event
				Event event = m_eventTrackingService.newEvent(EVENT_ABSENCE, presenceReference(m_locationId), true);
				m_eventTrackingService.post(event, m_session);
			}
		}
	}

	/**
	 * @return the notificationService
	 */
	public NotificationService getNotificationService()
	{
		return notificationService;
	}

	/**
	 * @param notificationService the notificationService to set
	 */
	public void setNotificationService(NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}
	
	// Maintenance thread
	
	protected class Maintenance implements Runnable
	{
		/** My thread running my timeout checker. */
		protected Thread m_maintenanceChecker = null;

		/** Configuration: how often in seconds to check for expired presence */
		protected long m_refresh = 15;

		/** Signal to the timeout checker to stop. */
		protected boolean m_maintenanceCheckerStop = false;

		/**
		 * Construct.
		 */
		public Maintenance()
		{
		}

		/**
		 * Start the maintenance thread.
		 */
		public void start()
		{
			if (m_maintenanceChecker != null) return;

			m_maintenanceChecker = new Thread(this, "SakaiPresenceService.Maintenance");
			m_maintenanceChecker.setDaemon(true);
			m_maintenanceCheckerStop = false;
			m_maintenanceChecker.start();
		}

		/**
		 * Stop the maintenance thread
		 */
		public void stop()
		{
			if (m_maintenanceChecker != null)
			{
				m_maintenanceCheckerStop = true;
				m_maintenanceChecker.interrupt();
				try
				{
					// wait for it to die
					m_maintenanceChecker.join();
				}
				catch (InterruptedException ignore)
				{
				}
				m_maintenanceChecker = null;
			}

			// Nothing to do 
			
		}

		/**
		 * Run the maintenance thread. Every REFRESH seconds, check for expired presence
		 */
		public void run()
		{
			// wait till things are rolling
			ComponentManager.waitTillConfigured();

			if (log.isDebugEnabled()) log.debug("run()");

			while (!m_maintenanceCheckerStop)
			{
				try
				{
					if (log.isDebugEnabled()) log.debug("checking for expired presence");
					checkAllPresenceForExpiration();

				}
				catch (Exception e)
				{
					//SAK-20847 don't print the stacktrace unless we are in debug mode
					log.warn("Exception checking for expired presence. If the app server is currently stopped you can safely ignore this warning. Under any other circumstances, enable debug logging to see the cause.");
					if (log.isDebugEnabled()) {
						log.debug("The exception is: ", e);
					}
				}

				// cycle every REFRESH seconds
				if (!m_maintenanceCheckerStop)
				{
					try
					{
						Thread.sleep(m_refresh * 1000L);
					}
					catch (Exception ignore)
					{
					}
				}
			}

			if (log.isDebugEnabled()) log.debug("done");
		}
	}

}
