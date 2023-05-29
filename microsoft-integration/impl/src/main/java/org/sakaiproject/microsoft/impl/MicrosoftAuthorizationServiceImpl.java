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
package org.sakaiproject.microsoft.impl;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.sakaiproject.microsoft.api.MicrosoftAuthorizationService;
import org.sakaiproject.microsoft.api.SakaiProxy;
import org.sakaiproject.microsoft.api.data.MicrosoftAuthorizationAccount;
import org.sakaiproject.microsoft.api.data.MicrosoftCredentials;
import org.sakaiproject.microsoft.api.data.MicrosoftRedirectURL;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftCredentialsException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftInvalidCredentialsException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftInvalidTokenException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftNoCredentialsException;
import org.sakaiproject.microsoft.api.model.MicrosoftAccessToken;
import org.sakaiproject.microsoft.api.persistence.MicrosoftAccessTokenRepository;
import org.sakaiproject.microsoft.api.persistence.MicrosoftConfigRepository;
import org.sakaiproject.microsoft.provider.DelegatedAuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.graph.requests.GraphServiceClient;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class MicrosoftAuthorizationServiceImpl implements MicrosoftAuthorizationService {
	
	@Setter
	MicrosoftConfigRepository microsoftConfigRepository;
	
	@Setter
	MicrosoftAccessTokenRepository microsoftAccessTokenRepository;
	
	@Autowired
	SakaiProxy sakaiProxy;
	
	@Setter
	private CacheManager cacheManager;
	private Cache cache = null;
	
	private static final String CACHE_NAME = MicrosoftAuthorizationServiceImpl.class.getName() + "_cache";
	private static final String CACHE_TOKENS = "key::tokens";

	public void init() {
		log.info("Initializing MicrosoftAuthorization Service");
	}
	
	private Cache getCache() {
		if(cache == null) {
			cache = cacheManager.getCache(CACHE_NAME);
		}
		return cache;
	}
	
	@Override
	public void resetCache() {
		getCache().invalidate();
	}
	
	@Override
	public GraphServiceClient getDelegatedGraphClient(String userId) throws MicrosoftCredentialsException {
		GraphServiceClient client = null;
		MicrosoftAccessToken accessToken = null;
		try {
			MicrosoftCredentials microsoftCredentials = microsoftConfigRepository.getCredentials();
			if(microsoftCredentials.hasValue()) {
				
				accessToken = getAccessToken(userId);
				
				if(accessToken == null) {
					throw new MicrosoftNoCredentialsException();
				}
				
				DelegatedAuthProvider authProvider = DelegatedAuthProvider.builder()
						.authorizationService(this)
						.sakaiUserId(userId)
						.authority(microsoftCredentials.getAuthority())
						.clientId(microsoftCredentials.getClientId())
						.secret(microsoftCredentials.getSecret())
						.microsoftAccessToken(accessToken)
						.build();
				
				client = GraphServiceClient
						.builder()
						.authenticationProvider(authProvider)
						.buildClient();
			} else {
				throw new MicrosoftNoCredentialsException();
			}
		}catch(MicrosoftCredentialsException e) {
			throw e;
		} catch(Exception e) {
			log.error("ERROR Delegated Client: {}", e);
		}
		return client;
	}
	
	@Override
	public void checkDelegatedClient(String userId) throws MicrosoftCredentialsException {

		GraphServiceClient client = getDelegatedGraphClient(userId);
		
		try {
			log.debug("Validating Delegated Client....");
			//validate credentials (we don't care about the result)
			client.me().buildRequest().get();
		} catch(IllegalArgumentException e) {
			log.info("ERROR invalid token for user: {}", userId);
			throw new MicrosoftInvalidTokenException();
		} catch(Exception e) {
			log.warn("Unexpected ERROR validating client/token for user: {}", userId);
			throw new MicrosoftInvalidCredentialsException();
		}
	}

	@Override
	public MicrosoftRedirectURL getAuthenticationUrl() throws MicrosoftNoCredentialsException {
		MicrosoftRedirectURL ret = null;
		MicrosoftCredentials microsoftCredentials = microsoftConfigRepository.getCredentials();
		if(microsoftCredentials.hasValue()) {
			String state = UUID.randomUUID().toString();
			
			Map<String, String> params = new HashMap<>();
			params.put(CLIENT_ID, microsoftCredentials.getClientId());
			params.put(RESPONSE_MODE, RESPONSE_MODE_DEFAULT);
			params.put(RESPONSE_TYPE, RESPONSE_TYPE_DEFAULT);
			params.put(SCOPE, microsoftCredentials.getDelegatedScope()+" "+OFFLINE_ACCESS_SCOPE);
			params.put(STATE, state); //randomly generated and verified when returned
			params.put(REDIRECT_URI, sakaiProxy.getServerUrl() + REDIRECT_TOKEN_ENDPOINT);
			
			String str_params = params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
			
			String authUrl = microsoftCredentials.getAuthority() + ENDPOINT_AUTHORIZE + Q_MARK + str_params;		
			log.debug("authUrl : {}", authUrl);
			
			ret = MicrosoftRedirectURL.builder()
					.URL(authUrl)
					.state(state)
					.build();
		} else {
			throw new MicrosoftNoCredentialsException();
		}
		return ret;
	}
	
	@Override
	public boolean processAuthorizationCode(String userId, String authorizationCode){
		try {
			MicrosoftCredentials microsoftCredentials = microsoftConfigRepository.getCredentials();
			if(microsoftCredentials.hasValue()) {
			
				ConfidentialClientApplication app = ConfidentialClientApplication.builder(
						microsoftCredentials.getClientId(), ClientCredentialFactory.createFromSecret(microsoftCredentials.getSecret()))
					.authority(microsoftCredentials.getAuthority())
					.build();
	
	            IAuthenticationResult result = app.acquireToken(AuthorizationCodeParameters
	                    .builder(authorizationCode, new URI(sakaiProxy.getServerUrl() + REDIRECT_TOKEN_ENDPOINT))
	                    .scopes(new HashSet<String>(Arrays.asList((microsoftCredentials.getDelegatedScope()+" "+OFFLINE_ACCESS_SCOPE).split(" "))))
	                    .build())
	                    .get();
	            
	            MicrosoftAccessToken aux = MicrosoftAccessToken.builder()
	            	.sakaiUserId(userId)
	            	.microsoftUserId(result.account().username())
	            	.accessToken(app.tokenCache().serialize())
	            	.account(MicrosoftAuthorizationAccount.builder()
	            			.homeAccountId(result.account().homeAccountId())
			            	.environment(result.account().environment())
							.username(result.account().username())
							.hashCode(result.account().hashCode())
							.build())
	            	.build();
	            
	            //save to DB
	            saveAccessToken(aux);
			
	            return true;
			}
		} catch(Exception e) {
			log.error("Error processing authorization code for user={}", userId);
		}
		return false;
	}

	@Override
	public void saveAccessToken(MicrosoftAccessToken accessToken) {
		log.debug("Saving token: sakaiUserId={}, mcUser={}, token={}, account={}", accessToken.getSakaiUserId(), accessToken.getMicrosoftUserId(), accessToken.getAccessToken(), accessToken.getAccount());
		
		if (microsoftAccessTokenRepository.exists(accessToken.getSakaiUserId())) {
			microsoftAccessTokenRepository.merge(accessToken);
		} else {
			microsoftAccessTokenRepository.save(accessToken);
		}

		//update cache
		Map<String, MicrosoftAccessToken> tokensMap = new HashMap<String, MicrosoftAccessToken>();
		
		Cache.ValueWrapper cachedValue = getCache().get(CACHE_TOKENS);
		if(cachedValue != null) {
			tokensMap = (Map<String, MicrosoftAccessToken>)cachedValue.get();
		} 
		tokensMap.put(accessToken.getSakaiUserId(), accessToken);
		
		log.debug("Storing access token (SAVE) in cache");
		getCache().put(CACHE_TOKENS, tokensMap);
	}

	@Override
	public MicrosoftAccessToken getAccessToken(String userId) {
		log.debug("Getting token for user: {}", userId);
		
		Cache.ValueWrapper cachedValue = getCache().get(CACHE_TOKENS);
		if(cachedValue != null) {
			Map<String, MicrosoftAccessToken> tokensMap = (Map<String, MicrosoftAccessToken>)cachedValue.get();
			if(tokensMap.containsKey(userId)) {
				log.debug("Access Token From CACHE!!!");
				return tokensMap.get(userId);
			}
		}
		
		log.debug("Access Token From DB!!!");
		MicrosoftAccessToken ret = microsoftAccessTokenRepository.findBySakaiId(userId);
		
		if(ret != null) {
			//update cache
			Map<String, MicrosoftAccessToken> tokensMap = new HashMap<String, MicrosoftAccessToken>();
			if(cachedValue != null) {
				tokensMap = (Map<String, MicrosoftAccessToken>)cachedValue.get();
			} 
			tokensMap.put(ret.getSakaiUserId(), ret);
			
			getCache().put(CACHE_TOKENS, tokensMap);
		}
		
		return ret;
	}
	
	@Override
	public boolean revokeAccessToken(String userId) {
		try {
			microsoftAccessTokenRepository.delete(microsoftAccessTokenRepository.findBySakaiId(userId));
			
			//update cache
			Cache.ValueWrapper cachedValue = getCache().get(CACHE_TOKENS);
			if(cachedValue != null) {
				Map<String, MicrosoftAccessToken> tokensMap = (Map<String, MicrosoftAccessToken>)cachedValue.get();
				tokensMap.remove(userId);
				
				getCache().put(CACHE_TOKENS, tokensMap);
			}
			
			return true;
		}catch(Exception e) {
			log.warn("Error removing Microsoft Access Token for user {}: {}", userId, e.getMessage());
		}
		return false;
	}
}
