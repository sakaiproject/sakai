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

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchStatus;
import org.sakaiproject.search.elasticsearch.filter.SearchItemFilter;

import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 *
 */
public interface ElasticSearchIndexBuilder extends SearchIndexBuilder {

    void initialize(ElasticSearchIndexBuilderEventRegistrar eventRegistrar, Client client);

    Set<String> getTriggerFunctions();

    Set<String> getContentFunctions();

    String getEventResourceFilter();

    SearchResponse search(String searchTerms, List<String> references, List<String> siteIds, int start, int end);

    // response type is different from search to match historical behavior (and is actually easier to
    // keep the SearchResponse processing inside the index builder b/c it means we don't have to
    // expose any impl details like the searched on field name

    SearchResponse search(String searchTerms, List<String> references, List<String> siteIds, int start, int end, Map<String,String> additionalSearchInformation);

    String[] searchSuggestions(String searchString, String currentSite, boolean allMySites);

    String getFieldFromSearchHit(String fieldReference, SearchHit hit);

    boolean getUseFacetting();

    // crappy concession to search() requirements in ElasticSearchService. would be better if this sort of thing
    // was just returned along w results
    String getFacetName();

    // crappy concession to search() requirements in ElasticSearchService. would be better if this sort of thing
    // was just returned along w results
    SearchItemFilter getFilter();

    StringBuilder getStatus(StringBuilder into);

    int getNDocs();

    SearchStatus getSearchStatus();
}
