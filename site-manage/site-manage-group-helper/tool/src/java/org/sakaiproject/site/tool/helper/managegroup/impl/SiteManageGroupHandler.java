package org.sakaiproject.site.tool.helper.managegroup.impl;

import java.util.Comparator;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService;
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
public class SiteManageGroupHandler {
    public Site site = null;
    public SiteService siteService = null;
    public ToolManager toolManager = null;
    public SessionManager sessionManager = null;
    public ServerConfigurationService serverConfigurationService;
    private Map groups = null;
    public String[] selectedTools = new String[] {};
    private Set unhideables = null;
    public String state = null;
    public String title = "";
    public String test = null;
    public boolean update = false;
    public boolean done = false;
    
    //Just something dumb to bind to in order to supress warning messages
    public String nil = null;
    
    private final String TOOL_CFG_FUNCTIONS = "functions.require";
    private final String TOOL_CFG_MULTI = "allowMultiple";
    private final String SITE_UPD = "site.upd";
    private final String HELPER_ID = "sakai.tool.helper.id";
    private final String UNHIDEABLES_CFG = "poh.unhideables";
    private final String GROUP_ADD = "group.add";
    private final String GROUP_DELETE = "group.delete";
    private final String GROUP_RENAME = "group.rename";
    private final String GROUP_SHOW = "group.show";
    private final String GROUP_HIDE = "group.hide";
    private final String SITE_REORDER = "group.reorder";
    private final String SITE_RESET = "group.reset";

    //System config for which tools can be added to a site more then once
    private final String MULTI_TOOLS = "sakai.site.multiPlacementTools";

    // Tool session attribute name used to schedule a whole page refresh.
    public static final String ATTR_TOP_REFRESH = "sakai.vppa.top.refresh"; 

    private String[] defaultMultiTools = {"sakai.news", "sakai.iframe"};
    
