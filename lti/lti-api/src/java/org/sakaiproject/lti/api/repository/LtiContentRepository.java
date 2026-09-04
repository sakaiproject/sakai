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

import org.sakaiproject.lti.api.model.LtiContent;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface LtiContentRepository extends SpringCrudRepository<LtiContent, Long> {

    /**
     * Finds all content items visible to the requester, eagerly fetching the associated tool so
     * that tool-derived fields can be read after the session closes. Admins see every content item;
     * non-admins see items owned by their site or globally available (null site) items.
     *
     * @param siteId the requesting site (may be null for admins)
     * @param isAdmin whether the request is made with an admin role
     * @return the visible content items, ordered by id
     */
    List<LtiContent> findVisibleContents(String siteId, boolean isAdmin);

    /**
     * Finds a single content item by id. Admins can see any content item. Non-admins can only see a
     * content item that is owned by their site or is a globally available (null site) item.
     *
     * @param id the content id
     * @param siteId the requesting site (may be null for admins)
     * @param isAdmin whether the request is made with an admin role
     * @return the matching content item or empty
     */
    Optional<LtiContent> findVisibleContent(Long id, String siteId, boolean isAdmin);

    /**
     * Counts the content items grouped by their tool. Each returned tuple is
     * {@code [toolId (Long), contentCount (Long), distinctSiteCount (Long)]}. Tools with no content
     * items are not represented in the result.
     *
     * @return A List of 3 element array of Longs, one array per tool. tool id, content count and distinct site count
     */
    List<Long[]> countContentsByTool();

    /**
     * Reassigns content items from one tool to another. When {@code siteId} is non-null only content in
     * that site is reassigned (the non-admin case); when null every matching content item is reassigned.
     *
     * @param currentToolId the tool the content is currently associated with
     * @param newToolId the tool the content should be associated with
     * @param siteId the site to restrict to, or null for all sites
     * @return the number of content items reassigned
     */
    int reassignTool(Long currentToolId, Long newToolId, String siteId);

    /**
     * Finds the distinct site ids that have content for the given tool but no matching lti_tool_site
     * deployment for that tool. Used to auto-deploy a tool to sites that reference it via content.
     *
     * @param toolId the tool id
     * @return the site ids needing a deployment
     */
    List<String> findSitesNeedingDeployment(Long toolId);
}
