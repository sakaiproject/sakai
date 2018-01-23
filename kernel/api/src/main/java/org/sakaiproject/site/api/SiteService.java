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

package org.sakaiproject.site.api;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.w3c.dom.Element;

/**
 * <p>
 * SiteService manages Sites.
 * </p>
 */
public interface SiteService extends EntityProducer
{
	/** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
	static final String APPLICATION_ID = "sakai:site";

	/** The Entity Reference sub-type for Site references. */
	static final String SITE_SUBTYPE = "site";

	/** The Entity Reference sub-type for Group references. */
	static final String GROUP_SUBTYPE = "group";

	/** The Entity Reference sub-type for Page references. */
	static final String PAGE_SUBTYPE = "page";

	/** The Entity Reference sub-type for Tool references. */
	static final String TOOL_SUBTYPE = "tool";

	/** This string starts the references to resources in this service. */
	static final String REFERENCE_ROOT = Entity.SEPARATOR + SITE_SUBTYPE;

	/** Name for the event of accessing role swap functionality. */
	static final String SITE_ROLE_SWAP = "site.roleswap";
	
	/** Name for the event of visiting a site. */
	static final String SITE_VISIT = "site.visit";

	/** Name for the event of visiting an unpublished site. */
	static final String SITE_VISIT_UNPUBLISHED = "site.visit.unp";

	/** Name for the event of accessing a site. */
	// static final String SECURE_ACCESS_SITE = "site.access";
	/** Name for the event of adding a site. */
	static final String SECURE_ADD_SITE = "site.add";

	/** Name for the event of adding a course site */ 
	static final String SECURE_ADD_COURSE_SITE = "site.add.course";
	
	/** Name for the event of adding a portfolio site */ 
	static final String SECURE_ADD_PORTFOLIO_SITE = "site.add.portfolio";
	
	/** Name for the event of adding a project site */ 
	static final String SECURE_ADD_PROJECT_SITE = "site.add.project";

	/** Name for the event of creating a site from a sakai archive (KNL-1210) */
	static final String SECURE_IMPORT_ARCHIVE = "site.import.archive";
	
	/** Name for the event of adding a user's Home site. */
	static final String SECURE_ADD_USER_SITE = "site.add.usersite";

	/** Name for the event of removing a site. */
	static final String SECURE_REMOVE_SITE = "site.del";
	
	/** Name for the event of removing a site that has already been softly deleted */
	static final String SECURE_REMOVE_SOFTLY_DELETED_SITE = "site.del.softly.deleted";
	
	/** Name for the event of visiting a softly deleted site. */
	static final String SITE_VISIT_SOFTLY_DELETED = "site.visit.softly.deleted";

	/** Name for the event of updating a site. */
	static final String SECURE_UPDATE_SITE = "site.upd";

	/** Name for the event of updating a site's membership. */
	static final String SECURE_UPDATE_SITE_MEMBERSHIP = "site.upd.site.mbrshp";

	/** Name for the event of updating a site's groups' memberships. */
	static final String SECURE_UPDATE_GROUP_MEMBERSHIP = "site.upd.grp.mbrshp";

	/** Name for the event of viewing project site participants. */
	static final String SECURE_VIEW_ROSTER = "site.viewRoster";

	/** The name of the main container for a resource in a site (channel, calendar, etc.). */
	static final String MAIN_CONTAINER = "main";

	/** The name of a special site that is a template for non-user sites. */
	static final String SITE_TEMPLATE = "!worksite";

	/** The name of a special site that is a template for user sites. */
	static final String USER_SITE_TEMPLATE = "!user";

	/** The name of a special site that is used if the requested site is not available. */
	static final String SITE_ERROR = "!error";

	/** The name of a special site that is used if the requested url is not understood. */
	static final String URL_ERROR = "!urlError";

	/** The property to indicate a parent site's id */
	public static final String PROP_PARENT_ID = "sakai:parent-id";

	/** The property to indicate whether or not subsites are to be added to the tool list */
	public static final String PROP_SHOW_SUBSITES = "sakai:show-subsites";
	
	/** The configuration property for enabling or disabling user-site caching, defaults to true. */
	public static final String PROP_CACHE_USER_SITES = "user.site.cache.enabled";

	/**
	 * Event for adding user to site
	 * info logged: user id, site id, role name, active status, provided status
	 */
	static final String EVENT_USER_SITE_MEMBERSHIP_ADD = "user.site.membership.add";
	
	/**
	 * Event for changing user role in site
	 * info logged: user id, site id, old role name, new role name, active status, provided status
	 */
	static final String EVENT_USER_SITE_MEMBERSHIP_UPDATE = "user.site.membership.update";
	
	/**
	 * Event for removing user in site
	 * info logged: user id, site id, role name
	 */
	static final String EVENT_USER_SITE_MEMBERSHIP_REMOVE = "user.site.membership.delete";
	
	/**
	 * Event for adding user to group
	 * info logged: user id, group id, role name, active status, provided status
	 */
	static final String EVENT_USER_GROUP_MEMBERSHIP_ADD = "user.group.membership.add";	
	
