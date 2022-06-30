package org.sakaiproject.tool.assessment.facade;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
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

		// If the assessment is deleted, or the submission is not forGrade just set hasAutoSubmissionRun = true; do not update gradebook
		if (Boolean.FALSE.equals(adata.getForGrade()) && assessment.getStatus() != AssessmentBaseIfc.DEAD_STATUS) {

			// SAM-1088 check to see if last user attempt was after due date
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

			// If it's an "empty" submission don't autosubmit; change status and save (status = 5, hasAutoSubmitRun = true)
			// We determine "empty" if it has an attempt date but submitted date is null
			// Attempt date is populated as soon as student clicks "Begin"; submit date is populated as soon as student makes any progress (next, save, submit)
			// So if there is an attempt date but no submit date, we can safely assume this is a student who began a quiz and did nothing (either walked away, or logged out immediately)
			if (adata.getAttemptDate() != null && adata.getSubmittedDate() == null) {
				adata.setStatus(AssessmentGradingData.NO_SUBMISSION);
			}
			else {
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
