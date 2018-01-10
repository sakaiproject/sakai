/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Collator;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.TermFrequency;
import org.sakaiproject.search.tool.api.SearchBean;
import org.sakaiproject.search.tool.model.SearchOutputItem;
import org.sakaiproject.search.tool.model.SearchPage;
import org.sakaiproject.search.tool.model.SearchTerm;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.StringUtil;

/**
 * Implementation of the search bean backing bean
 * 
 * @author ieb
 */
@Slf4j
 public class SearchBeanImpl implements SearchBean
{

	public static final String SEARCH_SITE_IDS = "search_site_ids";

	/**
	 * The searhc string parameter name
	 */
	private static final String SEARCH_PARAM = "search";
	
	/**
	 * Where we should be searching (current site, all sites I'm a member of)
	 */
	private static final String SEARCH_SCOPE = "scope";
	
	public static enum Scope {SITE, MINE};

	/**
	 * The results page
	 */
	private static final String SEARCH_PAGE = "page";

	public static final String SEARCH_ALL_SITES = "search_all_sites";

	/**
	 * The search criteria
	 */
	private String search;
	
	
	//private Scope scope = Scope.SITE;
	private Scope scope = null;
	
	/**
	 * The Search Service to use
	 */
	private SearchService searchService;

	private SiteService siteService;

	/**
	 * Time taken
	 */
	private double timeTaken = 0;

	/**
	 * The number of results per page
	 */
	private int pagesize = 10;

	/**
	 * The number of list links
	 */
	private int nlistPages = 5;

	/**
	 * The default request page
	 */
	private int requestPage = 0;
	
	
	private int censoredResults = 0;

	/**
	 * The current search list
	 */
	private SearchList searchResults;

	private String placementId;

	private String toolId;
	
	private ToolManager toolManager;

	private SecurityService securityService;

	private ServerConfigurationService serverConfigurationService;
	
	private String siteId;

	private String sortName = "normal";

	private String filterName = "normal";

	private String errorMessage;

	private List<TermFrequency> termsVectors;

	private List<TermHolder> termList;

	private Site currentSite;

	private int nTerms = 100;

	private List<SearchTerm> finalTermList;

	private int topTerms = 10;

	private boolean relativeTerms = true;

	private float divisorTerms = 3;

	private String requestURL;
	
	private String currentUser;

	private List<SearchOutputItem> outputItems;
	
	
	private String searchTermSuggestion = null;
	
	private boolean inPDAPortal = false;
	
	// Empty constructor to aid in testing.
	 
	public SearchBeanImpl(String siteId, SearchService ss, String search,ToolManager tm,
			SecurityService securityService, ServerConfigurationService serverConfigurationService) {
		super();
		this.siteId = siteId;
		this.searchService = ss;
		this.search = search;
		this.toolManager = tm;
		this.serverConfigurationService = serverConfigurationService;
		this.securityService = securityService;
	}
	
	/**
	 * Creates a searchBean
	 * 
	 * @param request
	 *        The HTTP request
	 * @param searchService
	 *        The search service to use
	 * @param siteService
	 *        the site service
	 * @param portalService
	 *        the portal service
	 * @throws IdUnusedException
	 *         if there is no current worksite
	 */
	public SearchBeanImpl(HttpServletRequest request, SearchService searchService,
			SiteService siteService, ToolManager toolManager, UserDirectoryService userDirectoryService, SecurityService securityService, ServerConfigurationService serverConfigurationService) throws IdUnusedException
	{
		this.search = request.getParameter(SEARCH_PARAM);
		this.searchService = searchService;
		this.siteService = siteService;
		this.toolManager = toolManager;
		this.placementId = this.toolManager.getCurrentPlacement().getId();
		this.toolId = this.toolManager.getCurrentTool().getId();
		this.siteId = this.toolManager.getCurrentPlacement().getContext();
		this.serverConfigurationService = serverConfigurationService;
		this.securityService = securityService;
		try
		{
			this.requestPage = Integer.parseInt(request.getParameter(SEARCH_PAGE));
		}
		catch (Exception ex)
		{
			log.debug(ex.getMessage());

		}
		currentSite = this.siteService.getSite(this.siteId);
		try
		{
			this.scope = Scope.valueOf(request.getParameter(SEARCH_SCOPE));
		}
		catch (NullPointerException npe)
		{}
		catch (IllegalArgumentException iae)
		{
			log.debug(iae.getMessage());
			log.warn("Invalid Scope Supplied: "+ request.getParameter(SEARCH_SCOPE));

		}
		
		User user = userDirectoryService.getCurrentUser();
		if (user != null)
			currentUser = user.getId();
		
		requestURL = request.getRequestURL().toString();
		
		// normal or pda portal?
		checkPdaPortal(request);
	}
	
