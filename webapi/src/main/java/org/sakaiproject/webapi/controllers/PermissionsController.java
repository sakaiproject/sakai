/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

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
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST endpoints for managing permissions. Used by the SakaiPermissions web component
 */
@Slf4j
@RestController
public class PermissionsController extends AbstractSakaiApiController {

    @Autowired
    private AuthzGroupService authzGroupService;

    @Autowired
    private FunctionManager functionManager;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    private SecurityService securityService;

    @GetMapping(value = "/sites/{siteId}/permissions/{tool}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getPermissions(@PathVariable String siteId, @PathVariable String tool, 
                                             @RequestParam String ref, 
                                             @RequestParam(required = false) String overrideRef) {

        Session session = checkSakaiSession();
        final String siteRef = siteService.siteReference(siteId);

        if (!authzGroupService.allowUpdate(siteRef)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to update permissions for this site");
        }

        Site site = getSiteById(siteId);

        // Get the site's AuthzGroup once for potential reuse
        AuthzGroup siteAuthzGroup;
        try {
            siteAuthzGroup = authzGroupService.getAuthzGroup(siteRef);
        } catch (GroupNotDefinedException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No site realm defined for ref " + siteRef);
        }

        AuthzGroup authzGroup;
        try {
            authzGroup = authzGroupService.getAuthzGroup(ref);
        } catch (GroupNotDefinedException e) {
            // Instructor editing a folder that doesn't have a realm yet
            authzGroup = siteAuthzGroup;
        }

        AuthzGroup overrideAuthzGroup = null;
        if (StringUtils.isNotBlank(overrideRef)) {
            String tempOverrideRef = overrideRef;
            boolean done = false;
            // We need to make sure the ref is for a content folder in this site
            if (tempOverrideRef.matches("^/content/(group|group-user)/" + siteId + "/.*")) {
                // Keep trying parent folders until we find one with permissions or reach site level
                while (!done) {
                    try {
                        overrideAuthzGroup = authzGroupService.getAuthzGroup(tempOverrideRef);
                        done = true;
                    } catch (GroupNotDefinedException e) {
                        // Try parent folder - first check if we're already at site level
                        String siteRoot = "/content/group/" + siteId + "/";
                        String siteUserRoot = "/content/group-user/" + siteId + "/";
                        if (tempOverrideRef.equals(siteRoot) || tempOverrideRef.equals(siteUserRoot)) {
                            // At site level, use site's AuthzGroup
                            overrideAuthzGroup = siteAuthzGroup;
                            done = true;
                        } else {
                            // Remove the last folder segment but preserve trailing slash
                            String path = tempOverrideRef.substring(0, tempOverrideRef.length() - 1); // remove trailing slash
                            int lastSlash = path.lastIndexOf('/');
                            if (lastSlash > 0) {
                                tempOverrideRef = path.substring(0, lastSlash + 1); // restore trailing slash
                                log.debug("Trying parent folder for permissions: {}", tempOverrideRef);
                            } else {
                                // Shouldn't happen with our path pattern, but just in case
                                overrideAuthzGroup = siteAuthzGroup;
                                done = true;
                            }
                        }
                    }
                }
            } else {
                // Not a content path, try it directly once
                try {
                    overrideAuthzGroup = authzGroupService.getAuthzGroup(tempOverrideRef);
                } catch (GroupNotDefinedException e) {
                    // Use site's AuthzGroup
                    overrideAuthzGroup = siteAuthzGroup;
                }
            }
        }

        try {
            Set<Role> roles = authzGroup.getRoles();
            Map<String, Set<String>> on = new HashMap<>();
            Map<String, Set<String>> locked = new HashMap<>();
            for (Role role : roles) {
                Role overrideRole = overrideAuthzGroup != null ? overrideAuthzGroup.getRole(role.getId()) : null;
                Set<String> allowedFunctions = overrideRole != null ? overrideRole.getAllowedFunctions() : role.getAllowedFunctions();
                if (overrideRole != null) {
                    allowedFunctions.addAll(role.getAllowedFunctions());
                }

                Set<String> filteredFunctions = tool != null
                    ? allowedFunctions.stream().filter(f -> f.startsWith(tool + ".")).collect(Collectors.toSet())
                    : allowedFunctions;

                on.put(role.getId(), filteredFunctions);

                if (overrideRole != null) {
                    // If a overrideReference is provided this means that the authzgroup indicated by
                    // that reference takes precedence over what is set in the main group, the one
                    // indicated by "reference". So, if the override authzgroup has a function set,
                    // we need to prevent the user from unsetting that. So, we "lock" it.
                    locked.put(role.getId(), overrideRole.getAllowedFunctions());
                }
            }

            Map<String, String> roleNameMappings
                = roles.stream().collect(
                    Collectors.toMap(Role::getId, r -> authzGroupService.getRoleName(r.getId())));

            List<String> available = functionManager.getRegisteredFunctions(tool + ".");
            if (!ref.equals(siteService.siteReference(siteId))) {
                available = available.stream().filter(p -> (!p.contains("all.groups"))).collect(Collectors.toList());
            }

            List<PermissionGroup> groups = site.getGroups().stream().map(PermissionGroup::new).collect(Collectors.toList());

            return Map.of("on", on,
                          "locked", locked,
                          "available", available,
                          "roleNameMappings", roleNameMappings,
                          "groups", groups);
        } catch (Exception e) {
            log.error("Error getting permissions: {}", e.toString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting permissions");
        }
    }

    @PostMapping(value = "/sites/{siteId}/permissions")
    public String setPermissions(@PathVariable String siteId, 
                                @RequestParam String ref,
                                @RequestParam Map<String, String> params) {

        Session session = checkSakaiSession();

        Site site = getSiteById(siteId);

        if (!authzGroupService.allowUpdate(site.getReference())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to update permissions for this site");
        }

        List<String> userMutableFunctions = functionManager.getRegisteredUserMutableFunctions();

        AuthzGroup authzGroup;
        try {
            authzGroup = authzGroupService.getAuthzGroup(ref);
        } catch (GroupNotDefinedException e) {
            try {
                // Only reason to be here is for a folder like /content/group/SITE_ID/FolderName/SubFolderName/
                if (!ref.matches("^/content/(group|group-user)/" + siteId + "/.*")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reference format: " + ref);
                }
                authzGroup = authzGroupService.addAuthzGroup(ref);
            } catch (GroupIdInvalidException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add realm for ref " + ref);
            } catch (GroupAlreadyDefinedException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add duplicate realm for ref " + ref);
            } catch (AuthzPermissionException ex) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The permissions for this site cannot be updated by the current user");
            }
        }

        try {
            boolean changed = false;
            for (String name : params.keySet()) {
                if (!name.contains(":") || name.equals("ref")) {
                    continue;
                }
                String value = params.get(name);
                String roleId = name.substring(0, name.indexOf(":"));
                Role role = authzGroup.getRole(roleId);
                if (role == null) {
                    role = authzGroup.addRole(roleId);
                }
                if (role == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role id '" + roleId + "' provided in parameters");
                }
                String function = name.substring(name.indexOf(":") + 1);

                // Only change this function if registered as userMutable
                if (securityService.isSuperUser() || userMutableFunctions.contains(function)) {
                    if ("true".equals(value)) {
                        role.allowFunction(function);
                    } else {
                        role.disallowFunction(function);
                    }
                } else {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The function " + function + " cannot be updated by the current user");
                }
                changed = true;
            }

            if (changed) {
                try {
                    authzGroupService.save(authzGroup);
                } catch (AuthzPermissionException ape) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The permissions for this site cannot be updated by the current user");
                }
            }
        } catch (GroupNotDefinedException gnde) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No realm defined for ref " + ref);
        } catch (RoleAlreadyDefinedException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tried to add a role that already exists");
        }
        return "SUCCESS";
    }

    private Site getSiteById(String siteId) {

        Site site;
        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find site by siteId: " + siteId);
        }
        return site;
    }

    public static class PermissionGroup {

        public String reference;
        public String title;

        public PermissionGroup(Group group) {

            this.reference = group.getReference();
            this.title = group.getTitle();
        }
    }
}
