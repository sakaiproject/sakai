/**
 * 
 */
package org.sakaiproject.dash.entity;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.event.api.Event;
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
