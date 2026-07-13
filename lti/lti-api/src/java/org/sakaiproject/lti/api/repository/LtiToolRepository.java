/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.lti.api.repository;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.lti.api.model.LtiTool;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface LtiToolRepository extends SpringCrudRepository<LtiTool, Long> {

    List<LtiTool> findBySiteId(String siteId);
    int deleteBySiteId(String siteId);

    /**
     * Finds a single tool by id, applying the same visibility rules as the legacy DAO.
     * Admins can see any tool. Non-admins can only see a tool that is owned by their
     * site, is a globally visible (non-stealthed) system tool, or has been deployed to
     * their site via lti_tool_site.
     *
     * @param id the tool id
     * @param siteId the requesting site (may be null for admins)
     * @param isAdmin whether the request is made with an admin role
     * @return the matching tool, or empty if none is visible to the requester
     */
    Optional<LtiTool> findVisibleTool(Long id, String siteId, boolean isAdmin);

    /**
     * Finds all tools visible to the requester, applying the same visibility rules as the legacy list DAO.
     * Admins (or any request that includes stealthed tools) see every tool. Otherwise the result contains
     * tools owned by the requesting site, globally visible (non-stealthed system) tools and, when launchable
     * tools are requested, tools deployed to the site via lti_tool_site.
     *
     * @param siteId the requesting site (may be null for admins)
     * @param isAdmin whether the request is made with an admin role
     * @param includeStealthed whether stealthed/other-site tools should be included
     * @param includeLaunchable whether tools deployed to the site should be included
     * @return the visible tools, ordered by id
     */
    List<LtiTool> findVisibleTools(String siteId, boolean isAdmin, boolean includeStealthed, boolean includeLaunchable);
}