	/**
	 * Event for changing user role in group
	 * info logged: user id, site id, old role name, new role name, active status, provided status
	 */
	static final String EVENT_USER_GROUP_MEMBERSHIP_UPDATE = "user.group.membership.update";
	
	/**
	 * Event for removing user in group
	 * info logged: user id, site id, role name
	 */
	static final String EVENT_USER_GROUP_MEMBERSHIP_REMOVE = "user.group.membership.delete";

	/**
	 * Event for recording site visits that are denied based on permissions.
	 * The resource referenced is the Site ID and the User ID will be available.
	 */
	static final String EVENT_SITE_VISIT_DENIED = "site.visit.denied";

	/**
	 * An event to trigger User-Site cache invalidation across the cluster.
	 * The resource referenced is the Site ID.
	 */
	static final String EVENT_SITE_USER_INVALIDATE = "site.usersite.invalidate";

	/**
	 * An event for starting the site import
	 */
	static final String EVENT_SITE_IMPORT_START = "site.import.start";
	
	/**
	 * An event for ending the site import
	 */
	static final String EVENT_SITE_IMPORT_END = "site.import.end";

	/**
	 * An event for starting the site duplication
	 */
	static final String EVENT_SITE_DUPLICATE_START = "site.duplicate.start";
	
	/**
	 * An event for ending the site duplication
	 */
	static final String EVENT_SITE_DUPLICATE_END = "site.duplicate.end";

	/**
	 * An event tracking roster add
	 */
	static final String EVENT_SITE_ROSTER_ADD = "site.roster.add";
	
	/**
	 * An event for tracking roster remove
	 */
	static final String EVENT_SITE_ROSTER_REMOVE = "site.roster.remove";

	/** An event for publishing a site. */
	static final String EVENT_SITE_PUBLISH = "site.publish";
	
	/** An event for unpublishing a site. */
	static final String EVENT_SITE_UNPUBLISH = "site.unpublish";
	
	/**
	 * <p>
	 * SelectionType enumerates different supported types of selection criteria for getting / counting sites.
	 * </p>
	 */
	public class SelectionType
	{
		private final String m_id;

		private final boolean m_ignoreSpecial;

		private final boolean m_ignoreUser;

		private final boolean m_ignoreUnpublished;
		
		//always true, we always ignore unpublished sites
		private final boolean m_ignoreSoftlyDeleted;

		private SelectionType(String id, boolean ignoreSpecial, boolean ignoreUser, boolean ignoreUnpublished, boolean ignoreSoftlyDeleted)
		{
			m_id = id;
			m_ignoreSpecial = ignoreSpecial;
			m_ignoreUser = ignoreUser;
			m_ignoreUnpublished = ignoreUnpublished;
			m_ignoreSoftlyDeleted =  ignoreSoftlyDeleted;
		}

		public String toString()
		{
			return m_id;
		}

		public boolean isIgnoreSpecial()
		{
			return m_ignoreSpecial;
		}

		public boolean isIgnoreUser()
		{
			return m_ignoreUser;
		}

		public boolean isIgnoreUnpublished()
		{
			return m_ignoreUnpublished;
		}
		
		public boolean isIgnoreSoftlyDeleted()
		{
			return m_ignoreSoftlyDeleted;
		}

		/** Get sites that the current user has read access to (non-myWorkspace, non-special). */
		public static final SelectionType ACCESS = new SelectionType("access", true, true, false, true);

		/** Get sites that the current user has write access to (non-myWorkspace, non-special). */
		public static final SelectionType UPDATE = new SelectionType("update", true, true, false, true);

		/** Get sites that the current user does not have read access to but are joinable (non-myWorkspace, non-special). */
		public static final SelectionType JOINABLE = new SelectionType("joinable", true, true, true, true);

		/** Get sites that are marked for view (non-myWorkspace, non-special). */
		public static final SelectionType PUBVIEW = new SelectionType("pubView", true, true, true, true);

		/** Get any sites. */
		public static final SelectionType ANY = new SelectionType("any", false, false, false, true);

		/** Get any non-user sites. */
		public static final SelectionType NON_USER = new SelectionType("nonUser", false, true, false, true);

		/** Get sites that the current user is a member of regardless whether site is published */
		public static final SelectionType MEMBER = new SelectionType("member", true, true, false, true);
		
		/** Get my deleted sites. */
		public static final SelectionType DELETED = new SelectionType("deleted", true, true, false, false);

		/** Get any deleted sites, normally used by admin or purge job. */
		public static final SelectionType ANY_DELETED = new SelectionType("anyDeleted", false, false, false, false);

		/** Get unpublished sites the current user has access to */
		public static final SelectionType INACTIVE_ONLY = new SelectionType("inactive", true, true, false, true);
	}

	/**
	 * <p>
	 * SortType enumerates different supported types of site sorting.
	 * </p>
	 */
	public class SortType
	{
		private final String m_id;

		private final boolean m_asc;

		private SortType(String id, boolean asc)
		{
			m_id = id;
			m_asc = asc;
		}

		public String toString()
		{
			return m_id;
		}

		public boolean isAsc()
		{
			return m_asc;
		}

