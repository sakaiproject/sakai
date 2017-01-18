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
import java.util.LinkedList;
import java.util.List;

public class Interaction implements Serializable {

	private static final long serialVersionUID = 1L;

	private String interactionId;

	private String type;

	private List<String> objectiveIds;

	private String timestamp;

	private List<String> correctResponses;

	private double weighting;

	private String learnerResponse;

	private String result;

	private String latency;

	private String description;

	private long contentPackageId;

	private String learnerId;

	private String scoId;

	private long attemptNumber;

	private String activityTitle;

	private List<Objective> objectives;

	public Interaction() {
		objectiveIds = new LinkedList<String>();
		correctResponses = new LinkedList<String>();
		objectives = new LinkedList<Objective>();
	}

	public String getActivityTitle() {
		return activityTitle;
	}

	public long getAttemptNumber() {
		return attemptNumber;
	}

	public long getContentPackageId() {
		return contentPackageId;
	}

	public List<String> getCorrectResponses() {
		return correctResponses;
	}

	public String getDescription() {
		return description;
	}

	public String getInteractionId() {
		return interactionId;
	}

	public String getLatency() {
		return latency;
	}

	public String getLearnerId() {
		return learnerId;
	}

	public String getLearnerResponse() {
		return learnerResponse;
	}

	public List<String> getObjectiveIds() {
		return objectiveIds;
	}

	public List<Objective> getObjectives() {
		return objectives;
	}

	public String getResult() {
		return result;
	}

	public String getScoId() {
		return scoId;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getType() {
		return type;
	}

	public double getWeighting() {
		return weighting;
	}

	public void setActivityTitle(String activityTitle) {
		this.activityTitle = activityTitle;
	}

	public void setAttemptNumber(long attemptNumber) {
		this.attemptNumber = attemptNumber;
	}

	public void setContentPackageId(long contentPackageId) {
		this.contentPackageId = contentPackageId;
	}

	public void setCorrectResponses(List<String> correctResponses) {
		this.correctResponses = correctResponses;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setInteractionId(String id) {
		this.interactionId = id;
	}

	public void setLatency(String latency) {
		this.latency = latency;
	}

	public void setLearnerId(String learnerId) {
		this.learnerId = learnerId;
	}

	public void setLearnerResponse(String learnerResponse) {
		this.learnerResponse = learnerResponse;
	}

	public void setObjectiveIds(List<String> objectiveIds) {
		this.objectiveIds = objectiveIds;
	}

	public void setObjectives(List<Objective> objectives) {
		this.objectives = objectives;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public void setScoId(String scoId) {
		this.scoId = scoId;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setWeighting(double weighting) {
		this.weighting = weighting;
	}

}
