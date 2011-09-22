/**
 * 
 */
package org.sakaiproject.dash.entity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
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
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;

/**
 * THIS WILL BE MOVED TO THE content PROJECT IN SAKAI CORE ONCE THE INTERFACE IS MOVED TO KERNEL
 *
 */
public class ResourceSupport {
	



	private static Log logger = LogFactory.getLog(ResourceEntityType.class); 

	protected SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	protected DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}

	public static final String ENTITY_TYPE_IDENTIFIER = "resource";
	
	public void init() {
		logger.info("init()");
		
		this.dashboardLogic.registerEntityType(new ResourceEntityType());
		this.dashboardLogic.registerEventProcessor(new ContentNewEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ContentAccessUpdateEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ContentTitleUpdateEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ContentVisibilityUpdateEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ContentAvailableEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ContentReviseEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ContentRemoveEventProcessor());
	}
	
	public Date getReleaseDate(String entityReference) {
		Date releaseDate = null;
		if(entityReference == null) {
			logger.warn("isAvailable() invoked with null entity reference");
		} else {
			ContentResource resource = (ContentResource) sakaiProxy.getEntity(entityReference);
			if(resource == null) {
				logger.warn("getReleaseDate() problem retrieving resource with entity reference " + entityReference);
			} else {
				Time releaseTime = resource.getReleaseDate();
				if(releaseTime != null) {
					releaseDate = new Date(releaseTime.getTime());
				}
			}
		}
		return releaseDate;
	}

	public Date getRetractDate(String entityReference) {
		Date retractDate = null;
		if(entityReference == null) {
			logger.warn("isAvailable() invoked with null entity reference");
		} else {
			ContentResource resource = (ContentResource) sakaiProxy.getEntity(entityReference);
			if(resource == null) {
				logger.warn("getRetractDate() problem retrieving resource with entity reference " + entityReference);
			} else {
				Time retractTime = resource.getRetractDate();
				if(retractTime != null) {
					retractDate = new Date(retractTime.getTime());
				}
			}
		}
		return retractDate;
	}


	
	public class ResourceEntityType implements EntityType {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getIdentifier()
		 */
		public String getIdentifier() {
			return ENTITY_TYPE_IDENTIFIER;
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
			ContentResource resource = (ContentResource) sakaiProxy.getEntity(entityReference);
			ResourceLoader rl = new ResourceLoader("dash_entity");
			if(resource != null) {
				ResourceProperties props = resource.getProperties();
				values.put(VALUE_TITLE, props.getProperty(ResourceProperties.PROP_DISPLAY_NAME));

				String descr = props.getProperty(ResourceProperties.PROP_DESCRIPTION);
				if(descr != null && !descr.trim().equals("")) {
					values.put(VALUE_DESCRIPTION, descr);
				}

				try {
					DateFormat df = DateFormat.getDateTimeInstance();
					values.put(VALUE_NEWS_TIME, df.format(new Date(props.getTimeProperty(ResourceProperties.PROP_CREATION_DATE).getTime())));
				} catch (EntityPropertyNotDefinedException e) {
					logger.warn("getValues(" + entityReference + "," + localeCode + ") EntityPropertyNotDefinedException: " + e);
				} catch (EntityPropertyTypeException e) {
					logger.warn("getValues(" + entityReference + "," + localeCode + ") EntityPropertyTypeException: " + e);
				}
				
				values.put(VALUE_ENTITY_TYPE, ENTITY_TYPE_IDENTIFIER);
				
				User user = sakaiProxy.getUser(props.getProperty(ResourceProperties.PROP_CREATOR));
				if(user != null) {
					values.put(VALUE_USER_NAME, user.getDisplayName());
				}
				
				// "more-info"
				List<Map<String,String>> infoList = new ArrayList<Map<String,String>>();
				Map<String,String> infoItem = new HashMap<String,String>();
				infoItem.put(VALUE_INFO_LINK_URL, resource.getUrl());
				infoItem.put(VALUE_INFO_LINK_SIZE, Long.toString(resource.getContentLength()));
				infoItem.put(VALUE_INFO_LINK_MIMETYPE, resource.getContentType());
				infoItem.put(VALUE_INFO_LINK_TARGET, sakaiProxy.getTargetForMimetype(resource.getContentType()));
				// TODO: VALUE_INFO_LINK_TITLE depends on VALUE_INFO_LINK_TARGET. If new window, title might be "View the damned item". Otherwise "Download the stupid thing"? 
				infoItem.put(VALUE_INFO_LINK_TITLE, rl.getString("resource.info.link"));
				infoList.add(infoItem);
				values.put(VALUE_MORE_INFO, infoList);
				

			}
			return values ;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getProperties(java.lang.String, java.lang.String)
		 */
		public Map<String, String> getProperties(String entityReference,
				String localeCode) {
			ResourceLoader rl = new ResourceLoader("dash_entity");
			
			
			Map<String, String> props = new HashMap<String, String>();
			
			props.put(LABEL_NEWS_TIME, rl.getString("resource.news.time"));
			props.put(LABEL_USER_NAME, rl.getString("resource.user.name"));
			
			return props ;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getAccessUrlTarget(java.lang.String)
		 */
		public String getAccessUrlTarget(String entityReference) {
			// ignored
			return null;
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
			section1.add(VALUE_NEWS_TIME);
			section1.add(VALUE_USER_NAME);
			order.add(section1);
			List<String> section2 = new ArrayList<String>();
			section2.add(VALUE_DESCRIPTION);
			order.add(section2);
			List<String> section3 = new ArrayList<String>();
			section3.add(VALUE_MORE_INFO);
			order.add(section3);

			return order;
		}

		public boolean isAvailable(String entityReference) {
			
			boolean isAvailable = false;
			if(entityReference == null) {
				logger.warn("isAvailable() invoked with null entity reference");
			} else {
				ContentResource resource = (ContentResource) sakaiProxy.getEntity(entityReference);
				if(resource == null) {
					logger.warn("isAvailable() problem retrieving resource with entity reference " + entityReference);
				} else {
					isAvailable = resource.isAvailable();
				}
			}
			return isAvailable;
		}

		public String getString(String key) {
			ResourceLoader rl = new ResourceLoader("dash_entity");
			return rl.getString(key);
		}
	}


	/**
	 * 
	 */
	public class ContentReviseEventProcessor implements EventProcessor {

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_CONTENT_REVISE;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			// TODO Auto-generated method stub
			logger.info("\n\n\n=============================================================\n" + event  
					+ "\n=============================================================\n\n\n");

		}
	}

	/**
	 * 
	 */
	public class ContentAvailableEventProcessor implements EventProcessor {

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_CONTENT_AVAILABLE;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			logger.info("\n\n\n=============================================================\n" + event  
					+ "\n=============================================================\n\n\n");
		}
	}

	/**
	 * 
	 */
	public class ContentNewEventProcessor implements EventProcessor {

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_CONTENT_NEW;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {

			logger.info("\n\n\n=============================================================\n" + event  
					+ "\n=============================================================\n\n\n");
			Entity entity = sakaiProxy.getEntity(event.getResource());
			if(entity != null && entity instanceof ContentResource) {
				ContentResource resource = (ContentResource) entity;
				
				if (!sakaiProxy.isAttachmentResource(resource.getId()))
				{
					// only when the resource is not attachment
					Context context = dashboardLogic.getContext(event.getContext());
					if(context == null) {
						context = dashboardLogic.createContext(event.getContext());
					}
					
					SourceType sourceType = dashboardLogic.getSourceType(ENTITY_TYPE_IDENTIFIER);
					if(sourceType == null) {
						sourceType = dashboardLogic.createSourceType(ENTITY_TYPE_IDENTIFIER, SakaiProxy.PERMIT_RESOURCE_ACCESS, EntityLinkStrategy.ACCESS_URL);
					}
					
					ResourceProperties props = resource.getProperties();
					String title = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
					
					Date eventTime = null;
					try {
						// this.eventTime = original.getEventTime();
						// the getEventTime() method did not exist before kernel 1.2
						// so we use reflection
						Method getEventTimeMethod = event.getClass().getMethod("getEventTime", null);
						eventTime = (Date) getEventTimeMethod.invoke(event, null);
					} catch (SecurityException e) {
						logger.warn("Error getting event time " + e);
					} catch (NoSuchMethodException e) {
						logger.warn("Error getting event time " + e);
					} catch (IllegalArgumentException e) {
						logger.warn("Error getting event time " + e);
					} catch (IllegalAccessException e) {
						logger.warn("Error getting event time " + e);
					} catch (InvocationTargetException e) {
						logger.warn("Error getting event time " + e);
					}
					
					if(eventTime == null) {
						try {
							eventTime = new Date(props.getTimeProperty(ResourceProperties.PROP_CREATION_DATE).getTime());
						} catch (EntityPropertyNotDefinedException e) {
							logger.warn("Error getting event time " + e);
						} catch (EntityPropertyTypeException e) {
							logger.warn("Error getting event time " + e);
						}
					}
					
					if(eventTime == null) {
						eventTime = new Date();
					}
					
					NewsItem newsItem = dashboardLogic.createNewsItem(title , eventTime, resource.getReference(), resource.getUrl(), context, sourceType);
					if(dashboardLogic.isAvailable(newsItem.getEntityReference(), ENTITY_TYPE_IDENTIFIER)) {
						dashboardLogic.createNewsLinks(newsItem);
						Date retractDate = getRetractDate(newsItem.getEntityReference());
						if(retractDate != null && retractDate.after(new Date())) {
							dashboardLogic.scheduleAvailabilityCheck(newsItem.getEntityReference(), ENTITY_TYPE_IDENTIFIER, retractDate);
						}
					} else {
						
						Date releaseDate = getReleaseDate(newsItem.getEntityReference());
						if(releaseDate != null && releaseDate.after(new Date())) {
							dashboardLogic.scheduleAvailabilityCheck(newsItem.getEntityReference(), ENTITY_TYPE_IDENTIFIER, releaseDate);
						}
					}
				}
			}
		}
	}

	/**
	 * 
	 */
	public class ContentRemoveEventProcessor implements EventProcessor {

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_CONTENT_REMOVE;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			logger.info("\n\n\n=============================================================\n" + event  
					+ "\n=============================================================\n\n\n");
			if(logger.isDebugEnabled()) {
				logger.debug("removing news links and news item for " + event.getResource());
			}
			dashboardLogic.removeNewsItem(event.getResource());
		}
	}

	public class ContentVisibilityUpdateEventProcessor implements EventProcessor {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_CONTENT_UPD_VISIBILITY;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			logger.info("\n\n\n=============================================================\n" + event  
					+ "\n=============================================================\n\n\n");
			
			Entity entity = sakaiProxy.getEntity(event.getResource());
			if(entity != null && entity instanceof ContentResource) {
				if(logger.isDebugEnabled()) {
					logger.debug("updating links to resource " + entity.getId());
				}
				dashboardLogic.updateNewsLinks(event.getResource());
			} else if(entity!= null && entity instanceof ContentCollection) {
				ContentCollection collection = (ContentCollection) entity;
				List<ContentResource> resources = sakaiProxy.getAllContentResources(collection.getId());
				if(resources != null) {
					for(ContentResource resource : resources) {
						if(logger.isDebugEnabled()) {
							logger.debug("updating links to resources in collection " + collection.getId() + ": " + resource.getId());
						}
						dashboardLogic.updateNewsLinks(resource.getReference());
					}
				}
			}
			
		}
	}

	public class ContentAccessUpdateEventProcessor implements EventProcessor {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_CONTENT_UPD_ACCESS;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
		
			logger.info("\n\n\n=============================================================\n" + event  
					+ "\n=============================================================\n\n\n");
			
			Entity entity = sakaiProxy.getEntity(event.getResource());
			if(entity != null && entity instanceof ContentResource) {
				if(logger.isDebugEnabled()) {
					logger.debug("updating links to resource " + entity.getId());
				}
				dashboardLogic.updateNewsLinks(event.getResource());
			} else if(entity!= null && entity instanceof ContentCollection) {
				ContentCollection collection = (ContentCollection) entity;
				List<ContentResource> resources = sakaiProxy.getAllContentResources(collection.getId());
				if(resources != null) {
					for(ContentResource resource : resources) {
						if(logger.isDebugEnabled()) {
							logger.debug("updating links to resources in collection " + collection.getId() + ": " + resource.getId());
						}
						dashboardLogic.updateNewsLinks(resource.getReference());
					}
				}
			}
		}
	}

	public class ContentTitleUpdateEventProcessor implements EventProcessor {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_CONTENT_UPD_TITLE;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			Entity entity = sakaiProxy.getEntity(event.getResource());
			if(entity != null && entity instanceof ContentResource) {
				ContentResource resource = (ContentResource) entity;
				String title = resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
				if(title != null) {
					dashboardLogic.reviseNewsItemTitle(event.getResource(), title);
				}
			}
		}
	}
}
