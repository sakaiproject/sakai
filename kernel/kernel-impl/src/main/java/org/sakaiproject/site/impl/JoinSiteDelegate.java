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
package org.sakaiproject.site.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.site.api.AllowedJoinableAccount;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * Contains methods used to join a site and to check for the different join options and restrictions.
 * @author sfoster
 */
@Slf4j
public class JoinSiteDelegate
{
    // apis
    private SiteService             siteService;
    private SecurityService         securityService;
    private UserDirectoryService    userDirectoryService;

    // class members
    private static final String COMMA_DELIMITER					= ",";										// Comma delimiter (for csv parsing)
    private static final String JOINSITE_GROUP_NO_SELECTION		= "noSelection";							// The value of the joiner group site
    private static final String SAK_PERM_SITE_UPD				= "site.upd";								// The name of the site update permission

    // sakai.properties
    private static final String SAK_PROP_JOIN_GROUP_ENABLED 			= "sitemanage.join.joinerGroup.enabled";
    private static final String SAK_PROP_JOIN_LIMIT_ACCOUNT_TYPES 		= "sitemanage.join.limitAccountTypes.enabled";
    private static final String SAK_PROP_JOIN_ACCOUNT_TYPES	 			= "sitemanage.join.allowedJoinableAccountTypes";
    private static final String SAK_PROP_JOIN_ACCOUNT_TYPE_LABELS		= "sitemanage.join.allowedJoinableAccountTypeLabels";
    private static final String SAK_PROP_JOIN_ACCOUNT_TYPE_CATEGORIES	= "sitemanage.join.allowedJoinableAccountTypeCategories";
    private static final String SAK_PROP_JOIN_EXCLUDE_FROM_PUBLIC_LIST 	= "sitemanage.join.excludeFromPublicList.enabled";
    private static final String SAK_PROP_SITE_BROWSER_JOIN_ENABLED		= "sitebrowser.join.enabled";
    
    // site properties
    private static final String SITE_PROP_JOIN_LIMIT_OFFICIAL 	= "joinLimitByAccountType";					// The name (key) of the join limit official site property
    private static final String SITE_PROP_JOIN_ACCOUNT_TYPES 	= "joinLimitedAccountTypes";				// The name (key) of the limit join to account types site property
    private static final String SITE_PROP_JOINSITE_GROUP_ID 	= "joinerGroup";							// The name (key) of the joiner group site property
    
    // global switches
    private static final boolean GLOBAL_JOIN_GROUP_ENABLED					= ServerConfigurationService.getBoolean( SAK_PROP_JOIN_GROUP_ENABLED, Boolean.FALSE );
    private static final boolean GLOBAL_JOIN_EXCLUDE_FROM_PUBLIC_ENABLED	= ServerConfigurationService.getBoolean( SAK_PROP_JOIN_EXCLUDE_FROM_PUBLIC_LIST, Boolean.FALSE );
    private static       boolean GLOBAL_JOIN_LIMIT_BY_ACCOUNT_TYPE_ENABLED	= ServerConfigurationService.getBoolean( SAK_PROP_JOIN_LIMIT_ACCOUNT_TYPES, Boolean.FALSE );
    private static final boolean GLOBAL_SITE_BROWSER_JOIN_ENABLED			= ServerConfigurationService.getBoolean( SAK_PROP_SITE_BROWSER_JOIN_ENABLED, Boolean.FALSE );
    
    // account type lists
    private static List<String> 					allowJoinableAccountTypes 			= new ArrayList<String>();
    private static LinkedHashSet<String> 			allowJoinableAccountTypeCategories	= new LinkedHashSet<String>();
    private static List<AllowedJoinableAccount> 	allowJoinableAccounts				= new ArrayList<AllowedJoinableAccount>();
    private static final List<String> accountTypes			= Arrays.asList(ArrayUtils.nullToEmpty(ServerConfigurationService.getStrings(SAK_PROP_JOIN_ACCOUNT_TYPES)));
    private static final List<String> accountTypeLabels		= Arrays.asList(ArrayUtils.nullToEmpty(ServerConfigurationService.getStrings(SAK_PROP_JOIN_ACCOUNT_TYPE_LABELS)));
    private static final List<String> accountTypeCategories	= Arrays.asList(ArrayUtils.nullToEmpty(ServerConfigurationService.getStrings(SAK_PROP_JOIN_ACCOUNT_TYPE_CATEGORIES)));
    
