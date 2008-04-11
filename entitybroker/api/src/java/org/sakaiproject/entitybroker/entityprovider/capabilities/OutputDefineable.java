/**
 * $Id$
 * $URL$
 * HTMLdefineable.java - entity-broker - Apr 6, 2008 7:44:11 PM - azeckoski
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

import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;

/**
 * Allows this entity to define the data that is returned for it depending on the extension,
 * if you just want to use the internal methods to handle formatting the output
 * then simply use {@link Outputable}<br/>
 * The extension for the data request will be available in the {@link EntityView} which
 * is passed into {@link EntityViewAccessProvider#handleAccess(EntityView, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface OutputDefineable extends Outputable, EntityViewAccessProvider {

   /**
    * Defines the extensions (format types) specially handled by this provider,
    * the extension goes on the end of an entity URL (after a ".") to indicate the return type,
    * <b>NOTE:</b> Mixing together the returned extensions is handled as follows:
    * any defined extensions missing from this but included from {@link Outputable} will be handled
    * as if they were only included in {@link Outputable}, any extensions that exist in both {@link Outputable}
    * and {@link OutputDefineable} will be handled by {@link OutputDefineable}
    * 
    * @return an array containing the extensions which you want passed through to this,
    * use the constants (example: {@link #HTML}) or feel free to make up your own (use lowercase chars)
    */
   public String[] getDefinedExtensions();

}
