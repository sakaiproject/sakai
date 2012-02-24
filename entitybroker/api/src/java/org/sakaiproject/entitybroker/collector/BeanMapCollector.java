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
import java.util.Map;


/**
 * Implement this in order to cause spring to inject a map of type -> a set of beans 
 * into your spring bean (if any exist) which implement the type interfaces you define 
 * (those interfaces will also have to implement {@link AutoRegister})
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface BeanMapCollector {

   /**
    * This setter will be called automatically and the beans which are being collected for
    * you will be placed in the map such that it is a map of class type -> list of beans of that type
    * @param collectedBeans a list of all collected beans which were autoregistered
    */
   public void setCollectedBeansMap(Map<Class<?>, List<?>> collectedBeans);

   /**
    * This allows the developer to set the types of the beans which they want collected
    * @return the class type of the beans to collect
    */
   public Class<?>[] getCollectedTypes();

}
