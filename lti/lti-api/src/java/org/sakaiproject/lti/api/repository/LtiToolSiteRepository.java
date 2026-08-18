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

import org.sakaiproject.lti.api.model.LtiToolSite;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface LtiToolSiteRepository extends SpringCrudRepository<LtiToolSite, Long> {

    /**
     * Finds a single tool/site deployment by id, applying the same visibility rules as the legacy DAO.
     * Admins can see any deployment. Non-admins can only see a deployment that is owned by their site
     * or is a globally available (null site) deployment.
     *
     * @param id the tool/site deployment id
     * @param siteId the requesting site (may be null for admins)
     * @param isAdmin whether the request is made with an admin role
     * @return the matching deployment, or empty if none is visible to the requester
     */
    Optional<LtiToolSite> findVisibleToolSite(Long id, String siteId, boolean isAdmin);

    /**
     * Finds all tool/site deployments visible to the requester, ordered by id. Admins see every
     * deployment; non-admins see deployments owned by their site or globally available (null site) ones.
     *
     * @param siteId the requesting site (may be null for admins)
     * @param isAdmin whether the request is made with an admin role
     * @return the visible deployments, ordered by id
     */
    List<LtiToolSite> findVisibleToolSites(String siteId, boolean isAdmin);
}
