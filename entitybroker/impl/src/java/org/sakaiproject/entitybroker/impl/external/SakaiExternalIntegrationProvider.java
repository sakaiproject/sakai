/**
 * $Id$
 * $URL$
 * ExternalIntegrationProvider.java - entity-broker - Jan 12, 2009 6:24:04 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl.external;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider;
import org.sakaiproject.entitybroker.util.servlet.DirectServlet;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
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
public class SakaiExternalIntegrationProvider implements ExternalIntegrationProvider {

    private static final Log log = LogFactory.getLog(SakaiExternalIntegrationProvider.class);

    // SAKAI
    private EntityManager entityManager; // for find entity by reference
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private EventTrackingService eventTrackingService; // for fire event
    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
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
        error.printStackTrace(pw);
        String stacktrace = "Full stacktrace:\n" + error.getClass().getSimpleName() + ":" 
                + error.getMessage() + ":\n" + sw.toString();

        String body = subject + ":\n " + sakaiVersion + "\n" + serverInfo + "\n" 
                + requestInfo + "\n" + usageSessionInfo;

        // attempt to get the email address, if it is not there then we will not send an email
        String emailAddr = serverConfigurationService.getString("direct.error.email");
        if (emailAddr == null) {
            emailAddr = serverConfigurationService.getString("portal.error.email");
        }
        if (emailAddr != null) {
            String from = "\"<no-reply@" + serverConfigurationService.getServerName() + ">";
            if (emailService != null) {
                emailService.send(from, emailAddr, subject, body + "\n" + stacktrace, emailAddr, null, null);
            } else {
                log.error("Could not send email, no emailService");
            }
        }
        String errorMessage = subject + ":" + body;
        return errorMessage;
    }

}
