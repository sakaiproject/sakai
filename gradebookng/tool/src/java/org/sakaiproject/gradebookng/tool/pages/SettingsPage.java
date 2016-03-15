package org.sakaiproject.gradebookng.tool.pages;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.gradebookng.business.GbCategoryType;
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

	private boolean gradeEntryExpanded = false;
	private boolean gradeReleaseExpanded = false;
	private boolean categoryExpanded = false;
	private boolean gradingSchemaExpanded = false;

	public SettingsPage() {
		disableLink(this.settingsPageLink);
	}

	public SettingsPage(final boolean gradeEntryExpanded, final boolean gradeReleaseExpanded,
											final boolean categoryExpanded, final boolean gradingSchemaExpanded) {
		disableLink(this.settingsPageLink);
		this.gradeEntryExpanded = gradeEntryExpanded;
		this.gradeReleaseExpanded = gradeReleaseExpanded;
		this.categoryExpanded = categoryExpanded;
		this.gradingSchemaExpanded = gradingSchemaExpanded;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// get settings data
		final GradebookInformation settings = this.businessService.getGradebookSettings();

		// setup page model
		final GbSettings gbSettings = new GbSettings(settings);
		final CompoundPropertyModel<GbSettings> formModel = new CompoundPropertyModel<GbSettings>(gbSettings);

		final SettingsGradeEntryPanel gradeEntryPanel = new SettingsGradeEntryPanel("gradeEntryPanel", formModel, gradeEntryExpanded);
		final SettingsGradeReleasePanel gradeReleasePanel = new SettingsGradeReleasePanel("gradeReleasePanel", formModel, gradeReleaseExpanded);
		final SettingsCategoryPanel categoryPanel = new SettingsCategoryPanel("categoryPanel", formModel, categoryExpanded);
		final SettingsGradingSchemaPanel gradingSchemaPanel = new SettingsGradingSchemaPanel("gradingSchemaPanel", formModel, gradingSchemaExpanded);

		// form
		final Form<GbSettings> form = new Form<GbSettings>("form", formModel) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onValidate() {
				super.onValidate();

				final GbSettings model = getModelObject();

				final List<CategoryDefinition> categories = model.getGradebookInformation().getCategories();

				// validate the categories
				if (model.getGradebookInformation().getCategoryType() == GbCategoryType.WEIGHTED_CATEGORY.getValue()) {

					BigDecimal totalWeight = BigDecimal.ZERO;
					for (final CategoryDefinition cat : categories) {

						if (cat.getWeight() == null) {
							error(getString("settingspage.update.failure.categorymissingweight"));
						} else {
							// extra credit items do not participate in the weightings, so exclude from the tally
							if (!cat.isExtraCredit()) {
								totalWeight = totalWeight.add(BigDecimal.valueOf(cat.getWeight()));
							}
						}
					}

					if (totalWeight.compareTo(BigDecimal.ONE) != 0) {
						error(getString("settingspage.update.failure.categoryweighttotals"));
					}
				}

				// if categories and weighting selected AND if course grade display points was selected,
				// give error message
				if (model.getGradebookInformation().getCategoryType() == GbCategoryType.WEIGHTED_CATEGORY.getValue()
						&& model.getGradebookInformation().isCourseGradeDisplayed()
						&& model.getGradebookInformation().isCoursePointsDisplayed()) {
					error(getString("settingspage.displaycoursegrade.incompatible"));
				}

				// validate the course grade display settings
				if (model.getGradebookInformation().isCourseGradeDisplayed()) {
					int displayOptions = 0;

					if (model.getGradebookInformation().isCourseLetterGradeDisplayed()) {
						displayOptions++;
					}

					if (model.getGradebookInformation().isCourseAverageDisplayed()) {
						displayOptions++;
					}

					if (model.getGradebookInformation().isCoursePointsDisplayed()) {
						displayOptions++;
					}

					if (displayOptions == 0) {
						error(getString("settingspage.displaycoursegrade.notenough"));
					}
				}

			}

			@Override
			public void onSubmit() {

				final GbSettings model = getModelObject();

				// update settings
				SettingsPage.this.businessService.updateGradebookSettings(model.getGradebookInformation());

				getSession().info(getString("settingspage.update.success"));
				setResponsePage(new SettingsPage(gradeEntryPanel.isExpanded(), gradeReleasePanel.isExpanded(), categoryPanel.isExpanded(), gradingSchemaPanel.isExpanded()));
			}
		};

		// cancel button
		final Button cancel = new Button("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				setResponsePage(new GradebookPage());
			}
		};
		cancel.setDefaultFormProcessing(false);
		form.add(cancel);

		// panels
		form.add(gradeEntryPanel);
		form.add(gradeReleasePanel);
		form.add(categoryPanel);
		form.add(gradingSchemaPanel);

		add(form);

		// expand/collapse panel actions
		add(new AjaxLink<String>("expandAll") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				target.appendJavaScript("$('#settingsAccordion .panel-collapse').collapse('show');");
			}
		});
		add(new AjaxLink<String>("collapseAll") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				target.appendJavaScript("$('#settingsAccordion .panel-collapse').collapse('hide');");
			}
		});
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		final String version = ServerConfigurationService.getString("portal.cdn.version", "");

		response.render(CssHeaderItem.forUrl(String.format("/gradebookng-tool/styles/gradebook-settings.css?version=%s", version)));
		response.render(
				JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-settings.js?version=%s", version)));

	}
}
