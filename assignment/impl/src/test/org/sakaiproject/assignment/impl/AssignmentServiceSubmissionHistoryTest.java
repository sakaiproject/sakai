/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.assignment.impl;

import javax.annotation.Resource;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AssignmentTestConfiguration.class)
public class AssignmentServiceSubmissionHistoryTest {

    private static final String HISTORY = ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT;
    private static final Instant SUBMITTED = Instant.parse("2026-09-01T12:00:00Z");

    @Autowired private AssignmentService assignmentService;
    @Autowired private EntityManager entityManager;
    @Autowired private FormattedText formattedText;
    @Resource(name = "org.sakaiproject.time.api.UserTimeService")
    private UserTimeService userTimeService;

    @Before
    public void setUp() {
        when(formattedText.escapeHtml(anyString())).thenAnswer(invocation ->
                StringEscapeUtils.escapeHtml4(invocation.getArgument(0)));
        when(formattedText.escapeHtml(anyString(), eq(false))).thenAnswer(invocation ->
                StringEscapeUtils.escapeHtml4(invocation.getArgument(0)));
        when(userTimeService.dateTimeFormat(SUBMITTED, FormatStyle.LONG, FormatStyle.LONG)).thenReturn("Original submission time");
    }

    @Test
    public void archivesFeedbackAndEscapedAttachmentsBeforeExistingHistory() {
        AssignmentSubmission submission = submitted();
        submission.setFeedbackText("<p>Feedback</p>");
        submission.getProperties().put(HISTORY, "older history");
        addAttachment(submission, "original", "A & <B>", "/access/original?a=1&b=2", false);
        addAttachment(submission, "inline", "Inline", "/access/inline", true);
        Reference missing = mock(Reference.class);
        when(entityManager.newReference("missing")).thenReturn(missing);
        submission.getAttachments().add("missing");

        assignmentService.archiveSubmissionHistory(submission);

        Assert.assertEquals("<h4>Original submission time</h4><div style=\"margin:0;padding:0\"><p>Feedback</p>"
                + "<ul><li><a href=\"/access/original?a=1&amp;b=2\">A &amp; &lt;B&gt;</a></li></ul></div>older history",
                submission.getProperties().get(HISTORY));
        Assert.assertEquals(SUBMITTED, submission.getDateSubmitted());
        Assert.assertTrue(submission.getSubmitted());
        Assert.assertEquals(3, submission.getAttachments().size());
        Assert.assertEquals("<p>Feedback</p>", submission.getFeedbackText());
    }

    @Test
    public void firstDraftArchivesOriginalFilesAndLaterSubmitDoesNotArchiveDraftFiles() {
        AssignmentSubmission submission = submitted();
        addAttachment(submission, "original", "Original", "/access/original", false);
        assignmentService.archiveSubmissionHistory(submission);
        String history = submission.getProperties().get(HISTORY);
        Assert.assertTrue(history.contains("/access/original"));
        Assert.assertFalse(history.contains("null</div>"));

        // The caller now saves the replacement as a draft, then submits that draft later.
        submission.setSubmitted(false);
        submission.setFeedbackText(null);
        submission.getAttachments().clear();
        addAttachment(submission, "replacement", "Replacement", "/access/replacement", false);
        assignmentService.archiveSubmissionHistory(submission);
        Assert.assertEquals(history, submission.getProperties().get(HISTORY));
    }

    @Test
    public void emptyAndInitialDraftSubmissionsDoNotCreateHistory() {
        AssignmentSubmission submission = new AssignmentSubmission();
        addAttachment(submission, "draft", "Draft", "/access/draft", false);
        assignmentService.archiveSubmissionHistory(submission);
        Assert.assertFalse(submission.getProperties().containsKey(HISTORY));
        submission.setSubmitted(true);
        submission.getAttachments().clear();
        assignmentService.archiveSubmissionHistory(submission);
        Assert.assertFalse(submission.getProperties().containsKey(HISTORY));
    }

    @Test
    public void feedbackOnlyHistoryPreservesGradingAndModificationDateFallbacks() {
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setFeedbackText("Feedback");
        submission.getProperties().put(AssignmentConstants.PROP_LAST_GRADED_DATE, "Graded date");
        assignmentService.archiveSubmissionHistory(submission);
        Assert.assertEquals("<h4>Graded date</h4><div style=\"margin:0;padding:0\">Feedback</div>",
                submission.getProperties().get(HISTORY));

        submission.getProperties().clear();
        submission.setDateModified(SUBMITTED);
        assignmentService.archiveSubmissionHistory(submission);
        String expectedDate = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
                .withZone(ZoneId.systemDefault()).format(SUBMITTED);
        Assert.assertEquals("<h4>" + expectedDate + "</h4><div style=\"margin:0;padding:0\">Feedback</div>",
                submission.getProperties().get(HISTORY));
    }

    private AssignmentSubmission submitted() {
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setSubmitted(true);
        submission.setDateSubmitted(SUBMITTED);
        submission.getProperties().put(AssignmentConstants.PROP_LAST_GRADED_DATE, "Later grading time");
        return submission;
    }

    private void addAttachment(AssignmentSubmission submission, String id, String name, String url, boolean inline) {
        Reference reference = mock(Reference.class);
        ResourceProperties properties = mock(ResourceProperties.class);
        when(entityManager.newReference(id)).thenReturn(reference);
        when(reference.getProperties()).thenReturn(properties);
        when(reference.getUrl()).thenReturn(url);
        when(properties.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME)).thenReturn(name);
        when(properties.getProperty(AssignmentConstants.PROP_INLINE_SUBMISSION)).thenReturn(Boolean.toString(inline));
        submission.getAttachments().add(id);
    }
}
