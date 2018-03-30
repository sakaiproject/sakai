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
import java.util.Optional;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;

public interface ContentReviewQueueService {

	/**
	 * Queue a content item that will later be sent to the content review service
	 * when the Content Review Process Job is run.
	 * @param providerId the id of content review implementation
	 * @param userId the user id associated with the item to be reviewed
	 * @param siteId the site id associated with the item to be reviewed
	 * @param taskId the task id associated with the item to reviewed
	 * @param content a {@code java.util.List} of the items to be reviewed
	 * @throws QueueException
	 */
	void queueContent(Integer providerId, String userId, String siteId, String taskId, List<ContentResource> content)
			throws QueueException;

	/**
	 * Get the score that has been stored by the content review service. 
	 * @param providerId the id of content review implementation
	 * @param contentId the id of the content item
	 * @return an {@code int}
	 * @throws QueueException
	 * @throws ReportException
	 * @throws Exception
	 * @deprecated Use getQueuedItem method to get ContentReviewItem object
	 */
	@Deprecated
	int getReviewScore(Integer providerId, String contentId)
			throws QueueException, ReportException, Exception;

	/**
	 * Get the current status of the content item {@code org.sakaiproject.contentreview.dao.ContentReviewConstants}
	 * @param providerId the id of content review implementation
	 * @param contentId the id of the content item
	 * @return a {@code java.lang.Long}
	 * @throws QueueException
	 * @deprecated Use getQueuedItem method to get ContentReviewItem object
	 */
	@Deprecated
	Long getReviewStatus(Integer providerId, String contentId) throws QueueException;

	/**
	 * Get when the content item was first queued.
	 * @param providerId the id of content review implementation
	 * @param contentId the id of the content item
	 * @return a {@code java.util.Date}
	 * @throws QueueException
	 * @deprecated Use getQueuedItem method to get ContentReviewItem object
	 */
	@Deprecated
	Date getDateQueued(Integer providerId, String contentId) throws QueueException;

	/**
	 * Gets when the item was submitted to the content review service.
	 * @param providerId the id of content review implementation
	 * @param contentId the id of the content item
	 * @return the {@code java.util.Date}
	 * @throws QueueException
	 * @throws SubmissionException
	 * @deprecated Use getQueuedItem method to get ContentReviewItem object
	 */
	@Deprecated
	Date getDateSubmitted(Integer providerId, String contentId) throws QueueException, SubmissionException;

	/**
	 * Get the items for a site or a task.
	 * @param providerId the id of content review implementation
	 * @param siteId the site id associated with the item to be reviewed
	 * @param taskId the task id associated with the item to reviewed
	 * @return a {@code java.util.List<ContentReviewItem>}
	 */
	List<ContentReviewItem> getContentReviewItems(Integer providerId, String siteId, String taskId);

	/**
	 * Get an item that has been queued and contentId.
	 * @param providerId the id of content review implementation
	 * @param contentId the id of the content item
	 * @return {@code Optional<ContentReviewItem>}
	 */
	Optional<ContentReviewItem> getQueuedItem(Integer providerId, String contentId);
	
	/**
	 * Get an item that has been queued via external ID.
	 * @param providerId the id of content review implementation
	 * @param externalId the external id of the content item
	 * @return {@code Optional<ContentReviewItem>}
	 */
	Optional<ContentReviewItem> getQueuedItemByExternalId(Integer providerId, String externalId);

	/**
	 * Get items queued but have not yet been submitted to the content review service
	 * @param providerId the id of content review implementation
	 * @return {@code List<ContentReviewItem>}
	 */
	List<ContentReviewItem> getQueuedNotSubmittedItems(Integer providerId);
	
	/**
	 * Get the next item in the queue to be submitted to the content review service
	 * @param providerId the id of content review implementation
	 * @return  {@code Optional<ContentReviewItem>}
	 */
	Optional<ContentReviewItem> getNextItemInQueueToSubmit(Integer providerId);

	/**
	 * Get items awaiting a report from the content review service
	 * @param providerId the id of content review implementation
	 * @return {@code List<ContentReviewItem>}
	 */
	List<ContentReviewItem> getAwaitingReports(Integer providerId);
	
	/**
	 * Sets the items to be retried for a specific user and a status of {@code ContentReviewConstants}
	 * @param providerId the id of content review implementation
	 * @param userId the user id for which items to reset
	 */
	void resetUserDetailsLockedItems(Integer providerId, String userId);
	
	/**
	 * Remove an item from queue
	 * @param providerId the id of content review implementation
	 * @param contentId the id of the content item
	 */
	void removeFromQueue(Integer providerId, String contentId);
	
	/**
	 * Gets all items grouped by site and task
	 * @param providerId the id of content review implementation
	 * @return {@code List<ContentReviewItem>}
	 */
	List<ContentReviewItem> getAllContentReviewItemsGroupedBySiteAndTask(Integer providerId);

	/* Its not ideal to directly expose the direct DAO methods here, 
	 * but this was needed in order to not have to change the
	 * TurnitinImpl. These should be removed from the service when the
	 * new TurnitinImpl is written
	 */
	/**
	 * Update an item
	 * @param item to update
	 */
	@Deprecated
	void update(ContentReviewItem item);

	/**
	 * Delete an item
	 * @param item to delete
	 */
	@Deprecated
	void delete(ContentReviewItem item);
}