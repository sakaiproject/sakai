/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.model.api;

import java.io.Serializable;

public class Progress implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// cmi.progress_measure
	private double progressMeasure;
	// cmi.completion_threshold
	private double completionThreshold;
	// cmi.completion_status
	private String completionStatus;
	// cmi.success_status
	private String successStatus;
	// cmi.location
	private String learnerLocation;
	// cmi.max_time_allowed
	private long maxSecondsAllowed;
	// cmi.total_time
	private String totalSessionSeconds;
	
	
	public String getCompletionStatus() {
		return completionStatus;
	}
	public void setCompletionStatus(String completionStatus) {
		this.completionStatus = completionStatus;
	}
	public String getSuccessStatus() {
		return successStatus;
	}
	public void setSuccessStatus(String successStatus) {
		this.successStatus = successStatus;
	}
	public String getLearnerLocation() {
		return learnerLocation;
	}
	public void setLearnerLocation(String learnerLocation) {
		this.learnerLocation = learnerLocation;
	}
	public long getMaxSecondsAllowed() {
		return maxSecondsAllowed;
	}
	public void setMaxSecondsAllowed(long maxSecondsAllowed) {
		this.maxSecondsAllowed = maxSecondsAllowed;
	}
	public String getTotalSessionSeconds() {
		return totalSessionSeconds;
	}
	public void setTotalSessionSeconds(String totalSessionSeconds) {
		this.totalSessionSeconds = totalSessionSeconds;
	}
	public double getProgressMeasure() {
		return progressMeasure;
	}
	public void setProgressMeasure(double progressMeasure) {
		this.progressMeasure = progressMeasure;
	}
	public double getCompletionThreshold() {
		return completionThreshold;
	}
	public void setCompletionThreshold(double completionThreshold) {
		this.completionThreshold = completionThreshold;
	}
	
}
