/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