		/** Sort on title ASC */
		public static final SortType NONE = new SortType("none", true);

		/** Sort on id ASC */
		public static final SortType ID_ASC = new SortType("id", true);

		/** Sort on id DESC */
		public static final SortType ID_DESC = new SortType("id", false);

		/** Sort on title ASC */
		public static final SortType TITLE_ASC = new SortType("title", true);

		/** Sort on title DESC */
		public static final SortType TITLE_DESC = new SortType("title", false);

		/** Sort on type ASC */
		public static final SortType TYPE_ASC = new SortType("type", true);

		/** Sort on type DESC */
		public static final SortType TYPE_DESC = new SortType("type", false);

		/** Sort on published ASC */
		public static final SortType PUBLISHED_ASC = new SortType("published", true);

		/** Sort on published DESC */
		public static final SortType PUBLISHED_DESC = new SortType("published", false);

		/** Sort on created by ASC */
		public static final SortType CREATED_BY_ASC = new SortType("created by", true);

		/** Sort on created by DESC */
		public static final SortType CREATED_BY_DESC = new SortType("created by", false);

		/** Sort on modified by ASC */
		public static final SortType MODIFIED_BY_ASC = new SortType("modified by", true);

		/** Sort on modified by DESC */
		public static final SortType MODIFIED_BY_DESC = new SortType("modified by", false);

		/** Sort on created time ASC */
		public static final SortType CREATED_ON_ASC = new SortType("created on", true);

		/** Sort on created time DESC */
		public static final SortType CREATED_ON_DESC = new SortType("created on", false);

		/** Sort on modified time ASC */
		public static final SortType MODIFIED_ON_ASC = new SortType("modified on", true);

		/** Sort on modified time DESC */
		public static final SortType MODIFIED_ON_DESC = new SortType("modified on", false);
		
		/** Sort on softly deleted ASC */
		public static final SortType SOFTLY_DELETED_ASC = new SortType("softly deleted", true);
		
		/** Sort on softly deleted DESC */
		public static final SortType SOFTLY_DELETED_DESC = new SortType("softly deleted", false);
		
		/** Sort on softly deleted ASC */
		public static final SortType SOFTLY_DELETED_DATE_ASC = new SortType("softly deleted date", true);
		
		/** Sort on softly deleted DESC */
		public static final SortType SOFTLY_DELETED_DATE_DESC = new SortType("softly deleted date", false);
	}

	/**
	 * Get an array of names to match the site page layout options.
	 * 
	 * @return The array of human readable layout titles possible for any site page.
	 */
	String[] getLayoutNames();

	/**
	 * check permissions for accessing (i.e. visiting) a site. If a null site id is supplied, false will be returned.
     * Similarly, if no site with the supplied id is found, false will be returned.
	 * 
	 * @param id
	 *        The site id.
	 * @return true if the site is allowed to access the site, false if not.
	 */
	boolean allowAccessSite(String id);

	/**
	 * Is this a valid site id?
	 * 
	 * @param id
	 *        The site id string. If null, false is returned.
	 * @return True if a site with this id is defined, false if not.
	 */
	boolean siteExists(String id);

	/**
	 * Access a site object. This method does not perform any security/permission checks. 
	 * If you need permission checks to occur, use {@link getSiteVisit(String id)} instead
	 * 
	 * @param id
	 *        The site id string.
	 * @return A site object containing the site information.
	 * @exception IdUnusedException
	 *            if not found.
	 */
	Site getSite(String id) throws IdUnusedException;

	/**
	 * Access a site object for purposes of having the user visit the site - visitation permissions are in effect.
	 * 
	 * @param id
	 *        The site id string.
	 * @return A site object containing the site information.
	 * @exception IdUnusedException
	 *            if not found.
	 * @exception PermissionException
	 *            if the current user does not have permission to visit this site.
	 */
	Site getSiteVisit(String id) throws IdUnusedException, PermissionException;

	/**
	 * check permissions for updating a site
	 * 
	 * @param id
	 *        The site id.
	 * @return true if the site is allowed to update the site, false if not.
	 */
	boolean allowUpdateSite(String id);

	/**
	 * check permissions for updating a site's membership
	 * 
	 * @param id
	 *        The site id.
	 * @return true if the site is allowed to update the site's membership, false if not.
	 */
	boolean allowUpdateSiteMembership(String id);

	/**
	 * check permissions for updating a site's groups' memberships
	 * 
	 * @param id
	 *        The site id.
	 * @return true if the site is allowed to update the site's groups' memberships, false if not.
	 */
	boolean allowUpdateGroupMembership(String id);

	/**
	 * Save any updates to this site - it must be a defined site (the id must exist) and the user must have update permissions.
	 * 
	 * @param site
	 *        The site, modified, to save.
	 * @throws IdUnusedException
	 *         If the site's id is not a defined site id.
	 * @throws PermissionException
	 *         If the end user does not have permission to update the site.
	 */
	void save(Site site) throws IdUnusedException, PermissionException;

