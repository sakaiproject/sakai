package org.sakaiproject.gradebookng.tool.actions;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.panels.GradeCategoryStatisticsPanel;

import java.io.Serializable;

public class ViewAssignmentGradeStatisticsAction extends InjectableAction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public ActionResponse handleEvent(final JsonNode params, final AjaxRequestTarget target) {
        final GradebookPage gradebookPage = (GradebookPage) target.getPage();
        final GbModalWindow window = gradebookPage.getGradeLogWindow();
        final String category = params.get("categoryId").asText();
        window.setAssignmentToReturnFocusTo(category);
        window.setContent(new GradeCategoryStatisticsPanel(window.getContentId(),
                Model.of(Long.valueOf(category)),
                window, category));
        window.show(target);

        return new EmptyOkResponse();
    }
}