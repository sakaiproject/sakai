/**
 * $Id$
 * $URL$
 * ExternalIntegrationProviderMock.java - entity-broker - Jan 13, 2009 5:52:37 PM - azeckoski
 **********************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

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
     * @see org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider#getMaxJSONLevel()
     */
    public String getMaxJSONLevel() {
        return "12";
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

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider#findService(java.lang.Class)
     */
    public <T> T findService(Class<T> type) {
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfigurationSetting(String settingName, T defaultValue) {
        if ("entitybroker.batch.enable".equals(settingName)) {
            return ((T) Boolean.TRUE);
        }
        return defaultValue;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.extension.LearningTrackingProvider#registerStatement(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Float)
     */
    public void registerStatement(String prefix, String actorEmail, String verbStr, String objectURI, Boolean resultSuccess, Float resultScaledScore) {
        // nothing to do
    }

}
