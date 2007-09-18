/*
 * Created on 24 Aug 2007
 */

package org.sakaiproject.entitybroker.impl.access;

import org.sakaiproject.entitybroker.access.PortletAccessProvider;
import org.sakaiproject.entitybroker.access.PortletAccessProviderManager;

/**
 * A simple implementation of the {@link PortletAccessProviderManager} using weak references.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class PortletAccessProviderManagerImpl extends
      AccessProviderManagerImpl<PortletAccessProvider> implements PortletAccessProviderManager {

}
