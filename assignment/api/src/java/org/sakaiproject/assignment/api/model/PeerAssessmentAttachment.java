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

