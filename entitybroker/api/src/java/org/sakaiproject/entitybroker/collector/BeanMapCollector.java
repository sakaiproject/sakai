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
