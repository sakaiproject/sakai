package org.sakaiproject.gradebookng.tool.panels.importExport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem.Status;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem.Type;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.ImportExportPage;

import lombok.extern.slf4j.Slf4j;

/**
 * Page to allow the user to select which items in the imported file are to be imported
 */
@Slf4j
public class GradeItemImportSelectionStep extends Panel {

	private static final long serialVersionUID = 1L;

	private final String panelId;
	private final IModel<ImportWizardModel> model;

	// a count of the items that can be selected
	private int selectableItems = 0;

	// flag indicating if the 'N/A / no changes' items are hidden
	private boolean naHidden = false;

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

		// get the count of items that are selectable
		GradeItemImportSelectionStep.this.selectableItems = importWizardModel.getProcessedGradeItems().stream().filter(item -> item.getStatus() != Status.SKIP).collect(Collectors.toList()).size();

		// label to show if all items are actually hidden
		final Label allHiddenLabel = new Label("allHiddenLabel", new ResourceModel("importExport.selection.hideitemsallhidden")) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return GradeItemImportSelectionStep.this.naHidden && (GradeItemImportSelectionStep.this.selectableItems == 0);
			}
		};
		allHiddenLabel.setOutputMarkupPlaceholderTag(true);
		add(allHiddenLabel);

		// button to hide NA/no changes items
		final AjaxLink<Void> hideNoChanges = new AjaxLink<Void>("hideNoChanges") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {

				// toggle button state
				if(GradeItemImportSelectionStep.this.naHidden) {
					//toggling off
					GradeItemImportSelectionStep.this.naHidden = false;
					this.add(AttributeModifier.replace("class", "button"));
					this.add(AttributeModifier.replace("aria-pressed", "false"));
				} else {
					//toggling on
					GradeItemImportSelectionStep.this.naHidden = true;
					this.add(AttributeModifier.replace("class", "button on"));
					this.add(AttributeModifier.replace("aria-pressed", "true"));
				}
				target.add(this);
				target.add(allHiddenLabel);

				// toggle elements
				target.appendJavaScript("$('.no_changes').toggle();");
				if(GradeItemImportSelectionStep.this.selectableItems == 0) {
					target.appendJavaScript("$('.selection_form').toggle();");
					//TODO show a message
				}
			}
		};
		add(hideNoChanges);


		// get the list of items to display
		// to retain order we use the grade items as the primary list
		// and pick out the comment item from the mapping
		// however we set the data into the allItems list as it includes the comment items
		final List<ProcessedGradeItem> allItems = importWizardModel.getProcessedGradeItems();
		final List<ProcessedGradeItem> gradeItems = filterListByType(allItems, Type.GB_ITEM);
		final Map<String, ProcessedGradeItem> commentMap = createCommentMap(allItems);
		final Map<String, ProcessedGradeItem> gradeItemMap = createGradeItemMap(allItems);

		final Form<?> form = new Form("form") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
				boolean validated = true;

				// get the items that were selected
				final List<ProcessedGradeItem> selectedGradeItems = filterListByType(allItems.stream().filter(item -> item.isSelected()).collect(Collectors.toList()), Type.GB_ITEM);
				final List<ProcessedGradeItem> selectedCommentItems = filterListByType(allItems.stream().filter(item -> item.isSelected()).collect(Collectors.toList()), Type.COMMENT);

				log.debug("Selected grade items: " + selectedGradeItems.size());
				log.debug("Selected grade items: " + selectedGradeItems);

				log.debug("Selected comment items: " + selectedCommentItems.size());
				log.debug("Selected comment items: " + selectedCommentItems);

				// combine the two lists. since comments can be toggled independently, the selectedGradeItems may not contain the item we need to update
				// this is combined with a 'type' and 'selected' check when adding the data to the gradebook, in the next step
				final List<ProcessedGradeItem> itemsToProcess = new ArrayList<>();
				itemsToProcess.addAll(selectedGradeItems);
				itemsToProcess.addAll(selectedCommentItems);


				// this has an odd model so we need to have the validation in the onSubmit.
				if (itemsToProcess.size() == 0) {
					validated = false;
					error(getString("importExport.selection.noneselected"));
				}

				if (validated) {

					// clear any previous errors
					final ImportExportPage page = (ImportExportPage) getPage();
					page.clearFeedback();

					// Process the selected items into the create/update lists
					// Note that the update list needs SKIP so we include comments
					final List<ProcessedGradeItem> itemsToCreate = filterListByStatus(itemsToProcess, Status.NEW);
					final List<ProcessedGradeItem> itemsToUpdate = filterListByStatus(itemsToProcess, Status.UPDATE, Status.SKIP);
					final List<ProcessedGradeItem> itemsToModify = filterListByStatus(itemsToProcess, Status.MODIFIED);

					log.debug("Items to create: " + itemsToCreate.size());
					log.debug("Items to update: " + itemsToUpdate.size());
					log.debug("Items to modify: " + itemsToModify.size());

					// repaint panel
					Component newPanel = null;
					importWizardModel.setItemsToCreate(itemsToCreate);
					importWizardModel.setItemsToUpdate(itemsToUpdate);
					importWizardModel.setItemsToModify(itemsToModify);

					// create those that need to be created. When finished all, continue.
					if (itemsToCreate.size() > 0) {
						importWizardModel.setStep(1);
						importWizardModel.setTotalSteps(itemsToCreate.size());
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
		form.add(backButton);

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

		form.add(new Button("nextbutton"));

		// render the list - comments are nested
		final ListView<ProcessedGradeItem> itemList = new ListView<ProcessedGradeItem>("items", gradeItems) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<ProcessedGradeItem> item) {

				// get name of this item
				// we DO NOT set the data into the item model since this only iterates the grade items
				// instead we operate directly on the allItems list objects via the mapping
				final String key = item.getModelObject().getItemTitle();
				final ProcessedGradeItem gradeItem = gradeItemMap.get(key);
				final ProcessedGradeItem commentItem = commentMap.get(key);

				log.debug("grade item: " + gradeItem);
				log.debug("matching comment: " + commentItem);

				final WebMarkupContainer gbItemContainer = new WebMarkupContainer("gb_item");
				final CheckBox gradeItemCheckbox = new CheckBox("checkbox", new PropertyModel<Boolean>(gradeItem, "selected"));
				final Label gradeItemTitle = new Label("title", gradeItem.getItemTitle() + gradeItem.getType().name());
				final Label gradeItemPoints = new Label("points", gradeItem.getItemPointValue());
				final Label gradeItemStatus = new Label("status", new ResourceModel("importExport.status." + gradeItem.getStatus().name()));

				gbItemContainer.add(gradeItemCheckbox);
				gbItemContainer.add(gradeItemTitle);
				gbItemContainer.add(gradeItemPoints);
				gbItemContainer.add(gradeItemStatus);
				item.add(gbItemContainer);

				// render the comments row as well
				final WebMarkupContainer commentContainer = new WebMarkupContainer("comments");
				final CheckBox commentsCheckbox = new CheckBox("checkbox", new PropertyModel<Boolean>(commentItem, "selected"));
				final Label commentsStatus = new Label("status", new ResourceModel("importExport.status." + commentItem.getStatus().name()));
				commentContainer.add(commentsCheckbox);
				commentContainer.add(commentsStatus);
				item.add(commentContainer);

				// special handling for external assignments
				if (gradeItem.getStatus() == Status.EXTERNAL) {
					gradeItemCheckbox.setVisible(false);
					commentsCheckbox.setVisible(false);
					gradeItemCheckbox.getParent().add(new AttributeAppender("class", " no_changes external"));
					commentsCheckbox.getParent().add(new AttributeAppender("class", " no_changes external"));
				}

				// special handling for no changes
				if (gradeItem.getStatus() == Status.SKIP) {
					gradeItemCheckbox.setVisible(false);
					gradeItemCheckbox.getParent().add(new AttributeAppender("class", " no_changes"));
				}
				if (commentItem.getStatus() == Status.SKIP) {
					commentsCheckbox.setVisible(false);
					commentsCheckbox.getParent().add(new AttributeAppender("class", " no_changes"));
				}


			}

		};

		itemList.setReuseItems(true);
		form.add(itemList);

	}

	/**
	 * Filter the list of items by the given statuses
	 * @param itemList
	 * @param statuses
	 * @return
	 */
	private List<ProcessedGradeItem> filterListByStatus(final List<ProcessedGradeItem> itemList, final Status... statuses) {
		final List<Status> statusList = Arrays.asList(statuses);
		final List<ProcessedGradeItem> filteredList = itemList.stream().filter(item -> statusList.contains(item.getStatus())).collect(Collectors.toList());
		return filteredList;
	}

	/**
	 * Filter the list of items by the given type
	 * @param itemList
	 * @param type
	 * @return
	 */
	private List<ProcessedGradeItem> filterListByType(final List<ProcessedGradeItem> itemList, final Type type) {
		final List<ProcessedGradeItem> filteredList = itemList.stream().filter(item -> item.getType() == type).collect(Collectors.toList());
		return filteredList;
	}


	/**
	 * Map a gradebook item to its comment column, if any. All gb items will have an entry, the value may be null if there are no comments.
	 * Entries are keyed on the gradebook item title.
	 * @param items
	 * @return
	 */
	private Map<String, ProcessedGradeItem> createCommentMap(final List<ProcessedGradeItem> items) {

		final List<ProcessedGradeItem> gbItems = filterListByType(items, Type.GB_ITEM);
		final List<ProcessedGradeItem> commentItems = filterListByType(items, Type.COMMENT);

		final Map<String, ProcessedGradeItem> rval = new HashMap<>();

		//match up the gradebook items with the comment columns. comment columns have the same title.
		gbItems.forEach(gbItem -> {
			final ProcessedGradeItem commentItem = commentItems.stream().filter(item -> StringUtils.equalsIgnoreCase(item.getItemTitle(), gbItem.getItemTitle())).findFirst().orElse(null);
			rval.put(gbItem.getItemTitle(), commentItem);
		});

		return rval;
	}

	/**
	 * Create a map of item title to item. Only gb items are in this map.
	 * @param items
	 * @return
	 */
	private Map<String, ProcessedGradeItem> createGradeItemMap(final List<ProcessedGradeItem> items) {
		final List<ProcessedGradeItem> gbItems = filterListByType(items, Type.GB_ITEM);
		final Map<String, ProcessedGradeItem> rval = gbItems.stream().collect(Collectors.toMap(ProcessedGradeItem::getItemTitle, Function.identity()));
		return rval;
	}

}
