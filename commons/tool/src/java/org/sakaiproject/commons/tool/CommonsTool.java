/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.commons.tool;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.commons.api.CommonsConstants;
import org.sakaiproject.commons.api.CommonsManager;
import org.sakaiproject.commons.api.CommonsReferenceReckoner;
import org.sakaiproject.commons.api.SakaiProxy;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.ResourceLoader;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Adrian Fish (adrian.r.fish@gmail.com)
 */
@Slf4j
public class CommonsTool extends HttpServlet {

    private CommonsManager commonsManager;
    private SakaiProxy sakaiProxy;
    private ServerConfigurationService serverConfigurationService;

    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        log.debug("init");

        try {
            ComponentManager componentManager = org.sakaiproject.component.cover.ComponentManager.getInstance();
            sakaiProxy = (SakaiProxy) componentManager.get(SakaiProxy.class);
            commonsManager = (CommonsManager) componentManager.get(CommonsManager.class);
            serverConfigurationService = (ServerConfigurationService) componentManager.get(ServerConfigurationService.class);
        } catch (Throwable t) {
            throw new ServletException("Failed to initialise CommonsTool servlet.", t);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        log.debug("doGet()");

        String userId = null;
        Session session = (Session) request.getAttribute(RequestFilter.ATTR_SESSION);
        if (session != null) {
            userId = session.getUserId();
        } else {
            throw new ServletException("Not logged in.");
        }

        String siteLanguage = sakaiProxy.getCurrentSiteLocale();

        Locale locale = null;
        ResourceLoader rl = null;

        if (siteLanguage != null) {
            String[] parts = siteLanguage.split("_");
            if (parts.length == 1) {
                locale = new Locale(parts[0]);
            } else if (parts.length == 2) {
                locale = new Locale(parts[0], parts[1]);
            } else if (parts.length == 3) {
                locale = new Locale(parts[0], parts[1], parts[2]);
            }
            rl = new ResourceLoader("commons");
            rl.setContextLocale(locale);
        } else {
            rl = new ResourceLoader(userId, "commons");
            locale = rl.getLocale();
        }

        if (locale == null || rl == null) {
            log.error("Failed to load the site or user i18n bundle");
        }

        String language = locale.getLanguage();
        String country = locale.getCountry();

        if (StringUtils.isNotBlank(country)) {
            language += "_" + country;
        }

        String ref = request.getParameter("ref");
        if (StringUtils.isNotBlank(ref)) {
            String postId = CommonsReferenceReckoner.reckoner().reference(ref).reckon().getPostId();
            request.setAttribute("postId", postId);
        }

        request.setAttribute("sakaiHtmlHead", (String) request.getAttribute("sakai.html.head"));
        request.setAttribute("isolanguage", language);
        request.setAttribute("userId", userId);
        String siteId = sakaiProxy.getCurrentSiteId();
        request.setAttribute("siteId", siteId);
        boolean isUserSite = sakaiProxy.isUserSite(siteId);
        request.setAttribute("isUserSite", isUserSite);
        request.setAttribute("embedder", isUserSite ? CommonsConstants.SOCIAL : CommonsConstants.SITE);
        request.setAttribute("commonsId", isUserSite ? CommonsConstants.SOCIAL : siteId);
        String maxUploadSize = serverConfigurationService.getString("content.upload.max", "20");
        request.setAttribute("maxUploadSize", maxUploadSize);
        request.setAttribute("portalCDNQuery", PortalUtils.getCDNQuery());

        String pathInfo = request.getPathInfo();

        if (pathInfo != null ) {
            String[] pathParts = pathInfo.split("/");

            if (pathParts.length == 3 && pathParts[1].equals("posts")) {
                request.setAttribute("postId", pathParts[2]);
            }
        }

        response.setContentType("text/html");
        request.getRequestDispatcher("/WEB-INF/bootstrap.jsp").include(request, response);
    }
}
