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
package uk.ac.cam.caret.sakai.rwiki.tool.bean;

// FIXME: Tool

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
