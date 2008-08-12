/**
 * $Id$
 * $URL$
 * HttpServletAccessProviderManagerMock.java - entity-broker - Apr 6, 2008 12:20:45 PM - azeckoski
 **************************************************************************
 * Copyright 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
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
