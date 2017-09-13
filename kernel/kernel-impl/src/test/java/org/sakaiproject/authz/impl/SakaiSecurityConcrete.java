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
import org.sakaiproject.tool.api.ToolManager;
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
    private ToolManager toolManager;

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

    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
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

	@Override
	protected ToolManager toolManager() {
		// TODO Auto-generated method stub
		return toolManager;
	}
}
