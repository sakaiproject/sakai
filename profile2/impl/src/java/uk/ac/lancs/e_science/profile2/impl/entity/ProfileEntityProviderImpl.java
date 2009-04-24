package uk.ac.lancs.e_science.profile2.impl.entity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.tool.api.SessionManager;

import uk.ac.lancs.e_science.profile2.api.ProfileService;
import uk.ac.lancs.e_science.profile2.api.entity.ProfileEntityProvider;
import uk.ac.lancs.e_science.profile2.api.entity.model.UserProfile;

public class ProfileEntityProviderImpl implements ProfileEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, RESTful {

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
	
	public boolean entityExists(String eid) {
		//check the user is valid. if it is then return true as everyone has a profile.
		return profileService.checkUserProfileExists(eid);
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
	
	public Object getEntity(EntityReference ref) {
	
		if (sessionManager.getCurrentSessionUserId() == null) {
			throw new SecurityException();
		}
		
		if (ref.getId() == null) {
			return profileService.getPrototype();
		}
		
		//get the profile, with privacy checks against the requesting user
		UserProfile entity = profileService.getUserProfile(ref.getId(), sessionManager.getCurrentSessionUserId());
		return entity;
	}
	
	@EntityCustomAction(action="image",viewKey=EntityView.VIEW_SHOW)
	public Object getImage(OutputStream out, EntityView view, EntityReference ref) {
		
		if (sessionManager.getCurrentSessionUserId() == null) {
			throw new SecurityException();
		}
		
		if (ref.getId() == null) {
			return null;
		}
		
		//get profile image. TODO add requesting userId and the type of image, or make this two functions one for main and one for thumbnail
		byte[] b = profileService.getProfileImage(ref.getId());
		
		if(b == null) {
			return null;
		}
		
		try {
			out.write(b);
			return new ActionReturn(out);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	public String[] getHandledOutputFormats() {
		return new String[] {Formats.XML, Formats.JSON};
	}

	public String[] getHandledInputFormats() {
		return new String[] {Formats.XML, Formats.JSON};
	}
		
	
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateEntity(EntityReference ref, Object entity) {
		// TODO Auto-generated method stub
	}

	public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		// TODO Auto-generated method stub
	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		// TODO Auto-generated method stub
	}
	
	public void deleteEntity(EntityReference ref) {
		// TODO Auto-generated method stub
	}

	public List<?> getEntities(EntityReference ref, Search search) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	
	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}
	
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	private ProfileService profileService;
	public void setProfileService(ProfileService profileService) {
		this.profileService = profileService;
	}


	

}
