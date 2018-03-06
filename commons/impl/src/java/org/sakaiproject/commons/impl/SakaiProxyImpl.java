/*************************************************************************************
 * Copyright 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.

 *************************************************************************************/
package org.sakaiproject.commons.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.fileupload.FileItem;

import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.commons.api.CommonsConstants;
import org.sakaiproject.commons.api.CommonsFunctions;
import org.sakaiproject.commons.api.SakaiProxy;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.delegatedaccess.logic.ProjectLogic;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.*;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.FormattedText;

/**
 * @author Adrian Fish (adrian.r.fish@gmail.com)
 */
@Setter @Slf4j
public class SakaiProxyImpl implements SakaiProxy {

    private AuthzGroupService authzGroupService;
    private ContentHostingService contentHostingService;
    private EntityManager entityManager;
    private EventTrackingService eventTrackingService;
    private FunctionManager functionManager;
    private MemoryService memoryService;
    private SecurityService securityService;
    private SessionManager sessionManager;
    private ServerConfigurationService serverConfigurationService;
    private SiteService siteService;
    private ToolManager toolManager;
    private UserDirectoryService userDirectoryService;
    private ProjectLogic projectLogic;

    public void init() {
    }


    public Session getCurrentSession() {
        return sessionManager.getCurrentSession();
    }

    public String getCurrentSiteId() {
        return toolManager.getCurrentPlacement().getContext(); // equivalent to
    }

    public Site getSiteOrNull(String siteId) {

        Site site = null;

        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException idue) {
            log.warn("No site with id '" + siteId + "'");
        }

