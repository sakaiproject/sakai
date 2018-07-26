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
package org.sakaiproject.announcement.tool;

import java.util.Properties;
import org.sakaiproject.announcement.tool.AnnouncementActionState.DisplayOptions;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.api.MenuItem;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * Contains all necessary information to build the menu bar for Announcement pages.
 * 
 * @author bjones86
 */
public class MenuBuilder
{
    private static final String ANNOUNCEMENT_ACTION         = "AnnouncementAction";
    private static final String CONTEXT_RESOURCE_LOADER     = "tlang";
    private static final String CONTEXT_TOOL_TITLE          = "toolTitle";
    private static final String TOOL_PROP_ENABLE_REORDER    = "enableReorder";

    // APIs
    private static final ToolManager                TM  = (ToolManager)                 ComponentManager.get( ToolManager.class );
    private static final ServerConfigurationService SCS = (ServerConfigurationService)  ComponentManager.get( ServerConfigurationService.class );

    /**
     * Enumerate the possible choices in the Announcements menu bar.
     */
    public enum ActiveTab
    {
        LIST,
        VIEW,
        ADD,
        EDIT,
        DELETE,
        MERGE,
        REORDER,
        OPTIONS,
        PERMISSIONS
    }

    /**
     *
     * @param portlet
     * @param data
     * @param activeTab
     * @param rl
     * @param context
     * @param menuNewEnabled
     * @param menuMergeEnabled
     * @param menuPermissionsEnabled
     * @param menuOptionsEnabled
     * @param displayOptions
     */
    public static void buildMenuForGeneral( VelocityPortlet portlet, RunData data, ActiveTab activeTab, ResourceLoader rl, Context context, boolean menuNewEnabled,
                                                boolean menuMergeEnabled, boolean menuPermissionsEnabled, boolean menuOptionsEnabled, DisplayOptions displayOptions )
    {
        Menu menu = new MenuImpl( portlet, data, ANNOUNCEMENT_ACTION );

        // List announcements
        menu.add( buildMenuEntry( rl.getString( "java.refresh" ), AnnouncementAction.REFRESH_BUTTON_HANDLER, ActiveTab.LIST.equals( activeTab ), true ) );

        // Add announcement
        if( menuNewEnabled )
        {
            menu.add( buildMenuEntry( rl.getString( "gen.new" ), "doNewannouncement", ActiveTab.ADD.equals( activeTab ), menuNewEnabled ) );
        }

        // Edit announcement only displayed if it's the active tab
        if( ActiveTab.EDIT.equals( activeTab ) )
        {
            menu.add( buildMenuEntry( rl.getString("gen.revise"), "doReviseannouncementfrommenu", true, true ) );
        }

        // Delete announcement only displayed if it's the active tab
        if( ActiveTab.DELETE.equals( activeTab ) )
        {
            menu.add( buildMenuEntry( rl.getString("gen.delete2"), "doDeleteannouncement", true, true ) );
        }

        // Merge announcements
        if( menuMergeEnabled )
        {
            menu.add( buildMenuEntry( rl.getString( "java.merge" ), AnnouncementAction.MERGE_BUTTON_HANDLER, ActiveTab.MERGE.equals( activeTab ), menuMergeEnabled ) );
        }

        // Reorder announcements
        boolean sakaiReorderProp = SCS.getBoolean( AnnouncementAction.SAK_PROP_ANNC_REORDER, AnnouncementAction.SAK_PROP_ANNC_REORDER_DEFAULT );
        Properties props = TM.getCurrentPlacement().getPlacementConfig();
        if( ((props.containsKey( TOOL_PROP_ENABLE_REORDER ) && props.getProperty( TOOL_PROP_ENABLE_REORDER ).equalsIgnoreCase( "true" )) && menuNewEnabled && !ActiveTab.VIEW.equals( activeTab ))
                || ((!props.containsKey( TOOL_PROP_ENABLE_REORDER ) || !props.getProperty( TOOL_PROP_ENABLE_REORDER ).equalsIgnoreCase( "false" ))
                    && menuNewEnabled && !ActiveTab.VIEW.equals( activeTab ) && sakaiReorderProp) )
        {
            menu.add( buildMenuEntry( rl.getString( "java.reorder" ), AnnouncementAction.REORDER_BUTTON_HANDLER, ActiveTab.REORDER.equals( activeTab ), true ) );
        }

        // Options
        if( menuOptionsEnabled )
        {
            menu.add( buildMenuEntry( rl.getString( "custom.options" ), "doOptions", ActiveTab.OPTIONS.equals( activeTab ), menuOptionsEnabled ) );
        }

        // Permissions
        if( displayOptions != null && !displayOptions.isShowOnlyOptionsButton() && menuPermissionsEnabled )
        {
            menu.add( buildMenuEntry( rl.getString( "java.permissions" ), AnnouncementAction.PERMISSIONS_BUTTON_HANDLER, ActiveTab.PERMISSIONS.equals( activeTab ), menuPermissionsEnabled ) );
        }

        addMenuToState( menu, ((JetspeedRunData) data).getPortletSessionState( portlet.getID() ) );
        addMenuToContext( menu, context );
        addRelatedItemsToContext( menu, context, displayOptions != null && !displayOptions.isShowOnlyOptionsButton(), rl );
    }

