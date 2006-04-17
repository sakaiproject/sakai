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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.api.SearchResult;

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

	public SearchListImpl(Hits h, Query query, int start, int end)
	{
		this.h = h;
		this.query = query;
		this.start = start;
		this.end = end;

	}

	/**
	 * @{inheritDoc}
	 */
	public Iterator iterator(final int startAt)
	{
		return new Iterator()
		{
			int counter = Math.max(startAt, start);

			public boolean hasNext()
			{
				return counter < Math.min(h.length(), end);
			}

			public Object next()
			{

				try
				{
					final int thisHit = counter;
					counter++;
					return new SearchResultImpl(h, thisHit, query);
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

	public Iterator iterator()
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

				o[i + start] = new SearchResultImpl(h, i + start, query);
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

	public boolean add(Object arg0)
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

	public Object get(int arg0)
	{
		try
		{
			return new SearchResultImpl(h, arg0, query);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to retrieve result ", e);
		}

	}

	public Object set(int arg0, Object arg1)
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public void add(int arg0, Object arg1)
	{
		throw new UnsupportedOperationException("Not Implemented");

	}

	public Object remove(int arg0)
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

	public ListIterator listIterator()
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public ListIterator listIterator(int arg0)
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

	public List subList(int arg0, int arg1)
	{
		throw new UnsupportedOperationException("Not Implemented");
	}

}
