/**
 * 
 */
package org.sakaiproject.dash.entity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;

/**
 * THIS WILL BE MOVED TO THE ANNOUNCEMENT PROJECT IN SAKAI CORE ONCE THE INTERFACE IS MOVED TO KERNEL
 *
 */
public class AnnouncementSupport{
	
	private Log logger = LogFactory.getLog(AnnouncementSupport.class);
	
	protected SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	protected DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}

	protected AnnouncementService announcementService;
	public void setAnnouncementService(AnnouncementService announcementService) {
		this.announcementService = announcementService;
	}

	public static final String IDENTIFIER = "announcement";
	
	public void init() {
		logger.info("init()");
		
		this.dashboardLogic.registerEntityType(new AnnouncementEntityType());
		this.dashboardLogic.registerEventProcessor(new AnnouncementNewEventProcessor());
		this.dashboardLogic.registerEventProcessor(new AnnouncementRemoveAnyEventProcessor());
		this.dashboardLogic.registerEventProcessor(new AnnouncementRemoveOwnEventProcessor());
		this.dashboardLogic.registerEventProcessor(new AnnouncementUpdateTitleEventProcessor());
	}
	
	public Date getReleaseDate(String entityReference) {
		Date releaseDate = null;
		AnnouncementMessage announcement = (AnnouncementMessage) sakaiProxy.getEntity(entityReference);
		ResourceProperties props = announcement.getProperties();
		Time releaseTime = null;
		try {
			releaseTime = props.getTimeProperty(SakaiProxy.ANNOUNCEMENT_RELEASE_DATE);
		} catch (EntityPropertyNotDefinedException e) {
			// do nothing -- no release date set, so return null
		} catch (EntityPropertyTypeException e) {
			logger.warn("Problem getting release date for announcement " + entityReference, e);
		}
		if(releaseTime != null) {
			releaseDate = new Date(releaseTime.getTime());
		}
		logger.debug("getReleaseDate() releaseDate: " + releaseDate);
		return releaseDate;
	}

	public Date getRetractDate(String entityReference) {
		Date retractDate = null;
		AnnouncementMessage announcement = (AnnouncementMessage) sakaiProxy.getEntity(entityReference);
		ResourceProperties props = announcement.getProperties();
		
		Time retractTime = null;
		try {
			retractTime = props.getTimeProperty(SakaiProxy.ANNOUNCEMENT_RETRACT_DATE);
		} catch (EntityPropertyNotDefinedException e) {
			// do nothing -- no retract date set, so return null
		} catch (EntityPropertyTypeException e) {
			logger.warn("Problem getting retract date for announcement " + entityReference, e);
		}
		if(retractTime != null) {
			retractDate = new Date(retractTime.getTime());
		}
		logger.debug("getRetractDate() retractDate: " + retractDate);
		return retractDate;
	}
	
	/**
	 * Inner class: AnnouncementEntityType
	 * @author zqian
	 *
	 */
	public class AnnouncementEntityType implements EntityType {
		
		protected static final String LABEL_METADATA = "annc_metadata-label";

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getIdentifier()
		 */
		public String getIdentifier() {
			return IDENTIFIER;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getEntityLinkStrategy(java.lang.String)
		 */
		public EntityLinkStrategy getEntityLinkStrategy(String entityReference) {
			
			return EntityLinkStrategy.SHOW_PROPERTIES;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getValues(java.lang.String, java.lang.String)
		 */
		public Map<String, Object> getValues(String entityReference,
				String localeCode) {
			Map<String, Object> values = new HashMap<String, Object>();
			AnnouncementMessage announcement = (AnnouncementMessage) sakaiProxy.getEntity(entityReference);
			ResourceLoader rl = new ResourceLoader("dash_entity");
			if(announcement != null) {
				AnnouncementMessageHeader header = announcement.getAnnouncementHeader();
				ResourceProperties props = announcement.getProperties();
				values.put(EntityType.VALUE_ENTITY_TYPE, IDENTIFIER);
				DateFormat df = DateFormat.getDateTimeInstance();
				values.put(VALUE_NEWS_TIME, df.format(new Date(header.getDate().getTime())));
				values.put(VALUE_DESCRIPTION, announcement.getBody());
				values.put(VALUE_TITLE, header.getSubject());
				User user = header.getFrom();
				if(user != null) {
					values.put(VALUE_USER_NAME, user.getDisplayName());
				}
				
				// more info
				List<Map<String,String>> infoList = new ArrayList<Map<String,String>>();
				Map<String,String> infoItem = new HashMap<String,String>();
				infoItem.put(VALUE_INFO_LINK_URL, announcement.getUrl());
				infoItem.put(VALUE_INFO_LINK_TITLE, rl.getString("announcement.info.link"));
				infoList.add(infoItem);
				values.put(VALUE_MORE_INFO, infoList);
				
				// "attachments": [ ... ]
				List<Reference> attachments = header.getAttachments();
				if(attachments != null && ! attachments.isEmpty()) {
					List<Map<String,String>> attList = new ArrayList<Map<String,String>>();
					for(Reference ref : attachments) {
						ContentResource resource = (ContentResource) ref.getEntity();
						Map<String, String> attInfo = new HashMap<String, String>();
						attInfo.put(VALUE_ATTACHMENT_TITLE, resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
						attInfo.put(VALUE_ATTACHMENT_URL, resource.getUrl());
						attInfo.put(VALUE_ATTACHMENT_MIMETYPE, resource.getContentType());
						attInfo.put(VALUE_ATTACHMENT_SIZE, Long.toString(resource.getContentLength()));
						attInfo.put(VALUE_ATTACHMENT_TARGET, sakaiProxy.getTargetForMimetype(resource.getContentType()));
						attList.add(attInfo );
					}
					values.put(VALUE_ATTACHMENTS, attList);
				}
			}
			
			return values;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getProperties(java.lang.String, java.lang.String)
		 */
		public Map<String, String> getProperties(String entityReference,
				String localeCode) {
			ResourceLoader rl = new ResourceLoader("dash_entity");
			Map<String, String> props = new HashMap<String, String>();
			props.put(LABEL_METADATA, rl.getString("announcement.metadata"));
			//props.put(LABEL_ATTACHMENTS, rl.getString("announcement.attachments"));
			return props;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getOrder(java.lang.String, java.lang.String)
		 */
		public List<List<String>> getOrder(String entityReference, String localeCode) {
			List<List<String>> order = new ArrayList<List<String>>();
			
			List<String> section0 = new ArrayList<String>();
			section0.add(VALUE_TITLE);
			order.add(section0);
			
			List<String> section1 = new ArrayList<String>();
			section1.add(LABEL_METADATA);
			order.add(section1);
			List<String> section2 = new ArrayList<String>();
			section2.add(VALUE_DESCRIPTION);
			order.add(section2);
			List<String> section3 = new ArrayList<String>();
			section3.add(VALUE_ATTACHMENTS);
			order.add(section3);
			List<String> section4 = new ArrayList<String>();
			section4.add(VALUE_MORE_INFO);
			order.add(section4);
			return order;
		}

		public void init() {
			logger.info("init()");
			dashboardLogic.registerEntityType(this);
		}

		public boolean isAvailable(String entityReference) {
			AnnouncementMessage announcement = (AnnouncementMessage) sakaiProxy.getEntity(entityReference);
			if(announcement != null) {
				if(announcement.getHeader().getDraft()) {
					return false;
				}
				
				Date releaseDate = getReleaseDate(entityReference);
				logger.debug("isAvailable() releaseDate: " + releaseDate);
				if(releaseDate != null && releaseDate.after(new Date())) {
					return false;
				}
				
				Date retractDate = getRetractDate(entityReference);
				logger.debug("isAvailable() retractDate: " + retractDate);
				if(retractDate != null && retractDate.before(new Date())) {
					return false;
				}
				return true;
			}
			return false;
		}
		
		public boolean isUserPermitted(String sakaiUserId, String accessPermission,
				String entityReference, String contextId) {
			// use the message access checking for now
			// use message read permission
			List users = sakaiProxy.unlockUsers(accessPermission, sakaiProxy.getSiteReference(contextId));
			for (Object user : users)
			{
				if (sakaiUserId.equals(((User) user).getId()))
				{
					// user can submit
					return true;
				}
			}
			return false;
		}

		public String getString(String key, String dflt) {
			ResourceLoader rl = new ResourceLoader("dash_entity");
			return rl.getString(key, dflt);
		}

		public String getGroupTitle(int numberOfItems, String contextTitle) {
			ResourceLoader rl = new ResourceLoader("dash_entity");
			Object[] args = new Object[]{ numberOfItems, contextTitle };
			return rl.getFormattedMessage("announcement.grouped.title", args );
	}
	}
	
	/**
	 * Inner class: AnnouncementNewEventProcessor
	 * @author zqian
	 *
	 */
	public class AnnouncementNewEventProcessor implements EventProcessor {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {

			return SakaiProxy.EVENT_ANNOUNCEMENT_NEW;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			String eventId = event.getEvent();
			
			Entity entity = sakaiProxy.getEntity(event.getResource());
			// handle add events
			if(entity != null && entity instanceof AnnouncementMessage) {
			
				AnnouncementMessage ann = (AnnouncementMessage) entity;
				Context context = dashboardLogic.getContext(event.getContext());
				if(context == null) {
					context = dashboardLogic.createContext(event.getContext());
				}
				SourceType sourceType = dashboardLogic.getSourceType(IDENTIFIER);
				if(sourceType == null) {
					sourceType = dashboardLogic.createSourceType(IDENTIFIER, SakaiProxy.PERMIT_ANNOUNCEMENT_ACCESS, EntityLinkStrategy.SHOW_PROPERTIES);
				}
				String accessUrl = ann.getUrl();
				NewsItem newsItem = dashboardLogic.createNewsItem(ann.getAnnouncementHeader().getSubject(), event.getEventTime(), ann.getReference(), context, sourceType);
				if(dashboardLogic.isAvailable(newsItem.getEntityReference(), IDENTIFIER)) {
					dashboardLogic.createNewsLinks(newsItem);
					Date retractDate = getRetractDate(newsItem.getEntityReference());
					if(retractDate != null && retractDate.after(new Date())) {
						dashboardLogic.scheduleAvailabilityCheck(newsItem.getEntityReference(), IDENTIFIER, retractDate);
					}
				} else {
					
					Date releaseDate = getReleaseDate(newsItem.getEntityReference());
					if(releaseDate != null && releaseDate.after(new Date())) {
						dashboardLogic.scheduleAvailabilityCheck(newsItem.getEntityReference(), IDENTIFIER, releaseDate);
					}
				}
				
			} else {
				// for now, let's log the error
				logger.info(eventId + " is not processed for entityReference " + event.getResource());
			}
		}
	}

	/**
	 * Inner class: AnnouncementRemoveAnyEventProcessor
	 * @author zqian
	 *
	 */
	public class AnnouncementRemoveAnyEventProcessor implements EventProcessor {
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {

			return SakaiProxy.EVENT_ANNOUNCEMENT_REMOVE_ANY;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing news links and news item for " + event.getResource());
			}
			dashboardLogic.removeNewsItem(event.getResource());
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing calendar links and news item for " + event.getResource());
			}
			dashboardLogic.removeCalendarItems(event.getResource());
		}
	}
	
	/**
	 * Inner class: AnnouncementRemoveOwnEventProcessor
	 */
	public class AnnouncementRemoveOwnEventProcessor implements EventProcessor {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {

			return SakaiProxy.EVENT_ANNOUNCEMENT_REMOVE_OWN;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing news links and news item for " + event.getResource());
			}
			dashboardLogic.removeNewsItem(event.getResource());
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing calendar links and news item for " + event.getResource());
			}
			dashboardLogic.removeCalendarItems(event.getResource());
		}
	}
	
	/**
	 * Inner Class: AnnouncementUpdateTitleEventProcessor
	 */
	public class AnnouncementUpdateTitleEventProcessor implements EventProcessor {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_ANNC_UPDATE_TITLE;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing calendar links and calendar item for " + event.getResource());
			}
			Entity entity = sakaiProxy.getEntity(event.getResource());
			
			if(entity != null && entity instanceof AnnouncementMessage) {
				// get the assignment entity and its current title
				AnnouncementMessage annc = (AnnouncementMessage) entity;
				
				String title = annc.getAnnouncementHeader().getSubject();
				// update news item title
				dashboardLogic.reviseNewsItemTitle(annc.getReference(), title);
				
				// update calendar item title
				dashboardLogic.reviseCalendarItemsTitle(annc.getReference(), title);
			}
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing news links and news item for " + event.getResource());
			}

		}

	}
}
