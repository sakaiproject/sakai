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

import java.time.Instant;
import java.util.Date;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import lombok.Setter;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.sakaiproject.scorm.dao.api.AttemptDao;
import org.sakaiproject.scorm.model.api.Attempt;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public class AttemptDaoImpl implements AttemptDao
{
	@Setter private SessionFactory sessionFactory;

	@Override
	public int count(final long contentPackageId, final String learnerId)
	{
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<Attempt> root = query.from(Attempt.class);
		query.select(cb.count(root.get("id"))).where(cb.equal(root.get("contentPackageId"), contentPackageId), cb.equal(root.get("learnerId"), learnerId));
		Long result = session.createQuery(query).uniqueResult();
		return result != null ? result.intValue() : 0;
	}

	@Override
	public List<Attempt> find(long contentPackageId)
	{
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Attempt> query = cb.createQuery(Attempt.class);
		Root<Attempt> root = query.from(Attempt.class);
		query.select(root).where(cb.equal(root.get("contentPackageId"), contentPackageId));
		return session.createQuery(query).getResultList();
	}

	@Override
	public List<Attempt> find(long contentPackageId, String learnerId)
	{
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Attempt> query = cb.createQuery(Attempt.class);
		Root<Attempt> root = query.from(Attempt.class);
		query.select(root).where(cb.equal(root.get("contentPackageId"), contentPackageId), cb.equal(root.get("learnerId"), learnerId));
		query.orderBy(cb.desc(root.get("attemptNumber")));
		return session.createQuery(query).getResultList();
	}

	@Override
	public List<Attempt> find(String courseId, String learnerId)
	{
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Attempt> query = cb.createQuery(Attempt.class);
		Root<Attempt> root = query.from(Attempt.class);
		query.select(root).where(cb.equal(root.get("courseId"), courseId), cb.equal(root.get("learnerId"), learnerId));
		query.orderBy(cb.desc(root.get("attemptNumber")));
		return session.createQuery(query).getResultList();
	}

	@Override
	public Attempt find(String courseId, String learnerId, long attemptNumber)
	{
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Attempt> query = cb.createQuery(Attempt.class);
		Root<Attempt> root = query.from(Attempt.class);
		query.select(root).where(
				cb.equal(root.get("courseId"), courseId),
				cb.equal(root.get("learnerId"), learnerId),
				cb.equal(root.get("attemptNumber"), attemptNumber));
		List<Attempt> result = session.createQuery(query).getResultList();
		return result.isEmpty() ? null : result.get(result.size() - 1);
	}

	@Override
	public Attempt load(long id)
	{
		return sessionFactory.getCurrentSession().getReference(Attempt.class, id);
	}

	@Override
	public Attempt lookup(final long contentPackageId, final String learnerId, final long attemptNumber)
	{
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Attempt> query = cb.createQuery(Attempt.class);
		Root<Attempt> root = query.from(Attempt.class);
		query.select(root).where(
				cb.equal(root.get("contentPackageId"), contentPackageId),
				cb.equal(root.get("learnerId"), learnerId),
				cb.equal(root.get("attemptNumber"), attemptNumber));
		return session.createQuery(query).uniqueResult();
	}

	@Override
	public void save(Attempt attempt)
	{
		attempt.setLastModifiedDate(Date.from(Instant.now()));
		sessionFactory.getCurrentSession().merge(attempt);
	}

	@Override
	public Attempt lookupNewest(long contentPackageId, String learnerId)
	{
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Attempt> query = cb.createQuery(Attempt.class);
		Root<Attempt> root = query.from(Attempt.class);
		Subquery<Long> newestAttempt = query.subquery(Long.class);
		Root<Attempt> newestRoot = newestAttempt.from(Attempt.class);
		newestAttempt.select(cb.max(newestRoot.<Long>get("attemptNumber"))).where(
				cb.equal(newestRoot.get("contentPackageId"), contentPackageId), cb.equal(newestRoot.get("learnerId"), learnerId));
		query.select(root).where(cb.equal(root.get("contentPackageId"), contentPackageId),
				cb.equal(root.get("learnerId"), learnerId), cb.equal(root.get("attemptNumber"), newestAttempt));
		List<Attempt> result = session.createQuery(query).getResultList();
		return result.isEmpty() ? null : result.get(0);
	}
}
