/**
 * $Id$
 * $URL$
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
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

package org.sakaiproject.entitybroker.collector;

import java.util.List;

/**
 * Implement this in order to cause spring to inject a set of beans into your spring bean
 * (if any exist) which implement the interface you define (they will also have to
 * implement {@link AutoRegister})
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface BeanCollector<T> {

   /**
    * This setter will be called automatically and the beans which are being collected for
    * you will be placed in the List
    * @param collectedBeans a list of all collected beans which were autoregistered
    */
   public void setCollectedBeans(List<T> collectedBeans);

   /**
    * This allows the developer to set the type of the beans which they want collected
    * @return the class type of the beans to collect
    */
   public Class<T> getCollectedType();

}
