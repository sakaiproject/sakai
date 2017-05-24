package org.sakaiproject.gradebookng.tool.actions;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.ajax.AjaxRequestTarget;

public interface Action {
    public ActionResponse handleEvent(JsonNode params, AjaxRequestTarget target);
}
