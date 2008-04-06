/**
 * $Id$
 * $URL$
 * JSONable.java - entity-broker - Apr 6, 2008 6:23:55 PM - azeckoski
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
 * The entity can be returned as JSON and will automatically be returned using
 * the internal JSON methods, if this entity is not {@link Resolvable} then
 * the entity meta data will be returned only (e.g. prefix/id/reference/exists)<br/>
 * If you want to define the JSON that is returned then use {@link JSONdefineable}
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface JSONable extends EntityProvider {

   /**
    * the extension which goes on this entity URL (after a ".") to indicate the return should be JSON data
    */
   public final String EXTENSION = "json";

   // this space intentionally left blank

}