	/**
	 * Save only site membership updates to this site - it must be a defined site (the id must exist) and the user must have site membership update permissions.
	 * 
	 * @param site
	 *        The site, modified, to save.
	 * @throws IdUnusedException
	 *         If the site's id is not a defined site id.
	 * @throws PermissionException
	 *         If the end user does not have permission to update the membership of the site.
	 */
	void saveSiteMembership(Site site) throws IdUnusedException, PermissionException;

	/**
	 * Save only site group membership updates to this site - it must be a defined site (the id must exist) and the user must have site group membership update permissions.
	 * 
	 * @param site
	 *        The site, modified, to save.
	 * @throws IdUnusedException
	 *         If the site's id is not a defined site id.
	 * @throws PermissionException
	 *         If the end user does not have permission to update the membership of the site.
	 */
	void saveGroupMembership(Site site) throws IdUnusedException, PermissionException;

	/**
	 * Save a site's information display fields: description and info url
	 * 
	 * @param id
	 *        The site id to update.
	 * @param description
	 *        The updated description.
	 * @param infoUrl
	 *        The updated infoUrl
	 * @throws IdUnusedException
	 *         If the site's id is not a defined site id.
	 * @throws PermissionException
	 *         If the end user does not have permission to update the site.
	 */
	void saveSiteInfo(String id, String description, String infoUrl) throws IdUnusedException, PermissionException;

	/**
	 * check permissions for addSite().
	 * 
	 * @param id
	 *        The site id.
	 * @return true if the site is allowed to addSite(id), false if not.
	 */
	boolean allowAddSite(String id);
	
	/**
	 *  Can the user add sites of type Course as defined by courseSiteType in sakai.properties
	 * @return
	 */
	boolean allowAddCourseSite();

	/**
	 *  Can the user add sites of type Portfolio as defined by portfolioSiteType in sakai.properties
	 * @return
	 */
	boolean allowAddPortfolioSite();
	
	/**
	 *  Can the user add sites of type Project as defined by projectSiteType in sakai.properties
	 * @return
	 */
	boolean allowAddProjectSite();

	/**
	 * Can the user create sites with a sakai archive 
	 * @return
	*/
	boolean allowImportArchiveSite();

	/**
	 * Add a new site. The site will exist with just an id once done, so remove() it if you don't want to keep it.
	 * 
	 * @param id
	 *        The site id.
	 * @param type
	 *        The site type.
	 * @return The new site object.
	 * @exception IdInvalidException
	 *            if the site id is invalid.
	 * @exception IdUsedException
	 *            if the site id is already used.
	 * @exception PermissionException
	 *            if the current user does not have permission to add a site.
	 */
	Site addSite(String id, String type) throws IdInvalidException, IdUsedException, PermissionException;

	/**
	 * Add a new site. Will be structured just like <other>.
	 * 
	 * @param id
	 *        The site id.
	 * @param other
	 *        The site to make this site a structural copy of.
	 * @return The new site object.
	 * @exception IdInvalidException
	 *            if the site id is invalid.
	 * @exception IdUsedException
	 *            if the site id is already used.
	 * @exception PermissionException
	 *            if the current user does not have permission to add a site.
	 */
	Site addSite(String id, Site other) throws IdInvalidException, IdUsedException, PermissionException;

	/**
	 * check permissions for removeSite().
	 * 
	 * @param id
	 *        The site id.
	 * @return true if the site is allowed to removeSite(id), false if not.
	 */
	boolean allowRemoveSite(String id);

	/**
	 * Remove this site's information.
	 * 
	 * <p>If site.soft.deletion=true, the site will be softly deleted and user access will be removed.
	 * The site will be hard deleted after either the grace period has expired.
	 * The site may also be hard deleted by issuing another removeSite request by a user that has permission
	 * to remove softly deleted sites.
	 * 
	 * @param site
	 *        The site id.
	 * @exception PermissionException
	 *            if the current user does not have permission to remove this site.
	 * @exception IdUnusedException 
	 * 			  if site does not exist
	 */
	void removeSite(Site site) throws PermissionException, IdUnusedException;

	/**
	 * Access the internal reference which can be used to access the site from within the system.
	 * 
	 * @param id
	 *        The site id.
	 * @return The the internal reference which can be used to access the site from within the system.
	 */
	String siteReference(String id);

	/**
	 * Access the internal reference which can be used to access the site page from within the system.
	 * 
	 * @param siteId
	 *        The site id.
	 * @param pageId
	 *        The page id.
	 * @return The the internal reference which can be used to access the site page from within the system.
	 */
	String sitePageReference(String siteId, String pageId);

	/**
	 * Access the internal reference which can be used to access the site tool from within the system.
	 * 
	 * @param siteId
	 *        The site id.
	 * @param toolId
	 *        The tool id.
	 * @return The the internal reference which can be used to access the site tool from within the system.
	 */
	String siteToolReference(String siteId, String toolId);

	/**
	 * Access the internal reference which can be used to access the site group from within the system.
	 * 
	 * @param siteId
	 *        The site id.
	 * @param groupId
	 *        The group id.
	 * @return The the internal reference which can be used to access the site group from within the system.
	 */
	String siteGroupReference(String siteId, String groupId);

