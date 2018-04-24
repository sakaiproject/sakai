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

package org.sakaiproject.site.impl;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.authz.api.*;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.*;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.*;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.*;

/**
 * <p>
 * BaseSiteService is a base implementation of the SiteService.
 * </p>
 */
@Slf4j
public abstract class BaseSiteService implements SiteService, Observer
{
	/**
	 * Security advisor when updating sites. We only have one so we can check we pop the same one off the stack
	 * that we put on.
	 */
	private final static SecurityAdvisor ALLOW_ADVISOR;

	static {
		ALLOW_ADVISOR = new SecurityAdvisor(){
			public SecurityAdvice isAllowed(String userId, String function, String reference)
			{
				return SecurityAdvice.ALLOWED;
			}
		};
	}

	/** The layouts in human readable form (localized) */
	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.SiteImplProperties";
	private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.siteimpl.site-impl";
	private static final String RESOURCECLASS = "resource.class.siteimpl";
	private static final String RESOURCEBUNDLE = "resource.bundle.siteimpl";
	private static final String ORIGINAL_SITE_ID_PROPERTY = "original-site-id";

	private ResourceLoader rb = null;
	// protected ResourceLoader rb = new ResourceLoader("site-impl");

	/** Storage manager for this service. */
	private Storage m_storage = null;

	/** The initial portion of a relative access point URL. */
	protected String m_relativeAccessPoint = null;

	/** A site cache. */
	protected SiteCache m_siteCache = null;

	/** The name/bean for the User-Site cache. */
	protected static final String USER_SITE_CACHE = "org.sakaiproject.site.api.SiteService.userSiteCache";

	/** Cache for sites accessible to a given user. */
	protected Cache m_userSiteCache = null;

	/** A set of observers watching site removals **/
	protected Set<SiteRemovalAdvisor> siteRemovalAdvisors;

	/** A list of observers watching site save events **/
	protected List<SiteAdvisor> siteAdvisors;
        
    /** sfoster9@uwo.ca - A delegate class to contain the join methods **/
    protected JoinSiteDelegate joinSiteDelegate;

	/** SAK-29138 - a site title advisor **/
	protected SiteTitleAdvisor m_siteTitleAdvisor;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Abstractions, etc.
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct storage for this service.
	 */
	protected abstract Storage newStorage();

	/**
	 * Access the partial URL that forms the root of resource URLs.
	 * 
	 * @param relative
	 *        if true, form within the access path only (i.e. starting with /content)
	 * @return the partial URL that forms the root of resource URLs.
	 */
	protected String getAccessPoint(boolean relative)
	{
		return (relative ? "" : serverConfigurationService().getAccessUrl()) + m_relativeAccessPoint;
	}

