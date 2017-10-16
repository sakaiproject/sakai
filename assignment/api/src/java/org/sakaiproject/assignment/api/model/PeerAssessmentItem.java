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
package org.sakaiproject.assignment.api.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.entity.api.Reference;

@Entity
@Table(name = "ASN_PEER_ASSESSMENT_ITEM_T",
       indexes = {@Index(name = "PEER_ASSESSOR2_I", columnList = "ASSIGNMENT_ID, ASSESSOR_USER_ID")})
@NamedQueries({
        @NamedQuery(name = "findPeerAssessmentItemsBySubmissions",
                    query = "from PeerAssessmentItem p where p.id.submissionId in (:submissionIds) order by p.assignmentId, p.id.submissionId, p.id.assessorUserId"),
        @NamedQuery(name = "findPeerAssessmentItemsByUserAndAssignment",
                    query = "from PeerAssessmentItem p where p.id.assessorUserId = :assessorUserId and p.assignmentId = :assignmentId order by p.assignmentId, p.id.submissionId, p.id.assessorUserId"),
        @NamedQuery(name = "findPeerAssessmentItemsByUserAndSubmission",
                    query = "from PeerAssessmentItem p where p.id.assessorUserId = :assessorUserId and p.id.submissionId = :submissionId order by p.assignmentId, p.id.submissionId, p.id.assessorUserId"),
        @NamedQuery(name = "findPeerAssessmentItemsBySubmissionId",
                    query = "from PeerAssessmentItem p where p.id.submissionId = :submissionId	order by p.assignmentId, p.id.submissionId, p.id.assessorUserId"),
        @NamedQuery(name = "findPeerAssessmentItemsByAssignmentId",
                    query = "from PeerAssessmentItem p where p.assignmentId = :assignmentId order by p.assignmentId, p.id.submissionId, p.id.assessorUserId")})
@Data
@NoArgsConstructor
public class PeerAssessmentItem implements Serializable {

    private static final long serialVersionUID = -8376570648172966170L;

    @EmbeddedId
    private AssessorSubmissionId id;

    @Column(name = "ASSIGNMENT_ID", nullable = false)
    private String assignmentId;

    @Column(name = "SCORE")
    private Integer score;

    @Lob
    @Column(name = "REVIEW_COMMENT")
    private String comment;

    @Column(name = "REMOVED", nullable = false)
    private Boolean removed = Boolean.FALSE;

    //submitted is only a flag to help with the UI show/hide reviews
    //that the user still needs to complete (more of a hide flag than a submit)
    @Column(name = "SUBMITTED", nullable = false)
    private Boolean submitted = Boolean.FALSE;

    //resource ids for attachments associated with this item stored in separate table
    @Transient
    private List<PeerAssessmentAttachment> attachmentList;

    @Transient
    private List<Reference> attachmentRefList;

    @Transient
    private String assessorDisplayName;

    @Transient
    private Integer scaledFactor = AssignmentConstants.DEFAULT_SCALED_FACTOR;

    /**
     * Score is stored as a integer value in the DB, but is really a decimal value (divide by "factor")
     *
     * @return
     */
    @Transient
    public String getScoreDisplay() {
        return getScore() == null ? "" : "" + score / (double) getScaledFactor();
    }

    @Transient
    public boolean isDraft() {
        return !submitted && (getScore() != null || (getComment() != null && !"".equals(getComment().trim())));
    }
}
