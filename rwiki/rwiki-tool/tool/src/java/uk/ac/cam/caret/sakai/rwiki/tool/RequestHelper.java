/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.tool;

import javax.servlet.http.HttpServletRequest;
import org.sakaiproject.component.cover.ServerConfigurationService;

import lombok.extern.slf4j.Slf4j;

import uk.ac.cam.caret.sakai.rwiki.tool.api.CommandService;
import uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand;

public class RequestHelper
{
	public static final String PANEL = "panel";

	public static final String ACTION = "action";

	public static final String TITLE_PANEL = "Title";

	public static final String HELPER_PATH = "helper";

	public static final String WIKI_PATH = "wiki";

	private String defaultAction = "view";

	private CommandService commandService;

	public void init()
	{

	}

	public HttpCommand getCommandForRequest(HttpServletRequest request)
	{
		String panel = request.getParameter(PANEL);
		String action = request.getParameter(ACTION);

		// Cope with Sakai panel behaviour
		if (TITLE_PANEL.equals(panel))
		{
			action = panel;
		}

		// Cope with helper call:
		String requestPath = request.getRequestURI().substring(
				request.getContextPath().length()
						+ request.getServletPath().length());
		
		// SAK-13408 - Tomcat and WAS have different URL structures; Attempting to add a 
		// link or image would lead to site unavailable errors in websphere if the tomcat
		// URL structure is used.
		if("websphere".equals(ServerConfigurationService.getString("servlet.container"))) {
			String[] parts = requestPath.split("/");

			if ((parts.length >= 4) && (parts[3].equals(HELPER_PATH)))
			{
				action = HELPER_PATH;
			}

		}
		else if (requestPath != null
				&& requestPath.startsWith("/" + HELPER_PATH + "/"))
			action = HELPER_PATH;
		
		if (action == null)
		{
			action = defaultAction;
		}

		
		return commandService.getCommand(action);
	}

	public CommandService getCommandService()
	{
		return commandService;
	}

	public void setCommandService(CommandService commandService)
	{
		this.commandService = commandService;
	}

	public String getDefaultAction()
	{
		return defaultAction;
	}

	public void setDefaultAction(String defaultAction)
	{
		this.defaultAction = defaultAction;
	}

}
