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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.DataStoreCredentialRefreshListener;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.googledrive.model.GoogleDriveItem;
import org.sakaiproject.googledrive.model.GoogleDriveUser;
import org.sakaiproject.googledrive.repository.GoogleDriveUserRepository;
import org.sakaiproject.googledrive.repository.JPADataStoreFactory;

/**
 * Implementation of the GoogleDriveService interface.

 * @see GoogleDriveService
 */
@Slf4j
public class GoogleDriveServiceImpl implements GoogleDriveService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

	private final String GOOGLEAPPS_FOLDER_MIMETYPE = "application/vnd.google-apps.folder";

	@Setter private GoogleDriveUserRepository googledriveRepo;

	@Setter
	private ServerConfigurationService serverConfigurationService;

	@Setter
	private UserDirectoryService userDirectoryService;

	@Setter
	private SessionManager sessionManager;

	@Setter
	private MemoryService memoryService;

	private Cache<String, Drive> googledriveUserCache;
	private Cache<String, List<GoogleDriveItem>> driveRootItemsCache;
	private Cache<String, List<GoogleDriveItem>> driveChildrenItemsCache;
	private Cache<String, GoogleDriveItem> driveItemsCache;

	private String redirectUri = null;

	// Multitenant Google instances
	private final String GOOGLE_DEFAULT_ORGANIZATION = null;

	/** Global instance of the JSON factory. */
	private static final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
	/** DataStore to store authorization tokens for this application. */
	private DataStoreFactory dataStore = null;

	private NetHttpTransport httpTransport = null;

	private Map<String, GoogleAuthorizationCodeFlow> googleAuthorizationCodeFlowMap = new HashMap<>();

	public void init() {
		log.debug("GoogleDriveServiceImpl init");
		OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		// Set the portal redirect URI
		redirectUri = serverConfigurationService.getServerUrl() + "/sakai-googledrive-tool";
		log.info("Google redirect URI set to {}", redirectUri);

		String defaultClientId = serverConfigurationService.getString(GOOGLEDRIVE_PREFIX + GOOGLEDRIVE_CLIENT_ID, null);
		String defaultClientSecret = serverConfigurationService.getString(GOOGLEDRIVE_PREFIX + GOOGLEDRIVE_CLIENT_SECRET, null);

		List<String> additionalClientIdList = serverConfigurationService.getStringList(GOOGLEDRIVE_PREFIX + GOOGLEDRIVE_ADDITIONAL + GOOGLEDRIVE_CLIENT_ID, new ArrayList<>());
		List<String> additionalClientSecretList = serverConfigurationService.getStringList(GOOGLEDRIVE_PREFIX + GOOGLEDRIVE_ADDITIONAL + GOOGLEDRIVE_CLIENT_SECRET, new ArrayList<>());
		List<String> additionalOrganizationList = serverConfigurationService.getStringList(GOOGLEDRIVE_PREFIX + GOOGLEDRIVE_ADDITIONAL + GOOGLEDRIVE_ORGANIZATION, new ArrayList<>());

		try {
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			dataStore = new JPADataStoreFactory(googledriveRepo);

			// This initializes the default tenant, all the instance users may use this tenant because their organization is null.
			if (StringUtils.isNoneBlank(defaultClientId, defaultClientSecret)) {
				log.info("Initializing default Google credentials...");
				this.appendGoogleOrganizationCredentials(defaultClientId, defaultClientSecret, GOOGLE_DEFAULT_ORGANIZATION);
				log.info("Default Google credentials initialized....");
			}

			// Initialize all the additional Google Drive Credentials, users must belong to the organization to use these tenants.
			if (additionalClientIdList.size() == additionalClientSecretList.size() &&  additionalClientSecretList.size() == additionalOrganizationList.size() ) {
				int googleDriveOrganizationCount = additionalClientIdList.size();
				log.info("Initializing additional Google credentials for {} organization(s).", googleDriveOrganizationCount);
				for (int i = 0 ; i < googleDriveOrganizationCount ; i++) {
					String additionalClientId = additionalClientIdList.get(i);
					String additionalClientSecret = additionalClientSecretList.get(i);
					String additionalOrganization = additionalOrganizationList.get(i);
					if (StringUtils.isNoneBlank(additionalClientId, additionalClientSecret, additionalOrganization)) {
						this.appendGoogleOrganizationCredentials(additionalClientId, additionalClientSecret, additionalOrganization);
					}
				}
			}
		} catch(Exception e) {
			log.error("Error while trying to init Google Drive configuration. {} ", e.getMessage());
		}

		driveRootItemsCache = memoryService.<String, List<GoogleDriveItem>>getCache("org.sakaiproject.googledrive.service.driveRootItemsCache");
		driveChildrenItemsCache = memoryService.<String, List<GoogleDriveItem>>getCache("org.sakaiproject.googledrive.service.driveChildrenItemsCache");
		googledriveUserCache = memoryService.<String, Drive>getCache("org.sakaiproject.googledrive.service.googledriveUserCache");
		driveItemsCache = memoryService.<String, GoogleDriveItem>getCache("org.sakaiproject.googledrive.service.driveItemsCache");
	}

	// Checks if the user's tenant has Google credentials.
	public boolean isGoogleDriveEnabledForUser() {
		String userId = this.getCurrentUserId();
		String userGoogleOrganization = this.getUserGoogleOrganization(userId);
		boolean isGoogleDriveEnabledForUser = googleAuthorizationCodeFlowMap.get(userGoogleOrganization) != null;
		log.debug("The user {} organization is {}, Google integration is {}.", userId, userGoogleOrganization, isGoogleDriveEnabledForUser ? "ENABLED" : "DISABLED");
		return isGoogleDriveEnabledForUser;
	}

	public String formAuthenticationUrl() {
		log.debug("formAuthenticationUrl");
		if(!this.isGoogleDriveEnabledForUser()) {
			return null;
		}
		String userId = this.getCurrentUserId();
		String userGoogleOrganization = this.getUserGoogleOrganization(userId);
		GoogleAuthorizationCodeFlow flow = this.getGoogleAuthorizationCodeFlow(userGoogleOrganization);
		String redirectTo = flow.newAuthorizationUrl()
				.setRedirectUri(redirectUri)
				.setState(redirectUri)
				.build();

		log.debug("redirectTo : {}", redirectTo);
		return redirectTo;
	}

	public boolean token(String userId, String code) {
		if (!this.isGoogleDriveEnabledForUser()) {
			return false;
		}

		try {
			String userGoogleOrganization = this.getUserGoogleOrganization(userId);
			GoogleAuthorizationCodeFlow flow = this.getGoogleAuthorizationCodeFlow(userGoogleOrganization);
			GoogleTokenResponse googleResponse = flow.newTokenRequest(code)
					.setRedirectUri(redirectUri)
					.execute();
			Credential cred = flow.createAndStoreCredential(googleResponse, userId);

			Drive service = new Drive.Builder(httpTransport, jsonFactory, cred)
				.setApplicationName(GOOGLEDRIVE_APP_NAME)
				.build();

			Drive.About about = service.about();
			Drive.About.Get get = about.get().setFields("user(displayName, emailAddress, permissionId)");
			About ab = get.execute();
			log.debug("About : {}", ab.toString());
			
			GoogleDriveUser du = getGoogleDriveUser(userId);
			du.setGoogleDriveUserId(ab.getUser().getPermissionId());
			du.setGoogleDriveName(ab.getUser().getEmailAddress());
			googledriveRepo.update(du);
			return true;

		} catch(Exception e) {
			log.warn("GoogleDrive: Error while retrieving or saving the credentials for user {} : {}", userId, e.getMessage());
			revokeGoogleDriveConfiguration(userId);
		}
		return false;
	}

	private Drive refreshToken(String userId){
		if(!this.isGoogleDriveEnabledForUser()){
			return null;
		}
		Drive googledriveUser = googledriveUserCache.get(userId);
		if(googledriveUser != null) {
			log.debug("refreshToken : Reusing previous user data {}", googledriveUser);
			return googledriveUser;
		}

		try {
			String userGoogleOrganization = this.getUserGoogleOrganization(userId);
			GoogleAuthorizationCodeFlow flow = this.getGoogleAuthorizationCodeFlow(userGoogleOrganization);
			Credential storedCredential = flow.loadCredential(userId);
			if (storedCredential == null) {
				log.warn("Couldn't retrieve Google credential for user {}", userId);
				return null;
			}

			Credential credential = new Credential.Builder(flow.getMethod())
				.setRequestInitializer(flow.getRequestInitializer())
				.setTokenServerEncodedUrl(flow.getTokenServerEncodedUrl())
				.setClientAuthentication(flow.getClientAuthentication())
				.setTransport(httpTransport)
				.setJsonFactory(jsonFactory)
				.addRefreshListener(new DataStoreCredentialRefreshListener(userId, flow.getCredentialDataStore()))
				.addRefreshListener(new CredentialRefreshListener() {
					public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) {
						log.error("OAuth token refresh error: {}", tokenErrorResponse);
					}
					public void onTokenResponse(Credential credential, TokenResponse tokenResponse) {
						log.debug("OAuth token was refreshed");
					}
				}).build();
			credential.setAccessToken(storedCredential.getAccessToken());
			credential.setRefreshToken(storedCredential.getRefreshToken());

			Drive service = new Drive.Builder(httpTransport, jsonFactory, credential)
				.setApplicationName(GOOGLEDRIVE_APP_NAME)
				.build();

			googledriveUserCache.put(userId, service);
			return service;
		} catch(Exception e) {
			log.warn("GoogleDrive: Error while refreshing or saving the token for user {} : {}", userId, e.getMessage());
		}
		return null;
	}

	public GoogleDriveUser getGoogleDriveUser(String userId) {
		return googledriveRepo.findBySakaiId(userId);
	}

	public List<GoogleDriveItem> getDriveRootItems(String userId) {
		if(!this.isGoogleDriveEnabledForUser()){
			return null;
		}
		List<GoogleDriveItem> cachedItems = driveRootItemsCache.get(userId);
		if(cachedItems != null) {
			log.debug("getDriveRootItems : Returning cached items {} ", cachedItems);
			return cachedItems;
		}
		List<GoogleDriveItem> items = new ArrayList<>();
		try {
			Drive service = this.refreshToken(userId);
			if(service == null){
				return null;
			}

			Drive.Files.List list = service.files().list();
			list.setFields(String.format("nextPageToken, files(%s)", String.join(",", fileFieldsToRequest)));
			list.setOrderBy("folder,name");
			String queryString = "'root' in parents and trashed = false";
			list.setQ(queryString);
			FileList result = list.execute();
			List<File> files = result.getFiles();
			for (File file : files) {
				log.debug("Google data : {}", file);
				GoogleDriveItem gdi = new GoogleDriveItem();
				gdi.setGoogleDriveItemId(file.getId());
				gdi.setName(file.getName());
				gdi.setDownloadUrl(file.getWebContentLink());
				gdi.setViewUrl(file.getWebViewLink());
				gdi.setSize(file.getSize());
				if(GOOGLEAPPS_FOLDER_MIMETYPE.equals(file.getMimeType())) {
					gdi.setFolder(true);
					gdi.setChildren(true);
				}
				gdi.setMimeType(file.getMimeType());
				gdi.setParentId("root");
				gdi.setIcon(file.getIconLink());
				gdi.setThumbnail(file.getThumbnailLink());
				log.debug("Processed data : {}", gdi);
				items.add(gdi);
			}
			driveRootItemsCache.put(userId, items);
		} catch(Exception e) {
			log.error("getDriveRootItems: id {} - error {}", userId, e.getMessage());
		}
		return items;
	}

	public List<GoogleDriveItem> getDriveChildrenItems(String userId, String itemId, int depth) {
		if(!this.isGoogleDriveEnabledForUser()){
			return null;
		}
		String cacheId = userId + "#" + itemId;
		List<GoogleDriveItem> cachedItems = driveChildrenItemsCache.get(cacheId);
		if(cachedItems != null) {
			log.debug("getDriveChildrenItems : Returning cached items {}", cachedItems);
			return cachedItems;
		}

		List<GoogleDriveItem> items = new ArrayList<>();
		try {
			Drive service = this.refreshToken(userId);
			if(service == null){
				return null;
			}

			Drive.Files.List list = service.files().list();
			list.setFields(String.format("nextPageToken, files(%s)", String.join(",", fileFieldsToRequest)));
			list.setOrderBy("folder,name");
			String queryString = "'"+itemId+"' in parents and trashed = false";
			list.setQ(queryString);
			FileList result = list.execute();
			List<File> files = result.getFiles();
			for (File file : files) {
				log.debug("Google data : {}", file);
				GoogleDriveItem gdi = new GoogleDriveItem();
				gdi.setGoogleDriveItemId(file.getId());
				gdi.setName(file.getName());
				gdi.setDownloadUrl(file.getWebContentLink());
				gdi.setViewUrl(file.getWebViewLink());
				gdi.setSize(file.getSize());
				if(GOOGLEAPPS_FOLDER_MIMETYPE.equals(file.getMimeType())) {
					gdi.setFolder(true);
					gdi.setChildren(true);
				}
				gdi.setMimeType(file.getMimeType());
				gdi.setParentId(itemId);
				gdi.setDepth(depth+1);
				gdi.setIcon(file.getIconLink());
				gdi.setThumbnail(file.getThumbnailLink());
				log.debug("Processed data : {}", gdi);
				items.add(gdi);
			}
			driveChildrenItemsCache.put(cacheId, items);
		} catch(Exception e) {
			log.error("getDriveChildrenItems: id {} - error {}", userId, e.getMessage());
		}
		return items;
	}

	public GoogleDriveItem getDriveItem(String userId, String itemId) {
		if(!this.isGoogleDriveEnabledForUser()){
			return null;
		}
		GoogleDriveItem cachedItem = driveItemsCache.get(itemId);
		if(cachedItem != null){
			log.debug("getDriveItem : Returning cached gdi {}", cachedItem);
			return cachedItem;
		}
		GoogleDriveItem gdi = new GoogleDriveItem();
		try {
			Drive service = this.refreshToken(userId);
			if(service == null){
				return null;
			}
			File file = service.files().get(itemId).setFields(String.join(",", fileFieldsToRequest)).execute();
			log.debug("Google data : {}", file);
			gdi.setGoogleDriveItemId(file.getId());
			gdi.setName(file.getName());
			gdi.setDownloadUrl(file.getWebContentLink());
			gdi.setViewUrl(file.getWebViewLink());
			gdi.setSize(file.getSize());
			gdi.setMimeType(file.getMimeType());
			log.debug("Processed data : {}", gdi);
			driveItemsCache.put(itemId, gdi);
		} catch(Exception e) {
			log.error("getDriveItem: item id {} - error {}", itemId, e.getMessage());
		}
		return gdi;
	}

	public InputStream downloadDriveFile(String userId, String itemId) {
		if(!this.isGoogleDriveEnabledForUser()) {
			return null;
		}

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			Drive service = this.refreshToken(userId);
			if(service == null) {
				return null;
			}
			log.debug("Downloading : {}", itemId);
			service.files().get(itemId).executeMediaAndDownloadTo(outputStream);
			try(InputStream in = new ByteArrayInputStream(outputStream.toByteArray())){
				return in;
			}
		} catch(Exception e) {
			log.error("downloadDriveFile: item {} - error {}", itemId, e.getMessage());
		}

		return null;
	}

	public boolean revokeGoogleDriveConfiguration(String userId){
		log.info("revokeGoogleDriveConfiguration for user {}", userId);
		try {
			cleanGoogleDriveCacheForUser(userId);
			String userGoogleOrganization = this.getUserGoogleOrganization(userId);
			GoogleAuthorizationCodeFlow flow = this.getGoogleAuthorizationCodeFlow(userGoogleOrganization);
			DataStore<StoredCredential> credentialStore = flow.getCredentialDataStore();
			return (credentialStore.delete(userId) != null);
		} catch (Exception e) {
			log.warn("Error while trying to remove GoogleDrive configuration : {}", e.getMessage());
		}
		return false;
	}

	public void cleanGoogleDriveCacheForUser(String userId){
		log.debug("cleanGoogleDriveCacheForUser {}", userId);
		// clean caches
		googledriveUserCache.remove(userId);
		driveRootItemsCache.remove(userId);
		driveChildrenItemsCache.clear();
		driveItemsCache.clear();
	}

	// Utility method to request the google secrets file for the tenant.
	private GoogleClientSecrets requestClientSecretsFile(String clientId, String clientSecret, String googleOrganization) {
		JsonNode rootNode = OBJECT_MAPPER.createObjectNode();
		JsonNode webInfo = OBJECT_MAPPER.createObjectNode();
		((ObjectNode) webInfo).put("client_id", clientId);
		((ObjectNode) webInfo).put("auth_uri", ENDPOINT_AUTH);
		((ObjectNode) webInfo).put("token_uri", ENDPOINT_TOKEN);
		((ObjectNode) webInfo).put("auth_provider_x509_cert_url", ENDPOINT_CERTS);
		((ObjectNode) webInfo).put("client_secret", clientSecret);
		((ObjectNode) rootNode).set("web", webInfo);
		try (InputStream isJson = IOUtils.toInputStream(rootNode.toString(), Charset.defaultCharset())) {
			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(isJson));
			log.debug("Loaded secrets for the '{}' organization", this.printOrganization(googleOrganization));
			return clientSecrets;
		} catch (IOException e) {
			log.error("Fatal error getting the client secrets file {}", e.getMessage());
		}
		return null;
	}

	// Initializes the Google Code flow and adds it to the map.
	private void appendGoogleOrganizationCredentials(String clientId, String clientSecret, String googleOrganization) throws Exception {
		log.info("Initializing credentials for the '{}' organization", StringUtils.isBlank(googleOrganization) ? "default" : googleOrganization);
		GoogleClientSecrets clientSecrets = this.requestClientSecretsFile(clientId, clientSecret, googleOrganization);
		if (clientSecrets == null) {
			log.error("Error getting the client secrets file for the organization '{}'", this.printOrganization(googleOrganization));
			return;
		}
		log.debug("Saved secrets for the '{}' organization", this.printOrganization(googleOrganization));
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, Collections.singleton(DriveScopes.DRIVE))
				.setDataStoreFactory(dataStore)
				.setApprovalPrompt("force")
				.setAccessType("offline")
				.build();
		log.debug("Flow built for the '{}' organization", this.printOrganization(googleOrganization));
		googleAuthorizationCodeFlowMap.put(googleOrganization, flow);
		log.debug("Saved flow for the '{}' organization", this.printOrganization(googleOrganization));
		log.info("Credentials for the '{}' organization initialized", this.printOrganization(googleOrganization));
	}

	// Utility method for pretty logging using 'default' instead of null organization.
	private String printOrganization(String googleOrganization) {
		return StringUtils.isBlank(googleOrganization) ? "default" : googleOrganization;
	}

	private GoogleAuthorizationCodeFlow getGoogleAuthorizationCodeFlow(String googleOrganization) {
		log.debug("Getting GoogleAuthorizationCodeFlow for organization '{}'", this.printOrganization(googleOrganization));
		return googleAuthorizationCodeFlowMap.get(googleOrganization);
	}

	private String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}

	// Utility method to retrieve the user's google organization.
	private String getUserGoogleOrganization(String userId) {
		log.debug("Getting google organization for user {}", userId);
		try {
			Set<String> googleOrganizations = googleAuthorizationCodeFlowMap.keySet();
			User user = userDirectoryService.getUser(userId);
			String userEid = user.getEid();
			String userEmail = user.getEmail();
			if (StringUtils.isNotBlank(userEid)) {
				// Try the userEid first, it could be an email that ends with the google domain
				String[] userEidArray = userEid.split("@");
				if (userEidArray.length == 2 && googleOrganizations.contains(userEidArray[1])) {
					log.debug("User eid {} contains the organization {}", userEid, userEidArray[1]);
					return userEidArray[1];
				}
			}
			if (StringUtils.isNotBlank(userEmail)) {
				// Try the userEid first
				String[] userEmailArray = userEmail.split("@");
				if (userEmailArray.length == 2 && googleOrganizations.contains(userEmailArray[1])) {
					log.debug("User email {} contains the organization {}", userEmail, userEmailArray[1]);
					return userEmailArray[1];
				}
			}
		} catch (Exception e) {
			log.error("Error getting the user organization for {}, assigning the default organization: {}", userId, e.getMessage());
		}
		log.debug("User {} does not match with any organization, returning the default.", userId);
		return GOOGLE_DEFAULT_ORGANIZATION;
	}

}
