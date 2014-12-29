/**
 * $Id$
 * $URL$
 * EntityRequestHandler.java - entity-broker - Apr 6, 2008 9:03:03 AM - azeckoski
 **************************************************************************
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
 */

package org.sakaiproject.entitybroker.providers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.exception.EntityException;

/**
 * Handles the URL/request processing for an entity in a central location
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface EntityRequestHandler {

    /**
     * The reserved word used to trigger entity descriptions
     */
    public static String DESCRIBE = "describe";
    public static String SLASH_DESCRIBE = EntityReference.SEPARATOR + DESCRIBE;
    /**
     * The reserved word used to trigger batch operations
     */
    public static String BATCH = "batch";
    public static String SLASH_BATCH = EntityReference.SEPARATOR + BATCH;
    /**
     * This is the name of the header which will contain the id of newly created entities
     */
    public static String HEADER_ENTITY_ID = "EntityId";
    /**
     * This is the name of the header which will contain the reference of created/updated entities
     */
    public static String HEADER_ENTITY_REFERENCE = "EntityReference";
    /**
     * This is the name of the header that will contain created/updated entities SHOW URL
     */
    public static String HEADER_ENTITY_URL = "Location";
    /**
     * The id used in generated URLs
     */
    public static String FAKE_ID = ":ID:";
    /**
     * This is the special indicator used to denote that POST should be translated to a PUT or DELETE
     * in order to compensate for browser limitations,
     * Example: /people/1?_method=PUT
     */
    public static String COMPENSATE_METHOD = "_method";

    /**
     * Handles the servlet request response cycle for all direct servlet accesses,
     * logically, we only want to let this request continue on if the entity exists AND
     * there is an http access provider to handle it AND the user can access it
     * (there is some auth completed already or no auth is required)
     * 
     * @param req the servlet request
     * @param res the servlet response
     * @param path the path from the request (if null it will be generated from the req)
     * @return the entity reference that was handled as part of this request
     * @throws EntityException if entity could not be found or failure parsing
     */
    public String handleEntityAccess(HttpServletRequest req, HttpServletResponse res, String path);

    /**
     * Handles an error which occurs by sending an email and logging extra info about the failure
     * @param req the current request
     * @param error the current error that occurred
     * @return the comprehensive error message
     */
    public String handleEntityError(HttpServletRequest req, Throwable error);

}
