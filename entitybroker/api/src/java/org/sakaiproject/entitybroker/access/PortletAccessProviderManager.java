/******************************************************************************
 * HttpServletAccessProviderManager.java - created by antranig on 24 August 2007
 *****************************************************************************/

package org.sakaiproject.entitybroker.access;

/**
 * Central manager for all PortletAccessProvider implementations. These will be injected from the
 * tool webapps or elsewhere and will come and go unpredictably.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
// implements the Global Pan-Handler
public interface PortletAccessProviderManager {

   public void registerProvider(String prefix, PortletAccessProvider provider);

   public void unregisterProvider(String prefix, PortletAccessProvider provider);

   public PortletAccessProvider getProvider(String prefix);

}
