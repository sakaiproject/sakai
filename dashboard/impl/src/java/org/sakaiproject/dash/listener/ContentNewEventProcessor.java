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

import org.apache.log4j.Logger;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.Realm;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;

/**
 * 
 *
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

			Context context = this.dashboardLogic.getContext(event.getContext());
			if(context == null) {
				context = this.dashboardLogic.createContext(event.getContext());
			}
			Realm realm = this.dashboardLogic.getRealm(event.getContext());
			if(realm == null) {
				realm = this.dashboardLogic.createRealm(event.getContext());
			}
			SourceType sourceType = this.dashboardLogic.getSourceType("resource");
			if(sourceType == null) {
				sourceType = this.dashboardLogic.createSourceType("resource");
			}
			
			ResourceProperties props = resource.getProperties();
			String title = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			
			NewsItem newsItem = dashboardLogic.createNewsItem(title , event.getEventTime(), resource.getReference(), resource.getUrl(), context, realm, sourceType);
			dashboardLogic.createNewsLinks(newsItem);
		}
	}
	
	public void init() {
		this.dashboardLogic.registerEventProcessor(this);
	}

}
