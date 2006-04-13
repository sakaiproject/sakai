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
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.cover.SessionManager;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiPermissions;
import uk.ac.cam.caret.sakai.rwiki.service.exception.PermissionException;
import uk.ac.cam.caret.sakai.rwiki.service.exception.VersionException;
import uk.ac.cam.caret.sakai.rwiki.tool.RWikiServlet;
import uk.ac.cam.caret.sakai.rwiki.tool.RequestScopeSuperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ErrorBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.UpdatePermissionsBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.ViewParamsHelperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.command.helper.ErrorBeanHelper;

/**
 * @author andrew
 */
// FIXME: Tool
public class UpdatePermissionsCommand implements HttpCommand
{
	private static Log log = LogFactory.getLog(UpdatePermissionsCommand.class);

	private RWikiObjectService objectService;

	private String contentChangedPath;

	private String noUpdatePath;

	private String successfulPath;

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.HttpCommand#execute(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public void execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{

		RequestScopeSuperBean rssb = RequestScopeSuperBean
				.getFromRequest(request);

		ViewParamsHelperBean vphb = rssb.getNameHelperBean();

		UpdatePermissionsBean upb = rssb.getUpdatePermissionsBean();

		String version = vphb.getSubmittedVersion();
		Date versionDate = new Date(Long.parseLong(version));
		String name = vphb.getGlobalName();
		String realm = vphb.getLocalSpace();

		RWikiPermissions perms = upb.getPermissions();
		String updateMethod = upb.getUpdatePermissionsMethod();

		if (updateMethod != null
				&& updateMethod.equals(UpdatePermissionsBean.OVERWRITE_VALUE))
		{
			perms = upb.getOverwritePermissions();
			// Set the permissions as the overwrite permissions.
			upb.setPermissions(perms);
		}

		try
		{
			objectService.update(name, realm, versionDate, perms);
		}
		catch (VersionException e)
		{
			// The page has changed underneath us...

			// redirect probably back to the edit page
			this.contentChangedDispatch(request, response);
			return;
		}
		catch (PermissionException e)
		{
			// Redirect back to a no permission page...
			this.noUpdateAllowed(request, response);
			return;
		}
		// Successful update
		this.successfulUpdateDispatch(request, response);
		ViewBean vb = new ViewBean(name, realm);
		String requestURL = request.getRequestURL().toString();
		SessionManager.getCurrentToolSession().setAttribute(
				RWikiServlet.SAVED_REQUEST_URL, requestURL + vb.getViewUrl());
		return;
	}

	private void successfulUpdateDispatch(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		RequestDispatcher rd = request.getRequestDispatcher(successfulPath);
		rd.forward(request, response);
	}

	private void contentChangedDispatch(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		ErrorBean errorBean = ErrorBeanHelper.getErrorBean(request);
		// FIXME internationalise this!!
		errorBean
				.addError("Content has changed since you last viewed it. Please update the new content or overwrite it with the submitted content.");
		RequestDispatcher rd = request.getRequestDispatcher(contentChangedPath);
		rd.forward(request, response);
	}

	private void noUpdateAllowed(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		ErrorBean errorBean = ErrorBeanHelper.getErrorBean(request);
		// FIXME internationalise this!!
		errorBean.addError("You do not have permission to update this page.");
		RequestDispatcher rd = request.getRequestDispatcher(noUpdatePath);
		rd.forward(request, response);
	}

	public String getContentChangedPath()
	{
		return contentChangedPath;
	}

	public void setContentChangedPath(String contentChangedPath)
	{
		this.contentChangedPath = contentChangedPath;
	}

	public String getNoUpdatePath()
	{
		return noUpdatePath;
	}

	public void setNoUpdatePath(String noUpdatePath)
	{
		this.noUpdatePath = noUpdatePath;
	}

	public RWikiObjectService getObjectService()
	{
		return objectService;
	}

	public void setObjectService(RWikiObjectService objectService)
	{
		this.objectService = objectService;
	}

	public String getSuccessfulPath()
	{
		return successfulPath;
	}

	public void setSuccessfulPath(String successfulPath)
	{
		this.successfulPath = successfulPath;
	}

}
