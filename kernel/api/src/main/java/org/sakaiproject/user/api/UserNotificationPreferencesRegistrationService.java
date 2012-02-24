/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2010 The Sakai Foundation
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

import java.util.List;

/**
 * Service interface that allows tools to register themselves as having email notification preferences.
 * This keeps tool specific code out of the Preferences tool.
 * @author chrismaurer
 *
 */
public interface UserNotificationPreferencesRegistrationService {

	/**
	 * Method to register a UserNotificationPreferencesRegistration object
	 * @param reg
	 */
	public void register(UserNotificationPreferencesRegistration reg);
	
	/**
	 * Gets all UserNotificationPreferencesRegistration objects that have been registered
	 * @return
	 */
	public List<UserNotificationPreferencesRegistration> getRegisteredItems();
	
	/**
	 * Get the keys for all UserNotificationPreferencesRegistration objects that have been registered
	 * @return
	 */
	public List<String> getKeys();
}
