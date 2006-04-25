/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import java.io.IOException;

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
                StringBuffer path = new StringBuffer(request.getContextPath());
                if (authzService.isUserAbleToGrade(gradebookUid)) {
		            if(logger.isDebugEnabled()) logger.debug("Sending user to the overview page");
                    path.append("/overview.jsf");
                } else if (authzService.isUserAbleToViewOwnGrades(gradebookUid)) {
		            if(logger.isDebugEnabled()) logger.debug("Sending user to the student view page");
                    path.append("/studentView.jsf");
                } else {
					// The role filter has not been invoked yet, so this could happen here
					throw new RuntimeException("User " + authnService.getUserUid() + " attempted to access gradebook " + gradebookUid + " without any role");
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


