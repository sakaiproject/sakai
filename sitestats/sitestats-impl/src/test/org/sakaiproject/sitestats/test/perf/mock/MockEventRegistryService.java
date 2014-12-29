package org.sakaiproject.sitestats.test.perf.mock;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.parser.EventFactory;
import org.sakaiproject.sitestats.api.parser.ToolFactory;

public class MockEventRegistryService implements EventRegistryService {

	@Override
	public Set<String> getEventIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getAnonymousEventIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ToolInfo> getEventRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ToolInfo> getEventRegistry(String siteId,
			boolean onlyAvailableInSite) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEventName(String eventId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolName(String toolId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolIcon(String toolId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ToolInfo> getEventIdToolMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ToolFactory getToolFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EventFactory getEventFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getServerEventIds() {
		// TODO Auto-generated method stub
		return Arrays.asList(new String[]{"site.add", "site.del", "user.add", "user.del", "user.login"});
	}

	@Override
	public boolean isRegisteredEvent(String eventId) {
		return  !getServerEventIds().contains(eventId);
	}

}
