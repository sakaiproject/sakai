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

package org.sakaiproject.tool.gradebook.facades.standalone.dataload;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.facades.standalone.EnrollmentStandalone;
import org.sakaiproject.tool.gradebook.facades.standalone.TeachingAssignmentStandalone;
import org.sakaiproject.tool.gradebook.facades.standalone.UserStandalone;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * Loads teaching assignment data via Hibernate for testing
 */
public class TeachingAssignmentLoaderHibernateImpl extends HibernateDaoSupport implements TeachingAssignmentLoader {
	private static final Log log = LogFactory.getLog(TeachingAssignmentLoaderHibernateImpl.class);

    public static String GRADEBOOK_TA_TEACHER = "QA_2";
    public static String GRADEBOOK_TA_STUDENT = "QA_6";

	public void loadTeachingAssignments() {
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				List users;

				users = session.find("select user from UserStandalone user where user.userUid=?", UserLoader.AUTHID_TEACHER_ALL, Hibernate.STRING);
				UserStandalone teacherAll = (UserStandalone)users.get(0);

				users = session.find("select user from UserStandalone user where user.userUid=?", UserLoader.AUTHID_TEACHER_AND_STUDENT, Hibernate.STRING);
				UserStandalone teacherStudent = (UserStandalone)users.get(0);

				List gradebooks = session.find("select gb from Gradebook gb order by gb.id");

				for (Iterator iter = gradebooks.iterator(); iter.hasNext(); ) {
					Gradebook gradebook = (Gradebook)iter.next();
					session.save(new TeachingAssignmentStandalone(teacherAll, gradebook));

					if (gradebook.getUid().equals(GRADEBOOK_TA_TEACHER)) {
						session.save(new TeachingAssignmentStandalone(teacherStudent, gradebook));
					} else if (gradebook.getUid().equals(GRADEBOOK_TA_STUDENT)) {
						session.save(new EnrollmentStandalone(teacherStudent, gradebook));
					}
				}
				return null;
			}
		});
	}
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
