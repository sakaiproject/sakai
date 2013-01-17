package org.sakaiproject.search.elasticsearch;

import com.google.common.collect.ForwardingList;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.terms.InternalTermsFacet;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.api.SearchResult;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 10/31/12
 * Time: 2:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class ElasticSearchList extends ForwardingList<SearchResult> implements SearchList {
    private final List<SearchResult> results;
    private final SearchResponse response;

    public ElasticSearchList(SearchResponse response, SearchIndexBuilder searchIndexBuilder, String facetName) {
        this.response = response;
        results = new ArrayList<SearchResult>();
            int i=0;
            for (SearchHit hit : response.getHits()) {
                ElasticSearchResult result = new ElasticSearchResult(hit, (InternalTermsFacet) response.getFacets().facet(facetName), searchIndexBuilder);
                result.setIndex(i++);
                results.add(result);
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