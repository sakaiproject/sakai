/**
 * Copyright (c) 2018- Charles R. Severance
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
 *
 * Author: Charles Severance <csev@umich.edu>
 */
package org.sakaiproject.lti13;

import io.jsonwebtoken.Jws;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.HashSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONValue;
import org.json.simple.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.lti.util.LegacyShaUtil;
import org.sakaiproject.lti.util.SakaiLTIUtil;
import org.sakaiproject.lti.util.SakaiKeySetUtil;
import static org.sakaiproject.lti.util.SakaiLTIUtil.LTI13_PATH;
import static org.sakaiproject.lti.util.SakaiLTIUtil.getOurServerUrl;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti13.LineItemUtil;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.tsugi.lti.LTIUtil;
import org.tsugi.jackson.JacksonUtil;
import org.tsugi.lti13.LTICustomVars;
import org.tsugi.lti13.LTI13KeySetUtil;
import org.tsugi.lti13.LTI13Util;
import org.tsugi.lti13.LTI13JwtUtil;
import org.tsugi.lti13.LTI13ConstantsUtil;

import org.tsugi.oauth2.objects.AccessToken;
import org.tsugi.oauth2.objects.ClientAssertion;
import org.tsugi.lti13.objects.Endpoint;
import org.tsugi.lti13.objects.LaunchLIS;
import org.tsugi.ags2.objects.Result;
import org.tsugi.ags2.objects.Score;
import org.tsugi.lti13.objects.LaunchJWT;
import org.tsugi.lti13.objects.OpenIDProviderConfiguration;
import org.tsugi.lti13.objects.LTIPlatformConfiguration;
import org.tsugi.lti13.objects.LTILaunchMessage;

import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.sakaiproject.lti13.util.SakaiLineItem;

import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CommentDefinition;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;

import org.tsugi.lti.LTIUtil;

/**
 *
 */
