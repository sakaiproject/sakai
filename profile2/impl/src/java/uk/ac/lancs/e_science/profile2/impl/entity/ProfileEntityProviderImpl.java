package uk.ac.lancs.e_science.profile2.impl.entity;

import java.util.List;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

import uk.ac.lancs.e_science.profile2.api.ProfileService;
import uk.ac.lancs.e_science.profile2.api.entity.ProfileEntityProvider;
import uk.ac.lancs.e_science.profile2.api.entity.model.UserProfile;

public class ProfileEntityProviderImpl implements ProfileEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, RESTful {

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	public boolean entityExists(String id) {
		//check the user is valid. if it is then return true as everyone has a profile.
		return profileService.checkUserProfileExists(id);
	}

	public String createEntity(EntityReference ref, Object entity) {
		UserProfile userProfile = (UserProfile) entity;
		profileService.save(userProfile);
		return userProfile.getUserUuid();
	}

	public Object getSampleEntity() {
		UserProfile userProfile = profileService.getPrototype();
		return userProfile;
	}

	public void updateEntity(EntityReference ref, Object entity) {
		// TODO Auto-generated method stub
		
	}

	public Object getEntity(EntityReference ref) {
	
		if (ref.getId() == null) {
			return profileService.getPrototype();
		}
		
		//no security yet. add another param on here for the person requesting the profile which will be used to check what they can see
		UserProfile entity = profileService.getUserProfile(ref.getId());
		return entity;
	}

	public void deleteEntity(EntityReference ref) {
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
		return new String[] {Formats.XML, Formats.JSON};
	}
	
	
	
	
	
	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}
	
	private ProfileService profileService;
	public void setProfileService(ProfileService profileService) {
		this.profileService = profileService;
	}

}
