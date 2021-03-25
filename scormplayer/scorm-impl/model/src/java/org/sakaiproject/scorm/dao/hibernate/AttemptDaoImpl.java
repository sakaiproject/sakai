/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;

import org.sakaiproject.scorm.dao.api.AttemptDao;
import org.sakaiproject.scorm.model.api.Attempt;

import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

public class AttemptDaoImpl extends HibernateDaoSupport implements AttemptDao
{
	@Override
	public int count(final long contentPackageId, final String learnerId)
	{
		HibernateCallback hcb = new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				Criteria criteria = session.createCriteria(Attempt.class)
						.add(Restrictions.eq("contentPackageId", contentPackageId))
						.add(Restrictions.eq("learnerId", learnerId))
						.setProjection(Projections.count("id"));
				return criteria.uniqueResult();
			}
		};

		Object result = getHibernateTemplate().execute(hcb);
		int r = 0;
		if (result != null)
		{
			if (result instanceof Number)
			{
				r = ((Number) result).intValue();
			}
		}

		return r;
	}

	@Override
	public List<Attempt> find(long contentPackageId)
	{
		return (List<Attempt>) getHibernateTemplate().getSessionFactory().getCurrentSession()
				.createQuery("from " + Attempt.class.getName() + " where contentPackageId=:cpid ")
				.setParameter("cpid", contentPackageId)
				.getResultList();
	}

	@Override
	public List<Attempt> find(long contentPackageId, String learnerId)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("from ").append(Attempt.class.getName()).append(" where contentPackageId=:cpid and learnerId=:lid order by attemptNumber desc");
		return (List<Attempt>) getHibernateTemplate().getSessionFactory().getCurrentSession()
				.createQuery(buffer.toString())
				.setParameter("cpid", contentPackageId)
				.setParameter("lid", learnerId)
				.getResultList();
	}

	@Override
	public List<Attempt> find(String courseId, String learnerId)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("from ").append(Attempt.class.getName()).append(" where courseId=:cid and learnerId=:lid order by attemptNumber desc");
		return (List<Attempt>) getHibernateTemplate().getSessionFactory().getCurrentSession()
				.createQuery(buffer.toString())
				.setParameter("cid", courseId)
				.setParameter("lid", learnerId)
				.getResultList();
	}

	@Override
	public Attempt find(String courseId, String learnerId, long attemptNumber)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("from ").append(Attempt.class.getName()).append(" where courseId=:cid and learnerId=:lid and attemptNumber=:number ");
		List<Attempt> r = (List<Attempt>) getHibernateTemplate().getSessionFactory().getCurrentSession()
				.createQuery(buffer.toString())
				.setParameter("cid", courseId)
				.setParameter("lid", learnerId)
				.setParameter("number", attemptNumber)
				.getResultList();
		if (r.isEmpty())
		{
			return null;
		}

		return (Attempt) r.get(r.size() - 1);
	}

	@Override
	public Attempt load(long id)
	{
		return (Attempt) getHibernateTemplate().load(Attempt.class, id);
	}

	@Override
	public Attempt lookup(final long contentPackageId, final String learnerId, final long attemptNumber)
	{
		HibernateCallback hcb = new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				StringBuilder buffer = new StringBuilder();
				buffer.append("from ").append(Attempt.class.getName()).append(" where contentPackageId=:contentPackageId and learnerId=:learnerId and attemptNumber=:attemptNumber");

				Query query = session.createQuery(buffer.toString());
				query.setParameter("contentPackageId", contentPackageId);
				query.setParameter("learnerId", learnerId);
				query.setParameter("attemptNumber", attemptNumber);

				return query.uniqueResult();
			}
		};

		Attempt attempt = (Attempt) getHibernateTemplate().execute(hcb);
		return attempt;
	}

	@Override
	public void save(Attempt attempt)
	{
		attempt.setLastModifiedDate(new Date());
		getHibernateTemplate().saveOrUpdate(attempt);
	}

	@Override
	public Attempt lookupNewest(long contentPackageId, String learnerId)
	{
		// First figure out the highest attempt nr..
		DetachedCriteria sub = DetachedCriteria.forClass(Attempt.class)
				.add(Restrictions.eq("contentPackageId", contentPackageId))
				.add(Restrictions.eq("learnerId", learnerId))
				.setProjection(Projections.max("attemptNumber"));

		// Than use it as restriction
		DetachedCriteria criteria = DetachedCriteria.forClass(Attempt.class)
				.add(Restrictions.eq("contentPackageId", contentPackageId))
				.add(Restrictions.eq("learnerId", learnerId))
				.add(Subqueries.propertyEq("attemptNumber", sub));

		return uniqueResult(criteria);
	}

	protected Attempt uniqueResult(final DetachedCriteria criteria)
	{
		return (Attempt)getHibernateTemplate().executeWithNativeSession(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				Criteria s = criteria.getExecutableCriteria(session);
				return s.uniqueResult();
			}
		});
	}
}
