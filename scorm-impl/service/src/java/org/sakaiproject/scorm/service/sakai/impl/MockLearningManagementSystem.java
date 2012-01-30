package org.sakaiproject.scorm.service.sakai.impl;

import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.exceptions.LearnerNotDefinedException;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;

public class MockLearningManagementSystem implements LearningManagementSystem, ScormConstants {

	public boolean canConfigure(String context) {
		return hasPermission(context, "scorm.configure");
	}

	public boolean canDelete(String context) {
		return hasPermission(context, "scorm.delete");
	}

	public boolean canGrade(String context) {
		return hasPermission(context, "scorm.grade");
	}

	public boolean canLaunch(String context) {
		return hasPermission(context, "scorm.launch");
	}

	public boolean canLaunchNewWindow() {
		return true;
	}

	public boolean canModify(String context) {
		return canConfigure(context) || canDelete(context) || canGrade(context);
	}

	public boolean canUpload(String context) {
		return hasPermission(context, "scorm.upload");
	}

	public boolean canUseRelativeUrls() {
		return false;
	}

	public boolean canValidate(String context) {
		return hasPermission(context, "scorm.validate");
	}

	public boolean canViewResults(String context) {
		return hasPermission(context, "scorm.view.results");
	}

	public String currentContext() {
		return "context1";
	}

	public String currentLearnerId() {
		return "learner1";
	}

	public Learner getLearner(String learnerId) throws LearnerNotDefinedException {
		return new Learner(currentLearnerId(), getLearnerName(currentLearnerId()), "learner1");
	}

	public String getLearnerName(String learnerId) {

		return "Scott Scorm";
	}

	protected boolean hasPermission(String context, String lock) {

		return true;
	}

	public boolean isOwner() {
		return true;
	}

	protected boolean unlockCheck(String lock, String ref) {
		return true;

	}

}
