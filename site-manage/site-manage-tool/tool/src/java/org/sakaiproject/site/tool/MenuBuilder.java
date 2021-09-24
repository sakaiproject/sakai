/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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

import java.util.List;

import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.site.util.SiteTypeUtil;
import org.sakaiproject.sitemanage.api.SiteTypeProvider;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * Contains all necessary information to build the menu bar for Site Info pages.
 * Also now implements current tab highlighting.
 * 
 * @author bjones86
 */
public class MenuBuilder
{
    // APIs
    private static final SiteService SS = (SiteService) ComponentManager.get( SiteService.class );
    private static final ToolManager TM = (ToolManager) ComponentManager.get( ToolManager.class );

    // sakai.properties
    private static final String     SAK_PROP_SITE_SETUP_IMPORT_FILE                 = "site.setup.import.file";
    private static final boolean    SAK_PROP_SITE_SETUP_IMPORT_FILE_DEFAULT         = true;

    private static final String     SAK_PROP_DISPLAY_USER_AUDIT_LOG                 = "user_audit_log_display";
    private static final boolean    SAK_PROP_DISPLAY_USER_AUDIT_LOG_DEFAULT         = true;

    private static final String     SAK_PROP_CLEAN_IMPORT_SITE                      = "clean.import.site";
    private static final boolean    SAK_PROP_CLEAN_IMPORT_SITE_DEFAULT              = true;

    private static final String     SAK_PROP_ALLOW_DUPLICATE_SITE                   = "site.setup.allowDuplicateSite";
    private static final boolean    SAK_PROP_ALLOW_DUPLICATE_SITE_DEFAULT           = false;

    private static final String     SAK_PROP_SITE_SETUP_ALLOW_EDIT_ROSTER           = "site.setup.allow.editRoster";
    private static final boolean    SAK_PROP_SITE_SETUP_ALLOW_EDIT_ROSTER_DEFAULT   = true;

    /**
     * Enumerate the possible choices in the Site Info menu bar.
     */
    public enum SiteInfoActiveTab
    {
        SITE_INFO,
        EDIT_SITE_INFO,
        MANAGE_TOOLS,
        TOOL_ORDER,
        ADD_PARTICIPANTS,
        EDIT_CLASS_ROSTERS,
        MANAGE_PARTICIPANTS,
        MANAGE_GROUPS,
        LINK_TO_PARENT_SITE,
        EXTERNAL_TOOLS,
        MANAGE_ACCESS,
        DUPLICATE_SITE,
        IMPORT_FROM_SITE,
        IMPORT_FROM_ARCHIVE,
        USER_AUDIT_LOG
    }

    /**
     * Enumerate the possible choices in the Membership menu bar
     */
    public enum MembershipActiveTab
    {
        CURRENT_SITES,
        OFFICIAL_ENROLMENTS,
        JOINABLE_SITES
    }

    /**
     * Build the menu items for Membership.
     * If the menu contains entries at the end of the routine, it will be added to the context under the parameter named "menu".
     * 
     * @param portlet
     * @param data
     * @param state
     * @param context
     * @param rl
     * @param activeTab
     */
    public static void buildMenuForMembership( VelocityPortlet portlet, RunData data, SessionState state, Context context, ResourceLoader rl, MembershipActiveTab activeTab )
    {
        Menu menu = new MenuImpl( portlet, data, (String) state.getAttribute( SiteAction.STATE_ACTION ) );

        // Current sites
        menu.add( buildMenuEntry( rl.getString( "mb.cursit" ), "doGoto_unjoinable", activeTab.equals( MembershipActiveTab.CURRENT_SITES ) ) );

        // Official course enrolments
        menu.add( buildMenuEntry( rl.getString( "mb.enrolments"), "doGoto_enrolments", activeTab.equals( MembershipActiveTab.OFFICIAL_ENROLMENTS ) ) );

        // Joinable sites
        menu.add( buildMenuEntry( rl.getString( "mb.joisit" ), "doGoto_joinable", activeTab.equals( MembershipActiveTab.JOINABLE_SITES ) ) );

        // Add the menu to the context if it's not empty
        addMenuToContext( menu, context );
    }

