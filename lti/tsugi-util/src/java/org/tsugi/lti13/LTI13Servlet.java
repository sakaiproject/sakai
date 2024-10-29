/**
 * Copyright (c) 2018- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * Author: Charles Severance <csev@umich.edu>
 */
package org.tsugi.lti13;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONValue;

import lombok.extern.slf4j.Slf4j;


/**
 * Notes:
 *
 * This is a sample "Hello World" servlet for LTI13. It is a simple UI - mostly
 * intended to exercise the APIs and show the way for servlet-based LTI13 code.
 *
 * Here are the web.xml entries:
 *
 * <servlet>
 * <servlet-name>LTI13Servlet</servlet-name>
 * <servlet-class>org.tsugi.lti13.LTI13Servlet</servlet-class>
 * </servlet>
 * <servlet-mapping>
 * <servlet-name>LTI13Servlet</servlet-name>
 * <url-pattern>/lti13/*</url-pattern>
 * </servlet-mapping>
 *
 */
@SuppressWarnings("deprecation")
@Slf4j
public class LTI13Servlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String APPLICATION_JSON = "application/json";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI(); // /imsblis/lti13/keys
		// String launch_url = request.getParameter("launch_url");
		String[] parts = uri.split("/");

		if (parts.length > 2 && "keys".equals(parts[3])) {
			PrintWriter out = null;
			Map<String, String> retval = null;
			try {
				out = response.getWriter();
				retval = LTI13Util.generateKeys();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return;
			}

			String jsonText = JSONValue.toJSONString(retval);

			response.setContentType(APPLICATION_JSON);
			try {
				out.println(jsonText);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			return;
		}

		response.setContentType("text/html");
		try {
			PrintWriter out = response.getWriter();
			out.println("Yada456");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.error("Yada");
	}

}
