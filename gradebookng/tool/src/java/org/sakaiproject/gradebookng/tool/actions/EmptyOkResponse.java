package org.sakaiproject.gradebookng.tool.actions;

class EmptyOkResponse implements ActionResponse {
    public EmptyOkResponse() {
    }

    @Override
    public String getStatus() {
        return "OK";
    }

    @Override
    public String toJson() {
        return "{}";
    }
}