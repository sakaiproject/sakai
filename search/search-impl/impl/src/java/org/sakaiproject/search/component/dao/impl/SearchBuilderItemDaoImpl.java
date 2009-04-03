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

package org.sakaiproject.search.component.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.sakaiproject.search.dao.SearchBuilderItemDao;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.model.impl.SearchBuilderItemImpl;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
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
		HibernateTemplate ht = getHibernateTemplate();
		ht.saveOrUpdate(sb);
		ht.flush();

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
		if ( resourceName != null && resourceName.length() > 255 ) {
			dlog.warn("Entity Reference longer than 255 characters :"+resourceName);
			return null;
		}
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
	private int countPending(Connection connection)
	{

		PreparedStatement pst = null;
		ResultSet rst = null;
		try
		{
			pst = connection.prepareStatement("select count(*) from searchbuilderitem where searchstate = ? ");
			pst.clearParameters();
			pst.setInt(1, SearchBuilderItem.STATE_PENDING.intValue());
			rst = pst.executeQuery();
			if (rst.next())
			{
				return rst.getInt(1);
			}
			return 0;
		}
		catch (SQLException sqlex)
		{
			return 0;
		}
		finally
		{
			try
			{
				rst.close();
			}
			catch (Exception ex)
			{
				dlog.warn("Exception counting pending items", ex);
			}
			try
			{
				pst.close();
			}
			catch (Exception ex)
			{
				dlog.warn("Exception counting pending items", ex);
			}
		}

	}
	public int countPending()
	{
		
		
		HibernateCallback callback = new HibernateCallback()
		{
			public Object doInHibernate(Session session)
					throws HibernateException
			{
				return Integer.valueOf(countPending(session.connection()));
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
						Expression.eq("itemscope", SearchBuilderItem.ITEM_GLOBAL_MASTER))
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
						Expression.eq("itemscope", SearchBuilderItem.ITEM_SITE_MASTER)).list();
			}
		};

		return (List) getHibernateTemplate().execute(callback);
	}
}
