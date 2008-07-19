/**
 * $Id$
 * $URL$
 * DescribeDefineable.java - entity-broker - Jul 18, 2008 5:46:15 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import java.util.Locale;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;


/**
 * Allows an entity to define the description of itself in code rather than using properties,
 * this will be called each time a description is needed so it should be efficient<br/>
 * This is the configuration interface<br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * @see Describeable
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface DescribeDefineable extends Describeable {

   /**
    * Allows for complete control over the descriptions of entities<br/>
    * This will always be called first if it is defined, returning a null will
    * default to attempting to get the value from the properties (if any are defined),
    * returning an empty string will cause nothing to be shown for the description
    * 
    * @param locale this is the locale that the description should be created for
    * @param capability (optional) if null then the general description of the entity should be created,
    * otherwise provide the description for the capability that was provided
    * @return the string which describes this entity or this capability for this entity,
    * return null to allow this to attempt to get the value from the properties
    */
   public String getDescription(Locale locale, Class<? extends EntityProvider> capability);

}
