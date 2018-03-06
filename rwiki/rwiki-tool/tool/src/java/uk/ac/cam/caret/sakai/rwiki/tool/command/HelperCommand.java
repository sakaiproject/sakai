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

package uk.ac.cam.caret.sakai.rwiki.tool.command;

import org.sakaiproject.component.cover.ServerConfigurationService;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;

import uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand;

/**
 * HttpCommand which calls the file picker tool from sakai context.
 * 
 * @author andrew
 */
@Slf4j
public class HelperCommand implements HttpCommand
{

	private ActiveToolManager activeToolManager;

	private SessionManager sessionManager;

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		activeToolManager = (ActiveToolManager) load(cm,
				ActiveToolManager.class.getName());
		sessionManager = (SessionManager) load(cm, SessionManager.class
				.getName());
	}

	private Object load(ComponentManager cm, String name)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			log.error("Cant find Spring component named " + name);
		}
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand#execute(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public void execute(Dispatcher dispatcher, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{

		// FIXME!!
		String requestPath = request.getRequestURI().substring(
				request.getContextPath().length()
						+ request.getServletPath().length());

		String[] parts = requestPath.split("/");

		String helperId = null;
		
		// SAK-13408 - Tomcat and WAS have different URL structures; Attempting to add a 
		// link or image would lead to site unavailable errors in websphere if the tomcat
		// URL structure is used.
		if("websphere".equals(ServerConfigurationService.getString("servlet.container"))) {
			if (parts.length < 5)
			{
				throw new IllegalArgumentException(
						"You must provide a helper name to request.");
			}

			helperId = parts[4];
		}
		else {
			if (parts.length < 3)
			{
				throw new IllegalArgumentException(
						"You must provide a helper name to request.");
			}

			helperId = parts[2];
		}

		ActiveTool helperTool = activeToolManager.getActiveTool(helperId);
		// put state info in toolSession to communicate with helper

		StringBuffer context = new StringBuffer(request.getContextPath())
				.append(request.getServletPath());

		StringBuffer toolPath = new StringBuffer();
		
		// SAK-13408 - Tomcat and WAS have different URL structures; Attempting to add a 
		// link or image would lead to site unavailable errors in websphere if the tomcat
		// URL structure is used.
		if("websphere".equals(ServerConfigurationService.getString("servlet.container"))){
			String tid = org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
			context.append("/tool/");
			context.append(tid);
		
			for (int i = 3; i < 5; i++)
			{
				context.append('/');
				context.append(parts[i]);
			}
			for (int i = 5; i < parts.length; i++)
			{
				toolPath.append('/');
				toolPath.append(parts[i]);
			}
		} else {

			for (int i = 1; i < 3; i++)
			{
				context.append('/');
				context.append(parts[i]);
			}
			for (int i = 3; i < parts.length; i++)
			{
				toolPath.append('/');
				toolPath.append(parts[i]);
			}

		}

		request.removeAttribute(Tool.NATIVE_URL);

		// this is the forward call
		helperTool.help(request, response, context.toString(), toolPath
				.toString());

	}
}
