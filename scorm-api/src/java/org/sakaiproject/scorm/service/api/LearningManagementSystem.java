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
package org.sakaiproject.scorm.service.api;

import org.sakaiproject.scorm.exceptions.LearnerNotDefinedException;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.time.api.UserTimeService;

public interface LearningManagementSystem
{
	public boolean canConfigure(String context);

	public boolean canDelete(String context);

	public boolean canGrade(String context);

	public boolean canLaunch(ContentPackage contentPackage);
	
	public boolean canLaunchAttempt(ContentPackage contentPackage, long attemptNumber);

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

	public UserTimeService getUserTimeService();
}
