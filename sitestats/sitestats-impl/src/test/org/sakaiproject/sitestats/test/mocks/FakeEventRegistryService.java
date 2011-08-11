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
package org.sakaiproject.sitestats.test.mocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.parser.EventFactory;
import org.sakaiproject.sitestats.api.parser.ToolFactory;
import org.sakaiproject.sitestats.impl.event.EventUtil;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.tool.api.ToolManager;

public class FakeEventRegistryService implements EventRegistryService {
	private SiteService						M_ss;
	public void setSiteService(SiteService M_ss) {
		this.M_ss = M_ss;
	}
	private ToolManager						M_tm;
	public void setToolManager(ToolManager M_tm) {
		this.M_tm = M_tm;
	}

	public List<String> getAnonymousEventIds() {
		return Arrays.asList(FakeData.EVENT_CONTENTDEL);
	}

	public EventFactory getEventFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, ToolInfo> getEventIdToolMap() {
		return FakeData.EVENTID_TOOL_MAP;
	}

	public List<String> getEventIds() {
		return FakeData.EVENTIDS;
	}

	public String getEventName(String eventId) {
		// TODO Auto-generated method stub
		return null;
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

	public ToolFactory getToolFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolIcon(String toolId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolName(String toolId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getServerEventIds() {
		return new ArrayList<String>();
	}

}
