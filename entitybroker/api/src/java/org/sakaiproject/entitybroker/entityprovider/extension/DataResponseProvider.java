/**
 * $Id$
 * $URL$
 * DataResponseProvider.java - entity-broker - Apr 6, 2008 6:32:56 PM - azeckoski
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

import org.sakaiproject.entitybroker.EntityReference;


/**
 * Defines the general interface for a data response provider,
 * this will be used to choose the types of responses this entity will send back
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface DataResponseProvider {

   /**
    * Make and return the data responses for this type of data provider for a specific entity reference,
    * use the request to get any additional sent in information you may need or want
    * and use the response to hold the output you generate
    * 
    * @param ref an entity reference
    * @param req the servlet request (available in case you need to get anything out of it)
    * @param res the servlet response, put the correct data response into the outputstream
    */
   public void makeData(EntityReference ref, HttpServletRequest req, HttpServletResponse res);

}
