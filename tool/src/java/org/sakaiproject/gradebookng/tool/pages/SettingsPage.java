package org.sakaiproject.gradebookng.tool.pages;

import java.util.List;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.sakaiproject.gradebookng.tool.panels.SettingsCategoryPanel;
import org.sakaiproject.gradebookng.tool.panels.SettingsGradeEntryPanel;
import org.sakaiproject.gradebookng.tool.panels.SettingsGradeReleasePanel;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;


/**
 * Settings page
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class SettingsPage extends BasePage {
	
	private static final long serialVersionUID = 1L;

	public SettingsPage() {
		disableLink(this.settingsPageLink);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//get settings
		GradebookInformation settings = this.businessService.getGradebookSettings();
		
		//form model
		CompoundPropertyModel<GradebookInformation> formModel = new CompoundPropertyModel<GradebookInformation>(settings);
		
		//form
		Form<GradebookInformation> form = new Form<GradebookInformation>("form", formModel) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				
				GradebookInformation settings = this.getModelObject();
				System.out.println("settings: " + settings);
				
				
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
		
		//cancel button
		Button cancel = new Button("cancel") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onSubmit() {
				setResponsePage(new GradebookPage());
			}
		};
		cancel.setDefaultFormProcessing(false);
        form.add(cancel);
		
		//panels
		form.add(new SettingsGradeEntryPanel("gradeEntryPanel", formModel));
		form.add(new SettingsGradeReleasePanel("gradeReleasePanel", formModel));
		form.add(new SettingsCategoryPanel("categoryPanel", formModel));
		
		add(form);
		
	}
	
	
}
