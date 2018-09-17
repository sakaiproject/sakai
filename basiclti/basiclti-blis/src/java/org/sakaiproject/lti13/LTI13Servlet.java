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

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.basiclti.util.LegacyShaUtil;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;
import static org.sakaiproject.basiclti.util.SakaiBLTIUtil.getInt;
import static org.sakaiproject.basiclti.util.SakaiBLTIUtil.getLongKey;
import static org.sakaiproject.basiclti.util.SakaiBLTIUtil.getRB;
import static org.sakaiproject.basiclti.util.SakaiBLTIUtil.postError;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
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
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.tsugi.basiclti.XMLMap;

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

		// /imsblis/lti13/namesandroles/42
		if (parts.length == 5 && "namesandroles".equals(parts[3])) {
			String signed_placement = parts[4];
			handleNamesAndRoles(signed_placement, request, response);
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
			String signed_placement = parts[4];
			handleLineItemPost(signed_placement, request, response);
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

		if (scope.contains(AccessToken.SCOPE_NAMES_AND_ROLES)) {
			if (allowOutcomes != 1) {
				LTI13Util.return400(response, "invalid_scope", AccessToken.SCOPE_RESULT_READONLY);
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

	protected void handleLineItemPost(String signed_placement, HttpServletRequest request, HttpServletResponse response) {

		// Load the access token, checking the the secret
		SakaiAccessToken sat = getSakaiAccessToken(tokenKeyPair.getPublic(), request, response);
		log.debug("sat={}", sat);

		if (sat == null) {
			return;
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
		String userId = (String) jso.get("userId");
		String comment = (String) jso.get("comment");
		log.debug("scoreGivenStr={} scoreMaximumStr={} userId={} comment={}", scoreGiven, scoreMaximum, userId, comment);

		if (scoreGiven == null || userId == null) {
			LTI13Util.return400(response, "Missing scoreGiven or userId");
			return;
		}

		Map<String, Object> content = loadContent(signed_placement, response);
		if (content == null) {
			return;
		}

		String assignment = (String) content.get(LTIService.LTI_TITLE);
		if (assignment == null || assignment.length() < 1) {
			log.error("Could not determine assignment title {}", content.get(LTIService.LTI_ID));
			LTI13Util.return400(response, "Could not determine assignment title");
			return;
		}

		Site site = loadSiteFromContent(content, signed_placement, response);
		if (site == null) {
			return;
		}

		if (!checkUserInSite(site, userId)) {
			log.warn("User {} not found in siteId={}", userId, site.getId());
			LTI13Util.return400(response, "User does not belong to site");
			return;
		}

		Map<String, Object> tool = loadToolForContent(content, site, sat.tool_id, response);
		if (tool == null) {
			return;
		}

		Object retval = SakaiBLTIUtil.setGradeLTI13(site, userId, assignment, scoreGiven, scoreMaximum, comment);
		System.out.println("Lineitem retval=" + retval);
	}

	/*
	{
  "id" : "https://lms.example.com/sections/2923/memberships/?rlid=49566-rkk96",
  "members" : [
    {
      "status" : "Active",
      "context_id": "2923-abc",
      "context_label": "CPS 435",
      "context_title": "CPS 435 Learning Analytics",
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
			return;
		}

		if (!sat.hasScope(SakaiAccessToken.SCOPE_ROSTER)) {
			LTI13Util.return400(response, "Scope roster not in access token");
			log.error("Scope roster not in access token");
			return;
		}

		Map<String, Object> content = loadContent(signed_placement, response);
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

		int releaseName = getInt(tool.get(LTIService.LTI_SENDNAME));
		int releaseEmail = getInt(tool.get(LTIService.LTI_SENDEMAILADDR));
		// int allowOutcomes = getInt(tool.get(LTIService.LTI_ALLOWOUTCOMES));

		String assignment = (String) content.get(LTIService.LTI_TITLE);
		if (assignment == null || assignment.length() < 1) {
			assignment = null;
		}

		System.out.println("Here we are " + assignment);
		String maintainRole = site.getMaintainRole();

		PrintWriter out = response.getWriter();
		out.println("{");
		out.println(" \"id\" : \"http://TODO.wtf.com/\",");
		out.println(" \"members\": [");

		SakaiBLTIUtil.pushAdvisor();
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
		for (User user : users) {
			JSONObject jo = new JSONObject();
			jo.put("status", "Active");
			jo.put("context_id", site.getId());
			jo.put("context_title", site.getTitle());
			jo.put("user_id", user.getId());
			if (releaseName != 0) {
				jo.put("name", user.getDisplayName());
			}
			if (releaseEmail != 0) {
				jo.put("email", user.getEmail());
			}

			Member member = memberMap.get(user.getId());
			Map<String, Object> mm = new TreeMap<>();
			// Role role = member.getRole();
			String ims_user_id = member.getUserId();
			JSONArray roles = new JSONArray();
			if (ComponentManager.get(AuthzGroupService.class).isAllowed(ims_user_id, SiteService.SECURE_UPDATE_SITE, "/site/" + site.getId())) {
				roles.add("Instructor");
			} else {
				roles.add("Learner");
			}
			jo.put("roles", roles);
			/*
			if ( "true".equals(allowOutcomes) && assignment != null ) {
				String placement_secret  = pitch.getProperty(LTIService.LTI_PLACEMENTSECRET);
				String result_sourcedid = SakaiBLTIUtil.getSourceDID(user, placement_id, placement_secret);
				if ( result_sourcedid != null ) mm.put("/lis_result_sourcedid",result_sourcedid);
			}

			Collection groups = site.getGroupsWithMember(ims_user_id);

			if (groups.size() > 0) {
				List<Map<String, Object>> lgm = new ArrayList<Map<String, Object>>();
				for (Iterator i = groups.iterator();i.hasNext();) {
					Group group = (Group) i.next();
					Map<String, Object> groupMap = new HashMap<String, Object>();
					groupMap.put("/id", group.getId());
					groupMap.put("/title", group.getTitle());
					groupMap.put("/set", new HashMap(groupMap));
					lgm.add(groupMap);
				}
				mm.put("/groups/group", lgm);
			}

			lm.add(mm);
			 */

			if (!first) {
				out.println(",");
			}
			first = false;
			out.print(JacksonUtil.prettyPrint(jo));

		}
		out.println("");
		out.println(" ] }");

		SakaiBLTIUtil.popAdvisor();

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

	protected static Map<String, Object> loadContent(String signed_placement, HttpServletResponse response) {

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
		System.out.println("Content=" + content);
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

}
