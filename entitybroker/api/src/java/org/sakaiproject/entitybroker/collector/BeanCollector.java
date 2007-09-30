/**
 * BeanCollector.java - entity-broker - 2007 Sep 29, 2007 10:42:39 AM - azeckoski
 */

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
