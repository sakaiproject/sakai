package org.sakaiproject.gradebookng.tool.pages;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.gradebookng.tool.panels.SettingsCategoryPanel;
import org.sakaiproject.gradebookng.tool.panels.SettingsGradeEntryPanel;
import org.sakaiproject.gradebookng.tool.panels.SettingsGradeReleasePanel;
import org.sakaiproject.gradebookng.tool.panels.SettingsGradingSchemaPanel;
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
		
		//get settings data
		GradebookInformation settings = this.businessService.getGradebookSettings();
		
		//setup page model
		GbSettings gbSettings = new GbSettings(settings);
        CompoundPropertyModel<GbSettings> formModel = new CompoundPropertyModel<GbSettings>(gbSettings);
			
		//form
		Form<GbSettings> form = new Form<GbSettings>("form", formModel) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				
				GbSettings model = (GbSettings) this.getModelObject();
								
				//update settings
				businessService.updateGradebookSettings(model.getGradebookInformation());
				
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
		form.add(new SettingsGradingSchemaPanel("gradingSchemaPanel", formModel));
		
		add(form);
		
	}
	
}
