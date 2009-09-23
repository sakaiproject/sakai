/**
 * $URL:$
 * $Id:$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;

/**
 * @author Nuno Fernandes
 */
public class SakaiNavigationToolBar extends AjaxNavigationToolbar
{
	private static final long serialVersionUID = 1L;

	private final DataTable table;

	/**
	 * Constructor
	 * 
	 * @param table
	 *            data table this toolbar will be attached to
	 */
	public SakaiNavigationToolBar(final DataTable table)
	{
		super(table);
		this.table = table;

		WebMarkupContainer span = (WebMarkupContainer) get("span");
		span.add(new AttributeModifier("colspan", true, new Model(
			String.valueOf(table.getColumns().length))));

		span.get("navigator").replaceWith(newPagingNavigator("navigator", table));
		span.get("navigatorLabel").replaceWith(newNavigatorLabel("navigatorLabel", table));
	}


	/**
	 * Factory method used to create the paging navigator that will be used by the datatable
	 * 
	 * @param navigatorId
	 *            component id the navigator should be created with
	 * @param table
	 *            dataview used by datatable
	 * @return paging navigator that will be used to navigate the data table
	 */
	protected SakaiPagingNavigator newPagingNavigator(String navigatorId, final DataTable table)
	{
		return new SakaiPagingNavigator(navigatorId, table);
	}

	/**
	 * Factory method used to create the navigator label that will be used by the datatable
	 * 
	 * @param navigatorId
	 *            component id navigator label should be created with
	 * @param table
	 *            dataview used by datatable
	 * @return navigator label that will be used to navigate the data table
	 * 
	 */
	protected WebComponent newNavigatorLabel(String navigatorId, final DataTable table)
	{
		return new SakaiNavigatorLabel(navigatorId, table);
	}

	/**
	 * Hides this toolbar when there is only one page in the table
	 * 
	 * @see org.apache.wicket.Component#isVisible()
	 */
	@Override
	public boolean isVisible()
	{
		return true;
	}
}
