package org.sakaiproject.gradebookng.tool.actions;


import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;

import java.io.Serializable;

public class MoveAssignmentRightAction extends MoveAssignmentAction implements Action, Serializable {

    private static final long serialVersionUID = 1L;

    public MoveAssignmentRightAction(GradebookNgBusinessService businessService) {
        super(businessService);
    }

    @Override
    public ActionResponse handleEvent(JsonNode params, AjaxRequestTarget target) {
        final GradebookPage gradebookPage = (GradebookPage) target.getPage();

        final Long assignmentId = Long.valueOf(params.get("assignmentId").asText());

        GradebookUiSettings settings = gradebookPage.getUiSettings();

        if (settings == null) {
            settings = new GradebookUiSettings();
            gradebookPage.setUiSettings(settings);
        }

        if (settings.isCategoriesEnabled() && settings.isGroupedByCategory()) {
            try {
                final Integer order = calculateCurrentCategorizedSortOrder(assignmentId);
                MoveAssignmentRightAction.this.businessService.updateAssignmentCategorizedOrder(assignmentId,
                    (order.intValue() + 1));
            } catch (final Exception e) {
                return new ArgumentErrorResponse("Error reordering within category: " + e.getMessage());
            }
        } else {
            final int order = MoveAssignmentRightAction.this.businessService.getAssignmentSortOrder(assignmentId.longValue());
            MoveAssignmentRightAction.this.businessService.updateAssignmentOrder(assignmentId.longValue(), (order + 1));
        }

        // refresh the page
        target.appendJavaScript("location.reload();");

        return new EmptyOkResponse();
    }
}
