/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/ufp/usermembership/trunk/impl/src/java/org/sakaiproject/umem/impl/AuthzImpl.java $
 * $Id: AuthzImpl.java 4297 2007-03-16 12:22:04Z nuno@ufp.pt $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.umem.impl;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.umem.api.Authz;

@Slf4j
public class AuthzImpl implements Authz {

	/** Sakai services */
	private SecurityService			M_secs;

	// ################################################################
	// Spring bean methods
	// ################################################################
	public void setSecurityService(SecurityService securityService) {
		this.M_secs = securityService;
	}

	public void init() {
		log.info("init()");		
		// register functions
		FunctionManager.registerFunction(PERMISSION_UMEM_VIEW);
	}

	// ################################################################
	// Public methods
	// ################################################################
	/* (non-Javadoc)
	 * @see edu.ufp.sakai.tool.usermembership.api.Authz#isUserAbleToViewUmem(java.lang.String)
	 */
	public boolean isUserAbleToViewUmem(String siteId) {
		return isSuperUser() || hasPermission(SiteService.siteReference(siteId), PERMISSION_UMEM_VIEW);
	}
	
	// ################################################################
	// Private methods
	// ################################################################
	private boolean isSuperUser() {
		return M_secs.isSuperUser();
	}

	private boolean hasPermission(String reference, String permission) {
		return M_secs.unlock(permission, reference);
	}
}
