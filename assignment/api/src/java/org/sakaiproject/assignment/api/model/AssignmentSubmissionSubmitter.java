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

import javax.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "ASN_SUBMISSION_SUBMITTER")
@Data
@NoArgsConstructor
@ToString(exclude = {"submission"})
@EqualsAndHashCode(of = {"submission", "submitter"})
public class AssignmentSubmissionSubmitter {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "assignment_submission_submitters_sequence")
    @SequenceGenerator(name = "assignment_submission_submitters_sequence", sequenceName = "ASN_SUBMISSION_SUBMITTERS_S")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "SUBMISSION_ID", nullable = false)
    private AssignmentSubmission submission;

    @Column(name = "SUBMITTER", nullable = false)
    private String submitter;

    @Column(name = "SUBMITTEE", nullable = false)
    private Boolean submittee = Boolean.FALSE;

    @Column(name = "GRADE")
    private String grade;

    @Lob
    @Column(name = "FEEDBACK")
    private String feedback;
}
