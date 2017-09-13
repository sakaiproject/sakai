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

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.event.api.ActivityService;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.shortenedurl.api.ShortenedUrlService;
import org.sakaiproject.tool.assessment.samlite.api.SamLiteService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.lessonbuildertool.LessonBuilderAccessAPI;
import org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by jbush on 2/11/14.
 */
@WebService
public class AbstractWebService {
    protected SessionManager sessionManager;

    protected AssignmentService assignmentService;
    protected AuthenticationManager authenticationManager;
    protected AuthzGroupService authzGroupService;
    protected CalendarService calendarService;
    protected EventTrackingService eventTrackingService;
    protected GradebookService gradebookService;
    protected SecurityService securityService;
    protected ServerConfigurationService serverConfigurationService;
    protected SiteService siteService;
    protected TimeService timeService;
    protected ToolManager toolManager;
    protected UsageSessionService usageSessionService;
    protected PreferencesService preferencesService;
    protected UserDirectoryService userDirectoryService;
    protected ContentHostingService contentHostingService;
    protected EntityManager entityManager;
    protected DiscussionForumManager discussionForumManager;
    protected MessageForumsForumManager messageForumsForumManager;
    protected MessageForumsMessageManager messageForumsMessageManager;
    protected MessageForumsTypeManager messageForumsTypeManager;
    protected AreaManager areaManager;
    protected ThreadLocalManager threadLocalManager;
    protected SchedulerManager schedulerManager;
    protected ShortenedUrlService shortenedUrlService;
    protected SamLiteService samLiteService;
    protected IdManager idManager;
    protected GradebookExternalAssessmentService gradebookExternalAssessmentService;
    protected ActivityService activityService;
    protected QuestionPoolServiceAPI questionPoolServiceImpl;
    protected LessonBuilderAccessAPI lessonBuilderAccessAPI;

    
    @WebMethod(exclude = true)
    public void init() {
    }

    /**
     * Get the Session related to the given sessionid
     *
     * @param sessionid the id of the session to retrieve
     * @return the session, if it is active
     * @	if session is inactive
     */
    protected Session establishSession(String sessionid) {
        Session s = sessionManager.getSession(sessionid);

        if (s == null) {
            throw new RuntimeException("Session \"" + sessionid + "\" is not active");
        }
        s.setActive();
        sessionManager.setCurrentSession(s);
        return s;
    }
 
    /**
     * Get the IP related to this request
     *
     * @return the request remote address
     */
    protected String getUserIp() {
        Message message = PhaseInterceptorChain.getCurrentMessage();
        HttpServletRequest request = (HttpServletRequest)message.get(AbstractHTTPDestination.HTTP_REQUEST);
        return request.getRemoteAddr();
    }

    @WebMethod(exclude = true)
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @WebMethod(exclude = true)
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    
    @WebMethod(exclude = true)
    public void setAssignmentService(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }
    
    @WebMethod(exclude = true)
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @WebMethod(exclude = true)
    public void setGradebookService(GradebookService gradebookService) {
        this.gradebookService = gradebookService;
    }
    
    @WebMethod(exclude = true)
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    @WebMethod(exclude = true)
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }
    
    @WebMethod(exclude = true)
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    @WebMethod(exclude = true)
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    @WebMethod(exclude = true)
    public void setPreferencesService(PreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    @WebMethod(exclude = true)
    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }
    
    @WebMethod(exclude = true)
    public void setUsageSessionService(UsageSessionService usageSessionService) {
        this.usageSessionService = usageSessionService;
    }
    
    @WebMethod(exclude = true)
    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }
    
    @WebMethod(exclude = true)
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @WebMethod(exclude = true)
    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }
    
    @WebMethod(exclude = true)
    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }
    
    @WebMethod(exclude = true)
    public void setDiscussionForumManager(DiscussionForumManager discussionForumManager) {
        this.discussionForumManager = discussionForumManager;
    }
    
    @WebMethod(exclude = true)
    public void setMessageForumsForumManager(MessageForumsForumManager messageForumsForumManager) {
        this.messageForumsForumManager = messageForumsForumManager;
    }
    
    @WebMethod(exclude = true)
    public void setMessageForumsMessageManager(MessageForumsMessageManager messageForumsMessageManager) {
        this.messageForumsMessageManager = messageForumsMessageManager;
    }
    
    @WebMethod(exclude = true)
    public void setMessageForumsTypeManager(MessageForumsTypeManager messageForumsTypeManager) {
        this.messageForumsTypeManager = messageForumsTypeManager;
    }
    
    @WebMethod(exclude = true)
    public void setAreaManager(AreaManager areaManager) {
        this.areaManager = areaManager;
    }

    @WebMethod(exclude = true)
    public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
        this.threadLocalManager = threadLocalManager;
    }
    
    @WebMethod(exclude = true)
    public void setSchedulerManager(SchedulerManager schedulerManager) {
        this.schedulerManager = schedulerManager;
    }
    
    @WebMethod(exclude = true)
    public void setShortenedUrlService(ShortenedUrlService shortenedUrlService) {
        this.shortenedUrlService = shortenedUrlService;
    }
    
    @WebMethod(exclude = true)
    public void setSamLiteService(SamLiteService samLiteService) {
        this.samLiteService = samLiteService;
    }

    @WebMethod(exclude = true)
    public void setIdManager(IdManager idManager) {
        this.idManager = idManager;
    }
    
    @WebMethod(exclude = true)
    public void setGradebookExternalAssessmentService(GradebookExternalAssessmentService service) {
        this.gradebookExternalAssessmentService = service;
    }

    @WebMethod(exclude = true)
    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }

    @WebMethod(exclude = true)
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }
    
    @WebMethod(exclude = true)
    public void setQuestionPoolServiceImpl(QuestionPoolServiceAPI questionPoolServiceImpl) {
        this.questionPoolServiceImpl = questionPoolServiceImpl;
    }

    @WebMethod(exclude = true)
    public void setLessonBuilderAccessAPI(LessonBuilderAccessAPI lessonBuilderAccessAPI) {
        this.lessonBuilderAccessAPI = lessonBuilderAccessAPI;
    }

}