@SuppressWarnings("deprecation")
@Slf4j
public class LTI13Servlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String APPLICATION_JSON = "application/json; charset=utf-8";
	private static final String APPLICATION_JWT = "application/jwt";
	private static final String ERROR_DETAIL = "X-Sakai-LTI13-Error-Detail";
	protected static LTIService ltiService = null;

	// Used for signing and checking tokens
	// TODO: Rotate these after a while
	private KeyPair tokenKeyPair = null;

    private CacheManager cacheManager;
    private Cache cache;

	private static final String CACHE_NAME = LTI13Servlet.class.getName() + "_cache";
	private static final String CACHE_PUBLIC = "key::public";
	private static final String CACHE_PRIVATE = "key::private";

	private static final String PLUS_NRPS_PAGING = "plus:nrps_paging";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if (ltiService == null) {
			ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
		}

        cacheManager = (CacheManager) ComponentManager.get("org.sakaiproject.ignite.SakaiCacheManager");
        cache = cacheManager.getCache(CACHE_NAME);

		// Lets try to load from properties
		if (tokenKeyPair == null) {
			// lti.advantage.lti13servlet.public=MIIBIjANBgkqhkiG9w [snip] Yfu3RbCda/nq4lipjRQIDAQAB
			String publicB64 = ServerConfigurationService.getString("lti.advantage.lti13servlet.public", null);
			String privateB64 = ServerConfigurationService.getString("lti.advantage.lti13servlet.private", null);
			if ( publicB64 != null && privateB64 != null) {
				tokenKeyPair = LTI13Util.strings2KeyPair(publicB64, privateB64);
				if ( tokenKeyPair == null ) {
					Logger.getLogger(LTI13Servlet.class.getName()).log(Level.SEVERE, "Could not load tokenKeyPair from sakai.properties");
				} else {
					Logger.getLogger(LTI13Servlet.class.getName()).log(Level.INFO, "Loaded tokenKeyPair from sakai.properties");
				}
			}
		}

		// Get it from the cluster cache
		if (tokenKeyPair == null) {
			Cache.ValueWrapper publicB64 = cache.get(CACHE_PUBLIC);
			Cache.ValueWrapper privateB64 = cache.get(CACHE_PRIVATE);
			if ( publicB64 != null && privateB64 != null) {
				tokenKeyPair = LTI13Util.strings2KeyPair((String) publicB64.get(), (String) privateB64.get());
				if ( tokenKeyPair == null ) {
					Logger.getLogger(LTI13Servlet.class.getName()).log(Level.SEVERE, "Could not parse tokenKeyPair from Ignite Cache");
				} else {
					Logger.getLogger(LTI13Servlet.class.getName()).log(Level.INFO, "Loaded tokenKeyPair from Ignite Cache");
				}
			}
        }

		// Lets make a new key
		if (tokenKeyPair == null) {
			try {
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
				keyGen.initialize(2048);
				tokenKeyPair = keyGen.genKeyPair();
				String publicB64 = LTI13Util.getPublicB64(tokenKeyPair);
				String privateB64 = LTI13Util.getPrivateB64(tokenKeyPair);
				cache.put(CACHE_PUBLIC, publicB64);
				cache.put(CACHE_PRIVATE, privateB64);
				Logger.getLogger(LTI13Servlet.class.getName()).log(Level.INFO, "Generated tokenKeyPair and stored in Ignite Cache");
			} catch (NoSuchAlgorithmException ex) {
				Logger.getLogger(LTI13Servlet.class.getName()).log(Level.SEVERE, "Unable to generate tokenKeyPair", ex);
			}
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI(); // /imsblis/lti13/keys
		// String launch_url = request.getParameter("launch_url");

		String[] parts = uri.split("/");

		SakaiLineItem filter = getLineItemFilter(request);

		// Get a keys for a client_id
		// Pre SAK-46144 - /imsblis/lti13/keyset/{tool-id}
		// /imsblis/lti13/keyset
		if (parts.length >= 4 && "keyset".equals(parts[3])) {
			handleKeySet(request, response);
			return;
		}

		// Get the membership list for a placement
		// /imsblis/lti13/namesandroles/context:6
		if (parts.length == 5 && "namesandroles".equals(parts[3])) {
			String signed_placement = parts[4];
			handleNamesAndRoles(signed_placement, request, response);
			return;
		}

		// Get the course groups for a site
		// /imsblis/lti13/groupservice/{site-id}
		if (parts.length == 5 && "groupservice".equals(parts[3])) {
			String site_id = parts[4];
			handleGroupService(site_id, request, response);
			return;
		}

		// List lineitems created by a placement
		// /imsblis/lti13/lineitems/{signed-placement}
		if (parts.length == 5 && "lineitems".equals(parts[3]) ) {
			String signed_placement = parts[4];
			handleLineItemsGet(signed_placement, true /* all */, filter, request, response);
			return;
		}

		// Get lineitem data
		// /imsblis/lti13/lineitem/{signed-placement}
		if (parts.length == 5 && "lineitem".equals(parts[3]) ) {
			String signed_placement = parts[4];
			handleLineItemsGet(signed_placement, false /* all */, null /* filter */, request, response);
			return;
		}

		// /imsblis/lti13/lineitem/{signed-placement}/results
		if (parts.length == 6 && "lineitem".equals(parts[3]) && "results".equals(parts[5]) ) {
			String signed_placement = parts[4];
			String lineItem = null;
			handleLineItemsDetail(signed_placement, lineItem, true /*results */, null /* user_id */, request, response);
			return;
		}

		// Handle lineitems created by the tool
		// /imsblis/lti13/lineitems/{signed-placement}/{lineitem-id}
		if (parts.length == 6 && "lineitems".equals(parts[3])) {
			String signed_placement = parts[4];
			String lineItem = parts[5];
			handleLineItemsDetail(signed_placement, lineItem, false /*results */, null /* user_id */, request, response);
			return;
		}

		// /imsblis/lti13/lineitems/{signed-placement}/{lineitem-id}/results
		if (parts.length == 7 && "lineitems".equals(parts[3]) && "results".equals(parts[6])) {
			String signed_placement = parts[4];
			String lineItem = parts[5];
			handleLineItemsDetail(signed_placement, lineItem, true /*results */, null /* user_id */, request, response);
			return;
		}

		// /imsblis/lti13/lineitems/{signed-placement}/{lineitem-id}/results/{user-id}
		if (parts.length == 8 && "lineitems".equals(parts[3]) && "results".equals(parts[6])) {
			String signed_placement = parts[4];
			String lineItem = parts[5];
			handleLineItemsDetail(signed_placement, lineItem, true /*results */, parts[7], request, response);
			return;
		}

		// /imsblis/lti13/proxy
		if (parts.length == 4 && "proxy".equals(parts[3])) {
			handleProxy(request, response);
			return;
		}

		// /imsblis/lti13/postverify/{signed-placement}
		if (SakaiLTIUtil.checkSendPostVerify() && parts.length == 5 && "postverify".equals(parts[3])) {
			String signed_placement = parts[4];
			handlePostVerify(signed_placement, request, response);
			return;
		}

		// /imsblis/lti13/sakai_config
		if (parts.length == 4 && "sakai_config".equals(parts[3])) {
			handleSakaiConfig(request, response);
			return;
		}

		// /imsblis/lti13/well_known
		if (parts.length == 4 && "well_known".equals(parts[3])) {
			handleWellKnown(request, response);
			return;
		}

		// /imsblis/lti13/get_registration
		if (parts.length == 4 && "get_registration".equals(parts[3])) {
			handleGetRegistration(request, response);
			return;
		}

		log.error("Unrecognized GET request parts={} request={}", parts.length, uri);

		LTI13Util.return400(response, "Unrecognized GET request parts="+parts.length+" request="+uri);
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI(); // /imsblis/lti13/keys

		String[] parts = uri.split("/");

		// Handle lineitems created by the tool
		// /imsblis/lti13/lineitems/{signed-placement}/{lineitem-id}
		if (parts.length == 6 && "lineitems".equals(parts[3])) {
			String signed_placement = parts[4];
			String lineItem = parts[5];
			handleLineItemsDelete(signed_placement, lineItem, request, response);
			return;
		}

		log.error("Unrecognized DELETE request parts={} request={}", parts.length, uri);
		LTI13Util.return400(response, "Unrecognized DELETE request parts="+parts.length+" request="+uri);
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI(); // /imsblis/lti13/keys

		String[] parts = uri.split("/");

		// /imsblis/lti13/lineitems/{signed-placement}
		if (parts.length == 5 && "lineitem".equals(parts[3])) {
			log.error("Attempt to modify on-demand line item request={}", uri);
			LTI13Util.return400(response, "Attempt to modify an 'on-demand' line item");
			return;
		}

		// Handle lineitems created by the tool
		// /imsblis/lti13/lineitems/{signed-placement}/{lineitem-id}
		if (parts.length == 6 && "lineitems".equals(parts[3])) {
			String signed_placement = parts[4];
			String lineItem = parts[5];
			handleLineItemsUpdate(signed_placement, lineItem, request, response);
			return;
		}

		log.error("Unrecognized PUT request parts={} request={}", parts.length, uri);
		LTI13Util.return400(response, "Unrecognized PUT request parts="+parts.length+" request="+uri);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String uri = request.getRequestURI(); // /imsblis/lti13/keys
		// String launch_url = request.getParameter("launch_url");

		String[] parts = uri.split("/");

		// Get an access token for a tool
		// /imsblis/lti13/token/{tool-id}
		if (parts.length == 5 && "token".equals(parts[3])) {
			String tool_id = parts[4];
			handleTokenPost(tool_id, request, response);
			return;
		}

		// Set score for auto-created line item
		// /imsblis/lti13/lineitem/{signed-placement}
		// SAK-47261 - This pattern with no lineItem does not work post SAK-47621
		if (parts.length == 6 && "lineitem".equals(parts[3]) && "scores".equals(parts[5])) {
			String signed_placement = parts[4];
			String lineItem = null;
			handleLineItemScore(signed_placement, lineItem, request, response);
			return;
		}

		// Create a new lineitem for a placement
		// /imsblis/lti13/lineitems/{signed-placement}
		if (parts.length == 5 && "lineitems".equals(parts[3])) {
			String signed_placement = parts[4];
			handleLineItemsPost(signed_placement, request, response);
			return;
		}

		// Set a score for a lineitem created by a placement
		// /imsblis/lti13/lineitems/{signed-placement}/12345/scores
		if (parts.length == 7 && "lineitems".equals(parts[3]) && "scores".equals(parts[6])) {
			String signed_placement = parts[4];
			String lineItem = parts[5];
			handleLineItemScore(signed_placement, lineItem, request, response);
			return;
		}

		// Receive a tool configuration
		// /imsblis/lti13/registration_endpoint/{tool-key}
		if (parts.length == 5 && "registration_endpoint".equals(parts[3])) {
			String tool_key = parts[4];
			handleRegistrationEndpointPost(tool_key, request, response);
			return;
		}

		log.error("Unrecognized POST request parts={} request={}", parts.length, uri);
		LTI13Util.return400(response, "Unrecognized POST request parts="+parts.length+" request="+uri);

	}

	// A very locked down proxy - JSON Only
	protected void handleProxy(HttpServletRequest request, HttpServletResponse response) {
		String proxyUrl = request.getParameter("proxyUrl");
		if ( proxyUrl == null ) {
			LTI13Util.return400(response, "Missing proxyUrl");
			return;
		}

		Session sess = SessionManager.getCurrentSession();
		if ( sess == null || sess.getUserId() == null ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		// https://stackoverflow.com/a/43708457/1994792
		try {
			java.net.URL url = new java.net.URL(proxyUrl);
			java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setConnectTimeout(3000);
			con.setReadTimeout(3000);
			con.setInstanceFollowRedirects(true);

			try ( java.io.BufferedReader in = new java.io.BufferedReader(
				new java.io.InputStreamReader(con.getInputStream())) )
			{
				String inputLine;
				StringBuffer content = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					content.append(inputLine);
				}
				if ( content.length() > 10000 ) {
					LTI13Util.return400(response, "Content too long");
					return;
				}

				String jsonString = content.toString();

				Object js = JSONValue.parse(jsonString);
				if (js == null || !(js instanceof JSONObject)) {
					LTI13Util.return400(response, "Badly formatted JSON");
					return;
				}

				response.setContentType(APPLICATION_JSON);
				PrintWriter out = response.getWriter();

				out.println(((JSONObject) js).toJSONString());
			} catch (Exception e) {
				log.error("Error in proxy request", e);
				LTI13Util.return400(response, "Proxy request failed");
				return;
			}
		} catch (Exception e) {
			log.error("Error in proxy request", e);
			LTI13Util.return400(response, "Proxy request failed");
			return;
		}
	}

	// LTI PostVerify
	protected void handlePostVerify(String signed_placement, HttpServletRequest request, HttpServletResponse response) {

		String callback = request.getParameter("callback");
		if ( callback == null ) {
			LTI13Util.return400(response, "Missing callback parameter");
			return;
		}

		Session sess = SessionManager.getCurrentSession();
		if ( sess == null || sess.getUserId() == null ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		org.sakaiproject.lti.beans.LtiContentBean content = loadContentCheckSignature(signed_placement, response);
		if (content == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		Site site = loadSiteFromContent(content, signed_placement, response);
		if (site == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		Long toolKey = LTIUtil.toLongKey(content.toolId);
		if (toolKey < 0 ) {
			log.error("Content / Tool invalid content={} tool={}", content.id, toolKey);
			LTI13Util.return400(response, "Content / Tool mismatch");
			return;
		}

		org.sakaiproject.lti.beans.LtiToolBean tool = ltiService.getToolDaoAsBean(toolKey, site.getId(), true);
		if (tool == null) {
			log.error("Could not load tool={}", toolKey);
			LTI13Util.return400(response, "Missing tool");
			return;
		}

		KeyPair kp = SakaiKeySetUtil.getCurrent();
		Key privateKey = kp.getPrivate();
		Key publicKey = kp.getPublic();

		String kid = LTI13KeySetUtil.getPublicKID(publicKey);

		String context_id = site.getId();

		String user_id = sess.getUserId();
		String subject = SakaiLTIUtil.getSubject(user_id, context_id);

		try {
			Long issued = Long.valueOf(System.currentTimeMillis() / 1000L);
			String body =
				"{\n" +
				"\"iss\": \""+SakaiLTIUtil.getOurServerUrl()+"\",\n" +
				"\"aud\": \""+SakaiLTIUtil.getOurServerUrl()+"\",\n" +
				"\"exp\": \""+(issued+3600L)+"\",\n" +
				"\"user_id\": \""+user_id+"\",\n" +
				"\"context_id\": \""+context_id+"\",\n" +
				"\"sub\": \""+subject+"\"\n" +
				"}";

			// http://javadox.com/io.jsonwebtoken/jjwt/0.4/io/jsonwebtoken/JwtBuilder.html
			String jws = Jwts.builder()
				.setHeaderParam("kid", kid)
				.setPayload(body)
				.signWith(privateKey)
				.compact();

			// https://stackoverflow.com/questions/3324717/sending-http-post-request-in-java
			byte [] bytes = jws.getBytes();
			java.net.URL url = new java.net.URL(callback);
			java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
	        con.setDoOutput(true);
			con.setFixedLengthStreamingMode(bytes.length);
			con.setRequestProperty( "Content-Type", APPLICATION_JWT );
	        con.connect();
			OutputStream os = con.getOutputStream();
			os.write(bytes);
			os.flush();
			os.close();

		} catch (Exception e) {
			log.error("Error in post verify request", e);
			LTI13Util.return400(response, "Post verify request failed");
			return;
		}
	}

	// Retrieve the registration data for an LTI tool
	protected void handleGetRegistration(HttpServletRequest request, HttpServletResponse response) {
		String tool_key_str = request.getParameter("key");
		if ( tool_key_str == null ) {
			LTI13Util.return400(response, "Missing key parameter");
			return;
		}

		// Make sure the tool_key is a long
		Long tool_key = null;
		if ( tool_key_str != null ) {
			try {
				tool_key = Long.parseLong(tool_key_str);
			} catch (NumberFormatException e) {
				LTI13Util.return400(response, "Bad value for tool_key "+tool_key_str);
				log.error("Bad value for tool_key "+tool_key_str);
				return;
			}
		}

		Session sess = SessionManager.getCurrentSession();
		if ( sess == null || sess.getUserId() == null ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		// TODO: A little moar checking on the session.
		org.sakaiproject.lti.beans.LtiToolBean tool = ltiService.getToolDaoAsBean(tool_key, null, true);
		if (tool == null) {
			LTI13Util.return400(response, "Could not load tool");
			log.error("Could not load tool {}", tool_key);
			return;
		}

		String json_out = tool.lti13AutoRegistration;
		if ( json_out == null || json_out.length() < 1 ) {
			LTI13Util.return400(response, "Could not load tool configuration");
			log.error("Could not load tool configuration {}", tool_key);
			return;
		}

		response.setContentType(APPLICATION_JSON);
		try {
			PrintWriter out = response.getWriter();
			out.print(json_out);
		} catch (Exception e) {
			log.error("Error in get registration request", e);
			LTI13Util.return400(response, "Get registration request failed");
			return;
		}
	}

	// Provide LTI Advantage Sakai parameters through JSON
	protected void handleSakaiConfig(HttpServletRequest request, HttpServletResponse response) {
		String clientId = request.getParameter("clientId");
		if ( clientId == null ) {
			LTI13Util.return400(response, "Missing clientId");
			return;
		}

		String key = request.getParameter("key");
		if ( key == null ) {
			LTI13Util.return400(response, "Missing key");
			return;
		}

		String issuerURL = request.getParameter("issuerURL");
		if ( issuerURL == null ) {
			LTI13Util.return400(response, "Missing issuerURL");
			return;
		}

		String deploymentId = request.getParameter("deploymentId");
		if ( deploymentId == null ) {
			LTI13Util.return400(response, "Missing deploymentId");
			return;
		}

		String keySetUrl = getOurServerUrl() + "/imsblis/lti13/keyset";
		String tokenUrl = getOurServerUrl() + "/imsblis/lti13/token/" + key;
		String authOIDC = getOurServerUrl() + "/imsoidc/lti13/oidc_auth";

		String sakaiVersion = ServerConfigurationService.getString("version.sakai", "2");

		JSONObject context_obj = new JSONObject();
		context_obj.put("issuerURL", issuerURL);
		context_obj.put("clientId", clientId);
		context_obj.put("keySetUrl", keySetUrl);
		context_obj.put("tokenUrl", tokenUrl);
		context_obj.put("authOIDC", authOIDC);
		context_obj.put("deploymentId", deploymentId);
		context_obj.put("productFamilyCode", "sakai");
		context_obj.put("version", sakaiVersion);
		context_obj.put("answer", "42");

		response.setContentType(APPLICATION_JSON);
		try {
			PrintWriter out = response.getWriter();
			out.print(JacksonUtil.prettyPrint(context_obj));
		} catch (Exception e) {
			log.error("Error in sakai config request", e);
			LTI13Util.return400(response, "Sakai config request failed");
			return;
		}
	}

	/*
{
    "issuer": "https://server.example.com",
    "authorization_endpoint":  "https://server.example.com/connect/authorize",
    "token_endpoint": "https://server.example.com/connect/token",
    "token_endpoint_auth_methods_supported": ["private_key_jwt"],
    "token_endpoint_auth_signing_alg_values_supported": ["RS256"],
    "jwks_uri": "https://server.example.com/jwks.json",
    "registration_endpoint": "https://server.example.com/connect/register",
    "scopes_supported": ["openid", "https://purl.imsglobal.org/spec/lti-gs/scope/contextgroup.readonly",
       "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem",
       "https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly",
       "https://purl.imsglobal.org/spec/lti-ags/scope/score",
       "https://purl.imsglobal.org/spec/lti-reg/scope/registration"],
    "response_types_supported": ["id_token"],
    "subject_types_supported": ["public", "pairwise"],
    "id_token_signing_alg_values_supported":
      ["RS256", "ES256"],
    "claims_supported":
      ["sub", "iss", "name", "given_name", "family_name", "nickname", "picture", "email", "locale"],
     "https://purl.imsglobal.org/spec/lti-platform-configuration": {
        "product_family_code": "ExampleLMS",
        "messages_supported": [
            {"type": "LtiResourceLinkRequest"},
            {"type": "LtiDeepLinkingRequest"}],
        "variables": ["CourseSection.timeFrame.end", "CourseSection.timeFrame.begin", "Context.id.history", "ResourceLink.id.history"]
    }
}
	 */
	// Provide Well Known URL
	protected void handleWellKnown(HttpServletRequest request, HttpServletResponse response) {
		String clientId = request.getParameter("clientId");
		if ( clientId == null ) {
			LTI13Util.return400(response, "Missing clientId");
			return;
		}

		String key = request.getParameter("key");
		if ( key == null ) {
			LTI13Util.return400(response, "Missing key");
			return;
		}

		String issuerURL = request.getParameter("issuerURL");
		if ( issuerURL == null ) {
			LTI13Util.return400(response, "Missing issuerURL");
			return;
		}

		String deploymentId = request.getParameter("deploymentId");
		if ( deploymentId == null ) {
			LTI13Util.return400(response, "Missing deploymentId");
			return;
		}

		String keySetUrl = getOurServerUrl() + "/imsblis/lti13/keyset";
		String tokenUrl = getOurServerUrl() + "/imsblis/lti13/token/" + key;
		String authOIDC = getOurServerUrl() + "/imsoidc/lti13/oidc_auth";

		String sakaiVersion = ServerConfigurationService.getString("version.sakai", "2");

		LTIPlatformConfiguration lpc = new LTIPlatformConfiguration();
		lpc.product_family_code = "sakailms.org";
		lpc.version = sakaiVersion;

		LTILaunchMessage mp = new LTILaunchMessage();
		mp.type = LaunchJWT.MESSAGE_TYPE_LAUNCH;
		lpc.messages_supported.add(mp);

		mp = new LTILaunchMessage();
		mp.type = LaunchJWT.MESSAGE_TYPE_DEEP_LINK;
		lpc.messages_supported.add(mp);

		lpc.variables.add(LTICustomVars.USER_ID);
		lpc.variables.add(LTICustomVars.PERSON_EMAIL_PRIMARY);

		OpenIDProviderConfiguration pc = new OpenIDProviderConfiguration();
		pc.issuer = issuerURL;
		pc.authorization_endpoint = authOIDC;
		pc.token_endpoint = tokenUrl;
		pc.jwks_uri = keySetUrl;

		pc.registration_endpoint = getOurServerUrl() + LTI13_PATH + "registration_endpoint/" + key;

		pc.lti_platform_configuration = lpc;

		response.setContentType(APPLICATION_JSON);
		try {
			PrintWriter out = response.getWriter();
			out.print(JacksonUtil.prettyPrint(pc));
		} catch (Exception e) {
			log.error("Error in platform configuration request", e);
			LTI13Util.return400(response, "Platform configuration request failed");
			return;
		}
	}


	protected void handleKeySet(HttpServletRequest request, HttpServletResponse response) {
		PrintWriter out = null;

		String keySetJSON = null;
		try {
			keySetJSON = SakaiKeySetUtil.getKeySet();
		} catch (NoSuchAlgorithmException ex) {
			response.setHeader(ERROR_DETAIL, "NoSuchAlgorithmException");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error("NoSuchAlgorithmException");
			return;
		}

		// Send Response
		response.setContentType(APPLICATION_JSON);
		try {
			out = response.getWriter();
		} catch (Exception e) {
			log.error("Error getting response writer for key set request", e);
			LTI13Util.return400(response, "Key set request failed");
			return;
		}

		try {
			out.println(keySetJSON);
		} catch (Exception e) {
			log.error("Error writing key set response", e);
			LTI13Util.return400(response, "Key set response failed");
			return;
		}

	}

	protected void handleTokenPost(String tool_id, HttpServletRequest request, HttpServletResponse response) {
		/*
		Parameters for /imsblis/lti13/token/9
		Parameter Name - grant_type, Value - client_credentials
		Parameter Name - client_assertion_type, Value - urn:ietf:params:oauth:client-assertion-type:jwt-bearer
		Parameter Name - client_assertion, Value - eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwOlwvXC9sb2NhbGhvc3Q6ODg4OFwvdHN1Z2kiLCJzdWIiOiJsdGkxM19odHRwczpcL1wvd3d3LnNha2FpcHJvamVjdC5vcmdcL181MmE2N2I2OS1jNTk4LTRjZWQtYWNiMy1kOTQ5MzJkZTJiMWEiLCJhdWQiOiJodHRwOlwvXC9sb2NhbGhvc3Q6ODA4MFwvaW1zYmxpc1wvbHRpMTNcL3Rva2VuXC85IiwiaWF0IjoxNTM2NDMzODUwLCJleHAiOjE1MzY0MzM5MTAsImp0aSI6Imh0dHA6XC9cL2xvY2FsaG9zdDo4ODg4XC90c3VnaTViOTQxZWJhNWVkNjQifQ.JhwwgUEVV85HLteYmmSykQkMkmP-mcbV0R99tvP69hTFBJf3ZAS_uyfdXZoeRJaS5_hzwNf_b9HXYJWmZvYQK2NLt3s5GsW3h2pD4S3lVybIRXbpajr8NgeKA3BfsRLDoyKCLYn16BDR5w7ULZj0om8avVSFMUNbQYouc6XaTUPCZGfxPn-OPFYxX7SlDfIZjvbPWFxQh-cS90m_mKIcSYitoKrg9az59K6iGu-pq1PmZYSdt4xabh0_WoOiracvvJE6N1Um7A5enS3iXuHbCufKySIO2ykYtdRgVqhxP5YYPlar55nNRqEZtDgBgMMsneNePfMrifOvvFLkxnpefA
		Parameter Name - scope, Value - http://imsglobal.org/ags/lineitem http://imsglobal.org/ags/result/read
		 */

		if (tokenKeyPair == null) {
			LTI13Util.return400(response, "No token key available to sign tokens");
			log.error("No token key available to sign tokens");
			return;
		}

		String grant_type = request.getParameter(ClientAssertion.GRANT_TYPE);
		String client_assertion = request.getParameter(ClientAssertion.CLIENT_ASSERTION);
		String scope = request.getParameter(ClientAssertion.SCOPE);
		String missing = "";
		if (grant_type == null) {
			missing += " " + "grant_type";
		}
		if (client_assertion == null) {
			missing += " " + "client_assertion";
		}
		if (scope == null) {
			missing += " " + "scope";
		}
		if (missing.length() > 0) {
			LTI13Util.return400(response, "Token request missing fields:" + missing);
			log.error("Token Request missing fields: {}", missing);
			return;
		}

		String body = LTI13JwtUtil.rawJwtBody(client_assertion);
		if (body == null) {
			LTI13Util.return400(response, "Could not find Jwt Body in client_assertion");
			log.error("Could not find Jwt Body in client_assertion\n{}", client_assertion);
			return;
		}

		JSONObject jsonHeader = LTI13JwtUtil.jsonJwtHeader(client_assertion);
		if (jsonHeader == null) {
			LTI13Util.return400(response, "Could not parse Jwt Header in client_assertion");
			log.error("Could not parse Jwt Header in client_assertion\n{}", client_assertion);
			return;
		}

		Long toolKey = LTIUtil.toLongKey(tool_id);
		if (toolKey < 1) {
			LTI13Util.return400(response, "Invalid tool key");
			log.error("Invalid tool key {}", tool_id);
			return;
		}

		// Load the tool
		org.sakaiproject.lti.beans.LtiToolBean tool = ltiService.getToolDaoAsBean(toolKey, null, true);
		if (tool == null) {
			LTI13Util.return400(response, "Could not load tool");
			log.error("Could not load tool {}", tool_id);
			return;
		}

		// Get the correct public key.
		Key publicKey = null;
		try {
			publicKey = SakaiLTIUtil.getPublicKey(tool, client_assertion);
		} catch (Exception e) {
			log.error("Error getting public key", e);
			LTI13Util.return400(response, "Public key retrieval failed");
			return;
		}

		Jws<Claims> claims = Jwts.parser().setAllowedClockSkewSeconds(60).setSigningKey(publicKey).parseClaimsJws(client_assertion);

		if (claims == null) {
			LTI13Util.return400(response, "Could not verify signature");
			log.error("Could not verify signature {}", tool_id);
			return;
		}

		scope = scope.toLowerCase();


		SakaiAccessToken sat = new SakaiAccessToken();
		sat.tool_id = toolKey;
		Long issued = Long.valueOf(System.currentTimeMillis() / 1000L);
		sat.expires = issued + 3600L;

		// https://datatracker.ietf.org/doc/html/rfc6749#section-5.1
		HashSet<String> returnScopeSet = new HashSet<String> ();

		// Work through requested scopes
		if (scope.contains(LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY)) {
			if (!Boolean.TRUE.equals(tool.allowlineitems)) {
				LTI13Util.return400(response, "invalid_scope", LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY);
				log.error("Scope lineitem readonly not allowed {}", tool_id);
				return;
			}
			returnScopeSet.add(LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY);
			sat.addScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY);
		}

		if (scope.contains(LTI13ConstantsUtil.SCOPE_LINEITEM)) {
			if (!Boolean.TRUE.equals(tool.allowlineitems)) {
				LTI13Util.return400(response, "invalid_scope", LTI13ConstantsUtil.SCOPE_LINEITEM);
				log.error("Scope lineitem not allowed {}", tool_id);
				return;
			}
			returnScopeSet.add(LTI13ConstantsUtil.SCOPE_LINEITEM);

			sat.addScope(SakaiAccessToken.SCOPE_LINEITEMS);
			sat.addScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY);
		}

		if (scope.contains(LTI13ConstantsUtil.SCOPE_SCORE)) {
			if (!Boolean.TRUE.equals(tool.allowoutcomes) || !Boolean.TRUE.equals(tool.allowlineitems)) {
				LTI13Util.return400(response, "invalid_scope", LTI13ConstantsUtil.SCOPE_SCORE);
				log.error("Scope score not allowed {}", tool_id);
				return;
			}
			returnScopeSet.add(LTI13ConstantsUtil.SCOPE_SCORE);

			sat.addScope(SakaiAccessToken.SCOPE_SCORE);
		}

		if (scope.contains(LTI13ConstantsUtil.SCOPE_RESULT_READONLY)) {
			if (!Boolean.TRUE.equals(tool.allowoutcomes) || !Boolean.TRUE.equals(tool.allowlineitems)) {
				LTI13Util.return400(response, "invalid_scope", LTI13ConstantsUtil.SCOPE_RESULT_READONLY);
				log.error("Scope result readonly not allowed {}", tool_id);
				return;
			}
			returnScopeSet.add(LTI13ConstantsUtil.SCOPE_RESULT_READONLY);

			sat.addScope(SakaiAccessToken.SCOPE_RESULT_READONLY);
		}

		if (scope.contains(LTI13ConstantsUtil.SCOPE_NAMES_AND_ROLES)) {
			if (!Boolean.TRUE.equals(tool.allowroster)) {
				LTI13Util.return400(response, "invalid_scope", LTI13ConstantsUtil.SCOPE_NAMES_AND_ROLES);
				log.error("Scope names and roles not allowed {}", tool_id);
				return;
			}
			returnScopeSet.add(LTI13ConstantsUtil.SCOPE_NAMES_AND_ROLES);

			sat.addScope(SakaiAccessToken.SCOPE_ROSTER);
		}

		if (scope.contains(LTI13ConstantsUtil.SCOPE_CONTEXTGROUP_READONLY)) {
			if (!Boolean.TRUE.equals(tool.allowroster)) {
				LTI13Util.return400(response, "invalid_scope", LTI13ConstantsUtil.SCOPE_CONTEXTGROUP_READONLY);
				log.error("Scope context group not allowed {}", tool_id);
				return;
			}
			returnScopeSet.add(LTI13ConstantsUtil.SCOPE_CONTEXTGROUP_READONLY);

			sat.addScope(SakaiAccessToken.SCOPE_CONTEXTGROUP_READONLY);
		}

		String payload = JacksonUtil.toString(sat);
		String jws = Jwts.builder().setPayload(payload).signWith(tokenKeyPair.getPrivate()).compact();

		AccessToken at = new AccessToken();
		at.access_token = jws;
		at.scope = String.join(" ", new ArrayList<String>(returnScopeSet));

		String atsp = JacksonUtil.prettyPrintLog(at);

		response.setContentType(APPLICATION_JSON);
		try {
			PrintWriter out = response.getWriter();
			out.println(atsp);
			log.debug("Returning Token\n{}", atsp);
		} catch (IOException e) {
			log.error("Error in token post request", e);
			LTI13Util.return400(response, "Token post request failed");
			return;
		}
	}

	// SAK-47261 - lineItemId can only be null for old-style signed placements
	protected void handleLineItemScore(String signed_placement, String lineItemId, HttpServletRequest request, HttpServletResponse response) {

		// Make sure the lineItemId is a long
		Long lineitem_key = null;
		if ( lineItemId != null ) {
			try {
				lineitem_key = Long.parseLong(lineItemId);
			} catch (NumberFormatException e) {
				LTI13Util.return400(response, "Bad value for lineitem_key "+lineItemId);
				log.error("Bad value for lineitem_key "+lineItemId);
				return;
			}
		}

		// Load the access token, checking the the secret
		SakaiAccessToken sat = getSakaiAccessToken(tokenKeyPair.getPublic(), request, response);
		log.debug("sat={}", sat);

		if (sat == null) {
			return;  // Error already set
		}
		if (!sat.hasScope(SakaiAccessToken.SCOPE_SCORE)) {
			LTI13Util.return400(response, "Scope score not in access token");
			log.error("Scope score not in access token");
			return;
		}

		String jsonString;
		try {
			// https://stackoverflow.com/questions/1548782/retrieving-json-object-literal-from-httpservletrequest
			jsonString = IOUtils.toString(request.getInputStream(), java.nio.charset.StandardCharsets.UTF_8);
		} catch (IOException ex) {
			log.error("Could not read POST Data {}", ex.getMessage());
			LTI13Util.return400(response, "Could not read POST Data");
			return;
		}
		log.debug("jsonString={}", jsonString);

		Score scoreObj;
		try {
			ObjectMapper mapper = new ObjectMapper();
			scoreObj = mapper.readValue(jsonString, Score.class);
		} catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
			log.error("Could not read POST Data Jackson {}", ex.getMessage());
			LTI13Util.return400(response, "Could not read POST Data Jackson");
			return;
		}

		String userId = scoreObj.userId;
		if (userId == null) {
			LTI13Util.return400(response, "Missing userId");
			return;
		}

		Site site = null;
		org.sakaiproject.lti.beans.LtiToolBean tool = null;
		org.sakaiproject.lti.beans.LtiContentBean content = null;
		String assignment_name = null;

		// SAK-47261 - Legacy URL patterns with actual signed placement
		if ( isSignedPlacement(signed_placement) ) {
			content = loadContentCheckSignature(signed_placement, response);
			if (content == null) {
				LTI13Util.return400(response, "Could not load content from signed placement");
				log.error("Could not load content from signed placement = {}", signed_placement);
				return;
			}

			site = loadSiteFromContent(content, signed_placement, response);
			if (site == null) {
				LTI13Util.return400(response, "Could not load site associated with content");
				log.error("Could not load site associated with content={}", content.id);
				return;
			}

			tool = loadToolForContent(content, site, sat.tool_id, response);
			if (tool == null) {
				log.error("Could not load tool={} associated with content={}", sat.tool_id, content.id);
				return;
			}

			assignment_name = (String) content.title;
			if (assignment_name == null || assignment_name.length() < 1) {
				log.error("Could not determine assignment_name title {}", content.id);
				LTI13Util.return400(response, "Could not determine assignment_name");
				return;
			}

		} else { // SAK-47261 - It is just a site_id
			if ( lineitem_key == null ) {
				log.error("lineItem is required in url for site-id style urls={}", signed_placement);
				LTI13Util.return400(response, "lineItem is required in url for site-id style urls");
				return;
			}

			try {
				site = SiteService.getSite(signed_placement);
			} catch (IdUnusedException e) {
				log.error("No site/page associated with content siteId={}", signed_placement);
				LTI13Util.return400(response, "Could not load site associated with content");
				return;
			}

			tool = ltiService.getToolDaoAsBean(sat.tool_id, site.getId(), true);
			if (tool == null) {
				log.error("Could not load tool={}", sat.tool_id);
				LTI13Util.return400(response, "Missing tool");
				return;
			}

			if ( ! checkToolHasPlacements(sat.tool_id, signed_placement, response) ) return;

		}

		log.debug("tool={} content={} userId={}",
			tool != null ? tool.getId() : null,
			content != null ? content.getId() : null,
			userId);

		userId = SakaiLTIUtil.parseSubject(userId);
		if (!checkUserInSite(site, userId)) {
			log.warn("User {} not found in siteId={}", userId, site.getId());
			LTI13Util.return400(response, "User does not belong to site");
			return;
		}

		// TODO: Check if sat and tool match

		// When lineitem_key is null we are the "default" lineitem associated with the content object
		// if the content item is associated with an assignment, we talk to the assignment API,
		// if the content item is not associated with an assignment, we talk to the gradebook API
log.debug("calling SakaiLTIUtil.handleGradebookLTI13 bean version content="+content);
		Object retval = SakaiLTIUtil.handleGradebookLTI13(site, sat.tool_id, content, userId, lineitem_key, scoreObj);
		log.debug("handleGradebookLTI13 retval={}",retval);
		if ( retval instanceof String ) {
			LTI13Util.return400(response, (String) retval);
			return;
		}
	}

	// Receive a tool configuration
	// /imsblis/lti13/registration_endpoint/{tool-key}
	protected void handleRegistrationEndpointPost(String tool_key_str, HttpServletRequest request, HttpServletResponse response) {

		// Make sure the tool_key is a long
		Long tool_key = null;
		if ( tool_key_str != null ) {
			try {
				tool_key = Long.parseLong(tool_key_str);
			} catch (NumberFormatException e) {
				LTI13Util.return400(response, "Bad value for tool_key "+tool_key_str);
				log.error("Bad value for tool_key "+tool_key_str);
				return;
			}
		}

		// Get the authorization header
		// Authorization: Bearer eyJhbGciOiJSUzI1NiJ9.eyJ .
		String authorization = request.getHeader("authorization");

		if (authorization == null || !authorization.startsWith("Bearer")) {
			log.error("Invalid authorization {}", authorization);
			LTI13Util.return400(response, "invalid_authorization");
			return;
		}

		// https://stackoverflow.com/questions/7899525/how-to-split-a-string-by-space/7899558
		String[] parts = authorization.split("\\s+");
		if (parts.length != 2 || parts[1].length() < 1) {
			log.error("Bad authorization {}", authorization);
			LTI13Util.return400(response, "invalid_authorization");
			return;
		}

		String registration_token = parts[1];

		// TODO: Reject token with incorrect format, or expired

		String jsonString;
		try {
			// https://stackoverflow.com/questions/1548782/retrieving-json-object-literal-from-httpservletrequest
			jsonString = IOUtils.toString(request.getInputStream(), java.nio.charset.StandardCharsets.UTF_8);
		} catch (IOException ex) {
			log.error("Could not read POST Data {}", ex.getMessage());
			LTI13Util.return400(response, "Could not read POST Data");
			return;
		}

		// Don't fill my database up.
		if ( jsonString.length() > 300000 ) {
			LTI13Util.return400(response, "JSON too long ( > 300K");
			return;
		}

		log.debug("jsonString={}", jsonString);

		// TODO: Make this be a RegistrationRequest
		Object js = JSONValue.parse(jsonString);
		if (js == null || !(js instanceof JSONObject)) {
			LTI13Util.return400(response, "Badly formatted JSON");
			return;
		}
		JSONObject jso = (JSONObject) js;

		// Extract the bits
		String initiate_login_uri = SakaiLTIUtil.getStringNull(jso.get("initiate_login_uri"));
		String jwks_uri = SakaiLTIUtil.getStringNull(jso.get("jwks_uri"));
		log.debug("initiate_login_uri={} jwks_uri={}", initiate_login_uri, jwks_uri);
		Object redirect_uris_object = jso.get("redirect_uris");
		JSONArray redirect_uris = null;
		if ( redirect_uris_object != null && redirect_uris_object instanceof JSONArray ) {
			redirect_uris = (JSONArray) redirect_uris_object;
		}

		if (initiate_login_uri == null || jwks_uri == null || redirect_uris == null ) {
			LTI13Util.return400(response, "Missing initiate_login_uri, jwks_uri, redirect_uris");
			return;
		}

		org.sakaiproject.lti.beans.LtiToolBean tool = ltiService.getToolDaoAsBean(tool_key, null, true);
		if (tool == null) {
			log.error("Could not load tool={}", tool_key);
			LTI13Util.return400(response, "Missing tool");
			return;
		}

		// Check if the one time use token matching
		String tool_token = tool.lti13AutoToken;
		if ( tool_token == null || tool_token.length() < 1 ||
			! tool_token.equals(registration_token) ) {
			log.error("Bad registration_token");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		// Check the one time use token expiration
		int delta = 60*60; // An hour
		if ( ! LTI13Util.timeStampCheck(registration_token, delta) ) {
			log.error("Expired registration_token \n"+registration_token+":\n tool_token=\n"+tool_token+":");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String client_id = tool.lti13ClientId;

		jso.put("client_id", client_id);

		// TODO: Make this be a RegistrationResponse
		Object toolConfigurationObj = jso.get("https://purl.imsglobal.org/spec/lti-tool-configuration");
		if ( toolConfigurationObj instanceof JSONObject ) {
			JSONObject toolConfiguration = (JSONObject) toolConfigurationObj;
			String deployment_id = SakaiLTIUtil.getDeploymentId(null);
			toolConfiguration.put("deployment_id", deployment_id);
		}

		String json_out = null;
		try {
			json_out = JacksonUtil.prettyPrint(jso);
			tool.lti13AutoRegistration = json_out;
		} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
			log.error("Could not serialize JSON={}", e.getMessage());
			LTI13Util.return400(response, "Could not serialize JSON");
			return;

		}

		// Store the JSON
		tool.lti13AutoToken = "Used";
		tool.lti13AutoState = Integer.valueOf(2);
		String siteId = null;
		Object retval = ltiService.updateToolDao(tool_key, tool, siteId);

		if ( retval instanceof String) {
			log.error("Could not update tool={} retval={}", tool_key, retval);
			LTI13Util.return400(response, "Could not update tool");
			return;
		}

		response.setContentType(APPLICATION_JSON);
		try {
			PrintWriter out = response.getWriter();
			out.println(json_out);
			log.debug("Returning ToolConfiguration\n{}", json_out);
		} catch (IOException e) {
			log.error("Error in registration endpoint post request", e);
			LTI13Util.return400(response, "Registration endpoint post request failed");
			return;
		}

	}

	// https://github.com/IMSGlobal/LTI-spec-Names-Role-Provisioning/blob/develop/docs/names-role-provisioning-spec.md
	/*
	{
  "id" : "https://lms.example.com/sections/2923/memberships/?rlid=49566-rkk96",

  "context" : {
      "id": "2923-abc",
      "label": "CPS 435",
      "title": "CPS 435 Learning Analytics",
  }
  "members" : [
    {
      "status" : "Active",
      "name": "Jane Q. Public",
      "picture" : "https://platform.example.edu/jane.jpg",
      "given_name" : "Jane",
      "family_name" : "Doe",
      "middle_name" : "Marie",
      "email": "jane@platform.example.edu",
      "user_id" : "0ae836b9-7fc9-4060-006f-27b2066ac545",
      "roles": [
        "Instructor",
        "Mentor"
      ]
      "message" : [
        {
          "https://purl.imsglobal.org/spec/lti/claim/message_type" : "ltiResourceLinkRequest",
          "https://purl.imsglobal.org/spec/lti-bo/claim/basicoutcome" : {
            "lis_result_sourcedid": "example.edu:71ee7e42-f6d2-414a-80db-b69ac2defd4",
            "lis_outcome_service_url": "https://www.example.com/2344"
          },
          "https://purl.imsglobal.org/spec/lti/claim/custom": {
            "country" : "Canada",
            "user_mobile" : "123-456-7890"
          }
        }
      ]
    }
  ]
}
	 */
	protected void handleNamesAndRoles(String signed_placement, HttpServletRequest request, HttpServletResponse response)
			throws java.io.IOException {

		// HttpUtil.printHeaders(request);
		// HttpUtil.printParameters(request);
		log.debug("signed_placement={}", signed_placement);

		int start = NumberUtils.toInt(request.getParameter("start"), 0);
		int limit = NumberUtils.toInt(request.getParameter("limit"), -1);

		// Load the access token, checking the the secret
		SakaiAccessToken sat = getSakaiAccessToken(tokenKeyPair.getPublic(), request, response);
		if (sat == null) {
			return; // Error already set
		}

		if (!sat.hasScope(SakaiAccessToken.SCOPE_ROSTER)) {
			LTI13Util.return400(response, "Scope roster not in access token");
			log.error("Scope roster not in access token");
			return;
		}

		Site site = null;
		org.sakaiproject.lti.beans.LtiToolBean tool = null;

		// SAK-47261 - Legacy URL patterns with actual signed placement
		if ( isSignedPlacement(signed_placement) ) {
			org.sakaiproject.lti.beans.LtiContentBean content = loadContentCheckSignature(signed_placement, response);
			if (content == null) {
				LTI13Util.return400(response, "Could not load content from signed placement");
				log.error("Could not load content from signed placement = {}", signed_placement);
				return;
			}

			site = loadSiteFromContent(content, signed_placement, response);
			if (site == null) {
				LTI13Util.return400(response, "Could not load site associated with content");
				log.error("Could not load site associated with content={}", content.id);
				return;
			}

			tool = loadToolForContent(content, site, sat.tool_id, response);
			if (tool == null) {
				log.error("Could not load tool={} associated with content={}", sat.tool_id, content.id);
				return;
			}
		} else { // SAK-47261 - It is just a site_id
			try {
				site = SiteService.getSite(signed_placement);
			} catch (IdUnusedException e) {
				log.error("No site/page associated with content siteId={}", signed_placement);
				LTI13Util.return400(response, "Could not load site associated with content");
				return;
			}

			tool = ltiService.getToolDaoAsBean(sat.tool_id, site.getId(), true);
			if (tool == null) {
				log.error("Could not load tool={}", sat.tool_id);
				LTI13Util.return400(response, "Missing tool");
				return;
			}

			if ( ! checkToolHasPlacements(sat.tool_id, signed_placement, response) ) return;

		}

		String pagingstr = (String) site.getProperties().get(PLUS_NRPS_PAGING);
		int paging = NumberUtils.toInt(pagingstr, -1);
		if ( paging > 0 && ( limit < 0 || limit > paging ) ) limit = paging;

		// int allowOutcomes = (tool.allowoutcomes != null && Boolean.TRUE.equals(tool.allowoutcomes)) ? 1 : 0;

		/* SAK-47261 - Scope NRPS to Context, not Resource Link
		String assignment_name = (String) content.title;
		if (assignment_name == null || assignment_name.length() < 1) {
			assignment_name = null;
		}
		*/

		String maintainRole = site.getMaintainRole();

		PrintWriter out = null;

		SakaiLTIUtil.pushAdvisor();
		try {
			boolean success = false;

			List<Map<String, Object>> lm = new ArrayList<>();

			// Get users for each of the members. UserDirectoryService.getUsers will skip any undefined users.
			Set<Member> members = site.getMembers();
			Map<String, Member> memberMap = new HashMap<>();
			List<String> userIds = new ArrayList<>();
			for (Member member : members) {
				if ( ! member.isActive() ) continue;
				userIds.add(member.getUserId());
				memberMap.put(member.getUserId(), member);
			}

			List<User> users = UserDirectoryService.getUsers(userIds);

			String roleMapProp = tool.rolemap;
			Map<String, String> toolRoleMap = SakaiLTIUtil.convertOutboundRoleMapPropToMap(roleMapProp);

			// Hoist these out of the loop
			Map<String, String> propRoleMap = SakaiLTIUtil.convertOutboundRoleMapPropToMap(
				ServerConfigurationService.getString(SakaiLTIUtil.LTI_OUTBOUND_ROLE_MAP)
			);
			Map<String, String> defaultRoleMap = SakaiLTIUtil.convertOutboundRoleMapPropToMap(
				SakaiLTIUtil.LTI_OUTBOUND_ROLE_MAP_DEFAULT
			);

			Map<String, String> propLegacyMap = SakaiLTIUtil.convertLegacyRoleMapPropToMap(
				ServerConfigurationService.getString(SakaiLTIUtil.LTI_LEGACY_ROLE_MAP)
			);
			Map<String, String> defaultLegacyMap = SakaiLTIUtil.convertLegacyRoleMapPropToMap(
				SakaiLTIUtil.LTI_LEGACY_ROLE_MAP_DEFAULT
			);

			// Do we need a Link header
			// https://www.imsglobal.org/spec/lti-nrps/v2p0#limit-query-parameter
			// https://www.w3.org/Protocols/9707-link-header.html
			int user_count = users.size();
			if ( start < 0 ) start = 0;
			if ( limit < 0 ) limit = user_count;
			int next = start+limit;

			if ( next >= user_count) {
				log.debug("No Link header start={} limit={} count={} next={}", start, limit, user_count, next);
			} else {
				log.debug("Link header start={} limit={} count={} next={}", start, limit, user_count, next);
				// /imsblis/lti13/namesandroles/context:6
				String linkHeader = getOurServerUrl() + LTI13_PATH + "namesandroles/" + signed_placement + "?start=" + next;
				if ( limit > 0 ) linkHeader += "&limit=" + limit;
				linkHeader = "<" + linkHeader + ">; rel=\"next\"";
				log.debug("Link: {}", linkHeader);
			    response.addHeader("Link", linkHeader);
			}

			int current = 0;
			for (User user : users) {
				if ( current < start) {
					current++;
					continue;
				}
				if ( current == next) {
					log.debug("Limit reached current={} next={} start={} limit={}", current, next, start, limit);
					break;
				}

				JSONObject jo = new JSONObject();
				jo.put("status", "Active");
				String lti11_legacy_user_id = user.getId();
				jo.put("lti11_legacy_user_id", lti11_legacy_user_id);
				String subject = SakaiLTIUtil.getSubject(lti11_legacy_user_id, site.getId());
				jo.put("user_id", subject);   // TODO: Should be subject - LTI13 Quirk
				jo.put("lis_person_sourcedid", user.getEid());

				if (Boolean.TRUE.equals(tool.sendname)) {
					jo.put("name", user.getDisplayName());
					jo.put("given_name", user.getFirstName());
					jo.put("family_name", user.getLastName());
				}
				if (Boolean.TRUE.equals(tool.sendemailaddr)) {
					jo.put("email", user.getEmail());
				}

				Member member = memberMap.get(user.getId());
				Map<String, Object> mm = new TreeMap<>();
				Role role = member.getRole();
				String ims_user_id = member.getUserId();
				String outboundRole = null;
				String sakaiRole = role.getId();

				if (StringUtils.isNotBlank(sakaiRole)) {
					outboundRole = SakaiLTIUtil.mapOutboundRole(sakaiRole, toolRoleMap, propRoleMap, defaultRoleMap, propLegacyMap, defaultLegacyMap);
					log.debug("SakaiLTIUtil.mapOutboundRole sakaiRole={} outboundRole={}", sakaiRole, outboundRole);
				}

				JSONArray roles = new JSONArray();
				if ( StringUtils.isNotBlank(outboundRole) ) {
					roles.add(outboundRole);
				} else if (ComponentManager.get(AuthzGroupService.class).isAllowed(ims_user_id, SiteService.SECURE_UPDATE_SITE, "/site/" + site.getId())) {
					roles.add(LTI13ConstantsUtil.ROLE_INSTRUCTOR);
				} else {
					roles.add(LTI13ConstantsUtil.ROLE_LEARNER);
				}

				jo.put("roles", roles);

				JSONObject sakai_ext = new JSONObject();

				/* SAK-47261 - Scope NRPS to Context, not Resource Link
				if ( sat.hasScope(SakaiAccessToken.SCOPE_SCORE)  && assignment_name != null ) {
					String placement_secret  = (String) content.placementSecret;
					String placement_id = getPlacementId(signed_placement);
					String result_sourcedid = SakaiLTIUtil.getSourceDID(user, placement_id, placement_secret);
					if ( result_sourcedid != null ) sakai_ext.put("lis_result_sourcedid",result_sourcedid);
				}
				*/
				sakai_ext.put("sakai_role", sakaiRole);

				// TODO: Should we only do this if requested via groups=true ??
				Collection groups = site.getGroupsWithMember(ims_user_id);

				// https://www.imsglobal.org/spec/lti-gs/v1p0#within-memberships
				if (groups.size() > 0) {
					JSONArray group_enrollments = new JSONArray();
					JSONArray lgm = new JSONArray();
					for (Iterator i = groups.iterator();i.hasNext();) {
						Group group = (Group) i.next();
						JSONObject groupObj = new JSONObject();
						groupObj.put("id", group.getId());
						groupObj.put("title", group.getTitle());
						lgm.add(groupObj);

						JSONObject ltiGroupObj = new JSONObject();
						ltiGroupObj.put("group_id", group.getId());
						group_enrollments.add(ltiGroupObj);
					}
					sakai_ext.put("sakai_groups", lgm);
					jo.put("group_enrollments", group_enrollments);
				}

				jo.put("sakai_ext", sakai_ext);

				if (out == null) {
						JSONObject context_obj = new JSONObject();
						context_obj.put("id", site.getId());
						context_obj.put("title", site.getTitle());

						response.setContentType(APPLICATION_JSON);
						out = response.getWriter();
						out.println("{");
						String currentUrl = getOurServerUrl() + LTI13_PATH + "namesandroles/" + signed_placement;
						out.println(" \"id\" : "+JacksonUtil.toString(currentUrl)+",");
						out.println(" \"context\" : ");
						out.print(JacksonUtil.prettyPrint(context_obj));
						out.println(",");
						out.println(" \"members\": [");
				} else {
						out.println(",");
				}

				out.print(JacksonUtil.prettyPrint(jo));
				current++;

			}
			if ( out != null ) {
				out.println("");
				out.println(" ] }");
			}
		} finally {
			SakaiLTIUtil.popAdvisor();
		}

	}

	protected void handleGroupService(String site_id, HttpServletRequest request, HttpServletResponse response)
			throws java.io.IOException {

		String subject = request.getParameter("user_id");
		String user_id = null;
		if ( StringUtils.isNotEmpty(subject)) {
			user_id = SakaiLTIUtil.parseSubject(subject);
		}

		log.debug("site_id={} subject={} user_id={}", site_id, subject, user_id);

		int start = NumberUtils.toInt(request.getParameter("start"), 0);
		int limit = NumberUtils.toInt(request.getParameter("limit"), -1);

		// Load the access token, checking the the secret
		SakaiAccessToken sat = getSakaiAccessToken(tokenKeyPair.getPublic(), request, response);
		if (sat == null) {
			return; // Error already set
		}

		if (!sat.hasScope(SakaiAccessToken.SCOPE_CONTEXTGROUP_READONLY)) {
			LTI13Util.return400(response, "Scope contextgroup.readonly not in access token");
			log.error("Scope contextgroup.readonly not in access token");
			return;
		}

		Site site = null;
		org.sakaiproject.lti.beans.LtiToolBean tool = null;

		try {
			site = SiteService.getSite(site_id);
		} catch (IdUnusedException e) {
			log.error("No site/page associated with content siteId={}", site_id);
			LTI13Util.return400(response, "Could not load site associated with content");
			return;
		}

		tool = ltiService.getToolDaoAsBean(sat.tool_id, site.getId(), true);
		if (tool == null) {
			log.error("Could not load tool={}", sat.tool_id);
			LTI13Util.return400(response, "Missing tool");
			return;
		}

		// Don't let a tool access groups unless it is placed *somewhere* in this site
		if ( ! checkToolHasPlacements(sat.tool_id, site_id, response) ) return;

		String pagingstr = (String) site.getProperties().get(PLUS_NRPS_PAGING);
		int paging = NumberUtils.toInt(pagingstr, -1);
		if ( paging > 0 && ( limit < 0 || limit > paging ) ) limit = paging;

		PrintWriter out = null;

		Collection<Group> groups = StringUtils.isEmpty(user_id) ?
			site.getGroups() :
			site.getGroupsWithMember(user_id);

		SakaiLTIUtil.pushAdvisor();

		try {
			boolean success = false;

			// Do we need a Link header
			// https://www.imsglobal.org/spec/lti-nrps/v2p0#limit-query-parameter
			// https://www.w3.org/Protocols/9707-link-header.html
			int group_count = groups.size();
			if ( start < 0 ) start = 0;
			if ( limit < 0 ) limit = group_count;
			int next = start+limit;

			if ( next >= group_count) {
				log.debug("No Link header start={} limit={} count={} next={}", start, limit, group_count, next);
			} else {
				log.debug("Link header start={} limit={} count={} next={}", start, limit, group_count, next);
				// /imsblis/lti13/namesandroles/context:6
				String linkHeader = getOurServerUrl() + LTI13_PATH + "groupsservice/" + site_id + "?start=" + next;
				if ( limit > 0 ) linkHeader += "&limit=" + limit;
				linkHeader = "<" + linkHeader + ">; rel=\"next\"";
				log.debug("Link: {}", linkHeader);
				response.addHeader("Link", linkHeader);
			}

			boolean first = true;
			int current = 0;
			String currentUrl = getOurServerUrl() + LTI13_PATH + "groupsservice/" + site_id;

			response.setContentType(LTI13ConstantsUtil.CONTEXTGROUPCONTAINER_TYPE);
			out = response.getWriter();
			out.println("{");
			out.println(" \"id\" : "+JacksonUtil.toString(currentUrl)+", ");
			if ( StringUtils.isNotEmpty(subject) ) {
				out.println(" \"user_id\" : "+JacksonUtil.toString(subject)+", ");
			}
			out.println(" \"groups\": [");

			for (Group group : groups) {

				if ( current < start) {
					current++;
					continue;
				}
				if ( current == next) {
					log.debug("Limit reached current={} next={} start={} limit={}", current, next, start, limit);
					break;
				}

				if ( ! first ) {
					 out.println(",");
				}
				first = false;

				JSONObject groupObj = new JSONObject();
				groupObj.put("id", group.getId());
				groupObj.put("name", group.getTitle());
				groupObj.put("tag", "whatever");

				out.print(JacksonUtil.prettyPrint(groupObj));
				current++;
			}

			if ( out != null ) {
				out.println("");
				out.println(" ] }");
			}
		} finally {
			SakaiLTIUtil.popAdvisor();
		}

	}

	protected SakaiAccessToken getSakaiAccessToken(Key publicKey, HttpServletRequest request, HttpServletResponse response) {
	        log.debug("publicKey={}", publicKey);

	        String authorization = request.getHeader("authorization");

		if (authorization == null || !authorization.startsWith("Bearer")) {
			log.error("Invalid authorization {}", authorization);
			LTI13Util.return403(response, "invalid_authorization");
			return null;
		}

		// https://stackoverflow.com/questions/7899525/how-to-split-a-string-by-space/7899558
		String[] parts = authorization.split("\\s+");
		if (parts.length != 2 || parts[1].length() < 1) {
			log.error("Bad authorization {}", authorization);
			LTI13Util.return403(response, "invalid_authorization");
			return null;
		}

		String jws = parts[1];
		Claims claims;
		try {
			claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jws).getBody();
		} catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException
				| io.jsonwebtoken.security.SignatureException | IllegalArgumentException e) {
			log.error("{} Signature error {}\n{}", e.getClass().getName(), e.getMessage(), jws, e);
			LTI13Util.return403(response, "signature_error");
			return null;
		}

		// Reconstruct the SakaiAccessToken
		// https://www.baeldung.com/jackson-deserialization
		SakaiAccessToken sat;
		try {
			ObjectMapper mapper = new ObjectMapper();
			String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(claims);
			// System.out.println("jsonResult=" + jsonResult);
			sat = new ObjectMapper().readValue(jsonResult, SakaiAccessToken.class);
		} catch (IOException ex) {
			log.error("PARSE ERROR {}\n{}", ex.getMessage(), claims.toString());
			LTI13Util.return403(response, "token_parse_failure", ex.getMessage());
			return null;
		}

		// Validity check the access token
		if (sat.tool_id != null && sat.scope != null && sat.expires != null) {
			// All good
		} else {
			log.error("SakaiAccessToken missing required data {}", sat);
			LTI13Util.return403(response, "Missing required data in access_token");
			return null;
		}

		return sat;
	}

	// Sanity check signed_placement
	// f093de4f8b98530abb0e7784c380ab1668510a7308cc454c79d4e7a0334ab268:::92e7ddf2-1c60-486c-97ae-bc2ffbde8e67:::content:6

	/*
			String suffix =  ":::" + context_id + ":::" + resource_link_id;
			String base_string = placementSecret + suffix;
			String signature = LegacyShaUtil.sha256Hash(base_string);
			return signature + suffix;
	 */

	protected static boolean isSignedPlacement(String signed_placement)
	{
		String[] parts = signed_placement.split(":::");
		if (parts.length != 3 || parts[0].length() < 1 || parts[0].length() > 10000
				|| parts[1].length() < 1 || parts[2].length() <= 8
				|| !parts[2].startsWith("content:")) {
			return false;
		}
		return true;
	}

	protected static String[] splitSignedPlacement(String signed_placement, HttpServletResponse response) {
		if ( ! isSignedPlacement(signed_placement) ) {
			log.error("Bad signed_placement format {}", signed_placement);
			LTI13Util.return400(response, "bad signed_placement");
			return null;
		}
		String[] parts = signed_placement.split(":::");
		return parts;
	}

	// Assumes signature is correct and format is correct - call after loadContentCheckSignature
	protected static String getPlacementId(String signed_placement) {
		String[] parts = signed_placement.split(":::");
		return parts.length == 3 ? parts[2] : null;
	}

	// Assumes signature is correct and format is correct - call after loadContentCheckSignature
	protected static String getContextId(String signed_placement, HttpServletResponse response) {
		String[] parts = signed_placement.split(":::");
		return parts.length == 3 ? parts[1] : null;
	}

	protected static org.sakaiproject.lti.beans.LtiContentBean loadContentCheckSignature(String signed_placement, HttpServletResponse response) {

		String[] parts = splitSignedPlacement(signed_placement, response);
		if (parts == null) {
			return null;
		}

		String received_signature = parts[0];
		String context_id = parts[1];
		String placement_id = parts[2];
		log.debug("signature={} context_id={} placement_id={}", received_signature, context_id, placement_id);

		String contentIdStr = placement_id.substring(8);
		Long contentKey = LTIUtil.toLongKey(contentIdStr);
		if (contentKey < 0) {
			log.error("Bad placement format {}", signed_placement);
			LTI13Util.return400(response, "bad placement");
			return null;
		}

		// Note that all of the above checking requires no database access :)
		// Now we have a valid access token and valid JSON, proceed with validating the signed_placement
		org.sakaiproject.lti.beans.LtiContentBean content = ltiService.getContentAsBean(contentKey, context_id);
		if (content == null) {
			log.error("Could not load Content Item {}", contentKey);
			LTI13Util.return400(response, "Could not load Content Item");
			return null;
		}

		String placementSecret = content.placementsecret;
		if (placementSecret == null) {
			log.error("Could not load placementsecret {}", contentKey);
			LTI13Util.return400(response, "Could not load placementsecret");
			return null;
		}

		// Validate the signed_placement signature before proceeding
		String suffix = ":::" + context_id + ":::" + placement_id;
		String base_string = placementSecret + suffix;
		String signature = LegacyShaUtil.sha256Hash(base_string);
		if (signature == null || !signature.equals(received_signature)) {
			log.error("Could not verify signed_placement {}", signed_placement);
			LTI13Util.return400(response, "Could not verify signed_placement");
			return null;
		}

		return content;
	}

	protected Site loadSiteFromContent(org.sakaiproject.lti.beans.LtiContentBean content, String signed_placement, HttpServletResponse response) {
		String[] parts = splitSignedPlacement(signed_placement, response);
		if (parts == null) {
			return null;
		}

		String context_id = parts[1];

		// Good signed_placement, lets load the site and tool
		String siteId = content.siteId;
		if (siteId == null) {
			log.error("Could not find site content={}", content.id);
			LTI13Util.return400(response, "Could not find site for content");
			return null;
		}

		if (!siteId.equals(context_id)) {
			log.error("Found incorrect site for content={}", content.id);
			LTI13Util.return400(response, "Found incorrect site for content");
			return null;
		}

		Site site;
		try {
			site = SiteService.getSite(siteId);
		} catch (IdUnusedException e) {
			log.error("No site/page associated with content siteId={}", siteId);
			LTI13Util.return400(response, "Could not load site associated with content");
			return null;
		}

		return site;
	}

	protected static boolean checkUserInSite(Site site, String userId) {
		// Make sure user exists in site
		// Make sure the user exists in the site
		boolean userExistsInSite = false;
		try {
			Member member = site.getMember(userId);
			if (member != null) {
				userExistsInSite = true;
			}
		} catch (Exception e) {
			userExistsInSite = false;
		}
		return userExistsInSite;
	}

	protected static boolean checkToolHasPlacements(Long toolKey, String siteId, HttpServletResponse response)
	{
		 List<org.sakaiproject.lti.beans.LtiContentBean> contents = ltiService.getContentsAsBeans(null, null, 0, 2, siteId);
		if (contents.size() < 1) {
			log.error("Tool id={} has no placements in site={}", toolKey, siteId);
			LTI13Util.return400(response, "No placements for tool");
			return false;
		}
		return true;
	}

	protected org.sakaiproject.lti.beans.LtiToolBean loadToolForContent(org.sakaiproject.lti.beans.LtiContentBean content, Site site, Long expected_tool_id, HttpServletResponse response) {
		Long toolKey = LTIUtil.toLongKey(content.toolId);
		if (toolKey < 0 || !toolKey.equals(expected_tool_id)) {
			log.error("Content / Tool invalid content={} tool={}", content.id, toolKey);
			LTI13Util.return400(response, "Content / Tool mismatch");
			return null;
		}

		org.sakaiproject.lti.beans.LtiToolBean tool = ltiService.getToolDaoAsBean(toolKey, site.getId(), true);
		if (tool == null) {
			log.error("Could not load tool={}", toolKey);
			LTI13Util.return400(response, "Missing tool");
			return null;
		}
		return tool;
	}

	protected String getPostData(HttpServletRequest request, HttpServletResponse response)
	{
		String jsonString;
		try {
			// https://stackoverflow.com/questions/1548782/retrieving-json-object-literal-from-httpservletrequest
			jsonString = IOUtils.toString(request.getInputStream(), java.nio.charset.StandardCharsets.UTF_8);
		} catch (IOException ex) {
			log.error("Could not read POST Data {}", ex.getMessage());
			LTI13Util.return400(response, "Could not read POST Data");
			return null;
		}
		log.debug("jsonString={}", jsonString);
		return jsonString;
	}

	protected Object getJSONFromPOST(HttpServletRequest request, HttpServletResponse response)
	{
		String jsonString = getPostData(request, response);
		if ( jsonString == null ) return null; // Error already set

		Object js = JSONValue.parse(jsonString);
		if (js == null || !(js instanceof JSONObject)) {
			log.error("Badly formatted JSON");
			LTI13Util.return400(response, "Badly formatted JSON");
			return null;
		}
		return js;
	}

	protected Object getObjectFromPOST(HttpServletRequest request, HttpServletResponse response, Class whichClass) {
		// https://www.baeldung.com/jackson-deserialization
		String jsonString = getPostData(request, response);
		if ( jsonString == null ) return null; // Error already set

		try {
			Object retval = new ObjectMapper().readValue(jsonString, whichClass);
			return retval;
		} catch (IOException ex) {
			String error = "Could not parse input as " + whichClass.getSimpleName();
			log.error(error);
			LTI13Util.return400(response, error);
			return null;
		}
	}

	protected SakaiLineItem getLineItemFilter(HttpServletRequest request)
	{
		SakaiLineItem retval = new SakaiLineItem();
		boolean found = false;
		String tag = request.getParameter("tag");
		if ( tag != null && tag.length() > 0 ) {
			found = true;
			retval.tag = tag;
		}
		String lti_link_id = request.getParameter("lti_link_id");
		if ( lti_link_id != null && lti_link_id.length() > 0 ) {
			found = true;
			retval.resourceLinkId = lti_link_id;
		}
		String resource_id = request.getParameter("resource_id");
		if ( resource_id != null && resource_id.length() > 0 ) {
			found = true;
			retval.resourceId = resource_id;
		}

		if ( ! found ) return null;
		return retval;
	}

	/**
	 * Add a new line item for this placement
	 *
	 * @param signed_placement
	 * @param request
	 * @param response
	 */
	private void handleLineItemsPost(String signed_placement, HttpServletRequest request, HttpServletResponse response) throws IOException {

		// Load the access token, checking the the secret
		SakaiAccessToken sat = getSakaiAccessToken(tokenKeyPair.getPublic(), request, response);
		log.debug("sat={}", sat);

		if (sat == null) {
			return;  // No need - error is already set
		}
		if (!sat.hasScope(SakaiAccessToken.SCOPE_LINEITEMS)) {
			log.error("Scope lineitems not in access token");
			LTI13Util.return400(response, "Scope lineitems not in access token");
			return;
		}

		SakaiLineItem item = (SakaiLineItem) getObjectFromPOST(request, response, SakaiLineItem.class);
		if ( item == null )  {
			return; // Error already handled
		}

		Site site = null;
		org.sakaiproject.lti.beans.LtiToolBean tool = null;

		// SAK-47261 - Legacy URL patterns with actual signed placement
		if ( isSignedPlacement(signed_placement) ) {
			org.sakaiproject.lti.beans.LtiContentBean content = loadContentCheckSignature(signed_placement, response);
			if (content == null) {
				LTI13Util.return400(response, "Could not load content from signed placement");
				log.error("Could not load content from signed placement = {}", signed_placement);
				return;
			}

			site = loadSiteFromContent(content, signed_placement, response);
			if (site == null) {
				LTI13Util.return400(response, "Could not load site associated with content");
				log.error("Could not load site associated with content={}", content.id);
				return;
			}

			tool = loadToolForContent(content, site, sat.tool_id, response);
			if (tool == null) {
				log.error("Could not load tool={} associated with content={}", sat.tool_id, content.id);
				return;
			}
		} else { // SAK-47261 - It is just a site_id
			try {
				site = SiteService.getSite(signed_placement);
			} catch (IdUnusedException e) {
				log.error("No site/page associated with content siteId={}", signed_placement);
				LTI13Util.return400(response, "Could not load site associated with content");
				return;
			}

			tool = ltiService.getToolDaoAsBean(sat.tool_id, site.getId(), true);
			if (tool == null) {
				log.error("Could not load tool={}", sat.tool_id);
				LTI13Util.return400(response, "Missing tool");
				return;
			}

			if ( ! checkToolHasPlacements(sat.tool_id, signed_placement, response) ) return;

		}

		Assignment retval;
		try {
			retval = LineItemUtil.createLineItem(site, sat.tool_id, null /*content*/, item);
		} catch (Exception e) {
			log.error("Could not create lineitem: "+e.getMessage());
			LTI13Util.return400(response, "Could not create lineitem: "+e.getMessage());
			return;
		}

		// Add the link to this lineitem
		item.id = getOurServerUrl() + LTI13_PATH + "lineitems/" + signed_placement + "/" + retval.getId();

		log.debug("Lineitem item={}",item);
		response.setContentType(SakaiLineItem.CONTENT_TYPE);

		PrintWriter out = response.getWriter();
		String json_out = JacksonUtil.prettyPrint(item);
		log.debug("response={}", json_out);
		out.print(json_out);
	}

	/**
	 * Add a new line item for this placement
	 *
	 * @param signed_placement
	 * @param request
	 * @param response
	 */
	private void handleLineItemsUpdate(String signed_placement, String lineItem, HttpServletRequest request, HttpServletResponse response) throws IOException {

		// Make sure the lineItem id is a long
		Long lineitem_key;
		try {
			lineitem_key = Long.parseLong(lineItem);
		} catch (NumberFormatException e) {
			LTI13Util.return400(response, "Bad value for lineitem_key "+lineItem);
			log.error("Bad value for lineitem_key "+lineItem);
			return;
		}

		// Load the access token, checking the the secret
		SakaiAccessToken sat = getSakaiAccessToken(tokenKeyPair.getPublic(), request, response);
		log.debug("sat={}", sat);

		if (sat == null) {
			return;  // No need - error is already set
		}
		if (!sat.hasScope(SakaiAccessToken.SCOPE_LINEITEMS)) {
			LTI13Util.return400(response, "Scope lineitems not in access token");
			log.error("Scope lineitems not in access token");
			return;
		}

		SakaiLineItem item = (SakaiLineItem) getObjectFromPOST(request, response, SakaiLineItem.class);
		if ( item == null ) return; // Error alredy handled

		Site site = null;
		org.sakaiproject.lti.beans.LtiToolBean tool = null;

		// SAK-47261 - Legacy URL patterns with actual signed placement
		if ( isSignedPlacement(signed_placement) ) {
			org.sakaiproject.lti.beans.LtiContentBean content = loadContentCheckSignature(signed_placement, response);
			if (content == null) {
				LTI13Util.return400(response, "Could not load content from signed placement");
				log.error("Could not load content from signed placement = {}", signed_placement);
				return;
			}

			site = loadSiteFromContent(content, signed_placement, response);
			if (site == null) {
				LTI13Util.return400(response, "Could not load site associated with content");
				log.error("Could not load site associated with content={}", content.id);
				return;
			}

			tool = loadToolForContent(content, site, sat.tool_id, response);
			if (tool == null) {
				log.error("Could not load tool={} associated with content={}", sat.tool_id, content.id);
				return;
			}
		} else { // SAK-47261 - It is just a site_id
			try {
				site = SiteService.getSite(signed_placement);
			} catch (IdUnusedException e) {
				log.error("No site/page associated with content siteId={}", signed_placement);
				LTI13Util.return400(response, "Could not load site associated with content");
				return;
			}

			tool = ltiService.getToolDaoAsBean(sat.tool_id, site.getId(), true);
			if (tool == null) {
				log.error("Could not load tool={}", sat.tool_id);
				LTI13Util.return400(response, "Missing tool");
				return;
			}

			if ( ! checkToolHasPlacements(sat.tool_id, signed_placement, response) ) return;

		}

		Assignment retval;
		try {
			retval = LineItemUtil.updateLineItem(site, sat.tool_id, lineitem_key, item);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			LTI13Util.return400(response, "Could not update lineitem: "+e.getMessage());
			return;
		}

		// Add the link to this lineitem
		item.id = getOurServerUrl() + LTI13_PATH + "lineitems/" + signed_placement + "/" + retval.getId();

		log.debug("Lineitem item={}",item);
		response.setContentType(SakaiLineItem.CONTENT_TYPE);

		PrintWriter out = response.getWriter();
		out.print(JacksonUtil.prettyPrint(item));
	}

	/**
	 * List all LineItems for this placement ore retrieve the single LineItem created for this placement
	 *
	 * @param signed_placement
	 * @param all - Retrieve all the line items associated with the tool's placement.  Otherwise return one LineItem.
	 * @param filter - A LineItem with field upon which to filter, can also be null
	 * @param request
	 * @param response
	 */
	private void handleLineItemsGet(String signed_placement, boolean all, SakaiLineItem filter,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.debug("signed_placement={}; all={}", signed_placement, all);

		// Load the access token, checking the the secret
		SakaiAccessToken sat = getSakaiAccessToken(tokenKeyPair.getPublic(), request, response);
		if (sat == null) {
			return;
		}

		if (! (sat.hasScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY) || sat.hasScope(SakaiAccessToken.SCOPE_LINEITEMS) )) {
			LTI13Util.return400(response, "Scope lineitems.readonly not in access token");
			log.error("Scope lineitems.readonly not in access token");
			return;
		}

		Site site = null;
		org.sakaiproject.lti.beans.LtiToolBean tool = null;
		org.sakaiproject.lti.beans.LtiContentBean content = null;

		// SAK-47261 - Legacy URL patterns with actual signed placement
		if ( isSignedPlacement(signed_placement) ) {
			content = loadContentCheckSignature(signed_placement, response);
			if (content == null) {
				LTI13Util.return400(response, "Could not load content from signed placement");
				log.error("Could not load content from signed placement = {}", signed_placement);
				return;
			}

			site = loadSiteFromContent(content, signed_placement, response);
			if (site == null) {
				LTI13Util.return400(response, "Could not load site associated with content");
				log.error("Could not load site associated with content={}", content.id);
				return;
			}

			tool = loadToolForContent(content, site, sat.tool_id, response);
			if (tool == null) {
				log.error("Could not load tool={} associated with content={}", sat.tool_id, content.id);
				return;
			}
		} else { // SAK-47261 - It is just a site_id
			try {
				site = SiteService.getSite(signed_placement);
			} catch (IdUnusedException e) {
				log.error("No site/page associated with content siteId={}", signed_placement);
				LTI13Util.return400(response, "Could not load site associated with content");
				return;
			}

			tool = ltiService.getToolDaoAsBean(sat.tool_id, site.getId(), true);
			if (tool == null) {
				log.error("Could not load tool={}", sat.tool_id);
				LTI13Util.return400(response, "Missing tool");
				return;
			}

			if ( ! checkToolHasPlacements(sat.tool_id, signed_placement, response) ) return;

		}

		// If we are only returning a single line item
		if ( ! all ) {
			if ( content != null ) {
				response.setContentType(SakaiLineItem.CONTENT_TYPE);
				SakaiLineItem item = LineItemUtil.getDefaultLineItem(site, content);
				log.debug("single line item = {}", JacksonUtil.prettyPrint(item));
				PrintWriter out = response.getWriter();
				out.print(JacksonUtil.prettyPrint(item));
				return;
			}
			log.error("Single line item no longer supported with context_id scoped APIs");
			LTI13Util.return400(response, "Single line item no longer supported with context_id scopeed APIs");
			return;
		}

		// Find the line items created for this tool
		log.debug("filter={}", JacksonUtil.prettyPrint(filter));
		List<SakaiLineItem> toolItems = LineItemUtil.getLineItemsForTool(signed_placement, site, sat.tool_id, filter);

		response.setContentType(SakaiLineItem.CONTENT_TYPE_CONTAINER);
		PrintWriter out = response.getWriter();
		out.print("[");
		boolean first = true;

		for (SakaiLineItem item : toolItems) {
			out.println(first ? "" : ",");
			first = false;
			log.debug("line item = {}", JacksonUtil.prettyPrint(item));
			out.print(JacksonUtil.prettyPrint(item));
		}
		out.println("");
		out.println("]");
	}

	/**
	 * Provide the detail or results for a tool created lineitem
	 * @param signed_placement
	 * @param lineItem - Can be null
	 * @param results
	 * @param request
	 * @param response
	 */
	private void handleLineItemsDetail(String signed_placement, String lineItem, boolean results, String user_id,
		HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		log.debug("signed_placement={}", signed_placement);

		// Make sure the lineItem id is a long
		Long lineitem_key = null;
		if ( lineItem != null ) {
			try {
				lineitem_key = Long.parseLong(lineItem);
			} catch (NumberFormatException e) {
				LTI13Util.return400(response, "Bad value for lineitem_key "+lineItem);
				log.error("Bad value for lineitem_key "+lineItem);
				return;
			}
		}

		// Load the access token, checking the the secret
		SakaiAccessToken sat = getSakaiAccessToken(tokenKeyPair.getPublic(), request, response);
		if (sat == null) {
			return;
		}

		if (results && ! (sat.hasScope(SakaiAccessToken.SCOPE_RESULT_READONLY)) ) {
			LTI13Util.return400(response, "Scope result.readonly not in access token");
			log.error("Scope result.readonly not in access token");
			return;
		}

		Site site = null;
		org.sakaiproject.lti.beans.LtiToolBean tool = null;
		org.sakaiproject.lti.beans.LtiContentBean content = null;

		// SAK-47261 - Legacy URL patterns with actual signed placement
		if ( isSignedPlacement(signed_placement) ) {
			content = loadContentCheckSignature(signed_placement, response);
			if (content == null) {
				LTI13Util.return400(response, "Could not load content from signed placement");
				log.error("Could not load content from signed placement = {}", signed_placement);
				return;
			}

			site = loadSiteFromContent(content, signed_placement, response);
			if (site == null) {
				LTI13Util.return400(response, "Could not load site associated with content");
				log.error("Could not load site associated with content={}", content.id);
				return;
			}

			tool = loadToolForContent(content, site, sat.tool_id, response);
			if (tool == null) {
				log.error("Could not load tool={} associated with content={}", sat.tool_id, content.id);
				return;
			}
		} else { // SAK-47261 - It is just a site_id
			try {
				site = SiteService.getSite(signed_placement);
			} catch (IdUnusedException e) {
				log.error("No site/page associated with content siteId={}", signed_placement);
				LTI13Util.return400(response, "Could not load site associated with content");
				return;
			}

			tool = ltiService.getToolDaoAsBean(sat.tool_id, site.getId(), true);
			if (tool == null) {
				log.error("Could not load tool={}", sat.tool_id);
				LTI13Util.return400(response, "Missing tool");
				return;
			}

			if ( ! checkToolHasPlacements(sat.tool_id, signed_placement, response) ) return;

		}

		String context_id = site.getId();
		Assignment a = null;

		if ( lineitem_key != null ) {
			a = LineItemUtil.getColumnByKeyDAO(context_id, sat.tool_id, lineitem_key);
		} else if ( content != null ) {
			String assignment_label = (String) content.title;
			a = LineItemUtil.getColumnByLabelDAO(context_id, sat.tool_id, assignment_label);
		}

		if ( a == null ) {
			LTI13Util.return404(response, "Could not load column");
			log.error("Could not load column={}", lineitem_key);
			return;
		}

		// Return the line item metadata
		if ( ! results ) {
			SakaiLineItem item = LineItemUtil.getLineItem(signed_placement, a);

			String json_out = JacksonUtil.prettyPrint(item);
			log.debug("Returning {}", json_out);

			response.setContentType(SakaiLineItem.CONTENT_TYPE);
			PrintWriter out = response.getWriter();
			out.print(json_out);
			return;
		}

		resultsForAssignment(signed_placement, site, a, lineitem_key, user_id, request, response);

	}

	private void resultsForAssignment(String signed_placement, Site site, Assignment a,
			Long lineitem_key, String user_id, HttpServletRequest request, HttpServletResponse response)
	{
		log.debug("signed_placement={} user_id={}", signed_placement, user_id);
		// TODO: Is the outer container an array or an object - the spec and swagger doc disagree
		/*
			[{
			  "id": "https://lms.example.com/context/2923/lineitems/1/results/5323497",
			  "scoreOf": "https://lms.example.com/context/2923/lineitems/1",
			  "userId": "5323497",
			  "resultScore": 0.83,
			  "resultMaximum": 1,
			  "comment": "This is exceptional work."
			}]
		*/
		response.setContentType(Result.CONTENT_TYPE_CONTAINER);

		// Look up the assignment so we can find the max points
		GradingService gradingService = (GradingService) ComponentManager
				.get("org.sakaiproject.grading.api.GradingService");
		Session sess = SessionManager.getCurrentSession();

		// Indicate "who" is reading this grade - needs to be a real user account
		String gb_user_id = ServerConfigurationService.getString(
				"lti.outcomes.userid", "admin");
		String gb_user_eid = ServerConfigurationService.getString(
				"lti.outcomes.usereid", gb_user_id);
		sess.setUserId(gb_user_id);
		sess.setUserEid(gb_user_eid);

		String context_id = site.getId();

		SakaiLTIUtil.pushAdvisor();
		try {
			boolean success = false;

			List<Map<String, Object>> lm = new ArrayList<>();

			// Get users for each of the members. UserDirectoryService.getUsers will skip any undefined users.
			Map<String, Member> memberMap = new HashMap<>();
			List<String> userIds = new ArrayList<>();

			// TODO: Make this faster
			Set<Member> members = site.getMembers();
			for (Member member : members) {
				if ( user_id != null && ! user_id.equals(member.getUserId()) ) continue;
				userIds.add(member.getUserId());
				memberMap.put(member.getUserId(), member);
			}

			List<User> users = UserDirectoryService.getUsers(userIds);
			boolean first = true;

			response.setContentType(APPLICATION_JSON);
			PrintWriter out = response.getWriter();

			if ( user_id == null ) out.println("[");
			for (User user : users) {
				Result result = new Result();
                                String lti11_legacy_user_id = user.getId();
                                String subject = SakaiLTIUtil.getSubject(lti11_legacy_user_id, context_id);
				result.userId = subject;
				result.resultMaximum = a.getPoints();

				if ( signed_placement != null ) {
					if ( lineitem_key != null ) {
						result.id = getOurServerUrl() + LTI13_PATH + "lineitems/" + signed_placement + "/" + lineitem_key + "/results/" + user.getId();
						result.scoreOf = getOurServerUrl() + LTI13_PATH + "lineitems/" + signed_placement + "/" + lineitem_key;
					}  else {
						result.id = getOurServerUrl() + LTI13_PATH + "lineitem/" + signed_placement + "/results/" + user.getId();
						result.scoreOf = getOurServerUrl() + LTI13_PATH + "lineitem/" + signed_placement;
					}
				}

				try {
					CommentDefinition commentDef = gradingService.getAssignmentScoreComment(context_id, a.getId(), user.getId());
					if (commentDef != null) {
						result.comment = commentDef.getCommentText();
					}
				} catch(AssessmentNotFoundException e) {
					log.error(e.getMessage(), e);  // Unexpected
					break;
				}

				String actualGrade = null;
				result.resultScore = null;
				try {
					actualGrade = gradingService.getAssignmentScoreString(context_id, context_id, a.getId(), user.getId());
				} catch(AssessmentNotFoundException e) {
					log.error(e.getMessage(), e);  // Unexpected
					break;
				}

				if ( actualGrade != null ) {
					try {
						Double dGrade = Double.valueOf(actualGrade);
						result.resultScore = dGrade;
					} catch(NumberFormatException e) {
						log.error("Could not parse grade="+actualGrade);
						result.resultScore = null;
					}
				}

				if (!first) {
					out.println(",");
				}
				first = false;
				String json_out = JacksonUtil.prettyPrint(result);
				out.print(json_out);

			}
			if ( user_id == null ) {
				out.println("]");
			}
		} catch (Throwable t) {
			log.error(t.getMessage(), t);
		} finally {
			SakaiLTIUtil.popAdvisor();
		}
	}

	/**
	 *  Delete a tool created lineitem
	 * @param signed_placement
	 * @param lineItem
	 * @param results
	 * @param request
	 * @param response
	 */
	private void handleLineItemsDelete(String signed_placement, String lineItem, HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.debug("signed_placement={}", signed_placement);

		// Make sure the lineItem id is a long
		Long lineitem_key;
		try {
			lineitem_key = Long.parseLong(lineItem);
		} catch (NumberFormatException e) {
			LTI13Util.return400(response, "Bad value for lineitem_key "+lineItem);
			log.error("Bad value for lineitem_key "+lineItem);
			return;
		}

		// Load the access token, checking the the secret
		SakaiAccessToken sat = getSakaiAccessToken(tokenKeyPair.getPublic(), request, response);
		if (sat == null) {
			return;
		}

		if (! sat.hasScope(SakaiAccessToken.SCOPE_LINEITEMS) ) {
			LTI13Util.return400(response, "Scope lineitems not in access token");
			log.error("Scope lineitems not in access token");
			return;
		}

		Site site = null;
		org.sakaiproject.lti.beans.LtiToolBean tool = null;

		// SAK-47261 - Legacy URL patterns with actual signed placement
		if ( isSignedPlacement(signed_placement) ) {
			org.sakaiproject.lti.beans.LtiContentBean content = loadContentCheckSignature(signed_placement, response);
			if (content == null) {
				LTI13Util.return400(response, "Could not load content from signed placement");
				log.error("Could not load content from signed placement = {}", signed_placement);
				return;
			}

			site = loadSiteFromContent(content, signed_placement, response);
			if (site == null) {
				LTI13Util.return400(response, "Could not load site associated with content");
				log.error("Could not load site associated with content={}", content.id);
				return;
			}

			tool = loadToolForContent(content, site, sat.tool_id, response);
			if (tool == null) {
				log.error("Could not load tool={} associated with content={}", sat.tool_id, content.id);
				LTI13Util.return400(response, "Could not load tool associated with content");
				return;
			}
		} else { // SAK-47261 - It is just a site_id
			try {
				site = SiteService.getSite(signed_placement);
			} catch (IdUnusedException e) {
				log.error("No site/page associated with content siteId={}", signed_placement);
				LTI13Util.return400(response, "Could not load site associated with content");
				return;
			}

			tool = ltiService.getToolDaoAsBean(sat.tool_id, site.getId(), true);
			if (tool == null) {
				log.error("Could not load tool={}", sat.tool_id);
				LTI13Util.return400(response, "Missing tool");
				return;
			}

			if ( ! checkToolHasPlacements(sat.tool_id, signed_placement, response) ) return;

		}

		String context_id = site.getId();
		if ( LineItemUtil.deleteAssignmentByKeyDAO(context_id, sat.tool_id, lineitem_key) ) {
			return;
		}

		LTI13Util.return400(response, "Could not delete line item "+lineitem_key);
		log.error("Could delete line item={}", lineitem_key);
	}

}
