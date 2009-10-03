/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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
 *
 **********************************************************************************/
package org.sakaiproject.coursemanagement.impl.aop;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.coursemanagement.impl.exception.PermissionException;
import org.springframework.aop.MethodBeforeAdvice;

public class CourseManagementAdministrationAuthzAdvisor implements MethodBeforeAdvice {
	private static final Log log = LogFactory.getLog(CourseManagementAdministrationAuthzAdvisor.class);
	
	SecurityService securityService;
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void before(Method method, Object[] oa, Object obj) throws Throwable {
		if(log.isDebugEnabled()) log.debug("Checking authorization for CM Administration actions");

		// We can't check the standard site- or group- or resource-based authorization for modifying CM data,
		// since CM isn't scoped by sakai references.  So we allow only the super user.
		if(!securityService.isSuperUser()) {
			if(log.isDebugEnabled()) log.debug("Denying access to CM Administration on method " + method);
			throw new PermissionException("Only Sakai super-users (admins) can modify CM data");
		}

		if(log.isDebugEnabled()) log.debug("This user is permitted to use the CM Admin service");
	}
}
