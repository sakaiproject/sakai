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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
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

	@Setter private GoogleDriveUserRepository googledriveRepo;

	@Getter @Setter
	private ServerConfigurationService serverConfigurationService;

	@Getter @Setter
	private MemoryService memoryService;

	private Cache<String, Drive> googledriveUserCache;
	private Cache<String, List<GoogleDriveItem>> driveRootItemsCache;
	private Cache<String, List<GoogleDriveItem>> driveChildrenItemsCache;
	private Cache<String, GoogleDriveItem> driveItemsCache;

	private String clientId = null;
	private String clientSecret = null;
	private String redirectUri = null;

	private HttpTransport httpTransport = null;
	private JacksonFactory jsonFactory = null;
	private GoogleAuthorizationCodeFlow flow = null;
	private Drive service = null;
	private GoogleClientSecrets clientSecrets = null;

	public void init() {
		log.debug("GoogleDriveServiceImpl init");
		OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		clientId = serverConfigurationService.getString(GOOGLEDRIVE_PREFIX + GOOGLEDRIVE_CLIENT_ID, null);
		clientSecret = serverConfigurationService.getString(GOOGLEDRIVE_PREFIX + GOOGLEDRIVE_CLIENT_SECRET, null);
		redirectUri = serverConfigurationService.getString(GOOGLEDRIVE_PREFIX + GOOGLEDRIVE_REDIRECT_URI, "http://localhost:8080/sakai-googledrive-tool");

		try {
			JsonNode rootNode = OBJECT_MAPPER.createObjectNode();
			JsonNode webInfo = OBJECT_MAPPER.createObjectNode();
			((ObjectNode) webInfo).put("client_id", clientId);
			//((ObjectNode) webInfo).put("project_id", "algebraic-creek-243007");
			((ObjectNode) webInfo).put("auth_uri", ENDPOINT_AUTH);
			((ObjectNode) webInfo).put("token_uri", ENDPOINT_TOKEN);
			((ObjectNode) webInfo).put("auth_provider_x509_cert_url", ENDPOINT_CERTS);
			((ObjectNode) webInfo).put("client_secret", clientSecret);
			((ObjectNode) rootNode).set("web", webInfo);
			InputStream isJson = IOUtils.toInputStream(rootNode.toString());
			isJson.close();//?

			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			jsonFactory = JacksonFactory.getDefaultInstance();
			clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(isJson));

			DataStoreFactory dataStore = new JPADataStoreFactory(googledriveRepo);			
			flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, Collections.singleton(DriveScopes.DRIVE))
					.setDataStoreFactory(dataStore)
					.setApprovalPrompt("force")
					.setAccessType("offline")
					.build();
		} catch(Exception e) {
			log.error("Error while trying to init Google Drive configuration");
		}

		driveRootItemsCache = memoryService.<String, List<GoogleDriveItem>>getCache("org.sakaiproject.googledrive.service.driveRootItemsCache");
		driveChildrenItemsCache = memoryService.<String, List<GoogleDriveItem>>getCache("org.sakaiproject.googledrive.service.driveChildrenItemsCache");
		googledriveUserCache = memoryService.<String, Drive>getCache("org.sakaiproject.googledrive.service.googledriveUserCache");
		driveItemsCache = memoryService.<String, GoogleDriveItem>getCache("org.sakaiproject.googledrive.service.driveItemsCache");
	}

	public String formAuthenticationUrl() {
		log.debug("formAuthenticationUrl");
		if(!isConfigured()){
			return null;
		}

		String redirectTo = flow.newAuthorizationUrl()
				.setRedirectUri(redirectUri)
				.setState(redirectUri)
				.build();

		log.debug("redirectTo : {}", redirectTo);
		return redirectTo;
	}

	public boolean token(String userId, String code) {
		if (!isConfigured()) {
			return false;
		}

		try {
			GoogleTokenResponse googleResponse = flow.newTokenRequest(code)
					.setRedirectUri(redirectUri)
					.execute();
			Credential cred = flow.createAndStoreCredential(googleResponse, userId);

			service = new Drive.Builder(httpTransport, jsonFactory, cred)
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

	public Drive refreshToken(String userId){
		if(!isConfigured()){
			return null;
		}
		Drive googledriveUser = googledriveUserCache.get(userId);
		if(googledriveUser != null) {
			log.debug("refreshToken : Reusing previous user data {}", googledriveUser);
			return googledriveUser;
		}

		try {
			Credential storedCredential = flow.loadCredential(userId);
			if (storedCredential == null) {
				log.warn("Couldn't retrieve Google credential for user {}", userId);
				return null;
			}

			// This gives us is the ability to update our stored
			// credentials as they get refreshed (using the
			// DataStoreCredentialRefreshListener).
			GoogleCredential credential = new GoogleCredential.Builder()
					.setTransport(httpTransport)
					.setJsonFactory(jsonFactory)
					.setClientSecrets(clientSecrets)
					.addRefreshListener(new CredentialRefreshListener() {
						public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) {
							log.error("OAuth token refresh error: {}", tokenErrorResponse);
						}

						public void onTokenResponse(Credential credential, TokenResponse tokenResponse) {
							log.debug("OAuth token was refreshed");
						}
					})
					.addRefreshListener(new DataStoreCredentialRefreshListener(userId, flow.getCredentialDataStore()))
					.build();
			credential.setRefreshToken(storedCredential.getRefreshToken());
			credential.setAccessToken(storedCredential.getAccessToken());
			log.debug("credential.getServiceAccountUser() : {}", credential.getServiceAccountUser());

			service = new Drive.Builder(httpTransport, jsonFactory, credential)
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
		if(!isConfigured()){
			return null;
		}
		List<GoogleDriveItem> cachedItems = driveRootItemsCache.get(userId);
		if(cachedItems != null) {
			log.debug("getDriveRootItems : Returning cached items {} ", cachedItems);
			return cachedItems;
		}
		List<GoogleDriveItem> items = new ArrayList<>();
		try {
			service = refreshToken(userId);
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
				if("application/vnd.google-apps.folder".equals(file.getMimeType())) {
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
		if(!isConfigured()){
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
			service = refreshToken(userId);
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
				if("application/vnd.google-apps.folder".equals(file.getMimeType())) {
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
		if(!isConfigured()){
			return null;
		}
		GoogleDriveItem cachedItem = driveItemsCache.get(itemId);
		if(cachedItem != null){
			log.debug("getDriveItem : Returning cached gdi {}", cachedItem);
			return cachedItem;
		}
		GoogleDriveItem gdi = new GoogleDriveItem();
		try {
			service = refreshToken(userId);
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
		if(!isConfigured()) {
			return null;
		}

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			service = refreshToken(userId);
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

	private boolean isConfigured(){
		if (StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret) || StringUtils.isBlank(redirectUri) || clientSecrets == null || flow == null) {
			log.warn("GOOGLEDRIVE CONFIGURATION IS MISSING");
			return false;
		}
		return true;
	}

	public boolean revokeGoogleDriveConfiguration(String userId){
		log.info("revokeGoogleDriveConfiguration for user {}", userId);
		try {
			cleanGoogleDriveCacheForUser(userId);
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

}