	/**
	 * Access the site id extracted from a site reference.
	 * 
	 * @param ref
	 *        The site reference string.
	 * @return The the site id extracted from a site reference.
	 */
	protected String siteId(String ref)
	{
		String start = getAccessPoint(true) + Entity.SEPARATOR;
		int i = ref.indexOf(start);
		if (i == -1) return ref;
		String id = ref.substring(i + start.length());
		return id;
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @return true if allowd, false if not
	 */
	protected boolean unlockCheck(String lock, String resource)
	{
		if (!securityService().unlock(lock, resource))
		{
			return false;
		}

		return true;
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @exception PermissionException
	 *            Thrown if the user does not have access
	 */
	protected void unlock(String lock, String resource) throws PermissionException
	{
		if (!unlockCheck(lock, resource))
		{
			throw new PermissionException(sessionManager().getCurrentSessionUserId(), lock, resource);
		}
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock1
	 *        The lock id string.
	 * @param lock2
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @return true if either allowed, false if not
	 */
	protected boolean unlockCheck2(String lock1, String lock2, String resource)
	{
		if (!securityService().unlock(lock1, resource))
		{
			if (!securityService().unlock(lock2, resource))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock1
	 *        The lock id string.
	 * @param lock2
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @exception PermissionException
	 *            Thrown if the user does not have access to either.
	 */
	protected void unlock2(String lock1, String lock2, String resource) throws PermissionException
	{
		if (!unlockCheck2(lock1, lock2, resource))
		{
			throw new PermissionException(sessionManager().getCurrentSessionUserId(), lock1 + "/" + lock2, resource);
		}
	}

	/**
	 * Update the live properties for a site for when modified.
	 */
	protected void addLiveUpdateProperties(BaseSite site)
	{
		String current = sessionManager().getCurrentSessionUserId();

		site.m_lastModifiedUserId = current;
		site.m_lastModifiedTime = timeService().newTime();
	}

	/**
	 * Create the live properties for the site.
	 */
	protected void addLiveProperties(BaseSite site)
	{
		String current = sessionManager().getCurrentSessionUserId();

		site.m_createdUserId = current;
		site.m_lastModifiedUserId = current;

		Time now = timeService().newTime();
		site.m_createdTime = now;
		site.m_lastModifiedTime = (Time) now.clone();
	}

	/**
	 * Return the url unchanged, unless it's a reference, then return the reference url
	 */
	protected String convertReferenceUrl(String url)
	{
		// make a reference
		Reference ref = entityManager().newReference(url);

		// if it didn't recognize this, return it unchanged
		if (!ref.isKnownType()) return url;

		// return the reference's url
		return ref.getUrl();
	}

	/**
	 * Regenerate the page and tool ids for all sites.
	 */
	protected void regenerateAllSiteIds()
	{
		List<Site> sites = storage().getAll();
		for (Iterator<Site> iSites = sites.iterator(); iSites.hasNext();)
		{
			Site site = (Site) iSites.next();
			if (site != null)
			{
				Site edit = storage().get(site.getId());
				edit.regenerateIds();
				storage().save(edit);

				log.info("regenerateAllSiteIds: site: " + site.getId());
			}
			else
			{
				log.warn("regenerateAllSiteIds: null site in list");
			}
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Configuration
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** If true, run the regenerate ids pass on all sites at startup. */
	protected boolean m_regenerateIds = false;

	/**
	 * Configuration: regenerate all site;'s page and tool ids to assure uniqueness.
	 * 
	 * @param value
	 *        The regenerate ids value
	 */
	public void setRegenerateIds(String value)
	{
		m_regenerateIds = Boolean.valueOf(value).booleanValue();
	}

	/** The # seconds to cache the site queries. 0 disables the cache. */
	protected int m_cacheSeconds = 3 * 60;

	/**
	 * Set the # minutes to cache the site queries.
	 * 
	 * @param time
	 *        The # minutes to cache the site queries (as an integer string).
	 */
	public void setCacheMinutes(String time)
	{
		m_cacheSeconds = Integer.parseInt(time) * 60;
	}

	/** The # seconds to cache gets. 0 disables the cache. */
	protected int m_cacheCleanerSeconds = 15 * 60;

	/**
	 * Set the # minutes between cache cleanings.
	 * 
	 * @param time
	 *        The # minutes between cache cleanings. (as an integer string).
	 */
	public void setCacheCleanerMinutes(String time)
	{
		m_cacheCleanerSeconds = Integer.parseInt(time) * 60;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected abstract ServerConfigurationService serverConfigurationService();

	/**
	 * @return the EntityManager collaborator.
	 */
	protected abstract EntityManager entityManager();

	/**
	 * @return the EventTrackingService collaborator.
	 */
	protected abstract EventTrackingService eventTrackingService();

	/**
	 * @return the ThreadLocalManager collaborator.
	 */
	protected abstract ThreadLocalManager threadLocalManager();

	/**
	 * @return the SecurityService collaborator.
	 */
	protected abstract SecurityService securityService();

	/**
	 * @return the SessionManager collaborator.
	 */
	protected abstract SessionManager sessionManager();

	/**
	 * @return the TimeService collaborator.
	 */
	protected abstract TimeService timeService();

	/**
	 * @return the FunctionManager collaborator.
	 */
	protected abstract FunctionManager functionManager();

	/**
	 * @return the MemoryService collaborator.
	 */
	protected abstract MemoryService memoryService();

	/**
	 * @return the UserDirectoryService collaborator.
	 */
	protected abstract UserDirectoryService userDirectoryService();

	/**
	 * @return the AuthzGroupService collaborator.
	 */
	protected abstract AuthzGroupService authzGroupService();
	
	/**
	 * @return the ActiveToolManager collaborator.
	 */
	protected abstract ActiveToolManager activeToolManager();
	
	/**
	 * @return the IdManager collaborator.
	 */
	protected abstract IdManager idManager();
	
	/**
	 * 
	 * @return the NotificationService collaborator
	 */
	protected abstract NotificationService notificationService();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		siteAdvisors = new ArrayList<SiteAdvisor>();
		// Concurrent so that we never get ConcurrentModificationException when iterating.
		siteRemovalAdvisors = new CopyOnWriteArraySet<SiteRemovalAdvisor>();

		try
		{
			// Get resource bundle
			String resourceClass = serverConfigurationService().getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
			String resourceBundle = serverConfigurationService().getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
			rb = new Resource().getLoader(resourceClass, resourceBundle);
			
			m_relativeAccessPoint = REFERENCE_ROOT;

			// construct storage and read
			m_storage = newStorage();
			storage().open();

			if (m_regenerateIds)
			{
				regenerateAllSiteIds();
				m_regenerateIds = false;
			}

			// <= 0 minutes indicates no caching desired
			if (m_cacheSeconds > 0)
			{
				m_siteCache = new SiteCacheSafe(memoryService(), eventTrackingService());
			}

			// Register our user-site cache property
			serverConfigurationService().registerConfigItem(BasicConfigItem.makeDefaultedConfigItem(PROP_CACHE_USER_SITES, true, "org.sakaiproject.api.SiteService"));

			// Get the user-site cache from the MemoryService for now -- maybe directly from cache manager or Spring later.
			// Also register as an observer so we can catch site updates and invalidate.
			if (serverConfigurationService().getBoolean(PROP_CACHE_USER_SITES, true))
			{
				m_userSiteCache = memoryService().newCache(USER_SITE_CACHE);
				eventTrackingService().addObserver(this);
			}

			// register as an entity producer
			entityManager().registerEntityProducer(this, REFERENCE_ROOT);

			// register functions
			functionManager().registerFunction(SITE_ROLE_SWAP);
			functionManager().registerFunction(SITE_VISIT);
			functionManager().registerFunction(SITE_VISIT_UNPUBLISHED);
			functionManager().registerFunction(SECURE_ADD_SITE);
			functionManager().registerFunction(SECURE_ADD_USER_SITE);
			functionManager().registerFunction(SECURE_ADD_PORTFOLIO_SITE);
			functionManager().registerFunction(SECURE_REMOVE_SITE);
			functionManager().registerFunction(SECURE_UPDATE_SITE);
			functionManager().registerFunction(SECURE_VIEW_ROSTER);
			functionManager().registerFunction(SECURE_UPDATE_SITE_MEMBERSHIP);
			functionManager().registerFunction(SECURE_UPDATE_GROUP_MEMBERSHIP);
			functionManager().registerFunction(SECURE_ADD_COURSE_SITE);
			functionManager().registerFunction(SITE_VISIT_SOFTLY_DELETED);
			functionManager().registerFunction(SECURE_REMOVE_SOFTLY_DELETED_SITE);
			functionManager().registerFunction(SECURE_ADD_PROJECT_SITE);
			functionManager().registerFunction(SECURE_IMPORT_ARCHIVE);
			
                        
            // sfoster9@uwo.ca
            // assign a new JoinSiteDelegate to handle the join methods; provide it services from this class
            joinSiteDelegate = new JoinSiteDelegate( this, securityService(), userDirectoryService() );
			
			// SAK-29138
			m_siteTitleAdvisor = (SiteTitleAdvisor) ComponentManager.get( SiteTitleAdvisor.class );
		}
		catch (Exception t)
		{
			log.error(".init(): ", t);
		}
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		storage().close();
		m_storage = null;

		// Stop listening for site update events
		eventTrackingService().deleteObserver(this);

		log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * SiteService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public String[] getLayoutNames()
	{
		String[] rv = new String[2];
		rv[0] = rb.getString("sitpag.lay_sngl");
		rv[1] = rb.getString("sitpag.lay_dbl");
		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public boolean allowAccessSite(String id)
	{
		boolean rv = false;

		try
		{
			Site site = getSite(id);
			
			allowAccessSite(site);
			rv = true;
		}
		catch (Exception ignore)
		{
			// Not needed but makes the code clearer.
			rv = false;
		}

		return rv;
	}
	
	/**
	 * Checks to see if the current user has access to the site and throws an exception if they don't.
	 * This was extracted to keep the code common to getSiteVisit and allowSiteAccess
	 * @throws PermissionException If the user isn't allowed to access the site.
	 */
	protected void allowAccessSite(Site site) throws PermissionException
	{
		if (site.isSoftlyDeleted())
		{
			unlock(SITE_VISIT_SOFTLY_DELETED, site.getReference());
		}
		else
		{
			if (site.isPublished())
			{
				unlock(SITE_VISIT, site.getReference());
			}
			else
			{
				String roleswap = securityService().getUserEffectiveRole(site.getReference());
				if (roleswap!=null) // if in a swapped mode, treat it as a normal site else do the normal unpublished c
					unlock(SITE_VISIT, site.getReference());
				else
					unlock(SITE_VISIT_UNPUBLISHED, site.getReference());
			}
		}
	}


	/**
	 * Access site object from Cache (if available)
	 * 
	 * @param id
	 *        The site id string.
	 * @return A site object containing the site information or null
	 *            if not found
	 */
	protected Site getCachedSite(String id) 
	{
		if (id == null) return null;

		Site rv = null;

		// check the cache
		String ref = siteReference(id);
		if (m_siteCache != null)
		{
			// some cached things are Booleans (site exists), not sites
			Object o = m_siteCache.get(ref);
			if ((o != null) && (o instanceof Site))
			{
				rv = (Site) o;

				// return a copy of the site from the cache
				rv = new BaseSite(this,rv, true);

				return rv;
			}
		}
		return null;
	}

	/**
	 * Cache a copy of a site if caching is enabled.
	 *
	 * @param site the Site to cache
	 * @return true when the site was cached, false when the site is null or caching is disabled
	 */
	protected boolean cacheSite(Site site)
	{
		if (site != null && m_siteCache != null)
		{
			String ref = siteReference(site.getId());
			Site copy = new BaseSite(this, site, true);
			m_siteCache.put(ref, copy);
			return true;
		}
		return false;
	}

	/**
	 * Access an already defined site object.
	 * 
	 * @param id
	 *        The site id string.
	 * @return A site object containing the site information
	 * @exception IdUnusedException
	 *            if not found
	 */
	protected Site getDefinedSite(String id) throws IdUnusedException
	{
		if (id == null) throw new IdUnusedException("<null>");

		Site rv = getCachedSite(id);

		// Return the site from cache only if it is a BaseSite and is fully loaded.
		//
		// Note that getCachedSite always returns a BaseSite instance now, so
		// this instanceof check is not strictly necessary, but paranoid. If
		// the cast would fail, we have to retrieve the site. This is slightly
		// kludgy because the caching and lazy-loading are somewhat bolted on.
		if ( rv != null && rv instanceof BaseSite && ((BaseSite)rv).isFullyLoaded()) return rv;

		// Get the whole site, including the description.
		rv = storage().get(id);

		// if not found
		if (rv == null) throw new IdUnusedException(id);

		// get all of the site loaded
		rv.loadAll();

		// track it - we don't track site access -ggolden
		// EventTrackingService.post(EventTrackingService.newEvent(SECURE_ACCESS_SITE, site.getReference()));

		// cache a copy
		cacheSite(rv);

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public boolean siteExists(String id)
	{
		if (id != null)
		{
			// check the cache
			String ref = siteReference(id);
			if (m_siteCache != null)
			{
				Object o = m_siteCache.get(ref);
				if (o != null)
				{
					if (o instanceof Site)
					{
						return true;
					}

					if (o instanceof Boolean)
					{
						// misses are cached, too
						return o == Boolean.TRUE;
					}
				}
			}

			// check the exists cache
			if (storage().check(id))
			{
				// cache it
				if (m_siteCache != null)
				{
					m_siteCache.put(siteReference(id), Boolean.TRUE);
				}

				return true;
			}

			else
			{
				// cache the miss
				if (m_siteCache != null)
				{
					m_siteCache.put(siteReference(id), Boolean.FALSE);
				}
			}
		}

		return false;
	}

	/**
	 * @inheritDoc
	 */
	public Site getSite(String id) throws IdUnusedException
	{
		if (id == null)
		{
			throw new IdUnusedException("null");
		}

		try
		{
			return getDefinedSite(id);
		}
		catch (IdUnusedException e)
		{
			// if this is the current user's site, we can create it
			if (isUserSite(id) && id.substring(1).equals(sessionManager().getCurrentSessionUserId()))
			{
				// pick a template, type based, to clone it exactly but set this as the id
				BaseSite template = null;
				try
				{
					User user = userDirectoryService().getUser(sessionManager().getCurrentSessionUserId());
					template = (BaseSite) getDefinedSite(USER_SITE_TEMPLATE + "." + user.getType());
				}
				catch (Exception t)
				{
				}

				// if a type based template was not found, use the generic one
				// will throw IdUnusedException all the way out of this method if that's not defined
				if (template == null)
				{
					template = (BaseSite) getDefinedSite(USER_SITE_TEMPLATE);
				}

				// reserve a site with this id from the info store - if it's in use, this will return null
				try
				{
					// check security (throws if not permitted)
					unlock(SECURE_ADD_USER_SITE, siteReference(id));

					// reserve a site with this id from the info store - if it's in use, this will return null
					BaseSite site = (BaseSite) storage().put(id);
					if (site == null)
					{
						throw new IdUsedException(id);
					}

					site.setEvent(SECURE_ADD_SITE);

					// copy in the template
					site.set(template, false);
					
					// Localize the page & tool titles
					for ( Iterator it=site.getPages().iterator(); it.hasNext(); )
					{
						SitePage page = (SitePage)it.next();
						if (!page.getTitleCustom()) {
							page.localizePage();
						}
					}

					doSave(site, true);

					return site;
				}
				catch (IdUsedException ee)
				{
					throw e;
				}
				catch (PermissionException ee)
				{
					throw e;
				}
			}
			else
			{
				throw e;
			}
		}
	}
   
	/**
	 * @inheritDoc
	 */
	public Site getSiteVisit(String id) throws IdUnusedException, PermissionException
	{
		// get the site
		Site rv = getSite(id);
		
		// Check is user has access, throws PermissionException if the user doesn't
		allowAccessSite(rv);

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public boolean allowUpdateSite(String id)
	{
		return unlockCheck(SECURE_UPDATE_SITE, siteReference(id));
	}

	/**
	 * @inheritDoc
	 */
	public boolean allowUpdateSiteMembership(String id)
	{
		return unlockCheck(SECURE_UPDATE_SITE_MEMBERSHIP, siteReference(id));
	}

	/**
	 * @inheritDoc
	 */
	public boolean allowUpdateGroupMembership(String id)
	{
		return unlockCheck(SECURE_UPDATE_GROUP_MEMBERSHIP, siteReference(id));
	}

	/**
	 * @inheritDoc
	 */
	public boolean allowRoleSwap(String id)
	{
		return unlockCheck(SITE_ROLE_SWAP, siteReference(id));
	}
	
	/**
	 * @inheritDoc
	 */
	public void save(Site site) throws IdUnusedException, PermissionException
	{
		if (site.getId() == null) throw new IdUnusedException("<null>");

		String siteRef = site.getReference();

		if (!unlockCheck(SECURE_UPDATE_GROUP_MEMBERSHIP, siteRef) && !unlockCheck(SECURE_UPDATE_SITE_MEMBERSHIP, siteRef))
		{
			// check security (throws if not permitted)
			unlock(SECURE_UPDATE_SITE, siteRef);
		}

		// check for existance
		if (!storage().check(site.getId()))
		{
			throw new IdUnusedException(site.getId());
		}
		
		// Save the site
		doSave((BaseSite) site, false);
	}

	/**
	 * @inheritDoc
	 */
	public void saveSiteMembership(Site site) throws IdUnusedException, PermissionException
	{
		if (site.getId() == null) throw new IdUnusedException("<null>");

		// check security (throws if not permitted)
		unlock2(SECURE_UPDATE_SITE_MEMBERSHIP, SECURE_UPDATE_SITE, site.getReference());

		// check for existance
		if (!storage().check(site.getId()))
		{
			throw new IdUnusedException(site.getId());
		}

		try
		{
			enableAzgSecurityAdvisor();
			saveSiteAzg(site);
		}
		finally
		{
			disableAzgSecurityAdvisor();
		}

		// track it
		eventTrackingService().post(eventTrackingService().newEvent(SECURE_UPDATE_SITE_MEMBERSHIP, site.getReference(), true));
	}

	/**
	 * @inheritDoc
	 */
	public void saveGroupMembership(Site site) throws IdUnusedException, PermissionException
	{
		if (site.getId() == null) throw new IdUnusedException("<null>");

		// check security (throws if not permitted)
		unlock2(SECURE_UPDATE_GROUP_MEMBERSHIP, SECURE_UPDATE_SITE, site.getReference());

		// check for existance
		if (!storage().check(site.getId()))
		{
			throw new IdUnusedException(site.getId());
		}

		try
		{
			enableAzgSecurityAdvisor();
			saveGroupAzgs(site);
		}
		finally
		{
			disableAzgSecurityAdvisor();
		}

		// track it
		eventTrackingService().post(eventTrackingService().newEvent(SECURE_UPDATE_GROUP_MEMBERSHIP, site.getReference(), true));
	}

	/**
	 * Comlete the save process.
	 * 
	 * @param site
	 *        The site to save.
	 */
	protected void doSave(BaseSite site, boolean isNew)
	{
		if (isNew)
		{
			addLiveProperties(site);
		}

		// update the properties
		addLiveUpdateProperties(site);

		// Give the site advisors, if any, a chance to make last minute changes to the site
		for(Iterator<SiteAdvisor> iter = siteAdvisors.iterator(); iter.hasNext();) {
			try {
				iter.next().update(site);
			}
			catch (Exception e)
			{
				log.error("Advisor error in doSave()", e);
			}
		}

		site.setFullyLoaded(true);

		// complete the edit
		storage().save(site);
		
		// Invalidate the user-site cache.
		Site cached = getCachedSite(site.getId());
		if (cached != null ) {
			clearUserCacheForSite(site);
		}
		cacheSite(site);

		// save any modified azgs
		try
		{
			enableAzgSecurityAdvisor();
			saveSiteAzg(site);
			saveGroupAzgs(site);
		}
		finally
		{
			disableAzgSecurityAdvisor();
		}

		// sync up with all other services
		// TODO: do this under the security advisor, too, so we don't need all the various service security on site creation? -ggolden
		enableRelated(site, isNew);

		// track it
		String event = site.getEvent();
		if (event == null) event = SECURE_UPDATE_SITE;
		eventTrackingService().post(eventTrackingService().newEvent(event, site.getReference(), true));

		// clear the event for next time
		site.setEvent(null);
	}

	/**
	 * Establish a security advisor to allow the "embedded" azg work to occur with no need for additional security permissions.
	 */
	protected void enableAzgSecurityAdvisor()
	{
		// put in a security advisor so we can do our azg work without need of further permissions
		// TODO: could make this more specific to the AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP permission -ggolden
		securityService().pushAdvisor(ALLOW_ADVISOR);
	}

	/**
	 * Disabled the security advisor.
	 */
	protected void disableAzgSecurityAdvisor()
	{
		SecurityAdvisor popped = securityService().popAdvisor();
		if (!ALLOW_ADVISOR.equals(popped)) {
			if (popped == null)
			{
				log.warn("Someone has removed our advisor.");
			}
			else
			{
				log.warn("Removed someone elses advisor, adding it back.");
				securityService().pushAdvisor(popped);
			}
		}
	}

	/**
	 * Save the site's azg if modified.
	 * 
	 * @param site
	 *        The site to save.
	 */
	protected void saveSiteAzg(Site site)
	{
		if (((BaseSite) site).m_azgChanged)
		{
			try
			{
				authzGroupService().save(((BaseSite) site).m_azg);
			}
			catch (Exception t)
			{
				log.warn(".saveAzgs - site: " + t);
			}
			((BaseSite) site).m_azgChanged = false;
		}
	}

	/**
	 * Save group azgs that are modified.
	 * 
	 * @param site
	 *        The site to save.
	 */
	protected void saveGroupAzgs(Site site)
	{
		for (Iterator i = site.getGroups().iterator(); i.hasNext();)
		{
			BaseGroup group = (BaseGroup) i.next();
			if (group.m_azgChanged)
			{
				try
				{
					authzGroupService().save(group.m_azg);
				}
				catch (Exception t)
				{
					log.warn(".saveAzgs - group: " + group.getTitle() + " : " + t);
				}
				group.m_azgChanged = false;
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public void saveSiteInfo(String id, String description, String infoUrl) throws IdUnusedException, PermissionException
	{
		Site site = getSite(id);
		site.setDescription(description);
		site.setInfoUrl(infoUrl);
		save(site);
	}

	/**
	 * @inheritDoc
	 */
	public boolean allowAddSite(String id)
	{
		// check security (throws if not permitted)
		if (id != null && isUserSite(id))
		{
			return unlockCheck(SECURE_ADD_USER_SITE, siteReference(id));
		}
		else if (id != null && isCourseSite(id)) {
			return unlockCheck(SECURE_ADD_COURSE_SITE, siteReference(id));
		}
		else if (id != null && isPortfolioSite(id)) {
			return unlockCheck(SECURE_ADD_PORTFOLIO_SITE, siteReference(id));
		}
		else if (id != null && isProjectSite(id)) {
			return unlockCheck(SECURE_ADD_PROJECT_SITE, siteReference(id));
		}
		else
		{
			return unlockCheck(SECURE_ADD_SITE, siteReference(id));
		}
	}
	
	/**
	 * read the site Type definition from configuration files
	 */
	public List<String> getSiteTypeStrings(String type)
	{
		String[] siteTypes = serverConfigurationService().getStrings(type + "SiteType");
		if (siteTypes == null || siteTypes.length == 0)
		{
			siteTypes = new String[] {type};
		}
		return Arrays.asList(siteTypes);
	}

	private boolean isCourseSite(String siteId) {
		boolean rv = false;
		try {
			Site s = getSite(siteId);
			List<String> courseSiteTypes = getSiteTypeStrings("course");
			if (courseSiteTypes.contains(s.getType())) 
				return true;
				
		} catch (IdUnusedException e) {
			log.warn("isCourseSite(): no site with id: " + siteId);
		}
		
		return rv;
	}

	private boolean isPortfolioSite(String siteId) {
		boolean rv = false;
		try {
			Site s = getSite(siteId);
			List<String> portfolioSiteTypes = getSiteTypeStrings("portfolio");
			if (portfolioSiteTypes.contains(s.getType())) 
				return true;
				
		} catch (IdUnusedException e) {
			log.warn("isPortfolioSite(): no site with id: " + siteId);
		}
		
		return rv;
	}
	
	private boolean isProjectSite(String siteId) {
		boolean rv = false;
		try {
			Site s = getSite(siteId);
			List<String> projectSiteTypes = getSiteTypeStrings("project");
			if (projectSiteTypes.contains(s.getType())) 
				return true;
				
		} catch (IdUnusedException e) {
			log.warn("isProjectSite(): no site with id: " + siteId);
		}
		
		return rv;
	}
	
	public boolean allowAddCourseSite() {
		return unlockCheck(SECURE_ADD_COURSE_SITE, siteReference(null));
	}

	public boolean allowAddPortfolioSite() {
		return unlockCheck(SECURE_ADD_PORTFOLIO_SITE, siteReference(null));
	}
	
	public boolean allowAddProjectSite() {
		return unlockCheck(SECURE_ADD_PROJECT_SITE, siteReference(null));
	}

	public boolean allowImportArchiveSite() {
		return unlockCheck(SECURE_IMPORT_ARCHIVE, siteReference(null));
	}
	
	/**
	 * @inheritDoc
	 */
	public Site addSite(String id, String type) throws IdInvalidException, IdUsedException, PermissionException
	{
		// check for a valid site id
		if (!Validator.checkResourceId(id)) {
			throw new IdInvalidException("Id " + id + " is not a valid id format");
		}

		id = Validator.escapeResourceName(id);

		// check for a valid site type
		if (!Validator.checkSiteType(type)) {
			throw new IdInvalidException("Type " + type + " is not a valid type format");			
		}
		
		// check security (throws if not permitted)
		unlock(SECURE_ADD_SITE, siteReference(id));
		
		
		// SAK-12631
		if (getSiteTypeStrings("course").contains(type)) {
			unlock(SECURE_ADD_COURSE_SITE, siteReference(id));
		}

		// KNL-703
		if (getSiteTypeStrings("portfolio").contains(type)) {
			unlock(SECURE_ADD_PORTFOLIO_SITE, siteReference(id));
		}
		
		// KNL-952
		if (getSiteTypeStrings("project").contains(type)) {
			unlock(SECURE_ADD_PROJECT_SITE, siteReference(id));
		}

		// reserve a site with this id from the info store - if it's in use, this will return null
		Site site = storage().put(id);
		if (site == null)
		{
			throw new IdUsedException(id);
		}

		// set the type before we enable related, since the azg template for the site depends on type
		if (type != null)
		{
			site.setType(type);
		}

		((BaseSite) site).setEvent(SECURE_ADD_SITE);

		doSave((BaseSite) site, true);

		return site;
	}

	/**
	 * @inheritDoc
	 */
	public Site addSite(String id, Site other) throws IdInvalidException, IdUsedException, PermissionException
	{
		// check for a valid site id
		if (!Validator.checkResourceId(id)) {
			throw new IdInvalidException("Id " + id + " is not valid");
		}

		id = Validator.escapeResourceName(id);

		// check security (throws if not permitted)
		if (isUserSite(id))
		{
			unlock(SECURE_ADD_USER_SITE, siteReference(id));
		}
		else
		{
			unlock(SECURE_ADD_SITE, siteReference(id));
		}
		
		// SAK=12631
		if ( isCourseSite(other.getId()) ) {
			unlock(SECURE_ADD_COURSE_SITE, siteReference(id));			
		}

		// KNL-703
		if ( isPortfolioSite(other.getId()) ) {
			unlock(SECURE_ADD_PORTFOLIO_SITE, siteReference(id));			
		}
		
		// KNL-952
		if ( isProjectSite(other.getId()) ) {
			unlock(SECURE_ADD_PROJECT_SITE, siteReference(id));			
		}

		// reserve a site with this id from the info store - if it's in use, this will return null
		Site site = storage().put(id);
		if (site == null)
		{
			throw new IdUsedException(id);
		}

		// make this site a copy of other, but with new ids (not an exact copy)
		((BaseSite) site).set((BaseSite) other, false);

		// copy the realm (to get permissions settings)
		try
		{
			AuthzGroup realm = authzGroupService().getAuthzGroup(other.getReference());
			AuthzGroup re = authzGroupService().addAuthzGroup(site.getReference(), realm,
					userDirectoryService().getCurrentUser().getId());

			// clear the users from the copied realm, adding in the current user as a maintainer
			re.removeMembers();
			re.addMember(userDirectoryService().getCurrentUser().getId(), re.getMaintainRole(), true, false);

			authzGroupService().save(re);
		}
		catch (Exception e)
		{
			log.error(".addSite(): error copying realm", e);
		}

		// clear the site's notification id in properties
		site.getPropertiesEdit().removeProperty(ResourceProperties.PROP_SITE_EMAIL_NOTIFICATION_ID);

		// KNL-1103, store the site we are copying from
		site.getPropertiesEdit().addProperty(ORIGINAL_SITE_ID_PROPERTY, other.getId());

		((BaseSite) site).setEvent(SECURE_ADD_SITE);

		doSave((BaseSite) site, true);

		return site;
	}

	/**
	 * @inheritDoc
	 */
	public boolean allowRemoveSite(String id)
	{
		String lock = SECURE_REMOVE_SITE;
		if(serverConfigurationService().getBoolean("site.soft.deletion", true))
		{
			try
			{
				Site site = getSite(id);
				if (site.isSoftlyDeleted())
				{
					lock = SECURE_REMOVE_SOFTLY_DELETED_SITE;
				}
			}
			catch (IdUnusedException e)
			{
				// Ignore
			}
		}
		return unlockCheck(lock, siteReference(id));
	}

	/**
	 * @inheritDoc
	 */
	public void removeSite(Site site) throws PermissionException, IdUnusedException
	{
		// check security (throws if not permitted)
		unlock(SECURE_REMOVE_SITE, site.getReference());

		// if soft site deletes are active
		if(serverConfigurationService().getBoolean("site.soft.deletion", true)) {
			
			log.debug("Soft site deletes are enabled.");
			
			//KNL-983 only soft delete if not user site
			//made it verbose for logging purposes
			if(isUserSite(site.getId())) {
				log.debug("Site: " + site.getId() + " is user site and will be hard deleted.");
			} else if (isSpecialSite(site.getId())) {
				log.debug("Site: " + site.getId() + " is special site and will be hard deleted.");
			} else {
				log.debug("Site: " + site.getId() + " is not user or special site and will be soft deleted.");
			
				// if site is not already softly deleted, softly delete it
				// if already marked for deletion, check permission to hard delete, if ok, let continue.
				if(!site.isSoftlyDeleted()) {
					site.setSoftlyDeleted(true);
					save(site);
					return;
				} else {
					unlock(SECURE_REMOVE_SOFTLY_DELETED_SITE, site.getReference());
				}
			}
		}

		for (SiteRemovalAdvisor advisor: siteRemovalAdvisors)
		{
			advisor.removed(site);
		}

		// Invalidate the user-site cache.
		Site cached = getCachedSite(site.getId());
		if (cached != null ) {
			clearUserCacheForSite(site);
		}
		
		// complete the edit
		storage().remove(site);

		// track it
		eventTrackingService().post(eventTrackingService().newEvent(SECURE_REMOVE_SITE, site.getReference(), true));

		// get the services related to this site setup for the site's removal
		disableRelated(site);
	}

	/**
	 * @inheritDoc
	 */
	public String siteReference(String id)
	{
		return getAccessPoint(true) + Entity.SEPARATOR + id;
	}

	/**
	 * @inheritDoc
	 */
	public String sitePageReference(String siteId, String pageId)
	{
		return getAccessPoint(true) + Entity.SEPARATOR + siteId + Entity.SEPARATOR + PAGE_SUBTYPE + Entity.SEPARATOR + pageId;
	}

	/**
	 * @inheritDoc
	 */
	public String siteToolReference(String siteId, String toolId)
	{
		return getAccessPoint(true) + Entity.SEPARATOR + siteId + Entity.SEPARATOR + TOOL_SUBTYPE + Entity.SEPARATOR + toolId;
	}

	/**
	 * @inheritDoc
	 */
	public String siteGroupReference(String siteId, String groupId)
	{
		return getAccessPoint(true) + Entity.SEPARATOR + siteId + Entity.SEPARATOR + GROUP_SUBTYPE + Entity.SEPARATOR + groupId;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isUserSite(String site)
	{
		if (site == null) return false;

		// deal with a reference
		if (site.startsWith(siteReference("~")) && (!site.equals(siteReference("~")))) return true;

		// deal with an id
		return (site.startsWith("~") && (!site.equals("~")));
	}

	/**
	 * @inheritDoc
	 */
	public String getSiteUserId(String site)
	{
		// deal with a reference
		String ref = siteReference("~");
		if (site.startsWith(ref))
		{
			return site.substring(ref.length());
		}

		else if (site.startsWith("~"))
		{
			return site.substring(1);
		}

		return null;
	}

	/**
	 * @inheritDoc
	 */
	public String getUserSiteId(String userId)
	{
		return "~" + userId;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isSpecialSite(String site)
	{
		if (site == null) return false;

		// Note: ! is special except if it's !admin, not considered special

		// deal with a reference
		if (site.startsWith(siteReference("!")) && !site.equals(siteReference("!admin"))) return true;

		// TODO: legacy code - we don't use the "~" site anymore (!user.template*) -ggolden
		if (site.equals(siteReference("~"))) return true;

		// deal with an id
		if (site.startsWith("!") && !site.equals("!admin")) return true;

		// TODO: legacy code - we don't use the "~" site anymore (!user.template*) -ggolden
		if (site.equals("~")) return true;

		return false;
	}

	/**
	 * @inheritDoc
	 */
	public String getSiteSpecialId(String site)
	{
		// deal with a reference
		String ref = siteReference("!");
		if (site.startsWith(ref))
		{
			return site.substring(ref.length());
		}

		else if (site.startsWith("!"))
		{
			return site.substring(1);
		}

		return null;
	}

	/**
	 * @inheritDoc
	 */
	public String getSpecialSiteId(String special)
	{
		return "!" + special;
	}

	/**
	 * @inheritDoc
	 */
	public String getSiteDisplay(String id)
	{
		String rv = "(" + id + ")";

		if (isUserSite(id))
		{
			String userName = id;
			try
			{
				User user = userDirectoryService().getUser(getSiteUserId(id));
				userName = user.getDisplayName();
			}
			catch (UserNotDefinedException ignore)
			{
			}

			rv = "\"" + rb.getFormattedMessage("sitdis.usr", new Object[]{userName}) + "\" " + rv;
		}

		else
		{
			Site site = null;
			try
			{
				site = getSite(id);
				rv = "\"" + site.getTitle() + "\" " + rv;
			}
			catch (IdUnusedException ignore)
			{
			}
		}

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public ToolConfiguration findTool(String id)
	{
		if (id == null)
			return null;
		
		ToolConfiguration rv = null;

		// check the site cache
		if (m_siteCache != null)
		{
			rv = m_siteCache.getTool(id);
			if (rv != null)
			{
				// return a copy from the cache
				rv = new BaseToolConfiguration(this, rv, rv.getContainingPage(), true);
				return rv;
			}

			// if not, get the tool's site id, cache the site, and try again
			String siteId = storage().findToolSiteId(id);
			if (siteId != null)
			{
				// read and cache the site, pages, tools, etc.
				try
				{
					Site site = getDefinedSite(siteId);

					// return what we find from the copy we got from the cache
					rv = site.getTool(id);

					return rv;
				}
				catch (IdUnusedException e)
				{
				}
			}

			return null;
		}

		rv = storage().findTool(id);

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public SitePage findPage(String id)
	{
		if (id == null)
			return null;
		
		SitePage rv = null;

		// check the site cache
		if (m_siteCache != null)
		{
			rv = m_siteCache.getPage(id);
			if (rv != null)
			{
				rv = new BaseSitePage(this,rv, rv.getContainingSite(), true);
				return rv;
			}

			// if not, get the page's site id, cache the site, and try again
			String siteId = storage().findPageSiteId(id);
			if (siteId != null)
			{
				// read and cache the site, pages, tools
				try
				{
					Site site = getDefinedSite(siteId);

					// return what we find from the site copy from the cache
					rv = site.getPage(id);
					return rv;
				}
				catch (IdUnusedException e)
				{
				}
			}

			return null;
		}

		rv = storage().findPage(id);

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public boolean allowViewRoster(String id)
	{
		return unlockCheck(SECURE_VIEW_ROSTER, siteReference(id));
	}

	/**
	 * @inheritDoc
	 */
	public void join(String id) throws IdUnusedException, PermissionException
	{
		String user = sessionManager().getCurrentSessionUserId();
		if (user == null) {
		    throw new PermissionException(null, AuthzGroupService.SECURE_UPDATE_OWN_AUTHZ_GROUP, siteReference(id));
		}

		// get the site
		Site site = getDefinedSite(id);

		// must be joinable
		if (!site.isJoinable())
		{
			throw new PermissionException(user, AuthzGroupService.SECURE_UPDATE_OWN_AUTHZ_GROUP, siteReference(id));
		}

		// the role to assign
		String roleId = site.getJoinerRole();
		if (roleId == null)
		{
			log.warn(".join(): null site joiner role for site: " + id);
			throw new PermissionException(user, AuthzGroupService.SECURE_UPDATE_OWN_AUTHZ_GROUP, siteReference(id));
		}

		// sfoster9@uwo.ca
        // once joined, add the user to the specified join group and send an email, if these join options are selected
        // if anything goes wrong with adding to a group or sending email, the join should still finish
		try
		{
			// do the join
			authzGroupService().joinGroup(siteReference(id), roleId);
		}
		catch(GroupNotDefinedException e)
		{
			throw new IdUnusedException(e.getId());
		}
		catch(AuthzPermissionException e)
		{
			throw new PermissionException(e.getUser(), e.getFunction(), e.getResource());
		}
		catch(Exception e)
		{
			log.error(String.format("Unexpected exception joining user %s to site %s: ", user, id), e);
			return;
	}
		
		try
		{
			// add joining user to the site's join group; the delegate method checks if there are any groups to join
            joinSiteDelegate.addJoinerToGroup(site);
		}
		catch(Exception e)
		{
			log.error(String.format("Unexpected exception joining user %s to group in site %s: ", user, id), e);
		}
	}
        
    /**
	 * @inheritDoc
	 */
	public boolean isAllowedToJoin(String siteID)
	{
        Site site = null;
        try 
        {
            // get the site
            site = getDefinedSite(siteID);
        }
        catch (IdUnusedException iue)
        {
            log.error("Site could not be determined for allowed to join method: " + iue.getMessage(), iue);
            return false;
        }
        
        // pass to the JoinDelegate method to handle the logic
        return joinSiteDelegate.isAllowedToJoin(site);
	}

	/**
	 * @inheritDoc
	 */
    public String getJoinGroupId(String siteID)
    {
        Site site = null;
                
        try 
        {
            // get the site
            site = getDefinedSite(siteID);
        }
        catch (IdUnusedException iue)
        {
            log.error("Site could not be determined for getting the join group: " + iue.getMessage(), iue);
        }
        
        // pass to the JoinDelegate method to handle the logic
        return joinSiteDelegate.getJoinGroupId(site);
    }

    /**
     * @inheritDoc
     */
	public boolean isCurrentUserMemberOfSite( String siteID )
	{
		if( siteID == null || "".equals( siteID ) )
        {
			return false;
        }
		
		try
		{
            // get the current user
            User user = userDirectoryService().getCurrentUser();
            if( user == null )
            {
            	return false;
            }
            
			// If current user is a member of the site, return true; otherwise return false
            return getSite( siteID ).getMember( user.getId() ) != null;
		}
		catch( IdUnusedException ex ) 
        { 
            log.debug( "isAlreadyMember()", ex ); 
        }
		
		// Otherwise they're not already a member, return false
		return false;
	}
    
    /** 
     * @inheritDoc
     */
    public boolean isLimitByAccountTypeEnabled(String siteID)
    {
        return joinSiteDelegate.isLimitByAccountTypeEnabled(siteID);
    }
    
    /** 
     * @inheritDoc
     */
    public LinkedHashSet<String> getAllowedJoinableAccountTypeCategories()
    {
    	return joinSiteDelegate.getAllowedJoinableAccountTypeCategories();
    }
    
    /** 
     * @inheritDoc
     */
    public List<String> getAllowedJoinableAccountTypes()
    {
    	return joinSiteDelegate.getAllowedJoinableAccountTypes();
    }
    
    /** 
     * @inheritDoc
     */
    public List<AllowedJoinableAccount> getAllowedJoinableAccounts()
	{
    	return joinSiteDelegate.getAllowedJoinableAccounts();
	}
    
    /** 
     * @inheritDoc
     */
    public boolean isGlobalJoinGroupEnabled()
    {
    	return joinSiteDelegate.getGlobalJoinGroupEnabled();
    }
    
    /** 
     * @inheritDoc
     */
    public boolean isGlobalJoinExcludedFromPublicListEnabled()
    {
    	return joinSiteDelegate.getGlobalJoinExcludeFromPublicListEnabled();
    }

    /** 
     * @inheritDoc
     */
    public boolean isGlobalJoinLimitByAccountTypeEnabled()
    {
    	return joinSiteDelegate.getGlobalJoinLimitByAccountTypeEnabled();
    }

    /** 
     * @inheritDoc
     */
    public boolean isGlobalJoinFromSiteBrowserEnabled()
    {
    	return joinSiteDelegate.getGlobalSiteBrowserJoinEnabled();
    }

	/**
	 * @inheritDoc
	 */
	public void unjoin(String id) throws IdUnusedException, PermissionException
	{
		try
		{
			authzGroupService().unjoinGroup(siteReference(id));
		}
		catch (GroupNotDefinedException e)
		{
			throw new IdUnusedException(e.getId());
		}
		catch (AuthzPermissionException e)
		{
			throw new PermissionException(e.getUser(), e.getFunction(), e.getResource());
		}
	}

	/**
	 * @inheritDoc
	 */
	public boolean allowUnjoinSite(String id)
	{
		// basic unjoin AuthzGroup test
		if (!authzGroupService().allowUnjoinGroup(siteReference(id))) return false;

		// one more check - don't let a maintain role user unjoin a non-joinable site, or
		// a joinable site that does not have the maintain role as the joiner role.
		try
		{
			// get the site
			Site site = getDefinedSite(id);

			// get the AuthGroup
			AuthzGroup azg = authzGroupService().getAuthzGroup(siteReference(id));

			String user = sessionManager().getCurrentSessionUserId();
			if (user == null) return false;

			if ((StringUtil.different(site.getJoinerRole(), azg.getMaintainRole())) || (!site.isJoinable()))
			{
				Role role = azg.getUserRole(user);
				if (role == null)
				{
					return false;
				}
				if (role.getId().equals(azg.getMaintainRole()))
				{
					return false;
				}
			}
		}
		catch (IdUnusedException e)
		{
			return false;
		}
		catch (GroupNotDefinedException e)
		{
			return false;
		}

		return true;
	}

	/**
	 * @inheritDoc
	 */
	public String getSiteSkin(String id)
	{
		// check the site cache
		if (m_siteCache != null)
		{
			try
			{
				// this gets the site from the cache, or reads the site / pages / tools and caches it
                Site s = getDefinedSite(id);
                return adjustSkin(s.getSkin(), s.isPublished());
			}
			catch (IdUnusedException e)
			{
                return adjustSkin(null,true);
			}
        }

        //No site cache. Check the db.
        return adjustSkin(storage().getSiteSkin(id),true);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getSiteTypes()
	{
		return storage().getSiteTypes();
	}

	/**
	 * Convenience method to run the getSites() call common to all getUserSites* methods.
	 * @param requireDescription when true, full descriptions will be included; when false, full descriptions may be omitted.
	 * @param userID the returned sites will be those which can be accessed by the user with this internal ID. Uses the current user if null.
	 * @param includeUnpublishedSites when true, unpublished sites will be included; when false, unpublished sites will be omitted.
	 * @param excludedSites list with siteIDs to be excluded from being loaded. If no exclusions are required, set to NULL or empty list.
	 * @return 
	 */
	private List<Site> getUserSitesByPublishedStatus( boolean requireDescription, String userID, boolean includeUnpublishedSites, List excludedSites)
	{
		SortType sortType = SortType.TITLE_ASC;
		SelectionType selectionType = includeUnpublishedSites ? SelectionType.MEMBER : SelectionType.ACCESS;

		if( StringUtils.isBlank( userID ) )
		{
			return (List<Site>) getSites( selectionType, null, null, null, excludedSites, sortType, null, requireDescription );
		}
		else
		{
			return (List<Site>) getSites( selectionType, null, null, null, excludedSites, sortType, null, requireDescription, userID );
		}
	}

	/**
	 * @inheritDoc
	 */
	public List<Site> getUserSites() {
		return getUserSites(true);
	}

	/**
	 * @inheritDoc
	 */
	public List<Site> getUserSites(boolean requireDescription) {
		return getUserSites( requireDescription, false );
	}
	
	/**
	 * @inheritDoc
	 */
	public List<Site> getUserSites( boolean requireDescription, boolean includeUnpublishedSites ) {
		return getUserSites( requireDescription, false, null );
	}

	/**
	 * @inheritDoc
	 */
	public List<Site> getUserSites( boolean requireDescription, boolean includeUnpublishedSites, List excludedSites )
	{
		String userID = sessionManager().getCurrentSessionUserId();
		List<Site> userSites = getCachedUserSites( userID );
		

		// Retrieve sites on cache miss or anonymous user
		if( userSites == null )
		{
			userSites = getUserSitesByPublishedStatus( requireDescription, null, includeUnpublishedSites, excludedSites );

			// Cache the results
			setCachedUserSites( userID, userSites );
		}

		return userSites;
	}

	/**
	 * @inheritDoc
	 */
	public List<Site> getUserSites(boolean requireDescription, String userId)
	{
		return getUserSites( requireDescription, userId, false );
	}
	
	/**
	 * @inheritDoc
	 */
	public List<Site> getUserSites( boolean requireDescription, String userId, boolean includeUnpublishedSites )
	{
		return getUserSites( requireDescription, userId, includeUnpublishedSites, null );
	}

	/**
	 * @inheritDoc
	 */
	public List<Site> getUserSites( boolean requireDescription, String userID, boolean includeUnpublishedSites, List excludedSites )
	{
		List<Site> userSites = getCachedUserSites( userID );

		if( userSites == null )
		{
			userSites = getUserSitesByPublishedStatus( requireDescription, userID, includeUnpublishedSites, excludedSites );

			// Cache the results
			setCachedUserSites( userID, userSites );
		}

		return userSites;
	}

	/**
	 * Cache the list of accessible Sites for a user.
	 *
	 * @param userId the (internal) user ID for whom to cache sites; null will result in a no-op
	 * @param sites the list of sites that are accessible for the user; may be null to remove the user from the cache
	 */
	protected void setCachedUserSites(String userId, List<Site> sites)
	{
		if (m_userSiteCache != null && userId != null)
		{
			if (sites == null)
			{
				clearUserCacheForUser(userId);
			}
			else
			{
				m_userSiteCache.put(userId, sites);
			}
		}
	}

	/**
	 * Remove the list of cached sites for a specified user.
	 *
	 * @param userId the (internal) user ID for whom to purge sites; null will result in a no-op
	 */
	protected void clearUserCacheForUser(String userId)
	{
		if (m_userSiteCache != null && userId != null)
		{
			m_userSiteCache.remove(userId);
		}
	}

	/**
	 * Clear the user-site cache for all the members of this site.
	 *
	 * This is provided to force retrieval of the user-site list for all members of an updated site.
	 *
	 * If the site and user-site cache were more tightly integrated, we could update, but membership
	 * updates are relatively rare and the retrieval is relatively cheap when done occasionally.
	 *
	 * @param site The site for which all members' site cache should be cleared.
	 *
	 */
	protected void clearUserCacheForSite(Site site)
	{
		if (m_userSiteCache != null && site != null)
		{
			for (Member member : site.getMembers())
			{
				clearUserCacheForUser(member.getUserId());
			}
		}
	}

	/**
	 * Get the list of sites that are accessible to a given user from the cache.
	 *
	 * @param the internal user ID to check in the cache; null results in a null return
	 * @return a List of Sites that are accessible to the user, null on cache miss
	 */
	@SuppressWarnings("unchecked")
	protected List<Site> getCachedUserSites(String userId)
	{
		List<Site> userSites = null;
		if (m_userSiteCache != null && userId != null)
		{
			userSites = (List<Site>) m_userSiteCache.get(userId);
		}
		return userSites;
	}

	/**
	 * @inheritDoc
	 */
	public List<Site> getSites(SelectionType type, Object ofType, String criteria, Map propertyCriteria, SortType sort,
			PagingPosition page)
	{
		return getSites(type, ofType, criteria, propertyCriteria, sort, page, true);
	}
	
	/**
	 * @inheritDoc
	 */
	public List<Site> getSites(SelectionType type, Object ofType, String criteria, Map propertyCriteria, SortType sort,
			PagingPosition page, boolean requireDescription)
	{
		return getSites(type, ofType, criteria, propertyCriteria, null, sort, page, requireDescription);
	}
	
	/**
	 * @inheritDoc
	 */
	public List<Site> getSites(SelectionType type, Object ofType, String criteria, Map propertyCriteria, List excludedSites, SortType sort,
			PagingPosition page, boolean requireDescription)
	{
		return storage().getSites(type, ofType, criteria, propertyCriteria, excludedSites, sort, page, requireDescription);
	}

	/**
	 * @inheritDoc
	 */
	public List<Site> getSites(SelectionType type, Object ofType, String criteria, Map propertyCriteria, SortType sort, PagingPosition page, boolean requireDescription, String userId)
	{
		return getSites(type, ofType, criteria, propertyCriteria, null, sort, page, requireDescription, userId);
	}
	
	/**
	 * @inheritDoc
	 */
	public List<Site> getSites(SelectionType type, Object ofType, String criteria, Map propertyCriteria, List excludedSites, SortType sort, PagingPosition page, boolean requireDescription, String userId)
	{
		return storage().getSites(type, ofType, criteria, propertyCriteria, excludedSites, sort, page, requireDescription, userId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.site.api.SiteService#getSiteIds(org.sakaiproject.site.api.SiteService.SelectionType, java.lang.Object, java.lang.String, java.util.Map, org.sakaiproject.site.api.SiteService.SortType, org.sakaiproject.javax.PagingPosition)
	 */
	public List<String> getSiteIds(SelectionType type, Object ofType, String criteria, Map<String, String> propertyCriteria, SortType sort, PagingPosition page) {
	    return storage().getSiteIds(type, ofType, criteria, propertyCriteria, sort, page);
	}

	/**
	 * @inheritDoc
	 */
	public List<Site> getSoftlyDeletedSites() {
		return storage().getSoftlyDeletedSites();
	}

	/**
	 * @inheritDoc
	 */
	public int countSites(SelectionType type, Object ofType, String criteria, Map propertyCriteria)
	{
		return storage().countSites(type, ofType, criteria, propertyCriteria);
	}

	/**
	 * @inheritDoc
	 */
	public void setSiteSecurity(String siteId, Set updateUsers, Set visitUnpUsers, Set visitUsers)
	{
		storage().setSiteSecurity(siteId, updateUsers, visitUnpUsers, visitUsers);

		// the site's azg may have just been updated, so enforce site group subset membership
		enforceGroupSubMembership(siteId);

		Event invalidate = eventTrackingService().newEvent(EVENT_SITE_USER_INVALIDATE, siteId, true);
		eventTrackingService().post(invalidate);
	}

	/**
	 * @inheritDoc
	 */
	public void setUserSecurity(String userId, Set<String> updateSites, Set<String> visitUnpSites, Set<String> visitSites)
	{
		//KNL-512 we need to filter out any non-existent sites from the list
		List<String> nonExistentIds = new ArrayList<String>();
		
		Iterator<String> updateIt = updateSites.iterator();
		while (updateIt.hasNext())
		{
			String id = updateIt.next();
			if (!nonExistentIds.contains(id)) 
			{
				if (!this.siteExists(id)) 
				{
					log.warn("setUserSecurity passed a non existent site Id it will be discarded: " + id);
					nonExistentIds.add(id);
				}
			}
		}
		
		
		Iterator<String> visitUnpIt = visitUnpSites.iterator();
		while (visitUnpIt.hasNext())
		{
			String id = visitUnpIt.next();
			if (!nonExistentIds.contains(id)) 
			{
				if (!this.siteExists(id)) 
				{
					log.warn("setUserSecurity passed a non existent site Id it will be discarded: " + id);
					nonExistentIds.add(id);
				}
			}
		}
		
		
		
		Iterator<String> visitIt = visitSites.iterator();
		while (visitIt.hasNext())
		{
			String id = visitIt.next();
			if (!nonExistentIds.contains(id)) 
			{
				if (!this.siteExists(id)) 
				{
					log.warn("setUserSecurity passed a non existent site Id it will be discarded: " + id);
					nonExistentIds.add(id);
				}
			}
		}
		
		for (int i = 0; i < nonExistentIds.size(); i++)
		{
			String id = nonExistentIds.get(i);
			updateSites.remove(id);
			visitUnpSites.remove(id);
			visitSites.remove(id);		}
		
		storage().setUserSecurity(userId, updateSites, visitUnpSites, visitSites);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * EntityProducer implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		return "site";
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean willArchiveMerge()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public HttpAccess getHttpAccess()
	{
		return new HttpAccess()
		{
			public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref,
					Collection copyrightAcceptedRefs) throws EntityPermissionException, EntityNotDefinedException,
					EntityAccessOverloadException, EntityCopyrightException
			{
				try
				{
					Site site = (Site) ref.getEntity();
					String skin = getSiteSkin(site.getId());
					String skinRepo = serverConfigurationService().getString("skin.repo");
					String skinDefault = serverConfigurationService().getString("skin.default");

					// make sure that it points to the default if there is no skin
					if (skin == null)
					{
						skin = skinDefault;
					}

					res.setContentType("text/html; charset=UTF-8");
					PrintWriter out = res.getWriter();
					out
							.println("<!DOCTYPE html>");
					out.println("<html>");
					out.println("<head>");
					out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
					out.println("<meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />");
					out.println("<link href=\"" + skinRepo
							+ "/tool_base.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />");
					out.println("<link href=\"" + skinRepo + "/" + skin
							+ "/tool.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />");
					out.println(serverConfigurationService().getString("portal.include.extrahead", ""));

					out.println("<title>");
					out.println(site.getTitle());
					out.println("</title>");
					out.println("</head><body class=\"siteDescriptionFrame\"><div class=\"portletBody siteDescription\">");					

					// get the description - if missing, use the site title
					String description = site.getDescription();

					if (description == null)
					{
						description = site.getTitle();
					}

					out.println(description);
					out.println("</div></body></html>");
				}
				catch (Exception t)
				{
					throw new EntityNotDefinedException(ref.getReference());
				}
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		// for site access
		if (reference.startsWith(REFERENCE_ROOT))
		{
			String id = null;
			String container = null;
			String subType = SITE_SUBTYPE;

			// we will get null, service, siteId, page | group | tool, page/group/tool id
			String[] parts = StringUtil.split(reference, Entity.SEPARATOR);

			if (parts.length > 2)
			{
				id = parts[2];
				container = id;

				if (parts.length > 4)
				{
					subType = parts[3];
					id = parts[4];
				}
			}

			ref.set(APPLICATION_ID, subType, id, container, null);

			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityDescription(Reference ref)
	{
		// double check that it's mine
		if (!APPLICATION_ID.equals(ref.getType())) return null;

		String rv = rb.getFormattedMessage("entdsc.sit", new Object[]{ref.getReference()});

		try
		{
			Site site = getSite(ref.getId());
			rv = rb.getFormattedMessage("entdsc.sit_usr", new Object[]{
					site.getTitle() + " (" + site.getId() + ")",
					site.getCreatedTime().toStringLocalFull(),
					site.getCreatedBy().getDisplayName() + " (" + site.getCreatedBy().getDisplayId() + ")",
					StringUtil.limit((site.getDescription() == null ? "" : site.getDescription()), 30)});
		}
		catch (IdUnusedException e)
		{
		}
		catch (NullPointerException e)
		{
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Entity getEntity(Reference ref)
	{
		// double check that it's mine
		if (!APPLICATION_ID.equals(ref.getType())) return null;
		
		Entity rv = null;

		try
		{
			rv = getSite(ref.getId());
		}
		catch (IdUnusedException e)
		{
			log.warn("getEntity(): " + e);
		}
		catch (NullPointerException e)
		{
			log.warn("getEntity(): " + e);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		// double check that it's mine
		if (!APPLICATION_ID.equals(ref.getType())) return null;

		Collection rv = new Vector();

		try
		{
			// first, use the reference as an authzGroup (site, group, page or tool)
			rv.add(ref.getReference());

			// do NOT use the site if the reference is a group or other part
			// // if this is a sub-type, add the site's reference - container is site id
			// if (!SITE_SUBTYPE.equals(ref.getSubType()))
			// {
			// rv.add(siteReference(ref.getContainer()));
			// }

			// add the current user's realm
			ref.addUserAuthzGroup(rv, userId);

			// site helper
			rv.add("!site.helper");
		}
		catch (Exception e)
		{
			log.warn("getEntityRealms(): " + e);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityUrl(Reference ref) {
		String url = null;
		if (ref != null) {
		    try {
		        Site site = getSite(ref.getId());
		        url = site.getUrl();
		    } catch (IdUnusedException e) {
		        // this could happen if the site reference is invalid
		        if (log.isDebugEnabled()) log.debug("getEntityUrl(): " + e);
		    } catch (Exception e) {
		        // this is a real failure
		        log.error("getEntityUrl(): "+e.getClass().getName()+": " + e, e);
		    }
		}
		return url;
	}

	/**
	 * {@inheritDoc}
	 */
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport)
	{
		return "";
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Sync up with all other services for a site that exists.
	 * 
	 * @param site
	 *        The site.
	 */
	protected void enableRelated(BaseSite site, boolean isNew)
	{
		// skip if special
		if (isSpecialSite(site.getId()))
		{
			return;
		}

		try
		{
			// take care of our AuthzGroups
			enableAzgSecurityAdvisor();
			enableAzg(site);
		}
		finally
		{
			disableAzgSecurityAdvisor();
		}

		// offer to all EntityProducers that are ContexObservers
		for (Iterator i = entityManager().getEntityProducers().iterator(); i.hasNext();)
		{
			EntityProducer ep = (EntityProducer) i.next();
			if (ep instanceof ContextObserver)
			{
				try
				{
					ContextObserver co = (ContextObserver) ep;

					// is this CO's tools in the site?
					boolean toolPlacement = !site.getTools(co.myToolIds()).isEmpty();

					if (isNew)
					{
						co.contextCreated(site.getId(), toolPlacement);
					}

					else
					{
						co.contextUpdated(site.getId(), toolPlacement);
					}
				}
				catch (Exception t)
				{
					log.error("Error encountered while notifying ContextObserver of Site Change", t);
				}
			}
		}
	}

	/**
	 * Sync up with all other services for a site that is going away.
	 * 
	 * @param site
	 *        The site.
	 */
	protected void disableRelated(Site site)
	{
		// skip if special
		if (isSpecialSite(site.getId()))
		{
			return;
		}

		// send to all EntityProducers that are ContextObservers
		for (Iterator i = entityManager().getEntityProducers().iterator(); i.hasNext();)
		{
			EntityProducer ep = (EntityProducer) i.next();
			if (ep instanceof ContextObserver)
			{
				try
				{
					ContextObserver co = (ContextObserver) ep;

					// is this CO's tools in the site?
					boolean toolPlacement = !site.getTools(co.myToolIds()).isEmpty();
					co.contextDeleted(site.getId(), toolPlacement);
				}
				catch (Exception t)
				{
					log.error("Error encountered while notifying ContextObserver of Site Change", t);
				}
			}
		}

		// disable the azgs last, so permissions were in place for the above
		try
		{
			enableAzgSecurityAdvisor();
			disableAzg(site);
		}
		finally
		{
			disableAzgSecurityAdvisor();
		}
	}

	/**
	 * Enable the site and site group AuthzGroups.
	 * 
	 * @param site
	 *        The site.
	 */
	protected void enableAzg(BaseSite site)
	{
		// figure the site authorization group template
		String siteAzgTemplate = siteAzgTemplate(site);

		// try the site created-by user for the maintain role in the site
		String userId = site.getCreatedBy().getId();
		if (userId != null)
		{
			// make sure it's valid
			try
			{
				userDirectoryService().getUser(userId);
			}
			catch (UserNotDefinedException e1)
			{
				userId = null;
			}
		}

		// use the current user if needed
		if (userId == null)
		{
			User user = userDirectoryService().getCurrentUser();
			userId = user.getId();
		}

		enableAuthorizationGroup(site.getReference(), siteAzgTemplate, userId, "!site.template");

		// figure the group authorization group template
		String groupAzgTemplate = groupAzgTemplate(site);

		// enable a realm for each group: use the same template as for the site, but don't assign a user maintain in the group's azg
		for (Iterator iGroups = site.getGroups().iterator(); iGroups.hasNext();)
		{
			Group group = (Group) iGroups.next();
			enableAuthorizationGroup(group.getReference(), groupAzgTemplate, null, "!group.template");
		}

		// disable the authorization groups for any groups deleted in this edit
		for (Iterator iGroups = site.m_deletedGroups.iterator(); iGroups.hasNext();)
		{
			Group group = (Group) iGroups.next();
			disableAuthorizationGroup(group.getReference());
		}
	}

	/**
	 * Disable the site and site group azgs for a site that's being deleted.
	 * 
	 * @param site
	 *        The site.
	 */
	protected void disableAzg(Site site)
	{
		// disable a realm for each group
		for (Iterator iGroups = site.getGroups().iterator(); iGroups.hasNext();)
		{
			Group group = (Group) iGroups.next();
			disableAuthorizationGroup(group.getReference());
		}

		// disable realm last, to keep those permissions around
		disableAuthorizationGroup(site.getReference());
	}

	/**
	 * Figure the site's authorization group template, based on type and if it's a user site.
	 * 
	 * @param site
	 *        The site to figure the realm for.
	 * @return the site's authorization group template, based on type and if it's a user site.
	 */
	protected String siteAzgTemplate(Site site)
	{
		String azgTemplate = null;
		if (isUserSite(site.getId()))
		{
			azgTemplate = "!site.user";
		}
		else
		{
			// use the type's template, if defined
			azgTemplate = "!site.template";
			String type = site.getType();
			if (type != null)
			{
				azgTemplate = azgTemplate + "." + type;
			}
		}

		return azgTemplate;
	}

	/**
	 * Figure the authorization group template for a group of this site, based on type and if it's a user site.
	 * 
	 * @param site
	 *        The site to figure the authorization group templates for.
	 * @return the authorization group template for a group of this site, based on type and if it's a user site.
	 */
	protected String groupAzgTemplate(Site site)
	{
		String azgTemplate = null;
		if (isUserSite(site.getId()))
		{
			azgTemplate = "!group.user";
		}
		else
		{
			// use the type's template, if defined
			azgTemplate = "!group.template";
			String type = site.getType();
			if (type != null)
			{
				azgTemplate = azgTemplate + "." + type;
			}
		}

		return azgTemplate;
	}

	/**
	 * Setup the realm for an active site.
	 * 
	 * @param ref
	 *        The reference for which the realm will be created (site, user).
	 * @param templateId
	 *        The realm id of a template to use for the new realm.
	 * @param userId
	 *        The user to get maintain in this realm.
	 */
	protected void enableAuthorizationGroup(String ref, String templateId, String userId, String fallbackTemplate)
	{
		// see if it exists already
		try
		{
			AuthzGroup realm = authzGroupService().getAuthzGroup(ref);
		}
		catch (GroupNotDefinedException un)
		{
			// see if there's a new site AuthzGroup template
			AuthzGroup template = null;
			try
			{
				template = authzGroupService().getAuthzGroup(templateId);
			}
			catch (Exception e)
			{
				try
				{
					// if the template is not defined, try the fall back template
					template = authzGroupService().getAuthzGroup(fallbackTemplate);
				}
				catch (Exception ee)
				{
				}
			}

			// add the realm
			try
			{
				AuthzGroup realm = null;

				if (template == null)
				{
					realm = authzGroupService().addAuthzGroup(ref);
				}
				else
				{
					realm = authzGroupService().addAuthzGroup(ref, template, userId);
				}
			}
			catch (Exception e)
			{
				log.warn(".enableRealm: AuthzGroup exception: " + e);
			}
		}
	}

	/**
	 * Remove a site's realm.
	 * 
	 * @param site
	 *        The site.
	 */
	protected void disableAuthorizationGroup(String ref)
	{
		try
		{
			authzGroupService().removeAuthzGroup(ref);
		}
		catch (Exception e)
		{
			log.warn(".removeSite: AuthzGroup exception: " + e);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected interface Storage
	{
		/**
		 * Open and be ready to read / write.
		 */
		public void open();

		/**
		 * Close.
		 */
		public void close();

		/**
		 * Does the site with this id exist?
		 * 
		 * @param id
		 *        The site id.
		 * @return true if the site with this id exists, false if not.
		 */
		public boolean check(String id);

		/**
		 * Get the site with this id, or null if not found.
		 * 
		 * @param id
		 *        The site id.
		 * @return The site with this id, or null if not found.
		 */
		public Site get(String id);

		/**
		 * Get all sites.
		 * 
		 * @return The list of all sites.
		 */
		public List getAll();

		/**
		 * Add a new site with this id.
		 * 
		 * @param id
		 *        The site id.
		 * @return The site with this id, or null if in use.
		 */
		public Site put(String id);

		/**
		 * Save the changes.
		 * 
		 * @param site
		 *        The site to commit.
		 */
		public void save(Site site);

		/**
		 * Save the changes to the two info fields (description and infoUrl) only.
		 * 
		 * @param siteId
		 *        The site to commit.
		 * @param description
		 *        The new site description.
		 * @param infoUrl
		 *        The new site infoUrl.
		 */
		public void saveInfo(String siteId, String description, String infoUrl);

		/**
		 * Remove this site.
		 * 
		 * @param user
		 *        The site to remove.
		 */
		public void remove(Site site);

		/**
		 * Count all the sites.
		 * 
		 * @return The count of all sites.
		 */
		public int count();

		/**
		 * Access a unique list of String site types for any site type defined for any site, sorted by type.
		 * 
		 * @return A list (String) of all used site types.
		 */
		public List getSiteTypes();

		/**
		 * Access a list of Site objects that meet specified criteria.
		 * 
		 * @param type
		 *        The SelectionType specifying what sort of selection is intended.
		 * @param ofType
		 *        Site type criteria: null for any type; a String to match a single type; A String[], List or Set to match any type in the collection.
		 * @param criteria
		 *        Additional selection criteria: sits returned will match this string somewhere in their id, title, description, or skin.
		 * @param propertyCriteria
		 *        Additional selection criteria: sites returned will have a property named to match each key in the map, whose values match (somewhere in their value) the value in the map (may be null or empty).
		 * @param sort
		 *        A SortType indicating the desired sort. For no sort, set to SortType.NONE.
		 * @param page
		 *        The PagePosition subset of items to return.
		 * @return The List (Site) of Site objects that meet specified criteria.
		 */
		public List getSites(SelectionType type, Object ofType, String criteria, Map propertyCriteria, SortType sort,
				PagingPosition page);

		/**
		 * Access a list of Site objects that meet specified criteria, with control over description retrieval.
		 * Note that this signature is primarily provided to help with performance when retrieving lists of
		 * sites not for full display, specifically for the list of a user's sites for navigation. Note that
		 * any sites that have their descriptions, pages, or tools cached will be returned completely, so some
		 * or all full descriptions may be present even when requireDescription is passed as false.
		 *
		 * If a fully populated Site is desired from a potentially partially populated Site, call
		 * {@link #getSite(String id) getSite} or {@link Site#loadAll()}. Either method will load and cache
		 * whatever additional data is not yet cached.
		 *
		 * @param type
		 *        The SelectionType specifying what sort of selection is intended.
		 * @param ofType
		 *        Site type criteria: null for any type; a String to match a single type; A String[], List or Set to match any type in the collection.
		 * @param criteria
		 *        Additional selection criteria: sits returned will match this string somewhere in their id, title, description, or skin.
		 * @param propertyCriteria
		 *        Additional selection criteria: sites returned will have a property named to match each key in the map, whose values match (somewhere in their value) the value in the map (may be null or empty).
		 * @param sort
		 *        A SortType indicating the desired sort. For no sort, set to SortType.NONE.
		 * @param page
		 *        The PagePosition subset of items to return.
		 * @param requireDescription
		 *        When true, force a full retrieval of each description; when false, return any uncached descriptions as the empty string
		 * @return The List of Site objects that meet specified criteria.
		 */
		public List<Site> getSites(SelectionType type, Object ofType, String criteria, Map propertyCriteria, SortType sort,
				PagingPosition page, boolean requireDescription);
		
		/**
		 * Access a list of Site objects that meet specified criteria, with control over description retrieval.
		 * Note that this signature is primarily provided to help with performance when retrieving lists of
		 * sites not for full display, specifically for the list of a user's sites for navigation. Note that
		 * any sites that have their descriptions, pages, or tools cached will be returned completely, so some
		 * or all full descriptions may be present even when requireDescription is passed as false.
		 *
		 * If a fully populated Site is desired from a potentially partially populated Site, call
		 * {@link #getSite(String id) getSite} or {@link Site#loadAll()}. Either method will load and cache
		 * whatever additional data is not yet cached.
		 *
		 * @param type
		 *        The SelectionType specifying what sort of selection is intended.
		 * @param ofType
		 *        Site type criteria: null for any type; a String to match a single type; A String[], List or Set to match any type in the collection.
		 * @param criteria
		 *        Additional selection criteria: sits returned will match this string somewhere in their id, title, description, or skin.
		 * @param propertyCriteria
		 *        Additional selection criteria: sites returned will have a property named to match each key in the map, whose values match (somewhere in their value) the value in the map (may be null or empty).
		 * @param excludedSites 
		 *        List with siteIDs to be excluded from being loaded. If no exclusions are required, set to NULL or empty list.
		 * @param sort
		 *        A SortType indicating the desired sort. For no sort, set to SortType.NONE.
		 * @param page
		 *        The PagePosition subset of items to return.
		 * @param requireDescription
		 *        When true, force a full retrieval of each description; when false, return any uncached descriptions as the empty string
		 * @return The List of Site objects that meet specified criteria.
		 */
		public List<Site> getSites(SelectionType type, Object ofType, String criteria, Map propertyCriteria, List excludedSites, SortType sort,
				PagingPosition page, boolean requireDescription);

		/**
		 * Access a list of Site objects that meet specified criteria, with control over description retrieval.
		 * Note that this signature is primarily provided to help with performance when retrieving lists of
		 * sites not for full display, specifically for the list of a user's sites for navigation. Note that
		 * any sites that have their descriptions, pages, or tools cached will be returned completely, so some
		 * or all full descriptions may be present even when requireDescription is passed as false.
		 *
		 * If a fully populated Site is desired from a potentially partially populated Site, call
		 * {@link #getSite(String id) getSite} or {@link Site#loadAll()}. Either method will load and cache
		 * whatever additional data is not yet cached.
		 *
		 * @param type
		 *        The SelectionType specifying what sort of selection is intended.
		 * @param ofType
		 *        Site type criteria: null for any type; a String to match a single type; A String[], List or Set to match any type in the collection.
		 * @param criteria
		 *        Additional selection criteria: sits returned will match this string somewhere in their id, title, description, or skin.
		 * @param propertyCriteria
		 *        Additional selection criteria: sites returned will have a property named to match each key in the map, whose values match (somewhere in their value) the value in the map (may be null or empty).
		 * @param sort
		 *        A SortType indicating the desired sort. For no sort, set to SortType.NONE.
		 * @param page
		 *        The PagePosition subset of items to return.
		 * @param requireDescription
		 *        When true, force a full retrieval of each description; when false, return any uncached descriptions as the empty string
		 * @param userId
		 *        The returned sites will be those which can be accessed by the user with this internal ID
		 * @return The List of Site objects that meet specified criteria.
		 */
		public List<Site> getSites(SelectionType type, Object ofType, String criteria, Map propertyCriteria, SortType sort, PagingPosition page, boolean requireDescription, String userId);
		
		/**
		 * Access a list of Site objects that meet specified criteria, with control over description retrieval.
		 * Note that this signature is primarily provided to help with performance when retrieving lists of
		 * sites not for full display, specifically for the list of a user's sites for navigation. Note that
		 * any sites that have their descriptions, pages, or tools cached will be returned completely, so some
		 * or all full descriptions may be present even when requireDescription is passed as false.
		 *
		 * If a fully populated Site is desired from a potentially partially populated Site, call
		 * {@link #getSite(String id) getSite} or {@link Site#loadAll()}. Either method will load and cache
		 * whatever additional data is not yet cached.
		 *
		 * @param type
		 *        The SelectionType specifying what sort of selection is intended.
		 * @param ofType
		 *        Site type criteria: null for any type; a String to match a single type; A String[], List or Set to match any type in the collection.
		 * @param criteria
		 *        Additional selection criteria: sits returned will match this string somewhere in their id, title, description, or skin.
		 * @param propertyCriteria
		 *        Additional selection criteria: sites returned will have a property named to match each key in the map, whose values match (somewhere in their value) the value in the map (may be null or empty).
		 * @param excludedSites 
		 *        List with siteIDs to be excluded from being loaded. If no exclusions are required, set to NULL or empty list.
		 * @param sort
		 *        A SortType indicating the desired sort. For no sort, set to SortType.NONE.
		 * @param page
		 *        The PagePosition subset of items to return.
		 * @param requireDescription
		 *        When true, force a full retrieval of each description; when false, return any uncached descriptions as the empty string
		 * @param userId
		 *        The returned sites will be those which can be accessed by the user with this internal ID
		 * @return The List of Site objects that meet specified criteria.
		 */
		public List<Site> getSites(SelectionType type, Object ofType, String criteria, Map propertyCriteria, List excludedSites, SortType sort, PagingPosition page, boolean requireDescription, String userId);

		/**
		 * Get the Site IDs for all sites matching criteria.
		 * This is useful when you only need the listing of site ids (for other operations) and do not need the actual Site objects.
		 *
		 * All parameters are the same as {@link #getSites(org.sakaiproject.site.api.SiteService.SelectionType, Object, String, Map, org.sakaiproject.site.api.SiteService.SortType, PagingPosition)}
		 * 
		 * @param type
		 *        The SelectionType specifying what sort of selection is intended.
		 * @param ofType
		 *        Site type criteria: null for any type; a String to match a single type; A String[], List or Set to match any type in the collection.
		 * @param criteria
		 *        Additional selection criteria: sites returned will match this string somewhere in their id, title, description, or skin.
		 * @param propertyCriteria
		 *        Additional selection criteria: sites returned will have a property named to match each key in the map, whose values match (somewhere in their value) the value in the map (may be null or empty).
		 * @param sort
		 *        A SortType indicating the desired sort. For no sort, set to SortType.NONE.
		 * @param page
		 *        The PagePosition subset of items to return.
		 * @return a List of the Site IDs for the sites matching the criteria.
		 */
		List<String> getSiteIds(SelectionType type, Object ofType, String criteria, Map<String, String> propertyCriteria, SortType sort, PagingPosition page);

		/**
		 * Count the Site objets that meet specified criteria.
		 * 
		 * @param type
		 *        The SelectionType specifying what sort of selection is intended.
		 * @param ofType
		 *        Site type criteria: null for any type; a String to match a single type; A String[], List or Set to match any type in the collection.
		 * @param criteria
		 *        Additional selection criteria: sits returned will match this string somewhere in their id, title, description, or skin.
		 * @param propertyCriteria
		 *        Additional selection criteria: sites returned will have a property named to match each key in the map, whose values match (somewhere in their value) the value in the map (may be null or empty).
		 * @return The count of Site objets that meet specified criteria.
		 */
		public int countSites(SelectionType type, Object ofType, String criteria, Map propertyCriteria);

		/**
		 * Access the ToolConfiguration that has this id, if one is defined, else return null. The tool may be on any SitePage in any site.
		 * 
		 * @param id
		 *        The id of the tool.
		 * @return The ToolConfiguration that has this id, if one is defined, else return null.
		 */
		public ToolConfiguration findTool(String id);

		/**
		 * Access the Site id for the tool with this id.
		 * 
		 * @param id
		 *        The id of the tool.
		 * @return The Site id for the tool with this id, if the tool is found, else null.
		 */
		public String findToolSiteId(String id);

		/**
		 * Access the Page that has this id, if one is defined, else return null. The page may be on any Site.
		 * 
		 * @param id
		 *        The id of the page.
		 * @return The SitePage that has this id, if one is defined, else return null.
		 */
		public SitePage findPage(String id);

		/**
		 * Access the Site id for the page with this id.
		 * 
		 * @param id
		 *        The id of the page.
		 * @return The Site id for the page with this id, if the page is found, else null.
		 */
		public String findPageSiteId(String id);

		/**
		 * Read site properties from storage into the site's properties.
		 * 
		 * @param site
		 *        The site for which properties are desired.
		 */
		public void readSiteProperties(Site site, ResourcePropertiesEdit props);

		/**
		 * Read properties for all pages in the site
		 * 
		 * @param site
		 *        The site to read properties for.
		 */
		public void readSitePageProperties(Site site);

		/**
		 * Read site properties and all page and tool properties for the site from storage.
		 * 
		 * @param site
		 *        The site for which properties are desired.
		 */
		public void readAllSiteProperties(Site site);

		/**
		 * Read page properties from storage into the page's properties.
		 * 
		 * @param page
		 *        The page for which properties are desired.
		 */
		public void readPageProperties(SitePage page, ResourcePropertiesEdit props);

		/**
		 * Read tool configuration from storage into the tool's configuration properties.
		 * 
		 * @param tool
		 *        The tool for which properties are desired.
		 */
		public void readToolProperties(ToolConfiguration tool, Properties props);

		/**
		 * Read group properties from storage into the group's properties.
		 * 
		 * @param groupId
		 *        The groupId for which properties are desired.
		 */
		public void readGroupProperties(Group groupId, Properties props);

		/**
		 * Read site pages from storage into the site's pages.
		 * 
		 * @param site
		 *        The site for which pages are desired.
		 */
		public void readSitePages(Site site, ResourceVector pages);

		/**
		 * Read site page tools from storage into the page's tools.
		 * 
		 * @param page
		 *        The page for which tools are desired.
		 */
		public void readPageTools(SitePage page, ResourceVector tools);

		/**
		 * Read tools for all pages from storage into the site's page's tools.
		 * 
		 * @param site
		 *        The site for which tools are desired.
		 */
		public void readSiteTools(Site site);

		/**
		 * Return the skin for this site
		 * 
		 * @param siteId
		 *        The site id.
		 * @return the skin for this site.
		 */
		public String getSiteSkin(String siteId);

		/**
		 * Establish the internal security for this site. Previous security settings are replaced for this site. Assigning a user with update implies the two reads; assigning a user with unp read implies the other read.
		 * 
		 * @param siteId
		 *        The id of the site.
		 * @param updateUsers
		 *        The set of String User Ids who have update access.
		 * @param visitUnpUsers
		 *        The set of String User Ids who have visit unpublished access.
		 * @param visitUsers
		 *        The set of String User Ids who have visit access.
		 */
		public void setSiteSecurity(String siteId, Set updateUsers, Set visitUnpUsers, Set visitUsers);

		/**
		 * Establish the internal security for user for all sites. Previous security settings are replaced for this user. Assigning a user with update implies the two reads; assigning a user with unp read implies the other read.
		 * 
		 * @param userId
		 *        The id of the user.
		 * @param updateSites
		 *        The set of String site ids where the user has update access.
		 * @param visitUnpSites
		 *        The set of String site ids where the user has visit unpublished access.
		 * @param visitSites
		 *        The set of String site ids where the user has visit access.
		 */
		public void setUserSecurity(String userId, Set updateSites, Set visitUnpSites, Set visitSites);

		/**
		 * Write an updated tool configuration to the database.
		 * 
		 * @param tool
		 *        TooConfiguration to commit.
		 */
		public void saveToolConfig(ToolConfiguration tool);

		/**
		 * Access the Site id for the group with this id.
		 * 
		 * @param id
		 *        The id of the group.
		 * @return The Site id for the group with this id, if the group is found, else null.
		 */
		public String findGroupSiteId(String id);

		/**
		 * Read site groups from storage into the site's groups.
		 * 
		 * @param site
		 *        The site for which groups are desired.
		 * @param groups
		 *        The Collection to fill in.
		 */
		public void readSiteGroups(Site site, Collection groups);
		
		/**
		 * Get all sites that have been softly deleted
		 * 
		 * @return List of Sites or empty list if none.
		 */
		public List<Site> getSoftlyDeletedSites();
	}

	/**
	 * Adjust a skin value to be just a (folder) name, with no extension, and if missing, be null.
	 * 
	 * @param skin
	 *        The skin value to adjust.
	 * @return A defaulted and adjusted skin value.
	 */
	protected String adjustSkin(String skin, boolean published)
	{
		// return the skin as just a name, no ".css", and not dependent on the published status, or a null if not defined
		if (StringUtils.isEmpty(skin)) {
			skin = serverConfigurationService().getString("skin.default");
		}

		if (!skin.endsWith(".css")) return skin;

		return skin.substring(0, skin.lastIndexOf(".css"));
	}

	/**
	 * @inheritDoc
	 */
	public String merge(String siteId, Element el, String creatorId)
	{
		StringBuilder msg = new StringBuilder();

		try
		{
			// if the target site already exists, don't change the site attributes
			Site s = getSite(siteId);
		}
		catch (IdUnusedException e)
		{
			try
			{
				// reserve a site with this id from the info store - if it's in use, this will return null
				// check security (throws if not permitted)
				// TODO: why security on add_user_site? -ggolden
				unlock(SECURE_ADD_USER_SITE, siteReference(siteId));

				// reserve a site with this id from the info store - if it's in use, this will return null
				BaseSite site = (BaseSite) storage().put(siteId);
				if (site == null)
				{
					msg.append(this + "cannot find site: " + siteId);
				}
				else
				{		
					site.setEvent(SECURE_ADD_SITE);


					if (creatorId != null)
					{
						el.setAttribute("created-id", creatorId);
					}

					// assign source site's attributes to the target site
					((BaseSite) site).set(new BaseSite(this, el, timeService()), false);

					try
					{
						save(site);
					}
					catch (Exception t)
					{
						log.warn(".merge: " + t);
					}
				}
			}
			catch (PermissionException ignore)
			{
			}
		}

		return msg.toString();
	}

	/**
	 * @inheritDoc
	 */
	public Group findGroup(String refOrId)
	{
		if (refOrId == null)
			return null;
		
		Group rv = null;

		// parse the reference or id
		Reference ref = entityManager().newReference(refOrId);

		// for ref, get the site from the cache, or cache it and get the group from the site
		if (APPLICATION_ID.equals(ref.getType()))
		{
			try
			{
				// here we return the group from the site, so the group's containing site is really the site that contains it.
				Site site = getDefinedSite(ref.getContainer());
				rv = site.getGroup(ref.getId());
			}
			// we can ignore a site not found exception, just returning a null Group
			catch (IdUnusedException e)
			{
			}
		}

		// for id, check the cache or get the site from storage, then get the group from the site
		else
		{
			// check the site cache
			if (m_siteCache != null)
			{
				// this lets us find the group from an alredy cached site directly, by group id
				Group group = m_siteCache.getGroup(refOrId);
				if (group != null)
				{
					// Here we need to make a copy of the site, and pull the group from there,
					// so that the group's containing site really contains the group we return.
					// The group we get from the siteCache is a group from the actual cached site, so it's containing site is the actual cached site.

					// get a copy of the site from the cache
					Site site = new BaseSite(this,group.getContainingSite(), true);

					// get the group from there
					rv = site.getGroup(refOrId);
				}
			}

			// if we don't have it yet, get the group's site, and the group from there
			if (rv == null)
			{
				String siteId = storage().findGroupSiteId(refOrId);
				if (siteId != null)
				{
					try
					{
						// read (and cache if enabled) the full site
						Site site = getDefinedSite(siteId);

						// here we return the group from the site, so the group's containing site is really the site that contains it.
						rv = site.getGroup(refOrId);
					}
					// we can ignore a site not found exception, just returning a null Group
					catch (IdUnusedException e)
					{
					}
				}
			}
		}

		return rv;
	}

	/**
	 * Adjust any site groups for this site so that the group membership is a subset of the site's membership.
	 * 
	 * @param siteId
	 *        The site to adjust.
	 */
	protected void enforceGroupSubMembership(String siteId)
	{
		// just being paranoid, but lets make sure we don't get stuck in a loop here -ggolden
		if (threadLocalManager().get("enforceGroupSubMembership") != null)
		{
			log.warn(".enforceGroupSubMembership: recursion avoided!: " + siteId);
			return;
		}
		threadLocalManager().set("enforceGroupSubMembership", siteId);

		try
		{
			Site site = getDefinedSite(siteId);
			for (Iterator i = site.getGroups().iterator(); i.hasNext();)
			{
				Group group = (Group) i.next();
				group.keepIntersection(site);
			}

			try
			{
				// save any changed group azg
				enableAzgSecurityAdvisor();
				saveGroupAzgs(site);
			}
			finally
			{
				disableAzgSecurityAdvisor();
			}
		}
		catch (IdUnusedException e)
		{
			// site not found - will happen with site delete, no problem
		}

		threadLocalManager().set("enforceGroupSubMembership", null);
	}

	
	/**
	 * @inheritDoc
	 */
	public void addSiteAdvisor(SiteAdvisor siteAdvisor)
	{
		siteAdvisors.add(siteAdvisor);
	}

	/**
	 * @inheritDoc
	 */
	public List<SiteAdvisor> getSiteAdvisors()
	{
		return Collections.unmodifiableList(siteAdvisors);
	}

	/**
	 * @inheritDoc
	 */
	public boolean removeSiteAdvisor(SiteAdvisor siteAdvisor)
	{
		return siteAdvisors.remove(siteAdvisor);
	}

	/**
	 * @inheritDoc
	 */
	public void addSiteRemovalAdvisor(SiteRemovalAdvisor siteRemovalAdvisor)
	{
		siteRemovalAdvisors.add(siteRemovalAdvisor);
	}

	/**
	 * @inheritDoc
	 */
	public boolean removeSiteRemovalAdvisor(SiteRemovalAdvisor siteRemovalAdvisor)
	{
		return siteRemovalAdvisors.remove(siteRemovalAdvisor);
	}

	/**
	 * Process site update events (from EventTrackingService)
	 *
	 * The only events processed now are "site.usersite.invalidate" and "site.visit.denied",
	 * which were added to encapsulate the peculiarities of real-world events (users being
	 * added and removed from sites) versus the posted site and authz group events. The
	 * interesting actions where we can expect a user's view of membership to update
	 * immediately (add, join, unjoin) flow through setSiteSecurity with some variety of
	 * other events being posted. The invalidate event gets posted at the conclusion of that
	 * method to be picked up here, across the cluster. The visit denied event gets posted
	 * when a known user visits a known site and is denied. This is considered a signal to
	 * regenerate that user's cache since the assumption is generally that the user would have
	 * clicked a site link presented to them before their access was revoked.
	 *
	 * @param _ The Observable, which is effectively nothing with ETS
	 * @param eventObj The event from ETS; will be checked and no-op if null or not an Event
	 */
	public void update(Observable _, Object eventObj) {
		if (eventObj == null || !(eventObj instanceof Event))
		{
			return;
		}

		// TODO: Update this dispatching once ETS can register listeners for specific events
		Event event = (Event) eventObj;

		// When membership updates come in, we purge the user-site cache for all members.
		// This could be optimized by integrating the site and user-site caches more closely, but
		// it is a reasonable cost since the user-site cache will be regenerated for each user on
		// on their first portal hit. Membership updates are much more rare than visits, so this
		// allows the cache to have a reasonably high TTL across the cluster. The site will be
		// cached on every server, so it will not force each user to retrieve it, just recalculate
		// based on the cache and any other uncached sites on the next portal hit.
		//
		// We are catching adds and role updates with the invalidate event. The denied visit event
		// captures the case where someone visits a site from which they were removed. Drops are
		// harder to catch (because EVENT_USER_SITE_MEMBERSHIP_REMOVE is not fired consistently),
		// but this approach is generally acceptable. A user may need to be added to a site and
		// may communicate with someone who can do so and is logged in on a different server. This
		// will invalidate immediately. Drops are not typically communicated. The user will not
		// retain the privilege to view the site and the denied visit will remove the inaccessible
		// site from the user cache, so having his/her cache persist for the TTL is not problematic.

		String eventType = event.getEvent();

		if (EVENT_SITE_USER_INVALIDATE.equals(eventType) || PreferencesService.SECURE_EDIT_PREFS.equals(eventType)) {
			// KNL-1171: always clear the cache for the user as the Site below may have been deleted
			clearUserCacheForUser(event.getUserId());

			try {
				Site site = getSite(event.getResource());
				clearUserCacheForSite(site);
			} catch (IdUnusedException e) {
				if (log.isDebugEnabled())
				{
					log.debug("Site not found when handling an event (" + eventType + "), ID/REF: " + event.getResource());
				}
			}
		}
		else if (EVENT_SITE_VISIT_DENIED.equals(eventType) || AuthzGroupService.SECURE_UNJOIN_AUTHZ_GROUP.equals(eventType))
		{
			clearUserCacheForUser(event.getUserId());
		}
		else if(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP.equals(eventType) || SiteService.SECURE_UPDATE_GROUP_MEMBERSHIP.equals(eventType)
				|| SiteService.SECURE_UPDATE_SITE.equals(eventType))
		{
			notifySiteParticipant("/gradebook/" + event.getContext() + "/");
		}
	}
	protected Storage storage() {
		return m_storage;
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	public List<Site> getSubSites(String siteId) {
				
		if (StringUtils.isBlank(siteId)) {
			return new ArrayList<Site>();
		}
		Map<String, String> propMap = new HashMap<String, String>();
		propMap.put(PROP_PARENT_ID, siteId);
		
		return this.getSites(SelectionType.ACCESS, null, null, propMap, SiteService.SortType.TITLE_ASC, null);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	public String getParentSite(String siteId) {
		
		if (StringUtils.isBlank(siteId)) {
			return null;
		}
		
		String parentId = null;
		try {
			Site s = this.getSite(siteId);
			ResourceProperties rp = s.getProperties();
			parentId = rp.getProperty(PROP_PARENT_ID);
		} catch (IdUnusedException e) {
			log.error("getParentSite failed for " + siteId + ": " + e.getClass() + " : " + e.getMessage());
			return null;
		}
		return parentId;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserSpecificSiteTitle(Site site, String userID)
	{
		return getUserSpecificSiteTitle(site, userID, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserSpecificSiteTitle(Site site, String userID, List<String> siteProviders)
	{
		if( m_siteTitleAdvisor != null )
		{
			return m_siteTitleAdvisor.getUserSpecificSiteTitle( site, userID, siteProviders );
		}
		else
		{
			return site.getTitle();
		}
	}
	
	public void notifySiteParticipant(String filter) {		
		List<Notification> notifications = notificationService().findNotifications(
				"gradebook.updateItemScore", 
				filter);
		
		for (Notification notification : notifications) {
			String eventDataString = notification.getProperties().getProperty("SAKAI:conditionEventState");
			
			Event event = eventTrackingService().newEvent(
					"cond+" + notification.getFunction(), 
					notification.getResourceFilter() + eventDataString, 
					false);
			
			NotificationAction action = notification.getAction();
			action.notify(notification, event);
		}
	}
}
