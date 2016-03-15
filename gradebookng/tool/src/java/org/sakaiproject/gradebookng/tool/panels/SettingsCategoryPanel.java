package org.sakaiproject.gradebookng.tool.panels;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

public class SettingsCategoryPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	IModel<GbSettings> model;

	boolean isDropHighest = false;
	boolean isDropLowest = false;
	boolean isKeepHighest = false;
	boolean expanded = false;

	public SettingsCategoryPanel(final String id, final IModel<GbSettings> model, final boolean expanded) {
		super(id, model);
		this.model = model;
		this.expanded = expanded;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// get categories, passed in
		final List<CategoryDefinition> categories = this.model.getObject().getGradebookInformation().getCategories();

		// parse the categories and see if we have any drophighest/lowest/keep highest and set the flags for the checkboxes to use
		// also build a map that we can use to add/remove from
		for (final CategoryDefinition category : categories) {

			if (category.getDropHighest() != null && category.getDropHighest() > 0) {
				this.isDropHighest = true;
			}
			if (category.getDrop_lowest() != null && category.getDrop_lowest() > 0) {
				this.isDropLowest = true;
			}
			if (category.getKeepHighest() != null && category.getKeepHighest() > 0) {
				this.isKeepHighest = true;
			}
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
				expanded = true;
			}
		});
		settingsCategoriesPanel.add(new AjaxEventBehavior("hidden.bs.collapse") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
				settingsCategoriesPanel.add(new AttributeModifier("class", "panel-collapse collapse"));
				expanded = false;
			}
		});
		if (expanded) {
			settingsCategoriesPanel.add(new AttributeModifier("class", "panel-collapse collapse in"));
		}
		add(settingsCategoriesPanel);

		// category types
		final RadioGroup<Integer> categoryType = new RadioGroup<>("categoryType",
				new PropertyModel<Integer>(this.model, "gradebookInformation.categoryType"));
		categoryType.add(new Radio<>("none", new Model<>(GbCategoryType.NO_CATEGORY.getValue())));
		categoryType.add(new Radio<>("categoriesOnly", new Model<>(GbCategoryType.ONLY_CATEGORY.getValue())));
		categoryType.add(new Radio<>("categoriesAndWeighting", new Model<>(GbCategoryType.WEIGHTED_CATEGORY.getValue())));
		categoryType.setRequired(true);
		settingsCategoriesPanel.add(categoryType);

		// render category related form fields
		final WebMarkupContainer categoriesWrap = new WebMarkupContainer("categoriesWrap") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				// don't show if 'no categories'
				final GbCategoryType type = GbCategoryType
						.valueOf(SettingsCategoryPanel.this.model.getObject().getGradebookInformation().getCategoryType());
				return (type != GbCategoryType.NO_CATEGORY);
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
						c.setDrop_lowest(0);
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
				SettingsCategoryPanel.this.model.getObject().getGradebookInformation().getCategories()) {

			private static final long serialVersionUID = 1L;

			/*@Override
			public final List<CategoryDefinition> getList() {
				List<CategoryDefinition> categories = super.getList();

				Collections.sort(categories, businessService.getCategoryOrderComparator());
				return categories;
			}*/

			@Override
			protected void populateItem(final ListItem<CategoryDefinition> item) {

				final ListView<CategoryDefinition> lv = this; // reference to self

				final CategoryDefinition category = item.getModelObject();

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

				// drop highest
				final TextField<Integer> categoryDropHighest = new TextField<Integer>("categoryDropHighest",
						new PropertyModel<Integer>(category, "dropHighest"));
				categoryDropHighest.setOutputMarkupId(true);
				categoryDropHighest.add(new AjaxFormComponentUpdatingBehavior("blur") {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
					}
				});
				item.add(categoryDropHighest);

				// drop lowest
				final TextField<Integer> categoryDropLowest = new TextField<Integer>("categoryDropLowest",
						new PropertyModel<Integer>(category, "drop_lowest"));
				categoryDropLowest.setOutputMarkupId(true);
				categoryDropLowest.add(new AjaxFormComponentUpdatingBehavior("blur") {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
					}
				});
				item.add(categoryDropLowest);

				// keep highest
				final TextField<Integer> categoryKeepHighest = new TextField<Integer>("categoryKeepHighest",
						new PropertyModel<Integer>(category, "keepHighest"));
				categoryKeepHighest.setOutputMarkupId(true);
				categoryKeepHighest.add(new AjaxFormComponentUpdatingBehavior("blur") {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
					}
				});
				item.add(categoryKeepHighest);

				// remove button
				final AjaxButton remove = new AjaxButton("remove") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {

						// remove this category from the model
						final CategoryDefinition current = item.getModelObject();

						SettingsCategoryPanel.this.model.getObject().getGradebookInformation().getCategories().remove(current);
						int categoryIndex = 0;
						for (CategoryDefinition category : SettingsCategoryPanel.this.model.getObject().getGradebookInformation().getCategories()) {
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
						target.appendJavaScript("sakai.gradebookng.settings.categories = new GradebookCategorySettings($('#settingsCategories'));");
					}
				};
				remove.setDefaultFormProcessing(false);
				item.add(remove);

				final HiddenField<Integer> categoryOrderField = new HiddenField<Integer>("categoryOrder", new PropertyModel<Integer>(category, "categoryOrder"));
			/*	categoryOrderField.add(new AjaxFormComponentUpdatingBehavior("orderupdate.sakai") {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						Integer categoryOrder = categoryOrderField.getValue();
						
					}
				});*/
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
		final AjaxButton addCategory = new AjaxButton("addCategory") {
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
		cd.setWeight(new Double(0));
		cd.setAssignmentList(Collections.<Assignment> emptyList());

		GbSettings settings = (GbSettings) SettingsCategoryPanel.this.model.getObject();
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
				d = Double.valueOf(value) / 100;
			} catch (final NumberFormatException e) {
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

			// set the decimal precision
			final NumberFormat df = NumberFormat.getInstance();
			df.setMinimumFractionDigits(0);
			df.setMaximumFractionDigits(2);
			df.setRoundingMode(RoundingMode.DOWN);

			// convert to percentage representation
			final Double rval = value * 100;

			return df.format(rval);
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

		final List<CategoryDefinition> categories = SettingsCategoryPanel.this.model.getObject().getGradebookInformation().getCategories();

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
		return expanded;
	}
}