    /**
     *
     * @param portlet
     * @param data
     * @param displayOptions
     * @param activeTab
     * @param rl
     * @param context
     * @param menuNewEnabled
     * @param menuEditEnabled
     * @param menuDeleteEnabled
     */
    public static void buildMenuForMetaDataView( VelocityPortlet portlet, RunData data, DisplayOptions displayOptions, ActiveTab activeTab, ResourceLoader rl, Context context,
                                                    boolean menuNewEnabled, boolean menuEditEnabled, boolean menuDeleteEnabled )
    {
        Menu menu = new MenuImpl( portlet, data, ANNOUNCEMENT_ACTION );

        boolean buttonRequiringCheckboxesPresent = false;
        if( displayOptions != null && !displayOptions.isShowOnlyOptionsButton() )
        {
            buttonRequiringCheckboxesPresent = true;
            if( ActiveTab.VIEW.equals( activeTab ) )
            {
                menu.add( buildMenuEntry( rl.getString("gen.new"), "doNewannouncement", false, menuNewEnabled ) );
                menu.add( buildMenuEntry( rl.getString("gen.revise"), "doReviseannouncementfrommenu", ActiveTab.EDIT.equals( activeTab ), menuEditEnabled ) );
                menu.add( buildMenuEntry( rl.getString("gen.delete2"), "doDeleteannouncement", false, menuDeleteEnabled ) );
            }
        }

        addMenuToState( menu, ((JetspeedRunData) data).getPortletSessionState( portlet.getID() ) );
        addMenuToContext( menu, context );
        addRelatedItemsToContext( menu, context, buttonRequiringCheckboxesPresent, rl );
    }

    /**
     * Utility method to build a {@link MenuItem} for the given values
     *
     * @param title the title of the {@link MenuItem} (the text that appears in the UI for the tab)
     * @param action the action of the {@link MenuItem} (the method in SiteAction this tab calls when clicked)
     * @param isCurrent true if this {@link MenuItem} is the currently selected tab; false otherwise
     * @param isEnabled true if this {@link MenuItem} is to be enabled; false otherwise
     * @return the built {@link MenuItem}
     */
    private static MenuEntry buildMenuEntry( String title, String action, boolean isCurrent, boolean isEnabled )
    {
        MenuEntry entry = new MenuEntry( title, isEnabled, action );
        entry.setIsCurrent( isCurrent );
        return entry;
    }

    /**
     *
     * @param menu
     * @param state
     */
    private static void addMenuToState( Menu menu, SessionState state )
    {
        state.setAttribute( MenuItem.STATE_MENU, menu );
    }

    /**
     * Utility method to add the menu to the context under the parameter name "menu", only if the menu contains items.
     *
     * @param menu the {@link Menu} to add to the context if not empty
     * @param context the {@link Context} to add the menu to
     */
    private static void addMenuToContext( Menu menu, Context context )
    {
        if( !menu.isEmpty() )
        {
            context.put( Menu.CONTEXT_MENU, menu );
        }
    }

    /**
     *
     * @param menu
     * @param context
     * @param buttonRequiringCheckboxesPresent
     * @param rl
     */
    private static void addRelatedItemsToContext(Menu menu, Context context, boolean buttonRequiringCheckboxesPresent, ResourceLoader rl)
    {
        boolean enabledItemExists = false;
        for( MenuItem item : menu.getItems() )
        {
            if( item.getIsEnabled() )
            {
                enabledItemExists = true;
                break;
            }
        }

        context.put( AnnouncementAction.ENABLED_MENU_ITEM_EXISTS, enabledItemExists );
        context.put( AnnouncementAction.CONTEXT_ENABLE_ITEM_CHECKBOXES, enabledItemExists && buttonRequiringCheckboxesPresent );
        context.put( AnnouncementAction.CONTEXT_ENABLED_MENU_ITEM_EXISTS, enabledItemExists );
        context.put( Menu.CONTEXT_ACTION, ANNOUNCEMENT_ACTION );
        context.put( CONTEXT_RESOURCE_LOADER, rl );
        context.put( CONTEXT_TOOL_TITLE, TM.getCurrentPlacement().getTitle() );
    }
}
