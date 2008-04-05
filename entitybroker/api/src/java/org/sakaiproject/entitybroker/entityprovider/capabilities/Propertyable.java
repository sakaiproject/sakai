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

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Allows entities handled by the entity provider which implements this interface to have properties
 * attached to them, properties can be accessed via the {@link EntityBroker}, properties will be
 * stored and retrieved using the internal entity property retrieval implementation<br/>
 * <b>WARNING:</b> this should be used only for properties of entities which will be accessed very
 * lightly, for production level access you should use the {@link PropertyProvideable} instead <br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface Propertyable extends EntityProvider {

   // this space intentionally left blank

}
