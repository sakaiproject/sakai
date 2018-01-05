 /**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/announcement/trunk/announcement-tool/tool/src/java/org/sakaiproject/announcement/entityprovider/AnnouncementEntityProviderImpl.java $
 * $Id: AnnouncementEntityProviderImpl.java 87813 2011-01-28 13:42:17Z savithap@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.announcement.entityprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Sampleable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.MergedList;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

/**
 * Allows some basic functions on announcements.
 * Due to limitations of EntityBroker the internal URLs of the announcements service can't be exposed
 * directly, so we have to map them, with assumptions about characters used in IDs. Basically we pack together
 * the {siteId}:{channelId}:{announcementId} into the ID.
 *
 */
@Slf4j
public class AnnouncementEntityProviderImpl extends AbstractEntityProvider implements EntityProvider, AutoRegisterEntityProvider, ActionsExecutable, Outputable, Describeable, Sampleable, Resolvable {

	public final static String ENTITY_PREFIX = "announcement";
	
	private static final String PORTLET_CONFIG_PARAM_MERGED_CHANNELS = "mergedAnnouncementChannels";
	private static final String MOTD_SITEID = "!site";
	private static final String ADMIN_SITEID = "!admin";
	private static final String MOTD_CHANNEL_SUFFIX = "motd";
	public static int DEFAULT_NUM_ANNOUNCEMENTS = 3;
	public static int DEFAULT_DAYS_IN_PAST = 10;
	private static final long MILLISECONDS_IN_DAY = (24 * 60 * 60 * 1000);
	private static ResourceLoader rb = new ResourceLoader("announcement");
    
	/**
	 * Prefix for this provider
	 */
	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
	
