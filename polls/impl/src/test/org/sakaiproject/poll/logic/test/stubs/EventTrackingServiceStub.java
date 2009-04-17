/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.logic.test.stubs;

import java.util.Observer;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventDelayHandler;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;

public class EventTrackingServiceStub implements EventTrackingService {

	public void addLocalObserver(Observer arg0) {
		// TODO Auto-generated method stub

	}

	public void addObserver(Observer arg0) {
		// TODO Auto-generated method stub

	}

	public void addPriorityObserver(Observer arg0) {
		// TODO Auto-generated method stub

	}

	public void cancelDelays(String arg0) {
		// TODO Auto-generated method stub

	}

	public void cancelDelays(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	public void delay(Event arg0, Time arg1) {
		// TODO Auto-generated method stub

	}

	public void deleteObserver(Observer arg0) {
		// TODO Auto-generated method stub

	}

	public Event newEvent(String arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public Event newEvent(String arg0, String arg1, boolean arg2, int arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	public Event newEvent(String arg0, String arg1, String arg2, boolean arg3,
			int arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	public void post(Event arg0) {
		// TODO Auto-generated method stub

	}

	public void post(Event arg0, UsageSession arg1) {
		// TODO Auto-generated method stub

	}

	public void post(Event arg0, User arg1) {
		// TODO Auto-generated method stub

	}

	public void setEventDelayHandler(EventDelayHandler arg0) {
		// TODO Auto-generated method stub

	}

}
