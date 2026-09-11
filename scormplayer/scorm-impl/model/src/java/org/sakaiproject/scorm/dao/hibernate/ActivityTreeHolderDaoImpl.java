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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import lombok.Setter;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.sakaiproject.scorm.dao.api.ActivityTreeHolderDao;
import org.sakaiproject.scorm.model.api.ActivityTreeHolder;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ActivityTreeHolderDaoImpl implements ActivityTreeHolderDao
{
	@Setter private SessionFactory sessionFactory;

	@Override
	public ActivityTreeHolder find(long contentPackageId, String learnerId)
	{
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<ActivityTreeHolder> query = cb.createQuery(ActivityTreeHolder.class);
		Root<ActivityTreeHolder> root = query.from(ActivityTreeHolder.class);
		query.select(root).where(cb.equal(root.get("contentPackageId"), contentPackageId), cb.equal(root.get("learnerId"), learnerId));
		List<ActivityTreeHolder> result = session.createQuery(query).getResultList();
		return result.isEmpty() ? null : result.get(0);
	}

	@Override
	public void save(ActivityTreeHolder holder)
	{
		sessionFactory.getCurrentSession().merge(holder);
	}
}
