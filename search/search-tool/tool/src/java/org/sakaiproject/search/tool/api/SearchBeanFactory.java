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

package org.sakaiproject.search.tool.api;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.exception.PermissionException;

/**
 * A factory bean to construct backing beans efficiently on a request basis.
 * This is normally injected into the request attributes for each individual
 * page to construct the backing beans that they require. Ideally there should
 * be a one to one mapping between backing beans and pages, and the page should
 * be as simple as possible
 * 
 * @author ieb
 */
public interface SearchBeanFactory
{

	public final static String SEARCH_BEAN_FACTORY_ATTR = SearchBean.class
			.getName();

	/**
	 * create a search bean based on the request
	 * 
	 * @param request
	 * @return
	 * @throws PermissionException
	 */
	SearchBean newSearchBean(HttpServletRequest request)
			throws PermissionException;
	
	SearchBean newSearchBean(HttpServletRequest request, String sortName, String filterName)
	throws PermissionException;

	/**
	 * Create a search admin bean based ont he request
	 * 
	 * @param request
	 * @return
	 * @throws PermissionException
	 */
	SearchAdminBean newSearchAdminBean(HttpServletRequest request)
			throws PermissionException;
	
	/**
	 * get a OpenSearchBean model
	 * @param request
	 * @return
	 * @throws PermissionException
	 */
	OpenSearchBean newOpenSearchBean(HttpServletRequest request)
		throws PermissionException;
	
	/**
	 * get a Sherlock SearchBean model
	 * @param request
	 * @return
	 * @throws PermissionException
	 */
	SherlockSearchBean newSherlockSearchBean(HttpServletRequest request)
		throws PermissionException;

	/**
	 * set the servlet context that this factory is associated with
	 * @param servletContext
	 */
	void setContext(ServletContext servletContext);
	
	

}
