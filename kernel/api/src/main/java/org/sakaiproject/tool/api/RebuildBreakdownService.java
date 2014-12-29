/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2014 The Apereo Foundation.
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
 *
 **********************************************************************************/
package org.sakaiproject.tool.api;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * RebuildBreakdownService is responsible for converting the contents of a 
 * session to a serialized format or rebuilding a Session from the serialized 
 * format.  Sakai sessions frequently contain data that is not serializable, 
 * which limits sessions ability to be included in a cluster.  Depending upon 
 * the implementation, the service may store and retrieve this serialized data 
 * in objects that can be shared across servers.
 *
 * @author mgillian
 * @author Aaron Zeckoski (azeckoski @ unicon.net)
 */
public interface RebuildBreakdownService {

    /**
     * storeSession reads the contents of a session and stores a serialized 
     * version of the data in a distributed storage location
     *
     * @param session a Session that contains data the needs to be serialized
     * @param request [OPTIONAL] current servlet request OR null if you cannot get it
     * @return true if session was stored or false if rules stopped the session from being stored
     * @throws java.lang.IllegalArgumentException if any inputs are invalid
     */
    public boolean storeSession(Session session, HttpServletRequest request);

    /**
     * rebuildSession examines the internal data store and repopulates the 
     * Session with the reconstituted form of the objects that are stored internally
     *
     * @param session a Session that needs to be repopulated with serialized data
     * @return true if session was rebuilt or false if no session data exists in cache
     * @throws java.lang.IllegalArgumentException if any inputs are invalid
     */
    public boolean rebuildSession(Session session);

    /**
     * @return true if session distribution and replication handling is enabled
     */
    public boolean isSessionHandlingEnabled();

    /**
     * Purges the session data from distributed storage (if it exists)
     *
     * @param sessionId a string containing the unique identifier for a session
     */
    public void purgeSessionFromStorageById(String sessionId);

    /**
     * This allows for callback type processing to trigger session data rebuild on demand
     * by asking the service to retrieve the temporary stashed data so it can be used to
     * rebuild the data in the session
     *
     * @param attributeKey the attribute name where the value would have been stored in the Session or sub-session
     * @param sessionId [OPTIONAL] the id of the Session or sub-session, uses the current ToolSession if null
     * @return the stashed data which can be used to rebuild the session content
     * @throws java.lang.IllegalArgumentException if the attributeKey is not set
     * @throws java.lang.IllegalStateException if there is no session found for the given key
     */
    public StoreableBreakdown retrieveCallbackSessionData(String attributeKey, String sessionId);

    /**
     * Registers a breakdownable handler which is responsible for processing and general handling.
     *
     * WARNING: this registration is a weak one so you will need to have a strong reference (e.g. a service variable)
     * in your code to the handler (the Breakdownable object) you are registering.
     *
     * @param handler the breakdown handler to register
     *                NOTE: this should be defined in the same ClassLoader as the class it is managing
     * @throws java.lang.IllegalArgumentException if any inputs are invalid (null)
     * @throws java.lang.IllegalStateException if the handler fails any define method calls
     */
    public void registerBreakdownHandler(Breakdownable<?> handler);

    /**
     * Removes a breakdown handler and will no longer process classes of the type that it handles
     *
     * @param fullClassName a fully qualified implementation classname (e.g. org.sakaiproject.tool.impl.ThingImpl)
     * @throws java.lang.IllegalArgumentException if the input is null
     */
    public void unregisterBreakdownHandler(String fullClassName);

    /**
     * breakdownObject converts an object that implements the Breakdownable interface
     * into a Serializable object
     *
     * @param breakdownable an object that can be serialized by a Breakdownable handler.
     * @param size the size hint to indicate how small or complete the resulting
     * @return a Serialized version of the object
     * @see org.sakaiproject.tool.api.Breakdownable
     * @throws java.lang.IllegalArgumentException if any inputs are invalid
     * @throws java.lang.IllegalStateException if this is not a handled class type
     */
    public StoreableBreakdown breakdownObject(Object breakdownable, Breakdownable.BreakdownableSize size);

    /**
     * rebuildObject converts a Serialized object back to the original class
     *
     * @param fullClassName the fully qualified implementation classname (e.g. org.sakaiproject.tool.impl.ThingImpl)
     * @param size the size hint which was used when this class object data was serialized
     * @param data serialized data from the class
     * @return the original class that was Serialized OR null if it cannot be rebuilt
     * @see org.sakaiproject.tool.api.Breakdownable
     * @throws java.lang.IllegalArgumentException if any inputs are invalid/null
     */
    public Object rebuildObject(String fullClassName, Breakdownable.BreakdownableSize size, Serializable data);

}
