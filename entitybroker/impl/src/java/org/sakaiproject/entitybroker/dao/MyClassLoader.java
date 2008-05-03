/**
 * $Id$
 * $URL$
 * ClassloaderRetriever.java - entity-broker - May 3, 2008 5:48:50 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.dao;

/**
 * This simply has a method to return the classloader for this class<br/>
 * Just make a bean in spring like this:
 * <xmp><bean factory-bean="org.sakaiproject.entitybroker.dao.MyClassLoader" factory-method="getMyClassLoader" /></xmp>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class MyClassLoader {

   public static ClassLoader getMyClassLoader() {
      return MyClassLoader.class.getClassLoader();
   }

}
