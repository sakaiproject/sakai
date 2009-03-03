package org.sakaiproject.sitestats.test.mocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventDelayHandler;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;

public class FakeEventTrackingService extends Observable implements EventTrackingService {
	private List<Observer>	observers = new ArrayList<Observer>();

	public void addLocalObserver(Observer o) {
		observers.add(o);
	}

	public void addObserver(Observer o) {
		observers.add(o);
	}

	public void addPriorityObserver(Observer o) {
		observers.add(o);
	}

	public void cancelDelays(String arg0) {}

	public void cancelDelays(String arg0, String arg1) {}

	public void delay(Event arg0, Time arg1) {}

	public void deleteObserver(Observer o) {
		observers.remove(o);
	}

	public Event newEvent(String event, String resource, boolean modify) {
		return new FakeEvent(event, resource, modify);
	}

	public Event newEvent(String event, String resource, boolean modify, int priority) {
		return new FakeEvent(event, resource, modify, priority);
	}

	public Event newEvent(String event, String resource, String context, boolean modify, int priority) {
		return new FakeEvent(event, resource, context, modify, priority);
	}

	public void post(Event e) {
		for(Observer o : observers) {
			o.update(this, e);
		}
	}

	public void post(Event e, UsageSession arg1) {
		post(e);
	}

	public void post(Event e, User arg1) {
		post(e);
	}

	public void setEventDelayHandler(EventDelayHandler arg0) {}

}
