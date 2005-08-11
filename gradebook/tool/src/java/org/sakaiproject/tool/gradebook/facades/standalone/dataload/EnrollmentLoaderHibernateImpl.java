/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.facades.standalone.EnrollmentStandalone;
import org.sakaiproject.tool.gradebook.facades.standalone.UserStandalone;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * Loads enrollment data via Hibernate for testing
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class EnrollmentLoaderHibernateImpl extends HibernateDaoSupport implements EnrollmentLoader {
    private static final Log log = LogFactory.getLog(EnrollmentLoaderHibernateImpl.class);

	public void loadEnrollments() {
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query q = session.createQuery("select user from UserStandalone user where user.userUid like :uidprefix order by user.id");
				q.setString("uidprefix", UserLoader.AUTHID_STUDENT_PREFIX + "%");
				List users = q.list();
				List gradebooks = session.find("select gb from Gradebook gb order by gb.id");
				for (int i = 0; i < users.size(); i++) {
					UserStandalone user = (UserStandalone)users.get(i);

					// Everyone is added to Gradebook 8.
					session.save(new EnrollmentStandalone(user, (Gradebook)gradebooks.get(7)));

					// The first 150 students are added to Gradebook 7.
					if (i < 150) {
						session.save(new EnrollmentStandalone(user, (Gradebook)gradebooks.get(6)));

						// The first 50 students are added to Gradebooks 5 and 6, but 6 contains a special student....
						if (i < 50) {
							session.save(new EnrollmentStandalone(user, (Gradebook)gradebooks.get(4)));
							if (i < 49) {
								session.save(new EnrollmentStandalone(user, (Gradebook)gradebooks.get(5)));

								// The first 10 students are added to Gradebooks 2, 3, and 4.
								if (i < 10) {
									session.save(new EnrollmentStandalone(user, (Gradebook)gradebooks.get(3)));
									session.save(new EnrollmentStandalone(user, (Gradebook)gradebooks.get(2)));
									session.save(new EnrollmentStandalone(user, (Gradebook)gradebooks.get(1)));
								}
							}
						}
					}
				}
				return null;
			}
		});
	}

}