	/**
	 * check whether pda portal is being used now?
	 */
	private void checkPdaPortal(HttpServletRequest req)
	{
		// recognize what to do from the path
		String option = req.getContextPath();
		String[] parts = {};
		if (option != null && !"/".equals(option))
		{
			//get the parts (the first will be "")
			parts = option.split("/");
		}
		if ((parts.length >= 2) && (parts[2].equals("pda")))
		{
			inPDAPortal = true;
		}
	}

	/**
	 * @see #SearchBeanImpl(HttpServletRequest, SearchService, SiteService, ToolManager)
	 */
	public SearchBeanImpl(HttpServletRequest request, String sortName,
			String filterName, SearchService searchService,
			SiteService siteService, ToolManager toolManager,
			UserDirectoryService userDirectoryService, SecurityService securityService, ServerConfigurationService serverConfigurationService) throws IdUnusedException
	{
		this(request, searchService, siteService, toolManager, userDirectoryService, securityService, serverConfigurationService);
		this.sortName = sortName;
		this.filterName = filterName;
	}



	private void loadTermVectors()
	{
		
		List<SearchResult> searchResults = search();
		if (searchResults != null)
		{
			termsVectors = new ArrayList<TermFrequency>();
			termList = null;
			for (Iterator<SearchResult> i = searchResults.iterator(); i.hasNext();)
			{

				SearchResult sr = (SearchResult) i.next();
				try
				{
					TermFrequency tf = sr.getTerms();
					if (tf != null)
					{
						termsVectors.add(sr.getTerms());
					}
				}
				catch (IOException e)
				{
					log.warn("Failed to get term vector ", e);
				}
			}
		}
	}

	/**
	 * @deprecated
	 */
	public String getTerms(String format)
	{
		List<SearchTerm> l = getTerms();
		StringBuilder sb = new StringBuilder();
		for (Iterator li = l.iterator(); li.hasNext();)
		{
			SearchTerm t = (SearchTerm) li.next();
			sb.append(MessageFormat.format(format, new Object[] { t.getName(),
					t.getWeight() }));
		}
		return sb.toString();
	}

	protected class TermHolder
	{
		public int position;

		protected String term;

		protected int frequency;

	}

	private void mergeTerms()
	{
		if (termsVectors == null)
		{
			loadTermVectors();
		}
		if (termsVectors == null)
		{
			return;
		}
		HashMap<String, TermHolder> hm = new HashMap<String, TermHolder>();
		for (Iterator i = termsVectors.iterator(); i.hasNext();)
		{
			TermFrequency tf = (TermFrequency) i.next();
			String[] terms = tf.getTerms();
			int[] freq = tf.getFrequencies();
			for (int ti = 0; ti < terms.length; ti++)
			{
				TermHolder h = (TermHolder) hm.get(terms[ti]);
				if (h == null)
				{
					h = new TermHolder();
					h.term = terms[ti];
					h.frequency = freq[ti];
					hm.put(terms[ti], h);
				}
				else
				{
					h.frequency += freq[ti];
				}
			}
		}
		termList = new ArrayList<TermHolder>();
		termList.addAll(hm.values());
		Collections.sort(termList, new Comparator<TermHolder>()
		{

			public int compare(TermHolder a, TermHolder b)
			{

				return b.frequency - a.frequency;
			}

		});

	}



