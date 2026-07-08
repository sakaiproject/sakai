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
package org.sakaiproject.assignment.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.sakaiproject.assignment.api.AssignmentConstants.GRADE_SUBMISSION_GRADE;
import static org.sakaiproject.assignment.api.AssignmentConstants.IGNORE_LATE_PENALTY;
import static org.sakaiproject.assignment.api.AssignmentConstants.LATE_PENALTY_TYPE;
import static org.sakaiproject.assignment.api.AssignmentConstants.LATE_PENALTY_VALUE;
import static org.sakaiproject.assignment.api.AssignmentConstants.SUBMISSION_OPTION_SAVE;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.ResourceLoader;

/**
 * Verifies that {@code gradeSubmission} keeps the stored submission grade raw and
 * round-trips the per-submission ignore-late-penalty flag through its options. The
 * late-penalty computation itself (and that the effective grade is what integrates to
 * the gradebook) is exercised against the real service and calculator in the impl
 * module's {@code AssignmentServiceTest}, so it is not re-asserted here through a mock.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class AssignmentToolUtilsLatePenaltyTest {

    private static final String SITE_ID = "site1";
    private static final String STUDENT = "student1";

    @Mock private AssignmentService assignmentService;
    @Mock private GradingService gradingService;
    @Mock private FormattedText formattedText;
    @Mock private ResourceLoader resourceLoader;
    @Mock private UserDirectoryService userDirectoryService;

    private AssignmentToolUtils assignmentToolUtils;
    private Assignment assignment;
    private AssignmentSubmission submission;

    @Before
    public void setUp() {

        assignmentToolUtils = new AssignmentToolUtils(resourceLoader);
        assignmentToolUtils.setAssignmentService(assignmentService);
        assignmentToolUtils.setGradingService(gradingService);
        assignmentToolUtils.setFormattedText(formattedText);
        assignmentToolUtils.setUserDirectoryService(userDirectoryService);

        Instant due = Instant.now().minus(Duration.ofDays(3));

        assignment = new Assignment();
        assignment.setId("a1");
        assignment.setContext(SITE_ID);
        assignment.setTypeOfGrade(Assignment.GradeType.SCORE_GRADE_TYPE);
        assignment.setScaleFactor(100);
        assignment.setDueDate(due);
        assignment.setIsGroup(false);
        assignment.getProperties().put(LATE_PENALTY_TYPE, "FLAT");
        assignment.getProperties().put(LATE_PENALTY_VALUE, "5");

        submission = new AssignmentSubmission();
        submission.setId("s1");
        submission.setAssignment(assignment);
        submission.setGrade("9000");
        submission.setDateSubmitted(due.plus(Duration.ofHours(30)));
        submission.setGradeReleased(true);

        AssignmentSubmissionSubmitter submitter = new AssignmentSubmissionSubmitter();
        submitter.setSubmitter(STUDENT);
        submitter.setSubmission(submission);
        Set<AssignmentSubmissionSubmitter> submitters = new HashSet<>();
        submitters.add(submitter);
        submission.setSubmitters(submitters);
    }

    @Test
    public void gradeSubmissionRoundTripsIgnoreLatePenaltyProperty() throws Exception {

        User instructor = org.mockito.Mockito.mock(User.class);
        when(userDirectoryService.getCurrentUser()).thenReturn(instructor);
        when(gradingService.isGradebookGroupEnabled(anyString())).thenReturn(false);

        Map<String, Object> options = new HashMap<>();
        options.put("siteId", SITE_ID);
        options.put(GRADE_SUBMISSION_GRADE, "9000");
        options.put(IGNORE_LATE_PENALTY, "true");
        assignmentToolUtils.gradeSubmission(submission, SUBMISSION_OPTION_SAVE, options, new ArrayList<>());

        assertTrue(Boolean.parseBoolean(submission.getProperties().get(IGNORE_LATE_PENALTY)));
        // the stored grade is always the raw instructor grade
        assertEquals("9000", submission.getGrade());

        // saving again without the flag clears it
        options.remove(IGNORE_LATE_PENALTY);
        assignmentToolUtils.gradeSubmission(submission, SUBMISSION_OPTION_SAVE, options, new ArrayList<>());

        assertFalse(submission.getProperties().containsKey(IGNORE_LATE_PENALTY));
        assertEquals("9000", submission.getGrade());
    }
}
