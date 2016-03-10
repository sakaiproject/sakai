package org.sakaiproject.gradebookng.tool.panels;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * The panel for the add grade item window
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class AddOrEditGradeItemPanelContent extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	private GradebookNgBusinessService businessService;

	private AjaxCheckBox counted;
	private AjaxCheckBox released;

	public AddOrEditGradeItemPanelContent(final String id, final Model<Assignment> assignmentModel) {
		super(id, assignmentModel);

		final Gradebook gradebook = this.businessService.getGradebook();

		final Assignment assignment = assignmentModel.getObject();

		boolean categoriesEnabled = true;
		if (gradebook.getCategory_type() == GbCategoryType.NO_CATEGORY.getValue()) {
			categoriesEnabled = false;
		}

		// title
		final TextField<String> title = new TextField<String>("title", new PropertyModel<String>(assignmentModel, "name")) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled() {
				return !assignment.isExternallyMaintained();
			}
		};
		add(title);

		// points
		final TextField<Double> points = new TextField<Double>("points", new PropertyModel<Double>(assignmentModel, "points")) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled() {
				return !assignment.isExternallyMaintained();
			}
		};
		add(points);

		// due date
		// TODO date format needs to come from i18n
		final DateTextField dueDate = new DateTextField("duedate", new PropertyModel<Date>(assignmentModel, "dueDate"), "MM/dd/yyyy") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled() {
				return !assignment.isExternallyMaintained();
			}
		};
		add(dueDate);

		// category
		final List<CategoryDefinition> categories = new ArrayList<>();
		final Map<Long, CategoryDefinition> categoryMap = new LinkedHashMap<>();

		if (categoriesEnabled) {
			categories.addAll(this.businessService.getGradebookCategories());

			for (final CategoryDefinition category : categories) {
				categoryMap.put(category.getId(), category);
			}
		}

		final DropDownChoice<Long> categoryDropDown = new DropDownChoice<Long>("category",
				new PropertyModel<Long>(assignmentModel, "categoryId"), new ArrayList<Long>(categoryMap.keySet()),
				new IChoiceRenderer<Long>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(final Long value) {
						final CategoryDefinition category = categoryMap.get(value);
						if (GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY == gradebook.getCategory_type()) {
							final String weight = FormatHelper.formatDoubleAsPercentage(category.getWeight() * 100);
							return MessageFormat.format(getString("label.addgradeitem.categorywithweight"), category.getName(), weight);
						} else {
							return category.getName();
						}
					}

					@Override
					public String getIdValue(final Long object, final int index) {
						return object.toString();
					}

				});

		// if we don't have a category assigned we want the 'Choose One' message. setNullValid = false
		// if we have a category we want to be able to clear it. setNullValid = true
		categoryDropDown.setNullValid(false);
		if (assignment.getCategoryId() != null) {
			categoryDropDown.setNullValid(true);
		}
		categoryDropDown.setVisible(!categories.isEmpty());
		add(categoryDropDown);

		add(new WebMarkupContainer("noCategoriesMessage") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return categories.isEmpty();
			}
		});

		// extra credit
		// if an extra credit category is selected, this will be unchecked and disabled
		final AjaxCheckBox extraCredit = new AjaxCheckBox("extraCredit", new PropertyModel<Boolean>(assignmentModel, "extraCredit")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// nothing required
			}
		};
		extraCredit.setOutputMarkupId(true);
		extraCredit.setEnabled(!assignment.isCategoryExtraCredit());
		add(extraCredit);

		// released
		this.released = new AjaxCheckBox("released", new PropertyModel<Boolean>(assignmentModel, "released")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				if (!getModelObject()) {
					AddOrEditGradeItemPanelContent.this.counted.setModelObject(false);
					target.add(AddOrEditGradeItemPanelContent.this.counted);
				}
			}
		};
		this.released.setOutputMarkupId(true);
		add(this.released);

		// counted
		// if checked, release must also be checked and then disabled
		this.counted = new AjaxCheckBox("counted", new PropertyModel<Boolean>(assignmentModel, "counted")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				if (getModelObject()) {
					AddOrEditGradeItemPanelContent.this.released.setModelObject(true);
				}
				target.add(AddOrEditGradeItemPanelContent.this.released);
			}
		};

		if (this.businessService.categoriesAreEnabled()) {
			this.counted.setEnabled(assignment.getCategoryId() != null);
		}

		add(this.counted);

		// behaviour for when a category is chosen. If the category is extra credit, deselect and disable extra credit checkbox
		categoryDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {

				final Long selected = categoryDropDown.getModelObject();

				// if extra credit, deselect and disable the 'extraCredit' checkbox
				final CategoryDefinition category = categoryMap.get(selected);

				if (category != null && category.isExtraCredit()) {
					extraCredit.setModelObject(false);
					extraCredit.setEnabled(false);
				} else {
					extraCredit.setEnabled(true);
				}
				target.add(extraCredit);

				if (AddOrEditGradeItemPanelContent.this.businessService.categoriesAreEnabled()) {
					if (category == null) {
						AddOrEditGradeItemPanelContent.this.counted.setEnabled(false);
						AddOrEditGradeItemPanelContent.this.counted.setModelObject(false);
					} else {
						AddOrEditGradeItemPanelContent.this.counted.setEnabled(true);
						AddOrEditGradeItemPanelContent.this.counted.setModelObject(true);
						AddOrEditGradeItemPanelContent.this.released.setModelObject(true);
					}

					target.add(AddOrEditGradeItemPanelContent.this.counted);
					target.add(AddOrEditGradeItemPanelContent.this.released);
				}
			}
		});

	}
}
