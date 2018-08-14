package org.sakaiproject.sitestats.tool.wicket.components.paging.infinite;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * Navigation toolbar for the SakaiInfinitePagingDataTable
 * @author plukasew
 */
public class SakaiInfinitePagingDataTableNavigationToolbar extends InfinitePagingDataTableToolbar
{
	public SakaiInfinitePagingDataTableNavigationToolbar(final InfinitePagingDataTable<?, ?> table)
	{
		super(null, table);
	}

	@Override
	public void onInitialize()
	{
		super.onInitialize();
		WebMarkupContainer span = new WebMarkupContainer("span");
		add(span);
		span.add(AttributeModifier.replace("colspan", new AbstractReadOnlyModel<String>()
		{
			@Override
			public String getObject()
			{
				return String.valueOf(table.getColumns().size());
			}
		}));
		final Form<?> form = new Form<>("navForm");
		form.add(newPagingNavigator("navigator", table, form).setRenderBodyOnly(true));
		form.add(newNavigatorLabel("navigatorLabel", table).setRenderBodyOnly(true));
		span.add(form);
	}

	protected InfinitePagingNavigator newPagingNavigator(final String navigatorId, final InfinitePagingDataTable<?, ?> table, final Form<?> form)
	{
		return new InfinitePagingNavigator(navigatorId, table, form);
	}

	protected Label newNavigatorLabel(final String id, final InfinitePagingDataTable<?, ?> table)
	{
		return new Label(id, "")
		{

			@Override
			public void onConfigure()
			{
				long startRecord = table.getOffset();
				long rowCount = table.getRowCount();
				long endRecord = startRecord + rowCount;
				if (rowCount > 0)
				{
					++startRecord;
				}

				setDefaultModel(new StringResourceModel("paging_nav_label", table, new Model<>(), new ResourceModel("pager_textItem"), startRecord, endRecord));
			}
		};
	}
}
