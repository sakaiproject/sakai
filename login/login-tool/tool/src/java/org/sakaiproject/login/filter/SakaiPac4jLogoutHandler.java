/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.login.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.pac4j.core.config.Config;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.jee.http.adapter.JEEHttpActionAdapter;

import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * PAC4J logout handler that integrates with Sakai's session management.
 * Replaces the SakaiLogoutSamlFilter with PAC4J-based logout handling.
 */
@Slf4j
public class SakaiPac4jLogoutHandler extends SecurityContextLogoutHandler {

    @Setter private UsageSessionService usageSessionService;
    @Setter private SessionManager sessionManager;
    @Setter private Config config;

    @Getter @Setter private boolean invalidateSakaiSession = true;
    @Getter @Setter private String defaultUrl = "/portal";
    @Getter @Setter private String logoutUrlPattern = "/container/logout";

    private LogoutLogic logoutLogic = new DefaultLogoutLogic();

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // Perform standard Spring Security logout
        super.logout(request, response, authentication);

        // Perform Sakai session logout
        if (invalidateSakaiSession) {
            Session session = sessionManager.getCurrentSession();
            if (session != null) {
                log.debug("PAC4J logout invalidating Sakai session: {}", session.getId());
                usageSessionService.logout();
            }
        }

        // Perform PAC4J logout if config is available
        if (config != null) {
            try {
                final JEESessionStore sessionStore = new JEESessionStore();
                final JEEContext context = new JEEContext(request, response);
                final JEEHttpActionAdapter httpActionAdapter = JEEHttpActionAdapter.INSTANCE;
                
                logoutLogic.perform(context, sessionStore, config, httpActionAdapter, 
                                  defaultUrl, logoutUrlPattern, null, null, null);
                
                log.debug("PAC4J logout completed");
            } catch (Exception e) {
                log.warn("Error during PAC4J logout: {}", e.getMessage(), e);
            }
        }
    }

    public void setLogoutLogic(LogoutLogic logoutLogic) {
        this.logoutLogic = logoutLogic;
    }
}