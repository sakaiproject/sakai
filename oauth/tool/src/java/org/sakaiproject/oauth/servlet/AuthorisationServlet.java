/*
 * #%L
 * OAuth Tool
 * %%
 * Copyright (C) 2009 - 2013 The Sakai Foundation
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.sakaiproject.oauth.servlet;

import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.*;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.oauth.domain.Accessor;
import org.sakaiproject.oauth.domain.Consumer;
import org.sakaiproject.oauth.exception.OAuthException;
import org.sakaiproject.oauth.service.OAuthHttpService;
import org.sakaiproject.oauth.service.OAuthService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Colin Hebert
 */
public class AuthorisationServlet extends HttpServlet {
    /**
     * Name of the "authorise" button in the authorisation page.
     */
    public static final String AUTHORISE_BUTTON = "authorise";
    /**
     * Name of the "deny" button in the authorisation page.
     */
    public static final String DENY_BUTTON = "deny";
    private static final String LOGIN_PATH = "/login";
    private static final String SAKAI_LOGIN_TOOL = "sakai.login";

    // Services and settings
    private OAuthService oAuthService;
    private OAuthHttpService oAuthHttpService;
    private UserDirectoryService userDirectoryService;
    private SessionManager sessionManager;
    private ActiveToolManager activeToolManager;
    private ServerConfigurationService serverConfigurationService;
    private String authorisePath;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ComponentManager componentManager = org.sakaiproject.component.cover.ComponentManager.getInstance();
        oAuthService = (OAuthService) componentManager.get(OAuthService.class);
        oAuthHttpService = (OAuthHttpService) componentManager.get(OAuthHttpService.class);
        sessionManager = (SessionManager) componentManager.get(SessionManager.class);
        activeToolManager = (ActiveToolManager) componentManager.get(ActiveToolManager.class);
        userDirectoryService = (UserDirectoryService) componentManager.get(UserDirectoryService.class);
        serverConfigurationService =
                (ServerConfigurationService) componentManager.get(ServerConfigurationService.class);
        // TODO: get this path from the configuration (injection?)
        authorisePath = "/authorise.jsp";
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        handleRequest(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        handleRequest(request, response);
    }

    /**
     * Filter users trying to obtain an OAuth authorisation token.
     * <p/>
     * Three outcomes are possible:
     * <ul>
     * <li>The user isn't logged in the application: He's sent toward the login tool</li>
     * <li>The user is logged in but hasn't filled the authorisation form: He's sent toward the form</li>
     * <li>The user has filled the form: The OAuth system attenpts to grant a token</li>
     * </ul>
     *
     * @param request  current servlet request
     * @param response current servlet response
     * @throws IOException
     * @throws ServletException
     */
    private void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String pathInfo = request.getPathInfo();
        String currentUserId = sessionManager.getCurrentSessionUserId();
        if (currentUserId == null || (pathInfo != null && pathInfo.startsWith(LOGIN_PATH)))
            // Redirect non logged-in users and currently logging-in users requests
            sendToLoginPage(request, response);

        else if (request.getParameter(AUTHORISE_BUTTON) == null && request.getParameter(DENY_BUTTON) == null)
            // If logged-in but haven't yet authorised (or denied)
            sendToAuthorisePage(request, response);

        else
            // Even if the authorisation has been denied, send the client to the consumer's callback
            handleRequestAuth(request, response);
    }

    private void sendToLoginPage(HttpServletRequest request, HttpServletResponse response) throws ToolException {
        // If not logging-in, set the return path and proceed to the login steps
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.startsWith(LOGIN_PATH)) {
            Session session = sessionManager.getCurrentSession();

            // Set the return path for after login if needed
            // (Note: in session, not tool session, special for Login helper)
            StringBuffer returnUrl = request.getRequestURL();
            if (request.getQueryString() != null)
                returnUrl.append('?').append(request.getQueryString());
            session.setAttribute(Tool.HELPER_DONE_URL, returnUrl.toString());
        }

        // Redirect to the login tool
        ActiveTool loginTool = activeToolManager.getActiveTool(SAKAI_LOGIN_TOOL);
        String context = request.getContextPath() + request.getServletPath() + LOGIN_PATH;
        loginTool.help(request, response, context, LOGIN_PATH);
    }

    /**
     * Pre-set request attributes to provide additional information on the authorisation form.
     *
     * @param request  current servlet request
     * @param response current servlet response
     * @throws IOException
     * @throws ServletException
     */
    private void sendToAuthorisePage(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Accessor accessor = null;
        try {
            accessor = oAuthService.getAccessor(request.getParameter("oauth_token"), Accessor.Type.REQUEST);
            Consumer consumer = oAuthService.getConsumer(accessor.getConsumerId());
            accessor = oAuthService.startAuthorisation(accessor.getToken());

            User user = userDirectoryService.getCurrentUser();
            request.setAttribute("userName", user.getDisplayName());
            request.setAttribute("userId", user.getDisplayId());
            request.setAttribute("uiName", serverConfigurationService.getString("ui.service", "Sakai"));
            request.setAttribute("skinPath", serverConfigurationService.getString("skin.repo", "/library/skin"));
            request.setAttribute("defaultSkin", serverConfigurationService.getString("skin.default", "default"));
            request.setAttribute("authorise", AUTHORISE_BUTTON);
            request.setAttribute("deny", DENY_BUTTON);
            request.setAttribute("oauthVerifier", accessor.getVerifier());
            request.setAttribute("appName", consumer.getName());
            request.setAttribute("appDesc", consumer.getDescription());
            request.setAttribute("token", accessor.getToken());

            request.getRequestDispatcher(authorisePath).forward(request, response);
        } catch (OAuthException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    /**
     * Pre-set request attributes to provide additional information to the token creation process.
     *
     * @param request  current servlet request
     * @param response current servlet response
     * @throws IOException
     * @throws ServletException
     */
    private void handleRequestAuth(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        boolean authorised = request.getParameter(AUTHORISE_BUTTON) != null
                && request.getParameter(DENY_BUTTON) == null;
        String token = request.getParameter("oauthToken");
        String verifier = request.getParameter("oauthVerifier");
        String userId = sessionManager.getCurrentSessionUserId();

        oAuthHttpService.handleRequestAuthorisation(request, response, authorised, token, verifier, userId);
    }
}
