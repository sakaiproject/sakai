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
package org.sakaiproject.sitestats.test.mocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.parser.EventFactory;
import org.sakaiproject.sitestats.api.parser.ToolFactory;
import org.sakaiproject.sitestats.impl.event.EventUtil;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.tool.api.ToolManager;

public abstract class FakeEventRegistryService implements EventRegistryService {
	private SiteService						M_ss;
	public void setSiteService(SiteService M_ss) {
		this.M_ss = M_ss;
	}
	private ToolManager						M_tm;
	public void setToolManager(ToolManager M_tm) {
		this.M_tm = M_tm;
	}
	
	private StatsManager					M_sm;
	public void setStatsManager(StatsManager M_sm) {
		this.M_sm = M_sm;
	}

	public Set<String> getAnonymousEventIds() {
		return Collections.singleton(FakeData.EVENT_CONTENTDEL);
	}

	public Map<String, ToolInfo> getEventIdToolMap() {
		return FakeData.EVENTID_TOOL_MAP;
	}

	public Set<String> getEventIds() {
		if (M_sm.isEnableSitePresences()) {
			Set<String> eventIds = new HashSet<String>(FakeData.EVENTIDS);
			eventIds.add(StatsManager.SITEVISITEND_EVENTID);
			return eventIds;
		}
		return FakeData.EVENTIDS;
	}

	public List<ToolInfo> getEventRegistry() {
		return FakeData.EVENT_REGISTRY;
	}

	public List<ToolInfo> getEventRegistry(String siteId, boolean onlyAvailableInSite) {
//		if(siteId == null || (onlyAvailableInSite && !siteId.equals(FakeData.SITE_B_ID))) {
//			// return the full event registry
//			return FakeData.EVENT_REGISTRY;
//		}else {
//			// return only chat
//			return FakeData.EVENT_REGISTRY_CHAT;
//		}
		if(siteId == null) {
			// return the full event registry
			return FakeData.EVENT_REGISTRY;
		}else if(onlyAvailableInSite) {
			// return the event registry with only tools available in site
			return EventUtil.getIntersectionWithAvailableToolsInSite(M_ss, FakeData.EVENT_REGISTRY, siteId);
		}else{
			// return the event registry with only tools available in (whole) Sakai
			return FakeData.EVENT_REGISTRY;
		}
	}

	public List<String> getServerEventIds() {
		return new ArrayList<String>();
	}

	@Override
	public boolean isRegisteredEvent(String eventId) {
		return getEventIds().contains(eventId);
	}

}
