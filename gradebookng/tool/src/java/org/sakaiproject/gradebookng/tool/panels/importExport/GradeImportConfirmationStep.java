package org.sakaiproject.gradebookng.tool.panels.importExport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
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
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemDetail;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemStatus;
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

	public GradeImportConfirmationStep(final String id, final IModel<ImportWizardModel> importWizardModel) {
		super(id);
		this.panelId = id;
		this.model = importWizardModel;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final ImportWizardModel importWizardModel = this.model.getObject();

		final List<ProcessedGradeItem> itemsToCreate = importWizardModel.getItemsToCreate();
		final List<ProcessedGradeItem> itemsToUpdate = importWizardModel.getItemsToUpdate();
		final List<ProcessedGradeItem> itemsToModify = importWizardModel.getItemsToModify();

		final List<Assignment> assignmentsToCreate = importWizardModel.getAssignmentsToCreate();

		final Form<?> form = new Form("form") {
			private static final long serialVersionUID = 1L;

			boolean errors = false;

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
                        this.errors = true;
                    } catch (final ConflictingAssignmentNameException e) {
                    	getSession().error(new ResourceModel("error.addgradeitem.title").getObject());
                        this.errors = true;
                    } catch (final ConflictingExternalIdException e) {
                    	getSession().error(new ResourceModel("error.addgradeitem.exception").getObject());
                        this.errors = true;
                    } catch (final Exception e) {
                    	getSession().error(new ResourceModel("error.addgradeitem.exception").getObject());
                        this.errors = true;
                    }

					assignmentMap.put(StringUtils.trim(assignment.getName()), assignmentId);
				});

				//Modify any that need modification
				itemsToModify.forEach(item -> {

					final Double points = NumberUtils.toDouble(item.getItemPointValue());
					final Assignment assignment = GradeImportConfirmationStep.this.businessService.getAssignment(item.getItemTitle());
					assignment.setPoints(points);

					final boolean updated = GradeImportConfirmationStep.this.businessService.updateAssignment(assignment);
					if(!updated) {
						getSession().error(MessageHelper.getString("importExport.error.pointsmodification", assignment.getName()));
                        this.errors = true;
					}

					assignmentMap.put(StringUtils.trim(assignment.getName()), assignment.getId());
				});

				// add/update the data
				if (!this.errors) {

					final List<ProcessedGradeItem> itemsToSave = new ArrayList<ProcessedGradeItem>();
					itemsToSave.addAll(itemsToUpdate);
					itemsToSave.addAll(itemsToCreate);
					itemsToSave.addAll(itemsToModify);

					itemsToSave.forEach(processedGradeItem -> {
						log.debug("Processing item: " + processedGradeItem);

						final List<ProcessedGradeItemDetail> processedGradeItemDetails = processedGradeItem.getProcessedGradeItemDetails();

						processedGradeItemDetails.forEach(processedGradeItemDetail -> {
							log.debug("Processing detail: " + processedGradeItemDetail);

							//get data
							// if its an update/modify, this will get the id
							Long assignmentId = processedGradeItem.getItemId();

							//if assignment title was modified, we need to use that instead
							final String assignmentTitle = StringUtils.trim((processedGradeItem.getAssignmentTitle() != null) ? processedGradeItem.getAssignmentTitle() : processedGradeItem.getItemTitle());

							// a newly created assignment will have a null ID here and need a lookup from the map to get the ID
							if (assignmentId == null) {
								assignmentId = assignmentMap.get(assignmentTitle);
							}
							//TODO if assignmentId is still null, there will be a problem

							final GradeSaveResponse saveResponse = GradeImportConfirmationStep.this.businessService.saveGrade(assignmentId,
									processedGradeItemDetail.getStudentUuid(),
									processedGradeItemDetail.getGrade(), processedGradeItemDetail.getComment());

							// handle the response types
							switch(saveResponse) {
								case OK:
									// sweet
									break;
								case OVER_LIMIT:
									// no worries!
									break;
								case NO_CHANGE:
									// Try to save just the comments
									final String currentComment = StringUtils.trimToNull(GradeImportConfirmationStep.this.businessService.getAssignmentGradeComment(assignmentId, processedGradeItemDetail.getStudentUuid()));
									final String newComment = StringUtils.trimToNull(processedGradeItemDetail.getComment());

									if (!StringUtils.equals(currentComment, newComment)) {
										final boolean success = GradeImportConfirmationStep.this.businessService.updateAssignmentGradeComment(assignmentId, processedGradeItemDetail.getStudentUuid(), newComment);
										log.info("Saving comment: " + success + ", " + assignmentId + ", "+ processedGradeItemDetail.getStudentEid() + ", " + processedGradeItemDetail.getComment());
										if (!success) {
											getSession().error(new ResourceModel("importExport.error.comment").getObject());
											this.errors = true;
										}
									}
									break;
								case CONCURRENT_EDIT:
									// this will be handled eventually
									break;
								case ERROR:
									// uh oh
									getSession().error(new ResourceModel("importExport.error.grade").getObject());
									this.errors = true;
									break;
								default:
									break;
							}

							log.info("Saving grade for assignment id: " +  assignmentId + ", student: " + processedGradeItemDetail.getStudentEid() + ", grade: " + processedGradeItemDetail.getGrade() + ", comment: " + processedGradeItemDetail.getComment() + ", status: " + saveResponse);
						});
					});
				}

				if (!this.errors) {
					getSession().success(getString("importExport.confirmation.success"));
					setResponsePage(GradebookPage.class);
				}
				//auto refresh will render the errors
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

		// render items to be created
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

				// ensure we display the edited data if we have it (won't exist for an update)
				final String assignmentTitle = gradeItem.getAssignmentTitle();
				final Double assignmentPoints = gradeItem.getAssignmentPoints();

				item.add(new Label("itemTitle", (assignmentTitle != null) ? assignmentTitle : gradeItem.getItemTitle()));
				item.add(new Label("itemPointValue", (assignmentPoints != null) ? assignmentPoints : gradeItem.getItemPointValue()));

				//if comment and it's being updated, add additional row
				if (gradeItem.getType() == ProcessedGradeItem.Type.COMMENT && gradeItem.getCommentStatus().getStatusCode() != ProcessedGradeItemStatus.STATUS_NA) {

					item.add(new Behavior() {
						private static final long serialVersionUID = 1L;

						@Override
						public void afterRender(final Component component) {
							super.afterRender(component);
							component.getResponse().write("<tr class=\"comment\"><td class=\"item_title\" colspan=\"2\"><span>" + getString("importExport.commentname") + "</span></td></tr>");
						}
					});
				}
			}
		};

		return rval;
	}

}
