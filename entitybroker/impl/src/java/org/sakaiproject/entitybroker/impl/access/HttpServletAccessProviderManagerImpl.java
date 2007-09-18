/**
 * AccessProviderManagerImpl.java - created by antranig on 14 May 2007
 */

package org.sakaiproject.entitybroker.impl.access;

import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;

/**
 * A simple implementation of the {@link HttpServletAccessProviderManager} using weak references.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class HttpServletAccessProviderManagerImpl extends
      AccessProviderManagerImpl<HttpServletAccessProvider> implements
      HttpServletAccessProviderManager {

}
