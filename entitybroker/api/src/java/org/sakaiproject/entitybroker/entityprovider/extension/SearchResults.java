/**
 * $Id: SearchProvider.java 59674 2009-04-03 23:05:58Z arwhyte@umich.edu $
 * $URL:  $
 * SearchResults - entity-broker - Apr 5, 2008 7:19:14 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.entityprovider.extension;

import java.util.List;
import java.util.Vector;


/**
 * This represents the return data from a search
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class SearchResults {

    /**
     * The search query that was performed
     */
    public String query;
    /**
     * The index (position) of the result item to started on (used for paging) <br/>
     * 0 indicates the default value (start at the beginning) <br/>
     */
    public int startIndex = 0;
    /**
     * The maximum number of results returned after the startindex, 0 indicates the default value
     * (all items) <br/>
     */
    public int maxItemsToReturn = 0;
    /**
     * The total number of items matched by the search <br/>
     */
    public int totalItemsMatched = 0;
    /**
     * This is a holder for the original query before it is adjusted
     */
    public String originalQuery;
    /**
     * for paging the results
     */
    public int itemsPerPage = 15;

    /**
     * The list of all the search results (each result is a match for the search)
     */
    List<SearchResult> results = new Vector<SearchResult>(0);

    public SearchResults() {
    }

    public SearchResults(String query, int startIndex, int maxItemsToReturn) {
        this(startIndex, maxItemsToReturn, 0, null);
        this.query = query;
    }

    /**
     * use this constructor to build search results manually
     */
    public SearchResults(int start, int max, int total, List<SearchResult> results) {
        this.query = "featured:true";
        this.startIndex = start <= 0 ? 0 : start;
        this.maxItemsToReturn = max <= 0 ? 0 : max;
        this.itemsPerPage = maxItemsToReturn > 0 ? maxItemsToReturn : this.itemsPerPage;
        this.totalItemsMatched = total;
        if (results != null) {
            for (SearchResult searchResult : results) {
                this.results.add(searchResult);
            }
        }
    }

    /**
     * Add results to the total results,
     * this will not allow adding results beyond the totalItemsMatched (max) number
     * 
     * @param steepleItem the result to add
     * @return true if the item was added OR false if it was null or the max number is already added
     */
    public boolean addResult(SearchContent content) {
        boolean added = false;
        if (content != null) {
            SearchResult sr = new SearchResult(content);
            added = addResult(sr);
        }
        return added;
    }

    /**
     * Add results to the total results,
     * this will not allow adding results beyond the maxItemsToReturn (max) number
     * 
     * @param searchResult the result to add
     * @return true if the item was added OR false if it was null or the max number is already added
     */
    public boolean addResult(SearchResult searchResult) {
        boolean added = false;
        if (searchResult != null) {
            if (this.maxItemsToReturn <= 0 
                    || getResults().size() < this.maxItemsToReturn) {
                getResults().add(searchResult);
                // this will ensure that the total items matched is not completely wrong
                if (getResults().size() > this.totalItemsMatched) {
                    this.totalItemsMatched = getResults().size();
                }
                added = true;
            }
        }
        return added;
    }

    public String getQuery() {
        return query;
    }

    public List<SearchResult> getResults() {
        if (results == null) {
            results = new Vector<SearchResult>(0);
        }
        return results;
    }

    /**
     * @return the starting index for these results (starts at 0)
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * @return the ending index based on the start index and the max items to return 
     * (not based on the actual items in the results), return the max int if the max is 0
     */
    public int getEndIndex() {
        int end;
        int max = getMaxItemsToReturn();
        if (max <= 0) {
            end = Integer.MAX_VALUE;
        } else {
            end = startIndex + getMaxItemsToReturn();
        }
        return end;
    }

    /**
     * @return the true value of max items to return,
     * this will be <= 0 to indicate returning all items
     */
    public int getMaxItemsToReturn() {
        return maxItemsToReturn;
    }

    public int getTotalItemsMatched() {
        return totalItemsMatched;
    }

    /**
     * @return the starting number for these results (starts at 1)
     */
    public int getStartNum() {
        return startIndex + 1;
    }

    /**
     * @return the ending number for these results 
     * (this is the true ending number and also the ending index based on the items returned
     * and not only based on the max items to return)
     */
    public int getEndNum() {
        return startIndex + getResultsNum(); // or (startIndex + 1) + (resultsNum - 1)
    }

    /**
     * @return the count of the number of items returned
     */
    public int getResultsNum() {
        return getResults().size();
    }

    public int getPerPageNum() {
        return itemsPerPage;
    }

    public int getPagesNum() {
        int mod = totalItemsMatched % itemsPerPage;
        int div = totalItemsMatched / itemsPerPage;
        return (mod == 0 ? div : div+1);
    }

    /**
     * @return the count of the total number of items found
     */
    public int getTotalNum() {
        return totalItemsMatched;
    }

    public void setOriginalQuery(String originalQuery) {
        this.originalQuery = originalQuery;
    }

    public String getOriginalQuery() {
        return originalQuery;
    }
    
    public void setItemsPerPage(int itemsPerPage) {
        if (itemsPerPage <= 0) {
            itemsPerPage = 15;
        }
        this.itemsPerPage = itemsPerPage;
    }
    
    public int getItemsPerPage() {
        return itemsPerPage;
    }

}