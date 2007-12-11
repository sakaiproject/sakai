package org.sakaiproject.scorm.model.api;

import java.io.Serializable;
import java.util.Date;

public class Attempt implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long id;
	private String courseId;
	private String learnerId;
	private String learnerName;
	private long attemptNumber;
	private String scoreScaled;
	private String successStatus;
	private String completionStatus;
	private Date beginDate;
	private Date lastModifiedDate;
	
	private long dataManagerId = -1;
	
	private boolean isNotExited = true;
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


	public String getCourseId() {
		return courseId;
	}


	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}


	public Date getBeginDate() {
		return beginDate;
	}


	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}


	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}


	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}


	public long getDataManagerId() {
		return dataManagerId;
	}


	public void setDataManagerId(long dataManagerId) {
		this.dataManagerId = dataManagerId;
	}


	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public boolean isSuspended() {
		return isSuspended;
	}


	public void setSuspended(boolean isSuspended) {
		this.isSuspended = isSuspended;
	}


	public boolean isNotExited() {
		return isNotExited;
	}


	public void setNotExited(boolean isNotExited) {
		this.isNotExited = isNotExited;
	}

}
