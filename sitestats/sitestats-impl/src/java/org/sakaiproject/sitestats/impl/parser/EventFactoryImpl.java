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
package org.sakaiproject.sitestats.impl.parser;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.parser.EventFactory;
import org.xml.sax.Attributes;


public class EventFactoryImpl implements EventFactory, ObjectCreationFactory {
	public EventInfo createEvent(String eventId) {
		return new EventInfo(eventId);
	}

	public Object createObject(Attributes attributes) throws Exception {
		String eventId = attributes.getValue("eventId");
		String selected = attributes.getValue("selected");
		String anonymous = attributes.getValue("anonymous");

		if(eventId == null){
			throw new Exception("Mandatory eventId attribute not present on event tag.");
		}
		EventInfo eventInfo = new EventInfo(eventId);
		eventInfo.setSelected(Boolean.parseBoolean(selected));	
		eventInfo.setAnonymous(Boolean.parseBoolean(anonymous));	
		return eventInfo;
	}

	public Digester getDigester() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDigester(Digester arg0) {
		// TODO Auto-generated method stub

	}

}
