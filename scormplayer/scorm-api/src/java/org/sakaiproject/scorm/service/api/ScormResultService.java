package org.sakaiproject.scorm.service.api;

import java.util.List;

import org.sakaiproject.scorm.model.api.Attempt;

public interface ScormResultService {

	public Attempt lookupAttempt(String courseId, String learnerId, int attemptNumber);
	
	public List<Attempt> getAttempts(String courseId);
	
	
	
}
