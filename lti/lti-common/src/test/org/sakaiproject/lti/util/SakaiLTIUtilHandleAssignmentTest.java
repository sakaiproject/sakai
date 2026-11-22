/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.lti.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.api.FormattedText;
import org.tsugi.ags2.objects.Score;

public class SakaiLTIUtilHandleAssignmentTest {

	private static final String SITE_ID = "site1";
	private static final String GROUP_ID = "group1";
	private static final String GROUP_REF = "/site/" + SITE_ID + "/group/" + GROUP_ID;
	private static final String ASSIGNMENT_ID = "assignment1";
	private static final String ASSIGNMENT_REF = "/assignment/a/" + SITE_ID + "/" + ASSIGNMENT_ID;

	private MockedStatic<ComponentManager> componentManager;
	private MockedStatic<UserDirectoryService> userDirectoryService;
	private MockedStatic<SessionManager> sessionManager;
	private MockedStatic<SecurityService> securityService;
	private MockedStatic<ServerConfigurationService> serverConfigurationService;

	private AssignmentService assignmentService;
	private GradingService gradingService;
	private SiteService siteService;

	@Before
	public void setUp() {
		assignmentService = mock(AssignmentService.class);
		gradingService = mock(GradingService.class);
		siteService = mock(SiteService.class);
		PreferencesService preferencesService = mock(PreferencesService.class);
		UserTimeService userTimeService = mock(UserTimeService.class);
		FormattedText formattedText = mock(FormattedText.class);

		when(preferencesService.getLocale(anyString())).thenReturn(Locale.US);
		when(userTimeService.getLocalTimeZone(anyString())).thenReturn(TimeZone.getTimeZone("UTC"));
		when(formattedText.convertFormattedTextToPlaintext(any())).thenAnswer(i -> i.getArgument(0));

		componentManager = Mockito.mockStatic(ComponentManager.class);
		componentManager.when(() -> ComponentManager.get(AssignmentService.class)).thenReturn(assignmentService);
		componentManager.when(() -> ComponentManager.get(GradingService.class)).thenReturn(gradingService);
		componentManager.when(() -> ComponentManager.get(SiteService.class)).thenReturn(siteService);
		componentManager.when(() -> ComponentManager.get(PreferencesService.class)).thenReturn(preferencesService);
		componentManager.when(() -> ComponentManager.get(UserTimeService.class)).thenReturn(userTimeService);
		componentManager.when(() -> ComponentManager.get(FormattedText.class)).thenReturn(formattedText);

		userDirectoryService = Mockito.mockStatic(UserDirectoryService.class);
		userDirectoryService.when(() -> UserDirectoryService.getUser(anyString())).thenAnswer(i -> {
			User user = mock(User.class);
			when(user.getId()).thenReturn(i.getArgument(0));
			return user;
		});

		Session session = mock(Session.class);
		sessionManager = Mockito.mockStatic(SessionManager.class);
		sessionManager.when(SessionManager::getCurrentSession).thenReturn(session);

		securityService = Mockito.mockStatic(SecurityService.class);
		serverConfigurationService = Mockito.mockStatic(ServerConfigurationService.class);
	}

	@After
	public void tearDown() {
		componentManager.close();
		userDirectoryService.close();
		sessionManager.close();
		securityService.close();
		serverConfigurationService.close();
	}

	@Test
	public void autoReleasedGroupScoreIsSentToEveryGroupMember() throws Exception {
		Assignment assignment = ltiAssignment(true, true);
		AssignmentSubmission submission = submission(assignment, "student1", "student2", "student3");

		Site site = mock(Site.class);
		Group group = mock(Group.class);
		when(group.getId()).thenReturn(GROUP_ID);
		when(group.getReference()).thenReturn(GROUP_REF);
		when(site.getGroupsWithMember("student1")).thenReturn(List.of(group));
		when(siteService.getSite(SITE_ID)).thenReturn(site);
		when(assignmentService.getSubmission(ASSIGNMENT_ID, GROUP_ID)).thenReturn(submission);
		when(assignmentService.getGradeForSubmitter(submission, "student1")).thenReturn("8");
		when(assignmentService.getGradeForSubmitter(submission, "student2")).thenReturn("8");
		when(assignmentService.getGradeForSubmitter(submission, "student3")).thenReturn("9");

		Object result = SakaiLTIUtil.handleAssignment(assignment, "student1", score(8.0, "Nice work"));

		assertEquals(Boolean.TRUE, result);
		verify(gradingService).updateExternalAssessmentScore(SITE_ID, SITE_ID, ASSIGNMENT_REF, "student1", "8");
		verify(gradingService).updateExternalAssessmentScore(SITE_ID, SITE_ID, ASSIGNMENT_REF, "student2", "8");
		verify(gradingService).updateExternalAssessmentScore(SITE_ID, SITE_ID, ASSIGNMENT_REF, "student3", "9");
		verify(gradingService).updateExternalAssessmentComment(SITE_ID, SITE_ID, ASSIGNMENT_REF, "student2", "Nice work");
		verify(gradingService).updateExternalAssessmentComment(SITE_ID, SITE_ID, ASSIGNMENT_REF, "student3", "Nice work");
	}

