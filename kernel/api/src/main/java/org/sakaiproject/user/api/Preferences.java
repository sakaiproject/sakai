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

package org.sakaiproject.user.api;

import java.util.Collection;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;

/**
 * <p>
 * Preferences stores keyed sets of properties for a given user (id).
 * </p>
 */
public interface Preferences extends Entity, Comparable
{
	
	/**
	 * Field in which the users local preference is stored
	 */
	public static String FIELD_LOCALE = "locale";
	
	/**
	 * Access the user of the user who owns these Preferences.
	 * 
	 * @return The user id for these preferences.
	 */
	String getId();

	/**
	 * Access the properties keyed by the specified value.
	 * 
	 * @param key
	 *        The key to the properties.
	 * @return The properties keyed by the specified value (possibly empty)
	 */
	ResourceProperties getProperties(String key);

	/**
	 * Access the keys defined in this Preferences
	 * 
	 * @return A Collection of the keys (String) defined in this Preferences.
	 */
	Collection getKeys();
}
