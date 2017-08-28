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
@EqualsAndHashCode(of = "id")
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
