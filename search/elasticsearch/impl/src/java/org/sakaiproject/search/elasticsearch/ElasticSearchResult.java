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

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.opensearch.common.document.DocumentField;
import org.opensearch.search.SearchHit;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.PortalUrlEnabledProducer;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.TermFrequency;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 10/31/12
 * Time: 2:19 PM
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
public class ElasticSearchResult implements SearchResult {
    private int index;
    private SearchHit hit;
    private String newUrl;
    private Terms facet;
    private ElasticSearchIndexBuilder searchIndexBuilder;
    private static Analyzer analyzer = new StandardAnalyzer();
    private String searchTerms;

    public ElasticSearchResult(SearchHit hit, Terms facet, ElasticSearchIndexBuilder searchIndexBuilder, String searchTerms) {
        this.hit = hit;
        this.facet = facet;
        this.searchIndexBuilder = searchIndexBuilder;
        this.searchTerms = searchTerms;

    }


    @Override
    public float getScore() {
        return hit.getScore();
    }

    @Override
    public String getId() {
        return hit.getId();
    }

    @Override
    public String[] getFieldNames() {
        return hit.getFields().keySet().toArray(new String[hit.getFields().size()]);
    }

    @Override
    public String[] getValues(String string) {
        String[] values = new String[hit.getFields().size()];
        int i=0;
        for (DocumentField field: hit.getFields().values()) {
            values[i++] = field.getValue();
        }
        return values;
    }

    @Override
    public Map<String, String[]> getValueMap() {
        //TODO figure out what this is
        return new HashMap<String, String[]>();
    }

    @Override
    public String getUrl() {
        if (newUrl == null) {
            return getFieldFromSearchHit(SearchService.FIELD_URL);
        }
        return newUrl;
    }

    @Override
    public String getTitle() {
        String title = getFieldFromSearchHit(SearchService.FIELD_TITLE);
        if (title == null || title.isEmpty()) {
            return title;
        }
        String highlighted = highlight(title, 1);
        return highlighted != null ? highlighted : StringEscapeUtils.escapeHtml4(title);
    }

    @Override
    public String getSearchResult() {
        try {
            String reference = getFieldFromSearchHit(SearchService.FIELD_REFERENCE);
            if (reference == null) {
                return "";
            }

            EntityContentProducer sep = searchIndexBuilder.newEntityContentProducer(reference);
            if (sep == null) {
                return "";
            }

            String content = sep.getContent(reference);
            if (content == null || content.trim().isEmpty()) {
                return "";
            }

            String highlighted = highlight(content, 5);
            return highlighted != null ? highlighted : "";
        } catch (Exception e) {
            log.warn("Failed to highlight search result for [{}]: {}", searchTerms, e.getMessage());
            return "";
        }
    }

    private String highlight(String text, int maxFragments) {
        if (searchTerms == null || searchTerms.isEmpty()) {
            return null;
        }
        try {
            Query query = new QueryParser(SearchService.FIELD_CONTENTS, analyzer)
                    .parse(QueryParser.escape(searchTerms));
            Highlighter highlighter = new Highlighter(
                    new SimpleHTMLFormatter(), new SimpleHTMLEncoder(), new QueryScorer(query));
            try (TokenStream tokenStream = analyzer.tokenStream(
                    SearchService.FIELD_CONTENTS, new StringReader(text))) {
                return maxFragments == 1
                        ? highlighter.getBestFragment(tokenStream, text)
                        : highlighter.getBestFragments(tokenStream, text, maxFragments, " ... ");
            }
        } catch (Exception e) {
            log.debug("Could not highlight text: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String getReference() {
        return getFieldFromSearchHit(SearchService.FIELD_REFERENCE);
    }

    @Override
    public TermFrequency getTerms() throws IOException {
        if (facet == null) {
            return new ElasticSearchTermFrequency();
        }

        String[] terms = new String[facet.getBuckets().size()];
        int[] frequencies = new int[facet.getBuckets().size()];

        int i = 0;

        for (Terms.Bucket termFacet : facet.getBuckets()) {
            terms[i] = termFacet.getKeyAsString();
            frequencies[i] = (int) termFacet.getDocCount();
            i++;
        }

        return new ElasticSearchTermFrequency(terms, frequencies);
    }

    @Override
    public String getTool() {
        return getFieldFromSearchHit(SearchService.FIELD_TOOL);
    }

    @Override
    public boolean isCensored() {
        return false;
    }

    protected String getFieldFromSearchHit(String field) {
        return searchIndexBuilder.getFieldFromSearchHit(field, hit);
    }

    @Override
    public String getSiteId() {
        return getFieldFromSearchHit(SearchService.FIELD_SITEID);
    }

    @Override
    public String getCreatorDisplayName() {
        return getFieldFromSearchHit(SearchService.FIELD_CREATOR_DISPLAY_NAME);
    }

    @Override
    public String getCreatorId() {
        return getFieldFromSearchHit(SearchService.FIELD_CREATOR_ID);
    }

    @Override
    public String getCreatorUserName() {
        return getFieldFromSearchHit(SearchService.FIELD_CREATOR_USER_NAME);
    }


    @Override
    public void toXMLString(StringBuilder sb) {
        sb.append("<result");
        sb.append(" index=\"").append(getIndex()).append("\" ");
        sb.append(" score=\"").append(getScore()).append("\" ");
        sb.append(" sid=\"").append(StringEscapeUtils.escapeXml11(getId())).append("\" ");
        sb.append(" site=\"").append(StringEscapeUtils.escapeXml11(getSiteId())).append("\" ");
        sb.append(" reference=\"").append(StringEscapeUtils.escapeXml11(getReference())).append("\" ");
        try {
            sb.append(" title=\"").append(new String(Base64.encodeBase64(getTitle().getBytes("UTF-8")), "UTF-8")).append("\" ");
        } catch (UnsupportedEncodingException e) {
            sb.append(" title=\"").append(StringEscapeUtils.escapeXml11(getTitle())).append("\" ");
        }
        sb.append(" tool=\"").append(StringEscapeUtils.escapeXml11(getTool())).append("\" ");
        sb.append(" url=\"").append(StringEscapeUtils.escapeXml11(getUrl())).append("\" />");
    }

    @Override
    public void setUrl(String newUrl) {
        this.newUrl = newUrl;
    }

    public boolean hasPortalUrl() {
        log.debug("hasPortalUrl(" + getReference());
        EntityContentProducer sep = searchIndexBuilder.newEntityContentProducer(getReference());
        if (sep != null) {
            log.debug("got ECP for " + getReference());
            if (PortalUrlEnabledProducer.class.isAssignableFrom(sep.getClass())) {
                log.debug("has portalURL!");
                return true;
            }
        }
        return false;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public SearchHit getHit() {
        return hit;
    }

    public void setHit(SearchHit hit) {
        this.hit = hit;
    }

    public class ElasticSearchTermFrequency implements TermFrequency {
        String[] terms;
        int[] frequencies;

        public ElasticSearchTermFrequency(){
            this.terms = new String[0];
            this.frequencies = new int[0];
        }

        public ElasticSearchTermFrequency(String[] terms, int[] frequencies) {
            this.terms = terms;
            this.frequencies = frequencies;
        }

        public String[] getTerms() {
            return terms;
        }

        public int[] getFrequencies() {
            return frequencies;
        }
    }

}
