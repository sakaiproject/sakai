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

public class ContentReviewConstants {
	public static final String CONTENT_REVIEW_NOT_SUBMITTED = "Content awaiting submission";
	public static final Long CONTENT_REVIEW_NOT_SUBMITTED_CODE = new Long(1);

	public static final String CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT = "Content submitted for review and awaiting report";
	public static final Long CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE = new Long(2);

	public static final String CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE = "Content submitted and report available";
	public static final Long CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE = new Long(3);

	public static final String CONTENT_REVIEW_SUBMISSION_ERROR_RETRY = "Temporary error occurred submitting content - will retry";
	public static final Long CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE = new Long(4);

	public static final String CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY = "Error occurred submitting content - will not retry";
	public static final Long CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE = new Long(5);

	public static final String CONTENT_REVIEW_SUBMISSION_ERROR_USER_DETAILS = "Error occurred submitting content - inconplete or Ivalid user details";
	public static final Long CONTENT_REVIEW_SUBMISSION_ERROR_USER_DETAILS_CODE = new Long(6);

	public static final String CONTENT_REVIEW_REPORT_ERROR_RETRY = "Temporary error occurred retrieving report - will retry";
	public static final Long CONTENT_REVIEW_REPORT_ERROR_RETRY_CODE = new Long(7);

	public static final String CONTENT_REVIEW_REPORT_ERROR_NO_RETRY = "Error occurred retrieving report - will not retry";
	public static final Long CONTENT_REVIEW_REPORT_ERROR_NO_RETRY_CODE = new Long(8);

	public static final String CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_EXCEEDED = "Error number of retries exceeded";
	public static final Long CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_EXCEEDED_CODE = new Long(9);

	//URKUND PROPERTIES
	public static final String URKUND_OPTOUT_URL = "URKUND_OPTOUT_URL";
	public static final String URKUND_REPORT_URL = "URKUND_REPORT_URL";
	
	public static final int TURNITIN_PROVIDER_ID = 199481773;
	public static final int TURNITINOC_PROVIDER_ID = 1571541951;
}
