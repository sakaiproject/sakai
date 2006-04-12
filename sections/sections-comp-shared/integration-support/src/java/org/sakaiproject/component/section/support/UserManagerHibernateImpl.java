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

package org.sakaiproject.component.section.support;

import java.sql.SQLException;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.component.section.UserImpl;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class UserManagerHibernateImpl extends HibernateDaoSupport implements UserManager {
	private static Log log = LogFactory.getLog(UserManagerHibernateImpl.class);
	
	public User createUser(final String userUid, final String displayName,
			final String sortName, final String displayId) {
		
		if(log.isDebugEnabled()) log.debug("Creating a user named " + displayName + " with uid=" + userUid);

		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException ,SQLException {
				UserImpl user = new UserImpl(displayName, displayId, sortName, userUid);
				session.save(user);
				return user;
			}
		};
		return (User)getHibernateTemplate().execute(hc);
	}
	
	public User findUser(final String userUid) {
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException ,SQLException {
				Query q = session.getNamedQuery("findUser");
				q.setParameter("userUid", userUid);
				return q.uniqueResult();
			}
		};
		return (User)getHibernateTemplate().execute(hc);
	}

}
