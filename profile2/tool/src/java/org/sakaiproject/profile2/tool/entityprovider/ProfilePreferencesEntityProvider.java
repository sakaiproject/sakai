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

package org.sakaiproject.profile2.tool.entityprovider;

import java.util.Map;

import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Sampleable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Updateable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.profile2.logic.ProfilePreferencesLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.ProfilePreferences;

/**
 * This is the entity provider for a user's profile preferences.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfilePreferencesEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, AutoRegisterEntityProvider, Outputable, Updateable, Createable, Inputable, Sampleable, Describeable {
	
	public final static String ENTITY_PREFIX = "profile-preferences";
	
	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
		
	@Override
	public boolean entityExists(String eid) {
		return true;
	}

	@Override
	public Object getSampleEntity() {
		return new ProfilePreferences();
	}
	
	@Override
	public Object getEntity(EntityReference ref) {
	
		//convert input to uuid
		String uuid = sakaiProxy.ensureUuid(ref.getId());
		if(StringUtils.isBlank(uuid)) {
			throw new EntityNotFoundException("Invalid user.", ref.getId());
		}
		
		ProfilePreferences prefs = preferencesLogic.getPreferencesRecordForUser(uuid);
		if(prefs == null) {
			throw new EntityNotFoundException("ProfilePreferences could not be retrieved for " + ref.getId(), ref.getReference());
		}
		return prefs;
	}
	
	@Override
	public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
	
		String userId = ref.getId();
		if (StringUtils.isBlank(userId)) {
			throw new IllegalArgumentException("Cannot update, No userId in provided reference: " + ref);
		}
		
		if (entity.getClass().isAssignableFrom(ProfilePreferences.class)) {
			ProfilePreferences prefs = (ProfilePreferences) entity;
			preferencesLogic.savePreferencesRecord(prefs);
		} else {
			 throw new IllegalArgumentException("Invalid entity for update, must be ProfilePreferences object");
		}
		
	}
	
	@Override
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
				
		//reference will be the userUuid, which comes from the ProfilePreferences obj passed in
		String userUuid = null;

		if (entity.getClass().isAssignableFrom(ProfilePreferences.class)) {
			ProfilePreferences prefs = (ProfilePreferences) entity;
			
			if(preferencesLogic.savePreferencesRecord(prefs)) {
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

	
	@Override
	public String[] getHandledOutputFormats() {
		return new String[] {Formats.XML, Formats.JSON};
	}

	@Override
	public String[] getHandledInputFormats() {
		return new String[] {Formats.XML, Formats.JSON, Formats.HTML};
	}
	
	@Setter
	private SakaiProxy sakaiProxy;
	
	@Setter
	private ProfilePreferencesLogic preferencesLogic;

}
