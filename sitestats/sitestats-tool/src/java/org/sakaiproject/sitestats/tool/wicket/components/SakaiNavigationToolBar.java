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
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.components;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

/**
 * @author Nuno Fernandes
 */
public class SakaiNavigationToolBar extends AjaxNavigationToolbar
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param table
	 *            data table this toolbar will be attached to
	 */
	public SakaiNavigationToolBar(final DataTable table)
	{
		super(table);

		WebMarkupContainer span = (WebMarkupContainer) get("span");
		span.add(new AttributeModifier("colspan", new Model(String.valueOf(table.getColumns().size()))));

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
	@Override
	protected AjaxPagingNavigator newPagingNavigator(String navigatorId, final DataTable table)
	{
		// Create a standard AjaxPagingNavigator without customization
		AjaxPagingNavigator navigator = new AjaxPagingNavigator(navigatorId, table);
		navigator.setVersioned(false);
		
		// Add the row selector as a separate component next to the navigator
		WebMarkupContainer span = (WebMarkupContainer) get("span");
		
		// Only add the row selector if it doesn't already exist
		if (span.get("rowSizeSelector") == null) {
			span.add(createRowNumberSelector("rowSizeSelector", table));
		}
		
		return navigator;
	}
	
	/**
	 * Creates a row size selector dropdown that changes items per page
	 */
	protected DropDownChoice createRowNumberSelector(String id, final DataTable table) {
		List<String> choices = Arrays.asList("5", "10", "20", "50", "100", "200");
		
		// Create a model that gets/sets the page size from the table
		IModel<String> model = new Model<String>() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public String getObject() {
				return String.valueOf(table.getItemsPerPage());
			}
			
			@Override
			public void setObject(String object) {
				table.setItemsPerPage(Integer.parseInt(object));
			}
		};

		DropDownChoice<String> rowNumberSelector = new DropDownChoice<>(id, model, choices, new IChoiceRenderer<>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getDisplayValue(String object) {
				return new StringResourceModel("pager_textPageSize")
						.setParameters(object)
						.getString();
			}

			@Override
			public String getIdValue(String object, int index) {
				return object;
			}

			@Override
			public String getObject(String id, IModel choices) {
				return id;
			}
		});

		rowNumberSelector.add(new FormComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate() {
				// Tell the PageableListView which page to print next
				table.setCurrentPage(0);

				// Return the current page
				setResponsePage(getPage());
			}
		});

		return rowNumberSelector;
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
	@Override
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
