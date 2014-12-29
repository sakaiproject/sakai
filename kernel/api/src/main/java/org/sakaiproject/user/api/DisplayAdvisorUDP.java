/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 Sakai Foundation
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

package org.sakaiproject.user.api;

/**
 * <p>
 * DisplayAdvisorUDP is an optional interface for a UserDirectoryProvider to indicate that they should be called for User.getDisplayId() and User.getDisplayName().
 * </p>
 */
public interface DisplayAdvisorUDP
{
	/**
	 * Compute a display id for this user.
	 * 
	 * @param user
	 *        The User object.
	 * @return a display id for this user, or null if the UDP is not advising on this one.
	 */
	String getDisplayId(User user);

	/**
	 * Compute a display name for this user.
	 * 
	 * @param user
	 *        The User object.
	 * @return a display name for this user, or null if the UDP is not advising on this one.
	 */
	String getDisplayName(User user);
}
