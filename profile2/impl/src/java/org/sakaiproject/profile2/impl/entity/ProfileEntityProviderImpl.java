package org.sakaiproject.profile2.impl.entity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityURLRedirect;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;

import org.sakaiproject.profile2.api.ProfileConstants;
import org.sakaiproject.profile2.api.ProfileService;
import org.sakaiproject.profile2.api.entity.ProfileEntityProvider;
import org.sakaiproject.profile2.api.entity.model.Connection;
import org.sakaiproject.profile2.api.entity.model.UserProfile;
import org.sakaiproject.profile2.api.model.ResourceWrapper;

public class ProfileEntityProviderImpl implements ProfileEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, RESTful {

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
		
	public boolean entityExists(String eid) {
		//check the user is valid. if it is then return true as everyone has a 'profile'.
		//note that we DO NOT check if they have an actual profile, just if they exist.
		return profileService.checkUserExists(eid);
	}

	public Object getSampleEntity() {
		UserProfile userProfile = profileService.getPrototype();
		return userProfile;
	}
	
	public Object getEntity(EntityReference ref) {
	
		//get the full profile for the user. takes care of privacy checks against the current user
		UserProfile userProfile = profileService.getFullUserProfile(ref.getId());
		if(userProfile == null) {
			throw new EntityNotFoundException("Profile could not be retrieved for " + ref.getId(), ref.getReference());
		}
		return userProfile;
	}
	
	
	
	
	@EntityCustomAction(action="minimal",viewKey=EntityView.VIEW_SHOW)
	public Object getMinimalProfile(EntityReference ref, EntityView view) {
			
		boolean wantsFormatted = "formatted".equals(view.getPathSegment(3)) ? true : false;
		
		//get the minimal profile, with privacy checks against the requesting user
		UserProfile userProfile = profileService.getMinimalUserProfile(ref.getId());
		if(userProfile == null) {
			throw new EntityException("Profile could not be retrieved for " + ref.getId(), ref.getReference());
		}
		
		//if want formatted, convert and return as HTML, otherwise return the entity.
		if(wantsFormatted) {
			String formattedProfile = profileService.getUserProfileAsHTML(userProfile);
			ActionReturn actionReturn = new ActionReturn(Formats.UTF_8, Formats.HTML_MIME_TYPE, formattedProfile);
			return actionReturn;
		} else {
			return userProfile;
		}
	}
	
	
	@EntityCustomAction(action="academic",viewKey=EntityView.VIEW_SHOW)
	public Object getAcademicProfile(EntityReference ref, EntityView view) {
			
		boolean wantsFormatted = "formatted".equals(view.getPathSegment(3)) ? true : false;
		
		//get the academic profile, with privacy checks against the requesting user
		UserProfile userProfile = profileService.getAcademicUserProfile(ref.getId());
		if(userProfile == null) {
			throw new EntityException("Profile could not be retrieved for " + ref.getId(), ref.getReference());
		}
		
		//if want formatted, convert and return as HTML, otherwise return the entity.
		if(wantsFormatted) {
			String formattedProfile = profileService.getUserProfileAsHTML(userProfile);
			ActionReturn actionReturn = new ActionReturn(Formats.UTF_8, Formats.HTML_MIME_TYPE, formattedProfile);
			return actionReturn;
		} else {
			return userProfile;
		}
	}
	
	
	
	
	@EntityCustomAction(action="image",viewKey=EntityView.VIEW_SHOW)
	public Object getMainImage(OutputStream out, EntityView view, EntityReference ref) {
		
		ResourceWrapper resource = new ResourceWrapper();
		
		boolean wantsThumbnail = "thumb".equals(view.getPathSegment(3)) ? true : false;
		
		//get thumb if requested - will fallback by default
		if(wantsThumbnail) {
			resource = profileService.getProfileImage(ref.getId(), ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
		} else {
			resource = profileService.getProfileImage(ref.getId(),ProfileConstants.PROFILE_IMAGE_MAIN);
		}
		
		if(resource == null || resource.getBytes() == null) {
			throw new EntityNotFoundException("No profile image for " + ref.getId(), ref.getReference());
		}
		
		try {
			out.write(resource.getBytes());
			
			ActionReturn actionReturn = new ActionReturn("BASE64", resource.getMimeType(), out);
		
			return actionReturn;
		} catch (IOException e) {
			throw new EntityException("Error retrieving profile image for " + ref.getId() + " : " + e.getMessage(), ref.getReference());
		}
	}
	
	
			
	
	@EntityCustomAction(action="connections",viewKey=EntityView.VIEW_SHOW)
	public Object getConnections(EntityView view, EntityReference ref) {
				
		//get list of connections
		List<Connection> connections = profileService.getConnectionsForUser(ref.getId());
		if(connections == null) {
			throw new EntityException("Error retrieving connections for " + ref.getId(), ref.getReference());
		}
		ActionReturn actionReturn = new ActionReturn(connections);
		return actionReturn;
	}
	
	
	
	@EntityCustomAction(action="formatted",viewKey=EntityView.VIEW_SHOW)
	public Object getFormattedProfile(EntityReference ref) {
			
		//this allows a normal full profile to be returned formatted in HTML
		
		//get the full profile 
		UserProfile userProfile = (UserProfile) getEntity(ref);
		
		//convert UserProfile to HTML object
		String entity = profileService.getUserProfileAsHTML(userProfile);
		
		ActionReturn actionReturn = new ActionReturn("UTF-8", "text/html", entity);
		return actionReturn;
	}
	
	@EntityURLRedirect("/{prefix}/{id}/account")
	public String redirectUserAccount(Map<String,String> vars) {
		return "user/" + vars.get("id") + vars.get(TemplateParseUtil.DOT_EXTENSION);
	}

	
	
	public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		
		String userId = ref.getId();
		if (StringUtils.isBlank(userId)) {
			throw new IllegalArgumentException("Cannot update, No userId in provided reference: " + ref);
		}
		
		if (entity.getClass().isAssignableFrom(UserProfile.class)) {
			UserProfile userProfile = (UserProfile) entity;
			profileService.save(userProfile);
		} else {
			 throw new IllegalArgumentException("Invalid entity for update, must be UserProfile object");
		}
	}
	
	
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		
		//reference will be the userUuid, which comes from the UserProfile
		String userUuid = null;

		
		if (entity.getClass().isAssignableFrom(UserProfile.class)) {
			UserProfile userProfile = (UserProfile) entity;
			
			if(profileService.create(userProfile)) {
				userUuid = userProfile.getUserUuid();
			}
			if(userUuid == null) {
				throw new EntityException("Could not create entity", ref.getReference());
			}
		} else {
			 throw new IllegalArgumentException("Invalid entity for create, must be UserProfile object");
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
		return new String[] {Formats.XML, Formats.JSON, Formats.FORM};
	}

	public String[] getHandledInputFormats() {
		return new String[] {Formats.XML, Formats.JSON, Formats.HTML, Formats.FORM};
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
