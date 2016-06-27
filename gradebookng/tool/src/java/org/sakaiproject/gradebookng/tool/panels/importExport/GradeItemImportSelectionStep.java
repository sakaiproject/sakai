package org.sakaiproject.gradebookng.tool.panels.importExport;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemStatus;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.ImportExportPage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Page to allow the user to select which items in the imported file ar to be imported
 */
public class GradeItemImportSelectionStep extends Panel {
	private static final Logger log = Logger.getLogger(GradeItemImportSelectionStep.class);
	private static final long serialVersionUID = 1L;

	private final String panelId;
	private final IModel<ImportWizardModel> model;

	public GradeItemImportSelectionStep(final String id, final IModel<ImportWizardModel> importWizardModel) {
		super(id);
		this.panelId = id;
		this.model = importWizardModel;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final ImportWizardModel importWizardModel = this.model.getObject();

		final CheckGroup<ProcessedGradeItem> group = new CheckGroup<ProcessedGradeItem>("group", new ArrayList<ProcessedGradeItem>());

		final Form<?> form = new Form("form") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
				boolean validated = true;

				final List<ProcessedGradeItem> selectedGradeItems = (List<ProcessedGradeItem>) group.getModelObject();
				log.debug("Processed items: " + selectedGradeItems.size());

				// this has an odd model so we need to have the validation in the onSubmit.
				if (selectedGradeItems.size() == 0) {
					validated = false;
					error(getString("importExport.selection.noneselected"));
				}

				if (validated) {

					// clear any previous errors
					final ImportExportPage page = (ImportExportPage) getPage();
					page.clearFeedback();

					// Process the selected items into the create/update lists
					final List<ProcessedGradeItem> itemsToUpdate = filterListByStatus(selectedGradeItems,
							Arrays.asList(ProcessedGradeItemStatus.STATUS_UPDATE, ProcessedGradeItemStatus.STATUS_NA));
					final List<ProcessedGradeItem> itemsToCreate = filterListByStatus(selectedGradeItems,
							Arrays.asList(ProcessedGradeItemStatus.STATUS_NEW));

					log.debug("Filtered Update items: " + itemsToUpdate.size());
					log.debug("Filtered Create items: " + itemsToCreate.size());

					final List<ProcessedGradeItem> gbItemsToCreate = new ArrayList<ProcessedGradeItem>();
					itemsToCreate.forEach(item -> {
						// Don't want comment items here
						if (!"N/A".equals(item.getItemPointValue())) {
							gbItemsToCreate.add(item);
						}
					});

					log.debug("Actual items to create: " + gbItemsToCreate.size());

					// repaint panel
					Component newPanel = null;
					importWizardModel.setSelectedGradeItems(selectedGradeItems);
					importWizardModel.setGbItemsToCreate(gbItemsToCreate);
					importWizardModel.setItemsToCreate(itemsToCreate);
					importWizardModel.setItemsToUpdate(itemsToUpdate);
					if (gbItemsToCreate.size() > 0) {
						importWizardModel.setStep(1);
						importWizardModel.setTotalSteps(gbItemsToCreate.size());
						newPanel = new CreateGradeItemStep(GradeItemImportSelectionStep.this.panelId, Model.of(importWizardModel));
					} else {
						newPanel = new GradeImportConfirmationStep(GradeItemImportSelectionStep.this.panelId, Model.of(importWizardModel));
					}
					newPanel.setOutputMarkupId(true);
					GradeItemImportSelectionStep.this.replaceWith(newPanel);
				}

			}
		};
		add(form);
		form.add(group);

		final Button backButton = new Button("backbutton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {

				// clear any previous errors
				final ImportExportPage page = (ImportExportPage) getPage();
				page.clearFeedback();

				final Component newPanel = new GradeImportUploadStep(GradeItemImportSelectionStep.this.panelId);
				newPanel.setOutputMarkupId(true);
				GradeItemImportSelectionStep.this.replaceWith(newPanel);
			}
		};
		backButton.setDefaultFormProcessing(false);
		group.add(backButton);

		group.add(new Button("nextbutton"));

		group.add(new CheckGroupSelector("groupselector"));
		final ListView<ProcessedGradeItem> gradeList = new ListView<ProcessedGradeItem>("grades",
				importWizardModel.getProcessedGradeItems()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<ProcessedGradeItem> item) {

				final Check<ProcessedGradeItem> checkbox = new Check<>("checkbox", item.getModel());
				final Label itemTitle = new Label("itemTitle", new PropertyModel<String>(item.getDefaultModel(), "itemTitle"));
				final Label itemPointValue = new Label("itemPointValue",
						new PropertyModel<String>(item.getDefaultModel(), "itemPointValue"));
				final Label itemStatus = new Label("itemStatus");

				item.add(checkbox);
				item.add(itemTitle);
				item.add(itemPointValue);
				item.add(itemStatus);

				// Use the status code to look up the text representation
				final PropertyModel<ProcessedGradeItemStatus> statusProp = new PropertyModel<ProcessedGradeItemStatus>(
						item.getDefaultModel(), "status");
				final ProcessedGradeItemStatus status = statusProp.getObject();

				// For external items, set a different label and disable the control
				if (status.getStatusCode() == ProcessedGradeItemStatus.STATUS_EXTERNAL) {
					itemStatus.setDefaultModel(new StringResourceModel("importExport.status." + status.getStatusCode(), statusProp, null,
							status.getStatusValue()));
					item.setEnabled(false);
					item.add(new AttributeModifier("class", "external"));
				} else {

					itemStatus.setDefaultModel(new ResourceModel("importExport.status." + status.getStatusCode()));

					// if no changes, grey it out and remove checkbox
					if (status.getStatusCode() == ProcessedGradeItemStatus.STATUS_NA) {
						checkbox.setVisible(false);
						item.add(new AttributeAppender("class", Model.of("no_changes"), " "));
					}

				}

				final String naString = getString("importExport.selection.pointValue.na", new Model(), "N/A");
				if (naString.equals(item.getModelObject().getItemPointValue())) {
					item.add(new AttributeAppender("class", Model.of("comment"), " "));
				}

				// add an additional row for the comments for each
				final PropertyModel<String> commentLabelProp = new PropertyModel<String>(item.getDefaultModel(), "commentLabel");
				final PropertyModel<ProcessedGradeItemStatus> commentStatusProp = new PropertyModel<ProcessedGradeItemStatus>(
						item.getDefaultModel(), "commentStatus");
				final String commentLabel = commentLabelProp.getObject();
				final ProcessedGradeItemStatus commentStatus = commentStatusProp.getObject();

				item.add(new Behavior() {
					private static final long serialVersionUID = 1L;

					@Override
					public void afterRender(final Component component) {
						super.afterRender(component);
						if (commentLabel != null) {
							String rowClass = "comment";
							String statusValue = getString("importExport.status." + commentStatus.getStatusCode());
							if (commentStatus.getStatusCode() == ProcessedGradeItemStatus.STATUS_EXTERNAL) {
								rowClass += " external";
								statusValue = new StringResourceModel("importExport.status." + commentStatus.getStatusCode(),
										commentStatusProp, null, commentStatus.getStatusValue()).getString();
							}
							if (commentStatus.getStatusCode() == ProcessedGradeItemStatus.STATUS_NA) {
								rowClass += " no_changes";
							}

							component.getResponse().write(
									"<tr class=\"" + rowClass + "\">" +
											"<td></td>" +
											"<td class=\"item_title\">" + commentLabel + "</td>" +
											"<td class=\"item_points\">" + naString + "</td>" +
											"<td class=\"item_status\">" + statusValue + "</td>" +
											"</tr>"

							);
						}
					}
				});

			}

		};

		gradeList.setReuseItems(true);
		group.add(gradeList);

	}

	private List<ProcessedGradeItem> filterListByStatus(final List<ProcessedGradeItem> gradeList, final List<Integer> statuses) {
		final List<ProcessedGradeItem> filteredList = new ArrayList<ProcessedGradeItem>();
		for (final ProcessedGradeItem gradeItem : gradeList) {
			if (statuses.contains(gradeItem.getStatus().getStatusCode())) {
				filteredList.add(gradeItem);
			}
		}
		return filteredList;
	}

}
