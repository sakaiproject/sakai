/*
 * Copyright (c) 2003-2025 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 */
package org.sakaiproject.assignment.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.HardDeleteAware;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.assignment.api.taggable.AssignmentActivityProducer;

/**
 * Verifies that AssignmentServiceImpl participates in hard delete and performs expected actions.
 */
@RunWith(MockitoJUnitRunner.class)
public class AssignmentServiceImplHardDeleteTest {

    private static final String SITE_ID = "hard-delete-test";

    @Mock private ContentHostingService contentHostingService;
    @Mock private TaggingManager taggingManager;
    @Mock private AssignmentActivityProducer assignmentActivityProducer;

    @Spy private AssignmentServiceImpl service = new AssignmentServiceImpl();

    @Before
    public void setUp() {
        // Inject minimal dependencies used by hardDelete path
        service.setContentHostingService(contentHostingService);
        service.setTaggingManager(taggingManager);
        service.setAssignmentActivityProducer(assignmentActivityProducer);

        // Keep tagging no-op inside removeAssociatedTaggingItem
        when(taggingManager.isTaggable()).thenReturn(false);
    }

    @Test
    public void implementsHardDeleteAwareAndDeletesAssignmentsAndAttachments() throws Exception {
        // Given two assignments across deleted + active sets
        Assignment a1 = new Assignment();
        a1.setId("a1");
        a1.setContext(SITE_ID);

        Assignment a2 = new Assignment();
        a2.setId("a2");
        a2.setContext(SITE_ID);

        List<Assignment> deleted = new ArrayList<>(Collections.singletonList(a1));
        List<Assignment> active = Collections.singletonList(a2);

        // Spy: control the retrieval methods and the delete side-effect
        doReturn(deleted).when(service).getDeletedAssignmentsForContext(SITE_ID);
        doReturn(active).when(service).getAssignmentsForContext(SITE_ID);
        doNothing().when(service).deleteAssignment(any(Assignment.class));

        // No attachments to remove
        doReturn(new ArrayList<>()).when(contentHostingService).getAllResources("/attachment/" + SITE_ID + "/Assignments/");

        // When
        service.hardDelete(SITE_ID);

        // Then
        assertTrue("Service must advertise HardDeleteAware", service instanceof HardDeleteAware);
        verify(service, times(1)).deleteAssignment(a1);
        verify(service, times(1)).deleteAssignment(a2);
        verify(contentHostingService, times(1)).getAllResources("/attachment/" + SITE_ID + "/Assignments/");
    }
}
