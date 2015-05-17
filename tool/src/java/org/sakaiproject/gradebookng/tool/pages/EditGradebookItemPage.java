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
		CompoundPropertyModel<Assignment> formModel = new CompoundPropertyModel<Assignment>(assignment);
		
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
		
		/**
		 * Note the wicket:id bindings here are to the properties on the Assignment. They must match (HTML too).
		 */
		form.add(new TextField<String>("name").setRequired(true));
		form.add(new TextField<Double>("points").setRequired(true));
		form.add(new CheckBox("extraCredit"));
		form.add(new DateTextField("dueDate", "dd/MM/yyyy")); //TODO make this date string come from ResourceLoader?
		//there is no way (currently) to edit the category of an assignment. need sto be implemented
		//form.add(new DropDownChoice("category", new PropertyModel(gbAssignmentModel, "categoryName"), Arrays.asList(new String[] { "A", "B", "C" })));
		form.add(new CheckBox("released"));
		form.add(new CheckBox("counted"));

		add(form);
	}
}
