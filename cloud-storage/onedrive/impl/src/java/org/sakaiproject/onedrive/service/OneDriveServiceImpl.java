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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.NameValuePair;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.onedrive.model.OneDriveItem;
import org.sakaiproject.onedrive.model.OneDriveToken;
import org.sakaiproject.onedrive.model.OneDriveUser;
import org.sakaiproject.onedrive.repository.OneDriveUserRepository;
import org.sakaiproject.onedrive.util.HTTPConnectionUtil;

/**
 * Implementation of the OneDriveService interface.

 * @see OneDriveService
 */
@Slf4j
public class OneDriveServiceImpl implements OneDriveService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

	@Setter private OneDriveUserRepository onedriveRepo;

	@Getter @Setter
	private ServerConfigurationService serverConfigurationService;

	@Getter @Setter
	private MemoryService memoryService;

	private Cache<String, String> tokenCache;
	private Cache<String, OneDriveUser> onedriveUserCache;
	private Cache<String, List<OneDriveItem>> driveRootItemsCache;
	private Cache<String, List<OneDriveItem>> driveChildrenItemsCache;

	private String clientId = null;
	private String clientSecret = null;
	private String redirectUri = null;
	private static final String state = ONEDRIVE_STATE;
	private String bearer = null;

	public void init() {
		log.debug("OneDriveServiceImpl init");
		OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		clientId = serverConfigurationService.getString(ONEDRIVE_PREFIX + ONEDRIVE_CLIENT_ID, null);
		clientSecret = serverConfigurationService.getString(ONEDRIVE_PREFIX + ONEDRIVE_CLIENT_SECRET, null);
		redirectUri = serverConfigurationService.getString(ONEDRIVE_PREFIX + ONEDRIVE_REDIRECT_URI, "http://localhost:8080/sakai-onedrive-tool");

		tokenCache = memoryService.<String, String>getCache("org.sakaiproject.onedrive.service.tokenCache");
		driveRootItemsCache = memoryService.<String, List<OneDriveItem>>getCache("org.sakaiproject.onedrive.service.driveRootItemsCache");
		driveChildrenItemsCache = memoryService.<String, List<OneDriveItem>>getCache("org.sakaiproject.onedrive.service.driveChildrenItemsCache");
		onedriveUserCache = memoryService.<String, OneDriveUser>getCache("org.sakaiproject.onedrive.service.onedriveUserCache");
	}

	public String formAuthenticationUrl() {
		log.debug("formAuthenticationUrl");
		if(!isConfigured()){
			return null;
		}
	
		Map<String,String> params = new HashMap<>();
		params.put(ONEDRIVE_CLIENT_ID, clientId);
		params.put(ONEDRIVE_RESPONSE_MODE, ONEDRIVE_RESPONSE_MODE_DEFAULT);
		params.put(ONEDRIVE_RESPONSE_TYPE, ONEDRIVE_RESPONSE_TYPE_DEFAULT);
		params.put(ONEDRIVE_SCOPE, ONEDRIVE_SCOPE_DEFAULT_VALUES);
		params.put(ONEDRIVE_STATE, state);
		params.put(ONEDRIVE_REDIRECT_URI, redirectUri);
		String authUrl = ENDPOINT_LOGIN + ENDPOINT_AUTHORIZE + Q_MARK + HTTPConnectionUtil.formUrlencodedString(params);
		log.debug("authUrl : {}", authUrl);
		return authUrl;
	}

	public boolean token(String userId, String code) {
		if (!isConfigured()) {
			return false;
		}
		String prevToken = tokenCache.get(userId);
		if(prevToken != null) {
			log.debug("token : Reusing previous token {}", prevToken);
			return true;
		}
		
		Map<String,String> params = new HashMap<>();
		params.put(ONEDRIVE_CLIENT_ID, clientId);
		params.put(ONEDRIVE_SCOPE, ONEDRIVE_SCOPE_DEFAULT_VALUES);
		params.put(ONEDRIVE_CODE, code);
		params.put(ONEDRIVE_GRANT_TYPE, ONEDRIVE_GRANT_TYPE_DEFAULT);
		params.put(ONEDRIVE_CLIENT_SECRET, clientSecret);
		params.put(ONEDRIVE_REDIRECT_URI, redirectUri);
		try {
			StringEntity entity = new StringEntity(HTTPConnectionUtil.formUrlencodedString(params));
			String postResponse = HTTPConnectionUtil.makePostCall(ENDPOINT_LOGIN + ENDPOINT_TOKEN, entity);
			log.debug(postResponse);
			OneDriveToken ot = OBJECT_MAPPER.readValue(postResponse, OneDriveToken.class);
			log.debug(ot.toString());
			if(ot == null || ot.getCurrentToken() == null){
				log.warn("OneDrive: Error retrieving token for user {} : {}", userId, postResponse);
				return false;
			}
			bearer = ot.getCurrentToken();
			OneDriveUser ou = getCurrentDriveUser();
			ou.setSakaiUserId(userId);
			ou.setToken(ot.getCurrentToken());
			ou.setRefreshToken(ot.getRefreshToken());
			log.debug(ou.toString());
			ou = onedriveRepo.save(ou);
			if(ou != null){
				tokenCache.put(userId, ot.getCurrentToken());
				return true;
			}
		} catch(Exception e) {
			log.warn("OneDrive: Error while retrieving or saving the token for user {} : {}", userId, e.getMessage());
		}
		return false;
	}

	public OneDriveUser refreshToken(String userId){
		if(!isConfigured()){
			return null;
		}
		OneDriveUser onedriveUser = onedriveUserCache.get(userId);
		if(onedriveUser != null) {
			log.debug("refreshToken : Reusing previous user data {}", onedriveUser);
			return onedriveUser;
		}
		
		OneDriveUser ou = getOneDriveUser(userId);
		if(ou == null){
			log.debug("No OneDrive account found for user {}", userId);
			return null;
		}
		log.debug(ou.toString());
		Map<String,String> params = new HashMap<>();
		params.put(ONEDRIVE_CLIENT_ID, clientId);
		params.put(ONEDRIVE_SCOPE, ONEDRIVE_SCOPE_DEFAULT_VALUES);
		params.put(ONEDRIVE_REFRESH_TOKEN, ou.getRefreshToken());
		params.put(ONEDRIVE_GRANT_TYPE, ONEDRIVE_REFRESH_TOKEN);
		params.put(ONEDRIVE_CLIENT_SECRET, clientSecret);
		params.put(ONEDRIVE_REDIRECT_URI, redirectUri);
		try {
			StringEntity entity = new StringEntity(HTTPConnectionUtil.formUrlencodedString(params));
			String postResponse = HTTPConnectionUtil.makePostCall(ENDPOINT_LOGIN + ENDPOINT_TOKEN, entity);
			log.debug(postResponse);
			OneDriveToken ot = OBJECT_MAPPER.readValue(postResponse, OneDriveToken.class);
			log.debug(ot.toString());
			if(ot == null || ot.getCurrentToken() == null){
				log.warn("OneDrive: Error refreshing token for user {} : {}", userId, postResponse);
				return null;
			}
			bearer = ot.getCurrentToken();
			ou.setToken(ot.getCurrentToken());
			log.debug(ou.toString());
			onedriveRepo.update(ou);
			tokenCache.put(userId, ot.getCurrentToken());
			onedriveUserCache.put(userId, ou);
			return ou;
		} catch(Exception e) {
			log.warn("OneDrive: Error while refreshing or saving the token for user {} : {}", userId, e.getMessage());
		}
		return null;
	}

	private OneDriveUser getCurrentDriveUser(){
		if(!isConfigured()){
			return null;
		}
		OneDriveUser ou = null;
		try {
			List<NameValuePair> params = new ArrayList<>();
			String getResponse = HTTPConnectionUtil.makeGetCall(ENDPOINT_GRAPH + ENDPOINT_ME, params, bearer);
			ou = OBJECT_MAPPER.readValue(getResponse, OneDriveUser.class);
		} catch(Exception e) {
			log.error("getCurrentDriveUser: {}", e.getMessage());
		}
		return ou;
	}

	public OneDriveUser getOneDriveUser(String userId) {
		return onedriveRepo.findBySakaiId(userId);
	}

	public List<OneDriveItem> getDriveRootItems(String userId) {
		if(!isConfigured()){
			return null;
		}
		List<OneDriveItem> cachedItems = driveRootItemsCache.get(userId);
		if(cachedItems != null) {
			log.debug("getDriveRootItems : Returning cached items {}", cachedItems);
			return cachedItems;
		}
		try {
			OneDriveUser ou = refreshToken(userId);
			if(ou == null){
				return null;
			}
			List<NameValuePair> params = new ArrayList<>();
			bearer = ou.getToken();
			String getResponse = HTTPConnectionUtil.makeGetCall(ENDPOINT_GRAPH + ENDPOINT_DRIVES + ou.getOneDriveUserId() + ENDPOINT_ROOT_CHILDREN, params, bearer);
			JsonNode jsonNode = OBJECT_MAPPER.readValue(getResponse, JsonNode.class);
			JsonNode valueNode = jsonNode.get(JSON_ENTRY_VALUE);
			if(valueNode == null) {
				log.warn("Couldn't retrieve root items for user id {} : response is {}", userId, getResponse);
				return null;
			}
			List<OneDriveItem> items = OBJECT_MAPPER.readValue(valueNode.toString(), OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, OneDriveItem.class));
			driveRootItemsCache.put(userId, items);
			return items;
		} catch(Exception e) {
			log.error("getDriveRootItems: id {} - error {}", userId, e.getMessage());
		}
		return null;
	}

	public List<OneDriveItem> getDriveChildrenItems(String userId, String itemId, int depth) {
		if(!isConfigured()){
			return null;
		}
		String cacheId = userId + "#" + itemId;
		List<OneDriveItem> cachedItems = driveChildrenItemsCache.get(cacheId);
		if(cachedItems != null) {
			log.debug("getDriveChildrenItems : Returning cached items " + cachedItems);
			return cachedItems;
		}
		try {
			OneDriveUser ou = refreshToken(userId);
			if(ou == null){
				return null;
			}
			List<NameValuePair> params = new ArrayList<>();
			bearer = ou.getToken();
			String getResponse = HTTPConnectionUtil.makeGetCall(ENDPOINT_GRAPH + ENDPOINT_DRIVES + ou.getOneDriveUserId() + ENDPOINT_ITEMS + itemId + ENDPOINT_CHILDREN, params, bearer);
			JsonNode jsonNode = OBJECT_MAPPER.readValue(getResponse, JsonNode.class);
			JsonNode valueNode = jsonNode.get(JSON_ENTRY_VALUE);
			if(valueNode == null) {
				log.warn("Couldn't retrieve children items for item id {} : response is {}", itemId, getResponse);
				return null;
			}
			List<OneDriveItem> items = OBJECT_MAPPER.readValue(valueNode.toString(), OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, OneDriveItem.class));
			items.forEach(it -> it.setDepth(depth+1));
			driveChildrenItemsCache.put(cacheId, items);
			return items;			
		} catch(Exception e) {
			log.error("getDriveChildrenItems: id {} - error {}", userId, e.getMessage());
		}
		return null;
	}

	private boolean isConfigured(){
		if (StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret) || StringUtils.isBlank(redirectUri)) {
			log.warn("ONEDRIVE CONFIGURATION IS MISSING");
			return false;
		}
		return true;
	}

	public boolean revokeOneDriveConfiguration(String userId){
		log.info("revokeOneDriveConfiguration for user {}", userId);
		try {
			// delete onedrive user ddbb entry
			onedriveRepo.delete(getOneDriveUser(userId).getOneDriveUserId());			
			cleanOneDriveCacheForUser(userId);
			return true;
		} catch (Exception e) {
			log.warn("Error while trying to remove OneDrive configuration : {}", e.getMessage());
		}
		return false;
	}

	public void cleanOneDriveCacheForUser(String userId){
		log.debug("cleanOneDriveCacheForUser {}", userId);
		// clean caches
		tokenCache.remove(userId);
		onedriveUserCache.remove(userId);
		driveRootItemsCache.remove(userId);
		driveChildrenItemsCache.clear();
	}

}
