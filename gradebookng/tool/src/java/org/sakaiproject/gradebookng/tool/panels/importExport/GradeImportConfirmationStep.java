package org.sakaiproject.gradebookng.tool.panels.importExport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradeSaveResponse;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemDetail;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;

import lombok.extern.slf4j.Slf4j;

/**
 * Confirmation page for what is going to be imported
 */
@Slf4j
public class GradeImportConfirmationStep extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

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

				// only proceeed if no errors
				if (!this.errors) {

					final List<ProcessedGradeItem> itemsToSave = new ArrayList<ProcessedGradeItem>();
					itemsToSave.addAll(itemsToUpdate);
					itemsToSave.addAll(itemsToCreate);

					itemsToSave.forEach(processedGradeItem -> {
						log.debug("Looping through items to save");

						final List<ProcessedGradeItemDetail> processedGradeItemDetails = processedGradeItem.getProcessedGradeItemDetails();

						processedGradeItemDetails.forEach(processedGradeItemDetail -> {
							log.debug("Looping through detail items to save");

							//get data
							Long assignmentId = processedGradeItem.getItemId();

							//if assignment title was modified, we need to use that instead
							final String assignmentTitle = StringUtils.trim((processedGradeItem.getAssignmentTitle() != null) ? processedGradeItem.getAssignmentTitle() : processedGradeItem.getItemTitle());

							if (assignmentId == null) {
								// Should be a newly created GB item
								assignmentId = assignmentMap.get(assignmentTitle);
							}

							final GradeSaveResponse saved = GradeImportConfirmationStep.this.businessService.saveGrade(assignmentId,
									processedGradeItemDetail.getStudentUuid(),
									processedGradeItemDetail.getGrade(), processedGradeItemDetail.getComment());

							//if no change, try just the comment
							if (saved == GradeSaveResponse.NO_CHANGE) {

								// Check for changed comments
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
							} else if (saved != GradeSaveResponse.OK) {
								// Anything other than OK is bad
								getSession().error(new ResourceModel("importExport.error.grade").getObject());
								this.errors = true;
							}
							log.info("Saving grade: " + saved + ", " + assignmentId + ", " + processedGradeItemDetail.getStudentEid() + ", " + processedGradeItemDetail.getGrade() + ", " + processedGradeItemDetail.getComment());
						});
					});
				}

				if (!this.errors) {
					getSession().success(getString("importExport.confirmation.success"));
					setResponsePage(new GradebookPage());
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
				log.debug("Clicking back button...");
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

		// finish button
		form.add(new Button("finishbutton"));

		// items to be updated
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

		// items to be created
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

				final String commentLabel = gradeItem.getCommentLabel();

				//if comment label, add additional row
				if (commentLabel != null) {

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
