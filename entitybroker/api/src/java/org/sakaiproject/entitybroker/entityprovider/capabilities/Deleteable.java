/**
 * $Id$
 * $URL$
 * Deleteable.java - entity-broker - Apr 8, 2008 11:31:26 AM - azeckoski
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

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * This entity type can be deleted (this is the D in CRUD),
 * the current user id should be used for permissions checking in most cases<br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface Deleteable extends EntityProvider {

   /**
    * Deletes the entity identified by this entity reference,
    * if the entity cannot be found then nothing happens
    * 
    * @param ref the parsed reference object which uniquely represents this entity
    * @throws SecurityException if permissions prevented this entity from being created
    * @throws IllegalStateException for all other failures
    */
   public void deleteEntity(EntityReference ref);

}
