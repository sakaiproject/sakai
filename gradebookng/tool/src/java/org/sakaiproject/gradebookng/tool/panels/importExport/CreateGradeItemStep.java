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
package org.sakaiproject.gradebookng.tool.panels.importExport;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.util.ImportGradesHelper;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.model.UiMode;
import org.sakaiproject.gradebookng.tool.pages.ImportExportPage;
import org.sakaiproject.gradebookng.tool.panels.AddOrEditGradeItemPanelContent;
import org.sakaiproject.gradebookng.tool.panels.BasePanel;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.util.api.FormattedText;

import lombok.extern.slf4j.Slf4j;

/**
 * Importer has detected that items need to be created so extract the data and wrap the 'AddOrEditGradeItemPanelContent' panel
 */
@Slf4j
public class CreateGradeItemStep extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final String panelId;
	private final IModel<ImportWizardModel> model;

	PreviewImportedGradesPanel previewGradesPanel;

	public CreateGradeItemStep(final String id, final IModel<ImportWizardModel> importWizardModel) {
		super(id);
		this.panelId = id;
		this.model = importWizardModel;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final ImportWizardModel importWizardModel = this.model.getObject();

		final int step = importWizardModel.getStep();

		// original data
		final ProcessedGradeItem processedGradeItem = importWizardModel.getItemsToCreate().get(step - 1);

		// if using spreadsheet data, we'll create a blank assignment and fill the fields accordingly; otherwise, the assignment is already
		// in the wizard (Ie. back button)
		final Assignment assignmentFromModel = importWizardModel.getAssignmentsToCreate().get(processedGradeItem);
		final Assignment assignment = assignmentFromModel == null ? new Assignment() : assignmentFromModel;
		if (assignmentFromModel == null) {
			assignment.setName(StringUtils.trim(processedGradeItem.getItemTitle()));
			String itemPointValue = processedGradeItem.getItemPointValue();
			if (StringUtils.isNotBlank(itemPointValue)) {
				final String decimalSeparator = ComponentManager.get(FormattedText.class).getDecimalSeparator();
				if (",".equals(decimalSeparator)) {
					itemPointValue = itemPointValue.replace(decimalSeparator, ".");
				}
				assignment.setPoints(Double.parseDouble(itemPointValue));
			}
		}

		final Model<Assignment> assignmentModel = new Model<>(assignment);
		final Form<Assignment> form = new Form("form", assignmentModel);
		add(form);

		final AjaxButton nextButton = new AjaxButton("nextbutton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(final AjaxRequestTarget target) {
				saveItemAndProceed(true, target, form);
			}

			@Override
			protected void onError(final AjaxRequestTarget target) {
				final ImportExportPage page = (ImportExportPage) getPage();
				page.updateFeedback(target);
			}
		};
		form.add(nextButton);

		final AjaxButton backButton = new AjaxButton("backbutton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target) {
				saveItemAndProceed(false, target, form); // OWL
			}

			@Override
			protected void onError(final AjaxRequestTarget target) {
				final ImportExportPage page = (ImportExportPage) getPage();
				page.updateFeedback(target);
			}
		};
		form.add(backButton);

		final AjaxButton cancelButton = new AjaxButton("cancelbutton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target) {
				// clear any previous errors
				final ImportExportPage page = (ImportExportPage) getPage();
				page.clearFeedback();
				page.updateFeedback(target);
				setResponsePage(ImportExportPage.class);
			}
		};
		cancelButton.setDefaultFormProcessing(false);
		form.add(cancelButton);

		// wrap the form create panel
		form.add(new Label("createItemHeader",
				new StringResourceModel("importExport.createItem.heading").setParameters(step, importWizardModel.getTotalSteps())));
		AddOrEditGradeItemPanelContent aegipc = new AddOrEditGradeItemPanelContent("subComponents", assignmentModel, UiMode.ADD);
		aegipc.setCurrentGradebookAndSite(currentGradebookUid, currentSiteId);
		form.add(aegipc);
		this.previewGradesPanel = new PreviewImportedGradesPanel("previewGradesPanel", this.model);
		form.add(this.previewGradesPanel);
	}

	private void saveItemAndProceed(boolean forward, final AjaxRequestTarget target, final Form<?> form)
	{
		final ImportWizardModel importWizardModel = model.getObject();
		final int step = importWizardModel.getStep();
		// original data
		final ProcessedGradeItem processedGradeItem = importWizardModel.getItemsToCreate().get(step - 1);

		final Assignment newAssignment = (Assignment) form.getDefaultModel().getObject();
		final ImportExportPage page = (ImportExportPage) getPage();
		log.debug("GradebookAssignment: {}", newAssignment);

		// validate name is unique, first among existing gradebook items, second against new items to be created
		boolean validated = true;
		final List<Assignment> existingAssignments = businessService.getGradebookAssignments(currentGradebookUid, currentSiteId, SortType.SORT_BY_SORTING);
		if (!assignmentNameIsUnique(existingAssignments, newAssignment.getName()) || !assignmentNameIsUnique(newAssignment)) {
			validated = false;
			error(getString("error.addgradeitem.title"));
			page.updateFeedback(target);
		}

		if (validated) {

			// sync up the assignment data so we can present it for confirmation
			processedGradeItem.setAssignmentTitle(newAssignment.getName()); // need to retain the original title for matching later
			processedGradeItem.setItemPointValue(String.valueOf(newAssignment.getPoints())); // doesn't seem like anything actually uses this, but will leave it
			processedGradeItem.setRubricParameters(getRubricParameters(""));

			// add to model
			importWizardModel.getAssignmentsToCreate().put(processedGradeItem, newAssignment);

			Component newPanel;
			if (forward)
			{
				// Figure out if there are more steps
				// If so, go to the next step (ie do it all over again)
				if (step < importWizardModel.getTotalSteps()) {
					importWizardModel.setStep(step + 1);
					newPanel = new CreateGradeItemStep(CreateGradeItemStep.this.panelId, Model.of(importWizardModel));
					((CreateGradeItemStep)newPanel).setCurrentGradebookAndSite(currentGradebookUid, currentSiteId);
				} else {
					// If not, continue on in the wizard
					newPanel = new GradeImportConfirmationStep(CreateGradeItemStep.this.panelId, Model.of(importWizardModel));
					((GradeImportConfirmationStep)newPanel).setCurrentGradebookAndSite(currentGradebookUid, currentSiteId);
				}
			}
			else // back
			{
				if (step > 1) {
					importWizardModel.setStep(step - 1);
					newPanel = new CreateGradeItemStep(CreateGradeItemStep.this.panelId, Model.of(importWizardModel));
					((CreateGradeItemStep)newPanel).setCurrentGradebookAndSite(currentGradebookUid, currentSiteId);
				} else {
					// Reload everything. Rationale: final step can have partial success and partial failure. If content was imported from
					// the spreadsheet, the item selection page should reflect this when we return to it
					ImportGradesHelper.setupImportWizardModelForSelectionStep(page, CreateGradeItemStep.this, importWizardModel,
							CreateGradeItemStep.this.businessService, target, currentGradebookUid, currentSiteId);
					newPanel = new GradeItemImportSelectionStep(CreateGradeItemStep.this.panelId, Model.of(importWizardModel));
					((GradeItemImportSelectionStep)newPanel).setCurrentGradebookAndSite(currentGradebookUid, currentSiteId);
				}
			}

			// clear any previous errors
			page.clearFeedback();
			page.updateFeedback(target);

			// AJAX the new panel into place
			newPanel.setOutputMarkupId(true);
			final WebMarkupContainer container = page.container;
			container.addOrReplace(newPanel);
			target.add(newPanel);
		}
	}

	/**
	 * Checks if an assignment is unique given a list of existing assignments
	 *
	 * @param assignments
	 * @param name
	 * @return boolean
	 */
	private boolean assignmentNameIsUnique(final List<Assignment> assignments, final String name) {
		return !(assignments
				.stream()
				.filter(a -> StringUtils.equals(a.getName(), name))
				.findFirst()
				.isPresent());
	}

	/**
	 * Checks if a new assignment's name is unique amongst the list of assignments to be created.
	 * 
	 * @param newAssignment
	 * @return boolean
	 */
	private boolean assignmentNameIsUnique(final Assignment newAssignment) {

		final List<ProcessedGradeItem> itemsToCreate = model.getObject().getItemsToCreate();
		final Map<ProcessedGradeItem, Assignment> assignmentsToCreate = model.getObject().getAssignmentsToCreate();

		// assignment equality is based on id, which will be null until the assignments are persisted, so we can't use it
		// instead, we determine if the assignment object for the current item is already in the map, and set our expected
		// number of name matches accordingly
		final ProcessedGradeItem thisItem = itemsToCreate.get(model.getObject().getStep() - 1);
		int expectedNameMatchesIfUnique = assignmentsToCreate.get(thisItem) == null ? 0 : 1;

		return assignmentsToCreate.values().stream().filter(a -> StringUtils.equals(newAssignment.getName(), a.getName())).count() == expectedNameMatchesIfUnique;
	}
}
