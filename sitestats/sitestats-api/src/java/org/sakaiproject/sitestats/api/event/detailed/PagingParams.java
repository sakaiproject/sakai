/**
 * Copyright (c) 2006-2019 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	 * @param pageSize the number of results to return
	 */
	public PagingParams(long start, long pageSize)
	{
		this.start = start;
		this.startInt = (int) start;
		this.pageSize = pageSize;
		this.pageSizeInt = (int) pageSize;
	}
}
