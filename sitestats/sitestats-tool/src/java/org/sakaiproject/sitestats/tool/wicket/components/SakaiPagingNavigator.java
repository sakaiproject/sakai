package org.sakaiproject.sitestats.tool.wicket.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigationIncrementLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigationLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.StringResourceModel;

public class SakaiPagingNavigator extends Panel {

	private static final long serialVersionUID = 1L;

	/** The navigation bar to be printed, e.g. 1 | 2 | 3 etc. */
	private PagingNavigation pagingNavigation;

	private final IPageable pageable;
	private final IPagingLabelProvider labelProvider;

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
		super(id);

		this.pageable = pageable;
		this.labelProvider = labelProvider;

	}


	/**
	 * {@link IPageable} this navigator is linked with
	 * 
	 * @return {@link IPageable} instance
	 */
	public final IPageable getPageable()
	{
		return pageable;
	}

	protected void onBeforeRender()
	{
		if (get("first") == null)
		{
			// Get the navigation bar and add it to the hierarchy
			//pagingNavigation = newNavigation(pageable, labelProvider);
			//add(pagingNavigation);

			setModel(new CompoundPropertyModel(this));
			
			// Get the row number selector
			add(newRowNumberSelector(pageable));

			// Add additional page links
			add(newPagingNavigationLink("first", pageable, 0));
			add(newPagingNavigationIncrementLink("prev", pageable, -1));
			add(newPagingNavigationIncrementLink("next", pageable, 1));
			add(newPagingNavigationLink("last", pageable, -1));
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
		return new PagingNavigationIncrementLink(id, pageable, increment);
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
	protected Link newPagingNavigationLink(String id, IPageable pageable, int pageNumber)
	{
		return new PagingNavigationLink(id, pageable, pageNumber);
	}
	
	protected DropDownChoice newRowNumberSelector(final IPageable pageable)
	{
		List<String> choices = new ArrayList<String>();
		choices.add("5");
		choices.add("10");
		choices.add("20");
		choices.add("50");
		choices.add("100");
		choices.add("200");
		DropDownChoice rowNumberSelector = new DropDownChoice("rowNumberSelector", choices, new IChoiceRenderer() {
			public Object getDisplayValue(Object object) {
				return new StringResourceModel(
						"pager_textPageSize", 
						getParent(), 
						null,
						new Object[] {object}).getString();
			}
			public String getIdValue(Object object, int index) {
				return (String) object;
			}
		}) {
			@Override
			protected boolean wantOnSelectionChangedNotifications() {
				return true;
			}

			@Override
			protected void onSelectionChanged(Object newSelection) {
				// Tell the PageableListView which page to print next
				pageable.setCurrentPage(0);

				// We do need to redirect, else refresh refresh will go to next, next
				setRedirect(true);

				// Return the current page.
				setResponsePage(getPage());
				super.onSelectionChanged(newSelection);
			}
			
		};
		return rowNumberSelector;
	}
	
	public String getRowNumberSelector() {	
		return String.valueOf(((DataTable) pageable).getRowsPerPage());
	}
	public void setRowNumberSelector(String value) {
		((DataTable) pageable).setRowsPerPage(Integer.valueOf(value));
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
	protected PagingNavigation newNavigation(final IPageable pageable,
		final IPagingLabelProvider labelProvider)
	{
		return new PagingNavigation("navigation", pageable, labelProvider);
	}

	/**
	 * Gets the pageable navigation component for configuration purposes.
	 * 
	 * @return the associated pageable navigation.
	 */
	public final PagingNavigation getPagingNavigation()
	{
		return pagingNavigation;
	}
}
