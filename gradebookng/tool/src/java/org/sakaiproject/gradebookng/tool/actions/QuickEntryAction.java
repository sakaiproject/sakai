package org.sakaiproject.gradebookng.tool.actions;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.pages.QuickEntryPage;

import java.io.Serializable;

public class QuickEntryAction extends InjectableAction implements Serializable {
    private static final long serialVersionUID = 1L;

    public QuickEntryAction() {
    }

    @Override
    public ActionResponse handleEvent(final JsonNode params, final AjaxRequestTarget target) {
        final String assignmentId = params.get("assignmentId").asText();
        PageParameters paramsNow = new PageParameters();
        paramsNow.add("selected",assignmentId);
        final GradebookPage gradebookPage = (GradebookPage) target.getPage();
        gradebookPage.setResponsePage(QuickEntryPage.class, paramsNow);
        return new EmptyOkResponse();
    }
}