	/**
	 * Is this site (id or reference) a user site?
	 * 
	 * @param site
	 *        The site id or reference.
	 * @return true if this is a user site, false if not.
	 */
	boolean isUserSite(String site);

	/**
	 * Extract the user id for this user site from the site id or reference.
	 * 
	 * @param site
	 *        The site id or reference.
	 * @return The user id associated with this site.
	 */
	String getSiteUserId(String site);

	/**
	 * Form the site id for this user's site.
	 * 
	 * @param userId
	 *        The user id.
	 * @return The site id for this user's site.
	 */
	String getUserSiteId(String userId);

	/**
	 * Is this site (id or reference) a special site?
	 * 
	 * @param site
	 *        The site id or reference.
	 * @return true if this is a special site, false if not.
	 */
	boolean isSpecialSite(String site);

	/**
	 * Extract the special id for this special site from the site id or reference.
	 * 
	 * @param site
	 *        The site id or reference.
	 * @return The special id associated with this site.
	 */
	String getSiteSpecialId(String site);

	/**
	 * Form the site id for this special site.
	 * 
	 * @param special
	 *        The special id.
	 * @return The site id for this user's site.
	 */
	String getSpecialSiteId(String special);

	/**
	 * Form a display of the site title and id for this site.
	 * 
	 * @param id
	 *        The site id.
	 * @return A display of the site title and id for this site.
	 */
	String getSiteDisplay(String id);

	/**
	 * Access the ToolConfiguration that has this id, if one is defined, else return null. The tool may be on any Site and on any SitePage.
	 * 
	 * @param id
	 *        The id of the tool.
	 * @return The ToolConfiguration that has this id, if one is defined, else return null.
	 */
	ToolConfiguration findTool(String id);

	/**
	 * Access the Page that has this id, if one is defined, else return null. The page may be on any Site.
	 * 
	 * @param id
	 *        The id of the page.
	 * @return The SitePage that has this id, if one is defined, else return null.
	 */
	SitePage findPage(String id);

	/**
	 * check permissions for viewing project site participants
	 * 
	 * @param id
	 *        The site id.
	 * @return true if the site is allowed to addSite(id), false if not.
	 */
	boolean allowViewRoster(String id);

	/**
	 * Cause the current user to join the site as defined by the site's joinable flag and joiner role.
	 * 
	 * @param id
	 *        The site id.
	 * @throws IdUnusedException
	 *         if the id is not a valid site id.
	 * @exception PermissionException
	 *            if the current user does not have permission to join this site.
	 * @exception InUseException
	 *            if the site is otherwise being edited.
	 */
	void join(String id) throws IdUnusedException, PermissionException;
        
        /**
     * Check if current user has the allowed user account type to join the site.
     * The account type of the joining user can be restricted by the site maintainer if option is available system-wide.
     * 
     * @author sfoster9@uwo.ca, bjones86@uwo.ca
     * @param id
     *      The site id
     * @return true if the user has same user account type that the site is restricted to, or if the option is disabled system-wide
     */
    boolean isAllowedToJoin(String id);
    
    /**
     * Get the group that a joining user will be added to upon joining a site, if option is available system-wide
     * 
     * @author sfoster9@uwo.ca, bjones86@uwo.ca
     * @param id
     *      The site id
     * @return group id of the group that a joining user will be added to upon joining a site; an empty string represents no joiner group
     */
    String getJoinGroupId(String id);
    
    /**
	 * Determine if the current user is already a member of the site.
	 * 
     * @author sfoster9@uwo.ca, bjones86@uwo.ca
	 * @param siteID
	 * 		The ID of the site in question
	 * @return true if current user is a member of the site
	 */
    boolean isCurrentUserMemberOfSite(String id);
    
    /**
     * Checks if the system and the provided site allows account types of joining users to be limited
     * 
     * @author sfoster9@uwo.ca, bjones86@uwo.ca
     * @param id 
     *      The site to check if the join system will limit users to allowed account types
     * @return true if the join site is limiting account types
     */
    boolean isLimitByAccountTypeEnabled(String id);
    
    /**
     * Get the list of allowed account type categories
     * 
     * @author bjones86@uwo.ca
     * @return list of allowed account type categories from sakai.properties
     */
    LinkedHashSet<String> getAllowedJoinableAccountTypeCategories();
    
    /**
     * Get the (unfriendly) list of allowed account types
     * 
     * @author bjones86@uwo.ca
     * @return list of allowed account types from sakai.properties (internal account type key values)
     */
    List<String> getAllowedJoinableAccountTypes();
    
    /**
     * Get the 'friendly' list of AllowedJoinableAccount objects
     * 
     * @author bjones86@uwo.ca
     * @return
     */
    List<AllowedJoinableAccount> getAllowedJoinableAccounts();
    
    /**
     * Check if joiner group is enabled/disabled globally (sakai.property)
     * 
     * @author bjones86@uwo.ca
     * @return true/false (enabled/disabled)
     */
    boolean isGlobalJoinGroupEnabled();
    
    /**
     * Check if exclude from public list is enabled/disabled globally (sakai.property)
     * 
     * @author bjones86@uwo.ca
     * @return true/false (enabled/disabled)
     */
    boolean isGlobalJoinExcludedFromPublicListEnabled();
    
