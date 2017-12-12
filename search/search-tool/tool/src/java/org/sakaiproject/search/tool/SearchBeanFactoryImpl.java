/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
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

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.tool.api.OpenSearchBean;
import org.sakaiproject.search.tool.api.SearchAdminBean;
import org.sakaiproject.search.tool.api.SearchBean;
import org.sakaiproject.search.tool.api.SearchBeanFactory;
import org.sakaiproject.search.tool.api.SherlockSearchBean;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * @author ieb
 */
@Slf4j
public class SearchBeanFactoryImpl implements SearchBeanFactory
{

	private SearchService searchService;

	private SiteService siteService;

	private ToolManager toolManager;

	private SessionManager sessionManager;
	
	private UserDirectoryService userDirectoryService;

	private ServletContext context;
	
	private SecurityService securityService;
	private ServerConfigurationService serverConfigurationService;

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		sessionManager = (SessionManager) load(cm, SessionManager.class
				.getName());
		searchService = (SearchService) load(cm, SearchService.class.getName());
		siteService = (SiteService) load(cm, SiteService.class.getName());
		toolManager = (ToolManager) load(cm, ToolManager.class.getName());
		userDirectoryService = (UserDirectoryService) load(cm, UserDirectoryService.class.getName());
		securityService = (SecurityService)load(cm, SecurityService.class.getName());
		serverConfigurationService = (ServerConfigurationService) load(cm, ServerConfigurationService.class.getName());
		
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
			SearchBean searchBean = new SearchBeanImpl(request,
					searchService, siteService, toolManager, userDirectoryService, securityService, serverConfigurationService);

			return searchBean;
		}
		catch (IdUnusedException e)
		{
			throw new RuntimeException(
					Messages.getString("searchbeanfact_siteerror"));
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
					sessionManager, securityService, serverConfigurationService);
			return searchAdminBean;
		}
		catch (IdUnusedException e)
		{
			throw new RuntimeException(
					Messages.getString("searchbeanfact_siteerror"));
		}
	}

	public SearchBean newSearchBean(HttpServletRequest request, String sortName, String filterName) throws PermissionException
	{
		try
		{
			SearchBean searchBean = new SearchBeanImpl(request, sortName, filterName,
					searchService, siteService, toolManager, userDirectoryService, securityService, serverConfigurationService);

			return searchBean;
		}
		catch (IdUnusedException e)
		{
			throw new RuntimeException(
					Messages.getString("searchbeanfact_siteerror"));
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
					Messages.getString("searchbeanfact_siteerror"));
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
					Messages.getString("searchbeanfact_siteerror"));
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
