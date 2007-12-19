package org.sakaiproject.scorm.service.api;

import java.util.List;

import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.CMIFieldGroup;

public interface ScormResultService {

	public CMIFieldGroup getAttemptResults(Attempt attempt);
	
	public Attempt getAttempt(long id);
	
	public List<Attempt> getAttempts(long contentPackageId);
	
	public List<Attempt> getAttempts(String courseId, String learnerId);
	
	public void saveAttempt(Attempt attempt);
	
}
