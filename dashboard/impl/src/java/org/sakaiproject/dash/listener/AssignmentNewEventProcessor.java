/**
 * 
 */
package org.sakaiproject.dash.listener;

import java.util.Date;

import org.apache.log4j.Logger;
import org.sakaiproject.assignment.api.Assignment;
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

/**
 * @author jimeng
 *
 */
public class AssignmentNewEventProcessor implements EventProcessor {

	private static Logger logger = Logger.getLogger(AssignmentNewEventProcessor.class);
	
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

		return SakaiProxy.EVENT_ASSIGNMENT_NEW;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
	 */
	public void processEvent(Event event) {
		
		Entity entity = this.sakaiProxy.getEntity(event.getResource());
		
		if(entity != null && entity instanceof Assignment) {
		
			Assignment assn = (Assignment) entity;
			Context context = this.dashboardLogic.getContext(event.getContext());
			if(context == null) {
				context = this.dashboardLogic.createContext(event.getContext());
			}
			Realm realm = this.dashboardLogic.getRealm(event.getContext());
			if(realm == null) {
				realm = this.dashboardLogic.createRealm(event.getContext());
			}
			SourceType sourceType = this.dashboardLogic.getSourceType("assignment");
			if(sourceType == null) {
				sourceType = this.dashboardLogic.createSourceType("assignment");
			}
			
			NewsItem newsItem = this.dashboardLogic.createNewsItem(assn.getTitle(), event.getEventTime(), assn.getReference(), assn.getUrl(), context, realm, sourceType);
			this.dashboardLogic.createNewsLinks(newsItem);
		
			CalendarItem calendarItem = this.dashboardLogic.createCalendarItem(assn.getTitle(), new Date(assn.getDueTime().getTime()), assn.getReference(), assn.getUrl(), context, realm, sourceType);
			this.dashboardLogic.createCalendarLinks(calendarItem);
		} else {
			// for now, let's log the error
			logger.warn("Error trying to process " + this.getEventIdentifer() + " event for entityReference " + event.getResource());
		}
	}

	public void init() {
		
		this.dashboardLogic.registerEventProcessor(this);
	}
}
