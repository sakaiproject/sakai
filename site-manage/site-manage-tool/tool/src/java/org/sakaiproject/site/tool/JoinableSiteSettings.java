/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.site.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.cheftool.Context;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.AllowedJoinableAccount;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.tool.SiteAction.SiteInfo;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;

/**
 * This class handles all aspects of joinable site settings within the site-manage tool 
 * (Site Browser, Membership, Site Info->Edit Access, New Site->Edit Access).
 * 
 * @author bjones86, sfoster9, plukasew
 */
@Slf4j
public class JoinableSiteSettings
{

	// API's
	private static final UserDirectoryService 		userDirectoryService 	= (UserDirectoryService) 		ComponentManager.get( UserDirectoryService.class );
	private static final SiteService 				siteService 			= (SiteService) 				ComponentManager.get( SiteService.class );
	private static final DeveloperHelperService 	developerHelperService	= (DeveloperHelperService) 		ComponentManager.get( DeveloperHelperService.class );
	private static final ServerConfigurationService serverConfigService		= (ServerConfigurationService) 	ComponentManager.get( ServerConfigurationService.class );
	
	// State variable names
	private static final String STATE_JOIN_SITE_GROUP_ID				= "state_join_site_group";
	private static final String STATE_JOIN_SITE_EXCLUDE_PUBLIC_LIST		= "state_join_site_exclude_public_list";
	private static final String STATE_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE	= "state_join_site_limit_by_account_type";
	private static final String STATE_JOIN_SITE_ACCOUNT_TYPES 			= "state_join_site_account_types";
	private static final String STATE_JOIN_SITE_ACCOUNT_TYPE_PREFIX		= "state_join_site_account_type_";
	private static final String STATE_JOIN_SITE_SITE_BROWSER_SITE_ID	= "state_join_site_site_browser_site_id";
	
	// Site property names
	private static final String SITE_PROP_JOIN_SITE_GROUP_ID 				= "joinerGroup";
	private static final String SITE_PROP_JOIN_SITE_GROUP_NO_SEL				= "noSelection";
	private static final String SITE_PROP_JOIN_SITE_EXCLUDE_PUBLIC_LIST		= "joinExcludeFromPublicList";
	private static final String SITE_PROP_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE 	= "joinLimitByAccountType";
	private static final String SITE_PROP_JOIN_SITE_ACCOUNT_TYPES 			= "joinLimitedAccountTypes";
	
	// Context variable/element names
	private static final String CONTEXT_JOIN_SITE_GROUPS										= "siteGroups";
	private static final String CONTEXT_JOIN_SITE_GROUP_DROP_DOWN								= "selectJoinerGroup";
	private static final String CONTEXT_JOIN_SITE_EXCLUDE_PUBLIC_LIST_CHECKBOX					= "chkJoinExcludeFromPublicList";
	private static final String CONTEXT_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE_CHECKBOX				= "chkJoinLimitByAccountType";
	private static final String CONTEXT_JOIN_SITE_ACCOUNT_TYPES									= "joinableAccountTypes";
	private static final String CONTEXT_JOIN_SITE_ACCOUNT_CATEGORIES							= "joinableAccountTypeCategories";
	private static final String CONTEXT_JOIN_SITE_ACCOUNT_TYPE_CHECKBOX_PREFIX 					= "chkJoin-";
	private static final String CONTEXT_JOIN_SITE_GROUP_ENABLED									= "joinGroupEnabled";
	private static final String CONTEXT_JOIN_SITE_EXCLUDE_PUBLIC_LIST_ENABLED					= "joinExcludeFromPublicListEnabled";
	private static final String CONTEXT_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE_ENABLED					= "joinLimitAccountTypesEnabled";
	private static final String CONTEXT_JOIN_SITE_GROUP_ID										= SITE_PROP_JOIN_SITE_GROUP_ID;
	private static final String CONTEXT_JOIN_SITE_EXCLUDE_PUBLIC_LIST							= SITE_PROP_JOIN_SITE_EXCLUDE_PUBLIC_LIST;
	private static final String CONTEXT_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE							= SITE_PROP_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE;
	private static final String CONTEXT_JOIN_SITE_LIMIT_ACCOUNT_TYPES							= SITE_PROP_JOIN_SITE_ACCOUNT_TYPES;
	private static final String CONTEXT_JOIN_SITE_SITE_MAP										= "siteMap";
	private static final String CONTEXT_JOIN_SITE_CURRENT_USER									= "currentUser";
	private static final String CONTEXT_JOIN_SITE_ALREADY_MEMBER								= "alreadyMember";
	private static final String CONTEXT_JOIN_SITE_MSG											= "message";
	private static final String CONTEXT_JOIN_SITE_SUCCESS										= "success";
	private static final String CONTEXT_JOIN_SITE_SITE_TITLE									= "siteTitle";
	private static final String CONTEXT_JOIN_SITE_LINK											= "link";
	private static final String CONTEXT_JOIN_SITE_SITE_BROWSER_JOIN_ENABLED						= "siteBrowserJoinEnabled";
	private static final String CONTEXT_JOIN_SITE_GROUP_ENABLED_LOCAL_DISABLED_GLOBAL 			= "joinGroupEnabledLocalDisabledGlobal";
	private static final String CONTEXT_JOIN_SITE_EXCLUDE_ENABLED_LOCAL_DISABLED_GLOBAL 			= "joinExcludeEnabledLocalDisabledGlobal";
	private static final String CONTEXT_JOIN_SITE_LIMIT_ENABLED_LOCAL_DISABLED_GLOBAL 			= "joinLimitEnabledLocalDisabledGlobal";
	private static final String CONTEXT_UI_SERVICE = "uiService";
	
	// Message keys
	private static final String MSG_KEY_UNJOINABLE 			= "join.unjoinable";
	private static final String MSG_KEY_LOGIN				= "join.login";
	private static final String MSG_KEY_ALREADY_MEMBER_1		= "join.alreadyMember1";
	private static final String MSG_KEY_ALREADY_MEMBER_2	= "join.alreadyMember2";
	private static final String MSG_KEY_NOT_ALLOWED_TO_JOIN	= "join.notAllowed";
	private static final String MSG_KEY_JOIN_SUCCESS		= "join.success";
	private static final String MSG_KEY_JOIN_NOT_FOUND		= "join.notFound";
	private static final String MSG_KEY_JOIN_FAIL_PERM		= "join.failPermission";
	private static final String MSG_KEY_JOIN_FAIL			= "join.fail";
	
	// Random other things
	private static final String CSV_DELIMITER 			= ",";
	private static final String TRUE_STRING				= "true";
	private static final String FALSE_STRING				= "false";
	private static final String ON_STRING				= "on";
	private static final String FORM_PREFIX				= "form_";
	private static final String SITE_REF_PREFIX			= "/site/";
	private static final String SITE_BROWSER_MODE		= "sitebrowser.mode";
	private static final String DEFAULT_UI_SERVICE 		= "Sakai";
	public  static final String SITE_BROWSER_JOIN_MODE 	= "join";
	
