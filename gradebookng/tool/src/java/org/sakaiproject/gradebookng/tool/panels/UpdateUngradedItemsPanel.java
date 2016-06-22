package org.sakaiproject.gradebookng.tool.panels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbGradingType;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.gradebookng.tool.component.GbFeedbackPanel;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.service.gradebook.shared.GraderPermission;
import org.sakaiproject.service.gradebook.shared.PermissionDefinition;

/**
 *
 * Panel for the modal window that allows an instructor to update the ungraded items
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class UpdateUngradedItemsPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	private final ModalWindow window;

	private final IModel<Long> model;

	private static final double DEFAULT_GRADE = 0;

	public UpdateUngradedItemsPanel(final String id, final IModel<Long> model, final ModalWindow window) {
		super(id);
		this.model = model;
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final Long assignmentId = this.model.getObject();

		final Assignment assignment = this.businessService.getAssignment(assignmentId);

		final GbGradingType gradingType = GbGradingType.valueOf(this.businessService.getGradebook().getGrade_type());

		// form model
		final GradeOverride override = new GradeOverride();
		override.setGrade(String.valueOf(DEFAULT_GRADE));
		final CompoundPropertyModel<GradeOverride> formModel = new CompoundPropertyModel<GradeOverride>(override);

		// build form
		// modal window forms must be submitted via AJAX so we do not specify an onSubmit here
		final Form<GradeOverride> form = new Form<GradeOverride>("form", formModel);

		final GbAjaxButton submit = new GbAjaxButton("submit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {

				final GradeOverride override = (GradeOverride) form.getModelObject();

				final Assignment assignment = UpdateUngradedItemsPanel.this.businessService.getAssignment(assignmentId);

				try {
					final Double overrideValue = Double.valueOf(override.getGrade());
					final GbGroup group = override.getGroup();

					if (isExtraCredit(overrideValue, assignment, gradingType)) {
						target.addChildren(form, FeedbackPanel.class);
					}

					final boolean success = UpdateUngradedItemsPanel.this.businessService.updateUngradedItems(assignmentId, overrideValue, group);

					if (success) {
						UpdateUngradedItemsPanel.this.window.close(target);
						setResponsePage(new GradebookPage());
					} else {
						// InvalidGradeException
						error(getString("grade.notifications.invalid"));
						target.addChildren(form, FeedbackPanel.class);
						target.appendJavaScript("new GradebookUpdateUngraded($(\"#" + getParent().getMarkupId() + "\"));");
					}
				} catch (final NumberFormatException e) {
					// InvalidGradeException
					error(getString("grade.notifications.invalid"));
					target.addChildren(form, FeedbackPanel.class);
					target.appendJavaScript("new GradebookUpdateUngraded($(\"#" + getParent().getMarkupId() + "\"));");
				}
			}
		};
		form.add(submit);

		final GbAjaxButton cancel = new GbAjaxButton("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				UpdateUngradedItemsPanel.this.window.close(target);
			}
		};
		cancel.setDefaultFormProcessing(false);
		form.add(cancel);

		form.add(new TextField<Double>("grade").setRequired(true));

		if (GbGradingType.PERCENTAGE.equals(gradingType)) {
			form.add(new Label("points", getString("label.percentage.plain")));
		} else {
			form.add(new Label("points",
				new StringResourceModel("label.studentsummary.outof", null,
					new Object[] { assignment.getPoints() })));
		}

		final WebMarkupContainer hiddenGradePoints = new WebMarkupContainer("gradePoints");
		if (GbGradingType.PERCENTAGE.equals(gradingType)) {
			hiddenGradePoints.add(new AttributeModifier("value", 100));
		} else {
			hiddenGradePoints.add(new AttributeModifier("value", assignment.getPoints()));
		}
		form.add(hiddenGradePoints);

		final List<GbGroup> groups = this.businessService.getSiteSectionsAndGroups();
		groups.add(0, new GbGroup(null, getString("groups.all"), null, GbGroup.Type.ALL));

		if (this.businessService.getUserRole() == GbRole.TA) {
			boolean categoriesEnabled = this.businessService.categoriesAreEnabled();
			List<PermissionDefinition> permissions = this.businessService.getPermissionsForUser(
				this.businessService.getCurrentUser().getId());

			List<String> gradableGroupIds = new ArrayList<>();
			boolean canGradeAllGroups = false;

			for (PermissionDefinition permission : permissions) {
				if (permission.getFunction().equals(GraderPermission.GRADE.toString())) {
					if (categoriesEnabled && permission.getCategoryId() != null) {
						if (permission.getCategoryId().equals(assignment.getCategoryId())) {
							if (permission.getGroupReference() == null) {
								canGradeAllGroups = true;
								break;
							} else {
								gradableGroupIds.add(permission.getGroupReference());
							}
						}
					} else if (!categoriesEnabled && permission.getGroupReference() == null) {
						canGradeAllGroups = true;
						break;
					} else {
						gradableGroupIds.add(permission.getGroupReference());
					}
				}
			}
			if (!canGradeAllGroups) {
				// remove the ones that the user can't view
				final Iterator<GbGroup> iter = groups.iterator();
				while (iter.hasNext()) {
					final GbGroup group = iter.next();
					if (!gradableGroupIds.contains(group.getReference())) {
						iter.remove();
					}
				}
			}
		}

		final GradebookUiSettings settings = ((GradebookPage) getPage()).getUiSettings();

		final DropDownChoice<GbGroup> groupAndSectionFilter = new DropDownChoice<GbGroup>(
			"group",
			new PropertyModel<GbGroup>(override, "group"),
			groups,
			new ChoiceRenderer<GbGroup>() {
				private static final long serialVersionUID = 1L;

				@Override
				public Object getDisplayValue(final GbGroup g) {
					return g.getTitle();
				}

				@Override
				public String getIdValue(final GbGroup g, final int index) {
					return g.getId();
				}
			});

		groupAndSectionFilter.setNullValid(false);
		if (!groups.isEmpty()) {
			groupAndSectionFilter.setModelObject(
				(settings.getGroupFilter() != null) ? settings.getGroupFilter() : groups.get(0));
		}
		form.add(groupAndSectionFilter);

		add(form);

		// feedback panel
		form.add(new GbFeedbackPanel("updateGradeFeedback"));

		// confirmation dialog
		add(new Label("confirmationMessage",
			new StringResourceModel(
				"label.updateungradeditems.confirmation.general", null,
				new Object[]{"${score}", "${group}"})).setEscapeModelStrings(false));
	}

	private boolean isExtraCredit(Double grade, Assignment assignment, GbGradingType gradingType) {
		return (GbGradingType.PERCENTAGE.equals(gradingType) && grade > 100) ||
			(GbGradingType.POINTS.equals(gradingType) && grade > assignment.getPoints());
	}

	/**
	 * Model for this form
	 */
	class GradeOverride implements Serializable {

		private static final long serialVersionUID = 1L;

		@Getter
		@Setter
		private String grade;

		@Getter
		@Setter
		private GbGroup group;
	}

}
