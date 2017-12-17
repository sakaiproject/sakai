/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/search/trunk/search-impl/impl/src/java/org/sakaiproject/search/component/Messages.java $
 * $Id: Messages.java 59685 2009-04-03 23:36:24Z arwhyte@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (azeckoski @ unicon.net)
 **********************************************************************************/

package org.sakaiproject.search.entitybroker;

import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.entitybroker.entityprovider.extension.QuerySearch;
import org.sakaiproject.entitybroker.entityprovider.extension.SearchContent;
import org.sakaiproject.entitybroker.entityprovider.extension.SearchProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.SearchResults;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;

/**
 * This provides the hook between the entitybroker system and this search system and allows EB
 * providers to hook into the search system without crazy amounts of effort
 * 
 * This basically creates the SearchProvider which EB will look for and hook up and then also
 * creates the content producer which search looks for
 * 
 * I hope to be able to bypass the weird search system which reacts to an event and then asks me (the ECP) if I want to index something
 * which leads the ECP to respond by saying it wants search to add or delete or whatever something and then search puts an SBI into
 * the storage and later acts on this by asking my ECP again for the different bits of data (in multiple calls to the ECP) which 
 * finally ends up putting that data into the index. What I really want is a method that lets me call addThisToTheindex(..) but
 * I doubt I will be able to find something like this.... 
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public class SearchProviderImpl implements SearchProvider, EntityContentProducer {

    private SearchService searchService;
    private SearchIndexBuilder searchIndexBuilder;

    public void init() {
        log.info("SEARCH PROVIDER INIT");
    }

    // **************************************************
    // SearchProvider methods
    // **************************************************

    public boolean add(String reference, SearchContent content) {
        // FIXME search seems to NOT have any way to simply force something into the indexes... this is going to be a big pain -AZ
        return false;
    }

    public boolean remove(String reference) {
        // FIXME search seems to NOT have any way to simply force something out of the indexes... this is going to be a big pain -AZ
        return false;
    }

    public void resetSearchIndexes(String context) {
        if (context == null) {
            searchService.rebuildInstance();
        } else {
            searchService.rebuildSite(context);
        }
    }

    public SearchResults search(QuerySearch query) {
        // TODO searchService.search(searchTerms, contexts, searchStart, searchEnd)
        return null;
    }

    // **************************************************
    // EntityContentProducer methods
    // **************************************************

    public boolean canRead(String reference) {
        // TODO Auto-generated method stub
        return false;
    }

    public Integer getAction(Event event) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getContainer(String ref) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getContent(String reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public Reader getContentReader(String reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, ?> getCustomProperties(String ref) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getCustomRDF(String ref) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getId(String ref) {
        // TODO Auto-generated method stub
        return null;
    }

    public Iterator<String> getSiteContentIterator(String context) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getSiteId(String reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getSubType(String ref) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getTitle(String reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getTool() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getType(String ref) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getUrl(String reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isContentFromReader(String reference) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isForIndex(String reference) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean matches(String reference) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean matches(Event event) {
        // TODO Auto-generated method stub
        return false;
    }

    // **************************************************
    // INJECTION
    // **************************************************

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setSearchIndexBuilder(SearchIndexBuilder searchIndexBuilder) {
        this.searchIndexBuilder = searchIndexBuilder;
    }

}
