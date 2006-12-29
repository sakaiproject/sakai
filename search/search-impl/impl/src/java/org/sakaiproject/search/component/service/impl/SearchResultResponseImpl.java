/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.TermFrequency;
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

	private EntityManager entityManager;

	private SearchIndexBuilder searchIndexBuilder;

	private SearchService searchService;

	private Map attributes;

	public SearchResultResponseImpl(Map attributes, Query query,
			Analyzer analyzer, EntityManager entityManager,
			SearchIndexBuilder searchIndexBuilder, SearchService searchService)
			throws IOException
	{

		this.attributes = attributes;
		this.query = query;
		this.analyzer = analyzer;
		this.entityManager = entityManager;
		this.searchIndexBuilder = searchIndexBuilder;
		this.searchService = searchService;
	}
	public SearchResultResponseImpl(Attributes atts, Query query,
			Analyzer analyzer, EntityManager entityManager,
			SearchIndexBuilder searchIndexBuilder, SearchService searchService)
			throws IOException
	{
		Map m = new HashMap();
		for ( int i = 0; i < atts.getLength(); i++ ) {
			m.put(atts.getLocalName(i),atts.getValue(i));
		}
		try
		{
			String title = (String)m.get("title");
			if ( title != null ) {
				m.put("title", new String(Base64.decodeBase64(title.getBytes("UTF-8")),"UTF-8"));
			}
		}
		catch (UnsupportedEncodingException e)
		{
		}
		this.attributes = m;
		this.query = query;
		this.analyzer = analyzer;
		this.entityManager = entityManager;
		this.searchIndexBuilder = searchIndexBuilder;
		this.searchService = searchService;
	}

	public float getScore()
	{
		return Float.parseFloat((String)attributes.get("score"));
	}

	public String getId()
	{
		return (String)attributes.get("sid");
	}

	public String[] getFieldNames()
	{

		if (fieldNames != null)
		{
			return fieldNames;
		}
		fieldNames = new String[attributes.size()];
		int ii = 0;
		for (Iterator i = attributes.keySet().iterator(); i.hasNext();)
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
	public Map getValueMap()
	{
		HashMap hm = new HashMap();
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
		return (String) attributes.get("url");
	}

	public String getTitle()
	{
		return StringUtils.escapeHtml((String) attributes
				.get("title"), false);
	}

	public String getTool()
	{
		return StringUtils.escapeHtml((String) attributes
				.get("tool"), false);

	}

	public int getIndex()
	{
		return Integer.parseInt((String)attributes.get("index"));
	}

	public String getSearchResult()
	{
		try
		{
			Scorer scorer = new QueryScorer(query);
			Highlighter hightlighter = new Highlighter(scorer);
			StringBuffer sb = new StringBuffer();
			// contents no longer contains the digested contents, so we need to
			// fetch it from the EntityContentProducer

			Reference ref = entityManager.newReference(getReference());
			Entity entity = ref.getEntity();
			EntityContentProducer sep = searchIndexBuilder
					.newEntityContentProducer(ref);
			sb.append(sep.getContent(entity));

			String text = StringUtils.escapeHtml(sb.toString(), false);
			TokenStream tokenStream = analyzer.tokenStream(
					SearchService.FIELD_CONTENTS, new StringReader(text));
			return hightlighter.getBestFragments(tokenStream, text, 5, " ... ");
		}
		catch (IOException e)
		{
			return "Error: " + e.getMessage();
		}
	}

	public String getReference()
	{
		return (String) attributes.get("reference");
	}

	public TermFrequency getTerms() throws IOException
	{
		return null;
	}

	public void toXMLString(StringBuffer sb)
	{
		sb.append("<result");
		sb.append(" index=\"").append(getIndex()).append("\" ");
		sb.append(" score=\"").append(getScore()).append("\" ");
		sb.append(" sid=\"").append(getId()).append("\" ");
		sb.append(" reference=\"").append(getReference()).append("\" ");
		try
		{
			sb.append(" title=\"").append(
					new String(Base64.encodeBase64(getTitle().getBytes("UTF-8")),"UTF-8")).append("\" ");
		}
		catch (UnsupportedEncodingException e)
		{
			sb.append(" title=\"").append(getTitle()).append("\" ");
		}
		sb.append(" tool=\"").append(getTool()).append("\" ");
		sb.append(" url=\"").append(getUrl()).append("\" />");
	}

}
