package org.sakaiproject.assignment.api.model;

import java.io.Serializable;
import java.util.Objects;

public class PeerAssessmentAttachment implements Serializable{

    private Long id;
    private String submissionId;
    private String assessorUserId;
    private String resourceId;

    public PeerAssessmentAttachment(){}

    public PeerAssessmentAttachment(String s, String a, String r) {
        submissionId = s;
        assessorUserId = a;
        resourceId = r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getAssessorUserId() {
        return assessorUserId;
    }

    public void setAssessorUserId(String assessorUserId) {
        this.assessorUserId = assessorUserId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeerAssessmentAttachment that = (PeerAssessmentAttachment) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(submissionId, that.submissionId) &&
                Objects.equals(assessorUserId, that.assessorUserId) &&
                Objects.equals(resourceId, that.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, submissionId, assessorUserId, resourceId);
    }
}
