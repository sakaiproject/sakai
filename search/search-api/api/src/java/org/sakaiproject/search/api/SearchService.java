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

package org.sakaiproject.search.api;

import java.util.List;

/**
 * Provides a search interface
 * 
 * @author ieb
 */
public interface SearchService
{
	public static final String EVENT_TRIGGER_SEARCH = "search.update";

	public static final String EVENT_TRIGGER_INDEX_RELOAD = "search.index.reload";

	/**
	 * Perform a search, return results in a list.
	 * 
	 * @param searchTerms
	 *        the search terms
	 * @param contexts
	 *        a list of contexts in which to perform the search
	 * @param searchEnd
	 * @param searchStart
	 * @return
	 */
	SearchList search(String searchTerms, List contexts, int searchStart,
			int searchEnd);

	/**
	 * Adds a function for the SearchService to respond to and route to the
	 * index builder. EntityProducers that want their content to be searched,
	 * should register the events that indicate new data to this
	 * 
	 * @param function
	 */
	void registerFunction(String function);

	/**
	 * When reload is called, the index should be reloaded
	 */
	void reload();

	/**
	 * Trigger an refresh of the whole index
	 */
	void refreshInstance();

	/**
	 * trigger a rebuild of the whole index
	 */
	void rebuildInstance();

	/**
	 * Refresh the current site only
	 * 
	 * @param currentSiteId
	 */
	void refreshSite(String currentSiteId);

	/**
	 * rebuild the current site only
	 * 
	 * @param currentSiteId
	 */
	void rebuildSite(String currentSiteId);

	/**
	 * get the status of the search service
	 * 
	 * @return
	 */
	String getStatus();

	/**
	 * get the number of documents in the search index
	 * 
	 * @return
	 */
	int getNDocs();

	/**
	 * get the number of pending documents in the search index
	 * 
	 * @return
	 */
	int getPendingDocs();

	List getAllSearchItems();

	List getSiteMasterSearchItems();

	List getGlobalMasterSearchItems();

	SearchStatus getSearchStatus();

	boolean removeWorkerLock();
	
	



}
