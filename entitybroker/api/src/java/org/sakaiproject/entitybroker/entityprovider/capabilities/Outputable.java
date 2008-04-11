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

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * The entity can be returned as certain output formats which are handled automatically,
 * by default all requests go through to the available access providers: 
 * {@link EntityViewAccessProvider} or {@link HttpServletAccessProvider}
 * <br/>
 * If you want to define the data that is returned instead of using the internal methods 
 * then use {@link OutputDefineable}<br/>
 * <b>NOTE:</b> there is no internal handling of HTML, it will always redirect to the the available access provider
 * if there is one (if there is not one then the entity will be toStringed)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface Outputable extends EntityProvider {

   public static String HTML = "html";
   public static String XML = "xml";
   public static String JSON = "json";

   /**
    * Defines the extensions (format types) handled by this provider,
    * the extension which goes on the end of an entity URL (after a ".") to indicate the return type,
    * <b>WARNING:</b> not including {@link #HTML} in the return will stop all redirects to the access providers
    * and therefore will cause HTML requests for entities to go nowhere
    * 
    * @return an array containing the extensions handled,
    * use the constants (example: {@link #HTML}) or feel free to make up your own (use lowercase chars)
    */
   public String[] getHandledExtensions();

}
