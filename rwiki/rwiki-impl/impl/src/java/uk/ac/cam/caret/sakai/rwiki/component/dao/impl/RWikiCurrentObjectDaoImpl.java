/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.component.dao.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.type.Type;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import uk.ac.cam.caret.sakai.rwiki.component.Messages;
import uk.ac.cam.caret.sakai.rwiki.model.RWikiCurrentObjectImpl;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.ObjectProxy;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiCurrentObjectDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiHistoryObjectDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiObjectContentDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiCurrentObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiHistoryObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

// FIXME: Component

public class RWikiCurrentObjectDaoImpl extends HibernateDaoSupport implements
		RWikiCurrentObjectDao, ObjectProxy
{
	private static Log log = LogFactory.getLog(RWikiCurrentObjectDaoImpl.class);

	protected RWikiObjectContentDao contentDAO = null;

	protected RWikiHistoryObjectDao historyDAO = null;

	/**
	 * {@inheritDoc}
	 */
	public boolean exists(final String name)
	{
		long start = System.currentTimeMillis();
		try
		{
			HibernateCallback callback = new HibernateCallback()
			{

				public Object doInHibernate(Session session)
						throws HibernateException, SQLException
				{
					return session
							.createQuery(
									"select count(*) from RWikiCurrentObjectImpl r where r.name = ? ") //$NON-NLS-1$
							.setParameter(0, name, Hibernate.STRING).list();
				}

			};

			Integer count = (Integer) getHibernateTemplate().executeFind(
					callback).get(0);

			return (count.intValue() > 0);
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("RWikiObjectDaoImpl.exists: " + name, start, //$NON-NLS-1$
					finish);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public RWikiCurrentObject findByGlobalName(final String name)
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
					return session.createCriteria(RWikiCurrentObject.class)
							.add(Expression.eq("name", name)).list(); //$NON-NLS-1$
				}
			};
			List found = (List) getHibernateTemplate().execute(callback);
			if (found.size() == 0)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Found " + found.size() + " objects with name " //$NON-NLS-1$ //$NON-NLS-2$
							+ name);
				}
				return null;
			}
			if (log.isDebugEnabled())
			{
				log.debug("Found " + found.size() + " objects with name " //$NON-NLS-1$ //$NON-NLS-2$
						+ name + " returning most recent one."); //$NON-NLS-1$
			}
			return (RWikiCurrentObject) proxyObject(found.get(0));
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("RWikiObjectDaoImpl.findByGlobalName: " //$NON-NLS-1$
					+ name, start, finish);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List findByGlobalNameAndContents(final String criteria,
			final String user, final String realm)
	{

		String[] criterias = criteria.split("\\s\\s*"); //$NON-NLS-1$

		final StringBuffer expression = new StringBuffer();
		final List criteriaList = new ArrayList();
		criteriaList.add(realm);
		criteriaList.add("%" + criteria.toLowerCase() + "%"); //$NON-NLS-1$ //$NON-NLS-2$
		criteriaList.add("%" + criteria.toLowerCase() + "%"); //$NON-NLS-1$ //$NON-NLS-2$

		// WARNING: In MySQL like does not produce a case sensitive search so
		// this is Ok
		// Oracle can probaly do it, but would need some set up (maybee)
		// http://asktom.oracle.com/pls/ask/f?p=4950:8:::::F4950_P8_DISPLAYID:16370675423662

		for (int i = 0; i < criterias.length; i++)
		{
			if (!criterias[i].equals("")) //$NON-NLS-1$
			{
				expression.append(" or lower(c.content) like ? "); //$NON-NLS-1$
				criteriaList.add("%" + criterias[i].toLowerCase() + "%"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		if (criteria.equals("")) //$NON-NLS-1$
		{
			expression.append(" or lower(c.content) like ? "); //$NON-NLS-1$
			criteriaList.add("%%"); //$NON-NLS-1$
		}
		final Type[] types = new Type[criteriaList.size()];
		for (int i = 0; i < types.length; i++)
		{
			types[i] = Hibernate.STRING;
		}

		HibernateCallback callback = new HibernateCallback()
		{
			public Object doInHibernate(Session session)
					throws HibernateException
			{
				return session
						.createQuery(
								"select distinct r " //$NON-NLS-1$
										+ "		from RWikiCurrentObjectImpl as r, " //$NON-NLS-1$
										+ "			RWikiCurrentObjectContentImpl as c " //$NON-NLS-1$
										+ "   where r.realm = ? and (" //$NON-NLS-1$
										+ " lower(r.name) like ? or " //$NON-NLS-1$
										+ "          lower(c.content) like ? " //$NON-NLS-1$
										+ expression.toString() + " ) and " //$NON-NLS-1$
										+ "			r.id = c.rwikiid " //$NON-NLS-1$
										+ "  order by r.name ").setParameters( //$NON-NLS-1$
								criteriaList.toArray(), types).list();

			}
		};
		return new ListProxy((List) getHibernateTemplate().execute(callback),
				this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(RWikiCurrentObject rwo, RWikiHistoryObject rwho)
	{
		// should have already checked
		RWikiCurrentObjectImpl impl = (RWikiCurrentObjectImpl) rwo;
		getHibernateTemplate().saveOrUpdate(impl);
		// update the history
		if (rwho != null)
		{
			rwho.setRwikiobjectid(impl.getId());
			historyDAO.update(rwho);
		}
		// remember to save the content, and make certain the contentDAO is set
		// first
		impl.setRwikiObjectContentDao(contentDAO);
		impl.getRWikiObjectContent().setRwikiid(rwo.getId());
		contentDAO.update(impl.getRWikiObjectContent());

	}

	/**
	 * {@inheritDoc}
	 */
	public RWikiCurrentObject createRWikiObject(String name, String realm)
	{

		RWikiCurrentObjectImpl returnable = new RWikiCurrentObjectImpl();
		proxyObject(returnable);
		returnable.setName(name);
		returnable.setRealm(realm);
		returnable.setVersion(new Date());
		returnable.setRevision(new Integer(0));

		returnable.setContent(Messages.getString("RWikiCurrentObjectDaoImpl.30") //$NON-NLS-1$
				+ Messages.getString("RWikiCurrentObjectDaoImpl.31")); //$NON-NLS-1$
		return returnable;
	}

	/**
	 * {@inheritDoc}
	 */
	public List findChangedSince(final Date since, final String realm)
	{
		HibernateCallback callback = new HibernateCallback()
		{
			public Object doInHibernate(Session session)
					throws HibernateException
			{
				return session.createCriteria(RWikiCurrentObject.class).add(
						Expression.ge("version", since)).add( //$NON-NLS-1$
						Expression.eq("realm", realm)).addOrder( //$NON-NLS-1$
						Order.desc("version")).list(); //$NON-NLS-1$
			}
		};
		return new ListProxy((List) getHibernateTemplate().execute(callback),
				this);
	}

	/**
	 * {@inheritDoc}
	 */
	public List findReferencingPages(final String name)
	{
		HibernateCallback callback = new HibernateCallback()
		{
			public Object doInHibernate(Session session)
					throws HibernateException
			{
				return session.createQuery(
						"select r.name " + "from RWikiCurrentObjectImpl r " //$NON-NLS-1$ //$NON-NLS-2$
								+ "where referenced like ?").setParameter(0, //$NON-NLS-1$
						"%::" + name + "::%", Hibernate.STRING).list(); //$NON-NLS-1$ //$NON-NLS-2$
			}
		};
		return new ListProxy((List) getHibernateTemplate().execute(callback),
				this);
	}

	/**
	 * {@inheritDoc}
	 */
	public RWikiCurrentObject getRWikiCurrentObject(final RWikiObject reference)
	{
		long start = System.currentTimeMillis();
		try
		{
			HibernateCallback callback = new HibernateCallback()
			{
				public Object doInHibernate(Session session)
						throws HibernateException
				{
					return session.createCriteria(RWikiCurrentObject.class)
							.add(
									Expression.eq("id", reference //$NON-NLS-1$
											.getRwikiobjectid())).list();
				}
			};
			List found = (List) getHibernateTemplate().execute(callback);
			if (found.size() == 0)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Found " + found.size() + " objects with id " //$NON-NLS-1$ //$NON-NLS-2$
							+ reference.getRwikiobjectid());
				}
				return null;
			}
			if (log.isDebugEnabled())
			{
				log.debug("Found " + found.size() + " objects with id " //$NON-NLS-1$ //$NON-NLS-2$
						+ reference.getRwikiobjectid()
						+ " returning most recent one."); //$NON-NLS-1$
			}
			return (RWikiCurrentObject) proxyObject(found.get(0));
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer(
					"RWikiCurrentObjectDaoImpl.getRWikiCurrentObject: " //$NON-NLS-1$
							+ reference.getName(), start, finish);
		}
	}

	public RWikiObjectContentDao getContentDAO()
	{
		return contentDAO;
	}

	public void setContentDAO(RWikiObjectContentDao contentDAO)
	{
		this.contentDAO = contentDAO;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object proxyObject(Object o)
	{
		if (o != null && o instanceof RWikiCurrentObjectImpl)
		{
			RWikiCurrentObjectImpl rwCo = (RWikiCurrentObjectImpl) o;
			rwCo.setRwikiObjectContentDao(contentDAO);
		}
		return o;
	}

	public RWikiHistoryObjectDao getHistoryDAO()
	{
		return historyDAO;
	}

	public void setHistoryDAO(RWikiHistoryObjectDao historyDAO)
	{
		this.historyDAO = historyDAO;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getAll()
	{
		HibernateCallback callback = new HibernateCallback()
		{
			public Object doInHibernate(Session session)
					throws HibernateException
			{
				return session.createCriteria(RWikiCurrentObject.class)
						.addOrder(Order.desc("version")).list(); //$NON-NLS-1$
			}
		};
		return new ListProxy((List) getHibernateTemplate().execute(callback),
				this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateObject(RWikiObject rwo)
	{
		getHibernateTemplate().saveOrUpdate(rwo);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPageCount(final String group)
	{
		long start = System.currentTimeMillis();
		try
		{
			HibernateCallback callback = new HibernateCallback()
			{

				public Object doInHibernate(Session session)
						throws HibernateException, SQLException
				{
					return session.createQuery(
							"select count(*) " //$NON-NLS-1$
									+ "from RWikiCurrentObjectImpl r " //$NON-NLS-1$
									+ "where r.realm = ?").setParameter(0, //$NON-NLS-1$
							group, Hibernate.STRING).list();
				}

			};

			Integer count = (Integer) getHibernateTemplate().executeFind(
					callback).get(0);

			return count.intValue();
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("RWikiObjectDaoImpl.getPageCount: " + group, //$NON-NLS-1$
					start, finish);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List findRWikiSubPages(final String globalParentPageName)
	{
		HibernateCallback callback = new HibernateCallback()
		{
			public Object doInHibernate(Session session)
					throws HibernateException
			{
				String search = globalParentPageName.replaceAll("([A%_])", //$NON-NLS-1$
						"A$1"); //$NON-NLS-1$
				return session.createQuery(
						"from RWikiCurrentObjectImpl as r " //$NON-NLS-1$
								+ "where r.name like concat(?,'%') escape 'A' " //$NON-NLS-1$
								+ "order by name asc").setParameter(0, search, //$NON-NLS-1$
						Hibernate.STRING).list();
			}
		};
		return new ListProxy((List) getHibernateTemplate().execute(callback),
				this);
	}

	/**
	 * {@inheritDoc}
	 */
	public RWikiObject findLastRWikiSubPage(final String globalParentPageName)
	{
		HibernateCallback callback = new HibernateCallback()
		{
			public Object doInHibernate(Session session)
					throws HibernateException
			{
				String search = globalParentPageName.replaceAll("([A%_])", //$NON-NLS-1$
						"A$1"); //$NON-NLS-1$
				return session.createQuery(
						"from RWikiCurrentObjectImpl as r " //$NON-NLS-1$
								+ "where r.name like concat(?,'%') escape 'A' " //$NON-NLS-1$
								+ "order by name desc").setParameter(0, search, //$NON-NLS-1$
						Hibernate.STRING).list();
			}
		};
		List l = (List) getHibernateTemplate().execute(callback);
		if (l == null || l.size() == 0) return null;
		return (RWikiObject) l.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public List findAllChangedSince(final Date time, final String basepath)
	{
		HibernateCallback callback = new HibernateCallback()
		{
			public Object doInHibernate(Session session)
					throws HibernateException
			{
				String search = basepath.replaceAll("([A%_])", "A$1"); //$NON-NLS-1$ //$NON-NLS-2$
				return session
						.createQuery(
								"from RWikiCurrentObjectImpl as r " //$NON-NLS-1$
										+ "where r.name like concat(?,'%') escape 'A' " //$NON-NLS-1$
										+ "and r.version >= ? " //$NON-NLS-1$
										+ "order by r.version desc, r.name asc") //$NON-NLS-1$
						.setParameters(new Object[] { search, time },
								new Type[] { Hibernate.STRING, Hibernate.DATE })
						.list();
			}
		};
		return new ListProxy((List) getHibernateTemplate().execute(callback),
				this);

	}

	public List findAllPageNames()
	{
		HibernateCallback callback = new HibernateCallback()
		{
			public Object doInHibernate(Session session)
					throws HibernateException
			{
				return session.createQuery(
						"select r.name " + "from RWikiCurrentObjectImpl  r ") //$NON-NLS-1$ //$NON-NLS-2$
						.list();
			}
		};
		return (List) getHibernateTemplate().execute(callback);
	}

}
