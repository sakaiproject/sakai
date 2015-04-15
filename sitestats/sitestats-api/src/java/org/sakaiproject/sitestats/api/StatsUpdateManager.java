/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.api;

import java.util.Date;
import java.util.List;

import org.sakaiproject.event.api.Event;


public interface StatsUpdateManager {
	
	// -----------------------------------------------------------------------
	// --- Configuration methods ---------------------------------------------
	// -----------------------------------------------------------------------
	/**
	 * Enable/disable collect thread (collect sakai events in real time at a specified (small) time interval).
	 * For medium-large installation (more than 8000-10000 users) it is recommended to use a quartz job instead of the collect thread.
	 */
	public void setCollectThreadEnabled(boolean enabled);	
	/** Check whether collect thread is enabled.  */
	public boolean isCollectThreadEnabled();
	
	/**
	 * Time interval at which the collect thread will run and process all the events in queue since the last run.
	 * Please notice that this value cannot be set to high due to memory consumption. For medium-large installation
	 * (more than 8000-10000 users) it is recommended to use a quartz job instead of the collect thread.
	 */
	public void setCollectThreadUpdateInterval(long dbUpdateInterval);
	/** Get the collect thread sleep interval. */
	public long getCollectThreadUpdateInterval();
	

	/** Collect administrator events */
	public boolean isCollectAdminEvents();
	public void setCollectAdminEvents(boolean value);
	
	/** Collect events ONLY for sites with SiteStats tool? */
	public boolean isCollectEventsForSiteWithToolOnly();
	public void setCollectEventsForSiteWithToolOnly(boolean value);
	
	
	// -----------------------------------------------------------------------
	// --- Event collecting/aggregation methods ------------------------------
	// -----------------------------------------------------------------------
	
	/**
	 * Collect (process) a Sakai event into SiteStats tables.
	 * This method is called by the default quartz job implementation and should be called for every other custom quartz
	 * job implemented to this task (collect events).
	 * @param e An Event (can be built from sql fields using the CustomEventImpl class)
	 * @return True if event was successfully processed and persisted.
	 */
	public boolean collectEvent(Event e);
	/** 
	 * Collect (process) Sakai events into SiteStats tables.
	 * This method is called by the default quartz job implementation and should be called for every other custom quartz
	 * job implemented to this task (collect events).
	 * @param e A List of Event (can be built from sql fields using the CustomEventImpl class)
	 * @return True if events were successfully processed and persisted.
	 */
	public boolean collectEvents(List<Event> events);
	/** 
	 * Collect (process) Sakai events into SiteStats tables.
	 * This method is called by the default quartz job implementation and should be called for every other custom quartz
	 * job implemented to this task (collect events).
	 * @param e An array of Event (can be built from sql fields using the CustomEventImpl class)
	 * @return True if events were successfully processed and persisted.
	 */
	public boolean collectEvents(Event[] events);
	
	/**
	 * Collect Sakai events from SAKAI_EVENTS table for a specific site, between specified dates.
	 * Useful to collect events not processed by SiteStats (occurs when tool is configured to process
	 * events from sites with the tool placed, and the tool was placed some time after site creation).
	 * @param siteId The site id
	 * @param initialDate The initial date of events from SAKAI_EVENT
	 * @param finalDate The final date of events from SAKAI_EVENT
	 * @return The number of processed events
	 */
	public long collectPastSiteEvents(String siteId, Date initialDate, Date finalDate);
	
	/**
	 * Construct a new Event object using specified arguments. Useful for building Events read from SAKAI_EVENT and SAKAI_SESSION table.
	 * @param date The SAKAI_EVENT.EVENT_DATE field
	 * @param event The SAKAI_EVENT.EVENT field
	 * @param ref The SAKAI_EVENT.REF field
	 * @param sessionUser The SAKAI_SESSION.SESSION_USER field
	 * @param sessionId The SAKAI_SESSION.SESSION_ID field
	 * @return An Event object
	 */
	public Event buildEvent(Date date, String event, String ref, String sessionUser, String sessionId);
	
	/**
	 * Construct a new Event object using specified arguments. Useful for building Events read from SAKAI_EVENT and SAKAI_SESSION table.
	 * @param date The SAKAI_EVENT.EVENT_DATE field
	 * @param event The SAKAI_EVENT.EVENT field
	 * @param ref The SAKAI_EVENT.REF field
	 * @param context The SAKAI_EVENT.CONTEXT field
	 * @param sessionUser The SAKAI_SESSION.SESSION_USER field
	 * @param sessionId The SAKAI_SESSION.SESSION_ID field
	 * @return An Event object
	 */
	public Event buildEvent(Date date, String event, String ref, String context, String sessionUser, String sessionId);
	
	
	// -----------------------------------------------------------------------
	// --- QuartzJob runs methods --------------------------------------------
	// -----------------------------------------------------------------------
	/** Save a quartz job run */
	public boolean saveJobRun(JobRun jobRun);
	
	/** Get the latest job run */
	public JobRun getLatestJobRun() throws Exception;
	
	/** Get date of last event processed by the last job run */
	public Date getEventDateFromLatestJobRun() throws Exception;
	
	
	// -----------------------------------------------------------------------
	// --- Metrics methods ---------------------------------------------------
	// -----------------------------------------------------------------------
	/** Returns the total number of events waiting on the queue to be processed */
	public int getQueueSize();
	
	/** Check if real-time thread has work to do */
	public boolean isIdle();
	
	/** Reset metrics: 
	 * <ul>
	 * <li>number of total events processed;</li>
	 * <li>total time in event processing per event;</li>
	 * <li>total time elapsed since start/resetMetrics();</li>
	 * <li>number of events processed per sec;</li>
	 * <li>average time in event processing per event;</li>
	 * </ul>
	 */ 
	public void resetMetrics();
	
	/** Get the total number of events processed */
	public long getNumberOfEventsProcessed();
	
	/** Get the total time, in milliseconds, in event processing */
	public long getTotalTimeInEventProcessing();
	
	/** Get the time that occurred since start/last reset */
	public long getResetTime();
	
	/** Get the total time ellapsed, in milliseconds, since start */
	public long getTotalTimeElapsedSinceReset();
	
	/** Get the average number of events processed per second */
	public double getNumberOfEventsProcessedPerSec();
	
	/** Get the average number of events generated by Sakai per second */
	public double getNumberOfEventsGeneratedPerSec();
	
	/** Get the average time spent, in milliseconds, for event processing per event */
	public long getAverageTimeInEventProcessingPerEvent();
	
	/** Get current metrics as string:
	 * <ul>
	 * <li>number of total events processed so far;</li>
	 * <li>number of events processed per sec;</li>
	 * <li>average time in full event processing per event;</li>
	 * </ul>
	 */
	public String getMetricsSummary(boolean compact);
	
}
