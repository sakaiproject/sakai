/**
 * $Id$
 * $URL$
 * RequestGetter.java - entity-broker - Apr 8, 2008 8:56:18 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.entityprovider.extension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Allows for getting to the request and response objects for the current thread
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface RequestGetter {

   /**
    * @return the current request for this thread or null if none can be found
    */
   public HttpServletRequest getRequest();

   /**
    * @return the current response for this thread or null if none can be found
    */
   public HttpServletResponse getResponse();

}
