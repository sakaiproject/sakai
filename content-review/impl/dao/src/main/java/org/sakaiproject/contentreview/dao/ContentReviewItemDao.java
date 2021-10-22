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

import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class ContentReviewItemDao extends HibernateCommonDao<ContentReviewItem> {
	
	@SuppressWarnings("unchecked")
	public List<ContentReviewItem> findByProviderAnyMatching(Integer providerId, String contentId, String userId, String siteId, String taskId,
			String externalId, Long status, Integer errorCode) {

		Criteria c = sessionFactory.getCurrentSession()
				.createCriteria(ContentReviewItem.class)
				.add(Restrictions.eq("providerId", providerId));

		if (contentId != null) c.add(Restrictions.eq("contentId", contentId));
		if (userId != null) c.add(Restrictions.eq("userId", userId));
		if (siteId != null) c.add(Restrictions.eq("siteId", siteId));
		if (taskId != null) c.add(Restrictions.eq("taskId", taskId));
		if (externalId != null) c.add(Restrictions.eq("externalId", externalId));
		if (status != null) c.add(Restrictions.eq("status", status));
		if (errorCode != null) c.add(Restrictions.eq("errorCode", errorCode));

		return c.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ContentReviewItem> findByProviderGroupedBySiteAndTask(Integer providerId) {

		Criteria c = sessionFactory.getCurrentSession()
				.createCriteria(ContentReviewItem.class)
				.add(Restrictions.eq("providerId", providerId))
				.setProjection( Projections.projectionList()
						.add(Projections.groupProperty("siteId"))
						.add(Projections.groupProperty("taskId")));

		return c.list();
	}

	@SuppressWarnings("unchecked")
	public List<String> findByProviderGroupedBySite(Integer providerId) {

		Criteria c = sessionFactory.getCurrentSession()
				.createCriteria(ContentReviewItem.class)
				.add(Restrictions.eq("providerId", providerId))
				.setProjection( Projections.projectionList()
							.add(Projections.groupProperty("siteId"))
							.add(Projections.max("id").as("maxId"))
						)
				.addOrder(Order.desc("maxId"))
				.setMaxResults(999);

		List<Object[]> listOfObjects = c.list();
		return listOfObjects.stream().map(o -> o[0]).map(Objects::toString).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public List<ContentReviewItem> findByProviderAwaitingReports(Integer providerId) {

		Criteria c = sessionFactory.getCurrentSession()
				.createCriteria(ContentReviewItem.class)
				.add(Restrictions.eq("providerId", providerId))
				.add(Restrictions.in("status", new Long[]{ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE, ContentReviewConstants.CONTENT_REVIEW_REPORT_ERROR_RETRY_CODE}));
		
		return c.list();
	}
	
	public Optional<ContentReviewItem> findByProviderAndContentId(Integer providerId, String contentId) {

		Criteria c = sessionFactory.getCurrentSession()
				.createCriteria(ContentReviewItem.class)
				.add(Restrictions.eq("providerId", providerId))
				.add(Restrictions.eq("contentId", contentId));
		
		return Optional.ofNullable((ContentReviewItem) c.uniqueResult());
	}
	
	public Optional<ContentReviewItem> findByProviderAndExternalId(Integer providerId, String externalId) {

		Criteria c = sessionFactory.getCurrentSession()
				.createCriteria(ContentReviewItem.class)
				.add(Restrictions.eq("providerId", providerId))
				.add(Restrictions.eq("externalId", externalId));
		
		return Optional.ofNullable((ContentReviewItem) c.uniqueResult());
	}

	public Optional<ContentReviewItem> findByProviderSingleItemToSubmit(Integer providerId) {

		Calendar calendar = Calendar.getInstance();
		
		Criteria c = sessionFactory.getCurrentSession()
				.createCriteria(ContentReviewItem.class)
				.add(Restrictions.eq("providerId", providerId))
				.add(Restrictions.in("status", new Long[]{ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE}))
				.add(Restrictions.lt("nextRetryTime", calendar.getTime()))
				.setMaxResults(1);
		
		return Optional.ofNullable((ContentReviewItem) c.uniqueResult());
	}

	public Optional<ContentReviewItem> findByContentId(String contentId) {
		Criteria c = sessionFactory.getCurrentSession()
				.createCriteria(ContentReviewItem.class)
				.add(Restrictions.eq("contentId", contentId))
				.addOrder(Order.desc("dateQueued"))
				.setMaxResults(1);

		return Optional.ofNullable((ContentReviewItem) c.uniqueResult());
	}

	public boolean itemsExistForSiteAndTaskId(String siteId, String taskId)
	{
		Criteria c = sessionFactory.getCurrentSession()
				.createCriteria(ContentReviewItem.class)
				.add(Restrictions.eq("taskId", taskId))
				.add(Restrictions.eq("siteId", siteId))
				.setMaxResults(1);

		List result = c.list();
		return result != null && !result.isEmpty();
	}
}
