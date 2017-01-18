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

/*
 * This code has borrowed extensively from the Apache Wicket class
 * 	org.apache.wicket.markup.html.navigation.paging.PagingNavigator,
 * created by Juergen Donnerstag. The original license for that class
 * is copied below:
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

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

public class ClassicPagingNavigator extends Panel {

	private static final long serialVersionUID = 1L;
	
	public ClassicPagingNavigator(String id, DataTable table) {
		super(id);
		

		Form form = new Form("pagingForm");
		
		List<String> data = new LinkedList<String>();
		data.add("5");
		data.add("10");
		data.add("50");
		data.add("100");
		data.add("200");
		data.add("500");
		
		form.add(new LocalDropDownChoice("pagingDropDownChoice", new PropertyModel(table, "rowsPerPage"), data));
		
		// Add additional page links
		form.add(newPagingNavigationButton("first", table, 0));
		form.add(newPagingNavigationIncrementButton("prev", table, -1));
		form.add(newPagingNavigationIncrementButton("next", table, 1));
		form.add(newPagingNavigationButton("last", table, -1));
		
		add(form);
	}

	
	/**
	 * Create a new increment link. May be subclassed to make use of specialized
	 * links, e.g. Ajaxian links.
	 * 
	 * @param id
	 *            the link id
	 * @param pageable
	 *            the pageable to control
	 * @param increment
	 *            the increment
	 * @return the increment link
	 */
	protected Button newPagingNavigationIncrementButton(String id, IPageable pageable, int increment)
	{
		return new PagingNavigationIncrementButton(id, pageable, increment);
	}

	/**
	 * Create a new pagenumber link. May be subclassed to make use of
	 * specialized links, e.g. Ajaxian links.
	 * 
	 * @param id
	 *            the link id
	 * @param pageable
	 *            the pageable to control
	 * @param pageNumber
	 *            the page to jump to
	 * @return the pagenumber link
	 */
	protected Button newPagingNavigationButton(String id, IPageable pageable, int pageNumber)
	{
		return new PagingNavigationButton(id, pageable, pageNumber);
	}

	/**
	 * Create a new PagingNavigation. May be subclassed to make us of
	 * specialized PagingNavigation.
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

	
	public class LocalDropDownChoice extends DropDownChoice {

		private static final long serialVersionUID = 1L;

		public LocalDropDownChoice(String id, IModel model, List data) {
			super(id, model, data, new PagingChoiceRenderer());
		}
		
		@Override
	    protected boolean wantOnSelectionChangedNotifications()
	    {
	        return true;
	    }
	}

	

}
