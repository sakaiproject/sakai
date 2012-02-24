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

package org.sakaiproject.authz.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p>
 * SeriesIterator is an iterator over a series of other iterators.
 * </p>
 */
public class SeriesIterator implements Iterator
{
	/** The enumeration over which this iterates. */
	protected Iterator[] m_iterators = null;

	/** The m_iterators index that is current. */
	protected int m_index = 0;

	/**
	 * Construct to handle a series of two iterators.
	 * 
	 * @param one
	 *        The first iterator.
	 * @param two
	 *        The second iterator.
	 */
	public SeriesIterator(Iterator one, Iterator two)
	{
		m_iterators = new Iterator[2];
		m_iterators[0] = one;
		m_iterators[1] = two;

	} // SeriesIterator

	public Object next() throws NoSuchElementException
	{
		while (!m_iterators[m_index].hasNext())
		{
			m_index++;
			if (m_index >= m_iterators.length) throw new NoSuchElementException();
		}
		return m_iterators[m_index].next();
	}

	public boolean hasNext()
	{
		while (!m_iterators[m_index].hasNext())
		{
			m_index++;
			if (m_index >= m_iterators.length) return false;
		}
		return true;
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
