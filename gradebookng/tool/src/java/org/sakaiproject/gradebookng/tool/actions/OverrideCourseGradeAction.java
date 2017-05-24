package org.sakaiproject.gradebookng.tool.actions;


import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.panels.CourseGradeOverridePanel;
import org.sakaiproject.gradebookng.tool.panels.GradeLogPanel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class OverrideCourseGradeAction implements Action, Serializable {

    private static final long serialVersionUID = 1L;

    private GradebookNgBusinessService businessService;

    public OverrideCourseGradeAction(GradebookNgBusinessService businessService) {
        this.businessService = businessService;
    }

    @Override
    public ActionResponse handleEvent(JsonNode params, AjaxRequestTarget target) {
        String studentUuid = params.get("studentId").asText();

        final GradebookPage gradebookPage = (GradebookPage) target.getPage();
        final GbModalWindow window = gradebookPage.getUpdateCourseGradeDisplayWindow();
        window.setStudentToReturnFocusTo(studentUuid);
        window.setReturnFocusToCourseGrade();
        window.setContent(new CourseGradeOverridePanel(window.getContentId(),
                                                       Model.of(studentUuid),
                                                       window));
        window.showUnloadConfirmation(false);
        window.show(target);

        return new EmptyOkResponse();
    }
}
