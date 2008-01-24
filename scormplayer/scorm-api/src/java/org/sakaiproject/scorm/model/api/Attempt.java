package org.sakaiproject.scorm.model.api;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Attempt implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long id;
	private long contentPackageId;
	private String courseId;
	private String learnerId;
	private String learnerName;
	private long attemptNumber;
	private Date beginDate;
	private Date lastModifiedDate;
	private Map<String, Long> scoDataManagerMap;
	
	private boolean isNotExited;
	private boolean isSuspended;
	
	public Attempt() {
		this.isNotExited = true;
		this.isSuspended = false;
		this.scoDataManagerMap = new HashMap<String, Long>();
	}

	public Long getDataManagerId(String scoId) {
		return scoDataManagerMap.get(scoId);
	}
	
	public void setDataManagerId(String scoId, Long dataManagerId) {
		if (scoId != null)
			scoDataManagerMap.put(scoId, dataManagerId);
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


	/*public long getDataManagerId() {
		return dataManagerId;
	}


	public void setDataManagerId(long dataManagerId) {
		this.dataManagerId = dataManagerId;
	}*/


	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public boolean isSuspended() {
		return isSuspended;
	}

	public boolean getSuspended() {
		return isSuspended;
	}

	public void setSuspended(boolean isSuspended) {
		this.isSuspended = isSuspended;
	}


	public boolean isNotExited() {
		return isNotExited;
	}

	public boolean getNotExited() {
		return isNotExited;
	}

	public void setNotExited(boolean isNotExited) {
		this.isNotExited = isNotExited;
	}


	public long getContentPackageId() {
		return contentPackageId;
	}


	public void setContentPackageId(long contentPackageId) {
		this.contentPackageId = contentPackageId;
	}

	public Map<String, Long> getScoDataManagerMap() {
		return scoDataManagerMap;
	}

	public void setScoDataManagerMap(Map<String, Long> scoDataManagerMap) {
		this.scoDataManagerMap = scoDataManagerMap;
	}

}
