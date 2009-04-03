/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.ui.standalone;

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
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * An authentication filter for standalone use in demos and UI tests.
 */
public class AuthnFilter implements Filter {
	private static Log logger = LogFactory.getLog(AuthnFilter.class);

	private String authnRedirect;
	private String authnServiceBean;

	public void init(FilterConfig filterConfig) throws ServletException {
		authnRedirect = filterConfig.getInitParameter("authnRedirect");
		authnServiceBean = filterConfig.getInitParameter("authnServiceBean");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {
		HttpSession session = ((HttpServletRequest)request).getSession();
		Authn authnService = (Authn)WebApplicationContextUtils.getWebApplicationContext(session.getServletContext()).getBean(authnServiceBean);
		authnService.setAuthnContext(request);
		String userUid = authnService.getUserUid();
		if (logger.isInfoEnabled()) logger.info("userUid=" + userUid);
		if (userUid == null) {
			if (authnRedirect != null) {
				if (authnRedirect.equals(((HttpServletRequest)request).getRequestURI())) {
					// Don't redirect to the same spot.
					chain.doFilter(request, response);
				} else {
					// ((HttpServletRequest)request).getRequestDispatcher(authnRedirect).forward(request, response);
					((HttpServletResponse)response).sendRedirect(authnRedirect);
				}
			} else {
				((HttpServletResponse)response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	public void destroy() {
	}
}



