package org.sakaiproject.gradebookng.tool.panels;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.tool.component.GbCourseGradeLabel;
import org.sakaiproject.gradebookng.tool.component.GbFeedbackPanel;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * Panel for the course grade override window
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public class CourseGradeOverridePanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final ModalWindow window;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	public CourseGradeOverridePanel(final String id, final IModel<String> model, final ModalWindow window) {
		super(id, model);
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final String studentUuid = (String) getDefaultModelObject();

		// get the rest of the data we need
		// TODO some of this could be passed in through the model if it was a map...
		final GbUser studentUser = this.businessService.getUser(studentUuid);
		final String currentUserUuid = this.businessService.getCurrentUser().getId();
		final GbRole currentUserRole = this.businessService.getUserRole();
		final CourseGrade courseGrade = this.businessService.getCourseGrade(studentUuid);
		final Gradebook gradebook = this.businessService.getGradebook();

		// heading
		CourseGradeOverridePanel.this.window.setTitle(
				(new StringResourceModel("heading.coursegrade", null,
						new Object[] { studentUser.getDisplayName(), studentUser.getDisplayId() })).getString());

		// form model
		// we are only dealing with the 'entered grade' so we use this directly
		final Model<String> formModel = new Model<String>(courseGrade.getEnteredGrade());

		// form
		final Form<String> form = new Form<String>("form", formModel);

		form.add(new Label("studentName", studentUser.getDisplayName()));
		form.add(new Label("studentEid", studentUser.getDisplayId()));
		form.add(new Label("points", formatPoints(courseGrade, gradebook)));

		// setup a map of data for the course grade label
		final Map<String, Object> modelData = new HashMap<>();
		modelData.put("currentUserUuid", currentUserUuid);
		modelData.put("currentUserRole", currentUserRole);
		modelData.put("courseGrade", courseGrade);
		modelData.put("gradebook", gradebook);
		modelData.put("showPoints", false);
		modelData.put("showOverride", false);
		form.add(new GbCourseGradeLabel("calculated", Model.ofMap(modelData)));

		form.add(new TextField<String>("overrideGrade", formModel));

		final AjaxButton submit = new AjaxButton("submit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				final String newGrade = (String) form.getModelObject();

				// validate the grade entered is a valid one for the selected grading schema
				// though we allow blank grades so the override is removed
				if (StringUtils.isNotBlank(newGrade)) {
					final GradebookInformation gbInfo = CourseGradeOverridePanel.this.businessService.getGradebookSettings();

					final Map<String, Double> schema = gbInfo.getSelectedGradingScaleBottomPercents();
					if (!schema.containsKey(newGrade)) {
						error(new ResourceModel("message.addcoursegradeoverride.invalid").getObject());
						target.addChildren(form, FeedbackPanel.class);
						return;
					}
				}

				// save
				final boolean success = CourseGradeOverridePanel.this.businessService.updateCourseGrade(studentUuid, newGrade);

				if (success) {
					getSession().info(getString("message.addcoursegradeoverride.success"));
					setResponsePage(getPage().getPageClass());
				} else {
					error(new ResourceModel("message.addcoursegradeoverride.error").getObject());
					target.addChildren(form, FeedbackPanel.class);
				}

			}
		};
		form.add(submit);

		// feedback panel
		form.add(new GbFeedbackPanel("feedback"));

		// cancel button
		final AjaxButton cancel = new AjaxButton("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> f) {
				CourseGradeOverridePanel.this.window.close(target);
			}
		};
		cancel.setDefaultFormProcessing(false);
		form.add(cancel);

		add(form);

		// heading
		add(new Label("heading",
				new StringResourceModel("heading.coursegrade", null,
						new Object[] { studentUser.getDisplayName(), studentUser.getDisplayId() })));

	}

	/**
	 * Helper to format the points display
	 *
	 * @param courseGrade the {@link CourseGrade}
	 * @param gradebook the {@link Gradebook} which has the settings
	 * @return fully formatted string ready for display
	 */
	private String formatPoints(final CourseGrade courseGrade, final Gradebook gradebook) {

		// only display points if not weighted category type
		final GbCategoryType categoryType = GbCategoryType.valueOf(gradebook.getCategory_type());
		if (categoryType != GbCategoryType.WEIGHTED_CATEGORY) {

			final Double pointsEarned = courseGrade.getPointsEarned();
			final Double totalPointsPossible = courseGrade.getTotalPointsPossible();

			if (gradebook.isCoursePointsDisplayed()) {
				return new StringResourceModel("coursegrade.display.points-first", null,
						new Object[] { pointsEarned, totalPointsPossible }).getString();
			} else {
				return new StringResourceModel("coursegrade.display.points-second", null,
						new Object[] { pointsEarned, totalPointsPossible }).getString();
			}
		} else {
			return getString("coursegrade.display.points-none");
		}

	}

}
