package org.sakaiproject.scorm.ui.console.pages;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.wicket.markup.html.form.CancelButton;

public class PackageConfigurationPage extends ConsoleBasePage {

	private static final long serialVersionUID = 1L;

	@SpringBean
	ScormContentService contentService;
	
	private Time releaseOnTime, dueOnTime, acceptUntilTime;
	
	public PackageConfigurationPage(PageParameters params) {
		long contentPackageId = params.getLong("id");
		
		final ContentPackage contentPackage = contentService.getContentPackage(contentPackageId);
		
        Form form = new Form("configurationForm") {
			private static final long serialVersionUID = 1L;

			protected void onSubmit()
        	{
        		contentService.updateContentPackage(contentPackage);
        	}
        };
                
        TextField nameField = new TextField("packageName", new PropertyModel(contentPackage, "title"));
        nameField.setRequired(true);
        form.add(nameField);
        form.add(new RequiredDateTimeField("releaseOnDTF", new PropertyModel(contentPackage, "releaseOn")));
        form.add(new DateTimeField("dueOnDTF", new PropertyModel(contentPackage, "dueOn")));
        form.add(new DateTimeField("acceptUntilDTF", new PropertyModel(contentPackage, "acceptUntil")));
        form.add(new TextField("numberOfTries", new PropertyModel(contentPackage, "numberOfTries")));
        form.add(new Label("createdBy", new PropertyModel(contentPackage, "createdBy")));
        form.add(new Label("createdOn", new PropertyModel(contentPackage, "createdOn")));
        form.add(new Label("modifiedBy", new PropertyModel(contentPackage, "modifiedBy")));
        form.add(new Label("modifiedOn", new PropertyModel(contentPackage, "modifiedOn")));

        form.add(new CancelButton("cancel", PackageListPage.class));
        add(form);
	}
	
	
	public class RequiredDateTimeField extends DateTimeField {

		private static final long serialVersionUID = 1L;
		
		public RequiredDateTimeField(String id, IModel model) {
			super(id, model);
			setRequired(true);
		}	
	}


	public Time getReleaseOnTime() {
		return releaseOnTime;
	}


	public void setReleaseOnTime(Time releaseOnTime) {
		this.releaseOnTime = releaseOnTime;
	}


	public Time getDueOnTime() {
		return dueOnTime;
	}


	public void setDueOnTime(Time dueOnTime) {
		this.dueOnTime = dueOnTime;
	}


	public Time getAcceptUntilTime() {
		return acceptUntilTime;
	}


	public void setAcceptUntilTime(Time acceptUntilTime) {
		this.acceptUntilTime = acceptUntilTime;
	}
	
	
	
	
}
