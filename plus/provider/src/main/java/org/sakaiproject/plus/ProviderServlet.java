/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2009- The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *			 http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.plus;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;

import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.http.HttpResponse;  // Thanks Java 11
import java.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import java.security.Key;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import org.tsugi.lti.LTIConstants;
import org.tsugi.lti.LTIUtil;
import org.tsugi.lti13.LTI13JwtUtil;
import org.tsugi.lti13.LTI13KeySetUtil;
import org.tsugi.jackson.JacksonUtil;

import org.tsugi.http.HttpClientUtil;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.lti.api.LTIException;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.api.SiteEmailPreferenceSetter;
import org.sakaiproject.lti.api.UserFinderOrCreator;
import org.sakaiproject.lti.api.UserLocaleSetter;
import org.sakaiproject.lti.api.UserPictureSetter;
import org.sakaiproject.lti.api.SiteMembershipUpdater;
import org.sakaiproject.lti.api.SiteMembershipsSynchroniser;
import org.sakaiproject.lti.util.SakaiLTIUtil;
import org.sakaiproject.lti.util.SakaiKeySetUtil;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import static org.sakaiproject.site.api.SiteService.SITE_TITLE_MAX_LENGTH;
import org.sakaiproject.site.api.SiteService.SiteTitleValidationStatus;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.tsugi.lti13.objects.LaunchJWT;
import org.tsugi.lti13.objects.OpenIDProviderConfiguration;
import org.tsugi.lti13.objects.OpenIDClientRegistration;
import org.tsugi.lti13.objects.LTIToolConfiguration;
import org.tsugi.lti13.objects.LTILaunchMessage;
import org.sakaiproject.lti13.util.SakaiLaunchJWT;
import org.tsugi.lti13.LTI13Util;
import org.tsugi.lti13.LTI13ConstantsUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.client.utils.URIBuilder;

import org.sakaiproject.plus.api.PlusService;

import org.sakaiproject.plus.api.model.Tenant;

import org.sakaiproject.plus.api.repository.TenantRepository;
import org.sakaiproject.plus.api.repository.ContextRepository;

import org.sakaiproject.plus.api.Launch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

