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

import java.io.UnsupportedEncodingException;

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
	 * @return
	 */
	String getSearchResults(String searchItemFormat);

	/**
	 * get an html fragment representing a pager the
	 * 
	 * @param pagerFormat
	 *        A MessageFormat format string {0} is the page URL, {1} is the page
	 *        text, {2} is a css class id, 0 for first, 1 for middle, 2 for end
	 * @return
	 */
	String getPager(String pagerFormat) throws UnsupportedEncodingException;

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
	 */
	String getHeader(String headerFormat);
	
	/**
	 * returns true if search isEnabled
	 * @return
	 */
	boolean isEnabled();
}
