package org.sakaiproject.assignment.impl;

import static org.sakaiproject.assignment.api.AssignmentServiceConstants.REFERENCE_ROOT;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by enietzel on 5/11/17.
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AssignmentTestConfiguration.class})
public class AssignmentReferenceTest {

    @Autowired SecurityService securityService;
    @Autowired SessionManager sessionManager;
    @Autowired private AssignmentService assignmentService;
    @Autowired private EntityManager entityManager;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private AssignmentReferenceUtil assignmentReferenceUtil;

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
}
