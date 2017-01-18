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
		this.learnerName = new StringBuilder(learner.getDisplayName()).append(" (").append(learner.getDisplayId()).append(")").toString();
		this.learnerId = learner.getId();
		this.contentPackageId = contentPackageId;
		this.numberOfAttempts = 0;
	}

	public long getContentPackageId() {
		return contentPackageId;
	}

	public Date getLastAttemptDate() {
		return lastAttemptDate;
	}

	public String getLearnerId() {
		return learnerId;
	}

	public String getLearnerName() {
		return learnerName;
	}

	public String getNextLearnerIds() {
		return nextLearnerIds;
	}

	public int getNumberOfAttempts() {
		return numberOfAttempts;
	}

	public String getPreviousLearnerIds() {
		return previousLearnerIds;
	}

	public String getProgress() {
		return progress;
	}

	public String getScore() {
		return score;
	}

	public int getStatus() {
		return status;
	}

	public void setContentPackageId(long contentPackageId) {
		this.contentPackageId = contentPackageId;
	}

	public void setLastAttemptDate(Date lastAttemptDate) {
		this.lastAttemptDate = lastAttemptDate;
	}

	public void setLearnerId(String learnerId) {
		this.learnerId = learnerId;
	}

	public void setLearnerName(String learnerName) {
		this.learnerName = learnerName;
	}

	public void setNextLearnerIds(String nextLearnerId) {
		this.nextLearnerIds = nextLearnerId;
	}

	public void setNumberOfAttempts(int numberOfAttempts) {
		this.numberOfAttempts = numberOfAttempts;
	}

	public void setPreviousLearnerIds(String previousLearnerId) {
		this.previousLearnerIds = previousLearnerId;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
