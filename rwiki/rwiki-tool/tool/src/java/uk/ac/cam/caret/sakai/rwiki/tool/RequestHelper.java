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
package uk.ac.cam.caret.sakai.rwiki.tool;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.cam.caret.sakai.rwiki.tool.api.CommandService;
import uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand;

// FIXME: Tool
public class RequestHelper
{
	private static Log log = LogFactory.getLog(RequestHelper.class);

	public static final String PANEL = "panel";

	public static final String ACTION = "action";

	public static final String TITLE_PANEL = "Title";

	public static final String HELPER_PATH = "helper";

	public static final String WIKI_PATH = "wiki";

	private String defaultAction = "view";

	private CommandService commandService;

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
		if (requestPath != null
				&& requestPath.startsWith("/" + HELPER_PATH + "/"))
		{
			action = HELPER_PATH;
		}

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
