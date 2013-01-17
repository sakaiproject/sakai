package org.sakaiproject.search.elasticsearch;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.terms.InternalTermsFacet;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.highlight.HighlightField;
import org.sakaiproject.search.api.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 10/31/12
 * Time: 2:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class ElasticSearchResult implements SearchResult {
    private static final Log log = LogFactory.getLog(ElasticSearchResult.class);
    private int index;
    private SearchHit hit;
    private String newUrl;
    private InternalTermsFacet facet;
    private SearchIndexBuilder searchIndexBuilder;

    public ElasticSearchResult(SearchHit hit, InternalTermsFacet facet, SearchIndexBuilder searchIndexBuilder) {
        this.hit = hit;
        this.facet = facet;
        this.searchIndexBuilder = searchIndexBuilder;
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
        return hit.getSource().keySet().toArray(new String[hit.getSource().size()]);
    }

    @Override
    public String[] getValues(String string) {
        return hit.getSource().values().toArray(new String[hit.getSource().size()]);
    }

    @Override
    public Map<String, String[]> getValueMap() {
        //TODO figure out what the fuck this is
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
        StringBuilder sb = new StringBuilder();
        for (HighlightField fieldEntry : hit.getHighlightFields().values()) {
            sb.append(fieldEntry.getName()).append(": ");
            for (Text highlight : fieldEntry.getFragments())
                sb.append(highlight).append("... ");
        }
        return sb.toString();
    }

    @Override
    public String getReference() {
        return getFieldFromSearchHit(SearchService.FIELD_REFERENCE);
    }

    @Override
    public TermFrequency getTerms() throws IOException {

        String[] terms = new String[facet.entries().size()];
        int[] frequencies = new int[facet.entries().size()];

        int i = 0;

        for (TermsFacet.Entry termFacet : facet.entries()) {
            terms[i] = termFacet.getTerm();
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
        if (hit == null || hit.getSource().get(field) == null) {
            return null;
        }
        return (String) hit.getSource().get(field);
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
