package org.sakaiproject.sitestats.api.event.detailed;

/**
 * Immutable class to hold parameters for paging of detailed events queries
 *
 * @author plukasew
 */
public final class PagingParams
{
	public final long start;
	public final int startInt;
	public final long pageSize;
	public final int pageSizeInt;

	/**
	 * Constructor requiring all parameters
	 *
	 * @param start the starting row offset for results
	 * @param count the number of results to return
	 */
	public PagingParams(long start, long count)
	{
		this.start = start;
		startInt = (int) start;
		pageSize = count;
		pageSizeInt = (int) count;
	}
}