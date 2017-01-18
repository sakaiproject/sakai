/**
 * Copyright (c) 2007 The Apereo Foundation
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

	public double getCompletionThreshold() {
		return completionThreshold;
	}

	public String getLearnerLocation() {
		return learnerLocation;
	}

	public long getMaxSecondsAllowed() {
		return maxSecondsAllowed;
	}

	public double getProgressMeasure() {
		return progressMeasure;
	}

	public String getSuccessStatus() {
		return successStatus;
	}

	public String getTotalSessionSeconds() {
		return totalSessionSeconds;
	}

	public void setCompletionStatus(String completionStatus) {
		this.completionStatus = completionStatus;
	}

	public void setCompletionThreshold(double completionThreshold) {
		this.completionThreshold = completionThreshold;
	}

	public void setLearnerLocation(String learnerLocation) {
		this.learnerLocation = learnerLocation;
	}

	public void setMaxSecondsAllowed(long maxSecondsAllowed) {
		this.maxSecondsAllowed = maxSecondsAllowed;
	}

	public void setProgressMeasure(double progressMeasure) {
		this.progressMeasure = progressMeasure;
	}

	public void setSuccessStatus(String successStatus) {
		this.successStatus = successStatus;
	}

	public void setTotalSessionSeconds(String totalSessionSeconds) {
		this.totalSessionSeconds = totalSessionSeconds;
	}

}
