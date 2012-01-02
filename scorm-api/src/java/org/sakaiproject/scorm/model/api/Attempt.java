package org.sakaiproject.scorm.model.api;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Attempt implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
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


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
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

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((id == null) ? 0 : id.hashCode());
	    return result;
    }

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    Attempt other = (Attempt) obj;
	    if (id == null) {
		    if (other.id != null)
			    return false;
	    } else if (!id.equals(other.id))
		    return false;
	    return true;
    }

}
