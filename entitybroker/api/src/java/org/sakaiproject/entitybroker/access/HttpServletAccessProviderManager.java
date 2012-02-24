/**
 * $Id$
 * $URL$
 * HttpServletAccessProviderManager.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package org.sakaiproject.entitybroker.access;

/**
 * Central manager for all HttpServletAccessProvider implementations. These will be injected from
 * the tool webapps and will come and go unpredictably.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @deprecated Use {@link EntityViewAccessProviderManager} instead
 */
public interface HttpServletAccessProviderManager {

   public void registerProvider(String prefix, HttpServletAccessProvider provider);

   public void unregisterProvider(String prefix, HttpServletAccessProvider provider);

   /**
    * @param prefix an entity prefix
    * @return the provider related to this prefix or null if no provider can be found
    */
   public HttpServletAccessProvider getProvider(String prefix);

}
