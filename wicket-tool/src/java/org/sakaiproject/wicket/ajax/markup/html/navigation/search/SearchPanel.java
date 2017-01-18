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
package org.sakaiproject.wicket.ajax.markup.html.navigation.search;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class SearchPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private PropertyModel propertyModel;
	private TextField textField;
	
	public SearchPanel(String id, PropertyModel propertyModel) {
		super(id);
		this.propertyModel = propertyModel;
		
		Form form = new SearchForm("searchForm");
		form.add(textField = new TextField("searchField", propertyModel));
		form.add(new ClearButton("clearButton"));
		add(form);
	}
	
	protected void onSubmit() {
		
	}
	
	public class SearchForm extends Form {
		
		private static final long serialVersionUID = 1L;

		public SearchForm(String id) {
			super(id);
		}
		
		protected void onSubmit() {
			SearchPanel.this.onSubmit();
		}
	}
	
	public class ClearButton extends Button {

		private static final long serialVersionUID = 1L;

		public ClearButton(String id) {
			super(id);
			setDefaultFormProcessing(false);
		}
		
		public void onSubmit() {
			propertyModel.setObject(new String(""));
			textField.clearInput();
		}
		
	}

	
	

}
