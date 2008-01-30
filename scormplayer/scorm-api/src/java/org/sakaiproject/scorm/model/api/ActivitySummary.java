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
	
	public String getScoId() {
		return scoId;
	}
	public void setScoId(String scoId) {
		this.scoId = scoId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
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
	public double getScaled() {
		return scaled;
	}
	public void setScaled(double scaled) {
		this.scaled = scaled;
	}
	public double getRaw() {
		return raw;
	}
	public void setRaw(double raw) {
		this.raw = raw;
	}
	public double getMin() {
		return min;
	}
	public void setMin(double min) {
		this.min = min;
	}
	public double getMax() {
		return max;
	}
	public void setMax(double max) {
		this.max = max;
	}
	public double getScaledToPass() {
		return scaledToPass;
	}
	public void setScaledToPass(double scaledToPass) {
		this.scaledToPass = scaledToPass;
	}
	public long getContentPackageId() {
		return contentPackageId;
	}
	public void setContentPackageId(long contentPackageId) {
		this.contentPackageId = contentPackageId;
	}
	public String getLearnerId() {
		return learnerId;
	}
	public void setLearnerId(String learnerId) {
		this.learnerId = learnerId;
	}
	public long getAttemptNumber() {
		return attemptNumber;
	}
	public void setAttemptNumber(long attemptNumber) {
		this.attemptNumber = attemptNumber;
	}
	
	
}
