package org.sakaiproject.webservices;

import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * Created by jbush on 2/11/14.
 */
@WebService
public class AbstractWebService {
    protected SessionManager sessionManager;

    protected AssignmentService assignmentService;
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
    protected UserDirectoryService userDirectoryService;
    protected ContentHostingService contentHostingService;
    protected EntityManager entityManager;
    
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
}
