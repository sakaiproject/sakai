/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.component.common.type;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.api.common.type.TypeManager;
import org.sakaiproject.component.common.manager.PersistableHelper;
import org.sakaiproject.id.cover.IdManager;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
@Slf4j
public class TypeManagerImpl extends HibernateDaoSupport implements TypeManager
{
	private static final String ID = "id";

	private static final String FINDTYPEBYID = "findTypeById";

	private static final String UUID = "uuid";

	private static final String FINDTYPEBYUUID = "findTypeByUuid";

	private static final String AUTHORITY = "authority";

	private static final String DOMAIN = "domain";

	private static final String KEYWORD = "keyword";

	private static final String FINDTYPEBYTUPLE = "findTypeByTuple";

	private boolean cacheFindTypeByTuple = true;

	private boolean cacheFindTypeByUuid = true;

	private boolean cacheFindTypeById = true;

	private PersistableHelper persistableHelper; // dep inj

	/**
	 * @see org.sakaiproject.api.type.TypeManager#createType(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public Type createType(String authority, String domain, String keyword, String displayName, String description)
	{
		if (log.isDebugEnabled())
		{
			log.debug("createType(String " + authority + ", String " + domain + ", String " + keyword + ", String " + displayName
					+ ", String " + description + ")");
		}
		// validation
		if (authority == null || authority.length() < 1) throw new IllegalArgumentException("authority");
		if (domain == null || domain.length() < 1) throw new IllegalArgumentException("domain");
		if (keyword == null || keyword.length() < 1) throw new IllegalArgumentException("keyword");
		if (displayName == null || displayName.length() < 1) throw new IllegalArgumentException("displayName");

		TypeImpl ti = new TypeImpl();
		persistableHelper.createPersistableFields(ti);
		ti.setUuid(IdManager.createUuid());
		ti.setAuthority(authority);
		ti.setDomain(domain);
		ti.setKeyword(keyword);
		ti.setDisplayName(displayName);
		ti.setDescription(description);
		getHibernateTemplate().save(ti);
		return ti;
	}

	public void saveType(Type type)
	{
		if (log.isDebugEnabled())
		{
			log.debug("saveType(Type " + type + ")");
		}
		if (type == null) throw new IllegalArgumentException("type");

		if (type instanceof TypeImpl)
		{ // found well known Type
			TypeImpl ti = (TypeImpl) type;
			persistableHelper.modifyPersistableFields(ti);
			getHibernateTemplate().saveOrUpdate(ti);
		}
		else
		{ // found external Type
			throw new IllegalAccessError("Alternate Type implementations not supported yet.");
		}
	}

	/**
	 * @see org.sakaiproject.service.common.type.TypeManager#getType(java.lang.String)
	 */
	public Type getType(final String uuid)
	{
		if (log.isDebugEnabled())
		{
			log.debug("getType(String " + uuid + ")");
		}
		if (uuid == null || uuid.length() < 1)
		{
			throw new IllegalArgumentException("uuid");
		}

		final HibernateCallback hcb = new HibernateCallback()
		{
			public Object doInHibernate(Session session) throws HibernateException
			{
				Query q = session.getNamedQuery(FINDTYPEBYUUID);
				q.setString(UUID, uuid);
				q.setCacheable(cacheFindTypeByUuid);
				q.setCacheRegion(Type.class.getCanonicalName());
				return q.uniqueResult();
			}
		};
		Type type = (Type) getHibernateTemplate().execute(hcb);
		return type;
	}

	/**
	 * @see org.sakaiproject.service.common.type.TypeManager#getType(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Type getType(final String authority, final String domain, final String keyword)
	{
		if (log.isDebugEnabled())
		{
			log.debug("getType(String " + authority + ", String " + domain + ", String " + keyword + ")");
		}
		// validation
		if (authority == null || authority.length() < 1) throw new IllegalArgumentException("authority");
		if (domain == null || domain.length() < 1) throw new IllegalArgumentException("domain");
		if (keyword == null || keyword.length() < 1) throw new IllegalArgumentException("keyword");

		final HibernateCallback hcb = new HibernateCallback()
		{
			public Object doInHibernate(Session session) throws HibernateException
			{
				Query q = session.getNamedQuery(FINDTYPEBYTUPLE);
				q.setString(AUTHORITY, authority);
				q.setString(DOMAIN, domain);
				q.setString(KEYWORD, keyword);
				q.setCacheable(cacheFindTypeByTuple);
				q.setCacheRegion(Type.class.getCanonicalName());
				return q.uniqueResult();
			}
		};
		Type type = (Type) getHibernateTemplate().execute(hcb);
		return type;
	}

	/**
	 * @param cacheFindTypeByTuple
	 *        The cacheFindTypeByTuple to set.
	 */
	public void setCacheFindTypeByTuple(boolean cacheFindTypeByTuple)
	{
		if (log.isInfoEnabled())
		{
			log.info("setCacheFindTypeByTuple(boolean " + cacheFindTypeByTuple + ")");
		}

		this.cacheFindTypeByTuple = cacheFindTypeByTuple;
	}

	/**
	 * @param cacheFindTypeByUuid
	 *        The cacheFindTypeByUuid to set.
	 */
	public void setCacheFindTypeByUuid(boolean cacheFindTypeByUuid)
	{
		if (log.isInfoEnabled())
		{
			log.info("setCacheFindTypeByUuid(boolean " + cacheFindTypeByUuid + ")");
		}

		this.cacheFindTypeByUuid = cacheFindTypeByUuid;
	}

	/**
	 * @param cacheFindTypeById
	 *        The cacheFindTypeById to set.
	 */
	public void setCacheFindTypeById(boolean cacheFindTypeById)
	{
		if (log.isInfoEnabled())
		{
			log.info("setCacheFindTypeById(boolean " + cacheFindTypeById + ")");
		}

		this.cacheFindTypeById = cacheFindTypeById;
	}

	public void deleteType(Type type)
	{
		if (log.isDebugEnabled())
		{
			log.debug("deleteType(Type " + type + ")");
		}

		throw new UnsupportedOperationException("Types should never be deleted!");
	}

	/**
	 * @param persistableHelper
	 *        The persistableHelper to set.
	 */
	public void setPersistableHelper(PersistableHelper persistableHelper)
	{
		this.persistableHelper = persistableHelper;
	}

}
