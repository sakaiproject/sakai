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

package org.sakaiproject.webapi.permissions;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.webapi.common.AbstractSakaiRestEndpoint;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class PermissionsRestEndpoint extends AbstractSakaiRestEndpoint {

    @Resource
    private AuthzGroupService authzGroupService;

    @Resource
    private FunctionManager functionManager;

    @Resource
    private SecurityService securityService;

    @Resource
    private SiteService siteService;

    @GetMapping(value = "/sites/{siteId}/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getSitePermissions(@PathVariable String siteId, @RequestParam String tool, @RequestParam String groupRef) throws UserNotDefinedException {

        String userId = getCheckedSakaiSession().getUserId();

        String siteRef = siteService.siteReference(siteId);

        if (!authzGroupService.allowUpdate(siteRef)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Site site = getSite(siteId).orElseThrow(() -> new IllegalArgumentException("No site for id " + siteId));

        try {
            AuthzGroup authzGroup = authzGroupService.getAuthzGroup(groupRef);

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
            if (!groupRef.equals(siteRef)) {
            	available = available.stream().filter(p -> (p.indexOf("all.groups") == -1)).collect(Collectors.toList());
            }
            Map<String, Object> data = new HashMap<>();
            data.put("on", on);
            data.put("available", available);
            data.put("roleNameMappings", roleNameMappings);

            List<PermissionGroup> groups = site.getGroups().stream().map(PermissionGroup::new).collect(Collectors.toList());
            data.put("groups", groups);

            return ResponseEntity.ok(data);
        } catch (GroupNotDefinedException gnde) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/sites/{siteId}/permissions", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> setSitePermissions(@PathVariable String siteId, @RequestParam Map<String, Object> params) {

        String userId = getCheckedSakaiSession().getUserId();

        Site site = getSite(siteId).orElseThrow(() -> new IllegalArgumentException("No site for id " + siteId));

        if (!authzGroupService.allowUpdate(site.getReference())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<String> userMutableFunctions = functionManager.getRegisteredUserMutableFunctions();
        boolean admin = securityService.isSuperUser(userId);

        String groupRef = (String) params.get("groupRef");

        try {
            AuthzGroup authzGroup = authzGroupService.getAuthzGroup(groupRef);
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
            throw new IllegalArgumentException("No realm defined for ref " + groupRef + ".");
        }

        return ResponseEntity.ok("SUCCESS");
    }

    public class PermissionGroup {

        public String reference;
        public String title;

        public PermissionGroup(Group group) {

            this.reference = group.getReference();
            this.title = group.getTitle();
        }
    }

    private Optional<Site> getSite(String siteId) {

        try {
            Site site = siteService.getSite(siteId);
            return Optional.of(site);
        } catch (IdUnusedException idue) {
        }
        return Optional.empty();
    }
}
