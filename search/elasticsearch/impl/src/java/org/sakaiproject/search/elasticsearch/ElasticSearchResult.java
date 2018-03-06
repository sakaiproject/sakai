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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.util.Version;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.facet.terms.InternalTermsFacet;
import org.elasticsearch.search.facet.terms.TermsFacet;

import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.PortalUrlEnabledProducer;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.TermFrequency;

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
    private InternalTermsFacet facet;
    private ElasticSearchIndexBuilder searchIndexBuilder;
    private static Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
    private String searchTerms;

    public ElasticSearchResult(SearchHit hit, InternalTermsFacet facet, ElasticSearchIndexBuilder searchIndexBuilder, String searchTerms) {
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
        for (SearchHitField field: hit.getFields().values()) {
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
        return getFieldFromSearchHit(SearchService.FIELD_TITLE);
    }


    @Override
    public String getSearchResult() {
        try {
            TermQuery query = new TermQuery(new Term("text",searchTerms));

            Scorer scorer = new QueryScorer(query);
            Highlighter hightlighter = new Highlighter(new SimpleHTMLFormatter(), new SimpleHTMLEncoder(), scorer);
            StringBuilder sb = new StringBuilder();
            // contents no longer contains the digested contents, so we need to
            // fetch it from the EntityContentProducer

            String reference = getFieldFromSearchHit(SearchService.FIELD_REFERENCE);

            if (reference != null) {

                EntityContentProducer sep = searchIndexBuilder
                        .newEntityContentProducer(reference);
                if (sep != null) {
                    sb.append(sep.getContent(reference));
                }
            }
            String text = sb.toString();
            TokenStream tokenStream = analyzer.tokenStream(
                    SearchService.FIELD_CONTENTS, new StringReader(text));
            return hightlighter.getBestFragments(tokenStream, text, 5, " ... "); //$NON-NLS-1$
        } catch (IOException e) {
            return e.getMessage(); //$NON-NLS-1$
        } catch (InvalidTokenOffsetsException e) {
            return e.getMessage();
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

        String[] terms = new String[facet.getEntries().size()];
        int[] frequencies = new int[facet.getEntries().size()];

        int i = 0;

        for (TermsFacet.Entry termFacet : facet.getEntries()) {
            terms[i] = termFacet.getTerm().string();
            frequencies[i] = termFacet.getCount();
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
    public void toXMLString(StringBuilder sb) {
        sb.append("<result");
        sb.append(" index=\"").append(getIndex()).append("\" ");
        sb.append(" score=\"").append(getScore()).append("\" ");
        sb.append(" sid=\"").append(StringEscapeUtils.escapeXml(getId())).append("\" ");
        sb.append(" site=\"").append(StringEscapeUtils.escapeXml(getSiteId())).append("\" ");
        sb.append(" reference=\"").append(StringEscapeUtils.escapeXml(getReference())).append("\" ");
        try {
            sb.append(" title=\"").append(new String(Base64.encodeBase64(getTitle().getBytes("UTF-8")), "UTF-8")).append("\" ");
        } catch (UnsupportedEncodingException e) {
            sb.append(" title=\"").append(StringEscapeUtils.escapeXml(getTitle())).append("\" ");
        }
        sb.append(" tool=\"").append(StringEscapeUtils.escapeXml(getTool())).append("\" ");
        sb.append(" url=\"").append(StringEscapeUtils.escapeXml(getUrl())).append("\" />");
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
