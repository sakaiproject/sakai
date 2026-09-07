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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import uk.ac.cam.caret.sakai.rwiki.model.RWikiPropertyImpl;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiPropertyDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiProperty;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

// FIXME: Component
@Slf4j
@Transactional(readOnly = true)
public class RWikiPropertyDaoImpl implements RWikiPropertyDao {
	@Setter private SessionFactory sessionFactory;

	private String schemaVersion;

	public RWikiProperty getProperty(final String name)
	{
		long start = System.currentTimeMillis();
		try
		{
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<RWikiPropertyImpl> cq = cb.createQuery(RWikiPropertyImpl.class);
			Root<RWikiPropertyImpl> root = cq.from(RWikiPropertyImpl.class);

			cq.select(root).where(cb.equal(root.get("name"), name));

			List found = session.createQuery(cq).getResultList();
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
			return (RWikiProperty) found.get(0);
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("RWikiPropertyDaoImpl.getContentObject: "
					+ name, start, finish);
		}
	}

	public RWikiProperty createProperty()
	{
		return new RWikiPropertyImpl();
	}

	@Transactional
	public void update(RWikiProperty property)
	{
		sessionFactory.getCurrentSession().saveOrUpdate(property);
	}

	/**
	 * @return Returns the schemaVersion.
	 */
	public String getSchemaVersion()
	{
		return schemaVersion;
	}

	/**
	 * @param schemaVersion
	 *        The schemaVersion to set.
	 */
	public void setSchemaVersion(String schemaVersion)
	{
		this.schemaVersion = schemaVersion;
	}

}
