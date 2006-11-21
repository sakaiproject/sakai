package org.sakaiproject.portal.render.api;

public class RenderResult {

    private String title;
    private StringBuffer content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public StringBuffer getContent() {
        return content;
    }

    public void setContent(StringBuffer content) {
        this.content = content;
    }
}