    /**
     * Check if join limited by account types is enabled/disabled globally (sakai.property)
     * 
     * @author bjones86@uwo.ca
     * @return true/false (enabled/disabled)
     */
    boolean isGlobalJoinLimitByAccountTypeEnabled();
    
    /**
     * Check if join from Site Browser is enabled/disabled globally (sakai.property)
     * 
     * @author bjones86@uwo.ca
     * @return true/false (enabled/disabled)
     */
    boolean isGlobalJoinFromSiteBrowserEnabled();

	/**
	 * check permissions for unjoin() - unjoining the site and removing all role relationships.
	 * 
	 * @param id
	 *        The site id.
	 * @return true if the user is allowed to unjoin(id), false if not.
	 */
	boolean allowUnjoinSite(String id);

	/**
	 * Cause the current user to unjoin the site, removing all role relationships.
	 * 
	 * @param id
	 *        The site id.
	 * @throws IdUnusedException
	 *         if the id is not a valid site id.
	 * @exception PermissionException
	 *            if the current user does not have permission to unjoin this site.
	 * @exception InUseException
	 *            if the site is otherwise being edited.
	 */
	void unjoin(String id) throws IdUnusedException, PermissionException;

	/**
	 * Compute the skin to use for the (optional) site specified in the id parameter. If no site specified, or if the site has no skin defined, use the configured default skin.
	 * 
	 * @param id
	 *        The (optional) site id.
	 * @return A skin to use for this site.
	 */
	String getSiteSkin(String id);

	/**
	 * Access a unique list of String site types for any site type defined for any site, sorted by type.
	 * 
	 * @return A list (String) of all used site types.
	 */
	List<String> getSiteTypes();

	/**
	 * Access a list of sites that the current user can visit, sorted by title.
	 *
	 * This is a convenience and performance wrapper for getSites, because there are many places that need
	 * the complete list of sites for the current user, and getSites is unnecessarily verbose in that case.
	 * Because the semantics of this call are specific, it can also be optimized by the implementation.
	 * 
	 * Unpublished sites are not included; use getSites(boolean, boolean) to control if unpublished sites are included or not.
	 *
	 * The sites returned follow the same semantics as those from
	 * {@link #getSites(SelectionType, Object, String, Map, SortType, PagingPosition) getSites}.
	 *
	 * This signature is a wrapper for {@link #getUserSites(boolean) getUserSites(true)}, requiring descriptions be included.
	 *
	 * @return A List<Site> of those sites the current user can access.
	 */
	List<Site> getUserSites();

	/**
	 * Access a list of sites that the current user can visit, sorted by title, optionally requiring descriptions.
	 *
	 * This is a convenience and performance wrapper for getSites, because there are many places that need
	 * the complete list of sites for the current user, and getSites is unnecessarily verbose in that case.
	 * Because the semantics of this call are specific, it can also be optimized by the implementation.
	 * 
	 * Unpublished sites are not included; use getSites(boolean, boolean) to control if unpublished sites are included or not.
	 *
	 * The sites returned follow the same semantics as those from
	 * {@link #getSites(SelectionType, Object, String, Map, SortType, PagingPosition) getSites}.
	 *
	 * @param requireDescription when true, full descriptions will be included; when false, full descriptions may be omitted.
	 * @return A List<Site> of those sites the current user can access.
	 */
	List<Site> getUserSites(boolean requireDescription);

	/**
	 * Access a list of sites that the current user can visit, sorted by title, optionally requiring descriptions.
	 *
	 * This is a convenience and performance wrapper for getSites, because there are many places that need
	 * the complete list of sites for the current user, and getSites is unnecessarily verbose in that case.
	 * Because the semantics of this call are specific, it can also be optimized by the implementation.
	 *
	 * The sites returned follow the same semantics as those from
	 * {@link #getSites(SelectionType, Object, String, Map, SortType, PagingPosition) getSites}.
	 *
	 * @param requireDescription when true, full descriptions will be included; when false, full descriptions may be omitted.
	 * @param includeUnpublishedSites when true, unpublished sites will be included; when false, unpublished sites will be omitted.
	 * @return A List<Site> of those sites the current user can access.
	 */
	List<Site> getUserSites(boolean requireDescription, boolean includeUnpublishedSites);
	
	/**
	 * Access a list of sites that the current user can visit, sorted by title, optionally requiring descriptions.
	 *
	 * This is a convenience and performance wrapper for getSites, because there are many places that need
	 * the complete list of sites for the current user, and getSites is unnecessarily verbose in that case.
	 * Because the semantics of this call are specific, it can also be optimized by the implementation.
	 *
	 * The sites returned follow the same semantics as those from
	 * {@link #getSites(SelectionType, Object, String, Map, SortType, PagingPosition) getSites}.
	 *
	 * @param requireDescription when true, full descriptions will be included; when false, full descriptions may be omitted.
	 * @param includeUnpublishedSites when true, unpublished sites will be included; when false, unpublished sites will be omitted.
	 * @param excludedSites list with siteIDs to be excluded from being loaded. If no exclusions are required, set to NULL or empty list.
	 * @return A List<Site> of those sites the current user can access.
	 */
	List<Site> getUserSites(boolean requireDescription, boolean includeUnpublishedSites, List excludedSites);

