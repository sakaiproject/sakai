/**
 * Copyright (c) 2005-2016 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.facade.util.autosubmit;

import java.util.Map;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.facade.EventLogFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.PersistenceHelper;
import org.sakaiproject.tool.assessment.services.assessment.EventLogService;

/**
 * Queries for persisting a single attempt/submission and all related updates in a single transaction
 * @author plukasew
 */
public interface AutoSubmitQueriesAPI
{
	/**
	 * Persist updates to a single assessment attempt/submission to the database. This includes updating the autosubmit flags,
	 * updating the Samigo event log, and firing an event to trigger the email notification system.
	 * @param adata The data for this attempt/submission
	 * @param autoSubmit whether to autosubmit the attempt, or just update the flag indicating this submission has been processed
	 * @param publishedAssessment the assessment this attempt/submission is for
	 * @param persistenceHelper for retrying in case of database deadlock
	 * @param eventService for updating Samigo event logs
	 * @param eventLogFacade for updating Samigo event logs
	 * @param gbsHelper for updating grade in gradebook
	 * @param g for updating grading in gradebook
	 * @return true if the updates attempt/submission were processed, false if an error occurred
	 */
	public boolean autoSubmitSingleAssessment(AssessmentGradingData adata, boolean autoSubmit, PublishedAssessmentFacade publishedAssessment,
			PersistenceHelper persistenceHelper, EventLogService eventService, EventLogFacade eventLogFacade);
	/**
	  Updates a single grade item to the gradebook
	  This method should be called after autoSubmitSingleAssessment so the assessment with the grade is added
	  
	* @param adata The data for this attempt/submission
	* @param assessmentGrade actual grade to publish to gradebook
 	* @param toGradebookPublishedAssessmentSiteIdMap map of assessments that send grades to gradebook
	* @param persistenceHelper for retrying in case of database deadlock
	* @param gbsHelper for updating grade in gradebook
	* @param g for updating grading in gradebook
	* @return true if the grade was added successfully, false if an error occurred
	*/
	boolean autoSubmitUpdateSingleGrade(AssessmentGradingData adata, Double assessmentGrade, Map<Long, String> toGradebookPublishedAssessmentSiteIdMap, 
			PersistenceHelper persistenceHelper, GradebookServiceHelper gbsHelper, GradebookExternalAssessmentService g);
}
