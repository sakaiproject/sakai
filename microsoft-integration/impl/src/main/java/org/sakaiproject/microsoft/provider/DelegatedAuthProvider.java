package org.sakaiproject.microsoft.provider;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.microsoft.api.MicrosoftAuthorizationService;
import org.sakaiproject.microsoft.api.data.MicrosoftAuthorizationAccount;
import org.sakaiproject.microsoft.api.model.MicrosoftAccessToken;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.ITenantProfile;
import com.microsoft.aad.msal4j.RefreshTokenParameters;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.graph.authentication.IAuthenticationProvider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DelegatedAuthProvider implements IAuthenticationProvider {

	private MicrosoftAuthorizationService authorizationService;
	private String sakaiUserId;
	
	private String authority;
	private String clientId;
	private String secret;
	
	private MicrosoftAccessToken microsoftAccessToken;
	
	//aux class who implements IAccount
	//we will create an instance of this class with the contents of a MicrosoftAuthorizationAccount
	//MicrosoftAuthorizationAccount has the same attributes but does not implements IAccount, so we don't have Microsoft dependencies in the API
	private class AuxAccount implements IAccount{
		private String homeAccountId;
		private String environment;
		private String username;
		private int hashCode;
		
		public AuxAccount(MicrosoftAuthorizationAccount a) {
			this.homeAccountId = a.getHomeAccountId();
			this.environment = a.getEnvironment();
			this.username = a.getUsername();
			this.hashCode = a.getHashCode();
		}

		@Override
		public String homeAccountId() {
			return this.homeAccountId;
		}

		@Override
		public String environment() {
			return this.environment;
		}

		@Override
		public String username() {
			return this.username;
		}

		@Override
		public Map<String, ITenantProfile> getTenantProfiles() {
			return null;
		}
		
		@Override
		public int hashCode() {
			return this.hashCode;
		}
	}
	
	@Override
	public CompletableFuture<String> getAuthorizationTokenAsync(URL requestUrl) {
		CompletableFuture<String> token = new CompletableFuture<>();
		try {
			IAuthenticationResult result = getAccessTokenByClientCredentialGrant();
			
			token.complete(result.accessToken());
		} catch (Exception e) {
			log.error("Exception retrieving token from Microsoft Graph Auth Provider");
			throw new IllegalArgumentException("Exception retrieving token from Microsoft Graph Auth Provider");
		}
		return token;
	}

	/**
	 * Get Token 
	 * @return
	 * @throws Exception
	 */
	private IAuthenticationResult getAccessTokenByClientCredentialGrant() throws Exception {
		
		IAuthenticationResult ret = null;
		
		ConfidentialClientApplication app = ConfidentialClientApplication.builder(clientId, ClientCredentialFactory.createFromSecret(secret))
			.authority(authority)
			.build();
		
		//check if we have an account stored in cache
		if(microsoftAccessToken.getAccount() != null) {
			try {
				log.debug("Get Token Silently for user: {}", sakaiUserId);
				
				app.tokenCache().deserialize(microsoftAccessToken.getAccessToken());
		
				IAccount auxAccount = Stream.of(microsoftAccessToken.getAccount()).map(AuxAccount::new).findFirst().orElse(null);
				SilentParameters parameters = SilentParameters.builder(
						new HashSet<String>(Arrays.asList(MicrosoftAuthorizationService.SCOPE_DEFAULT.split(" "))),
						auxAccount
				).build();
		
				ret = app.acquireTokenSilently(parameters).get();
			}catch(Exception e) {
				log.warn("ERROR: getting silent token for user: {}", sakaiUserId);
			}
		}
		
		//if silent process has failed, try to refresh token
		if(ret == null && StringUtils.isNotBlank(microsoftAccessToken.getRefreshToken())){
			try {
				log.debug("Get Refresh Token for user: {}", sakaiUserId);
				
				RefreshTokenParameters parameters = RefreshTokenParameters.builder(
						new HashSet<String>(Arrays.asList(MicrosoftAuthorizationService.SCOPE_DEFAULT.split(" "))),
						microsoftAccessToken.getRefreshToken()
					).build();
				
				ret = app.acquireToken(parameters).get();
			}catch(Exception e) {
				log.warn("ERROR: getting refresh token for user: {}", sakaiUserId);
			}
		}
		
		//compare renewed token and the old one -> if they are different, store the new one
		if(ret != null && !microsoftAccessToken.getAccessToken().equals(app.tokenCache().serialize())) {
			authorizationService.saveAccessToken(microsoftAccessToken.toBuilder()
			    	.accessToken(app.tokenCache().serialize())
			    	.build());
		}
		
		return ret;
	}
}
