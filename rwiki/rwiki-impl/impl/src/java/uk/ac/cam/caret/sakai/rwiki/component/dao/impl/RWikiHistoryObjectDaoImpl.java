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
package uk.ac.cam.caret.sakai.rwiki.component.dao.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import uk.ac.cam.caret.sakai.rwiki.model.RWikiHistoryObjectImpl;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.ObjectProxy;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiHistoryObjectDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiObjectContentDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiCurrentObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiHistoryObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

// FIXME: Component

public class RWikiHistoryObjectDaoImpl extends HibernateDaoSupport implements
		RWikiHistoryObjectDao, ObjectProxy
{

	private RWikiObjectContentDao contentDAO;

	private static Log log = LogFactory.getLog(RWikiHistoryObjectDaoImpl.class);

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
									Expression.eq("revision", new Integer(
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
