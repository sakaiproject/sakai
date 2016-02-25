package org.sakaiproject.gradebookng.tool.panels;

import org.apache.commons.lang.StringUtils;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.util.string.ComponentRenderer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.SortDirection;
import org.sakaiproject.gradebookng.business.model.GbAssignmentGradeSortOrder;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 *
 * Header panel for each assignment column in the UI
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class AssignmentColumnHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final IModel<Assignment> modelData;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	private GradebookNgBusinessService businessService;

	public AssignmentColumnHeaderPanel(final String id, final IModel<Assignment> modelData) {
		super(id);
		this.modelData = modelData;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		getParentCellFor(this).setOutputMarkupId(true);

		final Assignment assignment = this.modelData.getObject();

		// get user's role
		final GbRole role = this.businessService.getUserRole();

		final Link<String> title = new Link<String>("title", Model.of(assignment.getName())) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {

				// toggle the sort direction on each click
				final GradebookPage gradebookPage = (GradebookPage) getPage();
				final GradebookUiSettings settings = gradebookPage.getUiSettings();

				// if null, set a default sort, otherwise toggle, save, refresh.
				if (settings.getAssignmentSortOrder() == null
						|| !assignment.getId().equals(settings.getAssignmentSortOrder().getAssignmentId())) {
					settings.setAssignmentSortOrder(new GbAssignmentGradeSortOrder(assignment.getId(), SortDirection.ASCENDING));
				} else {
					final GbAssignmentGradeSortOrder sortOrder = settings.getAssignmentSortOrder();
					SortDirection direction = sortOrder.getDirection();
					direction = direction.toggle();
					sortOrder.setDirection(direction);
					settings.setAssignmentSortOrder(sortOrder);
				}

				// clear any category sort order to prevent conflicts
				settings.setCategorySortOrder(null);

				// save settings
				gradebookPage.setUiSettings(settings);

				// refresh
				setResponsePage(new GradebookPage());
			}

		};
		title.add(new AttributeModifier("title", assignment.getName()));
		title.add(new Label("label", assignment.getName()));

		// set the class based on the sortOrder. May not be set for this assignment so match it
		final GradebookPage gradebookPage = (GradebookPage) getPage();
		final GradebookUiSettings settings = gradebookPage.getUiSettings();
		if (settings != null && settings.getAssignmentSortOrder() != null
				&& settings.getAssignmentSortOrder().getAssignmentId() == assignment.getId()) {
			title.add(
					new AttributeModifier("class", "gb-sort-" + settings.getAssignmentSortOrder().getDirection().toString().toLowerCase()));
		}

		add(title);

		add(new Label("totalPoints", Model.of(assignment.getPoints())));
		add(new Label("dueDate", Model.of(FormatHelper.formatDate(assignment.getDueDate(), getString("label.noduedate")))));

		final WebMarkupContainer externalAppFlag = gradebookPage.buildFlagWithPopover("externalAppFlag", "");
		if (assignment.getExternalAppName() == null) {
			externalAppFlag.setVisible(false);
		} else {
			externalAppFlag.setVisible(true);
			externalAppFlag.add(new AttributeModifier("data-content",
					gradebookPage.generatePopoverContent(new StringResourceModel("label.gradeitem.externalapplabel",
							null, new Object[] { assignment.getExternalAppName() }).getString())));
			String iconClass = "icon-sakai";
			if ("Assignments".equals(assignment.getExternalAppName())) {
				iconClass = "icon-sakai-assignment-grades";
			} else if ("Tests & Quizzes".equals(assignment.getExternalAppName())) {
				iconClass = "icon-sakai-samigo";
			} else if ("Lesson Builder".equals(assignment.getExternalAppName())) {
				iconClass = "icon-sakai-lessonbuildertool";
			}
			externalAppFlag
					.add(new AttributeModifier("class", "gb-external-app-flag " + iconClass));
		}
		add(externalAppFlag);

		add(gradebookPage.buildFlagWithPopover("extraCreditFlag", generateFlagPopover(HeaderFlagPopoverPanel.Flag.GRADE_ITEM_EXTRA_CREDIT))
				.setVisible(assignment.isExtraCredit()));
		add(gradebookPage.buildFlagWithPopover("isCountedFlag", generateFlagPopover(HeaderFlagPopoverPanel.Flag.GRADE_ITEM_COUNTED)).setVisible(assignment.isCounted()));
		add(gradebookPage.buildFlagWithPopover("notCountedFlag", generateFlagPopover(HeaderFlagPopoverPanel.Flag.GRADE_ITEM_NOT_COUNTED))
				.setVisible(!assignment.isCounted()));
		add(gradebookPage.buildFlagWithPopover("isReleasedFlag", generateFlagPopover(HeaderFlagPopoverPanel.Flag.GRADE_ITEM_RELEASED))
				.setVisible(assignment.isReleased()));
		add(gradebookPage.buildFlagWithPopover("notReleasedFlag", generateFlagPopover(HeaderFlagPopoverPanel.Flag.GRADE_ITEM_NOT_RELEASED))
				.setVisible(!assignment.isReleased()));

		add(new AttributeModifier("data-assignmentId", assignment.getId()));
		add(new AttributeModifier("data-category", assignment.getCategoryName()));
		add(new AttributeModifier("data-sort-order", assignment.getSortOrder()));
		add(new AttributeModifier("data-categorized-sort-order", assignment.getCategorizedSortOrder()));
		if (GbCategoryType.WEIGHTED_CATEGORY.equals(this.businessService.getGradebookCategoryType()) && assignment.getWeight() != null) {
			add(new AttributeModifier("data-category-weight", String.format("%s%%", Math.round(assignment.getWeight() * 100))));
		}
		add(new AttributeModifier("data-category-extra-credit", assignment.isCategoryExtraCredit()));
		add(new AttributeModifier("data-category-order", assignment.getCategoryOrder()));

		// menu
		final WebMarkupContainer menu = new WebMarkupContainer("menu") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				if (role != GbRole.INSTRUCTOR) {
					return false;
				}
				return true;
			}
		};

		menu.add(new AjaxLink<Long>("editAssignmentDetails", Model.of(assignment.getId())) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GradebookPage gradebookPage = (GradebookPage) getPage();
				final GbModalWindow window = gradebookPage.getAddOrEditGradeItemWindow();
				window.setTitle(getString("heading.editgradeitem"));
				window.setComponentToReturnFocusTo(getParentCellFor(this));
				window.setContent(new AddOrEditGradeItemPanel(window.getContentId(), window, getModel()));
				window.showUnloadConfirmation(false);
				window.show(target);
			}

		});

		menu.add(new AjaxLink<Long>("viewAssignmentGradeStatistics", Model.of(assignment.getId())) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GradebookPage gradebookPage = (GradebookPage) getPage();
				final GbModalWindow window = gradebookPage.getGradeStatisticsWindow();
				window.setComponentToReturnFocusTo(getParentCellFor(this));
				window.setContent(new GradeStatisticsPanel(window.getContentId(), getModel(), window));
				window.show(target);
			}
		});

		menu.add(new Link<Long>("moveAssignmentLeft", Model.of(assignment.getId())) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				// given the id, get the assignment, get the sort order, then update and refresh
				// note that we cannot use the passed in assignment sort order in here
				// as we may have had an async reorder on the front end but not had the model data updated,
				// so we just make sure we get it fresh

				final Long assignmentId = getModelObject();

				final GradebookPage gradebookPage = (GradebookPage) getPage();
				GradebookUiSettings settings = gradebookPage.getUiSettings();

				if (settings == null) {
					settings = new GradebookUiSettings();
					gradebookPage.setUiSettings(settings);
				}

				if (settings.isCategoriesEnabled()) {
					try {
						Integer order = calculateCurrentCategorizedSortOrder(assignmentId);
						AssignmentColumnHeaderPanel.this.businessService.updateAssignmentCategorizedOrder(assignmentId, (order.intValue() - 1));
					} catch (final Exception e) {
						e.printStackTrace();
						error("error reordering within category");
					}
				} else {
					final int order = AssignmentColumnHeaderPanel.this.businessService.getAssignmentSortOrder(assignmentId.longValue());
					AssignmentColumnHeaderPanel.this.businessService.updateAssignmentOrder(assignmentId.longValue(), (order - 1));
				}

				setResponsePage(new GradebookPage());
			}
		});

		menu.add(new Link<Long>("moveAssignmentRight", Model.of(assignment.getId())) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {

				final Long assignmentId = getModelObject();

				final GradebookPage gradebookPage = (GradebookPage) getPage();
				GradebookUiSettings settings = gradebookPage.getUiSettings();

				if (settings == null) {
					settings = new GradebookUiSettings();
					gradebookPage.setUiSettings(settings);
				}

				if (settings.isCategoriesEnabled()) {
					try {
						Integer order = calculateCurrentCategorizedSortOrder(assignmentId);
						AssignmentColumnHeaderPanel.this.businessService.updateAssignmentCategorizedOrder(assignmentId, (order.intValue() + 1));
					} catch (final Exception e) {
						e.printStackTrace();
						error("error reordering within category");
					}
				} else {
					final int order = AssignmentColumnHeaderPanel.this.businessService.getAssignmentSortOrder(assignmentId.longValue());
					AssignmentColumnHeaderPanel.this.businessService.updateAssignmentOrder(assignmentId.longValue(), (order + 1));
				}

				setResponsePage(new GradebookPage());
			}
		});

		menu.add(new AjaxLink<Long>("hideAssignment", Model.of(assignment.getId())) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final long assignmentId = getModelObject();
				target.appendJavaScript("sakai.gradebookng.spreadsheet.hideGradeItemAndSyncToolbar('" + assignmentId + "');");
			}
		});

		menu.add(new AjaxLink<Long>("setUngraded", Model.of(assignment.getId())) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {

				final GradebookPage gradebookPage = (GradebookPage) getPage();
				final GbModalWindow window = gradebookPage.getUpdateUngradedItemsWindow();
				final UpdateUngradedItemsPanel panel = new UpdateUngradedItemsPanel(window.getContentId(), getModel(), window);
				window.setTitle(getString("heading.updateungradeditems"));
				window.setComponentToReturnFocusTo(getParentCellFor(this));
				window.setContent(panel);
				window.showUnloadConfirmation(false);
				window.show(target);

				panel.setOutputMarkupId(true);
				target.appendJavaScript("new GradebookUpdateUngraded($(\"#" + panel.getMarkupId() + "\"));");
			}

			@Override
			public boolean isVisible() {
				if (assignment.isExternallyMaintained()) {
					return false;
				}
				return true;
			}

		});

		// delete item
		menu.add(new AjaxLink<Long>("deleteGradeItem", Model.of(assignment.getId())) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {

				final GradebookPage gradebookPage = (GradebookPage) getPage();
				final GbModalWindow window = gradebookPage.getDeleteItemWindow();
				final DeleteItemPanel panel = new DeleteItemPanel(window.getContentId(), getModel(), window);
				window.setTitle(getString("delete.label"));
				window.setComponentToReturnFocusTo(getParentCellFor(this));
				window.setContent(panel);
				window.showUnloadConfirmation(false);
				window.show(target);
			}

			@Override
			public boolean isVisible() {
				if (assignment.isExternallyMaintained()) {
					return false;
				}
				return true;
			}
		});

		add(menu);

	}


	private Component getParentCellFor(final Component component) {
		if (StringUtils.equals(component.getMarkupAttributes().getString("wicket:id"), "header")) {
			return component;
		} else {
			return getParentCellFor(component.getParent());
		}
	}


	private String generateFlagPopover(HeaderFlagPopoverPanel.Flag flag) {
		return new HeaderFlagPopoverPanel("popover", flag, this.modelData.getObject().getId()).toPopoverString();
	}


	/**
	 * Get the assignment's current sort index within it's category.
	 * If this value is null in the database, best calculate this index
	 * from the assignments.
	 * @param assignmentId the id of the assignment
	 * @return the current sort index of the assignment within their category
	 */
	private Integer calculateCurrentCategorizedSortOrder(final Long assignmentId) {
		final Assignment assignment = AssignmentColumnHeaderPanel.this.businessService.getAssignment(assignmentId.longValue());
		Integer order = assignment.getCategorizedSortOrder();

		if (order == null) {
			// if no categorized order for assignment, calculate one based on the default sort order
			List<Assignment> assignments = AssignmentColumnHeaderPanel.this.businessService.getGradebookAssignments();
			List<Long> assignmentIdsInCategory = assignments.stream()
					.filter(a -> a.getCategoryId() == assignment.getCategoryId())
					.map(Assignment::getId)
					.collect(Collectors.toList());

			order = assignmentIdsInCategory.indexOf(assignmentId);
		}

		return order;
	}
}
