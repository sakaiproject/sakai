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


import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.search.api.InvalidSearchQueryException;
import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.tool.api.ToolManager;

import uk.ac.cam.caret.sakai.rwiki.tool.util.WikiPageAction;

/**
 * Bean for helping with the search view
 * 
 * @author andrew
 */
@Slf4j
public class FullSearchBean
{

	/** Tool restriction: see RWikiEntityContentProducer.getTool() **/
	private static final String SEARCH_SUFFIX = " +tool:wiki";
		
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
	 * The default number of results
	 */
	private int nresults = 0;

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
		catch (NumberFormatException e) 
		{
			this.requestPage = 0;
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
	 * Parse wiki page name from search title obtained from search api
	 * 
	 * @return wiki page-name
	 */
	public String pageNameFromSearchTitle(String searchTitle)
	{
		if (searchTitle.startsWith(" /site/"))
		{
		String h = searchTitle.substring(searchTitle.lastIndexOf('/')+1);
		return h;
		}
		return searchTitle;
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
		log.debug("search()");
		if (searchResults == null)
		{
			List<String> l = new ArrayList<String>();

			l.add(toolManager.getCurrentPlacement().getContext());

			long start = System.currentTimeMillis();
			int searchStart = requestPage * pagesize;
			int searchEnd = searchStart + pagesize;
			try {
				searchResults = searchService.search(search.concat(SEARCH_SUFFIX), l, searchStart,
						searchEnd);
				long end = System.currentTimeMillis();
				timeTaken = end - start;
				nresults = searchResults.getFullSize();
			} 
			catch (InvalidSearchQueryException e) {
				log.error(e.getMessage(), e);
				return null;
				 
			}
			
		}
		log.debug("got a searchresult of: " + searchResults.size());
		return searchResults;
	}

	public List getSearchPages()
	{
		SearchList sr = (SearchList) search();
		//its possible for the searchlist to be null
		if (sr == null) {
			return null;
		}
		int npages = sr.getFullSize() / pagesize;
		List pages = new ArrayList();
		/*
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
		*/
		for (int i=0; i<npages; i++)
		{
		pages.add(new PageLink(i));
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
		return nresults;
	}

}
