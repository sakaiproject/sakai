/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation.
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

package org.sakaiproject.tool.impl;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Export some basic information about the sessions over JMX.
 * @author buckett
 * @author azeckoski
 */
@ManagedResource(objectName="org.sakaiproject:name=Sessions", description="Sakai Sessions Manager data")
public class JMXSessionComponent {

    // METHODS to get actions

    ServerConfigurationService serverConfigurationService;
    private SessionManager sessionManager;
    private SessionComponent sessionComponent;

    @ManagedAttribute(description="Sakai server id", currencyTimeLimit=600)
    public String getServerId() {
        return serverConfigurationService.getServerId();
    }

    @ManagedAttribute(description="Sakai server instance", currencyTimeLimit=600)
    public String getServerInstance() {
        return serverConfigurationService.getServerInstance();
    }

    @ManagedAttribute(description="Sakai server id instance", currencyTimeLimit=600)
    public String getServerIdInstance() {
        return serverConfigurationService.getServerIdInstance();
    }

    @ManagedAttribute(description="Sessions active in the past 5 minutes", currencyTimeLimit=20)
    public int getActive05Min() {
        return sessionManager.getActiveUserCount(300);
    }

    @ManagedAttribute(description="Sessions active in the past 10 minutes", currencyTimeLimit=40)
    public int getActive10Min() {
        return sessionManager.getActiveUserCount(600);
    }


    // LOOKUPS

    @ManagedAttribute(description="Sessions active in the past 15 minutes", currencyTimeLimit=60)
    public int getActive15Min() {
        return sessionManager.getActiveUserCount(900);
    }

    @ManagedAttribute(description="Sessions count in total", currencyTimeLimit=0)
    public int getSessionsCount() {
        return sessionManager.getSessions().size();
    }

    @ManagedAttribute(description="Sessions IDs (will match the cookie value)", currencyTimeLimit=0)
    public List<String> getSessionsIds() {
        List<Session> sessions = sessionManager.getSessions();
        List<String> ids = new ArrayList<String>(sessions.size());
        for (Session session : sessions) {
            ids.add(session.getId());
        }
        return ids;
    }

    @ManagedOperation(description="Sessions active in the past N seconds")
    public int getActive(int seconds) {
        return sessionManager.getActiveUserCount(seconds);
    }

    // SETTERS AND VARS

    @ManagedOperation(description="Summary of a current session (attributes and key fields) by session ID")
    public Map<String, String> getSession(String sessionId) {
        MySession s = (MySession) sessionManager.getSession(sessionId);
        return s.currentAttributesSummary();
    }

    @ManagedOperation(description="Invalidate a session by session ID")
    public void invalidateSession(String sessionId) {
        if (sessionComponent != null) {
            Session s = sessionComponent.m_sessions.get(sessionId);
            if (s != null) {
                s.invalidate();
            }
            sessionComponent.m_sessions.remove(sessionId);
        }
    }

    @ManagedOperation(description="Destroy a session by session ID (leaves storage intact)")
    public void killSession(String sessionId) {
        MySession s = (MySession) sessionManager.getSession(sessionId);
        s.destroy();
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        if (sessionManager instanceof SessionComponent) {
            this.sessionComponent = (SessionComponent) sessionManager;
        }
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

}
