package org.sakaiproject.gradebookng.tool.actions;

import java.io.Serializable;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.panels.CourseGradeBreakdownPanel;
import org.sakaiproject.gradebookng.tool.panels.ExportRubricPanel;

import com.fasterxml.jackson.databind.JsonNode;

public class ExportRubricAssignmentAction extends InjectableAction implements Serializable {

    private static final long serialVersionUID = 1L;

    public ExportRubricAssignmentAction() {
    }

    @Override
    public ActionResponse handleEvent(final JsonNode params, final AjaxRequestTarget target) {
        String assignmentId = params.get("assignmentId").asText();

        final GradebookPage gradebookPage = (GradebookPage) target.getPage();
        final GbModalWindow window = gradebookPage.getExportRubricWindow();

        ExportRubricPanel cgbp = new ExportRubricPanel(
            window.getContentId(),
            Model.of(Long.valueOf(assignmentId)),
            window,
            params
            );

        cgbp.setCurrentGradebookAndSite(currentGradebookUid, currentSiteId);
        window.setContent(cgbp);
        window.show(target);

        return new EmptyOkResponse();
    }
}
