/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2014 The Apereo Foundation.
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

import com.google.common.collect.MapMaker;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.azeckoski.reflectutils.ConstructorUtils;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.tool.api.*;
import org.sakaiproject.tool.api.Breakdownable.BreakdownableSize;

/**
 * Implements the handling of Session and other bean breakdowns and rebuilds
 *
 * Session related handling config settings (defaults shown):
 * session.cluster.replication=false
 * session.cluster.minSecsOldToStore=20
 * session.cluster.minSecsBetweenStores=10
 * session.cluster.minSecsAfterRebuild=30
 *
 * NOTE that org.sakaiproject.tool.impl.RebuildBreakdownService.cache must be set to a distributed store (like terracotta)
 */
@Slf4j
public class RebuildBreakdownServiceImpl implements RebuildBreakdownService {
    final static String SPECIAL_SESSION_KEY_PREFIX                  = "_sakai_session_";
    final static String SESSION_USER_ID_KEY                         = SPECIAL_SESSION_KEY_PREFIX+"UserId";
    final static String SESSION_USER_EID_KEY                        = SPECIAL_SESSION_KEY_PREFIX+"UserEid";
    final static String SESSION_CREATION_TIME_KEY                   = SPECIAL_SESSION_KEY_PREFIX+"CreationTime";
    final static String SESSION_LAST_ACCESSED_TIME_KEY              = SPECIAL_SESSION_KEY_PREFIX+"LastAccessedTime";
    final static String SESSION_CURRENT_TOOLSESSION_PLACEMENT_KEY   = SPECIAL_SESSION_KEY_PREFIX+"CurrentToolSessionPlacement";
    final static String SESSION_CURRENT_USAGESESSION_KEY            = SPECIAL_SESSION_KEY_PREFIX+"CurrentUsegeSessionId";

    final static String SESSION_TOOL_SESSIONS_KEY                   = SPECIAL_SESSION_KEY_PREFIX+"ToolSessions";
    final static String SESSION_CONTEXT_SESSIONS_KEY                = SPECIAL_SESSION_KEY_PREFIX+"ContextSessions";

    final static String SESSION_LAST_BREAKDOWN_KEY                  = SPECIAL_SESSION_KEY_PREFIX+"LastBreakdownTime";
    final static String SESSION_LAST_REBUILD_KEY                    = SPECIAL_SESSION_KEY_PREFIX+"LastRebuildTime";

    private final int minSecondsBetweenStoresDefault = 10;
    private final int minSecondsAfterRebuildDefault = 30;
    private final int smallestMinSecondsBetweenStores = 1;
    private final int minAgeToStoreSecondsDefault = 10;
    /**
     * sessionClassWhitelist contains a list of classnames that are known safe to serialize
     * and store in sessions (since they can be deserialized safely)
     */
    private Set<String> sessionClassWhitelist;
    /**
     * a list of all attributes in sessions (MySession or MyLittleSession) that should be skipped
     * when doing session breakdowns or rebuilds
     */
    private Set<String> sessionAttributeBlacklist;
    /**
     * Map that contains all the Breakdownable which were manually registered from the services
     */
    private ConcurrentMap<String, Breakdownable> breakdownableHandlers;
    private MemoryService memoryService;
    private SessionManager sessionManager;
    private ServerConfigurationService serverConfigurationService;
    private UsageSessionService usageSessionService;
    private Cache sessionCache; // Replicated, long lived
    private Cache stashingCache; // NON-replicated, short lived

    public void init() {
        log.info("INIT: session clustering=" + isSessionClusteringEnabled());
        if (isSessionClusteringEnabled()) {
            sessionCache = memoryService.newCache("org.sakaiproject.tool.impl.RebuildBreakdownService.cache");

            stashingCache = memoryService.newCache("org.sakaiproject.tool.impl.RebuildBreakdownService.stash");

            sessionClassWhitelist = new HashSet<String>(4); // number should match items count below
            sessionClassWhitelist.add(Locale.class.getName());
            sessionClassWhitelist.add("org.sakaiproject.event.api.SimpleEvent");
            sessionClassWhitelist.add("org.sakaiproject.authz.api.SimpleRole");
            sessionClassWhitelist.add("org.apache.commons.lang.mutable.MutableLong");

            sessionAttributeBlacklist = new HashSet<String>(6); // number should match items count below
            sessionAttributeBlacklist.add(SESSION_LAST_BREAKDOWN_KEY);
            sessionAttributeBlacklist.add(SESSION_LAST_REBUILD_KEY);
            /* from BasePreferencesService.ATTR_PREFERENCE_IS_NULL
             * This controls whether the session cached version of prefs is reloaded or assumed to be populated,
             * when it is true the processing assumes it is populated (very weird logic and dual-caching)
             */
            sessionAttributeBlacklist.add("attr_preference_is_null");
            /* from BasePreferencesService.ATTR_PREFERENCE
             * rebuild this manually on demand from the cache instead of storing it
             */
            sessionAttributeBlacklist.add("attr_preference");
            /** should be re-detected on rebuild of the session */
            sessionAttributeBlacklist.add("is_mobile_device");
            /** this is normally only set on login, we handle it specially on breakdown and rebuild */
            sessionAttributeBlacklist.add(UsageSessionService.USAGE_SESSION_KEY);
        }
        /* Create a map with weak references to the values */
        breakdownableHandlers = new MapMaker().weakValues().makeMap();
    }