	private static final String GROUP_PROP_WSETUP_CREATED = "group_prop_wsetup_created";

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
     * Gets the groups for the current site
     * @return Map of groups (id, group)
     */
    public Map getGroups() {
        if (site == null) {
            init();
        }
        if (update) {
            groups = new LinkedHashMap();
            if (site != null)
            {   
                // only show groups created by WSetup tool itself
    			Collection allGroups = (Collection) site.getGroups();
    			for (Iterator gIterator = allGroups.iterator(); gIterator.hasNext();) {
    				Group gNext = (Group) gIterator.next();
    				String gProp = gNext.getProperties().getProperty(
    						GROUP_PROP_WSETUP_CREATED);
    				if (gProp != null && gProp.equals(Boolean.TRUE.toString())) {
    					groups.put(gNext.getId(), gNext);
    				}
    			}
            }
        }
        return groups;
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
            
            } catch (IdUnusedException e) {
                // The siteId we were given was bogus
                e.printStackTrace();
            }
        }
        update = siteService.allowUpdateSite(site.getId());
        title = site.getTitle();
        
        String conf = serverConfigurationService.getString(UNHIDEABLES_CFG);
        if (conf != null) {
            unhideables = new HashSet();
            String[] toolIds = conf.split(",");
            for (int i = 0; i < toolIds.length; i++) {
                unhideables.add(toolIds[i].trim());
            }
        }
    }
    
    /**
     * Wrapper around siteService to save a site
     * @param site
     * @throws IdUnusedException
     * @throws PermissionException
     */
    public void saveSite(Site site) throws IdUnusedException, PermissionException {
        siteService.save(site);
    }
    
    /**
     * Gets the list of tools that can be added to the current site
     * @return List of Tools
     */
    public List getAvailableTools() {

        List tools = new Vector();

        if (site == null) {
            init();
        }
        
        Set categories = new HashSet();

        if (site.getType() == null || siteService.isUserSite(site.getId())) {
            categories.add("myworkspace");
        }
        else {
            categories.add(site.getType());
        }
        
        Set toolRegistrations = toolManager.findTools(categories, null);
        
        Vector multiPlacementToolIds = new Vector();

        String items[];
        if (serverConfigurationService.getString(MULTI_TOOLS) != null && 
                !"".equals(serverConfigurationService.getString(MULTI_TOOLS)))
            items = serverConfigurationService.getString(MULTI_TOOLS).split(",");
        else
            items = defaultMultiTools;

        for (int i = 0; i < items.length; i++) {
            multiPlacementToolIds.add(items[i]);
        }
     
        List currentTools = new Vector();
        
        SortedIterator i = new SortedIterator(toolRegistrations.iterator(), new ToolComparator());
        for (; i.hasNext();)
        {
            Tool tr = (Tool) i.next();
            Properties config = tr.getRegisteredConfig();
            String allowMultiple = config.getProperty(TOOL_CFG_MULTI);

            if (tr != null) {
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
     * Process the 'Save' post on page ordering.
     *
     */
    public String saveGroups () {
        if (state != null) {
          /*  String[] pages = state.split(" ");
            for (int i = 0; i < pages.length; i++) {
                if (pages[i] != null) {
                    SiteGroup realGroup = site.getGroup(pages[i]);
                    realGroup.setPosition(i);
                }
            }
            site.setCustomGroupOrdered(true);
            try {
                siteService.save(site);
                EventTrackingService.post(
                    EventTrackingService.newEvent(SITE_REORDER, "/site/" + site.getId(), false));

            } 
            catch (IdUnusedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
            catch (PermissionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }*/
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
    
    /**
     * Cancel out of the current action and go back to main view
     * 
     */
    public String back() {
      return "back";
    }
    
    public String reset() {
        try {
            siteService.save(site);
            EventTrackingService.post(
                EventTrackingService.newEvent(SITE_RESET, "/site/" + site.getId(), false));

        } 
        catch (IdUnusedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

        List requiredTools = null;
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
     * Checks to see if a given tool is allowed to be hidden.
     *
     * Useful for tools that have other requried permissions where setting the page
     * as visible may not make it visible to all users and thus causes some confusion.
     *
     * @return true if this tool is allowed to be hidden
     */
    public boolean allowsHide(String toolId) {
        if (unhideables == null || !unhideables.contains(toolId))
            return true;
        return false;
    }
    
    /**
     * Adds a new group to the current site
     * @param toolId
     * @param title
     * @return the newly added Group
     */
    public Group addGroup (String toolId, String title) {
        Group group = null;
        /*try {
            group= site.addGroup();
            group.setTitle(title);
            ToolConfiguration placement = group.addTool(toolId);        
            siteService.save(site);
            EventTrackingService.post(
                EventTrackingService.newEvent(GROUP_ADD, "/site/" + site.getId() +
                    "/page/" + group.getId() +
                    "/tool/" + toolId +
                    "/placement/" + placement.getId(), false));
        } 
        catch (IdUnusedException e) {
            e.printStackTrace();
            return null;
        } 
        catch (PermissionException e) {
            e.printStackTrace();
            return null;
        }*/
        init();
        
        return group;
    }
    
    /**
     * Removes a group from the site
     * 
     * @param groupId
     * @return title of page removed
     * @throws IdUnusedException
     * @throws PermissionException
     */
    public String removeGroup(String groupId)
                            throws IdUnusedException, PermissionException {
        Group group = site.getGroup(groupId);
        site.removeGroup(group);
        saveSite(site);

        EventTrackingService.post(
            EventTrackingService.newEvent(GROUP_DELETE, "/site/" + site.getId() +
                                          "/group/" + group.getId(), false));
        
        return group.getTitle();
    }
    
    
    /**
     * ** Copied from SiteAction.java.. should be in a common place such as util?
     * 
     * @author joshuaryan
     *
     */
    private class ToolComparator
    implements Comparator
    {    
        /**
        * implementing the Comparator compare function
        * @param o1 The first object
        * @param o2 The second object
        * @return The compare result. 1 is o1 < o2; 0 is o1.equals(o2); -1 otherwise
        */
        public int compare ( Object o1, Object o2)
        {
            try
            {
                return ((Tool) o1).getTitle().compareTo(((Tool) o2).getTitle());
            }
            catch (Exception e)
            {
            }
            return -1;
            
        }    // compare
        
    } //ToolComparator    
}

