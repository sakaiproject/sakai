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
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.api.section.facade.manager.Authn;
import org.sakaiproject.api.section.facade.manager.Authz;
import org.sakaiproject.api.section.facade.manager.Context;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public class RoleFilter implements Filter {
	private ApplicationContext ac;
	private String authnBeanName;
	private String authzBeanName;
	private String contextBeanName;
	private String roleParam;

	private static final Log log = LogFactory.getLog(RoleFilter.class);
	
	public void init(FilterConfig filterConfig) throws ServletException {
        ac = (ApplicationContext)filterConfig.getServletContext()
        	.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        authnBeanName = filterConfig.getInitParameter("authnBean");
		authzBeanName = filterConfig.getInitParameter("authzBean");
		contextBeanName = filterConfig.getInitParameter("contextBean");
		roleParam = filterConfig.getInitParameter("role");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		Authn authn = (Authn)ac.getBean(authnBeanName);
		Authz authz = (Authz)ac.getBean(authzBeanName);
		Context context = (Context)ac.getBean(contextBeanName);
		String userUuid = authn.getUserUuid(request);

        if(log.isDebugEnabled()) log.debug("Filtering request for user " + userUuid);

        String siteContext = context.getContext(request);
        Role siteRole = authz.getSiteRole(userUuid, siteContext);

		if (siteRole.getName().equals(roleParam)) {
				chain.doFilter(request, response);
		} else {
			if(log.isInfoEnabled()) log.info("PAGE VIEW AUTHZ FAILURE: User "
					+ userUuid + " in role "
					+ siteRole + " for site " + siteContext + " on page "
					+ ((HttpServletRequest)request).getServletPath());
			((HttpServletResponse)response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	public void destroy() {
		ac = null;
	}

}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
