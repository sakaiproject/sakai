/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.gradebook.facades.Authn;

/**
 * Sakai2 implementation of the gradebook's Authn facade.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
@Slf4j
public class AuthnSakai2Impl implements Authn {
	/**
	 * @see org.sakaiproject.tool.gradebook.facades.Authn#getUserUid()
	 */
	public String getUserUid() {
        Session session = SessionManager.getCurrentSession();
        String userId = session.getUserId();
        if(log.isDebugEnabled()) log.debug("current user id is " + userId);
        return userId;
    }

    /**
     * In Sakai, the framework maintains its own ThreadLocal
     * user context.
     */
    public void setAuthnContext(Object whatToAuthn) {
    }
}


