package uk.ac.lancs.e_science.profile2.impl.entity;

import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.entity.FriendsEntityProvider;
import uk.ac.lancs.e_science.profile2.api.entity.model.FriendsEntity;

public class FriendsEntityProviderImpl implements FriendsEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, RESTful {

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	public boolean entityExists(String id) {
		return true;
	}

	/** 
	 * get a list of friends online
	 */
	public Object getEntity(EntityReference ref) {
		String userUuid = ref.getId();
		if (userUuid == null) {
			return new FriendsEntity();
		}
		
		FriendsEntity friendsEntity = new FriendsEntity();
		
		
		
		return friendsEntity;
	}
	
	
	/**
	 * get an empty list as a sample
	 */
	public Object getSampleEntity() {
		return new FriendsEntity(new String[0]);
	}

	public String createEntity(EntityReference ref, Object entity) {
		return null;
	}
	
	public void updateEntity(EntityReference ref, Object entity) {}
	
	public void deleteEntity(EntityReference ref) {}

	public List<?> getEntities(EntityReference ref, Search search) {
		return null;
	}

	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON };
	}

	public String[] getHandledInputFormats() {
		return new String[] { Formats.XML, Formats.JSON };
	}
	
	
	// Added for compatibility
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		return createEntity(ref, entity);
	}

	public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		updateEntity(ref, entity);
	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		deleteEntity(ref);
	}
	
	
	// GET API'S
	
	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}
	
	private Profile profile;
	public void setProfile(Profile profile) {
		this.profile = profile;
	}
	
	

}
