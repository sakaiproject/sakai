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
import java.util.Map;

import org.sakaiproject.microsoft.api.data.MicrosoftCredentials;
import org.sakaiproject.microsoft.api.data.MicrosoftUserIdentifier;
import org.sakaiproject.microsoft.api.data.SakaiSiteFilter;
import org.sakaiproject.microsoft.api.data.SakaiUserIdentifier;
import org.sakaiproject.microsoft.api.model.MicrosoftConfigItem;



public interface MicrosoftConfigurationService {
	
	//------------------------------ CREDENTIALS -------------------------------------------------------
	MicrosoftCredentials getCredentials();
	void saveCredentials(MicrosoftCredentials credentials);

	//------------------------------- MICROSOFT SYNCHRONIZATION ------------------------------------
	Map<String, MicrosoftConfigItem> getDefaultSynchronizationConfigItems();
	Map<String, MicrosoftConfigItem> getAllSynchronizationConfigItems();
	
	//------------------------------- MICROSOFT SYNCHRONIZATION - NEW SITE ------------------------------------
	public SakaiSiteFilter getNewSiteFilter();
	public void saveNewSiteFilter(SakaiSiteFilter filter);
	
	//------------------------------- SAKAI - MICROSOFT USER MAPPING ------------------------------------
	SakaiUserIdentifier getMappedSakaiUserId();
	MicrosoftUserIdentifier getMappedMicrosoftUserId();
	void saveMappedSakaiUserId(SakaiUserIdentifier identifier);
	void saveMappedMicrosoftUserId(MicrosoftUserIdentifier identifier);
	
	//------------------------------------------- COMMON ------------------------------------------------
	public String getConfigItemValueByKey(String key);
	void saveOrUpdateConfigItem(MicrosoftConfigItem item);
	void saveConfigItems(List<MicrosoftConfigItem> list);
}