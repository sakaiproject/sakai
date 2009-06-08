package org.sakaiproject.profile2.impl.entity;

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
import org.sakaiproject.tool.api.SessionManager;

import org.sakaiproject.profile2.api.ProfilePrivacyService;
import org.sakaiproject.profile2.api.entity.ProfilePrivacyEntityProvider;
import org.sakaiproject.profile2.api.model.ProfilePrivacy;

public class ProfilePrivacyEntityProviderImpl implements ProfilePrivacyEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, RESTful {

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
		
	public boolean entityExists(String eid) {
		//check the user is valid. if it is then return true as everyone has a privacy record, even if its a default one.
		//note that we DO NOT check if they have an actual privacy record, just if they exist.
		return privacyService.checkUserExists(eid);
	}

	public Object getSampleEntity() {
		
		ProfilePrivacy privacy = privacyService.getPrototype();
		return privacy;
	}
	
	public Object getEntity(EntityReference ref) {
	
		ProfilePrivacy privacy = privacyService.getProfilePrivacyRecord(ref.getId());
		if(privacy == null) {
			throw new EntityNotFoundException("ProfilePrivacy could not be retrieved for " + ref.getId(), ref.getReference());
		}
		return privacy;
	}
	
	
	
	
	public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		
		String userId = ref.getId();
		if (StringUtils.isBlank(userId)) {
			throw new IllegalArgumentException("Cannot update, No userId in provided reference: " + ref);
		}
		
		if (entity.getClass().isAssignableFrom(ProfilePrivacy.class)) {
			ProfilePrivacy privacy = (ProfilePrivacy) entity;
			privacyService.save(privacy);
		} else {
			 throw new IllegalArgumentException("Invalid entity for update, must be ProfilePrivacy object");
		}
	
	}
	
	
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
				
		//reference will be the userUuid, which comes from the ProfilePrivacy obj passed in
		String userUuid = null;

		if (entity.getClass().isAssignableFrom(ProfilePrivacy.class)) {
			ProfilePrivacy privacy = (ProfilePrivacy) entity;
			
			if(privacyService.create(privacy)) {
				userUuid = privacy.getUserUuid();
			}
			if(userUuid == null) {
				throw new EntityException("Could not create entity", ref.getReference());
			}
		} else {
			 throw new IllegalArgumentException("Invalid entity for create, must be ProfilePrivacy object");
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
	
	private ProfilePrivacyService privacyService;
	public void setProfilePrivacyService(ProfilePrivacyService privacyService) {
		this.privacyService = privacyService;
	}
	
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}	
	

}
