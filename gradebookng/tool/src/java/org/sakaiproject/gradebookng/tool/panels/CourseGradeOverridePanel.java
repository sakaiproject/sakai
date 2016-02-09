package org.sakaiproject.gradebookng.tool.panels;

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
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.component.GbFeedbackPanel;
import org.sakaiproject.service.gradebook.shared.CourseGrade;

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

		// get user
		final GbUser user = this.businessService.getUser(studentUuid);

		// get the current course grade for the student
		final CourseGrade courseGrade = this.businessService.getCourseGrade(studentUuid);

		// form model
		// we are only dealing with the 'entered grade' so we use this directly
		final Model<String> formModel = new Model<String>(courseGrade.getEnteredGrade());

		// form
		final Form<String> form = new Form<String>("form", formModel);

		form.add(new Label("studentName", user.getDisplayName()));
		form.add(new Label("studentEid", user.getDisplayId()));
		form.add(new Label("points", "123"));
		form.add(new Label("calculated", formatCalculatedGrade(courseGrade)));
		form.add(new TextField<String>("overrideGrade", formModel));

		final AjaxButton submit = new AjaxButton("submit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				final String newGrade = (String) form.getModelObject();

				final boolean success = CourseGradeOverridePanel.this.businessService.updateCourseGrade(studentUuid, newGrade);

				if (success) {
					getSession().info(getString("message.addcoursegradeoverride.success"));
					setResponsePage(getPage().getPageClass());
				} else {
					error(new ResourceModel("message.addcoursegradeoverride.error").getObject());
					target.addChildren(form, FeedbackPanel.class);
				}

				// TODO validate the grade is a valid one for the grading scheme

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
				new StringResourceModel("heading.coursegrade", null, new Object[] { user.getDisplayName(), user.getDisplayId() })));

	}

	/**
	 * Format the course grade for display
	 *
	 * @param courseGrade
	 * @return
	 */
	private String formatCalculatedGrade(final CourseGrade courseGrade) {

		final String mappedGrade = courseGrade.getMappedGrade();
		final String calculatedGrade = FormatHelper.formatStringAsPercentage(courseGrade.getCalculatedGrade());

		String rval;

		if (StringUtils.isBlank(mappedGrade) && StringUtils.isBlank(calculatedGrade)) {
			rval = getString("coursegrade.override.calculated.format.none");
		} else if (StringUtils.isBlank(mappedGrade)) {
			rval = new StringResourceModel("coursegrade.override.calculated.format.one", null,
					new Object[] { calculatedGrade }).getString();
		} else if (StringUtils.isBlank(calculatedGrade)) {
			rval = new StringResourceModel("coursegrade.override.calculated.format.one", null,
					new Object[] { mappedGrade }).getString();
		} else {
			rval = new StringResourceModel("coursegrade.override.calculated.format.both", null,
					new Object[] { mappedGrade, calculatedGrade }).getString();
		}

		return rval;
	}

}
