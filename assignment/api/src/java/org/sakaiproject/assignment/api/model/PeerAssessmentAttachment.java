package org.sakaiproject.assignment.api.model;

import java.io.Serializable;
import javax.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ASN_PEER_ASSESSMENT_ATTACH_T",
       indexes = {@Index(name = "PEER_ASSESSOR_I", columnList = "SUBMISSION_ID, ASSESSOR_USER_ID")})
@NamedQuery(name = "findPeerAssessmentAttachmentsByUserAndSubmission",
            query = "from PeerAssessmentAttachment p where p.assessorUserId = :assessorUserId and p.submissionId = :submissionId order by p.resourceId, p.submissionId, p.assessorUserId")
@Data
@NoArgsConstructor
public class PeerAssessmentAttachment implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "peer_assessment_attachment_sequence")
    @SequenceGenerator(name = "peer_assessment_attachment_sequence", sequenceName = "ASN_PEER_ATTACH_S")
    private Long id;

    @Column(name = "SUBMISSION_ID", nullable = false)
    private String submissionId;

    @Column(name = "ASSESSOR_USER_ID", nullable = false)
    private String assessorUserId;

    @Column(name = "RESOURCE_ID", nullable = false)
    private String resourceId;

    public PeerAssessmentAttachment(String s, String a, String r) {
        submissionId = s;
        assessorUserId = a;
        resourceId = r;
    }
}

