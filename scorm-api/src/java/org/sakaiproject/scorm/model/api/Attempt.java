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

	public long getAttemptNumber() {
		return attemptNumber;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public long getContentPackageId() {
		return contentPackageId;
	}

	public String getCourseId() {
		return courseId;
	}

	public Long getDataManagerId(String scoId) {
		return scoDataManagerMap.get(scoId);
	}

	public Long getId() {
		return id;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public String getLearnerId() {
		return learnerId;
	}

	public String getLearnerName() {
		return learnerName;
	}

	public boolean getNotExited() {
		return isNotExited;
	}

	public Map<String, Long> getScoDataManagerMap() {
		return scoDataManagerMap;
	}

	public boolean getSuspended() {
		return isSuspended;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/*public long getDataManagerId() {
		return dataManagerId;
	}


	public void setDataManagerId(long dataManagerId) {
		this.dataManagerId = dataManagerId;
	}*/

	public boolean isNotExited() {
		return isNotExited;
	}

	public boolean isSuspended() {
		return isSuspended;
	}

	public void setAttemptNumber(long attemptNumber) {
		this.attemptNumber = attemptNumber;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public void setContentPackageId(long contentPackageId) {
		this.contentPackageId = contentPackageId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public void setDataManagerId(String scoId, Long dataManagerId) {
		if (scoId != null) {
			scoDataManagerMap.put(scoId, dataManagerId);
		}
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public void setLearnerId(String learnerId) {
		this.learnerId = learnerId;
	}

	public void setLearnerName(String learnerName) {
		this.learnerName = learnerName;
	}

	public void setNotExited(boolean isNotExited) {
		this.isNotExited = isNotExited;
	}

	public void setScoDataManagerMap(Map<String, Long> scoDataManagerMap) {
		this.scoDataManagerMap = scoDataManagerMap;
	}

	public void setSuspended(boolean isSuspended) {
		this.isSuspended = isSuspended;
	}

}
