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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.adl.datamodels.IDataManager;
import org.adl.datamodels.SCODataManager;

import org.sakaiproject.scorm.dao.api.DataManagerDao;

import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
public class DataManagerDaoImpl implements DataManagerDao
{
	@Setter private SessionFactory sessionFactory;

	@Override
	public List<IDataManager> find(long contentPackageId, String learnerId, long attemptNumber)
	{
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<IDataManager> query = cb.createQuery(IDataManager.class);
		Root<SCODataManager> root = query.from(SCODataManager.class);
		query.select(root).where(
				cb.equal(root.get("contentPackageId"), contentPackageId),
				cb.equal(root.get("userId"), learnerId),
				cb.equal(root.get("attemptNumber"), attemptNumber));
		return session.createQuery(query).getResultList();
	}

	@Override
	public IDataManager find(long contentPackageId, String learnerId, long attemptNumber, String scoId)
	{
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<SCODataManager> query = cb.createQuery(SCODataManager.class);
		Root<SCODataManager> root = query.from(SCODataManager.class);
		query.select(root).where(
				cb.equal(root.get("contentPackageId"), contentPackageId),
				cb.equal(root.get("userId"), learnerId),
				cb.equal(root.get("attemptNumber"), attemptNumber),
				cb.equal(root.get("scoId"), scoId));
		List<SCODataManager> result = session.createQuery(query).getResultList();
		return result.isEmpty() ? null : result.get(0);
	}

	public List<IDataManager> find(String courseId)
	{
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<IDataManager> query = cb.createQuery(IDataManager.class);
		Root<SCODataManager> root = query.from(SCODataManager.class);
		query.select(root).where(cb.equal(root.get("courseId"), courseId));
		return session.createQuery(query).getResultList();
	}

	@Override
	public IDataManager find(String courseId, String scoId, String userId, boolean fetchAll, long attemptNumber)
	{
		// All basic properties are eager in SCODataManager.hbm.xml, including when fetchAll is requested.
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<SCODataManager> query = cb.createQuery(SCODataManager.class);
		Root<SCODataManager> root = query.from(SCODataManager.class);
		query.select(root).where(
				cb.equal(root.get("courseId"), courseId),
				cb.equal(root.get("scoId"), scoId),
				cb.equal(root.get("userId"), userId),
				cb.equal(root.get("attemptNumber"), attemptNumber));
		List<SCODataManager> result = session.createQuery(query).getResultList();
		log.debug("Data managers found: {}", result.size());
		return result.isEmpty() ? null : result.get(result.size() - 1);
	}

	@Override
	public IDataManager find(String courseId, String scoId, String userId, long attemptNumber)
	{
		return find(courseId, scoId, userId, true, attemptNumber);
	}

	@Override
	public IDataManager findByActivityId(long contentPackageId, String activityId, String userId, long attemptNumber)
	{
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<SCODataManager> query = cb.createQuery(SCODataManager.class);
		Root<SCODataManager> root = query.from(SCODataManager.class);
		query.select(root).where(
				cb.equal(root.get("contentPackageId"), contentPackageId),
				cb.equal(root.get("activityId"), activityId),
				cb.equal(root.get("userId"), userId),
				cb.equal(root.get("attemptNumber"), attemptNumber));
		List<SCODataManager> result = session.createQuery(query).getResultList();
		log.debug("Data managers found: {}", result.size());
		return result.isEmpty() ? null : result.get(result.size() - 1);
	}

	@Override
	public IDataManager load(long id)
	{
		return sessionFactory.getCurrentSession().getReference(SCODataManager.class, id);
	}

	@Override
	public void save(IDataManager dataManager)
	{
		merge(dataManager, true);
	}

	private void merge(boolean isFirstTime, Object object)
	{
		sessionFactory.getCurrentSession().merge(object);
	}

	private void merge(IDataManager dataManager, boolean isFirstTime)
	{
		dataManager.setLastModifiedDate(Date.from(Instant.now()));
		merge(isFirstTime, dataManager);
	}

	@Override
	public void update(IDataManager dataManager)
	{
		merge(dataManager, false);
	}
}
