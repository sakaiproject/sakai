/**
 * $Id$
 * $URL$
 * MockAuthenticationManager.java - sakai-jackrabbit-impl - Apr 21, 2008 1:20:40 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.jcr.test.mock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.user.api.IdPwEvidence;

/**
 * Mocking the authn manager for tests
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class MockAuthenticationManager implements AuthenticationManager {

   private static final Log log = LogFactory.getLog(MockAuthenticationManager.class);

   /* (non-Javadoc)
    * @see org.sakaiproject.user.api.AuthenticationManager#authenticate(org.sakaiproject.user.api.Evidence)
    */
   public Authentication authenticate(Evidence e) throws AuthenticationException {
      if (e != null && e instanceof IdPwEvidence) {
         IdPwEvidence evidence = (IdPwEvidence) e;
         String eid = evidence.getIdentifier();
         if (eid == null)
         {
            log.debug("Authenticate null ");
            return null;
         }
         if (MockTestUser.SUPER.equals(eid))
         {
            log.debug("Authenticate eid=[" + eid + "] as superuser");
            return new org.sakaiproject.util.Authentication(MockTestUser.SUPERUSER.getId(), MockTestUser.SUPERUSER.getEid());
         }
         if (MockTestUser.AUTH.equals(eid))
         {
            log.debug("Authenticate eid=[" + eid + "] as auth");
            return new org.sakaiproject.util.Authentication(MockTestUser.AUTHUSER.getId(), MockTestUser.AUTHUSER.getEid());
         }
      }
      log.debug("Authenticate null ");
      return null;
   }

}
