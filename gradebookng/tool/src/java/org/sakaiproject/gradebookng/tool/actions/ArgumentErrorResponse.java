package org.sakaiproject.gradebookng.tool.actions;

public class ArgumentErrorResponse implements ActionResponse {
    private String msg;

    public ArgumentErrorResponse(final String msg) {
            this.msg = msg;
        }

    public String getStatus() {
            return "error";
        }

    public String toJson() {
            return String.format("{\"msg\": \"%s\"}", msg);
        }
}