        return site;
    }

    /**
     * {@inheritDoc}
     */
    public String getCurrentSiteLocale() {

        String siteId = toolManager.getCurrentPlacement().getContext();

        Site currentSite = getSiteOrNull(siteId);

        if (currentSite != null) {
            String locale = currentSite.getProperties().getProperty("locale_string");
            if (locale != null) {
                return locale;
            }
        }

        return null;
    }

    public Tool getCurrentTool() {
        return toolManager.getCurrentTool();
    }

    public String getCurrentToolId() {
        return toolManager.getCurrentPlacement().getId();
    }

    public String getCurrentUserId() {

        Session session = sessionManager.getCurrentSession();
        String userId = session.getUserId();
        return userId;
    }

    public ToolSession getCurrentToolSession() {
        return sessionManager.getCurrentToolSession();
    }

    public void setCurrentToolSession(ToolSession toolSession) {
        sessionManager.setCurrentToolSession(toolSession);
    }

    public String getDisplayNameForTheUser(String userId) {

        try {
            User sakaiUser = userDirectoryService.getUser(userId);
            return FormattedText.escapeHtmlFormattedText(sakaiUser.getDisplayName());
        } catch (Exception e) {
            return userId; // this can happen if the user does not longer exist
            // in the system
        }
    }

    public boolean isCurrentUserAdmin() {
        return securityService.isSuperUser();
    }

    public String getPortalUrl() {

        // don't use serverConfigurationService.getPortalUrl() as it can return
        // 'sakai-entitybroker-direct' instead of 'portal'
        String serverUrl = serverConfigurationService.getServerUrl();
        return serverUrl + serverConfigurationService.getString("portalPath");
    }

    public void registerEntityProducer(EntityProducer entityProducer) {
        entityManager.registerEntityProducer(entityProducer, "/commons");
    }

    public void registerFunction(String function) {

        List functions = functionManager.getRegisteredFunctions("commons.");

        if (!functions.contains(function)) {
            functionManager.registerFunction(function);
        }
    }

    public boolean isAllowedFunction(String function, String siteId) {

        try {
            Site site = siteService.getSite(siteId);
            Role role = getCurrentUserRoleForSite(site);
            return isAllowedFunction(function, role);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAllowedFunction(String function, Role role) {

        try {
            if (isCurrentUserAdmin()) {
                return true;
            }

            if (role == null) {
                return false;
            }

            return role.isAllowed(function);
        } catch (Exception e) {
            log.error("Caught exception while performing function test", e);
        }

        return false;
    }

    public void postEvent(String event, String reference, String siteId) {
        eventTrackingService.post(eventTrackingService.newEvent(event, reference, siteId, true, NotificationService.NOTI_OPTIONAL));
    }

    public Set<String> getSiteUsers(String siteId) {

        try {
            Site site = siteService.getSite(siteId);
            return site.getUsers();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    public String getCommonsToolId(String siteId) {

        try {
            Site site = siteService.getSite(siteId);
            ToolConfiguration tc = site.getToolForCommonId("sakai.commons");
            return tc.getId();
        } catch (Exception e) {
            return "";
        }
    }

    public Set<String> getSitePermissionsForCurrentUser(String siteId, String embedder) {

        Set<String> filteredFunctions = new TreeSet();

        Site site = null;
        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            log.error("Trying to get commons permissions for unknown site " + siteId, e);
        }

        String userId = getCurrentUserId();

        if (userId == null) {
            throw new SecurityException("This action (userPerms) is not accessible to anon and there is no current user.");
        }

        if (securityService.isSuperUser(userId)) {
            // Special case for the super admin
            filteredFunctions.addAll(functionManager.getRegisteredFunctions("commons"));
            return filteredFunctions;
        }

        Role siteRole = getCurrentUserRoleForSite(site);

        // Check to see if this is the user's own workspace
        if (siteService.getUserSiteId(userId).equals(siteId)) {
            // Make sure the basic set are allowed so that
            // the security manager can make the right decisions.
            AuthzGroup siteRealm = null;
            try {
                siteRealm = authzGroupService.getAuthzGroup("/site/" + siteId);
                if (!siteRole.isAllowed(CommonsFunctions.POST_CREATE)
                        || !siteRole.isAllowed(CommonsFunctions.POST_READ_ANY)
                        || !siteRole.isAllowed(CommonsFunctions.POST_UPDATE_OWN)
                        || !siteRole.isAllowed(CommonsFunctions.POST_DELETE_OWN)
                        || !siteRole.isAllowed(CommonsFunctions.COMMENT_CREATE)
                        || !siteRole.isAllowed(CommonsFunctions.COMMENT_READ_ANY)
                        || !siteRole.isAllowed(CommonsFunctions.COMMENT_UPDATE_OWN)
                        || !siteRole.isAllowed(CommonsFunctions.COMMENT_DELETE_OWN)) {

                    siteRole.allowFunction(CommonsFunctions.POST_CREATE);
                    siteRole.allowFunction(CommonsFunctions.POST_READ_ANY);
                    siteRole.allowFunction(CommonsFunctions.POST_UPDATE_OWN);
                    siteRole.allowFunction(CommonsFunctions.POST_DELETE_OWN);
                    siteRole.allowFunction(CommonsFunctions.COMMENT_CREATE);
                    siteRole.allowFunction(CommonsFunctions.COMMENT_READ_ANY);
                    siteRole.allowFunction(CommonsFunctions.COMMENT_UPDATE_OWN);
                    siteRole.allowFunction(CommonsFunctions.COMMENT_DELETE_OWN);
                    authzGroupService.save(siteRealm);
                }
            } catch (Exception e) {
                log.error("Exception while looking up or modifying user workspace role " +
                    siteRole.getId() + " in site " + siteId, e);
            }
        }

        // If the commons is attached to an assignment, then it gets the permissions of
        // user relative to the assignment
        if (embedder.equals(CommonsConstants.ASSIGNMENT)) {
            // This won't generally apply to workspace sites, but it's harmless
            if (siteRole.isAllowed(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION)) {
                filteredFunctions.add(CommonsFunctions.POST_CREATE);
                filteredFunctions.add(CommonsFunctions.POST_READ_ANY);
                filteredFunctions.add(CommonsFunctions.POST_UPDATE_OWN);
                filteredFunctions.add(CommonsFunctions.POST_DELETE_OWN);
                filteredFunctions.add(CommonsFunctions.COMMENT_CREATE);
                filteredFunctions.add(CommonsFunctions.COMMENT_READ_ANY);
                filteredFunctions.add(CommonsFunctions.COMMENT_UPDATE_OWN);
                filteredFunctions.add(CommonsFunctions.COMMENT_DELETE_OWN);
            }

            if (siteRole.isAllowed(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT)) {
                filteredFunctions.add(CommonsFunctions.POST_CREATE);
                filteredFunctions.add(CommonsFunctions.POST_READ_ANY);
                filteredFunctions.add(CommonsFunctions.POST_DELETE_ANY);
                filteredFunctions.add(CommonsFunctions.POST_UPDATE_OWN);
                filteredFunctions.add(CommonsFunctions.POST_DELETE_OWN);
                filteredFunctions.add(CommonsFunctions.COMMENT_CREATE);
                filteredFunctions.add(CommonsFunctions.COMMENT_READ_ANY);
                filteredFunctions.add(CommonsFunctions.COMMENT_UPDATE_OWN);
                filteredFunctions.add(CommonsFunctions.COMMENT_DELETE_OWN);
                filteredFunctions.add(CommonsFunctions.COMMENT_DELETE_ANY);
            }

            return filteredFunctions;
        }

        Set<String> functions = siteRole.getAllowedFunctions();

        AuthzGroup siteHelperRealm = null;
        try {
            siteHelperRealm = authzGroupService.getAuthzGroup("!site.helper");
        } catch (Exception e) {
            log.error("Error calling authzGroupService.getAuthzGroup(\"!site.helper\")", e);
        }
        if (siteHelperRealm != null) {
            Role siteHelperRole = siteHelperRealm.getRole(siteRole.getId());
            if (siteHelperRole != null) {
                // Merge in all the functions from the same role in !site.helper
                functions.addAll(siteHelperRole.getAllowedFunctions());
            }
        }

        // Don't like this startsWith use. Things could start with "commons" but
        // still not be relevant here
        filteredFunctions.addAll(functions.stream().filter(f -> f.startsWith("commons") || f.equals(SiteService.SECURE_UPDATE_SITE)).collect(Collectors.toSet()));

        return filteredFunctions;
    }


    public Role getCurrentUserRoleForSite(Site site) {
        String[] delegatedAccess = projectLogic.getCurrentUsersAccessToSite("/site/" + site.getId());
        if (delegatedAccess != null && delegatedAccess.length >= 2) {
            Role role = null;
            try {
                role = site.getRole(delegatedAccess[1]);
            } catch (Exception e) {
                log.error("Exception getting role for delegatedAccess role " +
                    delegatedAccess[1] + " in site " + site.getId(), e);
            }
            // This user has been delegated a role that doesn't exist in this site
            // Try "Student" and "access"
            if(role == null) {
                role = site.getRole("Student");
            }
            if(role == null) {
                role = site.getRole("access");
            }
            if(role == null) {
                log.error("Unable to find a role for user with delegatedAccess role " +
                    delegatedAccess[1] + " in site " + site.getId());
            }
            return role;
        }
        return site.getUserRole(getCurrentUserId());
    }


    public Map<String, Set<String>> getSitePermissions(String siteId) {

        Map<String, Set<String>> perms = new HashMap();

        String userId = getCurrentUserId();

        if (userId == null) {
            throw new SecurityException("This action (perms) is not accessible to anon and there is no current user.");
        }

        try {
            Site site = siteService.getSite(siteId);

            for (Role role : site.getRoles()) {
                Set<String> functions = role.getAllowedFunctions();
                perms.put(role.getId(), functions.stream().filter(f -> f.startsWith("commons")).collect(Collectors.toSet()));
            }
        } catch (Exception e) {
            log.error("Failed to get current site permissions.", e);
        }

        return perms;
    }

    public boolean setPermissionsForSite(String siteId, Map<String, Object> params) {

        String userId = getCurrentUserId();

        if (userId == null)
            throw new SecurityException("This action (setPerms) is not accessible to anon and there is no current user.");

        Site site = null;

        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException ide) {
            log.warn(userId + " attempted to update COMMONS permissions for unknown site " + siteId);
            return false;
        }

        boolean admin = securityService.isSuperUser(userId);

        try {

            AuthzGroup authzGroup = authzGroupService.getAuthzGroup(site.getReference());

            // admin can update permissions. check for anyone else
            if (!securityService.isSuperUser()) {

                Role siteRole = getCurrentUserRoleForSite(site);
                AuthzGroup siteHelperAuthzGroup = authzGroupService.getAuthzGroup("!site.helper");
                Role siteHelperRole = siteHelperAuthzGroup.getRole(siteRole.getId());

                if (!siteRole.isAllowed(SiteService.SECURE_UPDATE_SITE)) {
                    log.warn(userId + " attempted to update COMMONS permissions for site " + site.getTitle());
                    return false;
                }
            }

            boolean changed = false;

            for (String name : params.keySet()) {
                if (!name.contains(":")) {
                    continue;
                }

                String value = (String) params.get(name);

                String roleId = name.substring(0, name.indexOf(":"));

                Role role = authzGroup.getRole(roleId);
                if (role == null) {
                    throw new IllegalArgumentException("Invalid role id '" + roleId + "' provided in POST parameters.");
                }
                String function = name.substring(name.indexOf(":") + 1);

                if ("true".equals(value)) {
                    role.allowFunction(function);
                } else {
                    role.disallowFunction(function);
                }

                changed = true;
            }

            if (changed) {
                try {
                    authzGroupService.save(authzGroup);
                } catch (AuthzPermissionException ape) {
                    throw new SecurityException("The permissions for this site (" + siteId + ") cannot be updated by the current user.");
                }
            }

            return true;
        } catch (GroupNotDefinedException gnde) {
            log.error("No realm defined for site (" + siteId + ").", gnde);
        }

        return false;
    }

    public Cache getCache(String cache) {

        try {
            return memoryService.getCache(cache);
        } catch (Exception e) {
            log.error("Exception whilst retrieving '" + cache + "' cache. Returning null ...", e);
            return null;
        }
    }

    public boolean isUserSite(String siteId) {
        return siteService.isUserSite(siteId);
    }

    public void addObserver(Observer observer) {
        eventTrackingService.addObserver(observer);
    }

    public String storeFile(FileItem fileItem, String siteId) {

        SecurityAdvisor advisor = new SecurityAdvisor() {
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
                return SecurityAdvice.ALLOWED;
            }
        };
        securityService.pushAdvisor(advisor);
        try {
            String fileName = fileItem.getName();
            int lastIndexOf = fileName.lastIndexOf("/");
            if (lastIndexOf != -1 && (fileName.length() > lastIndexOf + 1)) {
                fileName = fileName.substring(lastIndexOf + 1);
            }
            String suffix = "";
            lastIndexOf = fileName.lastIndexOf(".");
            if (lastIndexOf != -1 && (fileName.length() > lastIndexOf + 1)) {
                suffix = fileName.substring(lastIndexOf + 1);
                fileName = fileName.substring(0, lastIndexOf);
            }
            String toolCollection = Entity.SEPARATOR + "group" + Entity.SEPARATOR +
                siteId + Entity.SEPARATOR + "commons" + Entity.SEPARATOR;

            try {
                contentHostingService.checkCollection(toolCollection);
            } catch (Exception e) {
                // add this collection
                ContentCollectionEdit toolEdit = contentHostingService.addCollection(toolCollection);
                toolEdit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, "commons");
                contentHostingService.commitCollection(toolEdit);
            }

            ContentResourceEdit edit
                = contentHostingService.addResource(toolCollection, fileName, suffix , 2);
            edit.setContent(fileItem.getInputStream());
            contentHostingService.commitResource(edit, NotificationService.NOTI_NONE);
            return edit.getUrl();
        } catch (Exception e) {
            log.error("Failed to store file.", e);
            return null;
        } finally {
            securityService.popAdvisor(advisor);
        }
    }
}
