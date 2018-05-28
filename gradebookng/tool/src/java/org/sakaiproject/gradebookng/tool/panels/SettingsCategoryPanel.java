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
import java.util.Set;

import org.apache.commons.lang.BooleanUtils;
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
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.gradebookng.tool.pages.SettingsPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;

public class SettingsCategoryPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	IModel<GbSettings> model;

	boolean isDropHighest = false;
	boolean isDropLowest = false;
	boolean isKeepHighest = false;
	boolean expanded = false;

	Radio<Integer> categoriesAndWeighting;

	Map<Long, Boolean> categoryDropKeepAvailability = new HashMap<>();

	public SettingsCategoryPanel(final String id, final IModel<GbSettings> model, final boolean expanded) {
		super(id, model);
		this.model = model;
		this.expanded = expanded;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final SettingsPage settingsPage = (SettingsPage) getPage();

		// get categories, passed in
		final List<CategoryDefinition> categories = this.model.getObject().getGradebookInformation().getCategories();

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

			// check points
			final Set<Double> points = new HashSet<>();
			final List<Assignment> assignments = category.getAssignmentList();
			assignments.forEach(a -> points.add(a.getPoints()));

			this.categoryDropKeepAvailability.put(category.getId(), (points.size() <= 1));

		}

		// if categories enabled but we don't have any yet, add a default one
		if (this.model.getObject().getGradebookInformation().getCategoryType() != GbCategoryType.NO_CATEGORY.getValue()
				&& categories.isEmpty()) {
			this.model.getObject().getGradebookInformation().getCategories().add(stubCategoryDefinition());
		}

		final WebMarkupContainer settingsCategoriesPanel = new WebMarkupContainer("settingsCategoriesPanel");
		// Preserve the expand/collapse state of the panel
		settingsCategoriesPanel.add(new AjaxEventBehavior("shown.bs.collapse") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
				settingsCategoriesPanel.add(new AttributeModifier("class", "panel-collapse collapse in"));
				SettingsCategoryPanel.this.expanded = true;
			}
		});
		settingsCategoriesPanel.add(new AjaxEventBehavior("hidden.bs.collapse") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
				settingsCategoriesPanel.add(new AttributeModifier("class", "panel-collapse collapse"));
				SettingsCategoryPanel.this.expanded = false;
			}
		});
		if (this.expanded) {
			settingsCategoriesPanel.add(new AttributeModifier("class", "panel-collapse collapse in"));
		}
		add(settingsCategoriesPanel);

		// category types (note categoriesAndWeighting treated differently due to inter panel updates)
		final RadioGroup<Integer> categoryType = new RadioGroup<>("categoryType",
				new PropertyModel<Integer>(this.model, "gradebookInformation.categoryType"));
		final Radio<Integer> none = new Radio<>("none", new Model<>(GbCategoryType.NO_CATEGORY.getValue()));
		final Radio<Integer> categoriesOnly = new Radio<>("categoriesOnly", new Model<>(GbCategoryType.ONLY_CATEGORY.getValue()));
		this.categoriesAndWeighting = new Radio<>("categoriesAndWeighting",
				new Model<>(GbCategoryType.WEIGHTED_CATEGORY.getValue()));

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
				final GradebookInformation settings = SettingsCategoryPanel.this.model.getObject().getGradebookInformation();
				return (GbCategoryType.valueOf(settings.getCategoryType()) != GbCategoryType.NO_CATEGORY);
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
					for (final CategoryDefinition c : SettingsCategoryPanel.this.model.getObject().getGradebookInformation()
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
					for (final CategoryDefinition c : SettingsCategoryPanel.this.model.getObject().getGradebookInformation()
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
					for (final CategoryDefinition c : SettingsCategoryPanel.this.model.getObject().getGradebookInformation()
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

		// add the options wrapper
		categoriesOptionsWrap.setOutputMarkupPlaceholderTag(true);
		categoriesWrap.add(categoriesOptionsWrap);

		// When category type changes, ensure form is updated to reflect new value
		categoryType.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {

				// adjust visibility of items depending on category type
				final GbCategoryType type = GbCategoryType
						.valueOf(SettingsCategoryPanel.this.model.getObject().getGradebookInformation().getCategoryType());

				categoriesWrap.setVisible(type != GbCategoryType.NO_CATEGORY);
				categoriesOptionsWrap.setVisible(type != GbCategoryType.NO_CATEGORY);

				// if categories only (2), the categories table will be visible but the weighting column and tally will not
				if (type == GbCategoryType.ONLY_CATEGORY) {
					target.appendJavaScript("$('.gb-category-weight').hide();");
					target.appendJavaScript("$('.gb-category-runningtotal').hide();");
				}

				// switching to categories but we don't have any, add a default one
				if (type != GbCategoryType.NO_CATEGORY && categories.isEmpty()) {
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
				name.add(new AjaxFormComponentUpdatingBehavior("blur") {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
					}
				});
				item.add(name);

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
				final Label numItems = new Label("numItems", new StringResourceModel("settingspage.categories.items", null,
						new Object[] { category.getAssignmentList().size() }));
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
							addDropKeepDisabledToolTip(categoryKeepHighest);
						}
						target.add(categoryDropHighest);
						target.add(categoryDropLowest);
						target.add(categoryKeepHighest);
					}
				});
				categoryDropHighest.setEnabled(dropKeepEnabled && categoryDropHighestEnabled);
				if (!categoryDropHighest.isEnabled()) {
					addDropKeepDisabledToolTip(categoryDropHighest);
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
							addDropKeepDisabledToolTip(categoryKeepHighest);
						}
						target.add(categoryDropHighest);
						target.add(categoryDropLowest);
						target.add(categoryKeepHighest);
					}
				});
				categoryDropLowest.setEnabled(dropKeepEnabled && categoryDropLowestEnabled);
				if (!categoryDropLowest.isEnabled()) {
					addDropKeepDisabledToolTip(categoryDropLowest);
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
							addDropKeepDisabledToolTip(categoryDropHighest);

							categoryDropLowest.setModelValue(new String[] { "0" });
							categoryDropLowest.setEnabled(false);
							addDropKeepDisabledToolTip(categoryDropLowest);
						}
						target.add(categoryDropHighest);
						target.add(categoryDropLowest);
						target.add(categoryKeepHighest);
					}
				});
				categoryKeepHighest.setEnabled(dropKeepEnabled && categoryKeepHighestEnabled);
				if (!categoryKeepHighest.isEnabled()) {
					addDropKeepDisabledToolTip(categoryKeepHighest);
				}
				item.add(categoryKeepHighest);

				// remove button
				final GbAjaxButton remove = new GbAjaxButton("remove") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {

						// remove this category from the model
						final CategoryDefinition current = item.getModelObject();

						SettingsCategoryPanel.this.model.getObject().getGradebookInformation().getCategories().remove(current);
						int categoryIndex = 0;
						for (final CategoryDefinition category : SettingsCategoryPanel.this.model.getObject().getGradebookInformation()
								.getCategories()) {
							category.setCategoryOrder(Integer.valueOf(categoryIndex));
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

				final GbCategoryType type = GbCategoryType
						.valueOf(SettingsCategoryPanel.this.model.getObject().getGradebookInformation().getCategoryType());
				if (type == GbCategoryType.ONLY_CATEGORY) {
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
			protected void onSubmit(final AjaxRequestTarget target, final Form<?> f) {

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

			return FormatHelper.formatGradeForDisplay(percentage);
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

			if (!categoryDefinition.isExtraCredit()) {
				total = total.add(weight);
			}
		}

		// if comparison passes, we have '1' as the value
		if (total.compareTo(BigDecimal.ONE) == 0) {
			runningTotal.add(new AttributeModifier("class", "text-success"));
			runningTotalMessage.setVisible(false);
		} else {
			runningTotal.add(new AttributeModifier("class", "text-danger"));
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
		if (!settings.isCourseGradeDisplayed()) {
			this.categoriesAndWeighting.setEnabled(true);
		} else {
			this.categoriesAndWeighting.setEnabled(false);
		}

		// if points selected, disable categories and weighting
		if (settings.isCourseGradeDisplayed() && settings.isCoursePointsDisplayed()) {
			this.categoriesAndWeighting.setEnabled(false);
		} else {
			this.categoriesAndWeighting.setEnabled(true);
		}

	}

	/**
	 * Helper to add the tooltip when drop/keep settings cause a field to be disabled.
	 * 
	 * @param textfield
	 */
	private void addDropKeepDisabledToolTip(final Component textfield) {
		textfield.add(AttributeModifier.replace("title", new ResourceModel("settingspage.categories.hover.dropkeepusage")));
		textfield.add(AttributeModifier.replace("aria-label", new ResourceModel("settingspage.categories.hover.dropkeepusage")));
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
