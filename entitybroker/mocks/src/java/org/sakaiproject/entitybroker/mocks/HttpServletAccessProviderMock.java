/**
 * $Id$
 * $URL$
 * HttpServletAccessProviderMock.java - entity-broker - Apr 6, 2008 12:18:50 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.mocks;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;
import org.springframework.mock.web.MockHttpServletResponse;


/**
 * Pretends to be an access servlet provider for things that use them,
 * will not throw any exceptions or do anything
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@SuppressWarnings("deprecation")
public class HttpServletAccessProviderMock implements HttpServletAccessProvider {

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.access.HttpServletAccessProvider#handleAccess(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.sakaiproject.entitybroker.EntityReference)
    */
   public void handleAccess(HttpServletRequest req, HttpServletResponse res, EntityReference ref) {
      // Okey dokey, do nothing but say all is well
      try {
         res.getWriter().print("HttpServletAccessProviderMock");
      } catch (IOException e) {
         // nothing to do here
      }
      ((MockHttpServletResponse) res).setStatus(HttpServletResponse.SC_OK);
   }

}
