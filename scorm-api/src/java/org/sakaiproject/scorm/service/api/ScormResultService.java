/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.service.api;

import java.util.List;

import org.sakaiproject.scorm.model.api.ActivityReport;
import org.sakaiproject.scorm.model.api.ActivitySummary;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.Interaction;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.model.api.LearnerExperience;

public interface ScormResultService {

	public ActivityReport getActivityReport(long contentPackageId, String learnerId, long attemptNumber, String scoId);
	
	public List<ActivitySummary> getActivitySummaries(long contentPackageId, String learnerId, long attemptNumber);
	
	public Interaction getInteraction(long contentPackageId, String learnerId, long attemptNumber, String scoId, String interactionId);
	
	public String[] getSiblingIds(long contentPackageId, String learnerId, long attemptNumber, String scoId, String interactionId);
	
	public Attempt getAttempt(long id);
	
	public Attempt getAttempt(long contentPackageId, String learnerId, long attemptNumber);

	public List<Attempt> getAttempts(long contentPackageId);
	
	public List<Attempt> getAttempts(long contentPackageId, String learnerId);
		
	public List<Attempt> getAttempts(String courseId, String learnerId);
	
	public int getNumberOfAttempts(long contentPackageId, String learnerId);
	
	public List<Learner> getLearners(long contentPackageId);
	
	public List<LearnerExperience> getLearnerExperiences(long contentPackageId);
	
	public void saveAttempt(Attempt attempt);
	
}
