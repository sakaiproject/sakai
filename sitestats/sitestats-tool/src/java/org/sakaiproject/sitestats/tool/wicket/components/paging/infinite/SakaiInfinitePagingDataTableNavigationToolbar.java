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
