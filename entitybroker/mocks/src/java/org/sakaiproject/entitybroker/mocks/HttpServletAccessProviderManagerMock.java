/**
 * $Id$
 * $URL$
 * HttpServletAccessProviderManagerMock.java - entity-broker - Apr 6, 2008 12:20:45 PM - azeckoski
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

import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;


/**
 * For testing things that use the access provider manager,
 * always return a new {@link HttpServletAccessProviderMock} unless the prefix is invalid,
 * pretends other methods work
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@SuppressWarnings("deprecation")
public class HttpServletAccessProviderManagerMock implements HttpServletAccessProviderManager {

   public Set<String> invalidPrefixes = new HashSet<String>();

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager#getProvider(java.lang.String)
    */
   public HttpServletAccessProvider getProvider(String prefix) {
      if (invalidPrefixes.contains(prefix)) {         
         return null;
      }
      return new HttpServletAccessProviderMock();
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager#registerProvider(java.lang.String, org.sakaiproject.entitybroker.access.HttpServletAccessProvider)
    */
   public void registerProvider(String prefix, HttpServletAccessProvider provider) {
      // Okey dokey, do nothing
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager#unregisterProvider(java.lang.String, org.sakaiproject.entitybroker.access.HttpServletAccessProvider)
    */
   public void unregisterProvider(String prefix, HttpServletAccessProvider provider) {
      // Okey dokey, do nothing
   }

}
