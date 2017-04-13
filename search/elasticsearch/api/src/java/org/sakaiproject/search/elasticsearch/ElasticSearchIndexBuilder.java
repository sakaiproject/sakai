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
