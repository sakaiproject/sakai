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
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.tool.api.SessionManager;

import uk.ac.lancs.e_science.profile2.api.ProfileImageManager;
import uk.ac.lancs.e_science.profile2.api.ProfileService;
import uk.ac.lancs.e_science.profile2.api.entity.ProfileEntityProvider;
import uk.ac.lancs.e_science.profile2.api.entity.model.Connection;
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
	
		//check auth
		if (sessionManager.getCurrentSessionUserId() == null) {
			throw new SecurityException();
		}
		
		//get the full profile, with privacy checks against the requesting user
		UserProfile entity = profileService.getFullUserProfile(ref.getId(), sessionManager.getCurrentSessionUserId());
		if(entity == null) {
			throw new EntityNotFoundException("Profile could not be retrieved for " + ref.getId(), ref.getReference());
		}
		return entity;
	}
	
	@EntityCustomAction(action="minimal",viewKey=EntityView.VIEW_SHOW)
	public Object getMinimalProfile(EntityReference ref) {
		
		//check auth
		if (sessionManager.getCurrentSessionUserId() == null) {
			throw new SecurityException();
		}
		
		//get the minimal profile, with privacy checks against the requesting user
		UserProfile entity = profileService.getMinimalUserProfile(ref.getId(), sessionManager.getCurrentSessionUserId());
		if(entity == null) {
			throw new EntityException("Profile could not be retrieved for " + ref.getId(), ref.getReference());
		}
		return entity;
	}
	
	@EntityCustomAction(action="image",viewKey=EntityView.VIEW_SHOW)
	public Object getMainImage(OutputStream out, EntityView view, EntityReference ref) {
		
		//check auth
		if (sessionManager.getCurrentSessionUserId() == null) {
			throw new SecurityException();
		}
		
		//get main profile image. 
		byte[] b = profileService.getProfileImage(ref.getId(), sessionManager.getCurrentSessionUserId(), ProfileImageManager.PROFILE_IMAGE_MAIN);
		
		if(b == null) {
			throw new EntityNotFoundException("No profile image for " + ref.getId(), ref.getReference());
		}
		
		try {
			out.write(b);
			return new ActionReturn(out);
		} catch (IOException e) {
			throw new EntityException("Error retrieving profile image for: " + ref.getId() + " : " + e.getMessage(), ref.getReference());
		}
	}
	
	@EntityCustomAction(action="imagethumb",viewKey=EntityView.VIEW_SHOW)
	public Object getThumbnailImage(OutputStream out, EntityView view, EntityReference ref) {
		
		//check auth
		if (sessionManager.getCurrentSessionUserId() == null) {
			throw new SecurityException();
		}
		
		//get thumbnail profile image. 
		byte[] b = profileService.getProfileImage(ref.getId(), sessionManager.getCurrentSessionUserId(), ProfileImageManager.PROFILE_IMAGE_THUMBNAIL);
		
		if(b == null) {
			throw new EntityNotFoundException("No thumbnail image for " + ref.getId(), ref.getReference());
		}
		
		try {
			out.write(b);
			return new ActionReturn(out);
		} catch (IOException e) {
			throw new EntityException("Error retrieving thumbnail image for: " + ref.getId() + " : " + e.getMessage(), ref.getReference());
		}
	}
	
	@EntityCustomAction(action="connections",viewKey=EntityView.VIEW_SHOW)
	public Object getConnections(EntityView view, EntityReference ref) {
		
		//check auth
		if (sessionManager.getCurrentSessionUserId() == null) {
			throw new SecurityException();
		}
		
		//get list of connections
		List<Connection> connections = profileService.getConnectionsForUser(ref.getId(), sessionManager.getCurrentSessionUserId());
		if(connections == null) {
			throw new EntityException("Error retrieving connections for: " + ref.getId(), ref.getReference());
		}
		ActionReturn actionReturn = new ActionReturn(connections);
		return actionReturn;
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
