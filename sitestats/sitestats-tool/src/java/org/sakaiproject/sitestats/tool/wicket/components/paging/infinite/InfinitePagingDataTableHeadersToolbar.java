/**
 * Copyright (c) 2006-2019 The Apereo Foundation
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
// Adapted from https://github.com/apache/wicket/blob/wicket-6.x/wicket-extensions/src/main/java/org/apache/wicket/extensions/markup/html/repeater/data/table/HeadersToolbar.java

package org.sakaiproject.sitestats.tool.wicket.components.paging.infinite;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.sort.AjaxFallbackOrderByBorder;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.sakaiproject.sitestats.api.StatsManager;

/**
 * A header toolbar for the SakaiInfinitePagingDataTable
 * @author plukasew
 * @param <S> sort type
 */
public class InfinitePagingDataTableHeadersToolbar<S> extends InfinitePagingDataTableToolbar
{
	private static final String TOOLBAR_JS = StatsManager.SITESTATS_WEBAPP + "/script/infinitepagingdatatableheaderstoolbar.js";

	public <T> InfinitePagingDataTableHeadersToolbar(final InfinitePagingDataTable<T, S> table, final ISortStateLocator<S> stateLocator)
	{
		super(null, table);

		RefreshingView<IColumn<T, S>> headers = new RefreshingView<IColumn<T, S>>("headers")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Iterator<IModel<IColumn<T, S>>> getItemModels()
			{
				List<IModel<IColumn<T, S>>> columnsModels = new LinkedList<>();

				for (IColumn<T, S> column : table.getColumns())
				{
					columnsModels.add(Model.of(column));
				}

				return columnsModels.iterator();
			}

			@Override
			protected void populateItem(Item<IColumn<T, S>> item)
			{
				final IColumn<T, S> column = item.getModelObject();

				WebMarkupContainer header;
				if (column.isSortable())
				{
					header = newSortableHeader("header", column.getSortProperty(), stateLocator);
				}
				else
				{
					header = new WebMarkupContainer("header");
				}

				if (column instanceof IStyledColumn)
				{
					InfinitePagingDataTable.CssAttributeBehavior cssAttributeBehavior = new InfinitePagingDataTable.CssAttributeBehavior()
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected String getCssClass()
						{
							return ((IStyledColumn<?, S>)column).getCssClass();
						}
					};

					header.add(cssAttributeBehavior);
				}

				item.add(header);
				item.setRenderBodyOnly(true);
				header.add(column.getHeader("label"));
			}
		};

		add(headers);
		WebMarkupContainer sortToggle = new WebMarkupContainer("sortToggle");
		sortToggle.setVisible(table.getColumns().stream().anyMatch(IColumn::isSortable));
		add(sortToggle);
		table.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forUrl(TOOLBAR_JS));
		long sortableCols = getTable().getColumns().stream().filter(IColumn::isSortable).count();
		String attachScript = String.format("RHTB.init('%s', %d);", getTable().getMarkupId(), sortableCols);
		response.render(OnLoadHeaderItem.forScript(attachScript));
	}

	protected WebMarkupContainer newSortableHeader(final String borderId, final S property, final ISortStateLocator<S> locator)
	{
		return new AjaxFallbackOrderByBorder<S>(borderId, property, locator, getAjaxCallListener())
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onAjaxClick(final AjaxRequestTarget target)
			{
				target.add(getTable());
			}

			@Override
			protected void onSortChanged()
			{
				super.onSortChanged();
				getTable().setOffset(0);
			}
		};
	}

	protected IAjaxCallListener getAjaxCallListener()
	{
		return null;
	}
}
