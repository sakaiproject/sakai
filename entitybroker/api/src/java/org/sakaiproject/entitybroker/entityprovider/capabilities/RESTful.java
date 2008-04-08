/**
 * $Id$
 * $URL$
 * RESTful.java - entity-broker - Apr 8, 2008 12:05:12 PM - azeckoski
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


/**
 * Indicates that entities handled by this provider are RESTful as defined by the REST microformat:<br/>
 * <a href="http://microformats.org/wiki/rest/urls">http://microformats.org/wiki/rest/urls</a><br/>
 * Requires all CRUD functionality and 
 * 
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface RESTful extends EntityProvider, CRUDable, HTMLable, OutputFormatable {

   // this space left blank intentionally

}
