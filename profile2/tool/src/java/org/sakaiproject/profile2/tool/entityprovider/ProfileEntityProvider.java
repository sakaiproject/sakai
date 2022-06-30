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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import org.sakaiproject.profile2.model.MimeTypeByteArray;
import org.sakaiproject.profile2.model.ProfileImage;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.Messages;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ResourceRegionHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;

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

	@EntityCustomAction(action="pronunciation",viewKey=EntityView.VIEW_SHOW)
	public Object getNamePronunciation(OutputStream out, EntityView view, Map<String,Object> params, EntityReference ref) {
		if (!sakaiProxy.isLoggedIn()) {
			throw new SecurityException("You must be logged in to get the name pronunciation of the student.");
		}
		String uuid = sakaiProxy.ensureUuid(ref.getId());
		if(StringUtils.isBlank(uuid)) {
			throw new EntityNotFoundException("Invalid user.", ref.getId());
		}
		
		MimeTypeByteArray mtba = profileLogic.getUserNamePronunciation(uuid);
		if(mtba != null && mtba.getBytes() != null) {
			try {
				HttpServletResponse response = requestGetter.getResponse();
				HttpServletRequest request = requestGetter.getRequest();
				response.setHeader("Expires", "0");
				response.setHeader("Pragma", "no-cache");
				response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
				response.setContentType(mtba.getMimeType());

				// Are we processing a Range request
				if (request.getHeader(HttpHeaders.RANGE) == null) {
					// Not a Range request
					byte[] bytes = mtba.getBytes();
					response.setContentLengthLong(bytes.length);
					out.write(bytes);
					return new ActionReturn(Formats.UTF_8, mtba.getMimeType() , out);
 				} else {
					// A Range request - we use springs HttpRange class
					Resource resource = new ByteArrayResource(mtba.getBytes());
					response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
					response.setContentLengthLong(resource.contentLength());
					response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
					try {
						ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(request);
						ServletServerHttpResponse outputMessage = new ServletServerHttpResponse(response);

						List<HttpRange> httpRanges = inputMessage.getHeaders().getRange();
						ResourceRegionHttpMessageConverter messageConverter = new ResourceRegionHttpMessageConverter();

						if (httpRanges.size() == 1) {
							ResourceRegion resourceRegion = httpRanges.get(0).toResourceRegion(resource);
							messageConverter.write(resourceRegion, MediaType.parseMediaType(mtba.getMimeType()), outputMessage);
						} else {
							messageConverter.write(HttpRange.toResourceRegions(httpRanges, resource), MediaType.parseMediaType(mtba.getMimeType()), outputMessage);
						}
					} catch (IllegalArgumentException iae) {
						response.setHeader("Content-Range", "bytes */" + resource.contentLength());
						response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
						log.warn("Name pronunciation request failed to send the requested range for {}, {}", ref.getReference(), iae.getMessage());
					}
				}
			} catch (Exception e) {
				throw new EntityException("Name pronunciation request failed, " + e.getMessage(), ref.getReference());
			}
		}
		return null;
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
