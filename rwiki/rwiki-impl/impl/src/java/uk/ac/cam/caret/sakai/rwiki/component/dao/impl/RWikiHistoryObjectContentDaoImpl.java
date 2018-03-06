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
package uk.ac.cam.caret.sakai.rwiki.component.dao.impl;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import uk.ac.cam.caret.sakai.rwiki.model.RWikiHistoryObjectContentImpl;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiObjectContentDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiHistoryObjectContent;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObjectContent;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

// FIXME: Component
@Slf4j
public class RWikiHistoryObjectContentDaoImpl extends HibernateDaoSupport
		implements RWikiObjectContentDao
{
	public RWikiObjectContent getContentObject(final RWikiObject parent)
	{
		long start = System.currentTimeMillis();
		try
		{
			HibernateCallback callback = new HibernateCallback()
			{
				public Object doInHibernate(Session session)
						throws HibernateException
				{
					return session.createCriteria(
							RWikiHistoryObjectContent.class).add(
							Expression.eq("rwikiid", parent.getId())).list();
				}

			};
			List found = (List) getHibernateTemplate().execute(callback);
			if (found.size() == 0)
			{
				log.debug("Found {} objects with id {}", found.size(), parent.getId());
				return null;
			}
			log.debug("Found {} objects with name {} returning most recent one.", found.size(), parent.getId());
			return (RWikiObjectContent) found.get(0);
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer(
					"RWikiHistroyObjectContentDaoImpl.getContentObject: "
							+ parent.getId(), start, finish);
		}
	}

	public RWikiObjectContent createContentObject(RWikiObject parent)
	{
		RWikiHistoryObjectContent rwco = new RWikiHistoryObjectContentImpl();
		rwco.setRwikiid(parent.getId());
		return rwco;
	}

	public void update(RWikiObjectContent content)
	{
		RWikiHistoryObjectContentImpl impl = (RWikiHistoryObjectContentImpl) content;
		getHibernateTemplate().saveOrUpdate(impl);
	}

}
