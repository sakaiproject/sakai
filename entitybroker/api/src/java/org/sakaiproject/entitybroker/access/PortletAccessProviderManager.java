/**
 * $Id$
 * $URL$
 * PortletAccessProviderManager.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 **/

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
