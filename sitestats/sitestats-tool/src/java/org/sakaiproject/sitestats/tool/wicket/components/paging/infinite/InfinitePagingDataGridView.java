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
