/**
 * Copyright (c) 2003 The Apereo Foundation
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
package org.sakaiproject.contentreview.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class ContentReviewItemDao extends HibernateCommonDao<ContentReviewItem> {
	
	@SuppressWarnings("unchecked")
	public List<ContentReviewItem> findByProviderAnyMatching(Integer providerId, String contentId, String userId, String siteId, String taskId,
			String externalId, Long status, Integer errorCode) {

		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<ContentReviewItem> cq = cb.createQuery(ContentReviewItem.class);
		Root<ContentReviewItem> root = cq.from(ContentReviewItem.class);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("providerId"), providerId));
		if (contentId != null) predicates.add(cb.equal(root.get("contentId"), contentId));
		if (userId != null) predicates.add(cb.equal(root.get("userId"), userId));
		if (siteId != null) predicates.add(cb.equal(root.get("siteId"), siteId));
		if (taskId != null) predicates.add(cb.equal(root.get("taskId"), taskId));
		if (externalId != null) predicates.add(cb.equal(root.get("externalId"), externalId));
		if (status != null) predicates.add(cb.equal(root.get("status"), status));
		if (errorCode != null) predicates.add(cb.equal(root.get("errorCode"), errorCode));

		cq.where(predicates.toArray(new Predicate[0]));

		return sessionFactory.getCurrentSession()
				.createQuery(cq)
				.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<ContentReviewItem> findByProviderGroupedBySiteAndTask(Integer providerId) {

		String hql = "SELECT DISTINCT i.siteId, i.taskId FROM ContentReviewItem i WHERE i.providerId = :providerId";
		List<Object[]> results = sessionFactory.getCurrentSession()
				.createQuery(hql, Object[].class)
				.setParameter("providerId", providerId)
				.list();

		return results.stream().map(row -> {
			ContentReviewItem item = new ContentReviewItem();
			item.setSiteId((String) row[0]);
			item.setTaskId((String) row[1]);
			return item;
		}).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public List<String> findByProviderGroupedBySite(Integer providerId) {

		String hql = "SELECT i.siteId FROM ContentReviewItem i WHERE i.providerId = :providerId " +
				 "GROUP BY i.siteId ORDER BY MAX(i.id) DESC";

		return sessionFactory.getCurrentSession()
				.createQuery(hql, String.class)
				.setParameter("providerId", providerId)
				.setMaxResults(999)
				.list();
	}

	@SuppressWarnings("unchecked")
	public List<ContentReviewItem> findByProviderAwaitingReports(Integer providerId) {

		String hql = "FROM ContentReviewItem i WHERE i.providerId = :providerId " +
				"AND i.status IN (:status1, :status2)";

		return sessionFactory.getCurrentSession()
				.createQuery(hql, ContentReviewItem.class)
				.setParameter("providerId", providerId)
				.setParameter("status1", ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE)
				.setParameter("status2", ContentReviewConstants.CONTENT_REVIEW_REPORT_ERROR_RETRY_CODE)
				.list();
	}
	
	public Optional<ContentReviewItem> findByProviderAndContentId(Integer providerId, String contentId) {

		String hql = "FROM ContentReviewItem i WHERE i.providerId = :providerId AND i.contentId = :contentId";

		return sessionFactory.getCurrentSession()
				.createQuery(hql, ContentReviewItem.class)
				.setParameter("providerId", providerId)
				.setParameter("contentId", contentId)
				.uniqueResultOptional();
	}
	
	public Optional<ContentReviewItem> findByProviderAndExternalId(Integer providerId, String externalId) {

		String hql = "FROM ContentReviewItem i WHERE i.providerId = :providerId AND i.externalId = :externalId";

		return sessionFactory.getCurrentSession()
				.createQuery(hql, ContentReviewItem.class)
				.setParameter("providerId", providerId)
				.setParameter("externalId", externalId)
				.uniqueResultOptional();
	}

	public Optional<ContentReviewItem> findByProviderSingleItemToSubmit(Integer providerId) {

		String hql = "FROM ContentReviewItem i WHERE i.providerId = :providerId " +
				"AND i.status IN (:status1, :status2) AND i.nextRetryTime < :now";

		return sessionFactory.getCurrentSession()
				.createQuery(hql, ContentReviewItem.class)
				.setParameter("providerId", providerId)
				.setParameter("status1", ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE)
				.setParameter("status2", ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE)
				.setParameter("now", Calendar.getInstance().getTime())
				.setMaxResults(1)
				.uniqueResultOptional();
	}
}
