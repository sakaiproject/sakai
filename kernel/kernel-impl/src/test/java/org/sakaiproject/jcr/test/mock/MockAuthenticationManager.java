/**
 * $Id$
 * $URL$
 * MockAuthenticationManager.java - sakai-jackrabbit-impl - Apr 21, 2008 1:20:40 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
