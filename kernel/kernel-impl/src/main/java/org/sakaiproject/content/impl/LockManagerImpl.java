/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import org.sakaiproject.content.api.Lock;
import org.sakaiproject.content.api.LockManager;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
public class LockManagerImpl implements LockManager {

	@Setter private SessionFactory sessionFactory;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.component.legacy.content.LockManagerIntf#lockObject(java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	public void lockObject(String assetId, String qualifierId, String reason, boolean system)
	{
		Lock newLock = findOrCreateLock(assetId, qualifierId, false);
		newLock.setAsset(assetId);
		newLock.setQualifier(qualifierId);
		newLock.setDateAdded(now());
		newLock.setActive(true);
		newLock.setReason(reason);
		newLock.setSystem(true);
		sessionFactory.getCurrentSession().saveOrUpdate(newLock);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.component.legacy.content.LockManagerIntf#removeLock(java.lang.String, java.lang.String)
	 */
	public void removeLock(String assetId, String qualifierId)
	{
		Lock oldLock = findOrCreateLock(assetId, qualifierId, true);
		oldLock.setActive(false);
		oldLock.setDateRemoved(now());
		sessionFactory.getCurrentSession().saveOrUpdate(oldLock);
	}

	protected Lock findLock(String assetId, String qualifierId)
	{
		try
		{
			return (Lock) safePopList(sessionFactory.getCurrentSession()
				.createNamedQuery("getLocks", org.sakaiproject.content.hbm.Lock.class)
				.setParameter("asset", assetId)
				.setParameter("qualifier", qualifierId)
				.list());
		}
		catch (HibernateException e)
		{
			log.debug("lock with assetId={} and qualifierId={} not found: {}", assetId, qualifierId, e.getMessage());
			return null;
		}
	}

	protected Lock findOrCreateLock(String assetId, String qualifierId, boolean expected)
	{
		Lock lock = findLock(assetId, qualifierId);
		if (lock == null)
		{
			if (expected == true)
			{
				log.warn("expected Lock not found: {}, {}", assetId, qualifierId);
			}
			return new org.sakaiproject.content.hbm.Lock();
		}

		if (expected == false && lock.isActive())
		{
			log.warn("Lock not expected, but found anyway: {}, {}", assetId, qualifierId);
		}
		return lock;

	}

	protected Object safePopList(List<?> list)
	{
		if (list == null) return null;
		if (list.size() == 0) return null;
		return list.get(0);
	}

	protected Date now()
	{
		return java.util.Calendar.getInstance().getTime();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.component.legacy.content.LockManagerIntf#getLocks(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Collection<Lock> getLocks(String assetId)
	{
		Collection<Lock> locks = null;
		log.debug("getLocks({})", assetId);
		try
		{
			locks = (List<Lock>) (List<?>) sessionFactory.getCurrentSession()
				.createNamedQuery("getActiveAssets", org.sakaiproject.content.hbm.Lock.class)
				.setParameter("asset", assetId)
				.list();
		}
		catch (HibernateException e)
		{
			log.error(e.getMessage());
			throw new RuntimeException(e);
		}
		if (locks == null) return null;
		if (locks.isEmpty()) return null;
		return locks;
	}

	// TODO create a faster query (don't need all rows)
	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.component.legacy.content.LockManagerIntf#isLocked(java.lang.String)
	 */
	public boolean isLocked(String assetId)
	{
		Collection<Lock> c = getLocks(assetId);
		if (c == null) return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.component.legacy.content.LockManagerIntf#removeAllLocks(java.lang.String)
	 */
	public void removeAllLocks(String qualifier)
	{
		Collection<Lock> locks = getQualifierLocks(qualifier);
		if (locks != null)
		{
			org.hibernate.Session session = sessionFactory.getCurrentSession();
			locks.forEach(session::delete);
		}
	}
	@SuppressWarnings("unchecked")
	protected Collection<Lock> getQualifierLocks(String qualifier)
	{
		Collection<Lock> locks = null;
		log.debug("getLocks({})", qualifier);
		try
		{
			locks = (List<Lock>) (List<?>) sessionFactory.getCurrentSession()
				.createNamedQuery("getActiveQualifierLocks", org.sakaiproject.content.hbm.Lock.class)
				.setParameter("qualifier", qualifier)
				.list();
		}
		catch (HibernateException e)
		{
			log.error(e.getMessage());
			throw new RuntimeException(e);
		}
		if (locks == null) return null;
		if (locks.isEmpty()) return null;
		return locks;
	}
}
