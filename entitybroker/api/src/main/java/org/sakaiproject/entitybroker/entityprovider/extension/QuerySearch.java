/**
 * $Id: SearchProvider.java 59674 2009-04-03 23:05:58Z arwhyte@umich.edu $
 * $URL:  $
 * QuerySearch - entity-broker - Apr 5, 2008 7:19:14 PM - azeckoski
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

import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

/**
 * An extension of the standard search object which allows for custom constructors to make it easier
 * to build up the search for the search query
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class QuerySearch extends Search {

    public static String queryField = "query";
    public static String contextsField = "contexts";

    public QuerySearch(String query) {
        super(queryField, query);
    }

    /**
     * @param query the query string
     * @param contexts the list of site contexts (locationIds) to limit the search to
     */
    public QuerySearch(String query, String[] contexts) {
        super();
        this.addRestriction( new Restriction(queryField, query) );
        this.addRestriction( new Restriction(contextsField, contexts) );
    }

}
