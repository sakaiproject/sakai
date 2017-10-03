/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import java.util.Iterator;
import java.util.Comparator;
import java.util.Vector;
import java.util.Collections;

/**
 * <p>
 * SortedIterator is a wrapper iterator that iterates over the wrapped iterator in a sorted order, 
 * the order controlled by a Comparator function provided at construction.
 * 
 * @deprecated use commons-collection instead, this will be removed after 2.9 - Dec 2011
 */
@Deprecated 
public class SortedIterator<E> implements Iterator<E>
{
	/** The sorted iterator. */
	protected Iterator m_iterator = null;

	/**
	 * Creates new SortedIterator based on the base iterator and the comparator function
	 * 
	 * @param iterator
	 *        The original Iterator
	 * @param comparator
	 *        The comparator object
	 */
	public SortedIterator(Iterator<? extends E> iterator, Comparator<? super E> comparator)
	{
		// construct a collection (Vector) from the base iterator so we can sort
		Vector collection = new Vector();
		while (iterator.hasNext())
		{
			collection.add(iterator.next());
		}

		// sort the collection based on comparator
		Collections.sort(collection, comparator);

		// remember the final sorted iterator
		m_iterator = collection.iterator();
	}

	/**
	 * Returns true if the iteration has more elements.
	 * 
	 * @return True if the iteration has more elements; False otherwise.
	 */
	public boolean hasNext()
	{
		return m_iterator.hasNext();
	}

	/**
	 * Returns the next element in the iteration.
	 * 
	 * @return The next element in the iteration
	 */
	public E next()
	{
		return (E) m_iterator.next();
	}

	/**
	 * Removes from the underlying collection the last element returned by the iterator (optional operation) (not supported).
	 */
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
