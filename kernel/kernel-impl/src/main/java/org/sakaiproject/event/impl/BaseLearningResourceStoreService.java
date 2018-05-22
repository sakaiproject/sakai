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

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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
//@Aspect
@Slf4j
public class BaseLearningResourceStoreService implements LearningResourceStoreService, ApplicationContextAware {

    private static final String ORIGIN_SAKAI_SYSTEM = "sakai.system";
    private static final String ORIGIN_SAKAI_CONTENT = "sakai.resources";

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
            Map<String, LearningResourceStoreProvider> beans = applicationContext.getBeansOfType(LearningResourceStoreProvider.class);
            for (LearningResourceStoreProvider lrsp : beans.values()) {
                if (lrsp != null) { // should not be null but this avoids killing everything if it is
                    registerProvider(lrsp);
                }
            }
            log.info("LRS Registered {} LearningResourceStoreProviders from the Spring AC during service INIT", beans.size());
        } else {
            log.info("LRS did not search for existing LearningResourceStoreProviders in the system (ac={}, enabled={})", applicationContext, isEnabled());
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
                log.info("LRS found {} origin filters: {}", originFilters.size(), originFilters);
            }
        }
        if (isEnabled() && eventTrackingService != null) {
            this.experienceObserver = new ExperienceObserver(this);
            eventTrackingService.addLocalObserver(this.experienceObserver);
            log.info("LRS registered local event tracking observer");
        }
        log.info("LRS INIT: enabled={}", isEnabled());
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
                    log.warn("LRS statement from ({}) skipped because there are no providers to process it: {}", origin, statement);
                }
            } else {
                // filter out certain tools and statement origins
                boolean skip = false;
                if (originFilters != null && !originFilters.isEmpty()) {
                    origin = StringUtils.trimToNull(origin);
                    if (origin != null && originFilters.contains(origin)) {
                        log.debug("LRS statement skipped because origin ({}) matches the originFilter", origin);
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
                    } else if (StringUtils.isNotBlank(statement.getRawJSON())) {
                        valid = true;
                    }
                    if (valid) {
                        // process this statement
                        log.debug("LRS statement being processed, origin={}, statement={}", origin, statement);
                        for (LearningResourceStoreProvider lrsp : providers.values()) {
                            // run the statement processing in a new thread
                            String threadName = "LRS_"+lrsp.getID();
                            Thread t = new Thread(new RunStatementThread(lrsp, statement), threadName); // each provider has it's own thread
                            t.setDaemon(true); // allow this thread to be killed when the JVM is shutdown
                            t.start();
                        }
                    } else {
                        log.warn("Invalid statment registered, statement will not be processed: {}", statement);
                    }
                } else {
                    log.debug("LRS statement being skipped, origin={}, statement={}", origin, statement);
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
                log.error("LRS Failure running LRS statement in provider ({}): statement=({}): ", lrsp.getID(), statement, e);
            }
        }
    };

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


    /* AOP processing of events
     * NOTE: this probably won't end up working - we might need to just use the service directly
     * mostly because the types of the objects (like Assignment) cannot be pulled into the
     * kernel because it would make the kernel depend on those projects,
     * but I think we will need access to the values in those objects and getting them
     * all via reflection is going to be a too much effort -AZ
     * 
     * Alos, for runtime weaving (load-time) we have to add this to spring and put aspect j into shared
     * <aop:aspectj-autoproxy/>
     * <bean class="org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator" />
     */

    /* 
     * Leaving this in so we don't forget that it was attempted
     * 
    @Before("execution(* org.sakaiproject.event.impl.*Service.init())")
    public void sampleBeforeInit() {
        log.info("AOP Before init()");
    }

    @AfterReturning("execution(* org.sakaiproject.event.impl.*Service.init(..))")
    public void sampleAfterInit() {
        log.info("AOP After init()");
    }

    @Around("execution(* *.setUsageSessionServiceSql(String)) && args(vendor)")
    public void sampleAround(ProceedingJoinPoint thisJoinPoint, String vendor) throws Throwable {
        log.info("AOP Around (before) setUsageSessionServiceSql("+vendor+")");
        thisJoinPoint.proceed(new Object[] {vendor});
        log.info("AOP Around (after) setUsageSessionServiceSql("+vendor+")");
    }
    */


    // EVENT conversion to statements

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
                if (statement != null && statement.isPopulated()) {
                    this.lrss.registerStatement(statement, origin);
                }
            }
        }
    }

    /**
     * Convenience method to turn events into statements,
     * this can only work for simple events where there is little student interaction
     * @param event an Event
     * @return a statement if one can be formed OR null if not
     */
    private LRS_Statement getEventStatement(Event event) {
        //If the event already has the statement set, just use that
        LRS_Statement statement=null;
        LRS_Verb verb=null;
        LRS_Actor actor=null;
        LRS_Context context=null;
        LRS_Object object=null;
        LRS_Result result = null;
        if (event.getLrsStatement() != null) {
            statement =  event.getLrsStatement();
            //If the statement is fully populated (with context) nothing left to do
            if (statement.isPopulated() && statement.getContext() != null) {
                return statement;
            }
            verb=statement.getVerb();
            actor=statement.getActor();
            context=statement.getContext();
            object=statement.getObject();
            result=statement.getResult();

        }
        try {
            //If verb not set try to get it from the event
            if (verb == null) {
                verb = getEventVerb(event);
            }
            // If object not set try to get it from the event
            if (object == null) {
                object = getEventObject(event);
            }
            //If actor is not null try to get it from the event
            if (actor == null) {
                actor = getEventActor(event);
            }
            //If context is not set get it from the event
            if (context == null) {
                context = getEventContext(event);
            }
            statement = new LRS_Statement(actor, verb, object,result,context);
        } catch (Exception e) {
            log.debug("LRS Unable to convert event ({}) into statement.", event, e);
            statement = null;
        }
        return statement;
    }
    /* (non-Javadoc)
     * @see org.sakaiproject.event.api.LearningResourceStoreService#getActor(String)
     */
    public LRS_Actor getActor(String userId) {
        LRS_Actor actor = null;
        User user = null;
        try {
            user = this.userDirectoryService.getUser(userId);
        } catch (UserNotDefinedException e) {
            user = null;
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
                log.debug("LRS Actor: No email set for user ({}), using generated one: {}", user.getId(), actorEmail);
            }
            actor = new LRS_Actor(actorEmail);
            if (StringUtils.isNotEmpty(user.getDisplayName())) {
                actor.setName(user.getDisplayName());
            }
            // set actor account object
            actor.setAccount(user.getEid(), serverConfigurationService.getServerUrl());
            // TODO implement OpenID support
        }
        return actor;
    }
    
    /* (non-Javadoc)
     * @see org.sakaiproject.event.api.LearningResourceStoreService#getEventActor(org.sakaiproject.event.api.Event)
     */
    public LRS_Actor getEventActor(Event event) {
    	return getActor(event);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.event.api.LearningResourceStoreService#getActor(org.sakaiproject.event.api.Event)
     */
    public LRS_Actor getActor(Event event) {
    	String userId = null;
    	if (event != null) {
    		userId = event.getUserId();
    	}
    	if (userId == null && event != null && event.getSessionId() != null) {
    		Session session = this.sessionManager.getSession(event.getSessionId());
    		if (session != null) {
    			userId = session.getUserId();
    		}
    	}
    	return getActor(userId);
    }

    /**
     * @param event
     * @return a valid context for the event (based on the site/course) OR null if one cannot be determined
     */
    private LRS_Context getEventContext(Event event) {
        LRS_Context context = null;
        if (event != null && event.getContext() != null) {
            String eventContext = event.getContext();
            String e = StringUtils.lowerCase(event.getEvent());
            // NOTE: wiki puts /site/ in front of the context, others are just the site_id
            if (StringUtils.startsWith(e, "wiki")) {
                eventContext = StringUtils.replace(eventContext, "/site/", "");
            }
            // the site is the parent for all event activities
            context = new LRS_Context("parent", serverConfigurationService.getPortalUrl()+"/site/"+eventContext);
        }
        return context;
    }

    private LRS_Verb getEventVerb(Event event) {
        LRS_Verb verb = null;
        if (event != null) {
            String e = StringUtils.lowerCase(event.getEvent());
            if ("user.login".equals(e)) {
                verb = new LRS_Verb(SAKAI_VERB.initialized);
            } else if ("user.logout".equals(e)) {
                verb = new LRS_Verb(SAKAI_VERB.exited);
            } else if ("annc.read".equals(e)) {
                verb = new LRS_Verb(SAKAI_VERB.experienced);
            } else if ("calendar.read".equals(e)) {
                verb = new LRS_Verb(SAKAI_VERB.experienced);
            } else if ("chat.new".equals(e)) {
                verb = new LRS_Verb(SAKAI_VERB.responded);
            } else if ("chat.read".equals(e)) {
                verb = new LRS_Verb(SAKAI_VERB.experienced);
            } else if ("content.read".equals(e)) {
                verb = new LRS_Verb(SAKAI_VERB.interacted);
            } else if ("content.new".equals(e) || "content.revise".equals(e)) {
                verb = new LRS_Verb(SAKAI_VERB.shared);
            } else if ("gradebook.read".equals(e)) {
                verb = new LRS_Verb(SAKAI_VERB.experienced);
            } else if ("lessonbuilder.page.read".equals(e) || "lessonbuilder.item.read".equals(e)) {
                verb = new LRS_Verb(SAKAI_VERB.experienced);
            } else if ("news.read".equals(e)) {
                verb = new LRS_Verb(SAKAI_VERB.experienced);
            } else if ("podcast.read".equals(e) || "podcast.read.public".equals(e) || "podcast.read.site".equals(e)) {
                verb = new LRS_Verb(SAKAI_VERB.experienced);
            } else if ("syllabus.read".equals(e)) {
                verb = new LRS_Verb(SAKAI_VERB.experienced);
            } else if ("webcontent.read".equals(e)) {
                verb = new LRS_Verb(SAKAI_VERB.experienced);
            } else if ("wiki.new".equals(e)) {
                verb = new LRS_Verb(SAKAI_VERB.initialized);
            } else if ("wiki.revise".equals(e)) {
                verb = new LRS_Verb(SAKAI_VERB.shared);
            } else if ("wiki.read".equals(e)) {
                verb = new LRS_Verb(SAKAI_VERB.experienced);
            }
        }
        return verb;
    }

    private LRS_Object getEventObject(Event event) {
        LRS_Object object = null;
        if (event != null) {
            String e = StringUtils.lowerCase(event.getEvent());
            /*
             * NOTE: use the following terms "view", "add", "edit", "delete"
             */
            if ("user.login".equals(e)) {
                object = new LRS_Object(serverConfigurationService.getPortalUrl(), "session-started");
            } else if ("user.logout".equals(e)) {
                object = new LRS_Object(serverConfigurationService.getPortalUrl()+"/logout", "session-ended");
            } else if ("annc.read".equals(e)) {
                object = new LRS_Object(serverConfigurationService.getPortalUrl() + event.getResource(), "view-announcement");
            } else if ("calendar.read".equals(e)) {
                object = new LRS_Object(serverConfigurationService.getPortalUrl() + event.getResource(), "view-calendar");
            } else if ("chat.new".equals(e) || "chat.read".equals(e)) {
                object = new LRS_Object(serverConfigurationService.getPortalUrl() + event.getResource(), "view-chats");
            } else if ("content.read".equals(e)) {
                object = new LRS_Object(serverConfigurationService.getAccessUrl() + event.getResource(), "view-resource");
            } else if ("content.new".equals(e)) {
                object = new LRS_Object(serverConfigurationService.getAccessUrl() + event.getResource(), "add-resource");
            } else if ("content.revise".equals(e)) {
                object = new LRS_Object(serverConfigurationService.getAccessUrl() + event.getResource(), "edit-resource");
            } else if ("gradebook.read".equals(e)) {
                object = new LRS_Object(serverConfigurationService.getPortalUrl() + event.getResource(), "view-grades");
            } else if ("lessonbuilder.page.read".equals(e) || "lessonbuilder.item.read".equals(e)) {
                object = new LRS_Object(serverConfigurationService.getPortalUrl() + event.getResource(), "view-lesson");
            } else if ("news.read".equals(e)) {
                object = new LRS_Object(serverConfigurationService.getPortalUrl() + event.getResource(), "view-news");
            } else if ("podcast.read".equals(e) || "podcast.read.public".equals(e) || "podcast.read.site".equals(e)) {
                object = new LRS_Object(serverConfigurationService.getPortalUrl() + event.getResource(), "view-podcast");
            } else if ("syllabus.read".equals(e)) {
                object = new LRS_Object(serverConfigurationService.getPortalUrl() + event.getResource(), "view-syllabus");
            } else if ("webcontent.read".equals(e)) {
                object = new LRS_Object(serverConfigurationService.getPortalUrl() + event.getResource(), "view-web-content");
            } else if ("wiki.new".equals(e)) {
                object = new LRS_Object(serverConfigurationService.getPortalUrl() + event.getResource(), "add-wiki-page");
            } else if ("wiki.revise".equals(e)) {
                object = new LRS_Object(serverConfigurationService.getPortalUrl() + event.getResource(), "edit-wiki-page");
            } else if ("wiki.read".equals(e)) {
                object = new LRS_Object(serverConfigurationService.getPortalUrl() + event.getResource(), "view-wiki-page");
            }
        }
        return object;
    }

    private String getEventOrigin(Event event) {
        String origin = null;
        if (event != null) {
            String e = StringUtils.lowerCase(event.getEvent());
            if ("user.login".equals(e) || "user.logout".equals(e)) {
                origin = ORIGIN_SAKAI_SYSTEM;
            } else if ("annc.read".equals(e)) {
                origin = "announcement";
            } else if ("calendar.read".equals(e)) {
                origin = "calendar";
            } else if ("chat.new".equals(e) || "chat.read".equals(e)) {
                origin = "chat";
            } else if ("content.read".equals(e) || "content.new".equals(e) || "content.revise".equals(e)) {
                origin = ORIGIN_SAKAI_CONTENT;
            } else if ("gradebook.read".equals(e)) {
                origin = "gradebook";
            } else if ("lessonbuilder.page.read".equals(e) || "lessonbuilder.item.read".equals(e)) {
                origin = "lessonbuilder";
            } else if ("news.read".equals(e)) {
                origin = "news";
            } else if ("podcast.read".equals(e) || "podcast.read.public".equals(e) || "podcast.read.site".equals(e)) {
                origin = "podcast";
            } else if ("syllabus.read".equals(e)) {
                origin = "syllabus";
            } else if ("webcontent.read".equals(e)) {
                origin = "webcontent";
            } else if ("wiki.new".equals(e) || "wiki.revise".equals(e) || "wiki.read".equals(e)) {
                origin = "rwiki";
            } else {
                origin = e;
            }
        }
        return origin;
    }


    // INJECTION

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
