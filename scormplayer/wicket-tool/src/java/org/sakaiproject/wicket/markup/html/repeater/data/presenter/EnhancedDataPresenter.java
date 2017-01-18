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
package org.sakaiproject.wicket.markup.html.repeater.data.presenter;

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.wicket.markup.html.repeater.data.table.EnhancedNavigationToolbar;
import org.sakaiproject.wicket.markup.html.repeater.util.EnhancedDataProvider;

public class EnhancedDataPresenter extends ClassicDataPresenter {

	private static final long serialVersionUID = 1L;
	
	private EnhancedDataProvider dataProvider = null;
	
	public EnhancedDataPresenter(String id, List<IColumn> columns, EnhancedDataProvider dataProvider) {
		super(id, columns, dataProvider);
		this.dataProvider = dataProvider;
	}
	
	protected List getFilterList() {
		return new LinkedList();
	}	
	
	@Override
	protected AbstractToolbar newNavigationToolbar(DataTable datatable, IDataProvider dataProvider) {
		return new EnhancedNavigationToolbar(datatable, (EnhancedDataProvider)dataProvider, getSearchPropertyModel(dataProvider), 
				getFilterPropertyModel(dataProvider), getInstructionsPropertyModel(dataProvider));
	}
		
	protected PropertyModel getSearchPropertyModel(IDataProvider dataProvider) {
		return new PropertyModel(dataProvider, "searchField");
	}
	
	protected PropertyModel getFilterPropertyModel(IDataProvider dataProvider) {
		return new PropertyModel(dataProvider, "filterField");
	}
	
	protected PropertyModel getInstructionsPropertyModel(IDataProvider dataProvider) {
		return new PropertyModel(dataProvider, "instructions");
	}
	
	protected boolean includeSearch() {
		return true;
	}
	
	protected boolean includeFilter() {
		return true;
	}
	
}
