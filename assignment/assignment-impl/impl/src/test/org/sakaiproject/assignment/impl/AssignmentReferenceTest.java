package org.sakaiproject.assignment.impl;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;

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
}
