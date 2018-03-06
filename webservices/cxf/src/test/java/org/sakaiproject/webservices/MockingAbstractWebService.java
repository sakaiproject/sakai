/**
 * Copyright (c) 2005 The Apereo Foundation
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
package org.sakaiproject.webservices;

import static org.mockito.Mockito.mock;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.ActivityService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.shortenedurl.api.ShortenedUrlService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.assessment.samlite.api.SamLiteService;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.tool.assessment.shared.impl.questionpool.QuestionPoolServiceImpl;

@Slf4j
public class MockingAbstractWebService {

	public static <U extends AbstractWebService> AbstractWebService getMockedAbstractWebService(Class<U> service) {
		AbstractWebService instance = null;
		try {
			instance = service.newInstance();
			instance.setAreaManager(mock(AreaManager.class));
			instance.setSessionManager(mock(SessionManager.class));
			instance.setAuthenticationManager(mock(AuthenticationManager.class));
			instance.setAssignmentService(mock(AssignmentService.class));
			instance.setAuthzGroupService(mock(AuthzGroupService.class));
			instance.setCalendarService(mock(CalendarService.class));
			instance.setEventTrackingService(mock(EventTrackingService.class));
			instance.setGradebookService(mock(GradebookService.class));
			instance.setSecurityService(mock(SecurityService.class));
			instance.setServerConfigurationService(mock(ServerConfigurationService.class));
			instance.setSiteService(mock(SiteService.class));
			instance.setTimeService(mock(TimeService.class));
			instance.setToolManager(mock(ToolManager.class));
			instance.setUsageSessionService(mock(UsageSessionService.class));
			instance.setUserDirectoryService(mock(UserDirectoryService.class));
			instance.setContentHostingService(mock(ContentHostingService.class));
			instance.setEntityManager(mock(EntityManager.class));
			instance.setDiscussionForumManager(mock(DiscussionForumManager.class));
			instance.setMessageForumsForumManager(mock(MessageForumsForumManager.class));
			instance.setMessageForumsMessageManager(mock(MessageForumsMessageManager.class));
			instance.setMessageForumsTypeManager(mock(MessageForumsTypeManager.class));
			instance.setThreadLocalManager(mock(ThreadLocalManager.class));
			instance.setSchedulerManager(mock(SchedulerManager.class));
			instance.setShortenedUrlService(mock(ShortenedUrlService.class));
			instance.setSamLiteService(mock(SamLiteService.class));
			instance.setIdManager(mock(IdManager.class));
			instance.setGradebookExternalAssessmentService(mock(GradebookExternalAssessmentService.class));
			instance.setActivityService(mock(ActivityService.class));
			instance.setPreferencesService(mock(PreferencesService.class));
			instance.setQuestionPoolServiceImpl(mock(QuestionPoolServiceImpl.class));
		} catch (InstantiationException | IllegalAccessException e) {
			log.error(e.getMessage(), e);
		}
		return instance;
	}
}
