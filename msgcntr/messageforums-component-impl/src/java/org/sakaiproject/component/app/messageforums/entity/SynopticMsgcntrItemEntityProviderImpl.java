/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.component.app.messageforums.entity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.SynopticMsgcntrItem;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.api.app.messageforums.entity.SynopticMsgcntrItemEntityProvider;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.user.api.UserDirectoryService;

@Slf4j
public class SynopticMsgcntrItemEntityProviderImpl 
implements SynopticMsgcntrItemEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, PropertyProvideable, RequestStorable, RESTful, RequestAware{

    private SynopticMsgcntrManager synopticMsgcntrManager;

    private RequestStorage requestStorage;
    public void setRequestStorage(RequestStorage requestStorage) {
        this.requestStorage = requestStorage;
    }
    
    private UserDirectoryService userDirectoryService;	
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    private RequestGetter requestGetter;
    public void setRequestGetter(RequestGetter requestGetter){
    	this.requestGetter = requestGetter;
    }
    
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	public List<String> findEntityRefs(String[] prefixes, String[] name,
			String[] searchValue, boolean exactMatch) {
	    return null;
	  }

	public Map<String, String> getProperties(String reference) {
		return null;
	}

	public String getPropertyValue(String reference, String name) {
		Map<String, String> props = getProperties(reference);
	    return props.get(name);
	}

	public void setPropertyValue(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	public SynopticMsgcntrManager getSynopticMsgcntrManager() {
		return synopticMsgcntrManager;
	}

	public void setSynopticMsgcntrManager(
			SynopticMsgcntrManager synopticMsgcntrManager) {
		this.synopticMsgcntrManager = synopticMsgcntrManager;
	}

	public boolean entityExists(String arg0) {
		return true;
	}

	public String createEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getSampleEntity() {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		
	}

	public Object getEntity(EntityReference ref) {
		String siteId = ref.getId();
		if (siteId == null) {
			return null;
		}
		String userId = userDirectoryService.getCurrentUser().getId();
		if(userId == null || "".equals(userId)){
			return null;
		}
		
		List<SynopticMsgcntrItem> smiList = synopticMsgcntrManager.getSiteSynopticMsgcntrItems(Arrays.asList(userId), siteId);
		SynopticMsgcntrItem smi = null;
		if(smiList != null && smiList.size() == 1){
			smi = smiList.get(0);
		}
		if (smi == null) {
			smi = synopticMsgcntrManager.createSynopticMsgcntrItem(null, null, null);
		}
		return smi;		
	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		// TODO Auto-generated method stub
		
	}

	public List<?> getEntities(EntityReference ref, Search search) {		
		String userId = userDirectoryService.getCurrentUser().getId();
		if(userId == null){
			return null;
		}
		
		return synopticMsgcntrManager.getWorkspaceSynopticMsgcntrItems(userId);
	}
	
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
	}

	public String[] getHandledInputFormats() {
		// TODO Auto-generated method stub
		return null;
	}
}
