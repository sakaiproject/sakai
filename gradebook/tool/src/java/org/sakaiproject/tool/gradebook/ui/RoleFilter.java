/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.ContextManagement;
import org.sakaiproject.tool.gradebook.facades.Role;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * A role-based authorization filter which takes four parameters:
 * <ul>
 *   <li>authnServiceBean - The Spring-configured authentication service
 *   <li>authzServiceBean - The Spring-configured authorization service
 *   <li>role - A string describing which role is required to access this resource
 *   <li>selectGradebookRedirect - Where to go if a gradebook doesn't seem to have
 *       been selected yet; this can happen on an application redeploy to Tomcat,
 *       since JSF beans will be lost but authentication for the session might
 *       still be active
 *   </ul>
 */
public class RoleFilter implements Filter {
	private static Log logger = LogFactory.getLog(RoleFilter.class);

	private String authnServiceBeanName;
	private String authzServiceBeanName;
	private String contextManagementServiceBeanName;
	private String roleParam;
	private String selectGradebookRedirect;

    private ApplicationContext ac;
    
	public void init(FilterConfig filterConfig) throws ServletException {
        if(logger.isInfoEnabled()) logger.info("Initializing gradebook role filter");

        ac = (ApplicationContext)filterConfig.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

        authnServiceBeanName = filterConfig.getInitParameter("authnServiceBean");
		authzServiceBeanName = filterConfig.getInitParameter("authzServiceBean");
		contextManagementServiceBeanName = filterConfig.getInitParameter("contextManagementServiceBean");
		roleParam = filterConfig.getInitParameter("role");
		selectGradebookRedirect = filterConfig.getInitParameter("selectGradebookRedirect");
    }

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {
		HttpSession session = ((HttpServletRequest)request).getSession();
                
		Authn authnService = (Authn)ac.getBean(authnServiceBeanName);
		Authz authzService = (Authz)ac.getBean(authzServiceBeanName);
		ContextManagement contextManagementService = (ContextManagement)ac.getBean(contextManagementServiceBeanName);
		String userUid = authnService.getUserUid(request);

        if(logger.isDebugEnabled()) logger.debug("Filtering request for user " + userUid);
        
		// Try to get the currently selected gradebook UID, if any
		// First check the context management service.
		// Then check for a locally maintained value.
		String gradebookUid = contextManagementService.getGradebookUid(request);
		if (gradebookUid == null) {
			gradebookUid = GradebookBean.getGradebookUidFromRequest(request);
		}

		if (gradebookUid != null) {
			Role role = authzService.getGradebookRole(gradebookUid, userUid);
			if(logger.isInfoEnabled()) logger.info("gradebookUid=" + gradebookUid + ", userUid=" + userUid + ", role=" + role);
			if (role.getName().equals(roleParam)) {
				chain.doFilter(request, response);
			} else {
				logger.error("AUTHORIZATION FAILURE: User " + userUid + " in role " + role + " in gradebook " + gradebookUid + " attempted to reach URL " + ((HttpServletRequest)request).getRequestURL());
				((HttpServletResponse)response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		} else {
			if (selectGradebookRedirect != null) {
				((HttpServletResponse)response).sendRedirect(selectGradebookRedirect);
			} else {
				// TODO Any better status code for this?
				((HttpServletResponse)response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		}
	}

	public void destroy() {
	}
}



