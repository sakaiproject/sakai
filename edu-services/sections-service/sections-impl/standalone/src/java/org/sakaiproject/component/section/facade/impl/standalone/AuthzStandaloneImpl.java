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
package org.sakaiproject.component.section.facade.impl.standalone;

import org.hibernate.Query;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.section.api.facade.manager.Authz;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import lombok.extern.slf4j.Slf4j;

/**
 * A standalone, hibernate-based implementation of the Authz facade.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class AuthzStandaloneImpl extends HibernateDaoSupport implements Authz {

	private Role getSiteRole(final String userUid, final String siteContext) {
		HibernateCallback hc = session -> {
            Query q = session.getNamedQuery("loadSiteParticipation");
            q.setParameter("userUid", userUid);
            q.setParameter("siteContext", siteContext);
            return q.uniqueResult();
        };
		Object result = getHibernateTemplate().execute(hc);
		if(result == null) {
			if(log.isDebugEnabled()) log.debug(userUid + " is not a member of the course at site context " + siteContext);
			return Role.NONE;
		}
		return((ParticipationRecord)result).getRole();
	}

	public boolean isSectionManagementAllowed(String userUid, String siteContext) {
		Role role = getSiteRole(userUid, siteContext);
		return role.isInstructor();
	}

	public boolean isSectionOptionsManagementAllowed(String userUid, String siteContext) {
		return isSectionManagementAllowed(userUid, siteContext);
	}

	public boolean isSectionEnrollmentMangementAllowed(String userUid, String siteContext) {
		Role role = getSiteRole(userUid, siteContext);
		return role.isInstructor() || role.isTeachingAssistant();
	}

	public boolean isSectionTaManagementAllowed(String userUid, String siteContext) {
		return isSectionManagementAllowed(userUid, siteContext);
	}

	public boolean isViewOwnSectionsAllowed(String userUid, String siteContext) {
		Role role = getSiteRole(userUid, siteContext);
		return role.isStudent();
	}

	public boolean isViewAllSectionsAllowed(String userUid, String siteContext) {
		Role role = getSiteRole(userUid, siteContext);
		return role.isInstructor() || role.isTeachingAssistant();
	}

	public boolean isSectionAssignable(String userUid, String siteContext) {
		Role role = getSiteRole(userUid, siteContext);
		return role.isTeachingAssistant() || role.isStudent();
	}

	public String getRoleDescription(String userUid, String siteContext) {
		Role role = getSiteRole(userUid, siteContext);
		if(role.isInstructor()) {
			return "Instructor";
		} else if(role.isStudent()) {
			return "Student";
		} else if(role.isTeachingAssistant()) {
			return "Teaching Assistant";
		} else {
			return null;
		}
	}
	
}
