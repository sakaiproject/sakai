/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.dao.hibernate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sakaiproject.scorm.dao.api.ContentPackageDao;
import org.sakaiproject.scorm.model.api.ContentPackage;

import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

public class ContentPackageDaoImpl extends HibernateDaoSupport implements ContentPackageDao
{
	@Override
	public int countContentPackages(String context, String name)
	{
		int count = 1;
		List<ContentPackage> contentPackages = find(context);
		Pattern p = Pattern.compile(name + "\\s*\\(?\\d*\\)?");

		for (ContentPackage cp : contentPackages)
		{
			Matcher m = p.matcher(cp.getTitle());
			if (m.matches())
			{
				count++;
			}
		}

		return count;
	}

	@Override
	public List<ContentPackage> find(String context)
	{
		String statement = new StringBuilder("from ").append(ContentPackage.class.getName()).append(" where context = :context and deleted = :deleted ").toString();
		return getHibernateTemplate().getSessionFactory().getCurrentSession().createQuery(statement)
				.setParameter("context", context)
				.setParameter("deleted", false)
				.getResultList();
	}

	@Override
	public ContentPackage load(long id)
	{
		return (ContentPackage) getHibernateTemplate().load(ContentPackage.class, id);
	}

	/**
	 * @param resourceId
	 * @return 
	 * @see org.sakaiproject.scorm.dao.api.ContentPackageDao#loadByResourceId(java.lang.String)
	 */
	@Override
	public ContentPackage loadByResourceId(String resourceId)
	{
		String statement = new StringBuilder("from ").append(ContentPackage.class.getName()).append(" where resourceId = :id and deleted = :deleted ").toString();
		List<ContentPackage> result = getHibernateTemplate().getSessionFactory().getCurrentSession().createQuery(statement)
			.setParameter("id", resourceId)
			.setParameter("deleted", false)
			.getResultList();

		if (result.isEmpty())
		{
			return null;
		}
		else
		{
			return result.get(0);
		}
	}

	@Override
	public void remove(ContentPackage contentPackage)
	{
		contentPackage.setDeleted(true);
		getHibernateTemplate().saveOrUpdate(contentPackage);
	}

	@Override
	public void save(ContentPackage contentPackage)
	{
		getHibernateTemplate().saveOrUpdate(contentPackage);
	}
}
