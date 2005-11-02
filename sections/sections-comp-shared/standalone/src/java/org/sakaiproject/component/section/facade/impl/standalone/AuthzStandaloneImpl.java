/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California and The Regents of the University of Michigan
*
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.component.section.facade.impl.standalone;

import java.sql.SQLException;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.api.section.facade.manager.Authz;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * A standalone, hibernate-based implementation of the Authz facade.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class AuthzStandaloneImpl extends HibernateDaoSupport implements Authz {
	private static final Log log = LogFactory.getLog(AuthzStandaloneImpl.class);

	private Role getSiteRole(final String userUid, final String siteContext) {
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query q = session.getNamedQuery("loadSiteParticipation");
				q.setParameter("userUid", userUid);
				q.setParameter("siteContext", siteContext);
				return q.uniqueResult();
			}
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
}
