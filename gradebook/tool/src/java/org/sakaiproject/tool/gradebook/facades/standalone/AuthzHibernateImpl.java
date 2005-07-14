/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.gradebook.facades.standalone;

import java.sql.SQLException;
import java.util.List;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.Role;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * A Hibernate implementation of the authorization service.  When running in
 * standalone mode, this determines whether the user is enrolled or is teaching
 * a course to determine the appropriate authorizations.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class AuthzHibernateImpl extends HibernateDaoSupport implements Authz {
    private static final Log log = LogFactory.getLog(AuthzHibernateImpl.class);

	/**
	 * @see org.sakaiproject.tool.gradebook.facades.Authz#getGradebookRole(java.lang.String, java.lang.String)
	 */
	public Role getGradebookRole(final String gradebookUid, final String userUid) {
        HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
                // Is this a student?
                List enrollments = session.find("from EnrollmentStandalone as enr where enr.gradebook.uid=? and enr.user.userUid=?",
                        new Object[] {gradebookUid, userUid}, new Type[] {Hibernate.STRING, Hibernate.STRING});
                if(enrollments.size() > 0) {
                    return Role.STUDENT;
                }

                // Is this an instructor?
                List teachingAssignments = session.find("from TeachingAssignmentStandalone as ta where ta.gradebook.uid=? and ta.user.userUid=?",
                        new Object[] {gradebookUid, userUid}, new Type[] {Hibernate.STRING, Hibernate.STRING});
                if(teachingAssignments.size() > 0) {
                    return Role.INSTRUCTOR;
                }

                // This user has no role in this gradebook
				return Role.NONE;
			}
        };
        return (Role)getHibernateTemplate().execute(hc);
	}

}



