/**
 * ClassLoaderReporter.java - entity-broker - 2007 Dec 10, 2007 11:47:29 PM - azeckoski
 */

package org.sakaiproject.entitybroker.util;

import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;

/**
 * Allows a bean to report the classloader that is appropriate for it and will be used
 * for dispatching into this beans environment, this is only needed in advanced
 * cases and should not normally be implemented<br/>
 * The primary use case here is to allow someone to set their classloader when they
 * are using a proxied bean or the implementation class is in the wrong classloader<br/>
 * This is primarily used in the case of the {@link HttpServletAccessProvider} and the
 * implementation of that should also implement this interface to be able to specify the 
 * classloader
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ClassLoaderReporter {

   /**
    * @return the classloader that is appropriate for executing methods against this bean
    */
   public ClassLoader getSuitableClassLoader();

}
