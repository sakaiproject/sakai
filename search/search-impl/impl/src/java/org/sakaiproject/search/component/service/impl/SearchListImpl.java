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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.PortalUrlEnabledProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.filter.SearchItemFilter;

/**
 * @author ieb
 */
public class SearchListImpl implements SearchList
{

	private static Log dlog = LogFactory.getLog(SearchListImpl.class);

	private Hits h;

	private Query query;

	private int start = 0;

	private int end = 500;

	private Analyzer analyzer;

	private SearchItemFilter filter;
	
	private SearchIndexBuilder searchIndexBuilder;

	private SearchService searchService;


	public SearchListImpl(Hits h, Query query, int start, int end,
			Analyzer analyzer, SearchItemFilter filter,  SearchIndexBuilder searchIndexBuilder, SearchService searchService)
	{
		this.h = h;
		this.query = query;
		this.start = start;
		this.end = end;
		this.analyzer = analyzer;
		this.filter = filter;
		this.searchIndexBuilder = searchIndexBuilder;
		this.searchService = searchService;


	}

	/**
	 * @{inheritDoc}
	 */
	public Iterator<SearchResult> iterator(final int startAt)
	{
		return new Iterator<SearchResult>()
		{
			int counter = Math.max(startAt, start);

			public boolean hasNext()
			{
				return counter < Math.min(h.length(), end);
			}

			public SearchResult next()
			{

				try
				{
					final int thisHit = counter;
					counter++;
					SearchResult result =  new SearchResultImpl(h, thisHit,
							query, analyzer,searchIndexBuilder,searchService);
					String url = checkUrl(result.getReference());
					if (url != null)
						result.setUrl(url);
					return filter.filter(result);
				}
				catch (IOException e)
				{
					throw new RuntimeException("Cant get Hit for some reason ",
							e);
				}
			}

			public void remove()
			{
				throw new UnsupportedOperationException("Not Implemented");
			}
			
			/**
			 * Check the results url before returning
			 * @param reference
			 * @return
			 */
			private String checkUrl(String reference) {
				if (searchIndexBuilder != null) {
					EntityContentProducer ecp = searchIndexBuilder.newEntityContentProducer(reference);
					if (ecp == null )
						return null;


					if (PortalUrlEnabledProducer.class.isAssignableFrom(ecp.getClass()))
						return ecp.getUrl(reference);
				}
				return null;
			}

		};
		
		
	}

	public int size()
	{
		return Math.min(h.length(), end - start);
	}

	public int getFullSize()
	{
		return h.length();
	}

	public boolean isEmpty()
	{
		return (size() == 0);
	}

	public boolean contains(Object arg0)
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public Iterator<SearchResult> iterator()
	{
		return iterator(0);
	}

	public Object[] toArray()
	{
		Object[] o;
		try
		{
			o = new Object[size()];
			for (int i = 0; i < o.length; i++)
			{

				o[i + start] = filter.filter(new SearchResultImpl(h, i + start,
						query, analyzer,searchIndexBuilder,searchService));
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to load all results ", e);
		}
		return o;
	}

	public Object[] toArray(Object[] arg0)
	{
		if (arg0 instanceof SearchResult[])
		{
			return toArray();
		}
		return null;
	}

	public boolean add(SearchResult arg0)
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public boolean remove(Object arg0)
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public boolean containsAll(Collection arg0)
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public boolean addAll(Collection arg0)
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public boolean addAll(int arg0, Collection arg1)
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public boolean removeAll(Collection arg0)
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public boolean retainAll(Collection arg0)
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public void clear()
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public SearchResult get(int arg0)
	{
		try
		{
			return filter
					.filter(new SearchResultImpl(h, arg0, query, analyzer,searchIndexBuilder,searchService));
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to retrieve result ", e);
		}

	}

	public SearchResult set(int arg0, SearchResult arg1)
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public void add(int arg0, SearchResult arg1)
	{
		throw new UnsupportedOperationException("Not Implemented");

	}

	public SearchResult remove(int arg0)
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public int indexOf(Object arg0)
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public int lastIndexOf(Object arg0)
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public ListIterator<SearchResult> listIterator()
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public ListIterator<SearchResult> listIterator(int arg0)
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public List<SearchResult> subList(int arg0, int arg1)
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public int getStart()
	{
		return start;
	}

}
