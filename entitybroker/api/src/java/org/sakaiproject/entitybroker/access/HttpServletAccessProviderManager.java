/**
 * HttpServletAccessProviderManager.java - created by antranig on 15 May 2007
 **/

package org.sakaiproject.entitybroker.access;

/**
 * Central manager for all HttpServletAccessProvider implementations. These will be injected from
 * the tool webapps and will come and go unpredictably.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public interface HttpServletAccessProviderManager {

   public void registerProvider(String prefix, HttpServletAccessProvider provider);

   public void unregisterProvider(String prefix, HttpServletAccessProvider provider);

   public HttpServletAccessProvider getProvider(String prefix);

}
