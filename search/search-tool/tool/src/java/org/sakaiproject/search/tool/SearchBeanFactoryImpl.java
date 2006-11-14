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

package org.sakaiproject.search.tool;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;

/**
 * @author ieb
 */
public class SearchBeanFactoryImpl implements SearchBeanFactory
{

	private static Log log = LogFactory.getLog(SearchBeanFactoryImpl.class);

	private SearchService searchService;

	private SiteService siteService;

	private ToolManager toolManager;

	private SessionManager sessionManager;

	private ServletContext context;

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		sessionManager = (SessionManager) load(cm, SessionManager.class
				.getName());
		searchService = (SearchService) load(cm, SearchService.class.getName());
		siteService = (SiteService) load(cm, SiteService.class.getName());
		toolManager = (ToolManager) load(cm, ToolManager.class.getName());
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

	public SearchBean newSearchBean(HttpServletRequest request, String sortName, String filterName) throws PermissionException
	{
		try
		{
			SearchBeanImpl searchBean = new SearchBeanImpl(request, sortName, filterName,
					searchService, siteService, toolManager);

			return searchBean;
		}
		catch (IdUnusedException e)
		{
			throw new RuntimeException(
					"You must access the Search through a woksite");
		}
	}

	public OpenSearchBean newOpenSearchBean(HttpServletRequest request) throws PermissionException
	{
		try
		{
			OpenSearchBean openSearchBean = new OpenSearchBeanImpl(request, 
					searchService, siteService, toolManager);

			return openSearchBean;
		}
		catch (IdUnusedException e)
		{
			throw new RuntimeException(
					"You must access the Search through a woksite");
		}
	}

	public SherlockSearchBean newSherlockSearchBean(HttpServletRequest request) throws PermissionException
	{
		try
		{
			SherlockSearchBean sherlockSearchBean = 
				new SherlockSearchBeanImpl(request, 
					context, 
					searchService, siteService, toolManager);

			return sherlockSearchBean;
		}
		catch (IdUnusedException e)
		{
			throw new RuntimeException(
					"You must access the Search through a woksite");
		}	
	}

	/**
	 * @return the context
	 */
	public ServletContext getContext()
	{
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(ServletContext context)
	{
		this.context = context;
	}

}
