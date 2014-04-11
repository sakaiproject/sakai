/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational
* Community License, Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.roster.tool;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * <code>RosterTool</code> performs basic checks and outputs a prebuilt startup
 * page.
 * 
 * @author Daniel Robinson (d.b.robinson@lancaster.ac.uk)
 * @author Adrian Fish (a.fish@lancaster.ac.uk)
 */
public class RosterTool extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(RosterTool.class);

	private transient SakaiProxy sakaiProxy = null;

	private Template bootstrapTemplate = null;
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		if (log.isDebugEnabled()) log.debug("init");

		try {
			VelocityEngine ve = new VelocityEngine();
            Properties props = new Properties();
            props.setProperty("file.resource.loader.path",config.getServletContext().getRealPath("/WEB-INF"));
            ve.init(props);
            bootstrapTemplate = ve.getTemplate("bootstrap.vm");
		} catch (Throwable t) {
			throw new ServletException("Failed to initialise RosterTool servlet.", t);
		}
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		if (log.isDebugEnabled()) log.debug("doGet()");
		
		if (null == sakaiProxy) {
			sakaiProxy = SakaiProxyImpl.instance();
		}

		String userId = sakaiProxy.getCurrentUserId();

		if (null == userId) {
			// We are not logged in
			throw new ServletException("getCurrentUser returned null.");
		}

		VelocityContext ctx = new VelocityContext();
		
		// This is needed so certain trimpath variables don't get parsed.
		ctx.put("D", "$");
       
		ctx.put("sakaiHtmlHead", (String) request.getAttribute("sakai.html.head"));
		
		ctx.put("userId", userId);
		ctx.put("state", sakaiProxy.getDefaultRosterStateString());
		ctx.put("siteId", sakaiProxy.getCurrentSiteId());
        ctx.put("language", (new ResourceLoader(userId)).getLocale().getLanguage());
		ctx.put("defaultSortColumn", sakaiProxy.getDefaultSortColumn());
        ctx.put("firstNameLastName", sakaiProxy.getFirstNameLastName());
		ctx.put("hideSingleGroupFilter", sakaiProxy.getHideSingleGroupFilter());
        ctx.put("viewUserDisplayId", sakaiProxy.getViewUserDisplayId());
        ctx.put("viewEmail", sakaiProxy.getViewEmail());
		ctx.put("superUser", sakaiProxy.isSuperUser());
		ctx.put("siteMaintainer", sakaiProxy.isSiteMaintainer(sakaiProxy.getCurrentSiteId()));
		
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html");
        Writer writer = new BufferedWriter(response.getWriter());
        try {
            bootstrapTemplate.merge(ctx,writer);
        } catch (Exception e) {
            log.error("Failed to merge template. Returning 500.",e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        writer.close();
	}
}
