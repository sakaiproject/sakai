/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
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

package org.sakaiproject.poll.api.logic;

import java.util.TimeZone;

import org.sakaiproject.tool.api.ToolSession;

public interface ExternalLogic {

	/**
	 * Check if this user has super admin access
	 * 
	 * @param userId the internal user id (not username)
	 * @return true if the user has admin access, false otherwise
	 */
	public boolean isUserAdmin(String userId);
	
	/**
	 * Check if the current user has super admin access
	 * 
	 * @param userId the internal user id (not username)
	 * @return true if the user has admin access, false otherwise
	 */
	public boolean isUserAdmin();

	/**
	 * @return the current location id of the current user
	 */
	public String getCurrentLocationId();

	/**
	 * @return the current location reference of the current user
	 */
	public String getCurrentLocationReference();
	
	/**
	 * @return the current tool URL of the current user
	 */
	String getCurrentToolURL();
	
	/**
	 * @return the current sakai user id (not username)
	 */
	public String getCurrentUserId();

    /**
	 * is the current user allowed to perform the action in the current location?
	 * @param permission
	 * @param locationReference
	 */
	public boolean isAllowedInLocation(String permission, String locationReference);
	
	/**
	 * is the current user allowed to perform the action in the current location?
	 * @param permission
	 * @param locationReference
	 * @param the user
	 */
	public boolean isAllowedInLocation(String permission, String locationReference, String userRefence);

    /**
	 *  get the correct Timezone for the the current user
	 * @return
	 */
	public TimeZone getLocalTimeZone();

    /**
	 * Needed to invoke helper tools
	 */
	public ToolSession getCurrentToolSession();

    /**
	 * Are the public access options available to the Instructor?
	 * poll.allow.public.access=true|false, default false
	 * @return
	 */
	public boolean isShowPublicAccess();

    /**
     * Get the total number of users that have permission for voting
     */
    public int getNumberUsersCanVote();

    /**
     * @param text
     * @param errorMessages
     * @return
     */
    public String processFormattedText(String text, StringBuilder errorMessages);
    
    /**
     * @param strFromBrowser
     * @param errorMessages
     * @param checkForEvilTags
     * @param replaceWhitespaceTags
     * @return
     */
    public String processFormattedText(String strFromBrowser, StringBuilder errorMessages,
            boolean checkForEvilTags,
            boolean replaceWhitespaceTags);

}
