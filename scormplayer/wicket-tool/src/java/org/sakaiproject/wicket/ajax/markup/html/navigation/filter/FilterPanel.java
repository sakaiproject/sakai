/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.wicket.ajax.markup.html.navigation.filter;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.wicket.markup.html.repeater.util.EnhancedDataProvider;

public class FilterPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	public FilterPanel(String id, EnhancedDataProvider dataProvider, PropertyModel filterModel) {
		super(id);
		
		Form form = new Form("filterForm");
		form.add(newDropDownChoice("dropdown", dataProvider, filterModel));
		form.add(newConfigurer("configurer", dataProvider));
		//form.add(new LimiterPanel("limiterPanel", newLimiter("limiter", dataProvider)));
		form.add(newLimiter("limiter", dataProvider));
		
		add(form);
	}

	protected DropDownChoice newDropDownChoice(String id, EnhancedDataProvider dataProvider, PropertyModel filterModel) {
		return new LocalDropDownChoice(id, filterModel, dataProvider.getFilterList(), dataProvider.getFilterChoiceRenderer());
	}
	
	protected Component newLimiter(String id, EnhancedDataProvider dataProvider) {
		Label limiter = new Label(id, new Model("invisible"));
		limiter.setVisible(false);
		return limiter;
	}
	
	protected Component newConfigurer(String id, EnhancedDataProvider dataProvider) {
		Label configurer = new Label(id, new Model("invisible"));
		configurer.setVisible(false);
		return configurer;
	}
	
	
	
	public class LocalDropDownChoice extends DropDownChoice {

		private static final long serialVersionUID = 1L;

		public LocalDropDownChoice(String id, IModel model, List data, IChoiceRenderer renderer) {
			super(id, model, data, renderer);
		}
		
		@Override
		public boolean isNullValid()
		{
			return true;
		}
		
		@Override
	    protected boolean wantOnSelectionChangedNotifications()
	    {
	        return true;
	    }
		
	}
	
}
