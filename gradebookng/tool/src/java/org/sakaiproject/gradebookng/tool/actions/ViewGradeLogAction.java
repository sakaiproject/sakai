package org.sakaiproject.gradebookng.tool.actions;


import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.panels.GradeLogPanel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ViewGradeLogAction extends InjectableAction implements Serializable {

    private static final long serialVersionUID = 1L;

    public ViewGradeLogAction() {
    }

    @Override
    public ActionResponse handleEvent(final JsonNode params, final AjaxRequestTarget target) {
        final String assignmentId = params.get("assignmentId").asText();
        final String studentUuid = params.get("studentId").asText();

        final Map<String, Object> model = new HashMap<>();
        model.put("assignmentId", Long.valueOf(assignmentId));
        model.put("studentUuid", studentUuid);

        final GradebookPage gradebookPage = (GradebookPage) target.getPage();
        final GbModalWindow window = gradebookPage.getGradeLogWindow();

        window.setAssignmentToReturnFocusTo(assignmentId);
        window.setStudentToReturnFocusTo(studentUuid);
        window.setContent(new GradeLogPanel(window.getContentId(), Model.ofMap(model), window));
        window.show(target);

        return new EmptyOkResponse();
    }
}