    /**
     * Default zero-arg constructor; check/verify limit by account type global switch and lists
     * 
     * @author bjones86@uwo.ca
     */
    public JoinSiteDelegate( SiteService siteService, SecurityService securityService, UserDirectoryService userDirectoryService )
    {
    	// Assign the APIs
    	this.siteService 			= siteService;
    	this.securityService 		= securityService;
    	this.userDirectoryService 	= userDirectoryService;
    	
    	// If join limit by account types is enabled globally in sakai.properties, check to make sure the lists are actually valid
    	if( GLOBAL_JOIN_LIMIT_BY_ACCOUNT_TYPE_ENABLED )
    	{
	        // If any of the lists are null, empty or of differing sizes, it is invalid; flip the global switch to disabled
	        if( ( accountTypes == null || accountTypeCategories == null || accountTypeLabels == null ) ||
	        		( accountTypes.isEmpty() || accountTypeCategories.isEmpty() || accountTypeLabels.isEmpty() ) ||
	        		( accountTypeCategories.size() != accountTypes.size() || accountTypeCategories.size() != accountTypeLabels.size() ) )
	        {
	        	GLOBAL_JOIN_LIMIT_BY_ACCOUNT_TYPE_ENABLED = false;
	        	log.warn( "The sakai.properties for allowed joinabel account types are invalid " + 
	                    "(sitmanage.join.allowedJoinableAccountTypeCategories, sitemanage.join.allowedJoinableAccountTypes, " +
	                    "sitemanage.join.allowedJoinableAccountTypeLabels). This option is now disabled globally." );
	        }
    	}
    	
    	// If join limit by account types is still enabled globally, initialize the 'friendly' list
    	if( GLOBAL_JOIN_LIMIT_BY_ACCOUNT_TYPE_ENABLED )
    	{
    		// Initialize the 'friendly' lists
			allowJoinableAccountTypes.addAll( accountTypes );
	        allowJoinableAccountTypeCategories.addAll( accountTypeCategories );
	        for( int i = 0; i < accountTypeCategories.size(); ++i )
            {
	        	allowJoinableAccounts.add( new AllowedJoinableAccount( allowJoinableAccountTypes.get( i ), accountTypeLabels.get( i ), accountTypeCategories.get( i ) ) );
            }
    	}
    }
    
    /**
     * Get the list of allowed account type categories
     * 
     * @author bjones86@uwo.ca
     * @return list of strings (allowed account type categories)
     */
    public LinkedHashSet<String> getAllowedJoinableAccountTypeCategories()
    {
    	return JoinSiteDelegate.allowJoinableAccountTypeCategories;
    }
    
    /**
     * Get the list of (unfriendly) allowed account types
     * 
     * @author bjones86@uwo.ca
     * @return list of strings (allowed account type keys)
     */
    public List<String> getAllowedJoinableAccountTypes()
    {
    	return JoinSiteDelegate.allowJoinableAccountTypes;
    }
    
    /**
     * Get the 'friendly' list of AllowedJoinableAccount objects
     * 
     * @author bjones86@uwo.ca
     * @return
     */
    public List<AllowedJoinableAccount> getAllowedJoinableAccounts()
    {
    	return JoinSiteDelegate.allowJoinableAccounts;
    }
    
    /**
     * Get the global joiner group enabled setting
     * 
     * @author bjones86@uwo.ca
     * @return true/false (enabled/disabled)
     */
    public boolean getGlobalJoinGroupEnabled()
    {
    	return JoinSiteDelegate.GLOBAL_JOIN_GROUP_ENABLED;
    }
    
    /**
     * Get the global join exclude from public list enabled setting
     * 
     * @author bjones86@uwo.ca
     * @return true/false (enabled/disabled)
     */
    public boolean getGlobalJoinExcludeFromPublicListEnabled()
    {
    	return JoinSiteDelegate.GLOBAL_JOIN_EXCLUDE_FROM_PUBLIC_ENABLED;
    }
    
