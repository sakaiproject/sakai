/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
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
package uk.ac.cam.caret.sakai.rwiki.tool.service.impl;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.cam.caret.sakai.rwiki.service.exception.PermissionException;
import uk.ac.cam.caret.sakai.rwiki.tool.api.CommandService;
import uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand;

/**
 * Implementation of RWikiCommandService, that is initialised with a map of
 * commands and a default command
 * 
 * @author andrew
 */
// FIXME: Tool
public class CommandServiceImpl implements CommandService
{
	private static Log log = LogFactory.getLog(CommandServiceImpl.class);

	private Map commandMap;

	private String template = "/WEB-INF/command-pages/{0}.jsp";

	private String permissionPath = "/WEB-INF/command-pages/permission.jsp";

	public String errorPath = "/WEB-INF/command-pages/errorpage.jsp";

	private class WrappedCommand implements HttpCommand
	{
		private HttpCommand command;

		public WrappedCommand(HttpCommand command)
		{
			this.command = command;
		}

		public void execute(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException
		{
			try
			{
				command.execute(request, response);
			}
			catch (Exception e)
			{
				if (request.getAttribute(PageContext.EXCEPTION) == null)
				{
					request.setAttribute(PageContext.EXCEPTION, e);
				}
				RequestDispatcher rd = request.getRequestDispatcher(errorPath);
				rd.forward(request, response);
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

		public void execute(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException
		{
			long start = System.currentTimeMillis();
			String actionPath = MessageFormat.format(template,
					new Object[] { action });
			long start1 = System.currentTimeMillis();
			start = System.currentTimeMillis();
			log.debug(" Going to " + actionPath);
			RequestDispatcher rd = request.getRequestDispatcher(actionPath);
			long start2 = System.currentTimeMillis();
			try
			{
				rd.forward(request, response);
			}
			catch (ServletException e)
			{
				if (e.getRootCause() instanceof PermissionException)
				{
					rd = request.getRequestDispatcher(permissionPath);
					rd.forward(request, response);
				}
				else
				{
					if (request.getAttribute(PageContext.EXCEPTION) == null)
					{
						request.setAttribute(PageContext.EXCEPTION, e);
					}
					rd = request.getRequestDispatcher(errorPath);
					rd.forward(request, response);
				}
			}
			catch (PermissionException e)
			{
				rd = request.getRequestDispatcher(permissionPath);
				rd.forward(request, response);
			}
			finally
			{
				long finish = System.currentTimeMillis();
				if ((start2 - start) > 20)
				{
					long i1 = start1 - start;
					long i2 = start2 - start1;
					long i3 = finish - start2;
					long i4 = finish - start;
					log
							.warn("Defult Command Service Dispatch Preamble taking too long, "
									+ "Message Format "
									+ i1
									+ " ms, getRequestDispatch "
									+ i2
									+ " ms, forward "
									+ i3
									+ " ms, overall "
									+ i4 + " ms");
				}
			}
		}

	}

	public void init()
	{
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

}
