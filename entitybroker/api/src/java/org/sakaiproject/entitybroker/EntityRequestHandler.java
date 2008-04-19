/**
 * $Id$
 * $URL$
 * EntityRequestHandler.java - entity-broker - Apr 6, 2008 9:03:03 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

}
