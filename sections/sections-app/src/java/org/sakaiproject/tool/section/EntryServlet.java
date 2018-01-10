/**
 * Copyright (c) 2005-2016 The Apereo Foundation
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
package org.sakaiproject.tool.section;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import org.sakaiproject.section.api.facade.manager.Authn;
import org.sakaiproject.section.api.facade.manager.Authz;
import org.sakaiproject.section.api.facade.manager.Context;
import org.sakaiproject.section.api.SectionManager;

@Slf4j
public class EntryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, java.io.IOException {
		doGet(req, resp);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
        if(log.isDebugEnabled()) log.debug("Entering sections tool... determining role appropriate view");

        ApplicationContext ac = (ApplicationContext)getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        Authn authnService = (Authn)ac.getBean("org.sakaiproject.section.api.facade.manager.Authn");
        Authz authzService = (Authz)ac.getBean("org.sakaiproject.section.api.facade.manager.Authz");
        Context contextService = (Context)ac.getBean("org.sakaiproject.section.api.facade.manager.Context");

        String userUid = authnService.getUserUid(null);
        String siteContext = contextService.getContext(null);
        
        boolean viewAllSections = authzService.isViewAllSectionsAllowed(userUid, siteContext);
        boolean viewOwnSections = authzService.isViewOwnSectionsAllowed(userUid, siteContext);

        StringBuilder path = new StringBuilder(request.getContextPath());
        if(viewAllSections) {
            if(log.isDebugEnabled()) log.debug("Sending user to the overview page");
            path.append("/overview.jsf");
        } else if (viewOwnSections) {
            if(log.isDebugEnabled()) log.debug("Sending user to the student view page");
            //Control if the access to the groups is closed
            SectionManager sm = (SectionManager)ac.getBean("org.sakaiproject.section.api.SectionManager");
            Calendar open = sm.getOpenDate(siteContext);
            Calendar now = Calendar.getInstance();
            if (now.before(open)) {
            	log.debug("SECTIONS: Grupos Cerrados...");
            	path.append("/closed.jsf");
            }else {
            	path.append("/studentView.jsf");
            };
        } else {
            // The role filter has not been invoked yet, so this could happen here
            path.append("/noRole.jsp");
        }
        String queryString = request.getQueryString();
        if (queryString != null) {
			path.append("?").append(queryString);
		}
        try {
			response.sendRedirect(path.toString());
		} catch (IOException e) {
			log.error("Could not redirect user: " + e);
		}
		
	}

}
