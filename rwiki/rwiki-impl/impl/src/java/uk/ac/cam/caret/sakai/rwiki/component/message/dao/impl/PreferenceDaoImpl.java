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
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import uk.ac.cam.caret.sakai.rwiki.message.model.PreferenceImpl;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.PreferenceDao;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Preference;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

/**
 * @author ieb
 */
@Slf4j
@Transactional(readOnly = true)
public class PreferenceDaoImpl implements
		PreferenceDao
{
	@Setter private SessionFactory sessionFactory;

	/**
	 * {@inheritDoc}
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
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PreferenceImpl> cq = cb.createQuery(PreferenceImpl.class);
			Root<PreferenceImpl> root = cq.from(PreferenceImpl.class);

			cq.select(root).where(cb.equal(root.get("userid"), user));

			return session.createQuery(cq).getResultList();
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("PreferenceDaoImpl.findByUserId: " + user,
					start, finish);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.PreferenceDao#createPreference(java.lang.String,
	 *      java.lang.String)
	 */
	public Preference createPreference(String user, String prefcontext,
			String preftype, String preference)
	{
		Preference pref = new PreferenceImpl();
		pref.setLastseen(new Date());
		pref.setPreference(preference);
		pref.setPrefcontext(prefcontext);
		pref.setPreftype(preftype);
		pref.setUserid(user);
		return pref;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.PreferenceDao#update(java.lang.Object)
	 */
	@Transactional
	public void update(Object o)
	{
		sessionFactory.getCurrentSession().saveOrUpdate(o);

	}

	public List findByUser(final String user, final String context)
	{
		long start = System.currentTimeMillis();
		try
		{
			// there is no point in sorting by version, since there is only one
			// version in
			// this table.
			// also using like is much slower than eq
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PreferenceImpl> cq = cb.createQuery(PreferenceImpl.class);
			Root<PreferenceImpl> root = cq.from(PreferenceImpl.class);

			String prefcontext = context + "%";
			cq.select(root).where(
				cb.equal(root.get("userid"), user),
				cb.like(root.get("prefcontext"), prefcontext)
			);

			return session.createQuery(cq).getResultList();
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("PreferenceDaoImpl.findByUser: " + user,
					start, finish);
		}
	}

	public List findByUser(final String user, final String context,
			final String type)
	{
		long start = System.currentTimeMillis();
		try
		{
			// there is no point in sorting by version, since there is only one
			// version in
			// this table.
			// also using like is much slower than eq
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PreferenceImpl> cq = cb.createQuery(PreferenceImpl.class);
			Root<PreferenceImpl> root = cq.from(PreferenceImpl.class);

			String prefcontext = context + "%";
			cq.select(root).where(
				cb.equal(root.get("userid"), user),
				cb.equal(root.get("preftype"), type),
				cb.like(root.get("prefcontext"), prefcontext)
			);

			return session.createQuery(cq).getResultList();
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("PreferenceDaoImpl.findByUser: " + user,
					start, finish);
		}
	}

	public List findExactByUser(final String user, final String context)
	{
		long start = System.currentTimeMillis();
		try
		{
			// there is no point in sorting by version, since there is only one
			// version in
			// this table.
			// also using like is much slower than eq
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PreferenceImpl> cq = cb.createQuery(PreferenceImpl.class);
			Root<PreferenceImpl> root = cq.from(PreferenceImpl.class);

			String prefcontext = context + "%";
			cq.select(root).where(
				cb.equal(root.get("userid"), user),
				cb.like(root.get("prefcontext"), prefcontext)
			);

			return session.createQuery(cq).getResultList();

		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("PreferenceDaoImpl.findExactByUser: " + user,
					start, finish);
		}
	}

	public Preference findExactByUser(final String user, final String context,
			final String type)
	{
		long start = System.currentTimeMillis();
		try
		{
			// there is no point in sorting by version, since there is only one
			// version in
			// this table.
			// also using like is much slower than eq
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PreferenceImpl> cq = cb.createQuery(PreferenceImpl.class);
			Root<PreferenceImpl> root = cq.from(PreferenceImpl.class);

			cq.select(root).where(
				cb.equal(root.get("userid"), user),
				cb.equal(root.get("preftype"), type),
				cb.equal(root.get("prefcontext"), context)
			);

			List found = session.createQuery(cq).getResultList();
			if (found.size() == 0)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Found " + found.size() + " objects with name "
							+ user);
				}
				return null;
			}
			if (log.isDebugEnabled())
			{
				log.debug("Found " + found.size() + " objects with name "
						+ user + " returning most recent one.");
			}
			return (Preference) found.get(0);
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("PreferenceDaoImpl.findExactByUser: " + user,
					start, finish);
		}
	}

	@Transactional
	public int delete(final String user, final String context, final String type)
	{
		List l = findByUser(user, context, type);
		int ndel = 0;
		for (Iterator i = l.iterator(); i.hasNext();)
		{
			Preference p = (Preference) i.next();
			delete(p);
			ndel++;
		}
		return ndel;
	}

	@Transactional
	public int deleteExact(final String user, final String context,
			final String type)
	{
		Preference p = findExactByUser(user, context, type);
		if (p == null) return 0;
		delete(p);
		return 1;
	}

	@Transactional
	public int delete(final Preference pref)
	{

		long start = System.currentTimeMillis();
		try
		{
			// there is no point in sorting by version, since there is only one
			// version in
			// this table.
			// also using like is much slower than eq
			sessionFactory.getCurrentSession().delete(pref);
			return 1;
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("PreferenceDaoImpl.delete: " + pref, start,
					finish);
		}
	}

}