	// sakai.properties
	private static final String SAK_PROP_UI_SERVICE = "ui.service";
	
	/**********************************************************************************************
	 ********************* SiteBrowserAction Methods (Site Browser tool) **************************
	 **********************************************************************************************/
	
	/**
	 * Prepare for the join context. Check all settings first before forwarding to build the context for the join mode
	 * 
	 * @param state
	 * 				the object to get the settings from
	 * @param rb
	 * 				the object used to access internationalized messages
	 * @param siteID
	 * 				the ID of the site in question
	 * @return a message string indicating a failure of some sort, or an empty string indicating the user is able to join
	 * the site in question
	 */
	public static String doJoinForSiteBrowser( SessionState state, ResourceLoader rb, String siteID )
	{
		String message = "";
		
		try
		{
			// Get the site and the current user
			Site site = siteService.getSite( siteID );
			User currentUser = userDirectoryService.getCurrentUser();
			
			// If the site isn't joinable, create the UI alert message
			if( !site.isJoinable() )
			{
				message = rb.getString( MSG_KEY_UNJOINABLE );
			}
			
			// If the user isn't logged in, create the UI alert message
			else if( currentUser == null || currentUser.getId() == null || "".equalsIgnoreCase( currentUser.getId() ) )
			{
				message = rb.getString( MSG_KEY_LOGIN );
			}
			
			// If the user is already a member of the site, create the UI alert message
			else if( siteService.isCurrentUserMemberOfSite( siteID ) )
			{
				message = rb.getString( MSG_KEY_ALREADY_MEMBER_1 );
			}
			
			// If join limitations are toggled, and they're not in the list of allowed joiner roles, create the UI message
			else if( siteService.isLimitByAccountTypeEnabled( siteID ) && !siteService.isAllowedToJoin( siteID ) )
			{
				message = rb.getString( MSG_KEY_NOT_ALLOWED_TO_JOIN );
			}
			
			// Otherwise, tell it to build the context for the join mode
			else
			{			
				state.setAttribute( STATE_JOIN_SITE_SITE_BROWSER_SITE_ID, siteID );
				state.setAttribute( SITE_BROWSER_MODE, SITE_BROWSER_JOIN_MODE );
			}
		}
		catch( IdUnusedException ex )
		{
			log.error( "doJoinForSiteBrowser()", ex );
			message = rb.getFormattedMessage( MSG_KEY_JOIN_NOT_FOUND, new Object[] { siteID } );
		}
		
		return message;
	}
	
	/**
	 * Build the context for the join mode.
	 * 
	 * @param state
	 * 				the object to get the settings from
	 * @param context
	 * 				the parameters being passed to the velocity template
	 * @param rb
	 * 				the object used to access internationalized messages
	 * @return the string postfix for the chef_sitebrowser_join.vm template
	 */
	public static String buildJoinContextForSiteBrowser( SessionState state, Context context, ResourceLoader rb )
	{
		// Get the site ID from the state
		String siteID = (String) state.getAttribute( STATE_JOIN_SITE_SITE_BROWSER_SITE_ID );
		String message = "";
		String siteTitle = "";
		String link = "";
		boolean success = false;
		
		try
		{
			// Get the user, site ID, realm ID, the current session and the joiner role
			Site site = siteService.getSite( siteID );
			User currentUser = userDirectoryService.getCurrentUser();
			siteTitle = site.getTitle();
			
			// If the site isn't joinable, create the UI message
			if( !site.isJoinable() )
			{
				message = rb.getString( MSG_KEY_UNJOINABLE );
			}
			
			// If the user isn't logged in, create the UI message
			else if( currentUser == null || currentUser.getId() == null || "".equalsIgnoreCase( currentUser.getId() ) )
			{
				message = rb.getString( MSG_KEY_LOGIN );
			}
			
			// If the user is already a member, create the UI message and the link
			else if( siteService.isCurrentUserMemberOfSite( siteID ) )
			{
				message = rb.getString( MSG_KEY_ALREADY_MEMBER_1 ) + " " + rb.getString( MSG_KEY_ALREADY_MEMBER_2 );
				link = developerHelperService.getLocationReferenceURL( SITE_REF_PREFIX + siteID );
				success = true;
			}
			
			// If join limitations are toggled, and they're not in the list of allowed joiner roles, create the UI message
			else if( !siteService.isAllowedToJoin( siteID ) )
			{
				message = rb.getString( MSG_KEY_NOT_ALLOWED_TO_JOIN );
			}
			
			// Otherwise, they're logged in, the site exists, it's joinable and either limit by account types is disabled globally or for the site,
			// Or limit by account types is enabled for this site and the user is of one of the correct account types allowed to join
			else
			{
				try
				{
					// Join the site, log success message, create the UI message and the link
					siteService.join( siteID );
					log.info( "Successfully added user '" + currentUser.getEid() + "' to site with ID '" + siteID + "'" );
					message = rb.getFormattedMessage( MSG_KEY_JOIN_SUCCESS, new Object[] { site.getTitle() } );
					link = developerHelperService.getLocationReferenceURL( SITE_REF_PREFIX + siteID );
					success = true;
				}
				catch( IdUnusedException ex )
				{
					log.debug( "buildJoinContextForSiteBrowser()", ex );
					message = rb.getFormattedMessage( MSG_KEY_JOIN_NOT_FOUND, new Object[] { siteID } );
				}
				catch( PermissionException ex )
				{
					log.debug( "buildJoinContextForSiteBrowser()", ex );
					message = rb.getString( MSG_KEY_JOIN_FAIL_PERM );
				}
				catch( IllegalArgumentException ex )
				{
					log.debug( "buildJoinContextForSiteBrowser()", ex );
					message = rb.getString( MSG_KEY_JOIN_FAIL );
				}
			}
		}
		catch( IdUnusedException ex )
		{
			log.debug( "buildJoinContextForSiteBrowser()", ex );
			message = rb.getFormattedMessage( MSG_KEY_JOIN_NOT_FOUND, new Object[] { siteID } );
		}
		
		// Load up the context object and return the string postfix for the chef_sitebrowser_join.vm template
		context.put( CONTEXT_JOIN_SITE_MSG, message );
		context.put( CONTEXT_JOIN_SITE_SUCCESS, success );
		context.put( CONTEXT_JOIN_SITE_SITE_TITLE, siteTitle );
		context.put( CONTEXT_JOIN_SITE_LINK, link );
		return "_" + SITE_BROWSER_JOIN_MODE;
	}
	
