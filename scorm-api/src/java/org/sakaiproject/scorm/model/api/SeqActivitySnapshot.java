package org.sakaiproject.scorm.model.api;

public class SeqActivitySnapshot {

	private long id;
	
	private String activityId;
	
	private String resourceId;
	
	private String scoId;

	
	
	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getScoId() {
		return scoId;
	}

	public void setScoId(String scoId) {
		this.scoId = scoId;
	}
	
}
