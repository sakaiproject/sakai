package org.sakaiproject.sitestats.tool.wicket.components.paging.infinite;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Form;
import org.sakaiproject.sitestats.tool.wicket.components.SakaiAjaxButton;

/**
 * Button for navigating between pages of the SakaiInfinitePagingDataTable
 * @author plukasew
 */
public class InfinitePagingNavigationIncrementButton extends SakaiAjaxButton
{
	protected final InfinitePagingDataTable table;
	private final boolean increment;

	public InfinitePagingNavigationIncrementButton(final String id, final InfinitePagingDataTable table, final boolean increment, final Form<?> form)
	{
		super(id, form);
		this.increment = increment;
		this.table = table;
		willRenderOnClick = true;
	}

	@Override
	public void onSubmit(AjaxRequestTarget target, Form<?> form)
	{
		if (increment)
		{
			table.nextPage();
		}
		else
		{
			table.prevPage();
		}

		if (target != null)
		{
			target.add(table);
		}
	}

	@Override
	public void onConfigure()
	{
		setEnabled(increment && table.hasNextPage() || !increment && table.hasPrevPage());
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.remove("onclick");
	}
}
