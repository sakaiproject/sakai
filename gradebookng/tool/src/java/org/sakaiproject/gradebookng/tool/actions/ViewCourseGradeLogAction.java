package org.sakaiproject.gradebookng.tool.actions;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.panels.CourseGradeOverrideLogPanel;

import java.io.Serializable;

public class ViewCourseGradeLogAction extends InjectableAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public ViewCourseGradeLogAction() {
	}

	@Override
	public ActionResponse handleEvent(final JsonNode params, final AjaxRequestTarget target) {
		final String studentUuid = params.get("studentId").asText();

		final GradebookPage page = (GradebookPage) target.getPage();
		final GbModalWindow window = page.getUpdateCourseGradeDisplayWindow();

		window.setStudentToReturnFocusTo(studentUuid);
		window.setReturnFocusToCourseGrade();
		window.setContent(new CourseGradeOverrideLogPanel(window.getContentId(), Model.of(studentUuid), window));
		window.showUnloadConfirmation(false);
		window.show(target);

		return new EmptyOkResponse();
	}
}