    @Override
    public boolean storeSession(Session s, HttpServletRequest request) {
        if (!isSessionClusteringEnabled()) {
            if (log.isDebugEnabled()) log.debug("Session Clustering not enabled");
            return false;
        }
        if (s == null) {
            throw new IllegalArgumentException("session cannot be null");
        }
        if (!(s instanceof MySession)) {
            // yes, this is kind of dumb but unless we change the class structures it is necessary
            throw new IllegalArgumentException("session ("+s.getId()+") MUST be a MySession implementation");
        }
        MySession ms = (MySession) s;
        if (!isSessionValid(ms)) {
            if (log.isDebugEnabled()) log.debug("Session ("+s.getId()+") not valid for clustering, not a MySession");
            return false;
        }
        if (request == null) {
            request = ms.currentRequest();
        }
        if (!isSessionBreakdownAllowed(ms, request)) {
            // logging in the check method itself
            return false;
        }

        String sessionId = ms.getId();
        if (log.isDebugEnabled()) log.debug("RebuildBreakdownServiceImpl.storeSession, for sessionId: [" + sessionId + "]");
        Map<String,Serializable> sessionMap = new HashMap<String,Serializable>();
        storeSessionSpecialAttributes(ms, sessionMap);
        storeSessionAttributes(ms, sessionMap);
        if (log.isDebugEnabled()) log.debug("RebuildBreakdownServiceImpl.storeSession, for sessionId: [" + sessionId + "] completed");
        sessionCache.put(sessionId, sessionMap);
        ms.setAttribute(SESSION_LAST_BREAKDOWN_KEY, System.currentTimeMillis());
        return true;
    }

    @Override
    public boolean rebuildSession(Session s) {
        if (!isSessionClusteringEnabled()) {
            if (log.isDebugEnabled()) log.debug("Session Clustering not enabled");
            return false;
        }
        if (s == null) {
            throw new IllegalArgumentException("session cannot be null");
        }
        if (!(s instanceof MySession)) {
            // yes, this is kind of dumb but unless we change the class structures it is necessary
            throw new IllegalArgumentException("session ("+s.getId()+") MUST be a MySession implementation");
        }
        MySession ms = (MySession) s;
        if (!ms.isValid() || ms.isInactive()) {
            throw new IllegalArgumentException("session cannot be invalid (valid="+ms.isValid()+") or inactive (inactive="+ms.isInactive()+")");
        }
        boolean rebuilt;
        String sessionId = ms.getId();
        @SuppressWarnings("unchecked")
        Map<String, Serializable> sessionMap = (Map<String, Serializable>) sessionCache.get(sessionId);
        if (sessionMap == null || sessionMap.isEmpty()) {
            // no data available to rebuild this session
            if (log.isDebugEnabled()) log.debug("rebuildSession, sessionId: [" + sessionId + "] data not found in store, cannot rebuild");
            rebuilt = false;
        } else {
            // REBUILD the session
            if (log.isDebugEnabled()) log.debug("rebuildSession, sessionId: [" + sessionId + "] from map("+sessionMap.size()+")");
            processMySessionMap(ms, sessionMap);
            // now that the session is fully rebuilt we need to make sure we reactivate it and make it current
            ms.setActive();
            sessionManager.setCurrentSession(ms); // this has to be set for some of the stuff below to work

            // SPECIAL cases related to the rebuild
            // Repopulate the current ToolSession
            if (sessionMap.containsKey(SESSION_CURRENT_TOOLSESSION_PLACEMENT_KEY)) {
                String currentToolSessionPlacementId = (String) sessionMap.get(SESSION_CURRENT_TOOLSESSION_PLACEMENT_KEY);
                if (currentToolSessionPlacementId != null) {
                    ToolSession ts = ms.getToolSession(currentToolSessionPlacementId);
                    if (ts != null) {
                        sessionManager.setCurrentToolSession(ts);
                        if (log.isDebugEnabled()) log.debug("rebuildSession, sessionId: [" + sessionId + "], updated current tool session("+ts.getId()+") for placement: "+currentToolSessionPlacementId);
                    }
                }
            }
            // Repopulate the current UsageSession and reassign to this server
            if (sessionMap.containsKey(SESSION_CURRENT_USAGESESSION_KEY)) {
                String currentUsageSessionId = (String) sessionMap.get(SESSION_CURRENT_USAGESESSION_KEY);
                UsageSession us = usageSessionService.getSession(currentUsageSessionId);
                if (us == null) {
                    // likely have to create a new one, warn and then attempt it (maybe should have failed here instead)
                    // TODO UsageSession startSession(String userId, String remoteAddress, String userAgent)
                    if (log.isDebugEnabled()) log.debug("rebuildSession, sessionId: [" + sessionId + "], made new usage session: "+currentUsageSessionId);
                }
                // NOTE that this usageSession will be realigned to the current server (as needed) by code in the RequestFilter
                ms.setAttribute(UsageSessionService.USAGE_SESSION_KEY, us);
                if (log.isDebugEnabled()) log.debug("rebuildSession, sessionId: [" + sessionId + "], reloaded usage session: "+currentUsageSessionId);
            }

            ms.setAttribute(SESSION_LAST_REBUILD_KEY, System.currentTimeMillis());
            rebuilt = true;
        }
        log.info("RBS rebuildSession, sessionId: [" + sessionId + "] complete, rebuilt: "+rebuilt);
        return rebuilt;
    }

