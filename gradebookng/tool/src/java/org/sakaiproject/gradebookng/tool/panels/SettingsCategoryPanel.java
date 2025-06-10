/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *			   http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.gradebookng.tool.panels;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import lombok.Getter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.gradebookng.tool.pages.SettingsPage;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.grading.api.GradingConstants;

public class SettingsCategoryPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	IModel<GbSettings> model;

	boolean isDropHighest = false;
	boolean isDropLowest = false;
	boolean isKeepHighest = false;
	boolean isEqualWeight = false;
	boolean expanded = false;

	Radio<Integer> categoriesAndWeighting;

	Map<Long, Boolean> categoryDropKeepAvailability = new HashMap<>();

	public SettingsCategoryPanel(final String id, final IModel<GbSettings> model, final boolean expanded) {
		super(id, model);
		this.model = model;
		this.expanded = expanded;
	}

	@Getter
	private enum DropKeepUsage {
		CATEGORY("settingspage.categories.instructions.applydropkeep"),
		EXCLUSIVE("settingspage.categories.hover.dropkeepusage");

		private String message;

		DropKeepUsage(final String message) {
			this.message = message;
		}

	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final SettingsPage settingsPage = (SettingsPage) getPage();

		GradebookInformation settings = model.getObject().getGradebookInformation();

		// get categories, passed in
		final List<CategoryDefinition> categories = settings.getCategories();

		// parse the categories
		// 1. see if we have any drophighest/lowest/keep highest and set the flags for the checkboxes to use
		// 2. check the assignments in each category and see if there are assignments with differing point values.
		// This means that drop/keep highest/lowest is not available for that category

		for (final CategoryDefinition category : categories) {

			// check settings
			if (category.getDropHighest() != null && category.getDropHighest() > 0) {
				this.isDropHighest = true;
			}
			if (category.getDropLowest() != null && category.getDropLowest() > 0) {
				this.isDropLowest = true;
			}
			if (category.getKeepHighest() != null && category.getKeepHighest() > 0) {
				this.isKeepHighest = true;
			}
			if (category.getEqualWeight()) {
				this.isEqualWeight = true;
			}

			// check for differing points if not using an equal weight category
			final Set<BigDecimal> points = new HashSet<>();
			if (!category.getEqualWeight()) {
				for (Assignment a : category.getAssignmentList()) {
					if (settings.getGradeType() != GradeType.LETTER) {
						// Possible for some tools to send a big floating point double here so round it
						points.add(new BigDecimal(a.getPoints()).setScale(2, RoundingMode.HALF_DOWN));
					} else {
						points.add(BigDecimal.valueOf(gradingService.getMaxPoints(settings.getSelectedGradingScaleBottomPercents()).orElse(0D)));
					}
				}
			}

			this.categoryDropKeepAvailability.put(category.getId(), (points.size() <= 1));

		}

		// if categories enabled but we don't have any yet, add a default one
		if (!Objects.equals(settings.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY)
				&& categories.isEmpty()) {
			settings.getCategories().add(stubCategoryDefinition());
		}

		final WebMarkupContainer settingsCategoriesAccordionButton = new WebMarkupContainer("settingsCategoriesAccordionButton");
		final WebMarkupContainer settingsCategoriesPanel = new WebMarkupContainer("settingsCategoriesPanel");
		
		// Set up accordion behavior
		setupAccordionBehavior(settingsCategoriesAccordionButton, settingsCategoriesPanel, this.expanded, 
			new AccordionStateUpdater() {
				@Override
				public void updateState(boolean newState) {
					SettingsCategoryPanel.this.expanded = newState;
					
					// When expanding the panel, reinitialize the drag functionality
					if (newState) {
						AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class).orElse(null);
						if (target != null) {
							target.appendJavaScript("sakai.gradebookng.settings.categories = new GradebookCategorySettings($('#settingsCategories'));");
						}
					}
				}
				
				@Override
				public boolean getState() {
					return SettingsCategoryPanel.this.expanded;
				}
			});
		
		add(settingsCategoriesPanel);
		add(settingsCategoriesAccordionButton);

		// category types (note categoriesAndWeighting treated differently due to inter panel updates)
		final RadioGroup<Integer> categoryType = new RadioGroup<>("categoryType",
				new PropertyModel<>(this.model, "gradebookInformation.categoryType"));
		final Radio<Integer> none = new Radio<>("none", new Model<>(GradingConstants.CATEGORY_TYPE_NO_CATEGORY));
		final Radio<Integer> categoriesOnly = new Radio<>("categoriesOnly", new Model<>(GradingConstants.CATEGORY_TYPE_ONLY_CATEGORY));
		this.categoriesAndWeighting = new Radio<>("categoriesAndWeighting", new Model<>(GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY));

		// on load, if course grade displayed and points selected, disable categories and weighting
		updateCategoriesAndWeightingRadioState();

		categoryType.add(none);
		categoryType.add(categoriesOnly);
		categoryType.add(this.categoriesAndWeighting);

		categoryType.setRequired(true);
		settingsCategoriesPanel.add(categoryType);

		// render category related form fields
		final WebMarkupContainer categoriesWrap = new WebMarkupContainer("categoriesWrap") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				// don't show if 'no categories'
				return !Objects.equals(settings.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY);
			}

		};
		categoriesWrap.setOutputMarkupPlaceholderTag(true);

		// wrapper for the options
		final WebMarkupContainer categoriesOptionsWrap = new WebMarkupContainer("categoriesOptionsWrap");

		// enable drop highest
		final AjaxCheckBox dropHighest = new AjaxCheckBox("dropHighest", Model.of(this.isDropHighest)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				SettingsCategoryPanel.this.isDropHighest = getModelObject();

				// reset
				if (!SettingsCategoryPanel.this.isDropHighest) {
					for (final CategoryDefinition c : settings
							.getCategories()) {
						c.setDropHighest(0);
					}
					target.appendJavaScript("$('.gb-category-drophighest').hide();");
				}

				target.add(categoriesWrap);
			}
		};

		dropHighest.setOutputMarkupId(true);
		categoriesOptionsWrap.add(dropHighest);

		// enable drop lowest
		final AjaxCheckBox dropLowest = new AjaxCheckBox("dropLowest", Model.of(this.isDropLowest)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				SettingsCategoryPanel.this.isDropLowest = getModelObject();

				// reset
				if (!SettingsCategoryPanel.this.isDropLowest) {
					for (final CategoryDefinition c : settings
							.getCategories()) {
						c.setDropLowest(0);
					}
					target.appendJavaScript("$('.gb-category-droplowest').hide();");
				}

				target.add(categoriesWrap);
			}
		};
		dropLowest.setOutputMarkupId(true);
		categoriesOptionsWrap.add(dropLowest);

		// enable keep highest
		final AjaxCheckBox keepHighest = new AjaxCheckBox("keepHighest", Model.of(this.isKeepHighest)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				SettingsCategoryPanel.this.isKeepHighest = getModelObject();

				// reset
				if (!SettingsCategoryPanel.this.isKeepHighest) {
					for (final CategoryDefinition c : settings
							.getCategories()) {
						c.setKeepHighest(0);
					}
					target.appendJavaScript("$('.gb-category-keephighest').hide();");
				}

				target.add(categoriesWrap);
			}
		};
		keepHighest.setOutputMarkupId(true);
		categoriesOptionsWrap.add(keepHighest);

		// enable equal weight
		final AjaxCheckBox equalWeight = new AjaxCheckBox("equalWeight", Model.of(this.isEqualWeight)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				SettingsCategoryPanel.this.isEqualWeight = getModelObject();

				// reset
				if (!SettingsCategoryPanel.this.isEqualWeight) {
					for (final CategoryDefinition c : settings
							.getCategories()) {
						c.setEqualWeight(false);
					}
					target.appendJavaScript("$('.gb-category-equalweight').hide();");
				}

				target.add(categoriesWrap);
			}
		};
		equalWeight.setOutputMarkupId(true);
		categoriesOptionsWrap.add(equalWeight);

		// add the options wrapper
		categoriesOptionsWrap.setOutputMarkupPlaceholderTag(true);
		categoriesWrap.add(categoriesOptionsWrap);

		// When category type changes, ensure form is updated to reflect new value
		categoryType.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {

				// adjust visibility of items depending on category type
				final Integer type = SettingsCategoryPanel.this.model.getObject().getGradebookInformation().getCategoryType();

				categoriesWrap.setVisible(!Objects.equals(type, GradingConstants.CATEGORY_TYPE_NO_CATEGORY));
				categoriesOptionsWrap.setVisible(!Objects.equals(type, GradingConstants.CATEGORY_TYPE_NO_CATEGORY));
				equalWeight.setEnabled(!Objects.equals(type, GradingConstants.CATEGORY_TYPE_ONLY_CATEGORY));

				// if categories only (2), the categories table will be visible but the weighting column and tally will not
				if (Objects.equals(type, GradingConstants.CATEGORY_TYPE_ONLY_CATEGORY)) {
					target.appendJavaScript("$('.gb-category-weight').hide();");
					target.appendJavaScript("$('.gb-category-runningtotal').hide();");

					// If instructor flips from weighted to unweighted categories, we need to zero out the existing equal weight settings
					equalWeight.setModelValue(new String[]{"false"});
					for (final CategoryDefinition c : SettingsCategoryPanel.this.model.getObject().getGradebookInformation().getCategories()) {
						c.setEqualWeight(false);
					}
					target.appendJavaScript("$('.gb-category-equalweight').hide();");
				}

				// switching to categories but we don't have any, add a default one
				if (!Objects.equals(type, GradingConstants.CATEGORY_TYPE_NO_CATEGORY) && categories.isEmpty()) {
					SettingsCategoryPanel.this.model.getObject().getGradebookInformation().getCategories().add(stubCategoryDefinition());
				}

				// if categories and weighting, disable course grade points
				final AjaxCheckBox points = settingsPage.getSettingsGradeReleasePanel().getPointsCheckBox();
				settingsPage.getSettingsGradeReleasePanel().updatePointsCheckboxState();
				if (points.isVisibleInHierarchy()) {
					target.add(points);
				}

				// reinitialize any custom behaviour
				target.appendJavaScript("sakai.gradebookng.settings.categories = new GradebookCategorySettings($('#settingsCategories'));");

				target.add(categoriesWrap);
			}
		});

		// running total
		final WebMarkupContainer runningTotalMessage = new WebMarkupContainer("runningTotalMessage");
		runningTotalMessage.setOutputMarkupPlaceholderTag(true);
		categoriesWrap.add(runningTotalMessage);
		final Label runningTotal = new Label("runningTotal");
		runningTotal.setOutputMarkupId(true);
		updateRunningTotal(runningTotal, runningTotalMessage);
		categoriesWrap.add(runningTotal);

		// categories list
		final ListView<CategoryDefinition> categoriesView = new ListView<CategoryDefinition>("categoriesView",
				this.model.getObject().getGradebookInformation().getCategories()) {

			private static final long serialVersionUID = 1L;

			/*
			 * @Override public final List<CategoryDefinition> getList() { List<CategoryDefinition> categories = super.getList();
			 *
			 * Collections.sort(categories, businessService.getCategoryOrderComparator()); return categories; }
			 */

			@Override
			protected void populateItem(final ListItem<CategoryDefinition> item) {

				final ListView<CategoryDefinition> lv = this; // reference to self

				final CategoryDefinition category = item.getModelObject();

				// get the config. If there are no categories, detault is that the settings are enabled.
				final boolean dropKeepEnabled = BooleanUtils
						.toBooleanDefaultIfNull(SettingsCategoryPanel.this.categoryDropKeepAvailability.get(category.getId()), true);

				// note that all of these fields must have an ajaxform behaviour attached
				// so that their data is persisted into the model.
				// if they don't, when the listview repaints, they will be cleared
				// this can be either an OnChangeAjaxBehavior for those that need something to happen
				// or an AjaxFormComponentUpdatingBehavior for those that just need the data kept

				// name
				final TextField<String> name = new TextField<String>("name", new PropertyModel<String>(category, "name"));

				// Set the maximum length of the input to 99 characters
				name.add(AttributeModifier.replace("maxlength", "99"));

				name.add(new AjaxFormComponentUpdatingBehavior("blur") {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
					}
				});
				item.add(name);

				// Proper label for the drag handle (for screen readers)
				item.add(new WebMarkupContainer("handle").add(new AttributeModifier("aria-label", new PropertyModel<>(category, "name"))));

				// weight
				final TextField<Double> weight = new TextField<Double>("weight", new PropertyModel<Double>(category, "weight")) {
					private static final long serialVersionUID = 1L;

					@SuppressWarnings("unchecked")
					@Override
					public <C> IConverter<C> getConverter(final Class<C> type) {
						return (IConverter<C>) new PercentConverter();
					}

				};

				// onchange, update the running total
				weight.add(new OnChangeAjaxBehavior() {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						updateRunningTotal(target, runningTotal, runningTotalMessage);
					}
				});
				item.add(weight);

				// num assignments
				final Label numItems = new Label("numItems", new StringResourceModel("settingspage.categories.items")
						.setParameters(category.getAssignmentList().size()));
				item.add(numItems);

				// extra credit
				final CheckBox extraCredit = new CheckBox("extraCredit", new PropertyModel<Boolean>(category, "extraCredit"));
				extraCredit.setOutputMarkupId(true);

				// onchange, update the running total as extra credit items are excluded, and disable the weighting box
				extraCredit.add(new OnChangeAjaxBehavior() {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						updateRunningTotal(target, runningTotal, runningTotalMessage);
					}
				});
				item.add(extraCredit);

				// declare these here so we can work with the values. Config updated afterwards
				// mutually exclusive rules apply here
				final TextField<Integer> categoryDropHighest = new TextField<Integer>("categoryDropHighest",
						new PropertyModel<Integer>(category, "dropHighest"));
				final TextField<Integer> categoryDropLowest = new TextField<Integer>("categoryDropLowest",
						new PropertyModel<Integer>(category, "dropLowest"));
				final TextField<Integer> categoryKeepHighest = new TextField<Integer>("categoryKeepHighest",
						new PropertyModel<Integer>(category, "keepHighest"));

				boolean categoryDropHighestEnabled = true;
				boolean categoryDropLowestEnabled = true;
				boolean categoryKeepHighestEnabled = true;

				if (category.getDropHighest() != null && category.getDropHighest().intValue() > 0) {
					categoryKeepHighest.setModelValue(new String[] { "0" });
					categoryKeepHighestEnabled = false;
				}

				if (category.getDropLowest() != null && category.getDropLowest().intValue() > 0) {
					categoryKeepHighest.setModelValue(new String[] { "0" });
					categoryKeepHighestEnabled = false;
				}

				if (category.getKeepHighest() != null && category.getKeepHighest().intValue() > 0) {
					categoryDropHighest.setModelValue(new String[] { "0" });
					categoryDropLowest.setModelValue(new String[] { "0" });
					categoryDropHighestEnabled = false;
					categoryDropLowestEnabled = false;
				}

				// drop highest config
				categoryDropHighest.setOutputMarkupId(true);
				categoryDropHighest.add(new AjaxFormComponentUpdatingBehavior("blur") {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						// if drop highest is non zero, keep highest is to be unavailable
						Integer value = categoryDropHighest.getModelObject();
						if (value == null) {
							value = 0;
							categoryDropHighest.setModelValue(new String[] { "0" });
						}

						// remove tooltips and recalc
						removeDropKeepDisabledToolTip(categoryDropHighest);
						removeDropKeepDisabledToolTip(categoryDropLowest);
						removeDropKeepDisabledToolTip(categoryKeepHighest);

						categoryKeepHighest.setEnabled(true);
						if (value.intValue() > 0) {
							categoryKeepHighest.setModelValue(new String[] { "0" });
							categoryKeepHighest.setEnabled(false);
							addDropKeepDisabledToolTip(categoryKeepHighest, DropKeepUsage.EXCLUSIVE);
						}
						target.add(categoryDropHighest);
						target.add(categoryDropLowest);
						target.add(categoryKeepHighest);
					}
				});
				categoryDropHighest.setEnabled(dropKeepEnabled && categoryDropHighestEnabled);
				if (!categoryDropHighest.isEnabled()) {
					addDropKeepDisabledToolTip(categoryDropHighest, dropKeepEnabled ? DropKeepUsage.EXCLUSIVE : DropKeepUsage.CATEGORY);
				}
				item.add(categoryDropHighest);

				// drop lowest config
				categoryDropLowest.setOutputMarkupId(true);
				categoryDropLowest.add(new AjaxFormComponentUpdatingBehavior("blur") {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						// if drop lowest is non zero, keep highest is to be unavailable
						// however also need to check the drop highest value here also
						Integer value1 = categoryDropLowest.getModelObject();
						Integer value2 = categoryDropHighest.getModelObject();

						if (value1 == null) {
							value1 = 0;
							categoryDropLowest.setModelValue(new String[] { "0" });
						}
						if (value2 == null) {
							value2 = 0;
							categoryDropHighest.setModelValue(new String[] { "0" });
						}

						// remove tooltips and recalc
						removeDropKeepDisabledToolTip(categoryDropHighest);
						removeDropKeepDisabledToolTip(categoryDropLowest);
						removeDropKeepDisabledToolTip(categoryKeepHighest);

						categoryKeepHighest.setEnabled(true);
						if (value1.intValue() > 0 || value2.intValue() > 0) {
							categoryKeepHighest.setModelValue(new String[] { "0" });
							categoryKeepHighest.setEnabled(false);
							addDropKeepDisabledToolTip(categoryKeepHighest, DropKeepUsage.EXCLUSIVE);
						}
						target.add(categoryDropHighest);
						target.add(categoryDropLowest);
						target.add(categoryKeepHighest);
					}
				});
				categoryDropLowest.setEnabled(dropKeepEnabled && categoryDropLowestEnabled);
				if (!categoryDropLowest.isEnabled()) {
					addDropKeepDisabledToolTip(categoryDropLowest, dropKeepEnabled ? DropKeepUsage.EXCLUSIVE : DropKeepUsage.CATEGORY);
				}
				item.add(categoryDropLowest);

				// keep highest config
				categoryKeepHighest.setOutputMarkupId(true);
				categoryKeepHighest.add(new AjaxFormComponentUpdatingBehavior("blur") {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						// if keep highest is non zero, drop highest AND drop lowest are to be unavailable
						Integer value = categoryKeepHighest.getModelObject();

						if (value == null) {
							value = 0;
							categoryKeepHighest.setModelValue(new String[] { "0" });
						}

						// remove tooltips and recalc
						removeDropKeepDisabledToolTip(categoryDropHighest);
						removeDropKeepDisabledToolTip(categoryDropLowest);
						removeDropKeepDisabledToolTip(categoryKeepHighest);

						categoryDropHighest.setEnabled(true);
						categoryDropLowest.setEnabled(true);
						if (value.intValue() > 0) {

							categoryDropHighest.setModelValue(new String[] { "0" });
							categoryDropHighest.setEnabled(false);
							addDropKeepDisabledToolTip(categoryDropHighest, DropKeepUsage.EXCLUSIVE);

							categoryDropLowest.setModelValue(new String[] { "0" });
							categoryDropLowest.setEnabled(false);
							addDropKeepDisabledToolTip(categoryDropLowest, DropKeepUsage.EXCLUSIVE);
						}
						target.add(categoryDropHighest);
						target.add(categoryDropLowest);
						target.add(categoryKeepHighest);
					}
				});
				categoryKeepHighest.setEnabled(dropKeepEnabled && categoryKeepHighestEnabled);
				if (!categoryKeepHighest.isEnabled()) {
					addDropKeepDisabledToolTip(categoryKeepHighest, dropKeepEnabled ? DropKeepUsage.EXCLUSIVE : DropKeepUsage.CATEGORY);
				}
				item.add(categoryKeepHighest);

				// equal weight
				final CheckBox equalWeight = new CheckBox("equalWeight", new PropertyModel<Boolean>(category, "equalWeight"));
				equalWeight.setOutputMarkupId(true);

				// onchange: remove ability to set drop/keep lowest/highest if different points
				equalWeight.add(new AjaxFormComponentUpdatingBehavior("change") {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						final boolean nodeChecked = equalWeight.getModelObject();
						final boolean catDropKeep = BooleanUtils
								.toBooleanDefaultIfNull(SettingsCategoryPanel.this.categoryDropKeepAvailability.get(category.getId()), true);

						if (!nodeChecked) {
							categoryKeepHighest.setModelValue(new String[] { "0" });
							categoryKeepHighest.setEnabled(!catDropKeep);
							addDropKeepDisabledToolTip(categoryKeepHighest, DropKeepUsage.EXCLUSIVE);

							// disable drop lowest
							categoryDropLowest.setModelValue(new String[] { "0" });
							categoryDropLowest.setEnabled(!catDropKeep);
							addDropKeepDisabledToolTip(categoryDropLowest, DropKeepUsage.EXCLUSIVE);

							// disable drop highest
							categoryDropHighest.setModelValue(new String[] { "0" });
							categoryDropHighest.setEnabled(!catDropKeep);
							addDropKeepDisabledToolTip(categoryDropHighest, DropKeepUsage.EXCLUSIVE);
						}
						else {
							categoryKeepHighest.setEnabled(true);
							categoryDropLowest.setEnabled(true);
							categoryDropHighest.setEnabled(true);

							removeDropKeepDisabledToolTip(categoryDropHighest);
							removeDropKeepDisabledToolTip(categoryDropLowest);
							removeDropKeepDisabledToolTip(categoryKeepHighest);
						}

						target.add(categoryDropHighest);
						target.add(categoryDropLowest);
						target.add(categoryKeepHighest);
					}
				});
				item.add(equalWeight);

				// remove button
				final GbAjaxButton remove = new GbAjaxButton("remove") {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onSubmit(final AjaxRequestTarget target) {

						// remove this category from the model
						final CategoryDefinition current = item.getModelObject();

						SettingsCategoryPanel.this.model.getObject().getGradebookInformation().getCategories().remove(current);
						int categoryIndex = 0;
						for (final CategoryDefinition category : SettingsCategoryPanel.this.model.getObject().getGradebookInformation()
								.getCategories()) {
							category.setCategoryOrder(categoryIndex);
							categoryIndex++;
						}

						// indicate to the listview that its model has changed and to rerender correctly
						lv.modelChanged();
						lv.removeAll();

						target.add(categoriesWrap);

						// update running total
						updateRunningTotal(target, runningTotal, runningTotalMessage);

						// reinitialize any custom behaviour
						target.appendJavaScript(
								"sakai.gradebookng.settings.categories = new GradebookCategorySettings($('#settingsCategories'));");
					}
				};
				remove.setDefaultFormProcessing(false);
				item.add(remove);

				final HiddenField<Integer> categoryOrderField = new HiddenField<Integer>("categoryOrder",
						new PropertyModel<Integer>(category, "categoryOrder"));
				/*
				 * categoryOrderField.add(new AjaxFormComponentUpdatingBehavior("orderupdate.sakai") { private static final long
				 * serialVersionUID = 1L;
				 *
				 * @Override protected void onUpdate(final AjaxRequestTarget target) { Integer categoryOrder =
				 * categoryOrderField.getValue();
				 *
				 * } });
				 */
				item.add(categoryOrderField);
			}

			@Override
			public void renderHead(final IHeaderResponse response) {
				super.renderHead(response);

				final Integer type = SettingsCategoryPanel.this.model.getObject().getGradebookInformation().getCategoryType();
				if (Objects.equals(type, GradingConstants.CATEGORY_TYPE_ONLY_CATEGORY)) {
					response.render(OnDomReadyHeaderItem.forScript("$('.gb-category-weight').hide();"));
					response.render(OnDomReadyHeaderItem.forScript("$('.gb-category-runningtotal').hide();"));
				}

				if (!SettingsCategoryPanel.this.isDropHighest) {
					response.render(OnDomReadyHeaderItem.forScript("$('.gb-category-drophighest').hide();"));
				}
				if (!SettingsCategoryPanel.this.isDropLowest) {
					response.render(OnDomReadyHeaderItem.forScript("$('.gb-category-droplowest').hide();"));
				}
				if (!SettingsCategoryPanel.this.isKeepHighest) {
					response.render(OnDomReadyHeaderItem.forScript("$('.gb-category-keephighest').hide();"));
				}
				if (!SettingsCategoryPanel.this.isEqualWeight) {
					response.render(OnDomReadyHeaderItem.forScript("$('.gb-category-equalweight').hide();"));
				}
			}
		};
		categoriesView.setReuseItems(true);
		categoriesWrap.add(categoriesView);
		categoriesWrap.setOutputMarkupId(true);
		settingsCategoriesPanel.add(categoriesWrap);

		// add category button
		final GbAjaxButton addCategory = new GbAjaxButton("addCategory") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(final AjaxRequestTarget target) {

				// add a new empty category to the model
				SettingsCategoryPanel.this.model.getObject().getGradebookInformation().getCategories().add(stubCategoryDefinition());
				target.add(categoriesWrap);

				// reinitialize any custom behaviour
				target.appendJavaScript("sakai.gradebookng.settings.categories = new GradebookCategorySettings($('#settingsCategories'));");
				// focus the new category input
				target.appendJavaScript("sakai.gradebookng.settings.categories.focusLastRow();");
			}
		};
		addCategory.setDefaultFormProcessing(false);
		categoriesWrap.add(addCategory);

	}

	/**
	 * Create a new category definition stub
	 *
	 * @return CategoryDefinition
	 */
	private CategoryDefinition stubCategoryDefinition() {
		final CategoryDefinition cd = new CategoryDefinition();
		cd.setExtraCredit(false);
		cd.setEqualWeight(false);
		cd.setWeight(Double.valueOf(0));
		cd.setAssignmentList(Collections.<Assignment>emptyList());
		cd.setDropHighest(0);
		cd.setDropLowest(0);
		cd.setKeepHighest(0);

		final GbSettings settings = this.model.getObject();
		cd.setCategoryOrder(settings.getGradebookInformation().getCategories().size());
		return cd;
	}

	/**
	 * Custom converter to flip between percentage for display and double for model object storage Trims off any trailing .0
	 *
	 */
	class PercentConverter implements IConverter<Double> {

		private static final long serialVersionUID = 1L;

		/**
		 * Back to object for the model.
		 *
		 * this is persisted as a fraction between 0 and 1 so for two decimal place precision in the UI we need 4 here.
		 *
		 * String will be a percentage, get it back to a decimal fraction.
		 */
		@Override
		public Double convertToObject(final String value, final Locale locale) throws ConversionException {

			// convert
			Double d;
			try {
				final NumberFormat format = NumberFormat.getInstance(locale);
				final Number number = format.parse(value);
				d = number.doubleValue() / 100;
			} catch (final java.text.ParseException e) {
				throw new ConversionException(e).setResourceKey("settingspage.update.failure.categoryweightnumber");
			}

			// want this truncated to four decimal places, or less
			// format, then parse back into a double
			final DecimalFormat df = new DecimalFormat();
			df.setMinimumFractionDigits(0);
			df.setMaximumFractionDigits(4);
			df.setRoundingMode(RoundingMode.HALF_UP);

			final String s = df.format(d);

			try {
				return df.parse(s).doubleValue();
			} catch (final ParseException e) {
				throw new ConversionException(e).setResourceKey("settingspage.update.failure.categoryweightnumber");
			}

		}

		/**
		 * Convert to percentage for display
		 *
		 * Double will be a decimal fraction between 0 and 1 inclusive.
		 */
		@Override
		public String convertToString(final Double value, final Locale locale) {

			// convert to percentage representation
			final Double percentage = value * 100;

			GradeType gradeType = SettingsCategoryPanel.this.businessService.getGradebookSettings(SettingsCategoryPanel.this.currentGradebookUid, SettingsCategoryPanel.this.currentSiteId).getGradeType();

			return FormatHelper.formatGradeForDisplay(percentage, gradeType);
		}

	}

	/**
	 * Helper to handle the value and style updates of the running total label
	 *
	 * @param runningTotal label component to update
	 * @param runningTotalMessage error message component
	 * @return
	 */
	private void updateRunningTotal(final Component runningTotal, final Component runningTotalMessage) {

		final List<CategoryDefinition> categories = this.model.getObject().getGradebookInformation().getCategories();

		BigDecimal total = BigDecimal.ZERO;
		for (final CategoryDefinition categoryDefinition : categories) {

			Double catWeight = categoryDefinition.getWeight();
			if (catWeight == null) {
				catWeight = 0D;
			}

			BigDecimal weight = BigDecimal.valueOf(catWeight);
			if (weight == null) {
				weight = BigDecimal.ZERO;
			}

			if (!categoryDefinition.getExtraCredit()) {
				total = total.add(weight);
			}
		}

		// if comparison passes, we have '1' as the value
		if (total.compareTo(BigDecimal.ONE) == 0) {
			runningTotal.add(new AttributeModifier("class", "sak-banner-success-inline"));
			runningTotalMessage.setVisible(false);
		} else {
			runningTotal.add(new AttributeModifier("class", "sak-banner-error-inline"));
			runningTotalMessage.setVisible(true);
		}

		runningTotal.setDefaultModel(Model.of(FormatHelper.formatDoubleAsPercentage(total.doubleValue() * 100)));
	}

	/**
	 * Helper to handle the value and style updates of the running total label and add to AJAX target
	 *
	 * @param target AJAX request target
	 * @param runningTotal label component to update
	 * @param runningTotalMessage error message component
	 * @return
	 */
	private void updateRunningTotal(final AjaxRequestTarget target, final Component runningTotal, final Component runningTotalMessage) {
		updateRunningTotal(runningTotal, runningTotalMessage);
		target.add(runningTotal);
		target.add(runningTotalMessage);
	}

	public boolean isExpanded() {
		return this.expanded;
	}

	// to enable inter panel comms
	Radio<Integer> getCategoriesAndWeightingRadio() {
		return this.categoriesAndWeighting;
	}

	// helper to apply the rules for whether the categories and weighting radio should be enabled
	// runs via data from the model
	protected void updateCategoriesAndWeightingRadioState() {
		final GradebookInformation settings = this.model.getObject().getGradebookInformation();

		// if course grade is NOT being displayed, enable categories and weighting
		if (!settings.getCourseGradeDisplayed()) {
			this.categoriesAndWeighting.setEnabled(true);
		} else {
			this.categoriesAndWeighting.setEnabled(false);
		}

		// if points selected, disable categories and weighting
		if (settings.getCourseGradeDisplayed() && settings.getCoursePointsDisplayed()) {
			this.categoriesAndWeighting.setEnabled(false);
		} else {
			this.categoriesAndWeighting.setEnabled(true);
		}

	}

	/**
	 * Helper to add the tooltip when drop/keep settings cause a field to be disabled.
	 *
	 * @param textfield
	 * @param usage determines which message bundle to use for title and aria-label
	 */
	private void addDropKeepDisabledToolTip(final Component textfield, final DropKeepUsage usage) {
		textfield.add(AttributeModifier.replace("title", new ResourceModel(usage.getMessage())));
		textfield.add(AttributeModifier.replace("aria-label", new ResourceModel(usage.getMessage())));
	}

	/**
	 * Helper to remove the tooltip from above
	 *
	 * @param textfield
	 */
	private void removeDropKeepDisabledToolTip(final Component textfield) {
		textfield.add(AttributeModifier.remove("title"));
		textfield.add(AttributeModifier.remove("aria-label"));
	}
}
