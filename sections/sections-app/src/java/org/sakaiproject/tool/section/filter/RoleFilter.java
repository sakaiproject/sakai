/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
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

package org.sakaiproject.tool.section.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.facade.manager.Authn;
import org.sakaiproject.api.section.facade.manager.Authz;
import org.sakaiproject.api.section.facade.manager.Context;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * An authorization filter to keep users out of pages they are not authorized
 * to access.
 *  
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class RoleFilter implements Filter {
	private static Log logger = LogFactory.getLog(RoleFilter.class);

	private String authnBeanName;
	private String authzBeanName;
	private String contextBeanName;
	private String authorizationFilterConfigurationBeanName;
	private String selectSiteRedirect;

    private ApplicationContext ac;

	public void init(FilterConfig filterConfig) throws ServletException {
        if(logger.isInfoEnabled()) logger.info("Initializing sections role filter");

        ac = (ApplicationContext)filterConfig.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

        authnBeanName = filterConfig.getInitParameter("authnServiceBean");
		authzBeanName = filterConfig.getInitParameter("authzServiceBean");
		contextBeanName = filterConfig.getInitParameter("contextManagementServiceBean");
		authorizationFilterConfigurationBeanName = filterConfig.getInitParameter("authorizationFilterConfigurationBean");
		selectSiteRedirect = filterConfig.getInitParameter("selectSiteRedirect");
    }

	public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest)servletRequest;
		String servletPath = request.getServletPath();
		if (logger.isDebugEnabled()) logger.debug("Filtering request for servletPath=" + servletPath);
		servletPath = servletPath.replaceFirst("^/", "");
		if (servletPath.indexOf("/") >= 0) {
			// Only protect the top-level folder, to allow for login through
			// a subdirectory, shared resource files, and so on.
			chain.doFilter(request, response);
			return;
		}

		Authn authn = (Authn)ac.getBean(authnBeanName);
		Authz authz = (Authz)ac.getBean(authzBeanName);
		Context context = (Context)ac.getBean(contextBeanName);
		AuthorizationFilterConfigurationBean authzFilterConfigBean = (AuthorizationFilterConfigurationBean)ac.getBean(authorizationFilterConfigurationBeanName);
		String userUid = authn.getUserUid(request);

        if (logger.isDebugEnabled()) logger.debug("Filtering request for user " + userUid + ", pathInfo=" + request.getPathInfo());

		// Try to get the currently selected site context, if any
		String siteContext = context.getContext(request);
		
        if(logger.isDebugEnabled()) logger.debug("context=" + siteContext);

		if (siteContext != null) {
			// Get the name of the page from the servlet path.
			String[] splitPath = servletPath.split("[./]");
			String pageName = splitPath[0];

			boolean isAuthorized = false;
			if(authz.isSectionManagementAllowed(userUid, siteContext) &&
					authzFilterConfigBean.getManageAllSections().contains(pageName)) {
				isAuthorized = true;
			} else if (authz.isViewAllSectionsAllowed(userUid, siteContext)
					&& authzFilterConfigBean.getViewAllSections().contains(pageName)) {
				isAuthorized = true;
			} else if (authz.isSectionTaManagementAllowed(userUid, siteContext)
					&& authzFilterConfigBean.getManageTeachingAssistants().contains(pageName)) {
				isAuthorized = true;
			} else if (authz.isSectionEnrollmentMangementAllowed(userUid, siteContext)
					&& authzFilterConfigBean.getManageEnrollments().contains(pageName)) {
				isAuthorized = true;
			} else if (authz.isViewOwnSectionsAllowed(userUid, siteContext)
					&& authzFilterConfigBean.getViewOwnSections().contains(pageName)) {
				isAuthorized = true;
			}

			if (isAuthorized) {
				chain.doFilter(request, response);
			} else {
				logger.error("AUTHORIZATION FAILURE: User " + userUid + " in site " +
					siteContext + " attempted to reach URL " + request.getRequestURL());
				((HttpServletResponse)response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		} else {
			if (selectSiteRedirect != null) {
				((HttpServletResponse)response).sendRedirect(selectSiteRedirect);
			} else {
				((HttpServletResponse)response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		}
	}

	public void destroy() {
		ac = null;
	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