@SuppressWarnings("deprecation")
@Slf4j
public class ProviderServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static ResourceLoader rb = new ResourceLoader("plus");
	private static final String LTI_RESOURCE_LINK = "blti:resource_link_id";

	private static final String DEFAULT_PRIVACY_URL = "https://www.sakailms.com/plus-privacylaunch";

	@Autowired private ServerConfigurationService serverConfigurationService;
	@Autowired private SiteMembershipUpdater siteMembershipUpdater;
	@Autowired private SiteMembershipsSynchroniser siteMembershipsSynchroniser;
	@Autowired private SiteEmailPreferenceSetter siteEmailPreferenceSetter;
	@Autowired private UserFinderOrCreator userFinderOrCreator;
	@Autowired private UserLocaleSetter userLocaleSetter;
	@Autowired private UserPictureSetter userPictureSetter;
	@Autowired private PlusService plusService;
	@Autowired private TenantRepository tenantRepository;
	@Autowired private SecurityService securityService;
	@Autowired private UsageSessionService usageSessionService;
	@Autowired private SiteService siteService;
	@Autowired private AuthzGroupService authzGroupService;
	@Autowired private SessionManager sessionManager;
	@Autowired private ToolManager toolManager;
	@Autowired private GradingService gradingService;
	@Autowired private FormattedText formattedText;

	private String randomUUID = UUID.randomUUID().toString();

	private KeyPair localKeyPair = LTI13Util.generateKeyPair();

	/**
	 * Setup a security advisor.
	 */
	public void pushAdvisor() {
		// setup a security advisor
		securityService.pushAdvisor(new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function,
					String reference) {
				return SecurityAdvice.ALLOWED;
			}
		});
	}

	/**
	 * Remove our security advisor.
	 */
	public void popAdvisor() {
		securityService.popAdvisor();
	}

	// TODO: Make this a *lot* prettier and add forward to knowledge base feature :)
	public void doError(HttpServletRequest request,HttpServletResponse response, String s, String message, Throwable e) throws java.io.IOException {
		if (e != null) {
			log.error(e.getLocalizedMessage(), e);
		}
		log.info("{}: {}", rb.getString(s), message);
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<body style=\"color: black; background-color: Pink;\">");
		out.println(rb.getString(s));
		out.println(htmlEscape(message));
		if ( e != null ) out.println(e.getMessage());
		out.println("</body></html>");
	}

	// TODO: Make this a *lot* prettier and add forward to knowledge base feature :)
	public void addError(PrintWriter out, String s, String message, Throwable e) throws java.io.IOException {
		if (e != null) {
			log.error(e.getLocalizedMessage(), e);
		}
		log.info("{}: {}", rb.getString(s), message);
		out.println(rb.getString(s));
		out.println(htmlEscape(message));
		if ( e != null ) out.println(e.getMessage());
	}

	@Override
	public void init(ServletConfig config) throws ServletException {

		super.init(config);
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

		ApplicationContext ac = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());

		// Warm up the keyset
		KeyPair kp = SakaiKeySetUtil.getCurrent();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		doPost(request, response);
	}

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("doPost {}", request.getPathInfo());

		String ipAddress = request.getRemoteAddr();
		if (log.isDebugEnabled()) {
			log.debug("Sakai Plus Provider request from IP={}", ipAddress);
		}

		if ( !plusService.enabled() ) {
			log.warn("LTI Advantage Provider is Disabled IP={}", ipAddress);
			response.sendError(HttpServletResponse.SC_FORBIDDEN,
					"LTI Advantage Provider is Disabled");
			return;
		}

		String uri = request.getPathInfo(); // plus/sakai/oidc_login/4444
		String[] parts = uri.split("/");

		// /plus/sakai/oidc_login/44guid44
		if (parts.length >= 3 && "oidc_login".equals(parts[1])) {
			if ( parts.length == 3 && StringUtils.isNotBlank(parts[2])) {
				handleOIDCLogin(request, response, parts[2]);
				return;
			}
			doError(request, response, "plus.oidc_login.format", uri, null);
			return;
		}

		// /plus/sakai/dynamic/44guid44?reg_token=..&openid_configuration=https:..
		// https://www.imsglobal.org/spec/lti-dr/v1p0
		if (parts.length >= 3 && "dynamic".equals(parts[1])) {
			if ( parts.length == 3 && StringUtils.isNotBlank(parts[2])) {
				handleDynamicRegistration(request, response, parts[2]);
				return;
			}
			doError(request, response, "plus.oidc_login.format", uri, null);
			return;
		}

		if (log.isDebugEnabled()) {
			Map<String, String[]> params = (Map<String, String[]>) request
					.getParameterMap();
			for (Map.Entry<String, String[]> param : params.entrySet()) {
				log.debug("{}:{}", param.getKey(), param.getValue()[0]);
			}
		}

		if ( "/canvas-config.json".equals(request.getPathInfo()) ) {
			if ( serverConfigurationService.getBoolean(PlusService.PLUS_CANVAS_ENABLED, PlusService.PLUS_CANVAS_ENABLED_DEFAULT))  {
				handleCanvasConfig(request, response);
				return;
			} else {
				log.warn("Canvas config is Disabled IP={}", ipAddress);
				response.sendError(HttpServletResponse.SC_FORBIDDEN,
						"Canvas config is Disabled");
				return;
			}
		}

		// REQUIRED. The issuer identifier identifying the learning platform.
		String id_token = request.getParameter("id_token");
		String state = request.getParameter("state");
		String payloadStr = request.getParameter("payload");

		// TODO: Check for and add Extra Canvas error detail...
		if ( StringUtils.isBlank(id_token) ) {
			doError(request, response, "plus.launch.id_token.notfound", null, null);
			return;
		}

		// Parse the id_token and check for format and missing values (validation is later)
		String rawbody = LTI13JwtUtil.rawJwtBody(id_token);
		ObjectMapper mapper = JacksonUtil.getLaxObjectMapper();

		SakaiLaunchJWT launchJWT = mapper.readValue(rawbody, SakaiLaunchJWT.class);

		String issuer = launchJWT.issuer;
		String clientId = launchJWT.audience;
		String deploymentId = launchJWT.deployment_id;

		if ( StringUtils.isBlank(issuer) || StringUtils.isBlank(clientId) || StringUtils.isBlank(deploymentId) ) {
			doError(request, response, "plus.launch.id_token.missing.data", null, null);
			return;
		}

		// Verify the state
		if ( StringUtils.isBlank(state) ) {
			doError(request, response, "plus.launch.state.notfound", null, null);
			return;
		}

		// First check the signature on state - this is inexpensive and requires no DB
		Key stateKey = localKeyPair.getPublic();
		// Chaos Monkey : stateKey = LTI13Util.generateKeyPair().getPublic();
		Claims claims = null;
		try {
			Jws<Claims> jws = Jwts.parser().setAllowedClockSkewSeconds(60).setSigningKey(stateKey).parseClaimsJws(state);
			claims = jws.getBody();
		} catch (io.jsonwebtoken.security.SignatureException e) {
			doError(request, response, "plus.launch.state.signature", null, null);
			return;
		}

		// TODO: Double check the browser signature

		// Load tenant and make sure it matches the Launch
		String tenant_guid = (String) claims.get("tenant_guid");

		Optional<Tenant> optTenant = tenantRepository.findById(tenant_guid);
		Tenant tenant = null;
		if ( optTenant.isPresent() ) {
			tenant = optTenant.get();
		}

		if ( tenant == null ) {
			doError(request, response, "plus.tenant.notfound", tenant_guid, null);
			return;
		}

		// For Canvas, it makes *lots* of deployment_id values for each clientId
		// so we just leave deployment_id blank for canvas and accept any deployment_id.
		// We track each deployent_id on the Context for AccessToken calls
		String missing = "";
		if (! issuer.equals(tenant.getIssuer()) ) missing = missing + "issuer mismatch " + issuer + "/" + tenant.getIssuer();
		if (! clientId.equals(tenant.getClientId()) ) missing = missing + "clientId mismatch " + clientId + "/" + tenant.getClientId();
		if (! tenant.validateDeploymentId(deploymentId) ) missing = missing + "deploymentId mismatch " + deploymentId + "/" + tenant.getDeploymentId();

		if ( ! missing.equals("") ) {
			doError(request, response, "plus.plusservice.tenant.check", missing, null);
		}

		// Store this all in payload for future use and to share some of the
		// processing code between LTI 1.1 and Advantage
		Map<String, String> payload = plusService.getPayloadFromLaunchJWT(tenant, launchJWT);
		payload.put("tenant_guid", tenant_guid);
		payload.put("id_token", id_token);

		log.debug("Message type="+launchJWT.message_type);

		// Look at the message type
		if ( LaunchJWT.MESSAGE_TYPE_LTI_CONTEXT.equals(launchJWT.message_type) || 
		     LaunchJWT.MESSAGE_TYPE_LAUNCH.equals(launchJWT.message_type) ) {
			// Fall through
		} else if ( LaunchJWT.MESSAGE_TYPE_LTI_DATA_PRIVACY_LAUNCH_REQUEST.equals(launchJWT.message_type) ) {
			String privacyUrl = serverConfigurationService.getString(PlusService.PLUS_SERVER_POLICY_URI,
				serverConfigurationService.getString(PlusService.PLUS_SERVER_TOS_URI, DEFAULT_PRIVACY_URL));
			log.debug("Redirecting privacyUrl="+privacyUrl);
			response.sendRedirect(privacyUrl);
			return;
		} else {
			doError(request, response, "plus.message_type.unsupported", launchJWT.message_type, null);
			return;
		}

		// Make sure we are not in an iframe in case we can't set a cookie
		String repost = request.getParameter("repost");
		if ( StringUtils.isBlank(repost) ) {
			boolean forceNewWindow = true;
			handleRepost(request, response, forceNewWindow);
			return;
		}

		// Now we are in the oidc_launch process so we proceed with all the validation
		log.debug("==== oidc_launch ====");

		/*
		 * If this is true, multiple issuer/clientid/deploymentid/subject users will map to a single
		 * Sakai user in ths instance based on email address.  This needs to be set to true if
		 * this Sakai is ever to become an enterprise-wde LMS using the email address as
		 * SSO without major conversion.  It also means that this Sakai can be behing an SSO or if users
		 * reset their passwords - they can directly log in with their email addresses and
		 * see all their sites and history.
		 *
		 * If this is true, the user's email address will be their EID across multiple Tenants.
		 * even though this setting is per-Tenant.  There is no account siloing between the Tenants
		 * that have this enabled.
		 */
		boolean isEmailTrustedConsumer = ! Boolean.FALSE.equals(tenant.getTrustEmail());

		try {
			Launch launch = validate(payload, launchJWT, tenant);

			User user = userFinderOrCreator.findOrCreateUser(payload, false, isEmailTrustedConsumer);
			if ( plusService.verbose() ) {
				log.info("user={}", user);
			} else {
				log.debug("user={}", user);
			}

			plusService.connectSubjectAndUser(launch.getSubject(), user);

			// Check if we are loop-backing on the same server, and already logged in as same user
			Session sess = sessionManager.getCurrentSession();
			String serverUrl = SakaiLTIUtil.getOurServerUrl();
			String iss = launch.tenant.getIssuer();
			if ( StringUtils.equals(iss, serverUrl) ) {
				log.debug("Running loopback id={} serverUrl={} iss={}", sess.getId(), serverUrl,iss);
			} else {
				sess.clear();
				log.debug("Session cleared id={} serverUrl={} iss={}", sess.getId(), serverUrl,iss);
			}

			loginUser(ipAddress, user);

			// Re-grab the session
			sess = sessionManager.getCurrentSession();

			// This needs to happen after login, when we have a session for the user.
			userLocaleSetter.setupUserLocale(payload, user, false, isEmailTrustedConsumer);

			userPictureSetter.setupUserPicture(payload, user, false, isEmailTrustedConsumer);

			if ( launch.getContext() != null ) {
				payload.put("lineitems_url", launch.getContext().getLineItems());
				payload.put("lineitems_token", launch.getContext().getLineItemsToken());

				payload.put("grade_token", launch.getContext().getGradeToken());

				payload.put("nrps_url", launch.getContext().getContextMemberships());
				payload.put("nrps_token", launch.getContext().getNrpsToken());
			}

			Site site = findOrCreateSite(payload, tenant);
			if ( plusService.verbose() ) {
				log.info("site={}", site);
			} else {
				log.debug("site={}", site);
			}

			plusService.connectContextAndSite(launch.getContext(), site);

			siteEmailPreferenceSetter.setupUserEmailPreferenceForSite(payload, user, site, false);

			site = siteMembershipUpdater.addOrUpdateSiteMembership(payload, false, user, site);

			String contextGuid = (String) payload.get("context_guid");
			if ( contextGuid != null && launch.getContext() != null ) {
				String roles = payload.get("roles");
				boolean isInstructor = (roles != null && roles.toLowerCase().contains("Instructor".toLowerCase()));
				plusService.requestSyncSiteMembershipsCheck(launch.getContext(), isInstructor);
			}

			// Construct a URL to the site
			StringBuilder url = new StringBuilder();
			url.append(SakaiLTIUtil.getOurServerUrl());
			url.append(serverConfigurationService.getString("portalPath", "/portal"));
			url.append("/site/");
			url.append(site.getId());

			log.debug("Redirecting {}", url.toString());

			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_FOUND);
			response.sendRedirect(url.toString());

		} catch (LTIException ltiException) {
			doError(request, response, ltiException.getErrorKey(), ltiException.getMessage(), ltiException.getCause());
		}

	}

	// https://www.imsglobal.org/spec/security/v1p0/
	// https://www.imsglobal.org/spec/security/v1p0/#step-1-third-party-initiated-login
	// https://openid.net/specs/openid-connect-core-1_0.html#ThirdPartyInitiatedLogin
	// https://www.imsglobal.org/spec/lti/v1p3/#additional-login-parameters
	protected void handleOIDCLogin(HttpServletRequest request, HttpServletResponse response, String tenant_guid) throws ServletException, IOException {
		log.debug("==== oidc_login ====");

		// REQUIRED. Hint to the Authorization Server about the login identifier the End-User might use to log in. The permitted values will be defined in the host specification.
		String login_hint = request.getParameter("login_hint");

		// OPTIONAL. The actual LTI message that is being launched.
		String lti_message_hint = request.getParameter("lti_message_hint");

		// Legacy lookup is our tenant_guid from the URL since most LMS's don't send the optional stuff
		// TODO: Future lookup allows for, iss, client_id, and deployment_id to uniquely define a tenant
		Optional<Tenant> optTenant = tenantRepository.findById(tenant_guid);
		Tenant tenant = null;
		if ( optTenant.isPresent() ) {
			tenant = optTenant.get();
		}

		if ( tenant == null ) {
			doError(request, response, "plus.tenant.notfound", tenant_guid, null);
			return;
		}

		String browserSig = LTIUtil.getBrowserSignature(request);
		String stateSig = LTI13Util.sha256(randomUUID + browserSig);
		Key privateKey = localKeyPair.getPrivate();
		String seconds = (Instant.now().getEpochSecond()+"");
		JwtBuilder jwt = Jwts.builder();
		jwt.claim("internal", stateSig);
		jwt.claim("time", seconds);
		jwt.claim("tenant_guid", tenant_guid);

		String jws = jwt.signWith(privateKey).compact();

		String redirect_uri = plusService.getOidcLaunch();
		try {
			URIBuilder redirect = new URIBuilder(tenant.getOidcAuth().trim());
			redirect.addParameter("scope", "openid");
			redirect.addParameter("response_type", "id_token");
			redirect.addParameter("response_mode", "form_post");
			redirect.addParameter("prompt", "none");
			redirect.addParameter("nonce", UUID.randomUUID().toString());
			if ( lti_message_hint != null ) redirect.addParameter("lti_message_hint", lti_message_hint);
			redirect.addParameter("client_id", tenant.getClientId());
			redirect.addParameter("login_hint", login_hint);
			redirect.addParameter("redirect_uri", redirect_uri);
			redirect.addParameter("state", jws);
			String redirect_url = redirect.build().toString();
			response.sendRedirect(redirect_url);
		} catch (URISyntaxException e) {
			log.error("Syntax exception building the URL with the params: {}.", e.getMessage());
		}
	}

	// https://www.imsglobal.org/spec/lti-dr/v1p0
	// http://localhost:8080/plus/sakai/dynamic/123456?reg_token=..&openid_configuration=https:..
	// See also tsugi/settings/key/auto_common.php
	protected void handleDynamicRegistration(HttpServletRequest request, HttpServletResponse response, String tenant_guid) throws ServletException, IOException {
		log.debug("==== dynamic ====");

		String openid_configuration = request.getParameter("openid_configuration");
		String registration_token = request.getParameter("registration_token");
		String unlock_token_request = request.getParameter("unlock_token");
		log.info("openid_configuration={} registration_token={} unlock_token={} tenant_guid={}", openid_configuration, registration_token, unlock_token_request, tenant_guid);

		// registration_token is optional
		String missing = "";
		if (StringUtils.isBlank(openid_configuration) ) missing = missing + " openid_configuration";
		if (StringUtils.isBlank(unlock_token_request) ) missing = missing + " unlock_token";

		if ( ! missing.equals("") ) {
			doError(request, response, "plus.dynamic.request.missing", missing, null);
			return;
		}

		Optional<Tenant> optTenant = tenantRepository.findById(tenant_guid);
		Tenant tenant = null;
		if ( optTenant.isPresent() ) {
			tenant = optTenant.get();
		}

		if ( tenant == null ) {
			doError(request, response, "plus.tenant.notfound", tenant_guid, null);
			return;
		}

		if ( ! unlock_token_request.equals(tenant.getOidcRegistrationLock()) ) {
			doError(request, response, "plus.dynamic.unlock.mismatch", tenant_guid, null);
			return;
		}

		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<body style=\"color: black; background-color: Azure;\">");
		out.println("<h1>");
		out.println(rb.getString("plus.dynamic.welcome"));
		out.println("</h1>");
		out.print("<p><strong>");
		out.println(rb.getString("plus.dynamic.starting"));
		out.println(htmlEscape(tenant_guid));
		out.print(" (<a href=\"https://www.imsglobal.org/spec/lti-dr/v1p0\" target=\"_blank\">");
		out.print(rb.getString("plus.dynamic.specification"));
		out.println(")</a></strong></p>");

		out.print("<p><strong>");
		out.print(rb.getString("plus.dynamic.config.url"));
		out.println("</strong><br/>");
		out.print(htmlEscape(openid_configuration));
		out.println("</p>");

		String body;
		StringBuffer dbs = new StringBuffer();
		try {
			HttpResponse<String> httpResponse = HttpClientUtil.sendGet(openid_configuration, null, null, dbs);
			body = httpResponse.body();
		} catch (Exception e) {
			log.error("Error retrieving openid_configuration at {}", openid_configuration);
			log.error(dbs.toString());
			addError(out, "plus.dynamic.badurl", openid_configuration, e);
			tenant.setStatus("Error retrieving openid_configuration at "+openid_configuration);
			dbs.append("Exception\n");
			dbs.append(e.getMessage());
			tenant.setDebugLog(dbs.toString());
			tenantRepository.save(tenant);
			return;
		}

		out.println(togglePre(rb.getString("plus.dynamic.retrieved.openid"), body));

		// Create and configure an ObjectMapper instance
		ObjectMapper mapper = JacksonUtil.getLaxObjectMapper();
		OpenIDProviderConfiguration openIDConfig;
		try {
			// Moodle returns an array of strings - the spec returns an array of objects
			body = org.tsugi.HACK.HackMoodle.hackOpenIdConfiguration(body);
			openIDConfig = mapper.readValue(body, OpenIDProviderConfiguration.class);

			if ( openIDConfig == null ) {
				addError(out, "plus.dynamic.parse", openid_configuration, null);
			}
		} catch ( Exception e ) {
			openIDConfig = null;
			addError(out, "plus.dynamic.parse", openid_configuration, e);
			dbs.append("Exception\n");
			dbs.append(e.getMessage());
		}

		if ( openIDConfig == null ) {
			log.error("Error parsing openid_configuration at {}", openid_configuration);
			log.error(dbs.toString());
			tenant.setStatus("Error parsing openid_configuration at "+openid_configuration);
			tenant.setDebugLog(dbs.toString());
			tenantRepository.save(tenant);
			return;
		}

		// TODO: Make sure issuer matches
		String issuer = openIDConfig.issuer;
		String authorization_endpoint = openIDConfig.authorization_endpoint;
		String token_endpoint = openIDConfig.token_endpoint;
		String jwks_uri = openIDConfig.jwks_uri;
		String registration_endpoint = openIDConfig.registration_endpoint;

		// Check for required items
		missing = "";
		if (StringUtils.isBlank(issuer) ) missing = missing + " issuer";
		if (StringUtils.isBlank(authorization_endpoint) ) missing = missing + " authorization_endpoint";
		if (StringUtils.isBlank(token_endpoint) ) missing = missing + " token_endpoint";
		if (StringUtils.isBlank(jwks_uri) ) missing = missing + " jwks_uri";
		if (StringUtils.isBlank(registration_endpoint) ) missing = missing + " registration_endpoint";

		if ( ! missing.equals("") ) {
			addError(out, "plus.dynamic.missing", missing, null);
			return;
		}

		if ( ! issuer.equals(tenant.getIssuer()) ) {
			log.error("Retrieved issuer {} does not match stored issuer {}", issuer, tenant.getIssuer());
			addError(out, "plus.dynamic.issuer.mismatch", issuer+" / "+tenant.getIssuer(), null);
			return;
		}

		OpenIDClientRegistration reg = new OpenIDClientRegistration();

		String serverUrl = null;
		String host = null;
		String domain = null;
		try {
			serverUrl = SakaiLTIUtil.getOurServerUrl();
			URL netUrl = new URL(serverUrl);
			host = netUrl.getHost();
			domain = serverConfigurationService.getString("plus.dynamic.domain", host);
		} catch (MalformedURLException e) {
			addError(out, "plus.dynamic.missing.domain", e.getMessage(), e.getCause());
			return;
		}

		String title = tenant.getTitle();
		if ( StringUtils.isBlank(title) ) {
			title = serverConfigurationService.getString(PlusService.PLUS_SERVER_TITLE, rb.getString(PlusService.PLUS_SERVER_TITLE));
		}
		String description = tenant.getDescription();
		if ( StringUtils.isBlank(description) ) {
			description = serverConfigurationService.getString(PlusService.PLUS_SERVER_DESCRIPTION, rb.getString(PlusService.PLUS_SERVER_DESCRIPTION));
		}

		// Lets full up the registration request
		reg.client_name = title;
		reg.client_uri = serverUrl;
		reg.initiate_login_uri = plusService.getOidcLogin(tenant);
		reg.redirect_uris.add(plusService.getOidcLaunch());
		reg.jwks_uri = plusService.getOidcKeySet();
		reg.policy_uri = serverConfigurationService.getString(PlusService.PLUS_SERVER_POLICY_URI, null);
		reg.tos_uri = serverConfigurationService.getString(PlusService.PLUS_SERVER_TOS_URI, null);
		reg.logo_uri = serverConfigurationService.getString(PlusService.PLUS_SERVER_LOGO_URI, null);
		reg.scope = LTI13ConstantsUtil.SCOPE_LINEITEM + " " +
					LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY + " " +
					LTI13ConstantsUtil.SCOPE_SCORE + " " +
					LTI13ConstantsUtil.SCOPE_RESULT_READONLY + " " +
					LTI13ConstantsUtil.SCOPE_NAMES_AND_ROLES;

		LTIToolConfiguration ltitc = new LTIToolConfiguration();
		ltitc.addCommonClaims();
		ltitc.domain = domain;
		ltitc.description = description;

		// Note: Not including placements is OK when there is just one end point for each message type
		LTILaunchMessage lm = new LTILaunchMessage();
		lm.type = LaunchJWT.MESSAGE_TYPE_LAUNCH;
		lm.label = rb.getString("plus.provision.sakai.plus");
		lm.target_link_uri = plusService.getPlusServletPath() + "/";
		ltitc.messages.add(lm);

		lm = new LTILaunchMessage();
		lm.type = LaunchJWT.MESSAGE_TYPE_LTI_CONTEXT;
		lm.label = rb.getString("plus.provision.context.launch");
		lm.target_link_uri = plusService.getPlusServletPath() + "/";
		ltitc.messages.add(lm);

		String privacyUrl = serverConfigurationService.getString(PlusService.PLUS_SERVER_POLICY_URI,
				serverConfigurationService.getString(PlusService.PLUS_SERVER_TOS_URI, null));
		if ( privacyUrl != null ) {
			lm = new LTILaunchMessage();
			lm.type = LaunchJWT.MESSAGE_TYPE_LTI_DATA_PRIVACY_LAUNCH_REQUEST;
			lm.label = rb.getString("plus.provision.context.launch");
			lm.target_link_uri = privacyUrl;
			ltitc.messages.add(lm);
		}

		reg.lti_tool_configuration = ltitc;

		Map<String, String> headers = new HashMap<String, String>();
		if (! StringUtils.isBlank(registration_token) ) headers.put("Authorization", "Bearer "+registration_token);
		headers.put("Content-type", "application/json");

		String regs = reg.prettyPrintLog();
		out.println(togglePre(rb.getString("plus.dynamic.client.request"), regs));
		body = null;
		try {
			HttpResponse<String> registrationResponse = HttpClientUtil.sendPost(registration_endpoint, regs, headers, dbs);
			body = registrationResponse.body();
			if ( StringUtils.isBlank(body) ) {
				addError(out, "plus.dynamic.registration.post", null, null);
			}
		} catch (Exception e) {
			body = null;
			log.error("Error posting client registration {}", registration_endpoint, e);
			addError(out, "plus.dynamic.registration.post", null, e);
		}

		out.println(togglePre(rb.getString("plus.dynamic.client.response"), body));

		// Remember the registration
		tenant.setOidcRegistration(body);

		if ( StringUtils.isBlank(body) ) {
			tenant.setStatus("Error posting client registration "+registration_endpoint);
			tenant.setDebugLog(dbs.toString());
			log.error(dbs.toString());
			tenantRepository.save(tenant);
			return;
		}

		// Create and configure an ObjectMapper instance
		mapper = JacksonUtil.getLaxObjectMapper();
		OpenIDClientRegistration platformResponse;
		try {
			platformResponse = mapper.readValue(body, OpenIDClientRegistration.class);

			if ( platformResponse == null ) {
				addError(out, "plus.dynamic.parse", openid_configuration, null);
			}
		} catch ( Exception e ) {
			platformResponse = null;
			addError(out, "plus.dynamic.parse", openid_configuration, e);
			dbs.append("Exception\n");
			dbs.append(e.getMessage());
		}

		if ( platformResponse == null || platformResponse.client_id == null || platformResponse.lti_tool_configuration == null) {
			tenant.setStatus("Error parsing client registration "+registration_endpoint);
			tenant.setDebugLog(dbs.toString());
			log.error(dbs.toString());
			tenantRepository.save(tenant);
			return;
		}

		LTIToolConfiguration tcResponse = platformResponse.lti_tool_configuration;
		String deployment_id = tcResponse.deployment_id;

		tenant.setOidcAuth(openIDConfig.authorization_endpoint);
		tenant.setOidcToken(openIDConfig.token_endpoint);
		tenant.setOidcKeySet(openIDConfig.jwks_uri);
		tenant.setOidcRegistrationEndpoint(openIDConfig.registration_endpoint);
		// Clear the registration key
		tenant.setOidcRegistrationLock(null);

		tenant.setClientId(platformResponse.client_id);
		if ( ! StringUtils.isBlank(deployment_id) ) {
			tenant.setDeploymentId(deployment_id);
			tenant.setStatus("Registration "+tenant_guid+" complete with deployment_id "+deployment_id);
		} else {
			tenant.setStatus("Registration "+tenant_guid+" finished, but without deployment_id");
		}
		tenant.setDebugLog(dbs.toString());

		tenantRepository.save(tenant);
		log.info(tenant.getStatus());

		out.println("<p>");
		out.println(rb.getString("plus.dynamic.lock.cleared"));
		out.println("</p>");

		out.println("<p><strong>");
		out.println(tenant.getStatus());
		out.println("</strong></p>");
		out.println("<p><button onclick=\"(window.opener || window.parent).postMessage({subject:'org.imsglobal.lti.close'}, '*')\">");
		out.print(rb.getString("plus.dynamic.continue"));
		out.println("</button></p>\n<hr/>\n");
		out.println(togglePre(rb.getString("plus.dynamic.debug"), dbs.toString()));
		out.println("</body>");
		out.println("</html>");
	}

	protected void handleRepost(HttpServletRequest request, HttpServletResponse response, boolean forceNewWindow) throws ServletException, IOException {
		log.debug("==== oidc_repost ====");

		StringBuilder r = new StringBuilder();
		r.append("<!DOCTYPE html>\n");
		r.append("<!-- SakaiPlus handleRepost() frame/cookie check... -->\n");
		r.append("<form id=\"popform\" method=\"post\" action=\"");
		r.append(SakaiLTIUtil.getOurServletPath(request));
		r.append("\">\n");
		r.append("<input type=\"hidden\" name=\"repost\" value=\"42\">\n");
		for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ) {
			String key = (String)e.nextElement();
			String value = request.getParameter(key);
			r.append("<input type=\"hidden\" name=\"");
			r.append(key);
			r.append("\" value=\"");
			r.append(value);
			r.append("\">\n");
		}
		r.append("<input id=\"repost_submit\" style=\"display:none;\" onclick=\"repost_click();\" type=\"submit\" name=\"POP\" value=\"");
		r.append(rb.getString("plus.repost.new.window"));
		r.append("\">\n");
		r.append("</form>\n");
		r.append("<div id=\"repost_done\" style=\"display: none; border: 1px; margin: 5px; padding 5px;\">\n");
		r.append("<button type=\"button\" onclick=\"return false;\">\n");
		r.append(rb.getString("plus.repost.loaded"));
		r.append("</button></div>\n");

		r.append("<script>\n");
		r.append("function repost_click() {\n");
		r.append("document.getElementById(\"repost_submit\").style.display = \"none\";\n");
		r.append("document.getElementById(\"repost_done\").style.display = \"block\";\n");
		r.append("}\n");

		if ( forceNewWindow ) {
			r.append("var forceNewWindow = true;\n");
		} else {
			r.append("var forceNewWindow = false;\n");
		}
		r.append("console.log('forceNewWindow', forceNewWindow);\n");

		// Check if we are https and cannot set a cookie w/ SameSite None (i.e Safari)
		// Also check if we already have a JESSSIONID
		r.append("  var cookie = 'sakaiplus_test_cookie=1; path=/';\n");
		r.append("  if (window.location.protocol === 'https:' ) {\n");
		r.append("    cookie = cookie + '; SameSite=None; secure';\n");
		r.append("  }\n");
		r.append("  document.cookie = cookie;\n");
		r.append("  var goodcookie = document.cookie.indexOf('sakaiplus_test_cookie') != 1 ||");
		r.append("    document.cookie.indexOf('JSESSIONID') != 1;\n");
		r.append("  console.log('goodcookie', goodcookie, document.cookie);\n");
		r.append("  if (!goodcookie) {\n");
		r.append("   console.log('Forcing new window, in order to set login cookie');\n");
		r.append("   forceNewWindow = true;\n");
		r.append("  }");

		// If we are not the top window and want to be the top window, pause for a user action
		r.append("if ( window != window.parent && forceNewWindow ) {\n");
		r.append("  document.getElementById('repost_submit').style.display = 'block';\n");
		r.append("  document.getElementById('popform').target = '_blank';\n");
		r.append("} else {\n");

		// We are either already in the top window, or don't want to be
		r.append("  document.getElementById('popform').submit();\n");
		r.append("}\n");

		r.append("</script>\n");
		LTIUtil.sendHTMLPage(response, r.toString());
	}

	protected Launch validate(Map payload, SakaiLaunchJWT launchJWT, Tenant tenant) throws LTIException
	{
		  //check parameters
		  String id_token = (String) payload.get("id_token");
		  String tenant_guid = (String) payload.get("tenant_guid");
		  String context_id = (String) payload.get(LTIConstants.CONTEXT_ID);

		// Begin Validation

		JSONObject header = LTI13JwtUtil.jsonJwtHeader(id_token);
		String kid = (String) header.get("kid");
		if ( StringUtils.isBlank(kid) ) {
			throw new LTIException( "plus.launch.kid.notfound", null, null);
		}

		// Find the pubic key for the id_token, first check the most recent keyset
		RSAPublicKey tokenKey = null;
		String cacheKeySet = tenant.getCacheKeySet();
		if ( ! StringUtils.isBlank(cacheKeySet) ) {
			try {
				tokenKey = LTI13KeySetUtil.getKeyFromKeySetString(kid, cacheKeySet);
			} catch(Exception e) {
				// No big thing - just ignore it and move on
				tokenKey = null;
				log.debug("Exception loading kid={} tenant={} keyset={}", kid, tenant_guid, cacheKeySet);
			}
		}

		if ( tokenKey == null ) {
			String oidcKeySet = tenant.getOidcKeySet();
			if ( StringUtils.isBlank(oidcKeySet) ) {
				throw new LTIException( "plus.launch.keyset.blank", tenant_guid, null);
			}
			log.debug("loading kid={} from keyset={}",kid, oidcKeySet);
			com.nimbusds.jose.jwk.JWKSet keySet = null;
			try {
				keySet =  LTI13KeySetUtil.getKeySetFromUrl(oidcKeySet);
			} catch(Exception e) {
				throw new LTIException( "plus.launch.keyset.load.fail", oidcKeySet, null);
			}
			log.debug("loaded keyset={} result={}",oidcKeySet, keySet.toString());
			try {
				tokenKey = LTI13KeySetUtil.getKeyFromKeySet(kid, keySet);
			} catch(Exception e) {
				throw new LTIException( "plus.launch.kid.load.fail", "kid="+kid+" keySet="+oidcKeySet, null);
			}

			// Store the new keyset in the Tenant
			if ( keySet != null ) {
				tenant.setCacheKeySet(keySet.toString());
				tenantRepository.save(tenant);
				log.debug("Stored new keyset in tenant");
			}

		}

		if ( tokenKey == null ) {
			throw new LTIException( "plus.launch.kid.load.fail", kid, null);
		}

		// Check the signature on incoming id_token
		try {
			// If you want a Chaos Monkey uncomment this :)
			// Jws<Claims> jwsClaims = Jwts.parser().setAllowedClockSkewSeconds(60).setSigningKey(stateKey).parseClaimsJws(id_token);
			Jws<Claims> jwsClaims = Jwts.parser().setAllowedClockSkewSeconds(60).setSigningKey(tokenKey).parseClaimsJws(id_token);
		} catch (Exception e) {
			throw new LTIException( "plus.launch.id_token.signature", kid, e);
		}

		Launch launch = null;
		try {
			launch = plusService.updateAll(launchJWT, tenant);
			// tenant_guid is already there
			if ( launch.getLink() != null ) payload.put("link_guid", launch.getLink().getId());
			if ( launch.getContext() != null ) payload.put("context_guid", launch.getContext().getId());
			if ( launch.getSubject() != null ) payload.put("subject_guid", launch.getSubject().getId());
		} catch (Exception e) {
			throw new LTIException( "plus.launch.id_token.load.fail", tenant_guid, e);
		}

		final Session sess = sessionManager.getCurrentSession();

		if (sess == null) {
			throw new LTIException( "launch.no.session", context_id, null);
		}

		return launch;

	}

	protected Site findOrCreateSite(Map payload, Tenant tenant) throws LTIException {

		String context_guid = (String) payload.get("context_guid");
		String siteId = context_guid;

		if (log.isDebugEnabled()) {
			log.debug("siteId={}", siteId);
		}

		final String context_title_orig = (String) payload.get(LTIConstants.CONTEXT_TITLE);
		final String context_label = (String) payload.get(LTIConstants.CONTEXT_LABEL);

		// Site title is editable; cannot but null/empty after HTML stripping, and cannot exceed max length
		String context_title = formattedText.stripHtmlFromText(context_title_orig, true, true);
		SiteTitleValidationStatus status = siteService.validateSiteTitle(context_title_orig, context_title);

		if (SiteTitleValidationStatus.STRIPPED_TO_EMPTY.equals(status)) {
			log.warn("Provided context_title is empty after HTML stripping: {}", context_title_orig);
		} else if (SiteTitleValidationStatus.EMPTY.equals(status)) {
			log.warn("Provided context_title is empty after trimming: {}", context_title_orig);
		} else if (SiteTitleValidationStatus.TOO_LONG.equals(status)) {
			log.warn("Provided context_title is longer than max site title length of {}: {}", SITE_TITLE_MAX_LENGTH, context_title_orig);
		}

		Site site = null;

		// Get the site if it exists
		try {
			site = siteService.getSite(siteId);
			if ( plusService.verbose() ) {
				log.info("Loaded existing plus site={}", site.getId());
			} else {
				log.debug("Loaded existing plus site={}", site.getId());
			}
			updateSiteDetailsIfChanged(site, context_title, context_label);
			return site;
		} catch (Exception e) {
			if ( plusService.verbose() ) {
				log.info("Did not find existing plus site={}", siteId);
			} else {
				log.debug("Did not find existing plus site={}", siteId);
			}
		}

		// If site does not exist, create the site
		pushAdvisor();
		try {
			String sakai_type = PlusService.PLUS_NEW_SITE_TYPE_DEFAULT;
			boolean templateSiteExists = false;
			String autoSiteTemplateId = tenant.getSiteTemplate();
			if ( StringUtils.isNotBlank(autoSiteTemplateId) ) {
				templateSiteExists = siteService.siteExists(autoSiteTemplateId);
				if ( !templateSiteExists ) log.warn("Could not find tenant-specified template site ({}).", autoSiteTemplateId);
			}

			if ( !templateSiteExists ) {
				autoSiteTemplateId = serverConfigurationService.getString(PlusService.PLUS_NEW_SITE_TEMPLATE);
				if ( StringUtils.isNotBlank(autoSiteTemplateId) ) {
					templateSiteExists = siteService.siteExists(autoSiteTemplateId);
					if ( !templateSiteExists ) log.warn("Could not find site template from sakai.properties {}={}.", PlusService.PLUS_NEW_SITE_TEMPLATE, autoSiteTemplateId);
				}
			}

			if ( !templateSiteExists ) {
				autoSiteTemplateId = PlusService.PLUS_NEW_SITE_TEMPLATE_DEFAULT;
				if ( StringUtils.isNotBlank(autoSiteTemplateId) ) {
					templateSiteExists = siteService.siteExists(autoSiteTemplateId);
					if ( !templateSiteExists ) log.warn("Could not find default site template={}.", autoSiteTemplateId);
				}
			}

			if (!templateSiteExists) {
				log.warn("Could not find template site ({}) falling back to ({}) instead.", autoSiteTemplateId, PlusService.PLUS_NEW_SITE_TEMPLATE_BACKUP);
				autoSiteTemplateId = PlusService.PLUS_NEW_SITE_TEMPLATE_BACKUP;
				templateSiteExists = siteService.siteExists(autoSiteTemplateId);
			}

			if(!templateSiteExists) {
				log.warn("Template site ({}) was not found. A site will be created with the default template.", autoSiteTemplateId);
			}

			boolean templateRealmExists = false;
			AuthzGroup azg = null;
			String autoRealmTemplateId = tenant.getRealmTemplate();
			if ( StringUtils.isNotBlank(autoRealmTemplateId) ) {
				try {
					azg = authzGroupService.getAuthzGroup(autoRealmTemplateId);
					templateRealmExists = true;
				} catch ( GroupNotDefinedException e ) {
					log.warn("Could not find tenant-specified realm template ({}).", autoRealmTemplateId);
				}
			}

			if ( ! templateRealmExists ) autoRealmTemplateId = serverConfigurationService.getString(PlusService.PLUS_NEW_SITE_REALM);

			if ( ! templateRealmExists && StringUtils.isNotBlank(autoRealmTemplateId) ) {
				try {
					azg = authzGroupService.getAuthzGroup(autoRealmTemplateId);
					templateRealmExists = true;
				} catch ( GroupNotDefinedException e ) {
					log.warn("Could not find sakai.properties realm template {}={}.", PlusService.PLUS_NEW_SITE_REALM, autoRealmTemplateId);
				}
			}

			if ( ! templateRealmExists ) autoRealmTemplateId = PlusService.PLUS_NEW_SITE_REALM_DEFAULT;

			if ( ! templateRealmExists && StringUtils.isNotBlank(autoRealmTemplateId) ) {
				try {
					azg = authzGroupService.getAuthzGroup(autoRealmTemplateId);
					templateRealmExists = true;
				} catch ( GroupNotDefinedException e ) {
					log.warn("Could not find default realm template {}={}.", PlusService.PLUS_NEW_SITE_REALM, autoRealmTemplateId);
				}
			}

			// Null might be just fine - it means use the realm from the site
			if ( ! templateRealmExists ) autoRealmTemplateId = null;

			if(StringUtils.isBlank(autoSiteTemplateId) || !templateSiteExists) {

				sakai_type = serverConfigurationService.getString(PlusService.PLUS_NEW_SITE_TYPE, PlusService.PLUS_NEW_SITE_TYPE_DEFAULT);
				if(StringUtils.isBlank(sakai_type)) {
					// It wasn't specced in the props. Test for the ims course context type.
					final String context_type = (String) payload.get(LTIConstants.CONTEXT_TYPE);
					if (LTIUtil.equalsIgnoreCase(context_type, "course")) {
						sakai_type = "course";
					} else {
						sakai_type = LTIConstants.NEW_SITE_TYPE;
					}
				}
				site = siteService.addSite(siteId, sakai_type);
				site.setType(sakai_type);
				log.debug("Creating siteId={} type={}", siteId, sakai_type);
			} else {
				Site autoSiteTemplate = siteService.getSite(autoSiteTemplateId);
				site = siteService.addSite(siteId, autoSiteTemplate, autoRealmTemplateId);
				log.debug("Creating siteId={} autoSiteTemplate={} autoRealmTemplateID={}", siteId, autoSiteTemplate.getId(), autoRealmTemplateId);
			}

			if (StringUtils.isNotBlank(context_title)) {
				site.setTitle(context_title);
			}
			if (StringUtils.isNotBlank(context_label)) {
				site.setShortDescription(context_label);
			}
			site.setJoinable(false);
			site.setPublished(true);
			site.setPubView(false);

			site.getPropertiesEdit().addProperty(PlusService.PLUS_PROPERTY, "true");

			String inBoundRoleMap = tenant.getInboundRoleMap();
			if ( StringUtils.isNotBlank(inBoundRoleMap) ) {
				log.debug("Custom inbound role mapping={}", inBoundRoleMap);
				site.getPropertiesEdit().addProperty(Site.PROP_LTI_INBOUND_ROLE_MAP, inBoundRoleMap);
			}

			try {
				siteService.save(site);
				log.info("Created  site={} label={} type={} title={}", siteId, context_label, sakai_type, context_title);
			} catch (Exception e) {
				throw new LTIException("launch.site.save", "siteId=" + siteId, e);
			}

			// Lets prime the Gradebook - SAK-49568
			try {
				Gradebook gb = gradingService.getGradebook(siteId, siteId);
				log.info("Gradebook site={} gb={}", siteId, gb);
			} catch (Exception e) {
				throw new LTIException("launch.site.gradebook", "siteId=" + siteId, e);
			}

		} catch (Exception e) {
			throw new LTIException("launch.create.site", "siteId=" + siteId, e);
		} finally {
			popAdvisor();
		}

		// Now lets retrieve that new site!
		try {
			return siteService.getSite(site.getId());
		} catch (IdUnusedException e) {
			throw new LTIException( "launch.site.invalid", "siteId="+siteId, e);

		}
	}

	private final void updateSiteDetailsIfChanged(Site site, String context_title, String context_label) {

		boolean changed = false;

		// Only copy title once
		if (StringUtils.isNotBlank(context_title) && LTIUtil.isBlank(site.getTitle()) ) {
			site.setTitle(context_title);
			changed = true;
		}

		// Only copy description once
		if (StringUtils.isNotBlank(context_label) && LTIUtil.isBlank(site.getShortDescription()) ) {
			site.setShortDescription(context_label);
			changed = true;
		}

		String plus_property = site.getProperties().getProperty(PlusService.PLUS_PROPERTY);
		if ( ! "true".equals(plus_property) ) {
			site.getPropertiesEdit().addProperty(PlusService.PLUS_PROPERTY, "true");
			changed = true;
		}

		if(changed) {
			try {
				siteService.save(site);
				log.info("Updated  site={} title={} label={}", site.getId(), context_title, context_label);
			} catch (Exception e) {
				log.warn("Failed to update site title and/or label");
			}
		}
	}

	private void loginUser(String ipAddress, User user) {
		Session sess = sessionManager.getCurrentSession();
		usageSessionService.login(user.getId(), user.getEid(), ipAddress, null, UsageSessionService.EVENT_LOGIN_WS);
		sess.setUserId(user.getId());
		sess.setUserEid(user.getEid());
	}

	// http://localhost:8080/plus/sakai/canvas-config.json?guid=123456
	// https://canvas.instructure.com/doc/api/file.navigation_tools.html
	private void handleCanvasConfig(HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		String guid = request.getParameter("guid");

		Optional<Tenant> optTenant = tenantRepository.findById(guid);
		Tenant tenant = null;
		if ( optTenant.isPresent() ) {
			tenant = optTenant.get();
		}

		if ( tenant == null ) {
			doError(request, response, "plus.tenant.notfound", guid, null);
			return;
		}
		JSONObject canvas = canvasDescriptor();

		String serverUrl = null;
		String host = null;
		String domain = null;
		try {
			serverUrl = SakaiLTIUtil.getOurServerUrl();
			URL netUrl = new URL(serverUrl);
			host = netUrl.getHost();
			domain = serverConfigurationService.getString(PlusService.PLUS_CANVAS_DOMAIN, host);
		} catch (MalformedURLException e) {
			doError(request, response, "canvas.error.missing.domain", e.getMessage(), e.getCause());
			return;
		}

		String title = tenant.getTitle();
		if ( StringUtils.isBlank(title) ) {
			title = serverConfigurationService.getString(PlusService.PLUS_CANVAS_TITLE, rb.getString(PlusService.PLUS_CANVAS_TITLE));
		}
		String description = tenant.getDescription();
		if ( StringUtils.isBlank(description) ) {
			description = serverConfigurationService.getString(PlusService.PLUS_CANVAS_DESCRIPTION, rb.getString(PlusService.PLUS_CANVAS_DESCRIPTION));
		}
		canvas.put("title", title);
		canvas.put("description", description);
		canvas.put("oidc_initiation_url", plusService.getOidcLogin(tenant));
		canvas.put("redirect_uris", plusService.getOidcLaunch());
		canvas.put("oidc_redirect_uris", plusService.getOidcLaunch());
		canvas.put("target_link_uri", plusService.getOidcLaunch());
		canvas.put("public_jwk_url", plusService.getOidcKeySet());
		JSONArray extensions = (JSONArray) canvas.get("extensions");
		JSONObject ext0 = (JSONObject) extensions.get(0);
		ext0.put("tool_id", guid);
		ext0.put("domain", domain);
		JSONObject settings = (JSONObject) ext0.get("settings");
		settings.put("icon_url", serverUrl + (String) settings.get("icon_url") );
		JSONArray placements = (JSONArray) settings.get("placements");

		for ( int i=0; i < placements.size(); i++) {
			JSONObject placement = (JSONObject) placements.get(i);
			placement.put("text", title);
			placement.put("icon_url", serverUrl + (String) placement.get("icon_url") );
			placement.put("target_link_uri", plusService.getPlusServletPath() );
		}

		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		out.print(canvas.toString());
		out.flush();
	}

	public JSONObject canvasDescriptor()
		throws java.io.IOException
	{
		InputStream stream = getServletContext().getResourceAsStream("/WEB-INF/descriptor.json");
		String text = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		JSONObject canvas = (JSONObject) JSONValue.parse(text);
		return canvas;
	}

	private String htmlEscape(String text)
	{
		return StringEscapeUtils.escapeHtml4(text);
	}

	// See also tsugi/lib/src/UI/Output.php
	private String togglePre(String title, String text)
	{
		String divId = (title+text).hashCode()+"";
		StringBuffer r = new StringBuffer();
		r.append("<p><strong>");
		r.append(htmlEscape(title));
		r.append(" (");
		r.append(text.length()+"");
		r.append(") ");
		r.append(rb.getString("plus.dynamic.characters"));
		r.append(" <a href=\"#\" onclick=\"var elem=document.getElementById('");
		r.append(divId);
		r.append("');if(elem.style.display=='block'){elem.style.display='none';}else{elem.style.display='block';}\" ");
		r.append(">");
		r.append(rb.getString("plus.dynamic.hide.show"));
		r.append("</a></strong></p>\n");
		r.append("<pre style=\"display:none; border: solid 1px\" id=\"");
		r.append(divId);
		r.append("\">");
		r.append(htmlEscape(text));
		r.append("\n</pre>\n");
		return r.toString();
	}

}

