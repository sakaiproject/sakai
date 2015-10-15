package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.pages.PermissionsPage;
import org.sakaiproject.gradebookng.tool.pages.SettingsPage;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;

public class SettingsGradeReleasePanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	IModel<GradebookInformation> model;

	public SettingsGradeReleasePanel(String id, IModel<GradebookInformation> model) {
		super(id, model);
		this.model = model;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
				
		//build form
		Form<GradebookInformation> form = new Form<GradebookInformation>("form", this.model) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				
				GradebookInformation settings = this.getModelObject();
				System.out.println("settings: " + settings.getGradeType());
				
				businessService.updateGradebookSettings(settings);
				/*
				Assignment assignment = this.getModelObject();
				
				//TODO validation of the fields here
				
				if(businessService.updateAssignment(assignment)) {
					GradebookPage rval = new GradebookPage();
					rval.info(MessageFormat.format(getString("message.edititem.success"),assignment.getName()));
					setResponsePage(rval);
				} else {
					error(getString("message.edititem.error"));
				}
				*/
				
				getSession().info(getString("settingspage.update.success"));
				setResponsePage(new SettingsPage());
				
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
				
		//display released items to students
        final AjaxCheckBox displayReleased = new AjaxCheckBox("displayReleased", new PropertyModel<Boolean>(model, "displayReleasedGradeItemsToStudents")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				//nothing required
			}
        };
        displayReleased.setOutputMarkupId(true);
        form.add(displayReleased);
        
        //display course grade
        final AjaxCheckBox displayCourseGrade = new AjaxCheckBox("displayCourseGrade", new PropertyModel<Boolean>(model, "courseGradeDisplayed")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				//nothing required
			}
        };
        displayCourseGrade.setOutputMarkupId(true);
        form.add(displayCourseGrade);
		
		add(form);
	}
}
