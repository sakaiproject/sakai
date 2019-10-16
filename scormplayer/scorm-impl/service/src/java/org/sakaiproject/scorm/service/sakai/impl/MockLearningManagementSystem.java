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


import static org.sakaiproject.scorm.api.ScormConstants.*;
import org.sakaiproject.scorm.exceptions.LearnerNotDefinedException;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.time.api.UserTimeService;

public class MockLearningManagementSystem implements LearningManagementSystem
{
	@Override
	public boolean canConfigure(String context)
	{
		return hasPermission(context, PERM_CONFIG);
	}

	@Override
	public boolean canDelete(String context)
	{
		return hasPermission(context, PERM_DELETE);
	}

	@Override
	public boolean canGrade(String context)
	{
		return hasPermission(context, PERM_GRADE);
	}

	public boolean canLaunch(String context)
	{
		return hasPermission(context, PERM_LAUNCH);
	}

	@Override
	public boolean canLaunchNewWindow()
	{
		return true;
	}

	@Override
	public boolean canModify(String context)
	{
		return canConfigure(context) || canDelete(context) || canGrade(context);
	}

	@Override
	public boolean canUpload(String context)
	{
		return hasPermission(context, PERM_UPLOAD);
	}

	@Override
	public boolean canUseRelativeUrls() {
		return false;
	}

	@Override
	public boolean canValidate(String context)
	{
		return hasPermission(context, PERM_VALIDATE);
	}

	@Override
	public boolean canViewResults(String context)
	{
		return hasPermission(context, PERM_VIEW_RESULTS);
	}

	@Override
	public String currentContext()
	{
		return "context1";
	}

	@Override
	public String currentLearnerId()
	{
		return "learner1";
	}

	@Override
	public Learner getLearner(String learnerId) throws LearnerNotDefinedException
	{
		return new Learner(currentLearnerId(), getLearnerName(currentLearnerId()), "learner1");
	}

	@Override
	public String getLearnerName(String learnerId)
	{
		return "Scott Scorm";
	}

	@Override
	public UserTimeService getUserTimeService()
	{
		return null;
	}

	protected boolean hasPermission(String context, String lock)
	{
		return true;
	}

	@Override
	public boolean isOwner()
	{
		return true;
	}

	protected boolean unlockCheck(String lock, String ref)
	{
		return true;
	}

	@Override
	public boolean canLaunchAttempt(ContentPackage contentPackage, long attemptNumber)
	{
		return true;
	}

	@Override
	public boolean canLaunch(ContentPackage contentPackage)
	{
		return canLaunchAttempt(contentPackage, -1);
	}
}
