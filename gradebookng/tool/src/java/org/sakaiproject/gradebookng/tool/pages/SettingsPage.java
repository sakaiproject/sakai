package org.sakaiproject.gradebookng.tool.pages;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.gradebookng.tool.panels.SettingsCategoryPanel;
import org.sakaiproject.gradebookng.tool.panels.SettingsGradeEntryPanel;
import org.sakaiproject.gradebookng.tool.panels.SettingsGradeReleasePanel;
import org.sakaiproject.gradebookng.tool.panels.SettingsGradingSchemaPanel;
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
		
		//get settings data
		GradebookInformation settings = this.businessService.getGradebookSettings();
		
		//setup page model
		GbSettings gbSettings = new GbSettings(settings);
        CompoundPropertyModel<GbSettings> formModel = new CompoundPropertyModel<GbSettings>(gbSettings);
			
		//form
		Form<GbSettings> form = new Form<GbSettings>("form", formModel) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onValidate() {
				super.onValidate();
				
				GbSettings model = (GbSettings) this.getModelObject();
				
				List<CategoryDefinition> categories = model.getGradebookInformation().getCategories();
				
				//validate the categories
				if(model.getGradebookInformation().getCategoryType() == 3) {
					double totalWeight = 0;
					for(CategoryDefinition cat: categories) {
						if(cat.getWeight() == null) {
							error(getString("settingspage.update.failure.categorymissingweight"));
						} else {
							totalWeight += cat.getWeight();
						}
					}
					if(Math.rint(totalWeight) != 1) {
						error(getString("settingspage.update.failure.categoryweighttotals"));
					}
				}
				
			}
			
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
		
		//expand/collapse panel actions
		add(new AjaxLink("expandAll") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavaScript("$('#settingsAccordion .panel-collapse').collapse('show');");
			}
		});
		add(new AjaxLink("collapseAll") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavaScript("$('#settingsAccordion .panel-collapse').collapse('hide');");
			}
		});
	}
	
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		String version = ServerConfigurationService.getString("portal.cdn.version", "");

		response.render(CssHeaderItem.forUrl(String.format("/gradebookng-tool/styles/gradebook-settings.css?version=%s", version)));
	}
}
