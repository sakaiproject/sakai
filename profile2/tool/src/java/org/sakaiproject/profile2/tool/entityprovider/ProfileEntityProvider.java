/**
 * Copyright (c) 2008-2012 The Sakai Foundation
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityURLRedirect;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Redirectable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Sampleable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfileImageLogic;
import org.sakaiproject.profile2.logic.ProfileLinkLogic;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.ProfileMessagingLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.BasicConnection;
import org.sakaiproject.profile2.model.BasicPerson;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfileImage;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.Messages;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

/**
 * This is the entity provider for a user's profile.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
@Slf4j
public class ProfileEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, AutoRegisterEntityProvider, Outputable, Resolvable, Sampleable, Describeable, Redirectable, ActionsExecutable, RequestAware {

	public final static String ENTITY_PREFIX = "profile";
	
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
		return new UserProfile();
	}
	
	@Override
	public Object getEntity(EntityReference ref) {
	
		//convert input to uuid
		String uuid = sakaiProxy.ensureUuid(ref.getId());
		if(StringUtils.isBlank(uuid)) {
			throw new EntityNotFoundException("Invalid user.", ref.getId());
		}
		
		//check for siteId in the request
		String siteId = requestGetter.getRequest().getParameter("siteId");
		
		//get the full profile for the user, takes care of privacy checks against the current user
		UserProfile userProfile = profileLogic.getUserProfile(uuid, siteId);
		if(userProfile == null) {
			throw new EntityNotFoundException("Profile could not be retrieved for " + ref.getId(), ref.getReference());
		}
		return userProfile;
	}
	
	
	
	
	@EntityCustomAction(action="image",viewKey=EntityView.VIEW_SHOW)
	public Object getProfileImage(OutputStream out, EntityView view, Map<String,Object> params, EntityReference ref) {
		
		final String id = ref.getId();

        final boolean wantsBlank = id.equals(ProfileConstants.BLANK);

        String uuid = "";

        if(!wantsBlank) {
		    //convert input to uuid
		    uuid = sakaiProxy.ensureUuid(ref.getId());
            if(StringUtils.isBlank(uuid)) {
                throw new EntityNotFoundException("Invalid user.", ref.getId());
            }
        }
		
		ProfileImage image = null;
		final boolean wantsThumbnail = StringUtils.equals("thumb", view.getPathSegment(3)) ? true : false;
		
		boolean wantsAvatar = false;
		if(!wantsThumbnail) {
			wantsAvatar = StringUtils.equals("avatar", view.getPathSegment(3)) ? true : false;
		}
		
		final boolean wantsOfficial = StringUtils.equals("official", view.getPathSegment(3)) ? true : false;

		if(log.isDebugEnabled()) {
			log.debug("wantsThumbnail:" + wantsThumbnail);
			log.debug("wantsAvatar:" + wantsAvatar);
			log.debug("wantsOfficial:" + wantsOfficial);
			log.debug("wantsBlank:" + wantsBlank);
		}
		
		//optional siteid
		final String siteId = (String)params.get("siteId");
		if(StringUtils.isNotBlank(siteId) && !sakaiProxy.checkForSite(siteId)){
			throw new EntityNotFoundException("Invalid siteId: " + siteId, ref.getReference());
		}
		
        if(wantsBlank) {
            image = imageLogic.getBlankProfileImage();
        } else {
		    //get thumb or avatar if requested - or fallback
            if(wantsThumbnail) {
                image = imageLogic.getProfileImage(uuid, null, null, ProfileConstants.PROFILE_IMAGE_THUMBNAIL, siteId);
            } 
            if(!wantsThumbnail && wantsAvatar) {
                image = imageLogic.getProfileImage(uuid, null, null, ProfileConstants.PROFILE_IMAGE_AVATAR, siteId);
            }
            if(!wantsThumbnail && !wantsAvatar) {
                image = imageLogic.getProfileImage(uuid, null, null, ProfileConstants.PROFILE_IMAGE_MAIN, siteId);
            }
            if(wantsOfficial) {
			    image = imageLogic.getOfficialProfileImage(uuid, siteId);
		    }
        }
		
		if(image == null) {
			throw new EntityNotFoundException("No profile image for " + id, ref.getReference());
		}
		
		//check for binary
		final byte[] bytes = image.getBinary();
		if(bytes != null && bytes.length > 0) {
			try {
				out.write(bytes);
				ActionReturn actionReturn = new ActionReturn("UTF-8", image.getMimeType(), out);
				
				Map<String,String> headers = new HashMap<>();
				headers.put("Expires", "Mon, 01 Jan 2001 00:00:00 GMT");
				headers.put("Cache-Control","no-cache, must-revalidate, max-age=0");
				headers.put("Pragma", "no-cache");
				
				actionReturn.setHeaders(headers);
				
				return actionReturn;
			} catch (IOException e) {
				throw new EntityException("Error retrieving profile image for " + id + " : " + e.getMessage(), ref.getReference());
			}
		}
		
		final String url = image.getUrl();
		if(StringUtils.isNotBlank(url)) {
			try {
				HttpServletResponse res = requestGetter.getResponse();
				res.addHeader("Expires", "Mon, 01 Jan 2001 00:00:00 GMT");
				res.addHeader("Cache-Control","no-cache, must-revalidate, max-age=0");
				res.addHeader("Pragma", "no-cache");
				res.sendRedirect(url);
			} catch (IOException e) {
				throw new EntityException("Error redirecting to external image for " + id + " : " + e.getMessage(), ref.getReference());
			}
		}
		
		return null;
	}
	
	
			
	
	@EntityCustomAction(action="connections",viewKey=EntityView.VIEW_SHOW)
	public Object getConnections(EntityView view, EntityReference ref) {
		
		if(!sakaiProxy.isLoggedIn()) {
			throw new SecurityException("You must be logged in to get a connection list.");
		}
		
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
		return new ActionReturn(connections);
	}
		
	@EntityCustomAction(action="friendStatus",viewKey=EntityView.VIEW_SHOW)
	public Object getConnectionStatus(EntityReference ref, Map<String, Object> parameters) {
		
		if(!sakaiProxy.isLoggedIn()) {
			throw new SecurityException("You must be logged in to get a friend status record.");
		}
		
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

	@EntityCustomAction(action="unreadMessagesCount",viewKey=EntityView.VIEW_SHOW)
	public Object getUnreadMessagesCount(EntityReference ref) {

		if (!sakaiProxy.isLoggedIn()) {
			throw new SecurityException("You must be logged in to get the unread messages count.");
		}

		//convert input to uuid
		String uuid = sakaiProxy.ensureUuid(ref.getId());
		if (StringUtils.isBlank(uuid)) {
			throw new EntityNotFoundException("Invalid user.", ref.getId());
		}
        
		if (sakaiProxy.isAdminUser() || sakaiProxy.getCurrentUserId().equals(uuid)) {
			return new ActionReturn(messagingLogic.getAllUnreadMessagesCount(uuid));
		} else {
			throw new SecurityException("You can only view your own message count.");
		}
	}
	
	@EntityCustomAction(action="formatted",viewKey=EntityView.VIEW_SHOW)
	public Object getFormattedProfile(EntityReference ref, EntityView view) {
			
		//this allows a normal full profile to be returned formatted in HTML
		
		final boolean wantsOfficial = StringUtils.equals("official", view.getPathSegment(3)) ? true : false;
		
		//get the full profile 
		UserProfile userProfile = (UserProfile) getEntity(ref);

		//Check for siteId in the request
		String siteId = requestGetter.getRequest().getParameter("siteId");
		
		//convert UserProfile to HTML object
		String formattedProfile = getUserProfileAsHTML(userProfile, siteId, wantsOfficial);
		
		//ActionReturn actionReturn = new ActionReturn("UTF-8", "text/html", entity);
		ActionReturn actionReturn = new ActionReturn(Formats.UTF_8, Formats.HTML_MIME_TYPE, formattedProfile);
		return actionReturn;
	}
	
	@EntityCustomAction(action="requestFriend",viewKey=EntityView.VIEW_SHOW)
	public Object requestFriend(EntityReference ref,Map<String,Object> params) {
		
		if(!sakaiProxy.isLoggedIn()) {
			throw new SecurityException("You must be logged in to make a connection request.");
		}
		
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
		
		if(!sakaiProxy.isLoggedIn()) {
			throw new SecurityException("You must be logged in to remove a connection.");
		}
		
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
		
		if(!sakaiProxy.isLoggedIn()) {
			throw new SecurityException("You must be logged in to confirm a connection request.");
		}
		
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
		
		if(!sakaiProxy.isLoggedIn()) {
			throw new SecurityException("You must be logged in to ignore a connection request.");
		}
		
		//convert input to uuid
		String uuid = sakaiProxy.ensureUuid(ref.getId());
		if(StringUtils.isBlank(uuid)) {
			throw new EntityNotFoundException("Invalid user.", ref.getId());
		}
		
		String friendId = (String) params.get("friendId");
		
		//we're ignoring a request FROM the friendId TO the uuid
		if(!connectionsLogic.ignoreFriendRequest(friendId, uuid)) {
			throw new EntityException("Error ignoring friend connection for " + ref.getId(), ref.getReference());
		}
		else
			return Messages.getString("Label.friend.add");
	}

    @EntityCustomAction(action="incomingConnectionRequests", viewKey=EntityView.VIEW_SHOW)
	public Object getIncomingConnectionRequests(EntityView view, EntityReference ref) {

		if(!sakaiProxy.isLoggedIn()) {
			throw new SecurityException("You must be logged in to get the incoming connection list.");
		}

		//convert input to uuid
		String uuid = sakaiProxy.ensureUuid(ref.getId());
		if (StringUtils.isBlank(uuid)) {
			throw new EntityNotFoundException("Invalid user.", ref.getId());
		}
		
		final List<BasicConnection> requests
			= connectionsLogic.getConnectionRequestsForUser(uuid).stream().map(p -> {
							BasicConnection bc = new BasicConnection();
							bc.setUuid(p.getUuid());
							bc.setDisplayName(p.getDisplayName());
							bc.setEmail(p.getProfile().getEmail());
                            bc.setProfileUrl(linkLogic.getInternalDirectUrlToUserProfile(p.getUuid()));
							bc.setType(p.getType());
							bc.setSocialNetworkingInfo(p.getProfile().getSocialInfo());
							return bc;
				}).collect(Collectors.toList());

		if (requests == null) {
			throw new EntityException("Error retrieving connection requests for " + ref.getId(), ref.getReference());
		}
		return new ActionReturn(requests);
	}

	@EntityCustomAction(action="outgoingConnectionRequests", viewKey=EntityView.VIEW_SHOW)
	public Object getOutgoingConnectionRequests(EntityView view, EntityReference ref) {

		if (!sakaiProxy.isLoggedIn()) {
			throw new SecurityException("You must be logged in to get the outgoing connection list.");
		}

		//convert input to uuid
		String uuid = sakaiProxy.ensureUuid(ref.getId());
		if (StringUtils.isBlank(uuid)) {
			throw new EntityNotFoundException("Invalid user.", ref.getId());
		}

		final List<BasicConnection> requests
			= connectionsLogic.getOutgoingConnectionRequestsForUser(uuid).stream().map(p -> {
							BasicConnection bc = new BasicConnection();
							bc.setUuid(p.getUuid());
							bc.setDisplayName(p.getDisplayName());
							bc.setEmail(p.getProfile().getEmail());
                            bc.setProfileUrl(linkLogic.getInternalDirectUrlToUserProfile(p.getUuid()));
							bc.setType(p.getType());
							bc.setSocialNetworkingInfo(p.getProfile().getSocialInfo());
							return bc;
				}).collect(Collectors.toList());

		if (requests == null) {
			throw new EntityException("Error retrieving outgoing connection requests for " + uuid, ref.getReference());
		}

		return new ActionReturn(requests);
	}

	@EntityURLRedirect("/{prefix}/{id}/account")
	public String redirectUserAccount(Map<String,String> vars) {
		return "user/" + vars.get("id") + vars.get(TemplateParseUtil.DOT_EXTENSION);
	}

	
	/**
	 * {@inheritDoc}
	 */
	private String getUserProfileAsHTML(UserProfile userProfile, String siteId, boolean official) {
		
		//note there is no birthday in this field. we need a good way to get the birthday without the year. 
		//maybe it needs to be stored in a separate field and treated differently. Or returned as a localised string.
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<script type=\"text/javascript\" src=\"/profile2-tool/javascript/profile2-eb.js\"></script>");
		
		sb.append("<div class=\"profile2-profile\">");
		
			sb.append("<div class=\"profile2-profile-image\">");
			sb.append("<img src=\"");
			if (official) {
				sb.append(imageLogic.getOfficialProfileImage(userProfile.getUserUuid(), siteId).getUrl());
			} else {
				sb.append(userProfile.getImageUrl());
			}
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
		
		String displayName = userProfile.getDisplayName();
		if(StringUtils.isNotBlank(displayName)) {
			sb.append("<div class=\"profile2-profile-displayName\">");
			sb.append(StringEscapeUtils.escapeHtml(displayName));
			sb.append("</div>");
		}
		
		//status
		if(userProfile.getStatus() != null) {
			String message = userProfile.getStatus().getMessage();
			if(StringUtils.isNotBlank(message)) {
				sb.append("<div class=\"profile2-profile-statusMessage\">");
				sb.append(StringEscapeUtils.escapeHtml(message));
				sb.append("</div>");
			}
			
			if(StringUtils.isNotBlank(userProfile.getStatus().getDateFormatted())) {
				sb.append("<div class=\"profile2-profile-statusDate\">");
				sb.append(userProfile.getStatus().getDateFormatted());
				sb.append("</div>");
			}
		}
		
		if(StringUtils.isNotBlank(userProfile.getUserUuid())) {
			sb.append("<div class=\"icon profile-image\">");
			
			sb.append("<div class=\"profile2-profile-view-full\">");
			sb.append("<a href=\"javascript:;\" onclick=\"window.open('" +
					linkLogic.getInternalDirectUrlToUserProfile(userProfile.getUserUuid()) +
					"','','resizable=yes,scrollbars=yes')\">" +
					Messages.getString("profile.view.full") + "</a>");
			sb.append("</div>");
			sb.append("</div>");
		}
		
		if(sakaiProxy.isConnectionsEnabledGlobally() && !sakaiProxy.getCurrentUserId().equals(userProfile.getUserUuid())) {
			
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
		
		//basic info
		
		String nickname = userProfile.getNickname();
		if(StringUtils.isNotBlank(nickname)) {
			sb.append("<div class=\"profile2-profile-nickname\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.nickname"));
			sb.append("</span>");
			sb.append(StringEscapeUtils.escapeHtml(nickname).toString());
			sb.append("</div>");
		}
		if(StringUtils.isNotBlank(userProfile.getPersonalSummary())) {
			sb.append("<div class=\"profile2-profile-personalSummary\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.personalSummary"));
			sb.append("</span>");
			
			//PRFL-389 abbreviate long personal summary
			int maxLength = Integer.parseInt(sakaiProxy.getServerConfigurationParameter("profile2.formatted.profile.summary.max", ProfileConstants.FORMATTED_PROFILE_SUMMARY_MAX_LENGTH));
			sb.append(ProfileUtils.truncateAndAbbreviate(ProfileUtils.processHtml(userProfile.getPersonalSummary()), maxLength, true));
			
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
		String position = userProfile.getPosition();
		if(StringUtils.isNotBlank(position)) {
			sb.append("<div class=\"profile2-profile-position\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.position"));
			sb.append("</span>");
			sb.append(StringEscapeUtils.escapeHtml(position));
			sb.append("</div>");
		}
		
		String department = userProfile.getDepartment();
		if(StringUtils.isNotBlank(department)) {
			sb.append("<div class=\"profile2-profile-department\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.department"));
			sb.append("</span>");
			sb.append(StringEscapeUtils.escapeHtml(department));
			sb.append("</div>");
		}
		
		String school = userProfile.getSchool();
		if(StringUtils.isNotBlank(school)) {
			sb.append("<div class=\"profile2-profile-school\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.school"));
			sb.append("</span>");
			sb.append(StringEscapeUtils.escapeHtml(school));
			sb.append("</div>");
		}
		
		String room = userProfile.getRoom();
		if(StringUtils.isNotBlank(room)) {
			sb.append("<div class=\"profile2-profile-room\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.room"));
			sb.append("</span>");
			sb.append(StringEscapeUtils.escapeHtml(room));
			sb.append("</div>");
		}
		
		String course = userProfile.getCourse();
		if(StringUtils.isNotBlank(course)) {
			sb.append("<div class=\"profile2-profile-course\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.course"));
			sb.append("</span>");
			sb.append(StringEscapeUtils.escapeHtml(course));
			sb.append("</div>");
		}
		
		String subjects = userProfile.getSubjects();
		if(StringUtils.isNotBlank(subjects)) {
			sb.append("<div class=\"profile2-profile-subjects\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.subjects"));
			sb.append("</span>");
			sb.append(StringEscapeUtils.escapeHtml(subjects));
			sb.append("</div>");
		}
		
		
		//personal info
		String favouriteBooks = userProfile.getFavouriteBooks();
		if(StringUtils.isNotBlank(favouriteBooks)) {
			sb.append("<div class=\"profile2-profile-favouriteBooks\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.favouriteBooks"));
			sb.append("</span>");
			sb.append(StringEscapeUtils.escapeHtml(favouriteBooks));
			sb.append("</div>");
		}
		
		String favouriteTvShows = userProfile.getFavouriteTvShows();
		if(StringUtils.isNotBlank(favouriteTvShows)) {
			sb.append("<div class=\"profile2-profile-favouriteTvShows\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.favouriteTvShows"));
			sb.append("</span>");
			sb.append(StringEscapeUtils.escapeHtml(favouriteTvShows));
			sb.append("</div>");
		}
		
		String favouriteMovies = userProfile.getFavouriteMovies();
		if(StringUtils.isNotBlank(favouriteMovies)) {
			sb.append("<div class=\"profile2-profile-favouriteMovies\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.favouriteMovies"));
			sb.append("</span>");
			sb.append(StringEscapeUtils.escapeHtml(favouriteMovies));
			sb.append("</div>");
		}
		
		String favouriteQuotes = userProfile.getFavouriteQuotes();
		if(StringUtils.isNotBlank(favouriteQuotes)) {
			sb.append("<div class=\"profile2-profile-favouriteQuotes\">");
			sb.append("<span class=\"profile2-profile-label\">");
			sb.append(Messages.getString("Label.favouriteQuotes"));
			sb.append("</span>");
			sb.append(StringEscapeUtils.escapeHtml(favouriteQuotes));
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
	
	
	
	
	
	
	@Override
	public String[] getHandledOutputFormats() {
		return new String[] {Formats.HTML, Formats.XML, Formats.JSON};
	}

	
	@Setter
	private RequestGetter requestGetter;
	
	
	@Setter
	private SakaiProxy sakaiProxy;
	
	@Setter
	private ProfileLogic profileLogic;
	
	@Setter	
	private ProfileConnectionsLogic connectionsLogic;
	
	@Setter	
	private ProfileImageLogic imageLogic;
	
	@Setter	
	private ProfileLinkLogic linkLogic;

	@Setter	
	private ProfileMessagingLogic messagingLogic;
	
}
