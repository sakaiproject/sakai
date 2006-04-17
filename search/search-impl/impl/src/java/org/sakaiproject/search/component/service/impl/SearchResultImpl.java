/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2006 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package org.sakaiproject.search.component.service.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.sakaiproject.search.api.SearchResult;

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

	public SearchResultImpl(Hits h, int index, Query query) throws IOException
	{
		this.h = h;
		this.index = index;
		this.doc = h.doc(index);
		this.query = query;
	}

	public float getScore()
	{
		try
		{
			return h.score(index);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Cant determine score ", e);
		}
	}

	public String getId()
	{
		return doc.get("id");
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
		return doc.get("url");
	}

	public String getTitle()
	{
		return doc.get("tool") + ": " + doc.get("title");
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
			Highlighter hightlighter = new Highlighter(scorer);
			StringBuffer sb = new StringBuffer();
			String[] contents = doc.getValues("contents");
			for (int i = 0; i < contents.length; i++)
			{
				sb.append(contents[i]);
			}
			String text = sb.toString();
			TokenStream tokenStream = new SimpleAnalyzer().tokenStream(
					"contents", new StringReader(text));
			return hightlighter.getBestFragments(tokenStream, text, 5, " ... ");
		}
		catch (IOException e)
		{
			return "Error: " + e.getMessage();
		}
	}

}
