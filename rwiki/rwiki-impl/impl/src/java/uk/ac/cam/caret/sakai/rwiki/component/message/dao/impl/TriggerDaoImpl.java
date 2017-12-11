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

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import uk.ac.cam.caret.sakai.rwiki.message.model.RwikiTriggerImpl;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.TriggerDao;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Trigger;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

/**
 * @author ieb
 */
@Slf4j
public class TriggerDaoImpl extends HibernateDaoSupport implements TriggerDao
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.TriggerDao#createTrigger(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public Trigger createTrigger(String pageName, String pageSpace,
			String triggerSpec, String user)
	{
		Trigger t = new RwikiTriggerImpl();
		t.setLastseen(new Date());
		t.setPagename(pageName);
		t.setPagespace(pageSpace);
		t.setTriggerspec(triggerSpec);
		t.setUser(user);
		return t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.TriggerDao#findByUser(java.lang.String)
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
					return session.createCriteria(Trigger.class).add(
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
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.TriggerDao#findBySpace(java.lang.String)
	 */
	public List findBySpace(final String space)
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
					return session.createCriteria(Trigger.class).add(
							Expression.eq("pagespage", space)).list();
				}
			};
			return (List) getHibernateTemplate().execute(callback);
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("PagePresenceDaoImpl.findBySpace: " + space,
					start, finish);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.TriggerDao#findByPage(java.lang.String,
	 *      java.lang.String)
	 */
	public List findByPage(final String space, final String page)
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
					return session.createCriteria(Trigger.class).add(
							Expression.eq("pagespage", space)).add(
							Expression.eq("pagename", page)).list();
				}
			};
			return (List) getHibernateTemplate().execute(callback);
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("PagePresenceDaoImpl.findByPage: " + space
					+ ":" + page, start, finish);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.TriggerDao#update(java.lang.Object)
	 */
	public void update(Object o)
	{
		getHibernateTemplate().saveOrUpdate(o);
	}

}
