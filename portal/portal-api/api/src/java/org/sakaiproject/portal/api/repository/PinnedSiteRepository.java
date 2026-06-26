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

import java.util.List;
import java.util.Optional;

import org.sakaiproject.portal.api.model.PinnedSite;
import org.sakaiproject.springframework.data.SpringCrudRepository;

/**
 * Persistence operations for {@link PinnedSite} records, which track the sites a user has pinned
 * (and the sites a user has explicitly unpinned). A user has at most one record per site.
 * {@code position} is a monotonic sort key used only for ordering pinned sites; it may contain gaps
 * and is not a contiguous index.
 */
public interface PinnedSiteRepository extends SpringCrudRepository<PinnedSite, Long> {

    /**
     * Finds the user's currently pinned sites, ordered by ascending {@code position}.
     *
     * @param userId the user whose pinned sites to retrieve
     * @return the pinned {@link PinnedSite} records in position order; empty if none
     */
    List<PinnedSite> findByUserIdOrderByPosition(String userId);

    /**
     * Finds the user's records in the given pinned state, ordered by ascending {@code position}.
     *
     * @param userId the user whose records to retrieve
     * @param hasBeenUnpinned {@code false} for currently pinned sites; {@code true} for sites the
     *        user has explicitly unpinned
     * @return the matching {@link PinnedSite} records in position order; empty if none
     */
    List<PinnedSite> findByUserIdAndHasBeenUnpinnedOrderByPosition(String userId, boolean hasBeenUnpinned);

    /**
     * Returns the highest {@code position} among the user's currently pinned sites. Used to append a
     * newly pinned site after the existing ones without loading them all.
     *
     * @param userId the user whose pinned sites to inspect
     * @return the maximum position, or {@link Optional#empty()} if the user has no pinned sites
     */
    Optional<Integer> findMaxPositionByUserId(String userId);

    /**
     * Finds the single record for the given user and site, in either pinned state.
     *
     * @param userId the user
     * @param siteId the site
     * @return the {@link PinnedSite} record, or {@link Optional#empty()} if none exists
     */
    Optional<PinnedSite> findByUserIdAndSiteId(String userId, String siteId);

    /**
     * Finds every record for the given site across all users, in either pinned state.
     *
     * @param siteId the site
     * @return the matching {@link PinnedSite} records; empty if none
     */
    List<PinnedSite> findBySiteId(String siteId);

    /**
     * Deletes the user's currently pinned records (those with {@code hasBeenUnpinned = false}).
     * Records for sites the user has explicitly unpinned are left in place.
     *
     * @param userId the user whose pinned records to delete
     * @return the number of rows deleted
     */
    Integer deleteByUserId(String userId);

    /**
     * Deletes every record for the given site across all users, regardless of pinned state.
     *
     * @param siteId the site to purge from all users' pinned/unpinned records
     * @return the number of rows deleted
     */
    Integer deleteBySiteId(String siteId);

    /**
     * Deletes the record for the given user and site, regardless of pinned state.
     *
     * @param userId the user
     * @param siteId the site
     * @return the number of rows deleted (0 or 1)
     */
    Integer deleteByUserIdAndSiteId(String userId, String siteId);

    /**
     * Deletes the user's records for the given sites, regardless of pinned state. Deletes are
     * issued in batches.
     *
     * @param userId the user
     * @param siteIds the sites to remove
     * @return the number of rows deleted
     */
    Integer deleteByUserIdAndSiteIds(String userId, List<String> siteIds);
}
