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
package org.sakaiproject.sitestats.api.event;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class EventInfo implements Serializable, Cloneable {
	private static final long	serialVersionUID	= 1L;

	@Getter @Setter private String	eventId;
	@Getter @Setter private boolean	selected;
	@Getter @Setter private boolean	anonymous;
	@Getter @Setter private boolean	resolvable = false;

	public EventInfo(String eventId) {
		this.eventId = eventId.trim();
	}

	public EventInfo(EventInfo info) {
		eventId = info.eventId;
		selected = info.selected;
		anonymous = info.anonymous;
		resolvable = info.resolvable;
	}

	@Override
	public EventInfo clone() {
		return new EventInfo(this);
	}

	@Override
	public boolean equals(Object arg0) {
		if(arg0 == null || !(arg0 instanceof EventInfo)) {
			return false;
		} else {
			EventInfo other = (EventInfo) arg0;
			return getEventId().equals(other.getEventId());
		}
	}

	@Override
	public int hashCode() {
		return getEventId().hashCode();
	}

	public String toString() {
		String template = "	-> EventInfo: %s [s:%b] [r:%b] [a:%b]\n";
		return String.format(template, eventId, selected, resolvable, anonymous);
	}
}
