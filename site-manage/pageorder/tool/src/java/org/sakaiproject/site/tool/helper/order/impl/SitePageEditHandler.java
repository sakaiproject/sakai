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
package org.sakaiproject.site.tool.helper.order.impl;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import uk.org.ponder.util.UniversalRuntimeException;

import org.sakaiproject.authz.api.*;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.*;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.util.Web;

/**
 * 
 * @author Joshua Ryan joshua.ryan@asu.edu
 *
 */
@Slf4j
public class SitePageEditHandler {
    public Site site;
    public SiteService siteService;
    public ToolManager toolManager;
    public SessionManager sessionManager;
    public ServerConfigurationService serverConfigurationService;
    public ContentHostingService contentHostingService;
    public AuthzGroupService authzGroupService;
    private Map<String, SitePage> pages;
    public String[] selectedTools = new String[] {};
    private Set<String> unhideables;
    private Set<String> uneditables;
    public String state;
    public String title = "";
    public boolean update;
    //This nil is needed for RSF Producers do not remove!
    public String nil = null;
    
    private final String TOOL_CFG_FUNCTIONS = "functions.require";
    private final String PORTAL_VISIBLE = "sakai-portal:visible";
    private final String TOOL_CFG_MULTI = "allowMultiple";
    private final String SITE_UPD = "site.upd";
    private final String SITE_VISIT = "site.visit";
    private final String HELPER_ID = "sakai.tool.helper.id";
    private final String UNHIDEABLES_CFG = "poh.unhideables";
    /**
     * Configuration: Tool IDs that page order help shouldn't allow to be hidden
     */
    private final String UNEDITABLES_CFG = "poh.uneditables";
    /**
     * Configuration: Should the page order helper allow pages to be disabled?
     */
    public final String DISABLE_ENABLED_CFG = "poh.allow.lock";

    /**
     * Configuration: Should the page order helper allow pages to be hidden?
     */
    public final String HIDDEN_ENABLED_CFG = "poh.allow.hide";
    private final String PAGE_ADD = "pageorder.add";
    private final String PAGE_DELETE = "pageorder.delete";
    private final String PAGE_RENAME = "pageorder.rename";
    private final String PAGE_SHOW = "pageorder.show";
    private final String PAGE_HIDE = "pageorder.hide";
    private final String PAGE_ENABLE = "pageorder.enable";
    private final String PAGE_DISABLE = "pageorder.disable";
    private final String SITE_REORDER = "pageorder.reorder";
    private final String SITE_RESET = "pageorder.reset";

    // Preserve for configuration backward compat
    public String ALLOW_TITLE_EDIT = "org.sakaiproject.site.tool.helper.order.rsf.PageListProducer.allowTitleEdit";

    //System config for which tools can be added to a site more then once
    private final String MULTI_TOOLS = "sakai.site.multiPlacementTools";

    // Tool session attribute name used to schedule a whole page refresh.
    public static final String ATTR_TOP_REFRESH = "sakai.vppa.top.refresh"; 

    private String[] defaultMultiTools = {"sakai.news", "sakai.iframe"};

    /**
     * Gets the current tool
     * @return Tool
     */
    public Tool getCurrentTool() {
        return toolManager.getCurrentTool();
    }

    /**
     * Generates the currentPlacementId as used by the portal to name the iframe the tool lives in
     * @return String currentPlacementId
     */
    public String getCurrentPlacementId() {
        return Web.escapeJavascript("Main" + toolManager.getCurrentPlacement().getId());
    }
    
    /**
     * Gets the pages for the current site
     * @return Map of pages (id, page)
     */
    public Map<String, SitePage> getPages() {
        if (site == null) {
            init();
        }
        if (update) {
            pages = new LinkedHashMap<>();
            if (site != null)
            {    
                List<SitePage> pageList = site.getOrderedPages();
                for (int i = 0; i < pageList.size(); i++) {
                    
                    SitePage page = pageList.get(i);
                    pages.put(page.getId(), page);
                }
            }
        }
        return pages;
    }
    
