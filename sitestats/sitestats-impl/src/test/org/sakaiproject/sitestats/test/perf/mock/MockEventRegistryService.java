package org.sakaiproject.sitestats.test.perf.mock;

import org.sakaiproject.sitestats.api.event.EventRegistryService;

import java.util.Arrays;
import java.util.List;

public abstract class MockEventRegistryService implements EventRegistryService {

	@Override
	public List<String> getServerEventIds() {
		return Arrays.asList(new String[]{"site.add", "site.del", "user.add", "user.del", "user.login"});
	}

	@Override
	public boolean isRegisteredEvent(String eventId) {
		return  !getServerEventIds().contains(eventId);
	}

}
