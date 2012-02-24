/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.citation.api;

import java.util.List;
import java.util.Map;

import org.sakaiproject.citation.util.api.SearchCancelException;
import org.sakaiproject.citation.util.api.SearchException;
import org.sakaiproject.citation.util.api.SearchQuery;


/**
 *
 */
public interface ActiveSearch
{
	public static final String BASIC_SEARCH_TYPE    = "basic";
	public static final String ADVANCED_SEARCH_TYPE = "advanced";

	/*******************************************************
	 * Search results
	 *******************************************************/

	/**
	 * Access the index of the first record in the current view page.  Record indexes start at 1.
	 * @return
	 */
	public int getFirstRecordIndex();

	/**
     * @return
     */
    public Map getIndex();

	/**
	 * Access the index of the last record in the current view page.  Record indexes start at 1,
	 * and the maximum legal value equals the size() of the full search results list or start
	 * index of the next page minus 1, which is start index of the current page plus the size of
	 * the current page minus 1.
	 * @return
	 */
	public int getLastRecordIndex();

	/**
	 * @return
	 */
	public Integer getNumRecordsFetched();

	/**
	 * @return
	 */
	public Integer getNumRecordsFound();

	/**
	 * @return
	 */
	public Integer getNumRecordsMerged();

	/**
	 * @return
	 */
	public Integer getPageSize();

	/**
     * @return
     */
    public String getRepositoryId();

	/**
	 * @return
	 */
	public String getRepositoryName();

	/**
	 * @return
	 */
	public SearchQuery getBasicQuery();

	/**
	 *
	 * @return
	 */
	public SearchQuery getAdvancedQuery();

	/**
	 * @return
	 */
	public String getSearchId();

	/**
	 * Gets the thread that is handling the active search
	 *
	 * @return Thread that is handling this active search
	 */
	public Thread getSearchThread();

	/**
	 *
	 * @return
	 */
	public String getSearchType();

	/**
	 * @return
	 */
	public CitationCollection getSearchResults();

	/**
	 * @return
	 */
	public String getSortBy();

    /**
	 * @return
	 */
	public Integer getStartRecord();

	/**
	 * Access the status message for this search.
	 * @return The current status message.
	 */
	public String getStatusMessage();

	/**
	 * Access the zero-based index indicating the page that was most recently accessed using the
	 * viewPage method.
	 * @return
	 */
	public int getViewPageNumber();

	/**
	 * Access the zero-based index indicating the page that was most recently accessed using the
	 * viewPage method.
	 * @return
	 */
	public int getViewPageSize();

	/**
	 * Get the databases to search
	 * @return An array of database IDs
	 */
	public String[] getDatabaseIds();

	/**
     * @return the firstPage
     */
    public boolean isFirstPage();

	/**
     * @return the lastPage
     */
    public boolean isLastPage();

	/**
     * @return the newSearch
     */
    public boolean isNewSearch();

	/**
     *
     */
    public void prepareForNextPage();

    /**
	 *
	 * @param advancedCriteria
	 */
	public void setAdvancedQuery(SearchQuery advancedCriteria);

	/**
	 * @param searchCriteria
	 */
	public void setBasicQuery(SearchQuery basicCriteria);

	/**
	 * @param firstPage
	 */
	public void setFirstPage( boolean firstPage );

	/**
     * @param index
     */
    public void setIndex(Map index);

	/**
	 * @param lastPage
	 */
	public void setLastPage( boolean lastPage );

	/**
	 * @param newSearch
	 */
	public void setNewSearch( boolean newSearch );

	/**
	 * @param numRecordsFetched
	 */
	public void setNumRecordsFetched( Integer numRecordsFetched );

	/**
	 * @param numRecordsFound
	 */
	public void setNumRecordsFound( Integer numRecordsFound );

	/**
	 * @param numRecordsMerged
	 */
	public void setNumRecordsMerged( Integer numRecordsMerged );

	/**
	 * @param pageSize
	 */
	public void setPageSize(Integer pageSize);

	/**
	 * @param pageSize
	 */
	public void setPageSize(String pageSize);

	/*******************************************************
	 * Current display page
	 *******************************************************/

	/**
	 * @param repositoryName
	 */
	public void setRepositoryName(String repositoryName);

	/**
	 * Sets the thread that is handling the active search
	 *
	 * @param searchThread
	 */
	public void setSearchThread( Thread searchThread );

	/**
	 *
	 * @param searchType
	 */
	public void setSearchType( String searchType );

	/**
	 * @param sortBy
	 */
	public void setSortBy(String sortBy);

	/**
	 * @param startRecord
	 */
	public void setStartRecord(Integer startRecord);

	/**
     * @param startRecord
     */
    public void setStartRecord(String startRecord);

    /**
	 * Set the status message for this search to null.
	 */
	public void setStatusMessage();

	/**
	 * Set the status message for this search.
	 * @param msg The message to set.
	 */
	public void setStatusMessage(String msg);

	/**
	 * @param size
	 */
	public void setViewPageSize(int size);

	/**
	 * This method gives access to a list of citations representing the first page of output based
	 * on a predefined "page size". In zero-based page numbering, this method returns page zero.
	 * @throws SearchException in case of an error in fetching the necessary records from the
	 * metasearch engine.
	 * @throws SearchCancelException TODO
	 */
	public List viewPage() throws SearchException, SearchCancelException;

	/**
	 * This method gives access to a list of citations representing a page of output based on a
	 * predefined "page size". The page number is zero-based.  This method can return any page
	 * that has already been accessed or the "next" page, a page that can be generated by retrieving
	 * no more than <i>pageSize</i> additional citations.
	 * @param page The zero-based index of the desired page.
	 * @throws SearchException if the requested page is beyond the next page (i.e. it can't be
	 * supplied without doing more than one additional search of size <i>pageSize</i>) or if the
	 * metasearch engine encounters a problem in fetching the necessary records.
	 * @throws SearchCancelException TODO
	 */
	public List viewPage(int page) throws SearchException, SearchCancelException;

	/**
	 * Set the list of databases to be searched
	 * @param A list of database IDs
	 */
	public void setDatabaseIds(String[] databaseIds);
	
	/**
	 * Resets the search
	 */
	public void resetSearch();
	
}
