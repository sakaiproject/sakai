/**
 * Copyright (c) 2026 The Apereo Foundation
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

package org.sakaiproject.webapi.beans;

import java.util.Optional;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteMemberRestBean {

    private String siteId;
    private String userId;
    private String role;
    private boolean active;

    public static SiteMemberRestBean of(String siteId, Member member) {
        return SiteMemberRestBean.builder()
                .siteId(siteId)
                .userId(member.getUserId())
                .role(Optional.ofNullable(member.getRole()).map(Role::getId).orElse(null))
                .active(member.isActive())
                .build();
    }
}
