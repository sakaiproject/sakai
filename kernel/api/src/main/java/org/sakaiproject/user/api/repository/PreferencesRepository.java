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
package org.sakaiproject.user.api.repository;

import java.util.Optional;

import org.sakaiproject.springframework.data.SpringCrudRepository;
import org.sakaiproject.user.api.model.PreferencesRecord;

public interface PreferencesRepository extends SpringCrudRepository<PreferencesRecord, String> {

    /**
     * Read the stored row, bypassing the session's persistence context.
     * The returned record is not managed by the session.
     */
    Optional<PreferencesRecord> findCurrentById(String id);

    /**
     * Delete the row only if it still has the given version.
     *
     * @return the number of rows deleted, zero when the row changed or no longer exists
     */
    int deleteByIdAndVersion(String id, Long version);
}
