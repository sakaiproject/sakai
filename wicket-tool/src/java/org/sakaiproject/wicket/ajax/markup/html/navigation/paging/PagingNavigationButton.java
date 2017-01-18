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

/* This code is a port of the Apache Wicket class 
 * 	org.apache.wicket.markup.html.navigation.paging.PagingNavigationLink
 * that uses a Button rather than a Link. 
 * 
 * The original code was authored by Jonathan Locke, Eelco Hillenius, and Martijn Dashorst
 * and licensed according to the license pasted below:
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

public class PagingNavigationButton extends Button {

	private static final long serialVersionUID = 1L;
	
	/** The pageable list view. */
	protected final IPageable pageable;

	/** The page of the PageableListView this link is for. */
	private final int pageNumber;
	    
	private boolean autoEnable = false;
	
	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The pageable component for this page link
	 * @param pageNumber
	 *            The page number in the PageableListView that this link links
	 *            to. Negative pageNumbers are relative to the end of the list.
	 */
	public PagingNavigationButton(final String id,
			final IPageable pageable, final int pageNumber)
	{
		super(id);
		setAutoEnable(true);
		this.pageNumber = pageNumber;
		this.pageable = pageable;
		setDefaultFormProcessing(false);
	}

	
	public void onSubmit()
	{
		pageable.setCurrentPage(getPageNumber());
	}

	/**
	 * Get pageNumber.
	 * 
	 * @return pageNumber.
	 */
	public final int getPageNumber()
	{
	    int idx = pageNumber;
		if (idx < 0)
		{
			idx = pageable.getPageCount() + idx;
		}
		
		if (idx > (pageable.getPageCount() - 1))
		{
			idx = pageable.getPageCount() - 1;
		}

		if (idx < 0)
		{
			idx = 0;
		}
		
		return idx;
	}

	/**
	 * @return True if this page is the first page of the containing
	 *         PageableListView
	 */
	public final boolean isFirst()
	{
		return getPageNumber() == 0;
	}

	/**
	 * @return True if this page is the last page of the containing
	 *         PageableListView
	 */
	public final boolean isLast()
	{
		return getPageNumber() == (pageable.getPageCount() - 1);
	}

	/**
	 * Returns true if this PageableListView navigation link links to the given
	 * page.
	 * 
	 * @param page
	 *            The page
	 * @return True if this link links to the given page
	 * @see org.apache.wicket.markup.html.link.PageLink#linksTo(org.apache.wicket.Page)
	 */
	public final boolean linksTo(final Page page)
	{
		return getPageNumber() == pageable.getCurrentPage();
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
