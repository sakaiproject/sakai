/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.gradebookng.tool.pages;

import java.math.BigDecimal;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.gradebookng.tool.panels.SettingsCategoryPanel;
import org.sakaiproject.gradebookng.tool.panels.SettingsGradeEntryPanel;
import org.sakaiproject.gradebookng.tool.panels.SettingsGradeReleasePanel;
import org.sakaiproject.gradebookng.tool.panels.SettingsGradingSchemaPanel;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.ConflictingCategoryNameException;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.service.gradebook.shared.exception.UnmappableCourseGradeOverrideException;

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

	private boolean hideGradeEntryFromNonAdmins;
	private static final String SAK_PROP_HIDE_GRADE_ENTRY_FROM_NON_ADMINS = "gradebook.settings.gradeEntry.hideFromNonAdmins";
	private static final boolean SAK_PROP_HIDE_GRADE_ENTRY_FROM_NON_ADMINS_DEFAULT = false;

	SettingsGradeEntryPanel gradeEntryPanel;
	SettingsGradeReleasePanel gradeReleasePanel;
	SettingsCategoryPanel categoryPanel;
	SettingsGradingSchemaPanel gradingSchemaPanel;

	public SettingsPage() {
		disableLink(this.settingsPageLink);
		setHideGradeEntryFromNonAdmins();
	}

	public SettingsPage(final boolean gradeEntryExpanded, final boolean gradeReleaseExpanded,
			final boolean categoryExpanded, final boolean gradingSchemaExpanded) {
		disableLink(this.settingsPageLink);
		this.gradeEntryExpanded = gradeEntryExpanded;
		this.gradeReleaseExpanded = gradeReleaseExpanded;
		this.categoryExpanded = categoryExpanded;
		this.gradingSchemaExpanded = gradingSchemaExpanded;
		setHideGradeEntryFromNonAdmins();
	}

	private void setHideGradeEntryFromNonAdmins() {
		hideGradeEntryFromNonAdmins = ServerConfigurationService.getBoolean(SAK_PROP_HIDE_GRADE_ENTRY_FROM_NON_ADMINS, SAK_PROP_HIDE_GRADE_ENTRY_FROM_NON_ADMINS_DEFAULT);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// get settings data
		final GradebookInformation settings = this.businessService.getGradebookSettings();

		// setup page model
		final GbSettings gbSettings = new GbSettings(settings);
		final CompoundPropertyModel<GbSettings> formModel = new CompoundPropertyModel<GbSettings>(gbSettings);

		this.gradeEntryPanel = new SettingsGradeEntryPanel("gradeEntryPanel", formModel, this.gradeEntryExpanded);
		this.gradeReleasePanel = new SettingsGradeReleasePanel("gradeReleasePanel", formModel, this.gradeReleaseExpanded);
		this.categoryPanel = new SettingsCategoryPanel("categoryPanel", formModel, this.categoryExpanded);
		this.gradingSchemaPanel = new SettingsGradingSchemaPanel("gradingSchemaPanel", formModel, this.gradingSchemaExpanded);

		// Hide the panel if sakai.property is true and user is not admin
		if (hideGradeEntryFromNonAdmins && !businessService.isSuperUser()) {
			gradeEntryPanel.setVisible(false);
		}

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

						// ensure we don't have drop highest and keep highest at the same time
						if ((cat.getDropHighest().intValue() > 0 && cat.getKeepHighest().intValue() > 0)
								|| (cat.getDropLowest().intValue() > 0 && cat.getKeepHighest().intValue() > 0)) {
							error(getString("settingspage.update.failure.categorydropkeepenabled"));
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

				Page responsePage = new SettingsPage(SettingsPage.this.gradeEntryPanel.isExpanded(),
						SettingsPage.this.gradeReleasePanel.isExpanded(), SettingsPage.this.categoryPanel.isExpanded(),
						SettingsPage.this.gradingSchemaPanel.isExpanded());

				// update settings
				try {
					SettingsPage.this.businessService.updateGradebookSettings(model.getGradebookInformation());
					getSession().success(getString("settingspage.update.success"));
				} catch (final ConflictingCategoryNameException e) {
					getSession().error(getString("settingspage.update.failure.categorynameconflict"));
					responsePage = getPage();
				} catch (final UnmappableCourseGradeOverrideException e) {
					getSession().error(getString("settingspage.update.failure.gradingschemamapping"));
					responsePage = getPage();
				} catch (final Exception e) {
					// catch all to prevent stacktraces
					getSession().error(e.getMessage());
					responsePage = getPage();
				}

				setResponsePage(responsePage);
			}
		};

		// cancel button
		final Button cancel = new Button("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				setResponsePage(GradebookPage.class);
			}
		};
		cancel.setDefaultFormProcessing(false);
		form.add(cancel);

		// panels
		form.add(this.gradeEntryPanel);
		form.add(this.gradeReleasePanel);
		form.add(this.categoryPanel);
		form.add(this.gradingSchemaPanel);

		add(form);

		// expand/collapse panel actions
		add(new GbAjaxLink("expandAll") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				target.appendJavaScript("$('#settingsAccordion .panel-collapse').collapse('show');");
			}
		});
		add(new GbAjaxLink("collapseAll") {
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

		// Drag and Drop (requires jQueryUI)
		response.render(
				JavaScriptHeaderItem.forUrl(String.format("/library/webjars/jquery-ui/1.12.1/jquery-ui.min.js?version=%s", version)));

		// chart requires ChartJS
		response.render(
				JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/webjars/chartjs/2.7.0/Chart.min.js?version=%s", version)));

		response.render(CssHeaderItem.forUrl(String.format("/gradebookng-tool/styles/gradebook-settings.css?version=%s", version)));
		response.render(JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-settings.js?version=%s", version)));

	}

	/**
	 * Getters for these panels as we need to interact with them from the child panels
	 */
	public SettingsGradeReleasePanel getSettingsGradeReleasePanel() {
		return this.gradeReleasePanel;
	}

	public SettingsCategoryPanel getSettingsCategoryPanel() {
		return this.categoryPanel;
	}

}
