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
import java.util.Map;

/**
 * 
 * ActivityService provides the ability to check if users are online and when they were last active.
 * 
 * <p>
 * Users are registered and updated each time they publish an event. As such you can tell <b>if</b> a user is active
 * and <b>when</b> they were last active (last event time).
 * </p>
 * 
 * <p>
 * For clustered environments, you may experience discrepancies when a new node comes online as the cache will start afresh
 * so users will only be repopulated into the cache once they start publishing events.</p>
 * 
 * @since 1.2.1
 */
public interface ActivityService {

	/**
	 * Check if a userId has an active Sakai session.
	 * @param userId	userId to check
	 * @return	true if active, false if not
	 */
	public boolean isUserActive(String userId);
	
	/**
	 * Get the list of users with active Sakai sessions, given the supplied list of userIds.
	 * @param userIds	userIds to check
	 * @return	List of userIds that have active Sakai sessions
	 */
	public List<String> getActiveUsers(List<String> userIds);
	
	/**
	 * Get last event time for the given user
	 * 
	 * @param userId	userId to check
	 * @return time of event as Long, null if no event.
	 */
	Long getLastEventTimeForUser(String userId);
	
	/**
	 * Get last event time for the given list of users
	 * 
	 * @param userIds	List of userId's to check
	 * @return map of userId and time of last event. If no event, there will be no entry in the map for that user.
	 */
	Map<String, Long> getLastEventTimeForUsers(List<String> userIds);
	
}

