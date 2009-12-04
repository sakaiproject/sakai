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
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.sitestats.api.StatsUpdateManager;

public class SiteStatsMetricsEntityProvider implements AutoRegisterEntityProvider, ActionsExecutable, Inputable, Outputable {
	private static Log				log				= LogFactory.getLog(SiteStatsMetricsEntityProvider.class);
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

	// --- ActionsExecutable ---------------------------------------------------------
	@EntityCustomAction(action = "get-metrics", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getMetrics(Search search, Map<String, Object> params) {
		if(developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference())) {
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("Number_of_total_events_processed", statsUpdateManager.getNumberOfEventsProcessed());
			map.put("Reset_or_Init_time", (new Date(statsUpdateManager.getResetTime()).toString()) + "( " + statsUpdateManager.getResetTime() + " ms)");
			map.put("Total_time_ellapsed_since_reset", statsUpdateManager.getTotalTimeEllapsedSinceReset() + " ms");
			map.put("Total_time_spent_processing_events", statsUpdateManager.getTotalTimeInEventProcessing() + " ms");
			map.put("Number_of_events_processed_per_sec", statsUpdateManager.getNumberOfEventsProcessedPerSec());
			map.put("Number_of_events_generated_in_Sakai_per_sec", statsUpdateManager.getNumberOfEventsGeneratedPerSec());
			map.put("Average_time_spent_in_event_processing_per_event", statsUpdateManager.getAverageTimeInEventProcessingPerEvent() + " ms");
			map.put("Event_queue_size", statsUpdateManager.getQueueSize());
			map.put("Idle", statsUpdateManager.isIdle());
			return new ActionReturn(map);
		}else{
			throw new SecurityException("Only administrator can perform this action.");
		}
	}
	
	@EntityCustomAction(action = "reset-metrics", viewKey = EntityView.VIEW_LIST)
	public ActionReturn resetMetrics(Search search, Map<String, Object> params) {
		if(developerHelperService.isUserAdmin(developerHelperService.getCurrentUserReference())) {
			statsUpdateManager.resetMetrics();
			return new ActionReturn(Boolean.TRUE);
		}else{
			throw new SecurityException("Only administrator can perform this action.");
		}
	}
	
}
