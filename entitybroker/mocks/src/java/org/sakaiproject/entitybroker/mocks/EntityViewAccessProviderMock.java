/**
 * $Id$
 * $URL$
 * EntityViewAccessProviderMock.java - entity-broker - Apr 11, 2008 4:31:51 PM - azeckoski
 **************************************************************************
 * Copyright 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
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
