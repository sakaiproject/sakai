/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import java.io.IOException;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.ContextManagement;

/**
 * Redirects the request to the role-appropriate initial view of the gradebook.
 */
public class EntryServlet extends HttpServlet {
    private static final Log logger = LogFactory.getLog(EntryServlet.class);

    public static final String INIT_SECRET = "org.apache.myfaces.secret";
    public static final String GENERATE_RANDOM_SECRET = "GENERATE_RANDOM_SECRET";
    public static final String DEFAULT_ALGORITHM = "DES";

    public void init(ServletConfig config) throws ServletException {
        ServletContext servletContext = config.getServletContext();
        handleMyFacesSecret(servletContext);
        super.init(config);
    }
    
    private void handleMyFacesSecret(ServletContext servletContext) {
        String secret = servletContext.getInitParameter(INIT_SECRET);
        if(secret == null) { // this means that org.apache.myfaces.secret context param was removed from gradebook web.xml
            if (logger.isWarnEnabled()) logger.warn("MyFaces ViewState encryption has been disabled.  See the MyFaces Wiki for encryption options.");
        } else if(secret.equalsIgnoreCase(GENERATE_RANDOM_SECRET)) {
            int length = 8;
            byte[] bytes = new byte[length];
            new Random().nextBytes(bytes);
            SecretKey secretKey = new SecretKeySpec(bytes, DEFAULT_ALGORITHM);
            servletContext.setAttribute("org.apache.myfaces.secret.CACHE", secretKey);
            if(logger.isDebugEnabled()) logger.debug("generated random MyFaces secret");
        } // else if this is not true, then org.apache.myfaces.secret context param was customized in web.xml, so let MyFaces StateUtils handle secret
    }

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
		            if(logger.isDebugEnabled()) logger.debug("Sending user to the overview page");
                    path.append("/overview.jsf");
                } else if (authzService.isUserAbleToViewOwnGrades(gradebookUid)) {
		            if(logger.isDebugEnabled()) logger.debug("Sending user to the student view page");
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
            logger.fatal("Could not redirect user: " + ioe);
        }
	}

}