	public boolean isEnabled()
	{
		return ("true".equals(serverConfigurationService.getString("search.enable",
				"false")));

	}
	
	public boolean isScope(String scope)
	{
		if (this.scope == null)
			this.scope = getDefaultSearchScope();
		
		return this.scope.equals(Scope.valueOf(scope));
	}



	/**
	 * Gets the current search request
	 * 
	 * @return current search request
	 */
	public String getSearch()
	{
		if (search == null) return "";
		return FormattedText.escapeHtml(search, false);
	}

	/**
	 * The time taken to perform the search only, not including rendering
	 * 
	 * @return
	 */
	public String getTimeTaken()
	{
		int tt = (int) timeTaken;
		return String.valueOf(tt);
	}

	
	/* assemble the list of search sites */
	
	protected List<String> getSearchSites (String[] toolPropertySiteIds) {
		List<String> l = new ArrayList<String>();
		
		l.add(this.siteId);
		if (toolPropertySiteIds != null) {
			String[] searchSiteIds = toolPropertySiteIds;

			for(int i = 0;i<searchSiteIds.length;i++){
				String ss = searchSiteIds[i];
				if (searchSiteIds[i].length() > 0) l.add(ss);
			}
		}
		if (scope != null && scope.equals(Scope.MINE)) {
			l.addAll(Arrays.asList(getAllUsersSites()));
		}
		
		 return l;
	}

	protected String[] getToolPropertySiteIds() {
		Properties props = extractPropertiesFromTool();
		String[] searchSiteIds;
		searchSiteIds = extractSiteIdsFromProperties(props);
		return searchSiteIds;
	}
	
	/**
	 * Get all the sites a user has access to.
	 * @return An array of site IDs.
	 */
	protected String[] getAllUsersSites() {
		List<Site> sites = siteService.getSites(
				org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
				null, null, null, null, null);
		List<String> siteIds = new ArrayList<String>(sites.size());
		for (Site site: sites) {
			if (site != null && site.getId() != null) {
				siteIds.add(site.getId());
			}
		}
		siteIds.add(siteService.getUserSiteId(currentUser));
		return siteIds.toArray(new String[siteIds.size()]);
	}

	/* get any site ids that are in the tool property and normalize the string.
	 * 
	 */
	protected String[] extractSiteIdsFromProperties(Properties props) {
	//	Properties props = extractPropertiesFromTool();
		
		String targetSiteId = StringUtils.trimToNull(props.getProperty(SEARCH_SITE_IDS));
		if (targetSiteId == null) return new String[] {""};
		String[] searchSiteIds = StringUtil.split(targetSiteId, ",");
		for(int i = 0;i<searchSiteIds.length;i++){
			searchSiteIds[i] = StringUtil.trimToZero(searchSiteIds[i]);
		}
		return searchSiteIds;
	}