    /**
     * Initialization method, just gets the current site in preperation for other calls
     *
     */
    public void init() {
        if (site == null) {
            String siteId = null;
            try {
                siteId = sessionManager.getCurrentToolSession()
                        .getAttribute(HELPER_ID + ".siteId").toString();
            }
            catch (java.lang.NullPointerException npe) {
                // Site ID wasn't set in the helper call!!
            }
            
            if (siteId == null) {
                siteId = toolManager.getCurrentPlacement().getContext();
            }
            
            try {    
                site = siteService.getSite(siteId);
                update = siteService.allowUpdateSite(site.getId());
                title = site.getTitle();
            
            } catch (IdUnusedException e) {
                // The siteId we were given was bogus
                log.error(e.getMessage(), e);
            }
        }
        
        String conf = serverConfigurationService.getString(UNHIDEABLES_CFG);
        if (conf != null) {
            unhideables = new HashSet<>();
            String[] toolIds = conf.split(",");
            for (int i = 0; i < toolIds.length; i++) {
                unhideables.add(toolIds[i].trim());
            }
        }
        String uneditablesConfig = serverConfigurationService.getString(UNEDITABLES_CFG, "");
        uneditables = new HashSet<>();
        for (String tool: uneditablesConfig.split(",")) {
            uneditables.add(tool);
        }
    }
    
    /**
     * Wrapper around siteService to save a site
     * @param site
     * @throws IdUnusedException
     * @throws PermissionException
     */
    public void saveSite(Site site) {
        try {
          siteService.save(site);
        }
        catch (Exception e) {
          throw UniversalRuntimeException.accumulate(e, "Error saving site");
        }
    }
    
    /**
     * Gets the list of tools that can be added to the current site
     * @return List of Tools
     */
    public List<Tool> getAvailableTools() {

        List<Tool> tools = new ArrayList<Tool>();

        if (site == null) {
            init();
        }
        
        Set<String> categories = new HashSet<String>();

        if (site.getType() == null || siteService.isUserSite(site.getId())) {
            categories.add("myworkspace");
        }
        else {
            categories.add(site.getType());
        }
        
        Set<Tool> toolRegistrations = toolManager.findTools(categories, null);
        
        List<String> multiPlacementToolIds = new ArrayList<String>();

        String items[];
        if (serverConfigurationService.getString(MULTI_TOOLS) != null && 
                !"".equals(serverConfigurationService.getString(MULTI_TOOLS)))
            items = serverConfigurationService.getString(MULTI_TOOLS).split(",");
        else
            items = defaultMultiTools;

        for (int i = 0; i < items.length; i++) {
            multiPlacementToolIds.add(items[i]);
        }
     
        SortedIterator i = new SortedIterator(toolRegistrations.iterator(), new ToolComparator());
        for (; i.hasNext();)
        {
        	Tool tr = (Tool) i.next();

        	if (tr != null) {
        		Properties config = tr.getRegisteredConfig();
        		String allowMultiple = config.getProperty(TOOL_CFG_MULTI);
        		if (multiPlacementToolIds.contains(tr.getId()) || "true".equals(allowMultiple)) {
        			tools.add(tr);
        		}
        		else if (site.getToolForCommonId(tr.getId()) == null) {
        			tools.add(tr);
        		}
        	}
        }
        
        return tools;
    }
    
    /**
     * Process tool adds
     * @return
     */
    public String addTools () {    
        for (int i = 0; i < selectedTools.length; i++) {
            SitePage page;
            try {
                page = site.addPage();
                Tool tool = toolManager.getTool(selectedTools[i]);
                page.setTitle(tool.getTitle());
                ToolConfiguration placement = page.addTool(tool.getId());
                siteService.save(site);
                EventTrackingService.post(
                    EventTrackingService.newEvent(PAGE_ADD, "/site/" + site.getId() +
                        "/page/" + page.getId() +
                        "/tool/" + selectedTools[i] +
                        "/placement/" + placement.getId(), false));
            }
            catch(Exception e) {
                throw UniversalRuntimeException.accumulate(e, "Error adding tool " + selectedTools[i]);
                }
        }
      
        return "success";
    }
    
