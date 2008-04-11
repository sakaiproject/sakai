/**
 * $Id$
 * $URL$
 * ViewAccessProvider.java - entity-broker - Apr 11, 2008 11:37:48 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.access;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.EntityView;

/**
 * Represents a bean which is capable of handling access for an {@link EntityView},
 * this replaces the {@link HttpServletAccessProvider} as all entity URLs are now being parsed
 * so more information can be provided through the {@link EntityView}
 * </p>
 * This interface would be typically implemented from a tool (webapp) context, and registered with
 * the {@link EntityViewAccessProviderManager} in a context loader listener.
 * </p>
 * If the implementation throws a {@link SecurityException} during the course of this method, the
 * access will be directed to a login page or authentication method before being redirected back to
 * the implementation method.
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface EntityViewAccessProvider {

   /**
    * Make and return the data responses for this type of data provider for a specific entity view
    * and entity reference (contained within the entity view),
    * use the request to get any additional sent in information you may need or want
    * and use the response to hold the output you generate
    * 
    * @param view an entity view, should contain all the information related to the incoming entity URL
    * @param req the servlet request (available in case you need to get anything out of it)
    * @param res the servlet response, put the correct data response into the outputstream
    */
   public void handleAccess(EntityView view, HttpServletRequest req, HttpServletResponse res);

}
