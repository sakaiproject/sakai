/**********************************************************************************
 * $URL:  $
 * $Id:  $
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

import org.sakaiproject.time.api.Time;

/**
 * Event delayers delay an event for future propagation by the system.
 */
public interface EventDelayHandler
{
	/**
	 * Read an event delay by a specified delay ID.
	 * 
	 * @param delayId
	 * @return
	 */
	Event readDelay(String delayId);

	/**
	 * Read a list of event delay IDs using the provided event and user ID.
	 * 
	 * @param event
	 * @param userId
	 * @return
	 */
	List<String> findDelayIds(Event event, String userId);

	/**
	 * Read a list of event delay IDs using the provided event. Does not look at the user ID.
	 * 
	 * @param event
	 * @return
	 */
	List<String> findDelayIds(Event event);

	/**
	 * Read a list of event delay IDs using resource and event.
	 * 
	 * @param resource
	 * @param event
	 * @return
	 */
	List<String> findDelayIds(String resource, String event);

	/**
	 * Read a list of event delay IDs using resource and event.
	 * 
	 * @param resource
	 * @return
	 */
	List<String> findDelayIds(String resource);

	/**
	 * Schedules a delayed invocation of this notification to run at the requested time.
	 * 
	 * @param event
	 * @return The ID of the delay
	 */
	String createDelay(Event event, Time fireTime);

	/**
	 * Schedules a delayed invocation of this notification to run at the requested time.
	 * 
	 * @param event
	 * @param userId
	 * @return The ID of the delay
	 */
	String createDelay(Event event, String userId, Time fireTime);

	/**
	 * Delete an event delay by referencing the delay ID.
	 * 
	 * @param delayId
	 * @return
	 */
	boolean deleteDelayById(String delayId);

	/**
	 * Delete an event delay by matching the event information.
	 * 
	 * @param e
	 * @return
	 */
	boolean deleteDelay(Event e);

	/**
	 * Delete a delayed event by matching resource and event
	 * 
	 * @param resource
	 * @param event
	 * @return
	 */
	boolean deleteDelay(String resource, String event);

	/**
	 * Delete all delayed events for a resource
	 * 
	 * @param resource
	 * @return
	 */
	boolean deleteDelay(String resource);
}
