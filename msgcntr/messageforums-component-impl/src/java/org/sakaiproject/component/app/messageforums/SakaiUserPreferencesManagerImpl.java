/**
 * 
 */
package org.sakaiproject.component.app.messageforums;

import java.util.TimeZone;

import org.sakaiproject.api.app.messageforums.UserPreferencesManager;
import org.sakaiproject.time.api.TimeService;

/**
 * @author branden
 *
 */
public class SakaiUserPreferencesManagerImpl implements UserPreferencesManager {

	protected TimeService timeService;
	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.messageforums.UserPreferencesManager#getTimeZone()
	 */
	public TimeZone getTimeZone() {
		TimeZone timeZone = timeService.getLocalTimeZone();
		
		if (timeZone == null) {
			timeZone = TimeZone.getDefault();
		}
		
		return timeZone;
	}

}