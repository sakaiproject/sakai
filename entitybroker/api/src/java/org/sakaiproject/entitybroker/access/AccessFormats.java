/**
 * $Id$
 * $URL$
 * HTMLable.java - entity-broker - Apr 6, 2008 7:37:54 PM - azeckoski
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

import org.sakaiproject.entitybroker.entityprovider.extension.Formats;

/**
 * Indicates that entity requests can be handled for certain formats which are indicated<br/>
 * <br/>
 * <b>NOTE:</b> By default all entity view requests go through to the available access providers: 
 * {@link EntityViewAccessProvider} or {@link HttpServletAccessProvider} if nothing is specified here
 * or this interface is not implemented
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface AccessFormats extends EntityViewAccessProvider, Formats {

   /**
    * Defines the access format types (extensions) handled by this access provider<br/>
    * The default if this interface is not implemented is to pass through all requests to the
    * access provider that is defined
    * 
    * @return an array containing the format types (from {@link Formats}) handled <br/>
    * OR empty array to indicate all are handled (same as not implementing {@link AccessFormats}) <br/>
    * OR null to indicate none are handled (same as not implementing {@link EntityViewAccessProvider}) <br/>
    * NOTE: use the constants (example: {@link #HTML}) or feel free to make up your own if you like
    */
   public String[] getHandledAccessFormats();

}
