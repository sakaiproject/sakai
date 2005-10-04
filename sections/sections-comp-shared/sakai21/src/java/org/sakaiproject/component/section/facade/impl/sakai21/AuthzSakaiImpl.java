/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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

package org.sakaiproject.component.section.facade.impl.sakai21;

import java.sql.SQLException;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.api.section.facade.manager.Authz;
import org.sakaiproject.service.legacy.realm.Realm;
import org.sakaiproject.service.legacy.realm.cover.RealmService;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * Uses Sakai's SecurityService to determine the current user's site role, or
 * consults the CourseSection membership to determine section role.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class AuthzSakaiImpl extends HibernateDaoSupport implements Authz {
    public static final String INSTRUCTOR_PERMISSION = "site.upd";
    public static final String TA_PERMISSION = "section.ta";
    public static final String STUDENT_PERMISSION = "site.visit";

	/**
	 * @inheritDoc
	 */
	public Role getSiteRole(String userUid, String siteContext) {
		String siteAuthzRef = SakaiUtil.getSiteReference();
        boolean isInstructor = SecurityService.unlock(INSTRUCTOR_PERMISSION, siteAuthzRef);
        boolean isTa = SecurityService.unlock(TA_PERMISSION, siteAuthzRef);
        boolean isStudent = SecurityService.unlock(STUDENT_PERMISSION, siteAuthzRef);

        if(isInstructor) {
           return Role.INSTRUCTOR;
        } else if(isTa) {
            return Role.TA;
        } else if(isStudent) {
           return Role.STUDENT;
        } else {
           return Role.NONE;
        }
	}

	/**
	 * @inheritDoc
	 */
	public Role getSectionRole(final String userUid, final String sectionUuid) {
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query q = session.getNamedQuery("loadSectionParticipation");
				q.setParameter("userUid", userUid);
				q.setParameter("sectionUuid", sectionUuid);
				return q.uniqueResult();
			}
		};
		ParticipationRecord record = (ParticipationRecord)getHibernateTemplate().execute(hc);
		if(record == null) {
			return Role.NONE;
		}
		return record.getRole();
	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
