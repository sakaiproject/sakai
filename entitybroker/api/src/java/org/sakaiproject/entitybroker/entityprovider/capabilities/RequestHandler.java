/**
 * $Id$
 * $URL$
 * RequestHandler.java - entity-broker - Apr 12, 2008 2:44:44 PM - azeckoski
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

import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Indicates that this entity provider will handle its own entity view requests,
 * this would be very unusual but it allows the entity provider itself to redirect
 * requests to a tool and normally would be used if there is some special circumstance only<br/>
 * This will be called before any other request handling and before the access provider
 * is called and will cause all other processing to be skipped<br/>
 * <b>NOTE:</b> if you want to stop certain requests from coming through then
 * a better option is to use {@link RequestInterceptor} which is triggered
 * just before this would be called
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface RequestHandler extends EntityProvider, EntityViewAccessProvider {

   // this space left blank intentionally

}