	/**
	 * Access a list of sites that the specified user can visit, sorted by title, optionally requiring descriptions.
	 *
	 * This is a convenience and performance wrapper for getSites. Unpublished sites are not included;
	 * use getSites(boolean, String, boolean) to control if unpublished sites are included or not.
	 *
	 * The sites returned follow the same semantics as those from
	 * {@link #getSites(SelectionType, Object, String, Map, SortType, PagingPosition, boolean, String) getSites}.
	 *
	 * @param requireDescription when true, full descriptions will be included; when false, full descriptions may be omitted.
	 * @param userId the returned sites will be those which can be accessed by the user with this internal ID. Uses the current user if null.
	 * @return A List<Site> of those sites the current user can access.
	 */
	List<Site> getUserSites(boolean requireDescription, String userId);

	/**
	 * Access a list of sites that the specified user can visit, sorted by title, optionally requiring descriptions.
	 *
	 * This is a convenience and performance wrapper for getSites.
	 * 
	 * Unpublished sites are not included; use getSites(String, boolean) to control if unpublished sites are included or not.
	 *
	 * The sites returned follow the same semantics as those from
	 * {@link #getSites(SelectionType, Object, String, Map, SortType, PagingPosition, boolean, String) getSites}.
	 *
	 * @param requireDescription when true, full descriptions will be included; when false, full descriptions may be omitted.
	 * @param userID the returned sites will be those which can be accessed by the user with this internal ID. Uses the current user if null.
	 * @param includeUnpublishedSites when true, unpublished sites will be included; when false, unpublished sites will be omitted.
	 * @return A List<Site> of those sites the current user can access.
	 */
	List<Site> getUserSites(boolean requireDescription, String userID, boolean includeUnpublishedSites);
	
	/**
	 * Access a list of sites that the specified user can visit, sorted by title, optionally requiring descriptions.
	 *
	 * This is a convenience and performance wrapper for getSites.
	 * 
	 * Unpublished sites are not included; use getSites(String, boolean) to control if unpublished sites are included or not.
	 *
	 * The sites returned follow the same semantics as those from
	 * {@link #getSites(SelectionType, Object, String, Map, SortType, PagingPosition, boolean, String) getSites}.
	 *
	 * @param requireDescription when true, full descriptions will be included; when false, full descriptions may be omitted.
	 * @param userID the returned sites will be those which can be accessed by the user with this internal ID. Uses the current user if null.
	 * @param includeUnpublishedSites when true, unpublished sites will be included; when false, unpublished sites will be omitted.
	 * @param excludedSites list with siteIDs to be excluded from being loaded. If no exclusions are required, set to NULL or empty list.
	 * @return A List<Site> of those sites the current user can access.
	 */
	List<Site> getUserSites(boolean requireDescription, String userID, boolean includeUnpublishedSites, List excludedSites);

	/**
	 * Access a list of Site objects that meet specified criteria.
	 * NOTE: The sites returned may not have child objects loaded. If these sites need to be saved
	 * a completely populated site should be retrieved from {@link #getSite(String)}
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
	 * @param userId 
	 *        The returned sites will be those which can be accessed by the user with this internal ID. Uses the current user if null.
	 * @return The List (Site) of Site objets that meet specified criteria.
	 */
	List<Site> getSites(SelectionType type, Object ofType, String criteria, Map<String, String> propertyCriteria, SortType sort, PagingPosition page, boolean requireDescription, String userId);

	/**
	 * Access a list of Site objects that meet specified criteria.
	 * NOTE: The sites returned may not have child objects loaded. If these sites need to be saved
	 * a completely populated site should be retrieved from {@link #getSite(String)}
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
	 * @return The List (Site) of Site objets that meet specified criteria.
	 */
	List<Site> getSites(SelectionType type, Object ofType, String criteria, Map<String, String> propertyCriteria, SortType sort, PagingPosition page);
	
	/**
	 * Access a list of Site objects that meet specified criteria, with control over description retrieval.
	 *
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
	 *        Additional selection criteria: sites returned will match this string somewhere in their id, title, description, or skin.
	 * @param propertyCriteria
	 *        Additional selection criteria: sites returned will have a property named to match each key in the map, whose values match (somewhere in their value) the value in the map (may be null or empty).
	 * @param sort
	 *        A SortType indicating the desired sort. For no sort, set to SortType.NONE.
	 * @param page
	 *        The PagePosition subset of items to return.
	 * @param requireDescription
	 *        When true, force a full retrieval of each description; when false, return any uncached descriptions as the empty string
	 * @return The List of Site objects that meet specified criteria, with potentially empty descriptions based on requireDescription and caching.
	 */
	List<Site> getSites(SelectionType type, Object ofType, String criteria, Map<String, String> propertyCriteria, SortType sort, PagingPosition page, boolean requireDescription);

