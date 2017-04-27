package org.sakaiproject.authz.impl;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * Created by buckett on 27/03/2017.
 */
public class SakaiSecurityConcrete extends SakaiSecurity {
    private ThreadLocalManager threadLocalManager;
    private AuthzGroupService authzGroupService;
    private UserDirectoryService userDirectoryService;
    private MemoryService memoryService;
    private EntityManager entityManager;
    private SessionManager sessionManager;
    private EventTrackingService eventTrackingService;
    private FunctionManager functionManager;
    private SiteService siteService;

    public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
        this.threadLocalManager = threadLocalManager;
    }

    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setMemoryService(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public void setFunctionManager(FunctionManager functionManager) {
        this.functionManager = functionManager;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        // In parent class
        this.serverConfigurationService = serverConfigurationService;
    }

    @Override
    protected ThreadLocalManager threadLocalManager() {
        return threadLocalManager;
    }

    @Override
    protected AuthzGroupService authzGroupService() {
        return authzGroupService;
    }

    @Override
    protected UserDirectoryService userDirectoryService() {
        return userDirectoryService;
    }

    @Override
    protected MemoryService memoryService() {
        return memoryService;
    }

    @Override
    protected EntityManager entityManager() {
        return entityManager;
    }

    @Override
    protected SessionManager sessionManager() {
        return sessionManager;
    }

    @Override
    protected EventTrackingService eventTrackingService() {
        return eventTrackingService;
    }

    @Override
    protected FunctionManager functionManager() {
        return functionManager;
    }

    @Override
    protected SiteService siteService() {
        return siteService;
    }
}
