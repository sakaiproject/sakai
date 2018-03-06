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
package uk.ac.cam.caret.sakai.rwiki.component.dao.impl;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import uk.ac.cam.caret.sakai.rwiki.model.RWikiHistoryObjectImpl;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.ObjectProxy;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiHistoryObjectDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiObjectContentDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiCurrentObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiHistoryObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

// FIXME: Component
@Slf4j
public class RWikiHistoryObjectDaoImpl extends HibernateDaoSupport implements
		RWikiHistoryObjectDao, ObjectProxy
{

	private RWikiObjectContentDao contentDAO;

	/**
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.dao.RWikiHistoryObjectDao#update(uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiHistoryObject)
	 */
	public void update(RWikiHistoryObject rwo)
	{
		// should have already checked
		RWikiHistoryObjectImpl impl = (RWikiHistoryObjectImpl) rwo;
		getHibernateTemplate().saveOrUpdate(impl);
		// and remember to save the content
		impl.getRWikiObjectContent().setRwikiid(rwo.getId());
		contentDAO.update(impl.getRWikiObjectContent());

	}

	/**
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.dao.RWikiHistoryObjectDao#createRWikiHistoryObject(uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiCurrentObject)
	 */
	public RWikiHistoryObject createRWikiHistoryObject(RWikiCurrentObject rwo)
	{
		RWikiHistoryObjectImpl returnable = new RWikiHistoryObjectImpl();
		returnable.setRwikiObjectContentDao(contentDAO);
		rwo.copyAllTo(returnable);
		returnable.setRwikiobjectid(rwo.getId());
		return returnable;
	}

	/**
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.dao.RWikiHistoryObjectDao#getRWikiHistoryObject(uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiObject,
	 *      int)
	 */
	public RWikiHistoryObject getRWikiHistoryObject(final RWikiObject rwo,
			final int revision)
	{
		long start = System.currentTimeMillis();
		try
		{
			HibernateCallback callback = new HibernateCallback()
			{
				public Object doInHibernate(Session session)
						throws HibernateException
				{
					return session.createCriteria(RWikiHistoryObject.class)
							.add(
									Expression.eq("rwikiobjectid", rwo
											.getRwikiobjectid())).add(
									Expression.eq("revision", Integer.valueOf(
											revision))).list();
				}
			};
			List found = (List) getHibernateTemplate().execute(callback);
			if (found.size() == 0)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Found " + found.size() + " objects with id "
							+ rwo.getRwikiobjectid());
				}
				return null;
			}
			if (log.isDebugEnabled())
			{
				log.debug("Found " + found.size() + " objects with id "
						+ rwo.getRwikiobjectid()
						+ " returning most recent one.");
			}
			return (RWikiHistoryObject) proxyObject(found.get(0));
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer(
					"RWikiHistoryObjectDaoImpl.getRWikiHistoryObject: "
							+ rwo.getName(), start, finish);
		}

	}

	/**
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.dao.RWikiHistoryObjectDao#findRWikiHistoryObjects(uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiObject)
	 */
	public List findRWikiHistoryObjects(final RWikiObject reference)
	{
		long start = System.currentTimeMillis();
		try
		{
			HibernateCallback callback = new HibernateCallback()
			{
				public Object doInHibernate(Session session)
						throws HibernateException
				{
					return session.createCriteria(RWikiHistoryObject.class)
							.add(
									Expression.eq("rwikiobjectid", reference
											.getRwikiobjectid())).addOrder(
									Order.asc("revision")).list();
				}
			};
			List found = (List) getHibernateTemplate().execute(callback);
			if (found.size() == 0)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Found " + found.size() + " objects with id "
							+ reference.getRwikiobjectid());
				}
				return null;
			}
			if (log.isDebugEnabled())
			{
				log.debug("Found " + found.size() + " objects with id "
						+ reference.getRwikiobjectid()
						+ " returning most recent one.");
			}
			return new ListProxy(found, this);
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer(
					"RWikiHistoryObjectDaoImpl.getRWikiHistoryObjects: "
							+ reference.getName(), start, finish);
		}
	}

	public List findRWikiHistoryObjectsInReverse(final RWikiObject reference)
	{
		long start = System.currentTimeMillis();
		try
		{
			HibernateCallback callback = new HibernateCallback()
			{
				public Object doInHibernate(Session session)
						throws HibernateException
				{
					return session.createCriteria(RWikiHistoryObject.class)
							.add(
									Expression.eq("rwikiobjectid", reference
											.getRwikiobjectid())).addOrder(
									Order.desc("revision")).list();
				}
			};
			List found = (List) getHibernateTemplate().execute(callback);
			if (found.size() == 0)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Found " + found.size() + " objects with id "
							+ reference.getRwikiobjectid());
				}
				return null;
			}
			if (log.isDebugEnabled())
			{
				log.debug("Found " + found.size() + " objects with id "
						+ reference.getRwikiobjectid()
						+ " returning most recent one.");
			}
			return new ListProxy(found, this);
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer(
					"RWikiHistoryObjectDaoImpl.getRWikiHistoryObjects: "
							+ reference.getName(), start, finish);
		}
	}

	public Object proxyObject(Object o)
	{
		if (o != null && o instanceof RWikiHistoryObjectImpl)
		{
			RWikiHistoryObjectImpl rwCo = (RWikiHistoryObjectImpl) o;
			rwCo.setRwikiObjectContentDao(contentDAO);
		}
		return o;
	}

	public RWikiObjectContentDao getContentDAO()
	{
		return contentDAO;
	}

	public void setContentDAO(RWikiObjectContentDao contentDAO)
	{
		this.contentDAO = contentDAO;
	}

	public List getAll()
	{
		HibernateCallback callback = new HibernateCallback()
		{
			public Object doInHibernate(Session session)
					throws HibernateException
			{
				return session.createCriteria(RWikiHistoryObject.class)
						.addOrder(Order.desc("version")).list();
			}
		};
		return new ListProxy((List) getHibernateTemplate().execute(callback),
				this);
	}

	public void updateObject(RWikiObject rwo)
	{
		getHibernateTemplate().saveOrUpdate(rwo);
	}

}
