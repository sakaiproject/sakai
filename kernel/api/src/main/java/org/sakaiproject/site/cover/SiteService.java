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

package org.sakaiproject.site.cover;

import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.site.api.Site;

/**
 * <p>
 * SiteService is a static Cover for the {@link org.sakaiproject.site.api.SiteService SiteService}; see that interface for usage details.
 * </p>
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class SiteService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.site.api.SiteService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.site.api.SiteService) ComponentManager
						.get(org.sakaiproject.site.api.SiteService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.site.api.SiteService) ComponentManager.get(org.sakaiproject.site.api.SiteService.class);
		}
	}

	private static org.sakaiproject.site.api.SiteService m_instance = null;

	public static java.lang.String APPLICATION_ID = org.sakaiproject.site.api.SiteService.APPLICATION_ID;

	public static java.lang.String REFERENCE_ROOT = org.sakaiproject.site.api.SiteService.REFERENCE_ROOT;
	
	public static java.lang.String SITE_ROLE_SWAP = org.sakaiproject.site.api.SiteService.SITE_ROLE_SWAP;

	public static java.lang.String SITE_VISIT = org.sakaiproject.site.api.SiteService.SITE_VISIT;

	public static java.lang.String SITE_VISIT_UNPUBLISHED = org.sakaiproject.site.api.SiteService.SITE_VISIT_UNPUBLISHED;

	public static java.lang.String SECURE_ADD_SITE = org.sakaiproject.site.api.SiteService.SECURE_ADD_SITE;

	public static java.lang.String SECURE_ADD_USER_SITE = org.sakaiproject.site.api.SiteService.SECURE_ADD_USER_SITE;

	public static java.lang.String SECURE_REMOVE_SITE = org.sakaiproject.site.api.SiteService.SECURE_REMOVE_SITE;
	
	public static java.lang.String SECURE_REMOVE_SOFTLY_DELETED_SITE = org.sakaiproject.site.api.SiteService.SECURE_REMOVE_SOFTLY_DELETED_SITE;
	
	public static java.lang.String SITE_VISIT_SOFTLY_DELETED = org.sakaiproject.site.api.SiteService.SITE_VISIT_SOFTLY_DELETED;
	
	public static java.lang.String SECURE_UPDATE_SITE = org.sakaiproject.site.api.SiteService.SECURE_UPDATE_SITE;
	
	public static java.lang.String SECURE_UPDATE_SITE_MEMBERSHIP = org.sakaiproject.site.api.SiteService.SECURE_UPDATE_SITE_MEMBERSHIP;

	public static java.lang.String SECURE_UPDATE_GROUP_MEMBERSHIP = org.sakaiproject.site.api.SiteService.SECURE_UPDATE_GROUP_MEMBERSHIP;

	public static java.lang.String SECURE_VIEW_ROSTER = org.sakaiproject.site.api.SiteService.SECURE_VIEW_ROSTER;

	public static java.lang.String MAIN_CONTAINER = org.sakaiproject.site.api.SiteService.MAIN_CONTAINER;

	public static java.lang.String SITE_TEMPLATE = org.sakaiproject.site.api.SiteService.SITE_TEMPLATE;

	public static java.lang.String USER_SITE_TEMPLATE = org.sakaiproject.site.api.SiteService.USER_SITE_TEMPLATE;

	public static java.lang.String SITE_ERROR = org.sakaiproject.site.api.SiteService.SITE_ERROR;

	public static java.lang.String URL_ERROR = org.sakaiproject.site.api.SiteService.URL_ERROR;

	public static java.lang.String SITE_SUBTYPE = org.sakaiproject.site.api.SiteService.SITE_SUBTYPE;

	public static java.lang.String GROUP_SUBTYPE = org.sakaiproject.site.api.SiteService.GROUP_SUBTYPE;

	public static java.lang.String PAGE_SUBTYPE = org.sakaiproject.site.api.SiteService.PAGE_SUBTYPE;

	public static java.lang.String TOOL_SUBTYPE = org.sakaiproject.site.api.SiteService.TOOL_SUBTYPE;

	public static java.lang.String PROP_PARENT_ID = org.sakaiproject.site.api.SiteService.PROP_PARENT_ID;

	public static java.lang.String PROP_SHOW_SUBSITES = org.sakaiproject.site.api.SiteService.PROP_SHOW_SUBSITES;

	public static java.lang.String EVENT_SITE_USER_INVALIDATE = org.sakaiproject.site.api.SiteService.EVENT_SITE_USER_INVALIDATE;

	public static java.lang.String EVENT_SITE_VISIT_DENIED = org.sakaiproject.site.api.SiteService.EVENT_SITE_VISIT_DENIED;
	
	public static java.lang.String EVENT_SITE_IMPORT_START = org.sakaiproject.site.api.SiteService.EVENT_SITE_IMPORT_START;
	
	public static java.lang.String EVENT_SITE_IMPORT_END = org.sakaiproject.site.api.SiteService.EVENT_SITE_IMPORT_END;

	public static java.lang.String EVENT_SITE_DUPLICATE_START = org.sakaiproject.site.api.SiteService.EVENT_SITE_DUPLICATE_START;
	
	public static java.lang.String EVENT_SITE_DUPLICATE_END = org.sakaiproject.site.api.SiteService.EVENT_SITE_DUPLICATE_END;
	
	public static java.lang.String EVENT_SITE_PUBLISH = org.sakaiproject.site.api.SiteService.EVENT_SITE_PUBLISH;
	
	public static java.lang.String EVENT_SITE_UNPUBLISH = org.sakaiproject.site.api.SiteService.EVENT_SITE_UNPUBLISH;

	public static boolean allowAccessSite(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return false;

		return service.allowAccessSite(param0);
	}

	public static org.sakaiproject.site.api.Site getSite(java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.getSite(param0);
	}

	public static boolean siteExists(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return false;

		return service.siteExists(param0);
	}

	public static org.sakaiproject.site.api.Site getSiteVisit(java.lang.String param0)
			throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.getSiteVisit(param0);
	}

	public static boolean allowUpdateSite(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return false;

		return service.allowUpdateSite(param0);
	}

	public static boolean allowUpdateSiteMembership(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return false;

		return service.allowUpdateSiteMembership(param0);
	}

	public static boolean allowUpdateGroupMembership(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return false;

		return service.allowUpdateGroupMembership(param0);
	}

	public static void save(org.sakaiproject.site.api.Site param0) throws org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return;

		service.save(param0);
	}

	public static void saveSiteMembership(org.sakaiproject.site.api.Site param0)
			throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return;

		service.saveSiteMembership(param0);
	}

	public static void saveGroupMembership(org.sakaiproject.site.api.Site param0)
			throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return;

		service.saveGroupMembership(param0);
	}

	public static void saveSiteInfo(java.lang.String param0, java.lang.String param1, java.lang.String param2)
			throws org.sakaiproject.exception.IdUnusedException, org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return;

		service.saveSiteInfo(param0, param1, param2);
	}

	public static boolean allowAddSite(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return false;

		return service.allowAddSite(param0);
	}

	public static boolean allowAddCourseSite() {
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return false;

		return service.allowAddCourseSite();		
	}
	
	public static boolean allowAddPortfolioSite() {
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return false;

		return service.allowAddPortfolioSite();		
	}

	public static boolean allowImportArchiveSite() {
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return false;

		return service.allowImportArchiveSite();
	}
	
	public static boolean allowAddProjectSite() {
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return false;

		return service.allowAddProjectSite();	
	}
	
	public static org.sakaiproject.site.api.Site addSite(java.lang.String param0, java.lang.String param1)
			throws org.sakaiproject.exception.IdInvalidException, org.sakaiproject.exception.IdUsedException,
			org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.addSite(param0, param1);
	}

	public static org.sakaiproject.site.api.Site addSite(java.lang.String param0, org.sakaiproject.site.api.Site param1)
			throws org.sakaiproject.exception.IdInvalidException, org.sakaiproject.exception.IdUsedException,
			org.sakaiproject.exception.PermissionException
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.addSite(param0, param1);
	}

	public static boolean allowRemoveSite(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return false;

		return service.allowRemoveSite(param0);
	}

	public static void removeSite(org.sakaiproject.site.api.Site param0) throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.IdUnusedException
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return;

		service.removeSite(param0);
	}
	
	public static java.lang.String siteReference(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.siteReference(param0);
	}

	public static java.lang.String sitePageReference(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.sitePageReference(param0, param1);
	}

	public static java.lang.String siteToolReference(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.siteToolReference(param0, param1);
	}

	public static java.lang.String siteGroupReference(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.siteGroupReference(param0, param1);
	}

	public static boolean isUserSite(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return false;

		return service.isUserSite(param0);
	}

	public static java.lang.String getSiteUserId(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.getSiteUserId(param0);
	}

	public static java.lang.String getUserSiteId(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.getUserSiteId(param0);
	}

	public static boolean isSpecialSite(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return false;

		return service.isSpecialSite(param0);
	}

	public static java.lang.String getSiteSpecialId(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.getSiteSpecialId(param0);
	}

	public static java.lang.String getSpecialSiteId(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.getSpecialSiteId(param0);
	}

	public static java.lang.String getSiteDisplay(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.getSiteDisplay(param0);
	}

	public static org.sakaiproject.site.api.ToolConfiguration findTool(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.findTool(param0);
	}

	public static org.sakaiproject.site.api.SitePage findPage(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.findPage(param0);
	}

	public static boolean allowViewRoster(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return false;

		return service.allowViewRoster(param0);
	}

	public static void unjoin(java.lang.String param0) throws org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.InUseException
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return;

		service.unjoin(param0);
	}

	public static boolean allowUnjoinSite(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return false;

		return service.allowUnjoinSite(param0);
	}

	public static java.lang.String getSiteSkin(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.getSiteSkin(param0);
	}

	public static java.util.List getSiteTypes()
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.getSiteTypes();
	}

	public static List<Site> getUserSites()
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.getUserSites();
	}

	public static List<Site> getUserSites(boolean requireDescription)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.getUserSites(requireDescription);
	}

	public static List<Site> getUserSites( boolean requireDescription, boolean includeUnpublishedSites )
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if( service == null ) return null;

		return service.getUserSites( requireDescription, includeUnpublishedSites );
	}

	public static java.util.List getSites(org.sakaiproject.site.api.SiteService.SelectionType param0, java.lang.Object param1,
			java.lang.String param2, java.util.Map param3, org.sakaiproject.site.api.SiteService.SortType param4,
			org.sakaiproject.javax.PagingPosition param5)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.getSites(param0, param1, param2, param3, param4, param5);
	}

	public static java.util.List getSites(org.sakaiproject.site.api.SiteService.SelectionType param0, java.lang.Object param1,
			java.lang.String param2, java.util.Map param3, org.sakaiproject.site.api.SiteService.SortType param4,
			org.sakaiproject.javax.PagingPosition param5, boolean param6)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.getSites(param0, param1, param2, param3, param4, param5, param6);
	}

	public static int countSites(org.sakaiproject.site.api.SiteService.SelectionType param0, java.lang.Object param1,
			java.lang.String param2, java.util.Map param3)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return 0;

		return service.countSites(param0, param1, param2, param3);
	}

	public static void setSiteSecurity(java.lang.String param0, java.util.Set param1, java.util.Set param2, java.util.Set param3)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return;

		service.setSiteSecurity(param0, param1, param2, param3);
	}

	public static void setUserSecurity(java.lang.String param0, java.util.Set param1, java.util.Set param2, java.util.Set param3)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return;

		service.setUserSecurity(param0, param1, param2, param3);
	}

	public static void join(java.lang.String param0) throws org.sakaiproject.exception.IdUnusedException,
			org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.InUseException
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return;

		service.join(param0);
	}

	public static java.lang.String merge(java.lang.String param0, org.w3c.dom.Element param1, java.lang.String param2)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.merge(param0, param1, param2);
	}

	public static org.sakaiproject.site.api.Group findGroup(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.findGroup(param0);
	}
	
	public static java.lang.String[] getLayoutNames()
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.getLayoutNames();
	}

	public static boolean allowRoleSwap(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return false;

		return service.allowRoleSwap(param0);
	}
	
	public static java.util.List<String> getSiteTypeStrings(java.lang.String param0)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.getSiteTypeStrings(param0);
	}

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
	public static String getUserSpecificSiteTitle( Site site, String userID )
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.getUserSpecificSiteTitle( site, userID );
	}

	/**
	 * Similar to getUserSpecificSiteTitle(Site site, String userId), but consumes the specified siteProviders (for performance savings)
	 *
	 * @see getUserspecificSiteTitle(Site site, String userId)
	 * @param siteProviders the site providers corresponding to the specified site; if null, they will be looked up
	 */
	public static String getUserSpecificSiteTitle(Site site, String userId, List<String> siteProviders)
	{
		org.sakaiproject.site.api.SiteService service = getInstance();
		if (service == null) return null;

		return service.getUserSpecificSiteTitle(site, userId, siteProviders);
	}
}
