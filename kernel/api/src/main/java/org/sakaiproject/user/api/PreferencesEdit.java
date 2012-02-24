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

import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;

/**
 * <p>
 * PreferencesEdit is a mutable Preferences.
 * </p>
 */
public interface PreferencesEdit extends Preferences, Edit
{
	/**
	 * Access the properties keyed by the specified value. If the key does not yet exist, create it.
	 * 
	 * @param key
	 *        The key to the properties.
	 * @return The properties keyed by the specified value (possibly empty)
	 */
	ResourcePropertiesEdit getPropertiesEdit(String key);
}
