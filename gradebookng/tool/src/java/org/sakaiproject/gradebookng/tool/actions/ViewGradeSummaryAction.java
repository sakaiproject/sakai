package org.sakaiproject.gradebookng.tool.actions;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.panels.StudentGradeSummaryPanel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ViewGradeSummaryAction extends InjectableAction implements Serializable {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	private GradebookNgBusinessService businessService;

	public ViewGradeSummaryAction() {
	}

	@Override
	public ActionResponse handleEvent(final JsonNode params, final AjaxRequestTarget target) {
		final String studentUuid = params.get("studentId").asText();

		final GradebookUiSettings settings = ((GradebookPage) target.getPage()).getUiSettings();

		final GbUser student = businessService.getUser(studentUuid);

		final Map<String, Object> model = new HashMap<>();
		model.put("studentUuid", studentUuid);
		model.put("groupedByCategoryByDefault", settings.isCategoriesEnabled());

		final GradebookPage gradebookPage = (GradebookPage) target.getPage();
		final GbModalWindow window = gradebookPage.getStudentGradeSummaryWindow();

		final Component content = new StudentGradeSummaryPanel(window.getContentId(), Model.ofMap(model), window);

		if (window.isShown() && window.isVisible()) {
			window.replace(content);
			content.setVisible(true);
			target.add(content);
		} else {
			window.setContent(content);
			window.show(target);
		}

		window.setStudentToReturnFocusTo(studentUuid);
		content.setOutputMarkupId(true);

		final String modalTitle = (new StringResourceModel("heading.studentsummary",
				null, new Object[] { student.getDisplayName(), student.getDisplayId() })).getString();

		window.setTitle(modalTitle);
		window.show(target);

		target.appendJavaScript(String.format(
				"new GradebookGradeSummary($(\"#%s\"), false, \"%s\");",
				content.getMarkupId(), modalTitle));

		return new EmptyOkResponse();
	}
}
