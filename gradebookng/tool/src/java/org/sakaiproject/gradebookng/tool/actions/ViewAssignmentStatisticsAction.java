package org.sakaiproject.gradebookng.tool.actions;


import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.panels.AddOrEditGradeItemPanel;
import org.sakaiproject.gradebookng.tool.panels.GradeStatisticsPanel;

import java.io.Serializable;

public class ViewAssignmentStatisticsAction implements Action, Serializable {

    private static final long serialVersionUID = 1L;

    private GradebookNgBusinessService businessService;

    public ViewAssignmentStatisticsAction(GradebookNgBusinessService businessService) {
        this.businessService = businessService;
    }

    @Override
    public ActionResponse handleEvent(JsonNode params, AjaxRequestTarget target) {
        String assignmentId = params.get("assignmentId").asText();

        final GradebookPage gradebookPage = (GradebookPage) target.getPage();
        final GbModalWindow window = gradebookPage.getGradeStatisticsWindow();
        window.setAssignmentToReturnFocusTo(assignmentId);
        window.setContent(new GradeStatisticsPanel(window.getContentId(),
                                                   Model.of(Long.valueOf(assignmentId)),
                                                   window));
        window.show(target);
        
        return new EmptyOkResponse();
    }
}
