/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.service.message.api;

/**
 * A service to express user preferences at a context
 * 
 * @author ieb
 */
public interface PreferenceService
{
	/**
	 * A preference type to express mail notification preference for the user on
	 * a context
	 */
	public static final String MAIL_NOTIFCIATION = "mail.notify";

	public static final String NONE_PREFERENCE = "none";

	public static final String DIGEST_PREFERENCE = "digest";

	public static final String SEPARATE_PREFERENCE = "separate";

	/**
	 * Update the users preference
	 * 
	 * @param user
	 * @param context
	 *        the path to the node in the wiki space where the preference is
	 *        bein expressed
	 * @param type
	 *        or preference
	 * @param perference
	 */
	void updatePreference(String user, String context, String type,
			String perference);

	/**
	 * Locate the most applicable preference at a given context of a given type
	 * 
	 * @param user
	 *        the userid
	 * @param context
	 *        the path to the context
	 * @param type
	 *        the type of preference
	 * @return
	 */
	String findPreferenceAt(String user, String context, String type);

	/**
	 * Removes a preference and all sub prefrences
	 * 
	 * @param user
	 * @param context
	 * @param type
	 */
	void deleteAllPreferences(String user, String context, String type);

	/**
	 * Removes a preference
	 * 
	 * @param user
	 * @param context
	 * @param type
	 */
	void deletePreference(String user, String context, String type);
}
