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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.section.support;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.component.section.UserImpl;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

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
