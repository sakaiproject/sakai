/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.roster.tool;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.roster.api.SakaiProxy;
import org.sakaiproject.roster.impl.SakaiProxyImpl;
import org.sakaiproject.util.ResourceLoader;

/**
 * <code>RosterTool</code> performs basic checks and redirects to roster.html
 * 
 * @author Daniel Robinson (d.b.robinson@lancaster.ac.uk)
 * @author Adrian Fish (a.fish@lancaster.ac.uk)
 */
public class RosterTool extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(RosterTool.class);

	private transient SakaiProxy sakaiProxy = null;
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		if (log.isDebugEnabled()) {
			log.debug("init");
		}
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		if (log.isDebugEnabled()) {
			log.debug("doGet()");
		}
		
		if (null == sakaiProxy) {
			sakaiProxy = SakaiProxyImpl.instance();
		}

		String userId = sakaiProxy.getCurrentUserId();

		if (null == userId) {
			// We are not logged in
			throw new ServletException("getCurrentUser returned null.");
		}
				
		if (request.getRequestURI().contains("/portal/pda/")) {
			Cookie params = new Cookie("sakai-tool-params", getToolParameters(userId));
			response.addCookie(params);

			RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/roster.html");
			dispatcher.include(request, response);
		} else {
			response.sendRedirect("/sakai-roster2-tool/roster.html?" + getToolParameters(userId));
		}
	}

	private String getToolParameters(String userId) {
		String toolParameters = "state=" + sakaiProxy.getDefaultRosterStateString()
				+ "&siteId=" + sakaiProxy.getCurrentSiteId() 
				+ "&skin=" + sakaiProxy.getSakaiSkin()
				+ "&language="+ (new ResourceLoader(userId)).getLocale().getLanguage()
				+ "&defaultSortColumn=" + sakaiProxy.getDefaultSortColumn()
				+ "&firstNameLastName=" + sakaiProxy.getFirstNameLastName()
				+ "&hideSingleGroupFilter=" + sakaiProxy.getHideSingleGroupFilter()
				+ "&viewUserDisplayId="	+ sakaiProxy.getViewUserDisplayId()
				+ "&viewEmail="	+ sakaiProxy.getViewEmail();
		return toolParameters;
	}
}
