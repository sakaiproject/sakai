package org.sakaiproject.scorm.service.api;

import org.sakaiproject.scorm.exceptions.LearnerNotDefinedException;
import org.sakaiproject.scorm.model.api.Learner;

public interface LearningManagementSystem {

	public boolean canConfigure(String context);

	public boolean canDelete(String context);

	public boolean canGrade(String context);

	public boolean canLaunch(String context);

	public boolean canLaunchNewWindow();

	public boolean canModify(String context);

	public boolean canUpload(String context);

	public boolean canUseRelativeUrls();

	public boolean canValidate(String context);

	public boolean canViewResults(String context);

	public String currentContext();

	public String currentLearnerId();

	public Learner getLearner(String learnerId) throws LearnerNotDefinedException;

	public String getLearnerName(String learnerId);

	public boolean isOwner();

}
