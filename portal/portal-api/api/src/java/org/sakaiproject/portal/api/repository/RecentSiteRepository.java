/*
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.portal.api.repository;

import java.util.Collection;
import java.util.List;

import org.sakaiproject.portal.api.model.RecentSite;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface RecentSiteRepository extends SpringCrudRepository<RecentSite, Long> {

    /**
     * Find all recent sites for a user
     *
     * @param userId the user to search for
     * @return all matching recent sites
     */
    List<RecentSite> findByUserId(String userId);

    /**
     * Find all users that have a recent site
     *
     * @param siteId the site to search for
     * @return all matching recent sites
     */
    List<RecentSite> findBySiteId(String siteId);

    /**
     * Delete all recent sites for a user
     *
     * @param userId the user whose recent sites will be deleted
     * @return the number of rows deleted
     */
    Integer deleteByUserId(String userId);

    /**
     * Delete a recent site for all users
     *
     * @param siteId the site to be deleted
     * @return the number of rows deleted
     */
    Integer deleteBySiteId(String siteId);

    /**
     * Delete a recent site for a user
     *
     * @param userId the user to remove recent site from
     * @param siteId the recent site to remove
     * @return the number of rows deleted
     */
    Integer deleteByUserIdAndSiteId(String userId, String siteId);

    /**
     * Delete recent sites for a user
     *
     * @param userId the user to remove recent sites from
     * @param siteIds a list of recent sites to remove
     * @return the number of rows deleted
     */
    Integer deleteByUserIdAndSiteIds(String userId, List<String> siteIds);
}
