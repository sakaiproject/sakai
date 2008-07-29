/**
 * $Id$
 * $URL$
 * URLconfigurable.java - entity-broker - Jul 29, 2008 2:11:58 PM - azeckoski
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

import java.util.Map;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;


/**
 * This entity type has the ability to define and handle configurable URLs<br/>
 * This adds the ability to supply a large set of simple redirects<br/>
 * URLs like this can be handled and supported:<br/>
 * /gradebook/7890/student/70987 to view all the grades for a student from a course <br/>
 * /gradebook/6758/item/Quiz1 to view a particular item in a gradebook by it's human readable name <br/>
 * /gradebook/item/6857657 to maybe just a view an item by its unique id. <br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * The convention interface is at {@link URLConfigurable}
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface URLConfigDefinable extends URLConfigurable {

   /**
    * Defines the set of simple URL rewrites for this prefix<br/>
    * Simple rewrites require no processing logic to handle the redirect and
    * the redirect is always processed before anything validity checks happen<br/>
    * Some examples:<br/>
    * /myprefix/item/{id} => /my-item/{id} <br/>
    * /myprefix/{year}/{month}/{day} => /myprefix/?date={year}-{month}-{day}
    * @return the map of incoming URL pattern => outgoing URL pattern 
    * OR null/empty if you have no simple mappings
    */
   public Map<String, String> defineURLMappings();

}
