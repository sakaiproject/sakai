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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem.Status;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem.Type;
import org.sakaiproject.gradebookng.tool.component.GbStyle;
import org.sakaiproject.gradebookng.tool.component.GbStyleableWebMarkupContainer;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.ImportExportPage;
import org.sakaiproject.gradebookng.tool.panels.BasePanel;

/**
 * Page to allow the user to select which items in the imported file are to be imported
 */
@Slf4j
public class GradeItemImportSelectionStep extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final String panelId;
	private final IModel<ImportWizardModel> model;

	// a count of the items that can be selected
	private int selectableItems = 0;

	// flag indicating if the 'N/A / no changes' items are hidden
	private boolean naHidden = false;

	GradeItemImportOmissionsPanel omissionsPanel;

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
		GradeItemImportSelectionStep.this.selectableItems = importWizardModel.getProcessedGradeItems().stream()
				.filter(item -> item.getStatus() != Status.SKIP).collect(Collectors.toList()).size();

		omissionsPanel = new GradeItemImportOmissionsPanel("omissionsPanel", model);
		add(omissionsPanel);

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
				if (GradeItemImportSelectionStep.this.naHidden) {
					// toggling off
					GradeItemImportSelectionStep.this.naHidden = false;
					this.add(AttributeModifier.replace("class", "button"));
					this.add(AttributeModifier.replace("aria-pressed", "false"));
				} else {
					// toggling on
					GradeItemImportSelectionStep.this.naHidden = true;
					this.add(AttributeModifier.replace("class", "button on"));
					this.add(AttributeModifier.replace("aria-pressed", "true"));
				}
				target.add(this);
				target.add(allHiddenLabel);

				// toggle elements
				target.appendJavaScript("$('.no_changes').toggle();");
				if (GradeItemImportSelectionStep.this.selectableItems == 0) {
					target.appendJavaScript("$('.selection_form').toggle();");
					// TODO show a message
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

		final Form<?> form = new Form("form");
		add(form);

		final AjaxButton backButton = new AjaxButton("backbutton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {

				// clear any previous errors
				final ImportExportPage page = (ImportExportPage) getPage();
				page.clearFeedback();
				page.updateFeedback(target);

				// Create the previous panel
				final Component previousPanel = new GradeImportUploadStep(GradeItemImportSelectionStep.this.panelId);
				previousPanel.setOutputMarkupId(true);

				// AJAX the previous panel into place
				WebMarkupContainer container = page.container;
				container.addOrReplace(previousPanel);
				target.add(container);
			}
		};
		backButton.setDefaultFormProcessing(false);
		form.add(backButton);

		final AjaxButton nextButton = new AjaxButton("nextbutton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

				// get the items that were selected
				final List<ProcessedGradeItem> selectedGradeItems = filterListByType(
						allItems.stream().filter(item -> item.isSelected()).collect(Collectors.toList()), Type.GB_ITEM);
				final List<ProcessedGradeItem> selectedCommentItems = filterListByType(
						allItems.stream().filter(item -> item.isSelected()).collect(Collectors.toList()), Type.COMMENT);

				log.debug("Selected grade items: {}", selectedGradeItems.size());
				log.debug("Selected grade items: {}", selectedGradeItems);
				log.debug("Selected comment items: {}", selectedCommentItems.size());
				log.debug("Selected comment items: {}", selectedCommentItems);

				// combine the two lists. since comments can be toggled independently, the selectedGradeItems may not contain the item we need to update
				// this is combined with a 'type' and 'selected' check when adding the data to the gradebook, in the next step
				final List<ProcessedGradeItem> itemsToProcess = new ArrayList<>();
				itemsToProcess.addAll(selectedGradeItems);
				itemsToProcess.addAll(selectedCommentItems);

				// this has an odd model so we need to have the validation in the onSubmit.
				final ImportExportPage page = (ImportExportPage) getPage();
				if (itemsToProcess.isEmpty()) {
					error(getString("importExport.selection.noneselected"));
					page.updateFeedback(target);
					return;
				}

				// clear any previous errors
				page.clearFeedback();
				page.updateFeedback(target);

				// Process the selected items into the create/update lists
				// Note that create and modify can only be for gb items - even if comments are Status.NEW they are handled as part of
				// the corresponding gb item data import
				final List<ProcessedGradeItem> itemsToCreate = filterListByType(filterListByStatus(itemsToProcess, Status.NEW), Type.GB_ITEM);
				final List<ProcessedGradeItem> itemsToUpdate = filterListByStatus(itemsToProcess, Status.UPDATE);
				final List<ProcessedGradeItem> itemsToModify = filterListByType(filterListByStatus(itemsToProcess, Status.MODIFIED), Type.GB_ITEM);

				log.debug("Items to create: {}", itemsToCreate.size());
				log.debug("Items to update: {}", itemsToUpdate.size());
				log.debug("Items to modify: {}", itemsToModify.size());

				// set data for next page
				importWizardModel.setItemsToCreate(itemsToCreate);
				importWizardModel.setItemsToUpdate(itemsToUpdate);
				importWizardModel.setItemsToModify(itemsToModify);
				importWizardModel.getAssignmentsToCreate().keySet().retainAll(itemsToCreate);

				// create those that need to be created. When finished all, continue.
				Component newPanel;
				if (itemsToCreate.size() > 0) {
					importWizardModel.setStep(1);
					importWizardModel.setTotalSteps(itemsToCreate.size());
					newPanel = new CreateGradeItemStep(GradeItemImportSelectionStep.this.panelId, Model.of(importWizardModel));
				} else {
					newPanel = new GradeImportConfirmationStep(GradeItemImportSelectionStep.this.panelId, Model.of(importWizardModel));
				}

				// AJAX the new panel into place
				newPanel.setOutputMarkupId(true);
				WebMarkupContainer container = page.container;
				container.addOrReplace(newPanel);
				target.add(newPanel);
			}
		};
		form.add(nextButton);

		final AjaxButton cancelButton = new AjaxButton("cancelbutton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				// clear any previous errors
				final ImportExportPage page = (ImportExportPage) getPage();
				page.clearFeedback();
				page.updateFeedback(target);
				setResponsePage(ImportExportPage.class);
			}
		};
		cancelButton.setDefaultFormProcessing(false);
		form.add(cancelButton);

		// render the list - comments are nested
		final ListView<ProcessedGradeItem> itemList = new ListView<ProcessedGradeItem>("items", gradeItems) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<ProcessedGradeItem> item) {

				// get name of this item
				// we DO NOT set the data into the item model since this only iterates the grade items
				// instead we operate directly on the allItems list objects via the mapping
				// note that there might not be a comment item so we setup a blank one for the pruposes of rendering only
				final String key = item.getModelObject().getItemTitle();
				final ProcessedGradeItem gradeItem = gradeItemMap.get(key);
				final ProcessedGradeItem commentItem = (commentMap.get(key) != null) ? commentMap.get(key) : setupBlankCommentItem();

				log.debug("grade item: {}", gradeItem);
				log.debug("matching comment: {}", commentItem);

				// setup our wrappers
				final GbStyleableWebMarkupContainer gbItemWrap = new GbStyleableWebMarkupContainer("gb_item");
				gbItemWrap.addStyle(GbStyle.GB_ITEM);

				final GbStyleableWebMarkupContainer commentWrap = new GbStyleableWebMarkupContainer("comments");
				commentWrap.addStyle(GbStyle.COMMENT);

				// render the item row
				final AjaxCheckBox gradeItemCheckbox = new AjaxCheckBox("checkbox", new PropertyModel<Boolean>(gradeItem, "selected")) {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						if (gradeItem.isSelected()) {
							gbItemWrap.addStyle(GbStyle.SELECTED);

							if (commentItem.isSelectable()) {
								commentItem.setSelected(true);
								commentWrap.addStyle(GbStyle.SELECTED);
							}

						} else {
							gbItemWrap.removeStyle(GbStyle.SELECTED);
						}
						gbItemWrap.style();
						commentWrap.style();
						target.add(commentWrap);
						target.add(gbItemWrap);
					}
				};
				final Label gradeItemTitle = new Label("title", gradeItem.getItemTitle());
				final Label gradeItemPoints = new Label("points", gradeItem.getItemPointValue());
				final Label gradeItemStatus = new Label("status", new ResourceModel("importExport.status." + gradeItem.getStatus().name()));

				gbItemWrap.add(gradeItemCheckbox);
				gbItemWrap.add(gradeItemTitle);
				gbItemWrap.add(gradeItemPoints);
				gbItemWrap.add(gradeItemStatus);
				item.add(gbItemWrap);

				// render the comments row
				final AjaxCheckBox commentsCheckbox = new AjaxCheckBox("checkbox", new PropertyModel<Boolean>(commentItem, "selected")) {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						if (commentItem.isSelected()) {
							commentWrap.addStyle(GbStyle.SELECTED);
						} else {
							commentWrap.removeStyle(GbStyle.SELECTED);
						}
						commentWrap.style();
						target.add(commentWrap);
					}
				};

				final Label commentsStatus = new Label("status", new ResourceModel("importExport.status." + commentItem.getStatus().name()));
				commentWrap.add(commentsCheckbox);
				commentWrap.add(commentsStatus);
				item.add(commentWrap);

				// special handling for external assignments
				if (gradeItem.getStatus() == Status.EXTERNAL) {
					gradeItemCheckbox.setVisible(false);
					commentsCheckbox.setVisible(false);
					gbItemWrap.addStyle(GbStyle.EXTERNAL);
					commentWrap.addStyle(GbStyle.EXTERNAL);
				}

				// special handling for no changes
				if (gradeItem.getStatus() == Status.SKIP) {
					gradeItemCheckbox.setVisible(false);
					gbItemWrap.addStyle(GbStyle.NO_CHANGES);
				}
				if (commentItem.getStatus() == Status.SKIP) {
					commentsCheckbox.setVisible(false);
					commentWrap.addStyle(GbStyle.NO_CHANGES);
				}

				// initialise the styling for each row
				gbItemWrap.style();
				commentWrap.style();
			}
		};

		itemList.setReuseItems(true);
		form.add(itemList);
	}

	/**
	 * Filter the list of items by the given statuses
	 * 
	 * @param itemList
	 * @param statuses
	 * @return
	 */
	private List<ProcessedGradeItem> filterListByStatus(final List<ProcessedGradeItem> itemList, final Status... statuses) {
		final List<Status> statusList = Arrays.asList(statuses);
		final List<ProcessedGradeItem> filteredList = itemList.stream().filter(item -> statusList.contains(item.getStatus()))
				.collect(Collectors.toList());
		return filteredList;
	}

	/**
	 * Filter the list of items by the given type
	 * 
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
	 * 
	 * @param items
	 * @return
	 */
	private Map<String, ProcessedGradeItem> createCommentMap(final List<ProcessedGradeItem> items) {

		final List<ProcessedGradeItem> gbItems = filterListByType(items, Type.GB_ITEM);
		final List<ProcessedGradeItem> commentItems = filterListByType(items, Type.COMMENT);
		final Map<String, ProcessedGradeItem> rval = new HashMap<>();

		// match up the gradebook items with the comment columns. comment columns have the same title.
		gbItems.forEach(gbItem -> {
			final ProcessedGradeItem commentItem = commentItems.stream()
					.filter(item -> StringUtils.equalsIgnoreCase(item.getItemTitle(), gbItem.getItemTitle())).findFirst().orElse(null);
			rval.put(gbItem.getItemTitle(), commentItem);
		});

		return rval;
	}

	/**
	 * Create a map of item title to item. Only gb items are in this map.
	 * 
	 * @param items
	 * @return
	 */
	private Map<String, ProcessedGradeItem> createGradeItemMap(final List<ProcessedGradeItem> items) {
		final List<ProcessedGradeItem> gbItems = filterListByType(items, Type.GB_ITEM);
		final Map<String, ProcessedGradeItem> rval = gbItems.stream()
				.collect(Collectors.toMap(ProcessedGradeItem::getItemTitle, Function.identity()));

		return rval;
	}

	/**
	 * If there is no comment item imported we need a blank object so that the row renders correctly
	 * 
	 * @return
	 */
	private ProcessedGradeItem setupBlankCommentItem() {
		final ProcessedGradeItem rval = new ProcessedGradeItem();
		rval.setType(Type.COMMENT);
		rval.setStatus(Status.SKIP);
		return rval;
	}
}