	/**
	 * Get the Site IDs for all sites matching criteria.
	 * This is useful when you only need the listing of site ids (for other operations) and do not need the actual Site objects.
	 *
	 * All parameters are the same as {@link #getSites(org.sakaiproject.site.api.SiteService.SelectionType, Object, String, Map, org.sakaiproject.site.api.SiteService.SortType, PagingPosition, String)}
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
	 * @param userId
	 *        The returns sites will be those which can be accessed by the user with this internal ID. Uses the current user if null.
	 * @return a List of the Site IDs for the sites matching the criteria.
	 */
	List<String> getSiteIds(SelectionType type, Object ofType, String criteria, Map<String, String> propertyCriteria, SortType sort, PagingPosition page, boolean requireDescription, String userId);

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
	 * Get all sites that have been softly deleted
	 * 
	 * @return List of Sites or empty list if none.
	 */
	List<Site> getSoftlyDeletedSites();
	
	
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
	int countSites(SelectionType type, Object ofType, String criteria, Map<String, String> propertyCriteria);

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
	void setSiteSecurity(String siteId, Set<String> updateUsers, Set<String> visitUnpUsers, Set<String> visitUsers);

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
	void setUserSecurity(String userId, Set<String> updateSites, Set<String> visitUnpSites, Set<String> visitSites);

	/**
	 * Merge the site information from the archive into the given site.
	 * 
	 * @param toSiteId
	 *        The target site id.
	 * @param e
	 *        The XML DOM tree of content to merge.
	 * @param creatorId
	 *        The site creator id for target site.
	 * @return A log of status messages from the archive.
	 */
	String merge(String toSiteId, Element e, String creatorId);

	/**
	 * Access a Group object, given a reference string or id.
	 * 
	 * @param refOrId
	 *        The group reference or id string.
	 * @return The Group object if found, or null if not.
	 */
	Group findGroup(String refOrId);
	
	/**
	 * Registers a SiteAdvisor with the SiteService.  Each registered advisor will be
	 * called immediately upon a call to save(Site).
	 * 
	 * @param advisor The SiteAdvisor to add
	 */
	public void addSiteAdvisor(SiteAdvisor advisor);
	
	/**
	 * Removes a SiteAdvisor.
	 * 
	 * @param advisor The SiteAdvisor to remove
	 * @return Whether the SiteAdvisor was previously registered and hence removed.
	 */
	public boolean removeSiteAdvisor(SiteAdvisor advisor);
	
	/**
	 * Lists the current SiteAdvisors registered with the SiteService.
	 * 
	 * @return An unmodifiable List containing the currently registered SiteAdvisors
	 */
	public List<SiteAdvisor> getSiteAdvisors();
	
	/**
	 * check permissions for role swapping capabilites
	 *
	 * @param id
	 *        The site id.
	 * @return true if the site is allowed to addRoleSwap(id), false if not.
	 */
	boolean allowRoleSwap(String id);
	
	/**
	 * returns all type strings that are associated with specified site type.
	 * Following is an example of site type settings, which defines two strings as "course"-type
	 * courseSiteType.count=2
	 * courseSiteType.1=course
	 * courseSiteType.2=course2
	 * @param type Site type
	 * @return list of site type strings
	 */
	public List<String> getSiteTypeStrings(String type);

	/**
	 * Add an advisor that gets called just before a site is deleted.
	 * @param siteRemovalAdvisor The site removal advisor which will get called.
	 */
	public void addSiteRemovalAdvisor(SiteRemovalAdvisor siteRemovalAdvisor);

	/**
	 * Remove an advisor that gets called just before a site is deleted.
	 * @param siteRemovalAdvisor The site removal advisor which is no longer needed.
	 * @return <code>true</code> is the advisor was removed.
	 */
	public boolean removeSiteRemovalAdvisor(SiteRemovalAdvisor siteRemovalAdvisor);
	
	/**
	 * Get the list of Sites that list the given site as a parent, ie the child sites of the given site
	 * 
	 * @param siteId - parent siteId
	 * @return
	 */
	public List<Site> getSubSites(String siteId);
	
	/**
	 * Get the parent siteId of the given site. A site can only have one parent.
	 * 
	 * @param siteId - child siteId
	 * @return
	 */
	public String getParentSite(String siteId);

	/**
	 * Given a site and a user ID, return the appropriate site or section title for the user.
	 * 
	 * SAK-29138 - Takes into account 'portal.use.sectionTitle' sakai.property; 
	 * if set to true, this method will return the title of the section the current 
	 * user is enrolled in for the site (if it can be found). Otherwise, it will 
	 * return the site title (default behaviour).
	 * 
	 * @param site the site in question
	 * @param userID the ID of the current user
	 * @return the site or section title
	 */
	public String getUserSpecificSiteTitle( Site site, String userID );

	/**
	 * Similar to getUserSpecificSiteTitle(Site site, String userId), but consumes the specified siteProviders (for performance savings)
	 *
	 * @see getUserSpecificSiteTitle(Site site, String userId)
	 * @param siteProviders the site providers corresponding to the specified site; if null, they will be looked up
	 */
	public String getUserSpecificSiteTitle(Site site, String userID, List<String> siteProviders);
}
