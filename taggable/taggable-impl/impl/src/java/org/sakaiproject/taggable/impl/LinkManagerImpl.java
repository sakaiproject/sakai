/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/syracuse/taggable/branches/oncourse_osp_enhancements/taggable-impl/impl/src/java/org/sakaiproject/taggable/impl/LinkManagerImpl.java $
 * $Id: LinkManagerImpl.java 46822 2008-03-17 16:19:47Z chmaurer@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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
package org.sakaiproject.taggable.impl;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.taggable.api.Link;
import org.sakaiproject.taggable.api.LinkManager;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

public class LinkManagerImpl extends HibernateDaoSupport implements LinkManager
{
	
	private static final String NULL_ARG = "Null Argument";
	protected static final String CONTEXT = "context",
			ACTIVITY_REF = "activityRef",
			TAG_CRITERIA_REF = "tagCriteriaRef",
			VISIBLE = "visible",
			QUERY_LINKS_BY_ACTIVITY_CONTEXT = "findLinksByActivityRefContext",
			QUERY_LINKS_BY_ACTIVITY_CONTEXT_VISIBLE = "findLinksByActivityRefContextVisible",
			QUERY_LINKS_BY_CRITERIA = "findLinksByCriteriaRef",
			QUERY_LINKS_BY_CRITERIA_VISIBLE = "findLinksByCriteriaRefVisible", 
			QUERY_DELETE_LINKS_BY_ACTIVITY_REF = "deleteLinksByActivityRef";

	public Link persistLink(String activityRef, String tagCriteriaRef, String rationale,
			String rubric, boolean visible, boolean locked) {
		if ((activityRef == null) || (tagCriteriaRef == null)) {
			throw new IllegalArgumentException(NULL_ARG);
		}

		LinkImpl link = new LinkImpl(activityRef, tagCriteriaRef, rationale, rubric,
				visible, locked);
		getHibernateTemplate().save(link);
		return link;
	}
	
	public Link lookupLink(List<Link> links, String tagCriteriaRef) {
		for (Link link : links) {
			if (link.getTagCriteriaRef().equalsIgnoreCase(tagCriteriaRef))
				return link;
		}
		return null;
	}

	public Link getLink(String ref) throws IdUnusedException, PermissionException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Link getLink(final String activityRef, final String tagCriteriaRef)
			throws PermissionException
	{
		if ((activityRef == null) || (tagCriteriaRef == null)) {
			throw new IllegalArgumentException(NULL_ARG);
		}

		return (Link) getHibernateTemplate().execute(session -> session.createCriteria(LinkImpl.class).add(
                Restrictions.eq(ACTIVITY_REF, activityRef)).add(
                Restrictions.eq(TAG_CRITERIA_REF, tagCriteriaRef)).uniqueResult());
	}

	public List<Link> getLinks(final String activityRef, final boolean any,
			final String context) {
		if ((activityRef == null) || (context == null)) {
			throw new IllegalArgumentException(NULL_ARG);
		}

		return getHibernateTemplate().execute((HibernateCallback<List<Link>>) session -> {
            String likeContext = "%/"+context+"/%";

            Query q = session.getNamedQuery(QUERY_LINKS_BY_ACTIVITY_CONTEXT);
            if (!any) {
                q = session.getNamedQuery(QUERY_LINKS_BY_ACTIVITY_CONTEXT_VISIBLE);
                q.setParameter(VISIBLE, true);
            }

            q.setParameter(ACTIVITY_REF, activityRef, StringType.INSTANCE);
            q.setParameter(CONTEXT, likeContext, StringType.INSTANCE);

            return q.list();
        });
	}
	
	public List<Link> getLinks(final String criteriaRef, final boolean any) {
		if (criteriaRef == null) {
			throw new IllegalArgumentException(NULL_ARG);
		}

		return getHibernateTemplate().execute((HibernateCallback<List<Link>>) session -> {

            Query q = session.getNamedQuery(QUERY_LINKS_BY_CRITERIA);
            if (!any) {
                q = session.getNamedQuery(QUERY_LINKS_BY_CRITERIA_VISIBLE);
                q.setParameter(VISIBLE, true);
            }

            q.setParameter(TAG_CRITERIA_REF, criteriaRef, StringType.INSTANCE);

            return q.list();
        });
	}
	
	public void removeLink(Link link) {
		if (link == null) {
			throw new IllegalArgumentException(NULL_ARG);
		}

		getHibernateTemplate().delete(link);
	}
	
	public void removeLinks(final String activityRef) {
		if (activityRef == null) {
			throw new IllegalArgumentException(NULL_ARG);
		}

		getHibernateTemplate().execute((HibernateCallback) session -> {
            Query q = session.getNamedQuery(QUERY_DELETE_LINKS_BY_ACTIVITY_REF);
            q.setParameter(ACTIVITY_REF, activityRef);
            q.executeUpdate();
            return null;
        });
	}
	
}
