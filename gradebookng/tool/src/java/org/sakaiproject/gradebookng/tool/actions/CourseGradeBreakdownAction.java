package org.sakaiproject.gradebookng.tool.actions;

import java.io.Serializable;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.actions.InjectableAction;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.panels.CourseGradeBreakdownPanel;
import org.sakaiproject.gradebookng.tool.panels.CourseGradeStatisticsPanel;
import com.fasterxml.jackson.databind.JsonNode;

public class CourseGradeBreakdownAction extends InjectableAction implements Serializable {

    private static final long serialVersionUID = 1L;

    public CourseGradeBreakdownAction() {
    }

    @Override
    public ActionResponse handleEvent(final JsonNode params, final AjaxRequestTarget target) {

        final String siteId = params.get("siteId").asText();

        final GradebookPage gradebookPage = (GradebookPage) target.getPage();
        final GbModalWindow window = gradebookPage.getGradeLogWindow();
        window.setTitle("Course Grade Breakdown");
        CourseGradeBreakdownPanel cgbp = new CourseGradeBreakdownPanel(window.getContentId(), window);
        window.setContent(cgbp);
        window.show(target);

        return new EmptyOkResponse();
    }
}
