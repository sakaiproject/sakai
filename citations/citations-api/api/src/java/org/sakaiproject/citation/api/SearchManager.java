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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.osid.repository.RepositoryIterator;
import org.sakaiproject.citation.api.ActiveSearch;
import org.sakaiproject.citation.util.api.SearchCancelException;
import org.sakaiproject.citation.util.api.SearchException;

/**
 *
 */
public interface SearchManager
{
	public static final int DEFAULT_PAGE_SIZE = 10;
	public static final int DEFAULT_START_RECORD = 1;
	public static final int MIN_START_RECORD = 1;
	public static final String DEFAULT_SORT_BY = "rank";


	public static final String ASSET_NOT_FETCHED = "An Asset is available, but has not yet been fetched.";

	public static final String METASEARCH_ERROR = "Metasearch error has occured. Please contact your site's support team.";

	public static final String SESSION_TIMED_OUT = "Metasearch session has " +
			"timed out. Please restart your search session.";

	/**
	 * @param search
	 * @return
	 * @throws SearchException
	 */
	public ActiveSearch doNextPage(ActiveSearch search)
	        throws SearchException;

	/**
	 * @param search
	 * @return
	 * @throws SearchException
	 */
	public ActiveSearch doPrevPage(ActiveSearch search)
	        throws SearchException;

	/**
	 * @param search
	 * @return
	 * @throws SearchException
	 * @throws SearchCancelException in the event of a user-submitted cancel
	 */
	public ActiveSearch doSearch(ActiveSearch search)
	        throws SearchException, SearchCancelException;

	/**
	 * @return The SearchDatabaseHierarchy for this search.
	 * @throws SearchException
	 * @see SearchDatabaseHierarchy
	 */
	public SearchDatabaseHierarchy getSearchHierarchy() throws SearchException;

	/**
	 * @return
	 */
	public ActiveSearch newSearch();

	/**
     * @param savedResults
     * @return
     */
    public ActiveSearch newSearch(CitationCollection savedResults);

	/**
     * @param resourceId
     * @return
     */
    public String getGoogleScholarUrl(String resourceId);

    public String getExternalSearchWindowName(String resourceId);

    /**
     * Supply the url for the savecite servlet to add a citation to a particular citation list.
     * @param resourceId The identifier for the citation list.
     */
	public String getSaveciteUrl(String resourceId, String saveciteClientId);
}
