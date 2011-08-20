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
 * The remove any event listener for all Announcement events
 * @author
 *
 */
public class AnnouncementRemoveAnyEventProcessor implements EventProcessor {

	private static Logger logger = Logger.getLogger(AnnouncementNewEventProcessor.class);
	
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

		return SakaiProxy.EVENT_ANNOUNCEMENT_REMOVE_ANY;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
	 */
	public void processEvent(Event event) {
		
		if(logger.isDebugEnabled()) {
			logger.debug("removing news links and news item for " + event.getResource());
		}
		this.dashboardLogic.removeNewsItem(event.getResource());
		
		if(logger.isDebugEnabled()) {
			logger.debug("removing calendar links and news item for " + event.getResource());
		}
		this.dashboardLogic.removeCalendarItem(event.getResource());
	}

	public void init() {
		
		this.dashboardLogic.registerEventProcessor(this);
	}
}
