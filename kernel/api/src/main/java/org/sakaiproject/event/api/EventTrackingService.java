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

package org.sakaiproject.event.api;

import java.util.Observer;

import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * The event tracking service provides activity event tracking and monitoring.<br />
 * Objects act as event generators, posting events to the service.<br />
 * Other objects act as event monitors, and are notified by the service when certain events occur.<br />
 * Events posted are also stored in event archives by the service.
 * </p>
 */
public interface EventTrackingService
{
	/** This string can be used to find the service in the service manager. */
	static final String SERVICE_NAME = EventTrackingService.class.getName();
	static final String UNKNOWN_USER = "?";

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
	Event newEvent(String event, String resource, boolean modify);

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
	Event newEvent(String event, String resource, boolean modify, int priority);

	/**
	 * Construct a Event object.
	 * 
	 * @param event
	 *        The Event id.
	 * @param resource
	 *        The resource reference.
	 * @param context
	 *        The Event's context (may be null).
	 * @param modify
	 *        Set to true if this event caused a resource modification, false if it was just an access.
	 * @param priority
	 *        The Event's notification priority. Use NotificationService.NOTI_OPTIONAL as default.
	 * @return A new Event object that can be used with this service.
	 */
	Event newEvent(String event, String resource, String context, boolean modify, int priority);

/**
	 * Construct a Event object.
	 * 
	 * @param event
	 *        The Event id.
	 * @param resource
	 *        The resource reference.
	 * @param context
	 *        The Event's context (may be null, if null will try to detect context).
	 * @param modify
	 *        Set to true if this event caused a resource modification, false if it was just an access.
	 * @param priority
	 *        The Event's notification priority. Use NotificationService.NOTI_OPTIONAL as default.
	 * @param lrsStatement
	 *        Additional, optional (currently unpersisted) lrsStatement passed to LRS service (null is the default)
	 * @return A new Event object that can be used with this service.
	 */
	Event newEvent(String event, String resource, String context, boolean modify, int priority, LRS_Statement lrsStatement);

	/**
	 * Post an event
	 * 
	 * @param event
	 *        The event object (created with newEvent()). Note: the current session user will be used as the user responsible for the event.
	 */
	void post(Event event);

	/**
	 * Post an event on behalf of a user's session
	 * 
	 * @param event
	 *        The event object (created with newEvent()).
	 * @param session
	 *        The usage session object of the user session responsible for the event.
	 */
	void post(Event event, UsageSession session);

	/**
	 * Post an event on behalf of a user.
	 * 
	 * @param event
	 *        The event object (created with newEvent()).
	 * @param user
	 *        The User object of the user responsible for the event.
	 */
	void post(Event event, User user);

	/**
	 * Add an observer of events. The observer will be notified whenever there are new events.
	 * 
	 * @param observer
	 *        The class observing.
	 */
	void addObserver(Observer observer);

	/**
	 * Add an observer of events. The observer will be notified whenever there are new events. Priority observers get notified first, before normal observers.
	 * 
	 * @param observer
	 *        The class observing.
	 */
	void addPriorityObserver(Observer observer);

	/**
	 * Add an observer of events. The observer will be notified whenever there are new events. Local observers get notified only of event generated on this application server, not on those generated elsewhere.
	 * 
	 * @param observer
	 *        The class observing.
	 */
	void addLocalObserver(Observer observer);

	/**
	 * Delete an observer of events.
	 * 
	 * @param observer
	 *        The class observing to delete.
	 */
	void deleteObserver(Observer observer);

	/**
	 * Sets the event delay handler if the service is to allow events to be delayed to a specified
	 * firing time.
	 * 
	 * @param handler
	 */
	void setEventDelayHandler(EventDelayHandler handler);

	/**
	 * Schedule an event to be fired at a specific time.  If the time is null or already passed, the
	 * event is fired immediately.
	 * 
	 * @param event
	 * @param fireTime
	 */
	void delay(Event event, Time fireTime);

	/**
	 * Cancel all delayed events for a resource that haven't yet fired.
	 * 
	 * @param resource
	 */
	void cancelDelays(String resource);

	/**
	 * Cancel delays for a resource of a certain event
	 * @param resource
	 * @param event
	 */
	void cancelDelays(String resource, String event);
}
