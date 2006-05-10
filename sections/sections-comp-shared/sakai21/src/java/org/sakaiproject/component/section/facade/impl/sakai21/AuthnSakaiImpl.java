/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California and The Regents of the University of Michigan
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.section.facade.impl.sakai21;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.facade.manager.Authn;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * Uses Sakai's SessionManager to determine the current user's uuid.
 * 
 * @author <a href="jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class AuthnSakaiImpl implements Authn {
    private static final Log log = LogFactory.getLog(AuthnSakaiImpl.class);

    /**
     * @see org.sakaiproject.api.section.facade.managers.Authn#getUserUid()
     */
    public String getUserUid(Object request) {
        Session session = SessionManager.getCurrentSession();
        String userId = session.getUserId();
        if(log.isDebugEnabled()) log.debug("current user id is " + userId);
        return userId;
    }

}
