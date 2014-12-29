/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.event.impl;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.tool.api.SessionManager;

/**
 * <p>
 * SessionServiceAdaptorTest extends the db alias service providing the dependency injectors for testing. *
 * </p>
 */
@SuppressWarnings("unchecked")
public class SessionServiceAdaptorTest extends UsageSessionServiceAdaptor {

    TimeService timeService;
    SqlService sqlService;
    ServerConfigurationService serverConfigurationService;
    ThreadLocalManager threadLocalManager;
    SessionManager sessionManager;
    IdManager idManager;
    EventTrackingService eventTrackingService;
    AuthzGroupService authzGroupService;
    UserDirectoryService userDirectoryService;
    MemoryService memoryService;

    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
        this.threadLocalManager = threadLocalManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setIdManager(IdManager idManager) {
        this.idManager = idManager;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
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

    /**
     * @return the TimeService collaborator.
     */
    protected TimeService timeService() {
        return timeService;
    }

    /** Dependency: SqlService. */
    /**
     * @return the SqlService collaborator.
     */
    protected SqlService sqlService() {
        return sqlService;
    }

    /**
     * @return the ServerConfigurationService collaborator.
     */
    protected ServerConfigurationService serverConfigurationService() {
        return serverConfigurationService;
    }

    /**
     * @return the ThreadLocalManager collaborator.
     */
    protected ThreadLocalManager threadLocalManager() {
        return threadLocalManager;
    }

    /**
     * @return the SessionManager collaborator.
     */
    protected SessionManager sessionManager() {
        return sessionManager;
    }

    /**
     * @return the IdManager collaborator.
     */
    protected IdManager idManager() {
        return idManager;
    }

    /**
     * @return the EventTrackingService collaborator.
     */
    protected EventTrackingService eventTrackingService() {
        return eventTrackingService;
    }

    /**
     * @return the AuthzGroupService collaborator.
     */
    protected AuthzGroupService authzGroupService() {
        return authzGroupService;
    }

    /**
     * @return the UserDirectoryService collaborator.
     */
    protected UserDirectoryService userDirectoryService() {
        return userDirectoryService;
    }

    /**
     * @return the MemoryService collaborator.
     */
    protected MemoryService memoryService() {
        return memoryService;
    }

}
