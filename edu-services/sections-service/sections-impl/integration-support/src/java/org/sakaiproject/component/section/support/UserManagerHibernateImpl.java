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
package org.sakaiproject.component.section.support;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.Query;

import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import org.sakaiproject.component.section.UserImpl;
import org.sakaiproject.section.api.coursemanagement.User;

@Slf4j
public class UserManagerHibernateImpl extends HibernateDaoSupport implements UserManager {
	public User createUser(final String userUid, final String displayName,
			final String sortName, final String displayId) {
		
		if(log.isDebugEnabled()) log.debug("Creating a user named {} with uid={}", displayName, userUid);

		HibernateCallback<UserImpl> hc = session -> {
            UserImpl user = new UserImpl(displayName, displayId, sortName, userUid);
            session.save(user);
            return user;
        };
		return getHibernateTemplate().execute(hc);
	}
	
	public User findUser(final String userUid) {
		HibernateCallback<User> hc = session -> {
            Query q = session.getNamedQuery("findUser");
            q.setParameter("userUid", userUid);
            return (User) q.uniqueResult();
        };
		return getHibernateTemplate().execute(hc);
	}

}
