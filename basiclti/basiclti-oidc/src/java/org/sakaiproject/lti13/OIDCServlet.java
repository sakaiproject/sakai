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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.lti.api.LTIService;

import org.tsugi.lti13.LTI13Util;
import org.apache.commons.lang.StringUtils;
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
		if (parts.length == 4 && "oidc_auth".equals(parts[3]) ) {
			handleOIDCAuthorization(request, response);
			return;
		}

		log.error("Unrecognized GET request parts={} request={}", parts.length, uri);

		LTI13Util.return400(response, "Unrecognized GET request parts="+parts.length+" request="+uri);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doGet(request, response);
	}

	/**
	 * Process the returned OIDC Authorization request
	 * @param signed_placement
	 * @param lineItem - Can be null
	 * @param results
	 * @param request
	 * @param response
	 */
	private void handleOIDCAuthorization(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String state = (String) request.getParameter("state");
		state = StringUtils.trimToNull(state);

		String login_hint = (String) request.getParameter("login_hint");
		if ( StringUtils.isEmpty(login_hint) ) state = null;

		String nonce = (String) request.getParameter("nonce");
		nonce = StringUtils.trimToNull(nonce);

		if ( state == null || login_hint == null || nonce == null ) {
			LTI13Util.return400(response, "Missing login_hint, nonce or state parameter");
			log.error("Missing login_hint or state parameter");
			return;
		}

		if ( ! login_hint.startsWith("/access/basiclti/site/") || 
			login_hint.contains("\"") || login_hint.contains("'") ||
			login_hint.contains("<") || login_hint.contains(">") ||
			login_hint.contains(" ") || login_hint.contains(";") )
		{
			LTI13Util.return400(response, "Bad format for login_hint");
			log.error("Bad format for login_hint");
			return;
		}

		String redirect = login_hint;
		redirect += ( redirect.contains("?") ? "&" : "?");
		redirect += "state=" + java.net.URLEncoder.encode(state);
		redirect += "&nonce=" + java.net.URLEncoder.encode(nonce);
		log.debug("redirect={}", redirect);

		// Check if we need to generate a page to re-grab the cookie
		String sessionCookie = HttpUtil.getCookie(request, cookieName);
		if ( StringUtils.isEmpty(sessionCookie) ) {
			PrintWriter out = null;
            		try {
                		out = response.getWriter();
				out.println("<script>window.location.href=\""+redirect+"\";</script>");
				out.println("<p>...</p>");
				out.print("<p><a href=\""+redirect+"\" style=\"display: none;\" id=\"linker\">");
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
			LTI13Util.return400(response, "Redirect failed "+unlikely.getMessage());

		}
	}

}