    /**
     * Build the menu items for Site Browser.
     * No active tab support here, as there is only ever one tab which is never active ("Search").
     * 
     * @param portlet
     * @param data
     * @param state
     * @param context
     * @param rl
     * @return
     */
    public static Menu buildMenuForSiteBrowser( VelocityPortlet portlet, RunData data, SessionState state, Context context, ResourceLoader rl )
    {
        Menu menu = new MenuImpl( portlet, data, (String) state.getAttribute( SiteAction.STATE_ACTION ) );

        // Search
        menu.add( buildMenuEntry( rl.getString( "list.search" ), "doShow_simple_search", false) );

        return menu;
    }

    /**
     * Build the menu items for Worksite Setup, taking into account permissions.
     * No active tab support here, as there is only ever one tab which is never active ("New").
     * If the menu contains entries at the end of the routine, it will be added to the context under the parameter named "menu".
     * 
     * @param portlet
     * @param data
     * @param state
     * @param context
     * @param rl
     */
    public static void buildMenuForWorksiteSetup( VelocityPortlet portlet, RunData data, SessionState state, Context context, ResourceLoader rl )
    {
        Menu menu = new MenuImpl( portlet, data, (String) state.getAttribute( SiteAction.STATE_ACTION ) );

        // Site List
        menu.add( buildMenuEntry( rl.getString( "java.siteList" ), "", true ) );

        // SAK-22438 if user can add one of these site types then they can see the link to add a new site
        if( SS.allowAddCourseSite() || SS.allowAddPortfolioSite() || SS.allowAddProjectSite() )
        {
            menu.add( buildMenuEntry( rl.getString( "java.new" ), "doNew_site", false ) );
        }

        // Add the menu to the context if it's not empty
        addMenuToContext( menu, context );
    }

