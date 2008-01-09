package org.sakaiproject.scorm.service.api;

import org.sakaiproject.scorm.exceptions.LearnerNotDefinedException;
import org.sakaiproject.scorm.model.api.Learner;

public interface LearningManagementSystem {

	public String currentContext();
	
	public String currentLearnerId();
	
	public boolean canLaunchNewWindow();
	
	public boolean canUseRelativeUrls();
	
	public boolean canModify(String context);
	
	public boolean canConfigure(String context);
	
	public boolean canViewResults(String context);
	
	public boolean canLaunch(String context);
	
	public boolean canDelete(String context);
	
	public boolean canUpload(String context);
	
	public boolean canValidate(String context);
	
	public String getLearnerName(String learnerId);
	
	public Learner getLearner(String learnerId) throws LearnerNotDefinedException;
	
	public boolean isOwner();
	
}
