/**
 * 
 */
package org.sakaiproject.dash.listener;

import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.event.api.Event;

/**
 * @author jimeng
 *
 */
public class AssignmentNewEventProcessor implements EventProcessor {

	protected DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
	 */
	@Override
	public String getEventIdentifer() {

		return SakaiProxy.EVENT_ASSIGNMENT_NEW;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
	 */
	@Override
	public void processEvent(Event event) {
		
		NewsItem newsItem = this.dashboardLogic.createNewsItem(event.getResource(), event.getEventTime(), event.getContext());
		this.dashboardLogic.createNewsLinks(newsItem);
		
		CalendarItem calendarItem = this.dashboardLogic.createCalendarItem(event.getResource(), event.getContext());
		this.dashboardLogic.createCalendarLinks(calendarItem);
	}

	public void init() {
		
		this.dashboardLogic.registerEventProcessor(this);
	}
}
