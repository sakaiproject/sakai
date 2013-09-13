/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/access/trunk/access-impl/impl/src/java/org/sakaiproject/access/tool/AccessServlet.java $
 * $Id: AccessServlet.java 17063 2006-10-11 19:48:42Z jimeng@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.api.privacy;

import java.util.Map;
import java.util.Set;
 
/**
 * <p>PrivacyManager maintains and queries user privacy settings</p>
 * 
 * <pre>
 * An implementation can modify the behavior of the Privacy Service within sakai.properties:
 * 
 * privacy.manager.defaultViewable=true|false
 *   a 'true' value will set privacy enabled for a user whose privacy settings are unknown
 *   a 'false' value will set privacy disabled for a user whose privacy settings are unknown
 *   If this value is not set, the default behavior will be to show users or make them viewable.
 *   
 * privacy.manager.overrideViewable=true|false
 *   a 'true' value will make all users viewable in the system
 *   a 'false' value will make all users hidden in the system
 *   Do not set this value for normal operation (non overridden behavior).
 *   
 * privacy.manager.userRecordHasPrecedence=true|false
 *   a 'true' value indicates that a user record has precedence over a system record
 *   a 'false' value indicates that a system record has precedence over a user record
 * 
 * </pre>
 * 
 */
public interface PrivacyManager 
{
	public static final String SYSTEM_RECORD_TYPE = "system_record";
	
	public static final String USER_RECORD_TYPE = "user_record";
	
	public static final String VISIBLE = "privacy_visible";
	public static final String HIDDEN = "privacy_hidden";
	public static final String PRIVACY_PREFS = "sakai:pref:privacy";
	public static final String DEFAULT_PRIVACY_KEY = "default";
	
	// Tool methods ------------------------------------------------------
	
	/**
	 * Determine user privacy within the specified context.
	 * @param contextId
	 * @param userId (UUID)
	 * @return true if privacy is enabled for the user, false otherwise
	 */
	public boolean isViewable(String contextId, String userId);
            
	/**
	 * Determine if a user has specifically made a choice.
	 * @param contextId
	 * @param userId (UUID)
	 * @return true if user made a privacy decision
	 */
	public boolean userMadeSelection(String contextId, String userId);

	
	/**
	 * Get a set of users who have privacy disabled within a context. (Visible)
	 * @param contextId
	 * @param userIds
	 * @return Set of <code>org.sakakproject.service.legacy.User.id</code> objects (UUID)
	 */
	Set findViewable(String contextId, Set userIds);
	
	/**
	 * Get a set of users who have privacy enabled within a context. (Hidden)
	 * @param contextId
	 * @param userIds
	 * @return Set of <code>org.sakakproject.service.legacy.User.id</code> objects (UUID)
	 */
	Set findHidden(String contextId, Set userIds);

	// Batch / Privacy Management methods --------------------------------
	
	/**
	 * Get the state of the users within the specified context filtered by a Boolean value.
	 * Implementation should delegate to AuthzGroupService to get users from Realm. 
	 * @param contextId
	 * @param value filter (True, False, NULL)
	 * @param recordType is a UUID i.e. (getSystemRecordType(), getUserRecordType())
	 * @return Set of users who satisfy the criteria
	 */
	Set getViewableState(String contextId, Boolean value, String recordType);
  
	/**
	 * Get the state of the users within the specified context.
	 * @param contextId
	 * @param recordType is a UUID i.e. (getSystemRecordType(), getUserRecordType())
	 * @return Map {key=userId, value=Boolean}
	 */
	Map getViewableState(String contextId, String recordType);
	
	/**
	 * Set user's privacy setting within the specified context given a record type.
	 * @param contextId
	 * @param userId (UUID)
	 * @param value using three-valued logic (True, False, NULL)
	 * @param recordType is a UUID i.e. (getSystemRecordType(), getUserRecordType())
	 */
	void setViewableState(String contextId, String userId, Boolean value, String recordType);
            
	/**
	 * Set the state for each entry in the userMap.
	 * @param contextId
	 * @param userMap is a Map {key=userId, value=Boolean}
	 * @param recordType is a UUID i.e. (getSystemRecordType(), getUserRecordType())
	 */
	void setViewableState(String contextId, Map userViewableState, String recordType);

	String getDefaultPrivacyState(String userId);

	void setDefaultPrivacyState(String userId, String visibility);
	
}
