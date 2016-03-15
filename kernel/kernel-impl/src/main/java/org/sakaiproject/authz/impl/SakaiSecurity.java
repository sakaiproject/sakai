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

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.*;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import java.util.*;

/**
 * <p>
 * SakaiSecurity is a Sakai security service.
 * </p>
 */
public abstract class SakaiSecurity implements SecurityService, Observer
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(SakaiSecurity.class);

	/** A cache of calls to the service and the results. */
	protected Cache m_callCache = null;

	/** ThreadLocalManager key for our SecurityAdvisor Stack. */
	protected final static String ADVISOR_STACK = "SakaiSecurity.advisor.stack";

	/** Session attribute to store roleswap state **/
	protected final static String ROLESWAP_PREFIX = "roleswap";

	/** The update event to post to clear cached security lookups involving the authz group **/
	protected final static String EVENT_ROLESWAP_CLEAR = "realm.clear.cache";

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies, configuration, and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the ThreadLocalManager collaborator.
	 */
	protected abstract ThreadLocalManager threadLocalManager();

	/**
	 * @return the AuthzGroupService collaborator.
	 */
	protected abstract AuthzGroupService authzGroupService();

	/**
	 * @return the UserDirectoryService collaborator.
	 */
	protected abstract UserDirectoryService userDirectoryService();

	/**
	 * @return the MemoryService collaborator.
	 */
	protected abstract MemoryService memoryService();

	/**
	 * @return the EntityManager collaborator.
	 */
	protected abstract EntityManager entityManager();
	
	/**
	 * @return the SessionManager collaborator.
	 */
	protected abstract SessionManager sessionManager();
	
	/**
	 * @return the EventTrackingService collaborator.
	 */
	protected abstract EventTrackingService eventTrackingService();

    protected abstract FunctionManager functionManager();
    
    /**
     * @return the SiteService collaborator
     */
    protected abstract SiteService siteService();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Configuration
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** The # minutes to cache the security answers. 0 disables the cache. */
	protected int m_cacheMinutes = 3;

	/**
	 * Set the # minutes to cache a security answer.
	 * 
	 * @param time
	 *        The # minutes to cache a security answer (as an integer string).
	 */
	public void setCacheMinutes(String time)
	{
		m_cacheMinutes = Integer.parseInt(time);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/


	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// <= 0 minutes indicates no caching desired
		if (m_cacheMinutes > 0)
		{
            org.sakaiproject.component.api.ServerConfigurationService scs = org.sakaiproject.component.cover.ServerConfigurationService.getInstance();
            cacheDebug = scs.getBoolean("memory.SecurityService.debug", false);
            if (cacheDebug) {
                M_log.warn("SecurityService DEBUG logging is enabled... this is very bad for PRODUCTION and should only be used for DEVELOPMENT");
                cacheDebugDetailed = scs.getBoolean("memory.SecurityService.debugDetails", cacheDebugDetailed);
            } else {
                cacheDebugDetailed = false;
            }
            m_callCache = memoryService().getCache("org.sakaiproject.authz.api.SecurityService.cache");
            m_superCache = memoryService().getCache("org.sakaiproject.authz.api.SecurityService.superCache");
            m_contentCache = memoryService().getCache("org.sakaiproject.authz.api.SecurityService.contentCache");
		}
		eventTrackingService().addObserver(this);
	}

    /**
     * Cache for holding the super user check cached results
     * Only used in the new caching system
     */
    Cache m_superCache;
    /**
     * Cache for holding the content authz check cached results
     * Only used in the new caching system
     */
    Cache m_contentCache;

    /**
     * KNL-1230
     * Get a permission check from the cache
     * @param key the cache key (generated using makeCacheKey)
     * @param isSuper true if this is a super user cache entry
     * @return boolean value if found, null if not found in the cache
     */
    Boolean getFromCache(String key, boolean isSuper) {
        Boolean result = null;
        if (m_callCache != null) {
            if (isSuper) {
                result = (Boolean) m_superCache.get(key);
            } else {
                if (key.contains("@/content")) {
                    result = (Boolean) m_contentCache.get(key);
                } else {
                    result = (Boolean) m_callCache.get(key);
                }
            }
            // see note below about forced cache expiration
        }
        if (cacheDebugDetailed) {
            if (result != null) {
                M_log.info("SScache:hit:"+key+":val="+result);
            } else {
                M_log.info("SScache:MISS:"+key);
            }
        }
        return result;
    }

    /**
     * KNL-1230
     * Add a permission check to the cache
     *
     * @param key the cache key (generated using makeCacheKey)
     * @param payload true if the permission is granted, false if not
     * @param isSuper true if this is a super user cache entry
     */
    void addToCache(String key, Boolean payload, boolean isSuper) {
        if (m_callCache != null && key != null) {
            if (isSuper) {
                m_superCache.put(key, payload);
                if (cacheDebugDetailed) {
                    M_log.info("SScache:ADD->super:"+key+"=>"+payload);
                }
            } else {
                if (key.contains("@/content")) {
                    m_contentCache.put(key, payload);
                    if (cacheDebugDetailed) {
                        M_log.info("SScache:ADD->content:"+key+"=>"+payload);
                    }
                } else {
                    m_callCache.put(key, payload);
                    if (cacheDebugDetailed) logCacheState("addToCache("+key+", "+payload+")");
                }
            }
            // see note below about forced cache expiration
        }
    }

    /* KNL-1230: expiration happens based on the following plan:
    if (user.template, site.helper, etc. change) then clear entire security cache
    else if the perms in a site changes we loop through all possible site users and the changed permissions and remove all those entries from the cache (including the entry for the anon user - e.g. unlock@@...)
    else if the perms for a user change, same as site perms but all the user sites and the changed permissions
    else if a user is added/removed from super user status then update the cache entry (easiest to simply make sure we update the cache when this happens rather than invalidating)
    NOTES:
    Cache keys are: unlock@{userId}@{perm}@{realm} AND super@{userId}
    This strategy eliminates the need to store the invalidation keys and is much simpler to code
    There is a very good chance many of those would not be in the cache but that should not cause a problem (however if it proves to be problematic we could do key checks to cut those down, but I don't think that is actually more efficient)
    Getting all possible perms is cheap, that's in memory already
    Get all siteids for a user or all userids for a site might be a little more costly, but the idea is that this is a rare case
    Super user change is event: SiteService.SECURE_UPDATE_SITE_MEMBERSHIP with context !/site/admin
     */

    /**
     * KNL-1230
     * Called when realms are changed (like a realm.upd Event), should handle inputs outside the range
     * @param azgReference should be the value from Event.ref
     * @param roles a set of roles that changed (may be null or empty)
     * @param permissions a set of permissions that changed (may be null or empty)
     * @return true if this was a realm and case we handle and we took action, false otherwise
     */
    public boolean notifyRealmChanged(String azgReference, Set<String> roles, Set<String> permissions) {
        if (m_callCache == null) return false; // do nothing no cache in use
        if (azgReference != null) {
            String ref = convertRealmRefToRef(azgReference); // strip off /realm/ from start
            if ("!site.helper".equals(ref)
                    || ref.startsWith("!user.template")
                //|| "/site/!site".equals(ref) // we might not need this one
            ) {
                if (permissions != null && !permissions.isEmpty()) {
                    // when the !site.helper or !user.template change then we need to just wipe the entire cache, this is a rare event
                    m_callCache.clear();
                    if (cacheDebug) M_log.info("SScache:changed template:CLEAR:"+ref);
                    return true;
                }

            } else if ("/site/!admin".equals(ref)) {
                // when the super user realm (!admin, also the event context) changes (realm.upd) then we wipe this cache out
                if (m_superCache != null) {
                    m_superCache.clear();
                    if (cacheDebug) M_log.info("SScache:changed !admin:CLEAR SUPER:"+ref);
                }
                return true;

            } else if (ref.startsWith("/content")) {
                // content realms require special handling
                // WARNING: this is handled in a simple but not very efficient way, should be improved later
                m_contentCache.clear();
                if (cacheDebug) M_log.info("SScache:changed content:CLEAR CONTENT:"+ref);
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
     * KNL-1230
     * Called when realms are removed (like a realm.del Event)
     * @param azgReference should be the value from Event.ref
     * @return true if this was a realm and case we handle and we took action, false otherwise
     */
    public boolean notifyRealmRemoved(String azgReference) {
        if (m_callCache == null ) return false; // do nothing no cache in use
        if (azgReference != null) {
            String ref = convertRealmRefToRef(azgReference); // strip off /realm/ from start
            if (ref.startsWith("/content")) {
                // content realms require special handling
                // WARNING: this is handled in a simple but not very efficient way, should be improved later
                m_contentCache.clear();
                if (cacheDebug) M_log.info("SScache:removed content:CLEAR CONTENT:"+ref);
                return true;

            } else {
                // we only process the event change when there are changed permissions
                cacheRealmPermsChanged(ref, null, null);
                return true;
            }
        }
        return false;
    }

    /* Don't think we need this right now but leaving it for future ref just in case -AZ
    void cacheUserPermsChanged(String userRef, Set<String> roles, Set<String> permissions) {
        if (m_callCache == null ) return; // do nothing if no cache in use
        // changed he permissions for a user
        if (permissions == null || permissions.isEmpty()) {
            List<String> allPerms = functionManager().getRegisteredFunctions();
            permissions = new HashSet<String>(allPerms);
        }
        String userId = userRef.substring(6);
        HashSet<String> keysToInvalidate = new HashSet<String>();
        // get all azgs for this user
        Set<String> azgRefs = authzGroupService().getAuthzGroupsIsAllowed(userId, "*", null); // "*" means ANY permission
        for (String ref : azgRefs) {
            for (String perm : permissions) {
                if (perm != null) {
                    keysToInvalidate.add(makeCacheKey(userId, perm, ref, false));
                }
            }
        }
        // invalidate all keys (do this as a batch)
        m_callCache.removeAll(keysToInvalidate);
    }
    */

    /**
     * KNL-1230
     * Flush out unlock check caches based on changes to the permissions in an AuthzGroup
     * @param realmRef an AuthzGroup realm reference (e.g. /site/123123-as-sda21-213-1-33233)
     * @param roles a set of roles that changed (may be null or empty)
     * @param permissions a set of permissions that changed (may be null or empty)
     */
    void cacheRealmPermsChanged(String realmRef, Set<String> roles, Set<String> permissions) {
        if (m_callCache == null) return; // do nothing if no cache in use
        String azgRef = convertRealmRefToRef(realmRef);
        if (permissions == null || permissions.isEmpty()) {
            List<String> allPerms = functionManager().getRegisteredFunctions();
            permissions = new HashSet<String>(allPerms);
        }
        HashSet<String> keysToInvalidate = new HashSet<String>();
        // changed permissions for a role in an AZG
        AuthzGroup azg;
        try {
            azg = authzGroupService().getAuthzGroup(azgRef);
        } catch (GroupNotDefinedException e) {
            // no group found so no invalidation needed
            if (cacheDebug) M_log.warn("SScache:changed FAIL: AZG realm not found:" + azgRef + " from " + realmRef);
            return; // SHORT CIRCUIT
        }
        if (roles == null || roles.isEmpty()) {
            Set<Role> allGroupRoles = azg.getRoles();
            roles = new HashSet<String>();
            for (Role role : allGroupRoles) {
                roles.add(role.getId());
            }
        }
        // first handle the .anon and .auth (maybe only needed for special cases?)
        if (roles.contains(AuthzGroupService.AUTH_ROLE)) {
            /* .auth (AUTH_ROLE) is a special case,
             * it could mean any possible user in the system so we cannot know which keys to invalidate.
             * We have to just flush the entire cache
             */
            m_callCache.clear();
            if (cacheDebug) M_log.info("SScache:changed .auth:CLEAR and DONE");
            return; // SHORT CIRCUIT
        }
        boolean anon = false;
        if (roles.contains(AuthzGroupService.ANON_ROLE)) {
            anon = true;
        }
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
            if (cacheDebug) M_log.info("SScache:changed .anon:found in "+azgRef);
            for (String perm : permissions) {
                if (perm != null) {
                    keysToInvalidate.add(makeCacheKey(null, perm, azgRef, false));
                }
            }
        }
        // now handle all the real users
        Set<Member> members = azg.getMembers();
        if (members != null && !members.isEmpty()) {
            for (Member member : members) {
                if (member != null && member.isActive() && member.getUserId() != null) {
                    for (String perm : permissions) {
                        if (perm != null) {
                            keysToInvalidate.add(makeCacheKey(member.getUserId(), perm, azgRef, false));
                        }
                    }
                }
            }
        }
        // invalidate all keys (do this as a batch)
        if (cacheDebug) M_log.info("SScache:changed "+azgRef+":keys="+keysToInvalidate);
        m_callCache.removeAll(keysToInvalidate);
        if (cacheDebug) logCacheState("cacheRealmPermsChanged("+realmRef+", roles="+roles+", perms="+permissions+")");
    }

    /**
     * KNL-1230
     * Convert a realm reference in to a standard reference
     * @param realmRef a realm specific ref (e.g. /realm//site/123123-as-sda21-213-1-33233)
     * @return a standard ref (e.g. /site/123123-as-sda21-213-1-33233)
     */
    String convertRealmRefToRef(String realmRef) {
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
     * KNL-1230
     * Make a cache key for security caching
     * @param userId the internal sakai user ID (can be null)
     * @param function the permission
     * @param reference the realm reference
     * @param isSuperKey if true this is a key for tracking super users, else generate a normal realm key
     * @return the key OR null if one cannot be properly made from these params
     */
    String makeCacheKey(String userId, String function, String reference, boolean isSuperKey) {
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
        // SPECIAL conversion to reduce duplicate caching data
        if (!reference.startsWith("/site") && !reference.startsWith("/content")) {
            // try to convert this from a special reference down to the authzgroup ref
            Reference ref = entityManager().newReference(reference);
            Collection<String> azgs = ref.getAuthzGroups(userId);
            for (String azgRef : azgs) {
                if (azgRef.startsWith("/site")) {
                    if (cacheDebug) M_log.warn("SScache:converted ref "+reference+" to "+azgRef);
                    reference = azgRef;
                    break;
                }
            }
        }
        // NOTE: userId can be null for this, others cannot be
        return "unlock@" + userId + "@" + function + "@" + reference;
    }

    // KNL-1230 added to assist with debugging caching issues
    /**
     * Enable cache debugging output in the logs
     * memory.SecurityService.debug=true
     */
    boolean cacheDebug = false;
    /**
     * Show extra details in the debugging including:
     * hits and misses, adds, ref conversions, all current entries data
     * memory.SecurityService.debugDetails=true
     */
    boolean cacheDebugDetailed = false;
    void logCacheState(String operator) {
        if (cacheDebug) {
            String name = m_callCache.getName();
            net.sf.ehcache.Ehcache ehcache = (Ehcache) m_callCache.unwrap(Ehcache.class); // DEBUGGING ONLY
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
            M_log.info("SScache:"+name+":: "+operator+" ::\n  entries(Ehcache[key => payload],"+keys.size()+" + "+countMaps+" = "+(keys.size()+countMaps)+"):\n"+entriesSB);
        }
    }

    /**
     * Converts a collection of authzgroup ids into authzgroup references
     * Added when removing the old MultiRefCache - KNL-1162
     *
     * @param azgIds a collection of authzgroup ids
     * @return a collection of authzgroup references (should match the incoming set of ids)
     */
    protected Collection<String> makeAzgRefsForAzgIds(Collection<String> azgIds) {
        // make refs for any azg ids
        Collection<String> azgRefs = null;
        if (azgIds != null) {
            azgRefs = new HashSet<String>(azgIds.size());
            for (String azgId : azgIds) {
                azgRefs.add(authzGroupService().authzGroupReference(azgId));
            }
        }
        return azgRefs;
    }


	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
        if (m_callCache != null) m_callCache.close();
        if (m_superCache != null) m_superCache.close();
        if (m_contentCache != null) m_contentCache.close();
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * SecurityService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public boolean isSuperUser()
	{
		User user = userDirectoryService().getCurrentUser();
		if (user == null) return false;

		return isSuperUser(user.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSuperUser(String userId)
	{
		// if no user or the no-id user (i.e. the anon user)
		if ((userId == null) || (userId.length() == 0)) return false;

		// check the cache
		String command = makeCacheKey(userId, null, null, true);
		if (m_callCache != null)
		{
			final Boolean value = getFromCache(command, true);
			if(value != null) return value.booleanValue();
		}

		boolean rv = false;

		// these known ids are super
		if (UserDirectoryService.ADMIN_ID.equalsIgnoreCase(userId))
		{
			rv = true;
		}

		else if ("postmaster".equalsIgnoreCase(userId))
		{
			rv = true;
		}

		// if the user has site modification rights in the "!admin" site, welcome aboard!
		else
		{
			if (authzGroupService().isAllowed(userId, SiteService.SECURE_UPDATE_SITE, "/site/!admin"))
			{
				rv = true;
			}
		}

		// cache
		if (m_callCache != null)
		{
			Collection<String> azgIds = new HashSet<String>();
			azgIds.add("/site/!admin");
			addToCache(command, rv, true);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean unlock(String lock, String resource)
	{
		return unlock(userDirectoryService().getCurrentUser(), lock, resource);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean unlock(User u, String function, String entityRef)
	{
		// pick up the current user if needed
		User user = u;
		if (user == null)
		{
			user = userDirectoryService().getCurrentUser();
		}
		return unlock(user.getId(), function, entityRef);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean unlock(String userId, String function, String entityRef)
	{
		return unlock(userId, function, entityRef, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean unlock(String userId, String function, String entityRef, Collection<String> azgs)
	{
		// make sure we have complete parameters (azgs is optional)
		if (userId == null || function == null || entityRef == null)
		{
			M_log.warn("unlock(): null: " + userId + " " + function + " " + entityRef);
			return false;
		}

		// if super, grant
		if (isSuperUser(userId))
		{
			return true;
		}

		// let the advisors have a crack at it, if we have any
		// Note: this cannot be cached without taking into consideration the exact advisor configuration -ggolden
		if (hasAdvisors())
		{
			SecurityAdvisor.SecurityAdvice advice = adviseIsAllowed(userId, function, entityRef);
			if (advice != SecurityAdvisor.SecurityAdvice.PASS)
			{
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
	protected boolean checkAuthzGroups(String userId, String function, String entityRef, Collection<String> azgs)
	{
		// check the cache
		String command = makeCacheKey(userId, function, entityRef, false);
		
		if (m_callCache != null)
		{
			final Boolean value = getFromCache(command, false);
			if(value != null) return value.booleanValue();
		}

		// get this entity's AuthzGroups if needed
		if (azgs == null)
		{
			// make a reference for the entity
			Reference ref = entityManager().newReference(entityRef);

			azgs = ref.getAuthzGroups(userId);
		}

		boolean rv = authzGroupService().isAllowed(userId, function, azgs);

		// cache
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
	 * @return A List (User) of the users can unlock the lock (may be empty).
	 */
	@SuppressWarnings("unchecked")
	public List<User> unlockUsers(String lock, String reference)
	{
		if (reference == null)
		{
			M_log.warn("unlockUsers(): null resource: " + lock);
			return new Vector<User>();
		}

		// make a reference for the resource
		Reference ref = entityManager().newReference(reference);

		// get this resource's Realms
		Collection<String> realms = ref.getAuthzGroups();

		// get the users who can unlock in these realms
		List<String> ids = new Vector<String>();
		ids.addAll(authzGroupService().getUsersIsAllowed(lock, realms));

		// convert the set of Users into a sorted list of users
		List<User> users = userDirectoryService().getUsers(ids);
		Collections.sort(users);

		return users;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * SecurityAdvisor Support
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Get the thread-local security advisor stack, possibly creating it
	 * 
	 * @param force
	 *        if true, create if missing
	 */
	@SuppressWarnings("unchecked")
	protected Stack<SecurityAdvisor>  getAdvisorStack(boolean force)
	{
		Stack<SecurityAdvisor>  advisors = (Stack<SecurityAdvisor>) threadLocalManager().get(ADVISOR_STACK);
		if ((advisors == null) && force)
		{
			advisors = new Stack<SecurityAdvisor>();
			threadLocalManager().set(ADVISOR_STACK, advisors);
		}

		return advisors;
	}

	/**
	 * Remove the thread-local security advisor stack
	 */
	protected void dropAdvisorStack()
	{
		threadLocalManager().set(ADVISOR_STACK, null);
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
	 * @return ALLOWED or NOT_ALLOWED if an advisor makes a decision, or PASS if there are no advisors or they cannot make a decision.
	 */
	protected SecurityAdvisor.SecurityAdvice adviseIsAllowed(String userId, String function, String reference)
	{
		Stack<SecurityAdvisor>  advisors = getAdvisorStack(false);
		if ((advisors == null) || (advisors.isEmpty())) return SecurityAdvisor.SecurityAdvice.PASS;

		// a Stack grows to the right - process from top to bottom
		for (int i = advisors.size() - 1; i >= 0; i--)
		{
			SecurityAdvisor advisor = (SecurityAdvisor) advisors.elementAt(i);

			SecurityAdvisor.SecurityAdvice advice = advisor.isAllowed(userId, function, reference);
			if (advice != SecurityAdvisor.SecurityAdvice.PASS)
			{
				return advice;
			}
		}

		return SecurityAdvisor.SecurityAdvice.PASS;
	}

	/**
	 * {@inheritDoc}
	 */
	public void pushAdvisor(SecurityAdvisor advisor)
	{
		Stack<SecurityAdvisor>  advisors = getAdvisorStack(true);
		advisors.push(advisor);
	}

	/**
	 * {@inheritDoc}
	 */
	public SecurityAdvisor popAdvisor(SecurityAdvisor advisor)
	{
		Stack<SecurityAdvisor> advisors = getAdvisorStack(false);
		if (advisors == null) return null;

		SecurityAdvisor rv = null;

		if (advisors.size() > 0)
		{
			if (advisor == null) 
			{
				rv = (SecurityAdvisor) advisors.pop();
			}
			else
			{
				SecurityAdvisor sa = advisors.firstElement();
				if (advisor.equals(sa))
				{
					rv = (SecurityAdvisor) advisors.pop();
				}
			}
		}

		if (advisors.isEmpty())
		{
			dropAdvisorStack();
		}

		return rv;
	}

	public SecurityAdvisor popAdvisor()
	{
		return popAdvisor(null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasAdvisors()
	{
		Stack<SecurityAdvisor>  advisors = getAdvisorStack(false);
		if (advisors == null) return false;

		return !advisors.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearAdvisors()
	{
		dropAdvisorStack();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean setUserEffectiveRole(String azGroupId, String role) {
		
		if (!unlock(SiteService.SITE_ROLE_SWAP, azGroupId))
			return false;
		
		// set the session attribute with the roleid
		sessionManager().getCurrentSession().setAttribute(ROLESWAP_PREFIX + azGroupId, role); 
		resetSecurityCache(azGroupId);

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserEffectiveRole(String azGroupId) {
		
		if (azGroupId == null || "".equals(azGroupId))
			return null;
		
		return (String) sessionManager().getCurrentSession().getAttribute(ROLESWAP_PREFIX + azGroupId);
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearUserEffectiveRole(String azGroupId) {
	
		// remove the attribute from the session
		sessionManager().getCurrentSession().removeAttribute(ROLESWAP_PREFIX + azGroupId);
		resetSecurityCache(azGroupId);
		
		return;
	}
	
	/**
	 * {@inheritDoc}
	 */
	
	public void clearUserEffectiveRoles() {
		
		// get all the roleswaps from the session and clear them
		
		Session session = sessionManager().getCurrentSession();
		
		for (Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements();)
		{
			String name = e.nextElement();
			if (name.startsWith(ROLESWAP_PREFIX)) {
				clearUserEffectiveRole(name.substring(ROLESWAP_PREFIX.length()));
			}
		}
		
		return;
	}
	
	/**
	 * Clear the results of security lookups involving the given authz group from the security lookup cache.
	 * 
	 * @param azGroupId
	 *        The authz group id.
	 */
	protected void resetSecurityCache(String azGroupId) {
		
		// This will clear all cached security lookups involving this realm, thereby forcing the permissions to be rechecked.
	
		// We could turn this into a SessionStateBindingListener so it gets called automatically when
		// the session is cleared.
		String realmRef = org.sakaiproject.authz.api.AuthzGroupService.REFERENCE_ROOT + Entity.SEPARATOR + azGroupId;
		eventTrackingService().post(eventTrackingService().newEvent(EVENT_ROLESWAP_CLEAR, realmRef, true));

		cacheRealmPermsChanged(realmRef, null, null);
	}

	@Override
	public void update(Observable o, Object obj) {
		if (obj == null || !(obj instanceof Event))
		{
			return;
		}

		Event event = (Event) obj;
		
		if (SiteService.EVENT_SITE_USER_INVALIDATE.equals(event.getEvent()))
		{
			Site site = null;
			try {
				site = siteService().getSite(event.getResource());
			} catch (IdUnusedException e) {
				M_log.warn("Security invalidation error when handling an event (" + event.getEvent() + "), for site " + event.getResource());
			}
			if (site != null) {
				resetSecurityCache(site.getReference());
			}
		}
	}
}
