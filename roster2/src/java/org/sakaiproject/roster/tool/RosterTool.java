/**
 * Copyright (c) 2010-2017 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational
* Community License, Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.roster.tool;

import java.io.IOException;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.sakaiproject.roster.api.SakaiProxy;
import org.sakaiproject.util.ResourceLoader;

/**
 * <code>RosterTool</code> performs basic checks and outputs a prebuilt startup
 * page.
 * 
 * @author Daniel Robinson (d.b.robinson@lancaster.ac.uk)
 * @author Adrian Fish (a.fish@lancaster.ac.uk)
 */
@Slf4j
public class RosterTool extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	private SakaiProxy sakaiProxy;

    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        try {
            ApplicationContext context
                = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
            sakaiProxy = (SakaiProxy) context.getBean("org.sakaiproject.roster.api.SakaiProxy");
        } catch (Throwable t) {
            throw new ServletException("Failed to initialise RosterTool servlet.", t);
        }
    }

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String userId = sakaiProxy.getCurrentUserId();

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
            rl = new ResourceLoader("org.sakaiproject.roster.i18n.ui");
            rl.setContextLocale(locale);
        } else {
            rl = new ResourceLoader(userId, "org.sakaiproject.roster.i18n.ui");
            locale = rl.getLocale();
        }

        if (locale == null || rl == null) {
            log.error("Failed to load the site or user i18n bundle");
        }

        String language = locale.getLanguage();
        String country = locale.getCountry();
            
        if (country != null && !country.equals("")) {
            language += "_" + country;
        }

		request.setAttribute("sakaiHtmlHead", (String) request.getAttribute("sakai.html.head"));
		request.setAttribute("userId", userId);
		request.setAttribute("state", sakaiProxy.getDefaultRosterStateString());
		request.setAttribute("siteId", sakaiProxy.getCurrentSiteId());
        request.setAttribute("language", language);
		request.setAttribute("defaultSortColumn", sakaiProxy.getDefaultSortColumn());
        request.setAttribute("firstNameLastName", sakaiProxy.getFirstNameLastName());
		request.setAttribute("hideSingleGroupFilter", sakaiProxy.getHideSingleGroupFilter());
        request.setAttribute("viewUserDisplayId", sakaiProxy.getViewUserDisplayId());
        request.setAttribute("viewUserProperty", sakaiProxy.getViewUserProperty());
        request.setAttribute("officialPicturesByDefault", sakaiProxy.getOfficialPicturesByDefault());
        request.setAttribute("viewEmail", sakaiProxy.getViewEmail());
		request.setAttribute("superUser", sakaiProxy.isSuperUser());
		request.setAttribute("siteMaintainer", sakaiProxy.isSiteMaintainer(sakaiProxy.getCurrentSiteId()));
        request.setAttribute("viewConnections", sakaiProxy.getViewConnections());
        request.setAttribute("showVisits", sakaiProxy.getShowVisits());

        response.setContentType("text/html");
        request.getRequestDispatcher("/WEB-INF/bootstrap.jsp").include(request, response);	
	}
}