    /**
     * Get the global join limit by account type enabled setting
     * 
     * @author bjones86@uwo.ca
     * @return true/false (enabled/disabled)
     */
    public boolean getGlobalJoinLimitByAccountTypeEnabled()
    {
    	return JoinSiteDelegate.GLOBAL_JOIN_LIMIT_BY_ACCOUNT_TYPE_ENABLED;
    }
    
    /**
     * Get the global site browser join enabled setting
     * 
     * @author bjones86@uwo.ca
     * @return true/false (enabled/disabled)
     */
    public boolean getGlobalSiteBrowserJoinEnabled()
    {
    	return JoinSiteDelegate.GLOBAL_SITE_BROWSER_JOIN_ENABLED;
    }
    
    /** 
     * Check if the system has restricted account types enabled as a joining option. If so,
     * check if the current user has allowed account type to join the worksite.
     * 
     * @param site 
     * 		The worksite to check the current user's ability to join against
     * @return true if either restricted account type join option is disabled at the system level, or if user has one of the site's allowed account type 
     */
    public boolean isAllowedToJoin(Site site)
    {
        // get current user
        User user = userDirectoryService.getCurrentUser();
        if(user == null || site == null)
        {
            return false;
        }
        
        // check first if account types are limited; if not, user is allowed (that is, user isn't being restricted)
    	// otherwise, check if the user's account type is an allowed joinable account type for the site
        return !isLimitByAccountTypeEnabled(site.getId()) || hasAllowedAccountTypeToJoin(user, site);
    }
    
    /**
     * Checks if the system and the provided site allows account types of joining users to be limited
     * 
     * @param siteID 
     * 		The site to check if the join system will limit users to allowed account types
     * @return true if the join site is limiting account types
     */
	public boolean isLimitByAccountTypeEnabled(String siteID)
	{
		if(StringUtils.isBlank(siteID))
		{
			return false;
		}
		
        // if the system allows joinable systems to limit on account type and the account types lists are valid
		if(GLOBAL_JOIN_LIMIT_BY_ACCOUNT_TYPE_ENABLED)
        {
            // get the site's property to determine if the site is restricted by account type
            return getBooleanSiteProperty(siteID, SITE_PROP_JOIN_LIMIT_OFFICIAL);
        }
		else
        {
			return false;
        }
	}
    
    /**
     * Get a site's group id for the group that a joiner will be added to upon joining a site, if enabled
     * 
     * @author sfoster9@uwo.ca, bjones86@uwo.ca
     * @param site 
     * 		Site to get current joiner group from
     * @return joinGroupId string representing the group id of the group that a joining user will be added to upon joining a site; the string "noSelection" represents no joiner group
     */
    public String getJoinGroupId(Site site)
    {
        // set group id to default no selection string
        String joinGroupId = JOINSITE_GROUP_NO_SELECTION;

        if(site != null)
        {
            // get the site's properties
            ResourceProperties rp = site.getProperties();
            
            // get the group id property
            if(rp != null)
            {
            	joinGroupId = rp.getProperty(SITE_PROP_JOINSITE_GROUP_ID);
            }
            
            // If the group was not found or the group id is null or empty (and the joiner group ID is not the 'noSelection' string), 
            // revert the site property to the "noSelection" setting
            if(!JOINSITE_GROUP_NO_SELECTION.equals(joinGroupId) && site.getGroup(joinGroupId) == null)
            {
                ResourcePropertiesEdit rpe = site.getPropertiesEdit();
                rpe.addProperty(SITE_PROP_JOINSITE_GROUP_ID, JOINSITE_GROUP_NO_SELECTION);
                joinGroupId = JOINSITE_GROUP_NO_SELECTION;

                try
                {
                    siteService.save(site);
                }
                catch(Exception e)
                {
                    log.error("Site " + site.getId() + " could not be saved during adding joiner to join group: " + e.getMessage(), e);
                }
            }
        }

        return joinGroupId;
    }
    
