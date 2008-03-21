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
import org.apache.wicket.ResourceReference;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.wicket.markup.html.form.CancelButton;
import org.sakaiproject.wicket.model.DecoratedPropertyModel;
import org.sakaiproject.wicket.model.SimpleDateFormatPropertyModel;

public class PackageConfigurationPage extends ConsoleBasePage {

	private static final long serialVersionUID = 1L;

	private static ResourceReference PAGE_ICON = new ResourceReference(PackageConfigurationPage.class, "res/table_edit.png");
	
	@SpringBean
	LearningManagementSystem lms;
	@SpringBean
	ScormContentService contentService;
	
	
	private String unlimitedMessage;
	
	public PackageConfigurationPage(PageParameters params) {
		long contentPackageId = params.getLong("contentPackageId");
		
		final ContentPackage contentPackage = contentService.getContentPackage(contentPackageId);
		
        Form form = new Form("configurationForm") {
			private static final long serialVersionUID = 1L;

			protected void onSubmit()
        	{
        		contentService.updateContentPackage(contentPackage);
        		setResponsePage(PackageListPage.class);
        	}
        };
           
        List<Integer> tryList = new LinkedList<Integer>();
        
        tryList.add(Integer.valueOf(-1));
        for (int i=1;i<=10;i++) {
        	tryList.add(Integer.valueOf(i));
        }

        
        this.unlimitedMessage = getLocalizer().getString("unlimited", this);
        
        TextField nameField = new TextField("packageName", new PropertyModel(contentPackage, "title"));
        nameField.setRequired(true);
        form.add(nameField);
        DateTimeField releaseOnDTF = new DateTimeField("releaseOnDTF", new PropertyModel(contentPackage, "releaseOn"));
        releaseOnDTF.setRequired(true);
        form.add(releaseOnDTF);
        form.add(new DateTimeField("dueOnDTF", new PropertyModel(contentPackage, "dueOn")));
        form.add(new DateTimeField("acceptUntilDTF", new PropertyModel(contentPackage, "acceptUntil")));
        form.add(new DropDownChoice("numberOfTries", new PropertyModel(contentPackage, "numberOfTries"), tryList, new TryChoiceRenderer()));
        form.add(new Label("createdBy", new DisplayNamePropertyModel(contentPackage, "createdBy")));
        form.add(new Label("createdOn", new SimpleDateFormatPropertyModel(contentPackage, "createdOn")));
        form.add(new Label("modifiedBy", new DisplayNamePropertyModel(contentPackage, "modifiedBy")));
        form.add(new Label("modifiedOn", new SimpleDateFormatPropertyModel(contentPackage, "modifiedOn")));

        form.add(new CancelButton("cancel", PackageListPage.class));
        add(form);
	}
	
	
	public class TryChoiceRenderer extends ChoiceRenderer {
		private static final long serialVersionUID = 1L;
		
		public TryChoiceRenderer() {
			super();
		}
		
		public Object getDisplayValue(Object object) {
			Integer n = (Integer)object;
			
			if (n.intValue() == -1)
				return unlimitedMessage;
				
			return object;
		}
		
	}
		
	public class DisplayNamePropertyModel extends DecoratedPropertyModel {

		private static final long serialVersionUID = 1L;
		
		public DisplayNamePropertyModel(Object modelObject, String expression) {
			super(modelObject, expression);
		}
		
		public Object convertObject(Object object) {
			String userId = String.valueOf(object);
			
			return lms.getLearnerName(userId);
		}
		
	}
	
	
	protected ResourceReference getPageIconReference() {
		return PAGE_ICON;
	}
	
	
}
