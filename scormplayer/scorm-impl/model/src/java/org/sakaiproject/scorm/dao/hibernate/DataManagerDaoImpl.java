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

import lombok.extern.slf4j.Slf4j;

import org.adl.datamodels.IDataManager;
import org.adl.datamodels.SCODataManager;

import org.sakaiproject.scorm.dao.api.DataManagerDao;

import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

@Slf4j
public class DataManagerDaoImpl extends HibernateDaoSupport implements DataManagerDao
{
	@Override
	public List<IDataManager> find(long contentPackageId, String learnerId, long attemptNumber)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("from ").append(SCODataManager.class.getName()).append(" where contentPackageId=:cpid and userId=:lid and attemptNumber=:number ");
		return (List<IDataManager>) getHibernateTemplate().getSessionFactory().getCurrentSession()
				.createQuery(buffer.toString())
				.setParameter("cpid", contentPackageId)
				.setParameter("lid", learnerId)
				.setParameter("number", attemptNumber)
				.getResultList();
	}

	@Override
	public IDataManager find(long contentPackageId, String learnerId, long attemptNumber, String scoId)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("from ").append(SCODataManager.class.getName()).append(" where contentPackageId=:cpid and userId=:lid and attemptNumber=:number and scoId=:scoid");
		List r = getHibernateTemplate().getSessionFactory().getCurrentSession()
				.createQuery(buffer.toString())
				.setParameter("cpid", contentPackageId)
				.setParameter("lid", learnerId)
				.setParameter("number", attemptNumber)
				.setParameter("scoid", scoId)
				.getResultList();

		if (r.isEmpty())
		{
			return null;
		}

		SCODataManager dm = (SCODataManager) r.get(0);
		return dm;
	}

	public List<IDataManager> find(String courseId)
	{
		List r = getHibernateTemplate().getSessionFactory().getCurrentSession()
				.createQuery("from " + SCODataManager.class.getName() + " where courseId=:cid ")
				.setParameter("cid", courseId)
				.getResultList();
		return r;
	}

	@Override
	public IDataManager find(String courseId, String scoId, String userId, boolean fetchAll, long attemptNumber)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("from ").append(SCODataManager.class.getName());

		if (fetchAll)
		{
			buffer.append(" fetch all properties ");
		}

		buffer.append(" where courseId=:cid and scoId=:scoid and userId=:uid and attemptNumber=:number ");
		List r = getHibernateTemplate().getSessionFactory().getCurrentSession()
				.createQuery(buffer.toString())
				.setParameter("cid", courseId)
				.setParameter("scoid", scoId)
				.setParameter("uid", userId)
				.setParameter("number", attemptNumber)
				.getResultList();


		log.debug("DataManagerDaoImpl::find: records: {}", r.size());

		if (r.isEmpty())
		{
			return null;
		}

		SCODataManager dm = (SCODataManager) r.get(r.size() - 1);
		return dm;
	}

	@Override
	public IDataManager find(String courseId, String scoId, String userId, long attemptNumber)
	{
		return find(courseId, scoId, userId, true, attemptNumber);
	}

	@Override
	public IDataManager findByActivityId(long contentPackageId, String activityId, String userId, long attemptNumber)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("from ").append(SCODataManager.class.getName());
		buffer.append(" where contentPackageId=:cpid and activityId=:aid and userId=:uid and attemptNumber=:number ");
		List r = getHibernateTemplate().getSessionFactory().getCurrentSession()
				.createQuery(buffer.toString())
				.setParameter("cpid", contentPackageId)
				.setParameter("aid", activityId)
				.setParameter("uid", userId)
				.setParameter("number", attemptNumber)
				.getResultList();

		log.debug("DataManagerDaoImpl::findByActivityId: records: {}", r.size());

		if (r.isEmpty())
		{
			return null;
		}

		SCODataManager dm = (SCODataManager) r.get(r.size() - 1);
		return dm;
	}

	@Override
	public IDataManager load(long id)
	{
		return (IDataManager) getHibernateTemplate().load(SCODataManager.class, id);
	}

	@Override
	public void save(IDataManager dataManager)
	{
		saveOrUpdate(dataManager, true);
	}

	private void saveOrUpdate(boolean isFirstTime, Object object)
	{
		getHibernateTemplate().saveOrUpdate(object);
	}

	private void saveOrUpdate(IDataManager dataManager, boolean isFirstTime)
	{
		dataManager.setLastModifiedDate(new Date());
		saveOrUpdate(isFirstTime, dataManager);
	}

	@Override
	public void update(IDataManager dataManager)
	{
		saveOrUpdate(dataManager, false);
	}
}
