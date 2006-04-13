/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2006 University of Cambridge
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

package org.sakaiproject.search;

import java.util.List;


/**
 * Provides a search interface
 * @author ieb
 *
 */
public interface SearchService {
	public static final String EVENT_TRIGGER_SEARCH = "search.update";
	public static final String EVENT_TRIGGER_INDEX_RELOAD = "search.index.reload";

	/**
	 * Perform a search, return results in a list.
	 * @param searchTerms the search terms
	 * @param contexts a list of contexts in which to perform the search
	 * @param searchEnd 
	 * @param searchStart 
	 * @return
	 */
	SearchList search( String searchTerms, List contexts, int searchStart, int searchEnd);
	
	/**
	 * Adds a function for the SearchService to respond to and route to 
	 * the index builder. EntityProducers that want their content to 
	 * be searched, should register the events that indicate new data
	 * to this 
	 * @param function
	 */
	void registerFunction(String function);

	/**
	 * When reload is called, the index should be reloaded
	 *
	 */
	void reload();
	
	/**
	 * Trigger an refresh of the whole index
	 *
	 */
	void refreshInstance();

	/**
	 * trigger a rebuild of the whole index
	 *
	 */
	void rebuildInstance();

	/**
	 * Refresh the current site only
	 * @param currentSiteId
	 */
	void refreshSite(String currentSiteId);

	/**
	 * rebuild the current site only
	 * @param currentSiteId
	 */
	void rebuildSite(String currentSiteId);

	/**
	 * get the status of the search service
	 * @return
	 */
	String getStatus();

	/**
	 * get the number of documents in the search index
	 * @return
	 */
	int getNDocs();

	/**
	 * get the number of pending documents in the search index
	 * @return
	 */
	int getPendingDocs();


}
