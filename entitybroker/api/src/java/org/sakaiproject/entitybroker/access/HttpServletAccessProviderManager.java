/**
 * $Id$
 * $URL$
 * HttpServletAccessProviderManager.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
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
 * Central manager for all HttpServletAccessProvider implementations. These will be injected from
 * the tool webapps and will come and go unpredictably.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @deprecated Use {@link EntityViewAccessProviderManager} instead
 */
public interface HttpServletAccessProviderManager {

   public void registerProvider(String prefix, HttpServletAccessProvider provider);

   public void unregisterProvider(String prefix, HttpServletAccessProvider provider);

   /**
    * @param prefix an entity prefix
    * @return the provider related to this prefix or null if no provider can be found
    */
   public HttpServletAccessProvider getProvider(String prefix);

}
