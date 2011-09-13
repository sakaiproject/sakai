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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.dash.listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import org.apache.log4j.Logger;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.dash.entity.EntityLinkStrategy;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.Realm;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;

/**
 * 
 * THIS WILL BE MOVED TO THE content PROJECT IN SAKAI CORE ONCE THE INTERFACE IS MOVED TO KERNEL
 */
public class ContentNewEventProcessor implements EventProcessor {
	
	private static Logger logger = Logger.getLogger(ContentNewEventProcessor.class);
	
	protected DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}

	protected SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy proxy) {
		this.sakaiProxy = proxy;
	}
	
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
		Entity entity = this.sakaiProxy.getEntity(event.getResource());
		if(entity != null && entity instanceof ContentResource) {
			ContentResource resource = (ContentResource) entity;
			
			if (!this.sakaiProxy.isAttachmentResource(resource.getId()))
			{
				// only when the resource is not attachment
				Context context = this.dashboardLogic.getContext(event.getContext());
				if(context == null) {
					context = this.dashboardLogic.createContext(event.getContext());
				}
				
				SourceType sourceType = this.dashboardLogic.getSourceType("resource");
				if(sourceType == null) {
					sourceType = this.dashboardLogic.createSourceType("resource", SakaiProxy.PERMIT_RESOURCE_ACCESS, EntityLinkStrategy.ACCESS_URL);
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(eventTime == null) {
					try {
						eventTime = new Date(props.getTimeProperty(ResourceProperties.PROP_CREATION_DATE).getTime());
					} catch (EntityPropertyNotDefinedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (EntityPropertyTypeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(eventTime == null) {
					eventTime = new Date();
				}
				
				NewsItem newsItem = dashboardLogic.createNewsItem(title , eventTime, resource.getReference(), resource.getUrl(), context, sourceType);
				dashboardLogic.createNewsLinks(newsItem);
			}
		}
	}
	
	public void init() {
		this.dashboardLogic.registerEventProcessor(this);
	}

}
