package org.sakaiproject.gradebookng.tool.pages;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.tool.panels.AddGradeItemPanelContent;
import org.sakaiproject.service.gradebook.shared.Assignment;


/**
 * Page for editing a gradebook item
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class EditGradebookItemPage extends BasePage {
	
	private static final long serialVersionUID = 1L;
	
	IModel<Long> model;

	public EditGradebookItemPage(IModel<Long> model) {
		this.model = model;
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		//unpack model
		Long assignmentId = this.model.getObject();
		
		//get assignment
		Assignment assignment = this.businessService.getAssignment(assignmentId);
		
		//form model
		Model formModel = new Model(assignment);
		
		//build form
		Form<Assignment> form = new Form<Assignment>("form", formModel) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				Assignment assignment = this.getModelObject();
				
				//TODO validation of the fields here
				
				if(businessService.updateAssignment(assignment)) {
					GradebookPage rval = new GradebookPage();
					rval.info(MessageFormat.format(getString("message.edititem.success"),assignment.getName()));
					setResponsePage(rval);
				} else {
					error(getString("message.edititem.error"));
				}
				
			}
		};
		
		Button cancel = new Button("cancel") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onSubmit() {
				setResponsePage(new GradebookPage());
			}
		};
		cancel.setDefaultFormProcessing(false);
		form.add(cancel);

		form.add(new AddGradeItemPanelContent("subComponents", formModel));

		add(form);
	}
}
