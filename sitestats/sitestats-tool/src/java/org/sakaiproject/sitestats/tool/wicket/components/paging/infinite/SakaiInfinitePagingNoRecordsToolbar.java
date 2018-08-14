package org.sakaiproject.sitestats.tool.wicket.components.paging.infinite;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.ResourceModel;

/**
 * Toolbar to display when there are no records to show in the table
 * @author plukasew
 */
public class SakaiInfinitePagingNoRecordsToolbar extends InfinitePagingDataTableToolbar
{
	public SakaiInfinitePagingNoRecordsToolbar(final InfinitePagingDataTable<?, ?> table)
	{
		super(null, table);

		WebMarkupContainer td = new WebMarkupContainer("td");
		add(td);

		td.add(AttributeModifier.replace("colspan", new AbstractReadOnlyModel<String>()
		{
			@Override
			public String getObject()
			{
				return String.valueOf(table.getColumns().size());
			}
		}));

		td.add(new Label("msg", new ResourceModel("no_data")));
	}

	@Override
	public boolean isVisible()
	{
		return getTable().getRowCount() < 1;
	}
}
