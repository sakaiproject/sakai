/**
 * $Id$
 * $URL$
 * Updateable.java - entity-broker - Apr 8, 2008 11:40:19 AM - azeckoski
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
 * This entity type can be updated (this is the U in CRUD),
 * the current user id should be used for permissions checking in most cases<br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface Updateable extends Resolvable {

   /**
    * Update an existing entity,
    * the object should contain the data needed to update the entity or this will fail<br/>
    * Typically the entity will be retrieved first using {@link Resolvable#getEntity(EntityReference)}
    * and the the fields will be updated and it will be passed into this method
    * 
    * @param ref the parsed reference object which uniquely represents this entity
    * @param entity an entity object
    * @throws IllegalArgumentException if the entity could not be updated because of missing or invalid data or could not find entity to update
    * @throws SecurityException if permissions prevented this entity from being updated
    * @throws IllegalStateException for all other failures
    */
   public void updateEntity(EntityReference ref, Object entity);

}
