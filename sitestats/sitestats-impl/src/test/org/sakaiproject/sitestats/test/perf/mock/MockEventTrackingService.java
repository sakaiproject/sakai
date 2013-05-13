package org.sakaiproject.sitestats.test.perf.mock;

import java.util.Observer;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventDelayHandler;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;

public class MockEventTrackingService implements EventTrackingService {

	@Override
	public Event newEvent(String event, String resource, boolean modify) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Event newEvent(String event, String resource, boolean modify,
			int priority) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Event newEvent(String event, String resource, String context,
			boolean modify, int priority) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void post(Event event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void post(Event event, UsageSession session) {
		// TODO Auto-generated method stub

	}

	@Override
	public void post(Event event, User user) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addObserver(Observer observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addPriorityObserver(Observer observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addLocalObserver(Observer observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteObserver(Observer observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setEventDelayHandler(EventDelayHandler handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delay(Event event, Time fireTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cancelDelays(String resource) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cancelDelays(String resource, String event) {
		// TODO Auto-generated method stub

	}

}
