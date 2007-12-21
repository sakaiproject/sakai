package org.sakaiproject.scorm.model.api;

public class LearnerExperience {

	private Learner learner;
	private long contentPackageId;
	private int numberOfAttempts;
	
	public LearnerExperience(Learner learner, long contentPackageId) {
		this.learner = learner;
		this.contentPackageId = contentPackageId;
	}
	
}
