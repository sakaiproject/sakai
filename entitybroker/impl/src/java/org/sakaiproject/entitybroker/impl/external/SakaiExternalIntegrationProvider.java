/**
 * $Id$
 * $URL$
 * ExternalIntegrationProvider.java - entity-broker - Jan 12, 2009 6:24:04 PM - azeckoski
 **********************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 **********************************************************************************/

package org.sakaiproject.entitybroker.impl.external;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider;
import org.sakaiproject.entitybroker.util.servlet.DirectServlet;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;


/**
 * This allows EB to integrate with external systems,
 * this combines with the implementation of {@link DeveloperHelperService} and {@link DirectServlet}
 * to allow external systems to plugin their own handling without
 * having to modify the core EB codebase
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public class SakaiExternalIntegrationProvider implements ExternalIntegrationProvider {

    // SAKAI
    private EntityManager entityManager; // for find entity by reference
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private EventTrackingService eventTrackingService; // for fire event
    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    private LearningResourceStoreService learningResourceStoreService;
    public void setLearningResourceStoreService(LearningResourceStoreService learningResourceStoreService) {
        this.learningResourceStoreService = learningResourceStoreService;
    }

    private ServerConfigurationService serverConfigurationService;
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    private EmailService emailService;
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    private UsageSessionService usageSessionService;
    public void setUsageSessionService(UsageSessionService usageSessionService) {
        this.usageSessionService = usageSessionService;
    }

    private SessionManager sessionManager;
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider#findService(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public <T> T findService(Class<T> type) {
        return (T) ComponentManager.getInstance().get(type);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.impl.ExternalIntegrationProvider#fireEvent(java.lang.String, java.lang.String)
     */
    public void fireEvent(String eventName, String reference) {
        // had to take out the exists check because it makes firing events for removing entities very annoying -AZ
        Event event = eventTrackingService.newEvent(eventName, reference, true, NotificationService.PREF_IMMEDIATE);
        eventTrackingService.post(event);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.impl.ExternalIntegrationProvider#getServerUrl()
     */
    public String getServerUrl() {
        return serverConfigurationService.getServerUrl();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider#getMaxJSONLevel()
     */
    public String getMaxJSONLevel() {
        return serverConfigurationService.getString("entitybroker.maxJSONLevel","7"); // default 7
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider#fetchEntity(java.lang.String)
     */
    public Object fetchEntity(String reference) {
        Object entity = null;
        try {
            // cannot test this in a meaningful way so the tests are designed to not get here -AZ
            entity = entityManager.newReference(reference).getEntity();
        } catch (Exception e) {
            log.warn("Failed to look up reference '" + reference
                    + "' to an entity in Sakai legacy entity system", e);
        }
        return entity;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider#handleUserSessionKey(javax.servlet.http.HttpServletRequest)
     */
    public void handleUserSessionKey(HttpServletRequest req) {
        // SAKAI        
        
        // http://jira.sakaiproject.org/jira/browse/SAK-14899 - added support for setting the sakai session id
        final String SAKAI_SESSION = "sakai.session";
        if (req.getParameter(SAKAI_SESSION) != null
                || req.getParameter(SESSION_ID) != null) {
            // set the session to the given id if possible or die
            String sessionId = req.getParameter(SAKAI_SESSION);
            if (sessionId == null) {
                sessionId = req.getParameter(SESSION_ID);
            }
            try {
                // this also protects us from null pointer where session service is not set or working
                Session s = sessionManager.getSession(sessionId);
                if (s != null) {
                    sessionManager.setCurrentSession(s);
                } else {
                    throw new IllegalArgumentException("Invalid sakai session id ("+sessionId+") supplied, could not find a valid session with that id to set");
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Failure attempting to set sakai session id ("+sessionId+"): " + e.getMessage());
            }
        }
        
        //https://jira.sakaiproject.org/browse/SAK-25843 - validate session on each request, if param is set
        final String VALIDATE_SESSION = "_validateSession";
        if (req.getParameter(VALIDATE_SESSION) != null) {
        	String current = sessionManager.getCurrentSessionUserId();
        	if (current == null) {
        		throw new SecurityException("Invalid session or session has timed out, and explicit session validation has been requested.");
        	}
        }
        
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider#handleEntityError(javax.servlet.http.HttpServletRequest, java.lang.Throwable)
     */
    public String handleEntityError(HttpServletRequest req, Throwable error) {
        String subject = "Direct request failure: " + error.getClass().getSimpleName() + ":" + error.getMessage();

        String sakaiVersion = "Sakai version: " + serverConfigurationService.getString("version.sakai") 
                + "("+serverConfigurationService.getString("version.service")+")\n ";

        String serverInfo = "Server: " + serverConfigurationService.getServerName() 
                + "("+serverConfigurationService.getServerId()+") ["+serverConfigurationService.getServerIdInstance()+"]\n ";

        String usageSessionInfo = "";
        if (usageSessionService != null) {
            UsageSession usageSession = usageSessionService.getSession();
            if (usageSession != null) {
                usageSessionInfo = "Server: " + usageSession.getServer() + "\n "
                        //+ "Hostname: " + usageSession.getHostName() + "\n " // removed since this is incompatible with older sakai
                        + "User agent: " + usageSession.getUserAgent() + "\n "
                        + "Browser ID: " + usageSession.getBrowserId() + "\n "
                        + "IP address: " + usageSession.getIpAddress() + "\n "
                        + "User ID: " + usageSession.getUserId() + "\n "
                        + "User EID: " + usageSession.getUserEid() + "\n "
                        + "User Display ID: " + usageSession.getUserDisplayId() + "\n ";
            }
        }

        String requestInfo = "";
        if (req != null) {
            requestInfo = "Request URI: "+req.getRequestURI()+"\n "
                    + "Path Info: "+req.getPathInfo()+"\n "
                    + "Context path: "+req.getContextPath()+"\n "
                    + "Method: "+req.getMethod()+"\n ";
        }

        // get the stacktrace out
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String stacktrace = "Full stacktrace:\n" + error.getClass().getSimpleName() + ":" 
                + error.getMessage() + ":\n" + sw.toString();

        String body = subject + ":\n " + sakaiVersion + "\n" + serverInfo + "\n" 
                + requestInfo + "\n" + usageSessionInfo;

        // attempt to get the email address, if it is not there then we will not send an email
        String emailAddr = serverConfigurationService.getString("direct.error.email", 
                serverConfigurationService.getString("portal.error.email"));

        if (emailAddr != null && !"".equals(emailAddr)) {
            String from = "\" <"+ serverConfigurationService.getString("setup.request","no-reply@" + serverConfigurationService.getServerName()) + ">";
            if (emailService != null) {
                emailService.send(from, emailAddr, subject, body + "\n" + stacktrace, emailAddr, null, null);
            } else {
                log.error("Could not send email, no emailService");
            }
        }
        String errorMessage = subject + ":" + body;
        log.error(errorMessage + "\n" + stacktrace);
        return errorMessage;
    }

    /**
     * String type: gets the printable name of this server
     */
    protected static final String SETTING_SERVER_NAME = "server.name";

    /**
     * String type: gets the unique id of this server (safe for clustering if used)
     */
    protected static final String SETTING_SERVER_ID = "server.cluster.id";

    /**
     * String type: gets the URL to this server
     */
    protected static final String SETTING_SERVER_URL = "server.main.URL";

    /**
     * String type: gets the URL to the portal on this server (or just returns the server URL if no
     * portal in use)
     */
    protected static final String SETTING_PORTAL_URL = "server.portal.URL";

    /**
     * Boolean type: if true then there will be data preloads and DDL creation, if false then data
     * preloads are disabled (and will cause exceptions if preload data is missing)
     */
    protected static final String SETTING_AUTO_DDL = "auto.ddl";

    @SuppressWarnings("unchecked")
    public <T> T getConfigurationSetting(String settingName, T defaultValue) {
        T returnValue = defaultValue;
        if (SETTING_SERVER_NAME.equals(settingName)) {
            returnValue = (T) serverConfigurationService.getServerName();
        } else if (SETTING_SERVER_URL.equals(settingName)) {
            returnValue = (T) serverConfigurationService.getServerUrl();
        } else if (SETTING_PORTAL_URL.equals(settingName)) {
            returnValue = (T) serverConfigurationService.getPortalUrl();
        } else if (SETTING_SERVER_ID.equals(settingName)) {
            returnValue = (T) serverConfigurationService.getServerIdInstance();
        } else {
            if (defaultValue == null) {
                returnValue = (T) serverConfigurationService.getString(settingName);
                if ("".equals(returnValue)) {
                    returnValue = null;
                }
            } else {
                if (defaultValue instanceof Number) {
                    int num = ((Number) defaultValue).intValue();
                    int value = serverConfigurationService.getInt(settingName, num);
                    returnValue = (T) Integer.valueOf(value);
                } else if (defaultValue instanceof Boolean) {
                    boolean bool = ((Boolean) defaultValue).booleanValue();
                    boolean value = serverConfigurationService.getBoolean(settingName, bool);
                    returnValue = (T) Boolean.valueOf(value);
                } else if (defaultValue instanceof String) {
                    returnValue = (T) serverConfigurationService.getString(settingName,
                            (String) defaultValue);
                }
            }
        }
        return returnValue;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.extension.LearningTrackingProvider#registerStatement(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Float)
     */
    public void registerStatement(String prefix, String actorEmail, String verbStr, String objectURI, Boolean resultSuccess, Float resultScaledScore) {
        if (prefix == null || "".equals(prefix)) {
            throw new IllegalArgumentException("prefix must be set");
        }
        LRS_Statement statement = new LRS_Statement(actorEmail, verbStr, objectURI);
        if (resultSuccess != null && resultScaledScore != null) {
            statement = new LRS_Statement(actorEmail, verbStr, objectURI, resultSuccess.booleanValue(), resultScaledScore.floatValue());
        } else {
            statement = new LRS_Statement(actorEmail, verbStr, objectURI);
        }
        learningResourceStoreService.registerStatement(statement, prefix);
    }

}