	@Test
	public void autoReleasedIndividualScoreIsSentToTheStudent() throws Exception {
		Assignment assignment = ltiAssignment(false, true);
		AssignmentSubmission submission = submission(assignment, "student1");

		when(assignmentService.getSubmission(eq(ASSIGNMENT_ID), any(User.class))).thenReturn(submission);
		when(assignmentService.getGradeForSubmitter(submission, "student1")).thenReturn("8");

		Object result = SakaiLTIUtil.handleAssignment(assignment, "student1", score(8.0, "Nice work"));

		assertEquals(Boolean.TRUE, result);
		verify(gradingService).updateExternalAssessmentScore(SITE_ID, SITE_ID, ASSIGNMENT_REF, "student1", "8");
		verify(gradingService).updateExternalAssessmentComment(SITE_ID, SITE_ID, ASSIGNMENT_REF, "student1", "Nice work");
	}

	@Test
	public void groupScoreIsNotSentToGradebookWithoutAutoRelease() throws Exception {
		Assignment assignment = ltiAssignment(true, false);
		AssignmentSubmission submission = submission(assignment, "student1", "student2");

		Site site = mock(Site.class);
		Group group = mock(Group.class);
		when(group.getId()).thenReturn(GROUP_ID);
		when(group.getReference()).thenReturn(GROUP_REF);
		when(site.getGroupsWithMember("student1")).thenReturn(List.of(group));
		when(siteService.getSite(SITE_ID)).thenReturn(site);
		when(assignmentService.getSubmission(ASSIGNMENT_ID, GROUP_ID)).thenReturn(submission);

		Object result = SakaiLTIUtil.handleAssignment(assignment, "student1", score(8.0, "Nice work"));

		assertEquals(Boolean.TRUE, result);
		verify(gradingService, never()).updateExternalAssessmentScore(anyString(), anyString(), anyString(), anyString(), any());
	}

	private Assignment ltiAssignment(boolean isGroup, boolean autoRelease) {
		Assignment assignment = new Assignment();
		assignment.setId(ASSIGNMENT_ID);
		assignment.setContext(SITE_ID);
		assignment.setIsGroup(isGroup);
		assignment.setGroups(isGroup ? Set.of(GROUP_REF) : Set.of());
		assignment.setTypeOfSubmission(Assignment.SubmissionType.EXTERNAL_TOOL_SUBMISSION);
		assignment.setTypeOfGrade(Assignment.GradeType.SCORE_GRADE_TYPE);
		assignment.setMaxGradePoint(1000);
		assignment.setScaleFactor(100);
		Map<String, String> properties = assignment.getProperties();
		properties.put(AssignmentConstants.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, AssignmentConstants.GRADEBOOK_INTEGRATION_ADD);
		properties.put(AssignmentConstants.NEW_ASSIGNMENT_CHECK_LTI_AUTO_RELEASE_GRADES, Boolean.toString(autoRelease));
		return assignment;
	}

	private AssignmentSubmission submission(Assignment assignment, String... submitterIds) {
		AssignmentSubmission submission = new AssignmentSubmission();
		submission.setId("submission1");
		submission.setAssignment(assignment);
		for (String submitterId : submitterIds) {
			AssignmentSubmissionSubmitter submitter = new AssignmentSubmissionSubmitter();
			submitter.setSubmitter(submitterId);
			submission.getSubmitters().add(submitter);
		}
		return submission;
	}

	private Score score(Double scoreGiven, String comment) {
		Score score = new Score();
		score.scoreGiven = scoreGiven;
		score.scoreMaximum = 10.0;
		score.comment = comment;
		score.activityProgress = Score.ACTIVITY_COMPLETED;
		score.gradingProgress = Score.GRADING_FULLYGRADED;
		return score;
	}
}
