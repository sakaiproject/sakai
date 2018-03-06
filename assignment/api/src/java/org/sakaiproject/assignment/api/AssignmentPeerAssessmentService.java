/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.assignment.api;

import java.util.Collection;
import java.util.List;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;
import org.sakaiproject.assignment.api.model.PeerAssessmentAttachment;
import org.sakaiproject.assignment.api.model.PeerAssessmentItem;

public interface AssignmentPeerAssessmentService extends ScheduledInvocationCommand {

    public void schedulePeerReview(String assignmentId);

    public void removeScheduledPeerReview(String assignmentId);

    public List<PeerAssessmentItem> getPeerAssessmentItems(String assignmentId, String assessorUserId, Integer scaledFactor);

    public PeerAssessmentItem getPeerAssessmentItem(String submissionId, String assessorUserId);

    public void savePeerAssessmentItem(PeerAssessmentItem item);

    public List<PeerAssessmentItem> getPeerAssessmentItems(String submissionId, Integer scaledFactor);

    public List<PeerAssessmentItem> getPeerAssessmentItemsByAssignmentId(String assignmentId, Integer scaledFactor);

    public List<PeerAssessmentItem> getPeerAssessmentItems(Collection<String> submissionsIds, Integer scaledFactor);

    public boolean updateScore(String submissionId, String assessorId);

    public List<PeerAssessmentAttachment> getPeerAssessmentAttachments(String submissionId, String assessorUserId);

    public PeerAssessmentAttachment getPeerAssessmentAttachment(String submissionId, String assessorUserId, String resourceId);

    public void savePeerAssessmentAttachments(PeerAssessmentItem item);

    public void removePeerAttachment(PeerAssessmentAttachment peerAssessmentAttachment);
}