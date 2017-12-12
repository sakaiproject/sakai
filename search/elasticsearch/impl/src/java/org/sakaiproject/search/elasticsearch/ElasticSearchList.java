/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.search.elasticsearch;

import java.util.*;

import com.google.common.collect.ForwardingList;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.terms.InternalTermsFacet;

import org.sakaiproject.search.api.*;
import org.sakaiproject.search.elasticsearch.filter.SearchItemFilter;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 10/31/12
 * Time: 2:19 PM
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
public class ElasticSearchList extends ForwardingList<SearchResult> implements SearchList {
    private final List<SearchResult> results;
    private final SearchResponse response;
    private final SearchItemFilter filter;

    public ElasticSearchList(String searchTerms, SearchResponse response, ElasticSearchService elasticSearchService, ElasticSearchIndexBuilder searchIndexBuilder, String facetName, SearchItemFilter filter) {
        this.response = response;
        this.filter = filter;
        results = new ArrayList<SearchResult>();
        List<String> references = new ArrayList<String>();
        for (SearchHit hit : response.getHits()) {
            references.add(searchIndexBuilder.getFieldFromSearchHit(SearchService.FIELD_REFERENCE, hit) );
        }

        SearchResponse highlightedResponse = null;

        try {
            highlightedResponse = elasticSearchService.search(searchTerms, new ArrayList<String>(), 0, references.size(), references, searchIndexBuilder.getName());
        } catch (Exception e) {
            log.error("problem running hightlighted and facetted search: " + e.getMessage(), e);
            return;
        }

        int i=0;
        for (SearchHit hit : highlightedResponse.getHits()) {
            InternalTermsFacet facet = null;
            if (searchIndexBuilder.getUseFacetting()){
                facet = (InternalTermsFacet) highlightedResponse.getFacets().facet(facetName);
            }
            ElasticSearchResult result = new ElasticSearchResult(hit, facet, searchIndexBuilder, searchTerms);
            result.setIndex(i++);
            results.add(filter.filter(result));
        }
    }

    @Override
    public Iterator<SearchResult> iterator(int startAt) {
        Iterator<SearchResult> iterator = iterator();
        //Skip the fist elements
        for (int i = 0; i < startAt && iterator.hasNext(); i++)
            iterator.next();
        return iterator;
    }

    @Override
    public int getFullSize() {
        if (response == null) {
            return 0;
        }
        return (int) response.getHits().getTotalHits();
    }

    @Override
    public int getStart() {
        return 0;
    }

    @Override
    protected List<SearchResult> delegate() {
        return Collections.unmodifiableList(results);
    }
}
