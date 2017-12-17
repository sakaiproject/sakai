/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.sakaiproject.section.api.facade.manager.Authn;

/**
 * An authentication filter for standalone use in demos and UI tests.
 */
@Slf4j
public class AuthnFilter implements Filter {

	private String authnRedirect;
	private String authnBean;

	public void init(FilterConfig filterConfig) throws ServletException {
		authnRedirect = filterConfig.getInitParameter("authnRedirect");
		authnBean = filterConfig.getInitParameter("authnBean");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {
		HttpSession session = ((HttpServletRequest)request).getSession(true);
		Authn authnService = (Authn)WebApplicationContextUtils.getWebApplicationContext(session.getServletContext()).getBean(authnBean);
		String userUid = null;
		try {
			userUid = authnService.getUserUid(request);
		} catch (Exception e) {
			if(log.isDebugEnabled()) log.debug("Could not get user uuid from authn service.");
		}
		if (log.isDebugEnabled()) log.debug("userUid=" + userUid);
		if (userUid == null) {
			if (authnRedirect != null) {
				if (authnRedirect.equals(((HttpServletRequest)request).getRequestURI())) {
					// Don't redirect to the same spot.
					chain.doFilter(request, response);
				} else {
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

