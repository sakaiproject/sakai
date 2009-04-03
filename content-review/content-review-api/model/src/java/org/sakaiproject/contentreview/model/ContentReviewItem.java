/******************************************************************************
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *****************************************************************************/

package org.sakaiproject.contentreview.model;

import java.util.Date;

/**
 * This is a POJO (data storage object)
 * @author David Jacka
 */
public class ContentReviewItem {
	
	public static final String NOT_SUBMITTED = "Content awaiting submission";
	public static final Long NOT_SUBMITTED_CODE = new Long(1);
	
	
	public static final String SUBMITTED_AWAITING_REPORT = "Content submitted for review and awaiting report";
	public static final Long SUBMITTED_AWAITING_REPORT_CODE= new Long(2);
	
	public static final String SUBMITTED_REPORT_AVAILABLE = "Content submitted and report available";
	public static final Long SUBMITTED_REPORT_AVAILABLE_CODE = new Long(3);
	
	public static final String SUBMISSION_ERROR_RETRY = "Temporary error occurred submitting content - will retry";
	public static final Long SUBMISSION_ERROR_RETRY_CODE = new Long(4);
	
	public static final String SUBMISSION_ERROR_NO_RETRY = "Error occurred submitting content - will not retry";
	public static final Long SUBMISSION_ERROR_NO_RETRY_CODE = new Long(5);
	
	public static final String SUBMISSION_ERROR_USER_DETAILS = "Error occurred submitting content - inconplete or Ivalid user details";
	public static final Long SUBMISSION_ERROR_USER_DETAILS_CODE = new Long(6);
	
	public static final String REPORT_ERROR_RETRY = "Temporary error occurred retrieving report - will retry";
	public static final Long REPORT_ERROR_RETRY_CODE = new Long(7);
	
	public static final String REPORT_ERROR_NO_RETRY = "Error occurred retrieving report - will not retry";
	public static final Long REPORT_ERROR_NO_RETRY_CODE = new Long(8);
	
	public static final Long SUBMISSION_ERROR_RETRY_EXCEEDED = new Long(9);
	
	private Long id; //hibernate uses this as a primary key
	private String contentId; //Sakai contentId
	private String userId; // Sakai userId
	private String siteId; // Sakai siteId
	private String taskId; // Sakai taskId
	private String externalId; //the id from the external reviewer
	private Date dateQueued;
	private Date dateSubmitted;
	private Date dateReportReceived;
	private Date nextRetryTime;
	
	private Long status;
	private Integer reviewScore;
	private String lastError;
	private String iconUrl;
	private Long retryCount;
	/**
	 * Default constructor
	 */
	public ContentReviewItem() {
		contentId = null;
	}

	/**
	 * Minimal constructor
	 */
	public ContentReviewItem(String contentId) {
		this.userId = null;
		this.siteId = null;
		this.contentId = contentId;
		this.externalId = null;
		this.dateQueued = null;
		this.dateSubmitted = null;
		this.dateReportReceived = null;
		this.status = null;
		this.reviewScore = null;
		this.taskId = null;
		this.retryCount = null;
	}
	
	/**
	 * A constructor that sets all members to null and status to the provided string. This
	 * is used for retrieving items by status.
	 * @param status
	 */
	
	/**
	 * Constructor for a newly queued item
	 */
	
	public ContentReviewItem(String userId, String siteId, String taskId, String contentId, Date dateQueued, Long status) {
		this.userId = userId;
		this.siteId = siteId;
		this.contentId = contentId;
		this.dateQueued = dateQueued;
		this.dateSubmitted = null;
		this.dateReportReceived = null;
		this.status = status;
		this.reviewScore = null;
		this.taskId = taskId;
		this.id = null;
	}

	/**
	 * Full constructor
	 */
	public ContentReviewItem(String userId, String siteId, String taskId, String contentId, String externalId, 
							Date dateQueued, Date dateSubmitted, Date dateReportReceived, long status,
							Integer reviewScore) {
		this.userId = userId;
		this.siteId = siteId;
		this.contentId = contentId;
		this.externalId = externalId;
		this.dateQueued = dateQueued;
		this.dateSubmitted = dateSubmitted;
		this.dateReportReceived = dateReportReceived;
		this.status = status;
		this.reviewScore = reviewScore;
		this.taskId = taskId;
	}

	
	/**
	 * Getters and Setters
	 */
	
	public Date getDateQueued() {
		return dateQueued;
	}

	public void setDateQueued(Date dateQueued) {
		this.dateQueued = dateQueued;
	}

	public Date getDateReportReceived() {
		return dateReportReceived;
	}

	public void setDateReportReceived(Date dateReportReceived) {
		this.dateReportReceived = dateReportReceived;
	}

	public Date getDateSubmitted() {
		return dateSubmitted;
	}

	public void setDateSubmitted(Date dateSubmitted) {
		this.dateSubmitted = dateSubmitted;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public Integer getReviewScore() {
		return reviewScore;
	}

	public void setReviewScore(Integer reviewScore) {
		this.reviewScore = reviewScore;
	}

	public Long getStatus() {
		return status;
	}

	public void setStatus(Long status) {
		this.status = status;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	
	public String getLastError() {
		return this.lastError;
	}
	
	public void setLastError(String le) {
		this.lastError = le;
	}
	
	public void setIconURL(String u) {
		this.iconUrl = u;
	}
	
	public String getIconUrl() {
		return this.iconUrl;
	}
	
	public Long getRetryCount() {
		return this.retryCount;
	}
	
	public void setRetryCount(Long l) {
		this.retryCount = l;
	}

	public Date getNextRetryTime() {
		return nextRetryTime;
	}

	public void setNextRetryTime(Date nextRetryTime) {
		this.nextRetryTime = nextRetryTime;
	}
}
