package org.sakaiproject.assignment.api.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Composite primary key
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessorSubmissionId implements Serializable {
    @Column(name = "SUBMISSION_ID", nullable = false)
    private String submissionId;

    @Column(name = "ASSESSOR_USER_ID", nullable = false)
    private String assessorUserId;
}
