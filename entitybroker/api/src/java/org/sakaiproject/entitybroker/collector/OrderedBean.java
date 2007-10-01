/**
 * OrderedBean.java - entity-broker - 2007 Oct 1, 2007 7:36:36 AM - azeckoski
 */

package org.sakaiproject.entitybroker.collector;

/**
 * This sets the order of a bean when it is grabbed by the bean collector
 * and ensures a guaranteed priority order of collected beans,
 * lower orders (numbers) will be loaded first
 * and the orders do not have to be consecutive (there can be gaps)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface OrderedBean extends AutoRegister {

   /**
    * Sets the order to load the bean which implements this method compared
    * to other beans of the same type, lower orders (numbers) will be loaded first
    * (i.e. order 1 will appear before order 3 in the list) and the 
    * orders do not have to be consecutive (there can be gaps), 
    * 2 beans with the same order or beans with no order set will be ordered randomly
    * @return an int which represents the loading order
    */
   public int getOrder();

}
