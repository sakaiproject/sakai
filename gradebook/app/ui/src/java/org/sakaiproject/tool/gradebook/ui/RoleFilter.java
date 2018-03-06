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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.ContextManagement;

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
 *
 * Because Tomcat 5.* developers decided to take an eccentric interpretation of the
 * ambiguous language in the Servlet specification, Tomcat doesn't accept the
 * combination of directories and wildcards for the "url-pattern" of the
 * filter mapping. As a result, the filter has to do some of that work itself.
 * In this case, we guard all pages at the top of the servlet path but let
 * other URLs (e.g., "/test/login.jsf") pass through.
 */
@Slf4j
public class RoleFilter implements Filter {
	private String authnServiceBeanName;
	private String authzServiceBeanName;
	private String contextManagementServiceBeanName;
	private String authorizationFilterConfigurationBeanName;
	private String selectGradebookRedirect;

	private ApplicationContext ac;

	public void init(FilterConfig filterConfig) throws ServletException {
		if(log.isInfoEnabled()) log.info("Initializing gradebook role filter");

		ac = (ApplicationContext)filterConfig.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

		authnServiceBeanName = filterConfig.getInitParameter("authnServiceBean");
		authzServiceBeanName = filterConfig.getInitParameter("authzServiceBean");
		contextManagementServiceBeanName = filterConfig.getInitParameter("contextManagementServiceBean");
		authorizationFilterConfigurationBeanName = filterConfig.getInitParameter("authorizationFilterConfigurationBean");
		selectGradebookRedirect = filterConfig.getInitParameter("selectGradebookRedirect");
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest)servletRequest;
		String servletPath = request.getServletPath();
		if (log.isDebugEnabled()) log.debug("Filtering request for servletPath=" + servletPath);
		servletPath = servletPath.replaceFirst("^/", "");
		if (servletPath.indexOf("/") >= 0) {
			// Only protect the top-level folder, to allow for login through
			// a subdirectory, shared resource files, and so on.
			chain.doFilter(request, response);
			return;
		}

		Authn authnService = (Authn)ac.getBean(authnServiceBeanName);
		Authz authzService = (Authz)ac.getBean(authzServiceBeanName);
		ContextManagement contextManagementService = (ContextManagement)ac.getBean(contextManagementServiceBeanName);
		AuthorizationFilterConfigurationBean authorizationFilterConfigurationBean = (AuthorizationFilterConfigurationBean)ac.getBean(authorizationFilterConfigurationBeanName);
		authnService.setAuthnContext(request);
		String userUid = authnService.getUserUid();

        if (log.isDebugEnabled()) log.debug("Filtering request for user " + userUid + ", pathInfo=" + request.getPathInfo());

		// Try to get the currently selected gradebook UID, if any
		// First check the context management service.
		// Then check for a locally maintained value.
		String gradebookUid = contextManagementService.getGradebookUid(request);
        if(log.isDebugEnabled()) log.debug("contextManagementService.getGradebookUid=" + gradebookUid);
		if (gradebookUid == null) {
			gradebookUid = GradebookBean.getGradebookUidFromRequest(request);
	        if (log.isDebugEnabled()) log.debug("GradebookBean.getGradebookUidFromRequest=" + gradebookUid);
		}

		if (gradebookUid != null) {
			if(log.isDebugEnabled()) log.debug("gradebookUid=" + gradebookUid + ", userUid=" + userUid);

			// Get the name of the page from the servlet path.
			String[] splitPath = servletPath.split("[./]");
			String pageName = splitPath[0];

			boolean isAuthorized;
			if (authzService.isUserAbleToGrade(gradebookUid) &&
				authorizationFilterConfigurationBean.getUserAbleToGradePages().contains(pageName)) {
				isAuthorized = true;
			} else if (authzService.isUserAbleToEditAssessments(gradebookUid) && authorizationFilterConfigurationBean.getUserAbleToEditPages().contains(pageName)) {
				isAuthorized = true;
			} else if (authzService.isUserAbleToViewOwnGrades(gradebookUid) && authorizationFilterConfigurationBean.getUserAbleToViewOwnGradesPages().contains(pageName)) {
				isAuthorized = true;
			} else {
				isAuthorized = false;
			}

			// SAK-13408 - This fix addresses the problem of the filter receiving a blank field on WebSphere.
			// Without this, users would be denied access to the tool.
			if("websphere".equals(ServerConfigurationService.getString("servlet.container")) && (isAuthorized || pageName.equals("")) ) {
				chain.doFilter(request, response);
			} else if ( !"websphere".equals(ServerConfigurationService.getString("servlet.container")) && isAuthorized) {
				chain.doFilter(request, response);
			} else {
				log.error("AUTHORIZATION FAILURE: User " + userUid + " in gradebook " + gradebookUid + " attempted to reach URL " + request.getRequestURL());
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
