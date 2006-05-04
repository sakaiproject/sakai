/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
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

package uk.ac.cam.caret.sakai.rwiki.component.message.dao.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import uk.ac.cam.caret.sakai.rwiki.message.model.PagePresenceImpl;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.PagePresenceDao;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.PagePresence;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

/**
 * @author ieb
 */
public class PagePresenceDaoImpl extends HibernateDaoSupport implements
		PagePresenceDao
{
	private static Log log = LogFactory.getLog(PagePresenceDaoImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.PagePresenceDao#createPagePresence(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public PagePresence createPagePresence(String pageName, String pageSpace,
			String sessionid, String user)
	{
		PagePresence pp = new PagePresenceImpl();
		pp.setLastseen(new Date());
		pp.setPagename(pageName);
		pp.setPagespace(pageSpace);
		pp.setSessionid(sessionid);
		pp.setUser(user);
		return pp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.PagePresenceDao#findBySpace(java.lang.String)
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
					return session.createCriteria(PagePresence.class).add(
							Expression.eq("pagespace", pageSpace)).addOrder(
							Order.desc("lastseen")).list();
				}
			};
			List l = (List) getHibernateTemplate().execute(callback);
			log.info("Found  " + l.size() + " in " + pageSpace);
			return l;
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
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.PagePresenceDao#findByPage(java.lang.String,
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
					return session.createCriteria(PagePresence.class).add(
							Expression.eq("pagename", pageName)).add(
							Expression.eq("pagespace", pageSpace)).addOrder(
							Order.desc("lastseen")).list();
				}
			};
			return (List) getHibernateTemplate().execute(callback);
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("PagePresenceDaoImpl.findByPage: "
					+ pageSpace + " :" + pageName, start, finish);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.PagePresenceDao#findByUser(java.lang.String)
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
					return session.createCriteria(PagePresence.class).add(
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
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.PagePresenceDao#findBySession(java.lang.String)
	 */
	public PagePresence findBySession(final String sessionid)
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
					return session.createCriteria(PagePresence.class).add(
							Expression.eq("sessionid", sessionid)).list();
				}
			};
			List found = (List) getHibernateTemplate().execute(callback);
			if (found.size() == 0)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Found " + found.size() + " objects with name "
							+ sessionid);
				}
				return null;
			}
			if (log.isDebugEnabled())
			{
				log.debug("Found " + found.size() + " objects with name "
						+ sessionid + " returning most recent one.");
			}
			return (PagePresence) found.get(0);
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("PagePresenceDaoImpl.findBySessionId: "
					+ sessionid, start, finish);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.PagePresenceDao#update(java.lang.Object)
	 */
	public void update(Object o)
	{
		getHibernateTemplate().saveOrUpdate(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.PagePresenceDao#findBySpaceOnly(java.lang.String,
	 *      java.lang.String)
	 */
	public List findBySpaceOnly(final String pageSpace, final String pageName)
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
					return session.createCriteria(PagePresence.class).add(
							Expression.eq("pagespace", pageSpace))
							.add(
									Expression.not(Expression.eq("pagename",
											pageName))).addOrder(
									Order.desc("lastseen")).list();
				}
			};
			List l = (List) getHibernateTemplate().execute(callback);
			log.info("Found " + l.size() + " in " + pageSpace + " : "
					+ pageName);
			return l;
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("PagePresenceDaoImpl.findBySpaceOnly: "
					+ pageSpace + " :" + pageName, start, finish);
		}
	}

}
