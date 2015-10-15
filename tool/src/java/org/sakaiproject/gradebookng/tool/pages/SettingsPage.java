package org.sakaiproject.gradebookng.tool.pages;

import org.apache.wicket.model.CompoundPropertyModel;
import org.sakaiproject.gradebookng.tool.panels.SettingsGradeEntryPanel;
import org.sakaiproject.gradebookng.tool.panels.SettingsGradeReleasePanel;
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
		
		//get settings
		GradebookInformation settings = this.businessService.getGradebookSettings();
		
		//form model
		CompoundPropertyModel<GradebookInformation> formModel = new CompoundPropertyModel<GradebookInformation>(settings);
		
		//panels
		add(new SettingsGradeEntryPanel("gradeEntryPanel", formModel));
		add(new SettingsGradeReleasePanel("gradeReleasePanel", formModel));
		
	}
	
	
}
