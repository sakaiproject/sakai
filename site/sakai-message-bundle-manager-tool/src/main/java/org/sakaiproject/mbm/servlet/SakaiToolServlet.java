/****************************************************************************** 
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://opensource.org/licenses/ECL-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.mbm.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.tool.api.Tool;

/**
 * SakaiToolServlet
 * 
 * This Servlet is used to forward requests from the portal to the
 * approriate servlet context.
 * 
 * @author Earle Nietzel
 *         (earle.nietzel@gmail.com)
 *
 */
@Slf4j
public class SakaiToolServlet extends HttpServlet {

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		if (log.isDebugEnabled()) {
			StringBuilder headers = new StringBuilder("Headers [");
			StringBuilder params = new StringBuilder("Params [");
			
			Enumeration<String> headerNames = req.getHeaderNames();
			while(headerNames.hasMoreElements()) {
				String s = headerNames.nextElement();
				headers.append(s).append("={").append(StringUtils.isNotEmpty(req.getHeader(s)) ? req.getHeader(s) : "null").append("},");
			}
			headers.append("]");

			Enumeration<String> paramNames = req.getParameterNames();
			while(paramNames.hasMoreElements()) {
				String s = paramNames.nextElement();
				params.append(s).append("={").append(StringUtils.isNotEmpty(req.getParameter(s)) ? req.getParameter(s) : "null").append("},");
			}
			params.append("]");

			log.debug("SakaiToolServlet: service(HttpServletRequest, HttpServletResponse)\n" +
					"context path = {}\n" +
					"request path = {}\n" +
					"headers      = {}\n" +
					"params       = {}\n" +
					"content type = {}\n" +
					"method       = {}\n" +
					"query        = {}\n" +
					"request url  = {}\n" +
					"request uri  = {}\n" +
					"locale       = {}\n",
					req.getContextPath(), req.getPathInfo(), headers, params, req.getContentType(), req.getMethod(),
					req.getQueryString(), req.getRequestURL(), req.getRequestURI(), req.getLocale()
			);
		}
		
		final String startPage = StringUtils.isNotEmpty(getInitParameter("index")) ? getInitParameter("index") : "/";
		final String contextPath = req.getContextPath();

		req.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);		
		
		HttpServletRequest wrappedReq = new HttpServletRequestWrapper(req) {
			public String getContextPath() {
				return contextPath;
			}
		};
		
		if (StringUtils.isEmpty(req.getPathInfo())) {
			resp.sendRedirect(contextPath + startPage);
		} else {
			RequestDispatcher dispatcher;
			if (StringUtils.isEmpty(req.getPathInfo())) {
				dispatcher = req.getRequestDispatcher("/");
			} else {
				dispatcher = req.getRequestDispatcher(req.getPathInfo());
			}
			dispatcher.forward(wrappedReq, resp);
		}
	}
}
