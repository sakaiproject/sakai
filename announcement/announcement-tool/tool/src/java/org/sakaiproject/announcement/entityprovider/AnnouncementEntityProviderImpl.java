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
 *       http://www.osedu.org/licenses/ECL-2.0
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementMessageHeaderEdit;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
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
public class AnnouncementEntityProviderImpl extends AbstractEntityProvider implements CoreEntityProvider, AutoRegisterEntityProvider, ActionsExecutable, RESTful{

	public final static String ENTITY_PREFIX = "announcement";
	
	private static final String PORTLET_CONFIG_PARAM_MERGED_CHANNELS = "mergedAnnouncementChannels";
	private static final String MOTD_SITEID = "!site";
	private static final String ADMIN_SITEID = "!admin";
	private static final String MOTD_CHANNEL_SUFFIX = "motd";
	public static int DEFAULT_NUM_ANNOUNCEMENTS = 3;
	public static int DEFAULT_DAYS_IN_PAST = 10;
	private static final Log log = LogFactory.getLog(AnnouncementEntityProviderImpl.class);
	private static ResourceLoader rb = new ResourceLoader("announcement");
	private EntityBrokerManager entityBrokerManager;
    
    
    public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager)
    {
       this.entityBrokerManager = entityBrokerManager;
    }
    
	/**
	 * Prefix for this provider
	 */
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	
	/**
	 * Get the list of announcements for a site (or user site, or !site for motd)
	 *
	 * @param siteId - siteId requested, or user site, or !site for motd.
	 * @param params - the raw URL params that were sent, for processing.
	 * @param onlyPublic - only show public announcements
	 * @return
	 */
	public List<?> getAnnouncements(String siteId, Map<String,Object> params, boolean onlyPublic) {
				
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
		
		log.debug("motdView: " + motdView);
		log.debug("siteId: " + siteId);
		log.debug("currentUserId: " + currentUserId);
		log.debug("onlyPublic: " + onlyPublic);
		
		//check current user has annc.read permissions for this site, not for public or motd though
		if(!onlyPublic && !motdView) {
			if(!securityService.unlock(currentUserId, AnnouncementService.SECURE_ANNC_READ, siteService.siteReference(siteId))) {
				throw new SecurityException("You do not have access to site: " + siteId);
			}
		}
		
		// get the channels
		List<String> channels = getChannels(siteId);
		if(channels.size() == 0){
			throw new EntityNotFoundException("No announcement channels found for site: " + siteId, siteId);
		}
		
		log.debug("channels: " + channels.toString());
		log.debug("num channels: " + channels.size());
		
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

		log.debug("numberOfAnnouncements: " + numberOfAnnouncements);
		log.debug("numberOfDaysInThePast: " + numberOfDaysInThePast);
		
		//get the Sakai Time for the given java Date
		Time t = timeService.newTime(getTimeForDaysInPast(numberOfDaysInThePast).getTime());
		
		//get the announcements for each channel
		List<Message> announcements = new ArrayList<Message>();
		
		//for each channel
		for(String channel: channels) {
			try {
				announcements.addAll(announcementService.getMessages(channel, t, numberOfAnnouncements, true, false, onlyPublic));
			} catch (PermissionException e) {
				log.warn("User: " + currentUserId + " does not have access to view the announcement channel: " + channel + ". Skipping...");
			}
		}
		
		log.debug("announcements.size(): " + announcements.size());
		
		//convert raw announcements into decorated announcements
		List<DecoratedAnnouncement> decoratedAnnouncements = new ArrayList<DecoratedAnnouncement>();
	
		for (Message m : announcements) {
			AnnouncementMessage a = (AnnouncementMessage)m;
			try {
				DecoratedAnnouncement da = createDecoratedAnnouncement(a,
					siteTitle);
				decoratedAnnouncements.add(da);
			} catch (Exception e) {
				//this can throw an exception if we are not logged in, ie public, this is fine so just deal with it and continue
				log.info("Exception caught processing announcement: " + m.getId() + " for user: " + currentUserId + ". Skipping...");
			}
		}
		
		//sort
		Collections.sort(decoratedAnnouncements);
		
		//reverse so it is date descending. This could be dependent on a parameter that specifies the sort order
		Collections.reverse(decoratedAnnouncements);
		
		//trim to final number, within bounds of list size.
		if(numberOfAnnouncements > announcements.size()) {
			numberOfAnnouncements = announcements.size();
		}
		decoratedAnnouncements = decoratedAnnouncements.subList(0, numberOfAnnouncements);
		
		
		return decoratedAnnouncements;
	}


	private DecoratedAnnouncement createDecoratedAnnouncement(
			AnnouncementMessage a, String siteTitle) {
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
				DecoratedAttachment decoratedAttachment = new DecoratedAttachment(name, url);
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
	 
	public DecoratedAnnouncement findEntityById(String entityId, String siteId) {
	      AnnouncementMessage tempMsg=null;
	      DecoratedAnnouncement decoratedAnnouncement = new DecoratedAnnouncement();
	      if (entityId != null) {
	         try {
	            AnnouncementChannel announcementChannel = announcementService.getAnnouncementChannel("/announcement/channel/"+siteId+"/main");
	            tempMsg = (AnnouncementMessage)announcementChannel.getMessage(entityId);
	         } catch (IdUnusedException e) {
	            log.error("ID Unused Exception");
	         } catch (PermissionException e) {
	            log.error("ID PermissionException");
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
						log.debug("mergeProp: " + mergeProp);
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
         throw new IllegalArgumentException("siteId must be set in order to get the announcements for a site, via the URL /announcement/site/siteId");
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

   public class DecoratedAttachment {
	  private String id;
	  private String name;
	  private String type;
	  private String url;
	  private String ref;
	  
      public DecoratedAttachment() {
         
      }
      
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

      public String getId() {
         return id;
      }

      public void setId(String id) {
         this.id = id;
      }

      public String getName() {
         return name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public String getType() {
         return type;
      }

      public void setType(String type) {
         this.type = type;
      }

      public void setUrl(String url) {
         this.url = url;
      }

      public String getUrl() {
         return url;
      }

      public void setRef(String ref) {
         this.ref = ref;
      }

      public String getRef() {
         return ref;
      }
   }

	
	public boolean entityExists(String id) {
		return true;
	}
	
	public Object getSampleEntity() {
		return new DecoratedAnnouncement();
	}
	
	
	/**
	 * Unimplemented EntityBroker methods
	 */
	public List<String> findEntityRefs(String[] arg0, String[] arg1, String[] arg2, boolean arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		// TODO Auto-generated method stub
		
	}
	
	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		// TODO Auto-generated method stub
		
	}
	
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON };
	}

	public String[] getHandledInputFormats() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<?> getEntities(EntityReference ref, Search search) {
		return null;
	}

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
	public class DecoratedAnnouncement implements Comparable<Object> {
		private String announcementId;
		private String title;
		private String body;
		private String createdByDisplayName;
		private Date createdOn;
		private List<DecoratedAttachment> attachments;
		private String siteId;
		private String siteTitle;
		private String channel;
		
		public DecoratedAnnouncement(){
		}

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
			return (siteId != null && channel != null && announcementId != null)?siteId+":"+ channel+ ":"+announcementId: null;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getBody() {
			return body;
		}

		public void setBody(String body) {
			this.body = body;
		}

		public String getCreatedByDisplayName() {
			return createdByDisplayName;
		}

		public void setCreatedByDisplayName(String createdByDisplayName) {
			this.createdByDisplayName = createdByDisplayName;
		}

		public Date getCreatedOn() {
			return createdOn;
		}

		public void setCreatedOn(Date createdOn) {
			this.createdOn = createdOn;
		}

		public List<DecoratedAttachment> getAttachments() {
			return attachments;
		}

		public void setAttachments(List<DecoratedAttachment> attachments) {
			this.attachments = attachments;
		}

		public String getSiteId() {
			return siteId;
		}

		public void setSiteId(String siteId) {
			this.siteId = siteId;
		}

		public String getAnnouncementId() {
			return announcementId;
		}

		public void setAnnouncementId(String announcementId) {
			this.announcementId = announcementId;
		}

		public String getSiteTitle() {
			return siteTitle;
		}

		public void setSiteTitle(String siteTitle) {
			this.siteTitle = siteTitle;
		}

		//default sort by date ascending
		public int compareTo(Object o) {
			Date field = ((DecoratedAnnouncement)o).getCreatedOn();
	        int lastCmp = createdOn.compareTo(field);
	        return (lastCmp != 0 ? lastCmp : createdOn.compareTo(field));
		}
		
	}
	private EntityManager entityManager;
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	private SecurityService securityService;
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	private AnnouncementService announcementService;
	public void setAnnouncementService(AnnouncementService announcementService) {
		this.announcementService = announcementService;
	}
	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	private TimeService timeService;
	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}
	
	private ToolManager toolManager;
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

}
