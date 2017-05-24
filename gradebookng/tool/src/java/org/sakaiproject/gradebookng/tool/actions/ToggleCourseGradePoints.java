package org.sakaiproject.gradebookng.tool.actions;


import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;

import java.io.Serializable;

public class ToggleCourseGradePoints implements Action, Serializable {

    private static final long serialVersionUID = 1L;

    public ToggleCourseGradePoints() {
    }

    @Override
    public ActionResponse handleEvent(JsonNode params, AjaxRequestTarget target) {
        final GradebookPage gradebookPage = (GradebookPage) target.getPage();

        // get current settings
        final GradebookUiSettings settings = gradebookPage.getUiSettings();
        final Boolean currentSetting = settings.getShowPoints();

        // toggle it
        final Boolean nextSetting = !currentSetting;

        // set it
        settings.setShowPoints(nextSetting);

        // save settings
        gradebookPage.setUiSettings(settings);
        
        // refresh the page
        target.appendJavaScript("location.reload();");

        return new EmptyOkResponse();
    }
}
