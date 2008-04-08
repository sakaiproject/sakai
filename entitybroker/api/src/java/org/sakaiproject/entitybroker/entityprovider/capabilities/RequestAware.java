/**
 * $Id$
 * $URL$
 * RequestAware.java - entity-broker - Apr 7, 2008 10:12:00 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;


/**
 * Indicates that this entity provider needs to be request aware, 
 * this allows the entity provider to get hold of information from the request at any time
 * by directly accessing the request and response objects (if we are inside a request),
 * if there is no current request then this method will fail to return anything<br/>
 * This is primarily intended to provide access to request parameters while operating
 * inside the entity provider
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface RequestAware extends EntityProvider {

   /**
    * Allows the entity provider to access the current request if it is available,
    * sets a getter service which will retrieve the current request/response if there is one<br/>
    * <b>NOTE:</b> this will only be the current request at the instant that the methods
    * on the getter service are called
    */
   public void setRequestGetter(RequestGetter requestGetter);

}
