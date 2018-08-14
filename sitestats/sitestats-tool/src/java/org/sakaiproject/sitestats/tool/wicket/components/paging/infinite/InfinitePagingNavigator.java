package org.sakaiproject.sitestats.tool.wicket.components.paging.infinite;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.sitestats.tool.wicket.components.dropdown.SakaiStringResourceChoiceRenderer;
import org.sakaiproject.sitestats.tool.wicket.components.dropdown.SakaiSpinnerDropDownChoice;
import org.sakaiproject.sitestats.tool.wicket.components.dropdown.SakaiSpinningSelectOnChangeBehavior;

/**
 * Paging navigator for the SakaiInfinitePagingDataTable
 * @author plukasew
 */
public class InfinitePagingNavigator extends Panel
{
	private final InfinitePagingDataTable table;
	private final String pageSizeSelection;
	private final Form<?> form;

	public InfinitePagingNavigator(final String id, final InfinitePagingDataTable table, final Form<?> form)
	{
		super(id);
		this.table = table;
		this.form = form;
		pageSizeSelection = String.valueOf(table.getItemsPerPage());
	}

	public final InfinitePagingDataTable getDataTable()
	{
		return table;
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		add(newPageSizeSelector(table));
		add(newInfinitePagingIncrementButton("prev", table, false, form));
		add(newInfinitePagingIncrementButton("next", table, true, form));
	}

	protected SakaiSpinnerDropDownChoice<String> newPageSizeSelector(final InfinitePagingDataTable table)
	{
		final List<String> choices = new ArrayList<>();
		choices.add("5");
		choices.add("10");
		choices.add("20");
		choices.add("50");
		choices.add("100");
		choices.add("200");

		IModel<String> choiceModel = new PropertyModel<>(this, "pageSizeSelection");
		IModel<String> labelModel = new StringResourceModel("pager_select_label", this, null);
		SakaiSpinnerDropDownChoice<String> ddc = new SakaiSpinnerDropDownChoice<>("pageSize", choiceModel, choices,
				new SakaiStringResourceChoiceRenderer("pager_textPageSize", this), labelModel,
				new SakaiSpinningSelectOnChangeBehavior()
				{
					@Override
					protected void onUpdate(AjaxRequestTarget target)
					{
						int pageSize = Integer.parseInt(pageSizeSelection);
						table.setItemsPerPage(pageSize);
						if (target != null)
						{
							target.add(table);
						}
					}
				});

		return ddc;
	}

	protected Button newInfinitePagingIncrementButton(String id, InfinitePagingDataTable table, boolean increment, Form<?> form)
	{
		return new InfinitePagingNavigationIncrementButton(id, table, increment, form);
	}
}
