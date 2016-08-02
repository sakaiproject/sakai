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
	 * @param updateCurrentGrade true if the score should be sent to the gradebook
	 * @param publishedAssessment the assessment this attempt/submission is for
	 * @param persistenceHelper for retrying in case of database deadlock
	 * @param updateGrades true if integrating with gradebook
	 * @param eventService for updating Samigo event logs
	 * @param eventLogFacade for updating Samigo event logs
	 * @param toGradebookPublishedAssessmentSiteIdMap map of assessments that send grades to gradebook
	 * @param gbsHelper for updating grade in gradebook
	 * @param g for updating grading in gradebook
	 * @return true if the updates attempt/submission were processed, false if an error occurred
	 */
	public boolean autoSubmitSingleAssessment(AssessmentGradingData adata, boolean autoSubmit, boolean updateCurrentGrade, PublishedAssessmentFacade publishedAssessment,
			PersistenceHelper persistenceHelper, boolean updateGrades, EventLogService eventService, EventLogFacade eventLogFacade,
			Map toGradebookPublishedAssessmentSiteIdMap, GradebookServiceHelper gbsHelper, GradebookExternalAssessmentService g);
}
