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

import java.util.List;

import org.sakaiproject.entity.api.Entity;

/**
 * <p>
 * Notification the interface for classes that act to notify, used with the GenericNotificationService.
 * </p>
 */
public interface Notification extends Entity
{
	/**
	 * Do the notification.
	 * 
	 * @param event
	 *        The event that matched criteria to cause the notification.
	 */
	void notify(Event event);

	/**
	 * Get the Event function. Only Events with this function code will trigger the notification.
	 * 
	 * @return The Event function to watch for.
	 */
	String getFunction();

	/**
	 * Get all the Event functions for this notification.
	 * 
	 * @return a List (String) of Event functions to watch for.
	 */
	List<String>  getFunctions();

	/**
	 * Check if the notification watches for events with this function code.
	 * 
	 * @param function
	 *        The Event function to test.
	 * @return true if this notification watches for evens with this function code, false if not.
	 */
	boolean containsFunction(String function);

	/**
	 * Get the resource reference filter. Only Events with references matching this will trigger the notification.
	 * 
	 * @return The resource reference filter.
	 */
	String getResourceFilter();

	/**
	 * Get the action helper that handles the notify() action.
	 * 
	 * @return The action helper that handles the notify() action.
	 */
	NotificationAction getAction();
}
