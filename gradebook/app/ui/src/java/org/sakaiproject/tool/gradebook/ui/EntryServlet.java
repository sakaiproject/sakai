/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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

package org.sakaiproject.tool.gradebook.ui;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.ContextManagement;

/**
 * Redirects the request to the role-appropriate initial view of the gradebook.
 */
@Slf4j
public class EntryServlet extends HttpServlet {

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, java.io.IOException {
		doGet(req, resp);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
        WebApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        Authn authnService = (Authn)appContext.getBean("org_sakaiproject_tool_gradebook_facades_Authn");
		Authz authzService = (Authz)appContext.getBean("org_sakaiproject_tool_gradebook_facades_Authz");
		ContextManagement contextMgm = (ContextManagement)appContext.getBean("org_sakaiproject_tool_gradebook_facades_ContextManagement");

        authnService.setAuthnContext(request);
        String gradebookUid = contextMgm.getGradebookUid(request);

        try {
            if (gradebookUid != null) {
                StringBuilder path = new StringBuilder(request.getContextPath());
                if (authzService.isUserAbleToGrade(gradebookUid)) {
		            if(log.isDebugEnabled()) log.debug("Sending user to the overview page");
                    path.append("/overview.jsf");
                } else if (authzService.isUserAbleToViewOwnGrades(gradebookUid)) {
		            if(log.isDebugEnabled()) log.debug("Sending user to the student view page");
                    path.append("/studentView.jsf");
                } else {
					// The role filter has not been invoked yet, so this could happen here
//					throw new RuntimeException("User " + authnService.getUserUid() + " attempted to access gradebook " + gradebookUid + " without any role");
                    path.append("/noRole.jsp");
                }
                String queryString = request.getQueryString();
                if (queryString != null) {
					path.append("?").append(queryString);
				}
                response.sendRedirect(path.toString());
            }
        } catch (IOException ioe) {
            log.error("Could not redirect user: {}", ioe.getMessage(), ioe);
        }
	}

}


