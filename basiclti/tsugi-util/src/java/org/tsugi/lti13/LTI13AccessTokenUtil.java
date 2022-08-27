/*
 * $URL$
 * $Id$
 *
 * Copyright (c) 2022- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.tsugi.lti13;

import java.lang.StringBuffer;

import java.util.Map;
import java.util.TreeMap;

import java.net.http.HttpResponse;  // Thanks Java 11

import java.security.Key;
import java.security.KeyPair;

import org.apache.commons.lang3.StringUtils;

import io.jsonwebtoken.Jwts;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.tsugi.http.HttpClientUtil;
import org.tsugi.jackson.JacksonUtil;
import org.tsugi.oauth2.objects.AccessToken;
import org.tsugi.oauth2.objects.ClientAssertion;

import lombok.extern.slf4j.Slf4j;

// https://www.imsglobal.org/spec/security/v1p0/

@Slf4j
public class LTI13AccessTokenUtil {

	/**
	 * Return a Map of values ready for posting
	 * 
	 *   grant_type=client_credentials
	 *   client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer
	 *   client_assertion=eyJ0eXAiOiJKV1QiLCJhbG....qEZtDgBgMMsneNePfMrifOvvFLkxnpefA
	 *   scope=http://imsglobal.org/ags/lineitem http://imsglobal.org/ags/result/read
	 *
	 *   https://www.imsglobal.org/spec/security/v1p0/#using-json-web-tokens-with-oauth-2-0-client-credentials-grant
	 */
	public static Map getClientAssertion(String[] scopes, KeyPair keyPair,
		String clientId, String deploymentId, String tokenAudience, StringBuffer dbs)
	{
		if ( dbs != null ) {
			dbs.append("getClientAssertion\n");
		}

		Map retval = new TreeMap();
		retval.put(ClientAssertion.GRANT_TYPE, ClientAssertion.GRANT_TYPE_CLIENT_CREDENTIALS);
		retval.put(ClientAssertion.CLIENT_ASSERTION_TYPE, ClientAssertion.CLIENT_ASSERTION_TYPE_JWT);
		if ( scopes != null && scopes.length > 0 ) {
			retval.put(ClientAssertion.SCOPE, String.join(" ", scopes));
			if ( dbs != null ) {
				dbs.append("scopes\n");
				dbs.append((String) retval.get(ClientAssertion.SCOPE));
				dbs.append("\n");
			}
		}

		ClientAssertion ca = new ClientAssertion();
		ca.issuer = clientId;  // This is our server
		ca.subject = clientId;
		ca.deployment_id = deploymentId;
		if ( !StringUtils.isEmpty(tokenAudience) ) ca.audience = tokenAudience;

		String cas = JacksonUtil.toString(ca);

		Key privateKey = keyPair.getPrivate();
		Key publicKey = keyPair.getPublic();

		String kid = LTI13KeySetUtil.getPublicKID(publicKey);

		log.debug("getClientAssertion kid={} token={}", kid, cas);
		if ( dbs != null && kid != null && cas != null ) {
			dbs.append("kid=");
			dbs.append(kid);
			dbs.append("\n");
			dbs.append(StringUtils.truncate(cas, 1000));
			dbs.append("\n");
		}

		String jws = Jwts.builder().setHeaderParam("kid", kid).
					setPayload(cas).signWith(privateKey).compact();
		retval.put(ClientAssertion.CLIENT_ASSERTION, jws);

		return retval;
	}

	public static AccessToken getScoreToken(String url, KeyPair keyPair,
		String clientId, String deploymentId, String tokenAudience, StringBuffer dbs)
	{
		Map assertion = getScoreAssertion(keyPair, clientId, deploymentId, tokenAudience, dbs);
		return retrieveToken(url, assertion, dbs);
	}

	public static Map getScoreAssertion(KeyPair keyPair,
		String clientId, String deploymentId, String tokenAudience, StringBuffer dbs)
	{
		return getClientAssertion(
				new String[] {
					LTI13ConstantsUtil.SCOPE_LINEITEM,
					LTI13ConstantsUtil.SCOPE_SCORE,
					LTI13ConstantsUtil.SCOPE_RESULT_READONLY
				},
			keyPair, clientId, deploymentId, tokenAudience, dbs);
	}

	public static AccessToken getNRPSToken(String url, KeyPair keyPair,
		String clientId, String deploymentId, String tokenAudience, StringBuffer dbs)
	{
		Map assertion = getNRPSAssertion(keyPair, clientId, deploymentId, tokenAudience, dbs);
		return retrieveToken(url, assertion, dbs);
	}

	public static Map getNRPSAssertion(KeyPair keyPair,
		String clientId, String deploymentId, String tokenAudience, StringBuffer dbs)
	{
		return getClientAssertion(
				new String[] {
					LTI13ConstantsUtil.SCOPE_NAMES_AND_ROLES
				},
			keyPair, clientId, deploymentId, tokenAudience, dbs);
	}

	public static AccessToken getLineItemsToken(String url, KeyPair keyPair,
		String clientId, String deploymentId, String tokenAudience, StringBuffer dbs)
	{
		Map assertion = getLineItemsAssertion(keyPair, clientId, deploymentId, tokenAudience, dbs);
		return retrieveToken(url, assertion, dbs);
	}


	public static Map getLineItemsAssertion(KeyPair keyPair,
		String clientId, String deploymentId, String tokenAudience, StringBuffer dbs)
	{
		return getClientAssertion(
				new String[] {
					LTI13ConstantsUtil.SCOPE_LINEITEM
				},
			keyPair, clientId, deploymentId, tokenAudience, dbs);
	}

	// https://www.imsglobal.org/spec/security/v1p0/#using-json-web-tokens-with-oauth-2-0-client-credentials-grant
	// https://canvas.instructure.com/doc/api/file.oauth_endpoints.html#post-login-oauth2-token
	protected static AccessToken retrieveToken(String url, Map assertion, StringBuffer dbs)
	{
		try {
			if ( dbs != null ) {
				dbs.append("retrieveToken\n");
				dbs.append(url);
				dbs.append(assertion.toString());
				dbs.append("\n");
			}

			HttpResponse<String> response = HttpClientUtil.sendPost(url, assertion, null, dbs);
			String responseStr = response.body();

			if ( responseStr == null ) {
				log.info("Empty / null response to POST url={} sent={}",url, assertion, responseStr);
				if ( dbs != null ) dbs.append("Empty / null response to POST\n");
				return null;
			}

			if ( dbs != null ) {
				dbs.append("responseStr\n");
				dbs.append(responseStr);
				dbs.append("\n");
			}

			ObjectMapper mapper = JacksonUtil.getLaxObjectMapper();
			AccessToken accessToken = mapper.readValue(responseStr, AccessToken.class);

			if ( accessToken == null || StringUtils.isEmpty(accessToken.access_token) ) {
				log.info("Failed to parse access token url={} sent={} received={}",url, assertion, responseStr);
				if ( dbs != null ) dbs.append("Could not parse access token\n");
				return null;
			}

			if ( dbs != null ) {
				dbs.append("access_token\n");
				dbs.append(accessToken);
				dbs.append("\n");
			}

			return accessToken;
		} catch (Exception e) {
			// TODO: Earle - is the PrintStackTrace redundant here?
			e.printStackTrace();
			log.error("Error retrieving token from {}", url, e);

			if ( dbs != null ) {
				dbs.append("Exception retrieving token ");
				dbs.append(e.getMessage());
				dbs.append("\n");
			}

			return null;
		}
	}

}
