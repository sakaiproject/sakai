package org.sakaiproject.sitestats.tool.wicket.components.paging.infinite;

import java.util.List;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.sakaiproject.sitestats.tool.wicket.providers.infinite.SortableInfiniteDataProvider;

/**
 * Adds Sakai customization + Ajax support to InfinitePagingDataTable
 * @author plukasew
 * @param <T> model type
 * @param <S> sort type
 */
public class SakaiInfinitePagingDataTable<T, S> extends InfinitePagingDataTable<T, S>
{
	public SakaiInfinitePagingDataTable(final String id, final List<? extends IColumn<T, S>> columns, final SortableInfiniteDataProvider<T, S> dataProvider,
		final int rowsPerPage)
	{
		super(id, columns, dataProvider, rowsPerPage);
		setOutputMarkupId(true);
		setVersioned(false);
		addTopToolbar(new SakaiInfinitePagingDataTableNavigationToolbar(this));
		addTopToolbar(new InfinitePagingDataTableHeadersToolbar(this, dataProvider));
		addBottomToolbar(new SakaiInfinitePagingNoRecordsToolbar(this));
	}
}
