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

/* This does not use the request filter because it needs full control of response headers.
	   But this also means that no work that should be in a session should be done in this servlet.
	   In particular, never use ThreadLocal in this servlet.
 */
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.beans.LtiToolBean;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.tsugi.lti.LTIUtil;
import org.tsugi.lti.ContentItem;
import org.sakaiproject.lti.util.SakaiLTIUtil;

import org.tsugi.lti13.LTI13Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.tsugi.util.Base64DoubleUrlEncodeSafe;
import org.tsugi.http.HttpUtil;

import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.ResourceLoader;

/**
 *
 */
@SuppressWarnings("deprecation")
@Slf4j
public class OIDCServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	protected static LTIService ltiService = null;

	private static ResourceLoader rb = new ResourceLoader("oidc");

	protected String cookieName = "JSESSIONID";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		if (System.getProperty(RequestFilter.SAKAI_COOKIE_PROP) != null) {
			cookieName = System.getProperty(RequestFilter.SAKAI_COOKIE_PROP);
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI(); // /imsoidc/lti13/oidc_auth
		// String launch_url = request.getParameter("launch_url");

		String[] parts = uri.split("/");

		// /imsoidc/lti13/oidc_auth?state=42&login_hint=/access/lti/site/92e..e8e67/content:6
		if (parts.length == 4 && "oidc_auth".equals(parts[3])) {
			handleOIDCAuthorization(request, response);
			return;
		}

		// /imsoidc/lti13/resigncontentitem?forward=http://localhost:8080/...
		if (parts.length == 4 && "resigncontentitem".equals(parts[3])) {
			handleResignContentItemResponse(request, response);
			return;
		}

		// /imsoidc/lti13/cookiecheck
		if (parts.length == 4 && "cookiecheck".equals(parts[3])) {
			handleCookieCheck(request, response);
			return;
		}

		log.error("Unrecognized GET request parts={} request={}", parts.length, uri);

		LTI13Util.return400(response, "Unrecognized GET request parts=" + parts.length + " request=" + uri);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doGet(request, response);
	}

	/**
	 * Process the returned OIDC Authorization request
	 *
	 * @param request
	 * @param response
	 */
	private void handleOIDCAuthorization(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String state = (String) request.getParameter("state");
		state = StringUtils.trimToNull(state);

		String redirect_uri = (String) request.getParameter("redirect_uri");
		redirect_uri = StringUtils.trimToNull(redirect_uri);

		String encoded_login_hint = (String) request.getParameter("login_hint");
		// Use Base64DoubleUrlEncodeSafe to properly handle URL encoding issues
		String login_hint = Base64DoubleUrlEncodeSafe.decodeDoubleSafe(encoded_login_hint);
		if (StringUtils.isEmpty(login_hint)) {
			state = null;
		}

		String nonce = (String) request.getParameter("nonce");
		nonce = StringUtils.trimToNull(nonce);

		if (state == null || login_hint == null || nonce == null) {
			LTI13Util.return400(response, "Missing login_hint, nonce or state parameter");
			log.error("Missing login_hint or state parameter");
			return;
		}

		// /access/lti/site/477ded8b-2d67-4897-9e00-0afc4eb8ae20/content:7
		// /access/lti/site/477ded8b-2d67-4897-9e00-0afc4eb8ae20/content:7
		if (!(login_hint.startsWith(LTIService.LAUNCH_PREFIX) || login_hint.startsWith(LTIService.LAUNCH_PREFIX_LEGACY))
				|| dangerousCharacters(login_hint) ) {
			LTI13Util.return400(response, "Bad format for login_hint");
			log.error("Bad format for login_hint");
			return;
		}

		String redirect = login_hint;
		redirect += (redirect.contains("?") ? "&" : "?");
		redirect += "state=" + java.net.URLEncoder.encode(state);
		redirect += "&nonce=" + java.net.URLEncoder.encode(nonce);
		redirect += "&redirect_uri=" + java.net.URLEncoder.encode(redirect_uri);
		log.debug("redirect={}", redirect);

		fancyRedirect(request, response, redirect);
	}

	/**
	 * Determine if a url contains dangerous characters
	 */
	private boolean dangerousCharacters(String url)
	{
		return ( url.contains("\"") || url.contains("'") || url.contains("<") || url.contains(">")
				|| url.contains(" ") || url.contains(";") || url.contains("\n") );
	}

	/**
	 * Redirect to a URL compensating for various scenarios
	 *
	 * @param request
	 * @param response
	 */
	private void fancyRedirect(HttpServletRequest request, HttpServletResponse response, String redirect) throws IOException {

		// Check if we need to generate a page to re-attach the cookie
		String sessionCookie = HttpUtil.getCookie(request, cookieName);
		if (StringUtils.isEmpty(sessionCookie)) {
			PrintWriter out = null;
			try {
				out = response.getWriter();
				response.setContentType("text/html");
				out.println("<script>window.location.href=\"" + redirect + "\";</script>");
				out.println("<p>...</p>");
				out.print("<p><a href=\"" + redirect + "\" style=\"display: none;\" id=\"linker\">");
				out.print(rb.getString("oidc.continue"));
				out.println("</a></p>");
				out.println("<script>setTimeout(function(){ document.getElementById('linker').style.display = 'inline'}, 1000);</script>");
			} catch (IOException e) {
				log.error("Error in fancyRedirect", e);
				LTI13Util.return400(response, "Redirect failed");
				return;
			}
		}

		try {
			response.sendRedirect(redirect);
		} catch (IOException unlikely) {
			log.error("failed redirect {}", unlikely.getMessage());
			LTI13Util.return400(response, "Redirect failed " + unlikely.getMessage());

		}
	}

	/**
	 * Forward a GET or POST from a page that we generate so the cookie gets re-associated
	 *
	 * @param request
	 * @param response
	 */
	private void handleResignContentItemResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String forward = Base64DoubleUrlEncodeSafe.decode((String) request.getParameter("forward"));
		forward = StringUtils.trimToNull(forward);

		Long toolKey = LTIUtil.toLongKey((String) request.getParameter("tool_id"));
		if ( StringUtils.isBlank(forward) || toolKey == null ) {
			LTI13Util.return400(response, "Missing forward or tool_id value");
			log.error("Missing forward or tool_id value");
			return;
		}

		String serverUrl = ServerConfigurationService.getServerUrl();
		if ( ! forward.startsWith(serverUrl) ) {
			LTI13Util.return400(response, "Must forward internally");
			log.error("Must forward internally");
			return;
		}

		// No tricky business
		if ( dangerousCharacters(forward) ) {
			LTI13Util.return400(response, "Bad format for forward");
			log.error("Bad format for forward");
			return;
		}

		// If this is not an LTI 1.1 message, no need to check signature and re-sign
		String oauth_consumer_key = request.getParameter("oauth_consumer_key");
		if ( StringUtils.isBlank(oauth_consumer_key) ) {
			PrintWriter out = null;
			try {
				out = response.getWriter();
				response.setContentType("text/html");
				out.print("<form method=\"post\" id=\"forwardform\" action=\"");
				out.print(forward);
				out.print("\">\n");

				Map<String, String[]> parameters = request.getParameterMap();
				for (Map.Entry<String, String[]> entry : parameters.entrySet())
				{
					if ( dangerousCharacters(entry.getKey()) ) continue;
					if ( StringUtils.equals("forward", entry.getKey()) ) continue;
					if ( StringUtils.equals("tool_id", entry.getKey()) ) continue;
					String[] values = entry.getValue();
					if ( values.length != 1 ) continue;
					out.print("<input type=\"hidden\" name=\"");
					out.print(entry.getKey());
					out.print("\" value=\"");
					out.print(StringEscapeUtils.escapeHtml4(values[0]));
					out.print("\">\n");
				}
				out.println("</form>");
				doCookieCheck(out, serverUrl);
			} catch (IOException e) {
				LTI13Util.return400(response, "Forward failure "+e.getMessage());
				log.error(e.getMessage(), e);
			}
			return;
		}

		// For an LTI 1.1 response, check the signature and if it passes, re-sign and forward
		// to the new URL
		ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
		LtiToolBean tool = ltiService.getToolDaoAsBean(toolKey, null, true);

		if ( tool == null ) {
			LTI13Util.return400(response, "Invalid tool_id");
			log.error("Invalid tool_id");
			return;
		}

		ContentItem contentItem = null;
		try {
			contentItem = new ContentItem(request);
		} catch (Exception e) {
			LTI13Util.return400(response, "Invalid content item: "+e.getMessage());
			log.error("Invalid content item: {}", e.getMessage());
			return;
		}

		String oauth_secret = SakaiLTIUtil.getSecret(tool);
		oauth_secret = SakaiLTIUtil.decryptSecret(oauth_secret);

		String URL = SakaiLTIUtil.getOurServletPath(request);

		if (!contentItem.validate(oauth_consumer_key, oauth_secret, URL)) {
			log.warn("Provider failed to validate message: {}", contentItem.getErrorMessage());
			String base_string = contentItem.getBaseString();
			if (base_string != null) {
				log.warn("base_string={}", base_string);
			}
			LTI13Util.return400(response, "Bad signature");
			log.error("Bad signature");
			return;
		}

		Properties ltiProps = new Properties();
		Map<String, String[]> parameters = request.getParameterMap();
		for (Map.Entry<String, String[]> entry : parameters.entrySet())
		{
			if ( dangerousCharacters(entry.getKey()) ) continue;
			if ( StringUtils.equals("forward", entry.getKey()) ) continue;
			if ( StringUtils.equals("tool_id", entry.getKey()) ) continue;
			if ( entry.getKey().startsWith("oauth_") ) continue;
			String[] values = entry.getValue();
			if ( values.length != 1 ) continue;
			ltiProps.setProperty(entry.getKey(), values[0]);
		}

		Map<String, String> extra = new HashMap<>();
		extra.put(LTIUtil.EXTRA_ERROR_TIMEOUT, rb.getString("oidc.continue"));
		extra.put(LTIUtil.EXTRA_HTTP_POPUP, LTIUtil.EXTRA_HTTP_POPUP_FALSE);  // Don't bother opening in new window in protocol mismatch
		extra.put(LTIUtil.EXTRA_FORM_ID, "forwardform");

		ltiProps = LTIUtil.signProperties(ltiProps, forward, "POST", oauth_consumer_key, oauth_secret, extra);

		String launchtext = rb.getString("oidc.continue");
		boolean autosubmit = false; // We will submit after checking the session cookie
		boolean dodebug = serverUrl.startsWith("http://localhost");
		String postData = LTIUtil.postLaunchHTML(ltiProps, forward, launchtext, autosubmit, dodebug, extra);

		PrintWriter out = null;
		try {
			out = response.getWriter();
			response.setContentType("text/html");
			out.print(postData);
			doCookieCheck(out, serverUrl);
		} catch (IOException e) {
			LTI13Util.return400(response, "Forward failure "+e.getMessage());
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Check if the request came in with a session cookie - we only return a substring so as not to reveal the actual session
	 *
	 * @param request
	 * @param response
	 */
	private void handleCookieCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String sessionCookie = HttpUtil.getCookie(request, cookieName);

		PrintWriter out = null;
		try {
			out = response.getWriter();
			response.setContentType("application/json");
			if ( StringUtils.isBlank(sessionCookie) || dangerousCharacters(sessionCookie) ) {
				out.println("{ \"cookie_prefix\" : null }");
			} else {
				out.print("{ \"cookie_prefix\" : \"");
				out.print(sessionCookie.substring(0,4));
				out.println("\"}");
			}
		} catch (IOException e) {
			LTI13Util.return400(response, "Forward failure "+e.getMessage());
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Add the browser code to check if we have re-established the session cookie
	 *
	 * @param out The current PrintWriter
	 */
	private void doCookieCheck(PrintWriter out, String serverUrl) {
		out.print("<button style=\"display: none;\" id=\"submitbutton\" onclick=\"document.getElementById('forwardform').submit();return false;\">");
		out.print(rb.getString("forward.continue"));
		out.println("</button>");
		out.print("<script>fetch('");
		out.print(serverUrl);
		out.println("/imsoidc/lti13/cookiecheck', { method: 'POST', credentials: 'same-origin' } ).then((response) => {");
		out.println("console.log(response);");
		out.println("return response.json();}).then((data) => {");
		out.println("console.log(data);");
		out.println("let cookie_prefix = data.cookie_prefix;");
		out.println("if ( typeof cookie_prefix == 'string' && cookie_prefix.length > 1 ) {");
		out.println("  console.log('We seem to have a session cookie in this page...');");
		out.println("  document.getElementById('forwardform').submit();");
		out.println("} else {");
		out.println("   console.log('It seems as though we have no cookie, enable user action...');");
		out.println("}");
		out.println("});");
		out.println("</script>");
		out.println("<script>setTimeout(function(){ document.getElementById('submitbutton').style.display = 'inline'}, 1000);</script>");
	}
}