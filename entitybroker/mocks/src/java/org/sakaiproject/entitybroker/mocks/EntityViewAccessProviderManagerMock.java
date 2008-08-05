/**
 * $Id$
 * $URL$
 * EntityViewAccessProviderManagerMock.java - entity-broker - Apr 11, 2008 4:37:13 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.mocks;

import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;


/**
 * For testing things that use the access provider manager,
 * always return a new {@link EntityViewAccessProviderMock} unless the prefix is invalid,
 * pretends other methods work
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EntityViewAccessProviderManagerMock implements EntityViewAccessProviderManager {

   public Set<String> invalidPrefixes = new HashSet<String>();

   public EntityViewAccessProvider getProvider(String prefix) {
      if (invalidPrefixes.contains(prefix)) {         
         return null;
      }
      return new EntityViewAccessProviderMock();
   }

   public void registerProvider(String prefix, EntityViewAccessProvider provider) {
      // Okey dokey, do nothing
   }

   public void unregisterProvider(String prefix) {
      // Okey dokey, do nothing
   }

}
