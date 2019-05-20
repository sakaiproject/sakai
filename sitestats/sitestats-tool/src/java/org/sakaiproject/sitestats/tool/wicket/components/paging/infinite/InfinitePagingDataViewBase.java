/**
 * Copyright (c) 2006-2018 The Apereo Foundation
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
package org.sakaiproject.sitestats.tool.wicket.components.paging.infinite;

import java.util.Iterator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;
import org.sakaiproject.sitestats.tool.wicket.providers.infinite.InfiniteDataProvider;
import org.sakaiproject.sitestats.tool.wicket.providers.infinite.PagedInfiniteIterator;

/**
 * Abstract base class for the dataview used in SakaiInfinitePagingDataTable
 * @author plukasew
 */
public abstract class InfinitePagingDataViewBase<T> extends AbstractInfinitePagingView<T>
{
	private final InfiniteDataProvider<T> dataProvider;

	public InfinitePagingDataViewBase(String id, InfiniteDataProvider<T> dataProvider)
	{
		super(id);
		this.dataProvider = Args.notNull(dataProvider, "dataProvider");
	}

	protected final InfiniteDataProvider<T> internalGetDataProvider()
	{
		return dataProvider;
	}

	@Override
	protected final Iterator<IModel<T>> getItemModels(long offset, long count)
	{
		this.offset = offset;
		PagedInfiniteIterator<? extends T> pii = internalGetDataProvider().iterator(offset, count);
		hasNextPage = pii.hasNextPage();
		rowCount = pii.getRowCount();

		return new ModelIterator<>(internalGetDataProvider(), pii.getIterator(), count);
	}

	private static final class ModelIterator<T> implements Iterator<IModel<T>>
	{
		private final Iterator<? extends T> items;
		private final InfiniteDataProvider<T> dataProvider;
		private final long max;
		private long index;

		public ModelIterator(InfiniteDataProvider<T> dataProvider, Iterator<? extends T> items, long count)
		{
			this.dataProvider = dataProvider;
			max = count;
			this.items = items;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasNext()
		{
			return items != null && items.hasNext() && (index < max);
		}

		@Override
		public IModel<T> next()
		{
			index++;
			return dataProvider.model(items.next());
		}
	}

	@Override
	protected void onDetach()
	{
		dataProvider.detach();
		super.onDetach();
	}
}
