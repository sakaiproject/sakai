/**********************************************************************************
*
 * Copyright (c) 2003, 2004, 2007, 2008 The Sakai Foundation
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
package edu.indiana.lib.twinpeaks.search;

import java.lang.*;
import java.util.*;

import org.w3c.dom.*;

/**
 * Search characteristics, all matching items
 */
public interface SearchResultInterface {

	/**
	 * Save various attributes of the general search request
	 * @param query The QueryBase extension that sent the search request
	 */
  public void initialize(QueryBase query);

	/**
	 * Populate the search result list
	 */
	public void doParse();

	/**
	 * Fetch the original query text
	 * @return Search string
	 */
	public String getQuery();

	/**
	 * Return search results as a String
	 * @return Result Document
	 */
	public String getSearchResponseString();

	/**
	 * Return the starting item number for this search (one based)
	 * @return Starting item number
	 */
	public int getSearchStart();

	/**
	 * Return the count of matching items found
	 * @return Item count
	 */
	public int getMatchCount();

	/**
	 * Fetch the "next preview page" reference (used to paginate results
	 * null if none)
	 * @return Next page reference
	 */
	public String getNextPreviewPage();

	/**
	 * Fetch the "previous preview page" reference (used to paginate results,
	 * null if none)
	 * @return Previous page reference
	 */
	public String getPreviousPreviewPage();

	/**
	 * Can this display be paginated (next/previous pages for display)?
	 * @return true if so
	 */
	public boolean canPaginate();

	/**
	 * Get an iterator to the result list
	 * @return An iterator to the list of matching items
	 */
	public Iterator iterator();
}