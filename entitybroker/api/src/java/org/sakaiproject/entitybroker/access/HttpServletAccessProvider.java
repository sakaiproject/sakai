/**
 * $Id$
 * $URL$
 * HttpServletAccessProvider.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 * created by antranig on 14 May 2007
 **/

package org.sakaiproject.entitybroker.access;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.EntityReference;

/**
 * Represents a bean which is capable of handling direct HTTP access for an entity.
 * </p>
 * This interface would be typically implemented from a tool (webapp) context, and registered with
 * the {@link HttpServletAccessProviderManager} in a context loader listener.
 * </p>
 * If the implementation throws a {@link SecurityException} during the course of this method, the
 * access will be directed to a login page or authentication method before being redirected to the
 * tool.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public interface HttpServletAccessProvider {

   /**
    * Define the way that access is handled for your entities
    * 
    * @param req
    *           the servlet request
    * @param res
    *           the servlet response
    * @param reference
    *           a globally unique reference to an entity, consists of the entity prefix and the
    *           local id
    */
   public void handleAccess(HttpServletRequest req, HttpServletResponse res, EntityReference ref);

}
