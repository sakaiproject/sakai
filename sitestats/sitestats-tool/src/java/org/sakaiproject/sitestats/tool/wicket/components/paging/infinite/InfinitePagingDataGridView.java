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

import java.util.List;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.sakaiproject.sitestats.tool.wicket.providers.infinite.InfiniteDataProvider;

/**
 * DataGridView for the SakaiInfinitePagingDataTable
 * @author plukasew
 * @param <T> model type
 */
public class InfinitePagingDataGridView<T> extends AbstractInfinitePagingDataGridView<T>
{
	public InfinitePagingDataGridView(final String id, final List<? extends ICellPopulator<T>> populators, final InfiniteDataProvider<T> dataProvider)
	{
		super(id, populators, dataProvider);
	}

	public List<? extends ICellPopulator<T>> getPopulators()
	{
		return internalGetPopulators();
	}

	public InfiniteDataProvider<T> getDataProvider()
	{
		return internalGetDataProvider();
	}
}
