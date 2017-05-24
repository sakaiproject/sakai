package org.sakaiproject.gradebookng.tool.actions;


import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.panels.GradeLogPanel;
import org.sakaiproject.gradebookng.tool.panels.InstructorGradeSummaryGradesPanel;
import org.sakaiproject.gradebookng.tool.panels.StudentGradeSummaryPanel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ViewGradeSummaryAction implements Action, Serializable {

    private static final long serialVersionUID = 1L;

    private GradebookNgBusinessService businessService;

    public ViewGradeSummaryAction(GradebookNgBusinessService businessService) {
        this.businessService = businessService;
    }

    @Override
    public ActionResponse handleEvent(JsonNode params, AjaxRequestTarget target) {
        String studentUuid = params.get("studentId").asText();

        GradebookUiSettings settings = ((GradebookPage) target.getPage()).getUiSettings();

        GbUser student = businessService.getUser(studentUuid);

        Map<String, Object> model = new HashMap<>();
        model.put("studentUuid", studentUuid);
        model.put("groupedByCategoryByDefault", settings.isCategoriesEnabled());

        final GradebookPage gradebookPage = (GradebookPage) target.getPage();
        final GbModalWindow window = gradebookPage.getStudentGradeSummaryWindow();

        Component content = new StudentGradeSummaryPanel(window.getContentId(), Model.ofMap(model), window);

        if (window.isShown() && window.isVisible()) {
            window.replace(content);
            content.setVisible(true);
            target.add(content);
        } else {
            window.setContent(content);
            window.show(target);
        }

        window.setStudentToReturnFocusTo(studentUuid);
        content.setOutputMarkupId(true);

        String modalTitle = (new StringResourceModel("heading.studentsummary",
            null, new Object[]{student.getDisplayName(), student.getDisplayId()})).getString();

        window.setTitle(modalTitle);
        window.show(target);

        target.appendJavaScript(String.format(
            "new GradebookGradeSummary($(\"#%s\"), false, \"%s\");",
            content.getMarkupId(), modalTitle));

        return new EmptyOkResponse();
    }
}
