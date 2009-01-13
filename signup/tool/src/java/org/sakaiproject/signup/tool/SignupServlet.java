/**********************************************************************************
 * $URL$
 * $Id$
***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Yale University
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *   
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/
package org.sakaiproject.signup.tool;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.jsf.util.JsfTool;

/**
 * <p>
 * This Servlet class will enable the path to javaScript and image files in
 * Signup tool and it will also enable the ajax call later in the future. For
 * Sakai 2.5, it will not pick up the CSS file via the 'sakai.html.head'. and we
 * take this out.
 * </P>
 */
@SuppressWarnings("serial")
public class SignupServlet extends JsfTool {

	private String headerPreContent;

	/**
	 * Initialize the Servlet class.
	 */
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		// headerPreContent =
		// servletConfig.getInitParameter("headerPreContent");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		// addPrecontent(request);

		// if(ajax request)
		// else call the super doPost
		super.doPost(request, response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// addPrecontent(request);
		// if(ajax request)
		// else call the super doGet
		super.doGet(request, response);
	}

	/*
	 * private void addPrecontent(HttpServletRequest request) { String
	 * sakaiHeader = (String) request.getAttribute("sakai.html.head");
	 * request.setAttribute("sakai.html.head", sakaiHeader + headerPreContent); }
	 */
}
