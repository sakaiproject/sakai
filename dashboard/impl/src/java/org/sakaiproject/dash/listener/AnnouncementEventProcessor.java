/**
 * 
 */
package org.sakaiproject.dash.listener;

import java.util.Date;

import org.apache.log4j.Logger;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.cover.AnnouncementService;
import org.sakaiproject.message.api.*;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.DashboardLogicImpl;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.Realm;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;

/**
 * The event listener for all Announcement events
 * @author
 *
 */
public class AnnouncementEventProcessor implements EventProcessor {

	private static Logger logger = Logger.getLogger(AnnouncementEventProcessor.class);
	
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

		return SakaiProxy.EVENT_ANNOUNCEMENT_NEW;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
	 */
	public void processEvent(Event event) {
		
		String resource =  event.getResource();
		
		String eventId = event.getEvent();
		
		String proxyStringNew = SakaiProxy.EVENT_ANNOUNCEMENT_NEW;
		
		String proxyStringRemove = SakaiProxy.EVENT_ANNOUNCEMENT_REMOVE_OWN;
		
		if (SakaiProxy.EVENT_ANNOUNCEMENT_NEW.equals(eventId))
		{
			Entity entity = this.sakaiProxy.getEntity(event.getResource());
			// handle add events
			if(entity != null && entity instanceof AnnouncementMessage) {
			
				AnnouncementMessage ann = (AnnouncementMessage) entity;
				Context context = this.dashboardLogic.getContext(event.getContext());
				if(context == null) {
					context = this.dashboardLogic.createContext(event.getContext());
				}
				Realm realm = this.dashboardLogic.getRealm(event.getContext());
				if(realm == null) {
					realm = this.dashboardLogic.createRealm(null, event.getContext());
				}
				SourceType sourceType = this.dashboardLogic.getSourceType("announcement");
				if(sourceType == null) {
					sourceType = this.dashboardLogic.createSourceType("announcement");
				}
				
				NewsItem newsItem = this.dashboardLogic.createNewsItem(ann.getAnnouncementHeader().getSubject(), event.getEventTime(), AnnouncementService.getAnnouncementReference(event.getContext()).getReference(), " ", context, realm, sourceType);
				this.dashboardLogic.createNewsLinks(newsItem);
			}
		} else if (SakaiProxy.EVENT_ANNOUNCEMENT_REMOVE_OWN.equals(eventId) || SakaiProxy.EVENT_ANNOUNCEMENT_REMOVE_ANY.equals(eventId)) {
			// handle remove events
			logger.debug("process " + this.getEventIdentifer() + " event for entityReference " + event.getResource());
		} else {
			// for now, let's log the error
			logger.info(eventId + " is not processed for entityReference " + event.getResource());
		}
	}

	public void init() {
		
		this.dashboardLogic.registerEventProcessor(this);
	}
}
