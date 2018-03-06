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
package org.sakaiproject.authz.impl;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Simple builder for tests
 */
public class AuthzGroupBuilder {

    AuthzGroup group;
    Set<Member> members = new HashSet<>();
    Map<String, Role> roles = new HashMap<>();

    public AuthzGroupBuilder(AuthzGroupService authzGroupService, String id) throws GroupNotDefinedException {
        group = mock(AuthzGroup.class);
        when(authzGroupService.getAuthzGroup(id)).thenReturn(group);
    }

    public AuthzGroupBuilder addMember(String userId, String roleId, boolean active) {
        Role role = roles.getOrDefault(roleId, mock(Role.class));
        Member member = mock(Member.class);
        when(member.getUserId()).thenReturn(userId);
        when(member.getRole()).thenReturn(role);
        when(member.isActive()).thenReturn(active);
        members.add(member);
        return this;
    }

    public AuthzGroup build() {
        when(group.getRoles()).thenReturn(new HashSet<>(roles.values()));
        when(group.getMembers()).thenReturn(members);
        return group;
    }
}
