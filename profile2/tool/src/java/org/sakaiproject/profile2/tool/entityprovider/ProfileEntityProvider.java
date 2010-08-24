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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityURLRedirect;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfileImageLogic;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.BasicConnection;
import org.sakaiproject.profile2.model.ProfileImage;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.Messages;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * This is the entity provider for a user's profile.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, AutoRegisterEntityProvider, RESTful, RequestAware {

	public final static String ENTITY_PREFIX = "profile";
	
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
		
	public boolean entityExists(String eid) {
		return true;
	}

	public Object getSampleEntity() {
		return new UserProfile();
	}
	
	public Object getEntity(EntityReference ref) {
	
		//convert input to uuid
		String uuid = sakaiProxy.ensureUuid(ref.getId());
		if(StringUtils.isBlank(uuid)) {
			throw new EntityNotFoundException("Invalid user.", ref.getId());
		}
		
		//get the full profile for the user, takes care of privacy checks against the current user
		UserProfile userProfile = profileLogic.getUserProfile(uuid);
		if(userProfile == null) {
			throw new EntityNotFoundException("Profile could not be retrieved for " + ref.getId(), ref.getReference());
		}
		return userProfile;
	}
	
	
	
	
	@EntityCustomAction(action="image",viewKey=EntityView.VIEW_SHOW)
	public Object getProfileImage(OutputStream out, EntityView view, Map<String,Object> params, EntityReference ref) {
		
		//convert input to uuid
		String uuid = sakaiProxy.ensureUuid(ref.getId());
		if(StringUtils.isBlank(uuid)) {
			throw new EntityNotFoundException("Invalid user.", ref.getId());
		}
		
		ProfileImage image = new ProfileImage();
		boolean wantsThumbnail = "thumb".equals(view.getPathSegment(3)) ? true : false;
		
		//optional siteid
		String siteId = (String)params.get("siteId");
		if(StringUtils.isNotBlank(siteId) && !sakaiProxy.checkForSite(siteId)){
			throw new EntityNotFoundException("Invalid siteId: " + siteId, ref.getReference());
		}
		
		//get thumb if requested - will fallback by default
		if(wantsThumbnail) {
			image = imageLogic.getProfileImage(uuid, null, null, ProfileConstants.PROFILE_IMAGE_THUMBNAIL, siteId);
		} else {
			image = imageLogic.getProfileImage(uuid, null, null, ProfileConstants.PROFILE_IMAGE_MAIN, siteId);
		}
		
		if(image == null) {
			throw new EntityNotFoundException("No profile image for " + ref.getId(), ref.getReference());
		}
		
		//check for binary
		final byte[] bytes = image.getBinary();
		if(bytes != null && bytes.length > 0) {
			try {
				out.write(bytes);
				ActionReturn actionReturn = new ActionReturn("BASE64", null, out);
				return actionReturn;
			} catch (IOException e) {
				throw new EntityException("Error retrieving profile image for " + ref.getId() + " : " + e.getMessage(), ref.getReference());
			}
		}
		
		
		String url = image.getUrl();
		if(StringUtils.isNotBlank(url)) {
			try {
				requestGetter.getResponse().sendRedirect(url);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	
			
	
	@EntityCustomAction(action="connections",viewKey=EntityView.VIEW_SHOW)
	public Object getConnections(EntityView view, EntityReference ref) {
		
		//convert input to uuid
		String uuid = sakaiProxy.ensureUuid(ref.getId());
		if(StringUtils.isBlank(uuid)) {
			throw new EntityNotFoundException("Invalid user.", ref.getId());
		}
		
		//get list of connections
		List<BasicConnection> connections = connectionsLogic.getBasicConnectionsForUser(uuid);
		if(connections == null) {
			throw new EntityException("Error retrieving connections for " + ref.getId(), ref.getReference());
		}
		ActionReturn actionReturn = new ActionReturn(connections);
		return actionReturn;
	}
		
	@EntityCustomAction(action="friendStatus",viewKey=EntityView.VIEW_SHOW)
	public Object getConnectionStatus(EntityReference ref, Map<String, Object> parameters) {
		
		//convert input to uuid (user making query)
		String uuid = sakaiProxy.ensureUuid(ref.getId());
		if(StringUtils.isBlank(uuid)) {
			throw new EntityNotFoundException("Invalid user.", ref.getId());
		}
		
		if (false == parameters.containsKey("friendId")) {
			throw new EntityNotFoundException("Parameter must be specified: friendId", ref.getId());
		}
		
		return connectionsLogic.getConnectionStatus(uuid, parameters.get("friendId").toString());
	}
	
	
	@EntityCustomAction(action="formatted",viewKey=EntityView.VIEW_SHOW)
	public Object getFormattedProfile(EntityReference ref) {
			
		//this allows a normal full profile to be returned formatted in HTML
		
		//get the full profile 
		UserProfile userProfile = (UserProfile) getEntity(ref);
		
		//convert UserProfile to HTML object
		String entity = getUserProfileAsHTML(userProfile);
		
		ActionReturn actionReturn = new ActionReturn("UTF-8", "text/html", entity);
		return actionReturn;
	}
	
	@EntityCustomAction(action="requestFriend",viewKey=EntityView.VIEW_SHOW)
	public Object requestFriend(EntityReference ref,Map<String,Object> params) {
		
		//convert input to uuid
		String uuid = sakaiProxy.ensureUuid(ref.getId());
		if(StringUtils.isBlank(uuid)) {
			throw new EntityNotFoundException("Invalid user.", ref.getId());
		}
		
		String friendId = (String) params.get("friendId");
		
		//get list of connections
		if(!connectionsLogic.requestFriend(uuid, friendId)) {
			throw new EntityException("Error requesting friend connection for " + ref.getId(), ref.getReference());
		}
		else
			return Messages.getString("Label.friend.requested");
	}
	
	@EntityCustomAction(action="removeFriend",viewKey=EntityView.VIEW_SHOW)
	public Object removeFriend(EntityReference ref,Map<String,Object> params) {
		
		//convert input to uuid
		String uuid = sakaiProxy.ensureUuid(ref.getId());
		if(StringUtils.isBlank(uuid)) {
			throw new EntityNotFoundException("Invalid user.", ref.getId());
		}
		
		String friendId = (String) params.get("friendId");
		
		//get list of connections
		if(!connectionsLogic.removeFriend(uuid, friendId)) {
			throw new EntityException("Error removing friend connection for " + ref.getId(), ref.getReference());
		}
		else
			return Messages.getString("Label.friend.add");
	}
	
	@EntityCustomAction(action="confirmFriendRequest",viewKey=EntityView.VIEW_SHOW)
	public Object confirmFriendRequest(EntityReference ref,Map<String,Object> params) {
		
		//convert input to uuid
		String uuid = sakaiProxy.ensureUuid(ref.getId());
		if(StringUtils.isBlank(uuid)) {
			throw new EntityNotFoundException("Invalid user.", ref.getId());
		}
		
		String friendId = (String) params.get("friendId");
		
		//get list of connections
		if(!connectionsLogic.confirmFriendRequest(friendId, uuid)) {
		//if(!connectionsLogic.confirmFriendRequest(uuid, friendId)) {
			throw new EntityException("Error confirming friend connection for " + ref.getId(), ref.getReference());
		}
		else
			return Messages.getString("Label.friend.remove");
	}
	
	@EntityCustomAction(action="ignoreFriendRequest",viewKey=EntityView.VIEW_SHOW)
	public Object ignoreFriendRequest(EntityReference ref,Map<String,Object> params) {
		
		//convert input to uuid
		String uuid = sakaiProxy.ensureUuid(ref.getId());
		if(StringUtils.isBlank(uuid)) {
			throw new EntityNotFoundException("Invalid user.", ref.getId());
		}
		
		String friendId = (String) params.get("friendId");
		
		//get list of connections
		if(!connectionsLogic.ignoreFriendRequest(uuid, friendId)) {
			throw new EntityException("Error ignoring friend connection for " + ref.getId(), ref.getReference());
		}
		else
			return Messages.getString("Label.friend.add");
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
			profileLogic.saveUserProfile(userProfile);
		} else {
			 throw new IllegalArgumentException("Invalid entity for update, must be UserProfile object");
		}	
	}
	
	
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		
		//reference will be the userUuid, which comes from the UserProfile
		String userUuid = null;
		
		if (entity.getClass().isAssignableFrom(UserProfile.class)) {
			UserProfile userProfile = (UserProfile) entity;
			
			if(profileLogic.saveUserProfile(userProfile)) {
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
	
	
	
	/**
	 * {@inheritDoc}
	 */
	private String getUserProfileAsHTML(UserProfile userProfile) {
		
		//note there is no birthday in this field. we need a good way to get the birthday without the year. 
		//maybe it needs to be stored in a separate field and treated differently. Or returned as a localised string.
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<script type=\"text/javascript\" src=\"/profile2-tool/javascript/profile2-eb.js\"></script>");
		
		sb.append("<div class=\"profile2-profile\">");
		
			sb.append("<div class=\"profile2-profile-image\">");
			sb.append("<img src=\"");
			sb.append(userProfile.getImageUrl());
			sb.append("\" />");
			sb.append("</div>");
		
		
		sb.append("<div class=\"profile2-profile-content\">");
		
		if(StringUtils.isNotBlank(userProfile.getUserUuid())) {
			sb.append("<div class=\"profile2-profile-userUuid\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.userUuid"));
			sb.append("</span>");
			sb.append(userProfile.getUserUuid());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getDisplayName())) {
			sb.append("<div class=\"profile2-profile-displayName\">");
			sb.append(userProfile.getDisplayName());
			sb.append("</div>");
		}
		
		if(!sakaiProxy.getCurrentUserId().equals(userProfile.getUserUuid())) {
			
			int connectionStatus = connectionsLogic.getConnectionStatus(sakaiProxy.getCurrentUserId(), userProfile.getUserUuid());
		
			if(connectionStatus == ProfileConstants.CONNECTION_CONFIRMED) {
				sb.append("<div id=\"profile_friend_" + userProfile.getUserUuid() + "\" class=\"icon connection-confirmed\"><a href=\"javascript:;\" onClick=\"return removeFriend('" + sakaiProxy.getCurrentUserId() + "','" + userProfile.getUserUuid() + "');\">" + Messages.getString("Label.friend.remove") + "</a></div>");
			}
			else if(connectionStatus == ProfileConstants.CONNECTION_REQUESTED) {
				sb.append("<div id=\"profile_friend_" + userProfile.getUserUuid() + "\" class=\"icon connection-request\">" + Messages.getString("Label.friend.requested") + "</div>");
			}
			else if(connectionStatus == ProfileConstants.CONNECTION_INCOMING) {
				sb.append("<div id=\"profile_friend_" + userProfile.getUserUuid() + "\" class=\"icon connection-request\">" + Messages.getString("Label.friend.requested") + "<a href=\"javascript:;\" title=\"" + Messages.getString("Label.friend.confirm") + "\" onClick=\"return confirmFriendRequest('" + sakaiProxy.getCurrentUserId() + "','" + userProfile.getUserUuid() + "');\"><img src=\"/library/image/silk/accept.png\"></a><a href=\"javascript:;\" title=\"" + Messages.getString("Label.friend.ignore") + "\" onClick=\"return ignoreFriendRequest('" + sakaiProxy.getCurrentUserId() + "','" + userProfile.getUserUuid() + "');\"><img src=\"/library/image/silk/cancel.png\"></a></div>");
			}
			else {
				sb.append("<div id=\"profile_friend_" + userProfile.getUserUuid() + "\" class=\"icon connection-add\"><a href=\"javascript:;\" onClick=\"return requestFriend('" + sakaiProxy.getCurrentUserId() + "','" + userProfile.getUserUuid() + "');\">" + Messages.getString("Label.friend.add") + "</a></div>");
			}
			
			sb.append("<br />");
		}
		
		//status
		if(userProfile.getStatus() != null) {
			if(StringUtils.isNotBlank(userProfile.getStatus().getMessage())) {
				sb.append("<div class=\"profile2-profile-statusMessage\">");
				sb.append(userProfile.getStatus().getMessage());
				sb.append("</div>");
			}
			
			if(StringUtils.isNotBlank(userProfile.getStatus().getDateFormatted())) {
				sb.append("<div class=\"profile2-profile-statusDate\">");
				sb.append(userProfile.getStatus().getDateFormatted());
				sb.append("</div>");
			}
		}
		
		//basic info
		if(StringUtils.isNotBlank(userProfile.getNickname())) {
			sb.append("<div class=\"profile2-profile-nickname\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.nickname"));
			sb.append("</span>");
			sb.append(userProfile.getNickname());
			sb.append("</div>");
		}
		if(StringUtils.isNotBlank(userProfile.getPersonalSummary())) {
			sb.append("<div class=\"profile2-profile-personalSummary\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.personalSummary"));
			sb.append("</span>");
			
			//PRFL-389 abbreviate long personal summary
			int maxLength = Integer.parseInt(sakaiProxy.getServerConfigurationParameter("profile2.formatted.profile.summary.max", ProfileConstants.FORMATTED_PROFILE_SUMMARY_MAX_LENGTH));
			sb.append(StringUtils.abbreviate(userProfile.getPersonalSummary(), maxLength));
			
			sb.append("</div>");
		}
		
		
		//contact info
		if(StringUtils.isNotBlank(userProfile.getEmail())) {
			sb.append("<div class=\"profile2-profile-email\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.email"));
			sb.append("</span>");
			sb.append(userProfile.getEmail());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getHomepage())) {
			sb.append("<div class=\"profile2-profile-homepage\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.homepage"));
			sb.append("</span>");
			sb.append(userProfile.getHomepage());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getHomephone())) {
			sb.append("<div class=\"profile2-profile-homephone\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.homephone"));
			sb.append("</span>");
			sb.append(userProfile.getHomephone());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getWorkphone())) {
			sb.append("<div class=\"profile2-profile-workphone\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.workphone"));
			sb.append("</span>");
			sb.append(userProfile.getWorkphone());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getMobilephone())) {
			sb.append("<div class=\"profile2-profile-mobilephone\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.mobilephone"));
			sb.append("</span>");
			sb.append(userProfile.getMobilephone());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getFacsimile())) {
			sb.append("<div class=\"profile2-profile-facsimile\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.facsimile"));
			sb.append("</span>");
			sb.append(userProfile.getFacsimile());
			sb.append("</div>");
		}
		
		
		
		//academic info
		if(StringUtils.isNotBlank(userProfile.getPosition())) {
			sb.append("<div class=\"profile2-profile-position\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.position"));
			sb.append("</span>");
			sb.append(userProfile.getPosition());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getDepartment())) {
			sb.append("<div class=\"profile2-profile-department\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.department"));
			sb.append("</span>");
			sb.append(userProfile.getDepartment());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getSchool())) {
			sb.append("<div class=\"profile2-profile-school\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.school"));
			sb.append("</span>");
			sb.append(userProfile.getSchool());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getRoom())) {
			sb.append("<div class=\"profile2-profile-room\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.room"));
			sb.append("</span>");
			sb.append(userProfile.getRoom());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getCourse())) {
			sb.append("<div class=\"profile2-profile-course\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.course"));
			sb.append("</span>");
			sb.append(userProfile.getCourse());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getSubjects())) {
			sb.append("<div class=\"profile2-profile-subjects\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.subjects"));
			sb.append("</span>");
			sb.append(userProfile.getSubjects());
			sb.append("</div>");
		}
		
		
		//personal info
		if(StringUtils.isNotBlank(userProfile.getFavouriteBooks())) {
			sb.append("<div class=\"profile2-profile-favouriteBooks\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.favouriteBooks"));
			sb.append("</span>");
			sb.append(userProfile.getFavouriteBooks());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getFavouriteTvShows())) {
			sb.append("<div class=\"profile2-profile-favouriteTvShows\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.favouriteTvShows"));
			sb.append("</span>");
			sb.append(userProfile.getFavouriteTvShows());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getFavouriteMovies())) {
			sb.append("<div class=\"profile2-profile-favouriteMovies\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.favouriteMovies"));
			sb.append("</span>");
			sb.append(userProfile.getFavouriteMovies());
			sb.append("</div>");
		}
		
		if(StringUtils.isNotBlank(userProfile.getFavouriteQuotes())) {
			sb.append("<div class=\"profile2-profile-favouriteQuotes\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.favouriteQuotes"));
			sb.append("</span>");

			sb.append(userProfile.getFavouriteQuotes());
			sb.append("</div>");
		}
		
		sb.append("</div>");
		sb.append("</div>");
		
		//add the stylesheet
		sb.append("<link href=\"");
		sb.append(ProfileConstants.ENTITY_CSS_PROFILE);
		sb.append("\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />");
		
		return sb.toString();
	}
	
	
	
	
	
	
	

	

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		// TODO Auto-generated method stub
		
	}

	public List<?> getEntities(EntityReference ref, Search search) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	
	private RequestGetter requestGetter;
	public void setRequestGetter(RequestGetter requestGetter) {
		this.requestGetter = requestGetter;
	}

	
	
	public String[] getHandledOutputFormats() {
		return new String[] {Formats.XML, Formats.JSON};
	}

	public String[] getHandledInputFormats() {
		return new String[] {Formats.XML, Formats.JSON, Formats.HTML};
	}
	
	
	
	
	
	
		
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	private ProfileLogic profileLogic;
	public void setProfileLogic(ProfileLogic profileLogic) {
		this.profileLogic = profileLogic;
	}
	
	private ProfileConnectionsLogic connectionsLogic;
	public void setConnectionsLogic(ProfileConnectionsLogic connectionsLogic) {
		this.connectionsLogic = connectionsLogic;
	}
	
	private ProfileImageLogic imageLogic;
	public void setImageLogic(ProfileImageLogic imageLogic) {
		this.imageLogic = imageLogic;
	}

	
	
	

}
