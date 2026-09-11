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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import lombok.Setter;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.sakaiproject.scorm.dao.api.ContentPackageDao;
import org.sakaiproject.scorm.model.api.ContentPackage;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ContentPackageDaoImpl implements ContentPackageDao
{
	@Setter private SessionFactory sessionFactory;

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
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<ContentPackage> query = cb.createQuery(ContentPackage.class);
		Root<ContentPackage> root = query.from(ContentPackage.class);
		query.select(root).where(cb.equal(root.get("context"), context), cb.equal(root.get("deleted"), false));
		return session.createQuery(query).getResultList();
	}

	@Override
	public ContentPackage load(long id)
	{
		return sessionFactory.getCurrentSession().getReference(ContentPackage.class, id);
	}

	/**
	 * @param resourceId
	 * @return 
	 * @see org.sakaiproject.scorm.dao.api.ContentPackageDao#loadByResourceId(java.lang.String)
	 */
	@Override
	public ContentPackage loadByResourceId(String resourceId)
	{
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<ContentPackage> query = cb.createQuery(ContentPackage.class);
		Root<ContentPackage> root = query.from(ContentPackage.class);
		query.select(root).where(cb.equal(root.get("resourceId"), resourceId), cb.equal(root.get("deleted"), false));
		List<ContentPackage> result = session.createQuery(query).getResultList();
		return result.isEmpty() ? null : result.get(0);
	}

	@Override
	public void remove(ContentPackage contentPackage)
	{
		contentPackage.setDeleted(true);
		sessionFactory.getCurrentSession().merge(contentPackage);
	}

	@Override
	public void save(ContentPackage contentPackage)
	{
		sessionFactory.getCurrentSession().merge(contentPackage);
	}
}
