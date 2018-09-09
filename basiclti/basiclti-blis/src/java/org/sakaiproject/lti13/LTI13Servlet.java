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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Base64;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;
import static org.sakaiproject.basiclti.util.SakaiBLTIUtil.getLongKey;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.lti.api.LTIService;

import org.tsugi.http.HttpUtil;
import org.tsugi.basiclti.BasicLTIUtil;
import org.tsugi.jackson.JacksonUtil;
import org.tsugi.lti13.LTI13KeySetUtil;
import org.tsugi.lti13.LTI13Util;
import org.tsugi.lti13.LTI13JwtUtil;

import org.tsugi.oauth2.objects.AccessToken;

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

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if (ltiService == null) {
			ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
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

		HttpUtil.printHeaders(request);
		HttpUtil.printParameters(request);

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

		String grant_type = request.getParameter(AccessToken.GRANT_TYPE);
		String client_assertion = request.getParameter(AccessToken.CLIENT_ASSERTION);
		String scope = request.getParameter(AccessToken.SCOPE);
		String missing = "";
		if ( grant_type == null ) missing += " " + "grant_type";
		if ( client_assertion == null ) missing += " " + "client_assertion";
		if ( scope == null ) missing += " " + "scope";
		if ( missing.length() > 0 ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(ERROR_DETAIL, "Token request missing fields:"+missing);
			log.error("Token Request missing fields: {}", missing);
			return;
		}

		String body = LTI13JwtUtil.rawJwtBody(client_assertion);
System.out.println("body="+body);
		if ( body == null ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(ERROR_DETAIL, "Could not find Jwy Body in client_assertion");
			log.error("Could not find Jwy Body in client_assertion\n{}", client_assertion);
			return;
		}

		Object body_json = JSONValue.parse(body);
		System.out.println("body_json\n"+body_json);

		// Load the tool
		Map<String, Object> tool = loadTool(tool_id);
System.out.println("tool="+tool);
		if ( tool == null ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(ERROR_DETAIL, "Could not load tool");
			log.error("Could not load tool {}", tool_id);
			return;
		}
		log.error("Yada");
		String tool_public = (String) tool.get(LTIService.LTI13_TOOL_PUBLIC);
// System.out.println("tool_public="+tool_public);

		if ( tool_public == null ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(ERROR_DETAIL, "Could not find tool public key");
			log.error("Could not find tool public key {}", tool_id);
			return;
		}

		Key publicKey = LTI13Util.string2PublicKey(tool_public);
// System.out.println("publicKey="+publicKey);
		if ( publicKey == null ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(ERROR_DETAIL, "Could not deserialize tool public key");
			log.error("Could not deserialize tool public key {}", tool_id);
			return;
		}

		Jws<Claims> claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(client_assertion);
// System.out.println("claims="+claims);

		if ( claims == null ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(ERROR_DETAIL, "Could not verify signature");
			log.error("Could not verify signature {}", tool_id);
			return;
		}

		System.out.println("scope="+scope);

		// We are in good shape w.r.t the signature
		AccessToken at = new AccessToken();
		at.access_token = "42";

		response.setContentType(APPLICATION_JSON);
		String atsp = JacksonUtil.prettyPrintLog(at);

		try {
System.out.println("Returning Token");
			PrintWriter out = response.getWriter();
			out.println(atsp);
System.out.println(atsp);
			log.debug("Returning Token\n{}", atsp);
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error(e.getMessage(), e);
		}
	}

	protected void handleLineItemPost(String sourcedid, HttpServletRequest request, HttpServletResponse response) {
		System.out.println("Sourcedid="+sourcedid);

	}

	protected Map<String, Object> loadTool(String tool_id) {
		Map<String, Object> tool = null;

		Long toolKey = getLongKey(tool_id);
		if (toolKey < 1) {
			return null;
		}

		tool = ltiService.getToolDao(toolKey, null, true);
// System.out.println("tool_id="+tool_id);System.out.println(tool);
		return tool;
	}

	protected Map<String, Object> loadContent(String content_id) {
		Map<String, Object> content = null;

		Long contentKey = getLongKey(content_id);
		if (contentKey < 1) {
			return null;
		}

		// Leave off the siteId - bypass all checking - because we need to
		content = ltiService.getContentDao(contentKey);
System.out.println("content_id="+content_id);System.out.println(content);

		return content;
	}


}
