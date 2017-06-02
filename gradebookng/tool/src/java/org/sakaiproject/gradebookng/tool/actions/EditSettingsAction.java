package org.sakaiproject.gradebookng.tool.actions;


import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.sakaiproject.gradebookng.tool.pages.SettingsPage;

import java.io.Serializable;

public class EditSettingsAction extends InjectableAction implements Serializable {

    private static final long serialVersionUID = 1L;

    public EditSettingsAction() {
    }

    private class EmptyOkResponse implements ActionResponse {
        public EmptyOkResponse() {
        }

        public String getStatus() {
            return "OK";
        }

        public String toJson() {
            return "{}";
        }
    }

    @Override
    public ActionResponse handleEvent(JsonNode params, AjaxRequestTarget target) {
        target.getPage().setResponsePage(SettingsPage.class);

        return new EmptyOkResponse();
    }
}
