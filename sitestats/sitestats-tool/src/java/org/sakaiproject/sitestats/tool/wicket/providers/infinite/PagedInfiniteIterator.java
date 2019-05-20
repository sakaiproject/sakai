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
