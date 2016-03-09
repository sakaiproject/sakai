package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;

import java.util.List;

/**
 *
 * Panel for the modal window that allows an instructor to update the Course Grade Display settings
 *
 */
public class UpdateCourseGradeDisplayPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	private final ModalWindow window;

	public UpdateCourseGradeDisplayPanel(final String id, final ModalWindow window) {
		super(id);
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// set window title
		UpdateCourseGradeDisplayPanel.this.window.setTitle(getString("heading.updatecoursegradedisplay"));

		// get settings data
		final GradebookInformation settings = this.businessService.getGradebookSettings();

		final GbSettings gbSettings = new GbSettings(settings);
		final CompoundPropertyModel<GbSettings> formModel = new CompoundPropertyModel<GbSettings>(gbSettings);

		// form
		Form<GbSettings> form = new Form<GbSettings>("form", formModel);

		// letter grade
		form.add(new CheckBox("letterGrade",
				new PropertyModel<Boolean>(formModel, "gradebookInformation.courseLetterGradeDisplayed")));

		// percentage
		form.add(new CheckBox("percentage",
				new PropertyModel<Boolean>(formModel, "gradebookInformation.courseAverageDisplayed")));

		// points
		form.add(new CheckBox("points",
				new PropertyModel<Boolean>(formModel, "gradebookInformation.coursePointsDisplayed")));

		final AjaxButton submit = new AjaxButton("submit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				// update settings
				UpdateCourseGradeDisplayPanel.this.businessService.updateGradebookSettings(formModel.getObject().getGradebookInformation());

				getSession().info(getString("label.updatecoursegradedisplay.success"));
				UpdateCourseGradeDisplayPanel.this.window.close(target);
				setResponsePage(new GradebookPage());
			}
		};
		form.add(submit);

		final AjaxButton cancel = new AjaxButton("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				UpdateCourseGradeDisplayPanel.this.window.close(target);
			}
		};
		cancel.setDefaultFormProcessing(false);
		form.add(cancel);

		add(form);
	}
}
