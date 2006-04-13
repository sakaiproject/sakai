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
package uk.ac.cam.caret.sakai.rwiki.tool.command;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class HelperCommand implements HttpCommand
{

	private ActiveToolManager activeToolManager;

	private SessionManager sessionManager;

	public ActiveToolManager getActiveToolManager()
	{
		return activeToolManager;
	}

	public void setActiveToolManager(ActiveToolManager activeToolManager)
	{
		this.activeToolManager = activeToolManager;
	}

	public SessionManager getSessionManager()
	{
		return sessionManager;
	}

	public void setSessionManager(SessionManager sessionManager)
	{
		this.sessionManager = sessionManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand#execute(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public void execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{

		// FIXME!!
		String requestPath = request.getRequestURI().substring(
				request.getContextPath().length()
						+ request.getServletPath().length());

		String[] parts = requestPath.split("/");

		if (parts.length < 3)
		{
			throw new IllegalArgumentException(
					"You must provide a helper name to request.");
		}

		String helperId = parts[2];

		ActiveTool helperTool = activeToolManager.getActiveTool(helperId);
		// put state info in toolSession to communicate with helper

		StringBuffer context = new StringBuffer(request.getContextPath())
				.append(request.getServletPath());

		for (int i = 1; i < 3; i++)
		{
			context.append('/');
			context.append(parts[i]);
		}

		StringBuffer toolPath = new StringBuffer();
		for (int i = 3; i < parts.length; i++)
		{
			toolPath.append('/');
			toolPath.append(parts[i]);
		}

		request.removeAttribute(Tool.NATIVE_URL);

		// this is the forward call
		helperTool.help(request, response, context.toString(), toolPath
				.toString());

	}
}
