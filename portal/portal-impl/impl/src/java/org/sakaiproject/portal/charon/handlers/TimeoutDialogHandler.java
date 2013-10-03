/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.charon.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * This is a portal handler to get the necessary information for the timeout
 * dialog functionality. This is new functionality in Sakai 2.6, detailed in
 * SAK-13987.
 * 
 * Currently it includes 2 primary endpoints. One endpoint, mounted at 
 * timeout/config returns the configuration information that the javascript
 * needs including 
 * 
 * 1) Is this functionality enabled?  
 *    true/false property = timeoutDialogEnabled
 * 2) How long before the timeout should the dialog be shown?
 *    integer property = timeoutDialogWarningSeconds
 * 3) The logged out URL 
 *    string property = loggedOutUrl
 *    
 * These are all standard sakai.properties. For simplicity these are just
 * newline separated for now, it is easy to get them in javascript.
 * 
 * The primary endpoint at /timeout is a URL fragment for the i18n html dialog
 * that will be presented to the user. This can be formatted in the timeout.vm
 * file. It adds 2 new properties to sitenav.properties
 * timeout_dialog_warning_message, and timeout_dialog_keepalive.
 * 
 * @author sgithens
 *
 */
public class TimeoutDialogHandler extends BasePortalHandler 
{
	private static final String URL_FRAGMENT = "timeout";
	private static final String CONFIG_PART = "config";
	
	private ServerConfigurationService serverConfigService;
	
	public TimeoutDialogHandler()
	{
		setUrlFragment(TimeoutDialogHandler.URL_FRAGMENT);
		serverConfigService = (ServerConfigurationService) 
			ComponentManager.get(ServerConfigurationService.class);
	}
	
	@Override
	public int doGet(String[] parts, HttpServletRequest req,
			HttpServletResponse res, Session session)
	throws PortalHandlerException 
	{
		if ((parts.length == 3) 
				&& parts[1].equals(TimeoutDialogHandler.URL_FRAGMENT)
				&& parts[2].equals(TimeoutDialogHandler.CONFIG_PART)) {
			
			StringBuilder values = new StringBuilder();
			values.append(serverConfigService.getBoolean("timeoutDialogEnabled", false));
			values.append("\n");
			values.append(serverConfigService.getInt("timeoutDialogWarningSeconds", 600));
			values.append("\n");
			values.append(serverConfigService.getLoggedOutUrl());
			
			res.setContentType("text/plain");
			try {
				res.getWriter().print(values.toString());
			} catch (IOException e) {
				throw new PortalHandlerException(e);
			}
			return END;
		}
		else if ((parts.length >= 2) && parts[1].equals(TimeoutDialogHandler.URL_FRAGMENT)) {
		
			try {
				PortalRenderContext rcontext = portal.includePortal(req, res, session,
						null,
						/* toolId */null, req.getContextPath() + req.getServletPath(),
						/* prefix */"site", /* doPages */false, /* resetTools */false,
						/* includeSummary */false, /* expandSite */false);
				portal.sendResponse(rcontext, res, parts[1], "text/html; charset=UTF-8");
			} catch (Exception e) {
				throw new PortalHandlerException(e);
			}
			
			return END;
		}
		else 
		{
			return NEXT;
		}
	}

}
