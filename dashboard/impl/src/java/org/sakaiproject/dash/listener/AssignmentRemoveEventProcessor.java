/**
 * 
 */
package org.sakaiproject.dash.listener;

import org.apache.log4j.Logger;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.event.api.Event;

/**
 * 
 * THIS WILL BE MOVED TO THE assignment PROJECT IN SAKAI CORE ONCE THE INTERFACE IS MOVED TO KERNEL
 */
public class AssignmentRemoveEventProcessor implements EventProcessor {

	private static Logger logger = Logger.getLogger(ContentRemoveEventProcessor.class);
	
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
		
		return SakaiProxy.EVENT_ASSIGNMENT_REMOVE;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
	 */
	public void processEvent(Event event) {
		
		if(logger.isDebugEnabled()) {
			logger.debug("removing calendar links and calendar item for " + event.getResource());
		}
		this.dashboardLogic.removeCalendarItem(event.getResource());
		
		if(logger.isDebugEnabled()) {
			logger.debug("removing news links and news item for " + event.getResource());
		}
		this.dashboardLogic.removeNewsItem(event.getResource());

	}

	public void init() {
		this.dashboardLogic.registerEventProcessor(this);
	}

}
