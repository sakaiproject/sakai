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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.tool.api;

import java.io.UnsupportedEncodingException;
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
	 * get an html fragmnent representing the search results
	 * 
	 * @param searchItemFormat
	 *        A Message format string {0} is the result index, {1} is the item
	 *        UR, {2} is the item title, {3} is the content fragment, {4} is the
	 *        score
	 * @param errorFeedbackFormat {0} is the error message location
	 * @return
	 * @deprecated
	 */
	String getSearchResults(String searchItemFormat, String errorFeedbackFormat);

	/**
	 * get an html fragment representing a pager the
	 * 
	 * @param pagerFormat
	 *        A MessageFormat format string {0} is the page URL, {1} is the page
	 *        text, {2} is a css class id, 0 for first, 1 for middle, 2 for end
	 * @return
	 * @deprecated
	 */
	String getPager(String pagerFormati, String singlePageFormat) throws UnsupportedEncodingException;

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
	 * Format the header, param {0} is the start doc on the page {1} is the end
	 * doc {2} is the total docs, {3} is the time taken.
	 * 
	 * @param headerFormat
	 * @return
	 * @deprecated
	 */
	String getHeader(String headerFormat);
	
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
	

	
}
