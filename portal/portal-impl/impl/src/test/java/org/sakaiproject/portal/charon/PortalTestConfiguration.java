package org.sakaiproject.portal.charon;

import org.mockito.Mockito;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.presence.api.PresenceService;
import org.sakaiproject.profile2.logic.ProfileImageLogic;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
public class PortalTestConfiguration {

    @Bean(name = "org.sakaiproject.tool.api.ActiveToolManager")
    public ActiveToolManager activeToolManager() {
        return mock(ActiveToolManager.class);
    }

    @Bean(name = "org.sakaiproject.coursemanagement.api.CourseManagementService")
    public CourseManagementService courseManagementService() {
        return mock(CourseManagementService.class);
    }

    @Bean(name = "org.sakaiproject.event.api.EventTrackingService")
    public EventTrackingService eventTrackingService() {
        return mock(EventTrackingService.class);
    }

    @Bean(name = "org.sakaiproject.user.api.PreferencesService")
    public PreferencesService preferencesService() {
        return mock(PreferencesService.class);
    }

    @Bean(name = "org.sakaiproject.presence.api.PresenceService")
    public PresenceService presenceService() {
        return mock(PresenceService.class);
    }

    @Bean(name = "org.sakaiproject.profile2.logic.ProfileImageLogic")
    public ProfileImageLogic profileImageLogic() {
        return mock(ProfileImageLogic.class);
    }

    @Bean(name = "org.sakaiproject.authz.api.SecurityService")
    public SecurityService securityService() {
        return mock(SecurityService.class);
    }

    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    public ServerConfigurationService serverConfigurationService() {
        ServerConfigurationService scs = mock(ServerConfigurationService.class);
        Mockito.when(scs.getString("portal.mutable.sitename", "-")).thenReturn("-");
        Mockito.when(scs.getString("portal.mutable.pagename", "-")).thenReturn("-");
        return scs;
    }

    @Bean(name = "org.sakaiproject.tool.api.SessionManager")
    public SessionManager sessionManager() {
        return mock(SessionManager.class);
    }

    @Bean(name = "org.sakaiproject.site.api.SiteService")
    public SiteService siteService() {
        return mock(SiteService.class);
    }

    @Bean(name = "org.sakaiproject.thread_local.api.ThreadLocalManager")
    public ThreadLocalManager threadLocalManager() {
        return mock(ThreadLocalManager.class);
    }

    @Bean(name = "org.sakaiproject.time.api.TimeService")
    public TimeService timeService() {
        return mock(TimeService.class);
    }

    @Bean(name = "org.sakaiproject.user.api.UserDirectoryService")
    public UserDirectoryService userDirectoryService() {
        return mock(UserDirectoryService.class);
    }

    @Bean(name = "org.sakaiproject.time.api.UserTimeService")
    public UserTimeService userTimeService() {
        return mock(UserTimeService.class);
    }

}
