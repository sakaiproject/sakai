/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.component.message.dao.impl;

import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import uk.ac.cam.caret.sakai.rwiki.message.model.RwikiMessageImpl;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.MessageDao;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Message;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

/**
 * @author ieb
 */
public class MessageDaoImpl extends HibernateDaoSupport implements MessageDao {
	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.MessageDao#createMessage(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public Message createMessage(String pageSpace, String pageName,
			String sessionid, String user, String message)
	{
		Message m = new RwikiMessageImpl();
		m.setLastseen(new Date());
		m.setPagename(pageName);
		m.setPagespace(pageSpace);
		m.setSessionid(sessionid);
		m.setUser(user);
		m.setMessage(message);
		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.MessageDao#findBySpace(java.lang.String)
	 */
	public List findBySpace(final String pageSpace)
	{
		long start = System.currentTimeMillis();
		try
		{
			// there is no point in sorting by version, since there is only one
			// version in
			// this table.
			// also using like is much slower than eq
			HibernateCallback callback = new HibernateCallback()
			{
				public Object doInHibernate(Session session)
						throws HibernateException
				{
					return session.createCriteria(Message.class).add(
							Expression.eq("pagespace", pageSpace)).list();
				}
			};
			return (List) getHibernateTemplate().execute(callback);
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("PagePresenceDaoImpl.findBySpace: "
					+ pageSpace, start, finish);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.MessageDao#findByPage(java.lang.String,
	 *      java.lang.String)
	 */
	public List findByPage(final String pageSpace, final String pageName)
	{
		long start = System.currentTimeMillis();
		try
		{
			// there is no point in sorting by version, since there is only one
			// version in
			// this table.
			// also using like is much slower than eq
			HibernateCallback callback = new HibernateCallback()
			{
				public Object doInHibernate(Session session)
						throws HibernateException
				{
					return session.createCriteria(Message.class).add(
							Expression.eq("pagespace", pageSpace)).add(
							Expression.eq("pagename", pageName)).list();
				}
			};
			return (List) getHibernateTemplate().execute(callback);
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("PagePresenceDaoImpl.findByPage: "
					+ pageSpace + ":" + pageName, start, finish);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.MessageDao#findByUser(java.lang.String)
	 */
	public List findByUser(final String user)
	{
		long start = System.currentTimeMillis();
		try
		{
			// there is no point in sorting by version, since there is only one
			// version in
			// this table.
			// also using like is much slower than eq
			HibernateCallback callback = new HibernateCallback()
			{
				public Object doInHibernate(Session session)
						throws HibernateException
				{
					return session.createCriteria(Message.class).add(
							Expression.eq("user", user)).list();
				}
			};
			return (List) getHibernateTemplate().execute(callback);
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("PagePresenceDaoImpl.findByUser: " + user,
					start, finish);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.MessageDao#update(java.lang.Object)
	 */
	public void update(Object o)
	{
		getHibernateTemplate().saveOrUpdate(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.MessageDao#findBySession(java.lang.String)
	 */
	public List findBySession(String session)
	{
		long start = System.currentTimeMillis();
		try
		{
			// there is no point in sorting by version, since there is only one
			// version in
			// this table.
			// also using like is much slower than eq
			HibernateCallback callback = new HibernateCallback()
			{
				public Object doInHibernate(Session session)
						throws HibernateException
				{
					return session.createCriteria(Message.class).add(
							Expression.eq("sessionid", session)).list();
				}
			};
			return (List) getHibernateTemplate().execute(callback);
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("PagePresenceDaoImpl.findBySession: "
					+ session, start, finish);
		}
	}

}