    /** 
     * Adds current user to provided site
     * 
     * @param site 
     * 		Site to add current user to
     */
    public void addJoinerToGroup(Site site)
    {
    	// can't add joiner to a site that doesn't exist
        if(site == null)
        {
        	return;
        }
    	
    	// can't add joiner to a group if they're not a member of the site
        if(!siteService.isCurrentUserMemberOfSite(site.getId()))
        {
        	return;
        }
        
        // Create the SecurityAdvisor (elevated permissions needed to use add members)
        SecurityAdvisor yesMan = new SecurityAdvisor()
        {
            @Override
            public SecurityAdvice isAllowed(String userID, String function, String reference)
            {
                // get permission to update the site
                if(SAK_PERM_SITE_UPD.equalsIgnoreCase(function))
                {
                    return SecurityAdvice.ALLOWED;
                }
                else
                {
                    return SecurityAdvice.PASS;
                }
            }
        };

        try
        {
            // Push the security advisor onto the stack
            securityService.pushAdvisor(yesMan);

            // check if the site has the join group property enabled
            if(GLOBAL_JOIN_GROUP_ENABLED);
            {
                //get joiner group id
                String joinerGroupId = getJoinGroupId(site);

                // if the joiner group is found
                if(joinerGroupId != null && !JOINSITE_GROUP_NO_SELECTION.equals(joinerGroupId))
                {
                    try 
                    {
                    	User user = userDirectoryService.getCurrentUser();
                    	if( user == null )
                    	{
                    		throw new UserNotDefinedException( "No user is logged in" );
                    	}
                    	
                    	// try adding the user to the group
                        try {
                            site.getGroup(joinerGroupId).insertMember(user.getId(), site.getJoinerRole(), true, false);
                            siteService.saveGroupMembership(site);
                        } catch (IllegalStateException e) {
                            log.error(".addJoinerToGroup: User with id {} cannot be inserted in group with id {} because the group is locked", user.getId(), site.getGroup(joinerGroupId).getId());
                        }
                    }
                    catch(Exception e)
                    {
                        log.error("Join group not found during site join: " + e.getMessage(), e);
                    }
                }
            }
        }
        finally
        {
            // pop the Security Advisor off no matter what happens
            securityService.popAdvisor(yesMan);
        }
    }    
    
    /**
     * Helper method to get the value of a boolean property
     * 
     * @param siteID 
     * 		The site to retrieve the property from
     * @param propertyName 
     * 		The boolean property name to retrieve
     * @return true if the boolean property is found and is set to true
     */
    private boolean getBooleanSiteProperty(String siteID, String propertyName)
    {
        try 
        {
            // for example, return the site property called 'joinLimitOfficial' (boolean)
            Site site = siteService.getSite(siteID);
            ResourceProperties props = site.getProperties();
            return props.getBooleanProperty(propertyName);
        }
        catch(Exception e)
        {
            log.debug(String.format("Error retrieving property %s from site %s ", propertyName, siteID), e);
        }
        return false;
    }
        
    /**
     * Check if the user's account type is an allowed joinable account type for the site
     * 
     * @param user 
     * 		User to check account type
     * @param site 
     * 		Site to check for account types
     * @return true if user's account type matches one of the site's allowed account types
     */
	private boolean hasAllowedAccountTypeToJoin(User user, Site site)
	{
		if(user == null || site == null)
		{
			return false;
		}
		
		if(!GLOBAL_JOIN_LIMIT_BY_ACCOUNT_TYPE_ENABLED)
		{
			return true;
		}
		
        try 
        { 
            // checks to see if the user's account type is in the allowed joinable account types for this site
            ResourceProperties rp = site.getProperties();
            String csv = rp.getProperty(SITE_PROP_JOIN_ACCOUNT_TYPES);

            if(csv != null)
            {
                List<String> accntTypes = Arrays.asList(csv.split(COMMA_DELIMITER));
                
                // if the current user has an allowed user types to join this site, as chosen by the site maintainer
                return accntTypes.contains(user.getType());
            }
        }
        catch( Exception e ) 
        { 
            log.error( "Error occurred when checking if user has the allowed account type: " + e.getMessage(), e ); 
        }

        return false;
	}
}
