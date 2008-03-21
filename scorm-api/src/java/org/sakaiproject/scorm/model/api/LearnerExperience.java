package org.sakaiproject.scorm.model.api;

import java.io.Serializable;
import java.util.Date;

import org.sakaiproject.scorm.api.ScormConstants;

public class LearnerExperience implements Serializable, ScormConstants {

	private static final long serialVersionUID = 1L;
	
	private String learnerName;
	private String learnerId;
	private long contentPackageId;
	private String progress;
	private String score;
	private int numberOfAttempts;

	private Date lastAttemptDate;
	private int status;
	
	private String previousLearnerIds;
	private String nextLearnerIds;
	
	public LearnerExperience(Learner learner, long contentPackageId) {
		this.learnerName = new StringBuilder(learner.getDisplayName()).append(" (")
			.append(learner.getDisplayId()).append(")").toString();
		this.learnerId = learner.getId();
		this.contentPackageId = contentPackageId;
		this.numberOfAttempts = 0;
	}

	public String getLearnerName() {
		return learnerName;
	}

	public void setLearnerName(String learnerName) {
		this.learnerName = learnerName;
	}

	public String getLearnerId() {
		return learnerId;
	}

	public void setLearnerId(String learnerId) {
		this.learnerId = learnerId;
	}

	public long getContentPackageId() {
		return contentPackageId;
	}

	public void setContentPackageId(long contentPackageId) {
		this.contentPackageId = contentPackageId;
	}

	public int getNumberOfAttempts() {
		return numberOfAttempts;
	}

	public void setNumberOfAttempts(int numberOfAttempts) {
		this.numberOfAttempts = numberOfAttempts;
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public Date getLastAttemptDate() {
		return lastAttemptDate;
	}

	public void setLastAttemptDate(Date lastAttemptDate) {
		this.lastAttemptDate = lastAttemptDate;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getPreviousLearnerIds() {
		return previousLearnerIds;
	}

	public void setPreviousLearnerIds(String previousLearnerId) {
		this.previousLearnerIds = previousLearnerId;
	}

	public String getNextLearnerIds() {
		return nextLearnerIds;
	}

	public void setNextLearnerIds(String nextLearnerId) {
		this.nextLearnerIds = nextLearnerId;
	}
	
}
