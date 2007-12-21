package org.sakaiproject.scorm.service.api;

import java.util.List;

import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.CMIFieldGroup;
import org.sakaiproject.scorm.model.api.Learner;

public interface ScormResultService {

	public CMIFieldGroup getAttemptResults(Attempt attempt);
	
	public Attempt getAttempt(long id);
	
	public List<Attempt> getAttempts(long contentPackageId);
	
	public List<Attempt> getAttemtps(long contentPackageId, String learnerId);
	
	public List<Attempt> getAttempts(String courseId, String learnerId);
	
	public List<Learner> getLearners(long contentPackageId);
	
	public void saveAttempt(Attempt attempt);
	
}
