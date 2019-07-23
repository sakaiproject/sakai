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
import org.sakaiproject.basiclti.util.LegacyShaUtil;
import static org.sakaiproject.basiclti.util.SakaiBLTIUtil.getInt;
import static org.sakaiproject.basiclti.util.SakaiBLTIUtil.getLongKey;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;
import static org.sakaiproject.basiclti.util.SakaiBLTIUtil.LTI13_PATH;
import static org.sakaiproject.basiclti.util.SakaiBLTIUtil.getOurServerUrl;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lti.api.LTIService;
import static org.sakaiproject.lti13.LineItemUtil.getLineItem;

import org.tsugi.basiclti.BasicLTIUtil;
import org.tsugi.jackson.JacksonUtil;
import org.tsugi.lti13.LTI13KeySetUtil;
import org.tsugi.lti13.LTI13Util;
import org.tsugi.lti13.LTI13JwtUtil;

import org.tsugi.oauth2.objects.AccessToken;
import org.tsugi.lti13.objects.Endpoint;

import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CommentDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.tsugi.ags2.objects.LineItem;
import org.tsugi.ags2.objects.Result;
import org.tsugi.lti13.objects.LaunchLIS;

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

		LineItem filter = getLineItemFilter(request);

		// Get a keys for a client_id
		// /imsblis/lti13/keyset/{tool-id}
		if (parts.length == 5 && "keyset".equals(parts[3])) {
			String client_id = parts[4];
			handleKeySet(client_id, request, response);
			return;
		}

		// Get the membership list for a placement
		// /imsblis/lti13/namesandroles/context:6
		if (parts.length == 5 && "namesandroles".equals(parts[3])) {
			String signed_placement = parts[4];
			handleNamesAndRoles(signed_placement, request, response);
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


		// Handle lineitems created by the tool
		// /imsblis/lti13/lineitems/{signed-placement}/{lineitem-id}
		if (parts.length == 6 && "lineitems".equals(parts[3])) {
			String signed_placement = parts[4];
			String lineItem = parts[5];
			handleLineItemsUpdate(signed_placement, lineItem, request, response);
			return;
		}

		log.error("Unrecognized DELETE request parts={} request={}", parts.length, uri);
		LTI13Util.return400(response, "Unrecognized DELETE request parts="+parts.length+" request="+uri);
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

		log.error("Unrecognized POST request parts={} request={}", parts.length, uri);
		LTI13Util.return400(response, "Unrecognized POST request parts="+parts.length+" request="+uri);

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
			log.error("Could not find Jwt Body in client_assertion\n{}", client_assertion);
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

		Jws<Claims> claims = Jwts.parser().setAllowedClockSkewSeconds(60).setSigningKey(publicKey).parseClaimsJws(client_assertion);

		if (claims == null) {
			LTI13Util.return400(response, "Could not verify signature");
			log.error("Could not verify signature {}", tool_id);
			return;
		}

		scope = scope.toLowerCase();

		int allowOutcomes = getInt(tool.get(LTIService.LTI_ALLOWOUTCOMES));
		int allowRoster = getInt(tool.get(LTIService.LTI_ALLOWROSTER));
		int allowSettings = getInt(tool.get(LTIService.LTI_ALLOWSETTINGS));
		int allowLineItems = getInt(tool.get(LTIService.LTI_ALLOWLINEITEMS));

		SakaiAccessToken sat = new SakaiAccessToken();
		sat.tool_id = toolKey;
		Long issued = new Long(System.currentTimeMillis() / 1000L);
		sat.expires = issued + 3600L;

		// Work through requested scopes
		if (scope.contains(Endpoint.SCOPE_LINEITEM_READONLY)) {
			if (allowLineItems != 1) {
				LTI13Util.return400(response, "invalid_scope", Endpoint.SCOPE_LINEITEM_READONLY);
				log.error("Scope lineitem not allowed {}", tool_id);
				return;
			}
			sat.addScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY);
		}

		if (scope.contains(Endpoint.SCOPE_LINEITEM)) {
			if (allowLineItems != 1) {
				LTI13Util.return400(response, "invalid_scope", Endpoint.SCOPE_LINEITEM);
				log.error("Scope lineitem not allowed {}", tool_id);
				return;
			}
			sat.addScope(SakaiAccessToken.SCOPE_LINEITEMS);
			sat.addScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY);
		}

		if (scope.contains(Endpoint.SCOPE_SCORE)) {
			if (allowOutcomes != 1 || allowLineItems != 1) {
				LTI13Util.return400(response, "invalid_scope", Endpoint.SCOPE_SCORE);
				log.error("Scope lineitem not allowed {}", tool_id);
				return;
			}
			sat.addScope(SakaiAccessToken.SCOPE_BASICOUTCOME);
		}

		if (scope.contains(Endpoint.SCOPE_RESULT_READONLY)) {
			if (allowOutcomes != 1 || allowLineItems != 1) {
				LTI13Util.return400(response, "invalid_scope", Endpoint.SCOPE_RESULT_READONLY);
				log.error("Scope lineitem not allowed {}", tool_id);
				return;
			}
			sat.addScope(SakaiAccessToken.SCOPE_BASICOUTCOME);
		}

		if (scope.contains(LaunchLIS.SCOPE_NAMES_AND_ROLES)) {
			if (allowOutcomes != 1) {
				LTI13Util.return400(response, "invalid_scope", LaunchLIS.SCOPE_NAMES_AND_ROLES);
				log.error("Scope lineitem not allowed {}", tool_id);
				return;
			}
			sat.addScope(SakaiAccessToken.SCOPE_ROSTER);
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

	protected void handleLineItemScore(String signed_placement, String lineItem, HttpServletRequest request, HttpServletResponse response) {

		// Make sure the lineItem id is a long
		Long assignment_id = null;
		if ( lineItem != null ) {
			try {
				assignment_id = Long.parseLong(lineItem);
			} catch (NumberFormatException e) {
				LTI13Util.return400(response, "Bad value for assignment_id "+lineItem);
				log.error("Bad value for assignment_id "+lineItem);
				return;
			}
		}
		// Load the access token, checking the the secret
		SakaiAccessToken sat = getSakaiAccessToken(tokenKeyPair.getPublic(), request, response);
		log.debug("sat={}", sat);

		if (sat == null) {
			return;  // Error already set
		}
		if (!sat.hasScope(SakaiAccessToken.SCOPE_BASICOUTCOME)) {
			LTI13Util.return400(response, "Scope basic outcome not in access token");
			log.error("Scope basic outcome not in access token");
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

		Object js = JSONValue.parse(jsonString);
		if (js == null || !(js instanceof JSONObject)) {
			LTI13Util.return400(response, "Badly formatted JSON");
			return;
		}
		JSONObject jso = (JSONObject) js;

		Long scoreGiven = SakaiBLTIUtil.getLongNull(jso.get("scoreGiven"));
		Long scoreMaximum = SakaiBLTIUtil.getLongNull(jso.get("scoreMaximum"));
		String userId = SakaiBLTIUtil.getStringNull(jso.get("userId"));  // TODO: LTI13 quirk - should be subject
		String comment = SakaiBLTIUtil.getStringNull(jso.get("comment"));
		log.debug("scoreGivenStr={} scoreMaximumStr={} userId={} comment={}", scoreGiven, scoreMaximum, userId, comment);

		if (scoreGiven == null || userId == null) {
			LTI13Util.return400(response, "Missing scoreGiven or userId");
			return;
		}

		Map<String, Object> content = loadContentCheckSignature(signed_placement, response);
		if (content == null) {
			return;
		}

		String assignment_name = (String) content.get(LTIService.LTI_TITLE);
		if (assignment_name == null || assignment_name.length() < 1) {
			log.error("Could not determine assignment_name title {}", content.get(LTIService.LTI_ID));
			LTI13Util.return400(response, "Could not determine assignment_name");
			return;
		}

		Site site = loadSiteFromContent(content, signed_placement, response);
		if (site == null) {
			return;
		}

		String context_id = site.getId();
		userId = SakaiBLTIUtil.parseSubject(userId);
		if (!checkUserInSite(site, userId)) {
			log.warn("User {} not found in siteId={}", userId, context_id);
			LTI13Util.return400(response, "User does not belong to site");
			return;
		}

		Map<String, Object> tool = loadToolForContent(content, site, sat.tool_id, response);
		if (tool == null) {
			return;
		}

		Object retval;
		if ( assignment_id == null ) {
			retval = SakaiBLTIUtil.setGradeLTI13(site, sat.tool_id, content, userId, assignment_name, scoreGiven, scoreMaximum, comment);
			log.debug("Lineitem retval={}",retval);
		} else {
			// TODO: Could make a new method collapsing these tool calls into a single scan
			Assignment assnObj = LineItemUtil.getAssignmentByKeyDAO(context_id, sat.tool_id, assignment_id);
			if ( assnObj == null || assnObj.getName() == null ) {
				LTI13Util.return400(response, "Unable to load assignment "+assignment_id);
				return;
			}
			assignment_name = assnObj.getName();
			retval = SakaiBLTIUtil.setGradeLTI13(site, sat.tool_id, content, userId, assignment_name, scoreGiven, scoreMaximum, comment);
			log.debug("Lineitem retval={}",retval);
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

		Map<String, Object> content = loadContentCheckSignature(signed_placement, response);
		if (content == null) {
			LTI13Util.return400(response, "Could not load content from signed placement");
			log.error("Could not load content from signed placement = {}", signed_placement);
			return;
		}

		Site site = loadSiteFromContent(content, signed_placement, response);
		if (site == null) {
			LTI13Util.return400(response, "Could not load site associated with content");
			log.error("Could not load site associated with content={}", content.get(LTIService.LTI_ID));
			return;
		}

		Map<String, Object> tool = loadToolForContent(content, site, sat.tool_id, response);
		if (tool == null) {
			log.error("Could not load tool={} associated with content={}", sat.tool_id, content.get(LTIService.LTI_ID));
			return;
		}

		int releaseName = getInt(tool.get(LTIService.LTI_SENDNAME));
		int releaseEmail = getInt(tool.get(LTIService.LTI_SENDEMAILADDR));
		// int allowOutcomes = getInt(tool.get(LTIService.LTI_ALLOWOUTCOMES));

		String assignment_name = (String) content.get(LTIService.LTI_TITLE);
		if (assignment_name == null || assignment_name.length() < 1) {
			assignment_name = null;
		}

		JSONObject context_obj = new JSONObject();
		context_obj.put("id", site.getId());
		context_obj.put("title", site.getTitle());

		String maintainRole = site.getMaintainRole();

		PrintWriter out = response.getWriter();
		out.println("{");
		out.println(" \"id\" : \"http://TODO.wtf.com/we_eliminated_json_ld_but_forgot_to_remove_this\",");
		out.println(" \"context\" : ");
		out.print(JacksonUtil.prettyPrint(context_obj));
		out.println(",");
		out.println(" \"members\": [");

		SakaiBLTIUtil.pushAdvisor();
		try {
			boolean success = false;

			List<Map<String, Object>> lm = new ArrayList<>();

			// Get users for each of the members. UserDirectoryService.getUsers will skip any undefined users.
			Set<Member> members = site.getMembers();
			Map<String, Member> memberMap = new HashMap<>();
			List<String> userIds = new ArrayList<>();
			for (Member member : members) {
				userIds.add(member.getUserId());
				memberMap.put(member.getUserId(), member);
			}

			List<User> users = UserDirectoryService.getUsers(userIds);
			boolean first = true;

			// TODO: Use LTISERVICE.LTI_ROLEMAP after SAK-40632 is completed and merged
			String roleMapProp = (String) tool.get("rolemap");
			roleMapProp = "maintain:Dude";

			Map<String, String> roleMap = SakaiBLTIUtil.convertRoleMapPropToMap(roleMapProp);
			for (User user : users) {
				JSONObject jo = new JSONObject();
				jo.put("status", "Active");
				String lti11_legacy_user_id = user.getId();
				jo.put("lti11_legacy_user_id", lti11_legacy_user_id);
				String subject = SakaiBLTIUtil.getSubject(lti11_legacy_user_id, site.getId());
				jo.put("user_id", subject);   // TODO: Should be subject - LTI13 Quirk
				jo.put("lis_person_sourcedid", user.getEid());

				if (releaseName != 0) {
					jo.put("name", user.getDisplayName());
				}
				if (releaseEmail != 0) {
					jo.put("email", user.getEmail());
				}

				Member member = memberMap.get(user.getId());
				Map<String, Object> mm = new TreeMap<>();
				Role role = member.getRole();
				String ims_user_id = member.getUserId();

				JSONArray roles = new JSONArray();

				// If there is a role mapping, it has precedence over site.update
				if ( roleMap.containsKey(role.getId()) ) {
					roles.add(roleMap.get(role.getId()));
				} else if (ComponentManager.get(AuthzGroupService.class).isAllowed(ims_user_id, SiteService.SECURE_UPDATE_SITE, "/site/" + site.getId())) {
					roles.add("Instructor");
				} else {
					roles.add("Learner");
				}
				jo.put("roles", roles);

				JSONObject sakai_ext = new JSONObject();
				if ( sat.hasScope(SakaiAccessToken.SCOPE_BASICOUTCOME)  && assignment_name != null ) {
					String placement_secret  = (String) content.get(LTIService.LTI_PLACEMENTSECRET);
					String placement_id = getPlacementId(signed_placement);
					String result_sourcedid = SakaiBLTIUtil.getSourceDID(user, placement_id, placement_secret);
					if ( result_sourcedid != null ) sakai_ext.put("lis_result_sourcedid",result_sourcedid);
				}

				Collection groups = site.getGroupsWithMember(ims_user_id);

				if (groups.size() > 0) {
					JSONArray lgm = new JSONArray();
					for (Iterator i = groups.iterator();i.hasNext();) {
						Group group = (Group) i.next();
						JSONObject groupObj = new JSONObject();
						groupObj.put("id", group.getId());
						groupObj.put("title", group.getTitle());
						lgm.add(groupObj);
					}
					sakai_ext.put("sakai_groups", lgm);
				}

				jo.put("sakai_ext", sakai_ext);

				if (!first) {
					out.println(",");
				}
				first = false;
				out.print(JacksonUtil.prettyPrint(jo));

			}
			out.println("");
			out.println(" ] }");
		} finally {
			SakaiBLTIUtil.popAdvisor();
		}

	}

	protected SakaiAccessToken getSakaiAccessToken(Key publicKey, HttpServletRequest request, HttpServletResponse response) {
		String authorization = request.getHeader("authorization");

		if (authorization == null || !authorization.startsWith("Bearer")) {
			log.error("Invalid authorization {}", authorization);
			LTI13Util.return400(response, "invalid_authorization");
			return null;
		}

		// https://stackoverflow.com/questions/7899525/how-to-split-a-string-by-space/7899558
		String[] parts = authorization.split("\\s+");
		if (parts.length != 2 || parts[1].length() < 1) {
			log.error("Bad authorization {}", authorization);
			LTI13Util.return400(response, "invalid_authorization");
			return null;
		}

		String jws = parts[1];
		Claims claims;
		try {
			claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jws).getBody();
		} catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException
				| io.jsonwebtoken.security.SignatureException | IllegalArgumentException e) {
			log.error("Signature error {}\n{}", e.getMessage(), jws);
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
		if (sat.tool_id != null && sat.scope != null && sat.expires != null) {
			// All good
		} else {
			log.error("SakaiAccessToken missing required data {}", sat);
			LTI13Util.return400(response, "Missing required data in access_token");
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
	protected static String[] splitSignedPlacement(String signed_placement, HttpServletResponse response) {
		String[] parts = signed_placement.split(":::");
		if (parts.length != 3 || parts[0].length() < 1 || parts[0].length() > 10000
				|| parts[1].length() < 1 || parts[2].length() <= 8
				|| !parts[2].startsWith("content:")) {
			log.error("Bad signed_placement format {}", signed_placement);
			LTI13Util.return400(response, "bad signed_placement");
			return null;
		}
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

	protected static Map<String, Object> loadContentCheckSignature(String signed_placement, HttpServletResponse response) {

		String[] parts = splitSignedPlacement(signed_placement, response);
		if (parts == null) {
			return null;
		}

		String received_signature = parts[0];
		String context_id = parts[1];
		String placement_id = parts[2];
		log.debug("signature={} context_id={} placement_id={}", received_signature, context_id, placement_id);

		String contentIdStr = placement_id.substring(8);
		Long contentKey = getLongKey(contentIdStr);
		if (contentKey < 0) {
			log.error("Bad placement format {}", signed_placement);
			LTI13Util.return400(response, "bad placement");
			return null;
		}

		// Note that all of the above checking requires no database access :)
		// Now we have a valid access token and valid JSON, proceed with validating the signed_placement
		Map<String, Object> content = ltiService.getContentDao(contentKey);
		if (content == null) {
			log.error("Could not load Content Item {}", contentKey);
			LTI13Util.return400(response, "Could not load Content Item");
			return null;
		}

		String placementSecret = (String) content.get(LTIService.LTI_PLACEMENTSECRET);
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

	protected Site loadSiteFromContent(Map<String, Object> content, String signed_placement, HttpServletResponse response) {
		String[] parts = splitSignedPlacement(signed_placement, response);
		if (parts == null) {
			return null;
		}

		String context_id = parts[1];

		// Good signed_placement, lets load the site and tool
		String siteId = (String) content.get(LTIService.LTI_SITE_ID);
		if (siteId == null) {
			log.error("Could not find site content={}", content.get(LTIService.LTI_ID));
			LTI13Util.return400(response, "Could not find site for content");
			return null;
		}

		if (!siteId.equals(context_id)) {
			log.error("Found incorrect site for content={}", content.get(LTIService.LTI_ID));
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

	protected Map<String, Object> loadToolForContent(Map<String, Object> content, Site site, Long expected_tool_id, HttpServletResponse response) {
		Long toolKey = getLongKey(content.get(LTIService.LTI_TOOL_ID));
		// System.out.println("toolKey="+toolKey+" sat.tool_id="+sat.tool_id);
		if (toolKey < 0 || !toolKey.equals(expected_tool_id)) {
			log.error("Content / Tool invalid content={} tool={}", content.get(LTIService.LTI_ID), toolKey);
			LTI13Util.return400(response, "Content / Tool mismatch");
			return null;
		}

		Map<String, Object> tool = ltiService.getToolDao(toolKey, site.getId());
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

	protected LineItem getLineItemFilter(HttpServletRequest request)
	{
		LineItem retval = new LineItem();
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

		LineItem item = (LineItem) getObjectFromPOST(request, response, LineItem.class);
		if ( item == null )  {
			return; // Error alredy handled
		}


		Map<String, Object> content = loadContentCheckSignature(signed_placement, response);
		if (content == null) {
			return;
		}

		Site site = loadSiteFromContent(content, signed_placement, response);
		if (site == null) {
			return;
		}

		Map<String, Object> tool = loadToolForContent(content, site, sat.tool_id, response);
		if (tool == null) {
			return;
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
		response.setContentType(LineItem.MIME_TYPE);

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
		Long assignment_id;
		try {
			assignment_id = Long.parseLong(lineItem);
		} catch (NumberFormatException e) {
			LTI13Util.return400(response, "Bad value for assignment_id "+lineItem);
			log.error("Bad value for assignment_id "+lineItem);
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

		LineItem item = (LineItem) getObjectFromPOST(request, response, LineItem.class);
		if ( item == null ) return; // Error alredy handled


		Map<String, Object> content = loadContentCheckSignature(signed_placement, response);
		if (content == null) {
			return;
		}

		Site site = loadSiteFromContent(content, signed_placement, response);
		if (site == null) {
			return;
		}

		Map<String, Object> tool = loadToolForContent(content, site, sat.tool_id, response);
		if (tool == null) {
			return;
		}

		Assignment retval;
		try {
			retval = LineItemUtil.updateLineItem(site, sat.tool_id, assignment_id, item);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			LTI13Util.return400(response, "Could not update lineitem: "+e.getMessage());
			return;
		}

		// TODO: Does PUT need to return the entire line item - I think the code below
		// actually is wrong - we just need to do a GET to get the entire line
		// item after the PUT.  It seems wasteful to always do the GET after PUT
		// when the tool can do it if it wants the newly updated item.  So
		// For now I am sending nothing back for a pUT request.
		// https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PUT

		/*
		// Add the link to this lineitem
		item.id = getOurServerUrl() + LTI13_PATH + "lineitems/" + signed_placement + "/" + retval.getId();

		log.debug("Lineitem item={}",item);
		response.setContentType(LineItem.MIME_TYPE);

		PrintWriter out = response.getWriter();
		out.print(JacksonUtil.prettyPrint(item));
		*/
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
	private void handleLineItemsGet(String signed_placement, boolean all, LineItem filter,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.debug("signed_placement={}", signed_placement);

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

		Map<String, Object> content = loadContentCheckSignature(signed_placement, response);
		if (content == null) {
			LTI13Util.return400(response, "Could not load content from signed placement");
			log.error("Could not load content from signed placement = {}", signed_placement);
			return;
		}

		Site site = loadSiteFromContent(content, signed_placement, response);
		if (site == null) {
			LTI13Util.return400(response, "Could not load site associated with content");
			log.error("Could not load site associated with content={}", content.get(LTIService.LTI_ID));
			return;
		}

		Map<String, Object> tool = loadToolForContent(content, site, sat.tool_id, response);
		if (tool == null) {
			log.error("Could not load tool={} associated with content={}", sat.tool_id, content.get(LTIService.LTI_ID));
			return;
		}

		// If we are only returning a single line item
		if ( ! all ) {
			response.setContentType(LineItem.MIME_TYPE);
			LineItem item = LineItemUtil.getLineItem(content);
			PrintWriter out = response.getWriter();
			out.print(JacksonUtil.prettyPrint(item));
			return;
		}

		// Return all the line items for the tool
		List<LineItem> preItems = LineItemUtil.getPreCreatedLineItems(site, sat.tool_id, filter);

		List<LineItem> toolItems = LineItemUtil.getLineItemsForTool(signed_placement, site, sat.tool_id, filter);

		response.setContentType(LineItem.MIME_TYPE_CONTAINER);

		PrintWriter out = response.getWriter();
		out.print("[");
		boolean first = true;
		for (LineItem item : preItems) {
			out.println(first ? "" : ",");
			first = false;
			out.print(JacksonUtil.prettyPrint(item));
		}

		for (LineItem item : toolItems) {
			out.println(first ? "" : ",");
			first = false;
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
		Long assignment_id = null;
		if ( lineItem != null ) {
			try {
				assignment_id = Long.parseLong(lineItem);
			} catch (NumberFormatException e) {
				LTI13Util.return400(response, "Bad value for assignment_id "+lineItem);
				log.error("Bad value for assignment_id "+lineItem);
				return;
			}
		}

		// Load the access token, checking the the secret
		SakaiAccessToken sat = getSakaiAccessToken(tokenKeyPair.getPublic(), request, response);
		if (sat == null) {
			return;
		}
/*
		if (! (sat.hasScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY) || sat.hasScope(SakaiAccessToken.SCOPE_LINEITEMS) )) {
			LTI13Util.return400(response, "Scope lineitems.readonly not in access token");
			log.error("Scope lineitems.readonly not in access token");
			return;
		}
*/
		Map<String, Object> content = loadContentCheckSignature(signed_placement, response);
		if (content == null) {
			LTI13Util.return400(response, "Could not load content from signed placement");
			log.error("Could not load content from signed placement = {}", signed_placement);
			return;
		}

		Site site = loadSiteFromContent(content, signed_placement, response);
		if (site == null) {
			LTI13Util.return400(response, "Could not load site associated with content");
			log.error("Could not load site associated with content={}", content.get(LTIService.LTI_ID));
			return;
		}
		String context_id = site.getId();

		Map<String, Object> tool = loadToolForContent(content, site, sat.tool_id, response);
		if (tool == null) {
			log.error("Could not load tool={} associated with content={}", sat.tool_id, content.get(LTIService.LTI_ID));
			return;
		}

		Assignment a;

		if ( assignment_id != null ) {
			a = LineItemUtil.getAssignmentByKeyDAO(context_id, sat.tool_id, assignment_id);
		} else {
			String assignment_label = (String) content.get(LTIService.LTI_TITLE);
			a = LineItemUtil.getAssignmentByLabelDAO(context_id, sat.tool_id, assignment_label);
		}

		if ( a == null ) {
			LTI13Util.return400(response, "Could not load assignment");
			log.error("Could not load assignment_id={}", assignment_id);
			return;
		}

		// Return the line item metadata
		if ( ! results ) {
			LineItem item = getLineItem(signed_placement, a);

			response.setContentType(LineItem.MIME_TYPE);
			String json_out = JacksonUtil.prettyPrint(item);
			log.debug("Returning {}", json_out);
			PrintWriter out = response.getWriter();
			out.print(json_out);
			return;
		}

		resultsForAssignment(signed_placement, site, a, assignment_id, user_id, request, response);

	}

	private void resultsForAssignment(String signed_placement, Site site, Assignment a,
			Long assignment_id, String user_id, HttpServletRequest request, HttpServletResponse response)
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
		response.setContentType(Result.MIME_TYPE_CONTAINER);

		// Look up the assignment so we can find the max points
		GradebookService g = (GradebookService) ComponentManager
				.get("org.sakaiproject.service.gradebook.GradebookService");
		Session sess = SessionManager.getCurrentSession();

		// Indicate "who" is reading this grade - needs to be a real user account
		String gb_user_id = ServerConfigurationService.getString(
				"basiclti.outcomes.userid", "admin");
		String gb_user_eid = ServerConfigurationService.getString(
				"basiclti.outcomes.usereid", gb_user_id);
		sess.setUserId(gb_user_id);
		sess.setUserEid(gb_user_eid);

		String context_id = site.getId();

		SakaiBLTIUtil.pushAdvisor();
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
			PrintWriter out = response.getWriter();

			if ( user_id == null ) out.println("[");
			for (User user : users) {
				Result result = new Result();
                                String lti11_legacy_user_id = user.getId();
                                String subject = SakaiBLTIUtil.getSubject(lti11_legacy_user_id, context_id);
				result.userId = subject;
				result.resultMaximum = a.getPoints();

				if ( signed_placement != null ) {
					if ( assignment_id != null ) {
						result.id = getOurServerUrl() + LTI13_PATH + "lineitems/" + signed_placement + "/" + assignment_id + "/results/" + user.getId();
						result.scoreOf = getOurServerUrl() + LTI13_PATH + "lineitems/" + signed_placement + "/" + assignment_id;
					}  else {
						result.id = getOurServerUrl() + LTI13_PATH + "lineitem/" + signed_placement + "/results/" + user.getId();
						result.scoreOf = getOurServerUrl() + LTI13_PATH + "lineitem/" + signed_placement;
					}
				}

				try {
					CommentDefinition commentDef = g.getAssignmentScoreComment(context_id, a.getId(), user.getId());
					if (commentDef != null) {
						result.comment = commentDef.getCommentText();
					}
				} catch(AssessmentNotFoundException | GradebookNotFoundException e) {
					log.error(e.getMessage(), e);  // Unexpected
					break;
				}

				String actualGrade = null;
				result.resultScore = null;
				try {
					actualGrade = g.getAssignmentScoreString(context_id, a.getId(), user.getId());
				} catch(AssessmentNotFoundException | GradebookNotFoundException e) {
					log.error(e.getMessage(), e);  // Unexpected
					break;
				}

				if ( actualGrade != null ) {
					try {
						Double dGrade = new Double(actualGrade);
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
			SakaiBLTIUtil.popAdvisor();
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
		Long assignment_id;
		try {
			assignment_id = Long.parseLong(lineItem);
		} catch (NumberFormatException e) {
			LTI13Util.return400(response, "Bad value for assignment_id "+lineItem);
			log.error("Bad value for assignment_id "+lineItem);
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

		Map<String, Object> content = loadContentCheckSignature(signed_placement, response);
		if (content == null) {
			LTI13Util.return400(response, "Could not load content from signed placement");
			log.error("Could not load content from signed placement = {}", signed_placement);
			return;
		}

		Site site = loadSiteFromContent(content, signed_placement, response);
		if (site == null) {
			LTI13Util.return400(response, "Could not load site associated with content");
			log.error("Could not load site associated with content={}", content.get(LTIService.LTI_ID));
			return;
		}

		Map<String, Object> tool = loadToolForContent(content, site, sat.tool_id, response);
		if (tool == null) {
			log.error("Could not load tool={} associated with content={}", sat.tool_id, content.get(LTIService.LTI_ID));
			return;
		}

		String context_id = site.getId();
		if ( LineItemUtil.deleteAssignmentByKeyDAO(context_id, sat.tool_id, assignment_id) ) {
			return;
		}

		LTI13Util.return400(response, "Could not delete assignment "+assignment_id);
		log.error("Could delete assignment={}", assignment_id);
	}

}
