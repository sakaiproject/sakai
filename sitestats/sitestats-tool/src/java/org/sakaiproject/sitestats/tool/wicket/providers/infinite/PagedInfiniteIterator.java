package org.sakaiproject.sitestats.tool.wicket.providers.infinite;

import java.util.Iterator;

/**
 * A paged iterator over an unknown number of objects of type T
 * @author plukasew
 */
public class PagedInfiniteIterator<T>
{
	private final boolean hasPrevPage, hasNextPage;
	private final int rowCount;
	private final Iterator<? extends T> iter;

	public PagedInfiniteIterator(Iterator<? extends T> iterator, boolean hasPrevPage, boolean hasNextPage, int rowCount)
	{
		iter = iterator;
		this.hasPrevPage = hasPrevPage;
		this.hasNextPage = hasNextPage;
		this.rowCount = rowCount;
	}

	public boolean hasPrevPage()
	{
		return hasPrevPage;
	}

	public boolean hasNextPage()
	{
		return hasNextPage;
	}

	public Iterator<? extends T> getIterator()
	{
		return iter;
	}

	public int getRowCount()
	{
		return rowCount;
	}
}
