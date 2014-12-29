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

package uk.ac.cam.caret.sakai.rwiki.tool.bean;


import java.util.List;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;

/**
 * Bean for helping with the search view
 * 
 * @author andrew
 */
public class SearchBean
{

	/**
	 * Parameter name for requesting search terms.
	 */
	public static final String SEARCH_PARAM = "search";

	/**
	 * Parameter name for requesting search terms.
	 */
	public static final String PAGE_PARAM = "page";

	/**
	 * Parameter name for the realm to which the search is restricted.
	 */
	public static final String REALM_PARAM = "realm";

	/**
	 * The search criteria
	 */
	private String search;

	/**
	 * The realm to restrict the search to
	 */
	private String realm;

	/**
	 * RWikiObjectService to use
	 */
	private RWikiObjectService objectService;

	/**
	 * Creates a searchBean
	 * 
	 * @param search
	 * @param user
	 * @param realm
	 * @param objectService
	 */
	public SearchBean(String search, String realm,
			RWikiObjectService objectService)
	{
		this.search = search;
		this.realm = realm;
		this.objectService = objectService;
	}

	/**
	 * Set the RWikiObjectService for searching from
	 * 
	 * @param objectService
	 */
	public void setRWikiObjectService(RWikiObjectService objectService)
	{
		this.objectService = objectService;
	}

	/**
	 * Gets the current search request
	 * 
	 * @return current search request
	 */
	public String getSearch()
	{
		return search;
	}

	/**
	 * Gets the current search realm
	 * 
	 * @return current search realm
	 */
	public String getRealm()
	{
		return realm;
	}

	/**
	 * Perform the search
	 * 
	 * @return a list of page names that match the search criteria
	 */
	public List getSearchResults()
	{
		return search();
	}

	/**
	 * Perform the search
	 * 
	 * @return a list of page names that match the search criteria
	 */
	public List search()
	{
		return objectService.search(search, realm);
	}
}
