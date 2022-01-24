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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.elasticsearch.filter.SearchItemFilter;

import com.google.common.collect.ForwardingList;
import lombok.extern.slf4j.Slf4j;

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
        results = new ArrayList<>();
        List<String> references = new ArrayList<>();

        SearchHits hits = response.getHits();
        if (hits.getTotalHits().value > 0) {
            for (SearchHit hit : hits) {
                references.add(searchIndexBuilder.getFieldFromSearchHit(SearchService.FIELD_REFERENCE, hit));
            }

            SearchResponse highlightedResponse;
            try {
                highlightedResponse = elasticSearchService.search(searchTerms, new ArrayList<>(), 0, references.size(), references, searchIndexBuilder.getName());
            } catch (Exception e) {
                log.error("problem running hightlighted and facetted search: {}", e);
                return;
            }

            int i = 0;
            for (SearchHit hit : highlightedResponse.getHits()) {
                Terms facet = null;
                if (searchIndexBuilder.getUseFacetting()) {
                    facet = highlightedResponse.getAggregations().get(facetName);
                }
                ElasticSearchResult result = new ElasticSearchResult(hit, facet, searchIndexBuilder, searchTerms);
                result.setIndex(i++);
                results.add(filter.filter(result));
            }
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
        return (int) response.getHits().getTotalHits().value;
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
