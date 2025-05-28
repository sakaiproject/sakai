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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.pac4j.core.config.Config;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.jee.http.adapter.JEEHttpActionAdapter;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * PAC4J authentication filter that integrates with Sakai's session management.
 * Replaces the SakaiCasAuthenticationFilter with PAC4J-based authentication.
 */
@Slf4j
public class SakaiPac4jAuthenticationFilter extends OncePerRequestFilter {

    private Config config;
    private String clients;
    private String authorizers;
    private String matchers;
    private Boolean multiProfile;
    private SecurityLogic securityLogic;

    public SakaiPac4jAuthenticationFilter(Config config) {
        this.config = config;
        this.securityLogic = new DefaultSecurityLogic();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Check if user is already logged into Sakai
        if (isLoggedIntoSakai()) {
            log.debug("User already logged into Sakai, continuing filter chain");
            filterChain.doFilter(request, response);
            return;
        }

        // Create PAC4J context
        final JEESessionStore sessionStore = new JEESessionStore();
        final JEEContext context = new JEEContext(request, response);
        final JEEHttpActionAdapter httpActionAdapter = JEEHttpActionAdapter.INSTANCE;

        // Perform PAC4J security check
        securityLogic.perform(context, sessionStore, config, (ctx, store, profiles, parameters) -> {
            log.debug("PAC4J authentication successful");
            try {
                filterChain.doFilter(request, response);
            } catch (IOException | ServletException e) {
                throw new RuntimeException(e);
            }
            return null;
        }, httpActionAdapter, clients, authorizers, matchers, multiProfile);
    }

    private boolean isLoggedIntoSakai() {
        Session session = SessionManager.getCurrentSession();
        if (session != null && session.getUserEid() != null) {
            log.debug("Currently logged into Sakai as: {}", session.getUserEid());
            return true;
        }
        log.debug("Currently not logged into Sakai");
        return false;
    }

    public void setClients(String clients) {
        this.clients = clients;
    }

    public void setAuthorizers(String authorizers) {
        this.authorizers = authorizers;
    }

    public void setMatchers(String matchers) {
        this.matchers = matchers;
    }

    public void setMultiProfile(Boolean multiProfile) {
        this.multiProfile = multiProfile;
    }

    public void setSecurityLogic(SecurityLogic securityLogic) {
        this.securityLogic = securityLogic;
    }
}