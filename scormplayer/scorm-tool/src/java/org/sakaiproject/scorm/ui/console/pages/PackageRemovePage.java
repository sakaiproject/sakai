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
package org.sakaiproject.scorm.ui.console.pages;

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.wicket.markup.html.form.CancelButton;

public class PackageRemovePage extends ConsoleBasePage {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean
	ScormClientFacade clientFacade;

	public PackageRemovePage(PageParameters params) {
		String title = params.getString("title");
		final String courseId = params.getString("courseId");
		final long id = params.getLong("id");
		
		ContentPackage contentPackage = new ContentPackage(title, courseId);
		
		List<ContentPackage> list = new LinkedList<ContentPackage>();
		list.add(contentPackage);
		
		List<IColumn> columns = new LinkedList<IColumn>();
		columns.add(new PropertyColumn(new Model("Content Package"), "title", "title"));
		
		DataTable removeTable = new DataTable("removeTable", columns.toArray(new IColumn[columns.size()]), 
				new ListDataProvider(list), 3);
		
		
		Form removeForm = new Form("removeForm") {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void onSubmit() {
				clientFacade.contentPackageInterface().removeContentPackage(id);
				setResponsePage(PackageListPage.class);
			}
			
		};
		
		removeForm.add(removeTable);
		removeForm.add(new CancelButton("cancel", PackageListPage.class));
		
		add(removeForm);
	}
	
}