	/**
	 * Get the list of announcements for a site (or user site, or !site for motd).
	 * This is aimed to providing a list of announcements similar to those that the synoptic announcement
	 * tool shows. It doesn't show announcements that are outside their date range even if you
	 * have permission to see them (eg from being a maintainer in the site).
	 *
	 * @param siteId - siteId requested, or user site, or !site for motd.
	 * @param params - the raw URL params that were sent, for processing.
	 * @param onlyPublic - only show public announcements
	 * @return
	 */
	private List<?> getAnnouncements(String siteId, Map<String,Object> params, boolean onlyPublic) {
				
		//check if we are loading the MOTD
		boolean motdView = false;
		if(StringUtils.equals(siteId, MOTD_SITEID)) {
			motdView = true;
		}
		
		//get number of announcements and days in the past to show from the URL params, validate and set to 0 if not set or conversion fails.
		//we use this zero value to determine if we need to look up from the tool config, or use the defaults if still not set.
		int numberOfAnnouncements = NumberUtils.toInt((String)params.get("n"), 0);
		int numberOfDaysInThePast = NumberUtils.toInt((String)params.get("d"), 0);
		
		//get currentUserId for permissions checks, although unused for motdView and onlyPublic
		String currentUserId = sessionManager.getCurrentSessionUserId();
		
		if(log.isDebugEnabled()) {
			log.debug("motdView: {}", motdView);
			log.debug("siteId: {}", siteId);
			log.debug("currentUserId: {}", currentUserId);
			log.debug("onlyPublic: {}", onlyPublic);
		}
		
		//check current user has annc.read permissions for this site, not for public or motd though
		if(!onlyPublic && !motdView) {
			if(!securityService.unlock(AnnouncementService.SECURE_ANNC_READ, siteService.siteReference(siteId))) {
				throw new SecurityException("You do not have access to site: " + siteId);
			}
		}
		
		// get the channels
		List<String> channels = getChannels(siteId);
		if(channels.size() == 0){
			throw new EntityNotFoundException("No announcement channels found for site: " + siteId, siteId);
		}
		
		if(log.isDebugEnabled()) {
			log.debug("channels: {}", channels.toString());
			log.debug("num channels: {}", channels.size());
		}
		
		Site site = null;
		String siteTitle = null;
		ToolConfiguration synopticTc = null;
		
		if(!motdView) {
			
			//get site
			try {
				site = siteService.getSite(siteId);
			} catch (IdUnusedException e) {
				throw new IllegalArgumentException("No site found for the siteid:" + siteId + " : "+e.getMessage());
			}
			
			//get properties for synoptic tool in this site
			synopticTc = site.getToolForCommonId("sakai.synoptic.announcement");
			
		}
		
		if(synopticTc != null){
			Properties props = synopticTc.getPlacementConfig();
			if(props.isEmpty()) {
				props = synopticTc.getConfig();
			}
			
			if(props != null){
				
				//only get these from the synoptic tool config if not already set in the URL params
				if (numberOfAnnouncements == 0 && props.get("items") != null) {
					numberOfAnnouncements = getIntegerParameter(props, "items", DEFAULT_NUM_ANNOUNCEMENTS);
				}
				if (numberOfDaysInThePast == 0 && props.get("days") != null) {
					numberOfDaysInThePast = getIntegerParameter(props, "days", DEFAULT_DAYS_IN_PAST);
				}
			}
		}
		
		
		//get site title
		if(!motdView) {
			siteTitle = site.getTitle();
		} else {
			siteTitle = rb.getString("motd.title");
		}
		
		//if numbers are still zero, use the defaults
		if(numberOfAnnouncements == 0) {
			numberOfAnnouncements = DEFAULT_NUM_ANNOUNCEMENTS;
		}
		if(numberOfDaysInThePast == 0) {
			numberOfDaysInThePast = DEFAULT_DAYS_IN_PAST;
		}

		if(log.isDebugEnabled()) {
			log.debug("numberOfAnnouncements: {}", numberOfAnnouncements);
			log.debug("numberOfDaysInThePast: {}", numberOfDaysInThePast);
		}
		
		//get the Sakai Time for the given java Date
		Time t = timeService.newTime(getTimeForDaysInPast(numberOfDaysInThePast).getTime());
		
		//get the announcements for each channel
		List<Message> announcements = new ArrayList<Message>();
		
		//for each channel
		for(String channel: channels) {
			try {
				announcements.addAll(announcementService.getMessages(channel, t, numberOfAnnouncements, true, false, onlyPublic));
			} catch (PermissionException e) {
				log.warn("User: {} does not have access to view the announcement channel: {}. Skipping...", currentUserId, channel);
				//user may not have access to view the channel but get all public messages in this channel
				AnnouncementChannel announcementChannel = (AnnouncementChannel)announcementService.getChannelPublic(channel);
				if(announcementChannel != null){
					List<Message> publicMessages = announcementChannel.getMessagesPublic(null, true);
					for(Message message : publicMessages){
						//Add message only if it is within the time range
						if(isMessageWithinPastNDays(message, numberOfDaysInThePast)){
							announcements.add(message);
						}
					}
				}
			}
		}
		
		if(log.isDebugEnabled()) {
			log.debug("announcements.size(): {}", announcements.size());
		}
		
		//convert raw announcements into decorated announcements
		List<DecoratedAnnouncement> decoratedAnnouncements = new ArrayList<DecoratedAnnouncement>();
	
		for (Message m : announcements) {
			AnnouncementMessage a = (AnnouncementMessage)m;
			if(announcementService.isMessageViewable(a)) {
				try {
					DecoratedAnnouncement da = createDecoratedAnnouncement(a, siteTitle);
					decoratedAnnouncements.add(da);
				} catch (Exception e) {
					//this can throw an exception if we are not logged in, ie public, this is fine so just deal with it and continue
					log.info("Exception caught processing announcement: {} for user: {}. Skipping...", m.getId(), currentUserId);
				}
			}
		}
		
		//sort
		Collections.sort(decoratedAnnouncements);
		
		//reverse so it is date descending. This could be dependent on a parameter that specifies the sort order
		Collections.reverse(decoratedAnnouncements);
		
		//trim to final number, within bounds of list size.
		if(numberOfAnnouncements > decoratedAnnouncements.size()) {
			numberOfAnnouncements = decoratedAnnouncements.size();
		}
		decoratedAnnouncements = decoratedAnnouncements.subList(0, numberOfAnnouncements);
		
		
		return decoratedAnnouncements;
	}

