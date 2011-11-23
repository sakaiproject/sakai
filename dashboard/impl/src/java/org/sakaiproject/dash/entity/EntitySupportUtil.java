/**
 * 
 */
package org.sakaiproject.dash.entity;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.dash.entity.AssignmentSupport.AssignmentEntityType;
import org.sakaiproject.dash.entity.AssignmentSupport.AssignmentNewEventProcessor;
import org.sakaiproject.dash.entity.AssignmentSupport.AssignmentRemoveEventProcessor;
import org.sakaiproject.dash.entity.AssignmentSupport.AssignmentUpdateAccessEventProcessor;
import org.sakaiproject.dash.entity.AssignmentSupport.AssignmentUpdateEventProcessor;
import org.sakaiproject.dash.entity.AssignmentSupport.AssignmentUpdateOpenDateEventProcessor;
import org.sakaiproject.dash.entity.AssignmentSupport.AssignmentUpdateTitleEventProcessor;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.RepeatingCalendarItem;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;

/**
 * the util class to be used by various EntitySupport classes
 *
 */
public class EntitySupportUtil{
	
	private Log logger = LogFactory.getLog(EntitySupportUtil.class);
	
	static ResourceLoader rl = new ResourceLoader("dash_entity");
	
	protected static DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}
	
	public void init() {
		logger.info("init()");
	}
	
	/**
	 * update NewsItem time and title related to an event
	 * @param event
	 */
	public static void updateNewsItemTimeTitle(Event event) {
		NewsItem nItem = dashboardLogic.getNewsItem(event.getResource());
		if (nItem != null)
		{
			Date newTime = event.getEventTime();
			if ((newTime.getTime() - nItem.getNewsTime().getTime()) > 1000)
			{
				// set values on the item to trigger calculation of new grouping identifier
				String newLabelKey = "dash.updated";
				nItem.setNewsTime(newTime);
				nItem.setNewsTimeLabelKey(newLabelKey);
				// if this is not an update within object creation
				dashboardLogic.reviseNewsItemTitle(event.getResource(), nItem.getTitle(), newTime, newLabelKey, nItem.getGroupingIdentifier());
			}
		}
	}
	
}
