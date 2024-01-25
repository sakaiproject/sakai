/**
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.microsoft.api.persistence;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.microsoft.api.model.SiteSynchronization;
import org.sakaiproject.serialization.SerializableRepository;

public interface MicrosoftSiteSynchronizationRepository extends SerializableRepository<SiteSynchronization, String> {
	Optional<SiteSynchronization> findById(String id);
	Optional<SiteSynchronization> findBySiteTeam(String siteId, String teamId);
	List<SiteSynchronization> findBySite(String siteId);
	List<SiteSynchronization> findByTeam(String teamId);
	List<String> findBySiteIdList(List<String> siteIds);
	
	long countSiteSynchronizationsByTeamId(String teamId, boolean forced);
	
	Integer deleteSiteSynchronizationsById(List<String> ids);
}
