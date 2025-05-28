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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.pac4j.core.config.Config;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.jee.http.adapter.JEEHttpActionAdapter;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * PAC4J authentication entry point for Spring Security integration.
 * Initiates the authentication process when unauthenticated users access protected resources.
 */
@Slf4j
public class SakaiPac4jEntryPoint implements AuthenticationEntryPoint {

    @Setter private Config config;
    @Setter private String clientName = "";
    @Setter private String authorizers = "";
    @Setter private String matchers = "";

    private SecurityLogic securityLogic = new DefaultSecurityLogic();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                        AuthenticationException authException) throws IOException, ServletException {
        
        log.debug("Starting PAC4J authentication process for request: {}", request.getRequestURI());
        
        if (config == null) {
            log.error("PAC4J config is null, cannot proceed with authentication");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication configuration error");
            return;
        }

        try {
            final JEESessionStore sessionStore = new JEESessionStore();
            final JEEContext context = new JEEContext(request, response);
            final JEEHttpActionAdapter httpActionAdapter = JEEHttpActionAdapter.INSTANCE;

            securityLogic.perform(context, sessionStore, config, (ctx, store, profiles, parameters) -> {
                log.debug("PAC4J authentication successful for user: {}", 
                         profiles != null && !profiles.isEmpty() ? profiles.iterator().next().getId() : "unknown");
                return null;
            }, httpActionAdapter, clientName, authorizers, matchers, false);
            
        } catch (Exception e) {
            log.error("Error during PAC4J authentication: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error");
        }
    }

    public void setSecurityLogic(SecurityLogic securityLogic) {
        this.securityLogic = securityLogic;
    }
}