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
		String userUid = authnService.getUserUid(request);
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

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
