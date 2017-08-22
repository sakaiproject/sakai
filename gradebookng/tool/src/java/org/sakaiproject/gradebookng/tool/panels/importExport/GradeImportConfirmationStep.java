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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.gradebookng.business.GradeSaveResponse;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem.Type;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemDetail;
import org.sakaiproject.gradebookng.business.util.MessageHelper;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.pages.ImportExportPage;
import org.sakaiproject.gradebookng.tool.panels.BasePanel;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;

import lombok.extern.slf4j.Slf4j;

/**
 * Confirmation page for what is going to be imported
 */
@Slf4j
public class GradeImportConfirmationStep extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final String panelId;
	private final IModel<ImportWizardModel> model;

	private boolean errors = false;

	public GradeImportConfirmationStep(final String id, final IModel<ImportWizardModel> importWizardModel) {
		super(id);
		this.panelId = id;
		this.model = importWizardModel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final ImportWizardModel importWizardModel = this.model.getObject();

		final List<ProcessedGradeItem> itemsToCreate = importWizardModel.getItemsToCreate();
		final List<ProcessedGradeItem> itemsToUpdate = importWizardModel.getItemsToUpdate();
		final List<ProcessedGradeItem> itemsToModify = importWizardModel.getItemsToModify();

		// note these are sorted alphabetically for now.
		// This may be changed to reflect the original order however any sorting needs to take into account that comments have the same
		// title as the item
		Collections.sort(itemsToCreate);
		Collections.sort(itemsToUpdate);
		Collections.sort(itemsToModify);

		final List<Assignment> assignmentsToCreate = importWizardModel.getAssignmentsToCreate();

		final Form<Void> form = new Form<Void>("form") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {

				final Map<String, Long> assignmentMap = new HashMap<>();

				// Create new GB items
				assignmentsToCreate.forEach(assignment -> {

					Long assignmentId = null;

					try {
						assignmentId = GradeImportConfirmationStep.this.businessService.addAssignment(assignment);
					} catch (final AssignmentHasIllegalPointsException e) {
						getSession().error(new ResourceModel("error.addgradeitem.points").getObject());
						GradeImportConfirmationStep.this.errors = true;
					} catch (final ConflictingAssignmentNameException e) {
						getSession().error(new ResourceModel("error.addgradeitem.title").getObject());
						GradeImportConfirmationStep.this.errors = true;
					} catch (final ConflictingExternalIdException e) {
						getSession().error(new ResourceModel("error.addgradeitem.exception").getObject());
						GradeImportConfirmationStep.this.errors = true;
					} catch (final Exception e) {
						getSession().error(new ResourceModel("error.addgradeitem.exception").getObject());
						GradeImportConfirmationStep.this.errors = true;
					}

					assignmentMap.put(StringUtils.trim(assignment.getName()), assignmentId);
				});

				// Modify any that need modification
				itemsToModify.forEach(item -> {

					final Double points = NumberUtils.toDouble(item.getItemPointValue());
					final Assignment assignment = GradeImportConfirmationStep.this.businessService.getAssignment(item.getItemTitle());
					assignment.setPoints(points);

					final boolean updated = GradeImportConfirmationStep.this.businessService.updateAssignment(assignment);
					if (!updated) {
						getSession().error(MessageHelper.getString("importExport.error.pointsmodification", assignment.getName()));
						GradeImportConfirmationStep.this.errors = true;
					}

					assignmentMap.put(StringUtils.trim(assignment.getName()), assignment.getId());
				});

				// add/update the data
				if (!GradeImportConfirmationStep.this.errors) {

					final List<ProcessedGradeItem> itemsToSave = new ArrayList<ProcessedGradeItem>();
					itemsToSave.addAll(itemsToUpdate);
					itemsToSave.addAll(itemsToCreate);
					itemsToSave.addAll(itemsToModify);

					itemsToSave.forEach(processedGradeItem -> {
						log.debug("Processing item: " + processedGradeItem);

						final List<ProcessedGradeItemDetail> processedGradeItemDetails = processedGradeItem.getProcessedGradeItemDetails();

						processedGradeItemDetails.forEach(processedGradeItemDetail -> {
							log.debug("Processing detail: " + processedGradeItemDetail);

							// get data
							// if its an update/modify, this will get the id
							Long assignmentId = processedGradeItem.getItemId();

							final String assignmentTitle = StringUtils.trim(processedGradeItem.getItemTitle());

							// a newly created assignment will have a null ID here and need a lookup from the map to get the ID
							if (assignmentId == null) {
								assignmentId = assignmentMap.get(assignmentTitle);
							}
							// TODO if assignmentId is still null, there will be a problem

							// just save comment
							if (processedGradeItem.getType() == ProcessedGradeItem.Type.COMMENT) {
								saveComment(assignmentId, processedGradeItemDetail);
							}

							// save grade (including comments)
							if (processedGradeItem.getType() == ProcessedGradeItem.Type.GB_ITEM) {

								final GradeSaveResponse response = saveGrade(assignmentId, processedGradeItemDetail);

								// handle the response types
								switch (response) {
									case OK:
										// sweet
										break;
									case OVER_LIMIT:
										// no worries!
										break;
									case NO_CHANGE:
										// Try to save just the comments
										saveComment(assignmentId, processedGradeItemDetail);
										break;
									case CONCURRENT_EDIT:
										// this will be handled eventually
										break;
									case ERROR:
										// uh oh
										getSession().error(new ResourceModel("importExport.error.grade").getObject());
										GradeImportConfirmationStep.this.errors = true;
										break;
									default:
										break;
								}
								log.info("Saved grade for assignment id: " + assignmentId + ", student: "
										+ processedGradeItemDetail.getStudentEid() + ", grade: " + processedGradeItemDetail.getGrade()
										+ ", comment: " + processedGradeItemDetail.getComment() + ", status: " + response);
							}

						});
					});
				}

				if (!GradeImportConfirmationStep.this.errors) {
					getSession().success(getString("importExport.confirmation.success"));
					setResponsePage(GradebookPage.class);
				}
				// auto refresh will render the errors
			}
		};
		add(form);

		// back button
		final Button backButton = new Button("backbutton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {

				// clear any previous errors
				final ImportExportPage page = (ImportExportPage) getPage();
				page.clearFeedback();

				Component newPanel = null;
				if (assignmentsToCreate.size() > 0) {
					newPanel = new CreateGradeItemStep(GradeImportConfirmationStep.this.panelId, Model.of(importWizardModel));
				} else {
					newPanel = new GradeItemImportSelectionStep(GradeImportConfirmationStep.this.panelId, Model.of(importWizardModel));
				}

				newPanel.setOutputMarkupId(true);
				GradeImportConfirmationStep.this.replaceWith(newPanel);
			}
		};
		backButton.setDefaultFormProcessing(false);
		form.add(backButton);

		// cancel button
		final Button cancelButton = new Button("cancelbutton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				// clear any previous errors
				final ImportExportPage page = (ImportExportPage) getPage();
				page.clearFeedback();

				setResponsePage(ImportExportPage.class);
			}
		};
		cancelButton.setDefaultFormProcessing(false);
		form.add(cancelButton);

		// finish button
		form.add(new Button("finishbutton"));

		// render items to be updated
		final boolean hasItemsToUpdate = !itemsToUpdate.isEmpty();
		final WebMarkupContainer gradesUpdateContainer = new WebMarkupContainer("grades_update_container") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return hasItemsToUpdate;
			}
		};
		add(gradesUpdateContainer);

		if (hasItemsToUpdate) {
			final ListView<ProcessedGradeItem> updateList = makeListView("grades_update", itemsToUpdate);
			updateList.setReuseItems(true);
			gradesUpdateContainer.add(updateList);
		}

		// render items to be created
		final boolean hasItemsToCreate = !itemsToCreate.isEmpty();
		final WebMarkupContainer gradesCreateContainer = new WebMarkupContainer("grades_create_container") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return hasItemsToCreate;
			}
		};
		add(gradesCreateContainer);

		if (hasItemsToCreate) {
			final ListView<ProcessedGradeItem> createList = makeListView("grades_create", itemsToCreate);
			createList.setReuseItems(true);
			gradesCreateContainer.add(createList);
		}

		// render items to be modified
		final boolean hasItemsToModify = !itemsToModify.isEmpty();
		final WebMarkupContainer gradesModifyContainer = new WebMarkupContainer("grades_modify_container") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return hasItemsToModify;
			}
		};
		add(gradesModifyContainer);

		if (hasItemsToModify) {
			final ListView<ProcessedGradeItem> modifyList = makeListView("grades_modify", itemsToModify);
			modifyList.setReuseItems(true);
			gradesModifyContainer.add(modifyList);
		}

	}

	/**
	 * Helper to create a listview for what needs to be shown
	 * 
	 * @param markupId wicket markup id
	 * @param itemList ist of stuff
	 * @return
	 */
	private ListView<ProcessedGradeItem> makeListView(final String markupId, final List<ProcessedGradeItem> itemList) {

		final ListView<ProcessedGradeItem> rval = new ListView<ProcessedGradeItem>(markupId, itemList) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<ProcessedGradeItem> item) {

				final ProcessedGradeItem gradeItem = item.getModelObject();

				String displayTitle = gradeItem.getItemTitle();
				if (gradeItem.getType() == Type.COMMENT) {
					displayTitle = MessageHelper.getString("importExport.confirmation.commentsdisplay", gradeItem.getItemTitle());
				}

				item.add(new Label("title", displayTitle));
				item.add(new Label("points", gradeItem.getItemPointValue()));

			}
		};

		return rval;
	}

	private void saveComment(final Long assignmentId, final ProcessedGradeItemDetail processedGradeItemDetail) {
		final String currentComment = StringUtils.trimToNull(GradeImportConfirmationStep.this.businessService
				.getAssignmentGradeComment(assignmentId, processedGradeItemDetail.getStudentUuid()));
		final String newComment = StringUtils.trimToNull(processedGradeItemDetail.getComment());

		if (!StringUtils.equals(currentComment, newComment)) {
			final boolean success = GradeImportConfirmationStep.this.businessService.updateAssignmentGradeComment(assignmentId,
					processedGradeItemDetail.getStudentUuid(), newComment);
			log.info("Saving comment: " + success + ", " + assignmentId + ", " + processedGradeItemDetail.getStudentEid() + ", "
					+ processedGradeItemDetail.getComment());
			if (!success) {
				getSession().error(new ResourceModel("importExport.error.comment").getObject());
				this.errors = true;
			}
		}
	}

	private GradeSaveResponse saveGrade(final Long assignmentId, final ProcessedGradeItemDetail processedGradeItemDetail) {
		return GradeImportConfirmationStep.this.businessService.saveGrade(assignmentId,
				processedGradeItemDetail.getStudentUuid(),
				processedGradeItemDetail.getGrade(), processedGradeItemDetail.getComment());
	}
}
