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
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.commons.codec.binary.Base64;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.lti.api.LTIService;

import org.tsugi.lti13.LTI13Util;
import org.apache.commons.lang3.StringUtils;
import org.tsugi.http.HttpUtil;

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

	// TODO: Come up with a better way to handle this in RequestFilter
	protected String cookieName = "JSESSIONID";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI(); // /imsoidc/lti13/oidc_auth
		// String launch_url = request.getParameter("launch_url");

		String[] parts = uri.split("/");

		// /imsoidc/lti13/oidc_auth?state=42&login_hint=/access/basiclti/site/92e..e8e67/content:6
		if (parts.length == 4 && "oidc_auth".equals(parts[3])) {
			handleOIDCAuthorization(request, response);
			return;
		}

		// /imsoidc/lti13/lti112?tool_state=42&platform_state=/access/basiclti/site/92e..e8e67/content:6
		if (parts.length == 4 && "lti112".equals(parts[3])) {
			handleLTI112Authorization(request, response);
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
                byte[] valueDecoded = Base64.decodeBase64(encoded_login_hint);
                String login_hint = new String(valueDecoded);
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

		if (!login_hint.startsWith("/access/basiclti/site/")
				|| login_hint.contains("\"") || login_hint.contains("'")
				|| login_hint.contains("<") || login_hint.contains(">")
				|| login_hint.contains(" ") || login_hint.contains(";")) {
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
	 * Process the returned LTI 1.1.2 Authorization request
	 *
	 * @param request
	 * @param response
	 */
	private void handleLTI112Authorization(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String tool_state = (String) request.getParameter("tool_state");
		tool_state = StringUtils.trimToNull(tool_state);

		String platform_state = (String) request.getParameter("platform_state");
		platform_state = StringUtils.trimToNull(platform_state);

		if (tool_state == null || platform_state == null) {
			LTI13Util.return400(response, "Missing tool_state or platform_state parameter");
			log.error("Missing tool_state or platform_state parameter");
			return;
		}

		if (!platform_state.startsWith("/access/basiclti/site/")
				|| platform_state.contains("\"") || platform_state.contains("'")
				|| platform_state.contains("<") || platform_state.contains(">")
				|| platform_state.contains(" ") || platform_state.contains(";")) {
			LTI13Util.return400(response, "Bad format for platform_state");
			log.error("Bad format for platform_state");
			return;
		}

		String redirect = platform_state;
		redirect += (redirect.contains("?") ? "&" : "?");
		redirect += "tool_state=" + java.net.URLEncoder.encode(tool_state);
		log.debug("redirect={}", redirect);

		fancyRedirect(request, response, redirect);
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
				out.println("<script>window.location.href=\"" + redirect + "\";</script>");
				out.println("<p>...</p>");
				out.print("<p><a href=\"" + redirect + "\" style=\"display: none;\" id=\"linker\">");
				out.print(rb.getString("oidc.continue"));
				out.println("</a></p>");
				out.println("<script>setTimeout(function(){ document.getElementById('linker').style.display = 'inline'}, 1000);</script>");
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			return;
		}

		try {
			response.sendRedirect(redirect);
		} catch (IOException unlikely) {
			log.error("failed redirect {}", unlikely.getMessage());
			LTI13Util.return400(response, "Redirect failed " + unlikely.getMessage());

		}
	}

}