	/**
	 * Checks if the given message was posted in the last N days, where N is the value of the maxDaysInPast
	 * @param message
	 * @param numberOfDaysInPast
	 * @return
	 */
	private boolean isMessageWithinPastNDays(Message message, int numberOfDaysInPast){
		long timeDeltaMSeconds = timeService.newTime().getTime() - message.getHeader().getDate().getTime();
		long numDays = timeDeltaMSeconds / MILLISECONDS_IN_DAY;
		return (numDays <= numberOfDaysInPast);
	}


	private DecoratedAnnouncement createDecoratedAnnouncement(AnnouncementMessage a, String siteTitle) {
		String reference = a.getReference();
		String announcementId = a.getId();
		Reference ref = entityManager.newReference(reference);
		String siteId = ref.getContext();
		String channel = ref.getContainer();

		DecoratedAnnouncement da = new DecoratedAnnouncement(siteId, channel, announcementId);

		da.setTitle(a.getAnnouncementHeader().getSubject());
		da.setBody(a.getBody());
		da.setCreatedByDisplayName(a.getHeader().getFrom().getDisplayName());
		da.setCreatedOn(new Date(a.getHeader().getDate().getTime()));
		da.setSiteId(siteId);
		da.setSiteTitle(siteTitle);
		
		//get attachments
		List<DecoratedAttachment> attachments = new ArrayList<DecoratedAttachment>();
		for (Reference attachment : (List<Reference>) a.getHeader().getAttachments()) {
			String url = attachment.getUrl();
			String name = attachment.getProperties().getPropertyFormatted(attachment.getProperties().getNamePropDisplayName());
			String attachId = attachment.getId();
			String type = attachment.getProperties().getProperty(attachment.getProperties().getNamePropContentType());
			String attachRef = attachment.getReference();								
			DecoratedAttachment decoratedAttachment = new DecoratedAttachment(attachId,name,type,url,attachRef);
			attachments.add(decoratedAttachment);
		}
		da.setAttachments(attachments);
		return da;
	}
	
	/**
	* Return a list of DecoratedAttachment objects
	* @param attachments List of Reference objects
	* @return
	*/
	private List<DecoratedAttachment> decorateAttachments(List<Reference> attachments) {
	      List<DecoratedAttachment> decoAttachments = new ArrayList<DecoratedAttachment>();
	      for(Reference attachment : attachments){
	         DecoratedAttachment da = new DecoratedAttachment();
	         da.setId(Validator.escapeHtml(attachment.getId()));
	         da.setName(Validator.escapeHtml(attachment.getProperties().getPropertyFormatted(attachment.getProperties().getNamePropDisplayName())));
	         da.setType(attachment.getProperties().getProperty(attachment.getProperties().getNamePropContentType()));
	         
	         da.setUrl(attachment.getUrl());
	         da.setRef(attachment.getEntity().getReference());
	         decoAttachments.add(da);
	      }
	      return decoAttachments;
	   }
	
	/**
	 * Gets an announcement based on the id and site
	 * @param entityId	id of the announcement
	 * @param siteId	siteid
	 * @return
	 */
	private DecoratedAnnouncement findEntityById(String entityId, String siteId) {
	      AnnouncementMessage tempMsg=null;
	      DecoratedAnnouncement decoratedAnnouncement = new DecoratedAnnouncement();
	      if (entityId != null) {
	         try {
	            AnnouncementChannel announcementChannel = announcementService.getAnnouncementChannel("/announcement/channel/"+siteId+"/main");
	            tempMsg = (AnnouncementMessage)announcementChannel.getMessage(entityId);
	         } catch (Exception e) {
				log.error("Error finding announcement: {} in site: {}.{}:{}", entityId, siteId, e.getClass(), e.getStackTrace());
	         }
	      }
	      decoratedAnnouncement.setSiteId(tempMsg.getId());
	      decoratedAnnouncement.setBody(tempMsg.getBody());
	      AnnouncementMessageHeader header = tempMsg.getAnnouncementHeader();
	      decoratedAnnouncement.setTitle(header.getSubject());

	      List attachments = header.getAttachments();
	      List<DecoratedAttachment> attachmentUrls = decorateAttachments(attachments);
	      
	      decoratedAnnouncement.setAttachments(attachmentUrls);
	      decoratedAnnouncement.setCreatedOn(new Date(header.getDate().getTime()));
	      decoratedAnnouncement.setCreatedByDisplayName(header.getFrom().getDisplayName());
	      decoratedAnnouncement.setSiteId(siteId);

	      return decoratedAnnouncement;
	   }

	
	/**
	 * Utility routine used to get an integer named value from a map or supply a default value if none is found.
	 */
	
