package org.sakaiproject.gradebookng.tool.actions;


import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.panels.GradeLogPanel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ViewGradeLogAction implements Action, Serializable {

    private static final long serialVersionUID = 1L;

    private GradebookNgBusinessService businessService;

    public ViewGradeLogAction(GradebookNgBusinessService businessService) {
        this.businessService = businessService;
    }

    @Override
    public ActionResponse handleEvent(JsonNode params, AjaxRequestTarget target) {
        String assignmentId = params.get("assignmentId").asText();
        String studentUuid = params.get("studentId").asText();

        Map<String, Object> model = new HashMap<>();
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
