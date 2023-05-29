/**
* Copyright (c) 2023 Apereo Foundation
* 
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*             http://opensource.org/licenses/ecl2
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.microsoft.api;

import org.sakaiproject.microsoft.api.data.MicrosoftRedirectURL;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftCredentialsException;
import org.sakaiproject.microsoft.api.model.MicrosoftAccessToken;

public interface MicrosoftAuthorizationService {
	
	// AUTHORIZATION CONSTANTS
	public final String ENDPOINT_AUTHORIZE = "oauth2/v2.0/authorize";
	public final String Q_MARK = "?";
	
	public final String CLIENT_ID = "client_id";
	public final String CLIENT_SECRET = "client_secret";

	public final String SCOPE = "scope";
	public final String SCOPE_DEFAULT = "https://graph.microsoft.com/.default";
	public final String DELEGATED_SCOPE_DEFAULT = "User.Read Files.Read.All";
	public final String OFFLINE_ACCESS_SCOPE = "offline_access";
	
	public final String STATE = "state";

	public final String REDIRECT_URI = "redirect_uri";
	public final String REDIRECT_TOKEN_ENDPOINT = "/microsoft-authorization/token";
	
	public final String RESPONSE_MODE = "response_mode";
	public final String RESPONSE_MODE_DEFAULT = "query";
	
	public final String RESPONSE_TYPE = "response_type";
	public final String RESPONSE_TYPE_DEFAULT = "code";
	
	public final String MICROSOFT_SESSION_REDIRECT = "microsoft_session_redirect";
	
	public void resetCache();
	
	Object getDelegatedGraphClient(String userId) throws MicrosoftCredentialsException;
	void checkDelegatedClient(String userId) throws MicrosoftCredentialsException;
	
	MicrosoftRedirectURL getAuthenticationUrl() throws MicrosoftCredentialsException;
	
	boolean processAuthorizationCode(String userId, String authorizationCode);
	
	void saveAccessToken(MicrosoftAccessToken accessToken);
	MicrosoftAccessToken getAccessToken(String userId);
	boolean revokeAccessToken(String userId);
}