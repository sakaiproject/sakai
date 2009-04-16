/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.event.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.api.Event;

/**
 * <p>
 * BasicEventTracking is a basic implementation of the EventTracking service.
 * </p>
 * <p>
 * Events are just logged, and observers notified.
 * </p>
 */
public abstract class BasicEventTracking extends BaseEventTrackingService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(BasicEventTracking.class);

	/** String used to identify this service in the logs */
	protected static final String m_logId = "EventTracking: ";

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Event post / flow
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Cause this new event to get to wherever it has to go for persistence, etc.
	 * 
	 * @param event
	 *        The new event to post.
	 */
	protected void postEvent(Event event)
	{
		String reportId = null;
		if (event.getSessionId() != null)
		{
			reportId = event.getSessionId();
		}
		else
		{
			reportId = "~" + event.getUserId();
		}

		M_log.info(m_logId + reportId + "@" + event);

		// notify observers, sending the event
		notifyObservers(event, true);
	}
}
