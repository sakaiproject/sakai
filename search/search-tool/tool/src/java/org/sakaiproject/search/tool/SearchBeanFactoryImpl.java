/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2006 University of Cambridge
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

package org.sakaiproject.search.tool;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.search.SearchService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;

/**
 * @author ieb
 */
public class SearchBeanFactoryImpl implements SearchBeanFactory
{

	private SearchService searchService;

	private SiteService siteService;

	private ToolManager toolManager;

	private SessionManager sessionManager;

	/**
	 * {@inheritDoc}
	 * 
	 * @throws PermissionException
	 */
	public SearchBean newSearchBean(HttpServletRequest request)
	{
		try
		{
			SearchBeanImpl searchBean = new SearchBeanImpl(request,
					searchService, siteService, toolManager);

			return searchBean;
		}
		catch (IdUnusedException e)
		{
			throw new RuntimeException(
					"You must access the Search through a woksite");
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws PermissionException
	 */
	public SearchAdminBean newSearchAdminBean(HttpServletRequest request)
			throws PermissionException
	{
		try
		{
			SearchAdminBeanImpl searchAdminBean = new SearchAdminBeanImpl(
					request, searchService, siteService, toolManager,
					sessionManager);
			return searchAdminBean;
		}
		catch (IdUnusedException e)
		{
			throw new RuntimeException(
					"You must access the Search through a woksite");
		}
	}

	/**
	 * @return Returns the searchService.
	 */
	public SearchService getSearchService()
	{
		return searchService;
	}

	/**
	 * @param searchService
	 *        The searchService to set.
	 */
	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	/**
	 * @return Returns the portalService.
	 */
	public ToolManager getToolManager()
	{
		return toolManager;
	}

	/**
	 * @param portalService
	 *        The portalService to set.
	 */
	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
	}

	/**
	 * @return Returns the siteService.
	 */
	public SiteService getSiteService()
	{
		return siteService;
	}

	/**
	 * @param siteService
	 *        The siteService to set.
	 */
	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	/**
	 * @return Returns the sessionManager.
	 */
	public SessionManager getSessionManager()
	{
		return sessionManager;
	}

	/**
	 * @param sessionManager
	 *        The sessionManager to set.
	 */
	public void setSessionManager(SessionManager sessionManager)
	{
		this.sessionManager = sessionManager;
	}

}
