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
package org.sakaiproject.contentreview.service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.dao.ContentReviewConstants;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.dao.ContentReviewItemDao;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;

@Slf4j
public class ContentReviewQueueServiceImpl implements ContentReviewQueueService {

	@Setter
	private ContentReviewItemDao itemDao;
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.contentreview.common.service.ContentReviewCommonService#queueContent(java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.util.List, int)
	 */
	@Override
	@Transactional
	public void queueContent(Integer providerId, String userId, String siteId, String taskId, List<ContentResource> content)
			throws QueueException {
		
		Objects.requireNonNull(providerId, "providerId cannot be null");
		Objects.requireNonNull(userId, "userId cannot be null");
		Objects.requireNonNull(siteId, "siteId cannot be null");
		Objects.requireNonNull(taskId, "taskId cannot be null");
		Objects.requireNonNull(content, "content cannot be null");
				
		for (ContentResource resource : content) {
			String contentId = resource.getId();
			
			/*
			 * first check that this content has not been submitted before this may
			 * not be the best way to do this - perhaps use contentId as the primary
			 * key for now id is the primary key and so the database won't complain
			 * if we put in repeats necessitating the check
			 */
			
			Optional<ContentReviewItem> existingItem = itemDao.findByProviderAndContentId(providerId, contentId);
			
			if (existingItem.isPresent()) {
				throw new QueueException("Content " + contentId + " is already queued");
			}
			
			ContentReviewItem item = new ContentReviewItem(contentId, userId, siteId, taskId, new Date(), ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE, providerId);
			
			log.debug("Adding content: " + contentId + " from site " + siteId + " and user: " + userId + " for task: " + taskId + " to submission queue");
			
			itemDao.create(item);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.contentreview.common.service.ContentReviewCommonService#getReviewScore(java.lang.Integer, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional(readOnly=true)
	@Deprecated
	public int getReviewScore(Integer providerId, String contentId) throws QueueException, ReportException, Exception {
		Optional<ContentReviewItem> item = getQueuedItem(providerId, contentId);
		return item.isPresent()  ? item.get().getReviewScore() : null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.contentreview.common.service.ContentReviewCommonService#getReviewStatus(java.lang.Integer, java.lang.String)
	 */
	@Override
	@Transactional(readOnly=true)
	@Deprecated
	public Long getReviewStatus(Integer providerId, String contentId) throws QueueException {
		Optional<ContentReviewItem> item = getQueuedItem(providerId, contentId);
		return item.isPresent()  ? item.get().getStatus() : null;
	}
		
	/* (non-Javadoc)
	 * @see org.sakaiproject.contentreview.common.service.ContentReviewCommonService#getDateQueued(java.lang.Integer, java.lang.String)
	 */
	@Override
	@Transactional(readOnly=true)
	@Deprecated
	public Date getDateQueued(Integer providerId, String contentId) throws QueueException {
		Optional<ContentReviewItem> item = getQueuedItem(providerId, contentId);
		return item.isPresent()  ? item.get().getDateQueued() : null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.contentreview.common.service.ContentReviewCommonService#getDateSubmitted(java.lang.Integer, java.lang.String)
	 */
	@Override
	@Transactional(readOnly=true)
	@Deprecated
	public Date getDateSubmitted(Integer providerId, String contentId) throws QueueException, SubmissionException {
		Optional<ContentReviewItem> item = getQueuedItem(providerId, contentId);
		return item.isPresent()  ? item.get().getDateSubmitted() : null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.contentreview.common.service.ContentReviewCommonService#getAllContentReviewItems(java.lang.Integer, java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional(readOnly=true)
	public List<ContentReviewItem> getContentReviewItems(Integer providerId, String siteId, String taskId) {
		Objects.requireNonNull(providerId, "providerId cannot be null");

		return itemDao.findByProviderAnyMatching(providerId, null, null, siteId, taskId, null, null, null);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.contentreview.common.service.ContentReviewCommonService#getAllContentReviewItems(java.lang.Integer, java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional(readOnly=true)
	public List<ContentReviewItem> getAllContentReviewItemsGroupedBySiteAndTask(Integer providerId) {
		Objects.requireNonNull(providerId, "providerId cannot be null");

		log.debug("Returning list of items grouped by site and task");

		return itemDao.findByProviderGroupedBySiteAndTask(providerId);
	}

	
	/* (non-Javadoc)
	 * @see org.sakaiproject.contentreview.common.service.ContentReviewCommonService#resetUserDetailsLockedItems(java.lang.Integer, java.lang.String)
	 */
	@Override
	@Transactional
	public void resetUserDetailsLockedItems(Integer providerId, String userId) {
		Objects.requireNonNull(providerId, "providerId cannot be null");
		Objects.requireNonNull(userId, "userId cannot be null");

		List<ContentReviewItem> lockedItems = itemDao.findByProviderAnyMatching(providerId, null, userId, null, null, null, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_USER_DETAILS_CODE, null);
		for (ContentReviewItem item : lockedItems) {
			item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
			itemDao.save(item);
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.contentreview.common.service.ContentReviewCommonService#removeFromQueue(java.lang.Integer, java.lang.String)
	 */
	@Override
	@Transactional
	public void removeFromQueue(Integer providerId, String contentId) {
		Objects.requireNonNull(providerId, "providerId cannot be null");
		Objects.requireNonNull(contentId, "contentId cannot be null");

		Optional<ContentReviewItem> item = itemDao.findByProviderAndContentId(providerId, contentId);
		if (item.isPresent()) {
			itemDao.delete(item.get());
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.contentreview.service.ContentReviewQueueService#getQueuedItem(java.lang.Integer, java.lang.String)
	 */
	@Override
	@Transactional(readOnly=true)
	public Optional<ContentReviewItem> getQueuedItem(Integer providerId, String contentId) {
		Objects.requireNonNull(providerId, "providerId cannot be null");
		Objects.requireNonNull(contentId, "contentId cannot be null");
		
		return itemDao.findByProviderAndContentId(providerId, contentId);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.contentreview.service.ContentReviewQueueService#getQueuedItem(java.lang.Integer, java.lang.String)
	 */
	@Override
	@Transactional(readOnly=true)
	public Optional<ContentReviewItem> getQueuedItemByExternalId(Integer providerId, String externalId) {
		Objects.requireNonNull(providerId, "providerId cannot be null");
		Objects.requireNonNull(externalId, "externalId cannot be null");
		
		return itemDao.findByProviderAndExternalId(providerId, externalId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.contentreview.service.ContentReviewQueueService#getQueuedNotSubmittedItems(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly=true)
	public List<ContentReviewItem> getQueuedNotSubmittedItems(Integer providerId) {
		return itemDao.findByProviderAnyMatching(providerId, null, null, null, null, null, ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE, null);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.contentreview.service.ContentReviewQueueService#getNextItemInQueueToSubmit(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly=true)
	public Optional<ContentReviewItem> getNextItemInQueueToSubmit(Integer providerId) {
		Objects.requireNonNull(providerId, "providerId cannot be null");
		return itemDao.findByProviderSingleItemToSubmit(providerId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.contentreview.service.ContentReviewQueueService#getAwaitingReports(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly=true)
	public List<ContentReviewItem> getAwaitingReports(Integer providerId) {
		Objects.requireNonNull(providerId, "providerId cannot be null");
		return itemDao.findByProviderAwaitingReports(providerId);
	}
	
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.contentreview.service.ContentReviewQueueService#update(org.sakaiproject.contentreview.dao.ContentReviewItem)
	 */
	@Override
	@Transactional
	public void update(ContentReviewItem item) {
		Objects.requireNonNull(item, "item cannot be null");
		Objects.requireNonNull(item.getProviderId(), "providerId cannot be null");
		
		itemDao.save(item);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.contentreview.service.ContentReviewQueueService#delete(org.sakaiproject.contentreview.dao.ContentReviewItem)
	 */
	@Override
	@Transactional
	public void delete(ContentReviewItem item) {
		Objects.requireNonNull(item, "item cannot be null");
		Objects.requireNonNull(item.getId(), "Id cannot be null");
		Objects.requireNonNull(item.getProviderId(), "providerId cannot be null");
		
		itemDao.delete(item);
	}	
}
