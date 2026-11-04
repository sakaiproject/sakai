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

import org.opensearch.action.search.SearchResponse;
import org.opensearch.search.SearchHit;
import org.opensearch.search.aggregations.bucket.terms.Terms;
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

        Terms facet = facetOfReturnedHits(searchTerms, elasticSearchService, searchIndexBuilder, facetName);
        int i = 0;
        for (SearchHit hit : response.getHits()) {
            ElasticSearchResult result = new ElasticSearchResult(hit, facet, searchIndexBuilder, searchTerms);
            result.setIndex(i++);
            results.add(filter.filter(result));
        }
    }

    /**
     * The search's own facet spans every match, including sites the user can't access, so when it has any terms they
     * are counted again over just the returned hits. The default mapping has no facet field, so this normally costs
     * no second query.
     */
    private Terms facetOfReturnedHits(String searchTerms, ElasticSearchService elasticSearchService, ElasticSearchIndexBuilder searchIndexBuilder, String facetName) {
        if (!searchIndexBuilder.getUseFacetting()) {
            return null;
        }
        Terms facet = response.getAggregations().get(facetName);
        if (facet == null || facet.getBuckets().isEmpty()) {
            return facet;
        }
        List<String> references = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            references.add(searchIndexBuilder.getFieldFromSearchHit(SearchService.FIELD_REFERENCE, hit));
        }
        // without references the query below wouldn't be limited to anything
        if (references.isEmpty()) {
            return null;
        }
        try {
            return elasticSearchService.search(searchTerms, new ArrayList<>(), new ArrayList<>(), 0, references.size(), references, searchIndexBuilder.getName())
                    .getAggregations().get(facetName);
        } catch (Exception e) {
            log.warn("Could not limit search facets to the returned results for [{}]: {}", searchTerms, e.toString());
            return null;
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
