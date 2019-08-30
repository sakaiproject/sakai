package org.sakaiproject.googledrive.service;

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

}
