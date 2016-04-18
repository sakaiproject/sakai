/**
 * $Id$
 * $URL$
 * Example.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.entitybroker.providers.EntityRequestHandler;
import org.sakaiproject.entitybroker.rest.EntityBrokerRESTServiceManager;
import org.sakaiproject.entitybroker.util.servlet.DirectServlet;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.BasicAuth;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;

import lombok.extern.slf4j.Slf4j;

/**
 * Direct servlet allows unfettered access to entity URLs within Sakai, it also handles
 * authentication (login) if required (without breaking an entity URL)<br/>
 * This primarily differs from the access servlet in that it allows posts to work 
 * and removes most of the proprietary checks
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Sakai Software Development Team
 */
@Slf4j
@SuppressWarnings("deprecation")
public class SakaiDirectServlet extends DirectServlet {

    private static final long serialVersionUID = 1L;
    private transient EntityBrokerRESTServiceManager entityRESTServiceManager;
    private transient BasicAuth basicAuth;

    @Override
    public void initialize() {
        super.initialize();
        try {
            basicAuth = new BasicAuth();
            basicAuth.init();
        } catch (Exception e) {
            throw new IllegalStateException("FAILURE during init direct servlet", e);
        }
    }

    @Override
    public String getCurrentLoggedInUserId() {
        return SessionManager.getCurrentSessionUserId();
    }

    @Override
    public EntityRequestHandler initializeEntityRequestHandler() {
        // fire up the EB rest services
        EntityBrokerManager ebm = (EntityBrokerManager) ComponentManager.get(EntityBrokerManager.class.getName());
        // for legacy support
        HttpServletAccessProviderManager hsapm = (HttpServletAccessProviderManager) 
        ComponentManager.get(HttpServletAccessProviderManager.class.getName());
        entityRESTServiceManager = new EntityBrokerRESTServiceManager(ebm, hsapm);
        EntityRequestHandler erh = entityRESTServiceManager.getEntityRequestHandler();
        if (erh == null) {
            throw new RuntimeException("FAILED to load EntityRequestHandler");
        }
        return erh;
    }

    @Override
    public void handleUserLogin(HttpServletRequest req, HttpServletResponse res, String path) {
        // attempt basic auth first
        try {
            if (basicAuth.doAuth(req, res)) {
                return;
            }
        } catch (IOException ioe) {
            throw new RuntimeException("IO Exception intercepted during logon ", ioe);
        }

        // get the Sakai session (using the cover)
        Session session = SessionManager.getCurrentSession();

        // set the return path for after login if needed
        // (Note: in session, not tool session, special for Login helper)
        boolean helperURLSet = false;
        if (path != null) {
            // defines where to go after login succeeds
            helperURLSet = true;
            String returnURL = Web.returnUrl( req, Validator.escapeUrl(path) );
            log.info("Direct Login: Setting session ("+session.getId()+") helper URL ("+Tool.HELPER_DONE_URL+") to "+returnURL);
            session.setAttribute(Tool.HELPER_DONE_URL, returnURL);
        }

        // check that we have a return path set; might have been done earlier
        if (! helperURLSet && session.getAttribute(Tool.HELPER_DONE_URL) == null) {
            session.setAttribute(Tool.HELPER_DONE_URL, "/direct/describe");
            log.info("doLogin - no HELPER_DONE_URL found, proceeding with default HELPER_DONE_URL: " + "/direct/describe");
        }

        // map the request to the helper, leaving the path after ".../options" for the helper
        ActiveTool tool = ActiveToolManager.getActiveTool("sakai.login");
        String context = req.getContextPath() + req.getServletPath() + "/login";
        try {
            tool.help(req, res, context, "/login");
        } catch (ToolException e) {
            throw new RuntimeException("Failure attempting to use Sakai login helper: " + e.getMessage(), e);
        }
    }

    @Override
    public void dispatch(HttpServletRequest req, HttpServletResponse res) throws ServletException {
        // intercept this and try to do the sakai basic auth
        try {
            // NOTE: should this only run when the user is not authorized? It currently allows basic auth to override existing auth -AZ
            basicAuth.doLogin(req);
        } catch (IOException ioe) {
            throw new RuntimeException("IO Exception intercepted during basic auth: " + ioe, ioe);
        }

        // continue on to the standard dispatch method
        super.dispatch(req, res);
    }

}
