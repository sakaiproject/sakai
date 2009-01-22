/**
 * $Id$
 * $URL$
 * ExternalIntegrationProviderMock.java - entity-broker - Jan 13, 2009 5:52:37 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.util.external;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider;


/**
 * A mock which provides a placeholder class to handle external integration
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ExternalIntegrationProviderMock implements ExternalIntegrationProvider {

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider#fetchEntity(org.sakaiproject.entitybroker.EntityReference)
     */
    public Object fetchEntity(String reference) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider#fireEvent(java.lang.String, java.lang.String)
     */
    public void fireEvent(String eventName, String reference) {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider#getServerUrl()
     */
    public String getServerUrl() {
        return "http://localhost:8080";
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider#handleEntityError(javax.servlet.http.HttpServletRequest, java.lang.Throwable)
     */
    public String handleEntityError(HttpServletRequest req, Throwable error) {
        return "Error occurred (mock): " + error;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider#handleUserSessionKey(javax.servlet.http.HttpServletRequest)
     */
    public void handleUserSessionKey(HttpServletRequest req) {
        // nothing to do
    }

}
