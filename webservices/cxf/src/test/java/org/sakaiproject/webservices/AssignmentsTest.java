/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.webservices;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class AssignmentsTest {

    private static final String SESSION_ID = "session-1";
    private static final String ASSIGNMENT_ID = "assignment-1";
    private static final String ASSIGNMENT_REFERENCE = "/assignment/a/site-1/assignment-1";

    private final Assignments assignments = new Assignments();
    private final AssignmentService assignmentService = mock(AssignmentService.class);
    private final Assignment assignment = new Assignment();

    @Before
    public void setUp() throws Exception {
        SessionManager sessionManager = mock(SessionManager.class);
        Session session = mock(Session.class);
        when(sessionManager.getSession(SESSION_ID)).thenReturn(session);
        when(session.getUserId()).thenReturn("user-1");
        assignments.setSessionManager(sessionManager);
        assignments.setAssignmentService(assignmentService);

        assignment.setId(ASSIGNMENT_ID);
        assignment.setContext("site-1");
        when(assignmentService.getAssignment(ASSIGNMENT_ID)).thenReturn(assignment);
    }

    @Test
    public void skipsSubmissionLookupWhenGradingPermissionIsDenied() {
        assertEquals("<submissions />", assignments.getSubmissionsForAssignment(SESSION_ID, ASSIGNMENT_ID));

        verify(assignmentService).allowGradeSubmission(ASSIGNMENT_REFERENCE);
        verify(assignmentService, never()).getSubmissions(any(Assignment.class));
    }

    @Test
    public void returnsSubmissionXmlWhenGradingPermissionIsGranted() throws Exception {
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setId("submission-1");
        submission.setGrade("90");
        submission.setSubmittedText("Submitted work");
        submission.setFeedbackComment("Feedback comment");
        submission.setFeedbackText("Inline feedback");
        when(assignmentService.allowGradeSubmission(ASSIGNMENT_REFERENCE)).thenReturn(true);
        when(assignmentService.getSubmissions(assignment)).thenReturn(Set.of(submission));
        when(assignmentService.getSubmissionStatus(submission.getId(), true)).thenReturn("Graded");

        String result = assignments.getSubmissionsForAssignment(SESSION_ID, ASSIGNMENT_ID);
        NodeList submissions = Xml.createSecureDocumentBuilderFactory().newDocumentBuilder()
                .parse(new InputSource(new StringReader(result))).getElementsByTagName("submission");

        assertEquals(1, submissions.getLength());
        Element returnedSubmission = (Element) submissions.item(0);
        assertEquals("90", returnedSubmission.getAttribute("grade"));
        assertEquals("Submitted work", returnedSubmission.getAttribute("submitted-text"));
        assertEquals("Feedback comment", returnedSubmission.getAttribute("feedback-comment"));
        assertEquals("Inline feedback", returnedSubmission.getAttribute("feedback-text"));
        assertEquals("Graded", returnedSubmission.getAttribute("status"));
    }
}
