/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * A removed assignment is only soft-deleted (SAK-33477): the row still loads, so
 * {@code objectExists()} must inspect the deleted flag itself - otherwise Lessons
 * tags the item "Not Published" instead of "Deleted".
 */
public class AssignmentEntityTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock private AssignmentService assignmentService;
    @Mock private Assignment assignment;

    private static final String ASSIGNMENT_ID = "assignment-1";

    private AssignmentEntity entity;

    @Before
    public void setUp() throws Exception {
        AssignmentEntity.setAssignmentService(assignmentService);
        when(assignmentService.getAssignment(ASSIGNMENT_ID)).thenReturn(assignment);
        entity = new AssignmentEntity(LessonEntity.TYPE_ASSIGNMENT, ASSIGNMENT_ID, 1);
    }

    @After
    public void tearDown() {
        AssignmentEntity.setAssignmentService(null);
    }

    @Test
    public void objectExists_isFalse_whenSoftDeleted() {
        when(assignment.getDeleted()).thenReturn(Boolean.TRUE);

        assertFalse(entity.objectExists());   // -> Lessons shows "Deleted", not "Not Published"
    }

    @Test
    public void objectExists_isTrue_whenLive() {
        when(assignment.getDeleted()).thenReturn(Boolean.FALSE);

        assertTrue(entity.objectExists());
    }

    @Test
    public void objectExists_isTrue_whenDeletedFlagNull() {
        when(assignment.getDeleted()).thenReturn(null);   // DELETED column is nullable

        assertTrue(entity.objectExists());
    }

    @Test
    public void notPublished_stillTrue_whenSoftDeleted() {
        when(assignment.getDeleted()).thenReturn(Boolean.TRUE);

        assertTrue(entity.notPublished());    // students still can't see it
    }
}
