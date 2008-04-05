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

import org.sakaiproject.entitybroker.collector.AutoRegister;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * By implementing this interface you are telling the {@link EntityProviderManager} to register this
 * entity broker as soon as spring creates it, to be exposed as part of the {@link EntityBroker}
 * <br/> This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public interface AutoRegisterEntityProvider extends EntityProvider, AutoRegister {

   // no methods (this space intentionally left blank -AZ)

}
