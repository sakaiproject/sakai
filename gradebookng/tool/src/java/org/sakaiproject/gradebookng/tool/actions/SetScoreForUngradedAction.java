package org.sakaiproject.gradebookng.tool.actions;


import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.panels.UpdateUngradedItemsPanel;
import org.sakaiproject.gradebookng.tool.panels.ZeroUngradedItemsPanel;

import java.io.Serializable;

public class SetScoreForUngradedAction implements Action, Serializable {

    private static final long serialVersionUID = 1L;

    public SetScoreForUngradedAction() {
    }

    @Override
    public ActionResponse handleEvent(JsonNode params, AjaxRequestTarget target) {
        final String assignmentId = params.get("assignmentId").asText();

        final GradebookPage gradebookPage = (GradebookPage)target.getPage();
        final GbModalWindow window = gradebookPage.getUpdateUngradedItemsWindow();
        final UpdateUngradedItemsPanel panel = new UpdateUngradedItemsPanel(
            window.getContentId(),
            Model.of(Long.valueOf(assignmentId)),
            window);

        window.setTitle(gradebookPage.getString("heading.updateungradeditems"));
        window.setAssignmentToReturnFocusTo(assignmentId);
        window.setContent(panel);
        window.showUnloadConfirmation(false);
        window.show(target);

        panel.setOutputMarkupId(true);
        target.appendJavaScript("new GradebookUpdateUngraded($(\"#" + panel.getMarkupId() + "\"));");

        return new EmptyOkResponse();
    }
}
