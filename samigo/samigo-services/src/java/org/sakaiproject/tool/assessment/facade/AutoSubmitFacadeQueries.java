package org.sakaiproject.tool.assessment.facade;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.util.ExtendedTimeDeliveryService;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

/**
 *
 * @author plukasew
 */
@Slf4j
public class AutoSubmitFacadeQueries extends HibernateDaoSupport implements AutoSubmitFacadeQueriesAPI
{
	@Override
	public boolean processAttempt(AssessmentGradingData adata, boolean updateGrades, AssessmentGradingFacadeQueriesAPI agfq, PublishedAssessmentFacade assessment,
			Date currentTime, String lastAgentId, Long lastPublishedAssessmentId, Map<Long, Set<PublishedSectionData>> sectionSetMap)
	{
		boolean autoSubmitCurrent = false;
		adata.setHasAutoSubmissionRun(Boolean.TRUE);

		Date endDate = new Date();
		if (Boolean.FALSE.equals(adata.getForGrade())) {

			// SAM-1088 getting the assessment so we can check to see if last user attempt was after due date
			Date dueDate = assessment.getAssessmentAccessControl().getDueDate();
			Date retractDate = assessment.getAssessmentAccessControl().getRetractDate();
			Integer lateHandling = assessment.getAssessmentAccessControl().getLateHandling();
			boolean acceptLate = AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.toString().equals(lateHandling);
			ExtendedTimeDeliveryService assessmentExtended = new ExtendedTimeDeliveryService(assessment,
					adata.getAgentId());

			//If it has extended time, just continue for now, no method to tell if the time is passed
			if (assessmentExtended.hasExtendedTime()) {
				//Continue on and try to submit it but it may be late, just change the due date
				dueDate = assessmentExtended.getDueDate() != null ? assessmentExtended.getDueDate() : dueDate;

				// If the extended time student received a retract date
				if (assessmentExtended.getRetractDate() != null) {
					retractDate =  assessmentExtended.getRetractDate();
					acceptLate = true;
				}
			}

			// If the due date or retract date hasn't passed yet, go on to the next one, don't consider it yet
			if (acceptLate && retractDate != null && (currentTime.before(retractDate) || adata.getAttemptDate().after(retractDate))) {
				return true;
			}
			else if ( (!acceptLate || retractDate == null) && dueDate != null && currentTime.before(dueDate)) {
				return true;
			}

			adata.setForGrade(Boolean.TRUE);
			if (adata.getTotalAutoScore() == null) {
				adata.setTotalAutoScore(0d);
			}
			if (adata.getFinalScore() == null) {
				adata.setFinalScore(0d);
			}

			if (adata.getAttemptDate() != null && dueDate != null &&
					adata.getAttemptDate().after(dueDate)) {
				adata.setIsLate(true);
			}
			// SAM-1088
			else if (adata.getSubmittedDate() != null && dueDate != null &&
					adata.getSubmittedDate().after(dueDate)) {
				adata.setIsLate(true);
			}
			// SAM-2729 user probably opened assessment and then never submitted a question
			if (adata.getSubmittedDate() == null && adata.getAttemptDate() != null) {
				adata.setSubmittedDate(endDate);
			}

			autoSubmitCurrent = true;
			adata.setIsAutoSubmitted(Boolean.TRUE);
			if (lastPublishedAssessmentId.equals(adata.getPublishedAssessmentId())
					&& lastAgentId.equals(adata.getAgentId())) {
				adata.setStatus(AssessmentGradingData.AUTOSUBMIT_UPDATED);
			} else {
				adata.setStatus(AssessmentGradingData.SUBMITTED);
			}

			agfq.completeItemGradingData(adata, sectionSetMap);
		}

		boolean success = agfq.saveOrUpdateAssessmentGrading(adata);
		if (!success) {
			log.error("Unable to persist assessement grading data for id {}", adata.getAssessmentGradingId());
			return false;
		}

		if (autoSubmitCurrent) {
			GradingService gs = new GradingService();
			if (updateGrades) {
				gs.notifyGradebookByScoringType(adata, assessment); // this may throw runtime exceptions triggering a rollback
			}

			// if we get this far, the processing of this attempt was successful so it is now safe to
			// update the log and email the student (triggered by the same method)
			gs.updateAutosubmitEventLog(adata);
		}

		return true;
	}
}
