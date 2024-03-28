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

import org.sakaiproject.scorm.dao.api.ActivityTreeHolderDao;
import org.sakaiproject.scorm.model.api.ActivityTreeHolder;

import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

public class ActivityTreeHolderDaoImpl extends HibernateDaoSupport implements ActivityTreeHolderDao
{
	@Override
	public ActivityTreeHolder find(long contentPackageId, String learnerId)
	{

		List r = getHibernateTemplate().getSessionFactory().getCurrentSession()
				.createQuery("from " + ActivityTreeHolder.class.getName() + " where contentPackageId=:cpid and learnerId=:lid")
				.setParameter("cpid", contentPackageId)
				.setParameter("lid", learnerId)
				.getResultList();
		if (r.isEmpty())
		{
			return null;
		}

		ActivityTreeHolder holder = (ActivityTreeHolder) r.get(0);
		return holder;
	}

	@Override
	public void save(ActivityTreeHolder holder)
	{
		getHibernateTemplate().saveOrUpdate(holder);
	}
}
