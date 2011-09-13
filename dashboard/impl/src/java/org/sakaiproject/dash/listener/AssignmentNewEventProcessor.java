/**
 * 
 */
package org.sakaiproject.dash.listener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.dash.entity.EntityLinkStrategy;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.event.api.Event;

/**
 * 
 * THIS WILL BE MOVED TO THE assignment PROJECT IN SAKAI CORE ONCE THE INTERFACE IS MOVED TO KERNEL
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
	
	protected EntityBroker entityBroker;
	public void setEntityBroker(EntityBroker entityBroker) {
		this.entityBroker = entityBroker;
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
			
			/** consult in assignment entity provider to get the deepLink **/
			String assignmentUrl = "";
			Map<String, Object> assignData = new HashMap<String, Object>();
		    Map<String, Object> params = new HashMap<String, Object>();
		    
		    // get the link as student view first
		    params.put("allowReadAssignment", Boolean.TRUE);
		    params.put("allowAddAssignment", Boolean.FALSE);
		    params.put("allowSubmitAssignment", Boolean.TRUE);
            // pass in the assignment reference to get the assignment data we need
            //ActionReturn ret = entityBroker.executeCustomAction(assn.getReference(), "deepLinkWithPermissions", params, null);
            //if (ret != null && ret.getEntityData() != null) {
            //        Object returnData = ret.getEntityData().getData();
            //        assignData = (Map<String, Object>)returnData;
            //    }
            //assignmentUrl = (String) assignData.get("assignmentUrl");
            
			SourceType sourceType = this.dashboardLogic.getSourceType("assignment");
			if(sourceType == null) {
				sourceType = this.dashboardLogic.createSourceType("assignment", SakaiProxy.PERMIT_ASSIGNMENT_ACCESS, EntityLinkStrategy.SHOW_PROPERTIES);
			}
			
			
			NewsItem newsItem = this.dashboardLogic.createNewsItem(assn.getTitle(), event.getEventTime(), assn.getReference(), assignmentUrl, context, sourceType);
			this.dashboardLogic.createNewsLinks(newsItem);
		
			CalendarItem calendarItem = this.dashboardLogic.createCalendarItem(assn.getTitle(), new Date(assn.getDueTime().getTime()), assn.getReference(), assignmentUrl, context, sourceType);
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
