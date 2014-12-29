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

package org.sakaiproject.search.tool.api;

import java.util.List;

import org.sakaiproject.search.tool.model.SearchOutputItem;
import org.sakaiproject.search.tool.model.SearchPage;
import org.sakaiproject.search.tool.model.SearchTerm;

/**
 * An interface describing the backing bean to the search page
 * 
 * @author ieb
 */
public interface SearchBean
{

	/**
	 * Title for the search page
	 * 
	 * @return
	 */
	String getSearchTitle();

	/**
	 * true if the user has admin rights
	 * 
	 * @return
	 */
	boolean hasAdmin();

	/**
	 * Gets the base url for the tool
	 * 
	 * @return
	 */
	String getToolUrl();

	/**
	 * The search text
	 * 
	 * @return
	 */
	String getSearch();

	
	/**
	 * returns true if search isEnabled
	 * @return
	 */
	boolean isEnabled();

	/**
	 * Get a formatted list of terms
	 * @param format format is {0} is the term {1} is the frequency
	 * @return
	 */

	String getTerms(String format);

	/**
	 * Returns true if a search has been performed and there are some results
	 * @return
	 */
	boolean hasResults();
	
	/**
	 * Get the OpensearchURL
	 * @return
	 */
	String getOpenSearchUrl();
	
	/**
	 * Get the site title 
	 * @return
	 */
	String getSiteTitle();
	
	/**
	 * get the base URL
	 * @return
	 */
	String getBaseUrl();
	
	/**
	 * get the name of the system
	 * @return
	 */
	String getSystemName();
	
	
	/** 
	 * get the found string
	 * @return
	 */
	String getSearchFound();
	
	/**
	 * get a pager objects
	 * @return
	 */
	List<SearchPage> getPages();
	
	/**
	 * get the results for the current pages
	 * @return
	 */
	List<SearchOutputItem> getResults();
	
	/**
	 * get the terms on the current page
	 * @return
	 */
	List<SearchTerm> getTerms();

	
	/**
	 * does the page have an error
	 * @return
	 */
	boolean hasError();
	
	/**
	 * get the error message
	 * @return
	 */
	String getErrorMessage();

	/**
	 * get the RSS URL
	 * @return
	 */
	String getRssURL();
	
	/**
	 * get the date now in RFS-822 format
	 * @return
	 */
	String getDateNow();
	
	/**
	 * get the request URL
	 * @return
	 */
	String getRequestUrl();

	/**
	 * @return
	 */
	int getNresults();

	/**
	 * @return
	 */
	boolean foundNoResults();

	/**
	 * @return
	 */
	String getSherlockIconUrl();

	/**
	 * @return
	 */
	String getSherlockUpdateUrl();

	/**
	 * @return
	 */
	String getPortalBaseUrl();

	boolean isScope(String scope);
	
	String getSuggestion();
	
	boolean hasSuggestion();
	
	String getSuggestionUrl();
	
	/**
	 * SRCH-96
	 * whether the node is search server or not
	 * @return
	 */
	boolean isSearchServer();
}
