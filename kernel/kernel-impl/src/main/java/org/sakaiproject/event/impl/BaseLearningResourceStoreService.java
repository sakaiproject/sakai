/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreProvider;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb.SAKAI_VERB;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Core implementation of the LRS integration
 * This will basically just reroute calls over to the set of known {@link LearningResourceStoreProvider}.
 * It also does basic config handling (around enabling/disabling the overall processing) and filtering handled statement origins.
 * 
 * Configuration:
 * 1) Enable LRS processing
 * Default: false
 * lrs.enabled=true
 * 2) Enabled statement origin filters
 * Default: No filters (all statements processed)
 * lrs.origins.filter=tool1,tool2,tool3
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public class BaseLearningResourceStoreService implements LearningResourceStoreService, ApplicationContextAware {

    private static final String ORIGIN_SAKAI_SYSTEM = "sakai.system";
    private static final String ORIGIN_SAKAI_CONTENT = "sakai.resources";

    private static final Log log = LogFactory.getLog(BaseLearningResourceStoreService.class);

    /**
     * Stores the complete set of known LRSP providers (from the Spring AC or registered manually)
     */
    private ConcurrentHashMap<String, LearningResourceStoreProvider> providers;
    /**
     * Contains a timestamp of the last time we warned that there are no providers to handle processing
     */
    private long noProvidersWarningTS = 0;
    /**
     * Stores the complete set of origin filters for the LRS service,
     * Anything with an origin that matches the ones in this set will be blocked from being processed
     */
    private HashSet<String> originFilters;
    /**
     * Allows us to be notified of all incoming local events
     */
    private ExperienceObserver experienceObserver;

    public void init() {
        providers = new ConcurrentHashMap<String, LearningResourceStoreProvider>();
        // search for known providers
        if (isEnabled() && applicationContext != null) {
            @SuppressWarnings("unchecked")
            Map<String, LearningResourceStoreProvider> beans = applicationContext.getBeansOfType(LearningResourceStoreProvider.class);
            for (LearningResourceStoreProvider lrsp : beans.values()) {
                if (lrsp != null) { // should not be null but this avoids killing everything if it is
                    registerProvider(lrsp);
                }
            }
            log.info("LRS Registered "+beans.size()+" LearningResourceStoreProviders from the Spring AC during service INIT");
        } else {
            log.info("LRS did not search for existing LearningResourceStoreProviders in the system (ac="+applicationContext+", enabled="+isEnabled()+")");
        }
        if (isEnabled() && serverConfigurationService != null) {
            String[] filters = serverConfigurationService.getStrings("lrs.origins.filter");
            if (filters == null || filters.length == 0) {
                log.info("LRS filters are not configured: All statements will be passed through to the LRS");
            } else {
                originFilters = new HashSet<String>(filters.length);
                for (int i = 0; i < filters.length; i++) {
                    if (filters[i] != null) {
                        originFilters.add(filters[i]);
                    }
                }
                log.info("LRS found "+originFilters.size()+" origin filters: "+originFilters);
            }
        }
        if (isEnabled() && eventTrackingService != null) {
            this.experienceObserver = new ExperienceObserver(this);
            eventTrackingService.addLocalObserver(this.experienceObserver);
            log.info("LRS registered local event tracking observer");
        }
        log.info("LRS INIT: enabled="+isEnabled());
    }

    public void destroy() {
        if (providers != null) {
            providers.clear();
        }
        originFilters = null;
        providers = null;
        if (experienceObserver != null && eventTrackingService != null) {
            eventTrackingService.deleteObserver(experienceObserver);
        }
        experienceObserver = null;
        log.info("LRS DESTROY");
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.event.api.LearningResourceStoreService#registerStatement(org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement, java.lang.String)
     */
    public void registerStatement(LRS_Statement statement, String origin) {
        if (statement == null) {
            log.error("LRS registerStatement call INVALID, statement is null and must not be");
            //throw new IllegalArgumentException("statement must be set");
        } else if (isEnabled()) {
            if (providers == null || providers.isEmpty()) {
                if (noProvidersWarningTS < (System.currentTimeMillis() - 86400000)) { // check if we already warned in the last 24 hours
                    noProvidersWarningTS = System.currentTimeMillis();
                    log.warn("LRS statement from ("+origin+") skipped because there are no providers to process it: "+statement);
                }
            } else {
                // filter out certain tools and statement origins
                boolean skip = false;
                if (originFilters != null && !originFilters.isEmpty()) {
                    origin = StringUtils.trimToNull(origin);
                    if (origin != null && originFilters.contains(origin)) {
                        if (log.isDebugEnabled()) log.debug("LRS statement skipped because origin ("+origin+") matches the originFilter");
                        skip = true;
                    }
                }
                if (!skip) {
                    // validate the statement
                    boolean valid = false;
                    if (statement.isPopulated()
                            && statement.getActor() != null 
                            && statement.getVerb() != null 
                            && statement.getObject() != null) {
                        valid = true;
                    } else if (statement.getRawMap() != null 
                            && !statement.getRawMap().isEmpty()) {
                        valid = true;
                    } else if (statement.getRawJSON() != null 
                            && !StringUtils.isNotBlank(statement.getRawJSON())) {
                        valid = true;
                    }
                    if (valid) {
                        // process this statement
                        if (log.isDebugEnabled()) log.debug("LRS statement being processed, origin="+origin+", statement="+statement);
                        for (LearningResourceStoreProvider lrsp : providers.values()) {
                            // run the statement processing in a new thread
                            String threadName = "LRS_"+lrsp.getID();
                            Thread t = new Thread(new RunStatementThread(lrsp, statement), threadName); // each provider has it's own thread
                            t.setDaemon(true); // allow this thread to be killed when the JVM is shutdown
                            t.start();
                        }
                    } else {
                        log.warn("Invalid statment registered, statement will not be processed: "+statement);
                    }
                } else {
                    if (log.isDebugEnabled()) log.debug("LRS statement being skipped, origin="+origin+", statement="+statement);
                }
            }
        }
    }

    /**
     * internal class to support threaded execution of statements processing
     */
    private static class RunStatementThread implements Runnable {
        final LearningResourceStoreProvider lrsp;
        final LRS_Statement statement;
        public RunStatementThread(LearningResourceStoreProvider lrsp, LRS_Statement statement) {
            this.lrsp = lrsp;
            this.statement = statement;
        }
        @Override
        public void run() {
            try {
                lrsp.handleStatement(statement);
            } catch (Exception e) {
                log.error("LRS Failure running LRS statement in provider ("+lrsp.getID()+"): statement=("+statement+"): "+e, e);
            }
        }
    };

    private static class ExperienceObserver implements Observer {
        final BaseLearningResourceStoreService lrss;
        public ExperienceObserver(BaseLearningResourceStoreService lrss) {
            this.lrss = lrss;
        }
        @Override
        public void update(Observable observable, Object object) {
            if (object != null && object instanceof Event) {
                Event event = (Event) object;
                // convert event into origin
                String origin = this.lrss.getEventOrigin(event);
                // convert event into statement when possible
                LRS_Statement statement = this.lrss.getEventStatement(event);
                if (statement != null) {
                    this.lrss.registerStatement(statement, origin);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.event.api.LearningResourceStoreService#isEnabled()
     */
    public boolean isEnabled() {
        boolean enabled = false;
        if (serverConfigurationService != null) {
            enabled = serverConfigurationService.getBoolean("lrs.enabled", enabled);
        }
        return enabled;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.event.api.LearningResourceStoreService#registerProvider(org.sakaiproject.event.api.LearningResourceStoreProvider)
     */
    public boolean registerProvider(LearningResourceStoreProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("LRS provider must not be null");
        }
        return providers.put(provider.getID(), provider) != null;
    }

    /**
     * @param event an Event
     * @return a statement if one can be formed OR null if not
     */
    private LRS_Statement getEventStatement(Event event) {
        LRS_Statement statement;
        try {
            LRS_Verb verb = getEventVerb(event);
            if (verb != null) {
                LRS_Object object = getEventObject(event);
                if (object != null) {
                    LRS_Actor actor = getEventActor(event);
                    statement = new LRS_Statement(actor, verb, object);
                } else {
                    statement = null;
                }
            } else {
                statement = null;
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) log.debug("LRS Unable to convert event ("+event+") into statement: "+e);
            statement = null;
        }
        return statement;
    }

    /**
     * @param event an Event
     * @return the actor for the user related to the event (OR null if no user can be found)
     */
    private LRS_Actor getEventActor(Event event) {
        LRS_Actor actor = null;
        User user = null;
        if (event.getUserId() != null) {
            try {
                user = this.userDirectoryService.getUser(event.getUserId());
            } catch (UserNotDefinedException e) {
                user = null;
            }
        } else if (event.getSessionId() != null) {
            Session session = this.sessionManager.getSession(event.getSessionId());
            if (session != null) {
                try {
                    user = this.userDirectoryService.getUser(session.getUserId());
                } catch (UserNotDefinedException e) {
                    user = null;
                }
            }
        }
        if (user != null) {
            String actorEmail;
            if (StringUtils.isNotEmpty(user.getEmail())) {
                actorEmail = user.getEmail();
            } else {
                // no email set - make up something like one
                String server = serverConfigurationService.getServerName();
                if ("localhost".equals(server)) {
                    server = "tincanapi.dev.sakaiproject.org";
                } else {
                    server = serverConfigurationService.getServerId()+"."+server;
                }
                actorEmail = user.getId()+"@"+server;
                log.warn("LRS Actor: No email set for user ("+user.getId()+"), using generated one: "+actorEmail);
            }
            actor = new LRS_Actor(actorEmail);
        }
        return actor;
    }

    private LRS_Verb getEventVerb(Event event) {
        LRS_Verb verb = null;
        if ("user.login".equals(event.getEvent())) {
            verb = new LRS_Verb(SAKAI_VERB.initialized);
        } else if ("user.logout".equals(event.getEvent())) {
            verb = new LRS_Verb(SAKAI_VERB.exited);
        } else if ("content.read".equals(event.getEvent())) {
            verb = new LRS_Verb(SAKAI_VERB.interacted);
        }
        return verb;
    }

    private LRS_Object getEventObject(Event event) {
        LRS_Object object = null;
        if ("user.login".equals(event.getEvent()) || "user.logout".equals(event.getEvent())) {
            object = new LRS_Object(serverConfigurationService.getPortalUrl(), "session");
        } else if ("content.read".equals(event.getEvent())) {
            object = new LRS_Object(serverConfigurationService.getAccessUrl()+event.getResource(), "read");
        }
        return object;
    }

    private String getEventOrigin(Event event) {
        String origin = null;
        if ("user.login".equals(event.getEvent()) || "user.logout".equals(event.getEvent())) {
            origin = ORIGIN_SAKAI_SYSTEM;
        } else if ("content.read".equals(event.getEvent())) {
            origin = ORIGIN_SAKAI_CONTENT;
        } else {
            origin = event.getEvent();
        }
        return origin;
    }



    ApplicationContext applicationContext;
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    EventTrackingService eventTrackingService;
    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    ServerConfigurationService serverConfigurationService;
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    SessionManager sessionManager;
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    UserDirectoryService userDirectoryService;
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

}
