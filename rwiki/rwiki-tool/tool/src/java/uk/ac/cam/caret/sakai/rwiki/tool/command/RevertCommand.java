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
import uk.ac.cam.caret.sakai.rwiki.service.exception.PermissionException;
import uk.ac.cam.caret.sakai.rwiki.service.exception.VersionException;
import uk.ac.cam.caret.sakai.rwiki.tool.RWikiServlet;
import uk.ac.cam.caret.sakai.rwiki.tool.RequestScopeSuperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.EditBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ErrorBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.HistoryBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.ViewParamsHelperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.command.helper.ErrorBeanHelper;

/**
 * @author andrew
 */
// FIXME: Tool
public class RevertCommand implements HttpCommand
{

	private static Log log = LogFactory.getLog(RevertCommand.class);

	private String successfulPath;

	private RWikiObjectService objectService;

	private String noUpdatePath;

	private String contentChangedPath;

	public void execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{

		RequestScopeSuperBean rssb = RequestScopeSuperBean
				.getFromRequest(request);

		ViewParamsHelperBean vphb = rssb.getNameHelperBean();

		String revision = request.getParameter(HistoryBean.REVISION_PARAM);
		String version = request.getParameter(HistoryBean.VERSION_PARAM);

		// interpret version as Date and revision as int
		Date versionDate = new Date(Long.parseLong(version));
		int revisionInt = Integer.parseInt(revision);

		String name = vphb.getGlobalName();
		String realm = vphb.getLocalSpace();

		try
		{
			objectService.revert(name, realm, versionDate, revisionInt);
			// objectService.update(name, user, realm, versionDate, content);
		}
		catch (VersionException e)
		{
			// treat like a save that hasn't completed properly.
			EditBean editBean = rssb.getEditBean();
			editBean.setPreviousRevision(revisionInt);
			editBean.setPreviousVersion(version);
			editBean.setSaveType("revert");

			this.contentChangedDispatch(request, response);
			return;
		}
		catch (PermissionException e)
		{
			// Fail to revert!
			this.noUpdateAllowed(request, response);
			return;
		}
		// Successful reversion
		this.successfulUpdateDispatch(request, response);
		// set the state to view
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

	public RWikiObjectService getRWikiObjectService()
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