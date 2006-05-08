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

package org.sakaiproject.search.component.dao.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.type.Type;
import org.sakaiproject.search.dao.SearchBuilderItemDao;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.model.impl.SearchBuilderItemImpl;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author ieb
 */
public class SearchBuilderItemDaoImpl extends HibernateDaoSupport implements
		SearchBuilderItemDao
{

	/**
	 * debug logger
	 */
	private static Log dlog = LogFactory.getLog(SearchBuilderItemDaoImpl.class);

	/**
	 * create a new search builder item {@inheritDoc}
	 */
	public SearchBuilderItem create()
	{
		return new SearchBuilderItemImpl();
	}

	/**
	 * update a search Builder item
	 * 
	 * @{inheritDoc}
	 */
	public void update(SearchBuilderItem sb)
	{
		getHibernateTemplate().saveOrUpdate(sb);

	}

	/**
	 * reset all the documents to cause a reload of current state
	 * 
	 * @{inheritDoc}
	 */
	public List getAll()
	{
		HibernateCallback callback = new HibernateCallback()
		{
			public Object doInHibernate(Session session)
					throws HibernateException
			{
				return session.createCriteria(SearchBuilderItemImpl.class)
						.list();
			}
		};

		return (List) getHibernateTemplate().execute(callback);
	}

	/**
	 * locate a document by name, to modify it
	 * 
	 * @{inheritDoc}
	 */
	public SearchBuilderItem findByName(final String resourceName)
	{
		HibernateCallback callback = new HibernateCallback()
		{
			public Object doInHibernate(Session session)
					throws HibernateException
			{
				return session.createCriteria(SearchBuilderItemImpl.class).add(
						Expression.eq("name", resourceName)).list();
			}

		};
		List l = (List) getHibernateTemplate().execute(callback);
		if (l.size() == 0)
		{
			return null;
		}
		else
		{
			return (SearchBuilderItem) l.get(0);
		}

	}

	public int countPending()
	{

		HibernateCallback callback = new HibernateCallback()
		{
			public Object doInHibernate(Session session)
					throws HibernateException
			{
				// first try and get and lock the writer mutex

				List l = session
						.createQuery(
								"select count(*) from "
										+ SearchBuilderItemImpl.class.getName()
										+ " where searchstate = ? and searchaction <> ?")
						.setParameters(
								new Object[] { SearchBuilderItem.STATE_PENDING,
										SearchBuilderItem.ACTION_UNKNOWN },
								new Type[] { Hibernate.INTEGER,
										Hibernate.INTEGER }).list();
				if (l == null || l.size() == 0)
				{
					return new Integer(0);
				}
				else
				{
					dlog.debug("Found " + l.get(0) + " Pending Documents ");
					return l.get(0);
				}

			}
		};

		return ((Integer) getHibernateTemplate().execute(callback)).intValue();
	}

	public List getGlobalMasters()
	{
		HibernateCallback callback = new HibernateCallback()
		{
			public Object doInHibernate(Session session)
					throws HibernateException
			{
				return session.createCriteria(SearchBuilderItemImpl.class).add(
						Expression.eq("name", SearchBuilderItem.GLOBAL_MASTER))
						.list();
			}
		};

		return (List) getHibernateTemplate().execute(callback);
	}

	public List getSiteMasters()
	{
		HibernateCallback callback = new HibernateCallback()
		{
			public Object doInHibernate(Session session)
					throws HibernateException
			{
				return session.createCriteria(SearchBuilderItemImpl.class).add(
						Expression.like("name",
								SearchBuilderItem.SITE_MASTER_PATTERN)).add(
						Expression.not(Expression.eq("context",
								SearchBuilderItem.GLOBAL_CONTEXT))).list();
			}
		};

		return (List) getHibernateTemplate().execute(callback);
	}
}