	private int getIntegerParameter(Map<?,?> params, String paramName, int defaultValue) {
		String intValString = (String) params.get(paramName);

		if (StringUtils.trimToNull(intValString) != null) {
			return Integer.parseInt(intValString);
		}
		else {
			return defaultValue;
		}
	}
	
	
	/**
	 * Utility to get the date for n days ago
	 * @param n	number of days in the past
	 * @return
	 */
	private Date getTimeForDaysInPast(int n) {
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -n);
		 
		return cal.getTime();
	}
	
	
	/**
	 * Helper to get the channels for a site. 
	 * <p>
	 * If user site and not superuser, returns all available channels for this user.<br />
	 * If user site and superuser, return all merged channels.<br />
	 * If normal site, returns all merged channels.<br />
	 * If motd site, returns the motd channel.
	 * 
	 * @param siteId
	 * @return
	 */
	private List<String> getChannels(String siteId) {
		
		List<String> channels = new ArrayList<String>();
		
		//if motd
		if(StringUtils.equals(siteId, MOTD_SITEID)) {
			log.debug("is motd site, returning motd channel");
			channels = Collections.singletonList(announcementService.channelReference(siteId, MOTD_CHANNEL_SUFFIX));
			return channels;
		}
		
		//if user site
		if(siteService.isUserSite(siteId)) {
			//if not super user, get all channels this user has access to
			if(!securityService.isSuperUser()){
				log.debug("is user site and not super user, returning all permitted channels");
				channels = Arrays.asList(new MergedList().getAllPermittedChannels(new AnnouncementChannelReferenceMaker()));
				return channels;
			}
		}
		
		//this is either a normal site, or we are a super user
		//so get the merged announcements for this site
		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			//this should have been caught and dealt with already so just return empty list
			return channels;
		}
		if(site != null) {
			ToolConfiguration toolConfig = site.getToolForCommonId("sakai.announcements");
			
			if(toolConfig != null){
				Properties props = toolConfig.getPlacementConfig();
				if(props.isEmpty()) {
					props = toolConfig.getConfig();
				}
				
				if(props != null){

					String mergeProp = (String)props.get(PORTLET_CONFIG_PARAM_MERGED_CHANNELS);
					if(StringUtils.isNotBlank(mergeProp)) {
						log.debug("is normal site or super user, returning all merged channels in this site");
						log.debug("mergeProp: {}", mergeProp);
						channels = Arrays.asList(new MergedList().getChannelReferenceArrayFromDelimitedString(new AnnouncementChannelReferenceMaker().makeReference(siteId), mergeProp));
					} else {
						log.debug("is normal site or super user but no merged channels, using original siteId channel");
						channels = Collections.singletonList(announcementService.channelReference(siteId, SiteService.MAIN_CONTAINER));
					}
				}
			}
		}
		
		return channels;
	}
	
	
	
	
	
	/*
	 * Callback class so that we can form references in a generic way.
	 */
	private final class AnnouncementChannelReferenceMaker implements MergedList.ChannelReferenceMaker {
		public String makeReference(String siteId){
			return announcementService.channelReference(siteId, SiteService.MAIN_CONTAINER);
		}
	}
	
	
	
	/**
	 * site/siteId
	 */
	@EntityCustomAction(action="site",viewKey=EntityView.VIEW_LIST)
	public List<?> getAnnouncementsForSite(EntityView view, Map<String, Object> params) {
		
		//get siteId
		String siteId = view.getPathSegment(2);
		
		//check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException("siteId must be set in order to get the announcements for a site, via the URL /announcement/site/siteId");
		}

		boolean onlyPublic = true;
		
		//check if logged in
		String currentUserId = sessionManager.getCurrentSessionUserId();
		boolean isLoggedIn = StringUtils.isNotBlank(currentUserId);
		boolean canReadThemAnyway = securityService.unlock(AnnouncementService.SECURE_ANNC_READ, siteService.siteReference(siteId));

		if (isLoggedIn || canReadThemAnyway) {
			//not logged in so set flag to just return any public announcements for the site
			onlyPublic = false;
		}

		//check this is a valid site
		if(!siteService.siteExists(siteId)) {
			throw new EntityNotFoundException("Invalid siteId: " + siteId, siteId);
		}

		List<?> l = getAnnouncements(siteId, params, onlyPublic);
		return l;
    }
	
	/**
	 * user
	 */
	@EntityCustomAction(action="user",viewKey=EntityView.VIEW_LIST)
	public List<?> getAnnouncementsForUser(EntityView view, Map<String, Object> params) {

		String userId = sessionManager.getCurrentSessionUserId();
		if (StringUtils.isBlank(userId)) {
			//throw new SecurityException("You must be logged in to get your announcements.");
			return getMessagesOfTheDay(view, params);
		}
		
		//we still need a siteId since Announcements keys it's data on a channel reference created from a siteId.
		//in the case of a user, this is the My Workspace siteId for that user (as an internal user id)
		String siteId = siteService.getUserSiteId(userId);
		if(StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException("No siteId was found for userId: " + userId);
		}
		
		//if admin user, siteID is the admin workspace
		if(StringUtils.equals(userId, userDirectoryService.ADMIN_EID)){
			siteId = ADMIN_SITEID;
		}

		List<?> l = getAnnouncements(siteId, params, false);
		return l;
    }
	
	/**
	 * motd
	 */
	@EntityCustomAction(action="motd",viewKey=EntityView.VIEW_LIST)
	public List<?> getMessagesOfTheDay(EntityView view, Map<String, Object> params) {

		//MOTD announcements are published to a special site
		List<?> l = getAnnouncements(MOTD_SITEID, params, false);
		return l;
	}
	
	// The reason this is EntityView.VIEW_LIST, is we want the URL pattern to be /announcement/channel/.... rather
	// than //announcement/{id}/channel.
	
	/**
	 * This handles announcements, URLs should be like, /announcement/msg/{context}/{channelId}/{announcementId} 
	 * an example would be /announcement/msg/21b1984d-af58-43da-8583-f4adee769aa2/main/5641323b-761a-4a4d-8761-688f4928141b .
	 * Context is normally the site ID and the channelId is normally "main" unless there are multiple channels in a site.
	 * This is an alternative to using the packed IDs.
	 *
	 */
	@EntityCustomAction(action="msg", viewKey=EntityView.VIEW_LIST)
	public DecoratedAnnouncement showAnnouncement(EntityView view, Map<String, Object> params) throws EntityPermissionException {
		
		// This is all more complicated because entitybroker isn't very flexible and announcements can only be loaded once you've got the
		// channel in which they reside first.
		String siteId = view.getPathSegment(2);
		String channelId = view.getPathSegment(3);
		String announcementId = view.getPathSegment(4);
		return getAnnouncement(siteId, channelId, announcementId);
	}
	
	/**
	* message/siteId/EntityID
	*/
	@EntityCustomAction(action="message",viewKey=EntityView.VIEW_LIST)
	public Object getAnnouncementByID(EntityView view, Map<String, Object> params) {
		String siteId = view.getPathSegment(2);
		String msgId = view.getPathSegment(3);
		
		//check siteId supplied
		if (StringUtils.isBlank(siteId)|| StringUtils.isBlank(msgId)) {
			throw new IllegalArgumentException("siteId and msgId must be set in order to get the announcements for a site, via the URL /announcement/message");
		}
		
		boolean onlyPublic = false;
		
		//check if logged in
		String currentUserId = sessionManager.getCurrentSessionUserId();
		if (StringUtils.isBlank(currentUserId)) {
			//not logged in so set flag to just return any public announcements for the site
			onlyPublic = true;
		}
		
		//check this is a valid site
		if(!siteService.siteExists(siteId)) {
			throw new EntityNotFoundException("Invalid siteId: " + siteId, siteId);
		}
		return findEntityById(msgId, siteId);
	}

	/**
	 * Get a DecoratedAnnouncement given the siteId, channelId and announcementId
	 * @param siteId		
	 * @param channelId
	 * @param announcementId
	 * @return
	 */
	private DecoratedAnnouncement getAnnouncement(String siteId, String channelId, String announcementId) {
		if (announcementId == null || announcementId.length() == 0) {
			throw new IllegalArgumentException("You must supply an announcementId");
		}
		if (siteId == null || siteId.length() == 0) {
			throw new IllegalArgumentException("You must supply the siteId.");
		}
		if (channelId == null || channelId.length() == 0) {
			throw new IllegalArgumentException("You must supply an channelId");
		}
		String ref = announcementService.channelReference(siteId, channelId);
		try {
			AnnouncementChannel channel = announcementService.getAnnouncementChannel(ref);
			AnnouncementMessage message = channel.getAnnouncementMessage(announcementId);
			return createDecoratedAnnouncement(message, null);
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Couldn't find: "+ e.getId(), e.getId());
		} catch (PermissionException e) {
			throw new EntityException("You don't have permissions to access this channel.", e.getResource(), 403);
		}
	}

	/**
	 * Model class for an attachment
	 */
	@NoArgsConstructor
	public class DecoratedAttachment {

		@Getter @Setter private String id;
		@Getter @Setter private String name;
		@Getter @Setter private String type;
		@Getter @Setter private String url;
		@Getter @Setter private String ref;
		
		public DecoratedAttachment(String name, String url){
			this.name = name;
			this.url = url;
		}
		
		public DecoratedAttachment(String id, String name, String type, String url, String ref) {
			this.id = id;
			this.name = name;
			this.type = type;
			this.url = url;
			this.setRef(ref);
		}
	}

	
	@Override
	public Object getSampleEntity() {
		return new DecoratedAnnouncement();
	}
	
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON };
	}
	
	@Override
	public Object getEntity(EntityReference ref) {
		// This is the packed ID.
		String id = ref.getId();
		if (id != null) {
			String parts[] = id.split(":");
			if (parts.length == 3) {
				String siteId = parts[0];
				String channelId = parts[1];
				String announcementId = parts[2];
				return getAnnouncement(siteId, channelId, announcementId);
			}
		}
		return null;
	}

	/**
	 * Class to hold only the fields that we want to return
	 */
	@NoArgsConstructor
	public class DecoratedAnnouncement implements Comparable<Object> {
		
		@Getter @Setter private String title;
		@Getter @Setter private String body;
		@Getter @Setter private String createdByDisplayName;
		@Getter @Setter private Date createdOn;
		@Getter @Setter private List<DecoratedAttachment> attachments;
		@Getter @Setter private String siteId;
		@Getter @Setter private String announcementId;
		@Getter @Setter private String siteTitle;
		@Getter @Setter private String channel;

		/**
		 * As we are packing these fields into the ID, we need all of them.
		 * @param siteId
		 * @param channel
		 * @param announcementId
		 */
		public DecoratedAnnouncement(String siteId, String channel, String announcementId) {
			this.siteId = siteId;
			this.channel = channel;
			this.announcementId = announcementId;
		}

		public String getId() {
			return String.format("%s:%s:%s", siteId, channel, announcementId);
		}

		//default sort by date ascending
		public int compareTo(Object o) {
			Date field = ((DecoratedAnnouncement)o).getCreatedOn();
	        int lastCmp = createdOn.compareTo(field);
	        return (lastCmp != 0 ? lastCmp : createdOn.compareTo(field));
		}
		
	}
	
	@Setter
	private EntityManager entityManager;
	
	@Setter
	private SecurityService securityService;
	
	@Setter
	private SessionManager sessionManager;
	
	@Setter
	private SiteService siteService;
	
	@Setter
	private AnnouncementService announcementService;

	@Setter
	private UserDirectoryService userDirectoryService;
	
	@Setter
	private TimeService timeService;
	
	@Setter
	private ToolManager toolManager;
	
}
