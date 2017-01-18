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
package org.sakaiproject.scorm.service.sakai.impl;

import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.exceptions.LearnerNotDefinedException;
import org.sakaiproject.scorm.model.api.ContentPackage;
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

	public boolean canLaunchAttempt(ContentPackage contentPackage, long attemptNumber) {
	    return true;
    }

	public boolean canLaunch(ContentPackage contentPackage) {
	    return canLaunchAttempt(contentPackage, -1);
    }

}
