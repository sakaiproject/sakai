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
package org.sakaiproject.googledrive.service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.sakaiproject.googledrive.model.GoogleDriveItem;
import org.sakaiproject.googledrive.model.GoogleDriveUser;

/**
 * Interface for communicating with the GoogleDrive API.
 */
public interface GoogleDriveService {

	// GOOGLEDRIVE CONSTANTS
	public final String GOOGLEDRIVE_PREFIX = "googledrive.";
	public final String GOOGLEDRIVE_ENABLED = GOOGLEDRIVE_PREFIX + "enabled";
	public final String GOOGLEDRIVE_CLIENT_ID = "client_id";
	public final String GOOGLEDRIVE_CLIENT_SECRET = "client_secret";
	public final String GOOGLEDRIVE_REDIRECT_URI = "redirect_uri";
	public final String GOOGLEDRIVE_APP_NAME = "SAKAI-GOOGLE-DRIVE";

	public final List<String> fileFieldsToRequest = Arrays.asList("id", "name", "mimeType", "webViewLink", "webContentLink", "size", "iconLink", "thumbnailLink"); //, "exportLinks", "permissions");

	// ENDPOINTS
	public final String ENDPOINT_AUTH = "https://accounts.google.com/o/oauth2/auth";
	public final String ENDPOINT_CERTS = "https://www.googleapis.com/oauth2/v1/certs";
	public final String ENDPOINT_TOKEN = "https://oauth2.googleapis.com/token";

	// FUNCTIONS
	public String formAuthenticationUrl();
	public List<GoogleDriveItem> getDriveRootItems(String userId);
	public List<GoogleDriveItem> getDriveChildrenItems(String userId, String itemId, int depth);
	public GoogleDriveUser getGoogleDriveUser(String userId);
	public boolean token(String userId, String code);
	public boolean revokeGoogleDriveConfiguration(String userId);
	public void cleanGoogleDriveCacheForUser(String userId);
	public GoogleDriveItem getDriveItem(String userId, String itemId);
	public InputStream downloadDriveFile(String userId, String itemId);
}
