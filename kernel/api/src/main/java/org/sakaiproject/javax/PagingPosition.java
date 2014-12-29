/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.javax;

/**
 * <p>
 * PagingPosition models a current position in a paging display, with a first and last item value, 1 based.
 * </p>
 * <p>
 * Implementation note: the default Object.equals() is fine for this class.
 * </p>
 */
public class PagingPosition implements Cloneable
{
	/** The first item position on the current page, 1 based. */
	protected int m_first = 1;

	/** The last item position on the current page, 1 based. */
	protected int m_last = 1;

	/** If true, paging is ebabled, otherwise all items should be used. */
	protected boolean m_paging = true;

	/**
	 * Construct, setting position to select all possible items.
	 */
	public PagingPosition()
	{
		m_first = 1;
		m_last = 1;
	}

	/**
	 * Construct, setting the first and last.
	 * 
	 * @param first
	 *        The first item position, 1 based.
	 * @param last
	 *        The last item position, 1 based.
	 */
	public PagingPosition(int first, int last)
	{
		m_first = first;
		m_last = last;
		validate();
	}

	/**
	 * Adjust the first and list item position by distance, positive or negative.
	 * 
	 * @param distance
	 *        The positive or negative distance to move the first and last item positions.
	 */
	public void adjustPostition(int distance)
	{
		m_first += distance;
		m_last += distance;
		validate();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object clone()
	{
		try
		{
			return super.clone();
		}
		catch (CloneNotSupportedException ignore)
		{
			throw new RuntimeException("Assertion failure");
		}
	}

	/**
	 * Access the first item position, 1 based.
	 * 
	 * @return the first item position, 1 based.
	 */
	public int getFirst()
	{
		return m_first;
	}

	/**
	 * Access the last item position, 1 based.
	 * 
	 * @return the last item position, 1 based.
	 */
	public int getLast()
	{
		return m_last;
	}

	/**
	 * Check if we have paging enabled.
	 * 
	 * @return true if paging is enabled, false if not.
	 */
	public boolean isPaging()
	{
		return m_paging;
	}

	/**
	 * Set the paging enabled value.
	 * 
	 * @param paging
	 *        the new paging enabled value.
	 */
	public void setPaging(boolean paging)
	{
		m_paging = paging;
	}

	/**
	 * Set the first and last positions.
	 * 
	 * @param first
	 *        The new first item position, 1 based.
	 * @param last
	 *        The new last item position, 1 based.
	 */
	public void setPosition(int first, int last)
	{
		m_first = first;
		m_last = last;
		validate();
	}

	/**
	 * Adjust the first and last to be valid.
	 */
	protected void validate()
	{
		if (m_first < 0) m_first = 1;
		if (m_last < m_first) m_last = m_first;
	}

	/**
	 * Adjust the first and last to be valid and within the range 1..biggestLast
	 * 
	 * @param biggestLast
	 *        The largest valid value for last
	 */
	public void validate(int biggestLast)
	{
		if (m_first < 0) m_first = 1;
		if (m_last > biggestLast) m_last = biggestLast;
		if (m_last < m_first) m_last = m_first;
	}
}