    /**
     * Build the menu items for the given SiteInfoActiveTab, taking into account sakai.properties and permissions.
     * If the menu contains entries at the end of the routine, it will be added to the context under the parameter named "menu".
     * 
     * @param portlet
     * @param data
     * @param state
     * @param context
     * @param site
     * @param rl
     * @param siteTypeProvider
     * @param activeTab
     */
    public static void buildMenuForSiteInfo( VelocityPortlet portlet, RunData data, SessionState state, Context context, Site site, ResourceLoader rl,
                                                        SiteTypeProvider siteTypeProvider, SiteInfoActiveTab activeTab )
    {
        // Get any necessary info from the site
        ResourceProperties siteProperties = site.getProperties();
        String siteType = site.getType();
        String siteID = site.getId();

        // Permissions checks
        boolean isMyWorkspace = SiteAction.isSiteMyWorkspace( site );
        boolean allowUpdateSite = SS.allowUpdateSite( siteID );
        boolean allowViewRoster = SS.allowViewRoster( siteID );
        boolean allowUpdateSiteMembership = SS.allowUpdateSiteMembership( siteID );
        boolean allowUpdateGroupMembership = SS.allowUpdateGroupMembership( siteID );

        // Build the Menu object
        Menu menu = new MenuImpl( portlet, data, (String) state.getAttribute( SiteAction.STATE_ACTION ) );

        if( !isMyWorkspace )
        {
            // Main landing page, 'Site Information'
            menu.add( buildMenuEntry( rl.getString( "sinfo.other" ), "doMenu_siteInfo", activeTab.equals( SiteInfoActiveTab.SITE_INFO ) ) );
        }

        if( allowUpdateSite )
        {
            if( !isMyWorkspace )
            {
                // 'Edit Site Information'
                menu.add( buildMenuEntry( rl.getString( "java.editsite" ), "doMenu_edit_site_info", activeTab.equals( SiteInfoActiveTab.EDIT_SITE_INFO ) ) );
            }

            // 'Manage Tools'
            menu.add( buildMenuEntry( rl.getString( "java.edittools" ), "doMenu_edit_site_tools", activeTab.equals( SiteInfoActiveTab.MANAGE_TOOLS ) ) );

            // If the page order helper is available, not stealthed and not hidden, show the link
            if( !TM.isStealthed("sakai-site-pageorder-helper" ) )
            {
                // In particular, need to check site types for showing the tool or not
                if( SiteAction.isPageOrderAllowed( siteType, siteProperties.getProperty( SiteConstants.SITE_PROPERTY_OVERRIDE_HIDE_PAGEORDER_SITE_TYPES ) ) )
                {
                    // 'Tool Order'
                    menu.add( buildMenuEntry( rl.getString( "java.orderpages" ), "doPageOrderHelper", activeTab.equals( SiteInfoActiveTab.TOOL_ORDER ) ) );
                }
            }

            menu.add( buildMenuEntry( rl.getString( "java.datemanager" ), "doDateManagerHelper", activeTab.equals( SiteInfoActiveTab.TOOL_ORDER ) ) );
        }

        // If the add participant helper is available, not stealthed and not hidden, show the tab
        if( allowUpdateSiteMembership && !isMyWorkspace && !TM.isStealthed( SiteAction.getAddUserHelper( site ) ) )
        {
            // 'Add Participants'
            menu.add( buildMenuEntry( rl.getString( "java.addp" ), "doParticipantHelper", activeTab.equals( SiteInfoActiveTab.ADD_PARTICIPANTS ) ) );
        }

        if( allowViewRoster && !isMyWorkspace )
        {
            // 'Manage Participants'
            menu.add( buildMenuEntry( rl.getString( "java.manageParticipants" ), "doMenu_siteInfo_manageParticipants", activeTab.equals( SiteInfoActiveTab.MANAGE_PARTICIPANTS ) ) );
        }

        if( allowUpdateSiteMembership && !isMyWorkspace )
        {
            boolean allowEditRosterEnabled = ServerConfigurationService.getBoolean( SAK_PROP_SITE_SETUP_ALLOW_EDIT_ROSTER, SAK_PROP_SITE_SETUP_ALLOW_EDIT_ROSTER_DEFAULT );
            if( allowEditRosterEnabled && siteType != null && SiteTypeUtil.isCourseSite( siteType ) )
            {
                // 'Edit Class Roster(s)'
                menu.add( buildMenuEntry( rl.getString( "java.editc" ), "doMenu_siteInfo_editClass", activeTab.equals( SiteInfoActiveTab.EDIT_CLASS_ROSTERS ) ) );
            }
        }

        if( allowUpdateGroupMembership )
        {
            boolean groupSupportEnabled = ServerConfigurationService.getBoolean( SiteAction.SAK_PROP_SITE_SETUP_GROUP_SUPPORT, SiteAction.SAK_PROP_SITE_SETUP_GROUP_SUPPORT_DEFAULT );
            if( !isMyWorkspace && groupSupportEnabled ) {
                // 'Manage Groups'
                menu.add( buildMenuEntry( rl.getString( "java.group" ), "doManageGroupHelper", activeTab.equals( SiteInfoActiveTab.MANAGE_GROUPS ) ) );
            }
        }

        if( allowUpdateSite && !isMyWorkspace )
        {
            if( !TM.isStealthed( "sakai-site-manage-link-helper" ) )
            {
                // 'Link to Parent Site'
                menu.add( buildMenuEntry( rl.getString( "java.link" ), "doLinkHelper", activeTab.equals( SiteInfoActiveTab.LINK_TO_PARENT_SITE ) ) );
            }

            if( !TM.isStealthed( "sakai.basiclti.admin.helper" ) )
            {
                // 'External Tools'
                menu.add( buildMenuEntry( rl.getString( "java.external" ), "doExternalHelper", activeTab.equals( SiteInfoActiveTab.EXTERNAL_TOOLS ) ) );
            }

            List<String> providedSiteTypes = siteTypeProvider.getTypes();
            boolean isProvidedType = false;
            if( siteType != null && providedSiteTypes.contains( siteType ) )
            {
                isProvidedType = true;
            }

            if( !isProvidedType )
            {
                // 'Manage Access'
                menu.add( buildMenuEntry( rl.getString( "java.siteaccess" ), "doMenu_edit_site_access", activeTab.equals( SiteInfoActiveTab.MANAGE_ACCESS ) ) );

                boolean duplicateSiteEnabled = ServerConfigurationService.getBoolean( SAK_PROP_ALLOW_DUPLICATE_SITE, SAK_PROP_ALLOW_DUPLICATE_SITE_DEFAULT );
                if( SS.allowAddSite( null ) && duplicateSiteEnabled )
                {
                    // 'Duplicate Site'
                    menu.add( buildMenuEntry( rl.getString( "java.duplicate" ), "doMenu_siteInfo_duplicate", activeTab.equals( SiteInfoActiveTab.DUPLICATE_SITE ) ) );
                }

                // Import link should be visible even if only one site
                List<Site> updatableSites = SS.getSites( SelectionType.UPDATE, null, null, null, SortType.TITLE_ASC, null );
                if( updatableSites.size() > 0 )
                {
                    String action = ServerConfigurationService.getBoolean( SAK_PROP_CLEAN_IMPORT_SITE, SAK_PROP_CLEAN_IMPORT_SITE_DEFAULT )
                                    ? "doMenu_siteInfo_importSelection" : "doMenu_siteInfo_import";

                    // 'Import from Site'
                    menu.add( buildMenuEntry( rl.getString( "java.import" ), action, activeTab.equals( SiteInfoActiveTab.IMPORT_FROM_SITE ) ) );

                    boolean importFromFileEnabled = ServerConfigurationService.getBoolean( SAK_PROP_SITE_SETUP_IMPORT_FILE, SAK_PROP_SITE_SETUP_IMPORT_FILE_DEFAULT );
                    if( importFromFileEnabled )
                    {
                        // 'Import from Archive File'
                        menu.add( buildMenuEntry( rl.getString( "java.importFile" ), "doAttachmentsMtrlFrmFile", activeTab.equals( SiteInfoActiveTab.IMPORT_FROM_ARCHIVE ) ) );
                    }
                }
            }

            boolean eventLogEnabled = ServerConfigurationService.getBoolean( SAK_PROP_DISPLAY_USER_AUDIT_LOG, SAK_PROP_DISPLAY_USER_AUDIT_LOG_DEFAULT );
            if( !TM.isStealthed( "sakai.useraudit" ) && eventLogEnabled )
            {
                // 'User Audit Log'
                menu.add( buildMenuEntry( rl.getString( "java.userAuditEventLog" ), "doUserAuditEventLog", activeTab.equals( SiteInfoActiveTab.USER_AUDIT_LOG ) ) );
            }

            if(allowUpdateSite){
                List<SitePage> pages = site.getPages();
                for(SitePage page : pages){
                    if (page.isHomePage()) {
                        //now we know this site has a home page.
                        menu.add(new MenuEntry(rl.getString("manage.overview"),
                                "doManageOverview"));
                        break;
                    }
                }
            }
        }

        // Add the menu to the context if it's not empty
        addMenuToContext( menu, context );
    }

    /**
     * Utility method to build a {@link MenuItem} for the given values
     * 
     * @param title the title of the {@link MenuItem} (the text that appears in the UI for the tab)
     * @param action the action of the {@link MenuItem} (the method in SiteAction this tab calls when clicked)
     * @param isCurrent true if this {@link MenuItem} is the currently selected tab; false otherwise
     * @return the built {@link MenuItem}
     */
    private static MenuEntry buildMenuEntry( String title, String action, boolean isCurrent )
    {
        MenuEntry entry = new MenuEntry( title, action );
        entry.setIsCurrent( isCurrent );
        return entry;
    }

    /**
     * Utility method to add the menu to the context under the parameter name "menu", only if the menu contains items.
     *
     * @param menu the {@link Menu} to add to the context if not empty
     * @param context the {@link Context} to add the menu to
     */
    public static void addMenuToContext( Menu menu, Context context )
    {
        if( !menu.isEmpty() )
        {
            context.put( Menu.CONTEXT_MENU, menu );
        }
    }
}