	/**
	 * Put the value of the exclude from public setting for the given site into the context
	 * for the Site Browser's visit interface
	 * 
	 * @param context
	 * 				the parameters being passed to the velocity template
	 * @param siteID
	 * 				the ID of the site in question
	 * @return status (true/false)
	 */
	public static boolean putIsSiteExcludedFromPublic( Context context, String siteID )
	{
		if( context == null || siteID == null )
		{
			return false;
		}
		
		boolean excludePublic = false;
		try
		{
			Site site = siteService.getSite( siteID );
			ResourceProperties rp = site.getProperties();
			try { excludePublic = rp.getBooleanProperty( SITE_PROP_JOIN_SITE_EXCLUDE_PUBLIC_LIST ); }
			catch( Exception ex ) { excludePublic = false; }
		}
		catch( IdUnusedException ex ) { excludePublic = false; }
		context.put( CONTEXT_JOIN_SITE_EXCLUDE_PUBLIC_LIST, excludePublic );
		
		return true;
	}
	
	/**
	 * Put a boolean value into the context which indicates if the current user is already a member of the site in question
	 * 
	 * @param context
	 * 				the parameters being passed to the velocity template
	 * @param siteID
	 * 				the ID of the site in question
	 * @return status (true/false)
	 */
	public static boolean putIsCurrentUserAlreadyMemberInContextForSiteBrowser( Context context, String siteID )
	{
		User currentUser = userDirectoryService.getCurrentUser();
		if( currentUser != null && currentUser.getEid() != null )
		{
			context.put( CONTEXT_JOIN_SITE_ALREADY_MEMBER, siteService.isCurrentUserMemberOfSite( siteID ) );
			return true;
		}
		else
		{
			context.put( CONTEXT_JOIN_SITE_ALREADY_MEMBER, false );
			return false;
		}
	}
	
	/**
	 * Put the current user object into the context for the site browser
	 * 
	 * @param context
	 * 				the parameters being passed to the velocity template
	 * @return status (true/false)
	 */
	public static boolean putCurrentUserInContextForSiteBrowser( Context context )
	{
		User currentUser = userDirectoryService.getCurrentUser();
		if( context == null || currentUser == null )
		{
			return false;
		}
		
		context.put( CONTEXT_JOIN_SITE_CURRENT_USER, currentUser );
		return true;
	}
	
	/**
	 * Put the global toggle value for joining with the site browser into the context
	 * 
	 * @param context
	 * 				the object to dump the settings into
	 * @return status (true/false)
	 */
	public static boolean putIsSiteBrowserJoinEnabledInContext( Context context )
	{
		if( context == null )
		{
			return false;
		}
		
		context.put( CONTEXT_JOIN_SITE_SITE_BROWSER_JOIN_ENABLED, siteService.isGlobalJoinFromSiteBrowserEnabled() );
		return true;
	}
	
	/**
	 * Put a map of site IDs->exclude from public site list setting into the context for the site
	 * browser's list mode.
	 * 
	 * @param context
	 * 				the parameters being passed to the velocity template
	 * @param sites
	 * 				the list of sites to create the map for
	 * @return status (true/false)
	 */
	@SuppressWarnings( "rawtypes" )
	public static boolean putSiteMapInContextForSiteBrowser( Context context, List sites )
	{
		if( context == null || sites == null )
		{
			return false;
		}
		
		// Loop through all the sites to create the map of site IDs->exclude from public setting
		Map<String, Boolean> siteMap = new HashMap<String, Boolean>();
		for( Object obj : sites )
		{
			Site site = (Site) obj;
			ResourceProperties rp = site.getProperties();
			boolean pubExcl = false;
			try { pubExcl = rp.getBooleanProperty( SITE_PROP_JOIN_SITE_EXCLUDE_PUBLIC_LIST ); }
			catch( Exception ex ) { pubExcl = false; }
			siteMap.put( site.getId(), Boolean.valueOf( pubExcl ) );
		}
		
		// Put the site map into the context
		context.put( CONTEXT_JOIN_SITE_SITE_MAP, siteMap );
		return true;
	}
	
	/**
	 * Helper method to determine if joining from the site browser has been enabled globally
	 * 
	 * @return true/false (enabled/disabled)
	 */
	public static boolean isJoinFromSiteBrowserEnabled()
	{
		return siteService.isGlobalJoinFromSiteBrowserEnabled();
	}
	
	/**********************************************************************************************
	 ********************* MembershipAction Methods (Membership tool) *****************************
	 **********************************************************************************************/
	
