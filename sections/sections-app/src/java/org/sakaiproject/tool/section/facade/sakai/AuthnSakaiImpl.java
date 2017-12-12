/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.section.facade.sakai;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.section.api.facade.manager.Authn;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * Uses Sakai's SessionManager to determine the current user's uuid.
 * 
 * @author <a href="jholtzman@berkeley.edu">Josh Holtzman</a>
 */
@Slf4j
public class AuthnSakaiImpl implements Authn {

    /**
     * @see org.sakaiproject.section.api.facade.managers.Authn#getUserUid()
     */
    public String getUserUid(Object request) {
        Session session = SessionManager.getCurrentSession();
        String userId = session.getUserId();
        if(log.isDebugEnabled()) log.debug("current user id is " + userId);
        return userId;
    }

}
