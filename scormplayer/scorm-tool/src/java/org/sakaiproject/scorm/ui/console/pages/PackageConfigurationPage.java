package org.sakaiproject.scorm.ui.console.pages;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormPermissionService;
import org.sakaiproject.wicket.markup.html.form.CancelButton;
import org.sakaiproject.wicket.model.DecoratedPropertyModel;
import org.sakaiproject.wicket.model.SimpleDateFormatPropertyModel;

public class PackageConfigurationPage extends ConsoleBasePage {

	private static final long serialVersionUID = 1L;

	@SpringBean
	ScormContentService contentService;
	@SpringBean
	ScormPermissionService permissionService;
	
	//private Time releaseOnTime, dueOnTime, acceptUntilTime;
	
	private String unlimitedMessage;
	
	
	public PackageConfigurationPage(PageParameters params) {
		long contentPackageId = params.getLong("id");
		
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
			
			return permissionService.getDisplayName(userId);
		}
		
	}
	

	/*public Time getReleaseOnTime() {
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
	}*/
	
	
	
	
}
