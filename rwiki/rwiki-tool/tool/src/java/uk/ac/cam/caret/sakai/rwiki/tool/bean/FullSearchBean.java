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

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.tool.api.ToolManager;

import uk.ac.cam.caret.sakai.rwiki.tool.util.WikiPageAction;

/**
 * Bean for helping with the search view
 * 
 * @author andrew
 */
public class FullSearchBean
{

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
	private SearchService searchService;

	/**
	 * Time taken
	 */
	private double timeTaken = 0;

	/**
	 * The number of results per page
	 */
	private int pagesize = 20;

	/**
	 * The number of list links
	 */
	private int nlistPages = 5;

	/**
	 * The default request page
	 */
	private int requestPage = 0;

	/**
	 * The current search list
	 */
	private SearchList searchResults;

	private ToolManager toolManager;

	/**
	 * Creates a searchBean
	 * 
	 * @param search
	 * @param user
	 * @param realm
	 * @param objectService
	 */
	public FullSearchBean(String search, String requestPage, String realm,
			SearchService searchService, ToolManager toolManager)
	{
		this.search = search;
		this.realm = realm;
		this.searchService = searchService;
		this.toolManager = toolManager;
		try
		{
			this.requestPage = Integer.parseInt(requestPage);
		}
		catch (Exception ex)
		{
		}
	}

	/**
	 * Set the RWikiObjectService for searching from
	 * 
	 * @param objectService
	 */
	public void setRWikiObjectService(SearchService searchService)
	{
		this.searchService = searchService;
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

	public String getTimeTaken()
	{
		int tt = (int) timeTaken;
		return String.valueOf(tt);
	}

	/**
	 * Perform the search
	 * 
	 * @return a list of page names that match the search criteria
	 */
	public List search()
	{
		if (searchResults == null)
		{
			List l = new ArrayList();

			l.add(toolManager.getCurrentPlacement().getContext());

			long start = System.currentTimeMillis();
			int searchStart = requestPage * pagesize;
			int searchEnd = searchStart + pagesize;
			searchResults = searchService.search(search, l, searchStart,
					searchEnd);
			long end = System.currentTimeMillis();
			timeTaken = 0.001 * (end - start);
		}
		return searchResults;
	}

	public List getSearchPages()
	{
		SearchList sr = (SearchList) search();
		int npages = sr.getFullSize() / pagesize;
		List pages = new ArrayList();
		int cpage = requestPage - (nlistPages / 2);
		if (cpage < 0)
		{
			cpage = 0;
		}
		int lastPage = Math.min(cpage + nlistPages, npages);
		while (cpage <= lastPage)
		{
			pages.add(new PageLink(cpage));
			cpage++;
		}
		return pages;
	}

	public class PageLink
	{
		private int pagenum = 0;

		public PageLink(int pagenum)
		{
			this.pagenum = pagenum;
		}

		public String getPage()
		{
			return String.valueOf(pagenum);
		}

		public String getPageNum()
		{
			return String.valueOf(pagenum);
		}

		public String getFullSearchLinkUrl()
		{
			return "?"
					+ ViewBean.ACTION_URL_ENCODED
					+ "="
					+ ViewBean.urlEncode(WikiPageAction.FULL_SEARCH_ACTION
							.getName()) + "&" + ViewBean.SEARCH_URL_ENCODED
					+ "=" + ViewBean.urlEncode(search) + "&"
					+ ViewBean.PAGE_URL_ENCODED + "="
					+ ViewBean.urlEncode(String.valueOf(pagenum)) + "&"
					+ ViewBean.REALM_URL_ENCODED + "="
					+ ViewBean.urlEncode(realm) + "&"
					+ ViewBean.PANEL_URL_ENCODED + "="
					+ ViewBean.MAIN_URL_ENCODED;
		}

	}

	/**
	 * @return Returns the nlistPages.
	 */
	public int getNlistPages()
	{
		return nlistPages;
	}

	/**
	 * @param nlistPages
	 *        The nlistPages to set.
	 */
	public void setNlistPages(int nlistPages)
	{
		this.nlistPages = nlistPages;
	}

	/**
	 * @return Returns the pagesize.
	 */
	public int getPagesize()
	{
		return pagesize;
	}

	/**
	 * @param pagesize
	 *        The pagesize to set.
	 */
	public void setPagesize(int pagesize)
	{
		this.pagesize = pagesize;
	}

	/**
	 * @return Returns the requestPage.
	 */
	public int getRequestPage()
	{
		return requestPage;
	}

	/**
	 * @param requestPage
	 *        The requestPage to set.
	 */
	public void setRequestPage(int requestPage)
	{
		this.requestPage = requestPage;
	}

	/**
	 * @param timeTaken
	 *        The timeTaken to set.
	 */
	public void setTimeTaken(double timeTaken)
	{
		this.timeTaken = timeTaken;
	}

	public int getNresults()
	{
		return 0; // TODO: SearchsearchResults.getFullSize();
	}

}
