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
