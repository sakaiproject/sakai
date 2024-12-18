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

package org.sakaiproject.webcomponents.permissions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupAlreadyDefinedException;
import org.sakaiproject.authz.api.GroupIdInvalidException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

/**
 * Creates a provider for dealing with permissions
 *
 * @author Adrian Fish (adrian.r.fish@gmail.com)
 */
@Slf4j
@Setter
public class PermissionsEntityProvider extends AbstractEntityProvider implements EntityProvider, ActionsExecutable, Outputable {

    private SiteService siteService;
    private AuthzGroupService authzGroupService;
    private FunctionManager functionManager;
    private ServerConfigurationService serverConfigurationService;
    private SecurityService securityService;

    public static String PREFIX = "permissions";
    public String getEntityPrefix() {
        return PREFIX;
    }

    public String[] getHandledOutputFormats() {
        return new String[] { Formats.JSON };
    }

    @EntityCustomAction(action = "getPerms", viewKey = EntityView.VIEW_SHOW)
    public ActionReturn handleGet(EntityView view, Map<String, Object> params) {

        final String siteId = view.getEntityReference().getId();

        final String siteRef = Entity.SEPARATOR + "site" + Entity.SEPARATOR + siteId;

        // expects permissions/siteId/getPerms[/:TOOL:]
        String tool = view.getPathSegment(3);

        String userId = developerHelperService.getCurrentUserId();
        if (!authzGroupService.allowUpdate(siteRef)) {
            throw new EntityException("This action (getPerms) is not allowed.", siteRef, HttpServletResponse.SC_FORBIDDEN);
        }

        String reference = (String) params.get("ref");

        Site site = getSiteById(siteId);
        AuthzGroup authzGroup;
        try {
            authzGroup = authzGroupService.getAuthzGroup(reference);
        } catch (GroupNotDefinedException e) {
            // Instructor editing a folder that doesn't have a realm yet
            try {
                authzGroup = authzGroupService.getAuthzGroup("/site/" + siteId);
            } catch (GroupNotDefinedException ex) {
                throw new IllegalArgumentException("No realm defined for ref /site/" + siteId + ".");
            }
        }

        try {
            Set<Role> roles = authzGroup.getRoles();
            Map<String, Set<String>> on = new HashMap<>();
            for (Role role : roles) {
                Set<String> functions = role.getAllowedFunctions();
                Set<String> filteredFunctions = new TreeSet<>();
                if (tool != null) {
                    for (String function : functions) {
                        if (function.startsWith(tool + ".")) {
                            filteredFunctions.add(function);
                        }
                    }
                } else {
                    filteredFunctions = functions;
                }
                on.put(role.getId(), filteredFunctions);
            }

            Map<String, String> roleNameMappings
                = roles.stream().collect(
                    Collectors.toMap(Role::getId, r -> authzGroupService.getRoleName(r.getId())));

            List<String> available = functionManager.getRegisteredFunctions(tool + ".");
            if (!reference.equals("/site/" + siteId)) {
            	available = available.stream().filter(p -> (!p.contains("all.groups"))).collect(Collectors.toList());
            }
            Map<String, Object> data = new HashMap<>();
            data.put("on", on);
            data.put("available", available);
            data.put("roleNameMappings", roleNameMappings);

            List<PermissionGroup> groups = site.getGroups().stream().map(PermissionGroup::new).collect(Collectors.toList());
            data.put("groups", groups);

            return new ActionReturn(data, null, Formats.JSON);
        } catch (Exception e) {
            throw new IllegalArgumentException("No realm defined for ref " + reference + ".");
        }
    }

    @EntityCustomAction(action="setPerms", viewKey=EntityView.VIEW_EDIT)
    public String handleSet(EntityReference entityRef, Map<String, Object> params) {

        String userId = developerHelperService.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException(
            "This action (setPerms) is not accessible to anon and there is no current user.");
        }

        String siteId = entityRef.getId();
        Site site = getSiteById(siteId);

        if (!authzGroupService.allowUpdate(site.getReference())) {
            throw new EntityException("This action (setPerms) is not allowed.", site.getReference(), HttpServletResponse.SC_FORBIDDEN);
        }

        List<String> userMutableFunctions = functionManager.getRegisteredUserMutableFunctions();
        boolean admin = developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference());

        String reference = (String) params.get("ref");

        AuthzGroup authzGroup;
        try {
            authzGroup = authzGroupService.getAuthzGroup(reference);
        } catch (GroupNotDefinedException e) {
            try {
                // Only reason to be here is for a folder like /content/group/SITE_ID/FolderName/SubFolderName/
                if (!reference.matches("^/content/(group|group-user)/" + siteId + "/.*")) {
                    throw new IllegalArgumentException("Invalid reference format: " + reference);
                }
                authzGroup = authzGroupService.addAuthzGroup(reference);
            } catch (GroupIdInvalidException ex) {
                throw new IllegalArgumentException("Cannot add realm for ref " + reference + ".", ex);
            } catch (GroupAlreadyDefinedException ex) {
                throw new IllegalArgumentException("Cannot add duplicate realm for ref " + reference + ".", ex);
            } catch (AuthzPermissionException ex) {
                throw new SecurityException("The permissions for this site (" + siteId + ") cannot be updated by the current user.");
            }
        }

        try {
            boolean changed = false;
            for (String name : params.keySet()) {
                if (!name.contains(":")) {
                    continue;
                }
                String value = (String) params.get(name);
                String roleId = name.substring(0, name.indexOf(":"));
                Role role = authzGroup.getRole(roleId);
                if (role == null) {
                    role = authzGroup.addRole(roleId);
                }
                if (role == null ) {
                    throw new IllegalArgumentException("Invalid role id '" + roleId + "' provided in POST parameters.");
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
            throw new IllegalArgumentException("No realm defined for ref " + reference + ".");
        } catch (RoleAlreadyDefinedException e) {
            throw new IllegalArgumentException("Tried to add a role that already exists.", e);
        }
        return "SUCCESS";
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

    public class PermissionGroup {

        public String reference;
        public String title;

        public PermissionGroup(Group group) {

            this.reference = group.getReference();
            this.title = group.getTitle();
        }
    }
}
