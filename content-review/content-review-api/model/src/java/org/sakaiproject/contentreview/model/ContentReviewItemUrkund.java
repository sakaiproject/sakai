/**********************************************************************************
 *
 * Copyright (c) 2017 Sakai Foundation
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
 **********************************************************************************/

package org.sakaiproject.contentreview.model;

import java.util.Date;


public class ContentReviewItemUrkund extends ContentReviewItem {

	private String reportUrl = "";
	private String optOutUrl = "";

	public ContentReviewItemUrkund() {
	}
	
	public ContentReviewItemUrkund(String userId, String siteId, String taskId, String contentId, Date dateQueued, Long status) {
		super(userId, siteId, taskId, contentId, dateQueued, status);
	}


	public String getReportUrl() {
		return reportUrl;
	}

	public void setReportUrl(String reportUrl) {
		this.reportUrl = reportUrl;
	}

	public String getOptOutUrl() {
		return optOutUrl;
	}

	public void setOptOutUrl(String optOutUrl) {
		this.optOutUrl = optOutUrl;
	}
}
