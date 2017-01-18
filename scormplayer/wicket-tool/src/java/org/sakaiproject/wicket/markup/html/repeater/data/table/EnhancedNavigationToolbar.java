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
package org.sakaiproject.wicket.markup.html.repeater.data.table;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.wicket.ajax.markup.html.navigation.filter.FilterPanel;
import org.sakaiproject.wicket.ajax.markup.html.navigation.search.SearchPanel;
import org.sakaiproject.wicket.markup.html.repeater.util.EnhancedDataProvider;

public class EnhancedNavigationToolbar extends ClassicNavigationToolbar {

	private static final long serialVersionUID = 1L;

	private EnhancedDataProvider dataProvider;
	private FilterPanel filterPanel;
	private SearchPanel searchPanel;
	private Label instructionLabel;
	private Label titleLabel;

	public EnhancedNavigationToolbar(DataTable table, EnhancedDataProvider dataProvider, 
			PropertyModel searchModel, PropertyModel filterModel, IModel instructionModel) {
		super(table);
		this.dataProvider = dataProvider;
	
		span.add(filterPanel = new LocalFilterPanel("filterPanel", dataProvider, filterModel));
		
		span.add(searchPanel = new LocalSearchPanel("searchPanel", searchModel));
		
		span.add(instructionLabel = new Label("instructions", instructionModel));
	
		span.add(titleLabel = new Label("tableTitle", new PropertyModel(dataProvider, "tableTitle")));
	
		titleLabel.setVisible(dataProvider.getTableTitle() != null);
	}
	
	@Override
	public boolean isVisible()
	{
		// Unlike the default wicket toolbars, we want our enhanced toolbars to always be visible.
		return true;
	}
	
	public class LocalFilterPanel extends FilterPanel {

		private static final long serialVersionUID = 1L;

		public LocalFilterPanel(String id, EnhancedDataProvider dataProvider, PropertyModel filterModel) {
			super(id, dataProvider, filterModel);
		}
		
		public boolean isVisible() {
			return dataProvider.isFilterVisible();
		}
		
		protected Component newLimiter(String id, final EnhancedDataProvider dataProvider) {
			AjaxCheckBox limiter = new AjaxCheckBox(id, new PropertyModel(dataProvider, "limited")) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					// Update table to reflect changes
					target.addComponent(table);
				}
				
				public boolean isVisible() {
					return dataProvider.isFilterLimiterVisible();
				}
			};
			return limiter;
		}
		
		protected Component newConfigurer(String id, final EnhancedDataProvider dataProvider) {
			AjaxFallbackLink configurer = new AjaxFallbackLink(id) {

				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					dataProvider.onClickConfigurer(target);
				}
				
				public boolean isVisible() {
					return dataProvider.isFilterConfigurerVisible();
				}
			};
			configurer.setVisible(false);
			return configurer;
		}
	}
	
	public class LocalSearchPanel extends SearchPanel {

		private static final long serialVersionUID = 1L;

		public LocalSearchPanel(String id, PropertyModel searchModel) {
			super(id, searchModel);
		}

		public boolean isVisible() {
			return dataProvider.isSearchVisible();
		}
	}
	
	public class LocalLabel extends Label {

		private static final long serialVersionUID = 1L;

		public LocalLabel(String id, IModel model) {
			super(id, model);
		}
		
		public boolean isVisible() {
			return dataProvider.isInstructionVisible();
		}
		
	}
	
	
	
	
}
