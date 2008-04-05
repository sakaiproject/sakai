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

import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider;

/**
 * Allows entities handled by the entity provider which implements this interface to have properties
 * attached to them, properties can be accessed via the {@link EntityBroker}, properties will be
 * stored and retrieved via the methods which are implemented in this interface<br/> Allows the
 * entity provider to define and control the way properties are stored with its own entities<br/>
 * <b>NOTE:</b> the validity of references and parameters is checked in the broker before the call
 * goes to the provider <br/> This is one of the capability extensions for the
 * {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface PropertyProvideable extends EntityProvider, PropertiesProvider {

   // this space intentionally left blank

}
