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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.taggable.api.Link;
import org.sakaiproject.taggable.api.LinkManager;
import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Transactional
public class LinkManagerImpl implements LinkManager
{
	
	private static final String NULL_ARG = "Null Argument";
	protected static final String ACTIVITY_REF = "activityRef",
			TAG_CRITERIA_REF = "tagCriteriaRef",
			VISIBLE = "visible";

	@Setter private SessionFactory sessionFactory;

	public Link persistLink(String activityRef, String tagCriteriaRef, String rationale,
			String rubric, boolean visible, boolean locked) {
		if ((activityRef == null) || (tagCriteriaRef == null)) {
			throw new IllegalArgumentException(NULL_ARG);
		}

		LinkImpl link = new LinkImpl(activityRef, tagCriteriaRef, rationale, rubric,
				visible, locked);
		sessionFactory.getCurrentSession().persist(link);
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

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<LinkImpl> cq = cb.createQuery(LinkImpl.class);
		Root<LinkImpl> root = cq.from(LinkImpl.class);
		cq.where(
			cb.equal(root.get(ACTIVITY_REF), activityRef),
			cb.equal(root.get(TAG_CRITERIA_REF), tagCriteriaRef)
		);
		return session.createQuery(cq).uniqueResult();
	}

	public List<Link> getLinks(final String activityRef, final boolean any,
			final String context) {
		if ((activityRef == null) || (context == null)) {
			throw new IllegalArgumentException(NULL_ARG);
		}

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<LinkImpl> cq = cb.createQuery(LinkImpl.class);
		Root<LinkImpl> root = cq.from(LinkImpl.class);
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get(ACTIVITY_REF), activityRef));
		predicates.add(cb.like(root.get(TAG_CRITERIA_REF), "%/" + context + "/%"));
		if (!any) {
			predicates.add(cb.equal(root.get(VISIBLE), true));
		}
		cq.select(root).where(predicates.toArray(new Predicate[0]));
		return new ArrayList<>(session.createQuery(cq).getResultList());
	}
	
	public List<Link> getLinks(final String criteriaRef, final boolean any) {
		if (criteriaRef == null) {
			throw new IllegalArgumentException(NULL_ARG);
		}

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<LinkImpl> cq = cb.createQuery(LinkImpl.class);
		Root<LinkImpl> root = cq.from(LinkImpl.class);
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get(TAG_CRITERIA_REF), criteriaRef));
		if (!any) {
			predicates.add(cb.equal(root.get(VISIBLE), true));
		}
		cq.select(root).where(predicates.toArray(new Predicate[0]));
		return new ArrayList<>(session.createQuery(cq).getResultList());
	}
	
	public void removeLink(Link link) {
		if (link == null) {
			throw new IllegalArgumentException(NULL_ARG);
		}

		sessionFactory.getCurrentSession().delete(link);
	}
	
	public void removeLinks(final String activityRef) {
		if (activityRef == null) {
			throw new IllegalArgumentException(NULL_ARG);
		}

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaDelete<LinkImpl> cd = cb.createCriteriaDelete(LinkImpl.class);
		Root<LinkImpl> root = cd.from(LinkImpl.class);
		cd.where(cb.equal(root.get(ACTIVITY_REF), activityRef));
		session.createMutationQuery(cd).executeUpdate();
	}
	
}
