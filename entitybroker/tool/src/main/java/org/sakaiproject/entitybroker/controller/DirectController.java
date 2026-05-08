/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
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
 */
package org.sakaiproject.entitybroker.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.providers.EntityRequestHandler;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.BasicAuth;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Spring WebMVC controller that handles direct endpoint access.
 * Maps all paths under the WAR context and delegates to EntityRequestHandler,
 * preserving the same authentication, login-redirect, and error-handling
 * behaviour as the previous servlet-based implementation.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class DirectController {

    private final EntityRequestHandler entityRequestHandler;
    private final BasicAuth basicAuth;

    @RequestMapping("/**")
    public void handleDirect(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Set default content type
        res.setContentType(Formats.HTML_MIME_TYPE);
        res.setCharacterEncoding(Formats.UTF_8);

        // Sakai BasicAuth (was in SakaiDirectServlet.dispatch())
        basicAuth.doLogin(req);

        // Login path detection
        String uri = req.getRequestURI();
        if (uri != null) {
            String[] parts = uri.split("/");
            if (parts.length > 0 && "login".equals(parts[parts.length - 1])) {
                handleUserLogin(req, res, null);
                return;
            }
        }

        dispatch(req, res);
    }

    private void dispatch(HttpServletRequest req, HttpServletResponse res) {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            try {
                entityRequestHandler.handleEntityAccess(req, res, path);
            } catch (EntityException e) {
                log.info("Could not process entity: {} ({}) [{}]: {}",
                        e.entityReference, e.responseCode, e.getCause(), e.getMessage());
                sendError(res, e.responseCode, e.getMessage());
            }
        } catch (SecurityException e) {
            if (currentUserId() == null) {
                String queryPath = path + (req.getQueryString() == null ? "" : "?" + req.getQueryString());
                handleUserLogin(req, res, queryPath);
                return;
            }
            String msg = "Security exception accessing entity URL: " + path + ": " + e.getMessage();
            log.info(msg);
            sendError(res, HttpServletResponse.SC_FORBIDDEN, msg);
        } catch (Exception e) {
            String msg = entityRequestHandler.handleEntityError(req, e);
            log.warn("{}: {}", msg, e);
            sendError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
        }
    }

    /**
     * Returns the current logged-in user ID, or null if anonymous.
     * Protected to allow override in tests
     */
    protected String currentUserId() {
        return SessionManager.getCurrentSessionUserId();
    }

    private void handleUserLogin(HttpServletRequest req, HttpServletResponse res, String path) {
        // Attempt BasicAuth challenge first (from SakaiDirectServlet.handleUserLogin())
        try {
            if (basicAuth.doAuth(req, res)) return;
        } catch (IOException ioe) {
            throw new RuntimeException("IO Exception during logon", ioe);
        }

        Session session = SessionManager.getCurrentSession();
        boolean helperURLSet = false;
        if (path != null) {
            helperURLSet = true;
            String returnURL = Web.returnUrl(req, Validator.escapeUrl(path));
            log.info("Direct Login: setting helper URL to {}", returnURL);
            session.setAttribute(Tool.HELPER_DONE_URL, returnURL);
        }
        if (!helperURLSet && session.getAttribute(Tool.HELPER_DONE_URL) == null) {
            session.setAttribute(Tool.HELPER_DONE_URL, "/direct/describe");
        }

        ActiveTool tool = ActiveToolManager.getActiveTool("sakai.login");
        String context = req.getContextPath() + req.getServletPath() + "/login";
        try {
            tool.help(req, res, context, "/login");
        } catch (ToolException e) {
            throw new RuntimeException("Failure using Sakai login helper: " + e.getMessage(), e);
        }
    }

    private void sendError(HttpServletResponse res, int code, String message) {
        try {
            res.reset();
            res.sendError(code, message);
        } catch (Exception e) {
            log.warn("Error sending error code {} message {}: {}", code, message, e);
        }
    }
}
