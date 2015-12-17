/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/login/tags/sakai-10.5/login-tool/tool/src/java/org/sakaiproject/login/tool/Oauth2AuthenticationRequest.java $
 * $Id: Oauth2AuthenticationRequest.java fsaez@entornosdeformacion.com $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.login.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;


public class Oauth2AuthenticationRequest extends HttpServlet
{
	private static final long serialVersionUID = -3589514330633190919L;

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(Oauth2AuthenticationRequest.class);

	private transient ServerConfigurationService serverConfigurationService;

	private String defaultReturnUrl;

	private MessageDigest md;

	/**
	 * Initialize the servlet.
	 * 
	 * @param config
	 *        The servlet config.
	 * @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		M_log.info("init()");
		serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
		defaultReturnUrl = serverConfigurationService.getString("portalPath", "/portal");

		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) { 
			M_log.warn("No SHA-256 support: " + e.getMessage());
		}
	}

	/**
	 * Shutdown the servlet.
	 */
	public void destroy()
	{
		M_log.info("destroy()");

		super.destroy();
	}

	/**
	 * Respond to requests.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @throws ServletException.
	 * @throws IOException.
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// get the session
		Session session = SessionManager.getCurrentSession();

		try
		{
			if(Oauth2ServerConfiguration.isEnabled()) {
				String authorizationEndpointUri = Oauth2ServerConfiguration.getAuthorizationEndpointUri();
				if(authorizationEndpointUri != null && !"".equals(authorizationEndpointUri)) {
					StringBuilder sb = new StringBuilder();
					sb.append(authorizationEndpointUri);
					sb.append("?redirect_uri=");
					sb.append(serverConfigurationService.getPortalUrl().replace("/portal", "/sakai-login-tool/oauth2/login"));
					String scope = Oauth2ServerConfiguration.getScope();
					if(!StringUtils.isEmpty(scope)) {
						sb.append("&scope=");
						sb.append(java.net.URLEncoder.encode(scope, "UTF-8").replace("+", "%20"));
					}
					sb.append("&state=");
					sb.append(byteArray2Hex(md.digest(session.getId().getBytes("UTF-8"))));
					sb.append("&response_type=code&client_id=");
					sb.append(Oauth2ServerConfiguration.getClientId());
	
					M_log.debug("OAUTH2 redirecting to "+res.encodeRedirectURL(sb.toString()));
	
					res.sendRedirect(res.encodeRedirectURL(sb.toString()));
					return;
				}
			}

		}
		catch (Exception ex)
		{
			M_log.warn("Send redirect Failed: " + ex.getMessage(), ex);
		}

		session.setAttribute(SkinnableLogin.ATTR_CONTAINER_CHECKED, SkinnableLogin.ATTR_CONTAINER_CHECKED);
		res.sendRedirect(res.encodeRedirectURL(getUrl(session, SkinnableLogin.ATTR_RETURN_URL)));
	}

	/**
	 * Gets a URL from the session, if not found returns the portal URL.
	 * @param session The users HTTP session.
	 * @param sessionAttribute The attribute the URL is stored under.
	 * @return The URL.
	 */
	private String getUrl(Session session, String sessionAttribute) {
		String url = (String) session.getAttribute(sessionAttribute);
		if (StringUtils.isEmpty(url))
		{
			M_log.debug("No "+ sessionAttribute + " URL, redirecting to portal URL.");
			url = defaultReturnUrl;
		}
		return url;
	}

	private static String byteArray2Hex(byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}
}

