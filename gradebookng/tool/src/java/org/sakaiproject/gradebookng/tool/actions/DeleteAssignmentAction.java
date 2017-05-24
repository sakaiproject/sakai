package org.sakaiproject.gradebookng.tool.actions;


import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.panels.DeleteItemPanel;

import java.io.Serializable;

public class DeleteAssignmentAction implements Action, Serializable {

    private static final long serialVersionUID = 1L;

    public DeleteAssignmentAction() {
    }

    @Override
    public ActionResponse handleEvent(JsonNode params, AjaxRequestTarget target) {
        String assignmentId = params.get("assignmentId").asText();

        final GradebookPage gradebookPage = (GradebookPage)target.getPage();
        final GbModalWindow window = gradebookPage.getDeleteItemWindow();

        window.setTitle(gradebookPage.getString("delete.label"));
        window.setAssignmentToReturnFocusTo(assignmentId);
        window.setContent(new DeleteItemPanel(
            window.getContentId(),
            Model.of(Long.valueOf(assignmentId)),
            window));
        window.showUnloadConfirmation(false);
        window.show(target);

        return new EmptyOkResponse();
    }
}
