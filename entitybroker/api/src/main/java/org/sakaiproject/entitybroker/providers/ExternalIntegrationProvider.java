/**
 * Copyright (c) 2007-2013 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.providers;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.entitybroker.entityprovider.extension.LearningTrackingProvider;

public interface ExternalIntegrationProvider extends LearningTrackingProvider {

    /**
     * The recommended param key to use for sending in session ids in requests
     */
    public static final String SESSION_ID = "_sessionId";

    /**
     * Allows services in the external system to be located 
     * (this is used to find external providers for overriding the default internal services)
     * 
     * @param <T>
     * @param type the service API class
     * @return the service OR null if it does not exist
     */
    public <T> T findService(Class<T> type);

    /**
     * Fires events from EB using an external event system <br/>
     * You can assume the eventName is not null and the reference has been validated and normalized <br/>
     * NOTE: if you have no way to handle external events then throw {@link UnsupportedOperationException}
     * 
     * @param eventName a string which represents the name of the event (e.g. announcement.create),
     * cannot be null or empty
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optionally the local id,
     * cannot be null or empty
     * @throws IllegalArgumentException if the event arguments are invalid or the event cannot be thrown
     * @throws UnsupportedOperationException if the external system does not handle events
     */
    public void fireEvent(String eventName, String reference);

    /**
     * Gets the full server URL for the server which this is being used on
     * @return the full server URL (default: http://localhost:8080), should not include a trailing slash
     * @throws UnsupportedOperationException if the external system cannot handle this
     */
    public String getServerUrl();

	    /**
     * Gets the entitybroker.maxJSONLevel config string from sakai.properties via ServerConfigurationService. Defaults to 7.
     */
    public String getMaxJSONLevel();

    /**
     * Fetches a concrete object representing this entity reference from an external system<br/>
     * Note that this object may be a {@link String} or {@link Map} and does not have to be a POJO,
     * the type of object should be determined out of band<br/>
     * This should return null if the entity exists but has no available object
     * 
     * @param reference a string representing a unique reference to an entity (e.g. /type/id/extra)
     * @return an object which represents the entity OR null if none can be found or this type does not support fetching
     * @throws SecurityException if the entity cannot be accessed by the current user or is not publicly accessible
     * @throws IllegalArgumentException if the reference is invalid
     * @throws UnsupportedOperationException if the external system cannot handle this
     * @throws IllegalStateException if any other error occurs
     */
    public Object fetchEntity(String reference);

    /**
     * Handles an error which occurs while processing an entity request,
     * e.g. by sending an email and logging extra info about the failure
     * 
     * @param req the current request
     * @param error the current error that occurred
     * @return the comprehensive error message to send back to the requester
     * @throws UnsupportedOperationException if the external system cannot handle this
     */
    public String handleEntityError(HttpServletRequest req, Throwable error);

    /**
     * Allows the external system to create a session based on a passed in session key (or other data in the request),
     * no returns are necessary and this method can simple be left unimplemented if it is not used <br/>
     * Most likely all requests will not include session keys so you should not fail if one is not set
     * in most cases
     * 
     * @param req the request as it is coming in (before any other processing has occurred)
     * @throws IllegalArgumentException if this request is invalid in some way
     * @throws SecurityException if this request is not allowed for security reasons
     */
    public void handleUserSessionKey(HttpServletRequest req);

    /**
     * Retrieves settings from the configuration service (sakai.properties)
     * 
     * @param settingName the name of the setting to retrieve, Should be a string name: e.g. auto.ddl,
     *            mystuff.config, etc.
     * @param defaultValue a specified default value to return if this setting cannot be found, 
     *          <b>NOTE:</b>  You can set the default value to null but you must specify the class type in parens
     * @return the value of the configuration setting OR the default value if none can be found
     */
    public <T> T getConfigurationSetting(String settingName, T defaultValue);

}
