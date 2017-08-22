package org.sakaiproject.assignment.impl;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by enietzel on 5/11/17.
 */
@Slf4j
public class AssignmentReferenceTest {

    @Test
    public void assignmentReferenceTest() {
        String reference = "/assignment/a/***CONTEXT_ID***/***ASSIGNMENT_ID***";
        AssignmentReferenceReckoner.AssignmentReference assignmentReference = AssignmentReferenceReckoner.reckoner().reference(reference).reckon();
        Assert.assertEquals("assignment", assignmentReference.getType());
        Assert.assertEquals("a", assignmentReference.getSubtype());
        Assert.assertEquals("***CONTEXT_ID***", assignmentReference.getContext());
        Assert.assertEquals("***ASSIGNMENT_ID***", assignmentReference.getId());
        Assert.assertEquals("", assignmentReference.getContainer());
    }

    @Test
    public void assignmentReferencePartsTest() {
        String reference = AssignmentReferenceReckoner.reckoner().context("***CONTEXT_ID***").id("***ASSIGNMENT_ID***").reckon().getReference();
        Assert.assertEquals("/assignment/a/***CONTEXT_ID***/***ASSIGNMENT_ID***", reference);
    }

    @Test
    public void submissionReferenceTest() {
        String reference = "/assignment/s/***CONTEXT_ID***/***SUBMISSION_ID***";
        AssignmentReferenceReckoner.AssignmentReference assignmentReference = AssignmentReferenceReckoner.reckoner().reference(reference).reckon();
        Assert.assertEquals("assignment", assignmentReference.getType());
        Assert.assertEquals("s", assignmentReference.getSubtype());
        Assert.assertEquals("***CONTEXT_ID***", assignmentReference.getContext());
        Assert.assertEquals("***SUBMISSION_ID***", assignmentReference.getId());
        Assert.assertEquals("", assignmentReference.getContainer());

        String reference2 = "/assignment/s/***CONTEXT_ID***/***ASSIGNMENT_ID***/***SUBMISSION_ID***";
        AssignmentReferenceReckoner.AssignmentReference assignmentReference2 = AssignmentReferenceReckoner.reckoner().reference(reference2).reckon();
        Assert.assertEquals("assignment", assignmentReference2.getType());
        Assert.assertEquals("s", assignmentReference2.getSubtype());
        Assert.assertEquals("***CONTEXT_ID***", assignmentReference2.getContext());
        Assert.assertEquals("***SUBMISSION_ID***", assignmentReference2.getId());
        Assert.assertEquals("***ASSIGNMENT_ID***", assignmentReference2.getContainer());
    }

    @Test
    public void submissionReferencePartsTest() {
        String reference = AssignmentReferenceReckoner.reckoner().context("***CONTEXT_ID***").id("***SUBMISSION_ID***").subtype("s").reckon().getReference();
        Assert.assertEquals("/assignment/s/***CONTEXT_ID***/***SUBMISSION_ID***", reference);

        String reference2 = AssignmentReferenceReckoner.reckoner().context("***CONTEXT_ID***").id("***SUBMISSION_ID***").subtype("s").container("***ASSIGNMENT_ID***").reckon().getReference();
        Assert.assertEquals("/assignment/s/***CONTEXT_ID***/***ASSIGNMENT_ID***/***SUBMISSION_ID***", reference2);
    }

    @Test
    public void assignmentTest() {
        final String context = UUID.randomUUID().toString();
        final String id = UUID.randomUUID().toString();
        Assignment assignment = new Assignment();
        assignment.setId(id);
        assignment.setContext(context);
        String reference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
        Assert.assertNotNull(reference);
        Assert.assertEquals("/assignment/a/" + context + "/" + id, reference);
    }

    @Test
    public void submissionTest() {
        final String context = UUID.randomUUID().toString();
        final String assignmentId = UUID.randomUUID().toString();
        final String submissionId = UUID.randomUUID().toString();
        Assignment assignment = new Assignment();
        assignment.setId(assignmentId);
        assignment.setContext(context);
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setId(submissionId);
        submission.setAssignment(assignment);
        String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
        Assert.assertNotNull(reference);
        Assert.assertEquals("/assignment/s/" + context + "/" + assignmentId + "/" + submissionId, reference);
    }
}
