package org.sakaiproject.scorm.model.api;

import java.io.Serializable;

public class Attempt implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String learnerId;
	private String learnerName;
	private long attemptNumber;
	private String scoreScaled;
	private String successStatus;
	private String completionStatus;
	
	
	
	private boolean isSuspended = false;
	
	
	public Attempt() {
		
	}
	

	public String getSuccessStatus() {
		return successStatus;
	}

	public void setSuccessStatus(String successStatus) {
		this.successStatus = successStatus;
	}


	public String getScoreScaled() {
		return scoreScaled;
	}


	public void setScoreScaled(String scoreScaled) {
		this.scoreScaled = scoreScaled;
	}


	public String getCompletionStatus() {
		return completionStatus;
	}


	public void setCompletionStatus(String completionStatus) {
		this.completionStatus = completionStatus;
	}


	public String getLearnerId() {
		return learnerId;
	}


	public void setLearnerId(String learnerId) {
		this.learnerId = learnerId;
	}


	public String getLearnerName() {
		return learnerName;
	}


	public void setLearnerName(String learnerName) {
		this.learnerName = learnerName;
	}


	public long getAttemptNumber() {
		return attemptNumber;
	}


	public void setAttemptNumber(long attemptNumber) {
		this.attemptNumber = attemptNumber;
	}
	
}
