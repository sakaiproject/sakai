/**
 * $URL$
 * $Id$
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

import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationIncrementLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

public class SakaiPagingNavigator extends AjaxPagingNavigator {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The pageable component the page links are referring to.
	 */
	public SakaiPagingNavigator(final String id, final IPageable pageable)
	{
		this(id, pageable, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The pageable component the page links are referring to.
	 * @param labelProvider
	 *            The label provider for the link text.
	 */
	public SakaiPagingNavigator(final String id, final IPageable pageable,
		final IPagingLabelProvider labelProvider)
	{
		super(id, pageable, labelProvider);

	}

	@Override
	protected void onBeforeRender()
	{
		if (get("rowNumberSelector") == null)
		{
			setDefaultModel(new CompoundPropertyModel(this));
			
			// Get the row number selector
			add(newRowNumberSelector(getPageable()));

			// Add additional page links
			replace(newPagingNavigationLink("first", getPageable(), 0));
			replace(newPagingNavigationIncrementLink("prev", getPageable(), -1));
			replace(newPagingNavigationIncrementLink("next", getPageable(), 1));
			replace(newPagingNavigationLink("last", getPageable(), -1));
		}
		super.onBeforeRender();
	}

	/**
	 * Create a new increment link. May be subclassed to make use of specialized links, e.g. Ajaxian
	 * links.
	 * 
	 * @param id
	 *            the link id
	 * @param pageable
	 *            the pageable to control
	 * @param increment
	 *            the increment
	 * @return the increment link
	 */
	protected Link newPagingNavigationIncrementLink(String id, IPageable pageable, int increment)
	{
		AjaxPagingNavigationIncrementLink link = new AjaxPagingNavigationIncrementLink(id, pageable, increment);
		link.setVersioned(false);
		return link;
	}

	/**
	 * Create a new pagenumber link. May be subclassed to make use of specialized links, e.g.
	 * Ajaxian links.
	 * 
	 * @param id
	 *            the link id
	 * @param pageable
	 *            the pageable to control
	 * @param pageNumber
	 *            the page to jump to
	 * @return the pagenumber link
	 */
	protected Link newPagingNavigationLink(String id, IPageable pageable, int pageNumber) {
		AjaxPagingNavigationLink link = new AjaxPagingNavigationLink(id, pageable, pageNumber);
		link.setVersioned(false);
		return link;
	}
	
	protected DropDownChoice newRowNumberSelector(final IPageable pageable) {
		List<String> choices = Arrays.asList("5", "10", "20", "50", "100", "200");

		DropDownChoice<String> rowNumberSelector = new DropDownChoice<>("rowNumberSelector", choices, new IChoiceRenderer<>() {
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
				pageable.setCurrentPage(0);

				// We do need to redirect, else refresh will go to next, next
				// setRedirect(true); // This line is commented out because it may not be needed

				// Return the current page.
				setResponsePage(getPage());
				super.onUpdate();
			}
		});

		return rowNumberSelector;
	}
	
	public String getRowNumberSelector() {	
		return String.valueOf(((DataTable) getPageable()).getItemsPerPage());
	}
	public void setRowNumberSelector(String value) {
		((DataTable) getPageable()).setItemsPerPage(Integer.valueOf(value));
	}

	/**
	 * Create a new PagingNavigation. May be subclassed to make us of specialized PagingNavigation.
	 * 
	 * @param pageable
	 *            the pageable component
	 * @param labelProvider
	 *            The label provider for the link text.
	 * @return the navigation object
	 */
	@Override
	protected PagingNavigation newNavigation(final String id, final IPageable pageable,
		final IPagingLabelProvider labelProvider)
	{
		return new PagingNavigation("navigation", pageable, labelProvider)
		{
			@Override
			public boolean isVisible()
			{
				// hide the numbered navigation bar e.g. 1 | 2 | 3 etc.
				return false;
			}
		};
	}
}
