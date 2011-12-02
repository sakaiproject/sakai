/**
 * 
 */
package org.sakaiproject.dash.listener;

import org.apache.log4j.Logger;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.site.api.SiteService;

/**
 * 
 * THIS WILL BE MOVED TO THE site-manage PROJECT IN SAKAI CORE ONCE THE INTERFACE IS MOVED TO KERNEL
 */
public class SiteMembershipRemoveEventProcessor implements EventProcessor {

	private static Logger logger = Logger.getLogger(SiteMembershipRemoveEventProcessor.class);
	
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

		return SiteService.EVENT_USER_SITE_MEMBERSHIP_REMOVE;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
	 */
	public void processEvent(Event event) {
		
		// here is the format of resource string. We need to parse it to get uid, role, provide, group id separately
		//uid=8f6f32b0-2a25-4661-9be8-6cda2b479e14
		String uid = null;
		
		String resource = event.getResource();
		String parts[] = resource.split(";");
		if( parts != null) {
			for(String pair : parts) {
				String entries[] = pair.split("=");
				if (entries != null)
				{
					if ("uid".equals(entries[0]))
					{
						uid = entries[1];
					}
				}
			}
		}
		String context = event.getContext();
		if (uid != null && context != null)
		{
			// now that we know the user is removed from the context, we will remove all caldendar item links and news item links
			this.dashboardLogic.removeNewsLinks(uid, context);
			this.dashboardLogic.removeCalendarLinks(uid, context);
		}

	}

	public void init() {
		
		this.dashboardLogic.registerEventProcessor(this);
	}
}
