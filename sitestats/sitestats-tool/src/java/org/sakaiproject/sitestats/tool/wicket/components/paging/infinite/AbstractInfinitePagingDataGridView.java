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
// Adapted from https://github.com/apache/wicket/blob/wicket-6.x/wicket-extensions/src/main/java/org/apache/wicket/extensions/markup/html/repeater/data/grid/AbstractDataGridView.java

package org.sakaiproject.sitestats.tool.wicket.components.paging.infinite;

import java.util.List;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.sitestats.tool.wicket.providers.infinite.InfiniteDataProvider;

/**
 * A DataGridView for the SakaiInfinitePagingDataTable
 * @author plukasew
 * @param <T> model type
 */
public class AbstractInfinitePagingDataGridView<T> extends InfinitePagingDataViewBase<T>
{
	private static final String CELL_REPEATER_ID = "cells";
	private static final String CELL_ITEM_ID = "cell";

	private final List<? extends ICellPopulator<T>> populators;

	public AbstractInfinitePagingDataGridView(final String id, final List<? extends ICellPopulator<T>> populators, final InfiniteDataProvider<T> dataProvider)
	{
		super(id, dataProvider);
		this.populators = populators;
	}

	protected final List<? extends ICellPopulator<T>> internalGetPopulators()
	{
		return populators;
	}

	/**
	 * Factory method for Item container that represents a cell.
	 *
	 * @see Item
	 * @see RefreshingView#newItem(String, int, IModel)
	 *
	 * @param id
	 *            component id for the new data item
	 * @param index
	 *            the index of the new data item
	 * @param model
	 *            the model for the new data item
	 *
	 * @return DataItem created DataItem
	 */
	protected Item<ICellPopulator<T>> newCellItem(final String id, final int index, final IModel<ICellPopulator<T>> model)
	{
		return new Item<>(id, index, model);
	}

	@Override
	protected final Item<T> newItem(final String id, final int index, final IModel<T> model)
	{
		return newRowItem(id, index, model);
	}

	/**
	 * Factory method for Item container that represents a row.
	 *
	 * @see Item
	 * @see RefreshingView#newItem(String, int, IModel)
	 *
	 * @param id
	 *            component id for the new data item
	 * @param index
	 *            the index of the new data item
	 * @param model
	 *            the model for the new data item.
	 *
	 * @return DataItem created DataItem
	 */
	protected Item<T> newRowItem(final String id, final int index, final IModel<T> model)
	{
		return new Item<>(id, index, model);
	}


	/**
	 * @see org.apache.wicket.markup.repeater.data.DataViewBase#onDetach()
	 */
	@Override
	protected void onDetach()
	{
		super.onDetach();
		if (populators != null)
		{
			for (ICellPopulator<T> populator : populators)
			{
				populator.detach();
			}
		}
	}

	/**
	 * @param item
	 * @see org.apache.wicket.markup.repeater.RefreshingView#populateItem(org.apache.wicket.markup.repeater.Item)
	 */
	@Override
	protected final void populateItem(final Item<T> item)
	{
		RepeatingView cells = new RepeatingView(CELL_REPEATER_ID);
		item.add(cells);

		int populatorsNumber = populators.size();
		for (int i = 0; i < populatorsNumber; i++)
		{
			ICellPopulator<T> populator = populators.get(i);
			IModel<ICellPopulator<T>> populatorModel = new Model<>(populator);
			Item<ICellPopulator<T>> cellItem = newCellItem(cells.newChildId(), i, populatorModel);
			cells.add(cellItem);

			populator.populateItem(cellItem, CELL_ITEM_ID, item.getModel());

			if (cellItem.get("cell") == null)
			{
				throw new WicketRuntimeException(
					populator.getClass().getName() +
						".populateItem() failed to add a component with id [" +
						CELL_ITEM_ID +
						"] to the provided [cellItem] object. Make sure you call add() on cellItem and make sure you gave the added component passed in 'componentId' id. ( *cellItem*.add(new MyComponent(*componentId*, rowModel) )");
			}
		}
	}
}
