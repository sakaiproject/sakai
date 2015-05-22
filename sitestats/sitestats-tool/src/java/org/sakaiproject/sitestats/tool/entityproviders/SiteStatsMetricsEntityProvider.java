/**
 * $URL: https://source.sakaiproject.org/svn/sitestats/trunk/sitestats-impl/src/java/org/sakaiproject/sitestats/impl/entity/SiteStatsMetricsEntityProvider.java $
 * $Id: SiteStatsMetricsEntityProvider.java 105078 2012-02-24 23:00:38Z ottenhoff@longsight.com $
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
package org.sakaiproject.sitestats.tool.entityproviders;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.sitestats.api.StatsUpdateManager;

public class SiteStatsMetricsEntityProvider extends AbstractEntityProvider implements AutoRegisterEntityProvider, ActionsExecutable, Inputable, Outputable, Describeable {

	public static String			PREFIX			= "sitestats-metrics";
	
		
	// --- Spring -------------------------------------------------------------------
	private StatsUpdateManager statsUpdateManager;
	public void setStatsUpdateManager(StatsUpdateManager statsUpdateManager) {
		this.statsUpdateManager = statsUpdateManager;
	}
	
	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}
	
	
	// --- EntityProvider ------------------------------------------------------------
	public String getEntityPrefix() {
		return PREFIX;
	}

	public Object getSampleEntity() {
		// there's none => returning sample string object
		return PREFIX;
	}
	

	// --- Outputable, Inputable -----------------------------------------------------
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
	}

	public String[] getHandledInputFormats() {
		return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
	}
	

	// --- ActionsExecutable (ALL measures) ------------------------------------------
	@EntityCustomAction(action = "get-all-metrics", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getAllMetrics(Search search, Map<String, Object> params) {
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("Number_of_total_events_processed", statsUpdateManager.getNumberOfEventsProcessed());
		map.put("Reset_or_Init_time", (new Date(statsUpdateManager.getResetTime()).toString()) + "( " + statsUpdateManager.getResetTime() + " ms)");
		map.put("Total_time_ellapsed_since_reset", statsUpdateManager.getTotalTimeElapsedSinceReset() + " ms");
		map.put("Total_time_spent_processing_events", statsUpdateManager.getTotalTimeInEventProcessing() + " ms");
		map.put("Number_of_events_processed_per_sec", statsUpdateManager.getNumberOfEventsProcessedPerSec());
		map.put("Number_of_events_generated_in_Sakai_per_sec", statsUpdateManager.getNumberOfEventsGeneratedPerSec());
		map.put("Average_time_spent_in_event_processing_per_event", statsUpdateManager.getAverageTimeInEventProcessingPerEvent() + " ms");
		map.put("Event_queue_size", statsUpdateManager.getQueueSize());
		map.put("Idle", statsUpdateManager.isIdle());
		return new ActionReturn(map);
	}
	
	@EntityCustomAction(action = "reset-all-metrics", viewKey = EntityView.VIEW_LIST)
	public ActionReturn resetAllMetrics(Search search, Map<String, Object> params) {
		if(developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference())) {
			statsUpdateManager.resetMetrics();
			return new ActionReturn(Boolean.TRUE);
		}else{
			throw new SecurityException("Only administrator can perform this action.");
		}
	}
	
	
	// --- ActionsExecutable (individual measures) -----------------------------------
	@EntityCustomAction(action = "get-total-events-processed", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getTotalEventsProcessed(Search search, Map<String, Object> params) {
		return new ActionReturn(statsUpdateManager.getNumberOfEventsProcessed());
	}
	
	@EntityCustomAction(action = "get-reset-time", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getResetTime(Search search, Map<String, Object> params) {
		return new ActionReturn(statsUpdateManager.getResetTime());
	}
	
	@EntityCustomAction(action = "get-time-ellapsed-since-reset", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getTimeEllapsedSinceReset(Search search, Map<String, Object> params) {
		return new ActionReturn(statsUpdateManager.getTotalTimeElapsedSinceReset());
	}
	
	@EntityCustomAction(action = "get-time-spent-processing-events", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getTimeSpentProcessingEvents(Search search, Map<String, Object> params) {
		return new ActionReturn(statsUpdateManager.getTotalTimeInEventProcessing());
	}
	
	@EntityCustomAction(action = "get-events-processed-per-sec", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getEventsProcessedPerSec(Search search, Map<String, Object> params) {
		return new ActionReturn(statsUpdateManager.getNumberOfEventsProcessedPerSec());
	}
	
	@EntityCustomAction(action = "get-events-generated-per-sec", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getEventsGeneratedPerSec(Search search, Map<String, Object> params) {
		return new ActionReturn(statsUpdateManager.getNumberOfEventsGeneratedPerSec());
	}
	
	@EntityCustomAction(action = "get-average-time-event-processing-per-event", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getAverageTimeInEventProcessingPerEvent(Search search, Map<String, Object> params) {
		return new ActionReturn(statsUpdateManager.getAverageTimeInEventProcessingPerEvent());
	}
	
	@EntityCustomAction(action = "get-queue-size", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getQueueSize(Search search, Map<String, Object> params) {
		return new ActionReturn(statsUpdateManager.getQueueSize());
	}
	
	@EntityCustomAction(action = "is-idle", viewKey = EntityView.VIEW_LIST)
	public ActionReturn isIdle(Search search, Map<String, Object> params) {
		return new ActionReturn(statsUpdateManager.isIdle());
	}
	
}
