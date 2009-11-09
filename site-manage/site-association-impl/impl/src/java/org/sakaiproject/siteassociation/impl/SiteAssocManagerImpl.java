/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007 The Sakai Foundation.
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

package org.sakaiproject.siteassociation.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.siteassociation.impl.AssociationImpl;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.siteassociation.api.SiteAssocManager;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class SiteAssocManagerImpl extends HibernateDaoSupport implements SiteAssocManager {

	private SiteService siteService;
	private static final String NULL_ARG = "Null Argument", TO_CONTEXT = "toContext", FROM_CONTEXT = "fromContext";
	
	public void addAssociation(String fromContext, String toContext) {
		if ((fromContext == null) || (toContext == null)) {
			throw new IllegalArgumentException(NULL_ARG);
		}

		AssociationImpl association = new AssociationImpl(fromContext,
				toContext);
		if (getHibernateTemplate().get(AssociationImpl.class, association) == null) {
			getHibernateTemplate().save(
					new AssociationImpl(fromContext, toContext));
		}		
	}

	public List<String> getAssociatedTo(final String context) {
		if (context == null) {
			throw new IllegalArgumentException(NULL_ARG);
		}

		List<AssociationImpl> associations = (List) getHibernateTemplate()
				.execute(new HibernateCallback() {
					public Object doInHibernate(Session session) {
						return session.createCriteria(AssociationImpl.class)
								.add(Restrictions.eq(TO_CONTEXT, context))
								.list();
					}
				});

		List<String> fromContexts = new ArrayList<String>();
		for (AssociationImpl ass : associations) {
			fromContexts.add(ass.getFromContext());
		}
		return fromContexts;
	}
	
	public List<String> getAssociatedFrom(final String context) {
		if (context == null) {
			throw new IllegalArgumentException(NULL_ARG);
		}

		List<AssociationImpl> associations = (List) getHibernateTemplate()
				.execute(new HibernateCallback() {
					public Object doInHibernate(Session session) {
						return session.createCriteria(AssociationImpl.class)
								.add(Restrictions.eq(FROM_CONTEXT, context))
								.list();
					}
				});

		List<String> toContexts = new ArrayList<String>();
		for (AssociationImpl ass : associations) {
			toContexts.add(ass.getToContext());
		}
		return toContexts;
	}

	public Site getSite(String context) {
		try {
			return siteService.getSite(context);
		} catch (IdUnusedException e) {
		}
		return null;
	}

	public void removeAssociation(String fromContext, String toContext) {
		if ((fromContext == null) || (toContext == null)) {
			throw new IllegalArgumentException(NULL_ARG);
		}

		AssociationImpl association = (AssociationImpl) getHibernateTemplate()
				.get(AssociationImpl.class,
						new AssociationImpl(fromContext, toContext));
		if (association != null) {
			getHibernateTemplate().delete(association);
		}		
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}


	
}
