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

import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventDelayHandler;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * BaseEventTrackingService is the base implmentation for the EventTracking service.
 * </p>
 */
@Slf4j
public abstract class BaseEventTrackingService implements EventTrackingService
{
	/** An observable object helper. */
	protected MyObservable m_observableHelper = new MyObservable();

	/** An observable object helper for see-it-first priority observers. */
	protected MyObservable m_priorityObservableHelper = new MyObservable();

	/** An observable object helper for see-only-local-events observers. */
	protected MyObservable m_localObservableHelper = new MyObservable();

	protected EventDelayHandler delayHandler;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Observable implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Cause this new event to get to wherever it has to go for persistence, etc.
	 *
	 * @param event
	 *        The new event to post.
	 */
	protected abstract void postEvent(Event event);

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Event post / flow - override
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Send notification about a new event to observers.
	 *
	 * @param event
	 *        The event to send notification about.
	 * @param local
	 *        True if the event originated on this server, false if it came from another server.
	 */
	protected void notifyObservers(Event event, boolean local)
	{
		// %%% inline like this, or on a new thread?

		if (log.isDebugEnabled()) log.debug(this + " Notification - Event: " + event);

		// first, notify all priority observers
		m_priorityObservableHelper.setChanged();
		m_priorityObservableHelper.notifyObservers(event);

		// notify the normal observers
		m_observableHelper.setChanged();
		m_observableHelper.notifyObservers(event);

		// if the event is local, notify local observers
		if (local)
		{
			m_localObservableHelper.setChanged();
			m_localObservableHelper.notifyObservers(event);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Observer notification
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the UsageSessionService collaborator.
	 */
	protected abstract UsageSessionService usageSessionService();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the SessionManager collaborator.
	 */
	protected abstract SessionManager sessionManager();

	/**
	 * @return the SecurityService collaborator.
	 */
	protected abstract SecurityService securityService();

	/**
	 * @return the ToolManager collaborator.
	 */
	protected abstract ToolManager toolManager();

	/**
	 * @return the EntityManager collaborator.
	 */
	protected abstract EntityManager entityManager();

	/**
	 * @return the TimeService collaborator.
	 */
	protected abstract TimeService timeService();

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		log.info(this + ".init()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		log.info(this + ".destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * EventTracking implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public void setEventDelayHandler(EventDelayHandler handler)
	{
		log.info("Setting the event delay handler to " + handler + " [was: " + delayHandler + "]");
		this.delayHandler = handler;
	}

	/**
	 * Construct a Event object.
	 *
	 * @param event
	 *        The Event id.
	 * @param resource
	 *        The resource reference.
	 * @param modify
	 *        Set to true if this event caused a resource modification, false if it was just an access.
	 * @return A new Event object that can be used with this service.
	 */
	public Event newEvent(String event, String resource, boolean modify)
	{
		return new BaseEvent(event, resource, modify, NotificationService.NOTI_OPTIONAL, null);
	}

	/**
	 * Construct a Event object.
	 *
	 * @param event
	 *        The Event id.
	 * @param resource
	 *        The resource reference.
	 * @param modify
	 *        Set to true if this event caused a resource modification, false if it was just an access.
	 * @param priority
	 *        The Event's notification priority.
	 * @return A new Event object that can be used with this service.
	 */
	public Event newEvent(String event, String resource, boolean modify, int priority)
	{
		return new BaseEvent(event, resource, modify, priority, null);
	}

	/**
	 * Construct a Event object.
	 *
	 * @param event
	 *        The Event id.
	 * @param resource
	 *        The resource reference.
	 * @param context
	 *        The Event's context.
	 * @param modify
	 *        Set to true if this event caused a resource modification, false if it was just an access.
	 * @param priority
	 *        The Event's notification priority.
	 * @return A new Event object that can be used with this service.
	 */
	public Event newEvent(String event, String resource, String context, boolean modify, int priority)
	{
		return new BaseEvent(event, resource, context, modify, priority, null);
	}

	/**
	 * Construct a Event object.
	 *
	 * @param event
	 *        The Event id.
	 * @param resource
	 *        The resource reference.
	 * @param context
	 *        The Event's context.
	 * @param modify
	 *        Set to true if this event caused a resource modification, false if it was just an access.
	 * @param priority
	 *        The Event's notification priority.
	 * @return A new Event object that can be used with this service.
	 */
	public Event newEvent(String event, String resource, String context, boolean modify, int priority, LRS_Statement lrsStatement)
	{
		return new BaseEvent(event, resource, context, modify, priority, lrsStatement);
	}


	/**
	 * Post an event
	 *
	 * @param event
	 *        The event object (created with newEvent()). Note: the current session user will be used as the user responsible for the event.
	 */
	public void post(Event event)
	{
		BaseEvent be = ensureBaseEvent(event);
		// get the session id or user id
		String id = usageSessionService().getSessionId();
		if (id != null)
		{
			be.setSessionId(id);
		}

		// post for the session "thread" user
		else
		{
			id = sessionManager().getCurrentSessionUserId();
			if (id == null)
			{
				id = UNKNOWN_USER;
			}

			be.setUserId(id);
		}

		postEvent(be);
	}

	/**
	 * Post an event on behalf of a user's session
	 *
	 * @param event
	 *        The event object (created with newEvent()).
	 * @param session
	 *        The usage session object of the user session responsible for the event.
	 */
	public void post(Event event, UsageSession session)
	{
		BaseEvent be = ensureBaseEvent(event);
		String id = UNKNOWN_USER;
		if (session != null) id = session.getId();

		be.setSessionId(id);

		postEvent(be);
	}

	/**
	 * Post an event on behalf of a user.
	 *
	 * @param event
	 *        The event object (created with newEvent()).
	 * @param user
	 *        The User object of the user responsible for the event.
	 */
	public void post(Event event, User user)
	{
		BaseEvent be = ensureBaseEvent(event);
		String id = UNKNOWN_USER;
		if (user != null) id = user.getId();

		be.setUserId(id);

		// establish a security advisor if the user id is set on the event.  this ensures that
		// false permission exceptions aren't encountered during an event refiring.
		boolean useAdvisor = false;
		if (be.getUserId() != null)
		{
			useAdvisor = true;
			securityService().pushAdvisor(newResourceAdvisor(be.getUserId()));
		}

		postEvent(be);

		// if an advisor was used, pop it off.
		if (useAdvisor)
			securityService().popAdvisor();
	}

	public void delay(Event event, Time fireTime)
	{
		Time now = timeService().newTime();
		if (fireTime == null || fireTime.before(now))
		{
			postEvent(event);
		}
		else
		{
			if (delayHandler != null)
			{
				// Make sure there is a userid associated with the event

				String id = event.getUserId();

				if (id == null)
				{
					id = sessionManager().getCurrentSessionUserId();
				}

				if (id == null)
				{
					id = UNKNOWN_USER;
				}

				delayHandler.createDelay(event, id, fireTime);
			}
			else
			{
				log.warn("Unable to create delayed event because delay handler is unset.  Firing now.");
				postEvent(event);
			}
		}
	}

	public void cancelDelays(String resource)
	{
		if (delayHandler != null)
		{
			delayHandler.deleteDelay(resource);
		}
	}

	public void cancelDelays(String resource, String event)
	{
		if (delayHandler != null)
		{
			delayHandler.deleteDelay(resource, event);
		}
	}

	/**
	 * Ensure that the provided event is an instance of BaseEvent.  If not, create a new BaseEvent
	 * and transfer state.
	 *
	 * @param e
	 * @return
	 */
	protected BaseEvent ensureBaseEvent(Event e)
	{
		BaseEvent event = null;
		if (e instanceof BaseEvent)
		{
			event = (BaseEvent) e;
		}
		else
		{
			event = new BaseEvent(e.getEvent(), e.getResource(), e.getModify(), e.getPriority(),null);
			event.setSessionId(e.getSessionId());
			event.setUserId(e.getUserId());
		}
		return event;
	}

	/**
	 * Refired events can occur under a different user and session than was originally available.
	 * To make sure permission exceptions aren't falsely encountered, a security advisor should be
	 * pushed on the stack to recreate the correct environment for security checks.
	 *
	 * @param userId
	 */
	private SecurityAdvisor newResourceAdvisor(final String eventUserId)
	{
		// security advisor is needed if an event is refired.  the refired event is under the
		// auspices of the job scheduler user and needs to be advised by the original user.
		return new SecurityAdvisor()
		{
			public SecurityAdvice isAllowed(String userId, String function, String reference)
			{
				SecurityAdvice sa = SecurityAdvice.PASS;
				if (userId.equals(eventUserId))
					sa = SecurityAdvice.ALLOWED;
				return sa;
			}
		};
	}

	/**
	 * Add an observer of events. The observer will be notified whenever there are new events.
	 *
	 * @param observer
	 *        The class observing.
	 */
	public void addObserver(Observer observer)
	{
		// keep this observer in one list only
		m_priorityObservableHelper.deleteObserver(observer);
		m_localObservableHelper.deleteObserver(observer);

		m_observableHelper.addObserver(observer);
	}

	/**
	 * Add an observer of events. The observer will be notified whenever there are new events. Priority observers get notified first, before normal observers.
	 *
	 * @param observer
	 *        The class observing.
	 */
	public void addPriorityObserver(Observer observer)
	{
		// keep this observer in one list only
		m_observableHelper.deleteObserver(observer);
		m_localObservableHelper.deleteObserver(observer);

		m_priorityObservableHelper.addObserver(observer);
	}

	/**
	 * Add an observer of events. The observer will be notified whenever there are new events. Local observers get notified only of event generated on this application server, not on those generated elsewhere.
	 *
	 * @param observer
	 *        The class observing.
	 */
	public void addLocalObserver(Observer observer)
	{
		// keep this observer in one list only
		m_observableHelper.deleteObserver(observer);
		m_priorityObservableHelper.deleteObserver(observer);

		m_localObservableHelper.addObserver(observer);
	}

	/**
	 * Delete an observer of events.
	 *
	 * @param observer
	 *        The class observing to delete.
	 */
	public void deleteObserver(Observer observer)
	{
		m_observableHelper.deleteObserver(observer);
		m_priorityObservableHelper.deleteObserver(observer);
		m_localObservableHelper.deleteObserver(observer);
	}

	/**
	 * Extend Observable to "public"ize setChanges, so we can set it. Why a helper object? Cause the service (which is observable) already 'extends' TurbineBaseService, and cannot also 'extend' Observable.
	 */
	protected class MyObservable extends Observable
	{
		public void setChanged()
		{
			super.setChanged();
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Event implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * <p>
	 * BaseEvent is the implementation of the Event interface.
	 * </p>
	 * <p>
	 * Event objects are posted to the EventTracking service, and may be listened for.
	 * </p>
	 */
	protected class BaseEvent implements Event
	{
		/**
		 * Be a good Serializable citizen
		 */
		private static final long serialVersionUID = 3690761674282252600L;

		/** The Event's sequence number. */
		protected long m_seq = 0;

		/** The Event's id string. */
		protected String m_id = "";

		/** The Event's resource reference string. */
		protected String m_resource = "";

		/** The Event's context. May be null. */
		protected String m_context = null;
		
		/** The Event's session id string. May be null. */
		protected String m_session = null;

		/** The Event's user id string. May be null. */
		protected String m_user = null;

		/** The Event's modify flag (true if the event caused a resource modification). */
		protected boolean m_modify = false;

		/** The Event's notification priority. */
		protected int m_priority = NotificationService.NOTI_OPTIONAL;

		/** Event creation time. */
		protected Date m_time = null;

		/** Event LRS Statement */
		protected LRS_Statement m_lrsStatement = null;

		/**
		 * Construct
		 *
		 * @param event
		 *        The Event id.
		 * @param resource
		 *        The resource id.
		 * @param modify
		 *        If the event caused a modify, true, if it was just an access, false.
		 * @param priority
		 *        The Event's notification priority.
		 */
		public BaseEvent(String event, String resource, boolean modify, int priority, LRS_Statement lrsStatement)
		{
			setEvent(event);
			setResource(resource);
			m_lrsStatement = lrsStatement;
			m_modify = modify;
			m_priority = priority;

			// Find the context using the reference (let the service that it belongs to parse it)
			if (resource != null && !"".equals(resource)) {
				Reference ref = entityManager().newReference(resource);
				if (ref != null) {
					m_context = ref.getContext();
				}
			}

			// If we still need to find the context, try the tool placement
			if (m_context == null) {
				Placement placement = toolManager().getCurrentPlacement();
				if (placement != null) {
					m_context = placement.getContext();
				}
			}

			// KNL-997
			String uId = sessionManager().getCurrentSessionUserId();
			if (uId == null)
			{
				uId = UNKNOWN_USER;
			}
			setUserId(uId);


		}

		/**
		 * Construct
		 *
		 * @param event
		 *        The Event id.
		 * @param resource
		 *        The resource id.
		 * @param modify
		 *        If the event caused a modify, true, if it was just an access, false.
		 * @param context
		 *        The Event's context (may be null)
		 * @param priority
		 *        The Event's notification priority.
		 */
		public BaseEvent(String event, String resource, String context, boolean modify, int priority, LRS_Statement lrsStatement)
		{
			this(event, resource, modify, priority, lrsStatement);
			//Use the context parameter if it's not null, otherwise default to the detected context
			if (context != null) {
				m_context = context;
			}
		}
		
		/**
		 * Construct
		 *
		 * @param seq
		 *        The event sequence number.
		 * @param event
		 *        The Event id.
		 * @param resource
		 *        The resource id.
		 * @param modify
		 *        If the event caused a modify, true, if it was just an access, false.
		 * @param priority
		 *        The Event's notification priority.
		 */
		public BaseEvent(String event, String resource, String context, boolean modify, int priority)
		{
			this(event, resource, context, modify, priority, null);
		}


		/**
		 * Construct
		 *
		 * @param seq
		 *        The event sequence number.
		 * @param event
		 *        The Event id.
		 * @param resource
		 *        The resource id.
		 * @param modify
		 *        If the event caused a modify, true, if it was just an access, false.
		 * @param priority
		 *        The Event's notification priority.
		 */
		public BaseEvent(long seq, String event, String resource, String context, boolean modify, int priority)
		{
			this(event, resource, context, modify, priority);
			m_seq = seq;
		}

		
		public BaseEvent(long seq, String event, String resource, String context, boolean modify, int priority, Date eventDate)
		{
			this(event, resource, context, modify, priority);
			m_seq = seq;
			m_time = eventDate;
		}

		/**
		 * Access the event id string
		 *
		 * @return The event id string.
		 */
		public String getEvent()
		{
			return m_id;
		}

		/**
		 * Set the event id.
		 *
		 * @param id
		 *        The event id string.
		 */
		protected void setEvent(String id)
		{
			if (id != null)
			{
				m_id = id;
			}
			else
			{
				m_id = "";
			}
		}

		/**
		 * Access the resource reference.
		 *
		 * @return The resource reference string.
		 */
		public String getResource()
		{
			return m_resource;
		}

		/**
		 * Set the resource id.
		 *
		 * @param id
		 *        The resource id string.
		 */
		protected void setResource(String id)
		{
			if (id != null)
			{
				m_resource = id;
			}
			else
			{
				m_resource = "";
			}
		}

		/**
		 * Access the resource reference.
		 *
		 * @return The resource reference string.
		 */
		public String getContext()
		{
			return m_context;
		}

		/**
		 * Access the resource metadata.
		 *
		 * @return The resource metadata string.
		 */
		public LRS_Statement getLrsStatement()
		{
			return m_lrsStatement;
		}

		/**
		 * Access the UsageSession id. If null, check for a User id.
		 *
		 * @return The UsageSession id string.
		 */
		public String getSessionId()
		{
			return m_session;
		}
		
		/**
		 * Set the session id.
		 *
		 * @param id
		 *        The session id string.
		 */
		protected void setSessionId(String id)
		{
			if ((id != null) && (id.length() > 0))
			{
				m_session = id;
			}
			else
			{
				m_session = null;
			}
		}

		/**
		 * Access the User id. If null, check for a session id.
		 *
		 * @return The User id string.
		 */
		public String getUserId()
		{
			return m_user;
		}

		/**
		 * Set the user id.
		 *
		 * @param id
		 *        The user id string.
		 */
		protected void setUserId(String id)
		{
			if ((id != null) && (id.length() > 0))
			{
				m_user = id;
			}
			else
			{
				m_user = null;
			}
		}

		/**
		 * Is this event one that caused a modify to the resource, or just an access.
		 *
		 * @return true if the event caused a modify to the resource, false if it was just an access.
		 */
		public boolean getModify()
		{
			return m_modify;
		}

		/**
		 * Access the event's notification priority.
		 *
		 * @return The event's notification priority.
		 */
		public int getPriority()
		{
			return m_priority;
		}

		/**
		 * @return A representation of this event's values as a string.
		 */
		public String toString()
		{
			return m_seq + ":" + getEvent() + "@" + getResource() + "[" + (getModify() ? "m" : "a") + ", " + getPriority() + "]";
		}

		public Date getEventTime() {
			return new Date(m_time.getTime());
		}
	}
}
