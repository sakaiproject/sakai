/**
 * $Id$
 * $URL$
 * Browseable.java - entity-broker - Aug 3, 2008 9:20:34 AM - azeckoski
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

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * This indicates that this entity will participate in browse functionality for entities,
 * For example, it will provide lists of entities which are visible to users in locations
 * which can be looked through and selected<br/>
 * Entities which do not implement this will not appear in lists of entities which are being browsed<br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * This extends {@link CollectionResolvable}
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface Browseable extends EntityProvider, CollectionResolvable {

   // this space intentionally left blank

}
