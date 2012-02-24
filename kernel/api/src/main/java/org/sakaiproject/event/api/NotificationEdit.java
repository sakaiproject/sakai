/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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

import org.sakaiproject.entity.api.Edit;

/**
 * <p>
 * NotificationEdit is a mutable Notification.
 * </p>
 */
public interface NotificationEdit extends Notification, Edit
{
	/**
	 * Do the notification.
	 * 
	 * @param event
	 *        The event that matched criteria to cause the notification.
	 */
	void notify(Event event);

	/**
	 * Set the Event function, clearing any that have already been set.
	 * 
	 * @param function
	 *        The Event function to watch for.
	 */
	void setFunction(String function);

	/**
	 * Add another Event function.
	 * 
	 * @param function
	 *        Another Event function to watch for.
	 */
	void addFunction(String function);

	/**
	 * Set the resource reference filter.
	 * 
	 * @param filter
	 *        The resource reference filter.
	 */
	void setResourceFilter(String filter);

	/**
	 * Set the action helper that handles the notify() action.
	 * 
	 * @param action
	 *        The action helper that handles the notify() action.
	 */
	void setAction(NotificationAction action);
}
