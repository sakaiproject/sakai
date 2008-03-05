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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.exceptions.ResourceNotDeletedException;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.wicket.markup.html.form.CancelButton;

public class PackageRemovePage extends ConsoleBasePage {
	
	private static final long serialVersionUID = 1L;
	
	private static Log log = LogFactory.getLog(PackageRemovePage.class);
	
	@SpringBean
	ScormContentService contentService;

	private Label alertLabel;
	private Button submitButton;
	
	public PackageRemovePage(final PageParameters params) {
		String title = params.getString("title");
		final long contentPackageId = params.getLong("contentPackageId");
		
		ContentPackage contentPackage = new ContentPackage(title, contentPackageId);
		
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
				try {
					contentService.removeContentPackage(contentPackageId);
					setResponsePage(PackageListPage.class);
				} catch (ResourceNotDeletedException rnde) {
					log.warn("Failed to delete all underlying resources ", rnde);
					alertLabel.setModel(new ResourceModel("exception.remove"));
					submitButton.setVisible(false);
					setResponsePage(PackageRemovePage.class, params);
				}
			}
			
		};
		removeForm.add(alertLabel = new Label("alert", new ResourceModel("verify.remove")));
		removeForm.add(removeTable);
		removeForm.add(submitButton = new Button("submit"));
		removeForm.add(new CancelButton("cancel", PackageListPage.class));
		
		add(removeForm);
	}
	
}
