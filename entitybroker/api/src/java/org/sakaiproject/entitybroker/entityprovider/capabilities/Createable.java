/**
 * $Id$
 * $URL$
 * Createable.java - entity-broker - Apr 8, 2008 11:14:05 AM - azeckoski
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
 * This entity type can be created (this is the C in CRUD),
 * the current user id should be used for permissions checking in most cases<br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface Createable extends EntityProvider {

   /**
    * Create a new entity and return the unique local id of the entity,
    * the object should contain the data needed to create the entity or this will fail
    * 
    * @param ref the parsed reference object which uniquely represents this entity
    * @param entity an entity object
    * @return the locally unique id of the new object
    * @throws IllegalArgumentException if the entity could not be created because of missing or invalid data
    * @throws SecurityException if permissions prevented this entity from being created
    * @throws IllegalStateException for all other failures
    */
   public String createEntity(EntityReference ref, Object entity);

   /**
    * Provides a sample entity object which can be populated with data and then passed to 
    * the {@link #createEntity(EntityReference, Object)} method,
    * this is necessary so that the type of the entity object is known and the right fields can
    * be filled, it also allows us to support the case of different read and write objects
    * <b>Note:</b> The entity class type needs to be able to be resolved from the ClassLoader of the 
    * EntityBrokerManager (currently this means deployed into shared) <br/> 
    * 
    * @return a sample entity object for entities of the type represented by this provider
    * @throws IllegalStateException if the sample object cannot be obtained for some reason
    */
   public Object getSampleEntity();

}
