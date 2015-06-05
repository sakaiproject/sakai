package org.sakaiproject.assignment.api;

import java.util.Collection;
import java.util.List;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;
import org.sakaiproject.assignment.api.model.PeerAssessmentItem;

public interface AssignmentPeerAssessmentService extends ScheduledInvocationCommand{

	public void schedulePeerReview(String assignmentId);
	
	public void removeScheduledPeerReview(String assignmentId);
	
	public List<PeerAssessmentItem> getPeerAssessmentItems(String assignmentId, String assessorUserId, Integer scaledFactor);
	
	public PeerAssessmentItem getPeerAssessmentItem(String submissionId, String assessorUserId);
	
	public void savePeerAssessmentItem(PeerAssessmentItem item);
	
	public List<PeerAssessmentItem> getPeerAssessmentItems(String submissionId, Integer scaledFactor);
	
	public List<PeerAssessmentItem> getPeerAssessmentItemsByAssignmentId(String assignmentId, Integer scaledFactor);
	
	public List<PeerAssessmentItem> getPeerAssessmentItems(Collection<String> submissionsIds, Integer scaledFactor);
	
	/**
	 * returns true if the score was updated and saved
	 * @param submissionId
	 * @return
	 */
	public boolean updateScore(String submissionId);
}