	/**
	 * Perform the steps needed to join a site. This includes determining if the global switch for
	 * join limited by account type is enabled, as well as if it's enabled for the current site along
	 * with the allowed account types set for the current site. The joiner group is also checked, and
	 * joined if necessary.
	 * 
	 * Update: (Dec 2013 - sfoster9@uwo.ca) these checks are now in kernel's join method, so just call join
	 * 
	 * @param siteID
	 * 				the ID of the site in question
	 * @return status (true/false)
	 * @throws IdUnusedException
	 * 				the ID (of the site) is un-used
	 * @throws PermissionException
	 * 				not enough permissions to join the site
	 * @throws InUseException
	 * 				the site is in an edit state somewhere else
	 */
	public static boolean doJoinForMembership( String siteID ) throws IdUnusedException, PermissionException, InUseException
	{
		// Get the current user
		User currentUser = userDirectoryService.getCurrentUser();
		
		if( siteID == null || siteID.isEmpty() || currentUser == null )
		{
			return false;
		}
		
		// If the user is allowed to join, join.
		if( siteService.isAllowedToJoin( siteID ) )
		{
			// Join the site (without catching, so the try/catch in MembershipAction will catch the appropriate exception
			// and create the corresponding user facing error message)
			siteService.join( siteID );
			log.info( "Successfully added user '" + currentUser.getEid() + "' to site '" + siteID + "'" );
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Filter the given list of sites, taking into account the site's setting for the 'exclude from
	 * public list' joinable site setting, and the master toggle.
	 * 
	 * @param sites
	 * 				the list of sites to filter
	 * @return status (true/false)
	 */
	public static boolean filterSitesListForMembership( List<Site> sites )
	{
		// If the exclude from public list setting is disabled globally, don't do anything to the list (no filter)
		if( !siteService.isGlobalJoinExcludedFromPublicListEnabled() )
		{
			return false;
		}
		
		// Otherwise remove any sites that have the exclude from public list setting enabled
		else
		{
			Iterator<Site> itr = sites.iterator();
			while( itr.hasNext() )
			{
				ResourceProperties rp = itr.next().getProperties();
				boolean excludePublic = false;
				try{ excludePublic = rp.getBooleanProperty( SITE_PROP_JOIN_SITE_EXCLUDE_PUBLIC_LIST ); }
				catch( Exception ex ) { excludePublic = false; }
				if( excludePublic )
				{
					itr.remove();
				}
			}
		}
		
		return true;
	}
	
	/***********************************************************************************************
	 ********************* SiteAction Methods (Site Info tool) *************************************
	 ***********************************************************************************************/
	
	/**
	 * Take the joinable site settings from the given ParameterParser object and dump them into the SiteInfo object
	 * 
	 * @param params
	 * 				the object to get the settings from
	 * @param siteInfo
	 * 				the object to dump the settings into
	 * @return status (true/false)
	 */
	public static boolean updateSiteInfoFromParams( ParameterParser params, SiteInfo siteInfo )
	{
		if( params == null || siteInfo == null )
		{
			return false;
		}
		
		if( siteService.isGlobalJoinGroupEnabled() && params.getString( SITE_PROP_JOIN_SITE_GROUP_ID ) != null )
		{
			siteInfo.joinerGroup = params.getString( SITE_PROP_JOIN_SITE_GROUP_ID );
		}
		
		if( siteService.isGlobalJoinExcludedFromPublicListEnabled() && params.getString( SITE_PROP_JOIN_SITE_EXCLUDE_PUBLIC_LIST ) != null )
		{
			siteInfo.joinExcludePublic = Boolean.valueOf( params.getString( SITE_PROP_JOIN_SITE_EXCLUDE_PUBLIC_LIST ) );
		}
		
		if( siteService.isGlobalJoinLimitByAccountTypeEnabled() && params.getString( SITE_PROP_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE ) != null )
		{
			siteInfo.joinLimitByAccountType = Boolean.valueOf( params.getString( SITE_PROP_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE ) );
		}
		
		if( siteService.isGlobalJoinLimitByAccountTypeEnabled() && params.getString( SITE_PROP_JOIN_SITE_ACCOUNT_TYPES ) != null )
		{
			siteInfo.joinLimitedAccountTypes = params.getString( SITE_PROP_JOIN_SITE_ACCOUNT_TYPES );
		}
		
		return true;
	}
	
	/**
	 * Take the joinable site settings from the given ResourceProperties (site properties) object and dump them into the SiteInfo object
	 * 
	 * @param props
	 * 				the object to get the settings from
	 * @param siteInfo
	 * 				the object to dumpt he settings into
	 * @return status (true/false)
	 */
	public static boolean updateSiteInfoFromSiteProperties( ResourceProperties props, SiteInfo siteInfo )
	{
		if( props == null || siteInfo == null )
		{
			return false;
		}
		
		if( siteService.isGlobalJoinGroupEnabled() && props.getProperty( SITE_PROP_JOIN_SITE_GROUP_ID ) != null )
		{
			siteInfo.joinerGroup = props.getProperty( SITE_PROP_JOIN_SITE_GROUP_ID );
		}
		
		if( siteService.isGlobalJoinExcludedFromPublicListEnabled() && props.getProperty( SITE_PROP_JOIN_SITE_EXCLUDE_PUBLIC_LIST ) != null )
		{
			try { siteInfo.joinExcludePublic = Boolean.valueOf( props.getBooleanProperty( SITE_PROP_JOIN_SITE_EXCLUDE_PUBLIC_LIST ) ); }
			catch( Exception ex ) { siteInfo.joinExcludePublic = false; }
		}
		
		if( siteService.isGlobalJoinLimitByAccountTypeEnabled() && props.getProperty( SITE_PROP_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE ) != null )
		{
			try { siteInfo.joinLimitByAccountType = Boolean.valueOf( props.getBooleanProperty( SITE_PROP_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE ) ); }
			catch( Exception ex ) { siteInfo.joinLimitByAccountType = false; }
		}
		
		if( siteService.isGlobalJoinLimitByAccountTypeEnabled() && props.getProperty( SITE_PROP_JOIN_SITE_ACCOUNT_TYPES ) != null )
		{
			siteInfo.joinLimitedAccountTypes  = props.getProperty( SITE_PROP_JOIN_SITE_ACCOUNT_TYPES );
		}
		
		return true;
	}
	
	/**
	 * On add new site; take the joinable site settings from the given SiteInfo object and dump them into 
	 * the ResourcePropertiesEdit (site properties) object
	 * 
	 * @param siteInfo
	 * 				the object to get the settings from
	 * @param props
	 * 				the object to dump the settings into
	 * @return status (true/false)
	 */
	public static boolean updateSitePropertiesFromSiteInfoOnAddNewSite( SiteInfo siteInfo, ResourcePropertiesEdit props )
	{
		if( props == null || siteInfo == null )
		{
			return false;
		}
		
		if( siteService.isGlobalJoinGroupEnabled() )
		{
			props.addProperty( SITE_PROP_JOIN_SITE_GROUP_ID, siteInfo.joinerGroup );
		}
		
		if( siteService.isGlobalJoinExcludedFromPublicListEnabled() )
		{
			props.addProperty( SITE_PROP_JOIN_SITE_EXCLUDE_PUBLIC_LIST, Boolean.toString( siteInfo.joinExcludePublic ) );
		}
		
		if( siteService.isGlobalJoinLimitByAccountTypeEnabled() )
		{
			props.addProperty( SITE_PROP_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE, Boolean.toString( siteInfo.joinLimitByAccountType ) );
			props.addProperty( SITE_PROP_JOIN_SITE_ACCOUNT_TYPES, siteInfo.joinLimitedAccountTypes );
		}
		
		return true;
	}
	
	/**
	 * On update of site attributes; take the joinable site settings from the state and dump them into the site's properties
	 * 
	 * @param site
	 * 				the site to dump the settings into
	 * @param state
	 * 				the object to get the settings from
	 * @return status (true/false)
	 */
	public static boolean updateSitePropertiesFromStateOnUpdateSiteAttributes( Site site, SessionState state )
	{
		if( site == null || state == null )
		{
			return false;
		}
		
		if( site.isJoinable() )
		{
			return updateSitePropertiesFromStateOnSiteUpdate( site.getPropertiesEdit(), state );
		}
		
		return false;
	}
	
	/**
	 * On save of Modify Access in Site Info; take the joinable site settings from the state and dump them into the site's properties
	 * 
	 * @param props
	 * 				the object to dump the settings into
	 * @param state
	 * 				the object to get the settings from
	 * @return status (true/false)
	 * @throws InvalidJoinableSiteSettingsException when the settings in the state are invalid
	 */
	public static boolean updateSitePropertiesFromStateOnSiteInfoSaveGlobalAccess( ResourcePropertiesEdit props, SessionState state )
	{
		// validate the state, throw an InvalidJoinableSiteSettingsException if the state's settings are invalid
		validateJoinableSiteSettings(state);

		if( props == null || state == null )
		{
			return false;
		}
		
		if( siteService.isGlobalJoinGroupEnabled() )
		{
			props.addProperty( SITE_PROP_JOIN_SITE_GROUP_ID, (String) state.getAttribute( FORM_PREFIX + CONTEXT_JOIN_SITE_GROUP_DROP_DOWN ) );
		}
		
		if( siteService.isGlobalJoinExcludedFromPublicListEnabled() )
		{
			props.addProperty( SITE_PROP_JOIN_SITE_EXCLUDE_PUBLIC_LIST, (String) state.getAttribute( FORM_PREFIX + CONTEXT_JOIN_SITE_EXCLUDE_PUBLIC_LIST_CHECKBOX ) );
		}
		
		if( siteService.isGlobalJoinLimitByAccountTypeEnabled() )
		{
			props.addProperty( SITE_PROP_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE, (String) state.getAttribute( FORM_PREFIX + CONTEXT_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE_CHECKBOX ) );
			aggregateSelectedAccountTypesAndAddToSiteProps( props, state );
		}
		
		return true;
	}
	
	/**
	 * On site update; take joinable site settings from the state and dump them into the site's properties
	 * 
	 * @param props
	 * 				the object to dump the settings into
	 * @param state
	 * 				the object to get the settings from
	 * @return status (true/false)
	 * @throws InvalidJoinableSiteSettingsException when the settings in the state are invalid
	 */
	public static boolean updateSitePropertiesFromStateOnSiteUpdate( ResourcePropertiesEdit props, SessionState state )
	{
		// validate the state, throw an InvalidJoinableSiteSettingsException if the state's settings are invalid
		validateJoinableSiteSettings(state);

		if( props == null || state == null )
		{
			return false;
		}
		
		if( siteService.isGlobalJoinGroupEnabled() )
		{
			props.addProperty( SITE_PROP_JOIN_SITE_GROUP_ID, state.getAttribute( STATE_JOIN_SITE_GROUP_ID ).toString() );
		}
		
		if( siteService.isGlobalJoinExcludedFromPublicListEnabled() )
		{
			props.addProperty( SITE_PROP_JOIN_SITE_EXCLUDE_PUBLIC_LIST, state.getAttribute( STATE_JOIN_SITE_EXCLUDE_PUBLIC_LIST ).toString() );
		}
		
		if( siteService.isGlobalJoinLimitByAccountTypeEnabled() )
		{
			props.addProperty( SITE_PROP_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE, state.getAttribute( STATE_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE ).toString() );
			aggregateSelectedAccountTypesAndAddToSiteProps( props, state );
		}
		
		return true;
	}
	
	/**
	 * On site update; take the joinable site settings from the state and dump them into the SiteInfo object
	 * 
	 * @param state
	 * 				the object to get the settings from
	 * @param siteInfo
	 * 				the object to dump the settings into
	 * @param isSiteJoinable
	 * 				is the site joinable or not (true/fasle)
	 * @return status (true/false)
	 */
	public static boolean updateSiteInfoFromStateOnSiteUpdate( SessionState state, SiteInfo siteInfo, boolean isSiteJoinable )
	{
		if( state == null || siteInfo == null )
		{
			return false;
		}
		
		if( isSiteJoinable )
		{
			if( siteService.isGlobalJoinGroupEnabled() && state.getAttribute( STATE_JOIN_SITE_GROUP_ID ) != null )
			{
				siteInfo.joinerGroup = (String) state.getAttribute( STATE_JOIN_SITE_GROUP_ID );
			}
			else
			{
				siteInfo.joinerGroup = "";
			}
			
			if( siteService.isGlobalJoinExcludedFromPublicListEnabled() && state.getAttribute( STATE_JOIN_SITE_EXCLUDE_PUBLIC_LIST ) != null )
			{
				siteInfo.joinExcludePublic = Boolean.valueOf( state.getAttribute( STATE_JOIN_SITE_EXCLUDE_PUBLIC_LIST ).toString() );
			}
			else
			{
				siteInfo.joinExcludePublic = false;
			}
			
			if( siteService.isGlobalJoinLimitByAccountTypeEnabled() && state.getAttribute( STATE_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE ) != null )
			{
				siteInfo.joinLimitByAccountType = Boolean.valueOf( state.getAttribute( STATE_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE ).toString() );
			}
			else
			{
				siteInfo.joinLimitByAccountType = false;
			}
			
			if( siteService.isGlobalJoinLimitByAccountTypeEnabled() )
			{
				Set<String> selectedAccountTypes = new HashSet<String>();
				for( AllowedJoinableAccount account : siteService.getAllowedJoinableAccounts() )
				{
					if( state.getAttribute( STATE_JOIN_SITE_ACCOUNT_TYPE_PREFIX + account.getType() ) != null )
					{
						if( TRUE_STRING.equalsIgnoreCase( state.getAttribute( STATE_JOIN_SITE_ACCOUNT_TYPE_PREFIX + account.getType() ).toString() ) )
						{
							if( selectedAccountTypes.contains( account.getType() ) )
							{
								continue;
							}
							
							selectedAccountTypes.add( account.getType() );
						}
					}
				}
				
				StringBuilder sb = new StringBuilder();
				String prefix = "";
				for( String accountType : selectedAccountTypes )
				{
					sb.append( prefix ).append( accountType );
					prefix = CSV_DELIMITER;
				}
				
				siteInfo.joinLimitedAccountTypes = sb.toString();
			}
			else
			{
				siteInfo.joinLimitedAccountTypes = "";
			}
		}
		else
		{
			siteInfo.joinerGroup = null;
		}
		
		return true;
	}
	
	/**
	 * Get all the form inputs for the joinable site settings (get the from the ParametersParser and put them into the state)
	 * 
	 * @param state
	 * 				the object to dump the settings into
	 * @param params
	 * 				the object to get the settings from
	 * @return status (true/false)
	 */
	public static boolean getAllFormInputs( SessionState state, ParameterParser params )
	{
		if( state == null || params == null )
		{
			return false;
		}
		
		if( siteService.isGlobalJoinGroupEnabled() )
		{
			readInputAndUpdateStateVariable( state, params, CONTEXT_JOIN_SITE_GROUP_DROP_DOWN, STATE_JOIN_SITE_GROUP_ID, false );
		}
		
		if( siteService.isGlobalJoinExcludedFromPublicListEnabled() )
		{
			readInputAndUpdateStateVariable( state, params, CONTEXT_JOIN_SITE_EXCLUDE_PUBLIC_LIST_CHECKBOX, STATE_JOIN_SITE_EXCLUDE_PUBLIC_LIST, true );
		}
		
		if( siteService.isGlobalJoinLimitByAccountTypeEnabled() )
		{
			readInputAndUpdateStateVariable( state, params, CONTEXT_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE_CHECKBOX, STATE_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE, true );
			for( String account : siteService.getAllowedJoinableAccountTypes() )
			{
				readInputAndUpdateStateVariable( state, params, CONTEXT_JOIN_SITE_ACCOUNT_TYPE_CHECKBOX_PREFIX + account, 
						STATE_JOIN_SITE_ACCOUNT_TYPE_PREFIX + account, true );
			}
		}
		
		return true;
	}
	
	/**
	 * On new site creation or Site Info->Edit Access; take the joinable site settings from the site's properties
	 * and dump them into the state
	 * 
	 * @param props
	 * 				the object to get the settings from
	 * @param state
	 * 				the object to dump the settings into
	 * @return status (true/false)
	 */
	public static boolean updateStateFromSitePropertiesOnEditAccessOrNewSite( ResourceProperties props, SessionState state )
	{
		if( props == null || state == null )
		{
			return false;
		}
		
		// Get these site properties regardless of if the global toggles are disabled, as we may need them in the state anyways
		// for clarity to the user (the checkboxes will still hold their initial choices, but will be disabled)
		state.setAttribute( STATE_JOIN_SITE_GROUP_ID, props.getProperty( SITE_PROP_JOIN_SITE_GROUP_ID ) );
		
		try { state.setAttribute( STATE_JOIN_SITE_EXCLUDE_PUBLIC_LIST, Boolean.valueOf( props.getBooleanProperty( SITE_PROP_JOIN_SITE_EXCLUDE_PUBLIC_LIST ) ) ); }
		catch( Exception ex ) { state.setAttribute( STATE_JOIN_SITE_EXCLUDE_PUBLIC_LIST, Boolean.FALSE ); }
		
		try { state.setAttribute( STATE_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE, Boolean.valueOf( props.getBooleanProperty( SITE_PROP_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE ) ) ); }
		catch( Exception ex ) { state.setAttribute( STATE_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE, Boolean.FALSE ); }
		
		state.setAttribute( STATE_JOIN_SITE_ACCOUNT_TYPES, props.getProperty( SITE_PROP_JOIN_SITE_ACCOUNT_TYPES ) );
		
		return true;
	}
	
	/**
	 * When a template site is selected; take the joinable site settings from the template site's properties
	 * and dump them into the SiteInfo object
	 * 
	 * @param props
	 * 				the object to get the settings from
	 * @param siteInfo
	 * 				the object to put the settings into
	 * @return status (true/false)
	 */
	public static boolean updateSiteInfoFromSitePropertiesOnSelectTemplate( ResourceProperties props, SiteInfo siteInfo )
	{
		if( props == null || siteInfo == null )
		{
			return false;
		}
		
		try { siteInfo.joinerGroup = props.getProperty( SITE_PROP_JOIN_SITE_GROUP_ID ); }
		catch( Exception ex ) { siteInfo.joinerGroup = SITE_PROP_JOIN_SITE_GROUP_NO_SEL; }
		
		try { siteInfo.joinExcludePublic = props.getBooleanProperty( SITE_PROP_JOIN_SITE_EXCLUDE_PUBLIC_LIST ); }
		catch( Exception ex ) { siteInfo.joinExcludePublic = false; }
		
		try { siteInfo.joinLimitByAccountType = props.getBooleanProperty( SITE_PROP_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE ); }
		catch( Exception ex ) { siteInfo.joinLimitByAccountType = false; }
		
		try { siteInfo.joinLimitedAccountTypes = props.getProperty( SITE_PROP_JOIN_SITE_ACCOUNT_TYPES ); }
		catch( Exception ex ) { siteInfo.joinLimitedAccountTypes = ""; }
		
		return true;
	}
	
	/**
	 * Put the joinable site settings into the context for Site Info->Edit Access when the site is null
	 * 
	 * @param context
	 * 				the parameters being passed to the velocity template
	 * @param siteInfo
	 * 				the object that contains (default) values for the joinable site settings
	 * @param isSiteJoinable
	 * 				true/false (joinable/not joinable)
	 * @return status (true/false)
	 */
	public static boolean addJoinableSiteSettingsToEditAccessContextWhenSiteIsNull( Context context, SiteInfo siteInfo, boolean isSiteJoinable )
	{
		if( context == null || siteInfo == null )
		{
			return false;
		}
		
		context.put( CONTEXT_UI_SERVICE, serverConfigService.getString( SAK_PROP_UI_SERVICE, DEFAULT_UI_SERVICE ) );
		
		if( isSiteJoinable )
		{
			putGlobalEnabledSettingsIntoContext( context );
			updateContextFromSiteInfo( context, siteInfo );
		}
		
		putAllowedJoinableAccountListsIntoContext( context );
		
		return true;
	}

	/**
	 * Put the joinable site settings into the context for Site Info->Edit Access when the site is not null
	 * 
	 * @param context
	 * 				the parameters being passed to the velocity template
	 * @param state
	 * 				the object that contains values for the joinable site settings
	 * @param site
	 * 				the site in question
	 * @param isSiteJoinable
	 * 				if the type of site is in the list of joinable site types
	 * @return status (true/false)
	 */
	public static boolean addJoinableSiteSettingsToEditAccessContextWhenSiteIsNotNull( Context context, SessionState state, Site site, boolean isSiteJoinable )
	{
		if( context == null || state == null )
		{
			return false;
		}
		
		context.put( CONTEXT_UI_SERVICE, serverConfigService.getString( SAK_PROP_UI_SERVICE, DEFAULT_UI_SERVICE ) );
		
		if( isSiteJoinable )
		{
			putGlobalEnabledSettingsIntoContext( context );
			
			// If join group is enabled globally...
			if( siteService.isGlobalJoinGroupEnabled() )
			{
				// And they've previously made a selection for join group, add their selection to the context
				if( state.getAttribute( STATE_JOIN_SITE_GROUP_ID ) != null )
				{
					context.put( CONTEXT_JOIN_SITE_GROUP_ID, state.getAttribute( STATE_JOIN_SITE_GROUP_ID ) );
				}
				
				// Add available site groups to the context
				putSiteGroupsIntoContext( site, context );
			}
			
			// If the join group is disabled globally...
			else
			{
				// And they've preivously made a selction for join group, add their selection to the context, as well as the site groups,
				// and the flag to indicate this feature has been enabled locally but disabled globally
				if( state.getAttribute( STATE_JOIN_SITE_GROUP_ID ) != null &&
						!"".equals( state.getAttribute( STATE_JOIN_SITE_GROUP_ID ).toString() ) &&
						!SITE_PROP_JOIN_SITE_GROUP_NO_SEL.equals( state.getAttribute( STATE_JOIN_SITE_GROUP_ID ).toString() ) )
				{
					context.put( CONTEXT_JOIN_SITE_GROUP_ID, state.getAttribute( STATE_JOIN_SITE_GROUP_ID ) );
					putSiteGroupsIntoContext( site, context );
					context.put( CONTEXT_JOIN_SITE_GROUP_ENABLED_LOCAL_DISABLED_GLOBAL, Boolean.TRUE );
				}
			}
			
			// Repeat the above process for exclude from public
			if( siteService.isGlobalJoinExcludedFromPublicListEnabled() )
			{
				if( state.getAttribute( STATE_JOIN_SITE_EXCLUDE_PUBLIC_LIST ) != null )
				{
					context.put( CONTEXT_JOIN_SITE_EXCLUDE_PUBLIC_LIST, state.getAttribute( STATE_JOIN_SITE_EXCLUDE_PUBLIC_LIST ) );
				}
			}
			else
			{
				if( state.getAttribute( STATE_JOIN_SITE_EXCLUDE_PUBLIC_LIST ) != null &&
						Boolean.valueOf( state.getAttribute( STATE_JOIN_SITE_EXCLUDE_PUBLIC_LIST ).toString() ) == Boolean.TRUE)
				{
					context.put( CONTEXT_JOIN_SITE_EXCLUDE_PUBLIC_LIST, state.getAttribute( STATE_JOIN_SITE_EXCLUDE_PUBLIC_LIST ) );
					context.put( CONTEXT_JOIN_SITE_EXCLUDE_ENABLED_LOCAL_DISABLED_GLOBAL, Boolean.TRUE );
				}
			}
			
			// Repeat the above process for limit by account types
			if( siteService.isGlobalJoinLimitByAccountTypeEnabled() )
			{
				if( state.getAttribute( STATE_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE ) != null )
				{
					context.put( CONTEXT_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE, state.getAttribute( STATE_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE ) );
				}
				if( putAllowedJoinableAccountListsIntoContext( context ) )
				{
					if( state.getAttribute( STATE_JOIN_SITE_ACCOUNT_TYPES ) != null )
					{
						context.put( CONTEXT_JOIN_SITE_LIMIT_ACCOUNT_TYPES, Arrays.asList( 
								state.getAttribute( STATE_JOIN_SITE_ACCOUNT_TYPES ).toString().split( CSV_DELIMITER ) ) );
					}
				}
			}
			else
			{
				if( state.getAttribute( STATE_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE ) != null &&
						Boolean.valueOf( state.getAttribute( STATE_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE ).toString() ) == Boolean.TRUE)
				{
					context.put( CONTEXT_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE, state.getAttribute( STATE_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE ) );
					context.put( CONTEXT_JOIN_SITE_LIMIT_ENABLED_LOCAL_DISABLED_GLOBAL, Boolean.TRUE );
					if( putAllowedJoinableAccountListsIntoContext( context ) )
					{
						if( state.getAttribute( STATE_JOIN_SITE_ACCOUNT_TYPES ) != null )
						{
							context.put( CONTEXT_JOIN_SITE_LIMIT_ACCOUNT_TYPES, Arrays.asList( 
									state.getAttribute( STATE_JOIN_SITE_ACCOUNT_TYPES ).toString().split( CSV_DELIMITER ) ) );
						}
					}
				}
				else
				{
					context.put( CONTEXT_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE, Boolean.FALSE );
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Put the joinable site settings into the context for the new site UI
	 * 
	 * @param context
	 * 				the parameters being passed to the velocity template
	 * @param siteInfo
	 * 				the object containing (default) values for the joinable site settings
	 * @return status (true/false)
	 */
	public static boolean addJoinableSiteSettingsToNewSiteConfirmContext( Context context, SiteInfo siteInfo )
	{
		if( context == null || siteInfo == null )
		{
			return false;
		}
		
		context.put( CONTEXT_UI_SERVICE, serverConfigService.getString( SAK_PROP_UI_SERVICE, DEFAULT_UI_SERVICE ) );
		
		putGlobalEnabledSettingsIntoContext( context );
		updateContextFromSiteInfo( context, siteInfo );
		
		return true;
	}
	
	/**
	 * Removes the joinable site settings from the state
	 * 
	 * @param state
	 * 				the state object to be modified
	 * @return status (true/false)
	 */
	public static boolean removeJoinableSiteSettingsFromState( SessionState state )
	{
		if( state == null )
		{
			return false;
		}
		
		else
		{
			if( siteService.isGlobalJoinGroupEnabled() )
			{
				state.removeAttribute( STATE_JOIN_SITE_GROUP_ID );
			}
			
			if( siteService.isGlobalJoinExcludedFromPublicListEnabled() )
			{
				state.removeAttribute( STATE_JOIN_SITE_EXCLUDE_PUBLIC_LIST );
			}
			
			if( siteService.isGlobalJoinLimitByAccountTypeEnabled() )
			{
				state.removeAttribute( STATE_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE );
				state.removeAttribute( STATE_JOIN_SITE_ACCOUNT_TYPES );
			}
		}
		
		return true;
	}
	
	/**********************************************************************************************
	 ******************************* Private Utility Methods **************************************
	 **********************************************************************************************/
	
	/**
	 * Put all the groups for the given site into the context, excluding official rosters.
	 * 
	 * @param site
	 * 				the site in question
	 * @param context
	 * 				the object to put the list of groups into
	 * @return status (true/false)
	 */
	private static boolean putSiteGroupsIntoContext( Site site, Context context )
	{
		if( site == null || context == null )
		{
			return false;
		}
		
		// Strip out any rosters from the list of groups
		Collection<Group> groups = site.getGroups();
		Iterator<Group> itr = groups.iterator();
		while( itr.hasNext() )
		{
			Group group = itr.next();
			if( group.getProviderGroupId() != null )
			{
				itr.remove();
			}
		}
		
		// Sort the list of groups based on group title
		List<Group> sortedGroupsWithoutRosters = new ArrayList<Group>( groups );
		Collections.sort( sortedGroupsWithoutRosters, new GroupTitleComparator() );
		groups = sortedGroupsWithoutRosters;
		context.put( CONTEXT_JOIN_SITE_GROUPS, groups );
		
		return true;
	}
	
	/**
	 * Put the allowed joinable account types/categories lists into the context. This method
	 * will also determine if the account type properties are valid and put the corresponding
	 * flag into the context, so that if the feature is either:
	 * 			 1) enabled globally
	 * 			 2) disabled globally but enabled locally 
	 * AND (for both 1 and 2) the sakai.properties for the account types are invalid; the 
	 * account type checkboxes will not be produced for the UI
	 * 
	 * @param context
	 * 				the parameters being passed to the velocity template
	 * @return true if account type lists are valid, false otherwise
	 */
	private static boolean putAllowedJoinableAccountListsIntoContext( Context context )
	{
		if( siteService.isGlobalJoinLimitByAccountTypeEnabled() )
		{
			context.put( CONTEXT_JOIN_SITE_ACCOUNT_TYPES, siteService.getAllowedJoinableAccounts() );
			context.put( CONTEXT_JOIN_SITE_ACCOUNT_CATEGORIES, siteService.getAllowedJoinableAccountTypeCategories() );
			context.put( CONTEXT_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE_ENABLED, Boolean.TRUE );
			return true;
		}
		else
		{
			context.put( CONTEXT_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE_ENABLED, Boolean.FALSE );
			return false;
		}
	}
	
	/**
	 * Put the values from the SiteInfo object into the context (if the setting is enabled)
	 * 
	 * @param context
	 * 				the parameters being passed to the velocity template
	 * @param siteInfo
	 * 				the object to get the values from
	 */
	private static void updateContextFromSiteInfo( Context context, SiteInfo siteInfo )
	{
		if( context == null || siteInfo == null )
		{
			return;
		}
		
		if( siteService.isGlobalJoinGroupEnabled() )
		{
			context.put( CONTEXT_JOIN_SITE_GROUP_ID, siteInfo.joinerGroup );
		}
		
		if( siteService.isGlobalJoinExcludedFromPublicListEnabled() )
		{
			context.put( CONTEXT_JOIN_SITE_EXCLUDE_PUBLIC_LIST, Boolean.valueOf( siteInfo.joinExcludePublic ) );
		}
		
		if( siteService.isGlobalJoinLimitByAccountTypeEnabled() )
		{
			context.put( CONTEXT_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE, Boolean.valueOf( siteInfo.joinLimitByAccountType ) );
			context.put( CONTEXT_JOIN_SITE_LIMIT_ACCOUNT_TYPES, Arrays.asList( siteInfo.joinLimitedAccountTypes.split( CSV_DELIMITER ) ) );
		}
	}
	
	/**
	 * Put the master switches (enabled/disabled) for each joinable setting into the context object
	 * 
	 * @param context
	 * 				the parameters being passed to the velocity template
	 */
	private static void putGlobalEnabledSettingsIntoContext( Context context )
	{
		if( context == null )
		{
			return;
		}
		
		context.put( CONTEXT_JOIN_SITE_GROUP_ENABLED, Boolean.valueOf( siteService.isGlobalJoinGroupEnabled() ) );
		context.put( CONTEXT_JOIN_SITE_EXCLUDE_PUBLIC_LIST_ENABLED, Boolean.valueOf( siteService.isGlobalJoinExcludedFromPublicListEnabled() ) );
		context.put( CONTEXT_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE_ENABLED, Boolean.valueOf( siteService.isGlobalJoinLimitByAccountTypeEnabled() ) );
	}
	
	/**
	 * Aggregate the selected allowed joinable account types from the state and add the (comma seperated) list to the site's properties
	 * 
	 * @param props
	 * 				the site's properties to add the list to
	 * @param state
	 * 				the state the contains the selected values
	 */
	private static void aggregateSelectedAccountTypesAndAddToSiteProps( ResourcePropertiesEdit props, SessionState state )
	{
		if( props == null || state == null )
		{
			return;
		}
		
		String attribute 	= "";
		String propertyList = "";
		String prefix 		= "";
		
		// Loop through all the account types
		StringBuilder sb = new StringBuilder();
		List<String> selectedAccountTypes = new ArrayList<String>();
		for( String account : siteService.getAllowedJoinableAccountTypes() )
		{
			// If the account type was selected, add it to the list
			attribute = state.getAttribute( STATE_JOIN_SITE_ACCOUNT_TYPE_PREFIX + account ).toString();
			if( TRUE_STRING.equalsIgnoreCase( attribute ) )
			{
				if( selectedAccountTypes.contains( account ) )
				{
					continue;
				}
				
				selectedAccountTypes.add( account );
			}
		}
		
		// Create the csv string of account types selected
		for( String accountType : selectedAccountTypes )
		{
			sb.append( prefix ).append( accountType );
			prefix = CSV_DELIMITER;
		}
		
		// Add the csv list to the site properties
		propertyList = sb.toString();
		props.addProperty( SITE_PROP_JOIN_SITE_ACCOUNT_TYPES, propertyList );
	}

	/**
	 * Validates the joinable site settings in the state
	 * @param state object containing the joinable site settings to validate
	 * @throws InvalidJoinableSiteSettingsException when the settings in the state are invalid
	 */
	public static void validateJoinableSiteSettings(SessionState state)
	{
		String attribute = "";
		if (siteService.isGlobalJoinLimitByAccountTypeEnabled())
		{
			attribute = state.getAttribute(STATE_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE).toString();
			if (TRUE_STRING.equalsIgnoreCase(attribute))
			{
				boolean accountTypeSelected = false;
				for (String account : siteService.getAllowedJoinableAccountTypes())
				{
					attribute = state.getAttribute(STATE_JOIN_SITE_ACCOUNT_TYPE_PREFIX + account).toString();
					if (TRUE_STRING.equalsIgnoreCase(attribute))
					{
						accountTypeSelected = true;
						break;
					}
				}
				
				if (!accountTypeSelected)
				{
					throw new InvalidJoinableSiteSettingsException("Limit join to specific accounts selected, but no accounts specified", "ediacc.noAccountTypesSelected");
				}
			}
		}
	}
	
	/**
	 * Read in form field values from the ParameterParser and update/remove values from the state
	 * 
	 * @param state
	 * 				the object to be updated with the form field values
	 * @param params
	 * 				the object that contains the form field values
	 * @param paramName
	 * 				the name of the form field value to retrieve
	 * @param stateAttributeName
	 * 				the name of the state attribute to update
	 * @param isBoolean
	 * 				denotes if the form field value is boolean (true/false)
	 */
	private static void readInputAndUpdateStateVariable( SessionState state, ParameterParser params, String paramName, String stateAttributeName, boolean isBoolean )
	{
		if( state == null || params == null || paramName == null || stateAttributeName == null )
		{
			return;
		}
		
		// Get the param value
		String paramValue = StringUtils.trimToNull( params.getString( paramName ) );
		
		// If the state attribute name is one of the joinable site setting's, flip the value from 'on'/'off' to 'true'/'false'
		if( STATE_JOIN_SITE_LIMIT_BY_ACCOUNT_TYPE.equalsIgnoreCase( stateAttributeName ) ||
				STATE_JOIN_SITE_EXCLUDE_PUBLIC_LIST.equalsIgnoreCase( stateAttributeName ) || stateAttributeName.startsWith( STATE_JOIN_SITE_ACCOUNT_TYPE_PREFIX ) )
		{
			if( paramValue != null && paramValue.equalsIgnoreCase( ON_STRING ) )
			{
				paramValue = TRUE_STRING;
			}
			else
			{
				paramValue = FALSE_STRING;
			}
		}
		
		// If the param value is not null, update the value in the state
		if( paramValue != null )
		{
			if( isBoolean )
			{
				state.setAttribute( stateAttributeName, Boolean.valueOf( paramValue ) );
			}
			else
			{
				state.setAttribute( stateAttributeName, paramValue );
			}
		} 
		
		// If the param value is null, and the state attribute name is the joiner group ID, this means that no joiner group was selected,
		// so we need to make the param value that of the 'noSelection' constant
		else if( STATE_JOIN_SITE_GROUP_ID.equalsIgnoreCase( stateAttributeName ) )
		{
			paramValue = SITE_PROP_JOIN_SITE_GROUP_NO_SEL;
		}
		
		// Otherwise, remove the attribute from the state
		else
		{
			state.removeAttribute( stateAttributeName );
		}
	}
	
	/**********************************************************************************************
	 ********************************** Sub-classes ***********************************************
	 **********************************************************************************************/
	
	/**
	 * Comparator class used to compare Group objects based on title
	 */
	public static class GroupTitleComparator implements Comparator<Group>
	{
		@Override
		public int compare( Group group1, Group group2 )
		{
			return group1.getTitle().compareToIgnoreCase( group2.getTitle() );
		}
	}
}
