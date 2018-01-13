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

package uk.ac.cam.caret.sakai.rwiki.tool.service.impl;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import lombok.extern.slf4j.Slf4j;

import uk.ac.cam.caret.sakai.rwiki.service.exception.PermissionException;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.tool.api.CommandService;
import uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand;
import uk.ac.cam.caret.sakai.rwiki.tool.command.Dispatcher;
import uk.ac.cam.caret.sakai.rwiki.tool.RequestScopeSuperBean;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;


/**
 * Implementation of RWikiCommandService, that is initialised with a map of
 * commands and a default command
 * 
 * @author andrew
 */
@Slf4j
public class CommandServiceImpl implements CommandService
{

	private Map commandMap;

	private String template = "/WEB-INF/command-pages/{0}.jsp";

	private String permissionPath = "/WEB-INF/command-pages/permission.jsp";

	private boolean trackReads = false;

	private EventTrackingService eventTrackingService = null;

	private class WrappedCommand implements HttpCommand
	{
		private HttpCommand command;

		public WrappedCommand(HttpCommand command)
		{
			this.command = command;
		}

		public void execute(Dispatcher dispatcher, HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException
		{
			try
			{
				command.execute(dispatcher,request, response);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	private class DefaultCommand implements HttpCommand
	{
		private String action;

		public DefaultCommand(String action)
		{
			this.action = action;
			log.debug("Created command " + action);
		}

		public void execute(Dispatcher dispatcher,HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException
		{
			String actionPath = MessageFormat.format(template,
					new Object[] { action });
			try
			{
				dispatcher.dispatch(actionPath, request, response);
				if ( trackReads && "view".equals(action) ) {
					RequestScopeSuperBean rssb = RequestScopeSuperBean.getInstance();
					String ref = rssb.getCurrentRWikiObjectReference();
					eventTrackingService.post(eventTrackingService.newEvent(
                                        	RWikiObjectService.EVENT_RESOURCE_READ, ref, true,
                                        	NotificationService.PREF_IMMEDIATE));

				}
			}
			catch (ServletException e)
			{
				if (e.getRootCause() instanceof PermissionException)
				{
					dispatcher.dispatch(permissionPath, request, response);
				}
				else
				{
					throw new RuntimeException(e);
				}
			}
			catch (PermissionException e)
			{
				dispatcher.dispatch(permissionPath, request, response);
			}
		}

	}

	public void init()
	{
        trackReads = ServerConfigurationService.getBoolean("wiki.trackreads", true);

		for (Iterator it = commandMap.keySet().iterator(); it.hasNext();)
		{
			String commandName = (String) it.next();
			HttpCommand toWrap = (HttpCommand) commandMap.get(commandName);
			commandMap.put(commandName, new WrappedCommand(toWrap));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RWikiCommandService#getCommand(java.lang.String)
	 */
	public HttpCommand getCommand(String commandName)
	{

		
		HttpCommand command = (HttpCommand) commandMap.get(commandName);

		if (command == null)
		{
			return new DefaultCommand(commandName);
		}
		return command;
	}

	public Map getCommandMap()
	{
		return commandMap;
	}

	public void setCommandMap(Map commandMap)
	{
		this.commandMap = commandMap;
	}

	public String getDefaultActionPathTemplate()
	{
		return template;
	}

	public void setDefaultActionPathTemplate(String template)
	{
		this.template = template;
	}

	public String getPermissionPath()
	{
		return permissionPath;
	}

	public void setPermissionPath(String permissionPath)
	{
		this.permissionPath = permissionPath;
	}

	public boolean getTrackReads()
	{
		return trackReads;
	}

	public void setTrackReads(boolean trackReads)
	{
		this.trackReads = trackReads;
	}

	public EventTrackingService getEventTrackingService()
	{
		return eventTrackingService;
	}

	public void setEventTrackingService(EventTrackingService eventTrackingService)
	{
		this.eventTrackingService = eventTrackingService;
	}

}