    /**
     * Process the 'Save' post on page ordering.
     *
     */
    public String savePages () {
        if (state != null) {
            String[] pages = state.split(" ");
            for (int i = 0; i < pages.length; i++) {
                if (pages[i] != null) {
                    SitePage realPage = site.getPage(pages[i]);
                    realPage.setPosition(i);
                }
            }
            site.setCustomPageOrdered(true);
            try {
                siteService.save(site);
                EventTrackingService.post(
                    EventTrackingService.newEvent(SITE_REORDER, "/site/" + site.getId(), false));

            } 
            catch (IdUnusedException e) {
                log.error(e.getMessage(), e);
            } 
            catch (PermissionException e) {
                log.error(e.getMessage(), e);
            }
        }

        ToolSession session = sessionManager.getCurrentToolSession();
        session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);

        return "done";
    }
    
    /**
     * Allows the Cancel button to return control to the tool calling this helper
     *
     */
    public String cancel() {
        ToolSession session = sessionManager.getCurrentToolSession();
        session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);

        return "done";
    }
    
    public String reset() {
        site.setCustomPageOrdered(false);
        try {
            siteService.save(site);
            EventTrackingService.post(
                EventTrackingService.newEvent(SITE_RESET, "/site/" + site.getId(), false));

        } 
        catch (IdUnusedException e) {
            log.error(e.getMessage(), e);
        } 
        catch (PermissionException e) {
            log.error(e.getMessage(), e);
        }

        return "";
    }

    /**
     * Checks if a given toolId is required or not for the current site being edited
     *
     * @param toolId
     * @return true if the tool is required
     */
    public boolean isRequired(String toolId) {
        if (site == null) {
            init();
        }

        List<String> requiredTools;
        if (site.getType() == null || siteService.isUserSite(site.getId())) {
            requiredTools = serverConfigurationService.getToolsRequired("myworkspace");
        }
        else {
            requiredTools = serverConfigurationService.getToolsRequired(site.getType());
        }

        if (requiredTools != null && requiredTools.contains(toolId)) {
            return true;
        }
        return false;
    }   

    /**
     * Checks if users can see a page or not
     * 
     * @param page The SitePage whose visibility is in question
     * @return true if users can see the page
     */
    public boolean isVisible(SitePage page) {
        List<ToolConfiguration> tools = page.getTools();
        Iterator<ToolConfiguration> iPt = tools.iterator();

        boolean visible = false;
        while( !visible && iPt.hasNext() ) 
        {
            ToolConfiguration placement = iPt.next();
            Properties roleConfig = placement.getConfig();
            String visibility = roleConfig.getProperty(PORTAL_VISIBLE);

            if ( ! "false".equals(visibility) ) visible = true;
        }
        
        return visible;
    }

    /**
     * Checks if users without site.upd can see a page or not
     * 
     * @param page The SitePage whose visibility is in question
     * @return true if users with out site.upd can see the page
     */
    public boolean isEnabled(SitePage page) {
        Set<Role> roles = getRolesWithout(page.getContainingSite(), SITE_UPD);
        // Should only have non site.upd roles now.
        // Now check to see if these roles have the permission listed in the functions require for the tool.
        List<String> permissions = getRequiredPermissions(page);
        for (String permission : permissions) {
            for (Role role : roles) {
                if (role.isAllowed(permission)) {
                    return true; // If any one of the permissions is allows for any role.
                }
            }
        }
        return false;
    }
    
    /**
     * Checks if the site has been ordered yet
     * 
     * @return true if the site has custom ordering
     */
    public boolean isSiteOrdered() {
        return site.isCustomPageOrdered();
    }

    /**
     * Checks to see if a given tool is allowed to be hidden.
     *
     * Useful for tools that have other requried permissions where setting the page
     * as visible may not make it visible to all users and thus causes some confusion.
     *
     * @return true if this tool is allowed to be hidden
     */
    private boolean allowsHide(String toolId) {
        return (unhideables == null || !unhideables.contains(toolId));
    }

    /**
     * Checks to see if a given SitePage is allowed to be hidden.
     *
     * Useful for pages with tools that have other requried permissions where setting 
     * the page as visible may not make it visible to all users and thus causes some
     * confusion.
     *
     * @return true if this tool is allowed to be hidden
     */
    public boolean allowsHide(SitePage page) {
        if (!(serverConfigurationService.getBoolean(HIDDEN_ENABLED_CFG, true)))
            return false;

        List<ToolConfiguration> tools = page.getTools();
        Iterator<ToolConfiguration> iPt = tools.iterator();

        boolean hideable = true;
        while( hideable && iPt.hasNext() )
        {
            ToolConfiguration placement = iPt.next();

            if (!allowsHide(placement.getToolId())) {
                hideable = false;
            }
        }
        return hideable;
    }

    /**
     * Can the page be disabled? We don't allow disabling if there is more than one tool on the page or
     * if there are no required functions listed in functions.require. We need just one tool so that we don't
     * accidentally disable multiple tools by disabling the home tool.
     *
     * @param page The SitePage that is in question.
     * @return <code>true</code> if the page can be disabled.
     * @see #DISABLE_ENABLED_CFG
     */
    public boolean allowDisable(SitePage page) {

        if (!(serverConfigurationService.getBoolean(DISABLE_ENABLED_CFG, true))) {
            return false;
        }
        List<String> permissions = getRequiredPermissions(page);
        return !(permissions.isEmpty() || permissions.contains(SITE_UPD) || permissions.contains(SITE_VISIT));
    }

    private List<String> getRequiredPermissions(SitePage page) {
        List<ToolConfiguration> tools = page.getTools();
        if (tools.size() == 1) {
            // Only if there is one
            ToolConfiguration toolConfiguration = tools.get(0);
            String functions = toolConfiguration.getConfig().getProperty(TOOL_CFG_FUNCTIONS);
            if (functions != null && functions.length() > 0) {
                return new ArrayList<>(Arrays.asList(StringUtils.split(functions, ',')));
            }
        }
        // Don't use Collections.EMPTY_LIST as it needs to be mutable.
        return new ArrayList<>();
    }
 
    /**
     * Disables a page for any user who doesn't have site.upd
     * 
     * @param pageId The Id of the Page
     * @return true for sucess, false for failuer
     * @throws IdUnusedException, PermissionException
     */
    public boolean disablePage(String pageId) throws SakaiException {
        EventTrackingService.post(
            EventTrackingService.newEvent(PAGE_DISABLE, "/site/" + site.getId() +
                                         "/page/" + pageId, false));
        return  pageVisibilityHelper(pageId, false, false);
    }
    
    /**
     * Enables a page for any user who doesn't have site.upd
     * 
     * @param pageId The Id of the Page
     * @return true for sucess, false for failuer
     * @throws IdUnusedException, PermissionException
     */
    public boolean enablePage(String pageId) throws SakaiException {
        EventTrackingService.post(
            EventTrackingService.newEvent(PAGE_ENABLE, "/site/" + site.getId() +
                                         "/page/" + pageId, false));
      
        return pageVisibilityHelper(pageId, true, true);
    }

    /**
     * Hides a page from any user who doesn't have site.upd
     * Implies enabled
     * 
     * @param pageId The Id of the Page
     * @return true for sucess, false for failuer
     * @throws IdUnusedException, PermissionException
     */
    public boolean hidePage(String pageId) throws SakaiException {
        EventTrackingService.post(
            EventTrackingService.newEvent(PAGE_HIDE, "/site/" + site.getId() +
                                         "/page/" + pageId, false));
        return  pageVisibilityHelper(pageId, false, true);
    }
    
    /**
     * Unhides a page for any user who doesn't have site.upd
     * Implies enabled
     * 
     * @param pageId The Id of the Page
     * @return true for sucess, false for failuer
     * @throws IdUnusedException, PermissionException
     */
    public boolean showPage(String pageId) throws SakaiException {
        EventTrackingService.post(
            EventTrackingService.newEvent(PAGE_SHOW, "/site/" + site.getId() +
                                         "/page/" + pageId, false));
      
        return pageVisibilityHelper(pageId, true, true);
    }
    
    /**
     * Handles the visiibility of a page with a combination of visible/enabled
     * @param pageId The Id of the Page
     * @param visible - Affects the sakai:portal-visible value for tools
     * @param enabled - Affects site.upd in functions.require for tools
     * @return true for sucess, false for failuer
     * @throws IdUnusedException, PermissionException
     */
    private boolean pageVisibilityHelper(String pageId, boolean visible, boolean enabled)
            throws SakaiException {

        if (site == null) {
            init();
        }
        SitePage page = site.getPage(pageId);
        List<ToolConfiguration> tools = page.getTools();
        Iterator<ToolConfiguration> iterator = tools.iterator();

        //If all the tools on a page require site.upd then only users with site.upd will see
        //the page in the site nav of Charon... not sure about the other Sakai portals floating about
        while( iterator.hasNext() ) {
            ToolConfiguration placement = iterator.next();
            final String toolId = placement.getToolId();
            Properties roleConfig = placement.getPlacementConfig();
            String visibility = roleConfig.getProperty(PORTAL_VISIBLE);
            boolean saveChanges = false;
            
            if ( "false".equals(visibility) && visible) {
                visibility = "true";
                saveChanges = true;
            } else if ( ( !"false".equals(visibility) )  && !visible )  {
                visibility = "false";
                saveChanges = true;
            }
            
            if (saveChanges) {
                final boolean specialHidden = getSitePropertySpecialHidden();
                if(specialHidden && "sakai.resources".equals(toolId)) {
                    final String siteCollectionId =  contentHostingService.getSiteCollection(placement.getSiteId());
                    try {
                        if ("true".equals(visibility)) {
                            contentHostingService.removeProperty(siteCollectionId, ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT);
                        } else {
                            contentHostingService.addProperty(siteCollectionId, ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT, "true");
                        }
                    } catch (InUseException | ServerOverloadException e) {
                        // log & do nothing
                        log.warn("Exception occurred when attempting to add / remove property from siteColleciton: '"
                                + siteCollectionId +"' ", e);
                    }

                }
                roleConfig.setProperty(PORTAL_VISIBLE, visibility);

                placement.save();
            }
            
        }
        try {
            AuthzGroup authzGroup =  authzGroupService.getAuthzGroup(site.getReference());
            List<String> permissions = getRequiredPermissions(page);
            if (!(permissions.isEmpty())) {
                // We never change SITE_UPD at all.
                permissions.remove(SITE_UPD);
                permissions.remove(SITE_VISIT);
                Set<Role> roles = getRolesWithout(authzGroup, SITE_UPD);

                for (Role role : roles) {
                    if (enabled) {
                        role.allowFunctions(permissions);
                    } else {
                        role.disallowFunctions(permissions);
                    }
                }
                // Need to save the authz as saving the site doesn't save the authzgroup (when changing permissions)
                authzGroupService.save(authzGroup);
            }
        } catch (GroupNotDefinedException | AuthzPermissionException e) {
            throw new SakaiException(e);
        }

        return true;
    }

    private Set<Role> getRolesWithout(AuthzGroup authzGroup, String function) {
        // Gets the roles
        Set<Role> roles = authzGroup.getRoles();
        roles.removeIf(role -> role.isAllowed(function));
        return roles;
    }

    /**
     * Adds a new single tool page to the current site
     * @param toolId
     * @param title
     * @return the newly added SitePage
     */
    public SitePage addPage (String toolId, String title) {
        SitePage page;
        try {
            page = site.addPage();
            page.setTitle(title);
            ToolConfiguration placement = page.addTool(toolId);        
            siteService.save(site);
            EventTrackingService.post(
                EventTrackingService.newEvent(PAGE_ADD, "/site/" + site.getId() +
                    "/page/" + page.getId() +
                    "/tool/" + toolId +
                    "/placement/" + placement.getId(), false));
        }
        catch (Exception e) {
            throw UniversalRuntimeException.accumulate(e, "Error adding page " + title);
        }
        init();
        
        return page;
    }
    
    /**
     * Removes a page from the site
     * 
     * @param pageId
     * @return title of page removed
     * @throws IdUnusedException
     * @throws PermissionException
     */
    public String removePage(String pageId) {
        SitePage page = site.getPage(pageId);
        site.removePage(page);
        saveSite(site);

        EventTrackingService.post(
            EventTrackingService.newEvent(PAGE_DELETE, "/site/" + site.getId() +
                                          "/page/" + page.getId(), false));
        
        return page.getTitle();
    }
    
    /**
     * Sets the title of a page, and if there is only one tool on a page the title of that tool.
     * Also optionally will alter the configuration of a tool
     * 
     * @param pageId
     * @param newTitle
     * @return the old title of the page
     * @throws IdUnusedException
     * @throws PermissionException
     */
    public String setTitle(String pageId, String newTitle) {
        SitePage page = site.getPage(pageId);
        String oldTitle = page.getTitle();
        page.setTitle(newTitle);
        page.setTitleCustom(true);

        // TODO: Find a way to call each tool to ask what fields they need configured
        // and what methods to use to validate the input..
        if (page.getTools().size() == 1) {
            ToolConfiguration tool = page.getTools().get(0);
            tool.setTitle(newTitle);
        }

        saveSite(site);
        
        EventTrackingService.post(
            EventTrackingService.newEvent(PAGE_RENAME, "/site/" + site.getId() +
                                        "/page/" + page.getId() +
                                        "/old_title/" + oldTitle +
                                        "/new_title/" + page.getTitle(), false));
        
        
        return oldTitle;
    }

    /**
     * Resets page title to the default and resets titleCustom flag
     * 
     * @param pageId The Id of the Page
     * @return reset page title
     * @throws IdUnusedException, PermissionException
     */
    public String resetTitle(String pageId) throws IdUnusedException, PermissionException {
        SitePage page = site.getPage(pageId);
        String oldTitle = page.getTitle();
        page.setTitleCustom(false);
        String newTitle = page.getTitle();
        page.setTitle(newTitle);

        // TODO: Find a way to call each tool to ask what fields they need configured
        // and what methods to use to validate the input..
        if (page.getTools().size() == 1) {
            ToolConfiguration tool = page.getTools().get(0);
            tool.setTitle(newTitle);
        }

        saveSite(site);
        
        EventTrackingService.post(
            EventTrackingService.newEvent(PAGE_RENAME, "/site/" + site.getId() +
                                        "/page/" + page.getId() +
                                        "/old_title/" + oldTitle +
                                        "/new_title/" + page.getTitle(), false));
        
        
        return newTitle;
    }
    
    /**
     * Sets property config/value of page tool, if only one tool on page
     * 
     * @param pageId
     * @param config
     * @param value
     * @return the old title of the page
     * @throws IdUnusedException
     * @throws PermissionException
     */
    public void setConfig(String pageId, String config, String value) {
        SitePage page = site.getPage(pageId);

        // TODO: Find a way to call each tool to ask what fields they need configured
        // and what methods to use to validate the input..
        if (page.getTools().size() == 1 && !"nil".equals(value)) {
            ToolConfiguration tool = page.getTools().get(0);
            tool.getPlacementConfig().setProperty(config, value);
        }

        saveSite(site);
    }
    
    
    /**
     * ** Copied from SiteAction.java.. should be in a common place such as util?
     * 
     * @author joshuaryan
     *
     */
    private class ToolComparator implements Comparator<Tool>
    {    
        /**
        * implementing the Comparator compare function
        * @param o1 The first object
        * @param o2 The second object
        * @return The compare result. 1 is o1 < o2; 0 is o1.equals(o2); -1 otherwise
        */
        public int compare (Tool o1, Tool o2)
        {
            try {
                return o1.getTitle().compareTo(o2.getTitle());
            }
            catch (Exception e)
            {
            }
            return -1;
            
        }    // compare
        
    } //ToolComparator    


    /**
     * Is the current user allowed to edit the title of the page.
     * @param page The page in question.
     * @return <code>true</code> if the page title can be edited.
     */
    public boolean allowEdit(SitePage page) {
        //default value is to allow the Title to be edited.  If the sakai properties
        //specifically requests this to be set to false, then do not allow this function
        boolean allow = serverConfigurationService.getBoolean(ALLOW_TITLE_EDIT, true);
        if (!(uneditables.isEmpty())) {
            for(Iterator<ToolConfiguration> toolIt = page.getTools().iterator(); toolIt.hasNext() && allow;) {
                ToolConfiguration toolConfig = toolIt.next();
                if (uneditables.contains(toolConfig.getToolId())) {
                    allow = false;
                }
            }
        }
        return allow;
    }

    private boolean getSitePropertySpecialHidden() {
        return serverConfigurationService.getBoolean(SiteConstants.SITE_PROPERTY_HIDE_RESOURCES_SPECIAL_HIDDEN,
                SiteConstants.SITE_PROPERTY_HIDE_RESOURCES_SPECIAL_HIDDEN_DEFAULT);
    }
}

