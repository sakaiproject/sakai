/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.charon.velocity;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.api.FormattedText;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Context Listener for the render engine.
 *
 * @author ieb
 * @since Sakai 2.4
 */
@Slf4j
public class PortalRenderEngineContextListener implements ServletContextListener {
    private FormattedText formattedText;
    private PortalService portalService;
    private ServerConfigurationService serverConfigurationService;
    private SessionManager sessionManager;
    private VelocityPortalRenderEngine vengine;

    public PortalRenderEngineContextListener() {
        this.formattedText = ComponentManager.get(FormattedText.class);
        this.portalService = ComponentManager.get(PortalService.class);
        this.serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
        this.sessionManager = ComponentManager.get(SessionManager.class);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        portalService.removeRenderEngine(Portal.DEFAULT_PORTAL_CONTEXT, vengine);
    }

    public void contextInitialized(ServletContextEvent sce) {
        try {
            vengine = new VelocityPortalRenderEngine();
            vengine.setContext(sce.getServletContext());
            vengine.setServerConfigurationService(serverConfigurationService);
            vengine.setSessionManager(sessionManager);
            vengine.setFormattedText(formattedText);
            vengine.setPortalService(portalService);
            vengine.init();
            portalService.addRenderEngine(Portal.DEFAULT_PORTAL_CONTEXT, vengine);
        } catch (Exception e) {
            log.error("Failed to register render engine with the portal service, this is probably fatal: {}", e.toString());
            log.debug(e.getMessage(), e);
        }
    }
}
