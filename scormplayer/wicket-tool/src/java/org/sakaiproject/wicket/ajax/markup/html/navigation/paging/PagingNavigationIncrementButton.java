/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.wicket.ajax.markup.html.navigation.paging;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.navigation.paging.IPageable;

public class PagingNavigationIncrementButton extends Button {
	private static final long serialVersionUID = 1L;

	/** The increment. */
	private final int increment;

	/** The PageableListView the page links are referring to. */
	protected final IPageable pageable;

	private boolean autoEnable = false;
	
	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The pageable component the page links are referring to
	 * @param increment
	 *            increment by
	 */
	public PagingNavigationIncrementButton(final String id, final IPageable pageable,
			final int increment)
	{
		super(id);
		setAutoEnable(true);
		this.increment = increment;
		this.pageable = pageable;
		setDefaultFormProcessing(false);
	}

	public void onSubmit()
	{
		// Tell the PageableListView which page to print next
		pageable.setCurrentPage(getPageNumber());

		// We do need to redirect, else refresh refresh will go to next, next
		setRedirect(true);

		// Return the current page.
		setResponsePage(getPage());
	}

	/**
	 * Determines the next page number for the pageable component.
	 * 
	 * @return the new page number
	 */
	public final int getPageNumber()
	{
		// Determine the page number based on the current
		// PageableListView page and the increment
		int idx = pageable.getCurrentPage() + increment;

		// make sure the index lies between 0 and the last page
		return Math.max(0, Math.min(pageable.getPageCount() - 1, idx));
	}

	/**
	 * @return True if it is referring to the first page of the underlying
	 *         PageableListView.
	 */
	public boolean isFirst()
	{
		return pageable.getCurrentPage() <= 0;
	}

	/**
	 * @return True if it is referring to the last page of the underlying
	 *         PageableListView.
	 */
	public boolean isLast()
	{
		return pageable.getCurrentPage() >= (pageable.getPageCount() - 1);
	}

	/**
	 * Returns true if the page link links to the given page.
	 * 
	 * @param page
	 *            ignored
	 * @return True if this link links to the given page
	 * @see org.apache.wicket.markup.html.link.PageLink#linksTo(org.apache.wicket.Page)
	 */
	public boolean linksTo(final Page page)
	{
		int currentPage = pageable.getCurrentPage();
		if (((increment < 0) && isFirst()) || ((increment > 0) && isLast()))
		{
			return true;
		}

		return false;
	}
	
	
	public boolean isEnabled()
	{
		// If we're auto-enabling
		if (getAutoEnable())
		{
			// the link is enabled if this link doesn't link to the current page
			return !linksTo(getPage());
		}
		return super.isEnabled();
	}
	
	public final boolean getAutoEnable()
	{
		return autoEnable;
	}
	
	/**
	 * Sets whether this link should automatically enable/disable based on
	 * current page.
	 * 
	 * @param autoEnable
	 *            whether this link should automatically enable/disable based on
	 *            current page.
	 * @return This
	 */
	public final Button setAutoEnable(final boolean autoEnable)
	{
		this.autoEnable = autoEnable;
		return this;
	}	
}
