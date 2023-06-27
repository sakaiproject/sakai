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
package org.sakaiproject.microsoft.impl;

import java.util.List;
import java.util.Map;

import org.sakaiproject.microsoft.api.MicrosoftConfigurationService;
import org.sakaiproject.microsoft.api.data.MicrosoftCredentials;
import org.sakaiproject.microsoft.api.data.MicrosoftUserIdentifier;
import org.sakaiproject.microsoft.api.data.SakaiSiteFilter;
import org.sakaiproject.microsoft.api.data.SakaiUserIdentifier;
import org.sakaiproject.microsoft.api.model.MicrosoftConfigItem;
import org.sakaiproject.microsoft.api.persistence.MicrosoftConfigRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class MicrosoftConfigurationServiceImpl implements MicrosoftConfigurationService {
	
	
	@Setter
	MicrosoftConfigRepository microsoftConfigRepository;

	public void init() {
		log.info("Initializing MicrosoftConfigurationService Service");
	}
	
	//------------------------------ CREDENTIALS -------------------------------------------------------
	public MicrosoftCredentials getCredentials() {
		return microsoftConfigRepository.getCredentials();
	}
	
	public void saveCredentials(MicrosoftCredentials credentials) {
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftCredentials.KEY_AUTHORITY).value(credentials.getAuthority()).build());
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftCredentials.KEY_CLIENT_ID).value(credentials.getClientId()).build());
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftCredentials.KEY_SECRET).value(credentials.getSecret()).build());
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftCredentials.KEY_SCOPE).value(credentials.getScope()).build());
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftCredentials.KEY_DELEGATED_SCOPE).value(credentials.getDelegatedScope()).build());
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftCredentials.KEY_EMAIL).value(credentials.getEmail()).build());
	}
	
	//------------------------------- MICROSOFT SYNCHRONIZATION ------------------------------------
	public Map<String, MicrosoftConfigItem> getDefaultSynchronizationConfigItems(){
		return microsoftConfigRepository.getDefaultSynchronizationConfigItems();
	}
	
	public Map<String, MicrosoftConfigItem> getAllSynchronizationConfigItems(){
		return microsoftConfigRepository.getAllSynchronizationConfigItems();
	}
	
	//------------------------------ ONEDRIVE -------------------------------------------------------
	public boolean isOneDriveEnabled() {
		return Boolean.valueOf(microsoftConfigRepository.getConfigItemValueByKey(MicrosoftConfigRepository.ONEDRIVE_ENABLED));
	}
	
	//------------------------------- MICROSOFT SYNCHRONIZATION - NEW SITE ------------------------------------
	public SakaiSiteFilter getNewSiteFilter() {
		return microsoftConfigRepository.getNewSiteFilter();
	}
	
	public void saveNewSiteFilter(SakaiSiteFilter filter) {
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftConfigRepository.NEW_SITE_TYPE).value(filter.getSiteType()).build());
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftConfigRepository.NEW_SITE_PUBLISHED).value(Boolean.toString(filter.isPublished())).build());
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftConfigRepository.NEW_SITE_PROPERTY).value(filter.getSiteProperty()).build());
	}
	
	public long getSyncDuration() {
		return microsoftConfigRepository.getSyncDuration();
	}
	
	public void saveSyncDuration(long syncDuration) {
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftConfigRepository.NEW_SITE_SYNC_DURATION).value(String.valueOf(syncDuration)).build());
	}
	
	//------------------------------- MICROSOFT SYNCHRONIZATION - JOB ------------------------------------
	public SakaiSiteFilter getJobSiteFilter() {
		return microsoftConfigRepository.getJobSiteFilter();
	}
	
	public void saveJobSiteFilter(SakaiSiteFilter filter) {
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftConfigRepository.JOB_SITE_TYPE).value(filter.getSiteType()).build());
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftConfigRepository.JOB_SITE_PUBLISHED).value(Boolean.toString(filter.isPublished())).build());
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftConfigRepository.JOB_SITE_PROPERTY).value(filter.getSiteProperty()).build());
	}
	
	//------------------------------- SAKAI - MICROSOFT USER MAPPING ------------------------------------
	public SakaiUserIdentifier getMappedSakaiUserId() {
		return microsoftConfigRepository.getMappedSakaiUserId();
	}
	
	public MicrosoftUserIdentifier getMappedMicrosoftUserId() {
		return microsoftConfigRepository.getMappedMicrosoftUserId();
	}
	public void saveMappedSakaiUserId(SakaiUserIdentifier identifier) {
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(SakaiUserIdentifier.KEY).value(identifier.getCode()).build());
	}
	public void saveMappedMicrosoftUserId(MicrosoftUserIdentifier identifier) {
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftUserIdentifier.KEY).value(identifier.getCode()).build());
	}
	
	//------------------------------------------- COMMON ------------------------------------------------
	public String getConfigItemValueByKey(String key) {
		return microsoftConfigRepository.getConfigItemValueByKey(key);
	}
	
	public void saveOrUpdateConfigItem(MicrosoftConfigItem item) {
		if (microsoftConfigRepository.exists(item.getKey())) {
			microsoftConfigRepository.merge(item);
		} else {
			microsoftConfigRepository.save(item);
		}
	}
	
	public void saveConfigItems(List<MicrosoftConfigItem> list){
		list.forEach(item -> saveOrUpdateConfigItem(item));
	}
}
