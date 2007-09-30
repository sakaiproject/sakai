/**
 * BeanMapCollector.java - entity-broker - 2007 Sep 29, 2007 12:14:18 PM - azeckoski
 */

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
