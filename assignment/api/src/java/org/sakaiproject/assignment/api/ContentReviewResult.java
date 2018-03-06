/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.assignment.api;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.dao.ContentReviewConstants;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.entity.api.ResourceProperties;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Introduced for SAK-26322
 * A ContentReviewResult represents the results of a content review item
 */
@Slf4j
@Data
public class ContentReviewResult {
	/**
	 * The ContentResource which was reviewed
	 */
	private ContentResource contentResource;
	
	/**
	 * The ContentReviewItem obtained from the ContentResource
	 */
	private ContentReviewItem contentReviewItem;

	/**
	 * The URL to the content review report
	 */
	private String reviewReport;

	/**
	 * The css class of the content review icon associated with this item
	 */
	private String reviewIconCssClass;

	/**
	 * An error string, if any, return from the review service
	 */
	private String reviewError;

	
	/**
	 * Get status directly from ContentReviewItem
	 */
	public Long getStatus(){
		return contentReviewItem.getStatus();
	}

	public int getReviewScore() {

		if (contentResource == null){
			log.debug(this + " getReviewScore() called with contentResource == null");
			return -2;
		}
		try {
			//get the status from the ContentReviewItem, if it's in a valid status, get the score
			Long status = getStatus();
			if (status != null && (status.equals(ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE) || status.equals(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE)))	{
				log.debug(this + " getReviewStatus returned a state of: " + status);
				return -2;
			}

			int score = contentReviewItem.getReviewScore();
			log.debug(this + " getReviewScore(ContentResource) CR returned a score of: " + score);
			return score;
		} catch (Exception cie) {
			log.error("No tiene scoreeee {} ", contentResource.getId());
			return -2;
		}
	}
	
	/**
	 * Determines if this ContentReview result is from a student's inline text
	 */
	public boolean isInline() {
		ResourceProperties props = contentResource.getProperties();
		return "true".equals(props.getProperty(AssignmentConstants.PROP_INLINE_SUBMISSION));
	}
}
