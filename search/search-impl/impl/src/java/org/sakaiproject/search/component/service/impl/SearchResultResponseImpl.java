/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.component.service.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.PortalUrlEnabledProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.TermFrequency;
import org.sakaiproject.search.component.Messages;
import org.xml.sax.Attributes;



/**
 * @author ieb
 */
public class SearchResultResponseImpl implements SearchResult
{

	private static Log log = LogFactory.getLog(SearchResultResponseImpl.class);

	String[] fieldNames = null;

	private Query query = null;

	private Analyzer analyzer = null;

	private String url;
	private SearchIndexBuilder searchIndexBuilder;


	private Map<String, String> attributes;

	public SearchResultResponseImpl(Map<String, String> attributes, Query query,
			Analyzer analyzer, 
			SearchIndexBuilder searchIndexBuilder, SearchService searchService)
			throws IOException
	{

		this.attributes = attributes;
		this.query = query;
		this.analyzer = analyzer;
		this.searchIndexBuilder = searchIndexBuilder;
		
	}
	public SearchResultResponseImpl(Attributes atts, Query query,
			Analyzer analyzer, 
			SearchIndexBuilder searchIndexBuilder, SearchService searchService)
			throws IOException
	{
		Map<String, String> m = new HashMap<String, String>();
		for ( int i = 0; i < atts.getLength(); i++ ) {
			m.put(atts.getLocalName(i),atts.getValue(i));
		}
		try
		{
			String title = (String)m.get("title"); //$NON-NLS-1$
			if ( title != null ) {
				m.put("title", new String(Base64.decodeBase64(title.getBytes("UTF-8")),"UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		catch (UnsupportedEncodingException e)
		{
			log.debug(e);
		}
		this.attributes = m;
		this.query = query;
		this.analyzer = analyzer;
		this.searchIndexBuilder = searchIndexBuilder;
		
	}

	public float getScore()
	{
		return Float.parseFloat((String)attributes.get("score")); //$NON-NLS-1$
	}

	public String getId()
	{
		return (String)attributes.get("sid"); //$NON-NLS-1$
	}

	public String[] getFieldNames()
	{

		if (fieldNames != null)
		{
			return fieldNames;
		}
		fieldNames = new String[attributes.size()];
		int ii = 0;
		for (Iterator<String> i = attributes.keySet().iterator(); i.hasNext();)
		{
			fieldNames[ii++] = (String) i.next();
		}
		return fieldNames;
	}

	public String[] getValues(String fieldName)
	{
		return new String[] {(String)attributes.get(fieldName)};
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String[]> getValueMap()
	{
		Map<String, String[]> hm = new HashMap<String, String[]>();
		String[] fieldNames = getFieldNames();
		for (int i = 0; i < fieldNames.length; i++)
		{
			hm.put(fieldNames[i], new String[] { (String) attributes
					.get(fieldNames[i]) });
		}
		return hm;
	}

	
	public String getUrl()
	{
		if (url == null)
			url = (String) attributes.get("url"); //$NON-NLS-1$
		
		return url;
	}

	public String getTitle()
	{
		// I Have a feeling that this is not required as its already HTML Encoded
		return (String) attributes
				.get("title"); //$NON-NLS-1$
	}

	public String getTool()
	{
		// I Have a feeling that this is not required as its already HTML encoded
		return (String) attributes
				.get("tool"); //$NON-NLS-1$

	}

	public int getIndex()
	{
		return Integer.parseInt((String)attributes.get("index")); //$NON-NLS-1$
	}

	public String getSearchResult()
	{
		try
		{
			Scorer scorer = new QueryScorer(query);
			Highlighter hightlighter = new Highlighter(new SimpleHTMLFormatter(), new SimpleHTMLEncoder(), scorer);
			StringBuilder sb = new StringBuilder();
			// contents no longer contains the digested contents, so we need to
			// fetch it from the EntityContentProducer

			EntityContentProducer sep = searchIndexBuilder
					.newEntityContentProducer(getReference());
			if (sep != null)
			{
				sb.append(sep.getContent(getReference()));
			}
			String text = sb.toString();
			TokenStream tokenStream = analyzer.tokenStream(
					SearchService.FIELD_CONTENTS, new StringReader(text));
			return hightlighter.getBestFragments(tokenStream, text, 5, " ... "); //$NON-NLS-1$
		}
		catch (IOException e)
		{
			return Messages.getString("SearchResultResponseImpl.11") + e.getMessage(); //$NON-NLS-1$
		} catch (InvalidTokenOffsetsException e) {
			return Messages.getString("SearchResultResponseImpl.11") + e.getMessage(); 
		}
	}

	public String getReference()
	{
		return (String) attributes.get("reference"); //$NON-NLS-1$
	}

	public TermFrequency getTerms() throws IOException
	{
		return null;
	}

	public void toXMLString(StringBuilder sb)
	{
		sb.append("<result"); //$NON-NLS-1$
		sb.append(" index=\"").append(getIndex()).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(" score=\"").append(getScore()).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(" sid=\"").append(StringEscapeUtils.escapeXml(getId())).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(" reference=\"").append(StringEscapeUtils.escapeXml(getReference())).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
		try
		{
			sb.append(" title=\"").append( //$NON-NLS-1$
					new String(Base64.encodeBase64(getTitle().getBytes("UTF-8")),"UTF-8")).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		catch (UnsupportedEncodingException e)
		{
			sb.append(" title=\"").append(StringEscapeUtils.escapeXml(getTitle())).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		sb.append(" tool=\"").append(StringEscapeUtils.escapeXml(getTool())).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(" url=\"").append(StringEscapeUtils.escapeXml(getUrl())).append("\" />"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public String getSiteId() {
		return (String) attributes.get("site");
	}
	public boolean isCensored() {
		return false;
	}
	public void setUrl(String newUrl) {
		url = newUrl;
		
	}
	public boolean hasPortalUrl() {
		log.info("hasPortalUrl(" + getReference());
		EntityContentProducer sep = searchIndexBuilder
		.newEntityContentProducer(getReference());
		if (sep != null) {
			log.info("got ECP for " + getReference());
			if (PortalUrlEnabledProducer.class.isAssignableFrom(sep.getClass())) {
				log.info("has portalURL!");
				return true;
			}
		}
		return false;
	}

}
