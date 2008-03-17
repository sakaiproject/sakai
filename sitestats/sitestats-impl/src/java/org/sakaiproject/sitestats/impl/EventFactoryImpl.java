package org.sakaiproject.sitestats.impl;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.sakaiproject.sitestats.api.EventFactory;
import org.sakaiproject.sitestats.api.EventInfo;
import org.xml.sax.Attributes;


public class EventFactoryImpl implements EventFactory, ObjectCreationFactory {
	public EventInfo createEvent(String eventId) {
		return new EventInfoImpl(eventId);
	}

	public Object createObject(Attributes attributes) throws Exception {
		String eventId = attributes.getValue("eventId");
		String selected = attributes.getValue("selected");

		if(eventId == null){
			throw new Exception("Mandatory eventId attribute not present on event tag.");
		}
		EventInfo eventInfo = new EventInfoImpl(eventId);
		eventInfo.setSelected(Boolean.parseBoolean(selected));		
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
