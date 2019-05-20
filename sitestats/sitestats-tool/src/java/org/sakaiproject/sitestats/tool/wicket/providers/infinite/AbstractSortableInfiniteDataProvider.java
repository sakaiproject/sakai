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

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;

/**
 * Abstract base class for a sortable infinite data provider
 * @author plukasew
 * @param <T> model type
 * @param <S> sort type
 */
public abstract class AbstractSortableInfiniteDataProvider<T, S> implements SortableInfiniteDataProvider<T, S>
{
	private final SingleSortState<S> state = new SingleSortState<>();

	@Override
	public final ISortState<S> getSortState()
	{
		return state;
	}

	public SortParam<S> getSort()
	{
		return state.getSort();
	}

	/**
	 * Sets the current sort state
	 *
	 * @param param parameter containing new sorting information
	 */
	public void setSort(final SortParam<S> param)
	{
		state.setSort(param);
	}

	/**
	 * Sets the current sort state
	 *
	 * @param property sort property
	 * @param order sort order
	 */
	public void setSort(final S property, final SortOrder order)
	{
		state.setPropertySortOrder(property, order);
	}

	@Override
	public void detach()
	{
		// noop
	}
}
