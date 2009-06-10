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
import org.sakaiproject.profile2.model.ProfileStatus;
import org.sakaiproject.profile2.service.ProfileStatusService;
import org.sakaiproject.tool.api.SessionManager;

public class ProfileStatusEntityProviderImpl implements ProfileStatusEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, RESTful {
	
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
		
	public boolean entityExists(String eid) {
		//check if user has set their status recently
		return statusService.checkProfileStatusExists(eid);
	}

	public Object getSampleEntity() {
		
		ProfileStatus status = statusService.getPrototype();
		return status;
	}
	
	public Object getEntity(EntityReference ref) {
	
		ProfileStatus status = statusService.getProfileStatusRecord(ref.getId());
		if(status == null) {
			throw new EntityNotFoundException("ProfileStatus could not be retrieved for " + ref.getId(), ref.getReference());
		}
		return status;
	}
	
	
	public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
	
		String userId = ref.getId();
		if (StringUtils.isBlank(userId)) {
			throw new IllegalArgumentException("Cannot update, No userId in provided reference: " + ref);
		}
		
		if (entity.getClass().isAssignableFrom(ProfileStatus.class)) {
			ProfileStatus status = (ProfileStatus) entity;
			statusService.save(status);
		} else {
			 throw new IllegalArgumentException("Invalid entity for update, must be ProfileStatus object");
		}
	
	}
	
	
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
				
		//reference will be the userUuid, which comes from the ProfileStatus obj passed in
		String userUuid = null;

		if (entity.getClass().isAssignableFrom(ProfileStatus.class)) {
			ProfileStatus status = (ProfileStatus) entity;
			
			if(statusService.save(status)) {
				userUuid = status.getUserUuid();
			}
			if(userUuid == null) {
				throw new EntityException("Could not create entity", ref.getReference());
			}
		} else {
			 throw new IllegalArgumentException("Invalid entity for create, must be ProfileStatus object");
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
	
	private ProfileStatusService statusService;
	public void setStatusService(ProfileStatusService statusService) {
		this.statusService = statusService;
	}
	
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}	
	

}
