/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.onedrive.service;

import java.util.List;

import org.sakaiproject.onedrive.model.OneDriveItem;
import org.sakaiproject.onedrive.model.OneDriveUser;

/**
 * Interface for communicating with the OneDrive API.
 */
public interface OneDriveService {

	// ONEDRIVE CONSTANTS
	public final String ONEDRIVE_PREFIX = "onedrive.";
	public final String ONEDRIVE_ENABLED = ONEDRIVE_PREFIX + "enabled";
	public final String ONEDRIVE_CLIENT_ID = "client_id";
	public final String ONEDRIVE_CLIENT_SECRET = "client_secret";
	public final String ONEDRIVE_CODE = "code";
	public final String ONEDRIVE_ENDPOINT_URI = "endpoint_uri";
	public final String ONEDRIVE_GRANT_TYPE = "grant_type";
	public final String ONEDRIVE_GRANT_TYPE_DEFAULT = "authorization_code";
	public final String ONEDRIVE_REDIRECT_URI = "redirect_uri";
	public final String ONEDRIVE_REFRESH_TOKEN = "refresh_token";
	public final String ONEDRIVE_RESPONSE_MODE = "response_mode";
	public final String ONEDRIVE_RESPONSE_MODE_DEFAULT = "query";
	public final String ONEDRIVE_RESPONSE_TYPE = "response_type";
	public final String ONEDRIVE_RESPONSE_TYPE_DEFAULT = "code";
	public final String ONEDRIVE_SCOPE = "scope";
	public final String ONEDRIVE_SCOPE_DEFAULT_VALUES = "offline_access user.read files.read.all";//all in one variable, separate if necessary
	public final String ONEDRIVE_STATE = "state";

	// ENDPOINTS
	public final String ENDPOINT_AUTHORIZE = "authorize";
	public final String ENDPOINT_GRAPH = "https://graph.microsoft.com/v1.0/";
	public final String ENDPOINT_COMMON_LOGIN = "https://login.microsoftonline.com/common/oauth2/v2.0/";
	public final String ENDPOINT_DRIVES = "drives/";
	public final String ENDPOINT_ME = "me";
	public final String ENDPOINT_CHILDREN = "/children";
	public final String ENDPOINT_ITEMS = "/items/";
	public final String ENDPOINT_ROOT_CHILDREN = "/root/children";
	public final String ENDPOINT_TOKEN = "token";
	public final String JSON_ENTRY_VALUE = "value";
	public final String Q_MARK = "?";
	public final String SEPARATOR = "/";

	// FUNCTIONS
	public String formAuthenticationUrl();
	public List<OneDriveItem> getDriveRootItems(String userId);
	public List<OneDriveItem> getDriveChildrenItems(String userId, String itemId, int depth);
	public OneDriveUser getOneDriveUser(String userId);
	public OneDriveUser refreshToken(String userId);
	public boolean token(String userId, String code);
	public boolean revokeOneDriveConfiguration(String userId);
	public void cleanOneDriveCacheForUser(String userId);

}
