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

import java.util.Map;

/**
 * This interface provides methods to allow a tool to set various options for notification preferences
 * @author chrismaurer
 *
 */
public interface UserNotificationPreferencesRegistration {
	
	/**
	 * 
	 * @return String Returns the text to be displayed for the section header
	 */
	public String getSectionTitle();
	
	/**
	 * 
	 * @return String Returns the text to be displayed for the section description
	 */
	public String getSectionDescription();
	
	/**
	 * See NotificationService.PREF_NONE, NotificationService.PREF_IGNORE, NotificationService.PREF_DIGEST, NotificationService.PREF_IMMEDIATE, etc.
	 * This value will be the string representation of that int value.
	 * @return String The default notification option for this tool 
	 */
	public String getDefaultValue();
	
	/**
	 * Return the configured type.  Something like this: sakai:content
	 * @return String type
	 */
	public String getType();
	
	/**
	 * Return the prefix used in the preference storage.  Something like this: rsrc
	 * @return String prefix
	 */
	public String getPrefix();
	
	/**
	 * Return the toolId that identifies the tool registering these preferences.  Something like this: sakai.resources
	 * @return String toolId
	 */
	public String getToolId();
	
	/**
	 * Get the key/value pairs that represent the notification option texts and values
	 * @return
	 */
	public Map<String, String> getOptions();
	
	/**
	 * Can site specific options be set?
	 * @return boolean
	 */
	public boolean isOverrideBySite();
	
	/**
	 * Will the section div be expanded by default, or will only the section title be visible
	 * @return boolean
	 */
	public boolean isExpandByDefault();
	
	/**
	 * This method will allow registering tools to supply their own location for resource loaders
	 * @param location
	 * @return
	 */
	public Object getResourceLoader(String location);
	
	
}
