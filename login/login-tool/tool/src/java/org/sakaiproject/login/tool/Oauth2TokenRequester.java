/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/login/tags/sakai-10.5/login-tool/tool/src/java/org/sakaiproject/login/tool/Oauth2TokenRequester.java $
 * $Id: Oauth2TokenRequester.java fsaez@entornosdeformacion.com $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.login.tool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.util.UriUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;


public class Oauth2TokenRequester {
	
	protected final static int HTTP_SOCKET_TIMEOUT = 30000;
	
	// Allow for time sync issues by having a window of X seconds.
	private int timeSkewAllowance = 300;
	
	private static Log M_log = LogFactory.getLog(Oauth2TokenRequester.class);
	
	private String tokenUri = null;
	
	public Oauth2TokenRequester(String tokenUri) {
		this.tokenUri = tokenUri;
	}

	public String getOauth2Token(String authorizationCode, String redirectUri) throws Exception {
		
		String clientId = Oauth2ServerConfiguration.getClientId();
		String clientSecret = Oauth2ServerConfiguration.getClientSecret();
		
		Map<String, String> form = new HashMap<String, String>();
		form.put("grant_type", "authorization_code");
		form.put("code", authorizationCode);
		form.put("redirect_uri", redirectUri);
		
		HttpClient client = new HttpClient();
		HttpState state = client.getState();
		
		PostMethod method = new PostMethod(tokenUri);
        // post params
		method.addParameter("grant_type", "authorization_code");
		method.addParameter("code", authorizationCode);
		method.addParameter("redirect_uri", redirectUri);
		
		//TODO : accept other authentication types
		if(Oauth2ServerConfiguration.getAuthorizationType().equalsIgnoreCase(Oauth2ServerConfiguration.AUTH_TYPE_SECRET_BASIC)) {
			String authHeaderStr = String.format("Basic %s", new String(Base64.encodeBase64(String.format("%s:%s",
					UriUtils.encodePathSegment(clientId, "UTF-8"),
					UriUtils.encodePathSegment(clientSecret, "UTF-8")).getBytes()), "UTF-8"));
			method.addRequestHeader("Authorization", authHeaderStr);
		} else if(Oauth2ServerConfiguration.getAuthorizationType().equalsIgnoreCase(Oauth2ServerConfiguration.AUTH_TYPE_SECRET_POST)) {
			method.addParameter("client_id", clientId);
			if(!StringUtils.isEmpty(clientSecret))
				method.addParameter("client_secret", clientSecret);
		}
		    
		client.executeMethod( method );
		
		//String response = method.getResponseBodyAsString();
		BufferedReader br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
        String readLine;
        StringBuilder sb = new StringBuilder();
        while(((readLine = br.readLine()) != null)) {
          sb.append(readLine);
        }
		
        String response = sb.toString();
        M_log.debug("TOKEN response : "+response);
		
		JsonElement jsonRoot = new JsonParser().parse(response);
		if (!jsonRoot.isJsonObject()) {
			throw new AuthenticationServiceException("Token Endpoint did not return a JSON object: " + jsonRoot);
		}

		JsonObject tokenResponse = jsonRoot.getAsJsonObject();
		
		if (tokenResponse.get("error") != null) {

			// Handle error
			String error = tokenResponse.get("error").getAsString();

			M_log.error("Token Endpoint returned: " + error);

			throw new AuthenticationServiceException("Unable to obtain Access Token.  Token Endpoint returned: " + error);

		} else {
			String accessTokenValue = null;
			String idTokenValue = null;
			String refreshTokenValue = null;

			if (tokenResponse.has("access_token")) {
				accessTokenValue = tokenResponse.get("access_token").getAsString();
			} else {
				throw new AuthenticationServiceException("Token Endpoint did not return an access_token: " + response);
			}

			if (tokenResponse.has("id_token")) {
				idTokenValue = tokenResponse.get("id_token").getAsString();
			} else {
				M_log.error("Token Endpoint did not return an id_token");
				throw new AuthenticationServiceException("Token Endpoint did not return an id_token");
			}

			if (tokenResponse.has("refresh_token")) {
				refreshTokenValue = tokenResponse.get("refresh_token").getAsString();
			}
			
			JWT idToken = JWTParser.parse(idTokenValue);

			// validate our ID Token over a number of tests
			JWTClaimsSet idClaims = idToken.getJWTClaimsSet();
			
			Algorithm tokenAlg = idToken.getHeader().getAlgorithm();
			
			
			// check the issuer
			if (idClaims.getIssuer() == null) {
				throw new AuthenticationServiceException("Id Token Issuer is null");
			} else if (!idClaims.getIssuer().equals(Oauth2ServerConfiguration.getIssuer())){
				throw new AuthenticationServiceException("Issuers do not match, expected " + Oauth2ServerConfiguration.getIssuer() + " got " + idClaims.getIssuer());
			}
			
			// check expiration
			if (idClaims.getExpirationTime() == null) {
				throw new AuthenticationServiceException("Id Token does not have required expiration claim");
			} else {
				// it's not null, see if it's expired
				Date now = new Date(System.currentTimeMillis() - (timeSkewAllowance * 1000));
				if (now.after(idClaims.getExpirationTime())) {
					throw new AuthenticationServiceException("Id Token is expired: " + idClaims.getExpirationTime());
				}
			}

			// check not before
			if (idClaims.getNotBeforeTime() != null) {
				Date now = new Date(System.currentTimeMillis() + (timeSkewAllowance * 1000));
				if (now.before(idClaims.getNotBeforeTime())){
					throw new AuthenticationServiceException("Id Token not valid untill: " + idClaims.getNotBeforeTime());
				}
			}

			// check issued at
			if (idClaims.getIssueTime() == null) {
				throw new AuthenticationServiceException("Id Token does not have required issued-at claim");
			} else {
				// since it's not null, see if it was issued in the future
				Date now = new Date(System.currentTimeMillis() + (timeSkewAllowance * 1000));
				if (now.before(idClaims.getIssueTime())) {
					throw new AuthenticationServiceException("Id Token was issued in the future: " + idClaims.getIssueTime());
				}
			}

			// check audience
			if (idClaims.getAudience() == null) {
				throw new AuthenticationServiceException("Id token audience is null");
			} else if (!idClaims.getAudience().contains(clientId)) {
				throw new AuthenticationServiceException("Audience does not match, expected " + clientId + " got " + idClaims.getAudience());
			}
			
			return accessTokenValue;
		}
	}
}

