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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This is a POJO (data storage object)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentReviewItem {

	private Long id;
	private String contentId;
	private String userId;
	private String siteId;
	private String taskId;
	private String externalId;
	private Date dateQueued;
	private Date dateSubmitted;
	private Date dateReportReceived;
	private Date nextRetryTime;
	private Integer errorCode;
	private Integer providerId;
	private Long status;
	private Integer reviewScore;
	private String lastError;
	private Long retryCount;
	private Integer version;
	private Map<String, String> properties = new HashMap<>();

	public ContentReviewItem(String contentId, Integer providerId) {
		this(contentId, null, null, null, new Date(), ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE, providerId);
	}

	public ContentReviewItem(String contentId, String userId, String siteId, String taskId, Date dateQueued, Long status, Integer providerId) {
		this.contentId = contentId;
		this.userId = userId;
		this.siteId = siteId;
		this.taskId = taskId;
		this.dateQueued = dateQueued;
		this.status = status;
		this.providerId = providerId;
		this.nextRetryTime = new Date();
	}
}