	protected Properties extractPropertiesFromTool() {
		Placement placement = toolManager.getCurrentPlacement();
		Properties props = placement.getPlacementConfig();
		if(props.isEmpty())
			props = placement.getConfig();
		return props;
	}
	/**
	 * Perform the search
	 * 
	 * @return a list of page names that match the search criteria
	 */
	public SearchList search()
	{

		if (searchResults == null && errorMessage == null)
		{
			if (search != null && search.trim().length() > 0)
			{

				List<String> l = getSearchSites(getToolPropertySiteIds());
				long start = System.currentTimeMillis();
				int searchStart = requestPage * pagesize;
				int searchEnd = searchStart + pagesize;
				try
				{
					searchResults = searchService.search(search, l, searchStart,
							searchEnd, filterName, sortName);
					if (searchResults != null && searchResults.size() < 3) {
						if ((searchResults.size() > 0 && searchResults.get(0).getScore() < 1)) {
							log.debug("closest match: " + searchResults.get(0).getScore());
							String sug = searchService.getSearchSuggestion(search);
							log.debug("got suggestion: " + sug);
							this.searchTermSuggestion = sug;
						} else if (searchResults.size() == 0) {
							log.debug("No hits getting suggestion");
							String sug = searchService.getSearchSuggestion(search);
							log.debug("got suggestion: " + sug);
							this.searchTermSuggestion = sug;
						}
					} else if (searchResults == null || searchResults.size() == 0) {
						log.debug("No hits getting suggestion");
						String sug = searchService.getSearchSuggestion(search);
						log.debug("got suggestion: " + sug);
						this.searchTermSuggestion = sug;
					}
				}
				catch (Exception ex)
				{
					errorMessage = ex.getMessage();
					log.warn("Search Error encoutered, generated by a user action "
							+ ex.getClass().getName() + ":" + ex.getMessage());
					log.debug("Search Error Traceback ", ex);

				}
				long end = System.currentTimeMillis();
				timeTaken = 0.001 * (end - start);
			}
		}

		return searchResults;
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
	 * The Total number of results
	 * 
	 * @return
	 */
	public int getNresults()
	{
		return searchResults.getFullSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSearchTitle()
	{
		return Messages.getString("search_title") + " " + getSearch();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasAdmin()
	{
		boolean superUser = securityService.isSuperUser();
		return (superUser)
				|| ("true".equals(serverConfigurationService.getString(
						"search.allow.maintain.admin", "false")) && siteService
						.allowUpdateSite(siteId));
	}

	/**
	 * {@inheritDoc}
	 */
	public String getToolUrl()
	{

		if (inPDAPortal)
		{
			return serverConfigurationService.getString("portalPath") + "/pda/" + siteId + "/tool/"
			+ placementId;
		}
		else
		{
			return serverConfigurationService.getString("portalPath") + "/site/" +  siteId + "/tool/"
					+ placementId;
		}
	}

	public boolean hasResults()
	{
		SearchList sr = (SearchList) search();
		if (sr == null)
		{
			return false;
		}
		else
		{
			return (sr.size() > 0);
		}
	}
	public boolean foundNoResults() {
		if ( search == null || search.trim().length() == 0  ) {
			return false;
		}
		return !hasResults();
	}

	public String getOpenSearchUrl()
	{
		return serverConfigurationService.getPortalUrl() + "/tool/" + placementId
				+ "/opensearch";
	}
	
	
	public String getSherlockIconUrl()
	{
		return FormattedText.escapeHtml(getBaseUrl() + SherlockSearchBeanImpl.UPDATE_IMAGE,false);
	}

	public String getSherlockUpdateUrl()
	{
		return FormattedText.escapeHtml(getBaseUrl() + SherlockSearchBeanImpl.UPDATE_URL,false);
	}

	public String getBaseUrl()
	{
		return serverConfigurationService.getPortalUrl() + "/tool/" + placementId;
	}
	
	public String getPortalBaseUrl()
	{
		return serverConfigurationService.getPortalUrl() + "/directtool/" + placementId;
	}

	public String getSiteTitle()
	{
		return FormattedText.escapeHtml(currentSite.getTitle(), false);
	}

	public String getSystemName()
	{
		return FormattedText.escapeHtml(serverConfigurationService.getString(
				"ui.service", "Sakai"), false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.tool.SearchBean#getPages()
	 */
	public List<SearchPage> getPages()
	{
		List<SearchPage> pages = new ArrayList<SearchPage>();
		try
		{
			SearchList sr = (SearchList) search();
			if (sr == null) return pages;
			int npages = (sr.getFullSize()-1) / pagesize;
			int cpage = requestPage - (nlistPages / 2);
			if (cpage < 0)
			{
				cpage = 0;
			}
			int lastPage = Math.min(cpage + nlistPages, npages);
			boolean first = true;
			if (cpage == lastPage)
			{
				return pages;
			}
			else
			{
				while (cpage <= lastPage)
				{
					final String searchURL = "?search="
							+ URLEncoder.encode(search, "UTF-8") + "&page="
							+ String.valueOf(cpage)+"&scope=" + this.scope;

					final String name = String.valueOf(cpage + 1);
					String cssInd = "1";
					if (first)
					{
						cssInd = "0";
						first = false;
					}
					else if (cpage == (lastPage))
					{
						cssInd = "2";
					}
					pages.add(new SearchPage()
					{

						public String getName()
						{
							return FormattedText.escapeHtml(name, false);
						}

						public String getUrl()
						{
							return FormattedText.escapeHtml(searchURL, false);
						}

					});
					cpage++;
				}
			}

		}
		catch (Exception ex)
		{
			log.debug(ex.getMessage());
		}
		return pages;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.tool.SearchBean#getResults()
	 */
	@SuppressWarnings("unchecked")
	public List<SearchOutputItem> getResults()
	{
		if (outputItems == null) {
			
		outputItems = new ArrayList<SearchOutputItem>();
		SearchList sl = search();
		for (Iterator<SearchResult> i = sl.iterator(); i.hasNext();)
		{
			final SearchResult sr = (SearchResult) i.next();
			
			if (!sr.isCensored()) {
				outputItems.add(new SearchOutputItem()
				{

					public String getSearchResult()
					{
						try
						{
							return sr.getSearchResult();
						}
						catch (Exception ex)
						{
							return "";
						}
					}

					public String getTitle()
					{
						try
						{
							return FormattedText.escapeHtml(sr.getTitle(), false);
						}
						catch (Exception ex)
						{
							return "";
						}

					}

					public String getTool()
					{
						try
						{
							return FormattedText.escapeHtml(sr.getTool(), false);
						}
						catch (Exception ex)
						{
							return "";
						}

					}

					public String getUrl()
					{
						try
						{
							return FormattedText.escapeHtml(sr.getUrl(), false);
						}
						catch (Exception ex)
						{
							return "";
						}



					}

					private Site site = null;

					public String getSiteURL() {

						if (site == null)
							site = getSite();

						return (site != null) ? site.getUrl() : null;
					}

					public String getSiteTitle() {
						if (site == null)
							site = getSite();

						if (site != null)
							return FormattedText.escapeHtml(site.getTitle(), false);


						return "";

					}

					private Site getSite() {
						try {
							Site s = siteService.getSite(sr.getSiteId());
							return s;
						} catch (IdUnusedException e) {
							log.warn("site: " + sr.getSiteId() + "referenced in search results doesn't exits");
						}

						return null;
					}

					public boolean isVisible() {
						if (sr.isCensored())
							return false;
						else 
							return true;
					}

					public boolean hasPortalUrl() {
						return sr.hasPortalUrl();
					}



		
				});
			
			} else {
				censoredResults++;
			}
		
		}
		}
		
		return outputItems;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.tool.SearchBean#getSearchFound()
	 */
	public String getSearchFound()
	{
		SearchList sr = (SearchList) search();
		if (sr == null) return "";
		int total = sr.getFullSize();
		int start = 0;
		int end = 0;
		if (total > 0)
		{
			start = sr.getStart();
			end = Math.min(start + sr.size(), total);
			start++;
		}
		return MessageFormat.format(Messages.getString("jsp_found_line"), new Object[] {
				Integer.valueOf(start), Integer.valueOf(end), Integer.valueOf(total),
				Double.valueOf(timeTaken) });

	}
	
	public String getSearchFoundCensored()
	{
		if (this.getCensoredResultCount() == 1)
			return MessageFormat.format(Messages.getString("jsp_results_no_perm_singular"), new Object[] { censoredResults });
		else
			return MessageFormat.format(Messages.getString("jsp_results_no_perm_plural"), new Object[] { censoredResults });
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.tool.SearchBean#getTerms()
	 */
	public List<SearchTerm> getTerms()
	{
		if (termList == null)
		{
			mergeTerms();
			finalTermList = null;
		}
		if (termList == null)
		{
			return new ArrayList<SearchTerm>();
		}
		if (finalTermList != null)
		{
			return finalTermList;
		}
		finalTermList = new ArrayList<SearchTerm>();
		List<TermHolder> l = termList.subList(0, Math.min(nTerms, termList.size()));
		int j = 0;
		for (Iterator li = l.iterator(); li.hasNext();)
		{
			TermHolder t = (TermHolder) li.next();
			t.position = j;
			j++;
		}

		Collections.sort(l, new Comparator<TermHolder>()
		{
			Collator c = Collator.getInstance();

			public int compare(TermHolder a, TermHolder b)
			{
				return c.compare(a.term, b.term);
			}

		});
		int factor = 1;
		j = l.size();
		for (Iterator li = l.iterator(); li.hasNext();)
		{
			TermHolder t = (TermHolder) li.next();
			factor = Math.max(t.frequency, factor);
		}

		for (Iterator li = l.iterator(); li.hasNext();)
		{
			final TermHolder t = (TermHolder) li.next();
			float f = (float)(topTerms * t.frequency) / factor;
			if (relativeTerms)
			{
				f = (float)(topTerms * (l.size() - t.position)) / l.size();
			}
			f = f / divisorTerms;
			j--;
			final String weight = String.valueOf(f);
			final String searchScope = this.scope.name();
			finalTermList.add(new SearchTerm()
			{

				public String getName()
				{
					return FormattedText.escapeHtml(t.term, false);
				}

				public String getUrl()
				{
					try
					{
						return FormattedText
								.escapeHtml("?panel=Main&search=" + URLEncoder.encode(t.term,"UTF-8")+"&scope=" + searchScope, false);
					}
					catch (UnsupportedEncodingException e)
					{
						return FormattedText
						.escapeHtml("?panel=Main&search=" + URLEncoder.encode(t.term), false);

					}
				}

				public String getWeight()
				{
					return weight;
				}
			});
		}
		return finalTermList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.tool.SearchBean#hasError()
	 */
	public boolean hasError()
	{
		return (errorMessage != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.tool.SearchBean#getErrorMessage()
	 */
	public String getErrorMessage()
	{
		return FormattedText.escapeHtml(errorMessage, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.tool.api.SearchBean#getRssUrl()
	 */
	public String getRssURL()
	{
		if (hasResults())
		{

			try
			{
				return FormattedText.escapeHtml(getToolUrl() + "/rss20?search="
						+ URLEncoder.encode(search, "UTF-8")
						+ ((scope != null)?"&scope="+ URLEncoder.encode(scope.toString(), "UTF-8"):""), false);
			}
			catch (UnsupportedEncodingException e)
			{
				return FormattedText.escapeHtml(getToolUrl() + "/rss20?search="
						+ URLEncoder.encode(search)
						+ ((scope != null)?"&scope="+ URLEncoder.encode(scope.toString()):"") , false);
			}
		}
		else
		{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.tool.api.SearchBean#getDateNow()
	 */
	public String getDateNow()
	{
		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		return format.format(new Date(System.currentTimeMillis()));
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.tool.api.SearchBean#getRequestUrl()
	 */
	public String getRequestUrl()
	{
		return FormattedText.escapeHtml(requestURL, false);
	}

	private Boolean searchScopeSite = null;
	

	private Scope getDefaultSearchScope() {
		log.debug("setting default scope!");
		String siteId = toolManager.getCurrentPlacement().getContext();
		if (siteService.isUserSite(siteId)) {
			log.debug("got scope of Mine");
			return Scope.MINE;
		} else {
			log.debug("got scope of Site");
			return Scope.SITE;
		}

		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.tool.api.SearchBean#getCensoredResultCount()
	 */
	public int getCensoredResultCount() {
		getResults();
		return censoredResults;
	}

	public String getSuggestion() {
		return searchTermSuggestion;
	}

	public boolean hasSuggestion() {
		if (searchTermSuggestion != null) {
			return true;
		}
		return false;
	}

	public String getSuggestionUrl() {
		String searchURL;
		try {
			searchURL = "?search="
				+ URLEncoder.encode(searchTermSuggestion, "UTF-8") + "&scope=" + this.scope;
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
			return null;
		}
		return FormattedText.escapeHtml(searchURL, false);

	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSearchServer()
	{
		return searchService.isSearchServer();
	}
}
