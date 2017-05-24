package org.sakaiproject.gradebookng.tool.actions;


import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.sakaiproject.gradebookng.business.model.GbStudentNameSortOrder;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;

import java.io.Serializable;

public class SetStudentNameOrderAction implements Action, Serializable {

    private static final long serialVersionUID = 1L;

    public SetStudentNameOrderAction() {
    }

    @Override
    public ActionResponse handleEvent(JsonNode params, AjaxRequestTarget target) {
        final String orderParam = params.get("orderby").asText();
        final GbStudentNameSortOrder nameSortOrder = GbStudentNameSortOrder.valueOf(orderParam.toUpperCase());

        final GradebookPage gradebookPage = (GradebookPage)target.getPage();

        final GradebookUiSettings settings = gradebookPage.getUiSettings();
        settings.setNameSortOrder(nameSortOrder);

        // save settings
        gradebookPage.setUiSettings(settings);

        // refresh the page
        target.appendJavaScript("location.reload();");

        return new EmptyOkResponse();
    }
}
