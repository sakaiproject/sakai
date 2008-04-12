/**
 * $Id$
 * $URL$
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 **/

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Allows the entities handled by this provider to be accessed directly as objects,
 * this is also the interface for "reading" entities (this is the R in CRUD)<br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public interface Resolvable extends EntityProvider {

   /**
    * Allows this entity to be fetched based on the local id<br/> 
    * (the global reference string will consist of the entity prefix and the local id) <br/>
    * <b>Note:</b> The entity class type needs to be able to be resolved from the ClassLoader of the 
    * EntityBrokerManager (currently this means deployed into shared) <br/> 
    * <br/>The entity object does not have to be a model object itself and may simply
    * be something created (e.g. String, Map, etc.) to give to anyone calling this method.
    * 
    * @param ref the parsed reference object which uniquely represents this entity
    * @return an entity object of the type used for these entities
    */
   public Object getEntity(EntityReference ref);

}
