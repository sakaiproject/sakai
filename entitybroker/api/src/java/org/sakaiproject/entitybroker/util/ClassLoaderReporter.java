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

package org.sakaiproject.entitybroker.util;

import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;

/**
 * Allows a bean to report the classloader that is appropriate for it and will be used
 * for dispatching into this beans environment, this is only needed in advanced
 * cases and should not normally be implemented<br/>
 * The primary use case here is to allow someone to set their classloader when they
 * are using a proxied bean or the implementation class is in the wrong classloader<br/>
 * This is primarily used in the case of the {@link HttpServletAccessProvider}
 * or {@link EntityViewAccessProvider} 
 * and the implementations of those should also implement this interface 
 * to be able to specify the classloader
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ClassLoaderReporter {

   /**
    * @return the classloader that is appropriate for executing methods against this bean
    */
   public ClassLoader getSuitableClassLoader();

}
