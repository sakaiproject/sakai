/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.EventLogFacade;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.AutoSubmitAssessmentsJob;
import org.sakaiproject.tool.assessment.services.PersistenceHelper;
import org.sakaiproject.tool.assessment.services.assessment.EventLogService;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import lombok.extern.slf4j.Slf4j;

/**
 * Queries for persisting a single attempt/submission and all related updates in a single transaction
 * @author plukasew
 */
@Slf4j
public class AutoSubmitQueries extends HibernateDaoSupport implements AutoSubmitQueriesAPI
{
	@Override
	public boolean autoSubmitSingleAssessment(AssessmentGradingData adata, boolean autoSubmit, boolean updateCurrentGrade, PublishedAssessmentFacade publishedAssessment, 
			PersistenceHelper persistenceHelper, boolean updateGrades, EventLogService eventService, EventLogFacade eventLogFacade,
			Map<Long, String> toGradebookPublishedAssessmentSiteIdMap, GradebookServiceHelper gbsHelper, GradebookExternalAssessmentService g)
	{
		long gradingId;
		if (adata != null && adata.getAssessmentGradingId() != null)
		{
			gradingId = adata.getAssessmentGradingId();
		}
		else
		{
			log.error("AssessmentGradingData object/id cannot be null");
			return false;
		}
		
		try
		{	
			getHibernateTemplate().saveOrUpdate(adata);
			
			//update grades
			if(updateGrades && autoSubmit && updateCurrentGrade && toGradebookPublishedAssessmentSiteIdMap != null && toGradebookPublishedAssessmentSiteIdMap.containsKey(adata.getPublishedAssessmentId())) {
				String currentSiteId = toGradebookPublishedAssessmentSiteIdMap.get(adata.getPublishedAssessmentId());
				if (gbsHelper != null && gbsHelper.gradebookExists(GradebookFacade.getGradebookUId(currentSiteId), g)){
					int retryCount = persistenceHelper.getRetryCount();
					boolean success = false;
					while (retryCount > 0){
						try {
							Map<String, Double> studentScore = new HashMap<>();
							studentScore.put(adata.getAgentId(), adata.getFinalScore());
							gbsHelper.updateExternalAssessmentScores(adata.getPublishedAssessmentId(), studentScore, g);
							retryCount = 0;
							success = true;
						}
						catch (Exception e) {
							log.error("Error while updating external assessment score during auto submitting assessment grade data id: " + gradingId, e);
							retryCount = persistenceHelper.retryDeadlock(e, retryCount);
						}
					}
					
					if (!success)
					{
						return false;
					}
				}
			}
			
			if (autoSubmit) // if we get this far and have autosubmitted, log and notify
			{
				List eventLogDataList = eventService.getEventLogData(gradingId);
				if (!eventLogDataList.isEmpty()) {
					EventLogData eventLogData= (EventLogData) eventLogDataList.get(0);
					//will do the i18n issue later.
					eventLogData.setErrorMsg("No Errors (Auto submit)");
					Date endDate = new Date();
					eventLogData.setEndDate(endDate);
					if(eventLogData.getStartDate() != null) {
						double minute= 1000*60;
						int eclipseTime = (int)Math.ceil(((endDate.getTime() - eventLogData.getStartDate().getTime())/minute));
						eventLogData.setEclipseTime(eclipseTime); 
					} else {
						eventLogData.setEclipseTime(null); 
						eventLogData.setErrorMsg("Error during auto submit");
					}
					eventLogFacade.setData(eventLogData);
					eventService.saveOrUpdateEventLog(eventLogFacade);
				}

				EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_AUTO_SUBMIT_JOB,
						AutoSubmitAssessmentsJob.safeEventLength("publishedAssessmentId=" + adata.getPublishedAssessmentId() + 
								", assessmentGradingId=" + gradingId), true));

				Map<String, Object> notiValues = new HashMap<>();
				notiValues.put("publishedAssessmentID", adata.getPublishedAssessmentId());
				notiValues.put("assessmentGradingID", gradingId);
				notiValues.put("userID", adata.getAgentId());
				notiValues.put("submissionDate", adata.getSubmittedDate());

				String confirmationNumber = adata.getAssessmentGradingId() + "-" + publishedAssessment.getPublishedAssessmentId() + "-"
					+ adata.getAgentId() + "-" + adata.getSubmittedDate().toString();
				notiValues.put( "confirmationNumber", confirmationNumber );

				EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_AUTO, notiValues.toString(), AgentFacade.getCurrentSiteId(), false, SamigoConstants.NOTI_EVENT_ASSESSMENT_SUBMITTED));
			}
		}
		catch (Exception e)
		{
			log.error("Error while auto submitting assessment grade data id: " + gradingId, e);
			return false;
		}
		
		return true;

	}
}
