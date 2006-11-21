package org.sakaiproject.portal.render.portlet.services;

import java.util.Map;

/**
 * Attributes utilized by the {@link SakaiPortalCallbackService}
 * for communicating information back to the Portal
 * 
 */

public class PortletAttributes {

    private String title;
    private Map requestProperties;
    private Map responseProperties;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map getRequestProperties() {
        return requestProperties;
    }

    public void setRequestProperties(Map requestProperties) {
        this.requestProperties = requestProperties;
    }

    public Map getResponseProperties() {
        return responseProperties;
    }

    public void setResponseProperties(Map responseProperties) {
        this.responseProperties = responseProperties;
    }
}
