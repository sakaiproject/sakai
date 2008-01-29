package org.sakaiproject.scorm.model.api;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Interaction implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	private String type;
	private List<String> objectiveIds;
	private String timestamp;
	private List<String> correctResponses;
	private double weighting;
	private String learnerResponse;
	private String result;
	private String latency;
	private String description;
	
	public Interaction() {
		objectiveIds = new LinkedList<String>();
		correctResponses = new LinkedList<String>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public List<String> getCorrectResponses() {
		return correctResponses;
	}

	public void setCorrectResponses(List<String> correctResponses) {
		this.correctResponses = correctResponses;
	}

	public double getWeighting() {
		return weighting;
	}

	public void setWeighting(double weighting) {
		this.weighting = weighting;
	}

	public String getLearnerResponse() {
		return learnerResponse;
	}

	public void setLearnerResponse(String learnerResponse) {
		this.learnerResponse = learnerResponse;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getLatency() {
		return latency;
	}

	public void setLatency(String latency) {
		this.latency = latency;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getObjectiveIds() {
		return objectiveIds;
	}

	public void setObjectiveIds(List<String> objectiveIds) {
		this.objectiveIds = objectiveIds;
	}
	
}
