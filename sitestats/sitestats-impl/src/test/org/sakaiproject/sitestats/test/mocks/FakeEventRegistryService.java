package org.sakaiproject.sitestats.test.mocks;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.parser.EventFactory;
import org.sakaiproject.sitestats.api.parser.ToolFactory;
import org.sakaiproject.sitestats.test.data.FakeData;

public class FakeEventRegistryService implements EventRegistryService {

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
		// TODO Auto-generated method stub
		return null;
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

}
