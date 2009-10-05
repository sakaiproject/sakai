/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.search.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * Provides a search interface
 * 
 * @author ieb
 */
public interface SearchService extends Diagnosable
{
	public static final String REST_USERID = "u";

	public static final String REST_TERMS = "q";

	public static final String REST_CHECKSUM = "cs";

	public static final String REST_CONTEXTS = "ctx";

	public static final String REST_START = "s";

	public static final String REST_END = "e";

	/**
	 * event to trigger an update of the index sent from Search Service to index
	 * builders
	 */
	public static final String EVENT_TRIGGER_SEARCH = "search.update";

	/**
	 * event to trigger a reload of the search index by query nodes
	 */
	public static final String EVENT_TRIGGER_INDEX_RELOAD = "search.index.reload";

	public static final String EVENT_SEARCH = "search.query";

	public static final String EVENT_SEARCH_REF = "/search/query/";

	/*
	 * The search fields being stored in the index
	 */
	/**
	 * Search Index Field the site id of the entity ( where is was produced)
	 */
	public static final String FIELD_SITEID = "siteid";

	/**
	 * Search Index Field the url to the entity
	 */
	public static final String FIELD_URL = "url";

	/**
	 * Search Field The Name of the Tool that owns the entity
	 */
	public static final String FIELD_TOOL = "tool";

	/**
	 * Seadch Field The title of the entity
	 */
	public static final String FIELD_TITLE = "title";

	/**
	 * Searhc Field (term vector, not full contents) The contents of the Entity
	 */
	public static final String FIELD_CONTENTS = "contents";

	/**
	 * Search Field The context of the Entity
	 */
	public static final String FIELD_CONTEXT = "context";

	/**
	 * Search Field The tool subtype of the entity
	 */
	public static final String FIELD_SUBTYPE = "subtype";

	/**
	 * Search Field The tool type of the entity
	 */
	public static final String FIELD_TYPE = "type";

	/**
	 * Search Field The Sakai id of the entity
	 */
	public static final String FIELD_ID = "id";

	/**
	 * Search Field the container of the entity
	 */
	public static final String FIELD_CONTAINER = "container";

	/**
	 * Search field The reference of the entity
	 */
	public static final String FIELD_REFERENCE = "reference";
	
	
	public static final String FIELD_DIGEST_COUNT = "digestCount";

	public static final String DATE_STAMP = "indexdate";

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
	 * This is the same as standard search, but the caller can specify, by name, the 
	 * index Filter and the index Sorter by name
	 * The Sorter and the Filter will be consulted during the search, and hence should not
	 * make massive demands on the framework, otherwise they will cripple the search 
	 * performance
	 * @param searchTerms A search string
	 * @param contexts A list of contexts
	 * @param start starting from
	 * @param end ending at
	 * @param filterName a lucene filter 
	 * @param sorterName a lucene sorter
	 * @return
	 */
	public SearchList search(String searchTerms, List contexts, int start,
			int end, String filterName, String sorterName);
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

	/**
	 * get all the search items in the index (must be a lazy load list)
	 * 
	 * @return
	 */
	List getAllSearchItems();

	/**
	 * get the master itemf to sthe site
	 * 
	 * @return
	 */
	List getSiteMasterSearchItems();

	/**
	 * get the global master items
	 * 
	 * @return
	 */
	List getGlobalMasterSearchItems();

	/**
	 * Get the status of the search engine
	 * 
	 * @return
	 */
	SearchStatus getSearchStatus();

	/**
	 * force the removal of the worker lock
	 * 
	 * @return
	 */
	boolean removeWorkerLock();

	List getSegmentInfo();

	/**
	 * Force a reload regardless of if the index has changed
	 *
	 */
	void forceReload();

	/**
	 * get the term vector for this document, where document is the 
	 * @param doc
	 * @return
	 * @throws IOException 
	 */
	TermFrequency getTerms(int documentId) throws IOException;


	/**
	 * generates a block of XML representign the search results
	 * @param parameterMap
	 * @return
	 */
	String searchXML(Map parameterMap);

	/**
	 * @return
	 */
	boolean isEnabled();


}
