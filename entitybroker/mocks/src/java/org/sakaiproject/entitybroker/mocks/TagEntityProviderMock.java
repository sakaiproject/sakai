/**
 * $Id$
 * $URL$
 * TagEntityProviderMock.java - entity-broker - Aug 8, 2008 1:54:21 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.mocks;

import org.sakaiproject.entitybroker.entityprovider.capabilities.Taggable;


/**
 * This is an entity provider which uses the internal tagging service
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class TagEntityProviderMock extends CRUDableEntityProviderMock implements Taggable {

   public TagEntityProviderMock(String prefix, String[] ids) {
      super(prefix, ids);
   }

   
   // nothing else to do here

}
