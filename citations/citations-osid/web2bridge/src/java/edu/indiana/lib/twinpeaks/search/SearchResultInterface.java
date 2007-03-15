/**********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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