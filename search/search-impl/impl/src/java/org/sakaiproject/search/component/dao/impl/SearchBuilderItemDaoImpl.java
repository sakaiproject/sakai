/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2006 University of Cambridge
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

package org.sakaiproject.search.component.dao.impl;

import java.util.List;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Expression;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.dao.SearchBuilderItemDao;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.model.impl.SearchBuilderItemImpl;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

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
						.find(
								"select count(*) from "
										+ SearchBuilderItemImpl.class.getName()
										+ " where name <> ? and searchstate = ? and searchaction <> ?",
								new Object[] { SearchBuilderItem.INDEX_MASTER,
										SearchBuilderItem.STATE_PENDING,
										SearchBuilderItem.ACTION_UNKNOWN },
								new Type[] { Hibernate.STRING,
										Hibernate.INTEGER, Hibernate.INTEGER });
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
}
