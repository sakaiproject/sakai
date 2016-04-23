/********************************************************************************** 
 * $URL$ 
 * $Id$ 
 *********************************************************************************** 
 * 
 * Copyright (c) 2011 The Sakai Foundation 
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.osedu.org/licenses/ECL-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **********************************************************************************/ 

package org.sakaiproject.dash.entity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.app.SakaiProxy;
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
 * When this moves to content project, eliminate references to SakaiProxy and use sakai services instead.
 */
public class ResourceSupport {
	



	private static Logger logger = LoggerFactory.getLogger(ResourceEntityType.class); 

	ResourceLoader rl = new ResourceLoader("dash_entity");
	
	protected SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	protected DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}

	public static final String RESOURCE_TYPE_IDENTIFIER = "resource";
	public static final String DROPBOX_TYPE_IDENTIFIER = "dropbox";
	
	public void init() {
		logger.info("init()");
		
		this.dashboardLogic.registerEntityType(new ResourceEntityType());
		this.dashboardLogic.registerEntityType(new DropboxEntityType());
		this.dashboardLogic.registerEventProcessor(new ContentNewEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ContentAccessUpdateEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ContentTitleUpdateEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ContentVisibilityUpdateEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ContentAvailableEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ContentReviseEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ContentRemoveEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ContentUpdateEventProcessor());
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


	
	public class ResourceEntityType implements DashboardEntityInfo {
		
		public static final String LABEL_RESOURCE_METADATA = "resource_metadata-label";
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getIdentifier()
		 */
		public String getIdentifier() {
			return RESOURCE_TYPE_IDENTIFIER;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getValues(java.lang.String, java.lang.String)
		 */
		public Map<String, Object> getValues(String entityReference,
				String localeCode) {
			Map<String, Object> values = new HashMap<String, Object>();
			ContentResource resource = (ContentResource) sakaiProxy.getEntity(entityReference);
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
				
				values.put(VALUE_ENTITY_TYPE, RESOURCE_TYPE_IDENTIFIER);
				
				User user = sakaiProxy.getUser(props.getProperty(ResourceProperties.PROP_CREATOR));
				if(user != null) {
					values.put(VALUE_USER_NAME, user.getDisplayName());
				}
				
				// "more-info"
				List<Map<String,String>> infoList = new ArrayList<Map<String,String>>();
				Map<String,String> infoItem = new HashMap<String,String>();
				infoItem.put(VALUE_INFO_LINK_URL, resource.getUrl());
				infoItem.put(VALUE_INFO_LINK_MIMETYPE, resource.getContentType());
				infoItem.put(VALUE_INFO_LINK_TARGET, "_blank");
				// TODO: VALUE_INFO_LINK_TITLE depends on VALUE_INFO_LINK_TARGET. If new window, title might be "View the damned item". Otherwise "Download the stupid thing"? 
				infoItem.put(VALUE_INFO_LINK_TITLE, getMoreInfoTitleForMimetype(rl, resource.getResourceType(), Long.toString(resource.getContentLength())));
				infoList.add(infoItem);
				values.put(VALUE_MORE_INFO, infoList);
			}
			return values ;
		}
		
		/**
		 * get more info title based on content type
		 * @param contentType
		 * @return
		 */
		protected String getMoreInfoTitleForMimetype(ResourceLoader rl, String contentType, String size)
		{
			String rv = null;
			if (contentType.equals(ResourceType.TYPE_UPLOAD))
			{
				// uploaded file type
				rv = rl.getFormattedMessage("resource.info.link.file", new String[]{size});
			}
			else if (contentType.equals(ResourceType.TYPE_TEXT))
			{
				// text type
				rv = rl.getString("resource.info.link.text");
			}
			else if (contentType.equals(ResourceType.TYPE_HTML))
			{
				// html type
				rv = rl.getString("resource.info.link.html");
			}
			else if (contentType.equals(ResourceType.TYPE_URL))
			{
				// url link type
				rv = rl.getString("resource.info.link.url");
			}
			else if (contentType.equals("org.sakaiproject.citation.impl.CitationList"))
			{
				// citation list type
				rv = rl.getString("resource.info.link.citationlist");
			}
			return rv;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getProperties(java.lang.String, java.lang.String)
		 */
		public Map<String, String> getProperties(String entityReference,
				String localeCode) {
			ResourceLoader rl = new ResourceLoader("dash_entity");
			
			
			Map<String, String> props = new HashMap<String, String>();
			
			props.put(LABEL_RESOURCE_METADATA, rl.getString("resource.metadata"));
			
			return props ;
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
			section1.add(LABEL_RESOURCE_METADATA);
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
					String siteId = getSiteIdFromResource(resource);
					if (!sakaiProxy.isSitePublished(siteId))
					{
						// return false if site is unpublished
						return false;
					}
					
					isAvailable = resource.isAvailable();
				}
			}
			return isAvailable;
		}

		/**
		 * extract the site id from the resource object
		 * @param resource
		 * @return
		 */
		private String getSiteIdFromResource(ContentResource resource) {
			ContentCollection collection = resource.getContainingCollection();
			String collectionId = collection.getId();
			while (!ContentHostingService.COLLECTION_SITE.equals(collection.getId()))
			{
				// continue
				collectionId = collection.getId();
				collection = collection.getContainingCollection();
			}
			return collectionId.replaceAll(ContentHostingService.COLLECTION_SITE, "").replaceAll("/", "");
		}
		
		public boolean isUserPermitted(String sakaiUserId, String entityReference,
				String contextId) {
			boolean permitted = false;
			if(this.isAvailable(entityReference)) {
				permitted = sakaiProxy.isUserPermitted(sakaiUserId, SakaiProxy.PERMIT_RESOURCE_ACCESS, entityReference);
			} else {
				permitted = sakaiProxy.isUserPermitted(sakaiUserId, SakaiProxy.PERMIT_RESOURCE_MAINTAIN_1, entityReference)
					|| sakaiProxy.isUserPermitted(sakaiUserId, SakaiProxy.PERMIT_RESOURCE_MAINTAIN_2, entityReference);
			}
			return permitted;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getEventDisplayString(String key, String dflt) {
			ResourceLoader rl = new ResourceLoader("dash_entity");
			return rl.getString(key, dflt);
		}
		
		public String getGroupTitle(int numberOfItems, String contextTitle, String labelKey) {
			String titleKey = "resource.grouped.created";
			if(labelKey != null && ("dash.updated".equals(labelKey) || ("resource.updated".equals(labelKey)))) {
				titleKey = "resource.grouped.updated";
			} 
			ResourceLoader rl = new ResourceLoader("dash_entity");
			Object[] args = new Object[]{ numberOfItems, contextTitle };
			return rl.getFormattedMessage(titleKey, args );
		}

		public String getIconUrl(String subtype) {
			
			String url = sakaiProxy.getContentTypeImageUrl(subtype);
			if(url == null) {
				// url = DEFAULT_OF_SOME_SORT?
			}
			else
			{
				// add the library path
				url = "/library/image/" + url;
			}
			return url ;
		}

		public List<String> getUsersWithAccess(String entityReference) {
			
			boolean isDropboxResource = false;
			Entity entity = sakaiProxy.getEntity(entityReference);
			if(entity != null && entity instanceof ContentResource) {
				isDropboxResource = sakaiProxy.isDropboxResource(entity.getId());
			}
			SortedSet<String> list = new TreeSet<String>();
			if(this.isAvailable(entityReference)) {
				if(!isDropboxResource ) {
					// resource item
					list.addAll(sakaiProxy.getAuthorizedUsers(SakaiProxy.PERMIT_RESOURCE_ACCESS , entityReference));
				}
				else
				{
					// dropbox item
					list.addAll(sakaiProxy.getAuthorizedUsers(SakaiProxy.PERMIT_DROPBOX_MAINTAIN , entityReference));
				}
			} else {
				list.addAll(sakaiProxy.getAuthorizedUsers(SakaiProxy.PERMIT_RESOURCE_MAINTAIN_1 , entityReference));
				list.addAll(sakaiProxy.getAuthorizedUsers(SakaiProxy.PERMIT_RESOURCE_MAINTAIN_2 , entityReference));
			}
			return new ArrayList<String>(list);
		}
	}
	
	public class DropboxEntityType extends ResourceEntityType {

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getIdentifier()
		 */
		public String getIdentifier() {
			return DROPBOX_TYPE_IDENTIFIER;
		}

		public String getGroupTitle(int numberOfItems, String contextTitle, String labelKey) {
			String titleKey = "dropbox.grouped.created";
			if(labelKey != null && ("dash.updated".equals(labelKey) || ("dropbox.updated".equals(labelKey)))) {
				titleKey = "dropbox.grouped.updated";
			} 
			ResourceLoader rl = new ResourceLoader("dash_entity");
			Object[] args = new Object[]{ numberOfItems, contextTitle };
			return rl.getFormattedMessage(titleKey, args );
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
			
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}

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
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}
			
			Entity entity = sakaiProxy.getEntity(event.getResource());
			if(entity != null && entity instanceof ContentResource) {
				ContentResource resource = (ContentResource) entity;
				addContentNewsItem(event, resource);
				//				if (resource.isHidden())
				//				{
				//					
				//					// hide the resource, the need to remove all links in dashboard
				//					//dashboardLogic.removeNewsItem(event.getResource());
				//				}
				//				else
				//				{
				//					// add the links to dashboard
				//					addContentNewsItem(event, resource);
				//				}
			}
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

			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}
			Entity entity = sakaiProxy.getEntity(event.getResource());
			if(entity != null && entity instanceof ContentResource) {
				ContentResource resource = (ContentResource) entity;
				
				addContentNewsItem(event, resource);
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
			
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}
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
			
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}
			
			Entity entity = sakaiProxy.getEntity(event.getResource());
			if(entity != null && entity instanceof ContentResource) {
				if(logger.isDebugEnabled()) {
					logger.debug("updating links to resource " + entity.getId());
				}
				NewsItem newsItem = dashboardLogic.getNewsItem(event.getResource());
				if(newsItem == null) {
					// create it
					newsItem = addContentNewsItem(event, (ContentResource) entity);
				}
				if(newsItem == null) {
					logger.warn("error processing visibility change -- newsItem cannot be created for entity " + event.getResource());
				} else if(newsItem.getSourceType() == null) {
					logger.warn("error processing visibility change -- newsItem has null sourcetype " + newsItem.toString());
				} else {
					dashboardLogic.updateNewsLinks(event.getResource());
				}
			} else if(entity != null && entity instanceof ContentCollection) {
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
		
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}
			
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
					NewsItem item = dashboardLogic.getNewsItem(event.getResource());
					Date newTime = new Date();
					String labelKey = "dash.updated";
					if(item == null) {
						// TODO: in this case, we need to create a new NewsItem (with label key for "revised" and save it (along with links for every user who can see it)
					} else {
						// set values on the item to trigger calculation of new grouping identifier
						item.setNewsTime(newTime);
						item.setNewsTimeLabelKey(labelKey);
						dashboardLogic.reviseNewsItemTitle(event.getResource(), title, newTime, labelKey, item.getGroupingIdentifier());
					}
				}
			}
		}
	}
	
	/**
	 * Inner Class: ContentUpdateEventProcessor
	 */
	public class ContentUpdateEventProcessor implements EventProcessor {
		
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
			
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}
			
			// update NewsItem Title
			EntitySupportUtil.updateNewsItemTimeTitle(event);
		}

	}
	/**
	 * inner class to handle the logic of adding NewsItem for resource.
	 * @param event
	 * @param resource
	 * @return 
	 */
	private NewsItem addContentNewsItem(Event event, ContentResource resource) {
		NewsItem newsItem = null;
		if (!sakaiProxy.isAttachmentResource(resource.getId()))
		{
			// only when the resource is not attachment
			String contextString = event.getContext();
			Context context = dashboardLogic.getContext(contextString);
			
			String labelKey = "resource.added";
			SourceType sourceType = null;
			boolean isDropboxResource = sakaiProxy.isDropboxResource(resource.getId());
			if(isDropboxResource ) {
				sourceType = dashboardLogic.getSourceType(DROPBOX_TYPE_IDENTIFIER);
				labelKey = "dropbox.added";
			} else {
				sourceType = dashboardLogic.getSourceType(RESOURCE_TYPE_IDENTIFIER);
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
			
			String resourceReference = resource.getReference();
			// treat citation resources as normal content resource
			if(resourceReference != null && resourceReference.startsWith("/citation/content/")) {
				resourceReference = resourceReference.substring("/citation".length());
			}
			if (dashboardLogic.getNewsItem(resourceReference) == null)
			{
				newsItem = dashboardLogic.createNewsItem(title, eventTime, labelKey , resourceReference, context, sourceType, resource.getContentType());
				
				// check whether the associated site is published or not;
				// do not create any links if the site is unpublished
				boolean sitePublished = false;
				
				if(dashboardLogic.isAvailable(newsItem.getEntityReference(), RESOURCE_TYPE_IDENTIFIER)) {
					dashboardLogic.createNewsLinks(newsItem);
					
					// entity is available now -- check for retract date
					Date retractDate = getRetractDate(newsItem.getEntityReference());
					if(retractDate != null && retractDate.after(new Date())) {
						dashboardLogic.scheduleAvailabilityCheck(newsItem.getEntityReference(), RESOURCE_TYPE_IDENTIFIER, retractDate);
					}
				} else {
					// entity is not available now -- check for release date
					Date releaseDate = getReleaseDate(newsItem.getEntityReference());
					if(releaseDate != null && releaseDate.after(new Date())) {
						dashboardLogic.scheduleAvailabilityCheck(newsItem.getEntityReference(), RESOURCE_TYPE_IDENTIFIER, releaseDate);
					}
				}
			}
		}
		return newsItem;
	}
}
