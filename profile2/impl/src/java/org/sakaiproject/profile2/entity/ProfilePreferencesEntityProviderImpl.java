/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.profile2.entity;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.service.ProfilePreferencesService;
import org.sakaiproject.tool.api.SessionManager;

/**
 * This is the entity provider for a user's profile preferences.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfilePreferencesEntityProviderImpl implements CoreEntityProvider, AutoRegisterEntityProvider, RESTful {
	
	public final static String ENTITY_PREFIX = "profile-preferences";
	
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
		
	public boolean entityExists(String eid) {
		//check the user is valid. if it is then return true as everyone has a preferences record, even if its a default one.
		//note that we DO NOT check if they have an actual preferences record, just if they exist.
		return preferencesService.checkUserExists(eid);
	}

	public Object getSampleEntity() {
		
		ProfilePreferences prefs = preferencesService.getPrototype();
		return prefs;
	}
	
	public Object getEntity(EntityReference ref) {
	
		ProfilePreferences prefs = preferencesService.getProfilePreferencesRecord(ref.getId());
		if(prefs == null) {
			throw new EntityNotFoundException("ProfilePreferences could not be retrieved for " + ref.getId(), ref.getReference());
		}
		return prefs;
	}
	
	
	
	
	public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
	
		String userId = ref.getId();
		if (StringUtils.isBlank(userId)) {
			throw new IllegalArgumentException("Cannot update, No userId in provided reference: " + ref);
		}
		
		if (entity.getClass().isAssignableFrom(ProfilePreferences.class)) {
			ProfilePreferences prefs = (ProfilePreferences) entity;
			preferencesService.save(prefs);
		} else {
			 throw new IllegalArgumentException("Invalid entity for update, must be ProfilePreferences object");
		}
	
	}
	
	
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
				
		//reference will be the userUuid, which comes from the ProfilePreferences obj passed in
		String userUuid = null;

		if (entity.getClass().isAssignableFrom(ProfilePreferences.class)) {
			ProfilePreferences prefs = (ProfilePreferences) entity;
			
			if(preferencesService.create(prefs)) {
				userUuid = prefs.getUserUuid();
			}
			if(userUuid == null) {
				throw new EntityException("Could not create entity", ref.getReference());
			}
		} else {
			 throw new IllegalArgumentException("Invalid entity for create, must be ProfilePreferences object");
		}
		return userUuid;
	}

	

	
	
	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		// TODO Auto-generated method stub
	}

	public List<?> getEntities(EntityReference ref, Search search) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getHandledOutputFormats() {
		return new String[] {Formats.XML, Formats.JSON};
	}

	public String[] getHandledInputFormats() {
		return new String[] {Formats.XML, Formats.JSON, Formats.HTML};
	}
	
		
	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}
	
	private ProfilePreferencesService preferencesService;
	public void setPreferencesService(ProfilePreferencesService preferencesService) {
		this.preferencesService = preferencesService;
	}
	
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}	
	

}
