/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.logic;

import java.util.Map;

import org.sakaiproject.profile2.model.ExternalIntegrationInfo;

/**
 * An interface for dealing with external integrations in Profile2
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
public interface ProfileExternalIntegrationLogic {

	/**
	 * Get the ExternalIntegrationInfo record for a user or an empty record if none.
	 * @param userUuid
	 * @return
	 */
	public ExternalIntegrationInfo getExternalIntegrationInfo(final String userUuid);
	
	/**
	 * Update a user's ExternalIntegrationInfo
	 * @param info ExternalIntegrationInfo object for the user
	 * @return
	 */
	public boolean updateExternalIntegrationInfo(ExternalIntegrationInfo info);

	/**
	 * Returns a map of the Twitter OAuth consumer 'key' and 'secret'
	 * @return
	 */
	public Map<String,String> getTwitterOAuthConsumerDetails();

	/**
	 * Gets the Twitter name associated with the stored details, if any.
	 * @param info  ExternalIntegrationInfo object for the user
	 * @return name or null if none/error
	 */
	public String getTwitterName(ExternalIntegrationInfo info);

	/**
	 * Check if the stored Twitter credentials are valid.
	 * @param info   ExternalIntegrationInfo object for the user
	 * @return true if valid, false if not/error
	 */
	public boolean validateTwitterCredentials(ExternalIntegrationInfo info);

	/**
	 * Send a message to twitter ( runs in a separate thread)
	 * 
	 * <p>Will only run if twitter integration is enabled globally (ie via sakai.properties)
	 * and if the user has linked their account.</p>
	 * 
	 * <p>Messages longer than 140 chars will be truncated to 140 chars. This is also validated client side.</p>
	 *
	 * @param userUuid	uuid of the user
	 * @param message	the message
	 */
	public void sendMessageToTwitter(final String userUuid, String message);
	
	/**
	 * Generate the authentication URL we need to use to present to the user 
	 * @return url or null if not properly configured
	 */
	public String getGoogleAuthenticationUrl();
	
}
