/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/login/tags/sakai-10.5/login-tool/tool/src/java/org/sakaiproject/login/tool/Oauth2Login.java $
 * $Id: Oauth2Login.java fsaez@entornosdeformacion.com $
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
import java.util.Enumeration;
import java.util.Formatter;
import java.util.UUID;

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
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;

import com.google.gson.JsonObject;


public class Oauth2Login extends HttpServlet
{
	private static final long serialVersionUID = -3589514330633190919L;

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(Oauth2Login.class);
	
	private String defaultReturnUrl;

	private MessageDigest md;
	
	private transient ServerConfigurationService serverConfigurationService;
	private transient UserDirectoryService userDirectoryService;
	private String oauth2RedirectUri;

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
		userDirectoryService = (UserDirectoryService) ComponentManager.get(UserDirectoryService.class);
		defaultReturnUrl = serverConfigurationService.getString("portalPath", "/portal");
		oauth2RedirectUri = serverConfigurationService.getPortalUrl().replace("/portal", "/sakai-login-tool/oauth2/login");
		
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
			
				String state = req.getParameter("state");
				String code = req.getParameter("code");
				
				//check if returned state is equal to current session state
		        byte[] digest = md.digest(session.getId().getBytes("UTF-8"));
		        String stateFromSession = byteArray2Hex(digest);
		        
				// Oauth2 CSRF Check
				if (!stateFromSession.equals(state)) {
					M_log.warn("Oauth2 CSRF check failed for " + session.getUserEid() + ":" + state + " : " + stateFromSession);
					res.setStatus(400);
					return;
				}
				
				//use returned code to get an access token
				Oauth2TokenRequester tokenRequester = new Oauth2TokenRequester(Oauth2ServerConfiguration.getTokenEndpointUri());
				String accessTokenValue = tokenRequester.getOauth2Token(code, oauth2RedirectUri);
				
				//with the access token, get user info
				Oauth2UserInfoRequester userInfoRequester = new Oauth2UserInfoRequester(Oauth2ServerConfiguration.getUserInfoEndpointUri());
				JsonObject userInfoJson = userInfoRequester.getUserInfo(accessTokenValue);
				
				//extract user id from the returned user info.
				String userId = null;
				try {
					userId = userInfoJson.get(Oauth2ServerConfiguration.getProperty_userId()).getAsString();
				} catch(Exception ex) {
					M_log.warn("Error mapping user info :  " + Oauth2ServerConfiguration.getProperty_userId() + " does not exists in retrieved data");
				}
				
				User u = null;
	            try {
	            	//check if user exists in sakai
	            	if(userId != null)
	            		u = userDirectoryService.getUserByEid(userId);
	            }
	            catch (UserNotDefinedException ue) {
	            	//if not exists, should we create it?
					if (Oauth2ServerConfiguration.isCreateUser()) {
						try {
							String firstName = "";
							String lastName = "";
							try {
								if(Oauth2ServerConfiguration.getProperty_firstName() != null && Oauth2ServerConfiguration.getProperty_lastName() != null) {
									firstName = userInfoJson.get(Oauth2ServerConfiguration.getProperty_firstName()).getAsString();
									lastName = userInfoJson.get(Oauth2ServerConfiguration.getProperty_lastName()).getAsString();
								} else {
									String displayName = userInfoJson.get(Oauth2ServerConfiguration.getProperty_userName()).getAsString();
									String[] names = displayName.split(" ");
									firstName = names[0];
									for(int i = 1; i < names.length; i++) {
										if(!StringUtils.isEmpty(lastName)){
											lastName += " ";
										}
										lastName += names[i];
									}
								}
							} catch(Exception e) {
								M_log.warn("ERROR getting username for " + userId + ":" + e.getMessage());
							}
							
							String email = userInfoJson.get(Oauth2ServerConfiguration.getProperty_email()).getAsString();
							//GENERATE A RANDOM AND UNREACHABLE PASSWORD.
							byte[] pwDigest = md.digest(UUID.randomUUID().toString().getBytes("UTF-8"));
							String hiddenPW = byteArray2Hex(pwDigest);
						
							String userType = Oauth2ServerConfiguration.getUserType();
							
							u = userDirectoryService.addUser(null, userId, firstName, lastName, email, hiddenPW, userType, null);
							M_log.info("Oauth2 created new user: " + email + ":" + u.getEid() + ":" + u.getId());
						}catch(Exception e) {
							M_log.error("ERROR creating new user " + userId + ":" + e.getMessage());
						}
							
					}
					else {
	            		M_log.info("Oauth2 user does not exist in Sakai: " + userId);
					}
	            }
				
				// login the user
				if (u != null && UsageSessionService.login(u.getId(), u.getEid(), req.getRemoteAddr(), req.getHeader("user-agent"), UsageSessionService.EVENT_LOGIN_CONTAINER))
				{
					// get the return URL
					String url = getUrl(session, Tool.HELPER_DONE_URL);
	
					// cleanup session
					session.removeAttribute(Tool.HELPER_MESSAGE);
					session.removeAttribute(Tool.HELPER_DONE_URL);
					
					// needed if we want to revoke token when we logout
		            session.setAttribute("oauth2Token", accessTokenValue);
	
					// redirect to the done URL
					res.sendRedirect(res.encodeRedirectURL(url));
					return;
				}
			}
			
		}
		catch (Exception ex)
		{
			M_log.error("Authentication Failed: " + ex.getMessage(), ex);
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

