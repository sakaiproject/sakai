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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.PortalUrlEnabledProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.StoredDigestContentProducer;
import org.sakaiproject.search.api.TermFrequency;
import org.sakaiproject.search.component.Messages;
import org.sakaiproject.search.util.DigestStorageUtil;

/**
 * @author ieb
 */
public class SearchResultImpl implements SearchResult
{

	private static Log log = LogFactory.getLog(SearchResultImpl.class);

	private Hits h;

	private int index;

	private Document doc;

	String[] fieldNames = null;

	private Query query = null;

	private Analyzer analyzer = null;

	private SearchIndexBuilder searchIndexBuilder;

	private SearchService searchService;

	private String url;

	public SearchResultImpl(Hits h, int index, Query query, Analyzer analyzer,
			SearchIndexBuilder searchIndexBuilder,
			SearchService searchService) throws IOException
			{
		this.h = h;
		this.index = index;
		this.doc = h.doc(index);
		this.query = query;
		this.analyzer = analyzer;
		this.searchIndexBuilder = searchIndexBuilder;
		this.searchService = searchService;
			}

	public float getScore()
	{
		try
		{
			return h.score(index);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Cant determine score ", e); //$NON-NLS-1$
		}
	}

	public String getId()
	{
		return doc.get(SearchService.FIELD_ID);
	}

	public String[] getFieldNames()
	{
		if (fieldNames != null)
		{
			return fieldNames;
		}
		HashMap al = new HashMap();
		for (Enumeration e = doc.fields(); e.hasMoreElements();)
		{
			Field f = (Field) e.nextElement();
			al.put(f.name(), f);
		}
		fieldNames = new String[al.size()];
		int ii = 0;
		for (Iterator i = al.keySet().iterator(); i.hasNext();)
		{
			fieldNames[ii++] = (String) i.next();
		}
		return fieldNames;
	}

	public String[] getValues(String fieldName)
	{
		return doc.getValues(fieldName);
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
			hm.put(fieldNames[i], doc.getValues(fieldNames[i]));
		}
		return hm;
	}

	public String getUrl()
	{
		if (url == null)
			url = doc.get(SearchService.FIELD_URL);
		return url;
	}

	public String getTitle()
	{
		return doc.get(SearchService.FIELD_TITLE);
	}

	public String getTool()
	{
		return doc.get(SearchService.FIELD_TOOL);

	}

	public int getIndex()
	{
		return index;
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

			String[] references = doc.getValues(SearchService.FIELD_REFERENCE);

			if (references != null && references.length > 0)
			{

				for (int i = 0; i < references.length; i++)
				{
					EntityContentProducer sep = searchIndexBuilder
					.newEntityContentProducer(references[i]);
					if ( sep != null ) {
						//does this ecp store on the FS?
						if (sep instanceof StoredDigestContentProducer) {
							String digestCount = doc.get(SearchService.FIELD_DIGEST_COUNT);
							if (digestCount != null) {
								log.info("This file possibly has FS digests with index of " + digestCount);
								//FileInputStream fis = new FileInputStream(DigestStorageUtil.getPath(doc.get(SearchService.FIELD_REFERENCE)));
								BufferedReader input = null;
								try {
									String storePath = ServerConfigurationService.getString("bodyPath@org.sakaiproject.content.api.ContentHostingService");
									storePath += "/searchdigest/";
									String digestFilePath = storePath + DigestStorageUtil.getPath(doc.get(SearchService.FIELD_REFERENCE)) + "/digest." + digestCount;
									log.info("opening: " + digestFilePath);

									input =  new BufferedReader(new FileReader(digestFilePath));

									String line = null; //not declared within while loop
									/*
									 * readLine is a bit quirky :
									 * it returns the content of a line MINUS the newline.
									 * it returns null only for the END of the stream.
									 * it returns an empty String if two newlines appear in a row.
									 */
									while (( line = input.readLine()) != null){
										sb.append(line);
										sb.append(System.getProperty("line.separator"));
									}
								}
								catch (FileNotFoundException e) {
									log.warn("Unable to open digest for item: " + SearchService.FIELD_REFERENCE + " with count " + digestCount + " fileNotFound");
								}
								finally {
									if (input != null) {
										try {
											input.close();
										}
										catch (IOException iox) {
											log.debug("exeption in final block!");
										}
									} else  {
										sb.append(sep.getContent(references[i]));
							
									}
								}
							} else {
								sb.append(sep.getContent(references[i]));
							
							}



						} else {
							sb.append(sep.getContent(references[i]));
							
						}
					}
				}
			}
			String text = sb.toString();
			TokenStream tokenStream = analyzer.tokenStream(
					SearchService.FIELD_CONTENTS, new StringReader(text));
			return hightlighter.getBestFragments(tokenStream, text, 5, " ... "); //$NON-NLS-1$
		}
		catch (IOException e)
		{
			return Messages.getString("SearchResultImpl.2") + e.getMessage(); //$NON-NLS-1$
		}
	}

	public String getReference()
	{
		return doc.get(SearchService.FIELD_REFERENCE);
	}

	public TermFrequency getTerms() throws IOException
	{
		return searchService.getTerms(h.id(index));
	}

	public void toXMLString(StringBuilder sb)
	{
		sb.append("<result"); //$NON-NLS-1$
		sb.append(" index=\"").append(getIndex()).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(" score=\"").append(getScore()).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(" sid=\"").append(StringUtils.xmlEscape(getId())).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(" site=\"").append(StringUtils.xmlEscape(getSiteId())).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(" reference=\"").append(StringUtils.xmlEscape(getReference())).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
		try
		{
			sb.append(" title=\"").append( //$NON-NLS-1$
					new String(Base64.encodeBase64(getTitle().getBytes("UTF-8")),"UTF-8")).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		catch (UnsupportedEncodingException e)
		{
			sb.append(" title=\"").append(StringUtils.xmlEscape(getTitle())).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		sb.append(" tool=\"").append(StringUtils.xmlEscape(getTool())).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(" url=\"").append(StringUtils.xmlEscape(getUrl())).append("\" />"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String getSiteId() {
		return doc.get(SearchService.FIELD_SITEID);
	}

	public boolean isCensored() {
		return false;
	}


	public void setUrl(String newUrl) {
		url = newUrl;

	}

	public boolean hasPortalUrl() {
		log.debug("hasPortalUrl(" + getReference());
		EntityContentProducer sep = searchIndexBuilder
		.newEntityContentProducer(getReference());
		if (sep != null) {
			log.debug("got ECP for " + getReference());
			if (PortalUrlEnabledProducer.class.isAssignableFrom(sep.getClass())) {
				log.debug("has portalURL!");
				return true;
			}
		}
		return false;
	}

}
