package org.sakaiproject.sitestats.tool.wicket.components.paging.infinite;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * @author plukasew
 */
public class InfinitePagingDataTableToolbar extends Panel
{
	protected final InfinitePagingDataTable<?, ?> table;

	public InfinitePagingDataTableToolbar(final IModel<?> model, final InfinitePagingDataTable<?, ?> table)
	{
		super(table.newToolbarId(), model);
		this.table = table;
	}

	protected InfinitePagingDataTable<?, ?> getTable()
	{
		return table;
	}
}
