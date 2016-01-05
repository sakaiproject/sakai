/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/login/tags/sakai-10.5/login-tool/tool/src/java/org/sakaiproject/login/tool/Oauth2Logout.java $
 * $Id: Oauth2Logout.java fsaez@entornosdeformacion.com $
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
import java.io.InputStreamReader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.web.util.UriUtils;


public class Oauth2Logout extends HttpServlet {

	private static final long serialVersionUID = 9005106587134443216L;

	private static final Log M_log = LogFactory.getLog(Oauth2Logout.class);

	private ServerConfigurationService serverConfigurationService;
	private UsageSessionService usageSessionService;
	private SessionManager sessionManager;

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

		M_log.debug("init()");
		serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
		usageSessionService = (UsageSessionService) ComponentManager.get(UsageSessionService.class);
		sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);
	}

	/**
	 * Shutdown the servlet.
	 */
	public void destroy()
	{
		M_log.debug("destroy()");

		super.destroy();
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// get the session
		Session session = sessionManager.getCurrentSession();

		// Fetch the token from the seession
		String tokenData = (String) session.getAttribute("oauth2Token");

		if (!StringUtils.isEmpty(tokenData)) {
			try{
				M_log.debug("Oauth2 token for : " + session.getUserEid() + " : " + tokenData);

				HttpClient client = new HttpClient();
				HttpState state = client.getState();

				//set the revoke endpoint
				PostMethod method = new PostMethod(Oauth2ServerConfiguration.getRevokeEndpointUri());
				// post params
				method.addParameter("token", tokenData);
				method.addParameter("token_type_hint", "access_token");

				//TODO : accept other authentication types
				if(Oauth2ServerConfiguration.getAuthorizationType().equalsIgnoreCase(Oauth2ServerConfiguration.AUTH_TYPE_SECRET_BASIC)) {
					String authHeaderStr = String.format("Basic %s", new String(Base64.encodeBase64(String.format("%s:%s",
							UriUtils.encodePathSegment(Oauth2ServerConfiguration.getClientId(), "UTF-8"),
							UriUtils.encodePathSegment(Oauth2ServerConfiguration.getClientSecret(), "UTF-8")).getBytes()), "UTF-8"));
					method.addRequestHeader("Authorization", authHeaderStr);
				} else if(Oauth2ServerConfiguration.getAuthorizationType().equalsIgnoreCase(Oauth2ServerConfiguration.AUTH_TYPE_SECRET_POST)) {
					method.addParameter("client_id", Oauth2ServerConfiguration.getClientId());
					if(Oauth2ServerConfiguration.getClientSecret() != null && !"".equals(Oauth2ServerConfiguration.getClientSecret()))
						method.addParameter("client_secret", Oauth2ServerConfiguration.getClientSecret());
				}

				client.executeMethod( method );
				
				BufferedReader br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
				String readLine;
				StringBuilder sb = new StringBuilder();
				while(((readLine = br.readLine()) != null)) {
					sb.append(readLine);
				}

				String response = sb.toString();
				M_log.debug("REVOKE TOKEN response : "+response);
			}catch(Exception ex){
				M_log.error(String.format("Fatal error revoking the token for user %s (%s)",session.getUserEid(),ex));
			}
		}
		else {
			M_log.debug("No Oauth2 token for : " + session.getUserEid());
		}

		String returnUrl = Oauth2ServerConfiguration.getLogoutURL();

		// if we end up with nowhere to go, go to the portal
		if (returnUrl == null)
		{
			returnUrl = (String)session.getAttribute(Tool.HELPER_DONE_URL);
			if (StringUtils.isEmpty(returnUrl))
			{
				M_log.debug("complete: nowhere set to go, going to portal");
				returnUrl = serverConfigurationService.getPortalUrl();
			}
		}

		usageSessionService.logout();

		// redirect to the done URL
		res.sendRedirect(res.encodeRedirectURL(returnUrl));
	}
}

