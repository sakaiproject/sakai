/**
 * $Id$
 * $URL$
 * EntityViewAccessProviderMock.java - entity-broker - Apr 11, 2008 4:31:51 PM - azeckoski
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

import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Pretends to be an access servlet provider for things that use them,
 * will not throw any exceptions or do anything
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EntityViewAccessProviderMock implements EntityViewAccessProvider {

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.access.EntityViewAccessProvider#handleAccess(org.sakaiproject.entitybroker.EntityView, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   public void handleAccess(EntityView view, HttpServletRequest req, HttpServletResponse res) {
      // Okey dokey, do nothing but say all is well
      try {
         res.getWriter().print("EntityViewAccessProviderMock");
      } catch (IOException e) {
         // nothing to do here
      }
      ((MockHttpServletResponse) res).setStatus(HttpServletResponse.SC_OK);
   }

}
