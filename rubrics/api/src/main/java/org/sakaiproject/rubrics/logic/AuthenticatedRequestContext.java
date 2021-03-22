/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon based on code created by pascal alma
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.rubrics.logic;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
/**
 * Holds the info for a authenticated user (Principal)
 */
public class AuthenticatedRequestContext implements UserDetails {

    private final String userId;
    private final String username;
    private final String toolId;
    private final String contextId;
    private final String contextType;
    private final Collection<GrantedAuthority> authorities = new ArrayList<>();
    private final String queryMode;
    private static final String ALL_MODE = "all";
    private static final String SITE_MODE = "site";
    
    public AuthenticatedRequestContext(String userId, String toolId, String contextId, String contextType, String queryMode) {
        this.userId = userId;
        this.username = userId;
        this.toolId = toolId;
        this.contextId = contextId;
        this.contextType = contextType;
        this.queryMode = queryMode;
    }

    @JsonIgnore
    public String getUserId(){
        return userId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void addAuthority(GrantedAuthority authority) {
        authorities.add(authority);
    }

    @Override
    public String getPassword() {
        return null;
    }

    public String getToolId() {
        return toolId;
    }

    public String getContextId() {
        return contextId;
    }

    public String getContextType() {
        return contextType;
    }

    public boolean isEditor() {
        return this.getAuthorities().stream().anyMatch( authority ->
                Role.ROLE_EDITOR.name().equalsIgnoreCase(authority.getAuthority()));
    }

    public boolean isAssociator() {
        return this.getAuthorities().stream().anyMatch( authority ->
                Role.ROLE_ASSOCIATOR.name().equalsIgnoreCase(authority.getAuthority()));
    }

    public boolean isEvaluator() {
        return this.getAuthorities().stream().anyMatch( authority ->
                Role.ROLE_EVALUATOR.name().equalsIgnoreCase(authority.getAuthority()));
    }

    public boolean isEvaluee() {
        return this.getAuthorities().stream().anyMatch( authority ->
                Role.ROLE_EVALUEE.name().equalsIgnoreCase(authority.getAuthority()));
    }

    public boolean isEvalueeOnly() {
        return this.getAuthorities().stream().allMatch(authority ->
                Role.ROLE_EVALUEE.name().equalsIgnoreCase(authority.getAuthority()));
    }

    public boolean isSuperUser() {
        return this.getAuthorities().stream().allMatch(authority ->
                Role.ROLE_SUPERUSER.name().equalsIgnoreCase(authority.getAuthority()));
    }
    @JsonIgnore
    public int isSharedAll() {
    	switch (this.queryMode.toLowerCase()) {
		case ALL_MODE:
			return 1;
		case SITE_MODE:
			return 2;
		default:
			return 0;
		}
    }
}
