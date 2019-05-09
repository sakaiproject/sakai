package org.sakaiproject.sitestats.tool.wicket.providers.infinite;

import java.util.Iterator;

import lombok.Getter;

/**
 * A paged iterator over an unknown number of objects of type T
 * @author plukasew
 */
public class PagedInfiniteIterator<T>
{
	private final boolean hasPrevPage, hasNextPage;
	@Getter private final int rowCount;
	@Getter private final Iterator<? extends T> iterator;

	public PagedInfiniteIterator(Iterator<? extends T> iterator, boolean hasPrevPage, boolean hasNextPage, int rowCount)
	{
		this.iterator = iterator;
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
}
