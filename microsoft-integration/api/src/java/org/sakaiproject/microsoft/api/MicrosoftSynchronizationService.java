/**
* Copyright (c) 2023 Apereo Foundation
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
package org.sakaiproject.microsoft.api;

import java.util.List;

import org.sakaiproject.microsoft.api.data.SynchronizationStatus;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftCredentialsException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftGenericException;
import org.sakaiproject.microsoft.api.model.GroupSynchronization;
import org.sakaiproject.microsoft.api.model.SiteSynchronization;

public interface MicrosoftSynchronizationService {
	
	// ------------ Site Synchronization ---------------------------
	List<SiteSynchronization> getAllSiteSynchronizations(boolean fillSite);
	SiteSynchronization getSiteSynchronization(SiteSynchronization ss);
	SiteSynchronization getSiteSynchronization(SiteSynchronization ss, boolean fillSite);
	
	List<SiteSynchronization> getSiteSynchronizationsBySite(String siteId);
	List<SiteSynchronization> getSiteSynchronizationsByTeam(String teamId);
	
	long countSiteSynchronizationsByTeamId(String teamId, boolean forced);
	
	Integer deleteSiteSynchronizations(List<String> ids);
	
	void saveOrUpdateSiteSynchronization(SiteSynchronization ss);
	
	boolean removeUsersFromSynchronization(SiteSynchronization ss) throws MicrosoftCredentialsException;
	SynchronizationStatus checkStatus(SiteSynchronization ss);
	SynchronizationStatus runSiteSynchronization(SiteSynchronization ss) throws MicrosoftGenericException;

	// ------------ Group Synchronization ---------------------------
	List<GroupSynchronization> getAllGroupSynchronizationsBySiteSynchronizationId(String siteSynchronizationId);
	GroupSynchronization getGroupSynchronization(GroupSynchronization gs);
	long countGroupSynchronizationsByChannelId(String channelId);
	
	void saveOrUpdateGroupSynchronization(GroupSynchronization gs);
	
	void deleteAllGroupSynchronizationsBySiteSynchronizationId(String siteSynchronizationId);
	boolean deleteGroupSynchronization(String id);
}
