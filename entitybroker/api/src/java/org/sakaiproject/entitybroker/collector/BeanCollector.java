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
