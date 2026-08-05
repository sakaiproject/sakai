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
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import uk.ac.cam.caret.sakai.rwiki.message.model.PagePresenceImpl;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.PagePresenceDao;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.PagePresence;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

/**
 * @author ieb
 */
@Slf4j
public class PagePresenceDaoImpl extends HibernateDaoSupport implements
		PagePresenceDao
{
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
			HibernateCallback<List<PagePresence>> callback = session -> {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<PagePresence> cq = cb.createQuery(PagePresence.class);
				Root<PagePresence> root = cq.from(PagePresence.class);

				cq.select(root)
					.where(cb.equal(root.get("pagespace"), pageSpace))
					.orderBy(cb.desc(root.get("lastseen")));

				return session.createQuery(cq).getResultList();
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
			HibernateCallback<List<PagePresence>> callback = session -> {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<PagePresence> cq = cb.createQuery(PagePresence.class);
				Root<PagePresence> root = cq.from(PagePresence.class);

				cq.select(root)
					.where(cb.and(
						cb.equal(root.get("pagename"), pageName),
						cb.equal(root.get("pagespace"), pageSpace)
					))
				.orderBy(cb.desc(root.get("lastseen")));

				return session.createQuery(cq).getResultList();
			};
			return getHibernateTemplate().execute(callback);
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
			HibernateCallback<List<PagePresence>> callback = session -> {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<PagePresence> cq = cb.createQuery(PagePresence.class);
				Root<PagePresence> root = cq.from(PagePresence.class);

				cq.select(root).where(cb.equal(root.get("user"), user));

				return session.createQuery(cq).getResultList();
			};

			return getHibernateTemplate().execute(callback);
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
			HibernateCallback<List<PagePresence>> callback = session -> {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<PagePresence> cq = cb.createQuery(PagePresence.class);
				Root<PagePresence> root = cq.from(PagePresence.class);

				cq.select(root).where(cb.equal(root.get("sessionid"), sessionid));

				return session.createQuery(cq).getResultList();
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
	@Transactional
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
			HibernateCallback<List<PagePresence>> callback = session -> {
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<PagePresence> cq = cb.createQuery(PagePresence.class);
				Root<PagePresence> root = cq.from(PagePresence.class);
				cq.select(root)
					.where(cb.and(
						cb.equal(root.get("pagespace"), pageSpace),
						cb.notEqual(root.get("pagename"), pageName)
					))
					.orderBy(cb.desc(root.get("lastseen")));
				return session.createQuery(cq).getResultList();
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
