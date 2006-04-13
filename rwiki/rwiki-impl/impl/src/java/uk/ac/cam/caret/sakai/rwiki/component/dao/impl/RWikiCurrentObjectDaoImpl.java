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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Expression;
import net.sf.hibernate.expression.Order;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

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
							.find(
									"select count(*) from RWikiCurrentObjectImpl r where r.name = ?",
									name, Hibernate.STRING);
				}

			};

			Integer count = (Integer) getHibernateTemplate().executeFind(
					callback).get(0);

			return (count.intValue() > 0);
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("RWikiObjectDaoImpl.exists: " + name, start,
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
							.add(Expression.eq("name", name)).list();
				}
			};
			List found = (List) getHibernateTemplate().execute(callback);
			if (found.size() == 0)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Found " + found.size() + " objects with name "
							+ name);
				}
				return null;
			}
			if (log.isDebugEnabled())
			{
				log.debug("Found " + found.size() + " objects with name "
						+ name + " returning most recent one.");
			}
			return (RWikiCurrentObject) proxyObject(found.get(0));
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("RWikiObjectDaoImpl.findByGlobalName: "
					+ name, start, finish);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List findByGlobalNameAndContents(final String criteria,
			final String user, final String realm)
	{

		String[] criterias = criteria.split("\\s\\s*");

		final StringBuffer expression = new StringBuffer();
		final List criteriaList = new ArrayList();
		criteriaList.add(realm);
		criteriaList.add("%" + criteria.toLowerCase() + "%");
		criteriaList.add("%" + criteria.toLowerCase() + "%");

		// WARNING: In MySQL like does not produce a case sensitive search so
		// this is Ok
		// Oracle can probaly do it, but would need some set up (maybee)
		// http://asktom.oracle.com/pls/ask/f?p=4950:8:::::F4950_P8_DISPLAYID:16370675423662

		for (int i = 0; i < criterias.length; i++)
		{
			if (!criterias[i].equals(""))
			{
				expression.append(" or lower(c.content) like ? ");
				criteriaList.add("%" + criterias[i].toLowerCase() + "%");
			}
		}
		if (criteria.equals(""))
		{
			expression.append(" or lower(c.content) like ? ");
			criteriaList.add("%%");
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
				return session.find("select distinct r "
						+ "		from RWikiCurrentObjectImpl as r, "
						+ "			RWikiCurrentObjectContentImpl as c "
						+ "   where " + " r.realm = ? and ("
						+ " lower(r.name) like ? or "
						+ "          lower(c.content) like ? "
						+ expression.toString() + " ) and "
						+ "			r.id = c.rwikiid " + "  order by r.name ",
						criteriaList.toArray(), types);

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
		// FIXME internationalize!!

		returnable.setContent("No page content exists for this "
				+ "page, please create");
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
						Expression.ge("version", since)).add(
						Expression.eq("realm", realm)).addOrder(
						Order.desc("version")).list();
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
				return session.find("select r.name "
						+ "from RWikiCurrentObjectImpl r "
						+ "where referenced like ?", "%::" + name + "::%",
						Hibernate.STRING);
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
									Expression.eq("id", reference
											.getRwikiobjectid())).list();
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
			return (RWikiCurrentObject) proxyObject(found.get(0));
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer(
					"RWikiCurrentObjectDaoImpl.getRWikiCurrentObject: "
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
						.addOrder(Order.desc("version")).list();
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
					return session.find("select count(*) "
							+ "from RWikiCurrentObjectImpl r "
							+ "where r.realm = ?", group, Hibernate.STRING);
				}

			};

			Integer count = (Integer) getHibernateTemplate().executeFind(
					callback).get(0);

			return count.intValue();
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("RWikiObjectDaoImpl.getPageCount: " + group,
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
				String search = globalParentPageName.replaceAll("([A%_])",
						"A$1");
				return session.find("from RWikiCurrentObjectImpl as r "
						+ "where r.name like concat(?,'%') escape 'A' "
						+ "order by name asc", search, Hibernate.STRING);
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
				String search = globalParentPageName.replaceAll("([A%_])",
						"A$1");
				return session.find("from RWikiCurrentObjectImpl as r "
						+ "where r.name like concat(?,'%') escape 'A' "
						+ "order by name desc", search, Hibernate.STRING);
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
				String search = basepath.replaceAll("([A%_])", "A$1");
				return session.find("from RWikiCurrentObjectImpl as r "
						+ "where r.name like concat(?,'%') escape 'A' "
						+ "and r.version >= ? "
						+ "order by r.version desc, r.name asc", new Object[] {
						search, time }, new Type[] { Hibernate.STRING,
						Hibernate.DATE });
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
				return session.find("select r.name "
						+ "from RWikiCurrentObjectImpl  r ");
			}
		};
		return (List) getHibernateTemplate().execute(callback);
	}

}
