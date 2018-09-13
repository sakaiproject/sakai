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
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONValue;
import org.json.simple.JSONObject;

import org.apache.commons.io.IOUtils;

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

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.basiclti.util.LegacyShaUtil;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;
import static org.sakaiproject.basiclti.util.SakaiBLTIUtil.getInt;
import static org.sakaiproject.basiclti.util.SakaiBLTIUtil.getLongKey;
import static org.sakaiproject.basiclti.util.SakaiBLTIUtil.getRB;
import static org.sakaiproject.basiclti.util.SakaiBLTIUtil.postError;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lti.api.LTIService;

import org.tsugi.http.HttpUtil;
import org.tsugi.basiclti.BasicLTIUtil;
import org.tsugi.jackson.JacksonUtil;
import org.tsugi.lti13.LTI13KeySetUtil;
import org.tsugi.lti13.LTI13Util;
import org.tsugi.lti13.LTI13JwtUtil;

import org.tsugi.oauth2.objects.AccessToken;
import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;

/**
 *
 */
@SuppressWarnings("deprecation")
@Slf4j
public class LTI13Servlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String APPLICATION_JSON = "application/json; charset=utf-8";
	private static final String ERROR_DETAIL = "X-Sakai-LTI13-Error-Detail";
	protected static LTIService ltiService = null;

	// Used for signing and checking tokens
	// TODO: Rotate these after a while
	private KeyPair tokenKeyPair = null;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if (ltiService == null) {
			ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
		}
		if (tokenKeyPair == null) {
			try {
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
				keyGen.initialize(2048);
				tokenKeyPair = keyGen.genKeyPair();
			} catch (NoSuchAlgorithmException ex) {
				Logger.getLogger(LTI13Servlet.class.getName()).log(Level.SEVERE, "Unable to generate tokenKeyPair", ex);
			}
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI(); // /imsblis/lti13/keys
		// String launch_url = request.getParameter("launch_url");

		String[] parts = uri.split("/");

		// /imsblis/lti13/keyset/42
		if (parts.length == 5 && "keyset".equals(parts[3])) {
			String client_id = parts[4];
			handleKeySet(client_id, request, response);
			return;
		}

		log.error("Unrecognized GET request parts={} request={}", parts.length, uri);

		response.setHeader(ERROR_DETAIL, "Invalid request");
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String uri = request.getRequestURI(); // /imsblis/lti13/keys
		// String launch_url = request.getParameter("launch_url");

		String[] parts = uri.split("/");

		// /imsblis/lti13/token/42
		if (parts.length == 5 && "token".equals(parts[3])) {
			String client_id = parts[4];
			handleTokenPost(client_id, request, response);
			return;
		}

		// /imsblis/lti13/lineitem/42
		if (parts.length == 6 && "lineitem".equals(parts[3]) && "scores".equals(parts[5])) {
			String sourcedid = parts[4];
			handleLineItemPost(sourcedid, request, response);
			return;
		}

		log.error("Unrecognized POST request parts={} request={}", parts.length, uri);

		response.setHeader(ERROR_DETAIL, "Invalid request");
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	protected void handleKeySet(String tool_id, HttpServletRequest request, HttpServletResponse response) {
		PrintWriter out = null;
		Long toolKey = SakaiBLTIUtil.getLongKey(tool_id);
		String siteId = null;  // Full bypass mode
		Map<String, Object> tool = null;
		if (toolKey >= 0) {
			tool = ltiService.getToolDao(toolKey, siteId);
		}

		if (tool == null) {
			response.setHeader(ERROR_DETAIL, "Could not load keyset for client");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error("Could not load keyset for client_id={}", tool_id);
			return;
		}

		String publicSerialized = BasicLTIUtil.toNull((String) tool.get(LTIService.LTI13_PLATFORM_PUBLIC));
		if (publicSerialized == null) {
			response.setHeader(ERROR_DETAIL, "Client has no public key");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error("Client_id={} has no public key", tool_id);
			return;
		}

		Key publicKey = LTI13Util.string2PublicKey(publicSerialized);
		if (publicKey == null) {
			response.setHeader(ERROR_DETAIL, "Client public key deserialization error");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error("Client_id={} deserialization error", tool_id);
			return;
		}

		// Cast should work :)
		RSAPublicKey rsaPublic = (RSAPublicKey) publicKey;

		String keySetJSON = null;
		try {
			keySetJSON = LTI13KeySetUtil.getKeySetJSON(rsaPublic);
		} catch (NoSuchAlgorithmException ex) {
			response.setHeader(ERROR_DETAIL, "NoSuchAlgorithmException");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error("Client_id={} NoSuchAlgorithmException", tool_id);
			return;
		}

		try {
			out = response.getWriter();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		response.setContentType(APPLICATION_JSON);
		try {
			out.println(keySetJSON);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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

		String grant_type = request.getParameter(AccessToken.GRANT_TYPE);
		String client_assertion = request.getParameter(AccessToken.CLIENT_ASSERTION);
		String scope = request.getParameter(AccessToken.SCOPE);
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
			log.error("Could not find Jwy Body in client_assertion\n{}", client_assertion);
			return;
		}
		
		Long toolKey = getLongKey(tool_id);
		if (toolKey < 1) {
			LTI13Util.return400(response, "Invalid tool key");
			log.error("Invalis tool key {}", tool_id);
			return;
		}

		// Load the tool
		Map<String, Object> tool = ltiService.getToolDao(toolKey, null, true);
		if (tool == null) {
			LTI13Util.return400(response, "Could not load tool");
			log.error("Could not load tool {}", tool_id);
			return;
		}

		String tool_public = (String) tool.get(LTIService.LTI13_TOOL_PUBLIC);
		if (tool_public == null) {
			LTI13Util.return400(response, "Could not find tool public key");
			log.error("Could not find tool public key {}", tool_id);
			return;
		}

		Key publicKey = LTI13Util.string2PublicKey(tool_public);
		if (publicKey == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			LTI13Util.return400(response, "Could not deserialize tool public key");
			log.error("Could not deserialize tool public key {}", tool_id);
			return;
		}

		Jws<Claims> claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(client_assertion);

		if (claims == null) {
			LTI13Util.return400(response, "Could not verify signature");
			log.error("Could not verify signature {}", tool_id);
			return;
		}

		scope = scope.toLowerCase();

		int allowOutcomes = getInt(tool.get(LTIService.LTI_ALLOWOUTCOMES));
		int allowRoster = getInt(tool.get(LTIService.LTI_ALLOWROSTER));
		int allowSettings = getInt(tool.get(LTIService.LTI_ALLOWSETTINGS));

		SakaiAccessToken sat = new SakaiAccessToken();
		sat.tool_id = toolKey;
		Long issued = new Long(System.currentTimeMillis() / 1000L);
		sat.expires = issued + 3600L;

		// Work through requested scopes
		if (scope.contains(AccessToken.SCOPE_LINEITEM)) {
			if (allowOutcomes != 1) {
				// 400 with "invalid_scope" as the reason, i assume
				LTI13Util.return400(response, "invalid_scope", AccessToken.SCOPE_LINEITEM);
				log.error("Scope lineitem not allowed {}", tool_id);
				return;
			}
			sat.addScope(SakaiAccessToken.SCOPE_BASICOUTCOME);
		}

		if (scope.contains(AccessToken.SCOPE_SCORE)) {
			if (allowOutcomes != 1) {
				LTI13Util.return400(response, "invalid_scope", AccessToken.SCOPE_SCORE);
				log.error("Scope lineitem not allowed {}", tool_id);
				return;
			}
			sat.addScope(SakaiAccessToken.SCOPE_BASICOUTCOME);
		}

		if (scope.contains(AccessToken.SCOPE_RESULT_READONLY)) {
			if (allowOutcomes != 1) {
				LTI13Util.return400(response, "invalid_scope", AccessToken.SCOPE_RESULT_READONLY);
				log.error("Scope lineitem not allowed {}", tool_id);
				return;
			}
			sat.addScope(SakaiAccessToken.SCOPE_BASICOUTCOME);
		}
		String payload = JacksonUtil.toString(sat);
		String jws = Jwts.builder().setPayload(payload).signWith(tokenKeyPair.getPrivate()).compact();

		AccessToken at = new AccessToken();
		at.access_token = jws;

		response.setContentType(APPLICATION_JSON);
		String atsp = JacksonUtil.prettyPrintLog(at);

		try {
			PrintWriter out = response.getWriter();
			out.println(atsp);
			log.debug("Returning Token\n{}", atsp);
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error(e.getMessage(), e);
		}
	}

	protected void handleLineItemPost(String sourcedid, HttpServletRequest request, HttpServletResponse response) {
		HttpUtil.printHeaders(request);
		HttpUtil.printParameters(request);

		System.out.println("Sourcedid=" + sourcedid);

		// Load the access token, checking the the secret
		SakaiAccessToken sat = getSakaiAccessToken(tokenKeyPair.getPublic(), request, response);
		if ( sat == null ) return;

		System.out.println("sat=" + sat);
		
		String jsonString;
		try {
			// https://stackoverflow.com/questions/1548782/retrieving-json-object-literal-from-httpservletrequest
			jsonString = IOUtils.toString(request.getInputStream());
		} catch (IOException ex) {
			log.error("Could not read POST Data {}", ex.getMessage());
			LTI13Util.return400(response, "Could not read POST Data");
			return ;
		}
		System.out.println("jsonString="+jsonString);
		
		Object js = JSONValue.parse(jsonString);
		if ( js == null || ! (js instanceof JSONObject) ) {
			LTI13Util.return400(response, "Badly formatted JSON");
			return ;
		}
		System.out.println("js="+js);
		JSONObject jso = (JSONObject) js;
		
		Long scoreGivenStr = SakaiBLTIUtil.getLongNull(jso.get("scoreGiven"));
		Long scoreMaximumStr = SakaiBLTIUtil.getLongNull(jso.get("scoreMaximum"));
		String userId = (String) jso.get("userId");
		String comment = (String) jso.get("comment");
		log.debug("scoreGivenStr={} scoreMaximumStr={} userId={} comment={}", scoreGivenStr, scoreMaximumStr, userId, comment);

		if ( scoreGivenStr == null || userId == null ) {
			LTI13Util.return400(response, "Missing scoreGiven or userId");
			return ;
		}

		// Sanity check sourcedid
		// f093de4f8b98530abb0e7784c380ab1668510a7308cc454c79d4e7a0334ab268:::92e7ddf2-1c60-486c-97ae-bc2ffbde8e67:::content:6
		/* 
			String suffix =  ":::" + context_id + ":::" + resource_link_id;
			String base_string = placementSecret + suffix;
			String signature = LegacyShaUtil.sha256Hash(base_string);
			return signature + suffix;
		*/
		String[] parts = sourcedid.split(":::");
		if (parts.length != 3 || parts[0].length() < 1 || parts[0].length() > 10000 ||
			parts[1].length() < 1 || parts[2].length() <= 8 || 
			! parts[2].startsWith("content:")) {
			log.error("Bad sourcedid format {}",sourcedid);
			LTI13Util.return400(response, "bad sourcedid");
			return;
		}

		String received_signature = parts[0];
		String context_id = parts[1];
		String placement_id = parts[2];
		log.debug("signature={} context_id={} placement_id={}", received_signature, context_id, placement_id);
		
		String contentIdStr = placement_id.substring(8);
		Long contentKey = getLongKey(contentIdStr);
		if (contentKey < 0) {
			log.error("Bad placement format {}",sourcedid);
			LTI13Util.return400(response, "bad placement");
			return;
		}

		// Note that all of the above checking requires no database access :)
		// Now we have a valid access token and valid JSON, proceed with validating the sourcedid
		
		Map<String, Object> content = ltiService.getContentDao(contentKey);
		System.out.println("Content="+content);
		if ( content == null ) {
			log.error("Could not load Content Item {}",contentKey);
			LTI13Util.return400(response, "Could not load Content Item");
			return;
		}
		
		String placementSecret = (String) content.get(LTIService.LTI_PLACEMENTSECRET);
		if (placementSecret == null) {
			log.error("Could not load placementsecret {}",contentKey);
			LTI13Util.return400(response, "Could not load placementsecret");
			return;
		}

		// Validate the sourcedid signature before proceeding
		String suffix =  ":::" + context_id + ":::" + placement_id;
		String base_string = placementSecret + suffix;
		String signature = LegacyShaUtil.sha256Hash(base_string);
		if ( signature == null || ! signature.equals(received_signature)) {
			log.error("Could not verify sourcedid {}",sourcedid);
			LTI13Util.return400(response, "Could not verify sourcedid");
			return;
		}
		
		// Goog sourcedid, lets load the site and tool
		String siteId = (String) content.get(LTIService.LTI_SITE_ID);
		if (siteId == null) {
			log.error("Could not find site content={}",contentKey);
			LTI13Util.return400(response, "Could not find site for content");
			return;
		}
		
		Site site;
		try {
			site = SiteService.getSite(siteId);
		} catch (IdUnusedException e) {
			log.error("No site/page associated with content siteId={}", siteId);
			LTI13Util.return400(response, "Could not load site associated with content");
		}
		
		Long toolKey = getLongKey(content.get(LTIService.LTI_TOOL_ID));
		// System.out.println("toolKey="+toolKey+" sat.tool_id="+sat.tool_id);
		if (toolKey < 0 || !toolKey.equals(sat.tool_id)) {
			log.error("Content / Tool invalid content={} tool={}",contentKey, toolKey);
			LTI13Util.return400(response, "Content / Tool mismatch");
			return;
		}
		
		Map <String, Object> tool = ltiService.getToolDao(toolKey, siteId);
		if (tool == null) {
			log.error("Could not load tool={}",contentKey, toolKey);
			LTI13Util.return400(response, "Missing tool");
			return;
		}
		// System.out.println("tool="+tool);
		
		// We have validated everything and it is time to set the grade.
		
	}

	protected SakaiAccessToken getSakaiAccessToken(Key publicKey, HttpServletRequest request, HttpServletResponse response) {
		String authorization = request.getHeader("authorization");

		if (authorization == null || !authorization.startsWith("Bearer")) {
			LTI13Util.return400(response, "invalid_authorization");
			return null;
		}

		// https://stackoverflow.com/questions/7899525/how-to-split-a-string-by-space/7899558
		String[] parts = authorization.split("\\s+");
		if (parts.length != 2 || parts[1].length() < 1) {
			log.error("Bad authorization {}",authorization);
			LTI13Util.return400(response, "invalid_authorization");
			return null;
		}

		String jws = parts[1];
		Claims claims;
		try {
			claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jws).getBody();
		} catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException
				| io.jsonwebtoken.security.SignatureException | IllegalArgumentException e) {
			log.error("Signature error {}\n{}",e.getMessage(), jws);
			LTI13Util.return400(response, "signature_error");
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
			LTI13Util.return400(response, "token_parse_failure", ex.getMessage());
			return null;
		}

		// Validity check the access token
		if ( sat.tool_id != null && sat.scope != null && sat.expires != null ) {
			// All good
		} else {
			log.error("SakaiAccessToken missing required data {}",sat);
			LTI13Util.return400(response, "Missing required data in access_token");
			return null;
		}

		return sat;
	}

}
