/**
 * $Id$
 * $URL$
 * SiteEntityProvider.java - entity-broker - Jun 29, 2008 8:35:55 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

import org.azeckoski.reflectutils.ReflectUtils;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.EntityView.Method;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityParameters;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityURLRedirect;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.DepthLimitable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Redirectable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.providers.model.EntityGroup;
import org.sakaiproject.entitybroker.providers.model.EntitySite;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.util.FormattedText;

/**
 * Creates a provider for dealing with sites
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public class SiteEntityProvider extends AbstractEntityProvider implements CoreEntityProvider,
RESTful, ActionsExecutable, Redirectable, RequestStorable, DepthLimitable {

    private int maxDepth = 7;
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }
    public int getMaxDepth() {
        return maxDepth;
    }

    private SiteService siteService;
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private AuthzGroupService authzGroupService;
    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    private FunctionManager functionManager;
    public void setFunctionManager(FunctionManager functionManager) {
        this.functionManager = functionManager;
    }

    private UserEntityProvider userEntityProvider;
    public void setUserEntityProvider(UserEntityProvider userEntityProvider) {
        this.userEntityProvider = userEntityProvider;
    }
    
    private ServerConfigurationService serverConfigurationService;
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    private SecurityService securityService;
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public static String PREFIX = "site";
    public String getEntityPrefix() {
        return PREFIX;
    }

    private static final String GROUP_PROP_WSETUP_CREATED = "group_prop_wsetup_created";

    /** Property to set the default page size for lists of entities. */
    public static final String PROP_SITE_PROVIDER_PAGESIZE_DEFAULT = "site.entity.pagesize.default";

    /** Property to set the maximum page size for lists of entities. */
    public static final String PROP_SITE_PROVIDER_PAGESIZE_MAXIMUM = "site.entity.pagesize.maximum";

	private static final String PERMISSION_QUERY_PROPERTY_NAME = "permission";
    
    /**
     * The default page size for lists of entities. May be overridden with
     * a property of "site.entity.pagesize.default".
     */
    private int defaultPageSize = 50;
    
    /**
     * The maximum page size for lists of entities. May be overridden with
     * a property of "site.entity.pagesize.maximum".
     */
    private int maxPageSize = 500;

    private static String[] updateableSiteProps;
    
    public void init() {
        int dps = serverConfigurationService.getInt(
                PROP_SITE_PROVIDER_PAGESIZE_DEFAULT, defaultPageSize);
        if (dps > 0) {
            defaultPageSize = dps;
        }

        int mps = serverConfigurationService.getInt(
                PROP_SITE_PROVIDER_PAGESIZE_MAXIMUM, maxPageSize);
        if (mps >= defaultPageSize) {
            maxPageSize = mps;
        } else {
            maxPageSize = defaultPageSize;
        }

        updateableSiteProps = serverConfigurationService.getStrings("site.entity.updateable.props");
        // Set defaults as these are the same values that can be changed through the UI.
        if (updateableSiteProps == null) {
            updateableSiteProps = new String [] {"contact-email", "contact-name"};
        }
    }

    // ACTIONS

    @EntityURLRedirect("/{prefix}/{id}/memberships")
    public String redirectMemberships(Map<String, String> vars) {
        return MembershipEntityProvider.PREFIX + "/site/" + vars.get("id")
        + vars.get(TemplateParseUtil.DOT_EXTENSION);
    }

    @EntityCustomAction(action = "exists", viewKey = EntityView.VIEW_SHOW)
    public boolean checkSiteExists(EntityView view) {
        String siteId = view.getEntityReference().getId();
        boolean exists = entityExists(siteId);
        return exists;
    }

    @EntityCustomAction(action = "role", viewKey = "")
    public void handleRoles(EntityView view) {
        String siteId = view.getEntityReference().getId();
        String roleId = view.getPathSegment(3);
        if (roleId == null) {
            throw new IllegalArgumentException("No role id specified");
        }
        Site site = getSiteById(siteId);
        if (view.getMethod().equals(Method.POST.name())) {
            try {
                site.addRole(roleId);
            } catch (RoleAlreadyDefinedException e) {
                // Ignore
            }
        } else if (view.getMethod().equals(Method.DELETE.name())) {
            site.removeRole(roleId);
        } else {
            throw new IllegalArgumentException("Method " + view.getMethod() + " not supported");
        }
        try {
            siteService.save(site);
        } catch (IdUnusedException e) {
            // Ignore
        } catch (PermissionException e) {
            throw new SecurityException("User not allowed to update role " + roleId + " in site "
                    + siteId);
        }
    }

    @EntityCustomAction(action = "perms", viewKey = EntityView.VIEW_SHOW)
    public Map<String, Set<String>> handlePerms(EntityView view) {
        // expects site/siteId/perms[/:PREFIX:]
        String prefix = view.getPathSegment(3);

        String userId = developerHelperService.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException(
            "This action (perms) is not accessible to anon and there is no current user.");
        }

        String siteId = view.getEntityReference().getId();
        Site site = getSiteById(siteId);
        Set<Role> roles = site.getRoles();
        Map<String, Set<String>> perms = new HashMap<String, Set<String>>();
        for (Role role : roles) {
            Set<String> functions = role.getAllowedFunctions();
            Set<String> filteredFunctions = new TreeSet<String>();
            if (prefix != null) {
                for (String function : functions) {
                    if (function.startsWith(prefix)) {
                        filteredFunctions.add(function);
                    }
                }
            } else {
                filteredFunctions = functions;
            }
            perms.put(role.getId(), filteredFunctions);
        }
        return perms;
    }

    @EntityCustomAction(action="setPerms", viewKey=EntityView.VIEW_EDIT)
    public String handleSetPerms(EntityReference ref, Map<String, Object> params) {
        String userId = developerHelperService.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException(
            "This action (setPerms) is not accessible to anon and there is no current user.");
        }
        String siteId = ref.getId();
        Site site = getSiteById(siteId);
        List<String> userMutableFunctions = functionManager.getRegisteredUserMutableFunctions();
        boolean admin = developerHelperService.isUserAdmin(developerHelperService
                .getCurrentUserReference());

        try {
            AuthzGroup authzGroup = authzGroupService.getAuthzGroup(site.getReference());
            boolean changed = false;
            for (String name : params.keySet()) {
                if (!name.contains(":")) {
                    continue;
                }
                String value = (String) params.get(name);
                String roleId = name.substring(0, name.indexOf(":"));
                Role role = authzGroup.getRole(roleId);
                if (role == null) {
                    throw new IllegalArgumentException("Invalid role id '" + roleId
                            + "' provided in POST parameters.");
                }
                String function = name.substring(name.indexOf(":") + 1);

                // Only change this function if registered as userMutable
                if (admin || userMutableFunctions.contains(function)) {
                    if ("true".equals(value)) {
                        role.allowFunction(function);
                    } else {
                        role.disallowFunction(function);
                    }
                } else {
                    throw new SecurityException("The function " + function
                            + " cannot be updated by the current user.");
                }
                changed = true;
            }

            if (changed) {
                try {
                    authzGroupService.save(authzGroup);
                } catch (AuthzPermissionException ape) {
                    throw new SecurityException("The permissions for this site (" + siteId
                            + ") cannot be updated by the current user.");
                }
            }
        } catch (GroupNotDefinedException gnde) {
            throw new IllegalArgumentException("No realm defined for site (" + siteId + ").");
        }
        return "SUCCESS";
    }

    @EntityCustomAction(action = "group", viewKey = "")
    public EntityGroup handleGroups(EntityView view, Map<String, Object> params) {
        // expects site/siteId/group/groupId
        String siteId = view.getEntityReference().getId();
        String groupId = view.getPathSegment(3);
        EntityGroup eg = null;
        String groupTitle = params.containsKey("groupTitle") ? params.get("groupTitle").toString() : null;
        String groupDescription = params.get("groupDescription") != null ? params.get("groupDescription").toString() : null;
        // fix empty strings that may be specified
        if ("".equals(groupTitle)) {
            groupTitle = null;
        }
        if ("".equals(groupDescription)) {
            groupDescription = null;
        }
        List<String> userIds = params.get("userIds") != null ? Arrays.asList(params.get("userIds")
                .toString().split(",")) : new ArrayList<String>();
        Site site = getSiteById(siteId);

        // check if the user can access site
        isAllowedAccessSite(site);

        // check if the user can update group membership
        if (!siteService.allowUpdateGroupMembership(site.getId())) {
            throw new SecurityException("This group (" + groupId + ") in site (" + siteId
                    + ") cannot be updated by the current user.");
        }

        Group group = null;

        if (EntityView.Method.GET.name().equals(view.getMethod())) {
            // GET /direct/site/siteid/group/groupid
            if (groupId == null) {
                throw new IllegalArgumentException("Invalid path provided: expected to receive the groupId");
            }
            group = site.getGroup(groupId);

            eg = new EntityGroup(group);
            return eg;

        } else if (EntityView.Method.PUT.name().equals(view.getMethod())) {
            // PUT /direct/site/siteid/group - create a new group in the site (returns group id).
            // Params include title, description, optionally initial list of members
            if (groupTitle == null) {
                // No title metadata specified
                throw new IllegalArgumentException("A title needs to be provided for a new group.");
            }
            group = site.addGroup();
            group.getProperties().addProperty(GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
            group.setTitle(groupTitle);
            group.setDescription(groupDescription);
            // add new members
            for (String userId : userIds) {
                // Every user added via this EB is defined as non-provided
                Role role = site.getUserRole(userId);
                Member m = site.getMember(userId);
                if (group.getUserRole(userId) == null && role != null) {
                    try {
                        group.insertMember(userId, role.getId(), m != null ? m.isActive() : true, false);
                    } catch (IllegalStateException e) {
                        throw e;
                    }
                }
            }

            try {
                siteService.save(site);
            } catch (IdUnusedException e) {
                throw new IllegalArgumentException("Cannot find site with given id: " + siteId
                        + ":" + e.getMessage(), e);
            } catch (PermissionException e) {
                throw new SecurityException(
                        "Current user does not have permission to add a group to site:" + siteId);
            }

        } else if (EntityView.Method.POST.name().equals(view.getMethod())) {
            // POST /direct/site/siteid/group/groupid - update metadata for group but not membership
            if (groupTitle == null) {
                // No metadata specified
                throw new IllegalArgumentException("A group title needs to be provided to edit group: " + groupId);
            }
            group = site.getGroup(groupId);
            if (group != null) {
                checkGroupType(group);
                if (groupTitle != null) {
                    group.setTitle(groupTitle);
                }
                //clear description if it is not provided
                if (groupDescription != null) {
                    groupDescription = groupDescription.trim();
                }
                group.setDescription(groupDescription);
            } else {
                throw new IllegalArgumentException("Cannot find a group with given id: " + groupId
                        + " in site:" + siteId);
            }
            try {
                siteService.save(site);
            } catch (IdUnusedException e) {
                throw new IllegalArgumentException("Cannot find site with given id: " + siteId
                        + ":" + e.getMessage(), e);
            } catch (PermissionException e) {
                throw new SecurityException("This group: " + groupId
                        + " cannot be edited by the current user.");
            }

        } else if (EntityView.Method.DELETE.name().equals(view.getMethod())) {
            // DELETE /direct/site/siteid/group - delete an existing group in the site.
            if (groupId == null) {
                throw new IllegalArgumentException(
                "Invalid path provided: expect to receive the groupId");
            }
            group = site.getGroup(groupId);
            checkGroupType(group);
            try {
                site.deleteGroup(group);
            } catch (IllegalStateException e) {
                throw e;
            }
            try {
                siteService.save(site);
            } catch (IdUnusedException e) {
                throw new IllegalArgumentException("Cannot find site with given id: " + siteId
                        + ":" + e.getMessage(), e);
            } catch (PermissionException e) {
                throw new SecurityException("This group: " + groupId
                        + " cannot be deleted by the current user.");
            }
            return null;
        }

        eg = new EntityGroup(group);
        return eg;
    }

    @EntityCustomAction(action = "userPerms", viewKey = EntityView.VIEW_SHOW)
    public Set<String> handleUserPerms(EntityView view) {

        // expects site/siteId/userPerms[/:PREFIX:]
        String prefix = view.getPathSegment(3);

        String userId = developerHelperService.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("This action (userPerms) is not accessible to anon and there is no current user.");
        }

        String siteId = view.getEntityReference().getId();
        String siteReference = siteService.siteReference(siteId);

        List<String> functions = null;
        if (prefix != null) {
            functions = functionManager.getRegisteredFunctions(prefix);
        } else {
            functions = functionManager.getRegisteredFunctions();
        }

        Set<String> filteredFunctions = new TreeSet<String>();

        for (String function : functions) {
            if(securityService.unlock(userId,function,siteReference)) {
                filteredFunctions.add(function);
            }
        }

        return filteredFunctions;
    }

    @EntityCustomAction(action = "pages", viewKey = EntityView.VIEW_SHOW)
    public ActionReturn getPagesAndTools(EntityView view, Search search) {
        // expects site/siteId/pages
        String userId = developerHelperService.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException(
            "This action (pages) is not accessible to anon and there is no current user.");
        }
        boolean admin = developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference());

        String siteId = view.getEntityReference().getId();
        Site site = getSiteById(siteId);
        if (! admin) {
            Member member = site.getMember(userId);
            if (member == null || ! member.isActive()) {
                throw new SecurityException("User ("+userId+") cannot access the site pages list for site ("+site.getId()+")");
            }
            //role = member.getRole();
        }
        boolean includeProps = false;
        boolean includeConfig = false;
        if (search != null) {
            Restriction r = search.getRestrictionByProperty("props");
            if (r != null && r.getBooleanValue()) {
                includeProps = true;
            }
            Restriction r2 = search.getRestrictionByProperty("config");
            if (r2 != null && r2.getBooleanValue()) {
                includeConfig = true;
            }
        }

        // hardcoding to make this backwards compatible with 2.3 - ServerConfigurationService.CURRENT_PORTAL_PATH, PORTAL_BASE);
        String portalBase = (String) ThreadLocalManager.get("sakai:request.portal.path");
        if (portalBase == null || "".equals(portalBase) || "/sakai-entitybroker-direct".equals(portalBase)) {
            // this has to be here because the tc will expect it when the portal urls are generated and fail if it is missing -AZ
            ThreadLocalManager.set("sakai:request.portal.path", "/portal");
        }

        // get the pages for this site
        List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
        EntitySite es = new EntitySite(site,false);
        
        List<SitePage> pages = es.getSitePages();
        for (SitePage page : pages) {
            HashMap<String, Object> pageData = new HashMap<String, Object>();
            pageData.put("id", page.getId());
            pageData.put("layoutTitle", page.getLayoutTitle());
            pageData.put("layout", page.getLayout());
            pageData.put("position", page.getPosition());
            pageData.put("siteId", page.getSiteId());
            pageData.put("skin", page.getSkin());
            pageData.put("title", page.getTitle());
            pageData.put("url", page.getUrl());
            if (includeProps) {
                // get the properties
                HashMap<String, String> props = new HashMap<String, String>();
                ResourceProperties rp = page.getProperties();
                for (Iterator<String> iterator = rp.getPropertyNames(); iterator.hasNext();) {
                    String name = iterator.next();
                    String value = rp.getProperty(name);
                    props.put(name, value);
                }
                pageData.put("properties", page.getProperties());
            }
            List<Map<String, Object>> tools = new ArrayList<Map<String,Object>>();
            pageData.put("tools", tools);

            // Peek into the tools to see if they want to be popped up 
	        // Similar to PortalSiteHelperImpl.java
            String source = null;
            boolean toolPopup = false;
            int count = 0;
            // get the tool configs for each
            for (ToolConfiguration tc : (List<ToolConfiguration>) page.getTools() ) {
                // get the tool from column 0 for this tool config (if there is one)
                Tool tool = tc.getTool();
                if (tool != null) {
                    HashMap<String, Object> toolData = new HashMap<String, Object>();
                    tools.add(toolData);
                    // back to normal stuff again
                    toolData.put("id", tc.getId());
                    toolData.put("toolId", tool.getId());
                    toolData.put("placementId", tc.getId());
                    toolData.put("title", tool.getTitle());
                    toolData.put("description", tool.getDescription());
                    toolData.put("url", page.getUrl());
                    toolData.put("home", tool.getHome());
                    toolData.put("context", tc.getContext());
                    toolData.put("pageId", tc.getPageId());
                    toolData.put("pageOrder", tc.getPageOrder());
                    toolData.put("siteId", tc.getSiteId());
                    if (includeConfig && admin) {
                        toolData.put("config", tc.getConfig());
                        toolData.put("registeredConfig", tool.getRegisteredConfig());
                        toolData.put("mutableConfig", tool.getMutableConfig());
                    }

                    count++;
                    Properties toolProps = tc.getConfig();
                    if ( "sakai.web.168".equals(tc.getToolId()) ) {
                        source = toolProps.getProperty("source");
                        toolPopup = "true".equals(toolProps.getProperty("popup"));
                    } else if ( "sakai.iframe".equals(tc.getToolId()) ) {
                        source = toolProps.getProperty("source");
                        toolPopup = "true".equals(toolProps.getProperty("popup"));
                    } else if ( "sakai.basiclti".equals(tc.getToolId()) ) {
                        toolPopup = "on".equals(toolProps.getProperty("imsti.newpage"));
                        source = "/access/basiclti/site/"+tc.getContext()+"/"+tc.getId();
                    }
                }
            }
            if ( count != 1 ) {
                toolPopup = false;
                source = null;
            }
            pageData.put("toolpopup", Boolean.valueOf(toolPopup));
            pageData.put("toolpopupurl", source);

            // Add the pageData
            data.add( pageData );
        }

        return new ActionReturn(data);
    }
    
    /**
     * this API call returns data of all the sites the user has permission say "site.visit"
     * for example, /direct/site/withPerm/.json?permission=site.visit
     * @param ref
     * @param search
     * @return
     */
    
    @EntityCustomAction(action = "withPerm", viewKey = "")
        public List<EntitySite> getMySitesWithPermission(EntityReference ref, Search search) {
    
           Restriction restriction = (search.getRestrictionByProperty(PERMISSION_QUERY_PROPERTY_NAME));
           String permission = null;
           List<EntitySite> sites = new ArrayList<EntitySite>();
    
           if (restriction == null) {
        	   log.info("Their is no restriction found for Property: "+PERMISSION_QUERY_PROPERTY_NAME);
              return sites;
          }
           else {
              permission = (String) restriction.getValue();
           }
    
    
           //Get all sites user can access
          List<EntitySite> possibleSites = (List<EntitySite>)getEntities(ref, search);
           String userId = developerHelperService.getCurrentUserId();
    
          for(EntitySite site: possibleSites) {
              String siteId = site.getId();
    
              if (securityService.unlock(userId, permission, siteService.siteReference(siteId))) {
                  sites.add(site);
              }
          }
    
           return sites;
        }

    @EntityCustomAction(action = "info", viewKey = "")
    public EntitySite getInfo(EntityReference ref) {

        String siteId = ref.getId();

        Site site = getSiteById(siteId);

        // Get a sparse entity, just with the info
        EntitySite es = new EntitySite();
        es.copyInfo(site);

        return es;
    }

    /**
     * @param site
     *            the site to check perms in
     * @return true if the current user can view this site
     * @throws SecurityException
     *             if not allowed
     */
    protected boolean isAllowedAccessMembers(Site site) {
        // check if the current user can access this
        String userReference = developerHelperService.getCurrentUserReference();
        if (userReference == null) {
            throw new SecurityException("Anonymous users may not view memberships in ("
                    + site.getReference() + ")");
        } else {
            if (!siteService.allowViewRoster(site.getId())) {
                throw new SecurityException("Memberships in this site (" + site.getReference()
                        + ") are not accessible for the current user: " + userReference);
            }
        }
        return true;
    }


    // STANDARD METHODS

    public boolean entityExists(String id) {
        if (id == null) {
            return false;
        }
        if ("".equals(id)) {
            return true;
        }
        boolean exists = siteService.siteExists(id);
        return exists;
    }

    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        String siteId = null;
        if (ref.getId() != null && ref.getId().length() > 0) {
            siteId = ref.getId();
        }
        if (entity.getClass().isAssignableFrom(Site.class)) {
            // if someone passes in a Site
            Site site = (Site) entity;
            if (siteId == null && site.getId() != null) {
                siteId = site.getId();
            }

            // check description
            String description = site.getDescription();

            if (description != null) {
                StringBuilder alertMsg = new StringBuilder();
                description = FormattedText.processFormattedText(description, alertMsg);
                if (description == null) {
                    throw new IllegalArgumentException("Site description markup rejected: " + alertMsg.toString());
                }
            }

            Site s = null;
            try {
                s = siteService.addSite(siteId, site.getType());
                s.setCustomPageOrdered(site.isCustomPageOrdered());
                s.setDescription(description);
                s.setIconUrl(site.getIconUrl());
                s.setInfoUrl(site.getInfoUrl());
                s.setJoinable(site.isJoinable());
                s.setJoinerRole(site.getJoinerRole());
                s.setMaintainRole(site.getMaintainRole());
                s.setProviderGroupId(site.getProviderGroupId());
                s.setPublished(site.isPublished());
                s.setPubView(site.isPubView());
                s.setShortDescription(site.getShortDescription());
                s.setSkin(site.getSkin());
                s.setTitle(site.getTitle());
                siteService.save(s);
                siteId = s.getId();
            } catch (IdInvalidException e) {
                try {
                    siteService.removeSite(s);
                } catch (Exception e1) {
                    log.warn("Could not cleanup site on create failure: " + e1); // BLANK
                }
                throw new IllegalArgumentException("Cannot create site with given id: " + siteId
                        + ":" + e.getMessage(), e);
            } catch (IdUsedException e) {
                try {
                    siteService.removeSite(s);
                } catch (Exception e1) {
                    log.warn("Could not cleanup site on create failure: " + e1); // BLANK
                }
                throw new IllegalArgumentException("Cannot create site with given id: " + siteId
                        + ":" + e.getMessage(), e);
            } catch (PermissionException e) {
                try {
                    siteService.removeSite(s);
                } catch (Exception e1) {
                    log.warn("Could not cleanup site on create failure: " + e1); // BLANK
                }
                throw new SecurityException(
                        "Current user does not have permissions to create site: " + ref + ":"
                        + e.getMessage(), e);
            } catch (IdUnusedException e) {
                try {
                    siteService.removeSite(s);
                } catch (Exception e1) {
                    log.warn("Could not cleanup site on create failure: " + e1); // BLANK
                }
                throw new IllegalArgumentException("Cannot save new site with given id: " + siteId
                        + ":" + e.getMessage(), e);
            }
        } else if (entity.getClass().isAssignableFrom(EntitySite.class)) {
            // if they instead pass in the EntitySite object
            EntitySite site = (EntitySite) entity;
            if (siteId == null && site.getId() != null) {
                siteId = site.getId();
            }

            // check description
            String description = site.getDescription();

            if (description != null) {
                StringBuilder alertMsg = new StringBuilder();
                description = FormattedText.processFormattedText(description, alertMsg);
                if (description == null) {
                    throw new IllegalArgumentException("Site description markup rejected: " + alertMsg.toString());
                }
            }            

            Site s = null;
            try {
                s = siteService.addSite(siteId, site.getType());
                s.setCustomPageOrdered(site.isCustomPageOrdered());
                s.setDescription(description);
                s.setIconUrl(site.getIconUrl());
                s.setInfoUrl(site.getInfoUrl());
                s.setJoinable(site.isJoinable());
                s.setJoinerRole(site.getJoinerRole());
                s.setMaintainRole(site.getMaintainRole());
                s.setProviderGroupId(site.getProviderGroupId());
                s.setPublished(site.isPublished());
                s.setPubView(site.isPubView());
                s.setShortDescription(site.getShortDescription());
                s.setSkin(site.getSkin());
                s.setTitle(site.getTitle());
                // attempt to set the owner as requested
                String ownerUserId = site.getOwner();
                if (ownerUserId != null) {
                    ownerUserId = userEntityProvider.findAndCheckUserId(ownerUserId, null);
                    if (ownerUserId == null) {
                        throw new IllegalArgumentException(
                                "Invalid userId supplied for owner of site: " + site.getOwner());
                    }
                    ReflectUtils.getInstance().setFieldValue(s, "m_createdUserId", ownerUserId);
                }
                // save the site
                siteService.save(s);
                siteId = s.getId();
            } catch (IdInvalidException e) {
                try {
                    siteService.removeSite(s);
                } catch (Exception e1) {
                    log.warn("Could not cleanup site on create failure: " + e1); // BLANK
                }
                throw new IllegalArgumentException("Cannot create site with given id: " + siteId
                        + ":" + e.getMessage(), e);
            } catch (IdUsedException e) {
                try {
                    siteService.removeSite(s);
                } catch (Exception e1) {
                    log.warn("Could not cleanup site on create failure: " + e1); // BLANK
                }
                throw new IllegalArgumentException("Cannot create site with given id: " + siteId
                        + ":" + e.getMessage(), e);
            } catch (PermissionException e) {
                try {
                    siteService.removeSite(s);
                } catch (Exception e1) {
                    log.warn("Could not cleanup site on create failure: " + e1); // BLANK
                }
                throw new SecurityException(
                        "Current user does not have permissions to create site: " + ref + ":"
                        + e.getMessage(), e);
            } catch (IdUnusedException e) {
                try {
                    siteService.removeSite(s);
                } catch (Exception e1) {
                    log.warn("Could not cleanup site on create failure: " + e1); // BLANK
                }
                throw new IllegalArgumentException("Cannot save new site with given id: " + siteId
                        + ":" + e.getMessage(), e);
            }
        } else {
            throw new IllegalArgumentException(
            "Invalid entity for creation, must be Site or EntitySite object");
        }
        return siteId;
    }

    public Object getSampleEntity() {
        return new EntitySite();
    }

    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        String siteId = ref.getId();
        if (siteId == null) {
            throw new IllegalArgumentException("Cannot update, No siteId in provided reference: "
                    + ref);
        }
        Site s = getSiteById(siteId);
        if (s == null) {
            throw new IllegalArgumentException("Cannot find site to update with id: " + siteId);
        }

        // get the site current publish status
        boolean oldPublishStatus = s.isPublished();
        boolean newPublishStatus = oldPublishStatus;
        boolean admin = developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference());

        if (entity.getClass().isAssignableFrom(Site.class)) {
            // if someone passes in a Site
            Site site = (Site) entity;

            // check description
            String description = site.getDescription();

            if (description != null) {
                StringBuilder alertMsg = new StringBuilder();
                description = FormattedText.processFormattedText(description, alertMsg);
                if (description == null) {
                    throw new IllegalArgumentException("Site description markup rejected: " + alertMsg.toString());
                }
            }            

            s.setCustomPageOrdered(site.isCustomPageOrdered());
            s.setDescription(description);
            s.setIconUrl(site.getIconUrl());
            s.setInfoUrl(site.getInfoUrl());
            s.setJoinable(site.isJoinable());
            s.setJoinerRole(site.getJoinerRole());
            s.setMaintainRole(site.getMaintainRole());
            s.setProviderGroupId(site.getProviderGroupId());
            s.setPublished(site.isPublished());
            s.setPubView(site.isPubView());
            s.setShortDescription(site.getShortDescription());
            s.setSkin(site.getSkin());
            s.setTitle(site.getTitle());

            // put in properties if admin, otherwise allow update of specific configurable fields.
            if (admin) {
                ResourcePropertiesEdit rpe = s.getPropertiesEdit();
                rpe.set(site.getProperties());
            } else {
                if (updateableSiteProps != null && updateableSiteProps.length != 0) {
                    ResourcePropertiesEdit rpe = s.getPropertiesEdit();
                    for (String prop : updateableSiteProps) {
                        if (site.getProperties().getProperty(prop) != null) {
                            rpe.addProperty(prop, site.getProperties().getProperty(prop));
                        }
                    }
                }
            }
            
            // set the new publish status
            newPublishStatus = site.isPublished();
        } else if (entity.getClass().isAssignableFrom(EntitySite.class)) {
            // if they instead pass in the entitysite object
            EntitySite site = (EntitySite) entity;

            // check description
            String description = site.getDescription();

            if (description != null) {
                StringBuilder alertMsg = new StringBuilder();
                description = FormattedText.processFormattedText(description, alertMsg);
                if (description == null) {
                    throw new IllegalArgumentException("Site description markup rejected: " + alertMsg.toString());
                }
            }            

            s.setCustomPageOrdered(site.isCustomPageOrdered());
            if (description != null)
                s.setDescription(description);
            if (site.getIconUrl() != null)
                s.setIconUrl(site.getIconUrl());
            s.setJoinable(site.isJoinable());
            if (site.getJoinerRole() != null)
                s.setJoinerRole(site.getJoinerRole());
            if (site.getMaintainRole() != null)
                s.setMaintainRole(site.getMaintainRole());
            if (site.getProviderGroupId() != null)
                s.setProviderGroupId(site.getProviderGroupId());
            s.setPublished(site.isPublished());
            s.setPubView(site.isPubView());
            if (site.getShortDescription() != null)
                s.setShortDescription(site.getShortDescription());
            if (site.getSkin() != null)
                s.setSkin(site.getSkin());
            if (site.getTitle() != null)
                s.setTitle(site.getTitle());
            if (site.getInfoUrl() != null) 
            	s.setInfoUrl(site.getInfoUrl());

            // put in properties if admin, otherwise allow update of specific configurable fields.
            if (admin) {
                ResourcePropertiesEdit rpe = s.getPropertiesEdit();
                for (String key : site.getProps().keySet()) {
                    String value = site.getProps().get(key);
                    rpe.addProperty(key, value);
                }
            } else {
                if (updateableSiteProps != null && updateableSiteProps.length != 0) {
                    ResourcePropertiesEdit rpe = s.getPropertiesEdit();
                    for (String prop: updateableSiteProps) {
                        for (String key : site.getProps().keySet()) {
                            if (prop.equals(key)) {
                                String value = site.getProps().get(key);
                                rpe.addProperty(key, value);
                            }
                        }
                    }
                }
            }

            // attempt to set the owner as requested
            String ownerUserId = site.getOwner();
            if (ownerUserId != null) {
                ownerUserId = userEntityProvider.findAndCheckUserId(ownerUserId, null);
                if (ownerUserId == null) {
                    throw new IllegalArgumentException(
                            "Invalid userId supplied for owner of site: " + site.getOwner());
                }
                ReflectUtils.getInstance().setFieldValue(s, "m_createdUserId", ownerUserId);
            }
            
            // new publish status
            newPublishStatus = site.isPublished();
        } else {
            throw new IllegalArgumentException(
            "Invalid entity for update, must be Site or EntitySite object");
        }
        try {
            siteService.save(s);
            
            // post event
            EventTrackingService eventTrackingService = (EventTrackingService) ComponentManager.get("org.sakaiproject.event.api.EventTrackingService");
            if (oldPublishStatus && !newPublishStatus)
            {
               // unpublish a published site
               eventTrackingService.post(eventTrackingService.newEvent(SiteService.EVENT_SITE_UNPUBLISH,siteService.siteReference(siteId),true));
            }
            else if (!oldPublishStatus&& newPublishStatus)
            {
               // publish an unpublished site
               eventTrackingService.post(eventTrackingService.newEvent(SiteService.EVENT_SITE_PUBLISH,siteService.siteReference(siteId),true));
            }
        } catch (IdUnusedException e) {
           throw new IllegalArgumentException(
                                              "Sakai was unable to save a site which it just fetched: " + ref, e);
        } catch (PermissionException e) {
           throw new SecurityException("Current user does not have permissions to update site: "
                                       + ref + ":" + e.getMessage(), e);
        }
    }

    @EntityParameters(accepted = { "includeGroups" })
    public Object getEntity(EntityReference ref) {
        boolean includeGroups = false;
        if (requestStorage.getStoredValue("includeGroups") != null) {
            includeGroups = true;
        }
        if (ref.getId() == null) {
            return new EntitySite();
        }
        String siteId = ref.getId();
        Site site = getSiteById(siteId);
        // check if the user can access site
        isAllowedAccessSite(site);
        // convert
        EntitySite es = new EntitySite(site, includeGroups);
        return es;
    }

    /**
     * @param site
     *            the site to check perms in
     * @return true if the current user can view this site
     * @throws SecurityException
     *             if not allowed
     */
    protected boolean isAllowedAccessSite(Site site) {
        // check if the user can access this
        String userReference = developerHelperService.getCurrentUserReference();
        if (userReference == null) {
            if (!siteService.allowAccessSite(site.getId())) {
                throw new SecurityException(
                        "This site ("
                        + site.getReference()
                        + ") is not accessible to anon and there is no current user so the site is inaccessible");
            }
        } else {
            if (!site.isPubView() && !siteService.allowAccessSite(site.getId())) {
                throw new SecurityException("This site (" + site.getReference()
                        + ") is not public and is not accessible for the current user: "
                        + userReference);
            }
        }
        return true;
    }

    public void deleteEntity(EntityReference ref, Map<String, Object> params) {
        String siteId = ref.getId();
        if (siteId == null || "".equals(siteId)) {
            throw new IllegalArgumentException(
                    "Cannot delete site, No siteId in provided reference: " + ref);
        }
        Site site = getSiteById(siteId);
        if (site != null) {
            try {
                siteService.removeSite(site);
            } catch (PermissionException e) {
                throw new SecurityException("Permission denied: Site cannot be removed: " + ref);
            } catch (IdUnusedException e) {
            	throw new IllegalArgumentException(
                        "Cannot delete site, No siteId in provided reference: " + ref);
			}
        }
    }

    @EntityParameters(accepted = { "select", "selectionType", "search", "_start", "_limit" })
    public List<?> getEntities(EntityReference ref, Search search) {
        String criteria = null;
        String selectType = "access";
        Restriction select = search.getRestrictionByProperty("select");
        if (select == null) {
            select = search.getRestrictionByProperty("selectionType");
        }
        if (select != null) {
            selectType = select.value + "";
        }
        SelectionType sType = SelectionType.ACCESS;
        if ("access".equals(selectType)) {
            sType = SelectionType.ACCESS;
        } else if ("update".equals(selectType)) {
            sType = SelectionType.UPDATE;
        } else if ("joinable".equals(selectType)) {
            sType = SelectionType.JOINABLE;
        } else if ("pubView".equals(selectType)) {
            sType = SelectionType.PUBVIEW;
        } else {
            // based on the current user
            String userReference = developerHelperService.getCurrentUserReference();
            if (userReference == null) {
                sType = SelectionType.PUBVIEW;
            } else {
                if (developerHelperService.isUserAdmin(userReference)) {
                    sType = SelectionType.ANY;
                }
            }
        }
        
        int start = 1;
        if (search.getStart() > 0 && search.getStart() < Integer.MAX_VALUE) {
            // Search docs indicate 0-based indexing, while PagingPosition is 1-based
            start = (int) search.getStart() + 1;
        }

        int limit = defaultPageSize;
        if (search.getLimit() > 0 && search.getLimit() < Integer.MAX_VALUE) {
            limit = (int) search.getLimit();
        }
        if (limit > maxPageSize) {
            limit = maxPageSize;
        }

        Restriction restrict = search.getRestrictionByProperty("search");
        if (restrict == null) {
            restrict = search.getRestrictionByProperty("criteria");
        }
        if (restrict != null) {
            criteria = restrict.value + "";
        }
        List<Site> sites = siteService.getSites(sType, null, criteria, null, SortType.TITLE_ASC,
                new PagingPosition(start, limit));
        // convert these into EntityUser objects
        List<EntitySite> entitySites = new ArrayList<EntitySite>();
        for (Site site : sites) {
            EntitySite es = new EntitySite(site, false);
            entitySites.add(es);
        }
        return entitySites;
    }

    public String[] getHandledInputFormats() {
        return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
    }

    public String[] getHandledOutputFormats() {
        return new String[] { Formats.XML, Formats.JSON, Formats.HTML, Formats.FORM };
    }

    private Site getSiteById(String siteId) {
        Site site;
        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            throw new IllegalArgumentException("Cannot find site by siteId: " + siteId, e);
        }
        return site;
    }

    private RequestStorage requestStorage;

    public void setRequestStorage(RequestStorage requestStorage) {
        this.requestStorage = requestStorage;
    }

    /**
     * Remove the users list from the group provided. User memberships must be retrieved from the
     * Memberships Entity Provider
     * 
     * @param grp
     *            Group to trim
     * @return
     */
    protected Group trimGroupUsers(Group grp) throws IllegalStateException {
        Group newGrp = grp;
        try {
            newGrp.deleteMembers();
        } catch (IllegalStateException e) {
            log.error(".trimGroupUsers: Members from group with id {} cannot be deleted because the group is locked", newGrp.getId());
        }
        return newGrp;
    }

    /**
     * Only handle Site Info type groups.
     * 
     * @param group
     * @throws IllegalArgumentException
     *             if NOT a Site Info type group
     */
    private void checkGroupType(Group group) {
        if (group != null) {
            try {
                if (!group.getProperties().getBooleanProperty(GROUP_PROP_WSETUP_CREATED)) {
                    throw new IllegalArgumentException(
                    "This type of group (Section Info group) should not be edited by this entity provider. Only Site info groups are allowed.");
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(
                "This type of group (Section Info group) should not be edited by this entity provider. Only Site info groups are allowed.");
            }
        }
    }

}
