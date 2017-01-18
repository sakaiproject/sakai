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

public class ActivitySummary implements Serializable {

	private static final long serialVersionUID = 1L;

	// General data
	private String scoId;

	private String title;

	private long contentPackageId;

	private String learnerId;

	private long attemptNumber;

	// Progress data
	private double progressMeasure;

	private double completionThreshold;

	private String completionStatus;

	private String successStatus;

	private String learnerLocation;

	private long maxSecondsAllowed;

	private String totalSessionSeconds;

	// Score data
	private double scaled;

	private double raw;

	private double min;

	private double max;

	private double scaledToPass;

	public long getAttemptNumber() {
		return attemptNumber;
	}

	public String getCompletionStatus() {
		return completionStatus;
	}

	public double getCompletionThreshold() {
		return completionThreshold;
	}

	public long getContentPackageId() {
		return contentPackageId;
	}

	public String getLearnerId() {
		return learnerId;
	}

	public String getLearnerLocation() {
		return learnerLocation;
	}

	public double getMax() {
		return max;
	}

	public long getMaxSecondsAllowed() {
		return maxSecondsAllowed;
	}

	public double getMin() {
		return min;
	}

	public double getProgressMeasure() {
		return progressMeasure;
	}

	public double getRaw() {
		return raw;
	}

	public double getScaled() {
		return scaled;
	}

	public double getScaledToPass() {
		return scaledToPass;
	}

	public String getScoId() {
		return scoId;
	}

	public String getSuccessStatus() {
		return successStatus;
	}

	public String getTitle() {
		return title;
	}

	public String getTotalSessionSeconds() {
		return totalSessionSeconds;
	}

	public void setAttemptNumber(long attemptNumber) {
		this.attemptNumber = attemptNumber;
	}

	public void setCompletionStatus(String completionStatus) {
		this.completionStatus = completionStatus;
	}

	public void setCompletionThreshold(double completionThreshold) {
		this.completionThreshold = completionThreshold;
	}

	public void setContentPackageId(long contentPackageId) {
		this.contentPackageId = contentPackageId;
	}

	public void setLearnerId(String learnerId) {
		this.learnerId = learnerId;
	}

	public void setLearnerLocation(String learnerLocation) {
		this.learnerLocation = learnerLocation;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public void setMaxSecondsAllowed(long maxSecondsAllowed) {
		this.maxSecondsAllowed = maxSecondsAllowed;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public void setProgressMeasure(double progressMeasure) {
		this.progressMeasure = progressMeasure;
	}

	public void setRaw(double raw) {
		this.raw = raw;
	}

	public void setScaled(double scaled) {
		this.scaled = scaled;
	}

	public void setScaledToPass(double scaledToPass) {
		this.scaledToPass = scaledToPass;
	}

	public void setScoId(String scoId) {
		this.scoId = scoId;
	}

	public void setSuccessStatus(String successStatus) {
		this.successStatus = successStatus;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTotalSessionSeconds(String totalSessionSeconds) {
		this.totalSessionSeconds = totalSessionSeconds;
	}

}