    @Override
    public boolean isSessionHandlingEnabled() {
        return isSessionClusteringEnabled();
    }

    @Override
    public void purgeSessionFromStorageById(String sessionId) {
        if (this.sessionCache != null) {
            this.sessionCache.remove(sessionId);
        }
    }

    @Override
    public StoreableBreakdown retrieveCallbackSessionData(String attributeKey, String sessionId) {
        if (attributeKey == null) {
            throw new IllegalArgumentException("attributeKey must be set");
        }
        if (sessionId == null) {
            try {
                sessionId = sessionManager.getCurrentToolSession().getId();
            } catch (Exception e) {
                throw new IllegalStateException("no current tool session found: "+e, e);
            }
        }
        String storedKey = StoreableBreakdown.makeStashKey(sessionId, attributeKey);
        StoreableBreakdown data = (StoreableBreakdown) this.stashingCache.get(storedKey);
        if (data != null) {
            // clears the cache after retrieving the data if successful
            this.stashingCache.remove(storedKey);
        }
        return data;
    }

    // SUPPORT METHODS

    @Override
    public void registerBreakdownHandler(Breakdownable<?> handler) {
        if (log.isDebugEnabled()) log.debug("registering a BreakdownableHandler");
        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }
        try {
            String handlerName = handler.defineHandledClass().getName();
            if (log.isDebugEnabled()) log.debug("handler: [" + handlerName + "]");
            handler.defineClassLoader(); // just call to make sure it is not going to die
            breakdownableHandlers.putIfAbsent(handlerName, handler);
            if (log.isDebugEnabled()) log.debug("breakdownableHandlers now has [" + breakdownableHandlers.size() + "]");
        } catch (Exception e) {
            throw new IllegalStateException("Failure in the handler that was attempting to be registered: "+handler+" :: "+e, e);
        }
    }

    @Override
    public void unregisterBreakdownHandler(String fullClassName) {
        if (fullClassName == null) {
            throw new IllegalArgumentException("fullClassName cannot be null");
        }
        breakdownableHandlers.remove(fullClassName);
    }

    @Override
    public StoreableBreakdown breakdownObject(Object breakdownable, BreakdownableSize size) {
        if (breakdownable == null) {
            throw new IllegalArgumentException("breakdownable cannot be null");
        }
        if (size == null) {
            size = BreakdownableSize.TINY; // default to the smallest one
        }
        String className = breakdownable.getClass().getName();
        @SuppressWarnings("unchecked")
        Breakdownable<Object> handler = breakdownableHandlers.get(className);
        if (handler == null) {
            throw new IllegalStateException("No Breakdownable handler found for object class: "+className);
        }
        StoreableBreakdown sb;
        try {
            Serializable data = handler.makeBreakdown(breakdownable, size);
            sb = new StoreableBreakdown(className, size, data);
        } catch (Exception e) {
            sb = null;
            log.warn("Failure attempting to breakdown object (to size="+size+"): "+breakdownable+" :: "+e, e);
        }
        return sb;
    }

    @Override
    public Object rebuildObject(String className, BreakdownableSize size, Serializable data) {
        if (className == null) {
            throw new IllegalArgumentException("rebuildObject className cannot be null");
        }
        if (data == null) {
            throw new IllegalArgumentException("rebuildObject data cannot be null");
        }
        if (size == null) {
            size = BreakdownableSize.TINY; // Default
        }
        ClassLoader currentCL = Thread.currentThread().getContextClassLoader(); // likely the kernel CL (shared)
        Object rebuilt = null;
        try {
            Breakdownable<?> handler = breakdownableHandlers.get(className);
            if (handler != null) {
                // only rebuild if we have a handler
                ClassLoader objectCL;
                try {
                    objectCL = handler.defineClassLoader();
                } catch (Exception e) {
                    objectCL = null;
                    log.warn("Failure in defineClassLoader: "+e);
                }
                if (objectCL == null) {
                    objectCL = handler.getClass().getClassLoader();
                }
                Thread.currentThread().setContextClassLoader(objectCL);
                rebuilt = handler.doRebuild(data, size);
            }
        } catch (Exception e) {
            log.warn("Failure ("+e.getMessage()+") attempting to rebuild object: class="+className+", size:"+size+", data: "+data+" :: "+e, e);
        } finally {
            // reset back to the current CL
            Thread.currentThread().setContextClassLoader(currentCL);
        }
        return rebuilt;
    }

    /**
     * storeSessionSpecialAttributes() adds session attributes that need to be stored in the
     * cluster in the sessionMap.  These are attributes that are not part of session.getAttributes().
     * For example, it includes the sessions User EID and User ID, as well as data about the session's
     * ToolSessions and ContextSessions if the session is of type MySession.
     * @param s session that will have special attributes stored
     * @param sessionMap the Map that will contain the attributes that will eventually stored in the cluster
     */
    private void storeSessionSpecialAttributes(MySession s, Map<String, Serializable> sessionMap) {
        // special case ids from the session
        sessionMap.put(SESSION_USER_ID_KEY, s.getUserId());
        sessionMap.put(SESSION_USER_EID_KEY, s.getUserEid());
        sessionMap.put(SESSION_CREATION_TIME_KEY, s.getCreationTime());
        sessionMap.put(SESSION_LAST_ACCESSED_TIME_KEY, s.getLastAccessedTime());
        // special cases related to the session
        ToolSession ts = sessionManager.getCurrentToolSession();
        if (ts != null) {
            sessionMap.put(SESSION_CURRENT_TOOLSESSION_PLACEMENT_KEY, ts.getPlacementId());
        }
        UsageSession us = usageSessionService.getSession();
        if (us != null) {
            sessionMap.put(SESSION_CURRENT_USAGESESSION_KEY, us.getId());
        }
        // sub-sessions
        Map<String, MyLittleSession> toolSessions = s.m_toolSessions;
        if (toolSessions != null && toolSessions.size() > 0) {
            Map<String, Map<String, Serializable>> toolSessionMaps = new HashMap<String, Map<String, Serializable>>();
            for (Entry<String, MyLittleSession> entry: toolSessions.entrySet()) {
                String toolSessionId = entry.getKey();
                MyLittleSession toolSession = entry.getValue();
                if (!storeSubSession(toolSession)) {
                    if (log.isDebugEnabled()) log.debug("store toolSessions skipping subSession: "+toolSession);
                    continue;
                }
                Map<String, Serializable> toolSessionMap = new HashMap<String, Serializable>();
                storeSessionAttributes(toolSession, toolSessionMap);
                if (toolSessionMap.isEmpty()) {
                    if (log.isDebugEnabled()) log.debug("store toolSessions skipping subSession (no attributes included): "+toolSession);
                    continue;
                }
                toolSessionMap.put(SESSION_CREATION_TIME_KEY, toolSession.getCreationTime());
                toolSessionMap.put(SESSION_LAST_ACCESSED_TIME_KEY, toolSession.getLastAccessedTime());
                toolSessionMaps.put(toolSessionId, toolSessionMap);
            }
            sessionMap.put(SESSION_TOOL_SESSIONS_KEY, (Serializable) toolSessionMaps);
        }

        Map<String, MyLittleSession> contextSessions = s.m_contextSessions;
        if (contextSessions != null && contextSessions.size() > 0) {
            Map<String, Map<String, Serializable>> contextSessionMaps = new HashMap<String, Map<String, Serializable>>();
            for (Entry<String, MyLittleSession> entry: contextSessions.entrySet()) {
                String contextSessionId = entry.getKey();
                MyLittleSession contextSession = entry.getValue();
                if (!storeSubSession(contextSession)) {
                    if (log.isDebugEnabled()) log.debug("store contextSessions skipping subSession: "+contextSession);
                    continue;
                }
                Map<String, Serializable> contextSessionMap = new HashMap<String, Serializable>();
                storeSessionAttributes(contextSession, contextSessionMap);
                if (contextSessionMap.isEmpty()) {
                    if (log.isDebugEnabled()) log.debug("store contextSessions skipping subSession (no attributes included): "+contextSession);
                    continue;
                }
                contextSessionMap.put(SESSION_CREATION_TIME_KEY, contextSession.getCreationTime());
                contextSessionMap.put(SESSION_LAST_ACCESSED_TIME_KEY, contextSession.getLastAccessedTime());
                contextSessionMaps.put(contextSessionId, contextSessionMap);
            }
            sessionMap.put(SESSION_CONTEXT_SESSIONS_KEY, (Serializable) contextSessionMaps);
        }
    }

    /**
     * Checks if a subSession should be processed and stored in the distributed store
     *
     * @param subSession the sub session which we are checking to see if it should be stored
     * @return true if the session should be stored OR false if it should be skipped
     */
    private boolean storeSubSession(MyLittleSession subSession) {
        if (subSession == null) {
            return false;
        }
        if (MyLittleSession.TYPE_TOOL.equals(subSession.getSessionType())) {
            if (subSession.getSessionToolId() == null) {
                // don't store tool sessions if they have no toolId
                return false;
            } else if (StringUtils.contains(subSession.getSessionToolId(), "synoptic")) {
                // don't store tool sessions for synoptic tools
                return false;
            }
        }
        if (subSession.getContextId() != null && subSession.getContextId().equals(serverConfigurationService.getGatewaySiteId())) {
            // do not store context sessions if they are related to the gateway site
            return false;
        }
        if (!subSession.getAttributeNames().hasMoreElements()) {
            // don't store empty sessions
            return false;
        }
        return true;
    }

    /**
     * storeSessionAttributes() puts all of the attributes that are available from session.getAttribute()
     * into the sessionMap which will be stored in the cluster
     * @param s Session that is being stored
     * @param sessionMap the Map that will contain the attributes that will eventually stored in the cluster
     */
    private void storeSessionAttributes(HttpSession s, Map<String, Serializable> sessionMap) {
        @SuppressWarnings("unchecked")
        Enumeration<String> keys = s.getAttributeNames();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (sessionAttributeBlacklist.contains(key)) {
                // skip processing on this key
                continue;
            }
            if (log.isDebugEnabled()) log.debug("attempting to store session attribute key [" + key + "] in cache");
            Object object = s.getAttribute(key);
            Serializable toStore = serializeSessionAttribute(object);
            // now store it if we were successful
            if (toStore != null) {
                sessionMap.put(key, toStore);
                if (log.isDebugEnabled()) log.debug("RebuildBreakdownServiceImpl.storeSession, putting key [" + key + "], class: [" + object.getClass().getName() + "], value: [" + object + "]");
            }
        }
    }

    /**
     * serializeSessionAttribute() takes an object and returns a Serialized version of it, if
     * the object can be serialized.  Objects of type StoreableBreakdown are serialized here,
     * as well as objects that are either primitives or are part of a known list of classes
     * that can be serialized
     * @param object Object to be serialized
     * @return a Serialized version of the Object, or null if the object cannot be serialized
     */
    private Serializable serializeSessionAttribute(Object object) {
        // Convert object into something that can be stored
        Serializable toStore;
        if (object == null) {
            toStore = null;
        } else {
            try {
                @SuppressWarnings("UnnecessaryLocalVariable")
                StoreableBreakdown sb = breakdownObject(object, BreakdownableSize.TINY);
                toStore = sb;
            } catch (IllegalStateException e) {
                // no handler for this type of object
                if (isObjectSimple(object)) {
                    toStore = (Serializable) object;
                } else if (sessionClassWhitelist.contains(object.getClass().getName())) {
                    toStore = (Serializable) object;
                } else {
                    // do not store, maybe log trace or debug message
                    toStore = null;
                }
            }
        }
        return toStore;
    }

    /**
     * iterates through all of the items in the sessionMap and
     * sets the relevant properties of the session.  Some session attributes will be
     * set directly (via session.setAttribute()).  Others will require special handling,
     * such as ToolSessions contained within a Session, or session properties, such as User ID
     * or User EID.
     * @param s MySession that will be updated
     * @param sessionMap the map of attributes that will be applied to the session.
     */
    private void processMySessionMap(MySession s, Map<String, Serializable> sessionMap) {
        if (sessionMap != null && !sessionMap.isEmpty()) {
            for (Entry<String, Serializable> entry : sessionMap.entrySet()) {
                if (!handleMySessionSpecialKey(s, entry.getKey(), entry.getValue())) {
                    handleSessionStandardKey(s, entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * iterates through all of the items in the sessionMap and
     * sets the relevant properties of the session.  Some session attributes will be
     * set directly (via session.setAttribute()).  Others will require special handling,
     * such as ToolSessions contained within a Session, or session properties, such as User ID
     * or User EID.
     * @param s ToolSession or ContextSession that will be updated
     * @param sessionMap the map of attributes that will be applied to the session.
     */
    private void processMLSessionMap(MyLittleSession s, Map<String, Serializable> sessionMap) {
        if (sessionMap != null && !sessionMap.isEmpty()) {
            for (Entry<String, Serializable> entry : sessionMap.entrySet()) {
                if (!handleMLSessionSpecialKey(s, entry.getKey(), entry.getValue())) {
                    handleSessionStandardKey(s, entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * performs any special processing for keys found in the session cache.  These keys could
     * be attributes of the session itself, or objects like ToolSessions or ContextSessions
     * @param s Session being processed
     * @param key name of the object stored in the session cache
     * @param object the object that must be handled separately
     * @return true if the key was specially processed, false if key uses default processing
     */
    private boolean handleMySessionSpecialKey(MySession s, String key, Serializable object) {
        if (object != null) {
            if (SESSION_USER_ID_KEY.equals(key)) {
                s.setUserId((String) object);
                return true;
            } else if (SESSION_USER_EID_KEY.equals(key)) {
                s.setUserEid((String) object);
                return true;
            } else if (SESSION_CREATION_TIME_KEY.equals(key)) {
                if (object instanceof Long) {
                    s.m_created = (Long) object;
                }
            } else if (SESSION_LAST_ACCESSED_TIME_KEY.equals(key)) {
                if (object instanceof Long) {
                    s.m_accessed = (Long) object;
                }
            } else if (SESSION_TOOL_SESSIONS_KEY.equals(key)) {
                if (isObjectMap(object)) {
                    //noinspection unchecked
                    rebuildToolSessions(s, (Map<String, Serializable>)object);
                }
                return true;
            } else if (SESSION_CONTEXT_SESSIONS_KEY.equals(key)) {
                if (isObjectMap(object)) {
                    //noinspection unchecked
                    rebuildContextSessions(s, (Map<String, Serializable>)object);
                }
                return true;
            } else if (SESSION_CURRENT_TOOLSESSION_PLACEMENT_KEY.equals(key)) {
                // ORDER is critical for this key so we actually skip it here and then process it later in the rebuild
                return true;
            } else if (sessionAttributeBlacklist.contains(key)) {
                // skip this key entirely
                return true;
            }
        }
        return false;
    }

    /**
     * performs any special processing for keys found in the session cache.  These keys could
     * be attributes of the session itself, or objects like ToolSessions or ContextSessions
     * @param s Session being processed
     * @param key name of the object stored in the session cache
     * @param object the object that must be handled separately
     * @return true if the key was specially processed, false if key uses default processing
     */
    private boolean handleMLSessionSpecialKey(MyLittleSession s, String key, Serializable object) {
        if (object != null) {
            if (SESSION_CREATION_TIME_KEY.equals(key)) {
                if (object instanceof Long) {
                    s.m_created = (Long) object;
                }
                return true;
            } else if (SESSION_LAST_ACCESSED_TIME_KEY.equals(key)) {
                if (object instanceof Long) {
                    s.m_accessed = (Long) object;
                }
                return true;
            } else if (sessionAttributeBlacklist.contains(key)) {
                // skip this key entirely
                return true;
            }
        }
        return false;
    }

    /**
     * handleSessionStandardKey() set's the session attribute for the given key and object.
     * The object will be reconstructed if it is of type StoreableBreakdown; otherwise, it
     * is stored as the attributes value directly
     * @param s the Session that will contain the attribute
     * @param key the key for the attribute
     * @param object the value of the attribute, which will be reconstituted if of type StoreableBreakdown
     */
    private void handleSessionStandardKey(HttpSession s, String key, Serializable object) {
        if (s != null && key != null) {
            String className = object.getClass().getName();
            if (object instanceof StoreableBreakdown) {
                if (log.isDebugEnabled()) log.debug("rebuilding StoreableBreakdown, key: [" + key + "], className: [" + className + "]");
                StoreableBreakdown storedBreakdown = (StoreableBreakdown) object;
                Breakdownable<?> handler = breakdownableHandlers.get(storedBreakdown.getClassName());
                if (handler != null && handler instanceof BreakdownRebuildCallback) {
                    // Skip the rebuilding and only call the stashing function
                    boolean stashed = ((BreakdownRebuildCallback)handler).makeStash(storedBreakdown, key, s);
                    if (!stashed) {
                        String stashKey = storedBreakdown.makeStash(s.getId(), key);
                        stashingCache.put(stashKey, storedBreakdown);
                    }
                } else {
                    Object thing = this.rebuildObject(storedBreakdown.getClassName(), storedBreakdown.getSize(), storedBreakdown.getData());
                    s.setAttribute(key, thing);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("rebuilding Serializable, key: [" + key
                                      + "], className: [" + className + "], value: [" + object + "]");
                }
                s.setAttribute(key, object);
            }
        }
    }

    /**
     * rebuildToolSessions() expects to find a Map of Maps.  The outer map contains
     * the ToolSession ID's, and for each ToolSessionId, the inner map contains the
     * attributes of that tool session
     * @param mySession a Session that can be resolved to a MySession, giving access to the ToolSession property
     * @param toolSessionMap a Serialized map of maps.  The outer map containing the ToolSession ID, and the inner
     * map containing the details of the ToolSession
     */
    private void rebuildToolSessions(MySession mySession, Map<String, Serializable> toolSessionMap) {
        for (Entry<String, Serializable> entry : toolSessionMap.entrySet()) {
            String toolSessionKey = entry.getKey();
            // if a tool session doesn't exist for this key, a new one will be created automatically
            MyLittleSession toolSession = (MyLittleSession) mySession.getToolSession(toolSessionKey);
            Serializable serializable = entry.getValue();
            if (!(serializable instanceof Map)) {
                log.warn("inner object for toolSession [" + toolSessionKey
                                 + "] should be [Map], found ["
                                 + serializable.getClass().getName() + "]");
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Serializable> toolAttributes = (Map<String, Serializable>) serializable;
            processMLSessionMap(toolSession, toolAttributes);
        }
    }

    /**
     * rebuildContextSessions() expects to find a Map of Maps.  The outer map contains
     * the ContextSession ID's, and for each ContextSessionId, the inner map contains the
     * attributes of that context session
     * @param mySession a Session that can be resolved to a MySession, giving access to the ToolSession property
     * @param contextSessionMap a Serialized map of maps.  The outer map containing the ContextSession ID, and the inner
     * map containing the details of the ContextSession
     */
    private void rebuildContextSessions(MySession mySession, Map<String, Serializable> contextSessionMap) {
        for (Entry<String, Serializable> entry : contextSessionMap.entrySet()) {
            String contextSessionKey = entry.getKey();
            MyLittleSession contextSession = (MyLittleSession) mySession.getContextSession(contextSessionKey);
            Serializable serializable = entry.getValue();
            if (!(serializable instanceof Map)) {
                log.warn("inner object for contextSession ["
                                 + contextSessionKey + "] should be [Map], found ["
                                 + serializable.getClass().getName() + "]");
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Serializable> contextAttributes = (Map<String, Serializable>) serializable;
            processMLSessionMap(contextSession, contextAttributes);
        }
    }

    /**
     * We only want to breakdown a session as needed. Sakai will typically have 4+ requests per user click.
     * It also has pings which happen routinely to keep the session alive and update things in the portal.
     * If we actually have a likely chance of updates or there has been enough time then we will do the breakdown
     * and store it in the distributed cache. Otherwise we will skip it.
     * NOTE: this will be tricky to get right and could dramatically affect load and performance
     *
     * @param ms the MySession to check
     * @param req [OPTIONAL] the current request
     * @return true if the session should be processed now, false if processing should be skipped until later
     */
    private boolean isSessionBreakdownAllowed(MySession ms, HttpServletRequest req) {
        boolean allowed = false;
        boolean done = false;
        int minSecondsBetweenStores = minSecondsBetweenStoresDefault; // DEFAULT
        int minSecsOldToStore = minAgeToStoreSecondsDefault;
        long now = System.currentTimeMillis();

        // don't store invalidated or inactive or empty sessions
        if (ms == null || !ms.isValid() || ms.isInactive()) {
            allowed = false;
            done = true;
        }

        if (!done) {
            // first check for freshly created sessions, we don't store them until they are at least X seconds old (avoid server thrashing)
            if (serverConfigurationService != null) {
                // only try if we have a SCS AND the min has not been forced to the smallest value as an override
                minSecsOldToStore = serverConfigurationService.getInt("session.cluster.minSecsOldToStore", minAgeToStoreSecondsDefault);
            }
            long minMSOldToStore = minSecsOldToStore * 1000l;
            long sessionCreationMS = ms.getCreationTime();
            long sessionMSOld = now - sessionCreationMS;
            if (sessionMSOld > minMSOldToStore) {
                allowed = true;
            } else {
                allowed = false;
                done = true;
            }
        }

        if (req != null && !done) {
            // requests through access or direct should not result in storing the session ever
            String contextPath = req.getContextPath();
            if (StringUtils.startsWith(contextPath, "/direct")
                        || StringUtils.startsWith(contextPath, "/xlogin")
                        || StringUtils.startsWith(contextPath, "/access")
                    ) {
                if (log.isDebugEnabled()) log.debug("isSessionBreakdownAllowed("+ms.getId()+"): found direct or access: "+contextPath);
                allowed = false;
                done = true;
            }
            //noinspection ConstantConditions
            if (!done && !allowed) {
                // we will assume that POSTs changed something and therefore should be allowed to always update the session
                String method = req.getMethod().toUpperCase();
                if ("POST".equals(method)) {
                    if (log.isDebugEnabled()) log.debug("isSessionBreakdownAllowed("+ms.getId()+"): found POST: "+req.getRequestURI());
                    minSecondsBetweenStores = smallestMinSecondsBetweenStores; // reset to the shortest reasonable minimum
                    allowed = true;
                }
            }
        }

        if (!done) {
            // recently rebuilt sessions should not be stored for at least 30 seconds, check timing for all cases
            Long lastRebuild = (Long) ms.getAttribute(SESSION_LAST_REBUILD_KEY);
            if (lastRebuild != null) {
                int minSecondsAfterRebuild = minSecondsAfterRebuildDefault;
                if (serverConfigurationService != null) {
                    // only try if we have a SCS
                    minSecondsAfterRebuild = serverConfigurationService.getInt("session.cluster.minSecsAfterRebuild", minSecondsAfterRebuildDefault);
                    if (minSecondsAfterRebuild < 1) {
                        minSecondsAfterRebuild = minSecondsAfterRebuildDefault;
                    }
                }
                long minMSAfterRebuild = minSecondsAfterRebuild * 1000l;
                long msSinceLastRebuild = (now - lastRebuild);
                if (msSinceLastRebuild > minMSAfterRebuild) {
                    if (log.isDebugEnabled()) log.debug("isSessionBreakdownAllowed("+ms.getId()+"): rebuild min ("+minSecondsAfterRebuild+" s) passed: "+msSinceLastRebuild+" > "+minMSAfterRebuild);
                    allowed = true;
                }
            }
        }

        if (!done) {
            // session breakdown should not happen if it happened recently, check the timing in ALL cases
            Long lastBreakdown = (Long) ms.getAttribute(SESSION_LAST_BREAKDOWN_KEY);
            if (lastBreakdown != null) {
                //noinspection ConstantConditions
                if (serverConfigurationService != null
                            && minSecondsBetweenStores != smallestMinSecondsBetweenStores) {
                    // only try if we have a SCS AND the min has not been forced to the smallest value as an override
                    minSecondsBetweenStores = serverConfigurationService.getInt("session.cluster.minSecsBetweenStores", minSecondsBetweenStoresDefault);
                    if (minSecondsBetweenStores < smallestMinSecondsBetweenStores) {
                        minSecondsBetweenStores = smallestMinSecondsBetweenStores;
                    }
                }
                long minMSBetweenStores = minSecondsBetweenStores * 1000l;
                long msSinceLastBreakdown = (now - lastBreakdown);
                if (msSinceLastBreakdown > minMSBetweenStores) {
                    if (log.isDebugEnabled()) log.debug("isSessionBreakdownAllowed("+ms.getId()+"): store min ("+minSecondsBetweenStores+" s) passed: "+msSinceLastBreakdown+" > "+minMSBetweenStores);
                    allowed = true;
                }
            } else {
                // not stored before so store it
                if (log.isDebugEnabled()) log.debug("isSessionBreakdownAllowed("+ms.getId()+"): not stored before");
                allowed = true;
            }
        }
        return allowed;
    }

    /**
     * @return true if the session clustering is enabled
     */
    private boolean isSessionClusteringEnabled() {
        boolean enabled = false;
        if (serverConfigurationService != null) {
            enabled = serverConfigurationService.getBoolean("session.cluster.replication", false);
        }
        return enabled;
    }

    /**
     * isSessionValid() checks the properties of the session
     * @param mySession a Session
     * @return true if the session is valid or false otherwise
     */
    private boolean isSessionValid(MySession mySession) {
        if (mySession == null) {
            return false;
        }
        if (!mySession.isValid()) {
            return false;
        }
        if (mySession.isInactive()) {
            return false;
        }
        if (StringUtils.isBlank(mySession.getUserEid())) {
            return false;
        }
        if (StringUtils.isBlank(mySession.getUserId())) {
            return false;
        }
        return true;
    }

    /**
     * Determine if an object is a primitive type
     * @param object any object
     * @return true if the object is a String or primitive class type
     */
    private boolean isObjectSimple(Object object) {
        boolean primitive = false;
        if (object != null) {
            Class clazz = object.getClass();
            primitive = clazz.isPrimitive() || ConstructorUtils.isClassSimple(clazz);
        }
        return primitive;
    }

    /**
     * Determine if an object is a map
     * @param object any object
     * @return true if the object is a map
     */
    private boolean isObjectMap(Object object) {
        boolean map = false;
        if (object != null) {
            Class clazz = object.getClass();
            map = ConstructorUtils.isClassMap(clazz);
        }
        return map;
    }

    // SPRING SETTERS

    public void setMemoryService(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setUsageSessionService(UsageSessionService usageSessionService) {
        this.usageSessionService = usageSessionService;
    }

}
