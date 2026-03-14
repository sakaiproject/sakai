/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.authz.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IllegalSecurityAdvisorException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the SecurityService interface that provides security and authorization 
 * functionality for the Sakai system. This class manages permission checking, security advisors, 
 * role swapping, and caching of security decisions.
 *
 * <p>The SakaiSecurity service handles:
 * <ul>
 * <li>Permission checking (unlock operations) for users, functions, and entity references</li>
 * <li>Security advisor stack management for temporary permission overrides</li>
 * <li>Role swapping functionality for viewing content as different roles</li>
 * <li>Caching of security decisions to improve performance</li>
 * <li>Super user privilege checking</li>
 * <li>Integration with Sakai's authorization group (realm) system</li>
 * <li>Event-based cache invalidation when realms or permissions change</li>
 * </ul>
 *
 * <p>This implementation uses a multi-level caching strategy with separate caches for 
 * super user permissions and regular content permissions. Cache entries are automatically 
 * invalidated when relevant realm changes occur through the event system.
 *
 * <p>Thread-local storage is used to maintain security advisor stacks, allowing temporary 
 * permission modifications within specific execution contexts.
 *
 * <p><strong>Cache Expiration Strategy:</strong>
 * <ul>
 * <li>If user.template, site.helper, etc. change: clear entire security cache</li>
 * <li>If permissions in a site change: loop through all possible site users and the changed 
 *     permissions and remove all those entries from the cache (including the entry for the 
 *     anonymous user - e.g. unlock@@...)</li>
 * <li>If permissions for a user change: same as site permissions but all the user sites and 
 *     the changed permissions</li>
 * <li>If a user is added/removed from super user status: update the cache entry (easiest to 
 *     simply make sure we update the cache when this happens rather than invalidating)</li>
 * </ul>
 *
 * <p><strong>Cache Implementation Notes:</strong>
 * <ul>
 * <li>Cache keys are: unlock@{userId}@{perm}@{realm} AND super@{userId}</li>
 * <li>This strategy eliminates the need to store the invalidation keys and is much simpler to code</li>
 * <li>There is a very good chance many of those would not be in the cache but that should not 
 *     cause a problem (however if it proves to be problematic we could do key checks to cut those 
 *     down, but I don't think that is actually more efficient)</li>
 * <li>Getting all possible permissions is cheap, that's in memory already</li>
 * <li>Getting all the site IDs for a user or all the user IDs for a site might be a little more 
 *     costly, but the idea is that this is a rare case</li>
 * <li>Super user change is event: SiteService.SECURE_UPDATE_SITE_MEMBERSHIP with context !/site/admin</li>
 * </ul>
 *
 * <p>Implements Observer to receive notifications about realm changes and update caches accordingly.
 */
@Slf4j
public class SakaiSecurity implements SecurityService, Observer {
    protected final static String ADVISOR_STACK = "SakaiSecurity.advisor.stack"; // ThreadLocalManager key for our SecurityAdvisor Stack
    protected final static String EVENT_ROLESWAP_CLEAR = "realm.clear.cache"; // The update event to post to clear cached security lookups involving the authz group
    protected static final String ROLE_VIEW = "role.view";

    @Setter protected AuthzGroupService authzGroupService;
    @Setter protected EntityManager entityManager;
    @Setter protected EventTrackingService eventTrackingService;
    @Setter protected MemoryService memoryService;
    @Setter protected SessionManager sessionManager;
    @Setter protected ThreadLocalManager threadLocalManager;
    @Setter protected UserDirectoryService userDirectoryService;
    @Setter protected FunctionManager functionManager;
    @Setter protected ServerConfigurationService serverConfigurationService;
    @Setter protected SiteService siteService;
    @Setter protected ToolManager toolManager;

    protected Cache<String, Boolean> m_callCache = null; // A cache of calls to the service and the results
    private Cache<String, Boolean> m_contentCache; // holds the content authz check cached results
    private Cache<String, Boolean> m_superCache; // holds the superuser check cached results
    protected int m_cacheMinutes = 3; // The # minutes to cache the security answers. 0 disables the cache
    boolean cacheDebug = false; // Enable cache debugging output in the logs [memory.SecurityService.debug=true]
    boolean cacheDebugDetailed = false; // Show extra details in the debugging including hits and misses, adds, ref conversions, all current entries data [memory.SecurityService.debugDetails=true]
    private Set<String> svRoles;

    public void init() {
        // <= 0 minutes indicates no caching desired
        if (m_cacheMinutes > 0) {
            cacheDebug = serverConfigurationService.getBoolean("memory.SecurityService.debug", false);
            if (cacheDebug) {
                log.warn("SecurityService DEBUG logging is enabled... this is very bad for PRODUCTION and should only be used for DEVELOPMENT");
                cacheDebugDetailed = serverConfigurationService.getBoolean("memory.SecurityService.debugDetails", cacheDebugDetailed);
            } else {
                cacheDebugDetailed = false;
            }

            String[] externalRoles = serverConfigurationService.getString("studentview.roles", "").split(","); // get the roles that can be swapped to
            svRoles = new HashSet<>();
            for (String externalRole : externalRoles) {
                svRoles.add(externalRole.trim());
            }

            m_callCache = memoryService.getCache("org.sakaiproject.authz.api.SecurityService.cache");
            m_superCache = memoryService.getCache("org.sakaiproject.authz.api.SecurityService.superCache");
            m_contentCache = memoryService.getCache("org.sakaiproject.authz.api.SecurityService.contentCache");
        }
        eventTrackingService.addObserver(this);
    }

    /**
     * Set the # minutes to cache a security answer.
     *
     * @param time
     *        The # minutes to cache a security answer (as an integer string).
     */
    public void setCacheMinutes(String time) {
        m_cacheMinutes = Integer.parseInt(time);
    }

    /**
     * Get a permission check from the cache
     * @param key the cache key (generated using makeCacheKey)
     * @param isSuper true if this is a superuser cache entry
     * @return boolean value if found, null if not found in the cache
     */
    Boolean getFromCache(String key, boolean isSuper) {
        Boolean result = null;
        if (m_callCache != null) {
            if (isSuper) {
                result = m_superCache.get(key);
            } else {
                if (key.contains("@/content")) {
                    result = m_contentCache.get(key);
                } else {
                    result = m_callCache.get(key);
                }
            }
            // see the note below about forced cache expiration
        }
        if (cacheDebugDetailed) {
            if (result != null) {
                log.info("SScache:hit:{}:val={}", key, result);
            } else {
                log.info("SScache:MISS: {}", key);
            }
        }
        return result;
    }

    /**
     * Add a permission check to the cache
     *
     * @param key the cache key (generated using makeCacheKey)
     * @param payload true if the permission is granted, false if not
     * @param isSuper true if this is a superuser cache entry
     */
    void addToCache(String key, Boolean payload, boolean isSuper) {
        if (m_callCache != null && key != null) {
            if (isSuper) {
                m_superCache.put(key, payload);
                if (cacheDebugDetailed) {
                    log.info("SScache:ADD->super:{}=>{}", key, payload);
                }
            } else {
                if (key.contains("@/content")) {
                    m_contentCache.put(key, payload);
                    if (cacheDebugDetailed) {
                        log.info("SScache:ADD->content:{}=>{}", key, payload);
                    }
                } else {
                    m_callCache.put(key, payload);
                    if (cacheDebugDetailed) logCacheState("addToCache(" + key + ", " + payload + ")");
                }
            }
            // see the note below about forced cache expiration
        }
    }

    /**
     * Called when realms are changed (like a realm.upd Event), should handle inputs outside the range
     * @param azgReference should be the value from Event.ref
     * @param roles a set of roles that changed (maybe null or empty)
     * @param permissions a set of permissions that changed (maybe null or empty)
     * @return true if this was a realm and case we handle, and we took action, false otherwise
     */
    public boolean notifyRealmChanged(String azgReference, Set<String> roles, Set<String> permissions) {
        if (m_callCache == null) return false; // do nothing, no cache in use
        if (azgReference != null) {
            String ref = convertRealmRefToRef(azgReference); // strip off /realm/ from the start
            if ("!site.helper".equals(ref)
                    || ref.startsWith("!user.template")
                //|| "/site/!site".equals(ref) // we might not need this one
            ) {
                if (permissions != null && !permissions.isEmpty()) {
                    // when the !site.helper or !user.template change, then we need to just wipe the entire cache. This is a rare event
                    m_callCache.clear();
                    if (cacheDebug) log.info("SScache:changed template:CLEAR:{}", ref);
                    return true;
                }

            } else if ("/site/!admin".equals(ref)) {
                // when the superuser realm (!admin, also the event context) changes (realm.upd) then we wipe this cache out
                if (m_superCache != null) {
                    m_superCache.clear();
                    if (cacheDebug) log.info("SScache:changed !admin:CLEAR SUPER:{}", ref);
                }
                return true;

            } else if (ref.startsWith("/content")) {
                // content realms require special handling
                // WARNING: this is handled in a simple but not very efficient way, should be improved later
                m_contentCache.clear();
                if (cacheDebug) log.info("SScache:changed content:CLEAR CONTENT:{}", ref);
                return true;

            } else {
                if (permissions != null && !permissions.isEmpty()) {
                    // we only process the event change when there are changed permissions
                    cacheRealmPermsChanged(ref, roles, permissions);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Called when realms are removed (like a realm.del Event)
     * @param azgReference should be the value from Event.ref
     * @return true if this was a realm and case we handle, and we took action, false otherwise
     */
    public boolean notifyRealmRemoved(String azgReference) {
        if (m_callCache == null) return false; // do nothing, no cache in use
        if (azgReference != null) {
            String ref = convertRealmRefToRef(azgReference); // strip off /realm/ from the start
            if (ref.startsWith("/content")) {
                // content realms require special handling
                // WARNING: this is handled in a simple but not very efficient way, should be improved later
                m_contentCache.clear();
                if (cacheDebug) log.info("SScache:removed content:CLEAR CONTENT:{}", ref);

            } else {
                // we only process the event change when there are changed permissions
                cacheRealmPermsChanged(ref, null, null);
            }
            return true;
        }
        return false;
    }

    /**
     * Removes the specified users site visit permission from the call cache
     */
    protected void notifyMembersRemovedFromRealm(Set<String> userIds, String azgRef) {
        m_callCache.removeAll(userIds.stream().map(uid -> makeCacheKey(uid, null, SiteService.SITE_VISIT, azgRef, false)).collect(Collectors.toSet()));
    }

    /**
     * Flush out check caches based on changes to the permissions in an AuthzGroup
     * @param realmRef an AuthzGroup realm reference (e.g., /site/123123-as-sda21-213-1-33233)
     * @param roles a set of roles that changed (maybe null or empty)
     * @param permissions a set of permissions that changed (maybe null or empty)
     */
    protected void cacheRealmPermsChanged(String realmRef, Set<String> roles, Set<String> permissions) {
        if (m_callCache == null) return; // do nothing if no cache in use
        String azgRef = convertRealmRefToRef(realmRef);
        if (permissions == null || permissions.isEmpty()) {
            List<String> allPerms = functionManager.getRegisteredFunctions();
            permissions = new HashSet<>(allPerms);
        }
        HashSet<String> keysToInvalidate = new HashSet<>();
        // changed permissions for a role in an AZG
        AuthzGroup azg;
        try {
            azg = authzGroupService.getAuthzGroup(azgRef);
        } catch (GroupNotDefinedException e) {
            // no group found, so no invalidation needed
            if (cacheDebug) log.warn("SScache:changed FAIL: AZG realm not found:{} from ", azgRef, realmRef);
            return; // SHORT CIRCUIT
        }
        if (roles == null || roles.isEmpty()) {
            Set<Role> allGroupRoles = azg.getRoles();
            roles = new HashSet<>();
            for (Role role : allGroupRoles) {
                roles.add(role.getId());
            }
        }
        // first handle the .anon and .auth (maybe only needed for special cases?)
        if (roles.contains(AuthzGroupService.AUTH_ROLE)) {
            /* .auth (AUTH_ROLE) is a special case,
             * it could mean any possible user in the system, so we cannot know which keys to invalidate.
             * We have to just flush the entire cache
             */
            m_callCache.clear();
            if (cacheDebug) log.info("SScache:changed .auth:CLEAR and DONE");
            return; // SHORT CIRCUIT
        }
        boolean anon = roles.contains(AuthzGroupService.ANON_ROLE);
        if (!anon) {
            Set<Role> azgRoles = azg.getRoles();
            for (Role role : azgRoles) {
                if (AuthzGroupService.ANON_ROLE.equals(role.getId())) {
                    anon = true;
                    break;
                }
            }
        }
        if (anon) {
            // anonymous user access (ANON_ROLE) needs to force reset on anonymous changes in the site
            if (cacheDebug) log.info("SScache:changed .anon:found in {}", azgRef);
            for (String perm : permissions) {
                if (perm != null) {
                    keysToInvalidate.add(makeCacheKey(null, null, perm, azgRef, false));
                }
            }
        }

        m_callCache.removeAll(keysToInvalidate);

        // now handle all the real users
        // clear both normal and swapped users
        // start with a set of all roles to which one can swap
        Set<String> svRolesFinal = new HashSet<>(svRoles);
        svRolesFinal.retainAll(roles);

        Set<Member> members = azg.getMembers();
        if (members != null && !members.isEmpty()) {
            for (String perm : permissions) {
                if (perm != null) {
                    HashSet<String> permKeysToInvalidate = new HashSet<>();
                    for (Member member : members) {
                        if (member != null && member.isActive() && member.getUserId() != null) {
                            boolean canSwap = member.getRole().isAllowed(SiteService.SITE_ROLE_SWAP);
                            permKeysToInvalidate.add(makeCacheKey(member.getUserId(), null, perm, azgRef, false));
                            // Only invalidate swapped roles if the user can swap
                            // This is an approximation. If a user is swapped and their permission to swap is removed
                            // or the role they are swapped to has been removed from the site, 
                            // we will not invalidate their data. Their info may wait until the expiration time to sync up
                            if (canSwap) {
                                for (String invRole : svRolesFinal) {
                                    permKeysToInvalidate.add(makeCacheKey(member.getUserId(), invRole, perm, azgRef, false));
                                }
                            }
                        }
                    }
                    // invalidate all keys (do this as a batch)
                    if (cacheDebug) log.info("SScache:changed {}:keys={}", azgRef, keysToInvalidate);
                    m_callCache.removeAll(permKeysToInvalidate);
                }
            }
        }
        if (cacheDebug)
            logCacheState("cacheRealmPermsChanged(" + realmRef + ", roles=" + roles + ", perms=" + permissions + ")");
    }

    /**
     * Convert a realm reference in to a standard reference
     * @param realmRef a realm specific ref (e.g. /realm//site/123123-as-sda21-213-1-33233)
     * @return a standard ref (e.g., /site/123123-as-sda21-213-1-33233)
     */
    private String convertRealmRefToRef(String realmRef) {
        String rv = null;
        if (realmRef != null) {
            // strip off the leading /realm or /realm/
            if (realmRef.startsWith("/realm/")) {
                rv = realmRef.substring(7);
            } else if (realmRef.startsWith("/realm")) {
                rv = realmRef.substring(6);
            } else {
                rv = realmRef;
            }
        }
        return rv;
    }

    /**
     * Make a cache key for security caching
     * @param userId the internal sakai user ID (can be null)
     * @param function the permission
     * @param reference the realm reference
     * @param isSuperKey if true, this is a key for tracking superusers, else generate a normal realm key
     * @return the key OR null if one cannot be properly made from these params
     */
    private String makeCacheKey(String userId, String role, String function, String reference, boolean isSuperKey) {
        if (isSuperKey) {
            if (userId != null) {
                return "super@" + userId;
            } else {
                return null;
            }
        }
        if (function == null || reference == null) {
            return null;
        }
        if (role == null) role = "";
        // SPECIAL conversion to reduce duplicate caching data
        if (!reference.startsWith("/site") && !reference.startsWith("/content")) {
            // try to convert this from a special reference down to the authzgroup ref
            Reference ref = entityManager.newReference(reference);
            Collection<String> azgs = ref.getAuthzGroups(userId);
            for (String azgRef : azgs) {
                if (azgRef.startsWith("/site")) {
                    if (cacheDebug) log.warn("SScache:converted ref {} to {}", reference, azgRef);
                    reference = azgRef;
                    break;
                }
            }
        }
        // NOTE: userId can be a null for this, others cannot be
        return "unlock@" + userId + "@" + role + "@" + function + "@" + reference;
    }

    private void logCacheState(String operator) {
        if (cacheDebug) {
            String name = m_callCache.getName();
            net.sf.ehcache.Ehcache ehcache = m_callCache.unwrap(Ehcache.class); // DEBUGGING ONLY
            StringBuilder entriesSB = new StringBuilder();
            List keys = ehcache.getKeysWithExpiryCheck(); // only current keys
            entriesSB.append("   * keys(").append(keys.size()).append("):").append(new ArrayList<Object>(keys)).append("\n");
            Collection<Element> entries = ehcache.getAll(keys).values();
            int countMaps = 0;
            for (Element element : entries) {
                if (element == null) continue;
                int count = 0;
                countMaps += count;
                if (cacheDebugDetailed) {
                    entriesSB.append("   ").append(element.getObjectKey()).append(" => (").append(count).append(")").append(element.getObjectValue()).append("\n");
                }
            }
            log.info("SScache:{}:: {} ::\n  entries(Ehcache[key => payload],{} + {} = {}):\n{}", name, operator, keys.size(), countMaps, keys.size() + countMaps, entriesSB);
        }
    }

    public void destroy() {
        log.info("destroy()");
        if (m_callCache != null) m_callCache.close();
        if (m_superCache != null) m_superCache.close();
        if (m_contentCache != null) m_contentCache.close();
    }

    @Override
    public boolean isSuperUser() {
        User user = userDirectoryService.getCurrentUser();
        if (user == null) return false;

        return isSuperUser(user.getId());
    }

    @Override
    public boolean isSuperUser(String userId) {
        // if no user or the no-id user (i.e., the anon user)
        if ((userId == null) || (userId.isEmpty())) return false;

        // check the cache
        String command = makeCacheKey(userId, null, null, null, true);
        if (m_callCache != null) {
            final Boolean value = getFromCache(command, true);
            if (value != null) return value;
        }

        boolean rv = false;

        // these known ids are super
        if (UserDirectoryService.ADMIN_ID.equalsIgnoreCase(userId)) {
            rv = true;
        } else if ("postmaster".equalsIgnoreCase(userId)) {
            rv = true;
        } else {
            // if the user has site modification rights in the "!admin" site, welcome aboard!
            if (authzGroupService.isAllowed(userId, SiteService.SECURE_UPDATE_SITE, "/site/!admin")) {
                rv = true;
            }
        }

        // cache
        if (m_callCache != null) {
            Collection<String> azgIds = new HashSet<>();
            azgIds.add("/site/!admin");
            addToCache(command, rv, true);
        }

        return rv;
    }

    @Override
    public boolean unlock(String lock, String resource) {
        return unlock(userDirectoryService.getCurrentUser(), lock, resource);
    }

    @Override
    public boolean unlock(User u, String function, String entityRef) {
        // pick up the current user if needed
        User user = u;
        if (user == null) {
            user = userDirectoryService.getCurrentUser();
        }
        return unlock(user.getId(), function, entityRef);
    }

    @Override
    public boolean unlock(String userId, String function, String entityRef) {
        return unlock(userId, function, entityRef, null);
    }

    @Override
    public boolean unlock(String userId, String function, String entityRef, Collection<String> azgs) {
        // make sure we have complete parameters (azgs is optional)
        if (userId == null || function == null || entityRef == null) {
            log.warn("unlock(): null: {} {} {}", userId, function, entityRef);
            return false;
        }

        // if super user or site.visit.unp for users that are in role view mode
        // when a better way of ensuring certain users have specific permissions, this should be removed
        if (isSuperUser(userId) || (SiteService.SITE_VISIT_UNPUBLISHED.equals(function) && isUserRoleSwapped())) {
            return true;
        }

        // let the advisors have a crack at it, if we have any
        // Note: this cannot be cached without taking into consideration the exact advisor configuration -ggolden
        if (hasAdvisors()) {
            SecurityAdvisor.SecurityAdvice advice = adviseIsAllowed(userId, function, entityRef);
            if (advice != SecurityAdvisor.SecurityAdvice.PASS) {
                return advice == SecurityAdvisor.SecurityAdvice.ALLOWED;
            }
        }

        // check with the AuthzGroups appropriate for this entity
        return checkAuthzGroups(userId, function, entityRef, azgs);
    }

    /**
     * Check the appropriate AuthzGroups for the answer - this may be cached
     *
     * @param userId
     *        The user id.
     * @param function
     *        The security function.
     * @param entityRef
     *        The entity reference string.
     * @return true if allowed, false if not.
     */
    protected boolean checkAuthzGroups(String userId, String function, String entityRef, Collection<String> azgs) {
        // get this entity's AuthzGroups if needed
        if (azgs == null) {
            // make a reference for the entity
            Reference ref = entityManager.newReference(entityRef);
            azgs = ref.getAuthzGroups(userId);
        }

        // need to know whether role swap is in effect, since we can't share the cache entry between sessions
        // that are swapped and not swapped

        String roleswap = getUserEffectiveRole();

        // check the cache
        String command = makeCacheKey(userId, roleswap, function, entityRef, false);

        if (m_callCache != null) {
            final Boolean value = getFromCache(command, false);
            if (value != null) return value;
        }

        boolean rv = authzGroupService.isAllowed(userId, function, azgs);

        addToCache(command, rv, false);

        return rv;
    }

    /**
     * Access the List the Users who can unlock the lock for use with this resource.
     *
     * @param lock
     *        The lock id string.
     * @param reference
     *        The resource reference string.
     * @return A List (User) of the users can unlock the lock (maybe empty).
     */
    @Override
    public List<User> unlockUsers(String lock, String reference) {
        if (reference == null) {
            log.warn("unlockUsers(): null resource: {}", lock);
            return new ArrayList<>();
        }

        // make a reference for the resource
        Reference ref = entityManager.newReference(reference);

        // get this resource's Realms
        Collection<String> realms = ref.getAuthzGroups();

        // get the users who can unlock in these realms
        List<String> ids = new ArrayList<>(authzGroupService.getUsersIsAllowed(lock, realms));

        // convert the set of Users into a sorted list of users
        // and filter them so that only real users are displayed (not simulated users for the role view)
        return userDirectoryService.getUsers(ids).stream()
                .filter(user -> !UserDirectoryService.ROLEVIEW_USER_TYPE.equals(user.getType()))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get the thread-local security advisor stack, possibly creating it
     *
     * @param force
     *        if true, create if missing
     */
    @SuppressWarnings("unchecked")
    protected Stack<SecurityAdvisor> getAdvisorStack(boolean force) {
        Stack<SecurityAdvisor> advisors = (Stack<SecurityAdvisor>) threadLocalManager.get(ADVISOR_STACK);
        if ((advisors == null) && force) {
            advisors = new Stack<>();
            threadLocalManager.set(ADVISOR_STACK, advisors);
        }

        return advisors;
    }

    /**
     * Remove the thread-local security advisor stack
     */
    protected void dropAdvisorStack() {
        threadLocalManager.set(ADVISOR_STACK, null);
    }

    /**
     * Check the advisor stack - if anyone declares ALLOWED or NOT_ALLOWED, stop and return that, else, while they PASS, keep checking.
     *
     * @param userId
     *        The user id.
     * @param function
     *        The security function.
     * @param reference
     *        The Entity reference.
     * @return ALLOWED or NOT_ALLOWED if an advisor makes a decision, or PASS if there are no advisors, or they cannot make a decision.
     */
    protected SecurityAdvisor.SecurityAdvice adviseIsAllowed(String userId, String function, String reference) {
        Stack<SecurityAdvisor> advisors = getAdvisorStack(false);
        if ((advisors == null) || (advisors.isEmpty())) return SecurityAdvisor.SecurityAdvice.PASS;

        // a Stack grows to the right - process from top to bottom
        for (int i = advisors.size() - 1; i >= 0; i--) {
            SecurityAdvisor advisor = advisors.elementAt(i);

            SecurityAdvisor.SecurityAdvice advice = advisor.isAllowed(userId, function, reference);
            if (advice != SecurityAdvisor.SecurityAdvice.PASS) {
                return advice;
            }
        }

        return SecurityAdvisor.SecurityAdvice.PASS;
    }

    @Override
    public void pushAdvisor(SecurityAdvisor advisor) {
        Stack<SecurityAdvisor> advisors = getAdvisorStack(true);
        advisors.push(advisor);
    }

    @Override
    public SecurityAdvisor popAdvisor(SecurityAdvisor advisor) {
        Stack<SecurityAdvisor> advisors = getAdvisorStack(false);
        if (advisors == null) return null;

        SecurityAdvisor rv = null;

        if (!advisors.isEmpty()) {
            if (advisor == null) {
                // TODO: This seems dangerous to me. What if null is accidentally supplied and this
                // results in another advisor being popped off the stack? Seems like a very bad idea
                rv = advisors.pop();
            } else {
                SecurityAdvisor sa = advisors.peek();
                if (advisor.equals(sa)) {
                    rv = advisors.pop();
                } else {
                    // Code is attempting to popAdvisor in the wrong order, so we destroy the stack to be safe
                    dropAdvisorStack();
                    throw new IllegalSecurityAdvisorException("SecurityAdvisor not called in correct order");
                }
            }
        }

        if (advisors.isEmpty()) dropAdvisorStack();
        return rv;
    }

    @Override
    public SecurityAdvisor popAdvisor() {
        return popAdvisor(null);
    }

    @Override
    public boolean hasAdvisors() {
        Stack<SecurityAdvisor> advisors = getAdvisorStack(false);
        if (advisors == null) return false;

        return !advisors.isEmpty();
    }

    @Override
    public boolean setUserEffectiveRole(String azGroupId, String role) {
        // set the session attribute with the roleid
        sessionManager.getCurrentSession().setAttribute(ROLE_VIEW, role);

        return true;
    }

    @Override
    public String getUserEffectiveRole() {
        return (String) sessionManager.getCurrentSession().getAttribute(ROLE_VIEW);
    }

    /**
     * Clear the results of security lookups involving the given authz group from the security lookup cache.
     *
     * @param azGroupId
     *        The authz group id.
     */
    protected void resetSecurityCache(String azGroupId) {
        // This will clear all cached security lookups involving this realm, thereby forcing the permissions to be rechecked.
        // We could turn this into a SessionStateBindingListener, so it gets called automatically when
        // the session is cleared.
        String realmRef = org.sakaiproject.authz.api.AuthzGroupService.REFERENCE_ROOT + Entity.SEPARATOR + azGroupId;
        eventTrackingService.post(eventTrackingService.newEvent(EVENT_ROLESWAP_CLEAR, realmRef, true));

        cacheRealmPermsChanged(realmRef, null, null);
    }

    @Override
    public void update(Observable o, Object obj) {
        if (!(obj instanceof Event event)) {
            return;
        }

        if (SiteService.EVENT_SITE_USER_INVALIDATE.equals(event.getEvent())) {
            Site site = null;
            try {
                site = siteService.getSite(event.getResource());
            } catch (IdUnusedException e) {
                log.warn("Security invalidation error when handling an event ({}), for site {}", event.getEvent(), event.getResource());
            }
            if (site != null) {
                resetSecurityCache(site.getReference());
            }
        }
    }

    @Override
    public boolean isUserRoleSwapped() {
        final String effectiveRole = getUserEffectiveRole();
        return StringUtils.isNotBlank(effectiveRole);
    }
}
