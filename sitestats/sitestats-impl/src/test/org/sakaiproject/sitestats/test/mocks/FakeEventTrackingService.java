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
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.mockito.Mockito;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventDelayHandler;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.user.api.User;

public abstract class FakeEventTrackingService extends Observable implements EventTrackingService {
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

	public void deleteObserver(Observer o) {
		observers.remove(o);
	}

	public Event newEvent(String event, String resource, boolean modify) {
		FakeEvent fake = Mockito.spy(FakeEvent.class);
		fake.set(event, resource, FakeData.SITE_A_ID, modify, 3);
		return fake;
	}

	public Event newEvent(String event, String resource, boolean modify, int priority) {
		FakeEvent fake = Mockito.spy(FakeEvent.class);
		fake.set(event, resource, FakeData.SITE_A_ID, modify, priority);
		return fake;
	}

	public Event newEvent(String event, String resource, String context, boolean modify, int priority) {
		FakeEvent fake = Mockito.spy(FakeEvent.class);
		fake.set(event, resource, context, modify, priority);
		return fake;
	}

	public Event newEvent(String event, String resource, String context, boolean modify, int priority, LRS_Statement lrsStatement) {
		FakeEvent fake = Mockito.spy(FakeEvent.class);
		fake.set(event, resource, context, modify, priority);
		return fake;
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
