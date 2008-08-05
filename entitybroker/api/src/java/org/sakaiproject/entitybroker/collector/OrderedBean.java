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
